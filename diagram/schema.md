flowchart TD
    subgraph "Schema Management"
        SR[Schema Registry] -->|Schema Validation| A[Command Services]
        SR -->|Schema Lookup| K[Kafka Event Store]
        SR -->|Schema Validation| M[MongoDB Direct Write]
    end

    subgraph "Command Side"
        A -->|Validate Schema| SR
        A -->|Write Events| K
        A -->|Direct State Update| M
    end

    subgraph "Query Side"
        K -->|Event Projections| MP[MongoDB Projections]
        K -->|Event Archiving| CS[Cloud Storage]
    end

    subgraph "Event Replay Mechanism"
        CS -->|Load Historical Events| KConnect[Kafka Connect]
        KConnect -->|Replay to Kafka| K
        K -->|Direct Replay| EventReplay[Event Replay Service]
    end

    classDef registry fill:#ffb,stroke:#333,stroke-width:2px;
    classDef store fill:#f9f,stroke:#333,stroke-width:2px;
    classDef service fill:#bbf,stroke:#333,stroke-width:2px;
    classDef process fill:#bfb,stroke:#333,stroke-width:2px;

    class SR registry;
    class K,CS,MP,M store;
    class A,EventReplay service;
    class KConnect process;
