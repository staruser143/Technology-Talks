Azure Logic Apps supports a rich set of workflow and integration patterns to enable low-code/no-code orchestration of applications, services, and data across cloud and on-premises environments. Below is a categorized list of the key patterns Logic Apps supports:


---

🧩 1. Workflow Patterns

These define how processes are orchestrated:

Pattern	Description

Sequential Workflow	Executes steps in a linear order.
Parallel Execution	Executes multiple branches concurrently using Parallel control.
Conditional Execution	Uses If-Else branches based on expressions or values.
Loops	Iterates over items (For Each), repeats until a condition is met (Until).
Retry Pattern	Built-in retry policies on connectors in case of transient failures.
Scoped Execution (Try-Catch-Finally)	Uses Scope actions to implement error handling and clean-up steps.



---

🔁 2. Integration Patterns

These define how systems and services are integrated:

Pattern	Description

Request-Reply (Synchronous)	HTTP trigger + response. Used for APIs or synchronous integrations.
Fire-and-Forget (Asynchronous)	Triggered by events or messages; doesn’t expect immediate response.
Publish-Subscribe	Uses Azure Event Grid, Service Bus Topics for event-driven architectures.
Queue-Based Load Leveling	Uses Azure Service Bus or Storage Queues to handle traffic spikes.
Command Pattern	Sends commands to downstream systems via connectors or queues.
Event Sourcing / CQRS	Can be used to orchestrate event flows and projections.
Polling Pattern	Scheduled trigger or polling connector (e.g., SQL, FTP, Blob) to check for new data.
Push Trigger	Event-based trigger (e.g., HTTP, Event Grid, Service Bus message arrival).



---

🛡 3. Error Handling Patterns

To build resilient workflows:

Pattern	Description

Try-Catch-Finally with Scope	Gracefully handles errors and applies compensations.
Dead Letter Handling	Uses Service Bus dead-letter queues or logs failed runs.
Timeouts and Time-based Cancellations	Configurable per action or using Until/Delay/Timeout controls.



---

📡 4. Hybrid Integration Patterns

For on-premise and cloud interoperability:

Pattern	Description

On-Premises Gateway	Connect to on-prem SQL, SAP, file shares, Oracle, etc. via data gateway.
Hybrid Polling	Poll on-prem resources using gateway + schedule trigger.
Secure DMZ Pattern	Logic App in cloud talks to on-prem via hybrid network and gateway.



---

🌐 5. B2B and EDI Patterns

Using Logic Apps with Enterprise Integration Pack (EIP):

Pattern	Description

EDI X12/EDIFACT Message Processing	Parse, validate, and route messages (e.g., 850, 810) via integration account.
AS2/EDI over HTTP/SFTP	Secure message exchange with trading partners.
Partner Agreements & TPM	Define relationships with external partners for B2B communication.



---

🏗 6. Composite and Long-running Workflow Patterns

Pattern	Description

Human-in-the-Loop	Uses approval connectors (e.g., Outlook, Teams) for manual decision-making.
Chained Workflows	One Logic App calls another; allows modular orchestration.
Stateful Orchestration	Maintains state between steps across hours/days/weeks.
Durable Timer Pattern	Delays and waits (e.g., for a condition or a timer) within long-running workflows.



---

🔄 7. Integration with DevOps / CI-CD

Pattern	Description

Infrastructure as Code	Logic Apps defined in ARM templates or Bicep.
CI/CD with GitHub Actions / Azure DevOps	Automate deployment and testing of workflows.
Versioning & Change Control	Source control integration and export/import options.



---

If you’re looking for specific use cases (like invoice approvals, file movement, CRM updates, etc.), I can help map them to the appropriate patterns and connectors Logic Apps offers. Let me know!

