It's an excellent idea to visualize this architecture! A diagram makes the interactions much clearer. Here's a Mermaid diagram representing how your NestJS components, MongoDB ChangeStream, and Azure Service Bus work together, including the active-standby publisher and scaling consumers.
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

Explanation of the Diagram:
 * Kubernetes Cluster & Application Pods:
   * This represents your deployment environment. You deploy N identical replicas of your NestJS application. Each replica runs as a Kubernetes Pod.
   * Each Pod encapsulates the Leader Election Service, ChangeStream Producer Service, and Read Model Consumer Service. This is what "part of the same deployment unit" means – they are co-located within the same process.
 * External Dependencies:
   * MongoDB Replica Set/Sharded Cluster: Your source collection where events are stored. ChangeStream taps into its oplog.
   * Azure Service Bus Topic (AZ_SB): The message broker. Crucially configured with Partitioning and a Session-enabled Subscription.
   * MongoDB Offsets/Leases DB (Mongo_Offset_DB): A dedicated MongoDB collection used for:
     * Leader Election Leases: Stores which instance is the current leader and its heartbeat/expiry time.
     * ChangeStream Resume Tokens: Stores the _id of the last successfully processed event by the active ChangeStream Producer. This allows resuming after restarts/failovers.
   * MongoDB Read Model DB (ReadModel_DB): Your sink collection where the denormalized read models are built.
 * Leader Election Flow (Red Arrows):
   * All Leader Election Service (LE) instances (LE1, LE2, LEN) in every Pod periodically attempt to acquire or renew a lease in the Mongo_Offset_DB.
   * The Mongo_Offset_DB acts as the single source of truth for leadership. Only one instance succeeds in holding the valid, unexpired lease (leaderId matches its instanceId).
   * The Mongo_Offset_DB also provides the lease status back to all LE instances.
 * Publisher Flow - Active-Standby (Blue Arrows):
   * All ChangeStream Producer Service (CSP) instances (CSP1, CSP2, CSPN) continuously check with their co-located Leader Election Service (isLeader property).
   * Only the instance whose CSP is associated with the ACTIVE Leader (e.g., CSP1 in the diagram as CSP_Active) will:
     * Watch the MongoDB ChangeStream. This means it opens a connection and receives all changes from the MongoDB_RS.
     * Publish Events to Azure Service Bus. It transforms the ChangeStream event into a Service Bus message, setting the PartitionKey and SessionId to the aggregateId (e.g., event.aggregateId). This ensures all related events are grouped.
     * Persist its Resume Token to the Mongo_Offset_DB after successfully publishing an event. This is the single, shared point of truth for stream progress.
   * The STANDBY CSPs (CSP_Standby, CSP_StandbyN) remain idle; they do not watch the ChangeStream or publish events.
   * If the active leader fails, the Leader Election process quickly designates a new leader, and that new leader's CSP will take over, loading the last saved resume token to seamlessly continue.
 * Consumer Flow - Active-Active (Green Arrows):
   * All Read Model Consumer Service (RMC) instances (RMC1, RMC2, RMCN) in every Pod are active simultaneously.
   * They all subscribe to the same Azure Service Bus Topic and Subscription.
   * Azure Service Bus's partitioning and session features guarantee that:
     * Messages for a specific aggregateId (due to PartitionKey and SessionId) are routed to a single partition within the topic.
     * Only one RMC instance acquires the session for that aggregateId at any given time.
     * Messages within that session are delivered to that RMC in order.
   * Each RMC then processes the events it receives and performs idempotent upserts to update the ReadModel_DB. Idempotency handles potential retries or eventual consistency concerns.
This diagram clearly illustrates the active-standby pattern for the producer (ensuring no duplicate reading from ChangeStream and consistent resume token management) and the active-active, partitioned pattern for the consumers (ensuring scalable, ordered, and non-duplicate processing for Read Model updates).
