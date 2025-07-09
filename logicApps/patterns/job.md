graph TD
  A[MongoDB Atlas / AKS-hosted] --> B[Change Stream Listener<br>(Node.js / Azure Function)]
  B --> C[Azure Event Grid / Service Bus Topic]
  C --> D[Azure Logic App]
  D --> E[Trigger Azure Function<br>or Azure Batch Job]