Yes, you're absolutely correct — the **daily reminder workflow** should be **separate from the main workflow** that initiates the eSignature request. Here's how the separation works and why it's beneficial:

---

### 🧩 **Workflow Separation Overview**

#### ✅ **Main Workflow: Signature Request**
- **Trigger**: Agent submits an enrollment application.
- **Actions**:
  - Generate the enrollment document.
  - Send it to the eSignature platform (e.g., DocuSign).
  - Store the request ID and submission date.
  - Mark the application as `Awaiting Signature`.

#### ✅ **Reminder Workflow: Periodic Follow-Up**
- **Trigger**: Daily recurrence.
- **Actions**:
  - Query applications with `Awaiting Signature`.
  - Calculate days since submission.
  - Send reminders on Days 1, 3, 5, 7, 11.
  - Update reminder history.
  - Optionally mark as `Pending – Signature Not Received` after Day 15.

---

### 🧠 **Why Separate Workflows Are Better**

| **Reason** | **Benefit** |
|------------|-------------|
| **Modularity** | Easier to maintain and update each workflow independently |
| **Scalability** | Reminder logic can scale separately from submission logic |
| **Efficiency** | Avoids long-running or overly complex workflows |
| **Monitoring** | Each workflow can have its own diagnostics and alerts |
| **Flexibility** | You can change reminder logic without affecting submission flow |

---
