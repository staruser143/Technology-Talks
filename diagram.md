```mermaid
graph TD
    Client -->|Sends Events| Kafka[Kafka (Event Store)]
    Kafka -->|Updates| MongoDB[MongoDB (Read Models)]
    Kafka -->|Archives| S3[S3 (Archiving)]
```
