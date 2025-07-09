```mermaid
 graph TD
    A[ChangeStream Service] --> B[Service Bus]
    B[Service Bus] --> C[Azure Logic App]
    C[Azure Logic App] --> D[Salesforce API]

