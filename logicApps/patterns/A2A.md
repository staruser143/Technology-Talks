```mermaid
graph TD
    A[Salesforce] -->|API Call| B[Azure Logic App]
    C[Dynamics 365] -->|API Call| B
    B --> D[SQL Database] 
