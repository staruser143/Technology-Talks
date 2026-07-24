# Lineage validation 
- The process of verifying that the lineage shown in Alation (or any catalog) accurately reflects the **actual movement and transformation of data** across systems.

- In practice, lineage is often one of the least accurate parts of a catalog unless there is a formal validation process.

For the environment:

```text
Salesforce Data Cloud
          ↓
Snowflake
          ↓
AWS S3
          ↓
Glue Catalog
          ↓
Athena
          ↓
Power BI
```

the objective is to verify that every lineage relationship shown in Alation is correct, complete, and up to date.

***

# Why Lineage Validation is Needed

Lineage can be incorrect because of:

```text
Schema changes
ETL code changes
Missing connectors
Manual transformations
Untracked SQL
Legacy integrations
```

Example:

Alation may show:

```text
Salesforce
      ↓
Customer360
```

But the actual process may be:

```text
Salesforce
      ↓
Snowflake
      ↓
dbt
      ↓
Customer360
```

Without validation, impact analysis becomes unreliable.

***

# What Should Be Validated?

## 1. Source Validation

Verify that all source systems are captured.

Example:

```text
Customer360
```

Actual sources:

```text
Salesforce Data Cloud
MongoDB Atlas
Snowflake CRM
```

Questions:

* Are all source systems represented?
* Are any sources missing?
* Are obsolete sources still shown?

***

## 2. Table-Level Validation

Example:

```text
snowflake.customer_master
      ↓
athena.customer360
```

Validate:

* Source table name
* Target table name
* Mapping correctness

***

## 3. Column-Level Validation

This is the most important and most difficult.

Example:

```text
customer_id
      ↓
member_id
```

Verify:

```text
customer_id
     =
member_id
```

and not:

```text
customer_number
```

Column-level lineage is critical for regulatory and impact-analysis use cases.

***

## 4. Transformation Validation

Example:

```sql
full_name =
first_name || ' ' || last_name
```

Validate that lineage captures:

```text
first_name
last_name
      ↓
full_name
```

rather than showing:

```text
Customer Source
    ↓
Customer Target
```

with no transformation detail.

***

# Lineage Validation Framework

## Step 1: Connector Validation

Ensure metadata harvesting is working correctly.

Check:

```text
Snowflake Connector
MongoDB Connector
Salesforce Connector
Glue Connector
```

For each source, validate:

```text
Objects discovered
Objects expected
Coverage %
```

Example:

```text
Expected Tables: 1200

Discovered Tables: 1190

Coverage: 99.1%
```

***

## Step 2: Critical Data Product Validation

Do not validate the entire enterprise initially.

Start with:

```text
Customer 360
Claims Analytics
Provider 360
Broker Analytics
Enrollment Analytics
```

Focus on business-critical datasets.

***

## Step 3: Technical Validation

Compare lineage with actual implementation.

Review:

```text
Glue Jobs
Snowflake SQL
dbt Models
Airflow DAGs
Informatica Mappings
Fivetran Pipelines
```

Example:

```text
Alation says:

A → B → C

Actual ETL:

A → X → B → C
```

Gap identified.

***

## Step 4: Steward Validation

Technical lineage alone is insufficient.

Data Steward reviews:

```text
Source systems
Business meaning
Transformation logic
```

Questions:

```text
Does this lineage make business sense?

Are all source systems represented?

Can business users trust this?
```

***

## Step 5: Certification

After validation:

```text
Lineage Status:

Certified
```

Store:

```text
Validator
Validation Date
Review Date
```

Example:

```text
Customer360

Lineage Status:
Certified

Validated By:
Customer Data Steward

Last Reviewed:
2026-07-15
```

***

# Validation Levels

## Level 1 - Automated

System validates:

```text
Object existence
Schema changes
Broken relationships
Missing objects
```

Example:

```text
Source Table Deleted
```

Generate alert.

***

## Level 2 - Technical Review

Performed by:

```text
Data Engineers
Platform Teams
```

Verify:

```text
ETL logic
SQL logic
Pipelines
```

***

## Level 3 - Business Review

Performed by:

```text
Data Steward
Domain SME
```

Verify:

```text
Business correctness
Glossary alignment
Data product relationships
```

***

# Validation Checklist

For every Tier-1 asset:

### Source Validation

```text
✓ Source systems correct
✓ Source tables correct
✓ Source columns correct
```

### Transformation Validation

```text
✓ SQL transformation verified
✓ ETL mapping verified
✓ Aggregations verified
```

### Target Validation

```text
✓ Target dataset exists
✓ Target columns exist
✓ Business names correct
```

### Governance Validation

```text
✓ Owner assigned
✓ Steward assigned
✓ Classification assigned
✓ Description present
```

### Lineage Visualization Validation

```text
✓ Diagram accurate
✓ Impact analysis works
✓ Upstream dependencies complete
✓ Downstream dependencies complete
```

***

# Automation Opportunities

For a large enterprise, lineage validation should not rely entirely on manual reviews.

Automate:

## Schema Drift Detection

Example:

```text
Column Added
Column Removed
Datatype Changed
```

Trigger:

```text
Lineage Revalidation Required
```

***

## CI/CD Lineage Validation

During deployment:

```text
dbt Model
Glue Job
Snowflake Procedure
```

Run validation checks.

Fail deployment if:

```text
Lineage broken
```

***

## Data Quality + Lineage Validation

Example:

```text
Lineage Change
      +
Quality Score Drop
```

Potential issue detected.

Alert:

```text
Steward
Data Product Owner
```

***

# Recommended Operating Model

For the AWS + Snowflake + MongoDB Atlas + Salesforce Data Cloud landscape:

| Role               | Responsibility                |
| ------------------ | ----------------------------- |
| Platform Team      | Harvest lineage metadata      |
| Data Engineer      | Validate transformation logic |
| Data Steward       | Validate business correctness |
| Data Product Owner | Approve lineage certification |
| Governance Team    | Audit lineage quality         |

***

# KPI Dashboard for Lineage Validation

Track:

```text
% Assets with lineage

% Certified lineage

Lineage coverage by platform

Lineage validation SLA

Open lineage defects

Broken lineage links

Lineage freshness
```

Example target:

```text
Tier-1 Assets:
100% certified lineage

Tier-2 Assets:
80% lineage coverage

Enterprise:
90% lineage coverage
```

***

# Recommendation for the Alation Implementation

Initially validate lineage only for high-value data products:

```text
Customer 360
Provider 360
Broker 360
Claims Analytics
Enrollment Analytics
```

Implement a **quarterly lineage certification process**:

```text
Harvest Lineage
      ↓
Technical Review
      ↓
Steward Review
      ↓
Certification
      ↓
Publish in Alation Marketplace
```

This usually delivers far more business value than attempting to validate lineage across every dataset in the enterprise from day one.
