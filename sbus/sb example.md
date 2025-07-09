Absolutely! Here's a **complete NestJS service** that sends and receives messages from an **Azure Service Bus Topic with session support**, **duplicate detection**, and **exponential backoff retry logic**.

---

## ✅ AzureServiceBusService (Full Example)

```ts
// src/azure/azure.service.ts

import { Injectable, OnModuleDestroy } from '@nestjs/common';
import {
  ServiceBusClient,
  ServiceBusMessage,
  ServiceBusReceivedMessage,
  ServiceBusSessionReceiver,
} from '@azure/service-bus';

@Injectable()
export class AzureServiceBusService implements OnModuleDestroy {
  private readonly serviceBusClient: ServiceBusClient;
  private readonly topicName = 'your-topic-name';
  private readonly subscriptionName = 'your-subscription-name';

  constructor() {
    const connectionString = process.env.SERVICE_BUS_CONNECTION_STRING;
    this.serviceBusClient = new ServiceBusClient(connectionString);
  }

  // Send a domain event with session and deduplication
  async sendDomainEvent(domain: 'quote' | 'enrollment', aggregateId: string, event: any) {
    const sender = this.serviceBusClient.createSender(this.topicName);

    const message: ServiceBusMessage = {
      body: event,
      sessionId: aggregateId, // Ensures ordering
      messageId: event.eventId, // Enables duplicate detection
      applicationProperties: {
        domain,
        eventType: event.type,
      },
    };

    await sender.sendMessages(message);
    await sender.close();
  }

  // Receive and process messages with exponential backoff
  async receiveMessagesWithRetry(maxRetries = 5, baseDelay = 1000) {
    const sessionReceiver: ServiceBusSessionReceiver =
      await this.serviceBusClient.acceptNextSession(this.topicName, this.subscriptionName);

    sessionReceiver.subscribe({
      processMessage: async (message: ServiceBusReceivedMessage) => {
        await this.processWithRetry(message, sessionReceiver, maxRetries, baseDelay);
      },
      processError: async (err) => {
        console.error('Error receiving message:', err);
      },
    });
  }

  // Retry logic with exponential backoff
  private async processWithRetry(
    message: ServiceBusReceivedMessage,
    sessionReceiver: ServiceBusSessionReceiver,
    maxRetries: number,
    baseDelay: number,
  ) {
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        console.log(`Processing message (attempt ${attempt}):`, message.body);

        // Simulate domain event processing
        await this.processDomainEvent(message.body);

        // Acknowledge message
        await sessionReceiver.completeMessage(message);
        return;
      } catch (error) {
        if (attempt < maxRetries) {
          const jitter = Math.floor(Math.random() * 1000); // up to 1s jitter
          const delay = baseDelay * 2 ** attempt + jitter;
          console.warn(`Retrying in ${delay}ms (attempt ${attempt + 1})`);
          await this.sleep(delay);
        } else {
          console.error('Max retries reached. Dead-lettering message.');
          await sessionReceiver.deadLetterMessage(message, {
            deadLetterReason: 'MaxRetriesExceeded',
            deadLetterErrorDescription: error.message,
          });
        }
      }
    }
  }

  // Simulated domain event handler
  private async processDomainEvent(event: any): Promise<void> {
    // Replace with actual business logic
    if (Math.random() < 0.3) {
      throw new Error('Simulated transient error');
    }
    console.log('Event processed successfully:', event);
  }

  private sleep(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  async onModuleDestroy() {
    await this.serviceBusClient.close();
  }
}
```

---

### 🧪 Usage Example

You can call `sendDomainEvent()` to publish events and `receiveMessagesWithRetry()` to start consuming them.

---

