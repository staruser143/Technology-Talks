Perfect. Below is the **Correct vs Works vs Distractor conversion** of the **DynamoDB vs Aurora vs Timestream decision tree**, written exactly in **SAP‑C02 elimination style**.

Think of each row as how AWS expects you to **grade answer options** in a multi‑select question.

***

## ✅ Correct vs ✅ Works vs ❌ Distractor — Mega Decision Matrix

### Scenario Context (Implicit in All Rows)

> Global IoT platform with **massive telemetry ingestion**, **real‑time dashboards**, **long‑term time‑series analytics**, and **multi‑tenant SaaS metadata**

***

## 1️⃣ High‑Throughput Sensor Telemetry (Hot Operational Path)

| Option                             | Classification        | Why                                                                                                             |
| ---------------------------------- | --------------------- | --------------------------------------------------------------------------------------------------------------- |
| **DynamoDB (Global Tables)**       | ✅ **Correct**         | Designed for millions of writes/sec, predictable key access, sub‑10ms latency, native multi‑region replication  |
| Aurora (Serverless or Provisioned) | ✅ *Works (Badly)*     | Can ingest data but will not scale to hundreds of thousands of writes/sec without sharding and operational pain |
| Timestream                         | ✅ *Works (Partially)* | Optimized for time‑series, but not ideal for ultra‑low‑latency operational dashboards                           |
| OpenSearch                         | ❌ **Distractor**      | Not designed for high‑velocity transactional ingestion                                                          |
| S3 + Athena                        | ❌ **Distractor**      | Batch analytics only — not real‑time                                                                            |

🧠 **Exam cue:**

> “Hundreds of thousands of events per second” + “millisecond latency”  
> → **DynamoDB is the only Correct answer**

***

## 2️⃣ Live Dashboard Queries (Last X Minutes)

| Option                    | Classification       | Why                                                  |
| ------------------------- | -------------------- | ---------------------------------------------------- |
| **DynamoDB**              | ✅ **Correct**        | Primary‑key access gives consistent low latency      |
| Timestream (Memory Store) | ✅ *Works*            | Can serve recent data but higher latency variability |
| Aurora                    | ✅ *Works (Poor Fit)* | Queryable but not at this scale or speed             |
| Redshift                  | ❌ **Distractor**     | Analytics engine, not real‑time                      |
| Athena                    | ❌ **Distractor**     | Cold, scan‑based queries                             |

🧠 **Trap:**  
“SQL‑like queries” ≠ correct choice for dashboards  
Latency beats flexibility.

***

## 3️⃣ Long‑Term Historical Time‑Series Analytics (Years of Data)

| Option         | Classification   | Why                                                               |
| -------------- | ---------------- | ----------------------------------------------------------------- |
| **Timestream** | ✅ **Correct**    | Purpose‑built for time‑series aggregation, tiering, and retention |
| DynamoDB       | ✅ *Works*        | Possible with TTL disabled, but inefficient and costly            |
| S3 + Athena    | ✅ *Works*        | Good for batch analytics, not optimized for time functions        |
| Aurora         | ❌ **Distractor** | Poor time‑series compression and scaling                          |
| OpenSearch     | ❌ **Distractor** | Not intended for long‑term numeric analytics                      |

🧠 **Exam cue:**

> “Aggregated over months/years”  
> → Think **Timestream**, not DynamoDB

***

## 4️⃣ Multi‑Tenant SaaS Application Metadata

| Option                        | Classification   | Why                                                   |
| ----------------------------- | ---------------- | ----------------------------------------------------- |
| **Aurora (PostgreSQL/MySQL)** | ✅ **Correct**    | Relational integrity, transactions, joins, reporting  |
| DynamoDB                      | ✅ *Works*        | Possible with heavy denormalization; painful at scale |
| Aurora Serverless             | ✅ *Works*        | Still Aurora — capacity elasticity only               |
| Timestream                    | ❌ **Distractor** | Time‑series only                                      |
| Redshift                      | ❌ **Distractor** | OLAP, not OLTP                                        |

🧠 **Exam cue:**

> “Users, tenants, permissions, relationships”  
> → **Aurora**

***

## 5️⃣ Global Low‑Latency Reads & Writes

| Option                     | Classification   | Why                                |
| -------------------------- | ---------------- | ---------------------------------- |
| **DynamoDB Global Tables** | ✅ **Correct**    | Native multi‑region active‑active  |
| Aurora Global Database     | ✅ *Works*        | Writes still region‑bound          |
| Timestream                 | ✅ *Works (Read)* | No multi‑region write optimization |
| S3 Replication             | ❌ **Distractor** | Async replication only             |
| RDS Multi‑AZ               | ❌ **Distractor** | HA, not global                     |

🧠 **Exam cue:**

> “Global active‑active”  
> → DynamoDB wins

***

## 6️⃣ Single Database for Everything?

| Option                                          | Classification   | Why                                       |
| ----------------------------------------------- | ---------------- | ----------------------------------------- |
| **Split by access pattern (DDB + TS + Aurora)** | ✅ **Correct**    | AWS‑recommended architecture              |
| DynamoDB only                                   | ✅ *Works*        | Overloaded design, fails relational needs |
| Aurora only                                     | ✅ *Works*        | Will bottleneck at scale                  |
| Timestream only                                 | ❌ **Distractor** | Not general‑purpose                       |
| Redshift only                                   | ❌ **Distractor** | Analytics‑only                            |

🧠 **SAP‑C02 rule:**

> AWS rewards **polyglot persistence**

***

# ✅ Final One‑Line Answer (What AWS Wants)

| Data Type                        | Correct Choice |
| -------------------------------- | -------------- |
| Hot, real‑time telemetry         | **DynamoDB**   |
| Historical time‑series analytics | **Timestream** |
| Relational application data      | **Aurora**     |

***

## 🔥 Exam Elimination Cheat Codes

*   **Extreme TPS** → DynamoDB
*   **Time‑window analytics** → Timestream
*   **Relationships & transactions** → Aurora
*   **One DB for all** → Always wrong

***
