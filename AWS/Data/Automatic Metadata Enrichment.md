# Automatic Metadata Enrichment

For Harvesting technical metadata we need to automatically add:

* Business descriptions
* Business glossary mappings
* Data owners
* Stewards
* Sensitivity classifications
* Data quality indicators
* Certifications
* Data products
* Lineage context
* Usage guidance

For an enterprise like ours with **AWS + Snowflake + MongoDB Atlas + Salesforce Data Cloud**, the recommendation is a layered enrichment strategy.

***

# Metadata Enrichment Maturity Model

## Level 1: Technical Harvesting

Automatically harvested:

```text
Database
Schema
Table
Column
Data Type
View
Relationships
```

Example:

```text
customer_profile
 ├─ customer_id
 ├─ email
 ├─ phone
 └─ dob
```

Business users still don't know:

* What this dataset means
* Whether it is trusted
* Whether it contains PII
* Who owns it

***

## Level 2: Rule-Based Auto-Enrichment

This is the quickest win.

### Example: PII Detection

Rules:

```text
email        → PII
phone        → PII
ssn          → Restricted
dob          → Sensitive
address      → Sensitive
```

Automatically assign:

```text
Classification = Confidential
```

when matching patterns are found.

### Example

```text
customer_profile

Columns:
email
phone
dob
```

Automatically becomes:

```text
PII = YES
Classification = Confidential
```

***

# Automating Business Glossary Mapping

Create a glossary such as:

```text
Customer

Synonyms:
customer_id
member_id
subscriber_id
consumer_id
```

When metadata is harvested:

```text
customer_id
```

Automatically associate:

```text
Business Term = Customer
```

***

## Example

### Snowflake

```text
cust_id
```

### MongoDB

```text
memberId
```

### Salesforce

```text
subscriber_id
```

All become:

```text
Customer
```

in Alation.

This significantly improves business-user search.

***

# Automated Ownership Assignment

Instead of having organizations manually assign owners, We need to  build ownership rules.

Example:

```text
Database Prefix = claims_
      ↓
Claims Data Domain

Owner
=
Claims Director

Steward
=
Claims Data Steward
```

***

## Example

```text
snowflake.claims.member_claims
```

Automatically receives:

```text
Owner = Claims Domain
Steward = Claims Steward Team
```

***

# Domain-Based Metadata Enrichment

Define a domain registry:

```text
Customer
Claims
Provider
Broker
Enrollment
Sales
Finance
```

Rules:

```text
customer*
member*
subscriber*
```

→ Customer Domain

```text
claim*
adjudication*
payment*
```

→ Claims Domain

When assets arrive, they are auto-tagged.

***

# Auto-Generating Business Descriptions using AI

Example harvested metadata:

```text
customer_profile

Columns:
customer_id
email
phone
dob
loyalty_tier
```

An LLM can generate:

```text
Contains customer demographic and
contact information including loyalty
tier and communication preferences.
```

instead of forcing stewards to write descriptions manually.

***

# Data Classification Automation

For regulated domains this is critical.

Example policies:

## PII

```text
email
phone
mobile
dob
address
```

## PHI

```text
diagnosis
procedure
member_health
```

## PCI

```text
card_number
cvv
```

Automatically tag assets:

```text
Public

Internal

Confidential

Restricted
```

based on detected attributes.

***

# Data Quality Enrichment

Integrate with quality platforms.

Examples:

```text
Great Expectations
Soda
Monte Carlo
Collibra DQ
AWS Glue DQ
```

Publish:

```text
Completeness: 97%

Freshness: 2 hours

Quality Score: 92/100
```

inside Alation.

Business users can immediately determine whether a dataset is trustworthy.

***

# Usage-Based Enrichment

One of the most valuable automations.

Use:

```text
Snowflake Query Logs
Athena Query Logs
Power BI Usage
Tableau Usage
```

to identify:

```text
Most Used Assets

Least Used Assets

Certified Assets

Power Users
```

Example:

```text
Customer360
Queries/month: 24,000
Reports using dataset: 31
```

Automatically mark:

```text
Popular Asset
```

***

# Lineage Enrichment

Harvest lineage from:

```text
dbt
Airflow
Glue Jobs
Snowflake
Databricks
Fivetran
Informatica
```

Generate:

```text
Salesforce Data Cloud
         ↓
Snowflake
         ↓
AWS S3
         ↓
Athena
         ↓
Power BI
```

Business users can immediately understand impact analysis.

***

# Certification Automation

Instead of manually certifying datasets,we  Create certification rules.

Example:

```text
Quality Score > 90

AND

Owner Assigned

AND

Steward Assigned

AND

Description Present

AND

PII Classification Present
```

Then:

```text
Certified Asset = TRUE
```

automatically.

***

# Data Product Automation

For our environment, we would introduce a Data Product Framework.

Example:

```text
Customer 360

Sources
--------
Salesforce Data Cloud
Snowflake
MongoDB Atlas

Owner
--------
Customer Domain

Consumers
--------
Marketing
Sales
Analytics
```

Data products can be generated automatically from metadata patterns.

***

# Event-Driven Metadata Enrichment

Instead of waiting for nightly jobs:

```text
Snowflake Schema Change
        ↓
Event
        ↓
Metadata Harvest
        ↓
Enrichment Rules
        ↓
Alation Update
```

Similar patterns can be implemented for:

```text
MongoDB Atlas

Salesforce

AWS Glue Catalog

Databricks
```

***

# Recommended Architecture for the Environment

```text
                    Sources
--------------------------------------------------
AWS Glue Catalog
Snowflake
MongoDB Atlas
Salesforce Data Cloud
--------------------------------------------------

                    ↓

            Metadata Harvest

                    ↓

        Automated Enrichment Layer

        ├─ AI Description Generation
        ├─ Glossary Mapping
        ├─ PII Classification
        ├─ Domain Assignment
        ├─ Owner Assignment
        ├─ Steward Assignment
        ├─ Quality Score Assignment
        ├─ Certification Rules
        └─ Data Product Assignment

                    ↓

                 Alation

                    ↓

         Enterprise Marketplace
```

# Recommendation

Implement enrichment in **four layers**:

1. **Rule-based enrichment** (owners, glossary, classifications)
2. **Metadata-driven enrichment** (domains, tags, certifications)
3. **Operational enrichment** (usage, quality, lineage)
4. **GenAI enrichment** (descriptions, summaries, business context)

A realistic target should be:

```text
80-90% automated enrichment
10-20% human stewardship
```

This scales far better than expecting data stewards to manually curate thousands of assets across AWS, Snowflake, MongoDB Atlas, and Salesforce Data Cloud.
