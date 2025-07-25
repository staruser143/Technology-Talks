Thanks for the detailed use case — it’s a classic event-driven domain with branching logic, and your need for periodic retries, conditional flows, and multiple consumers makes this a great scenario to clarify when to use Kafka, queues, or both.


---

🎯 Your Use Case Summary

🔄 Base events for enrollment application flow are produced to a Kafka topic.

🧱 Read model is built in MongoDB by consuming all enrollment events.

🔁 If the application is submitted by a Broker, send periodic email reminders (max 5) until signature is obtained.

🧩 For some application events, integrate with Lead Management System (one-way integration).



---

🔍 Design Considerations by Scenario

1. Building Read Model from All Events

✅ Use Kafka topic + consumer group

Read model builder is a stateful stream processor (Kafka Streams or consumer + state store).

It replays all events in order and updates the domain MongoDB.

Should be in a dedicated consumer group, isolated from others.


2. Email Signature Follow-up (Periodic Retry, Max 5 Attempts)

🟨 Use Kafka → Task Queue (or Scheduler) → Email Worker

Periodic retry with logic like “send email every N hours until max 5 attempts or signature received” is not a natural fit for Kafka alone.

Why? Kafka doesn't have:

Delay queues / scheduling

Built-in retry counters or expiration

Visibility into "when" to reprocess



✅ Suggested Pattern:

Kafka Topic (enrollment-events)
  |
  |---> Email Coordinator (Kafka Consumer)
          |
          |---> if broker + signature not received:
                  enqueue job to Task Queue (e.g., SQS, Redis Queue)
                           |
                           --> Periodic Worker (cron/scheduled lambda/NestJS scheduler)
                                 |
                                 --> Send Email + Track Retry Count
                                 |
                                 --> Stop after 5 tries or when signature is done

3. Lead Management System Integration

✅ Use a Separate Kafka Topic

Create a new Kafka topic: lead-integration-events

Let a consumer service subscribe only to the relevant events and transform/send to the external system.


Why not use a queue here?

Kafka gives you decoupled processing, durability, replayability, and low latency for integrations.

If the external system is not latency sensitive or can be flaky, use Kafka → queue → integration service as a fallback.



---

🏗️ Recommended Architecture

[Kafka Topic: enrollment-events]
                                     |
        ------------------------------------------------------------------
        |                       |                                |        
[Read Model Builder]   [Email Coordinator]           [Lead Integration Handler]
        |                       |                                |
   (Updates MongoDB)     (Broker Check)                    (Filters events)
                              |                                    |
                          [Queue] (e.g., Redis, SQS)     [Kafka Topic: lead-integration-events]
                              |                                    |
                          [Scheduled Email Sender]     [Integration Worker (push to CRM/LeadSys)]


---

💡 Hybrid Justification (Kafka + Queue)

Scenario	Kafka Topic	Queue

Durable, ordered, multi-consumer event log	✅	❌
Time-based retries (email reminders)	❌	✅
Pushing events to external systems	✅ (with buffer)	Optional, if flaky target



---

📌 Summary

Keep Kafka as your source of truth/event store for all enrollment events.

Use queues for delayed or scheduled processing (e.g., email retries).

For system integrations (like lead management), use separate Kafka topics to fan out from base events.

Consider Kafka + scheduler + state store (Mongo) to track signature status and retry logic, or offload it to a more traditional task processor if scheduling is complex.



---

Would you like a concrete NestJS-based architecture (modules/services) or a Mermaid diagram for this?

