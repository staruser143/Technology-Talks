**Glossary Management** 
 Creating, maintaining, governing, and publishing a common set of **business terms and definitions** that everyone in the organization uses consistently.

In an Alation implementation, the **Business Glossary** is often the foundation that turns a technical catalog into a business-friendly data marketplace.

***

# Why is Glossary Management Needed?

Without a glossary, different teams often use different terminology.

### Example

Marketing:

```text
Customer
```

Sales:

```text
Account
```

CRM:

```text
Contact
```

MongoDB:

```text
memberId
```

Snowflake:

```text
customer_key
```

AWS Data Lake:

```text
cust_id
```

Business users may not know these all refer to the same concept.

A business glossary creates a standard definition:

```text
Business Term:
Customer

Definition:
An individual or organization that has
purchased or subscribed to one or more
products or services.

Mapped Assets:
customer_key
cust_id
memberId
ContactId
```

***

# What Does Glossary Management Include?

## 1. Creating Business Terms

Define important business concepts.

Examples:

```text
Customer
Member
Provider
Broker
Policy
Claim
Enrollment
Premium
Revenue
```

Each term should have:

* Name
* Definition
* Business owner
* Steward
* Related terms
* Classification

***

## 2. Maintaining Definitions

Business definitions evolve.

### Example

Old Definition

```text
Customer =
Anyone registered in our systems
```

New Definition

```text
Customer =
A person with at least one active policy
```

Glossary management ensures everyone uses the latest approved definition.

***

## 3. Mapping Terms to Data Assets

This is where Alation becomes powerful.

### Business Term

```text
Customer
```

Mapped To:

```text
Snowflake.customer_key

MongoDB.memberId

Salesforce.ContactId

AWS.customer_id
```

Now a business user can search:

```text
Customer
```

instead of knowing the physical column names.

***

## 4. Managing Synonyms

Different teams may use different words.

### Example

```text
Customer
```

Synonyms:

```text
Member
Subscriber
Consumer
Client
```

All point to the same glossary term.

This dramatically improves search and discoverability.

***

## 5. Establishing Relationships

Business terms are related.

Example:

```text
Customer
    ├─ Enrollment
    ├─ Policy
    ├─ Claim
    └─ Premium
```

This creates a business knowledge model.

***

# Typical Glossary Structure

For a Healthcare Payer organization:

```text
Customer Domain
 ├─ Member
 ├─ Subscriber
 ├─ Dependent

Claims Domain
 ├─ Claim
 ├─ Claim Status
 ├─ Adjudication

Provider Domain
 ├─ Provider
 ├─ Facility
 ├─ Network

Broker Domain
 ├─ Broker
 ├─ Agency

Enrollment Domain
 ├─ Enrollment
 ├─ Coverage
```

***

# Role of Data Steward in Glossary Management

The Data Steward typically owns glossary maintenance.

Responsibilities include:

```text
Create new terms

Review definitions

Approve updates

Map assets

Resolve conflicts

Maintain relationships
```

### Example

Business user asks:

> What is the difference between Member and Subscriber?

The Data Steward ensures the glossary clearly defines both.

***

# Glossary Management Workflow

```text
Business Need
      ↓
Create Term
      ↓
Review by Steward
      ↓
Approve Definition
      ↓
Map to Assets
      ↓
Publish
      ↓
Periodic Review
```

***

# Glossary Management in Alation

A typical Alation glossary term includes:

```text
Term:
Customer

Definition:
An individual who has purchased or
subscribed to a service.

Owner:
Customer Domain Lead

Steward:
Customer Data Steward

Related Terms:
Subscriber
Member
Policy

Mapped Assets:
Snowflake.dim_customer
AWS.customer_profile
MongoDB.members
Salesforce.Contact
```

When users search for:

```text
Customer
```

they can discover all related assets, reports, dashboards, and data products.

***

# How to Automate Glossary Management

For large enterprises, much of the process can be automated.

## Rule-Based Mapping

Example:

```text
customer_id
member_id
subscriber_id
```

Automatically map to:

```text
Customer
```

***

## AI-Assisted Mapping

An LLM can suggest:

```text
Column:
loyalty_tier

Suggested Term:
Customer Loyalty
```

Stewards then approve or reject the suggestion.

***

## Metadata-Driven Mapping

Example:

```text
Database = claims
```

Automatically associate glossary terms:

```text
Claim
Claim Status
Adjudication
```

***

# Common Glossary Management Challenges

## 1. Duplicate Terms

Example:

```text
Customer
Client
Member
Subscriber
```

used interchangeably.

**Solution:**

Create one primary term and manage the others as synonyms.

***

## 2. Too Many Terms

Some organizations create thousands of terms.

Result:

```text
Users can't find anything.
```

**Recommendation:**

Start with the most important 100–200 business terms.

***

## 3. No Ownership

Glossary terms become outdated.

**Recommendation:**

Every term should have:

```text
Owner
Steward
Review Date
```

***

## 4. Poor Adoption

Users continue relying on tribal knowledge.

**Recommendation:**

Make glossary terms visible in:

* Data catalog
* Marketplace
* Search results
* Reports
* Dashboards

***

# Recommended Glossary Model for the Environment

Given the domains:

```text
Salesforce Data Cloud
Snowflake
MongoDB Atlas
AWS Data Lake
```

I would create a domain-based glossary:

```text
Customer Domain
Claims Domain
Provider Domain
Broker Domain
Enrollment Domain
Finance Domain
```

Each domain should have:

```text
Domain Owner
Domain Steward
Approved Business Terms
Mapped Data Assets
Certified Data Products
```

***

# Relationship Between Glossary, Stewardship, and Marketplace

```text
Glossary Management
        ↓
Business Definitions

Data Stewardship
        ↓
Ownership & Governance

Metadata Enrichment
        ↓
Context & Trust

Alation Marketplace
        ↓
Business Discovery & Consumption
```

### In one sentence:

**Glossary Management is the process of defining and governing a common business vocabulary and linking those business terms to physical data assets, enabling users to search, understand, trust, and consume data consistently across AWS, Snowflake, MongoDB Atlas, Salesforce Data Cloud, and other platforms.**
