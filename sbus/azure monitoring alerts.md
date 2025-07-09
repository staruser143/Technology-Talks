Great! Here's a step-by-step guide to set up **Azure Monitor alerts** for **dead-lettered messages** in Azure Service Bus.

---

## ✅ Step-by-Step: Azure Monitor Alerts for Dead-Letter Messages

### **Step 1: Enable Diagnostic Settings**

1. Go to your **Azure Service Bus Namespace** in the Azure Portal.
2. Navigate to **"Diagnostic settings"** under **Monitoring**.
3. Click **"Add diagnostic setting"**.
4. Select **"Send to Log Analytics"**.
5. Choose or create a **Log Analytics workspace**.
6. Under **Logs**, check:
   - `ServiceBusLogs`
   - `ServiceBusDeadletterMessages`

> This enables logging of dead-letter events to Log Analytics.

---

### **Step 2: Query Dead-Letter Events in Log Analytics**

Go to your **Log Analytics workspace** and run a query like:

```kusto
AzureDiagnostics
| where ResourceType == "SERVICEBUSNAMESPACES"
| where OperationName == "DeadletterMessage"
| where TimeGenerated > ago(1h)
```

You can customize this to filter by topic, subscription, or message properties.

---

### **Step 3: Create an Alert Rule**

1. In the Log Analytics workspace, click **"New alert rule"**.
2. Set the **condition**:
   - Use the query above.
   - Set a **threshold**, e.g., more than 5 dead-lettered messages in 10 minutes.
3. Set the **action group**:
   - Choose how you want to be notified: **Email**, **SMS**, **Webhook**, **Logic App**, etc.
4. Name and save the alert rule.

---

### ✅ Example Use Case

**Alert Name**: `DeadLetterSpikeAlert`  
**Condition**: >10 dead-lettered messages in 15 minutes  
**Action**: Email to DevOps team + webhook to incident management system

---
