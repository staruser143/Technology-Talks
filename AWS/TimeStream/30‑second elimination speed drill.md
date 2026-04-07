Alright — **30‑second elimination speed drill**.  
This is designed so you can **answer instinctively** without overthinking, exactly how SAP‑C02 expects.

Read → eliminate → lock answer.  
No explanations while drilling. Explanations come *after*.

***

# ⚡ 30‑Second Elimination Speed Drill

**(Timestream vs DynamoDB vs OpenSearch vs S3)**

***

## Q1

Time‑stamped data, **full‑text search**, log correlation, dashboards.

**A.** Timestream  
**B.** DynamoDB  
**C.** OpenSearch  
**D.** S3 + Athena

✅ **Answer:** **C**

***

## Q2

Millions of metrics per second, **append‑only**, queries like “avg over last 5 minutes”.

**A.** DynamoDB  
**B.** Timestream  
**C.** OpenSearch  
**D.** S3

✅ **Answer:** **B**

***

## Q3

Low‑latency point lookups using `deviceId + timestamp`, **frequent updates**.

**A.** Timestream  
**B.** S3 + Athena  
**C.** DynamoDB  
**D.** OpenSearch

✅ **Answer:** **C**

***

## Q4

Logs stored for **5 years**, queried **once a month**, lowest cost required.

**A.** OpenSearch UltraWarm  
**B.** DynamoDB IA  
**C.** Timestream  
**D.** S3 + Athena

✅ **Answer:** **D**

***

## Q5

Near‑real‑time dashboards, sliding time windows, serverless, no cluster management.

**A.** OpenSearch  
**B.** DynamoDB  
**C.** S3  
**D.** Timestream

✅ **Answer:** **D**

***

## Q6

Time‑stamped records with **frequent deletes and overwrites**, strong consistency.

**A.** Timestream  
**B.** DynamoDB  
**C.** S3 + Athena  
**D.** OpenSearch

✅ **Answer:** **B**

***

## Q7

Application emits log messages with error strings, keywords, filters.

**A.** DynamoDB  
**B.** Timestream  
**C.** OpenSearch  
**D.** S3

✅ **Answer:** **C**

***

## Q8

Time‑series data but **no aggregations** needed; access is key‑based only.

**A.** Timestream  
**B.** DynamoDB  
**C.** OpenSearch  
**D.** S3

✅ **Answer:** **B**

***

## Q9

Historical trend analysis only, **PB‑scale**, cost matters more than latency.

**A.** DynamoDB  
**B.** OpenSearch  
**C.** Timestream  
**D.** S3 + Athena

✅ **Answer:** **D**

***

## Q10

Metrics ingestion at scale + short‑term dashboards **and** Amazon Managed Grafana.

**A.** DynamoDB  
**B.** S3  
**C.** Timestream  
**D.** OpenSearch

✅ **Answer:** **C**

***

# 🧠 Instant Elimination Reflex (Memorize)

Say this **in your head**, fast:

*   **Search words?** → ❌ everything except **OpenSearch**
*   **Point lookup?** → ❌ everything except **DynamoDB**
*   **Metrics + time windows?** → ❌ everything except **Timestream**
*   **Lowest cost, cold data?** → ❌ everything except **S3**

***

