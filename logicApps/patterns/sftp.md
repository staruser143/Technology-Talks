graph TD
  A[Trigger: Schedule or Event] --> B[Azure Logic App]
  B --> C[Download/Upload Files<br>via SFTP Connector]
  C --> D[Archive / Notify / Next Step]