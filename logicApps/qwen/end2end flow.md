Absolutely! Below is a **detailed end-to-end flow diagram** of your system — from the moment an **agent submits an application**, through **signature capture**, **Logic App orchestration**, and **final processing** — including how messages are handled in **Azure Service Bus** and when they are **removed**.

Since I can't render images directly, I’ll provide:

1. 🖼️ A **Mermaid.js diagram** (you can paste this into any Mermaid-compatible viewer like [Mermaid Live Editor](https://mermaid.live/edit))
2. 📝 A **step-by-step explanation** of each phase
3. 🔍 Key insights on **message lifecycle**, **state**, and **completion**

---

## 🖼️ Mermaid Diagram: End-to-End Flow

```mermaid
graph TD
    A[Kafka Topic<br>(All Applications)] -->|Filter: agent-submitted| B[Azure Service Bus Queue<br>'agent-signature-requests']
    
    B --> C{Logic App Triggered<br>(One instance per message)}
    C --> D[Parse Message:<br>applicationId, agentEmail]
    D --> E[Send Signature Request Email<br>with link to web portal]
    E --> F[Wait for HTTP Callback<br>(State Preserved)]
    
    F --> G[Agent Clicks Link]
    G --> H[Custom Web Portal<br>(Static Web App)]
    H --> I[Show Signature Form]
    I --> J[Agent Signs & Submits]
    J --> K[Azure Function API<br>/api/sign]
    
    K --> L[Save Signature<br>(e.g., MongoDB)]
    L --> M[Call Logic App Callback URL]
    M --> F  %% Back to waiting Logic App
    
    F --> N[Logic App Resumes]
    N --> O[Validate Signature Data]
    O --> P[Continue Processing<br>(e.g., create account, notify HR)]
    P --> Q[Update Status: COMPLETED]
    Q --> R[Workflow Successfully Ends]
    
    R --> S[Service Bus Message: <br>✅ Automatically COMPLETED<br>➡️ Removed from Queue]
    
    F --> T{No Signature in 12 Days?}
    T -- Yes --> U[Timeout (12-day scope)]
    U --> V[Update Status: PENDING_APPROVAL]
    V --> W[Notify Admin]
    W --> R  %% Also completes workflow
    
    style S fill:#d4fcbc,stroke:#2e8b57
    style R fill:#4CAF50,stroke:#fff,color:#fff
    style B fill:#ffd54f,stroke:#ff8f00
    style C fill:#64b5f6,stroke:#1565c0
    style K fill:#03a9f4,stroke:#0277bd
    style L fill:#03a9f4,stroke:#0277bd
    style M fill:#03a9f4,stroke:#0277bd
```

---

## 🔢 Step-by-Step Flow Explanation

| Step | Component | Action | Message/State |
|------|---------|--------|---------------|
| 1 | **Kafka → Service Bus** | Filter agent-submitted apps and send to SB queue | Message enqueued |
| 2 | **Service Bus Queue** | Holds message until processed | `Status: Active` |
| 3 | **Logic App (Standard)** | Triggered by message → **one workflow instance starts** | Message: `Peek-Locked` |
| 4 | **Logic App** | Sends email with link: `https://your-portal.com?app=APP-123` | State: "Waiting for callback" (persisted) |
| 5 | **Agent** | Clicks link → lands on web form | No action on message |
| 6 | **Web Portal** | Renders signature canvas | Frontend only |
| 7 | **Agent** | Signs and submits | POST to `/api/sign` |
| 8 | **Azure Function** | Receives signature, saves to DB | Optional persistence |
| 9 | **Azure Function** | Calls **Logic App callback URL** | Triggers resume |
| 10 | **Logic App** | Resumes from `Wait for HTTP request` | State restored |
| 11 | **Logic App** | Continues processing (e.g., validation, downstream APIs) | Workflow running |
| 12 | **Logic App** | Reaches end (success or timeout) | ✅ Workflow completes |
| 13 | **Service Bus** | Message is **automatically marked as 'Completed'** | 🗑️ **Deleted from queue** |

---

## 🔐 What Happens on Failure?

| Scenario | Outcome |
|--------|--------|
| Workflow crashes or fails | Message lock expires → **reprocessed** (up to max delivery count) |
| Max retries reached | Message moved to **dead-letter queue (DLQ)** for investigation |
| Callback URL called after workflow expired | Returns `410 Gone` — safe, but won’t resume |

> ✅ You can monitor DLQ messages to detect and fix issues.

---

## 🧠 Key Insights

### ✅ Message Removal is Automatic
- You **don’t need to write code** to delete the message.
- Azure Logic Apps calls `Complete()` on the Service Bus message **when the workflow finishes successfully**.

### 🔄 Long Waits Are Safe
- Even if the Logic App waits **12 days** for a callback, the message remains **locked and renewed**.
- As long as the workflow is alive, the message won’t be lost.

### 🛡️ Idempotency Matters
- If a message is reprocessed (due to failure), ensure downstream actions (e.g., email, DB update) are **idempotent**.
- Use `applicationId` as a key to avoid duplicates.

---

## 📎 Tools to Visualize This

1. **Paste the Mermaid code** into:  
   👉 [https://mermaid.live/edit](https://mermaid.live.edit)
2. It will render a clean, interactive diagram.
3. You can export as PNG or SVG for documentation.

---

## 📥 Want This as a PDF or Visio Diagram?

I can help you generate:
- A **PDF version** of this flow
- A **PlantUML** or **Draw.io** (diagrams.net) compatible version
- An **Azure Architecture Center-style diagram**

Just let me know your preferred format!

---

This end-to-end design ensures:
✅ Reliable message processing  
✅ Stateful long-running workflows  
✅ Secure signature capture  
✅ Automatic cleanup of completed messages  

Let me know if you'd like to add **monitoring (Application Insights)**, **alerts**, or **replay capability**!