Handling **consistency issues due to schema changes** in an **event-sourced system** is a critical challenge. Over time, the structure of events (their schema) may evolve, and you need to ensure that:
1. **Old events** can still be read and processed correctly.
2. **New events** adhere to the updated schema.
3. **Projections** and **read models** can handle both old and new event schemas.

Here’s how to handle schema changes in an event store like **Cosmos DB for NoSQL API**:

---

### **1. Schema Evolution Strategies**

#### **a. Versioning Events**
- **How It Works**:
  - Include a **version number** in each event to indicate its schema version.
  - Example:
    ```json
    {
      "eventID": "event-1",
      "quoteID": "quote-123",
      "eventType": "QuoteCreated",
      "version": 1, // Schema version
      "data": {
        "premium": 1000,
        "coverage": "full"
      }
    }
    ```
  - When the schema changes, increment the version number and update the event structure.
    ```json
    {
      "eventID": "event-2",
      "quoteID": "quote-123",
      "eventType": "QuoteCreated",
      "version": 2, // New schema version
      "data": {
        "premium": 1000,
        "coverage": "full",
        "deductible": 500 // New field
      }
    }
    ```

- **Advantages**:
  - Explicitly tracks schema versions, making it easy to handle old and new events.
  - Backward and forward compatibility can be managed.

- **Disadvantages**:
  - Requires additional logic to handle multiple versions of events.

#### **b. Default Values for New Fields**
- **How It Works**:
  - When adding new fields to the event schema, provide **default values** for old events.
  - Example:
    - Old event:
      ```json
      {
        "eventID": "event-1",
        "quoteID": "quote-123",
        "eventType": "QuoteCreated",
        "data": {
          "premium": 1000,
          "coverage": "full"
        }
      }
      ```
    - New event:
      ```json
      {
        "eventID": "event-2",
        "quoteID": "quote-123",
        "eventType": "QuoteCreated",
        "data": {
          "premium": 1000,
          "coverage": "full",
          "deductible": 500 // New field with default value
        }
      }
      ```
    - When processing old events, assume a default value for the new field (e.g., `deductible = 0`).

- **Advantages**:
  - Simplifies schema evolution by providing default values for missing fields.
  - No need to modify old events.

- **Disadvantages**:
  - Default values might not always be appropriate for all use cases.

#### **c. Schema Migration**
- **How It Works**:
  - Migrate old events to the new schema by rewriting them in the event store.
  - Example:
    - Old event:
      ```json
      {
        "eventID": "event-1",
        "quoteID": "quote-123",
        "eventType": "QuoteCreated",
        "data": {
          "premium": 1000,
          "coverage": "full"
        }
      }
      ```
    - New event (after migration):
      ```json
      {
        "eventID": "event-1",
        "quoteID": "quote-123",
        "eventType": "QuoteCreated",
        "data": {
          "premium": 1000,
          "coverage": "full",
          "deductible": 0 // Added during migration
        }
      }
      ```

- **Advantages**:
  - Ensures all events adhere to the latest schema.
  - Simplifies event processing logic.

- **Disadvantages**:
  - Requires downtime or a complex migration process.
  - Not feasible for large event stores with millions of events.

#### **d. Upcasting**
- **How It Works**:
  - Transform old events into the new schema at runtime when they are read from the event store.
  - Example:
    - Old event:
      ```json
      {
        "eventID": "event-1",
        "quoteID": "quote-123",
        "eventType": "QuoteCreated",
        "data": {
          "premium": 1000,
          "coverage": "full"
        }
      }
      ```
    - Upcast to new schema:
      ```json
      {
        "eventID": "event-1",
        "quoteID": "quote-123",
        "eventType": "QuoteCreated",
        "data": {
          "premium": 1000,
          "coverage": "full",
          "deductible": 0 // Added during upcasting
        }
      }
      ```

- **Advantages**:
  - No need to modify the event store.
  - Flexible and dynamic transformation.

- **Disadvantages**:
  - Adds complexity to the event processing logic.
  - Performance overhead for upcasting at runtime.

---

### **2. Handling Schema Changes in Projections and Read Models**

#### **a. Version-Aware Projections**
- **How It Works**:
  - Projections are aware of the event schema versions and handle them accordingly.
  - Example:
    - If the event version is 1, use the old schema.
    - If the event version is 2, use the new schema.

- **Advantages**:
  - Ensures that projections can handle both old and new events.

- **Disadvantages**:
  - Requires additional logic in projections.

#### **b. Rebuild Projections**
- **How It Works**:
  - When the schema changes, rebuild the projections from the event store.
  - Example:
    - Replay all events (old and new) to rebuild the read model.

- **Advantages**:
  - Ensures that the read model is consistent with the latest schema.

- **Disadvantages**:
  - Time-consuming for large event stores.

---

### **3. Best Practices for Schema Changes**

#### **a. Plan for Evolution**
- Design events with schema evolution in mind (e.g., include a version number).
- Avoid breaking changes whenever possible.

#### **b. Use Versioning**
- Always include a version number in events to track schema changes.

#### **c. Test Thoroughly**
- Test schema changes in a staging environment before deploying to production.
- Ensure that projections and read models can handle both old and new events.

#### **d. Document Changes**
- Maintain documentation of schema changes and their impact on the system.

#### **e. Use Upcasting or Default Values**
- Prefer **upcasting** or **default values** over schema migration to avoid modifying the event store.

---

### **4. Example Workflow**

#### **Step 1: Append Events with Versioning**
- Append events with a version number:
  ```json
  {
    "eventID": "event-1",
    "quoteID": "quote-123",
    "eventType": "QuoteCreated",
    "version": 1,
    "data": {
      "premium": 1000,
      "coverage": "full"
    }
  }
  ```

#### **Step 2: Update Schema**
- Add a new field (`deductible`) to the schema and increment the version number:
  ```json
  {
    "eventID": "event-2",
    "quoteID": "quote-123",
    "eventType": "QuoteCreated",
    "version": 2,
    "data": {
      "premium": 1000,
      "coverage": "full",
      "deductible": 500
    }
  }
  ```

#### **Step 3: Handle Old Events**
- Use **upcasting** or **default values** to handle old events:
  ```json
  {
    "eventID": "event-1",
    "quoteID": "quote-123",
    "eventType": "QuoteCreated",
    "version": 1,
    "data": {
      "premium": 1000,
      "coverage": "full",
      "deductible": 0 // Added during upcasting
    }
  }
  ```

#### **Step 4: Update Projections**
- Ensure projections can handle both old and new events:
  ```python
  def apply_projection(event):
      if event.version == 1:
          # Handle old schema
          premium = event.data["premium"]
          coverage = event.data["coverage"]
          deductible = 0  # Default value
      elif event.version == 2:
          # Handle new schema
          premium = event.data["premium"]
          coverage = event.data["coverage"]
          deductible = event.data["deductible"]
  ```

---

### **5. Summary**
- **Versioning**: Include a version number in events to track schema changes.
- **Default Values**: Provide default values for new fields in old events.
- **Upcasting**: Transform old events into the new schema at runtime.
- **Schema Migration**: Rewrite old events in the event store (not recommended for large systems).
- **Version-Aware Projections**: Ensure projections can handle multiple schema versions.

By following these strategies, you can handle schema changes in your event-sourced system while maintaining consistency and compatibility.
