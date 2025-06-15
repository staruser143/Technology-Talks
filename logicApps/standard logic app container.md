Here’s a **diagram** showing a **Standard Logic App** hosting **multiple workflows**, each with its own trigger and set of actions:

![Standard Logic App with Multiple Workflows](blob:https://m365.cloud.microsoft/2aa60437-7d29-491f-aba0-5ff4c4dd31a2)

### 🧩 Diagram Highlights:
- **Workflow 1**: Triggered by HTTP, sends an email and logs to storage.
- **Workflow 2**: Triggered by a timer, fetches and transforms data.
- **Workflow 3**: Triggered by Service Bus, parses a message and stores it in a database.

This illustrates how you can organize and run diverse workflows within a single Standard Logic App environment.

