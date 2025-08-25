

 ✅ For **scheduled background jobs** like sending reminders or checking timeouts, **Kubernetes `CronJob` is the superior, production-grade choice**.

---


## ✅ Recommended: Use Kubernetes `CronJob` for Both Workers

### ✅ 1. Reminder Worker (Every 30 minutes)

```yaml
# k8s/cronjob-reminders.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: send-signature-reminders
  labels:
    app: enrollment-system
spec:
  schedule: "*/30 * * * *"   # Every 30 minutes
  concurrencyPolicy: Forbid  # Prevent overlapping runs
  successfulJobsHistoryLimit: 1
  failedJobsHistoryLimit: 3
  jobTemplate:
    spec:
      template:
        spec:
          restartPolicy: OnFailure
          containers:
            - name: reminder-worker
              image: your-registry/enrollment-workers:v1.2
              imagePullPolicy: IfNotPresent
              command: ["node", "dist/workers/send-reminders.worker.js"]
              env:
                - name: DB_HOST
                  value: "postgres.prod.svc.cluster.local"
                - name: DB_USER
                  valueFrom:
                    secretKeyRef:
                      name: db-creds
                      key: username
                - name: EMAIL_SERVICE_KEY
                  valueFrom:
                    secretKeyRef:
                      name: email-secrets
                      key: api-key
```

---

### ✅ 2. Timeout Worker (Every 2 hours)

```yaml
# k8s/cronjob-timeouts.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: check-signature-timeouts
spec:
  schedule: "0 */2 * * *"   # Every 2 hours
  concurrencyPolicy: Forbid
  jobTemplate:
    spec:
      template:
        spec:
          restartPolicy: OnFailure
          containers:
            - name: timeout-worker
              image: your-registry/enrollment-workers:v1.2
              command: ["node", "dist/workers/check-timeouts.worker.js"]
              env:
                - name: DB_HOST
                  value: "postgres.prod.svc.cluster.local"
                - name: ALERT_WEBHOOK_URL
                  valueFrom:
                    secretKeyRef:
                      name: slack-secrets
                      key: webhook-url
```

---

## ✅ Worker Scripts 

### `send-reminders.worker.ts` — Runs Once

```ts
// src/workers/send-reminders.worker.ts
import { bootstrapWorker } from '../bootstrap';
import { ReminderService } from '../services/reminder.service';

async function run() {
  const app = await bootstrapWorker();
  const service = app.get(ReminderService);
  await service.sendDueReminders();
}

run().catch(err => {
  console.error('Reminder worker failed:', err);
  process.exit(1);
});
```

### `check-timeouts.worker.ts` — Runs Once

```ts
// src/workers/check-timeouts.worker.ts
import { bootstrapWorker } from '../bootstrap';
import { TimeoutService } from '../services/timeout.service';

async function run() {
  const app = await bootstrapWorker();
  const service = app.get(TimeoutService);
  await service.checkExpiredWorkflows();
}

run().catch(err => {
  console.error('Timeout worker failed:', err);
  process.exit(1);
});
```

> ✅ These are **one-time scripts** — no decorators, no long-running servers.

---

## ✅ Benefits of This Design

| Benefit | How Kubernetes `CronJob` Delivers It |
|-------|--------------------------------------|
| ✅ **Exactly-once execution** | `concurrencyPolicy: Forbid` prevents overlap |
| ✅ **Retry on failure** | Kubernetes retries failed jobs |
| ✅ **Clear observability** | `kubectl get jobs`, `kubectl logs job/...` |
| ✅ **No duplicate runs** | Even with multiple API replicas |
| ✅ **Resource efficiency** | Only runs when needed |
| ✅ **Audit trail** | Job history stored in etcd |
| ✅ **Portability** | Works on any Kubernetes cluster |

---

## ✅ Production Best Practices

1. **Use `concurrencyPolicy: Forbid`** — prevents overlapping runs.
2. **Set `startingDeadlineSeconds`** (optional):
   ```yaml
   startingDeadlineSeconds: 300  # If missed, don't run
   ```
3. **Use dedicated Docker image** for workers (lightweight, no API surface).
4. **Log to stdout/stderr** — integrate with Fluentd, Loki, or Azure Monitor.
5. **Monitor with Prometheus**:
   - Export metrics from worker scripts
   - Alert on failed jobs

---

## ✅ Final Architecture

```mermaid
graph TD
    SB[Service Bus] --> Consumer[Enrollment Consumer Pod]
    Consumer --> DB[(PostgreSQL)]

    CJ1[CronJob: Every 30 mins] --> RW[Reminder Worker Pod]
    CJ2[CronJob: Every 2 hours] --> TW[Timeout Worker Pod]

    RW --> DB
    RW --> Email[SendGrid]

    TW --> DB
    TW --> Alert[Slack/Email]

    WebApp --> API[Enrollment API Pod]
    API --> DB
```

- ✅ **Consumer**: Handles new submissions
- ✅ **API**: Captures signature
- ✅ **CronJobs**: Handle time-based logic
- ✅ **DB**: Stores `SignatureWorkflow`, `SignatureReminderLog`

---

## ✅ Summary

> ✅ ** Use Kubernetes `CronJob` for both reminder and timeout workers.**


This gives us:
- **Reliable, scalable, observable scheduling**
- **No vendor lock-in** (runs on any Kubernetes)
- **Clean separation** of concerns
- **Production-grade fault tolerance**


Let me know if you'd like:
- A **Helm chart** with both `CronJob`s
- **GitHub Actions CI/CD** to deploy them
- **Prometheus alerting rules** for failed jobs
- **Worker script templates** with error handling

This is the **correct, scalable, maintainable way** to handle time-based workflows on AKS.
