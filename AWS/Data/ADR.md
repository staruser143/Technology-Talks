# Architecture Decision Record

## Discovery and Consumption of Data Assets Across AWS and Non-AWS Platforms

---

## 1. Decision Title

**Adopt Alation Cloud as the Enterprise Data Marketplace and Discovery Layer, with AWS Glue Catalog retained as the AWS technical metadata repository and Amazon DataZone used selectively for AWS-native governed sharing**

---

## 2. Status

**Recommended**

---

## 3. Context

The enterprise data landscape consists of data assets distributed across multiple platforms:

```
AWS
├─ Amazon S3
├─ AWS Glue Data Catalog
├─ Amazon Athena
├─ Amazon Redshift
└─ AWS Lake Formation

Non-AWS / Multi-cloud
├─ Snowflake
├─ MongoDB Atlas
├─ Salesforce Data Cloud
├─ Azure data platforms
├─ Databricks
├─ Power BI
└─ Tableau
```

Business users need a single, governed, business-friendly way to:

- Discover available data assets
- Understand business meaning
- Identify trusted and certified datasets
- View ownership and stewardship details
- Understand lineage and quality
- Request access
- Consume data through appropriate tools
- Reuse data products across domains

AWS Glue Data Catalog is primarily a technical metadata repository for AWS analytics workloads. Amazon DataZone provides business catalog, publish/subscribe workflows, projects, environments, and governance capabilities, but is AWS-centric.

---

## 4. Problem Statement

The organization needs an architecture that enables **business users** to discover and consume data assets across both AWS and non-AWS platforms.

The key challenge is that metadata and governance are currently fragmented:

```
AWS assets       → AWS Glue Catalog / Lake Formation
Snowflake assets → Snowflake metadata and RBAC
MongoDB assets   → MongoDB Atlas metadata and roles
Salesforce data  → Salesforce metadata and permissions
BI assets        → Power BI / Tableau metadata
```

Without a unified enterprise catalog, business users face the following problems:

- Multiple search locations
- Inconsistent business definitions
- No unified glossary
- Duplicate or conflicting datasets
- Unclear ownership
- Manual access request processes
- Limited visibility into lineage and trust
- Poor reuse of certified data products

---

## 5. Decision Drivers

| Driver | Importance |
|--------|-----------|
| Multi-cloud and non-AWS source support | High |
| Business-user-friendly discovery | High |
| Enterprise glossary and semantic consistency | High |
| Data product marketplace capability | High |
| Access request and approval workflows | High |
| Cross-platform metadata harvesting | High |
| Cross-platform lineage | Medium to High |
| Integration with existing AWS Glue Catalog | High |
| Avoidance of AWS-only lock-in | High |
| Support for Snowflake, MongoDB Atlas, Salesforce Data Cloud | High |
| Data stewardship and certification workflows | High |
| Native AWS access fulfillment | Medium |
| Cost, operational complexity, and governance overhead | Medium |

---

## 6. Options Considered

---

## Option 1: AWS-Native Only

### AWS Glue Catalog + Amazon DataZone + Lake Formation

#### Architecture

```
AWS Sources
├─ S3
├─ Glue Catalog
├─ Redshift
├─ Athena
└─ Lake Formation
        ↓
  Amazon DataZone
        ↓
  Business Users
```

#### Description

Use AWS Glue Catalog as the technical metadata repository and Amazon DataZone as the business catalog, data portal, and governed sharing layer.

Amazon DataZone provides business catalog, publish/subscribe workflows, projects, environments, and data portal capabilities. It also supports automatic access fulfillment for Lake Formation-managed AWS resources.

#### Advantages

- Strong AWS-native integration
- Good fit for AWS S3, Glue, Athena, Redshift, and Lake Formation
- Built-in publish/subscribe workflow for AWS assets
- Automatic access fulfillment for supported AWS resources
- Lower integration complexity for AWS-only use cases

#### Limitations

- Best suited when AWS is the dominant data platform
- Non-AWS assets require custom asset types, APIs, or integration work
- Weaker fit for enterprise-wide multi-cloud discovery
- Limited native governance reach across Snowflake, Salesforce Data Cloud, MongoDB Atlas, Azure, and BI tools
- Potential AWS platform lock-in for catalog and governance experience

#### Fitment

Good for AWS-centric data lake environments.

Less suitable as the primary enterprise marketplace when major assets exist in Snowflake, Salesforce Data Cloud, MongoDB Atlas, and other clouds.

---

## Option 2: Alation Cloud as Enterprise Catalog Only

### Architecture

```
AWS Glue Catalog
Snowflake
MongoDB Atlas
Salesforce Data Cloud
Azure
Databricks
Power BI
Tableau
        ↓
  Alation Cloud
        ↓
  Business Users
```

#### Description

Use Alation Cloud as the enterprise-wide data discovery, glossary, governance, lineage, stewardship, and marketplace platform.

Alation has connector coverage across AWS Glue, AWS S3, AWS Redshift, Snowflake on AWS/Azure/GCP, MongoDB, MongoDB Atlas, Salesforce, Azure Synapse, Databricks, Power BI, Tableau, and many other platforms.

#### Advantages

- Strong multi-cloud support
- Broad enterprise connector ecosystem
- Unified business glossary
- Business-friendly search and discovery
- Data marketplace capability
- Cross-platform metadata harvesting
- Better fit for Snowflake, MongoDB Atlas, Salesforce, Azure, and BI platforms
- Avoids treating AWS Glue as the central repository for every non-AWS asset
- Supports data stewardship, certification, lineage, usage analytics, and trust indicators

#### Limitations

- Does not replace AWS Glue for AWS-native query engines
- Does not automatically fulfill AWS Lake Formation permissions in the same native way as DataZone
- Requires operational maturity around connector management, metadata enrichment, data stewardship, and glossary governance
- Access provisioning usually needs integration with ServiceNow, IAM, Snowflake RBAC, Salesforce permissions, MongoDB roles, or other workflow systems

#### Fitment

Strong fit for a multi-cloud enterprise data marketplace.

---

## Option 3: Hybrid Model (Recommended)

### Alation Cloud + AWS Glue Catalog + Selective Amazon DataZone

#### Architecture

```
                     Business Users
                           |
                           ↓
      +--------------------------------------------------+
      |             Alation Cloud                        |
      |--------------------------------------------------|
      | Enterprise Search                                |
      | Business Glossary                                |
      | Data Marketplace                                 |
      | Certified Data Products                          |
      | Stewardship                                      |
      | Lineage                                          |
      | Usage Analytics                                  |
      | Access Request Workflow                          |
      +--------------------------------------------------+
                           |
                           ↓
      +--------------------------------------------------+
      |  Source Metadata and Governance                  |
      |--------------------------------------------------|
      | AWS Glue Catalog                                 |
      | Snowflake Metadata                               |
      | MongoDB Atlas Metadata                           |
      | Salesforce Metadata                              |
      | Azure / Databricks Metadata                      |
      | Power BI / Tableau Metadata                      |
      +--------------------------------------------------+
                           |
                           ↓
      +--------------------------------------------------+
      |        Native Data Platforms                     |
      |--------------------------------------------------|
      | AWS S3 / Athena / Redshift / Lake Formation      |
      | Snowflake                                        |
      | MongoDB Atlas                                    |
      | Salesforce Data Cloud                            |
      | Azure Data Platforms                             |
      | Databricks                                       |
      | BI Platforms                                     |
      +--------------------------------------------------+
```

#### Description

Adopt a hybrid architecture:

```typescript
// Architecture Pattern
interface DataGovernanceArchitecture {
  enterpriseCatalog: "Alation Cloud";           // Business catalog and marketplace
  awsMetadataRepository: "AWS Glue Catalog";    // AWS technical metadata
  awsGovernedSharing: "Amazon DataZone";        // Optional AWS-native sharing
  sourcePlatforms: "Systems of record";         // Security and access control
}
```

- **Alation Cloud** = Enterprise business catalog and data marketplace
- **AWS Glue Catalog** = AWS technical metadata repository
- **Amazon DataZone** = Optional AWS-native governed sharing layer for AWS-specific use cases
- **Source platforms** = Systems of record for security and access control

#### Advantages

- Best fit for AWS and non-AWS data discovery
- Preserves AWS Glue for AWS-native technical metadata
- Allows Alation to harvest directly from Snowflake, MongoDB Atlas, Salesforce, AWS Glue, and BI tools
- Avoids forcing all metadata through Glue Catalog
- Provides a single enterprise discovery and marketplace experience
- Supports business glossary, stewardship, certification, lineage, trust, and access workflows
- Allows DataZone to be used selectively for AWS-native subscription and access fulfillment

#### Limitations

- Requires clear metadata ownership model
- Requires connector management
- Requires data stewardship operating model
- Requires governance alignment across Alation, Glue, Snowflake, Salesforce, MongoDB, and AWS Lake Formation
- Requires workflow integration for access provisioning

#### Fitment

**Best fit for the stated requirement.**

---

## 7. Decision

The recommended architecture is:

```typescript
// Final Architecture Decision
const dataGovernanceStrategy = {
  primaryLayer: "Alation Cloud",
  description: "Enterprise-wide data discovery, business catalog, governance, and data marketplace",
  
  awsTechnicalRepository: "AWS Glue Catalog",
  description: "Technical metadata repository for AWS assets",
  
  selectiveAwsLayer: "Amazon DataZone",
  description: "Optional AWS-native governed access and subscription workflows",
  
  accessControl: "Native platforms",
  description: "Snowflake RBAC, MongoDB Atlas roles, Lake Formation, IAM, etc."
};
```

---

## 8. Rationale

### 8.1 Multi-cloud and non-AWS coverage is a primary requirement

The data estate includes Snowflake, MongoDB Atlas, Salesforce Data Cloud, and potentially Azure. Alation is better aligned to this requirement because it provides broad connector coverage across these platforms without forcing all metadata through AWS Glue.

### 8.2 AWS Glue Catalog should not become the universal enterprise catalog

AWS Glue Catalog should remain the technical metadata authority for AWS assets. It is valuable for Athena, Glue ETL, Redshift Spectrum, EMR, and Lake Formation. However, using Glue as the intermediary for all non-AWS metadata is architecturally suboptimal—it introduces unnecessary coupling and reduces the platform's ability to represent domain-specific metadata semantics.

### 8.3 Amazon DataZone is valuable but AWS-centric

Amazon DataZone provides strong AWS-native capabilities such as business catalog, publish/subscribe workflows, projects, environments, and automatic access fulfillment for supported AWS assets. However, it is not the right choice as the sole enterprise catalog when major data assets exist outside AWS.

### 8.4 Alation best satisfies enterprise marketplace requirements

Alation is better suited as the enterprise business-facing layer because it supports:

- Unified discovery
- Business glossary
- Data marketplace
- Data product publishing
- Stewardship workflows
- Certification reviews
- Usage analytics
- Lineage
- Cross-platform metadata harvesting
- Business-friendly search

---

## 9. Target Architecture

```
+------------------------------------------------------------+
|                    Business Users                          |
|------------------------------------------------------------|
| Analysts | Data Stewards | Product Owners | Executives     |
+-----------------------------+------------------------------+
                              |
                              ↓
+------------------------------------------------------------+
|                     Alation Cloud                          |
|------------------------------------------------------------|
| Enterprise Data Marketplace                                |
| Business Glossary                                          |
| Metadata Enrichment                                        |
| Certified Data Products                                    |
| Stewardship Workflows                                      |
| Lineage and Impact Analysis                                |
| Usage Analytics                                            |
| Access Request Intake                                      |
+-----------------------------+------------------------------+
                              |
                              ↓
+------------------------------------------------------------+
|              Metadata Harvesting Connectors                |
|------------------------------------------------------------|
| AWS Glue | Snowflake | MongoDB Atlas | Salesforce          |
| Azure | Databricks | Power BI | Tableau                   |
+-----------------------------+------------------------------+
                              |
                              ↓
+------------------------------------------------------------+
|                 Native Source Platforms                    |
|------------------------------------------------------------|
| AWS S3 / Glue / Athena / Redshift / Lake Formation         |
| Snowflake                                                  |
| MongoDB Atlas                                              |
| Salesforce Data Cloud                                      |
| Azure Data Platforms                                       |
| Databricks                                                 |
| BI Platforms                                               |
+------------------------------------------------------------+
```

---

## 10. Metadata Ownership Model

| Metadata Type | System of Record |
|---------------|------------------|
| AWS table schema, partitions, locations | AWS Glue Catalog |
| AWS permissions | Lake Formation / IAM |
| Snowflake schema and technical metadata | Snowflake |
| Snowflake permissions | Snowflake RBAC |
| MongoDB collection structure | MongoDB Atlas |
| MongoDB access control | MongoDB Atlas roles |
| Salesforce objects and fields | Salesforce / Salesforce Data Cloud |
| Salesforce permissions | Salesforce security model |
| Business glossary | Alation |
| Business descriptions | Alation |
| Data stewardship details | Alation |
| Data product certification | Alation |
| Trust indicators | Alation |
| Marketplace publishing | Alation |
| Access request intake | Alation or ServiceNow-integrated workflow |

---

## 11. Data Discovery Flow

```
Business User
     |
     ↓
Searches in Alation
     |
     ↓
Finds business term or data product
     |
     ↓
Reviews description, owner, steward, quality, lineage
     |
     ↓
Identifies certified asset
     |
     ↓
Requests access
     |
     ↓
Access routed to relevant platform workflow
     |
     ↓
Consumes data in native tool
```

**Example:**

```
Search Term:
Customer

Alation Results:
├─ Customer 360 Data Product
├─ Snowflake dim_customer
├─ Salesforce Contact / Account object
├─ MongoDB members collection
├─ AWS Glue customer_profile table
└─ Power BI Customer Retention Dashboard
```

---

## 12. Data Consumption Pattern

Discovery should be centralized, but physical consumption should remain platform-native.

| Asset Location | Consumption Tool |
|---|---|
| AWS S3 / Glue | Athena, Redshift Spectrum, EMR, Spark |
| Redshift | Redshift Query Editor, BI tools |
| Snowflake | Snowflake UI, BI tools, data shares |
| MongoDB Atlas | Application APIs, Atlas SQL, BI Connector, ETL jobs |
| Salesforce Data Cloud | Salesforce-native interfaces, APIs, data shares |
| Azure | Synapse, Fabric, Power BI |
| Databricks | Databricks SQL, notebooks, Unity Catalog |
| BI Assets | Power BI, Tableau, Looker |

---

## 13. Access Request and Fulfillment Model

### Recommended Pattern

```
Alation
  = Request and governance workflow entry point

ServiceNow / Workflow Engine
  = Approval orchestration

Native platforms
  = Permission enforcement
```

### Flow

```
User requests access in Alation
        |
        ↓
Request routed to workflow system
        |
        ↓
Owner / steward approval
        |
        ↓
Access provisioned in native platform
        |
        ↓
User consumes data
```

### Platform Enforcement

| Platform | Enforcement Mechanism |
|---|---|
| AWS S3 / Glue | Lake Formation / IAM |
| Redshift | Redshift roles / IAM |
| Snowflake | Snowflake RBAC |
| MongoDB Atlas | Atlas roles |
| Salesforce Data Cloud | Salesforce permissions |
| Azure | Azure RBAC / Purview / Fabric controls |
| Databricks | Unity Catalog permissions |

---

## 14. Metadata Harvesting Strategy

### Recommended Harvesting Model

```
AWS Glue Catalog ───────┐
Snowflake ──────────────┤
MongoDB Atlas ──────────┤
Salesforce Data Cloud ──┤
Azure / Databricks ─────┤
Power BI / Tableau ─────┘
                         ↓
                    Alation Cloud
```

### Harvesting Principles

- Harvest AWS technical metadata from AWS Glue Catalog
- Harvest Snowflake metadata directly from Snowflake
- Harvest MongoDB Atlas metadata directly from MongoDB Atlas where supported
- Harvest Salesforce metadata directly through Salesforce connector/API capabilities
- Harvest BI metadata from Power BI/Tableau connectors
- Avoid routing non-AWS metadata through Glue unless the data is physically landed in AWS S3 for lakehouse consumption

---

## 15. Metadata Enrichment Strategy

Metadata harvesting alone is insufficient. The organization must automate enrichment.

### Enrichment Layers

```typescript
interface MetadataEnrichment {
  layers: [
    "Rule-based enrichment",
    "Domain-based enrichment",
    "Usage-based enrichment",
    "Quality-based enrichment",
    "Lineage-based enrichment",
    "AI-assisted description generation",
    "Steward-approved certification"
  ];
}
```

### Examples

| Enrichment Type | Automation Example |
|---|---|
| PII classification | email, phone, dob → Confidential / PII |
| Glossary mapping | customer_id, memberId, ContactId → Customer |
| Ownership assignment | claims_* datasets → Claims Domain Owner |
| Steward assignment | domain registry-based steward allocation |
| Usage enrichment | query logs identify popular assets |
| Quality enrichment | quality score imported from DQ tools |
| Certification candidate | owner + steward + lineage + quality > threshold |

---

## 16. Governance Operating Model

### Key Roles

| Role | Responsibility |
|---|---|
| Data Owner | Business accountability for data |
| Data Steward | Metadata quality, definitions, classifications, certification |
| Data Custodian | Technical platform and access control management |
| Data Product Owner | Publishes and maintains reusable data products |
| Governance Lead | Defines policies and standards |
| Alation Platform Admin | Connector and platform management |
| Metadata Operations Team | Harvesting, quality checks, glossary support, stewardship operations |

---

## 17. Certification Review Process

Certified datasets should be promoted in the marketplace.

### Certification Criteria

```
✓ Owner assigned
✓ Steward assigned
✓ Business description complete
✓ Glossary terms mapped
✓ Sensitivity classification applied
✓ Lineage validated
✓ Quality score above threshold
✓ Access policy documented
✓ Business approval completed
```

### Certification Levels

```
Bronze = Metadata complete
Silver = Metadata + quality + owner/steward validated
Gold   = Enterprise-approved, lineage validated, quality certified
```

**Example:**

```
Customer360
Status: Gold Certified
```

---

## 18. Lineage Validation Process

Lineage should be validated first for Tier-1 data products.

### Validation Flow

```
Harvest lineage
     |
     ↓
Compare with actual pipelines
     |
     ↓
Validate source-to-target mapping
     |
     ↓
Validate transformations
     |
     ↓
Steward review
     |
     ↓
Certification
```

### Priority Assets

- Customer 360
- Provider 360
- Claims Analytics
- Broker Analytics
- Enrollment Analytics

---

## 19. Connector Management

Connector management is a critical operational capability.

### Responsibilities

- Connector onboarding
- Service account creation
- Least-privilege access configuration
- Metadata harvesting schedule
- Query log ingestion
- Profiling configuration
- Lineage extraction
- Monitoring failures
- Version upgrades
- Credential rotation

### Recommended Ownership

| Connector | Owner |
|---|---|
| AWS Glue | Data Lake Team |
| Snowflake | Data Warehouse Team |
| MongoDB Atlas | Application Platform Team |
| Salesforce | CRM Platform Team |
| Power BI / Tableau | Analytics Platform Team |
| Alation Platform | Metadata Governance Team |

---

## 20. Risks and Mitigations

| Risk | Consequence | Mitigation |
|---|---|---|
| Multiple catalogs create confusion | Users do not know which catalog to trust | Define Glue as AWS technical catalog and Alation as enterprise business catalog |
| Duplicate assets | Search noise and poor adoption | Use data products, glossary mapping, certification, and asset curation |
| Metadata becomes stale | Users lose trust | Define harvesting SLAs and connector monitoring |
| Access fulfillment is manual | Slow onboarding and user frustration | Integrate Alation with ServiceNow/IAM/platform provisioning workflows |
| Poor stewardship adoption | Catalog remains technical, not business-ready | Establish domain-based data stewardship model |
| Weak lineage coverage | Poor impact analysis | Validate lineage for Tier-1 assets first |
| Over-harvesting metadata | Marketplace becomes cluttered | Onboard domains incrementally |
| Inconsistent glossary terms | Business confusion | Establish glossary governance council |
| Sensitive data misclassification | Compliance risk | Automate classification and require steward review |
| Alation perceived as replacing Glue | Architecture confusion | Communicate clear role separation |

---

## 21. Consequences of the Decision

### Positive Consequences

- Single enterprise marketplace for business users
- Stronger multi-cloud discovery
- Better business glossary alignment
- Better support for Snowflake, MongoDB Atlas, Salesforce, Azure, and BI tools
- Reduced AWS catalog lock-in
- Improved data product reuse
- Clear ownership and stewardship model
- Better trust through certification and lineage
- AWS Glue remains optimized for AWS-native analytics

### Negative Consequences

- Additional platform licensing and operational cost
- More complex metadata governance model
- Need for dedicated metadata operations team
- Access provisioning may need workflow integration
- Requires stewardship discipline
- Requires connector lifecycle management

---

## 22. Implementation Roadmap

### Phase 1: Foundation

```
✓ Define metadata operating model
✓ Define domains
✓ Define glossary structure
✓ Define certification levels
✓ Define ownership/stewardship model
✓ Configure Alation Cloud
✓ Integrate SSO
```

### Phase 2: Pilot Metadata Harvesting

```
✓ Connect AWS Glue Catalog
✓ Connect Snowflake
✓ Connect MongoDB Atlas
✓ Connect Salesforce / Salesforce Data Cloud where supported
✓ Connect Power BI / Tableau
✓ Validate metadata coverage
```

### Phase 3: Business Enrichment

```
✓ Add business descriptions
✓ Map glossary terms
✓ Assign owners and stewards
✓ Apply classifications
✓ Add usage guidance
✓ Identify certified candidate assets
```

### Phase 4: Marketplace Launch

```
✓ Publish first data products
✓ Enable access request workflows
✓ Promote certified assets
✓ Train business users
✓ Collect adoption feedback
```

### Phase 5: Governance and Automation

```
✓ Automate metadata enrichment
✓ Automate classification rules
✓ Integrate data quality scores
✓ Implement lineage validation
✓ Integrate ServiceNow/access workflows
✓ Monitor connector health
```

### Phase 6: Scale

```
✓ Expand to more domains
✓ Add Azure and Databricks sources
✓ Add more BI assets
✓ Establish quarterly certification reviews
✓ Track adoption and reuse metrics
```

---

## 23. Success Metrics

| Metric | Target |
|---|---|
| Tier-1 assets onboarded | 100% |
| Metadata freshness | < 24 hours |
| Certified Tier-1 data products | > 90% |
| Assets with owner and steward | > 95% |
| Assets with business description | > 90% |
| Assets mapped to glossary terms | > 80% |
| Connector success rate | > 99% |
| Access request SLA | < 3 business days |
| Business user adoption | Increasing monthly active users |
| Duplicate dataset reduction | Measurable reduction over time |
| Lineage coverage for Tier-1 assets | > 90% |

---

## 24. Final Recommendation

Adopt the following architecture:

```typescript
// Final Enterprise Data Governance Architecture
const recommendedArchitecture = {
  // Enterprise Data Marketplace
  alationCloud: {
    role: "Enterprise Data Marketplace, business catalog, discovery, glossary, stewardship, certification, lineage, and access request layer",
    capabilities: [
      "Business-user discovery",
      "Enterprise glossary",
      "Data product marketplace",
      "Certification workflows",
      "Stewardship tracking",
      "Lineage visualization",
      "Access request intake"
    ]
  },
  
  // AWS Technical Repository
  awsGlueCatalog: {
    role: "AWS technical metadata repository for S3, Athena, Glue, Redshift Spectrum, Lake Formation, and AWS-native analytics",
    capabilities: [
      "AWS table schema management",
      "Partition tracking",
      "Location management",
      "AWS-native query optimization"
    ]
  },
  
  // Optional AWS Governed Sharing
  amazonDataZone: {
    role: "Optional AWS-specific governed sharing and subscription layer where AWS-native access fulfillment is valuable",
    capabilities: [
      "AWS-native publish/subscribe",
      "Automatic access fulfillment",
      "Lake Formation integration",
      "Projects and environments"
    ],
    usage: "Selective, for AWS-centric workflows"
  },
  
  // Access Control
  nativePlatforms: {
    role: "Systems of record for physical access enforcement",
    examples: [
      "Lake Formation / IAM",
      "Snowflake RBAC",
      "MongoDB Atlas roles",
      "Salesforce permissions",
      "Azure RBAC"
    ]
  }
};
```

### Final Decision Statement

For the enterprise requirement of enabling business-user discovery and consumption of data assets across AWS, Snowflake, MongoDB Atlas, Salesforce Data Cloud, and future multi-cloud sources, **Alation Cloud as the enterprise marketplace paired with AWS Glue as the AWS technical metadata repository** provides the optimal balance of multi-cloud coverage, business-user experience, and governance maturity.

---

## Document Information

- **Last Updated**: 2026-07-15
- **Repository**: staruser143/Technology-Talks
- **Path**: AWS/Data/ADR.md
- **Format**: Markdown
- **Status**: Ready for Review and Approval
