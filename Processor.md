Guidance for Setting Up Cosmos DB Change Feed Processor

To use the Change Feed Processor efficiently, certain preconditions must be met, and a few best practices should be followed.


---

1. Preconditions for Setting Up Change Feed Processor


---

2. Steps to Set Up the Change Feed Processor

Step 1: Create the Lease Container

1. Create a new container for leases in the same database as your source container.


2. Configure the lease container as follows:

Container ID: leases (or a descriptive name)

Partition Key: /id (recommended)

Throughput: Set adequate RU/s depending on the workload.




Step 2: Enable Change Feed Processor in Code

Here's a refined example with proper lease container setup:

import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { CosmosClient, ChangeFeedProcessor } from '@azure/cosmos';

@Injectable()
export class ChangeFeedService implements OnModuleInit, OnModuleDestroy {
  private readonly client = new CosmosClient(process.env.COSMOS_DB_CONNECTION_STRING);
  private readonly databaseId = 'event-db';
  private readonly sourceContainerId = 'event-container';
  private readonly leaseContainerId = 'leases';
  private changeFeedProcessor: ChangeFeedProcessor;

  async onModuleInit() {
    const database = this.client.database(this.databaseId);
    const sourceContainer = database.container(this.sourceContainerId);
    const leaseContainer = database.container(this.leaseContainerId);

    // Initialize Change Feed Processor
    this.changeFeedProcessor = sourceContainer.items.changeFeedProcessor('event-processor')
      .setLeaseContainer(leaseContainer)
      .setStartFromBeginning()
      .setHandleChanges(async (changes) => {
        for (const change of changes) {
          console.log('Processing change:', change);
          await this.writeToReadModel(change);
        }
      });

    // Start the processor
    await this.changeFeedProcessor.start();
    console.log('Change Feed Processor started.');
  }

  async onModuleDestroy() {
    if (this.changeFeedProcessor) {
      await this.changeFeedProcessor.stop();
      console.log('Change Feed Processor stopped.');
    }
  }

  private async writeToReadModel(change: any) {
    const readContainer = this.client.database(this.databaseId).container('read-container');
    await readContainer.items.create({ ...change, processedAt: new Date() });
  }
}

Step 3: Test the Processor

Insert or update documents in the source container.

Monitor the logs to ensure changes are being processed and stored in the read container.



---

3. Best Practices for Lease Container

Partition Key: Use /id for the lease container partition key.

Consistent Database Location: The lease container must reside in the same database as the source container.

Throughput: Allocate appropriate RU/s based on the number of partitions in the source container.



---

4. Performance Optimization Tips

Minimize Lease Container RU Consumption: Lease operations consume RUs; monitor usage and scale throughput as needed.

Batch Processing: Use the maxItemsPerTrigger configuration to process events in batches for efficiency.

Idempotent Writes: Ensure idempotency in the event handling logic to avoid duplicate data writes.

Checkpointing: Rely on the built-in checkpointing feature for reliable processing.



---

5. Monitoring and Diagnostics

Enable Azure Monitor and Application Insights to track:

Change Feed Processor health

RU consumption for the source and lease containers

Error rates and 
