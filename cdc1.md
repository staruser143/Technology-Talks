You're absolutely correct that **all three approaches (CDC, Event Grid, and Event Hubs)** require you to build and manage event processors or consumers to handle changes and update the read model. However, the **complexity** and **management overhead** differ between these approaches due to their underlying mechanisms, scalability characteristics, and integration patterns. Let me break this down further:

---

### **1. CDC (Change Data Capture)**
   - **Complexity in Processing**:
     - **Database-Specific**: CDC is tightly coupled to the database (e.g., Cosmos DB for MongoDB API). You need to understand the database's change feed mechanism and how to process it.
     - **State Management**: You must manage the state of the change feed (e.g., checkpointing) to ensure you don't miss events or reprocess them.
     - **Error Handling**: Handling errors (e.g., transient failures, poison messages) requires custom logic, as the change feed is not inherently designed for robust event processing.
     - **Scaling**: Scaling the event processors can be challenging, as the change feed is tied to the database's partitioning model.

   - **What’s Different**:
     - CDC is **database-centric**, meaning you’re directly tied to the database's capabilities and limitations. This can make it harder to scale or adapt to new requirements (e.g., adding new consumers or integrating with external systems).

---

### **2. Event Grid**
   - **Complexity in Processing**:
     - **Event Schema**: Event Grid uses a standardized event schema, which simplifies event processing compared to database-specific change feeds.
     - **Integration**: Event Grid is designed to integrate seamlessly with other Azure services (e.g., Functions, Logic Apps), reducing the need for custom infrastructure.
     - **Error Handling**: Event Grid provides built-in retry mechanisms and dead-lettering, making error handling easier compared to CDC.
     - **Fan-Out**: Event Grid supports multiple subscribers, so you can easily add new consumers without modifying the event source.

   - **What’s Different**:
     - Event Grid abstracts away the **event distribution mechanism**, allowing you to focus on processing events rather than managing the change feed. This reduces the operational complexity compared to CDC.

---

### **3. Event Hubs**
   - **Complexity in Processing**:
     - **High Throughput**: Event Hubs is designed for high-throughput scenarios, so you need to handle partitioning, scaling, and checkpointing explicitly.
     - **Consumer Groups**: Event Hubs supports multiple consumer groups, enabling fan-out scenarios where different consumers can process the same event stream independently.
     - **Durability**: Events are stored durably in Event Hubs, so you don’t need to worry about losing events during processing.
     - **Error Handling**: Event Hubs provides robust mechanisms for handling failures, including checkpointing and dead-lettering.

   - **What’s Different**:
     - Event Hubs is **event-stream-centric**, meaning it’s designed for large-scale event streaming. While it requires more setup and management than Event Grid, it provides greater flexibility and scalability compared to CDC.

---

### **Key Differences in Complexity**
| **Aspect**                | **CDC**                              | **Event Grid**                     | **Event Hubs**                     |
|---------------------------|--------------------------------------|------------------------------------|------------------------------------|
| **Coupling**              | Tightly coupled to the database     | Decoupled via Event Grid topics    | Decoupled via Event Hubs           |
| **State Management**      | Manual checkpointing required       | Managed by Event Grid              | Manual checkpointing required      |
| **Error Handling**        | Custom logic needed                 | Built-in retries and dead-lettering| Built-in checkpointing and retries |
| **Scaling**               | Limited by database partitioning    | Automatically scales               | Explicit partitioning and scaling  |
| **Fan-Out**               | Single consumer per change feed     | Multiple subscribers supported     | Multiple consumer groups supported |
| **Integration**           | Database-specific                   | Seamless Azure integration         | Flexible, supports external systems|

---

### **Why CDC Might Feel More Complex**
1. **Database-Centric**: CDC requires you to work directly with the database's change feed, which may involve database-specific nuances (e.g., Cosmos DB's change feed API).
2. **State Management**: You need to manually manage the state of the change feed (e.g., checkpointing), which can be error-prone and adds complexity.
3. **Limited Flexibility**: CDC is not designed for fan-out scenarios or integration with external systems, so you may need to build additional infrastructure to achieve these goals.

---

### **Why Event Grid and Event Hubs Might Feel Less Complex**
1. **Abstraction**: Both Event Grid and Event Hubs abstract away the event distribution mechanism, allowing you to focus on processing events rather than managing the change feed.
2. **Built-In Features**: Both services provide built-in features for error handling, scaling, and fan-out, reducing the need for custom logic.
3. **Flexibility**: Both services are designed to integrate with other systems, making it easier to adapt to new requirements (e.g., adding new consumers or integrating with external systems).

---

### **When to Choose Which**
- **CDC**: If you want a simple, low-latency solution and are okay with tight coupling to the database.
- **Event Grid**: If you need decoupling, scalability, and seamless integration with Azure services.
- **Event Hubs**: If you need high throughput, durability, and flexibility for fan-out scenarios.

---

### **Final Thoughts**
While all three approaches require you to build and manage event processors, the **operational complexity** and **flexibility** differ significantly. CDC is simpler in terms of infrastructure but can become complex when scaling or integrating with other systems. Event Grid and Event Hubs, on the other hand, provide more robust and scalable solutions but require additional setup and management. Your choice should depend on your system's scalability, latency, and integration requirements.