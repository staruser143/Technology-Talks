Your client's concern stems from the idea that an event streaming platform like Confluent Kafka or Azure Event Hubs is inherently designed to distribute events to multiple consumers, ensuring scalability and flexibility. However, their view that event sourcing should only be done using an event streaming platform is not entirely correct. Here's how you can address their concerns:

Clarifying the Purpose of Event Sourcing vs. Event Streaming

1. Event Sourcing vs. Event Streaming:

Event Sourcing is a pattern where state changes are stored as a sequence of events in an event store, which is the single source of truth for reconstructing system state.

Event Streaming focuses on real-time event distribution, allowing multiple consumers to react to events as they occur.



2. Why Cosmos DB for NoSQL API is a Valid Event Store?

Cosmos DB supports event sourcing by persisting immutable events in order.

It provides Change Feed, which acts as an event stream, allowing consumers to build read models or integrate with other services.

It guarantees strong consistency options and low-latency queries, unlike Kafka/Event Hubs, where querying historical events is less efficient.

It provides high availability and global distribution, making it a robust event store.



3. How External Consumers Can Still Access Events?

Cosmos DB's Change Feed can be published to Kafka/Event Hubs if external consumers beyond your domain need to listen to events in real time.

This approach separates the concern of storing events from distributing them, maintaining a clean event sourcing model while still enabling event streaming.




Addressing Their Concerns with a Hybrid Approach

Instead of treating event sourcing store and event streaming platform as mutually exclusive, you can propose a hybrid architecture:

1. Cosmos DB as the event store (ensuring event immutability and queryability).


2. Use Change Feed to publish events to Event Hubs/Kafka, enabling multiple consumers beyond your domain to react in real time.



When to Use Kafka/Event Hubs Directly as an Event Store?

Using Kafka or Event Hubs directly as an event store works in cases where:

Events are transient and don’t require long-term storage.

Consumers process events in real-time, but event reconstruction/state rebuilding isn't a core requirement.

You need high-throughput event distribution with replayability.


Conclusion

Your client’s point is valid in terms of event distribution, but that doesn’t mean Cosmos DB is unsuitable for event sourcing. Instead of treating these as competing solutions, you can propose a hybrid architecture, using Cosmos DB for event storage and Change Feed to push events to Kafka/Event Hubs. This way, you meet both event sourcing (state reconstruction) and event streaming (external consumers) needs efficiently.

=====

Concern 1: Are Events Appended to Cosmos DB for NoSQL API Immutable?

By default, Cosmos DB for NoSQL API does not enforce immutability for documents. You need to implement immutability at the application level. Here’s how:

1. Use a Strict Append-Only Model:

Each event should be stored as a new document rather than updating an existing one.

Design your schema with an event ID, timestamp, and versioning to prevent overwrites.



2. Leverage Time-to-Live (TTL) for Soft Deletes (If Needed):

If certain events need to be "archived" but not deleted, you can set TTL policies instead of physically removing data.



3. Use Access Control to Prevent Modifications:

Configure Role-Based Access Control (RBAC) to ensure only append operations are allowed while preventing updates/deletes.



4. Partitioning Considerations:

Choose an event stream-oriented partition key (e.g., aggregateId) to ensure related events are stored together and efficiently retrieved.




Concern 2: Does Introducing Kafka/Event Hubs Introduce Latency and Eventual Consistency?

Yes, introducing Kafka or Event Hubs as an additional layer does introduce latency and eventual consistency, but the impact depends on:

1. Change Feed Processing Delay

Cosmos DB’s Change Feed is near real-time but not instant. There’s a slight delay (a few seconds in most cases) before events are picked up and published to Kafka/Event Hubs.

This can cause a lag between event creation in Cosmos DB and its propagation to downstream systems.



2. Eventual Consistency in Domain Data Store

If domain data stores are updated via Kafka/Event Hubs consumers, they will always be eventually consistent, as events take some time to propagate.

The actual delay depends on:

Kafka/Event Hubs throughput settings

Consumer processing speed

Retries, failures, and recovery mechanisms





How to Minimize Latency While Keeping Event Sourcing Integrity?

If strong consistency and low latency for domain updates are priorities, you can consider:

1. Directly Updating the Domain Store from Change Feed

Instead of going through Kafka/Event Hubs, process Change Feed directly to update the domain data store.

This eliminates an additional layer, reducing propagation delay.



2. Use Event Hubs/Kafka Only for External Consumers

Keep the domain data updates within Cosmos DB's Change Feed processing for low latency.

Publish only necessary events to Event Hubs/Kafka for external consumers.



3. Leverage Change Feed with Azure Functions for Near-Real-Time Updates

Azure Functions can react to Cosmos DB’s Change Feed almost instantly, making updates faster compared to a full Kafka/Event Hubs pipeline.




Final Recommendation

If the goal is strict event sourcing with minimal latency, stick to Cosmos DB’s Change Feed for domain updates and only use Kafka/Event Hubs for external consumers.

If event distribution is more important than low-latency domain updates, using Kafka/Event Hubs makes sense but introduces eventual consistency.


====

Concern: Ensuring Event Immutability in Cosmos DB

You’re absolutely right—Cosmos DB does not natively prevent updates to an appended document. Even though we can follow an append-only approach, there’s nothing stopping someone from modifying existing events.

How to Enforce Immutability?

To make sure events remain immutable, you need strict application-level and database-level controls:

1. Use a Unique ID for Every Event Document

Instead of updating an existing document, every new event should be stored as a separate document with a unique ID.

Example:

{
  "id": "event-123",
  "aggregateId": "order-456",
  "type": "ORDER_CREATED",
  "timestamp": "2025-02-12T12:34:56Z",
  "data": { "orderAmount": 100 }
}



2. Set Cosmos DB's etag (Optimistic Concurrency Control) to Prevent Updates

Each document in Cosmos DB has an etag that changes when the document is modified.

Use this to detect unintended modifications and reject updates unless a new event is being written.



3. Use Stored Procedures or Custom Policies to Restrict Updates

Implement a stored procedure that only allows inserts, preventing updates to existing documents.

Example stored procedure logic:

function createEvent(doc) {
  var context = getContext();
  var collection = context.getCollection();
  var request = context.getRequest();

  // Prevent updates by rejecting requests that already exist
  var query = 'SELECT * FROM c WHERE c.id = "' + doc.id + '"';
  var isAccepted = collection.queryDocuments(collection.getSelfLink(), query, function (err, documents, responseOptions) {
    if (err) throw new Error(err);
    if (documents.length > 0) {
      throw new Error('Updates are not allowed.');
    }
    request.setBody(doc);
  });
}



4. Use Cosmos DB Role-Based Access Control (RBAC)

Configure RBAC policies to only allow inserts and prevent update or delete operations.

If using an API layer, enforce it through API permissions instead of allowing direct Cosmos DB access.





---

Concern: Where Should Kafka/Event Hubs Publishing Happen?

You have two main options for publishing events to Kafka/Event Hubs:

Option 1: Publish to Kafka/Event Hubs at the Time of Writing to Event Store

The service that appends the event to Cosmos DB also publishes it to Kafka/Event Hubs.

Pros:
✅ Immediate availability of events in Kafka/Event Hubs for external consumers.
✅ No dependency on Change Feed processing speed.

Cons:
❌ Increases coupling between event writing and external event distribution.
❌ If event publishing fails, you might need a retry mechanism to ensure Kafka/Event Hubs stay in sync.


Option 2: Publish via Change Feed Processing (Preferred for Event Sourcing)

The Change Feed processor, which updates the domain data store, is also responsible for publishing events to Kafka/Event Hubs.

Pros:
✅ Guarantees that events are first persisted in the event store before being published.
✅ Decouples event writing from external event streaming.
✅ Ensures Kafka/Event Hubs consumers receive only committed events.

Cons:
❌ Small delay in event publication (Change Feed latency is a few seconds).
❌ If Change Feed processing fails, events might not be published to Kafka/Event Hubs immediately.



---

Final Recommendation

If external event consumers must receive events immediately, go with Option 1 (Publishing at Write Time).

If strict event sourcing consistency is more important, go with Option 2 (Change Feed Processing for Publishing).


A hybrid approach is also possible:

1. Write to Cosmos DB first.


2. If needed immediately, publish directly to Kafka/Event Hubs.


3. Use Change Feed as a fallback mechanism to detect any failures and re-publish if needed.



This way, you get fast external event propagation while ensuring event sourcing integrity.



====

Yes, you can leverage the Outbox Pattern to ensure that events are appended to the event store (Cosmos DB) and reliably published to Kafka/Event Hubs in a coordinated manner.


---

How the Outbox Pattern Works in This Context

Since Cosmos DB does not support multi-document transactions, we can introduce an Outbox table/collection to store events before they are published to Kafka/Event Hubs. The process would work as follows:

Step 1: Write the Event and Outbox Entry Together

Instead of publishing to Kafka/Event Hubs immediately, the event writer first appends the event to Cosmos DB and also writes an entry into an Outbox collection.

This ensures that event persistence happens first, before event distribution.

Example Outbox entry in Cosmos DB:

{
  "id": "outbox-789",
  "eventId": "event-123",
  "aggregateId": "order-456",
  "type": "ORDER_CREATED",
  "timestamp": "2025-02-12T12:34:56Z",
  "data": { "orderAmount": 100 },
  "status": "PENDING"
}


Step 2: A Background Process Reads the Outbox and Publishes Events

A separate background worker (Azure Function, Kubernetes Job, or a microservice) reads the pending events from the Outbox and publishes them to Kafka/Event Hubs.

After successful publishing, it marks the event as processed or deletes it.


Step 3: Handling Failures & Retrying

If Kafka/Event Hubs is unavailable, the event remains in the Outbox, and the worker retries later.

If an event fails to be published, it remains in a FAILED state, allowing manual reprocessing or automated retries.



---

Advantages of Using the Outbox Pattern Here

✅ Ensures Atomicity: Since event persistence (Cosmos DB write) and event streaming (Kafka/Event Hubs publish) are separate, the event is never lost, even if Kafka/Event Hubs is temporarily unavailable.

✅ Prevents Dual-Writes Problem: If you were writing to Cosmos DB and Kafka/Event Hubs simultaneously, a failure in one system could lead to inconsistent states. The Outbox pattern prevents this.

✅ Improves Reliability & Auditing: You can track which events were successfully published and which failed, allowing retries or manual recovery.

✅ Decouples Event Store from Event Streaming: The service writing to Cosmos DB doesn’t need to worry about failures in Kafka/Event Hubs—it just writes the event, and the Outbox processor handles the rest.


---

Where Should the Outbox Processing Happen?

You have two main choices for processing the Outbox:

Option 1: Use Cosmos DB Change Feed to Process the Outbox

Instead of a separate Outbox processor, Cosmos DB’s Change Feed can act as the Outbox processor by detecting new events and publishing them to Kafka/Event Hubs.

Pros:
✅ No need for separate polling logic.
✅ Change Feed ensures processing happens asynchronously and efficiently.

Cons:
❌ Slight delay (Change Feed is near real-time but not instant).


Option 2: Use a Dedicated Outbox Processor (Polling Model)

A separate microservice or an Azure Function regularly polls the Outbox collection, processes pending events, and publishes them.

Pros:
✅ More explicit control over retry policies and failure handling.
✅ Works even if Change Feed has delays.

Cons:
❌ Requires scheduled execution or event-driven triggering.



---

Final Recommendation

If you want reliability with minimal complexity, use Change Feed to detect new events in the Outbox and publish them to Kafka/Event Hubs.
If you need more control over retries and error handling, use a separate polling-based Outbox processor.

Would you like an architecture diagram for this approach?

