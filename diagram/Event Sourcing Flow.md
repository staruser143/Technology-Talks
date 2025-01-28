```mermaid
flowchart LR
    Client[Web / Mobile App]
    API[Write API]
    Kafka[Event Store]
    CommandBus[Command Bus ]
    CommandHandler[Command Handler ]
    MongoDB[(Domain Data store)]
    ReadDB[(Read Model Data Store)]
    QueryHandler[Query Handler]
    
    subgraph Command Side
        direction TB
        API --> |Write Requests| CommandBus
        CommandBus --> |Commands| CommandHandler
        CommandHandler --> |Events| Kafka
        Kafka --> |Entity State| MongoDB
        MongoDB --> |Project Events| ReadDB
        CommandHandler --> |Command Completed| CommandBus
        CommandBus --> |Command Completed| API
        API --> |Request Processed| Client
    end
    
    subgraph Query Side
        direction TB
        ReadAPI[Read API]
        ReadAPI --> |Query Requests| QueryHandler
        QueryHandler --> |Query Requests| ReadDB
        ReadDB --> |Query Results| QueryHandler
        QueryHandler --> |Query Results| ReadAPI
        ReadAPI -->|Query Resolved| Client
    end
    
    Client --> |Write Operations| API
    Client --> |Read Operations| ReadAPI

    style Client fill:#f9f,stroke:#333
    style Kafka fill:#ff9,stroke:#333
    style MongoDB fill:#9f9,stroke:#333
    style ReadDB fill:#9ff,stroke:#333
    style API fill:#f96,stroke:#333
    style ReadAPI fill:#69f,stroke:#333
```
