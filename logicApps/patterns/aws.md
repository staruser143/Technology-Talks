```mermaid
graph TD
  A[Trigger: Event Grid / Schedule / HTTP] --> B[Azure Logic App]
  B --> C[Call AWS API Gateway / Lambda / S3<br>via HTTPS or REST API]
