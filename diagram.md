```mermaid
graph TD
    Client -->|Sends Command| CommandHandler[CommandHandler]
    CommandHandler -->|Stores Event| Kafka[Kafka (EventStore)]
    Kafka -->|Publishes Event| EventProcessor[EventProcessor]
    EventProcessor -->|Updates| MongoDB[MongoDB (ReadModels)]
    EventProcessor -->|Archives| S3[S3 (Archiving)]
    MongoDB -->|Queries| QueryHandler[QueryHandler]
    QueryHandler -->|Returns Data| Client[Client]
```

