Below is a **clear, exam‑ready decision tree** to choose between  
**Amazon Timestream vs DynamoDB vs OpenSearch vs S3 (+ Athena)** — tuned for **SAP‑C02 elimination logic**.

This is written the way AWS **expects you to think under pressure**.

***

# ✅ Decision Tree: Choosing the Right AWS Data Store

***

## 🔴 STEP 1 — Is the data **time‑stamped**?

**NO** → ❌ Not this tree  
**YES** → Continue

***

## 🔴 STEP 2 — What is the **primary access pattern**?

### ✅ A. Free‑text search / log correlation required?

(contains keywords, stack traces, partial text lookup)

→ ✅ **Amazon OpenSearch Service**

**Why AWS expects this**

*   Full‑text search
*   Log analytics
*   Keyword-based filtering

🚫 **Eliminate**

*   Timestream (no search)
*   DynamoDB (no full text)
*   S3 (batch only)

***

### ✅ B. Key‑value access first?

(lookup by `PK`, low latency, maybe occasional range scan)

→ ✅ **Amazon DynamoDB**

**Signals**

*   Single-digit ms latency
*   Point reads / writes
*   Updates and deletes common
*   TTL acceptable but analytics minimal

🚫 **Eliminate**

*   Timestream (append‑optimized, not KV)
*   OpenSearch (too heavy)
*   S3 (not interactive)

***

### ✅ C. Time‑window analytics / aggregations?

(avg, sum, bin, sliding windows over time)

→ Continue to Step 3

***

## 🔴 STEP 3 — Is the data **append‑only**?

### ✅ YES (metrics / telemetry / sensor data)

→ ✅ **Amazon Timestream**

**Strong signals**

*   Millions of writes/sec
*   Retention policies
*   Hot + cold tiering
*   SQL with time functions
*   Near‑real‑time dashboards

🚫 **Eliminate**

*   DynamoDB (cost + design pain)
*   OpenSearch (not metric‑optimized)
*   S3 (not real‑time)

***

### ❌ NO (updates, deletes, overwrites frequent)

→ ✅ **Amazon DynamoDB**

**Exam rule**

> Frequent updates or deletes → NOT Timestream

***

## 🔴 STEP 4 — Data access frequency & cost sensitivity

### ✅ Cold data, long‑term retention, batch analytics

(monthly / weekly queries, cheapest storage)

→ ✅ **Amazon S3 + Athena**

**Signals**

*   1–10 TB+
*   Historical trends
*   Cost optimization emphasized
*   No real‑time needs

🚫 **Eliminate**

*   OpenSearch (expensive always‑on)
*   DynamoDB (cost grows fast)
*   Timestream (not cheapest long‑term)

***

## 🟦 FINAL ONE‑GLANCE TREE (Exam Friendly)

    Is data time‑stamped?
    │
    ├── Need free‑text search?
    │     └── YES → OpenSearch
    │
    ├── Key‑value access, low latency?
    │     └── YES → DynamoDB
    │
    ├── Time‑window analytics?
    │     │
    │     ├── Append‑only metrics?
    │     │     └── YES → Timestream
    │     │
    │     └── Frequent updates/deletes?
    │           └── YES → DynamoDB
    │
    └── Cold data, cheapest storage, batch queries?
          └── YES → S3 + Athena

***

## 🧠 SAP‑C02 Elimination Rules (Memorize)

| Phrase in question        | Eliminate            | Correct    |
| ------------------------- | -------------------- | ---------- |
| “full‑text search”        | Timestream, DynamoDB | OpenSearch |
| “millions of metrics/sec” | DynamoDB, S3         | Timestream |
| “frequent updates”        | Timestream           | DynamoDB   |
| “lowest cost storage”     | OpenSearch, DynamoDB | S3         |
| “real‑time dashboards”    | S3                   | Timestream |

***

## ✅ One‑Line Exam Memory Hack

> **Search → OpenSearch**  
> **Keys → DynamoDB**  
> **Metrics → Timestream**  
> **Cheap history → S3**

***


