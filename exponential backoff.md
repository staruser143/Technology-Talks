Here’s an enhanced version of the Azure Cosmos DB Change Feed Processor example with the following improvements:

Exponential Backoff Strategy for Retries

Structured Logging using SLF4J



---

Dependencies

Maven Dependencies

Add these dependencies to pom.xml:

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos</artifactId>
  <version>4.51.0</version>
</dependency>

<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-api</artifactId>
  <version>2.0.9</version>
</dependency>

<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-simple</artifactId>
  <version>2.0.9</version>
</dependency>


---

Complete Solution

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChangeFeedProcessorExample {

    private static final String COSMOS_DB_ENDPOINT = "your-cosmos-db-endpoint";
    private static final String COSMOS_DB_KEY = "your-cosmos-db-key";
    private static final String DATABASE_ID = "your-database-id";
    private static final String CONTAINER_ID = "your-container-id";
    private static final String LEASE_CONTAINER_ID = "your-lease-container-id";

    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedProcessorExample.class);

    public static void main(String[] args) {
        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(COSMOS_DB_ENDPOINT)
            .key(COSMOS_DB_KEY)
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .buildAsyncClient();

        CosmosAsyncDatabase database = client.getDatabase(DATABASE_ID);
        CosmosAsyncContainer container = database.getContainer(CONTAINER_ID);
        CosmosAsyncContainer leaseContainer = database.getContainer(LEASE_CONTAINER_ID);

        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .hostName("host-1")
            .feedContainer(container)
            .leaseContainer(leaseContainer)
            .handleChanges((List<CosmosItemProperties> changes, ChangeFeedProcessorContext context) -> {
                for (CosmosItemProperties item : changes) {
                    processItemWithRetry(item, context, 0);
                }
            })
            .buildChangeFeedProcessor();

        changeFeedProcessor.start()
            .doOnSuccess(aVoid -> logger.info("Change Feed Processor started."))
            .block();

        try {
            Thread.sleep(60000); // 1 minute delay for processing
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: ", e);
        }

        changeFeedProcessor.stop()
            .doOnSuccess(aVoid -> logger.info("Change Feed Processor stopped."))
            .block();

        client.close();
    }

    private static void processItemWithRetry(CosmosItemProperties item, ChangeFeedProcessorContext context, int attempt) {
        final int maxRetries = 5;
        final Duration initialBackoff = Duration.ofMillis(500);
        final double backoffMultiplier = 2.0;

        try {
            logger.info("Processing item: {}", item.toJson());

            if ("poison-message-id".equals(item.getId())) {
                throw new RuntimeException("Simulated poison message error");
            }

            // Explicit checkpointing after processing
            context.checkpoint()
                .doOnSuccess(ignored -> logger.info("Checkpoint committed for item: {}", item.getId()))
                .block();

        } catch (Exception e) {
            logger.error("Error processing item: {}, Attempt: {}", item.getId(), attempt, e);
            if (attempt < maxRetries) {
                // Exponential backoff
                Duration backoffDuration = initialBackoff.multipliedBy((long) Math.pow(backoffMultiplier, attempt));
                logger.warn("Retrying item: {} after backoff of {} ms", item.getId(), backoffDuration.toMillis());
                try {
                    Thread.sleep(backoffDuration.toMillis());
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                processItemWithRetry(item, context, attempt + 1);
            } else {
                logger.error("Max retries reached for item: {}, skipping processing", item.getId());
            }
        }
    }
}


---

Enhancements

1. Exponential Backoff for Retries

The backoff duration increases exponentially (initialBackoff * 2^attempt).

This helps reduce immediate load during transient errors.


2. Logging with SLF4J

Provides structured logging for better observability.

Logs successful processing, errors, retries, and checkpoints.


3. Retry Management

Limits retries with maxRetries.

Skips poison messages after maximum retries.



---

Sample Output

INFO Processing item: {"id":"123","data":"example"}
INFO Checkpoint committed for item: 123
WARN Retrying item: poison-message-id after backoff of 1000 ms
ERROR Max retries reached for item: poison-message-id, skipping processing


---

Next Steps

Would you like guidance on deploying this solution in a production environment or integrating it with monitoring tools like Application Insights?

