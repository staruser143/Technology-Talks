```mermaid
 graph TD
    A[Trigger] --> B[Condition Check]
    B -->|True| C[Path A]
    B -->|False| D[Path B]
    C --> E[Parallel Task 1]
    C --> F[Parallel Task 2] 
