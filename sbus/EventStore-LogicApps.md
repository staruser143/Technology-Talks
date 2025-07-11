flowchart TD
    EventStore[" Mongo Event Store"] --> CDC["MongoChangeStream Service"]
    
    subgraph "Event Distribution"
        CDC  --> ServiceBus["Azure Service Bus Topic"]
       
    end
    
    subgraph "Service Bus Subscribers"
        ServiceBus --> ReadModelService["READ Model Service"]
        ServiceBus --> AzureLogicApps["Post Enrollment Workflow"]
       
    end
    
