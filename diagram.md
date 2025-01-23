```mermaid
  graph TD;
      A-->B;
      A-->C;
      B-->D;
      C-->D;
```
```mermaid
  graph TD;
    Command[Command] -->|Sends Command| CommandHandler[Command Handler]
    CommandHandler -->|Stores Event| EventStore[Event Store]
    EventStore -->|Publishes 

```mermaid
  graph TD
    Kafka[Kafka (Event Store)] -->|Older Events| S3[S3 (Archiving)]
    S3 -->|Stores| ArchivedData[Archived Data]
```
