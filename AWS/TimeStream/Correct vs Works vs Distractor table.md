Below is a **SAP‑C02 “Correct vs Works vs Distractor”** table version of the decision tree (**Timestream vs DynamoDB vs OpenSearch vs S3/Athena**).  
Use this for **fast elimination** in multi‑answer questions.

***

# ✅ Correct vs Works vs Distractor (Time‑series choices)

## 1) **Primary requirement: Time‑window analytics on metrics (avg/sum over last N minutes, BIN, sliding windows)**

| Option                | Classification           | Why (SAP‑C02 elimination logic)                                                                                                      |
| --------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------ |
| **Amazon Timestream** | ✅ **Correct**            | Purpose‑built time‑series DB: optimized for **time‑range queries + aggregations + retention tiering**.                               |
| **S3 + Athena**       | ⚠️ **Works**             | Works for **batch analytics** on historical time windows, but not ideal for **near‑real‑time dashboards** or high‑frequency queries. |
| **DynamoDB**          | ⚠️ **Works (with pain)** | Can model time series with PK/SK and GSIs, but **window aggregations** become application-heavy and costlier.                        |
| **OpenSearch**        | ❌ **Distractor**         | Great for text/log search; time histograms exist, but it’s not the “AWS default best” for metrics aggregations at scale.             |

***

## 2) **Primary requirement: Free‑text search across time‑stamped logs (keywords, stack traces, partial matches)**

| Option                | Classification   | Why                                                                                                        |
| --------------------- | ---------------- | ---------------------------------------------------------------------------------------------------------- |
| **Amazon OpenSearch** | ✅ **Correct**    | Designed for **full‑text search**, filtering, log analytics, and correlation.                              |
| **S3 + Athena**       | ⚠️ **Works**     | Works if logs land in S3 and queries are **ad‑hoc/batch**; not as interactive as OpenSearch for search UX. |
| **DynamoDB**          | ❌ **Distractor** | No native full‑text search; would require external search layer.                                           |
| **Timestream**        | ❌ **Distractor** | Not meant for free‑text search/log message indexing.                                                       |

***

## 3) **Primary requirement: Key‑value, single‑digit ms reads/writes (deviceId + timestamp), point lookups dominate**

| Option              | Classification           | Why                                                                                                                                   |
| ------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------- |
| **Amazon DynamoDB** | ✅ **Correct**            | Best fit for **KV access patterns**, predictable latency, TTL, and frequent point reads/writes.                                       |
| **Timestream**      | ⚠️ **Works (sometimes)** | Can store time series, but if workload is **primarily point lookup** rather than time analytics, DynamoDB is the exam-favored answer. |
| **S3 + Athena**     | ❌ **Distractor**         | Not for low-latency point reads; it’s batch/analytical.                                                                               |
| **OpenSearch**      | ❌ **Distractor**         | Not a KV store; higher operational/cost footprint for simple lookups.                                                                 |

***

## 4) **Primary requirement: Millions of writes/sec telemetry, append‑only, built‑in retention + tiering**

| Option                | Classification          | Why                                                                                                                      |
| --------------------- | ----------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| **Amazon Timestream** | ✅ **Correct**           | Classic Timestream signal: **high ingest + append-only + time functions + retention policies**.                          |
| **DynamoDB**          | ⚠️ **Works**            | Can scale ingestion, but schema/cost can be painful for time-range analytics; no native time-series analytics functions. |
| **S3 + Athena**       | ⚠️ **Works (for cold)** | Works if you can accept **ingest to S3** and query later; not ideal for near-real-time.                                  |
| **OpenSearch**        | ❌ **Distractor**        | Can ingest, but expensive and not optimized for time-series metric storage at very high ingestion rates.                 |

***

## 5) **Primary requirement: Frequent updates/deletes to time‑stamped records + strong consistency needs**

| Option              | Classification           | Why                                                                                                  |
| ------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------- |
| **Amazon DynamoDB** | ✅ **Correct**            | Updates/deletes are normal KV operations; strong consistency available for reads (where applicable). |
| **OpenSearch**      | ⚠️ **Works (not ideal)** | Supports updates, but not the best fit for OLTP-like record mutation.                                |
| **Timestream**      | ❌ **Distractor**         | Exam red flag: **append‑optimized**; frequent mutation is a “don’t pick Timestream” hint.            |
| **S3 + Athena**     | ❌ **Distractor**         | Object storage isn’t designed for record-level updates/deletes as a primary pattern.                 |

***

## 6) **Primary requirement: Lowest cost, 3–7 years retention, queries weekly/monthly (cold historical analytics)**

| Option                 | Classification   | Why                                                                                         |
| ---------------------- | ---------------- | ------------------------------------------------------------------------------------------- |
| **Amazon S3 + Athena** | ✅ **Correct**    | Cheapest durable storage + serverless SQL queries; best for **cold, infrequent analytics**. |
| **Timestream**         | ⚠️ **Works**     | Can retain data, but not typically the lowest-cost long-term store compared to S3.          |
| **OpenSearch**         | ❌ **Distractor** | Expensive for long retention; clusters/tiers cost more than S3.                             |
| **DynamoDB**           | ❌ **Distractor** | Costly at large retention volumes; not meant for cheap archival analytics.                  |

***

# 🎯 “Trigger Phrase → Correct Answer” (1‑line reflex)

*   **“Free‑text search, log correlation”** → ✅ **OpenSearch**
*   **“KV low latency, point lookup”** → ✅ **DynamoDB**
*   **“Time-window aggregations, append-only telemetry, retention tiering”** → ✅ **Timestream**
*   **“Cheapest long-term retention, batch queries”** → ✅ **S3 + Athena**

***

# ⚡ Mini “Exam Trap” Reminders

*   **Time-stamped ≠ time-series DB automatically**  
    If it says **point lookups + low latency**, lean **DynamoDB**.
*   If it says **full‑text search**, eliminate everything except **OpenSearch** (S3+Athena only if “batch/ad-hoc”).
*   If it says **lowest cost retention**, eliminate **OpenSearch/DynamoDB** first; pick **S3+Athena**.
*   If it says **updates/deletes frequently**, eliminate **Timestream**.

***
