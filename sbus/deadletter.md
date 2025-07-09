To add **dead-letter handling** in your Azure Service Bus consumer service (e.g., in NestJS), you need to implement logic that:

1. **Detects when a message should be dead-lettered** (e.g., after max retries or due to validation failure).
2. **Moves the message to the dead-letter queue** using `deadLetterMessage()`.

---

### ✅ Step-by-Step Dead-Letter Handling

#### 1. **Dead-letter a message manually**
Use this when your business logic determines the message is invalid or cannot be processed.

```ts
await sessionReceiver.deadLetterMessage(message, {
  deadLetterReason: 'InvalidPayload',
  deadLetterErrorDescription: 'Missing required fields in event payload',
});
```

#### 2. **Dead-letter after max retries**
This is part of your retry logic. Here's how it fits into the full service:

---

### 🧩 Updated `processWithRetry()` with Dead-Letter Handling

```ts
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
        const jitter = Math.floor(Math.random() * 1000);
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
```

---

### 🧾 Optional: Read from Dead-Letter Queue

You can also create a separate receiver to read from the **dead-letter subqueue**:

```ts
const deadLetterReceiver = serviceBusClient.createReceiver(
  topicName,
  subscriptionName,
  { subQueueType: 'deadLetter' }
);

deadLetterReceiver.subscribe({
  processMessage: async (message) => {
    console.log('Dead-lettered message:', message.body);
    // Optionally log or forward to diagnostics
  },
  processError: async (err) => {
    console.error('Error reading dead-letter queue:', err);
  },
});
```

---

