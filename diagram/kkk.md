I'll create a Mermaid diagram to visualize the flow of events and data in the system.



```mermaid
flowchart TD
    subgraph Producers
        P1[Producer 1]
        P2[Producer 2]
        P3[Producer N]
    end

    subgraph "Kafka Event Store"
        K1[Topic 1]
        K2[Topic 2]
        K3[Topic N]
    end

    subgraph "Archival System"
        AS[Archival Service]
        TM[Time Monitor]
        BP[Batch Processor]
    end

    subgraph "Cloud Storage"
        B1[Recent Events]
        B2[Historical Events]
        B3[Backup Events]
    end

    subgraph "Query & Replay System"
        QS[Query Service]
        RS[Replay Service]
        ER[Entity Rebuilder]
    end

    P1 & P2 & P3 --> K1 & K2 & K3
    K1 & K2 & K3 --> AS
    AS --> TM
    TM --> |"Age > Threshold"| BP
    BP --> |"Batch Write"| B1 & B2 & B3
    
    QS --> B1 & B2 & B3
    QS --> K1 & K2 & K3
    RS --> B1 & B2 & B3
    RS --> |"Replay Events"| K1 & K2 & K3
    ER --> QS
    
    classDef kafka fill:#ff9900,stroke:#333,stroke-width:2px
    classDef storage fill:#42a5f5,stroke:#333,stroke-width:2px
    classDef service fill:#66bb6a,stroke:#333,stroke-width:2px
    
    class K1,K2,K3 kafka
    class B1,B2,B3 storage
    class AS,TM,BP,QS,RS,ER service

```

Let me explain the key components and flows in the diagram:

1. Event Production Flow:
   - Multiple producers write events to different Kafka topics
   - Events are stored in Kafka with their original ordering and timestamps

2. Archival Flow:
   - Archival Service continuously monitors Kafka topics
   - Time Monitor checks event age against configured threshold
   - Batch Processor aggregates events and writes to Cloud Storage
   - Events are organized in Cloud Storage by recency and type

3. Query & Replay Flow:
   - Query Service can access both Kafka and Cloud Storage
   - Replay Service can restore events back to Kafka when needed
   - Entity Rebuilder uses both systems to reconstruct entity states

Color Coding:
- Orange: Kafka components (Event Store)
- Blue: Cloud Storage components
- Green: Processing services

The system maintains data accessibility while efficiently managing storage across both hot (Kafka) and cold (Cloud Storage) tiers. Would you like me to:
1. Add more detail about any specific component?
2. Show the data flow for a specific scenario (like entity rebuild)?
3. Include additional components for monitoring or scaling?