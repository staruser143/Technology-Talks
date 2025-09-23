Absolutely! Let’s walk through **how to use your `EventData` library in a NestJS app that both reads from and writes to Kafka**, with **real, runnable examples**.

We’ll cover:
1. ✅ Publishing events to Kafka (writing)
2. ✅ Consuming events from Kafka (reading) — using your `EventData` library
3. ✅ Wiring up Kafka + `EventDataKafkaModule`
4. ✅ Full working example with controller, producer, consumer

---

## 🧱 Assumptions

- You’ve already built the `@yourorg/event-data` library (as previously designed).
- You’re using `@nestjs/microservices` + Kafka transport.
- Kafka is running locally (e.g., via Docker) on `localhost:9092`.
- You’ve installed:

```bash
npm install @nestjs/microservices kafkajs
npm install --save-dev @types/kafkajs
```

---

## 🚀 Step 1: App Module Setup — Enable Kafka + EventData

```ts
// app.module.ts

import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { EventDataModule } from '@yourorg/event-data'; // your lib
import { KafkaController } from './kafka.controller';
import { KafkaService } from './kafka.service';

@Module({
  imports: [
    // ✅ Enable Kafka support in your EventData library
    EventDataModule.forKafka(),

    // ✅ Register Kafka Client for producing messages
    ClientsModule.register([
      {
        name: 'KAFKA_SERVICE',
        transport: Transport.KAFKA,
        options: {
          client: {
            brokers: ['localhost:9092'],
          },
          consumer: {
            groupId: 'event-data-consumer-group',
          },
        },
      },
    ]),
  ],
  controllers: [KafkaController],
  providers: [KafkaService],
})
export class AppModule {}
```

> 💡 `EventDataModule.forKafka()` provides the `KafkaEventDataInterceptor`, which will auto-wrap incoming Kafka messages into `EventData` instances.

---

## 🧑‍🍳 Step 2: Kafka Producer Service — Writing to Kafka

```ts
// kafka.service.ts

import { Inject, Injectable } from '@nestjs/common';
import { ClientKafka } from '@nestjs/microservices';
import { EventData } from '@yourorg/event-data';

@Injectable()
export class KafkaService {
  constructor(
    @Inject('KAFKA_SERVICE') private readonly kafkaClient: ClientKafka,
  ) {}

  async emitUserCreatedEvent(userId: string, email: string): Promise<void> {
    // You can send plain object — it will be wrapped on consumer side
    const payload = {
      userId,
      email,
      event: 'user.created',
      timestamp: Date.now(),
    };

    // Emit to Kafka topic
    this.kafkaClient.emit('user-events', payload);

    console.log(`✅ Emitted to Kafka: user-events`, payload);
  }

  // Optional: Send an EventData instance (serialized)
  async emitWithMetadata(userId: string, action: string): Promise<void> {
    const eventData = new EventData(
      { userId, action },
      { source: 'api-gateway', traceId: 'abc123' },
    );

    // Serialize EventData to plain object for transport
    const payload = {
      ...eventData.getAll(),
      __meta eventData.getMetadata(), // optional: transport metadata
    };

    this.kafkaClient.emit('user-events', payload);
    console.log(`✅ Emitted EventData-wrapped event`);
  }
}
```

> 💡 Kafka payloads must be **serializable to JSON** — so we send plain objects. The consumer will reconstruct `EventData`.

---

## 👂 Step 3: Kafka Consumer Controller — Reading from Kafka

This is where your `EventData` library shines — incoming messages are auto-wrapped!

```ts
// kafka.controller.ts

import {
  Controller,
  EventPattern,
  Payload,
  OnModuleInit,
} from '@nestjs/microservices';
import { ClientKafka } from '@nestjs/microservices';
import { EventDataMap } from '@yourorg/event-data'; // ← interface for typing
import { Inject } from '@nestjs/common';

@Controller()
export class KafkaController implements OnModuleInit {
  constructor(
    @Inject('KAFKA_SERVICE') private readonly kafkaClient: ClientKafka,
  ) {}

  // Required for Kafka client to subscribe
  async onModuleInit() {
    this.kafkaClient.subscribeToResponseOf('user-events');
  }

  // ✅ Consume Kafka messages — @Payload() is auto-wrapped by KafkaEventDataInterceptor
  @EventPattern('user-events')
  async handleUserEvent(@Payload() eventData: EventDataMap) {
    console.log('📥 Received Kafka event as EventDataMap');

    // Use type-safe accessors
    const userId = eventData.get<string>('userId');
    const email = eventData.get<string>('email');
    const event = eventData.get<string>('event');
    const metadata = eventData.getMetadata();

    console.log(`Handling ${event} for user ${userId} (${email})`);
    console.log('_MetaData:', metadata);

    // Example: route based on event type
    if (event === 'user.created') {
      await this.handleUserCreated(userId, email);
    }
  }

  private async handleUserCreated(userId: string, email: string) {
    // Business logic — e.g., send welcome email, update analytics, etc.
    console.log(`🎉 Welcome email sent to ${email} (User ID: ${userId})`);
  }
}
```

> ✅ Thanks to `EventDataModule.forKafka()`, the `KafkaEventDataInterceptor` automatically:
> - Extracts `message.value`
> - Wraps it in `EventData` (which implements `EventDataMap`)
> - Injects metadata (topic, partition, correlationId, etc.)
> - Replaces `@Payload()` with the wrapped `EventDataMap`

---

## 🧪 Step 4: Trigger Event via HTTP (Optional)

Add an HTTP endpoint to trigger Kafka events — useful for testing.

```ts
// events.controller.ts

import { Controller, Post, Body } from '@nestjs/common';
import { KafkaService } from './kafka.service';

@Controller('events')
export class EventsController {
  constructor(private readonly kafkaService: KafkaService) {}

  @Post('user-created')
  async triggerUserCreated(@Body() body: { userId: string; email: string }) {
    await this.kafkaService.emitUserCreatedEvent(body.userId, body.email);
    return { message: 'Event published to Kafka' };
  }

  @Post('user-action')
  async triggerUserAction(@Body() body: { userId: string; action: string }) {
    await this.kafkaService.emitWithMetadata(body.userId, body.action);
    return { message: 'Event with metadata published' };
  }
}
```

Update `AppModule` to include this controller:

```ts
// app.module.ts
import { EventsController } from './events.controller';

@Module({
  imports: [EventDataModule.forKafka(), ClientsModule.register([...])],
  controllers: [KafkaController, EventsController], // ← added
  providers: [KafkaService],
})
export class AppModule {}
```

---

## 🏁 Step 5: Bootstrap App — main.ts

```ts
// main.ts

import { NestFactory } from '@nestjs/core';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { AppModule } from './app.module';
import { KafkaEventDataInterceptor } from '@yourorg/event-data';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // ✅ Apply Kafka interceptor globally for microservice context
  const kafkaInterceptor = app.get(KafkaEventDataInterceptor);
  app.useGlobalInterceptors(kafkaInterceptor);

  // Start Kafka microservice listener
  app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.KAFKA,
    options: {
      client: {
        brokers: ['localhost:9092'],
      },
      consumer: {
        groupId: 'event-data-consumer-group',
      },
    },
  });

  await app.startAllMicroservices();
  await app.listen(3000);

  console.log('🚀 HTTP server running on http://localhost:3000');
  console.log('👂 Kafka consumer listening for "user-events"');
}

bootstrap();
```

---

## 🧪 Step 6: Test It!

### 1. Start Kafka (if not already running)

```bash
# Using Docker
docker run -p 9092:9092 -p 2181:2181 --env ADVERTISED_HOST=localhost --env ADVERTISED_PORT=9092 spotify/kafka
```

> Or use [confluentinc/cp-kafka](https://hub.docker.com/r/confluentinc/cp-kafka) for production-like setup.

---

### 2. Start Your NestJS App

```bash
npm run start:dev
```

You should see:

```
🚀 HTTP server running on http://localhost:3000
👂 Kafka consumer listening for "user-events"
```

---

### 3. Send Test Event via HTTP

```bash
curl -X POST http://localhost:3000/events/user-created \
  -H "Content-Type: application/json" \
  -d '{"userId": "123", "email": "test@example.com"}'
```

---

### 4. Observe Logs

In your NestJS console:

```
✅ Emitted to Kafka: user-events { userId: '123', email: 'test@example.com', ... }
📥 Received Kafka event as EventDataMap
Handling user.created for user 123 (test@example.com)
_MetaData: { timestamp: ..., source: 'kafka', topic: 'user-events', ... }
🎉 Welcome email sent to test@example.com (User ID: 123)
```

✅ Success! Your `EventData` library automatically wrapped the Kafka message and made it available as a type-safe `EventDataMap`.

---

## 🔄 Optional: Read Metadata in Consumer

In `kafka.controller.ts`, you can access Kafka-specific meta

```ts
@EventPattern('user-events')
async handleUserEvent(@Payload() eventData: EventDataMap) {
  const topic = eventData.getMetadata().topic;
  const partition = eventData.getMetadata().partition;
  const offset = eventData.getMetadata().offset;

  console.log(`📬 From ${topic}[partition=${partition}, offset=${offset}]`);
}
```

---

## 📦 Publishing EventData with Metadata

If you want to preserve `EventData` metadata across services:

```ts
// In KafkaService
const eventData = new EventData(
  { userId, action: 'login' },
  { traceId: 'req-abc', source: 'auth-service' },
);

this.kafkaClient.emit('user-events', {
  ...eventData.getAll(),
  __meta: eventData.getMetadata(), // convention: include metadata under __meta
});
```

Then in consumer, reconstruct:

```ts
// You could enhance KafkaEventDataInterceptor to auto-detect and restore __meta
// (not shown here — but easy to add!)
```

---

## ✅ Summary: Kafka + EventData Library Flow

| Step | Component | What Happens |
|------|-----------|--------------|
| 1️⃣ | `KafkaService.emit(...)` | Sends plain object to Kafka topic |
| 2️⃣ | Kafka Broker | Stores message in `user-events` topic |
| 3️⃣ | `@EventPattern('user-events')` | Kafka microservice receives raw message |
| 4️⃣ | `KafkaEventDataInterceptor` | ⭐ Auto-wraps message.value into `EventData` instance |
| 5️⃣ | `handleUserEvent(@Payload() eventData: EventDataMap)` | ⭐ Handler receives type-safe `EventDataMap` |
| 6️⃣ | Business Logic | Uses `.get<T>()`, `.getMetadata()` for safe access |

---

## 🧩 Pro Tips

- ✅ Use `EventDataMap` interface in handlers — keeps you decoupled from implementation.
- ✅ Add schema validation (e.g., `zod`) on `.getAll()` if you need runtime safety.
- ✅ Use `correlationId` from metadata to trace events across services.
- ✅ Consider Avro/Schema Registry for production Kafka — you can still wrap deserialized data in `EventData`.

---

## 🚀 What’s Next?

You now have a **fully working Kafka producer/consumer app using your generic `EventData` library** — no custom DTOs needed!

Want me to show you how to:
- ➕ Add **schema validation** with `zod`?
- ➕ Add **OpenTelemetry tracing** using metadata?
- ➕ **Serialize/deserialize `EventData` with metadata** across services?
- ➕ Use with **Kafka + Avro + Schema Registry**?

Just ask — I’ll extend this example further!