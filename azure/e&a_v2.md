When using Azure Cosmos DB for MongoDB API as an event store, managing the lifecycle of events, including expiring and archiving, is crucial for maintaining performance and controlling costs. Here are some strategies you can follow:

### 1. **Time-to-Live (TTL) for Expiring Events**
   - **TTL Index**: Azure Cosmos DB supports Time-to-Live (TTL) functionality, which automatically expires documents after a specified period. You can create a TTL index on a field (e.g., `expireAt`) that stores the expiration timestamp.
   - **Implementation**:
     - Add a field like `expireAt` to each event document, setting its value to the desired expiration time.
     - Create a TTL index on this field.
     - Example:
       ```javascript
       db.events.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
       ```
     - Azure Cosmos DB will automatically delete documents when the `expireAt` timestamp is reached.

### 2. **Archiving Events**
   - **Archiving Strategy**: For events that need to be retained but are no longer actively queried, consider archiving them to a separate collection or storage solution.
   - **Implementation**:
     - Use a scheduled process (e.g., Azure Functions, Logic Apps, or a custom application) to identify and move older events to an archive collection or another storage service (e.g., Azure Blob Storage, Azure Data Lake).
     - Example:
       - Query events older than a certain date.
       - Insert them into an archive collection or export them to a file.
       - Delete the original events from the main collection.

### 3. **Partitioning and Sharding**
   - **Partitioning**: Use partitioning to distribute events across multiple collections or partitions based on criteria like date or event type. This can make it easier to manage and archive older events.
   - **Sharding**: If using sharding, you can move older shards to cheaper storage or archive them.

### 4. **Cold Storage for Archived Events**
   - **Cold Storage**: For long-term archival, consider moving events to a cost-effective cold storage solution like Azure Blob Storage (e.g., Cool or Archive tiers) or Azure Data Lake.
   - **Implementation**:
     - Export events to a file format like JSON or Parquet.
     - Upload the file to Azure Blob Storage or Data Lake.
     - Use Azure Data Factory or a custom script to automate this process.

### 5. **Event Sourcing Best Practices**
   - **Snapshotting**: For event-sourced systems, consider creating snapshots of aggregate states at regular intervals. This reduces the need to replay a large number of events and can help manage storage requirements.
   - **Compaction**: Periodically compact events by removing or archiving events that are no longer needed for replay (e.g., events older than a certain snapshot).

### 6. **Monitoring and Automation**
   - **Monitoring**: Use Azure Monitor and Cosmos DB metrics to track event storage growth and identify when archiving or expiration is needed.
   - **Automation**: Automate the archiving and expiration process using Azure Functions, Logic Apps, or custom scripts triggered by schedules or storage thresholds.

### Example Workflow:
1. **Expiration**:
   - Set a TTL index on the `expireAt` field for events that should expire after a certain period.
   - Example:
     ```javascript
     db.events.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
     ```

2. **Archiving**:
   - Use an Azure Function to periodically query events older than a specific date.
   - Export these events to Azure Blob Storage or move them to an archive collection.
   - Delete the original events from the main collection.

3. **Cold Storage**:
   - For long-term retention, move archived events to Azure Blob Storage's Cool or Archive tier.

By combining these strategies, you can effectively manage the lifecycle of events in Azure Cosmos DB for MongoDB API, balancing performance, cost, and compliance requirements.