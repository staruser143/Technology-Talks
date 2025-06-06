@Component
public class ChangeFeedProcessorStarter {

    // Injected from application.yml or application.properties
    @Value("${azure.cosmos.database}")
    private String databaseName;

    @Value("${azure.cosmos.container}")
    private String monitoredContainerName;

    @Value("${azure.cosmos.lease-container}")
    private String leaseContainerName;

    private final CosmosAsyncClient cosmosClient;

    public ChangeFeedProcessorStarter(CosmosAsyncClient cosmosClient) {
        this.cosmosClient = cosmosClient;
    }

    @PostConstruct
    public void startProcessor() {
        CosmosAsyncContainer monitoredContainer = cosmosClient
                .getDatabase(databaseName)
                .getContainer(monitoredContainerName);

        CosmosAsyncContainer leaseContainer = cosmosClient
                .getDatabase(databaseName)
                .getContainer(leaseContainerName);

        // 🔁 Create the Change Feed Processor
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(UUID.randomUUID().toString()) // unique instance name
                .feedContainer(monitoredContainer)
                .leaseContainer(leaseContainer)
                .handleChanges(new MyChangeFeedHandler()) // 🔗 LINKED TO YOUR HANDLER
                .options(new ChangeFeedProcessorOptions()
                        .setStartFromBeginning(true)
                        .setMaxItemCount(100)
                )
                .buildChangeFeedProcessor();

        // 🚀 Start the processor
        changeFeedProcessor.start()
                .doOnSuccess(v -> System.out.println("Change feed processor started"))
                .subscribe();
    }
}