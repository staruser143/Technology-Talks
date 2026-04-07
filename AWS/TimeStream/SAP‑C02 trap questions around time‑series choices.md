Below are **SAP‑C02–style trap questions** focused specifically on **time‑series data service selection**, designed to test **service boundaries, hidden constraints, and exam distractors**.

Each question is **intentionally tricky** and reflects *how AWS frames services in the exam*.

***

# 🔥 SAP‑C02 Trap Questions: Time‑Series Choices

***

## Question 1: “Metrics at Massive Scale”

A company collects **millions of sensor readings per second** from IoT devices worldwide.  
The data is **append‑only**, queried by **time windows**, and automatically expires after 90 days.  
The company does **not** want to manage servers or storage tiers.

Which solution is **MOST appropriate**?

**A.** Amazon DynamoDB with TTL  
**B.** Amazon Timestream  
**C.** Amazon OpenSearch Service  
**D.** Amazon Aurora with partitioning

✅ **Correct answer:** **B**

### Trap logic

| Option | Why it’s a trap                                          |
| ------ | -------------------------------------------------------- |
| A      | Works, but inefficient & costly for time‑range analytics |
| C      | Good for logs/search, not metric aggregations            |
| D      | Requires heavy management, not optimized for time‑series |

***

## Question 2: “Metrics + Full‑Text Search”

An operations team wants to:

*   Store **application metrics**
*   Perform **time‑series aggregations**
*   Also run **full‑text search** over error messages

Which architecture is **BEST**?

**A.** Amazon Timestream only  
**B.** Amazon OpenSearch only  
**C.** Timestream for metrics + OpenSearch for logs  
**D.** DynamoDB with GSIs

✅ **Correct answer:** **C**

### Exam insight

> When metrics **and** search are both required, AWS expects **service separation**, not overloading.

***

## Question 3: “Time‑Series vs Key‑Value Confusion”

A workload requires:

*   Single‑digit millisecond latency
*   Key‑based access (`deviceId + timestamp`)
*   Occasional range scans
*   **No complex aggregations**

Which service is the **BEST** choice?

**A.** Amazon Timestream  
**B.** Amazon DynamoDB  
**C.** Amazon Aurora Serverless  
**D.** Amazon S3 with Athena

✅ **Correct answer:** **B**

### Hidden trap

> **Timestream is NOT always the answer** for timestamped data.  
> If access is **key‑value first**, DynamoDB wins.

***

## Question 4: “Deletes and Updates”

A trading application stores **time‑stamped events**, but:

*   Frequently **updates records**
*   Performs **point deletes**
*   Requires **strong consistency**

Which service should be used?

**A.** Amazon Timestream  
**B.** Amazon DynamoDB  
**C.** Amazon OpenSearch  
**D.** Amazon S3

✅ **Correct answer:** **B**

🚨 **Exam red flag**

> Timestream is **append‑optimized**.  
> Frequent updates/deletes ⇒ **NOT Timestream**.

***

## Question 5: “Historical Analytics at Lowest Cost”

A company wants to:

*   Retain **5 years of time‑series data**
*   Run **weekly analytical queries**
*   Prioritize **lowest storage cost**

Which solution is BEST?

**A.** Amazon Timestream  
**B.** Amazon OpenSearch UltraWarm  
**C.** Amazon S3 + Athena  
**D.** Amazon DynamoDB Standard‑IA

✅ **Correct answer:** **C**

### AWS exam pattern

*   **Hot + real‑time** ⇒ Timestream
*   **Cold + cheap + batch** ⇒ S3 + Athena

***

## Question 6: “Near‑Real‑Time Dashboards”

Operations teams need:

*   Near‑real‑time aggregation
*   Sliding window queries
*   Built‑in time functions
*   Minimal infrastructure management

Select **TWO**.

**A.** Amazon Timestream  
**B.** Amazon DynamoDB  
**C.** Amazon Aurora PostgreSQL  
**D.** Amazon OpenSearch Service

✅ **Correct answers:** **A, D**

### Why this is tricky

*   DynamoDB ≠ built‑in time analytics
*   Aurora can do it, but **not managed or optimized**

***

## Question 7: “Prometheus‑Style Metrics”

A company runs Kubernetes and already uses **Prometheus format metrics**.  
They want **long‑term retention** without managing infrastructure.

Which solution is BEST?

**A.** Self‑managed Prometheus on EC2  
**B.** Amazon Timestream  
**C.** Amazon Managed Service for Prometheus  
**D.** Amazon DynamoDB

✅ **Correct answer:** **C**

⚠️ Common exam mistake

> Timestream ≠ Prometheus replacement  
> AWS expects **Managed Prometheus** for native Prom workloads.

***

## Question 8: “Operational Logs Misleading Trap”

An application emits:

*   Timestamped **log events**
*   Requires **free‑text search**
*   Needs **error correlation**

Which service is BEST?

**A.** Amazon Timestream  
**B.** Amazon OpenSearch Service  
**C.** Amazon DynamoDB  
**D.** Amazon Aurora

✅ **Correct answer:** **B**

🚨 Exam trick

> Logs are *time‑stamped*, but **time‑series ≠ logs**

***

# 🧠 SAP‑C02 Memory Hook

**Use Timestream when ALL are true:**

*   Append‑only
*   Time‑range analytics
*   Aggregations over windows
*   Minimal management
*   Metrics / telemetry data

If **search, updates, joins, or OLTP** appear → **eliminate Timestream**

***


