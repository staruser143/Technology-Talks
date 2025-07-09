```mermaid
 graph TD
    A[ChangeStream Service] --> B[Service Bus]
    B[Service Bus] --> C[Azure Logic App]
    C --> D[Read from MongoDB]
    C --> E[Write To MongoDB]

