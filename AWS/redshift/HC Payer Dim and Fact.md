Excellent choice of example — **Member / Enrollment / Claim** is *the* canonical healthcare‑payer model, and it maps perfectly to a **lakehouse + Redshift** architecture.

I’ll walk this **exactly the way a real payer platform is built**, not textbook‑style.

***

# ✅ Real Healthcare Payer Lakehouse Example

## (Member · Enrollment · Claim)

We’ll cover:

1.  **Business entities (what they really mean)**
2.  **Fact & Dimension model (star schema)**
3.  **Where each entity lives (S3 vs Redshift)**
4.  **How facts/dims are built in lakehouse**
5.  **Concrete SQL examples**
6.  **Typical analytics queries**

***

## 1️⃣ Healthcare Payer Core Entities (Real Meaning)

### 🧍 Member

*   A person covered by a health plan
*   Identified by **member\_id**, SSN (tokenized), DOB
*   Slowly changing (address, PCP, risk score change)

### 📅 Enrollment

*   Coverage periods for a member
*   One member → **multiple enrollment spans**
*   Plan, product, network, employer
*   Time‑dependent (start\_date / end\_date)

### 🧾 Claim

*   Medical or pharmacy event
*   High‑volume (millions/month)
*   Financially heavy (allowed, paid, deductible)
*   Tied to **member + enrollment + provider**

***

## 2️⃣ Canonical Star Schema (What Redshift Hosts)

### ⭐ Dimensions

| Dimension     | Description         |
| ------------- | ------------------- |
| dim\_member   | Who is the member   |
| dim\_plan     | Benefit / product   |
| dim\_provider | Doctor / hospital   |
| dim\_date     | Calendar attributes |

### ⭐ Facts

| Fact             | Grain                                  |
| ---------------- | -------------------------------------- |
| fact\_enrollment | One row per member per coverage period |
| fact\_claim      | One row per claim line                 |

✅ **Important:**  
Enrollment **is a fact** (time‑based transactional coverage), not a dimension — this is a common mistake.

***

## 3️⃣ Lakehouse Placement (Critical Architecture Mapping)

### 📦 Amazon S3 (Data Lake)

*   Raw eligibility files (834)
*   Raw claims files (837)
*   Pharmacy feeds
*   Provider directories
*   Reference codes (ICD, CPT)

Stored as:

*   **Bronze**: CSV/JSON (raw)
*   **Silver**: Parquet (cleaned)
*   **Gold**: Parquet/Iceberg (analytics‑ready)

### 🏗 Amazon Redshift (Warehouse Layer)

*   dim\_member
*   dim\_plan
*   dim\_provider
*   dim\_date
*   fact\_enrollment
*   fact\_claim

✅ Redshift holds **curated, query‑optimized** data only.

***

## 4️⃣ Fact & Dimension Design (Realistic)

### 🧍 `dim_member` (SCD Type 2)

```sql
CREATE TABLE dim_member (
    member_sk BIGINT IDENTITY,
    member_id VARCHAR(50),
    dob DATE,
    gender CHAR(1),
    risk_score DECIMAL(5,2),
    effective_date DATE,
    expiration_date DATE,
    current_flag BOOLEAN
)
DISTKEY(member_id)
SORTKEY(member_id, effective_date);
```

✅ Built from:

*   Eligibility feeds in S3 (834)
*   Risk scores from analytics jobs
*   Tokenized PII

***

### 📅 `fact_enrollment`

```sql
CREATE TABLE fact_enrollment (
    enrollment_sk BIGINT IDENTITY,
    member_sk BIGINT,
    plan_sk BIGINT,
    start_date DATE,
    end_date DATE,
    coverage_status VARCHAR(20)
)
DISTKEY(member_sk)
SORTKEY(start_date, end_date);
```

✅ Grain:

> **One row per member per continuous coverage span**

✅ Built from:

*   Parquet enrollment files in S3
*   Joined to dim\_member and dim\_plan

***

### 🧾 `fact_claim`

```sql
CREATE TABLE fact_claim (
    claim_sk BIGINT IDENTITY,
    claim_id VARCHAR(50),
    member_sk BIGINT,
    provider_sk BIGINT,
    service_date DATE,
    allowed_amount DECIMAL(12,2),
    paid_amount DECIMAL(12,2),
    claim_status VARCHAR(20)
)
DISTKEY(member_sk)
SORTKEY(service_date);
```

✅ Grain:

> **One row per claim line (not header)**

✅ Built from:

*   Massive claims datasets in S3 (Parquet)
*   Incremental loads

***

## 5️⃣ How These Are Built in a Lakehouse (End‑to‑End)

### Step 1: Ingest Raw Data → S3

*   834 / 837 feeds
*   Stored as raw zone

### Step 2: Transform → S3 (Glue / EMR)

*   Clean formats
*   Normalize units
*   Create Silver/Gold Parquet

### Step 3: External Tables (Spectrum)

```sql
CREATE EXTERNAL TABLE spectrum_claims (...)
STORED AS PARQUET
LOCATION 's3://lake/gold/claims/';
```

### Step 4: Build Facts & Dims in Redshift (CTAS)

```sql
CREATE TABLE fact_claim AS
SELECT
  c.claim_id,
  m.member_sk,
  p.provider_sk,
  c.service_date,
  c.allowed_amount,
  c.paid_amount,
  c.status
FROM spectrum_claims c
JOIN dim_member m
  ON c.member_id = m.member_id
 AND c.service_date BETWEEN m.effective_date AND m.expiration_date
JOIN dim_provider p
  ON c.provider_id = p.provider_id;
```

✅ This is the **lakehouse moment**

*   Data stays in S3
*   Redshift curates & optimizes

***

## 6️⃣ Real Analytics Queries Payers Run

### ✅ Medical Cost per Member per Month (PMPM)

```sql
SELECT
  d.year,
  SUM(f.allowed_amount) / COUNT(DISTINCT e.member_sk) AS pmpm
FROM fact_claim f
JOIN fact_enrollment e
  ON f.member_sk = e.member_sk
JOIN dim_date d
  ON f.service_date = d.date_key
GROUP BY d.year;
```

***

### ✅ Active Members by Plan

```sql
SELECT
  p.plan_name,
  COUNT(DISTINCT e.member_sk)
FROM fact_enrollment e
JOIN dim_plan p
  ON e.plan_sk = p.plan_sk
WHERE CURRENT_DATE BETWEEN e.start_date AND e.end_date
GROUP BY p.plan_name;
```

***

### ✅ High‑Cost Members (Risk Analytics)

```sql
SELECT
  m.member_id,
  SUM(f.allowed_amount) AS total_cost
FROM fact_claim f
JOIN dim_member m
  ON f.member_sk = m.member_sk
GROUP BY m.member_id
HAVING SUM(f.allowed_amount) > 100000;
```

***

## 7️⃣ Why This Works So Well in a Lakehouse

✅ **Scale**: Claims stay cheap in S3  
✅ **Performance**: Hot facts in Redshift  
✅ **Governance**: IAM + row/column security  
✅ **Flexibility**: Rebuild facts without re‑ingesting raw data  
✅ **Compliance**: Tokenization, audit, lineage

***

## ✅ Final Takeaway

*   ✅ You **still model Member / Enrollment / Claim exactly like a traditional warehouse**
*   ✅ Redshift still hosts all fact & dimension tables
*   ✅ The lakehouse changes **how data is sourced and transformed**, not the model
*   ✅ Redshift + Spectrum is the **analytics brain** of the payer lakehouse

***

