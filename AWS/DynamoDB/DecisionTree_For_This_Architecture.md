Below is a **single end‑to‑end decision tree** that **logically converges** to the **DynamoDB + Timestream + Aurora** architecture in the mega‑scenario.  
This is written in **SAP‑C02 elimination style** so you can *see* how AWS wants you to think.

***

# Unified Decision Tree → DynamoDB + Timestream + Aurora

    START
     |
     |-- Does the workload involve MULTIPLE data access patterns?
     |     |-- NO → Single database may suffice
     |     |-- YES
     |
     |-- Split data by semantics, NOT convenience
     |
     |================================================================
     | PATH 1: SENSOR / EVENT DATA
     |================================================================
     |
     |-- Is the data continuously generated with timestamps?
     |     |-- NO → Skip to App Data Path
     |     |-- YES
     |
     |-- Is the write throughput extremely high (100K+ writes/sec)?
     |     |-- NO → Aurora possible
     |     |-- YES
     |
     |-- Are queries based on (device_id, time_range)?
     |     |-- NO → OpenSearch / Redshift
     |     |-- YES
     |
     |-- Do you need sub‑10ms latency for live dashboards?
     |     |-- NO → Timestream only
     |     |-- YES
     |
     |-- Do you need GLOBAL write + read?
     |     |-- YES → DynamoDB Global Tables  ✅
     |     |-- NO → DynamoDB (single region)
     |
     |-- Do you need to retain data for YEARS with cost efficiency?
     |     |-- YES → Also send to Timestream ✅
     |
     |================================================================
     | PATH 2: TIME‑SERIES ANALYTICS
     |================================================================
     |
     |-- Is the data queried mainly by time windows and aggregates?
     |     |-- NO → DynamoDB / Redshift
     |     |-- YES
     |
     |-- Is long‑term retention required (months to years)?
     |     |-- NO → DynamoDB TTL sufficient
     |     |-- YES
     |
     |-- Are built‑in time functions needed (AVG, BIN, RATE)?
     |     |-- YES → Timestream ✅
     |     |-- NO → S3 + Athena
     |
     |================================================================
     | PATH 3: APPLICATION / BUSINESS DATA
     |================================================================
     |
     |-- Does the app manage users, tenants, and configurations?
     |     |-- NO → Skip
     |     |-- YES
     |
     |-- Do entities require relationships (tenant → device → user)?
     |     |-- NO → DynamoDB possible
     |     |-- YES
     |
     |-- Do you need joins, constraints, or transactions?
     |     |-- NO → DynamoDB
     |     |-- YES
     |
     |-- Is SQL‑based reporting required?
     |     |-- YES → Aurora ✅
     |

***

# Final Architecture (Decision Tree Output)

✅ **DynamoDB**  
→ Hot operational telemetry  
→ Massive global scale  
→ Millisecond dashboards  
→ TTL‑based retention

✅ **Timestream**  
→ Long‑term time‑series analytics  
→ Time‑window queries  
→ Aggregations and trends  
→ Cost‑optimized retention

✅ **Aurora (PostgreSQL/MySQL)**  
→ Multi‑tenant SaaS metadata  
→ Users, devices, permissions  
→ Transactions and joins  
→ ERP / reporting integration

***

# Why This Tree Matches AWS Exam Logic

AWS certification questions frequently:

*   **Force multiple correct answers**
*   **Punish “one DB for everything” designs**
*   Reward **semantic separation of data**

This tree emphasizes:

*   **Access pattern first**
*   **Latency vs analytics**
*   **Operational vs analytical vs relational**

***

# Single‑line Exam Elimination Rules (Memorize These)

| If the question says…           | You should think… |
| ------------------------------- | ----------------- |
| “millions of events per second” | DynamoDB          |
| “last X minutes dashboard”      | DynamoDB          |
| “aggregated over months”        | Timestream        |
| “users, tenants, permissions”   | Aurora            |
| “relational integrity”          | Aurora            |
| “time‑series optimized”         | Timestream        |

***

