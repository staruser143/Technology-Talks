```mermaid
graph TD
    A[UI Application] --> B[ Command Handler]
    B --> C[MongoDB Event Store Collection]

    C --> D[Kafka Connect - MongoDB Source Connector]
    D --> E[Kafka Topic: domain-events]

    E --> F[Kafka Consumer - Read Model Updater]
    F --> G[MongoDB Domain Store Collection]

    E --> H[Kafka Consumer - Salesforce Integration]
    H --> I[Salesforce REST API]

    E --> J[Kafka Consumer - Azure Logic Apps Trigger]
    J --> K[Azure Logic Apps Workflow]
