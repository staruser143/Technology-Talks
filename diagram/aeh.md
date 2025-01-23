flowchart TD
    Client[Client Application]
    EH[Azure Event Hubs]
    EHNS[Event Hubs Namespace]
    CP[Checkpoint Store\nAzure Blob Storage]
    SS[(Azure Cache for Redis)]
    ABS[(Azure Blob Storage)]
    ADLS[(Azure Data Lake)]
    
    Client -->|Events| EH
    EH -->|Process Events| Processor
    Processor -->|Store State| SS
    Processor -->|Capture| ADLS
    EH -->|Auto Capture| ABS
    Processor -->|Checkpoint| CP
    
    subgraph Real-time Processing
        EH
        EHNS
        Processor[Event Hub Processor]
        SS
    end
    
    subgraph Long-term Storage
        ABS
        ADLS
    end
    
    style Client fill:#f9f,stroke:#333
    style EH fill:#ff9,stroke:#333
    style SS fill:#9f9,stroke:#333
    style ADLS fill:#69f,stroke:#333