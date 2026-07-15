**Certification Reviews** 
- Formal governance reviews used to determine whether a data asset, dataset, dashboard, report, or data product can be marked as **Trusted**, **Certified**, **Approved**, or **Gold Standard** for enterprise use.

In Alation, certification helps answer a critical business question:

> **"Of the 50 datasets about customers, which one should I actually use?"**

***

# Why Certification Reviews Are Needed

Without certification, business users often find:

```text
customer
customer_v2
customer_master
customer_final
customer_final_v2
customer_gold
customer_prod
```

and have no idea which dataset is authoritative.

Certification identifies the organization's preferred and governed asset.

Example:

```text
✅ Customer360  (Certified)

❌ customer_tmp
❌ customer_test
❌ customer_backup
```

Business users are guided toward the trusted asset.

***

# What Gets Certified?

Typical candidates include:

### Data Assets

```text
Tables
Views
Collections
Files
```

### Data Products

```text
Customer 360
Provider 360
Claims Analytics
Broker Performance
```

### BI Assets

```text
Power BI reports
Tableau dashboards
Semantic models
```

### AI Assets

```text
Feature datasets
Training datasets
Prompt libraries
Knowledge bases
```

***

# What is Reviewed During Certification?

A certification review evaluates whether an asset meets predefined governance standards.

## 1. Ownership Review

Verify:

```text
✓ Data Owner assigned
✓ Data Steward assigned
✓ Technical Custodian assigned
```

Example:

```text
Owner:
Customer Domain Lead

Steward:
Customer Data Steward
```

***

## 2. Metadata Completeness Review

Verify:

```text
✓ Business name
✓ Description
✓ Glossary mappings
✓ Tags
✓ Classification
```

Example:

```text
Dataset:
Customer360

Description:
Gold customer record used for
enterprise analytics.
```

***

## 3. Data Quality Review

Verify quality metrics.

Example:

```text
Completeness > 95%

Freshness < 24 hours

Accuracy validated

Schema validation passed
```

Datasets failing quality thresholds are usually not certified.

***

## 4. Security & Compliance Review

Verify:

```text
✓ PII classified
✓ PHI classified
✓ GDPR reviewed
✓ Retention policy defined
✓ Access controls implemented
```

Example:

```text
Contains email addresses

Classification:
Confidential
```

***

## 5. Lineage Review

Verify lineage exists and has been validated.

Example:

```text
Salesforce Data Cloud
        ↓
Snowflake
        ↓
Customer360
        ↓
Power BI
```

Questions:

```text
Can we trace the source?

Is lineage complete?

Was lineage reviewed?
```

***

## 6. Business Validation Review

This is often the most important step.

Questions:

```text
Is this the official dataset?

Do business teams agree?

Should everyone use this asset?
```

Example:

```text
Customer Domain Council

Approved:
Customer360
```

***

# Typical Certification Workflow

```text
Metadata Harvest
       ↓
Steward Enrichment
       ↓
Data Quality Review
       ↓
Security Review
       ↓
Lineage Validation
       ↓
Business Approval
       ↓
Certification
       ↓
Publish to Marketplace
```

***

# Certification Levels

Many organizations use multiple levels.

## Bronze

```text
Metadata complete
Owner assigned
```

Basic catalog readiness.

***

## Silver

```text
Metadata complete
Quality validated
Lineage available
```

Suitable for general analytics.

***

## Gold

```text
Quality verified
Lineage certified
Business approved
Steward assigned
Compliance approved
```

Enterprise-recommended dataset.

Example:

```text
Customer360
Gold Certified
```

***

# Certification Review Checklist

For each asset:

```text
Ownership
---------
✓ Owner
✓ Steward

Metadata
---------
✓ Description
✓ Glossary Terms
✓ Tags

Quality
---------
✓ Freshness
✓ Completeness

Security
---------
✓ Classification
✓ Access Controls

Lineage
---------
✓ Upstream Sources
✓ Downstream Consumers

Business
---------
✓ Domain Approval
✓ Usage Guidance
```

***

# Automation Opportunities

We can automate much of the review process.

Example rules:

```text
Description Exists
AND

Owner Assigned
AND

Steward Assigned
AND

Quality Score > 90
AND

Lineage Exists
```

Then:

```text
Certification Candidate = TRUE
```

A steward only performs the final approval.

***

# Recommended Model for the Environment

For :

```text
AWS Glue Catalog
Snowflake
MongoDB Atlas
Salesforce Data Cloud
Alation Marketplace
```

I would establish a **Data Product Certification Board**.

### Participants

```text
Data Owner
Data Steward
Governance Lead
Security Representative
Platform Representative
```

### Review Frequency

```text
Gold Assets:
Quarterly

Silver Assets:
Semi-Annually

Bronze Assets:
Annually
```

***

# Example: Customer360 Certification Review

### Asset

```text
Customer360
```

### Sources

```text
Salesforce Data Cloud
Snowflake
MongoDB Atlas
```

### Review Results

```text
Owner Assigned         ✓
Steward Assigned       ✓
Description Present    ✓
Glossary Mapped        ✓
Lineage Validated      ✓
Quality Score          96%
PII Classified         ✓
Access Controls        ✓
```

### Outcome

```text
Certification Status:

✅ Gold Certified
```

The asset is then prominently promoted in Alation as the recommended enterprise customer dataset.

***

## In Summary

A **Certification Review** is a formal governance process that verifies a data asset is:

* Properly owned
* Well documented
* High quality
* Secure and compliant
* Lineage validated
* Business approved

The result is a **Certified Data Asset/Data Product** that business users can confidently discover and consume through Alation's data marketplace.
