## Reconstructing state of Domain Entity
* We can **reconstruct the state of a domain entity** by fetching all events for a specific **domain aggregate ID** from an event store and then **applying the events in sequence**. 
* This is the core idea behind **Event Sourcing**, a pattern commonly used in event-driven systems.

---

### **How Event Sourcing Works**

1. **Event Store**:
   - All events are stored in an **event store**, which acts as the system of record.
   - Each event is associated with a **domain aggregate ID** (e.g., `quoteId`, `enrollmentId`).

2. **Reconstructing State**:
   - To reconstruct the state of a domain entity:
     - Fetch all events for the specific **aggregate ID** from the event store.
     - Apply the events in the order they occurred (based on their **timestamp** or **event sequence number**).
   - The final state of the domain entity is the result of applying all the events sequentially.

---

### **Example: Reconstructing the State of an Insurance Quote**

Let’s use the **insurance domain** example to illustrate how this works.

#### **Aggregate ID**: `quote-123`

#### **Events Stored in the Event Store**:
1. **QuoteRequested**:
   ```json
   {
     "eventId": "event-001",
     "eventType": "QuoteRequested",
     "aggregateId": "quote-123",
     "timestamp": "2023-10-01T12:34:56Z",
     "payload": {
       "customerId": "cust-456",
       "policyType": "Health",
       "coverageAmount": 500000
     }
   }
   ```

2. **QuoteGenerated**:
   ```json
   {
     "eventId": "event-002",
     "eventType": "QuoteGenerated",
     "aggregateId": "quote-123",
     "timestamp": "2023-10-01T12:35:10Z",
     "payload": {
       "premiumAmount": 1000
     }
   }
   ```

3. **QuoteUpdated** (e.g., coverage amount changed):
   ```json
   {
     "eventId": "event-003",
     "eventType": "QuoteUpdated",
     "aggregateId": "quote-123",
     "timestamp": "2023-10-02T10:00:00Z",
     "payload": {
       "coverageAmount": 600000
     }
   }
   ```

4. **QuoteExpired**:
   ```json
   {
     "eventId": "event-004",
     "eventType": "QuoteExpired",
     "aggregateId": "quote-123",
     "timestamp": "2023-10-31T12:34:56Z",
     "payload": {
       "status": "Expired"
     }
   }
   ```

---

### **Reconstructing the State of the Quote**

1. **Fetch All Events for `quote-123`**:
   - Retrieve all events with the aggregate ID `quote-123` from the event store.

2. **Apply Events in Sequence**:
   - Start with an empty state for the quote.
   - Apply each event in the order of their timestamps.

#### **Step 1: Apply `QuoteRequested`**
- Initial state:
  ```json
  {}
  ```
- After applying `QuoteRequested`:
  ```json
  {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 500000
  }
  ```

#### **Step 2: Apply `QuoteGenerated`**
- Current state:
  ```json
  {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 500000
  }
  ```
- After applying `QuoteGenerated`:
  ```json
  {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 500000,
    "premiumAmount": 1000
  }
  ```

#### **Step 3: Apply `QuoteUpdated`**
- Current state:
  ```json
  {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 500000,
    "premiumAmount": 1000
  }
  ```
- After applying `QuoteUpdated`:
  ```json
  {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 600000, // Updated
    "premiumAmount": 1000
  }
  ```

#### **Step 4: Apply `QuoteExpired`**
- Current state:
  ```json
  {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 600000,
    "premiumAmount": 1000
  }
  ```
- After applying `QuoteExpired`:
  ```json
  {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 600000,
    "premiumAmount": 1000,
    "status": "Expired" // Updated
  }
  ```

---

### **Final State of the Quote**
After applying all events, the final state of the quote is:
```json
{
  "quoteId": "quote-123",
  "customerId": "cust-456",
  "policyType": "Health",
  "coverageAmount": 600000,
  "premiumAmount": 1000,
  "status": "Expired"
}
```

---

### **Advantages of Event Sourcing**

1. **Complete Audit Trail**:
   - Every change to the domain entity is recorded as an event, providing a full history of changes.

2. **Temporal Queries**:
   - We can reconstruct the state of the entity at any point in time by replaying events up to that timestamp.

3. **Decoupling**:
   - Events are immutable and self-contained, making the system more resilient to changes.

4. **Scalability**:
   - Event stores can handle high volumes of events, making them suitable for large-scale systems.

5. **Flexibility**:
   - We can introduce new event types or modify the state reconstruction logic without affecting existing events.

---

### **Challenges of Event Sourcing**

1. **Event Versioning**:
   - As the system evolves, the structure of events may change. We need to handle event versioning to ensure backward compatibility.

2. **Performance**:
   - Reconstructing the state by replaying a large number of events can be slow. To mitigate this, we can use **snapshots** (periodically saving the current state and replaying only the events after the snapshot).

3. **Complexity**:
   - Event sourcing introduces additional complexity in terms of event storage, replay, and state reconstruction.

---

### **Snapshotting for Performance Optimization**

To avoid replaying all events every time, we can periodically save a **snapshot** of the current state. For example:
- After every 100 events, save the current state of the entity.
- When reconstructing the state, start from the latest snapshot and replay only the events that occurred after the snapshot.

---

### **Key Takeaways**

1. **Event Sourcing**:
   - Allows us to reconstruct the state of a domain entity by replaying all events for a specific aggregate ID.

2. **Event Payload**:
   - Each event payload should contain only the data relevant to that event.

3. **Advantages**:
   - Provides a complete audit trail, temporal querying, and decoupling.

4. **Challenges**:
   - Requires handling event versioning, performance optimization (e.g., snapshots), and increased complexity.

By using event sourcing, we  can build a robust and flexible system that maintains a complete history of changes and allows for easy state reconstruction.
