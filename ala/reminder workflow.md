**Short answer:** Use a **stateful** Logic Apps workflow.

**Why (mapped to your scenario):**

1) **Long‑running, durable process (12‑day SLA).**  
   Stateless runs are meant to be short‑lived (in‑memory) and are optimized for low latency, while **stateful** runs persist state externally and are designed for long‑running orchestration. Microsoft’s guidance and limits documentation differentiate these modes and note the durability + storage trade‑off for stateful versus the performance character of stateless.[1](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare)[2](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-limits-and-config)

2) **Timers & reminders (every 2 days) with guaranteed resumption.**  
   The **Delay** / **Delay until** actions are the supported way to pause a workflow for hours/days; with **stateful** runs, the engine persists the wait so the workflow resumes even after platform restarts—ideal for your 2‑day reminder cadence and a 12‑day deadline.[3](https://learn.microsoft.com/en-us/azure/connectors/connectors-native-delay)

3) **Auditability & troubleshooting (capture comments, track decisions).**  
   You get full **run history** (trigger, inputs/outputs per action) to satisfy audits and post‑mortems—very useful when a manager adds comments or if the approval times out.[3](https://learn.microsoft.com/en-us/azure/connectors/connectors-native-delay)

4) **Clear terminal outcomes (APPROVED/REJECTED/PENDING).**  
   With stateful orchestration you can model a final **timeout path** that marks the order as **PENDING** after 12 days, and you can re‑run or resume with the same inputs if needed.[2](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-limits-and-config)[3](https://learn.microsoft.com/en-us/azure/connectors/connectors-native-delay)


---

## How I’d design it (stateful workflow)

**Trigger:** When an order is created (e.g., HTTP request from the web app, Service Bus/Event Grid, or a database change).[4](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-overview)

**Orchestration steps:**

1. **Initialize context**: `orderId`, `managerEmail`, `dueDate = utcNow()+P12D`, `status = "WAITING"`.  
2. **Send initial approval email** to the manager with a **deep link** back to your web app (include `orderId`/correlation ID in the URL). (Use Office 365 Outlook or SMTP connector.)[4](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-overview)  
3. **Parallel branches**:  
   - **Branch A – Await approval event**:  
     - Your web app posts the decision to a **callback endpoint** (Logic Apps request trigger) *or* emits a message to **Service Bus/Event Grid** that the workflow listens for.  
     - On receipt, set `status = "APPROVED"` or `"REJECTED"`, store `managerComments`, then **terminate** the run.[4](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-overview)  
   - **Branch B – Reminder & timeout**:  
     - `Until now() >= dueDate or status != "WAITING"`  
       - **Delay 2 days** → **Send reminder email** (includes the same approval link).  
     - If the loop exits because `now() >= dueDate` and still `status == "WAITING"`, set `status = "PENDING"` and **terminate**. (Delay/Delay‑until actions are supported; persistence survives restarts.)[3](https://learn.microsoft.com/en-us/azure/connectors/connectors-native-delay)  

**Why this works:** The state (timers, variables, correlation IDs) is **persisted** outside the runtime, so the 12‑day wait and 2‑day reminders are reliable even if the platform scales or restarts; you keep a full run history for compliance and support.[2](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-limits-and-config)[3](https://learn.microsoft.com/en-us/azure/connectors/connectors-native-delay)

---

## Hosting note (optional)

- If you’re happy with a simple, pay‑per‑execution model and don’t need single‑tenant/VNET features, **Logic Apps Consumption** is fine here (it’s **stateful** by default).  
- If you need single‑tenant isolation, VNET/private endpoints, or want to place multiple workflows in one app and tune the runtime, choose **Logic Apps Standard** (supports **both** stateful and stateless).[1](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare)

---

## Quick checklist for implementation

- **Use stateful workflow** with **Delay/Delay until** for reminders and deadline.[3](https://learn.microsoft.com/en-us/azure/connectors/connectors-native-delay)  
- **Persist decision** and comments to your DB when the web app calls back.[4](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-overview)  
- **Add run‑history monitoring & alerts** (failures, timeouts) for operations.[3](https://learn.microsoft.com/en-us/azure/connectors/connectors-native-delay)  
- **Set/verify run duration & retention** to cover the 12‑day SLA (defaults are generous for stateful; you can adjust in Standard via settings if needed).[2](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-limits-and-config)

