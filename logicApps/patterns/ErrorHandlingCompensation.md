```mermaid
 graph TD
    A[Trigger] --> B[Try Scope]
    B --> C[Main Logic]
    B --> D[Catch Scope]
    D --> E[Log Error]
    D --> F[Compensation Logic] 
