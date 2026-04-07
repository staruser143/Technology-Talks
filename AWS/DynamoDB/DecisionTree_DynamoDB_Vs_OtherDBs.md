Below is a **single, exam‑ and architecture‑ready decision tree** for choosing **Amazon DynamoDB vs other AWS databases**, optimized for **SAP‑C02 / real‑world design thinking**.

***

## High‑Level Decision Tree (Start Here)

    START
     |
     |-- Is the workload STRICTLY relational?
     |     |-- YES → Amazon RDS / Aurora
     |     |-- NO
     |
     |-- Do you need sub‑millisecond latency at massive scale?
     |     |-- NO → Consider Aurora, OpenSearch, or S3-based analytics
     |     |-- YES
     |
     |-- Is the access pattern predictable (key-based)?
     |     |-- NO → OpenSearch / Aurora / Redshift
     |     |-- YES
     |
     |-- Do you need joins, complex transactions, or ad-hoc SQL?
     |     |-- YES → Aurora / RDS
     |     |-- NO
     |
     |-- Is the data semi-structured or key–value?
     |     |-- YES → DynamoDB
     |     |-- NO → Assess OpenSearch / Timestream

✅ If you reach the last **YES**, **DynamoDB is the primary fit**.

***

## Expanded Decision Tree (Service-Level Precision)

### 1️⃣ Data Model Decision

**Do you need a relational schema?**

| Requirement                   | Choose           |
| ----------------------------- | ---------------- |
| Joins, foreign keys, SQL      | **RDS / Aurora** |
| Key–value / document          | **DynamoDB**     |
| Time‑series (timestamp‑heavy) | **Timestream**   |
| Full‑text search              | **OpenSearch**   |

***

### 2️⃣ Scale & Performance

**Do you need horizontal scaling without capacity planning?**

| Scenario                      | Choose         |
| ----------------------------- | -------------- |
| Millions of TPS, auto‑scaling | ✅ **DynamoDB** |
| Vertical scaling acceptable   | RDS            |
| Query‑heavy analytics         | Redshift       |
| Log/query search              | OpenSearch     |

📌 **Trap:** Aurora *can scale*, but not to DynamoDB’s *request-level elasticity*.

***

### 3️⃣ Access Pattern Predictability

**Can you define the access pattern upfront?**

| Pattern                   | Choose              |
| ------------------------- | ------------------- |
| Known PK / SK access      | ✅ **DynamoDB**      |
| Changing / ad‑hoc queries | Aurora / OpenSearch |
| Report‑driven analytics   | Redshift / Athena   |

📌 **Exam clue:**  
“**Access patterns are known**” → DynamoDB hint

***

### 4️⃣ Latency Requirements

**Is consistent single‑digit millisecond latency required globally?**

| Requirement                 | Choose                         |
| --------------------------- | ------------------------------ |
| Global low latency          | ✅ **DynamoDB + Global Tables** |
| Regional strong consistency | Aurora                         |
| Eventual OK, analytical     | S3 / Redshift                  |

***

### 5️⃣ Transaction & Consistency Needs

**Do you need complex multi‑row ACID transactions?**

| Need                         | Choose     |
| ---------------------------- | ---------- |
| Multi‑table joins, rollbacks | Aurora     |
| Item‑level ACID              | ✅ DynamoDB |
| Time‑window consistency      | Timestream |

📌 DynamoDB supports **transactions**, but **not relational joins**.

***

### 6️⃣ Query Flexibility

**Do you need ad‑hoc filters or search?**

| Query Type          | Choose     |
| ------------------- | ---------- |
| Primary key / GSI   | ✅ DynamoDB |
| Text search / fuzzy | OpenSearch |
| SQL analytics       | Redshift   |
| Time predicates     | Timestream |

📌 **Trap:** “Filter expressions” ≠ efficient queries in DynamoDB.

***

### 7️⃣ Cost & Operational Model

**Do you want zero‑ops and pay‑per‑request?**

| Model                   | Choose      |
| ----------------------- | ----------- |
| No servers, no patching | ✅ DynamoDB  |
| Managed but provisioned | Aurora      |
| Low‑cost cold storage   | S3 + Athena |

***

## Final Decision Summary Table

| Requirement                | Best Choice    |
| -------------------------- | -------------- |
| Massive scale + key access | ✅ **DynamoDB** |
| Relational OLTP            | Aurora / RDS   |
| Time‑series data           | Timestream     |
| Search & logs              | OpenSearch     |
| Analytics & BI             | Redshift       |
| Cheap archive              | S3             |

***

## DynamoDB Is the **Correct Answer** When the Question Says:

✅ “Single‑digit millisecond latency”  
✅ “Massive scale with unpredictable traffic”  
✅ “Serverless, fully managed”  
✅ “Key‑value or document store”  
✅ “Access patterns known in advance”

🚫 **Not** when it asks for joins, ad‑hoc SQL, or reports.

***
