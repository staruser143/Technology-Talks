**ChannelJobs** are the backbone of the delivery pipeline—an **atomic, per-channel unit of work** that carries everything a worker needs to send a message via Email, SMS, or Push. They decouple orchestration from delivery, enable retries and failover, and let you scale workers independently.

Below is an end‑to‑end view of **what ChannelJobs are, how they flow, and how to implement them** (Kafka and Azure Service Bus variants, with Node/NestJS examples).

---

## 🔎 What is a ChannelJob?

A **ChannelJob** is a **small, immutable message** placed on a channel-specific queue/topic (e.g., `notifications.email`, `notifications.sms`, `notifications.push`). It contains:

- **Envelope metadata**: `messageId`, `attemptId`, `correlationId`, `idempotencyKey`, `tenantId`, `priority`, `createdAt`, `schemaVersion`
- **Channel**: `email | sms | push`
- **Recipient** (`to`): email address, phone (E.164), or push token(s)
- **Rendered content**: HTML/text/subject (email), text (sms), title/body (push)
- **Provider hints & compliance**: region, DLT template ID (India SMS), sender ID, unicode flag, category/topic, tags
- **Retry policy**: initial/backoff/jitter/maxAttempts
- **Scheduling** (optional): `notBeforeAt`, `ttlSeconds`

> **Rule of thumb:** one job per **channel×recipient**. If an email has 3 recipients, create 3 jobs—keeps retries and bounces isolated.

---

## 🧭 Where ChannelJobs fit in the flow

**1) Producer → Orchestrator**
- Your app emits `SendNotification(notificationType, userId, payload, …)`.

**2) Orchestrator**
- Loads `NotificationDefinition` + `ChannelPolicy`, resolves user preferences & locale.
- Renders content via **LiquidJS → MJML → HTML** (for email) and Liquid-only for SMS/Push.
- Creates a `Message` record and **ChannelJobs** for chosen channels.
- Publishes ChannelJobs to the respective channel topic/queue (with idempotency keys).

**3) Channel Workers**
- Each channel has its own worker pool (stateless).
- Worker pulls ChannelJob, applies **routing** to pick a provider adapter (SendGrid/SES/ACS, Twilio/Infobip/ACS, FCM/APNs).
- Sends; updates `DeliveryAttempt` state; if **transient error**, schedules retry; if **permanent error**, marks failed and (optionally) triggers **fallback** to the next channel per policy.

**4) Status Webhooks**
- Providers post back delivery/complaint/bounce.
- Status ingestor updates attempts and (if configured) triggers **post-send fallback** or compensations.

**5) Observability**
- Every step carries `correlationId` to stitch traces, logs, and metrics.

---

## 🧱 ChannelJob Contract (example)

```json
{
  "schemaVersion": 1,
  "messageId": "msg_0a7f…",
  "attemptId": "att_01HZYX…",
  "correlationId": "c-789",
  "tenantId": "t-001",
  "channel": "sms",
  "priority": "high",
  "to": { "phone": "+9198XXXXXXX" },
  "rendered": { "text": "OTP 432198 for ACME. Expires in 5 mins." },
  "providerHints": {
    "region": "IN",
    "category": "transactional",
    "unicode": "false",
    "senderId": "ACMECO",
    "dltTemplateId": "110716XXXXXXX"
  },
  "retryPolicy": { "maxAttempts": 5, "backoff": { "type": "exp-jitter", "baseMs": 2000, "maxMs": 60000 } },
  "schedule": { "notBeforeAt": null, "ttlSeconds": 3600 },
  "idempotencyKey": "msg_0a7f…:sms:+9198XXXXXXX",
  "createdAt": "2025-09-19T05:45:00Z",
  "headers": { "traceparent": "00-…" }
}
```

**Size discipline:** keep jobs small. **Do not embed attachments**; instead, put them in object storage and pass **signed URLs** in `rendered.attachments[]`.

---

## 🔄 State Model (Message & DeliveryAttempt)

**DeliveryAttempt.status** typically evolves as:
- `queued` → `sending` → (`sent` | `failed-permanent` | `failed-transient`)
- If `sent`, provider webhook may later mark `delivered`, `bounced`, `complaint`, `blocked`.
- **Retries**: only on `failed-transient` within retry budget.
- **Fallback**: after permanent failure (or on retry exhaustion), the orchestrator (or a fallback-handler) creates the next channel’s ChannelJob.

Indexes you’ll want:
- `DeliveryAttempt(messageId, channel, status, createdAt)`
- `Message(userId, notificationType, createdAt)`
- Unique index on `DeliveryAttempt.idempotencyKey` (extra guard)

---

## 🧠 Idempotency & Exactly-once Semantics (practical)

- **At-least-once** delivery is the norm; use **idempotencyKey** per job to dedupe in:
  - Worker: check a Redis/DB key before sending.
  - Database: unique constraint on `(idempotencyKey)` for attempts.
- Make provider calls **idempotent** when possible (e.g., use a provider’s `messageId` or `X-Idempotency-Key` header if supported).
- Commit/ack the queue **after** you persist the attempt result (outbox/inbox patterns help).

---

## 🧮 Partitioning & Ordering

- **Kafka**: key jobs by `to` (email/phone) or `userId` to maintain order per recipient and avoid concurrency conflicts, while distributing load over partitions.
- **Azure Service Bus**: use **Sessions** to serialize per `userId`/recipient; otherwise default queues for max throughput.
- Avoid global ordering—it kills throughput.

---

## 🦺 Retry, Backoff, Circuit-breaking

- **Exponential backoff with jitter** per job (keep a retry counter in `DeliveryAttempt`).
- Distinguish **transient** (timeouts, 5xx, throttling) vs **permanent** (invalid address, policy violation).
- **Circuit breaker** per provider: if error rate spikes, open circuit → route to failover provider until half-open test passes.

---

## 🔐 Security & PII

- Minimize PII in jobs; use IDs and fetch PII from a **Profile service** if possible.
- Encrypt stored `to` fields at rest; mask in logs.
- Keep secrets **out** of jobs; workers pull provider credentials from Key Vault/Secrets Manager.
- For India SMS, ensure **DLT template IDs** are included and audited.

---

## ⚙️ Implementation Patterns

### Option A: Kafka-based (fits your KafkaJS experience)

**Topics**
- `notifications.email` | `notifications.sms` | `notifications.push` (N partitions each)
- DLQs: `*.dlq`

**Producer (Orchestrator)**
```ts
// jobs.producer.ts
import { Kafka } from 'kafkajs';

export class JobsProducer {
  private kafka = new Kafka({ clientId: 'notif-orch', brokers: process.env.KAFKA_BROKERS!.split(',') });
  private producer = this.kafka.producer();

  async start() { await this.producer.connect(); }

  async publish(channel: 'email'|'sms'|'push', job: any, key: string) {
    await this.producer.send({
      topic: `notifications.${channel}`,
      messages: [{ key, value: JSON.stringify(job), headers: { schemaVersion: `${job.schemaVersion}`, correlationId: job.correlationId } }]
    });
  }
}
```

**Worker (Email example)**
```ts
// worker-email.ts
import { Kafka } from 'kafkajs';
import { ProviderRouter } from './providers/router';
import { AttemptsStore } from './store';
import { RedisIdem } from './idem';

export class EmailWorker {
  constructor(private router: ProviderRouter, private store: AttemptsStore, private idem: RedisIdem) {}

  async start() {
    const kafka = new Kafka({ clientId: 'email-worker', brokers: process.env.KAFKA_BROKERS!.split(',') });
    const consumer = kafka.consumer({ groupId: 'email-workers' });
    await consumer.subscribe({ topic: 'notifications.email' });

    await consumer.run({
      eachMessage: async ({ message, partition, topic }) => {
        const job = JSON.parse(message.value!.toString());

        const idemKey = `att:${job.idempotencyKey}`;
        if (!(await this.idem.setIfAbsent(idemKey, '1', 24 * 3600))) {
          // already processed
          return;
        }

        const attemptId = job.attemptId;
        await this.store.markSending(attemptId);

        try {
          const adapter = await this.router.pick('email', job.providerHints); // routing rules + health
          const result = await adapter.send({
            from: job.providerHints.from ?? 'no-reply@acme.com',
            to: job.to.email,
            subject: job.rendered.subject,
            html: job.rendered.html,
            text: job.rendered.text
          });

          await this.store.markSent(attemptId, { provider: adapter.name, externalId: result.externalId });
          // actual "delivered" will be updated via webhook later
        } catch (err: any) {
          const { transient, code } = classify(err);
          if (transient && job.retryPolicy /* and attempts left */) {
            const nextAt = computeBackoff(job.retryPolicy, attemptId);
            await this.store.markRetry(attemptId, { code, nextAt });
            // requeue with delay: either via a delay topic or scheduler
            await scheduleRetry(job, nextAt);
          } else {
            await this.store.markFailedPermanent(attemptId, { code, message: err.message });
            // optional: signal fallback orchestrator to enqueue SMS job per ChannelPolicy
            await triggerFallback(job);
          }
        }
      }
    });
  }
}
```

**Retry mechanics on Kafka**
- Use a **delayed-retry pattern**:
  - Either a delay-capable broker (not native in Kafka), or
  - **Retry topics per delay**: `notifications.email.retry.5s`, `.1m`, `.5m` and a small scheduler to move messages when due.
- Alternatively, keep nextRetryAt in DB and have a **retry-puller** publish due jobs back.

**Schema governance**
- Maintain an **AsyncAPI**/JSON-Schema for ChannelJob; use a **schema registry** (if you prefer Avro/Protobuf) to ensure compatibility across services.

---

### Option B: Azure Service Bus (queues)

**Queues**
- `notif-email`, `notif-sms`, `notif-push` (max delivery count, lock duration)
- DLQs are built-in: `notif-email/$DeadLetterQueue`

**Features to leverage**
- **Scheduled messages**: perfect for backoff without extra infra
- **Message sessions**: to order messages by sessionId (e.g., per `userId`/recipient)
- **Deferral** (optional): advanced scenarios

**Producer**
```ts
import { ServiceBusClient } from '@azure/service-bus';

const sb = new ServiceBusClient(process.env.SB_CONNECTION!);
const sender = sb.createSender('notif-email');

await sender.sendMessages({
  body: job,
  subject: 'ChannelJob',
  applicationProperties: { schemaVersion: job.schemaVersion, correlationId: job.correlationId },
  sessionId: job.to.email,                   // maintain per-recipient order
  scheduledEnqueueTimeUtc: job.schedule?.notBeforeAt ? new Date(job.schedule.notBeforeAt) : undefined
});
```

**Worker**
- Receive with **auto-renew lock** while calling provider.
- On transient failure: compute delay and `schedule` a new message to the same queue at `now + backoff`.
- On permanent failure: `deadLetterMessage` or mark in DB and trigger fallback.

---

## 🧰 Routing & Provider Hints

`providerHints` guides the **routing layer**:
- **Region-aware** (IN vs EU vs US)
- **Capabilities** (unicode SMS, attachments, templates)
- **Regulatory** (DLT template ID for India, sender headers)
- **Cost/SLA** (weighted routing or primary/failover)

Routing returns an adapter; adapter maps the **uniform ChannelJob** to the **provider’s API** and normalizes results.

---

## 🧪 Testing Strategy

- **Contract tests**: ChannelJob JSON schema → validate producers & consumers.
- **Golden tests**: given a fixture payload, orchestrator produces N ChannelJobs with expected content.
- **Fault injection**: make adapters randomly fail to verify retries & fallback.
- **Load tests**: ensure worker scaling and provider rate limiting behavior (token bucket).

---

## 🧩 Fallback: who triggers it?

Two viable patterns:

1) **Webhook-driven fallback** (recommended):  
   - If a provider ultimately **bounces** or **blocks**, the **status ingestor** consults ChannelPolicy and **enqueues the next channel’s ChannelJob**.

2) **Worker-driven immediate fallback**:  
   - If the send fails with **permanent** reason (e.g., “unknown recipient”), worker publishes the next channel’s ChannelJob right away.

> Keep fallback generation idempotent (keyed by `messageId:nextChannel:recipient`).

---

## 🧱 Minimal data schema you’ll need

- `Message(id, userId, notificationType, templateKey, locale, correlationId, status, createdAt)`
- `DeliveryAttempt(id, messageId, channel, idempotencyKey, provider, status, attemptNo, nextRetryAt, errorCode, extId, createdAt)`
- Optionally `ChannelJobAudit(id, payloadHash, createdAt)` if you want a full payload audit.

---

## ✅ Implementation Checklist

- [ ] Define **ChannelJob JSON schema** (versioned).
- [ ] Create channel topics/queues + DLQs; decide partition/session keys.
- [ ] Build **JobsProducer** in orchestrator (with OTel headers).
- [ ] Implement **workers** per channel with:
  - [ ] Idempotency check
  - [ ] Provider routing + adapters
  - [ ] Retry & backoff
  - [ ] DLQ on poison/permanent failures
  - [ ] Metrics (success, failure, latency per provider)
- [ ] Implement **status webhook** ingestor → updates attempts and triggers fallback if applicable.
- [ ] Add **rate limiting** and **circuit breaker** in workers.
- [ ] Wire **observability** (spans, logs, correlationId).

---

Would you like this **Kafka-first** or **Azure Service Bus-first**, and which **initial providers** (ACS Email/SMS, SendGrid, Twilio)?
