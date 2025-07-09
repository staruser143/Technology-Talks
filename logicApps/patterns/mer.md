graph TD

%% MongoDB and CDC
A[MongoDB Atlas or AKS-hosted] --> B[Change Stream Listener<br>(Node.js / Azure Function)]

%% Events to Logic App
B --> C[Azure Event Grid / Service Bus Topic]
C --> D[Azure Logic App]

%% Logic App Trigger Paths
D --> E1[Trigger Batch/Real-time Jobs<br>(Azure Batch / Azure Function)]
D --> E2[Read/Write MongoDB<br>via API / Azure Function]
D --> E3[Send Conditional Emails<br>Office365 Connector]
D --> E4[Secure File Transfer<br>via SFTP Connector]
D --> E5[Call REST/SOAP APIs<br>via HTTP Connector]
D --> E6[Connect to On-Prem Systems<br>via Data Gateway]
D --> E7[Invoke AWS Services<br>via REST API / HTTPS]

%% Additional Triggers
F[Recurrence Trigger] --> D
G[Event Grid Trigger<br>(e.g., Blob Created)] --> D

%% Styling
classDef storage fill:#fff5e6,stroke:#ff9900,stroke-width:2px;
class A,F,G storage;