There are **three fundamentally different approaches** when trying to get metadata from MongoDB, Salesforce Data Cloud, and Snowflake into AWS Glue Catalog.

## First, Understanding What Glue Catalog Actually Stores

AWS Glue Catalog stores:

```text
Database
 ├─ Tables
 ├─ Columns
 ├─ Data Types
 ├─ Partitions
 ├─ Properties
 └─ Metadata
```

It is **not a data repository**.

It contains:

* Table definitions
* Schema metadata
* Locations of data
* Classifications
* Technical metadata

AWS Glue Data Catalog is a central metadata repository used by Athena, Glue ETL, EMR, Redshift Spectrum, and other AWS analytics services. [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/create-maintain-business-glossary.html), [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/datazone-concepts.html)

***

# Option 1 (Recommended): Land Data into AWS First

This is the most common enterprise pattern.

## Snowflake

```text
Snowflake
    ↓
S3
    ↓
Glue Crawler
    ↓
Glue Catalog
```

Methods:

* Snowflake UNLOAD to S3
* Snowflake Iceberg Tables
* Snowflake Data Sharing → S3
* ETL/ELT pipelines

Once files exist in S3:

```text
s3://customer-data/
    customer.parquet
```

Glue Crawler creates:

```text
Database: customer
Table: customer_profile
Columns:
  customer_id
  first_name
  last_name
```

Then:

* Athena can query it
* DataZone can catalog it
* Lake Formation can govern it

***

## MongoDB Atlas

```text
MongoDB Atlas
      ↓
AWS Glue Connector
      ↓
S3
      ↓
Glue Catalog
```

or

```text
MongoDB Atlas
      ↓
Kafka/Debezium
      ↓
S3
      ↓
Glue Catalog
```

Typical enterprise architectures:

```text
MongoDB CDC
      ↓
Kafka
      ↓
S3 Data Lake
      ↓
Glue Catalog
```

Now business users can discover data through AWS-native tools.

***

## Salesforce Data Cloud

```text
Salesforce Data Cloud
        ↓
AppFlow
        ↓
S3
        ↓
Glue Catalog
```

or

```text
Salesforce Data Cloud
        ↓
Data Share
        ↓
Snowflake
        ↓
S3
        ↓
Glue Catalog
```

AWS specifically supports AppFlow integrations for Salesforce-type sources which can land data in S3 and subsequently be cataloged and governed. DataZone documentation also references AWS Glue connectors and AppFlow as mechanisms for accessing data across AWS, third-party services, and on-premises systems. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/integrations/)

***

# Option 2: Use Glue Connectors Without Persisting Data in S3

For some use cases, Glue can connect directly to external systems.

## MongoDB

Glue supports MongoDB connectors.

```text
Glue Job
      ↓
MongoDB Atlas
```

Metadata can be inferred during ETL execution.

However:

* Not all collections automatically appear in Glue Catalog
* Discovery experience is weaker
* Athena cannot query directly

Usually this is used for ETL rather than cataloging.

***

## Snowflake

Glue can connect directly to Snowflake.

```text
Glue Job
     ↓
Snowflake
```

But again:

* Primarily ETL integration
* Metadata visibility is not equivalent to a fully cataloged AWS lake dataset

Most organizations still land data into S3.

***

## Salesforce

Similar concept:

```text
Glue
   ↓
Salesforce
```

Works forThis is actually a very important architectural consideration.

The key thing to understand is that **AWS Glue Catalog does not natively catalog every external platform in the same way that it catalogs S3, Redshift, or Iceberg tables**. For MongoDB, Salesforce Data Cloud, and Snowflake, there are several patterns with different levels of metadata richness.

# Option 1 – Catalog Through AWS Glue Connectors (Recommended AWS Approach)

## MongoDB Atlas

Glue supports connecting to MongoDB as a data source for ETL and metadata discovery.

Typical flow:

```text
MongoDB Atlas
      │
      ▼
AWS Glue Connection
      │
      ▼
Glue Crawler
      │
      ▼
Glue Catalog Tables
```

Result:

* Collections become tables.
* Fields become columns.
* Schema metadata is stored in Glue Catalog.
* Athena can query if data is materialized into S3.
* DataZone can discover the cataloged assets. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/integrations/), [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/datazone-concepts.html)

### Limitation

We only get technical schema metadata.

We do NOT automatically get:

* Business glossary
* Collection descriptions
* Data owner information
* Atlas lineage
* Atlas governance classifications

which is where Alation or a similar catalog provides much richer metadata.

***

## Salesforce Data Cloud

There are several possibilities:

### Pattern A – AppFlow + Glue

```text
Salesforce Data Cloud
        │
        ▼
Amazon AppFlow
        │
        ▼
S3
        │
        ▼
Glue Crawler
        │
        ▼
Glue Catalog
```

This is one of the most common AWS-native patterns.

Benefits:

* Glue Catalog contains metadata.
* Data becomes queryable through Athena.
* DataZone can catalog the Glue assets. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/integrations/), [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/)

### Pattern B – Glue Connector

Where supported:

```text
Salesforce
      │
      ▼
Glue Connection
      │
      ▼
Glue Catalog
```

This gives technical metadata discovery without necessarily landing data first.

### Limitation

What gets cataloged is often the ingested representation of Salesforce objects rather than the complete business semantic model found inside Salesforce Data Cloud.

***

## Snowflake

Snowflake is actually the easiest of the three.

Typical pattern:

```text
Snowflake
      │
      ▼
AWS Glue Connector
      │
      ▼
Glue Catalog
```

or

```text
Snowflake
      │
      ▼
Snowflake Data Export
      │
      ▼
S3
      │
      ▼
Glue Crawler
      │
      ▼
Glue Catalog
```

Result:

* Databases
* Schemas
* Tables
* Column metadata

can appear in Glue Catalog. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/integrations/), [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/datazone-concepts.html)

Again, this is primarily technical metadata.

***

# Option 2 – Use Glue as the Technical Catalog and Alation as the Business Catalog

This is the architecture I would recommend.

```text
MongoDB Atlas
Salesforce Data Cloud
Snowflake
AWS S3
AWS Redshift
        │
        ▼

AWS Glue Catalog
(Technical Metadata)

        │
        ▼

Alation
(Business Metadata)
```

Glue Catalog stores:

```text
table
schema
column
partition
location
```

Alation enriches with:

```text
business descriptions
owners
stewards
lineage
trust scores
usage statistics
data products
glossary mappings
```

Alation has connectors for AWS Glue, Snowflake, MongoDB Atlas, MongoDB, and Salesforce, allowing it to harvest metadata directly instead of relying solely on Glue as an intermediary. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)

***

# Option 3 – Register Assets in DataZone Without Glue

Many architects miss this possibility.

DataZone supports:

```text
Custom Asset Types
+
APIs
```

for non-native assets. AWS states that built-in producer sources are Glue Catalog and Redshift, while other asset types can be published via custom asset types and public APIs. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/integrations/)

Example:

```text
Snowflake Customer Table
```

can be represented in DataZone as:

```text
Business Name:
Customer 360

Physical Location:
Snowflake

Owner:
Marketing

Steward:
Data Governance Team
```

without being stored in Glue Catalog.

Similarly:

```text
MongoDB Collection
Salesforce Data Cloud Data Set
```

can be registered as DataZone business catalog assets through custom integrations. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/integrations/), [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/datazone-concepts.html)

***

# Comparing the Approaches

| Approach                 | MongoDB   | Salesforce Data Cloud | Snowflake | Recommendation     |
| ------------------------ | --------- | --------------------- | --------- | ------------------ |
| Glue Connector / Crawler | Good      | Good                  | Good      | Technical metadata |
| Land Data in S3 + Crawl  | Good      | Excellent             | Good      | Data lake centric  |
| DataZone Custom Assets   | Moderate  | Moderate              | Moderate  | Discovery only     |
| Alation Direct Connector | Excellent | Excellent             | Excellent | Enterprise catalog |
| Glue + Alation           | Excellent | Excellent             | Excellent | Best overall       |

***

# Recommendation for Our Environment

Considering our landscape:

```text
AWS
Snowflake
MongoDB Atlas
Salesforce Data Cloud
Future Azure Possibility
```

I would not try to force everything through Glue Catalog.

Instead:

```text
AWS Assets
    ↓
Glue Catalog

Snowflake
MongoDB Atlas
Salesforce Data Cloud
    ↓
Direct Metadata Harvesting

            ↓
         Alation

            ↓
Business Users
```

In other words:

* Use **Glue Catalog for AWS-native assets**.
* Let **Alation harvest metadata directly from Snowflake, MongoDB Atlas, and Salesforce Data Cloud**.
* If DataZone is retained, use it primarily for AWS data products and AWS-native governed access.

That gives us richer metadata, better lineage, less duplication, and avoids turning Glue Catalog into a metadata aggregation platform for systems it was not primarily designed to govern.
