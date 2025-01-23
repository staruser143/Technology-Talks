flowchart LR
    ReplayAPI[Replay API]
    KafkaOld[(Kafka\nOriginal Topics)]
    ReplayConsumer[Replay Consumer]
    ReplayTopic[(Kafka\nReplay Topic)]
    ReadModel[(Read Model)]
    
    ReplayAPI -->|1. Request Replay| ReplayConsumer
    KafkaOld -->|2. Read Historical Events| ReplayConsumer
    ReplayConsumer -->|3. Republish Events| ReplayTopic
    ReplayTopic -->|4. Process Events| ReadModel
    
    subgraph Replay Service
        ReplayConsumer
    end
    
    style ReplayAPI fill:#f96,stroke:#333
    style KafkaOld fill:#ff9,stroke:#333
    style ReplayTopic fill:#ff9,stroke:#333
    style ReadModel fill:#9ff,stroke:#333