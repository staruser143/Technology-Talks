# Connector Management 
Connectors are the foundation for:

```text
Metadata Harvesting
Lineage Collection
Usage Analytics
Data Profiling
Governance Automation
Marketplace Publishing
```

```text
Without Connectors
      ↓
Alation = Empty Catalog

With Connectors
      ↓
Alation = Enterprise Data Marketplace
```

***

# What is Connector Management?

Connector Management is the process of managing the lifecycle of integrations between Alation and source systems.

```text
Source Systems
├─ AWS Glue Catalog
├─ Snowflake
├─ MongoDB Atlas
├─ Salesforce Data Cloud
├─ Power BI
├─ Tableau
├─ Databricks
└─ Azure Synapse

        ↓

   Alation Connectors

        ↓

   Metadata Repository
```

The responsibilities include:

* Connector onboarding
* Authentication management
* Metadata harvesting schedules
* Lineage configuration
* Monitoring
* Troubleshooting
* Version upgrades
* Security governance

***

# Connector Lifecycle

## 1. Connector Onboarding

The first step is connecting a source.

Example:

```text
Snowflake
    ↓
Alation Snowflake Connector
```

During onboarding:

### Configure Connectivity

```text
Hostname
Account Name
Warehouse
Database
Role
Network Access
```

### Create Service Account

Example:

```text
ALATION_METADATA_USER
```

Principle:

```text
Least Privilege
```

Never use:

```text
ACCOUNTADMIN
SYSADMIN
ROOT
```

for harvesting.

***

# 2. Metadata Extraction Configuration

Decide what metadata should be harvested.

## Example: Snowflake

Harvest:

```text
Databases
Schemas
Tables
Views
Columns
Tags
Policies
```

Optional:

```text
Query Logs
Usage Statistics
Profiling
```

***

## Example: AWS Glue

Harvest:

```text
Databases
Tables
Columns
Partitions
Iceberg Metadata
```

Avoid harvesting unnecessary assets.

Otherwise we'll create:

```text
Metadata Noise
```

that business users won't use.

***

# 3. Scheduling Metadata Harvests

Metadata changes continuously.

### Typical Scheduling

| Source        | Frequency     |
| ------------- | ------------- |
| AWS Glue      | Every 6 Hours |
| Snowflake     | Every 6 Hours |
| Salesforce    | Daily         |
| MongoDB Atlas | Daily         |
| Power BI      | Daily         |
| Tableau       | Daily         |

***

### Why This Matters

Without scheduling:

```text
Source System
      ≠
Alation
```

Example:

```text
Snowflake Column Added
```

Business users won't see it until the next refresh.

***

# 4. Query Log Ingestion

Metadata alone tells us:

```text
What Exists
```

Query logs tell us:

```text
What Is Used
```

Example:

```text
Customer360
```

Query history shows:

```text
24,000 queries/month
```

Now Alation can identify:

```text
Popular Data Asset
```

This helps build:

* Trust scores
* Popularity metrics
* Top consumers

***

# 5. Data Profiling Management

Connectors can collect profiling information.

Example:

```text
Customer Table
```

Profile results:

```text
Rows: 12M

Null Emails: 2%

Distinct Customers: 11.8M
```

This enrichment helps business users assess data quality.

***

# 6. Lineage Collection Management

Probably the most complex area.

Lineage can come from:

```text
Snowflake SQL

dbt

AWS Glue Jobs

Airflow

Databricks

Power BI

Tableau
```

Connector management includes verifying:

```text
Lineage Coverage

Lineage Accuracy

Refresh Frequency
```

***

# 7. Monitoring Connector Health

A mature Alation implementation should have connector KPIs.

## Track

```text
Connector Status

Successful Runs

Failed Runs

Metadata Coverage

Lineage Coverage

Harvest Duration
```

***

## Example Dashboard

```text
Snowflake Connector

Status:
Healthy

Last Harvest:
2026-07-15 09:00

Objects Harvested:
12,543

Error Count:
0
```

***

# Connector Governance Model

For the environment, I would establish clear ownership.

| Connector             | Owner                     |
| --------------------- | ------------------------- |
| AWS Glue              | Data Lake Team            |
| Snowflake             | Data Warehouse Team       |
| MongoDB Atlas         | Application Platform Team |
| Salesforce Data Cloud | CRM Platform Team         |
| Power BI              | Reporting Team            |
| Tableau               | Analytics Team            |
| Alation Platform      | Metadata Governance Team  |

***

# Metadata Harvesting Categories

Connector management should classify assets into three categories.

## Tier 1

Critical business assets.

Example:

```text
Customer360
Claims360
Provider360
```

Harvest:

```text
Every 4 Hours
```

***

## Tier 2

Operational reporting datasets.

Harvest:

```text
Daily
```

***

## Tier 3

Legacy or infrequently used assets.

Harvest:

```text
Weekly
```

***

# Common Challenges

## Challenge 1: Too Much Metadata

Organizations often harvest:

```text
Everything
```

Result:

```text
50,000 tables
```

Business users become overwhelmed.

### Recommendation

Onboard domains incrementally:

```text
Customer Domain
Claims Domain
Provider Domain
```

first.

***

## Challenge 2: Duplicate Assets

Example:

```text
Snowflake.Customer

AWS.Customer

MongoDB.Customer
```

Business users see three assets.

### Recommendation

Use:

```text
Business Glossary

Data Products

Certification
```

to create a single business-facing view.

***

## Challenge 3: Stale Metadata

Example:

```text
Table Deleted
```

but Alation still shows it.

### Recommendation

Implement lifecycle rules:

```text
Inactive 90 Days
      ↓
Review
      ↓
Archive
```

***

# Recommended Connector Operating Model

For the target architecture:

```text
AWS Glue
Snowflake
MongoDB Atlas
Salesforce Data Cloud
Azure
Power BI
Tableau

        ↓

Connector Layer

        ↓

Metadata Governance Team

        ↓

Alation Enterprise Marketplace
```

### Metadata Governance Team Responsibilities

* Connector onboarding
* Credential management
* Scheduling harvests
* Monitoring failures
* Lineage validation
* Metadata quality checks
* Steward coordination
* Connector upgrades

***

# Recommended KPIs

Track these monthly:

```text
Connector Success Rate > 99%

Metadata Freshness < 24 hrs

Lineage Coverage > 80%

Certified Assets > 70%

Harvest Failures < 1%

Broken Lineage < 5%
```

# For the AWS + Snowflake + MongoDB Atlas + Salesforce Data Cloud Landscape

We need to  create a dedicated **Metadata & Catalog Operations Team** responsible for:

```text
1. Connector Management
2. Metadata Harvesting
3. Metadata Enrichment
4. Lineage Collection
5. Stewardship Coordination
6. Marketplace Operations
```

This team becomes the operational backbone of Alation, ensuring that metadata flowing from AWS Glue Catalog, Snowflake, MongoDB Atlas, Salesforce Data Cloud, and future Azure platforms remains accurate, fresh, and trustworthy for business users.
