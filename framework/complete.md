Absolutely! Below is the **complete, ready-to-use NestJS library structure** for your generic `EventDataMap` system — designed to encapsulate any incoming request/command/event into a standardized `Map`-like structure for use in event-driven architectures.

This includes:
- ✅ Core `EventDataMap` class
- ✅ Factory, Interceptor, Decorator
- ✅ Transport-agnostic design (HTTP + Kafka examples)
- ✅ CQRS integration example
- ✅ Dynamic module for easy import
- ✅ Folder structure + `index.ts` barrel exports
- ✅ README suggestion

---

## 📁 Final Folder Structure

```
libs/
└── event-data/
    ├── src/
    │   ├── event-data.interface.ts
    │   ├── event-data-map.class.ts
    │   ├── event-data.factory.ts
    │   ├── event-data.interceptor.ts
    │   ├── event-data.decorator.ts
    │   ├── event-data.module.ts
    │   ├── microservices/
    │   │   ├── kafka-event-data.interceptor.ts
    │   │   └── rmq-event-data.interceptor.ts
    │   └── index.ts
    ├── README.md
    └── package.json (optional, if published as npm package)
```

---

## 📄 1. `event-data.interface.ts`

```ts
// libs/event-data/src/event-data.interface.ts

export interface EventDataMap {
  get<T = any>(key: string): T | undefined;
  has(key: string): boolean;
  set(key: string, value: any): this;
  getAll(): Record<string, any>;
  getMetadata(): Record<string, any>;
  merge(other: EventDataMap): this;
}
```

---

## 📄 2. `event-data-map.class.ts`

```ts
// libs/event-data/src/event-data-map.class.ts

export class EventDataMap implements EventDataMap {
  private  Record<string, any> = {};
  private meta Record<string, any> = {};

  constructor(initialData: Record<string, any> = {}, meta Record<string, any> = {}) {
    this.data = { ...initialData };
    this.metadata = { ...metadata };
  }

  get<T = any>(key: string): T | undefined {
    return this.data[key] as T | undefined;
  }

  has(key: string): boolean {
    return Object.prototype.hasOwnProperty.call(this.data, key);
  }

  set(key: string, value: any): this {
    this.data[key] = value;
    return this;
  }

  getAll(): Record<string, any> {
    return { ...this.data };
  }

  getMetadata(): Record<string, any> {
    return { ...this.metadata };
  }

  merge(other: EventDataMap): this {
    Object.assign(this.data, other.getAll());
    Object.assign(this.metadata, other.getMetadata());
    return this;
  }
}
```

---

## 📄 3. `event-data.factory.ts`

```ts
// libs/event-data/src/event-data.factory.ts

import { Injectable } from '@nestjs/common';
import { EventDataMap } from './event-data-map.class';

@Injectable()
export class EventDataFactory {
  create(payload: any, metadata: Record<string, any> = {}): EventDataMap {
    const normalized = this.normalizePayload(payload);
    return new EventDataMap(normalized, metadata);
  }

  private normalizePayload(payload: any): Record<string, any> {
    if (payload == null) return {};
    if (typeof payload === 'object' && !Array.isArray(payload)) {
      return { ...payload };
    }
    return { payload };
  }
}
```

---

## 📄 4. `event-data.interceptor.ts` (HTTP)

```ts
// libs/event-data/src/event-data.interceptor.ts

import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { EventDataFactory } from './event-data.factory';
import { v4 as uuidv4 } from 'uuid'; // You'll need to install: npm i uuid

@Injectable()
export class EventDataInterceptor implements NestInterceptor {
  constructor(private readonly eventDataFactory: EventDataFactory) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const req = context.switchToHttp().getRequest();

    const payload = req.body || {};
    const metadata = {
      timestamp: Date.now(),
      correlationId: req.headers['x-correlation-id'] || uuidv4(),
      source: 'http',
      method: req.method,
      url: req.url,
    };

    const eventDataMap = this.eventDataFactory.create(payload, metadata);

    // Attach to request for controllers/services
    req.eventData = eventDataMap;

    return next.handle().pipe(
      map((data) => data),
    );
  }
}
```

> 💡 Install `uuid`:  
> ```bash
> npm install uuid
> npm install --save-dev @types/uuid
> ```

---

## 📄 5. `event-data.decorator.ts`

```ts
// libs/event-data/src/event-data.decorator.ts

import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { EventDataMap } from './event-data-map.class';

export const EventData = createParamDecorator(
  (data: unknown, ctx: ExecutionContext): EventDataMap => {
    const request = ctx.switchToHttp().getRequest();
    return request.eventData;
  },
);
```

---

## 📄 6. `microservices/kafka-event-data.interceptor.ts`

```ts
// libs/event-data/src/microservices/kafka-event-data.interceptor.ts

import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { EventDataFactory } from '../event-data.factory';
import { v4 as uuidv4 } from 'uuid';

@Injectable()
export class KafkaEventDataInterceptor implements NestInterceptor {
  constructor(private readonly eventDataFactory: EventDataFactory) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const args = context.getArgs();
    const kafkaMessage = args[0]; // Kafka message object

    const payload = kafkaMessage.value;
    const metadata = {
      timestamp: Date.now(),
      correlationId: kafkaMessage.headers?.['correlationId'] || uuidv4(),
      source: 'kafka',
      topic: kafkaMessage.topic,
      partition: kafkaMessage.partition,
      offset: kafkaMessage.offset,
    };

    const eventDataMap = this.eventDataFactory.create(payload, metadata);

    // Replace original message with wrapped eventDataMap
    args[0] = eventDataMap;

    return next.handle();
  }
}
```

---

## 📄 7. `microservices/rmq-event-data.interceptor.ts` (Optional)

```ts
// libs/event-data/src/microservices/rmq-event-data.interceptor.ts

import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { EventDataFactory } from '../event-data.factory';
import { v4 as uuidv4 } from 'uuid';

@Injectable()
export class RmqEventDataInterceptor implements NestInterceptor {
  constructor(private readonly eventDataFactory: EventDataFactory) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const [msg, rawMessage] = context.getArgs(); // content, raw AMQP message

    const metadata = {
      timestamp: Date.now(),
      correlationId: rawMessage.properties?.correlationId || uuidv4(),
      source: 'rmq',
      routingKey: rawMessage.fields?.routingKey,
    };

    const eventDataMap = this.eventDataFactory.create(msg, metadata);

    // Replace original message
    context.getArgs()[0] = eventDataMap;

    return next.handle();
  }
}
```

---

## 📄 8. `event-data.module.ts`

```ts
// libs/event-data/src/event-data.module.ts

import { Module, Global, DynamicModule } from '@nestjs/common';
import { EventDataFactory } from './event-data.factory';
import { EventDataInterceptor } from './event-data.interceptor';
import { KafkaEventDataInterceptor } from './microservices/kafka-event-data.interceptor';
import { RmqEventDataInterceptor } from './microservices/rmq-event-data.interceptor';

@Global() // Optional: makes providers available globally
@Module({
  providers: [
    EventDataFactory,
    EventDataInterceptor,
    KafkaEventDataInterceptor,
    RmqEventDataInterceptor,
  ],
  exports: [
    EventDataFactory,
    EventDataInterceptor,
    KafkaEventDataInterceptor,
    RmqEventDataInterceptor,
  ],
})
export class EventDataModule {
  static forRoot(): DynamicModule {
    return {
      module: EventDataModule,
    };
  }

  static forFeature(): DynamicModule {
    return {
      module: EventDataModule,
    };
  }
}
```

---

## 📄 9. `index.ts` (Barrel Export)

```ts
// libs/event-data/src/index.ts

export * from './event-data.interface';
export * from './event-data-map.class';
export * from './event-data.factory';
export * from './event-data.interceptor';
export * from './event-data.decorator';
export * from './event-data.module';
export * from './microservices/kafka-event-data.interceptor';
export * from './microservices/rmq-event-data.interceptor';

// Re-export decorator for convenience
export { EventData } from './event-data.decorator';
```

---

## 📄 10. `README.md` (Suggested)

```md
# @yourorg/event-data

> A NestJS library to encapsulate any incoming event/command/request into a generic `EventDataMap` for event-driven systems.

## Features

- ✅ Transport-agnostic (HTTP, Kafka, RMQ, etc.)
- ✅ Metadata injection (correlation ID, timestamps, source)
- ✅ No need for per-event DTOs
- ✅ Interceptors + Decorators included
- ✅ Works with CQRS, Microservices, REST

## Installation

```bash
npm install @yourorg/event-data
# or if local
npm install ../libs/event-data
```

## Usage

### 1. Import Module

```ts
// app.module.ts
import { EventDataModule } from '@yourorg/event-data';

@Module({
  imports: [EventDataModule.forRoot()],
})
export class AppModule {}
```

### 2. Use in Controller

```ts
import { EventData } from '@yourorg/event-data';

@Controller()
export class MyController {
  @Post('event')
  handle(@EventData() event: EventDataMap) {
    const userId = event.get<string>('userId');
    const meta = event.getMetadata();
  }
}
```

### 3. Use with Kafka

```ts
// main.ts
app.connectMicroservice({
  transport: Transport.KAFKA,
  options: { ... }
});

app.useGlobalInterceptors(new KafkaEventDataInterceptor(app.get(EventDataFactory)));
```

---

## License

MIT
```

---

## 🧩 Example: Integration with CQRS (Bonus)

Suppose you’re using `@nestjs/cqrs`.

### Generic Command

```ts
// commands/generic-event.command.ts
import { EventDataMap } from '@yourorg/event-data';

export class GenericEventCommand {
  constructor(public readonly eventData: EventDataMap) {}
}
```

### Generic Command Handler

```ts
// handlers/generic-event.handler.ts
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { GenericEventCommand } from '../commands/generic-event.command';

@CommandHandler(GenericEventCommand)
export class GenericEventHandler implements ICommandHandler<GenericEventCommand> {
  async execute(command: GenericEventCommand) {
    const { eventData } = command;
    const eventType = eventData.get<string>('type');

    switch (eventType) {
      case 'user.created':
        // delegate to specific handler
        break;
      case 'payment.processed':
        // ...
        break;
      default:
        throw new Error(`Unknown event type: ${eventType}`);
    }
  }
}
```

---

## 🚀 How to Use in Your App

1. **Create the folder structure** under `libs/event-data`
2. **Copy each file** above into its place
3. **Install `uuid`** if not already installed
4. **Import `EventDataModule.forRoot()`** in your `AppModule`
5. **Apply interceptors** globally or per route:

```ts
// main.ts
const app = await NestFactory.create(AppModule);
app.useGlobalInterceptors(app.get(EventDataInterceptor));
await app.listen(3000);
```

For Kafka:

```ts
const kafkaInterceptor = app.get(KafkaEventDataInterceptor);
app.useGlobalInterceptors(kafkaInterceptor);
```

---

## ✅ You’re Done!

You now have a **complete, reusable, transport-agnostic NestJS library** that wraps any incoming data into a consistent `EventDataMap`, ready for consumption in event handlers without needing custom DTOs.

---

## 💡 Pro Enhancement Ideas

- Add runtime schema validation using `zod` or `joi`
- Add `.toDto<T>(schema: ZodSchema<T>): T` method to `EventDataMap`
- Add OpenTelemetry tracing context auto-injection
- Add NestJS ConfigurableModuleBuilder for options (e.g., key mapping, validation rules)

---

Let me know if you want me to generate this as a **publishable npm package**, add **unit tests**, or integrate with **OpenTelemetry / Sentry / Validation** next!