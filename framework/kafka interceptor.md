Great question — and the short answer is:

> ✅ **No, you should NOT force every app to add the Kafka interceptor manually.**  
> 🎯 Instead, **design the library to be modular and configurable**, so apps **only enable what they need**.

---

## 🚫 Why Forcing Kafka Interceptor Everywhere Is Bad

1. **Unnecessary overhead** — Apps using only HTTP don’t need Kafka logic.
2. **Dependency pollution** — Might pull in Kafka types or cause conflicts.
3. **Violates Single Responsibility** — Library should not assume transport.
4. **Runtime errors** — If Kafka interceptor runs on non-Kafka context, it may crash (`context.getArgs()[0]` may not be a Kafka message).

---

## ✅ Recommended Design: Transport-Specific Modules

Use **feature modules** or **dynamic module configuration** so consumers **opt-in** to Kafka, RMQ, or HTTP support.

---

## 🛠️ Step-by-Step: Make Kafka Support Optional & Explicit

### 1. Update `EventDataModule` to Support Feature Modules

```ts
// libs/event-data/src/event-data.module.ts

import { Module, Global, DynamicModule } from '@nestjs/common';
import { EventDataFactory } from './event-data.factory';
import { EventDataInterceptor } from './event-data.interceptor';
import { KafkaEventDataInterceptor } from './microservices/kafka-event-data.interceptor';
import { RmqEventDataInterceptor } from './microservices/rmq-event-data.interceptor';

@Global()
@Module({
  providers: [EventDataFactory],
  exports: [EventDataFactory],
})
export class EventDataModule {
  // Root module — minimal, no interceptors bound globally
  static forRoot(): DynamicModule {
    return {
      module: EventDataModule,
    };
  }

  // HTTP support
  static forHttp(): DynamicModule {
    return {
      module: EventDataModule,
      providers: [EventDataInterceptor],
      exports: [EventDataInterceptor],
    };
  }

  // Kafka support
  static forKafka(): DynamicModule {
    return {
      module: EventDataModule,
      providers: [KafkaEventDataInterceptor],
      exports: [KafkaEventDataInterceptor],
    };
  }

  // RMQ support
  static forRmq(): DynamicModule {
    return {
      module: EventDataModule,
      providers: [RmqEventDataInterceptor],
      exports: [RmqEventDataInterceptor],
    };
  }

  // Combine multiple (optional)
  static forTransports(transports: ('http' | 'kafka' | 'rmq')[]): DynamicModule {
    const providers = [];
    const exports = [];

    if (transports.includes('http')) {
      providers.push(EventDataInterceptor);
      exports.push(EventDataInterceptor);
    }
    if (transports.includes('kafka')) {
      providers.push(KafkaEventDataInterceptor);
      exports.push(KafkaEventDataInterceptor);
    }
    if (transports.includes('rmq')) {
      providers.push(RmqEventDataInterceptor);
      exports.push(RmqEventDataInterceptor);
    }

    return {
      module: EventDataModule,
      providers,
      exports,
    };
  }
}
```

---

### 2. Apps Import Only What They Need

#### ➤ HTTP-only App

```ts
// app.module.ts
import { EventDataModule } from '@yourorg/event-data';

@Module({
  imports: [
    EventDataModule.forHttp(), // ✅ Only HTTP interceptor registered
  ],
})
export class AppModule {}
```

Then in `main.ts`, bind globally if desired:

```ts
const app = await NestFactory.create(AppModule);
const eventDataInterceptor = app.get(EventDataInterceptor);
app.useGlobalInterceptors(eventDataInterceptor);
```

#### ➤ Kafka Microservice App

```ts
// microservice.module.ts
import { EventDataModule } from '@yourorg/event-data';

@Module({
  imports: [
    EventDataModule.forKafka(), // ✅ Only Kafka interceptor registered
  ],
})
export class KafkaMicroserviceModule {}
```

In `main.ts` for microservice:

```ts
const app = await NestFactory.createMicroservice(AppModule, {
  transport: Transport.KAFKA,
  options: { ... },
});

const kafkaInterceptor = app.get(KafkaEventDataInterceptor);
app.useGlobalInterceptors(kafkaInterceptor);

await app.listen();
```

#### ➤ Hybrid App (HTTP + Kafka)

```ts
// app.module.ts
import { EventDataModule } from '@yourorg/event-data';

@Module({
  imports: [
    EventDataModule.forTransports(['http', 'kafka']),
  ],
})
export class HybridAppModule {}
```

Then bind both interceptors appropriately:

```ts
// main.ts
const app = await NestFactory.create(AppModule);

// For HTTP
app.useGlobalInterceptors(app.get(EventDataInterceptor));

// For Kafka microservice (if same app hosts both)
app.connectMicroservice({
  transport: Transport.KAFKA,
  options: { ... },
});
app.useGlobalInterceptors(app.get(KafkaEventDataInterceptor)); // applies to microservice too

await app.startAllMicroservices();
await app.listen(3000);
```

> 💡 Note: `useGlobalInterceptors` applies to both HTTP and microservices in the same Nest app. If you need **transport-specific interception**, you may need to apply interceptors at controller or handler level — or use middleware/pipe per transport.

---

## 🔄 Alternative: Auto-Detect Transport in One Interceptor (Advanced)

If you want **one universal interceptor**, you can detect context:

```ts
@Injectable()
export class UniversalEventDataInterceptor implements NestInterceptor {
  constructor(private factory: EventDataFactory) {}

  intercept(context: ExecutionContext, next: CallHandler) {
    let payload: any;
    let metadata: Record<string, any> = { timestamp: Date.now() };

    if (context.getType() === 'http') {
      const req = context.switchToHttp().getRequest();
      payload = req.body;
      metadata = {
        ...metadata,
        source: 'http',
        correlationId: req.headers['x-correlation-id'] || uuidv4(),
      };
    } else if (context.getType() === 'rpc') {
      // Could be Kafka, RMQ, gRPC, etc.
      const [arg0] = context.getArgs();

      // Detect Kafka
      if (arg0?.topic && arg0?.partition !== undefined) {
        payload = arg0.value;
        metadata = {
          ...metadata,
          source: 'kafka',
          topic: arg0.topic,
          correlationId: arg0.headers?.['correlationId'] || uuidv4(),
        };
      }
      // Add RMQ, NATS, etc. detection as needed
    }

    const eventData = this.factory.create(payload, metadata);

    // Mutate context based on type
    if (context.getType() === 'http') {
      context.switchToHttp().getRequest().eventData = eventData;
    } else {
      context.getArgs()[0] = eventData;
    }

    return next.handle();
  }
}
```

→ Then apps only import and bind `UniversalEventDataInterceptor`.

⚠️ **Trade-off**: More complex, harder to test, may break with new transports.

✅ **Recommendation**: Stick with **explicit, modular interceptors** — clearer, more maintainable.

---

## 📦 Bonus: Barrel Export Cleanup

Update `index.ts` to export module configs clearly:

```ts
// libs/event-data/src/index.ts

export * from './event-data.interface';
export * from './event-data.class';
export * from './event-data.factory';
export * from './event-data.interceptor';
export * from './event-data.decorator';
export * from './event-data.module';
export * from './microservices/kafka-event-data.interceptor';
export * from './microservices/rmq-event-data.interceptor';

export { EventData } from './event-data.decorator';

// Re-export module configs for clarity
export const EventDataHttpModule = EventDataModule.forHttp();
export const EventDataKafkaModule = EventDataModule.forKafka();
export const EventDataRmqModule = EventDataModule.forRmq();
```

Usage becomes even cleaner:

```ts
import { EventDataHttpModule } from '@yourorg/event-data';

@Module({
  imports: [EventDataHttpModule],
})
export class AppModule {}
```

---

## ✅ Summary: Best Practice

| Scenario                     | What to Do                                                                 |
|------------------------------|----------------------------------------------------------------------------|
| HTTP-only app                | Import `EventDataModule.forHttp()`                                         |
| Kafka microservice           | Import `EventDataModule.forKafka()`                                        |
| Hybrid app                   | Use `EventDataModule.forTransports(['http', 'kafka'])`                     |
| Future transport (e.g., NATS)| Add new interceptor + extend `forTransports` or create `forNats()`         |
| Want ultimate simplicity     | Use universal interceptor (with auto-detection) — but test thoroughly      |

---

## ✅ Final Answer

> **No — Kafka interceptor should NOT be added to every app.**  
> Instead, **make it opt-in** via `EventDataModule.forKafka()`, so only Kafka-based services include it. This keeps your library **lightweight, modular, and transport-agnostic**.

This design follows NestJS best practices and scales cleanly across monoliths, microservices, and hybrid architectures.

---

Let me know if you want me to generate a **plugin system** or **auto-registration based on config** next!