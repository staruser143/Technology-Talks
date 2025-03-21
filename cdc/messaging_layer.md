Abstracting the messaging layer to make the publisher and consumer agnostic to the underlying messaging implementation is a great way to create a modular, scalable, and easily extensible system. This can be achieved by implementing a design pattern like the **Strategy Pattern** or **Adapter Pattern**, along with a unified interface for your messaging layer. Here's how you can build such a framework in NestJS:

---

### 1. **Define a Common Interface**
Create an interface that defines the contract for your messaging layer. This will ensure all implementations (Event Grid, Service Bus, Event Hubs) conform to the same structure.

```typescript
export interface MessagingService {
  publish(eventName: string, data: any): Promise<void>;
  subscribe(handler: (message: any) => void): Promise<void>;
}
```

---

### 2. **Implement Messaging Services**
Create concrete implementations of the interface for each messaging platform.

#### Azure Event Grid Implementation:
```typescript
import { EventGridPublisherClient, AzureKeyCredential } from '@azure/eventgrid';
import { MessagingService } from './messaging-service.interface';

export class EventGridService implements MessagingService {
  private client: EventGridPublisherClient;

  constructor(endpoint: string, accessKey: string) {
    this.client = new EventGridPublisherClient(endpoint, new AzureKeyCredential(accessKey));
  }

  async publish(eventName: string, data: any): Promise<void> {
    await this.client.send([
      {
        eventType: eventName,
        subject: 'EventGrid',
        dataVersion: '1.0',
        data,
        eventTime: new Date(),
      },
    ]);
  }

  async subscribe(handler: (message: any) => void): Promise<void> {
    throw new Error('Event Grid does not support direct subscription in this abstraction.');
  }
}
```

#### Azure Service Bus Implementation:
```typescript
import { ServiceBusClient } from '@azure/service-bus';
import { MessagingService } from './messaging-service.interface';

export class ServiceBusService implements MessagingService {
  private client: ServiceBusClient;

  constructor(connectionString: string, queueName: string) {
    this.client = new ServiceBusClient(connectionString);
    this.receiver = this.client.createReceiver(queueName);
  }

  async publish(eventName: string, data: any): Promise<void> {
    const sender = this.client.createSender(queueName);
    await sender.sendMessages({ body: data });
    await sender.close();
  }

  async subscribe(handler: (message: any) => void): Promise<void> {
    this.receiver.subscribe({
      processMessage: async (message) => handler(message.body),
      processError: async (err) => console.error(err),
    });
  }
}
```

#### Azure Event Hubs Implementation:
```typescript
import { EventHubProducerClient, EventHubConsumerClient } from '@azure/event-hubs';
import { MessagingService } from './messaging-service.interface';

export class EventHubsService implements MessagingService {
  private producer: EventHubProducerClient;
  private consumer: EventHubConsumerClient;

  constructor(connectionString: string, eventHubName: string) {
    this.producer = new EventHubProducerClient(connectionString, eventHubName);
    this.consumer = new EventHubConsumerClient('$Default', connectionString, eventHubName);
  }

  async publish(eventName: string, data: any): Promise<void> {
    const batch = await this.producer.createBatch();
    batch.tryAdd({ body: data });
    await this.producer.sendBatch(batch);
  }

  async subscribe(handler: (message: any) => void): Promise<void> {
    this.consumer.subscribe({
      processEvents: async (events) => {
        for (const event of events) {
          handler(event.body);
        }
      },
      processError: async (err) => console.error(err),
    });
  }
}
```

---

### 3. **Messaging Service Factory**
Use a factory or dependency injection to dynamically select the messaging service implementation based on configuration.

#### Service Factory:
```typescript
import { Injectable } from '@nestjs/common';
import { MessagingService } from './messaging-service.interface';
import { EventGridService } from './event-grid.service';
import { ServiceBusService } from './service-bus.service';
import { EventHubsService } from './event-hubs.service';

@Injectable()
export class MessagingServiceFactory {
  static createService(serviceType: string): MessagingService {
    switch (serviceType) {
      case 'EventGrid':
        return new EventGridService('<event-grid-endpoint>', '<access-key>');
      case 'ServiceBus':
        return new ServiceBusService('<connection-string>', '<queue-name>');
      case 'EventHubs':
        return new EventHubsService('<connection-string>', '<event-hub-name>');
      default:
        throw new Error('Unsupported service type');
    }
  }
}
```

---

### 4. **Publisher and Consumer Use the Abstraction**
Now, the publisher and consumer code can use the abstracted interface without knowing the underlying implementation.

#### Publisher:
```typescript
const service = MessagingServiceFactory.createService('EventGrid');
await service.publish('UserCreatedEvent', { userId: 123, name: 'Alice' });
```

#### Consumer:
```typescript
const service = MessagingServiceFactory.createService('ServiceBus');
await service.subscribe((message) => {
  console.log('Received message:', message);
});
```

---

### 5. **Configuration-Driven Selection**
You can use environment variables or a configuration file to specify the messaging service type:
```typescript
const serviceType = process.env.MESSAGING_SERVICE || 'EventGrid';
const service = MessagingServiceFactory.createService(serviceType);
```

---

### Advantages of This Approach
- **Decoupling:** The publisher and consumer are agnostic to the messaging platform.
- **Flexibility:** Switching to a different messaging platform requires no code changes in the publisher/consumer logic.
- **Extensibility:** Adding support for a new platform (e.g., RabbitMQ) only requires a new implementation of the `MessagingService` interface.

This structure promotes clean architecture principles and ensures maintainability of your codebase. Let me know if you’d like a deeper dive into any specific part!