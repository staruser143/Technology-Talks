graph LR
    subgraph Event Store
        A[Append Event Request (streamId, eventData)] --> B{Check for Existing Event (streamId, version)}
        B -- Exists --> C[Conflict: Event Exists - Return Error]
        B -- Doesn't Exist --> D[Generate Next Version]
        D --> E[Conditional Insert (streamId, eventData, version)]
        E -- Success --> F[Event Appended]
        E -- Failure (Conflict) --> G[Retry with Latest Version]
        G --> E
    end

    F --> H[Change Feed Picks Up Event]

    subgraph Change Feed Processor
        H --> I{Check if Event Already Processed (eventId)}
        I -- Processed --> J[Skip Event]
        I -- Not Processed --> K[Update Domain Data]
        K --> L{Read Current Domain Version}
        L --> M[Conditional Upsert (recordId, updatedData, eventVersion)]
        M -- Success --> N[Mark Event as Processed (eventId)]
        N --> O[Domain Data Updated]
        M -- Failure (Conflict) --> P[Skip Update (or Retry)]
        P --> Q[Log Conflict]
        Q --> O
    end

    style C fill:#f9f,stroke:#333,stroke-width:2px
    style G fill:#f9f,stroke:#333,stroke-width:2px
    style J fill:#f9f,stroke:#333,stroke-width:2px
    style P fill:#f9f,stroke:#333,stroke-width:2px

