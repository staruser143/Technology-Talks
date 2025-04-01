flowchart TD
    MongoDB["MongoDB\n(Event Store)"] --> ChangeStream["Change Stream Service"]
    
    subgraph "Event Distribution"
        ChangeStream --> ServiceBus["Azure Service Bus Topics"]
        ChangeStream --> EventHubs["Azure Event Hubs"]
        ChangeStream --> CriticalProjectors["Critical Read Model\nProjectors"]
    end
    
    subgraph "Integration Microservices"
        ServiceBus --> SalesforceService["Salesforce Integration\nMicroservice"]
        ServiceBus --> SystemAService["System A Integration\nMicroservice"]
        ServiceBus --> SystemBService["System B Integration\nMicroservice"]
        ServiceBus --> StandardProjectors["Standard Read Model\nProjector Services"]
    end
    
    subgraph "Analytics Path"
        EventHubs --> ADLS["Azure Data Lake\nStorage Gen2"]
    end
    
    subgraph "External Systems"
        SalesforceService --> Salesforce["Salesforce\n(Lead Generation)"]
        SystemAService --> SystemA["System A API"]
        SystemBService --> SystemB["System B API"]
    end
    
    subgraph "Domain Read Models"
        CriticalProjectors --> QuoteReadModels["Quote Subdomain\nRead Models"]
        CriticalProjectors --> EnrollmentReadModels["Enrollment Subdomain\nRead Models"]
        StandardProjectors --> ReportingReadModels["Reporting\nRead Models"]
        StandardProjectors --> SearchReadModels["Search Optimization\nRead Models"]
    end
    
    classDef azure fill:#0072C6,color:white,stroke:#0072C6
    classDef mongo fill:#589636,color:white,stroke:#589636
    classDef salesforce fill:#1798c1,color:white,stroke:#1798c1
    classDef readmodel fill:#FF9900,color:black,stroke:#FF9900
    classDef external fill:#6B6B6B,color:white,stroke:#6B6B6B
    classDef microservice fill:#8A2BE2,color:white,stroke:#8A2BE2
    
    class MongoDB mongo
    class ServiceBus,EventHubs azure
    class SalesforceService,SystemAService,SystemBService,StandardProjectors microservice
    class Salesforce salesforce
    class QuoteReadModels,EnrollmentReadModels,ReportingReadModels,SearchReadModels readmodel
    class SystemA,SystemB external