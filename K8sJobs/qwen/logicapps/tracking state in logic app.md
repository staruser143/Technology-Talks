Great question! Tracking **which reminder emails have been sent** (e.g., day 3, 5, etc.) is essential for auditability, avoiding duplicates, and ensuring correct business logic. Since your workflow runs over **up to 12 days**, you need a **reliable and persistent way to track this state** — especially because transient memory (like variables in a running instance) isn't safe across restarts or long delays.

Let’s explore **where and how** to track reminder status in your **Azure Logic Apps + Service Bus** solution.

---

## ✅ Best Answer: **Use External Persistent Storage**

> 💡 **You should NOT rely on in-memory variables** in Logic Apps for long-running workflows — even though Logic Apps Standard supports state persistence, **loop control and conditional logic based on side effects (like "did I send day 3 email?") are not automatically tracked** unless you explicitly persist them.

### 🔧 Recommended: Store Reminder State in **Azure SQL Database**, **Cosmos DB**, or **Azure Table Storage**

These services provide:
- Durable, queryable storage
- Low-latency reads/writes
- Integration with Logic Apps via connectors
- Audit trail capability

---

## 🗂️ Suggested Data Model (Example: Cosmos DB or SQL Table)

```json
{
  "applicationId": "APP-7890",           // Partition key
  "agentEmail": "agent@broker.com",
  "signatureReceived": false,
  "signatureReceivedAt": null,
  "remindersSent": [
    {
      "day": 3,
      "sentAt": "2025-04-01T10:00:00Z",
      "subject": "Reminder: Please sign application APP-7890"
    },
    {
      "day": 5,
      "sentAt": "2025-04-03T10:00:00Z",
      "subject": "Second reminder: Action required"
    }
  ],
  "status": "WAITING_FOR_SIGNATURE",
  "createdAt": "2025-03-30T08:00:00Z",
  "ttl": 15552000  // Optional: auto-expire after ~180 days (Cosmos DB)
}
```

---

## 🔄 How the Workflow Uses This

### 1. **On Workflow Start (First Run)**
- Insert a new record into the tracking store with:
  ```json
  applicationId, agentEmail, remindersSent: []
  ```

### 2. **Before Sending Each Reminder**
- **Check** if reminder for day X is already in `remindersSent[]`
- If **not present**, send email and **append** the record:
  ```json
  { "day": 3, "sentAt": "..." }
  ```

> This prevents duplicate emails if the Logic App restarts or retries.

### 3. **On Signature Callback**
- Update record: `signatureReceived: true`, `signatureReceivedAt: utcNow()`

### 4. **On Timeout (after 12 days)**
- Update `status: PENDING_APPROVAL`

---

## 🎯 Why This Approach Is Robust

| Benefit | Explanation |
|-------|-------------|
| ✅ **Idempotency** | Prevents duplicate reminders |
| ✅ **Audit trail** | Full history of communications |
| ✅ **Resilience** | Survives app restarts, outages, retries |
| ✅ **Queryability** | Admins can check: “Which apps are waiting? Did we send reminders?” |
| ✅ **Integration** | Easy to connect to Power BI, dashboards, or CRM |

---

## 🛠️ Implementation Options

### Option 1: **Azure Cosmos DB** (Recommended)
- Schemaless, low-latency, global distribution.
- Use **Logic App connector** to `Get`, `Upsert`, or `Query` documents.

```text
Action: Get document by applicationId
Action: If day 3 not in remindersSent → Send email → Append day 3
Action: Upsert document
```

### Option 2: **Azure SQL Database**
- Use **stored procedures** or direct `INSERT`/`UPDATE` via Logic Apps.
- Good if you already have a SQL backend.

### Option 3: **Azure Table Storage**
- Lightweight, cost-effective.
- Less flexible than Cosmos DB but sufficient for simple tracking.

---

## ⚠️ Alternative (Not Recommended): In-Workflow Variables Only

You might be tempted to use a **workflow-scoped variable** like:

```json
"variables": [
  {
    "name": "RemindersSent",
    "type": "array",
    "value": []
  }
]
```

❌ **Why this is risky:**
- If the Logic App restarts (e.g., deployment, platform update), **in-memory variables may not be restored reliably** across long delays.
- You cannot **query or audit** this data externally.
- Hard to debug or monitor.

> ❌ Avoid relying solely on variables for critical tracking in long-running workflows.

---

## ✅ Pro Design: Combine Stateful Workflow + External Tracking

| Component | Role |
|--------|------|
| **Logic App (Standard)** | Orchestrates flow, handles delays, callbacks |
| **External Store (Cosmos DB / SQL)** | Tracks reminder history, audit log |
| **Service Bus** | Triggers initial workflow |
| **Email Service** | Sends signature requests and reminders |

---

## 📌 Example: Logic App Step – "Send Day 3 Reminder"

```plaintext
Action: Get Tracking Record (by applicationId)
Condition: Contains remindersSent where day == 3?
  No →
      Action: Send Email (Day 3 Reminder)
      Action: Append { day: 3, sentAt: utcNow() } to remindersSent
      Action: Upsert Tracking Record
  Yes →
      (Skip — already sent)
```

Repeat for days 5, 7, 9, 11.

---

## 🧩 Bonus: Automate Cleanup
- Use **Cosmos DB TTL** or a **scheduled Azure Function** to archive old records after 6–12 months.

---

## ✅ Summary: Where to Track Reminder Status?

> ✅ **Use a persistent external store** like:
- **Azure Cosmos DB** (best for flexibility and scale)
- **Azure SQL Database** (if already in your stack)
- **Azure Table Storage** (low-cost, simple)

> ❌ **Do not rely only on Logic App variables** for audit-critical, long-running state.

---

## 🛠️ Want Help Setting This Up?

I can provide:
- A **Cosmos DB container schema**
- **Logic App JSON** for upserting reminder status
- **Terraform/ARM template** for infrastructure
- Sample **Power Automate or API** to view pending apps

Let me know!