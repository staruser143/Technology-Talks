**Yes, this batch-processing pattern works perfectly with Azure Logic Apps (Standard)** — and in fact, **it works even better** than in the Consumption plan due to greater control, performance, and integration options.

---

### ✅ Logic Apps Standard Enhancements

Azure Logic Apps **Standard** (multi-tenant or single-tenant / ASE) supports all the same connectors and actions as Consumption, but with **additional capabilities** that make batch processing **more powerful and reliable**.

---

### 🔧 How the Same Pattern Works in Standard Logic Apps

The **workflow logic remains almost identical**, but you get advantages:

| Feature | Consumption Plan | Standard Plan |
|--------|------------------|---------------|
| Workflow Definition Format | JSON (restricted) | JSON, **Code View**, or **Designer** |
| Runtime Control | Limited | Full control (App Service plan) |
| Long-running workflows | Limited by timeout | ✅ Supports **Durable Entities / Orchestration** |
| Peek-Lock & Complete Messages | ✅ Yes | ✅ Yes (same connector) |
| Custom Code (Inline) | ❌ Not supported | ✅ **JavaScript, C# script** |
| Integration with Functions | ✅ HTTP | ✅ **In-process, isolated, or HTTP** |
| VNet, Identity, Scale | Limited | ✅ Full support |

---

### ✅ Example: Enhanced Batch Processing in Standard Logic App

You can use the **same workflow** as in Consumption, but now you can:

#### 1. ✅ Add **Inline Code** for message parsing
```javascript
// In an "Execute JavaScript Code" action
const messages = workflowContext.actions['Peek-Lock_Message'].outputs.value;
return messages.map(msg => ({
  body: atob(msg.ContentData), // decode base64
  messageId: msg.MessageId,
  processedAt: new Date().toISOString()
}));
```

#### 2. ✅ Use **Durable Entities or Orchestrations** for complex batching
If you need to **aggregate messages over time** (e.g., collect 100 messages or wait 5 minutes), use a **Durable Orchestrator**:

```csharp
[FunctionName("BatchOrchestrator")]
public static async Task<List<string>> RunOrchestrator(
    [OrchestrationTrigger] IDurableOrchestrationContext context)
{
    var messages = new List<string>();
    var deadline = context.CurrentUtcDateTime.AddMinutes(5);

    while (context.CurrentUtcDateTime < deadline && messages.Count < 100)
    {
        var msg = await context.WaitForExternalEvent<string>("ServiceBusMessage");
        messages.Add(msg);
    }

    await context.CallActivityAsync("ProcessBatch", messages);
    return messages;
}
```

Then use a **function to send events into the orchestration** from Service Bus.

#### 3. ✅ Better Error Handling & Retry Policies
Standard allows **custom retry policies** on actions:
```json
"retryPolicy": {
  "type": "exponential",
  "interval": "PT30S",
  "count": 6
}
```

---

### ✅ Deployment Options (Standard Only)

You can deploy your Standard Logic App using:
- **Visual Studio / VS Code**
- **ARM Templates**
- **Bicep**
- **Terraform**
- **CI/CD pipelines** (GitHub Actions, Azure DevOps)

This makes it easier to **manage and version-control** your batch logic.

---

### ✅ Best Practice: Use Hybrid Approach (Standard + Functions)

For **high-performance batching**, combine:
- **Azure Function (Service Bus Trigger)** → natively receives `Message[]` batch
- Then call a **Logic App (Standard)** for orchestration (e.g., transform, route, call APIs)

```csharp
[FunctionName("ProcessBatch")]
public async Task Run(
    [ServiceBusTrigger("myqueue", MaxConcurrentCalls = 5)] Message[] messages,
    ILogger log)
{
    var client = new HttpClient();
    var payload = new { messages = messages.Select(m => Encoding.UTF8.GetString(m.Body)) };
    await client.PostAsJsonAsync("https://your-logicapp.azurewebsites.net/api/process-batch", payload);
}
```

Then your Logic App handles the orchestration.

---

### ✅ Summary: Does It Work?

| Question | Answer |
|--------|--------|
| **Can Standard Logic Apps batch-read Service Bus messages?** | ✅ Yes — same as Consumption using Peek-Lock + loop |
| **Any advantages over Consumption?** | ✅ Yes: better performance, code, Durable Functions, VNet, retries |
| **Recommended for high-volume batching?** | ✅ Yes — especially when combined with Functions or Durable Tasks |
| **Can I use the same JSON workflow?** | ✅ Yes — just deploy it in a Standard app |

---

### 🛠️ Next Steps

If you're using **Standard**, consider:
1. Using **Peek-Lock + ForEach** (as shown) for simple batching.
2. Offloading heavy lifting to **Azure Functions**.
3. Using **Durable Functions** for stateful, time-based batching.
4. Adding **Application Insights** for monitoring message throughput.

---

Would you like:
- A **Visual Studio project template** for Standard Logic App with batching?
- A **Durable Function + Logic App** integration example?
- A **Bicep/ARM template** to deploy this?

Let me know — happy to help! 🚀