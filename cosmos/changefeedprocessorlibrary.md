## What is Change Feed Processor Library and should we use it

 The **Change Feed Processor Library** is a higher-level library provided by Azure Cosmos DB that simplifies working with the Change Feed. It manages:

- **Partition distribution:** Automatically distributes work across multiple hosts.
- **Checkpointing:** Tracks progress so you don’t reprocess changes after restarts.
- **Scaling:** Handles scaling out by adding more processors.

**Should we use it?**  
Yes, if we need to reliably process changes at scale, across multiple partitions, or want built-in checkpointing and fault tolerance, we should use the Change Feed Processor Library.  
For simple, single-instance, or demo scenarios, direct polling may suffice.

**Docs:**  
[Change Feed Processor in Azure Cosmos DB](https://learn.microsoft.com/en-us/azure/cosmos-db/change-feed-processor)

## How can we use Change Feed Processor library with NestJS?



> **Note:** The Change Feed Processor is now part of the `@azure/cosmos` SDK (v3+). We don’t need a separate package.

**Example: NestJS Service Using Change Feed Processor**

````typescript
import { Injectable, Logger, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { CosmosClient, ChangeFeedProcessor, ChangeFeedProcessorOptions } from '@azure/cosmos';

@Injectable()
export class CosmosChangeFeedService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(CosmosChangeFeedService.name);
  private client: CosmosClient;
  private processor: ChangeFeedProcessor;

  async onModuleInit() {
    this.client = new CosmosClient({
      endpoint: process.env.COSMOS_ENDPOINT,
      key: process.env.COSMOS_KEY,
    });

    const database = this.client.database('your-database');
    const monitoredContainer = database.container('your-monitored-container');
    const leaseContainer = database.container('your-lease-container'); // Must exist

    const handleChanges = async (changes: any[], _context: any) => {
      for (const change of changes) {
        this.logger.log(`Change detected: ${JSON.stringify(change)}`);
        // Your business logic here
      }
    };

    this.processor = monitoredContainer.items
      .getChangeFeedProcessor('my-processor', handleChanges, leaseContainer, {
        startFromBeginning: true, // or false to start from now
      } as ChangeFeedProcessorOptions);

    await this.processor.start();
    this.logger.log('Change Feed Processor started');
  }

  async onModuleDestroy() {
    if (this.processor) {
      await this.processor.stop();
      this.logger.log('Change Feed Processor stopped');
    }
  }
}
````

**Key Points:**
- **Lease Container:** We must create a separate container for leases (state tracking).  
- **Monitored Container:** The container we want to watch for changes.
- **Processor Name:** Must be unique per processor group.
- **handleChanges:** Our callback for processing detected changes.

**Register the service** in the  module’s providers.

**References:**
- [Azure Cosmos DB Change Feed Processor SDK docs](https://learn.microsoft.com/en-us/azure/cosmos-db/change-feed-processor)
- [@azure/cosmos Change Feed Processor API](https://learn.microsoft.com/en-us/javascript/api/@azure/cosmos/changefeedprocessor?view=azure-node-latest)

## How Change Feed Processor Library works?

 
1. **CosmosClient Initialization**  
   The service creates a `CosmosClient` using the  Cosmos DB endpoint and key.

2. **Container References**  
   - `monitoredContainer`: The Cosmos DB container we want to watch for changes.
   - `leaseContainer`: A separate container used by the processor to store its state (leases and checkpoints). This must exist before running the processor.

3. **Change Feed Processor Setup**  
   - The `getChangeFeedProcessor` method creates a processor with:
     - A unique processor name (`'my-processor'`).
     - A callback function (`handleChanges`) that is called whenever new changes are detected in the monitored container.
     - The lease container for state management.
     - Options (e.g., `startFromBeginning` to process all existing changes or only new ones).

4. **Change Handling**  
   - The `handleChanges` function receives an array of changed documents. We can add your business logic here to process each change.

5. **Lifecycle Management**  
   - On module initialization (`onModuleInit`), the processor is started and begins listening for changes.
   - On module destruction (`onModuleDestroy`), the processor is stopped gracefully.

**Summary:**  
This service automatically listens for changes in a Cosmos DB container and processes them using the logic, handling partitioning, scaling, and checkpointing for us.
It integrates seamlessly with NestJS’s lifecycle hooks.

## How would the processor name be unique, If there are multiple instances of NestJS service for High availability.

The **processor name** in the Change Feed Processor identifies a *processor group*—not an individual instance.
All instances of the service that use the **same processor name** and **same lease container** will automatically coordinate and share the work of processing changes across
partitions.

**How it works:**
- Multiple NestJS service instances (e.g., in a scaled-out deployment) use the same processor name and lease container.
- The Change Feed Processor library uses the lease container to coordinate which instance processes which partition.
- If an instance goes down, others take over its leases, ensuring high availability and no missed changes.

**We do NOT need to make the processor name unique per instance.**  
Instead, keep it the same for all instances in the same logical processor group.

**Summary Table:**

| Scenario                        | Processor Name | Lease Container | Behavior                                 |
|----------------------------------|---------------|----------------|------------------------------------------|
| Multiple instances, same group   | Same          | Same           | Workload is balanced and coordinated     |
| Different logical processors     | Different     | Same or Diff   | Each group processes changes separately  |

**References:**  
- [Change Feed Processor - High Availability](https://learn.microsoft.com/en-us/azure/cosmos-db/change-feed-processor#high-availability)

## is it possible that the changes can be read at same time by more than one instance of the NestJS SErvice.

 No, it is **not possible** for the same change to be processed simultaneously by more than one instance **when using the Change Feed Processor with the same processor name and lease container**.

**How it works:**
- The Change Feed Processor uses the lease container to coordinate partition ownership.
- Each partition’s changes are assigned to only one processor instance at a time.
- If an instance fails, its leases are taken over by others, but **no two instances process the same partition’s changes at the same time**.

**Result:**  
Each change is delivered to only one instance for processing, ensuring no duplicate processing across instances in the same processor group.

## Is there a possibility of duplicate processing happening here?

 Yes, **duplicate processing is possible** in some scenarios, even with the Change Feed Processor. This can happen if:

- An instance processes a batch of changes but **fails or crashes before checkpointing** (updating its progress in the lease container).
- When another instance takes over, it will **reprocess changes since the last checkpoint**.

**Best Practice:**  
Your change processing logic should be **idempotent**—able to safely handle the same change more than once without adverse effects.

**Summary:**  
- The processor minimizes duplicates, but they can occur after failures.
- Always design your handlers to tolerate duplicate events.

## How do I design the change processing logic to be idempotent so that there are no adverse effects

 To design your change processing logic to be **idempotent** (safe to process the same change more than once), follow these best practices:

### 1. Use Unique Identifiers
Each change (document) in Cosmos DB has a unique `id`. Use this to track which changes you’ve already processed.

### 2. Store Processing State
Maintain a record (in a database or cache) of processed change IDs or operation results. Before processing, check if the change has already been handled.

### 3. Upsert Instead of Insert
Use **upsert** (update or insert) operations instead of plain inserts. This way, reprocessing the same change just updates the record without creating duplicates.

### 4. Avoid Side Effects
If your logic triggers external actions (emails, payments, etc.), ensure those actions are only performed once per change ID.

### 5. Example Pattern

````typescript
// Pseudocode for idempotent processing
async function handleChange(change) {
  const alreadyProcessed = await processedChangesRepo.exists(change.id);
  if (alreadyProcessed) {
    return; // Skip duplicate
  }

  // Perform your processing logic here

  await processedChangesRepo.markAsProcessed(change.id);
}
````

### 6. Use Transactions Where Possible
If your data store supports transactions, use them to atomically process the change and record its ID as processed.

---

**Summary:**  
- Track processed changes by unique ID  
- Use upserts and checks to avoid duplicate effects  
- Make external actions idempotent  
- Use transactions if available

This ensures your system remains consistent even if the same change is processed multiple times.

## I want to use MongoDB Atlas as the destination data store for my scenario where the event sourced container is monitored by Change Feed Processor Library and then upserts are done on domain data store in Atlas MongoDB. Would this support the requirements of idempotency

 Yes, using **MongoDB Atlas** as your destination data store can fully support idempotency for your scenario, provided you design your upsert logic correctly.

### How to Achieve Idempotency with MongoDB

1. **Use a Unique Identifier:**  
   Each event/change from Cosmos DB should have a unique identifier (e.g., the document’s `id` or a dedicated event ID).

2. **Upsert Operation:**  
   Use MongoDB’s [`updateOne` with `upsert: true`](https://www.mongodb.com/docs/manual/reference/method/db.collection.updateOne/#upsert-parameter) to insert or update the document based on the unique identifier.  
   This ensures that if the same event is processed more than once, it will not create duplicates or cause adverse effects.

3. **Example Upsert Code:**

````typescript
// Example using the MongoDB Node.js driver
await mongoCollection.updateOne(
  { eventId: change.id }, // Filter by unique event ID
  { $set: { ...change, processedAt: new Date() } }, // Update fields
  { upsert: true }
);
````

4. **External Side Effects:**  
   If your processing logic triggers external actions (emails, notifications, etc.), ensure those actions are also idempotent—e.g., by checking if they’ve already been performed for the event ID.

---

**Summary:**  
- MongoDB’s upsert operations are ideal for idempotent writes.
- Always use a unique identifier from the event as the upsert key.
- This approach ensures that duplicate processing does not result in duplicate or inconsistent data.

You are well-supported for idempotency with this architecture.

Sri:  Don't I need to check if the event has already be processed and upserted in the desintation data store or not?

 With MongoDB’s **upsert** operation, you generally do **not** need a separate check before writing. The upsert itself acts as both the check and the write in a single atomic operation.

**How it works:**
- If a document with the unique event ID already exists, it is updated.
- If it does not exist, it is inserted.
- This prevents duplicates and ensures idempotency.

**Example:**

````typescript
await mongoCollection.updateOne(
  { eventId: change.id }, // Filter by unique event ID
  { $set: { ...change, processedAt: new Date() } },
  { upsert: true }
);
````

**When would you need an explicit check?**
- If you need to perform side effects (like sending an email) only on the first processing, you may want to check if the event was already processed before triggering the side effect.
- For pure data upserts, the upsert operation is sufficient and atomic.

**Summary:**  
- For data writes, upsert is enough—no separate check needed.
- For side effects, consider an explicit check to avoid repeated actions.

##  Since there can be multiple instances of Cosmos DB For NoSQL Change Feed Processor Library in NestJS service, is there a possiblity 
that the same event could have be processed by one instance earlier and now it can also be duplicated?

 When using the Cosmos DB Change Feed Processor Library **correctly** (same processor name and lease container), **each event is assigned to only one instance at a time**.
 However, **duplicate processing can still occur** in certain scenarios:

### When Can Duplicates Happen?
- If an instance processes an event but **crashes or loses connectivity before checkpointing** (updating its progress in the lease container), that event will be reprocessed by another instance.
- During failover or scaling, **leases may be reassigned** and uncheckpointed events can be delivered again.

### What Does Not Happen
- The same event is **not processed simultaneously** by multiple instances.
- The processor coordinates partition ownership to avoid concurrent processing of the same event.

### Implication
- **Duplicate processing is possible, but not concurrent duplicate processing.**
- This is why **idempotent processing logic** is required: so that reprocessing the same event does not cause adverse effects.

---

**Summary:**  
Yes, the same event can be processed more than once (but not at the same time by different instances), especially after failures or restarts. Always design your processing logic to be idempotent.

## Do we need a sequence number field in the event data to ensure the order of the events read by Change Feed Processor Library as well as the order of the upserts in destination data store.

 Including a **sequence number** (or similar ordering field, such as a timestamp or version) in your event data is a **best practice** if you need to guarantee the order of processing and upserts in your destination data store (e.g., MongoDB Atlas).

### Why?
- **Cosmos DB Change Feed** delivers changes in order **within a partition**, but not across partitions.
- If our destination data store or business logic requires strict ordering (e.g., for event sourcing, audit trails, or state transitions), a sequence number helps you:
  - Detect and ignore out-of-order or duplicate events.
  - Apply only the latest event if multiple arrive for the same entity.
  - Reconcile state if events are replayed.

### How to Use It
- Add a `sequenceNumber` or `eventVersion` field to each event.
- When upserting into MongoDB, compare the incoming event’s sequence number with the current stored value.
- Only apply the update if the incoming sequence number is **greater** than the stored one.

### Example Upsert Logic (Pseudocode)

````typescript
// Only update if the incoming event is newer
await mongoCollection.updateOne(
  { entityId: change.entityId, $or: [ { sequenceNumber: { $lt: change.sequenceNumber } }, { sequenceNumber: { $exists: false } } ] },
  { $set: { ...change } },
  { upsert: true }
);
````

### Summary
- **We should include a sequence number** if event order matters for your application.
- This helps ensure correct, idempotent, and ordered state in your destination data store, even if events are delivered or processed out of order.

## If we only need changes in order within a partition but not across partitions, could we avoid adding the sequence no field to event data in cosmos container.

 Yes, **if you only require changes to be processed in order within each partition**, you can rely on Cosmos DB’s Change Feed guarantee:  
> **Change Feed delivers changes in order within a partition key.**

We do **not** need to add a sequence number field to your event data for this scenario. Cosmos DB ensures that events are read and delivered in the order they were written within each partition.

**However:**
- If we ever need to process or reconcile events across multiple partitions in a specific order, a sequence number or timestamp would be necessary.
- For most event sourcing or CDC scenarios where partition-level order is sufficient, Cosmos DB’s built-in ordering is enough.

**Summary:**  
- **Within a partition:** No sequence number needed; Cosmos DB Change Feed preserves order.
- **Across partitions:** Sequence number or timestamp is needed if global order is required.



## When provisioning a **lease container** for the Change Feed Processor Library in Cosmos DB, you should use the following partition key:

```
/id
```

**Why?**
- The Change Feed Processor library uses the `id` property of each lease document as the partition key.
- This ensures even distribution and correct lease management across processor instances.

**How to provision:**
- When creating the lease container, set the partition key path to `/id`.
- The container does not need any special indexing or throughput settings beyond your expected scale.

**Example (Azure Portal or ARM):**
- Partition key path: `/id`

**Reference:**  
[Lease container requirements (Microsoft Docs)](https://learn.microsoft.com/en-us/azure/cosmos-db/change-feed-processor#lease-container-requirements)
