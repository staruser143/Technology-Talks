```mermaid
 graph TD
    A[Trigger Event] --> B[Azure Logic App]
    B --> C[Send Approval via Outlook]
    B --> D[Send Approval via Teams]
    C --> E[User Response]
    D --> E
    E --> F[Continue Workflow] 
