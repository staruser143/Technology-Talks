Absolutely! Here's a **practical example of a stateful workflow** using **Azure Logic Apps Standard**, which fully supports state persistence.

---

### 🎯 Use Case: **Employee Onboarding Workflow**
This workflow runs over several days and maintains state while waiting for approvals and external actions.

---

### ✅ Features Demonstrated:
- **State persistence** across days
- **Waiting for human approval**
- **Calling external systems**
- **Error handling and retries**
- **Checkpointing** after each step

---

### 📝 Workflow Steps

```plaintext
1. Trigger: When a new employee is added (via HTTP request)
2. Send welcome email
3. Create user account in Azure AD
4. Wait for Manager Approval (can take up to 5 days)
5. If approved:
   - Provision laptop via IT system
   - Schedule orientation training
6. Send final onboarding complete email
7. Log completion in HR database
```

---

### 💡 Why This Is Stateful
- The workflow **pauses at step 4** and waits for a human response.
- The **current state is saved** (e.g., employee name, email, timestamp).
- After approval (even days later), the workflow **resumes from where it left off**.
- No need to re-run earlier steps — state is preserved.

---

### 🔧 How It Works in Logic Apps Standard

#### 1. **Trigger: When a HTTP request is received**
```json
{
  "name": "John Doe",
  "email": "john.doe@company.com",
  "managerEmail": "manager@company.com"
}
```

#### 2. **Action: Send Welcome Email**
Using **Office 365 Outlook** or **SendGrid**:
> "Welcome, John! Your onboarding has started."

#### 3. **Action: Create User in Azure AD**
Use the **Azure AD connector** to create a user account.

#### 4. **Action: Wait for Manager Approval**
Use the **Approval action (Azure Logic Apps Standard)** or an **HTTP wait with callback**.

Example using **Azure AD Approval** connector:
- Sends approval request to manager.
- Workflow **pauses** until response.
- State (employee data, progress) is **persisted automatically**.

#### 5. **Condition: If approved**
```plaintext
Yes → 
   - Call IT system (via HTTP or connector) to assign laptop
   - Call Microsoft Teams API to schedule orientation meeting
No → 
   - Send rejection email
   - Log in audit log
```

#### 6. **Final Actions**
- Send "Onboarding Complete" email.
- Insert record into **Azure SQL DB** or **Dataverse**.

---

### 🖼️ Visual Representation (Simplified)
```
HTTP Trigger
     ↓
Send Welcome Email
     ↓
Create Azure AD User
     ↓
Wait for Manager Approval ←──(State persisted for days)
     ↓
[Check Approval Outcome]
     ↓
If Approved → Provision Laptop → Schedule Training
     ↓
Send Completion Email
     ↓
Update HR Database
```

---

### ✅ Key Stateful Behavior
| Step | State Preserved? | Notes |
|------|------------------|-------|
| After HTTP trigger | ✅ | Stores employee data |
| During approval wait | ✅ | Pauses for days; resumes automatically |
| After approval | ✅ | Remembers all prior outputs |
| On failure | ✅ | Can retry or resume from last checkpoint |

---

### 🛠️ Why Use Logic Apps Standard for This?
- **Automatic state persistence** via built-in storage (uses Azure Storage or SQL backend).
- **Durable execution**: Survives app restarts, scaling, or downtime.
- **Monitoring**: You can view the full run history and current state in the Azure portal.

---

### 💡 Pro Tip
You can even **suspend/resume** or **terminate** the workflow manually via the portal or API — all because it’s stateful.

---

### Want to Try It?
1. In the **Azure portal**, create a **Logic App (Standard)**.
2. Choose a **stateful workflow** template.
3. Use the **"Wait for HTTP request"** or **"Approval"** action to simulate delays.
4. Deploy and test with Postman or Power Automate.

---

Let me know if you'd like the **full JSON definition** or a **template** you can import!