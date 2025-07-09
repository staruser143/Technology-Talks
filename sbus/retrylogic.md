Implementing **retry logic** when consuming messages from Azure Service Bus in NestJS ensures that transient failures during message processing don’t result in message loss or unnecessary dead-lettering.

Here’s a structured approach to implement retry logic:

---

### ✅ 1. **Manual Retry with Attempt Counter**

You can use a custom property (e.g., `retryCount`) in the message or track it externally to retry a few times before abandoning or dead-lettering.

#### Example:
```ts
processMessage: async (message: ServiceBusReceivedMessage) => {
  const maxRetries = 3;
  let attempt = message.applicationProperties?.retryCount || 0;

  try {
    // Your domain logic here
    console.log(`Processing message attempt ${attempt}:`, message.body);

    // Simulate processing
    await processDomainEvent(message.body);

    // Acknowledge message
    await sessionReceiver.completeMessage(message);
  } catch (error) {
    attempt += 1;

    if (attempt < maxRetries) {
      console.warn(`Retrying message (attempt ${attempt})`);
      // Optionally re-send the message with updated retry count
      await sessionReceiver.abandonMessage(message);
    } else {
      console.error(`Max retries reached. Dead-lettering message.`);
      await sessionReceiver.deadLetterMessage(message, {
        deadLetterReason: 'MaxRetriesExceeded',
        deadLetterErrorDescription: error.message,
      });
    }
  }
}
```

---

### ✅ 2. **Use Built-in Retry Policy (for transient errors)**

Azure SDK has built-in retry policies for **network or service-level errors**, but not for **message processing logic**. You can configure it like this:

```ts
const client = new ServiceBusClient(connectionString, {
  retryOptions: {
    maxRetries: 5,
    retryDelayInMs: 1000,
    maxRetryDelayInMs: 10000,
  },
});
```

---

### ✅ 3. **Advanced: Use Middleware or Interceptors**

In NestJS, you can abstract retry logic using a **custom interceptor** or **middleware** around your message handler for cleaner separation.

---

### 🧠 Best Practices

- Use **exponential backoff** for retries.
- Avoid infinite retry loops.
- Log each retry attempt for observability.
- Consider using **dead-letter queues** for failed messages after max retries.

---

