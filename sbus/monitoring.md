To implement **logging and alerting** when messages are dead-lettered in Azure Service Bus, you can follow a few best practices depending on your infrastructure and monitoring tools.

---

## ✅ 1. **Logging Dead-Letter Events in NestJS**

You can log dead-letter events to a file, database, or external logging service like **Azure Monitor**, **Application Insights**, or **Logstash**.

### Example:
```ts
await sessionReceiver.deadLetterMessage(message, {
  deadLetterReason: 'MaxRetriesExceeded',
  deadLetterErrorDescription: error.message,
});

// Log the event
this.logger.error(`Dead-lettered message: ${message.messageId}`, {
  sessionId: message.sessionId,
  reason: 'MaxRetriesExceeded',
  error: error.message,
  payload: message.body,
});
```

You can use NestJS's built-in `Logger` or integrate with tools like **Winston**, **Pino**, or **Azure Application Insights SDK**.

---

## ✅ 2. **Alerting via Azure Monitor or Application Insights**

### Option A: **Azure Monitor Alerts**
- Enable diagnostics on your Service Bus namespace.
- Route logs to **Log Analytics**.
- Create a **custom query** to detect dead-letter events.
- Set up an **alert rule** to notify via email, SMS, or webhook.

### Option B: **Application Insights**
- Use the SDK to track custom events:
```ts
import { TelemetryClient } from 'applicationinsights';

const appInsights = new TelemetryClient('<INSTRUMENTATION_KEY>');
appInsights.trackEvent({
  name: 'DeadLetteredMessage',
  properties: {
    messageId: message.messageId,
    sessionId: message.sessionId,
    reason: 'MaxRetriesExceeded',
    error: error.message,
  },
});
```

---

## ✅ 3. **Optional: Forward Dead-Letter Messages to a Diagnostic Queue**

You can create a separate queue or topic to forward dead-lettered messages for inspection or reprocessing.

---

## 🧠 Best Practices

- Include **message metadata** in logs (e.g., `messageId`, `sessionId`, `eventType`).
- Use **structured logging** for easy querying.
- Set up **alerts for thresholds** (e.g., >10 dead-lettered messages in 5 minutes).
- Consider **automated reprocessing** for recoverable errors.

---
