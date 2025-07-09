```mermaid
 graph TD
    A[Trading Partner] -->|AS2/X12| B[Azure Logic App]
    B --> C[Validate EDI]
    C --> D[Store in Database] 
