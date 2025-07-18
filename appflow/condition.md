Certainly! Here's a detailed breakdown of the **conditional logic** used in the overall workflow, specifically focusing on how the system decides whether to initiate the **eSignature workflow** based on who submitted the enrollment application and whether the application qualifies under a **Special Enrollment Period (SEP)**.

---

### 🧠 **Conditional Logic Flow**

#### **Step 1: Enrollment Submission**
- The application is submitted either by:
  - An **agent** on behalf of an individual or group
  - An **individual or group** directly

#### **Step 2: SEP Validation**
- The system checks if the application qualifies for a **Special Enrollment Period**:
  - Validates life event type (e.g., marriage, birth, job loss)
  - Checks if the event occurred within the allowed timeframe (e.g., 60 days)
  - Optionally requests and verifies supporting documentation

#### **Step 3: Conditional Routing Based on Submission Source**

| **Condition** | **Outcome** |
|---------------|-------------|
| SEP is **approved** AND application is submitted by an **agent** | ✅ Trigger the **eSignature workflow** |
| SEP is **approved** AND application is submitted by an **individual or group** | 🚫 **Skip eSignature**, proceed directly to enrollment processing |
| SEP is **not approved** | ❌ Halt or reject the application, notify submitter |

---

### 🔄 **Why This Logic Matters**

- **Compliance**: Ensures only valid SEP applications are processed.
- **Efficiency**: Avoids unnecessary eSignature steps for direct submissions.
- **Automation**: Enables Logic Apps to route applications dynamically based on business rules.

---

### 🧩 **Implementation Tips in Azure Logic Apps**

- Use a **Condition block** after SEP validation:
  ```json
  {
    "and": [
      { "equals": ["@variables('sep_status')", "Approved"] },
      { "equals": ["@variables('submitted_by')", "Agent"] }
    ]
  }
  ```
- If true → call eSignature workflow.
- If false → skip or route to alternate processing.

---

Here’s a **sample Azure Logic App condition block** that checks whether:

1. The **SEP status** is `"Approved"`, and  
2. The application was submitted by an **agent**.

If both conditions are true, the workflow proceeds to trigger the eSignature process.

---

### 🧾 **Sample Condition Block (JSON)**

```json
{
  "type": "If",
  "expression": {
    "and": [
      {
        "equals": [
          "@variables('sep_status')",
          "Approved"
        ]
      },
      {
        "equals": [
          "@variables('submitted_by')",
          "Agent"
        ]
      }
    ]
  },
  "actions": {
    "Trigger_eSignature_Workflow": {
      "type": "Http",
      "inputs": {
        "method": "POST",
        "uri": "https://your-esignature-workflow-endpoint",
        "body": {
          "application_id": "@variables('application_id')",
          "agent_email": "@variables('agent_email')"
        }
      }
    }
  }
}
```

---

### 🧠 Notes:
- You can replace `@variables(...)` with `@items('YourLoopName')?...` if you're inside a loop.
- The `Http` action can call another Logic App, Azure Function, or external API.
- If the condition is false, you can add an `else` block to skip or log the decision.

Here is the complete Azure Logic App JSON template that integrates **SEP validation** and **conditional eSignature triggering** based on whether the application was submitted by an agent:

📄 [Download Logic App JSON Template](blob:https://m365.cloud.microsoft/aa85d675-ac71-4955-8dfa-2b77da66376f)

---

### 🔍 **What This Template Includes**

- **Trigger**: HTTP request with application details.
- **SEP Validation**: Placeholder logic to simulate SEP approval.
- **Conditional Logic**:
  - If SEP is approved **and** submitted by an **agent** → triggers eSignature workflow.
  - If submitted by an **individual/group** → skips eSignature and proceeds.

---

