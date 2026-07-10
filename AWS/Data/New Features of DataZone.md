## Key Recent Enhancements

### 1. Domain Units and Authorization Policies  -Released August 2024.

This is one of the most important enhancements for enterprises adopting a **data mesh** approach.

**What it adds**

* Business-unit/team hierarchy (Domain Units)
* Delegated governance
* Fine-grained authorization policies
* Control over project creation, glossary management, and compute usage

**Why it matters**
Instead of managing a single centralized DataZone domain,we can organize assets and projects by business area such as:

```text
Enterprise
├── Sales
├── Marketing
├── Finance
└── Healthcare Claims
```

This is especially useful for large healthcare payer organizations where claims, enrollment, provider, and finance teams need separate governance boundaries. [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/what-is-new-in-datazone.html)

***

### 2. Metadata Enforcement Rules - Released November 2024.

DataZone now allows organizations to enforce metadata requirements before access requests are approved.

**Examples**

* Cost center mandatory
* Business justification mandatory
* Data classification required
* Intended usage must be provided

**Benefit**
Improves governance and auditability while ensuring consumers provide sufficient business context before gaining access to sensitive datasets. [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/what-is-new-in-datazone.html)

***

### 3. Data Lineage (Generally Available) - Announced GA in late 2024.

**Features**

* Automatic lineage capture
  * AWS Glue
  * Amazon Redshift
  * Spark workloads
* Transformation tracking
* Schema evolution visibility
* Historical lineage versions
* OpenLineage compatibility

This feature enables us to answer queries like:

> "This dashboard metric is wrong. Which ETL job transformed the data and which source tables contributed to it?"

or

> "If I change this claims table, who will be impacted?"

This is one of the biggest gaps traditional Glue Catalog users had before DataZone lineage capabilities matured. [\[aws-news.com\]](https://aws-news.com/article/01938de1-2839-a0a2-d4f8-2a6899cd3682)

***

### 4. CloudFormation Support for Custom Blueprints - Released September 2024.

**What it adds**

* Infrastructure-as-Code support
* Automated environment creation
* Repeatable project provisioning

This is helpful if we  want DataZone integrated into enterprise platform engineering and CI/CD pipelines.

Instead of manually configuring environments, we can deploy them programmatically with CloudFormation. [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/what-is-new-in-datazone.html)

***

### 5. Enhanced SageMaker Integration - Released November 2024 and expanded further through 2025.

DataZone has become increasingly aligned with **SageMaker Unified Studio**.

Capabilities include:

* Import existing SageMaker domains
* Reuse users, permissions, and configurations
* Unified governance experience
* Shared catalog across analytics, data engineering, and AI/ML workloads

AWS is positioning DataZone governance as a foundational layer for SageMaker Unified Studio. [\[docs.aws.amazon.com\]](https://docs.aws.amazon.com/datazone/latest/userguide/what-is-new-in-datazone.html), [\[aws-news.com\]](https://aws-news.com/article/2025-09-11-use-the-amazon-datazone-upgrade-domain-to-amazon-sagemaker-and-expand-to-new-sql-analytics-data-processing-and-ai-uses-cases)

***

### 6. Upgrade Path to SageMaker Unified Studio - Introduced during 2025.

AWS added support for upgrading existing DataZone domains into the broader SageMaker Unified Studio ecosystem.

Migrated assets include:

* Catalog assets
* Metadata forms
* Glossaries
* Subscriptions

This allows organizations that started with DataZone governance to expand into:

* SQL analytics
* Data engineering
* AI/ML development
* GenAI workloads

without rebuilding governance structures. [\[aws-news.com\]](https://aws-news.com/article/2025-09-11-use-the-amazon-datazone-upgrade-domain-to-amazon-sagemaker-and-expand-to-new-sql-analytics-data-processing-and-ai-uses-cases)

***

### 7. AI-Assisted Cataloging

AWS continues to enhance DataZone's use of generative AI for metadata management.

Capabilities include:

* Auto-generated business names
* Improved dataset descriptions
* Better discoverability
* Automated catalog curation using LLMs

This reduces the manual effort required to make technical datasets understandable to business users. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/)

***

## The biggest recent improvements in DataZone are focused on:

✅ Governance  
✅ Lineage  
✅ Domain-based ownership  
✅ Self-service data access  
✅ SageMaker/AI integration

However, DataZone is still most powerful our data ecosystem is predominantly AWS-centric. 

For organizations cataloging assets across:

* AWS
* Snowflake
* MongoDB
* Salesforce Data Cloud
* Azure

platforms, tools such as **Alation, Collibra, Atlan, or Informatica Data Governance Cloud** often provide a more vendor-neutral governance experience, though typically at a higher cost. [\[aws.amazon.com\]](https://aws.amazon.com/datazone/features/), [\[aws.amazon.com\]](https://aws.amazon.com/blogs/big-data/category/analytics/amazon-datazone/)

