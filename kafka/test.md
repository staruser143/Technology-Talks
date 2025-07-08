graph TD
    subgraph "Kubernetes Cluster"
        subgraph "Application Pods (N Replicas)"
            direction LR
            Pod1["NestJS App Instance 1 (Pod)"]
            Pod2["NestJS App Instance 2 (Pod)"]
            PodN["NestJS App Instance N (Pod)"]
        end

        subgraph "Services within each Pod"
            direction TB
            LE1(Leader Election Service)
            CSP1(ChangeStream Producer Service)
            RMC1(Read Model Consumer Service)

            LE2(Leader Election Service)
            CSP2(ChangeStream Producer Service)
            RMC2(Read Model Consumer Service)

            LEN(Leader Election Service)
            CSPN(ChangeStream Producer Service)
            RMCN(Read Model Consumer Service)

            Pod1 --- LE1 & CSP1 & RMC1
            Pod2 --- LE2 & CSP2 & RMC2
            PodN --- LEN & CSPN & RMCN
        end
    end

    subgraph "External Dependencies"
        MongoDB_RS((MongoDB Replica Set / Sharded Cluster))
        AZ_SB(Azure Service Bus Topic)
        Mongo_Offset_DB((MongoDB Offsets/Leases DB))
        ReadModel_DB((MongoDB Read Model DB))
    end

    style Pod1 fill:#f9f,stroke:#333,stroke-width:2px
    style Pod2 fill:#f9f,stroke:#333,stroke-width:2px
    style PodN fill:#f9f,stroke:#333,stroke-width:2px

    %% Leader Election Flow
    LE1 -- "Periodically tries to acquire/renew lease (instanceId, heartbeatAt, expiresAt)" --> Mongo_Offset_DB
    LE2 -- "Periodically tries to acquire/renew lease" --> Mongo_Offset_DB
    LEN -- "Periodically tries to acquire/renew lease" --> Mongo_Offset_DB

    Mongo_Offset_DB -- "Lease status (leaderId, expiresAt)" --> LE1
    Mongo_Offset_DB -- "Lease status" --> LE2
    Mongo_Offset_DB -- "Lease status" --> LEN

    %% Publisher Flow (Active-Standby)
    CSP1 -- "Checks LeaderElectionService.isLeader" --> LE1
    CSP2 -- "Checks LeaderElectionService.isLeader" --> LE2
    CSPN -- "Checks LeaderElectionService.isLeader" --> LEN

    MongoDB_RS -- "ChangeStream (oplog)" --> CSP1
    MongoDB_RS -- "ChangeStream (oplog)" --> CSP2
    MongoDB_RS -- "ChangeStream (oplog)" --> CSPN

    CSP_Active(["ChangeStream Producer (ACTIVE Leader)"])
    CSP_Standby(["ChangeStream Producer (STANDBY)"])
    CSP_StandbyN(["ChangeStream Producer (STANDBY)"])

    %% Representing which one becomes active
    LE1 -.-> CSP_Active
    LE2 -.-> CSP_Standby
    LEN -.-> CSP_StandbyN

    CSP_Active -- "Watches ChangeStream" --> MongoDB_RS
    CSP_Active -- "Publishes Event (MessageId, PartitionKey=aggregateId, SessionId=aggregateId)" --> AZ_SB

    CSP_Active -- "Persists Resume Token" --> Mongo_Offset_DB
    Mongo_Offset_DB -- "Loads Resume Token (on leader takeover)" --> CSP_Active


    %% Consumer Flow (Active-Active)
    AZ_SB -- "Partitioned & Session-enabled messages" --> RMC1
    AZ_SB -- "Partitioned & Session-enabled messages" --> RMC2
    AZ_SB -- "Partitioned & Session-enabled messages" --> RMCN

    RMC1 -- "Updates Read Model (Idempotent upsert)" --> ReadModel_DB
    RMC2 -- "Updates Read Model (Idempotent upsert)" --> ReadModel_DB
    RMCN -- "Updates Read Model (Idempotent upsert)" --> ReadModel_DB

    %% Flow explanations
    subgraph "Key Flows"
        direction LR
        A[Leader Election] --> B[Active Publisher Selection]
        B --> C[ChangeStream Event Publishing]
        C --> D[Service Bus Partitioning/Sessions]
        D --> E[Read Model Consumption & Update]
    end

    linkStyle 0 stroke-width:2px,fill:none,stroke:red;
    linkStyle 1 stroke-width:2px,fill:none,stroke:red;
    linkStyle 2 stroke-width:2px,fill:none,stroke:red;
    linkStyle 3 stroke-width:2px,fill:none,stroke:red;
    linkStyle 4 stroke-width:2px,fill:none,stroke:red;
    linkStyle 5 stroke-width:2px,fill:none,stroke:red;

    linkStyle 6 stroke-width:2px,fill:none,stroke:blue;
    linkStyle 7 stroke-width:2px,fill:none,stroke:blue;
    linkStyle 8 stroke-width:2px,fill:none,stroke:blue;
    linkStyle 9 stroke-width:2px,fill:none,stroke:blue;
    linkStyle 10 stroke-width:2px,fill:none,stroke:blue;
    linkStyle 11 stroke-width:2px,fill:none,stroke:blue;

    linkStyle 12 stroke-width:2px,fill:none,stroke:green;
    linkStyle 13 stroke-width:2px,fill:none,stroke:green;
    linkStyle 14 stroke-width:2px,fill:none,stroke:green;

    linkStyle 15 stroke-width:2px,fill:none,stroke:purple;
    linkStyle 16 stroke-width:2px,fill:none,stroke:purple;
    linkStyle 17 stroke-width:2px,fill:none,stroke:purple;
