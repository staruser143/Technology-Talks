import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.ObjectMapper; // Or inject ObjectMapper if Spring manages it
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class MyChangeFeedListenerService implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MyChangeFeedListenerService.class);

    private final CosmosClient cosmosClient;
    private ChangeFeedProcessor changeFeedProcessor;
    private final ObjectMapper objectMapper; // Use this to convert ChangeFeedProcessorItem (JsonNode) to MyEvent

    @Value("${azure.cosmos.uri}")
    private String cosmosUri;

    @Value("${azure.cosmos.key}")
    private String cosmosKey;

    @Value("${azure.cosmos.databaseName}")
    private String databaseName;

    @Value("${cosmos.container.events}")
    private String eventsContainerName;

    @Value("${cosmos.container.leases}")
    private String leasesContainerName;

    public MyChangeFeedListenerService(CosmosClient cosmosClient, ObjectMapper objectMapper) {
        this.cosmosClient = cosmosClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startChangeFeedProcessor();
    }

    @Override
    public void destroy() throws Exception {
        stopChangeFeedProcessor();
    }

    public void startChangeFeedProcessor() {
        CosmosContainer eventsContainer = cosmosClient.getDatabase(databaseName).getContainer(eventsContainerName);
        CosmosContainer leasesContainer = cosmosClient.getDatabase(databaseName).getContainer(leasesContainerName);

        // Define options for the Change Feed Processor
        // You might start from beginning, or from a specific time, or latest
        CosmosChangeFeedProcessorOptions options = new CosmosChangeFeedProcessorOptions();
        options.setFeedPollInterval(Duration.ofSeconds(1)); // Poll every second
        // options.setStartTime(OffsetDateTime.now().minusDays(7)); // Example: start from 7 days ago
        // options.setStartFromBeginning(true); // Process all changes from the beginning of time

        // A unique name for your Change Feed Processor instance.
        // If you have multiple instances of your application, each should have a unique instance name
        // within the same lease container group.
        String instanceName = UUID.randomUUID().toString();

        this.changeFeedProcessor = new ChangeFeedProcessor.Builder()
                .hostName(instanceName) // Unique name for this instance
                .feedContainer(eventsContainer) // The container to listen for changes
                .leaseContainer(leasesContainer) // The container to store lease information (checkpointing)
                .handleChanges(getChangeFeedProcessorHandler()) // The handler for processing changes
                .options(options)
                .build();

        logger.info("Starting Change Feed Processor for container: {}", eventsContainerName);
        changeFeedProcessor.start()
                .subscribe(
                        null, // onNext: no need to do anything as result of start() is only success/failure
                        error -> logger.error("Change Feed Processor start failed: {}", error.getMessage(), error),
                        () -> logger.info("Change Feed Processor started successfully.")
                );
    }

    public void stopChangeFeedProcessor() {
        if (changeFeedProcessor != null) {
            logger.info("Stopping Change Feed Processor.");
            changeFeedProcessor.stop()
                    .subscribe(
                            null,
                            error -> logger.error("Change Feed Processor stop failed: {}", error.getMessage(), error),
                            () -> logger.info("Change Feed Processor stopped successfully.")
                    );
        }
    }

    // This is the core method that processes each batch of changes
    private Consumer<List<ChangeFeedProcessorItem>> getChangeFeedProcessorHandler() {
        return (List<ChangeFeedProcessorItem> changes) -> {
            logger.info("Received {} changes from change feed.", changes.size());
            for (ChangeFeedProcessorItem item : changes) {
                try {
                    // Each item is a ChangeFeedProcessorItem, which wraps the actual Cosmos DB item (JsonNode)
                    // You can access the raw JsonNode or use ObjectMapper to convert it to your POJO
                    MyEvent event = objectMapper.convertValue(item.getCurrent(), MyEvent.class);

                    // Now 'event' is your MyEvent object, and 'event.getPayload()' is your Map<String, Object>
                    logger.debug("Processing event: ID={}, Type={}, Timestamp={}",
                            event.getId(), event.getEventType(), event.getTimestamp());

                    // Accessing fields from the dynamic payload map
                    if (event.getPayload() != null) {
                        logger.debug("Event Payload: {}", event.getPayload());

                        // Example: Accessing specific fields from the payload
                        if ("UserCreated".equals(event.getEventType())) {
                            String userId = (String) event.getPayload().get("userId");
                            String username = (String) event.getPayload().get("username");
                            logger.info("  UserCreated Event - User ID: {}, Username: {}", userId, username);
                            // Add logic to update your read model here
                        } else if ("OrderPlaced".equals(event.getEventType())) {
                            String orderId = (String) event.getPayload().get("orderId");
                            Double totalAmount = (Double) event.getPayload().get("totalAmount");
                            List<String> items = (List<String>) event.getPayload().get("items");
                            logger.info("  OrderPlaced Event - Order ID: {}, Total Amount: {}, Items: {}",
                                    orderId, totalAmount, items);
                            // Add logic to update your read model here
                        }
                        // Handle other event types and their specific payload fields
                    }
                } catch (Exception e) {
                    logger.error("Error processing change feed item: {}", item.getCurrent(), e);
                    // Implement robust error handling: dead-letter queue, retry logic, etc.
                }
            }
        };
    }
}
