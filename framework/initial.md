This is a great architectural goal — to create a **generic, reusable library in NestJS** that **encapsulates incoming request/command data into a standardized Map-like structure**, so that **event handlers in an event-driven system** don’t need custom DTOs per event/command.

This promotes:
- Loose coupling
- Reusability
- Easier testing
- Schema-agnostic event handling

---

## 🧠 Core Design Principles

1. **Encapsulate any incoming data** (HTTP request body, Kafka message, RabbitMQ payload, gRPC call, etc.) into a **generic `EventDataMap`** (extends `Map<string, any>` or plain object with typing).
2. Provide **type-safe accessors** (get, has, set) to avoid `any`.
3. Allow **metadata injection** (e.g., correlation ID, timestamp, source service).
4. Be **transport-agnostic** — work with HTTP, Kafka, RabbitMQ, etc.
5. Integrate with NestJS **Interceptors, Guards, or Middleware** to auto-wrap incoming data.

---

## ✅ Step-by-Step Implementation

---

### 1. Define the Generic Event Data Interface

```ts
// libs/event-data/src/event-data.interface.ts

export interface EventDataMap {
  get<T = any>(key: string): T | undefined;
  has(key: string): boolean;
  set(key: string, value: any): this;
  getAll(): Record<string, any>;
  // Optional: Add metadata
  getMetadata(): Record<string, any>;
}
```

---

### 2. Implement the Concrete EventDataMap Class

```ts
// libs/event-data/src/event-data-map.class.ts

export class EventDataMap implements EventDataMap {
  private  Record<string, any> = {};
  private metadata: Record<string, any> = {};

  constructor(initialData: Record<string, any> = {}, metadata: Record<string, any> = {}) {
    this.data = { ...initialData };
    this.metadata = { ...metadata };
  }

  get<T = any>(key: string): T | undefined {
    return this.data[key] as T | undefined;
  }

  has(key: string): boolean {
    return key in this.data;
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

  // Optional: Merge another map
  merge(other: EventDataMap): this {
    Object.assign(this.data, other.getAll());
    Object.assign(this.metadata, other.getMetadata());
    return this;
  }
}
```

---

### 3. Create a Factory/Service to Auto-Wrap Incoming Data

```ts
// libs/event-data/src/event-data.factory.ts

import { Injectable } from '@nestjs/common';
import { EventDataMap } from './event-data-map.class';

@Injectable()
export class EventDataFactory {
  create(payload: any, metadata: Record<string, any> = {}): EventDataMap {
    // Normalize payload: if it's an object, use as-is. If array/string/other, wrap.
    const normalized = this.normalizePayload(payload);
    return new EventDataMap(normalized, metadata);
  }

  private normalizePayload(payload: any): Record<string, any> {
    if (payload == null) return {};
    if (typeof payload === 'object' && !Array.isArray(payload)) {
      return { ...payload };
    }
    // Wrap non-object payloads under a default key
    return { payload };
  }
}
```

---

### 4. Create NestJS Interceptor to Auto-Inject EventDataMap into Request Context

This is key — you want to **intercept incoming requests/events** and **replace or enrich `request.body` or context** with `EventDataMap`.

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
import { EventDataMap } from './event-data-map.class';

@Injectable()
export class EventDataInterceptor implements NestInterceptor {
  constructor(private readonly eventDataFactory: EventDataFactory) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const req = context.switchToHttp().getRequest();

    // Extract payload — customize per transport (HTTP, Kafka, etc.)
    const payload = req.body || {}; // For HTTP. For microservices, use context.getArgs()[0]

    // Inject correlation ID or other metadata
    const metadata = {
      timestamp: Date.now(),
      correlationId: req.headers['x-correlation-id'] || crypto.randomUUID(),
      source: 'http', // or detect from context
    };

    // Wrap payload
    const eventDataMap = this.eventDataFactory.create(payload, metadata);

    // Attach to request for downstream use (controllers, services, event handlers)
    req.eventData = eventDataMap;

    // Optionally: Replace body for controllers expecting EventDataMap
    // req.body = eventDataMap;

    return next.handle().pipe(
      map((data) => {
        // Optionally wrap response too, if needed
        return data;
      }),
    );
  }
}
```

> 💡 For **microservices (Kafka/RMQ)**, you’d create a similar interceptor or use a custom **transport strategy decorator/factory**.

---

### 5. Use in Controllers or Event Handlers

Now your controllers or CQRS command/event handlers can expect `EventDataMap` instead of specific DTOs.

```ts
// Example controller

@Controller('events')
export class EventsController {
  @Post('handle')
  handleEvent(@Req() req: any) {
    const eventData: EventDataMap = req.eventData;

    // Type-safe access
    const userId = eventData.get<string>('userId');
    const amount = eventData.get<number>('amount');
    const meta = eventData.getMetadata();

    // Pass to domain service or emit further events
    return { processed: true, meta };
  }
}
```

---

### 6. (Optional) Create a Custom Decorator for Syntactic Sugar

```ts
// libs/event-data/src/event-data.decorator.ts

import { createParamDecorator, ExecutionContext } from '@nestjs/common';

export const EventData = createParamDecorator(
  ( unknown, ctx: ExecutionContext): EventDataMap => {
    const request = ctx.switchToHttp().getRequest();
    return request.eventData;
  },
);
```

Usage:

```ts
@Post('handle')
handleEvent(@EventData() eventData: EventDataMap) {
  const userId = eventData.get<string>('userId');
  // ...
}
```

---

### 7. Integration with CQRS / Event Bus

If you’re using `@nestjs/cqrs`, you can wrap commands/events with `EventDataMap`:

```ts
export class GenericCommandEvent {
  constructor(public eventData: EventDataMap) {}
}

// In Command Handler
@CommandHandler(GenericCommandEvent)
export class GenericCommandHandler implements ICommandHandler<GenericCommandEvent> {
  execute(command: GenericCommandEvent) {
    const { eventData } = command;
    const action = eventData.get<string>('action');
    // route internally based on action or metadata
  }
}
```

This allows **one generic command handler** to route internally based on keys like `action`, `type`, etc.

---

## 📦 Library Structure (NestJS Dynamic Module)

To make this a reusable library:

```
libs/
  event-data/
    src/
      event-data.interface.ts
      event-data-map.class.ts
      event-data.factory.ts
      event-data.interceptor.ts
      event-data.decorator.ts
      event-data.module.ts
```

```ts
// event-data.module.ts

import { Module } from '@nestjs/common';
import { EventDataFactory } from './event-data.factory';
import { EventDataInterceptor } from './event-data.interceptor';

@Module({
  providers: [EventDataFactory, EventDataInterceptor],
  exports: [EventDataFactory],
})
export class EventDataModule {
  static forRoot(): DynamicModule {
    return {
      module: EventDataModule,
      global: true, // optional
    };
  }
}
```

Then in `AppModule`:

```ts
@Module({
  imports: [EventDataModule.forRoot()],
  // ...
})
export class AppModule {}
```

And globally bind the interceptor:

```ts
// main.ts or in a feature module
const app = await NestFactory.create(AppModule);
app.useGlobalInterceptors(new EventDataInterceptor(app.get(EventDataFactory)));
```

---

## 🔄 Handling Microservices (Kafka, RMQ, etc.)

You can extend this by creating **transport-specific interceptors or decorators**.

Example for Kafka:

```ts
export class KafkaEventDataInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler) {
    const message = context.getArgs()[0]; // Kafka message
    const payload = message.value;
    const metadata = {
      topic: message.topic,
      partition: message.partition,
      timestamp: Date.now(),
    };
    const eventDataMap = this.factory.create(payload, metadata);
    context.getArgs()[0] = eventDataMap; // replace message with wrapped version
    return next.handle();
  }
}
```

---

## ✅ Benefits

- ✅ No need to define DTO per event/command
- ✅ Centralized, consistent data access
- ✅ Metadata propagation (tracing, auditing)
- ✅ Easy to mock in tests
- ✅ Transport agnostic
- ✅ Integrates with NestJS ecosystem

---

## ⚠️ Caveats & Considerations

- **Type Safety**: You lose compile-time type safety unless you layer in schema validation (e.g., using `zod` or `class-validator` on top).
- **Documentation**: Harder to auto-generate API docs (Swagger) — you may need to manually describe expected keys.
- **Performance**: Minimal overhead, but avoid deep cloning large payloads unnecessarily.

> 💡 **Pro Tip**: You can combine this with a **schema registry** or **JSON Schema validator** to validate `EventDataMap` contents at runtime while keeping the interface generic.

---

## ✅ Final Thoughts

You’ve essentially built a **“Schemaless Event Envelope”** pattern. It’s very useful in **dynamic, polyglot, or legacy-integration scenarios** where rigid DTOs are impractical.

For stricter domains, you can still **layer typed facades on top** — e.g.:

```ts
class CreateUserFromEvent {
  static from(eventData: EventDataMap): CreateUserDto {
    return {
      name: eventData.get('name'),
      email: eventData.get('email'),
    };
  }
}
```

This gives you flexibility without sacrificing structure where needed.

---

Let me know if you want me to generate the full library code structure or integrate it with Kafka/CQRS specifically!