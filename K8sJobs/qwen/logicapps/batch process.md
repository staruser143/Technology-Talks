Yes, **Azure Logic Apps can batch read messages from an Azure Service Bus queue**, but with some important considerations and limitations.

### ✅ How Batching Works in Logic Apps with Service Bus

By default, the **Service Bus trigger in Logic Apps (Standard or Consumption)** does **not natively support batch processing of multiple messages in a single workflow run** out of the box. Instead:

- The **trigger fires once per message** when using the default `When messages are available in a queue (peek-lock)` trigger.
- Each message initiates a separate workflow instance.

However, you **can achieve batch processing** using one of the following approaches:

---

### ✅ Option 1: Use **"For each" with Peek-Lock and Manual Receive** (Manual Batching)

You can manually retrieve multiple messages using the **Service Bus connector actions** (not the trigger) in a loop:

#### Steps:
1. Use a **Recurrence trigger** (e.g., every 5 minutes).
2. Inside the workflow, use the **"Peek-Lock Message" action** with the option to receive **multiple messages** (set `Maximal number of messages to receive` to a number like 10, 50, etc.).
3. This returns a batch of messages as an array.
4. Use a **"For each" loop** to process each message.
5. After processing, **manually complete each message** using the **"Complete Message" action** (important to avoid reprocessing).
6. Optionally, handle errors and abandon messages if needed.

> 🔹 This gives you **true batching** — multiple messages processed in one workflow run.

#### Example:
```plaintext
Trigger: Recurrence (every 5 mins)
Action: Peek-Lock Message (max 10 messages)
Action: For each message in output array
    - Process message (e.g., transform, send to another service)
    - Complete Message
```

> ⚠️ You must **manually manage message lifecycle** (complete, abandon, defer) since auto-completion doesn't work here.

---

### ❌ Limitation: Built-in Trigger Doesn't Batch

The built-in trigger:
> `When messages are available in a queue (peek-lock)`

- Triggers **one workflow per message**
- Cannot batch multiple messages into a single run
- Not suitable if you want to aggregate or process messages in bulk

---

### ✅ Option 2: Use **Azure Functions + Logic Apps (Standard)**

If you need high-performance batch processing:
1. Use an **Azure Function** with **Service Bus trigger** (which supports batch natively via `Message[]` array).
2. The function processes the batch and then calls a Logic App (via HTTP) for orchestration if needed.

This leverages the **native batching capability of Azure Functions**, which is more efficient.

---

### ✅ Option 3: Logic Apps (Standard) with Custom Code or Loops

In **Logic Apps Standard (code-based)**, you have more control:
- Use **in-line code** (JavaScript) or **custom connectors**
- Or use the same **Peek-Lock + Loop** pattern as above

Also supports integration with **Durable Functions** or **Azure Functions** for advanced batching.

---

### Summary

| Method | Supports Batching | Notes |
|-------|-------------------|-------|
| Default Service Bus Trigger | ❌ No | One message per workflow run |
| Recurrence + Peek-Lock Multiple Messages | ✅ Yes | Manual receive & complete; full control |
| Azure Function (SB Trigger) + Logic App | ✅ Yes | Best for high-volume batching |
| Logic App Standard with Loop | ✅ Yes | Flexible, but requires manual message handling |

---

### Recommendation

If you need **true batch processing** (e.g., aggregate 10–100 messages and process them together), use:

> ✅ **Recurrence trigger + Peek-Lock multiple messages + For each loop + Complete message**

This allows batching within a single workflow execution.

Let me know if you'd like a sample workflow definition!