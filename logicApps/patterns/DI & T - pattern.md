```mermaid
 graph TD
    A[Blob Storage] --> B[Azure Logic App]
    B --> C[Azure Function: Transform Data]
    C --> D[Azure SQL Database] 
