
# Webhook Integration in Azure Logic Apps

Azure Logic Apps supports two primary webhook scenarios for integrating with external systems: **Webhook Trigger (Inbound)** and **Webhook Registration (Outbound)**. Each serves different purposes and is suited for specific use cases.

---

## 🔁 Webhook Trigger (Inbound)

### 📌 What It Is
- Logic App **waits for an external system** to send an HTTP POST request to a **callback URL**.
- This URL is generated when you use the **HTTP Request trigger** in Logic Apps.

### 📥 How It Works
1. Configure a Logic App with an HTTP trigger.
2. Logic App provides a unique callback URL.
3. External system sends data to this URL when an event occurs.
4. Logic App starts immediately and processes the incoming data.

### ✅ Use Cases
- Receive payment confirmation from a gateway.
- Trigger workflow when a new issue is created in GitHub.
- Start a process when a form is submitted.

---

## 🔁 Webhook Registration (Outbound)

### 📌 What It Is
- Logic App **registers a webhook** with an external service.
- The external service **calls back** to Logic Apps when a specific event occurs.

### 📤 How It Works
1. Logic App uses the **Webhook action** to register a callback URL with another system.
2. External system stores this URL and sends a notification when the event happens.
3. Logic App **pauses** and waits for the callback.
4. Once the callback is received, Logic App **resumes** and continues the workflow.

### ✅ Use Cases
- Wait for a long-running job to complete (e.g., file conversion, report generation).
- Monitor external systems that support webhook registration (e.g., approval systems).
- Integrate with services that use asynchronous callbacks.

---

## 🔍 Key Differences

| Feature                     | Webhook Trigger (Inbound)        | Webhook Registration (Outbound)     |
|-----------------------------|----------------------------------|-------------------------------------|
| **Initiated by**            | External system                  | Logic App                           |
| **Workflow behavior**       | Starts immediately               | Pauses and resumes                  |
| **Use case type**           | Event-driven start               | Asynchronous wait                   |
| **Callback URL**            | Provided by Logic App            | Registered with external service    |
| **Common trigger type**     | HTTP Request                     | Webhook action                      |

---

