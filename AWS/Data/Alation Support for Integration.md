for **Snowflake** and **MongoDB Atlas**, the answer is clearly **yes** based on Alation's published connector catalog.

For **Salesforce Data Cloud**, the answer is **"verify specifically during product evaluation"** because Alation publicly lists a **Salesforce connector**, but the connector documentation does not explicitly distinguish between traditional Salesforce CRM objects and Salesforce Data Cloud assets in the connector list. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)

## What Alation Explicitly Supports

Alation's connector catalog includes:

* Snowflake on AWS
* Snowflake on Azure
* Snowflake on GCP
* MongoDB
* MongoDB Atlas on AWS
* Salesforce
* AWS Glue
* AWS S3
* AWS Redshift
* Azure Synapse
* Databricks
* Power BI
* Tableau

and many other platforms. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)

***

## Snowflake

### Direct Metadata Harvesting

```text
Snowflake
     ↓
Alation Connector
     ↓
Alation Catalog
```

Alation can harvest:

* Databases
* Schemas
* Tables
* Views
* Columns
* Query history
* Usage patterns
* Lineage information
* Profiling information

through its Snowflake connectors. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)

### Recommendation

For Snowflake:

```text
Do NOT route metadata through Glue Catalog first.
```

Instead:

```text
Snowflake → Alation
```

is the cleaner architecture.

***

## MongoDB Atlas

### Direct Metadata Harvesting

```text
MongoDB Atlas
        ↓
Alation MongoDB Atlas Connector
        ↓
Alation Catalog
```

Alation explicitly lists:

```text
MongoDB
MongoDB Atlas on AWS
```

among its supported connectors. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)

### Metadata Typically Captured

Examples include:

* Databases
* Collections
* Field structures
* Metadata
* Usage information
* Lineage (where available through integrations)

depending on connector configuration and available permissions. [\[alation.com\]](https://www.alation.com/product/connectors/)

### Recommendation

For MongoDB Atlas:

```text
MongoDB Atlas → Alation
```

rather than:

```text
MongoDB Atlas → Glue Catalog → Alation
```

unless we are specifically building an AWS lakehouse copy of the data.

***

## Salesforce Data Cloud

This requires a bit more nuance.

### What is Explicitly Published

Alation explicitly lists:

```text
Salesforce
```

as a supported connector. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)

### What Needs Validation

Salesforce now has multiple products:

```text
Sales Cloud
Service Cloud
Marketing Cloud
Data Cloud
CRM Objects
Custom Objects
```

The public connector catalog does not explicitly state:

```text
Salesforce Data Cloud
```

in the connector name. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/)

Therefore, during vendor evaluation, we should specifically ask:

> Does the Salesforce connector harvest metadata from Salesforce Data Cloud data model objects, calculated insights, data streams, data lake objects, and Customer 360 assets?

### My Expectation

Given Alation's focus on enterprise cataloging and Salesforce support, I would expect some level of Data Cloud integration capability through either:

* Native connector support
* API-based harvesting
* Custom connector framework

but I would validate this with Alation before making it a selection criterion. [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)

***

## Recommended Architecture for our Scenario

Instead of:

```text
Snowflake
MongoDB Atlas
Salesforce Data Cloud
        ↓
Glue Catalog
        ↓
Alation
```

I would recommend:

```text
AWS Assets
   ↓
Glue Catalog
   ↓
Alation

Snowflake
   ↓
Alation

MongoDB Atlas
   ↓
Alation

Salesforce Data Cloud
   ↓
Alation
```

This gives:

* Less metadata duplication
* More complete metadata
* Better lineage
* Better stewardship information
* Better multi-cloud governance

while keeping Glue Catalog focused on AWS-native assets.

## Bottom-Line Recommendation

| Source                | Direct Alation Harvesting            | Confidence                                                                                                                                                                          |
| --------------------- | ------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Snowflake             | ✅ Yes                                | High [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)   |
| MongoDB Atlas         | ✅ Yes                                | High [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)   |
| Salesforce CRM        | ✅ Yes                                | High [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/)   |
| Salesforce Data Cloud | ⚠️ Likely, but verify exact coverage | Medium [\[alation.com\]](https://www.alation.com/product/connectors/all-connectors/), [\[alation.com\]](https://www.alation.com/product/connectors/) |

For our landscape (**AWS + Snowflake + MongoDB Atlas + Salesforce Data Cloud**), my preferred architecture would be:

```text
Glue Catalog = AWS metadata

Alation = Enterprise catalog

Snowflake → Alation
MongoDB Atlas → Alation
Salesforce Data Cloud → Alation
AWS Glue Catalog → Alation
```

with Alation serving as the single business discovery and governance experience for business users.
