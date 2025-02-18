graph LR
    subgraph Producer
        A[Application] --> B{Idempotent Producer};
        B -- Message (eventId generated) --> C[Event Hubs Partition];
        B -- Retry (same eventId) --> C;
    end

    C -- Message --> D[Consumer];

    subgraph Consumer
        D -- Event --> E{Check if eventId exists in store};
        E -- Yes (Duplicate) --> F[Skip Processing];
        E -- No (New) --> G[Process Event (Transactionally)];
        G -- Success --> H[Store eventId];
        G -- Failure --> I[Rollback & Retry/Dead-letter];
        H --> J[Commit Transaction];
    end

    style C fill:#ccf,stroke:#888,stroke-width:2px
    style B fill:#aaf,stroke:#888
    style D fill:#aaf,stroke:#888

    classDef eventHubs fill:#ccf,stroke:#888,stroke-width:2px
    class C eventHubs

