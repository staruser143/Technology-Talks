graph TD
  A[Trigger: Event / Schedule / HTTP Request] --> B[Azure Logic App]
  B --> C[Call External API<br>(REST/SOAP) via HTTP Action]
  C --> D[Process Response or Route to System]