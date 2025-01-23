flowchart LR
    Client[Client Application]
    Binding[Spring Cloud\nStream Bindings]
    Processor[Stream Processor]
    SS[(State Store)]
    CS[(Cloud Storage)]
    QA[Query Service]
    
    Client -->|Events| Binding
    Binding -->|Abstract Channel| Processor
    Processor -->|Current State| SS
    Processor -->|Archive Events| CS
    Client -->|Query Request| QA
    QA -->|Recent Data| SS
    QA -->|Historical Data| CS
    
    subgraph Message Processing Layer
        Binding
        Processor
        SS
    end
    
    subgraph Historical Layer
        CS
    end
    
    style Client fill:#f9f,stroke:#333
    style Binding fill:#ff9,stroke:#333
    style CS fill:#9f9,stroke:#333
    style QA fill:#69f,stroke:#333