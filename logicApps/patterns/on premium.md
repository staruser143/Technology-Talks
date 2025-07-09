graph TD
  A[Trigger: Recurrence / Event] --> B[Azure Logic App]
  B --> C[Connect via On-Premises<br>Data Gateway]
  C --> D[Access SQL / SAP / Oracle<br>or File Shares]