graph TD
  A[MongoDB (source)] --> B[Change Stream Listener]
  B --> C[Event Grid / Service Bus]
  C --> D[Logic App Triggered]
  D --> E[Call Azure Function / API<br>to Read/Write to MongoDB]