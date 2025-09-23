Absolutely! Below is a **realistic, production-grade Amazon Redshift table schema** designed for a **healthcare analytics data warehouse**, following **star schema modeling** (facts + dimensions). This schema supports common healthcare use cases like:

- Patient encounter analytics
- Clinical quality measures
- Readmission risk
- Provider performance
- Cost and utilization trends

We’ll include:

✅ Fact tables (measurable events)  
✅ Dimension tables (descriptive context)  
✅ Sample DDL (Data Definition Language) with Redshift-specific optimizations  
✅ Distribution keys, sort keys, encoding, and constraints  
✅ Example data relationships

---

## 🧩 SCHEMA OVERVIEW — “Healthcare Analytics Star Schema”

```
fact_patient_encounter  ←──┐
    │                      │
    ├─→ dim_patient        │
    ├─→ dim_provider       │
    ├─→ dim_facility       │
    ├─→ dim_date           │
    └─→ dim_diagnosis      │
                           │
fact_lab_result  ←─────────┘
    │
    ├─→ dim_patient
    ├─→ dim_lab_test
    └─→ dim_date

fact_claim
    │
    ├─→ dim_patient
    ├─→ dim_payer
    ├─→ dim_date_service
    └─→ dim_procedure
```

---

# 📄 1. DIMENSION TABLES

---

### ✅ `dim_patient` — Patient Master

```sql
CREATE TABLE dim_patient (
    patient_sk          BIGINT IDENTITY(1,1) ENCODE RAW,  -- Surrogate Key
    patient_id          VARCHAR(50) NOT NULL ENCODE LZO,   -- Source system ID (EHR MRN)
    first_name          VARCHAR(100) ENCODE LZO,
    last_name           VARCHAR(100) ENCODE LZO,
    date_of_birth       DATE ENCODE DELTA,
    gender              CHAR(1) ENCODE BYTEDICT,           -- M/F/U
    race                VARCHAR(50) ENCODE LZO,
    ethnicity           VARCHAR(50) ENCODE LZO,
    zip_code            CHAR(5) ENCODE LZO,
    phone               VARCHAR(20) ENCODE LZO,
    email               VARCHAR(255) ENCODE LZO,
    ssn_last4           CHAR(4) ENCODE LZO,                -- Masked for compliance
    is_active           BOOLEAN DEFAULT TRUE,
    effective_date      DATE DEFAULT CURRENT_DATE,
    end_date            DATE DEFAULT '9999-12-31',
    current_flag        BOOLEAN DEFAULT TRUE,

    PRIMARY KEY (patient_sk)
)
DISTSTYLE KEY
DISTKEY (patient_sk)
SORTKEY (patient_id, effective_date);
```

> 🔐 **PHI Note**: Avoid storing full SSN, address, etc. in analytics layer. Use tokenized or masked values.

---

### ✅ `dim_provider` — Clinician/Staff

```sql
CREATE TABLE dim_provider (
    provider_sk         BIGINT IDENTITY(1,1),
    provider_id         VARCHAR(50) NOT NULL,              -- NPI or system ID
    first_name          VARCHAR(100) ENCODE LZO,
    last_name           VARCHAR(100) ENCODE LZO,
    specialty           VARCHAR(100) ENCODE LZO,           -- e.g., "Cardiology"
    department          VARCHAR(100) ENCODE LZO,
    facility_id         VARCHAR(50) ENCODE LZO,            -- FK to dim_facility
    license_state       CHAR(2) ENCODE BYTEDICT,
    is_primary_care     BOOLEAN,
    hire_date           DATE ENCODE DELTA,
    termination_date    DATE ENCODE DELTA,

    PRIMARY KEY (provider_sk)
)
DISTSTYLE KEY
DISTKEY (provider_sk)
SORTKEY (provider_id, specialty);
```

---

### ✅ `dim_facility` — Hospital/Clinic

```sql
CREATE TABLE dim_facility (
    facility_sk         BIGINT IDENTITY(1,1),
    facility_id         VARCHAR(50) NOT NULL,
    facility_name       VARCHAR(255) ENCODE LZO,
    facility_type       VARCHAR(50) ENCODE LZO,            -- Hospital, Clinic, ASC
    city                VARCHAR(100) ENCODE LZO,
    state               CHAR(2) ENCODE BYTEDICT,
    zip_code            CHAR(5) ENCODE LZO,
    beds                INTEGER ENCODE DELTA,
    emr_system          VARCHAR(50) ENCODE LZO,            -- e.g., "Epic", "Cerner"
    is_active           BOOLEAN DEFAULT TRUE,

    PRIMARY KEY (facility_sk)
)
DISTSTYLE ALL                              -- Small table → replicate to all nodes
SORTKEY (facility_id);
```

---

### ✅ `dim_date` — Date Dimension (Pre-populated)

```sql
CREATE TABLE dim_date (
    date_sk             BIGINT NOT NULL,
    full_date           DATE NOT NULL,
    day_of_week         VARCHAR(10) ENCODE LZO,
    day_of_month        SMALLINT ENCODE DELTA,
    day_of_year         SMALLINT ENCODE DELTA,
    week_of_year        SMALLINT ENCODE DELTA,
    month_number        SMALLINT ENCODE DELTA,
    month_name          VARCHAR(10) ENCODE LZO,
    quarter             SMALLINT ENCODE DELTA,
    year                SMALLINT ENCODE DELTA,
    is_weekend          BOOLEAN,
    is_holiday          BOOLEAN,
    holiday_name        VARCHAR(50) ENCODE LZO,

    PRIMARY KEY (date_sk)
)
DISTSTYLE ALL
SORTKEY (full_date);
```

> 💡 Populate this once using a script — essential for time-based analytics.

---

### ✅ `dim_diagnosis` — ICD-10 Codes

```sql
CREATE TABLE dim_diagnosis (
    diagnosis_sk        BIGINT IDENTITY(1,1),
    icd10_code          VARCHAR(10) NOT NULL ENCODE LZO,
    description         VARCHAR(500) ENCODE LZO,
    category            VARCHAR(100) ENCODE LZO,           -- e.g., "Diabetes", "Cancer"
    severity_level      SMALLINT ENCODE DELTA,             -- 1 to 5
    is_chronic          BOOLEAN,

    PRIMARY KEY (diagnosis_sk)
)
DISTSTYLE ALL
SORTKEY (icd10_code);
```

---

# 📊 2. FACT TABLES

---

### ✅ `fact_patient_encounter` — Core Clinical Fact Table

```sql
CREATE TABLE fact_patient_encounter (
    encounter_id        VARCHAR(100) NOT NULL ENCODE LZO,  -- Source system key
    patient_sk          BIGINT NOT NULL ENCODE DELTA DISTKEY SORTKEY, -- JOIN KEY
    provider_sk         BIGINT NOT NULL ENCODE DELTA,
    facility_sk         BIGINT NOT NULL ENCODE DELTA,
    admit_date_sk       BIGINT NOT NULL ENCODE DELTA,      -- FK to dim_date
    discharge_date_sk   BIGINT ENCODE DELTA,
    diagnosis_sk        BIGINT ENCODE DELTA,               -- Primary diagnosis
    admit_source        VARCHAR(50) ENCODE LZO,            -- ER, Clinic, Transfer
    discharge_disposition VARCHAR(50) ENCODE LZO,          -- Home, SNF, Expired
    length_of_stay_days SMALLINT ENCODE DELTA,
    total_charges       DECIMAL(12,2) ENCODE DELTA,
    total_payments      DECIMAL(12,2) ENCODE DELTA,
    readmission_flag    BOOLEAN DEFAULT FALSE,             -- Y/N within 30 days
    mortality_flag      BOOLEAN DEFAULT FALSE,
    etl_load_timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (patient_sk) REFERENCES dim_patient(patient_sk),
    FOREIGN KEY (provider_sk) REFERENCES dim_provider(provider_sk),
    FOREIGN KEY (facility_sk) REFERENCES dim_facility(facility_sk),
    FOREIGN KEY (admit_date_sk) REFERENCES dim_date(date_sk)
)
DISTSTYLE KEY
DISTKEY (patient_sk)
SORTKEY (admit_date_sk, patient_sk, facility_sk);
```

> ⚡ **Performance Tip**: `DISTKEY (patient_sk)` colocates patient data across nodes → faster joins with dim_patient.  
> `SORTKEY (admit_date_sk, ...)` enables fast range scans (e.g., “Q1 2025 encounters”).

---

### ✅ `fact_lab_result` — Lab Events

```sql
CREATE TABLE fact_lab_result (
    lab_result_id       VARCHAR(100) NOT NULL ENCODE LZO,
    patient_sk          BIGINT NOT NULL ENCODE DELTA DISTKEY SORTKEY,
    lab_test_sk         BIGINT NOT NULL ENCODE DELTA,      -- FK to dim_lab_test
    order_date_sk       BIGINT NOT NULL ENCODE DELTA,
    result_date_sk      BIGINT NOT NULL ENCODE DELTA,
    result_value        VARCHAR(100) ENCODE LZO,           -- "7.4", "Positive", "120/80"
    result_unit         VARCHAR(20) ENCODE LZO,
    normal_range_low    VARCHAR(50) ENCODE LZO,
    normal_range_high   VARCHAR(50) ENCODE LZO,
    is_abnormal         BOOLEAN,
    ordering_provider_sk BIGINT ENCODE DELTA,
    facility_sk         BIGINT ENCODE DELTA,
    etl_load_timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (patient_sk) REFERENCES dim_patient(patient_sk),
    FOREIGN KEY (lab_test_sk) REFERENCES dim_lab_test(lab_test_sk),
    FOREIGN KEY (order_date_sk) REFERENCES dim_date(date_sk)
)
DISTSTYLE KEY
DISTKEY (patient_sk)
SORTKEY (order_date_sk, patient_sk);
```

---

### ✅ `dim_lab_test` — Reference Table for Labs

```sql
CREATE TABLE dim_lab_test (
    lab_test_sk         BIGINT IDENTITY(1,1),
    loinc_code          VARCHAR(10) ENCODE LZO,            -- Standard code
    test_name           VARCHAR(255) ENCODE LZO,
    category            VARCHAR(100) ENCODE LZO,           -- Chemistry, Hematology
    specimen_type       VARCHAR(50) ENCODE LZO,

    PRIMARY KEY (lab_test_sk)
)
DISTSTYLE ALL
SORTKEY (loinc_code);
```

---

# 🧪 3. SAMPLE QUERIES

---

### 🔍 Example 1: Daily Patient Census by Facility

```sql
SELECT
    d.full_date,
    f.facility_name,
    COUNT(*) AS patient_admissions
FROM fact_patient_encounter e
JOIN dim_date d ON e.admit_date_sk = d.date_sk
JOIN dim_facility f ON e.facility_sk = f.facility_sk
WHERE d.full_date >= CURRENT_DATE - 30
GROUP BY 1, 2
ORDER BY 1 DESC, 2;
```

---

### 📈 Example 2: Average Length of Stay by Diagnosis

```sql
SELECT
    dx.description AS diagnosis,
    AVG(e.length_of_stay_days) AS avg_los
FROM fact_patient_encounter e
JOIN dim_diagnosis dx ON e.diagnosis_sk = dx.diagnosis_sk
WHERE e.length_of_stay_days > 0
GROUP BY dx.description
ORDER BY avg_los DESC
LIMIT 10;
```

---

### 🤖 Example 3: Readmission Rate by Provider (for Redshift ML)

```sql
SELECT
    p.last_name || ', ' || p.first_name AS provider,
    COUNT(*) AS total_encounters,
    SUM(CASE WHEN e.readmission_flag THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS readmit_rate
FROM fact_patient_encounter e
JOIN dim_provider p ON e.provider_sk = p.provider_sk
GROUP BY p.provider_sk, p.last_name, p.first_name
ORDER BY readmit_rate DESC;
```

---

# 🛡️ 4. COMPLIANCE & SECURITY NOTES

- **Mask PHI**: Avoid storing full names, SSN, exact DOB in fact tables. Use `dim_patient` with limited access.
- **Column-Level GRANTs**:

```sql
-- Restrict PHI access
REVOKE SELECT ON dim_patient FROM analyst_group;
GRANT SELECT (patient_sk, gender, zip_code, race) ON dim_patient TO analyst_group;
```

- **Row-Level Security** (via Views):

```sql
CREATE VIEW dim_patient_analyst AS
SELECT patient_sk, gender, zip_code, race, age_group
FROM dim_patient;
-- age_group calculated as: CASE WHEN EXTRACT(YEAR FROM AGE(dob)) < 18 THEN 'Minor' ...
```

- **Audit**: Enable `enable_user_activity_logging` in Redshift → logs to S3 → monitor with Athena.

---

# 🚀 BONUS: Redshift Materialized View for Daily Summary

```sql
CREATE MATERIALIZED VIEW mv_daily_encounter_summary
SORTKEY (admit_date)
AS
SELECT
    d.full_date AS admit_date,
    f.facility_name,
    COUNT(*) AS admissions,
    AVG(e.length_of_stay_days) AS avg_los,
    SUM(e.total_charges) AS total_revenue
FROM fact_patient_encounter e
JOIN dim_date d ON e.admit_date_sk = d.date_sk
JOIN dim_facility f ON e.facility_sk = f.facility_sk
GROUP BY d.full_date, f.facility_name;
```

Refresh automatically or on schedule:

```sql
REFRESH MATERIALIZED VIEW mv_daily_encounter_summary;
```

---

## ✅ Summary: Why This Schema Works in Healthcare

| Feature | Benefit |
|--------|---------|
| **Star Schema** | Optimized for analytics, easy for BI tools |
| **Surrogate Keys** | Decouples from source systems, handles SCD |
| **DISTKEY on patient_sk** | Colocates patient data → faster joins |
| **SORTKEY on dates** | Fast time-range queries (critical for trends) |
| **Encoding (LZO, DELTA)** | Reduces storage, speeds I/O |
| **Compliance-ready** | PHI minimized, access controls, audit logs |

---

## 🎁 Want More?

I can generate:

✅ A **sample dataset INSERT script** to populate these tables  
✅ A **Glue ETL script** that transforms raw CSV → this schema  
✅ A **QuickSight dashboard** definition using these tables  
✅ A **Redshift ML CREATE MODEL** statement using this data

Just let me know which one you’d like next!