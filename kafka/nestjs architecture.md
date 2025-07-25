Here’s a modular NestJS-based architecture for your use case, broken down into components that align with your event-driven system involving:

Kafka as the event bus

MongoDB for read model

Periodic email retries to brokers

Optional external system integration (e.g., Lead Management)



---

✅ High-Level NestJS Modules

📦 src
├── app.module.ts
├── kafka
│   ├── kafka.module.ts
│   ├── kafka.service.ts
│   └── kafka.config.ts
├── enrollment-events
│   ├── enrollment-events.module.ts
│   ├── enrollment-events.handler.ts       <-- Consumes base Kafka topic
│   ├── read-model.service.ts              <-- Updates Mongo read model
│   ├── broker-check.service.ts            <-- Determines if broker signature required
│   └── lead-integration-producer.service.ts <-- Publishes to lead-integration topic
├── email
│   ├── email.module.ts
│   ├── email.task-scheduler.ts            <-- Scheduled job
│   ├── email.queue.service.ts             <-- Adds/removes tasks from retry queue
│   ├── email.sender.service.ts            <-- Actual email sender
│   └── email.status.store.ts              <-- Tracks retry count/status in Mongo
├── lead-integration
│   ├── lead-integration.module.ts
│   ├── lead-integration.handler.ts        <-- Consumes lead-integration Kafka topic
│   └── lead-integration.service.ts        <-- Pushes to external system
└── shared
    ├── mongo.module.ts
    ├── logger.service.ts
    └── config.service.ts


---

🧱 Module Responsibilities

1. Kafka Module (kafka.module.ts)

Sets up Kafka client (kafkajs) and makes producer/consumer available.

@Module({
  providers: [KafkaService],
  exports: [KafkaService],
})
export class KafkaModule {}


---

2. Enrollment Events Module

enrollment-events.handler.ts: Consumes enrollment-events topic.

Delegates:

read-model.service.ts: upserts data into Mongo.

broker-check.service.ts: checks if follow-up email is needed.

lead-integration-producer.service.ts: filters and forwards to lead-integration-events.




---

3. Email Module

email.queue.service.ts: Adds/removes broker follow-up tasks to/from queue (Redis, BullMQ, SQS).

email.task-scheduler.ts: Runs periodically (e.g., every hour) using @nestjs/schedule.

email.sender.service.ts: Sends email using a provider (e.g., SES).

email.status.store.ts: Tracks number of retry attempts and status (signature obtained or not).


You can use BullMQ with Redis for a distributed retry queue.


---

4. Lead Integration Module

lead-integration.handler.ts: Subscribes to lead-integration-events topic.

lead-integration.service.ts: Pushes relevant data to CRM or lead management platform via REST API, gRPC, etc.



---

🔁 Kafka Event Flow

[Kafka Topic: enrollment-events]
         ↓
enrollment-events.handler.ts
 ├─→ read-model.service.ts         <-- update Mongo
 ├─→ broker-check.service.ts       <-- add to email queue if needed
 └─→ lead-integration-producer.ts  <-- publish to lead-integration-events


---

🔁 Email Retry Flow

[email.queue.service.ts] <-- task created by broker-check.service.ts
        ↓
[email.task-scheduler.ts] (every hour)
        ↓
[email.sender.service.ts] → send email
        ↓
[email.status.store.ts] → update retry count
        ↓
  If retries > 5 → update Mongo flag to PENDING


---

🧪 Technologies & Packages

Concern	Package

Kafka	kafkajs
MongoDB	@nestjs/mongoose
Queue (Broker Follow-ups)	@nestjs/bull + BullMQ
Scheduler	@nestjs/schedule
Email	nodemailer, or AWS SES SDK
Config Management	@nestjs/config
External API Integration	axios, gRPC, etc.



---

🔐 State Tracking Suggestion

Track retry state per application:

{
  appId: string;
  brokerEmail: string;
  signatureObtained: boolean;
  retryCount: number;
  lastEmailSentAt: Date;
  status: 'PENDING' | 'AWAITING_SIGNATURE' | 'SIGNED';
}


---

🔄 Consumer Group Design

Kafka Topic	Consumer Group	Purpose

enrollment-events	read-model-service	Build Mongo projection
enrollment-events	broker-email-service	Push broker tasks to queue
enrollment-events	lead-producer-service	Publish to lead-integration-events
lead-integration-events	lead-integration-service	Integrate with CRM/LeadSys



---

🧠 Next Steps

I can provide:

Starter code structure (controllers, services, queue)

Detailed implementation of retry logic (BullMQ + NestJS)

Event formats and schemas

Diagrams (e.g., Mermaid, PlantUML)


Would you like me to generate any of these next?

