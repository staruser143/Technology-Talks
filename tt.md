graph TB
    subgraph clients["Client Applications Layer"]
        JavaApp["Java Applications<br/>(Spring Boot, Microservices)"]
        NonJavaApp["Non-Java Applications<br/>(Node.js, Python, .NET)"]
        WebApp["Web Applications<br/>(Frontend Apps, REST Consumers)"]
    end

    subgraph gateway["API Layer (Optional)"]
        LB["API Gateway / Load Balancer"]
    end

    subgraph core["Document Generation Service (Spring Boot)"]
        direction TB
        API["REST API Controller<br/>(Payload Reception)"]
        Mapper["Mapping Service<br/>(YAML → Field Mapping)"]
        TemplateEngine["Template Engine<br/>(FreeMarker Processing)"]
        PDFGen["PDF Generator<br/>(AcroForm / iText)"]
    end

    subgraph config["Configuration Management"]
        ConfigServer["Spring Cloud Config Server<br/>(YAML Mapping Definitions)"]
        GitRepo[("Git Repository<br/>(YAML Config Files)")]
    end

    subgraph storage["Template Storage"]
        DataRepo["Data Repository Service<br/>(FreeMarker & AcroForm Templates)"]
    end

    subgraph pipeline["Document Processing Pipeline"]
        direction LR
        Step1["1. Receive<br/>Payload"]
        Step2["2. Fetch<br/>Config"]
        Step3["3. Map<br/>Data"]
        Step4["4. Apply<br/>Template"]
        Step5["5. Generate<br/>PDF"]
        
        Step1 --> Step2 --> Step3 --> Step4 --> Step5
    end

    Output["Generated PDF Document<br/>(Returned to Client)"]

    JavaApp -->|REST Request| LB
    NonJavaApp -->|REST Request| LB
    WebApp -->|REST Request| LB
    
    LB -->|Forward Request| API
    
    API --> Mapper
    Mapper --> TemplateEngine
    TemplateEngine --> PDFGen
    
    Mapper -.->|Fetch YAML Mappings| ConfigServer
    ConfigServer -.->|Pull Config| GitRepo
    
    TemplateEngine -.->|Fetch Templates| DataRepo
    
    PDFGen --> Output
    Output -.->|Response| LB

    classDef clientStyle fill:#3b82f6,stroke:#1e40af,color:#fff,stroke-width:2px
    classDef coreStyle fill:#10b981,stroke:#059669,color:#fff,stroke-width:2px
    classDef configStyle fill:#a855f7,stroke:#7e22ce,color:#fff,stroke-width:2px
    classDef storageStyle fill:#f97316,stroke:#c2410c,color:#fff,stroke-width:2px
    classDef pipelineStyle fill:#06b6d4,stroke:#0891b2,color:#fff,stroke-width:2px
    classDef outputStyle fill:#10b981,stroke:#059669,color:#fff,stroke-width:3px

    class JavaApp,NonJavaApp,WebApp clientStyle
    class API,Mapper,TemplateEngine,PDFGen coreStyle
    class ConfigServer,GitRepo configStyle
    class DataRepo storageStyle
    class Step1,Step2,Step3,Step4,Step5 pipelineStyle
    class Output outputStyle