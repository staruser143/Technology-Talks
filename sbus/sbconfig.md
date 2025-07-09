Here's a breakdown of the diagram and its key components:

---

### 🧩 **Diagram Components Explained**

#### 1. **Sender**
- Represents your application or service that sends messages to the Azure Service Bus Topic.
- Messages can be either **session-enabled** or **non-session**.

#### 2. **Azure Service Bus Topic**
- The central component that receives messages.
- **Partitioning Enabled**: The topic is split into multiple partitions (e.g., Partition 1, Partition 2, Partition 3).
- Each partition is an independent message broker that stores and processes messages.

#### 3. **Message Distribution**
- **Non-session messages**: These are distributed **randomly** across partitions to balance load.
- **Session-enabled messages**: All messages with the same `SessionId` (e.g., `quoteId`) are routed to the **same partition** to preserve ordering.

#### 4. **Duplicate Detection**
- Each message has a unique `MessageId`.
- Azure Service Bus checks for duplicates within the **duplicate detection history window** (e.g., 10 minutes).
- If a message with the same `MessageId` is received again within that window, it is discarded.

#### 5. **Subscription**
- Consumers read messages from subscriptions attached to the topic.
- If **session support is enabled**, consumers use **session receivers** to read messages in order.
- Each session receiver locks and processes messages for one session at a time.

#### 6. **Receiver**
- Represents the service or worker that processes messages.
- Uses **exponential backoff retry logic** if message processing fails.
- Acknowledges messages after successful processing.

---

### 🔁 Flow Summary

1. Sender sends messages with `SessionId` and `MessageId`.
2. Topic distributes messages across partitions.
3. Session messages go to the same partition.
4. Duplicate messages are filtered.
5. Subscription receives messages.
6. Receiver processes and acknowledges them.

---

