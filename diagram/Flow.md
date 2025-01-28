```mermaid
sequenceDiagram
    participant User as Front-End App
    participant WriteAPI as Write API
    participant ReadAPI as Read API
    participant CB as Command Bus
    participant CH as Command Handler
    participant CV as Command Validator
    participant ES as Event Store (Kafka)
    participant EC as Event Consumers
    participant P as Projections (MongoDB)
    participant Q as Query Handler
  

    User->>WriteAPI: Send Command
    WriteAPI->>CB: Dispatch Command
    CB->>CH: Handle Command
    CH->>CV: Validate Command
    CV-->CH: Validation Successfull
    CH->>ES: Store Event
    ES->>EC: Consume Event
    EC->>P: Update Projection
    WriteAPI->>User: Command Completed

    User->>ReadAPI: Query Data
    ReadAPI->>Q: Handle Query
    Q->>P: Fetch Read Model
    P->>Q: Return Read Model
    Q->>ReadAPI: Return Query Result
    ReadAPI->>User: Send Query Response

```
