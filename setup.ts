setHandleChanges(async (changes) => {
  try {
    for (const change of changes) {
      console.log('Processing change:', change);
      await this.writeToReadModel(change);  // Ensure this operation is idempotent
    }
  } catch (error) {
    console.error('Error processing changes:', error);
    // Handle specific error types if needed
    await this.logError(error, changes);
  }
});

this.changeFeedProcessor = sourceContainer.items.changeFeedProcessor('event-processor')
  .setLeaseContainer(leaseContainer)
  .setHandleChanges(async (changes, context) => {
    try {
      await processChanges(changes);
      await context.checkpoint();  // Explicit checkpointing
    } catch (error) {
      console.error('Failed to process changes or checkpoint:', error);
    }
  });

async function processChange(change) {
  try {
    await this.writeToReadModel(change);
  } catch (error) {
    await this.sendToDeadLetterQueue(change, error);
  }
}