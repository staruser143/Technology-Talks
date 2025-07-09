graph TD
  A[Recurrence Trigger<br>or Event Grid] --> B[Azure Logic App]
  B --> C[Evaluate Conditions<br>via Expressions or API Call]
  C --> D[Send Email<br>Office365 / SMTP / Gmail Connector]