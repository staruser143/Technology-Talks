# Metadata Harvesting with Altion Cloud
- When using **Alation Cloud**, metadata harvesting is one of the first implementation workstreams. 
- We yypically do **not** harvest everything through AWS Glue Catalog. 
- Instead, Alation harvests metadata directly from each source system and builds a unified enterprise catalog.

## Recommended Metadata Harvesting Architecture

```text
AWS Glue Catalog ───────┐
Snowflake ──────────────┤
MongoDB Atlas ─────────┤
Salesforce Data Cloud ─┤
Azure Synapse ─────────┤
Power BI ──────────────┤
Tableau ───────────────┤
Databricks ────────────┘

           ↓

      Alation Cloud

           ↓

Enterprise Catalog
Business Glossary
Data Marketplace
Lineage
Stewardship
```

This is generally preferable to:

```text
Everything
    ↓
Glue Catalog
    ↓
Alation
```

because we preserve richer source metadata and reduce duplication.

***

# What Metadata Gets Harvested?

Most Alation connectors can harvest:

### Technical Metadata

```text
Database
Schema
Table
View
Column
Data Type
Relationships
Partitions
```

### Operational Metadata

```text
Query History
Usage Statistics
Popular Assets
Top Consumers
```

### Governance Metadata

```text
Owners
Stewards
Classifications
Tags
Certifications
Policies
```

### Lineage Metadata

```text
Source Dataset
      ↓
Transformation
      ↓
Target Dataset
      ↓
Report
```

Alation connector capabilities vary by source system, but metadata extraction, lineage, profiling, and query log processing are among the core capabilities of its connector framework.

***

# Harvesting AWS Assets

## Option 1 (Recommended)

Harvest from AWS Glue Catalog.

```text
S3
  ↓
Glue Catalog
  ↓
Alation
```

### Why?

Glue already contains:

* Databases
* Tables
* Schemas
* Partitions
* Iceberg metadata

No need to crawl S3 again.

For AWS we would typically configure:

```text
AWS Glue Connector
```

in Alation.

Harvest frequency:

```text
Daily
or
Every 6 Hours
```

is usually sufficient.

***

# Harvesting Snowflake Metadata

### Typical Flow

```text
Snowflake
    ↓
Snowflake Connector
    ↓
Alation
```

Harvest:

```text
Databases
Schemas
Tables
Views
Columns
Procedures
Tags
Query History
Usage Patterns
```

### Service Account

Create:

```text
ALATION_METADATA_USER
```

with:

```sql
USAGE
MONITOR
SELECT Metadata
```

privileges.

We should not use admin credentials.

***

# Harvesting MongoDB Atlas Metadata

### Typical Flow

```text
MongoDB Atlas
       ↓
Alation MongoDB Atlas Connector
       ↓
Alation
```

Harvest:

```text
Databases
Collections
Fields
Indexes
Metadata
```

Typical setup:

```text
Read-only service account
```

Example:

```text
alation_metadata_reader
```

Grant:

```text
readAnyDatabase
clusterMonitor
```

depending on metadata requirements.

***

# Harvesting Salesforce Data Cloud Metadata

This is typically the most complex source.

### Possible Metadata Sources

```text
Salesforce CRM

Salesforce Data Cloud

Custom Objects

Calculated Insights

Data Streams

Data Lake Objects
```

A connector or API-based approach generally harvests:

```text
Objects
Fields
Relationships
Descriptions
Ownership
```

For Data Cloud specifically, verify during Alation implementation exactly which asset types are supported in your version and connector package.

### Recommended Approach

```text
Salesforce
        ↓
Metadata APIs
        ↓
Alation
```

rather than creating duplicate metadata repositories.

***

# Metadata Harvesting Schedule

Not all systems need the same refresh frequency.

## Tier 1 Systems

```text
Snowflake
AWS Glue
Salesforce
```

Refresh:

```text
Every 4-6 Hours
```

***

## Tier 2 Systems

```text
MongoDB Atlas
Power BI
Tableau
```

Refresh:

```text
Daily
```

***

## Tier 3 Systems

```text
Legacy Applications
Static Sources
```

Refresh:

```text
Weekly
```

***

# What Happens After Harvesting?

Harvesting only gives technical metadata.

Example:

```text
Table:
customer_profile

Columns:
customer_id
email
phone
dob
```

Business users still do not understand the dataset.

The next step is **metadata enrichment**.

***

# Metadata Enrichment Workflow

```text
Harvest Metadata
       ↓
Assign Owner
       ↓
Assign Steward
       ↓
Add Business Description
       ↓
Map Glossary Terms
       ↓
Classify Sensitivity
       ↓
Certify Asset
       ↓
Publish to Marketplace
```

Example:

```text
Technical Name:
cust_prof_tbl

Business Name:
Customer Profile

Owner:
Customer Domain Team

Steward:
Data Governance Team

Classification:
Confidential

Glossary:
Customer
Member
Subscriber
```

This is where Alation delivers the majority of its business value.

***

# Recommended Source of Truth Model

We should not also allow metadata updates in multiple places.

I recommend:

| Metadata Type              | System of Record |
| -------------------------- | ---------------- |
| AWS Table Schema           | AWS Glue Catalog |
| Snowflake Schema           | Snowflake        |
| MongoDB Structure          | MongoDB Atlas    |
| Salesforce Objects         | Salesforce       |
| Business Definitions       | Alation          |
| Glossary                   | Alation          |
| Data Product Certification | Alation          |
| Stewardship                | Alation          |
| Security Permissions       | Source Platform  |

***

# Recommended Architecture for our Environment

Given our landscape:

```text
AWS
Snowflake
MongoDB Atlas
Salesforce Data Cloud
Future Azure
```

We would implement harvesting as:

```text
AWS Glue Catalog ────┐
Snowflake ───────────┤
MongoDB Atlas ──────┤
Salesforce Data Cloud┤
Power BI ───────────┤
Tableau ────────────┘

            ↓

        Alation

            ↓

 Business Marketplace
 Business Glossary
 Data Products
 Stewardship
 Lineage
 Access Requests
```

This avoids forcing everything through Glue Catalog, gives richer metadata, and supports your goal of using **Alation as the enterprise data marketplace across AWS and non-AWS platforms**.
