# Enabling Business-User Discovery & Self-Service Consumption of Data Assets in the AWS Data Lake

*Architecture, Solution Options, Implementation Challenges, and Recommendations*

**Analysis & Recommendation Document**
Prepared: July 2026

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Context & Objective](#2-business-context--objective)
3. [Solution Options](#3-solution-options)
4. [End-to-End Architecture](#4-end-to-end-architecture)
5. [Subscription & Approval Workflow](#5-subscription--approval-workflow)
6. [How DataZone Constructs Map to Lake Formation Permissions](#6-how-datazone-constructs-map-to-lake-formation-permissions)
7. [Business User Identity & Access](#7-business-user-identity--access)
8. [Infrastructure-as-Code: Provisioning via Terraform](#8-infrastructure-as-code-provisioning-via-terraform)
9. [Implementation Challenges & Mitigations](#9-implementation-challenges--mitigations)
10. [Recommendations](#10-recommendations)
11. [Conclusion](#11-conclusion)

---

## 1. Executive Summary

Business users today need a governed, self-service way to discover, understand, and consume curated data assets that live in the AWS data lake — without depending on data engineering teams for every request, and without compromising security or compliance controls. AWS provides a purpose-built pathway for this through Amazon DataZone, layered on top of AWS Glue Data Catalog and AWS Lake Formation for technical metadata and fine-grained access control, and integrated with AWS IAM Identity Center for identity federation.

This document consolidates the architecture, workflows, identity model, infrastructure-as-code approach, and implementation challenges for standing up this capability, and closes with a prioritized set of recommendations. It reflects a solution architecture reviewed end-to-end: from raw data sources, through cataloging and governance, into a business-friendly catalog with subscription-based access requests, through to actual query consumption — plus how all of it can be provisioned via Terraform in environments where console access is restricted.

> **Key Recommendation:** Adopt Amazon DataZone as the primary business-facing data catalog and subscription layer, backed by AWS Lake Formation for enforcement and AWS IAM Identity Center for federated identity, and provision all four layers via Terraform using a phased rollout that proves the pattern in a console-accessible environment before promoting to environments where Terraform is the only provisioning path.

---

## 2. Business Context & Objective

Organizations that centralize data in an AWS data lake (Amazon S3, cataloged via AWS Glue) frequently face a discovery and access gap: technical metadata exists, but business users have no easy way to search for data in business terms, understand data quality or ownership, or request access without opening a ticket to a data engineering team. The objective is to close that gap with:

- A searchable, business-friendly catalog of available data assets.
- A self-service request-and-approval workflow that doesn't bypass governance.
- Automatic, auditable provisioning of the underlying access grants once approved.
- A consumption layer (query engines and BI tools) that business users can use immediately after approval.
- An infrastructure-as-code approach consistent with the organization's environment-promotion model (console access in DEV only; Terraform-only in higher environments).

---

## 3. Solution Options

AWS offers a layered set of services that, combined, address the discovery-to-consumption lifecycle. Each layer has a distinct role; none of them substitutes for the others.

### 3.1 AWS Glue Data Catalog — Technical Metadata

The foundational, technical metadata store: schemas, table definitions, and partitions for data in Amazon S3. It is read by Athena, Redshift Spectrum, EMR, and DataZone alike. It has no business-user-facing interface on its own — it is the substrate, not the discovery layer.

### 3.2 AWS Lake Formation — Governance & Enforcement

Provides centralized, fine-grained access control (table, column, and row level) over data cataloged in Glue. It is the enforcement point: every grant that a business user ultimately receives, whether initiated manually or automatically through DataZone, is a Lake Formation permission (or, for hybrid-mode assets, an IAM-based opt-in principal registration).

### 3.3 Amazon DataZone — Business Catalog & Self-Service Layer

This is AWS's purpose-built answer to the discovery-and-consumption problem. Its core capabilities:

- **Business Data Catalog** — federated metadata from Glue, Redshift, and other sources, enriched with business names, glossaries, and descriptions so users can search in business language.
- **AI-assisted cataloging** — generative-AI features that auto-generate business descriptions, highlight impactful columns, and suggest analytical use cases.
- **Data Products** — curated bundles of assets that consumers subscribe to as a single unit rather than requesting each table individually.
- **Data quality visibility** — surfaces AWS Glue Data Quality (or third-party) metrics directly in search results.
- **Subscription & approval workflow** — the self-service request mechanism, detailed in Section 5.
- **Data Portal** — the browser-based application business users actually interact with, authenticated via IAM Identity Center or federated IAM.

### 3.4 Consumption Tools

Once a subscription is approved and the underlying grant is created, business users consume data through:

- **Amazon Athena** — serverless SQL query engine, the default for ad hoc analysis on approved S3/Glue assets.
- **Amazon Redshift Query Editor** — for warehouse-native assets and heavier analytical workloads.
- **Amazon QuickSight** — dashboards and visual exploration for users who prefer not to write SQL.

### 3.5 AWS IAM Identity Center — Identity & Federation

Business users authenticate to the DataZone data portal via IAM Identity Center rather than as native IAM users. Identity Center can use its built-in directory or federate an external IdP (Okta, Entra ID, etc.) via SAML/SCIM. IAM-based access is technically possible but is scoped to the domain's own AWS account and is best reserved for service accounts or administrative access, not a general business-user population.

---

## 4. End-to-End Architecture

The layers above compose into a single end-to-end flow, from raw data source to a business user running a query:

1. Data is registered in Amazon S3 and cataloged via AWS Glue crawlers into the Glue Data Catalog.
2. AWS Lake Formation is layered on top for centralized, fine-grained permission management.
3. Data owners publish curated assets — individually or packaged as Data Products — into the Amazon DataZone business catalog, adding business context and glossary terms.
4. Business users authenticate into the DataZone Data Portal (via IAM Identity Center) and search the catalog using business terminology, reviewing data quality signals as they go.
5. A user requests access by subscribing to an asset or data product; the asset owner is notified.
6. The owner approves or rejects the request through the DataZone subscription workflow.
7. On approval, DataZone automatically creates the necessary Lake Formation grants (for Glue/S3 assets) or Redshift grants (for warehouse assets), and provisions the asset into the subscribing project's environment.
8. The user queries the data via Amazon Athena or Amazon Redshift, or visualizes it in Amazon QuickSight.

This flow was mapped as a layered architecture diagram earlier in this engagement, showing sources feeding the Glue/Lake Formation governance layer, which feeds the DataZone business catalog, which business users interact with directly, terminating in the consumption tools.

### 4.1 A Practical Caveat: Managed vs. Unmanaged Assets

For lake data to be queryable immediately after subscription approval, the underlying asset must be Lake Formation-managed (a managed Glue table, or a Redshift table/view). If a data source is registered in Glue via a crawler but never brought under Lake Formation management — an "unmanaged" asset — DataZone's automatic fulfillment cannot create a grant. The asset remains discoverable and requestable, but query access does not follow automatically; this is addressed further in Section 6.

---

## 5. Subscription & Approval Workflow

The request-and-approval mechanism is the governance control point in this architecture, and it follows a consistent path regardless of asset type:

| Step | What Happens | System of Record |
|---|---|---|
| 1. Subscribe | Business user requests access to an asset from the Data Portal. | DataZone |
| 2. Request created | A subscription request is created and tracked. | DataZone / EventBridge |
| 3. Owner notified | The asset owner receives a notification to review. | DataZone |
| 4. Owner decision | Owner approves or rejects the request. | DataZone |
| 5a. If approved | DataZone assumes an IAM role and creates a Lake Formation grant (managed assets) or Redshift grant. | Lake Formation / Redshift |
| 5b. If rejected | Requester is notified; no grant is created. | DataZone |
| 6. Environment update | The asset is added to the subscribing project's environment (e.g. the Athena `_sub_db`). | DataZone |
| 7. Consumption | User queries the asset via Athena, Redshift, or QuickSight. | Athena / Redshift / QuickSight |

For unmanaged assets, DataZone instead publishes a "Subscription Request Created/Approved" event to Amazon EventBridge with the full request payload; a custom Lambda or Step Functions handler must be built to complete the grant outside of DataZone's automation.

---

## 6. How DataZone Constructs Map to Lake Formation Permissions

DataZone does not replace Lake Formation — it orchestrates it. Understanding this mapping is essential for troubleshooting access issues and for designing the Terraform provisioning model.

| DataZone Construct | Lake Formation / IAM Effect |
|---|---|
| Project environment | DataZone creates and owns IAM roles for producers and subscribers within that environment. |
| Subscription approved | DataZone assumes its own IAM role to initiate the grant — the business user's identity is never used directly. |
| Managed asset (Glue/Redshift) | DataZone automatically creates a Lake Formation grant (or Redshift grant). |
| Unmanaged asset | DataZone publishes an EventBridge event; grant creation is manual/custom. |
| Grant scope | Subscriber grants are read-only; the producer project retains broader/write permissions. |
| Lake Formation hybrid mode | Allows DataZone to register an IAM-managed table's S3 location and add the subscriber's role as an opt-in principal at subscribe-time, without pre-registration in Lake Formation. |

> **Note:** LF-TBAC (tag-based access control) is not supported for DataZone-managed Glue assets — only the standard named-resource permission model applies.

---

## 7. Business User Identity & Access

Business users should not be provisioned as individual native IAM users or roles. The recommended and AWS-designed path is federated SSO through AWS IAM Identity Center:

- An external identity provider (Okta, Microsoft Entra ID, etc.) federates into IAM Identity Center via SAML, with users and groups kept in sync via SCIM.
- IAM Identity Center is enabled for the DataZone domain using either implicit assignment (all directory users get access) or explicit assignment (specific users/groups are added) — this choice is **permanent** once made at the domain level and should default to explicit assignment for governed environments.
- DataZone maps the authenticated identity to a user profile, associated with domain units.
- Project membership (owner, contributor, or viewer) — assigned within DataZone, not IAM — determines what the user can actually do.
- At query time, the Data Portal requests short-lived, scoped IAM session credentials on the user's behalf; the business user never interacts with IAM directly.

Direct IAM-user federation into the Data Portal is technically supported (via the `datazone:GetIamPortalLoginUrl` action) but is scoped only to principals within the domain's own AWS account, making it impractical for a distributed business-user population — it is best reserved for service accounts or administrative/API access.

---

## 8. Infrastructure-as-Code: Provisioning via Terraform

Given the environment model in place — console access limited to DEV, Terraform-only provisioning for all higher environments — every layer of this architecture needs a Terraform-manageable path. This is supported today, with one notable gap.

| Architecture Layer | Terraform Resource(s) | Provider | Notes |
|---|---|---|---|
| Glue Data Catalog | `aws_glue_catalog_database`, `aws_glue_catalog_table`, `aws_glue_crawler` | aws | Mature, long-standing support. |
| Lake Formation setup | `aws_lakeformation_resource`, `aws_lakeformation_data_lake_settings` | aws | Requires careful admin/grantee principal separation. |
| Lake Formation grants | `aws_lakeformation_permissions` | aws | Pipeline execution role needs explicit grant/revoke IAM permissions. |
| DataZone domain & project | `aws_datazone_domain`, `aws_datazone_project` | aws | Resource-identity support added recently. |
| DataZone environment & profile | `aws_datazone_environment`, `aws_datazone_environment_profile`, `aws_datazone_environment_blueprint_configuration` | aws | — |
| DataZone glossary & metadata | `aws_datazone_glossary`, `aws_datazone_glossary_term`, `aws_datazone_form_type`, `aws_datazone_asset_type`, `aws_datazone_user_profile` | aws | — |
| DataZone subscription target / data source | `awscc_datazone_*` (Cloud Control API) | awscc | Gap in the standard aws provider; use awscc as the fallback. |
| Identity Center permission sets & assignment | `aws_ssoadmin_permission_set`, `aws_ssoadmin_account_assignment` | aws | — |
| Identity Center directory objects | `aws_identitystore_user`, `aws_identitystore_group` | aws | Only relevant if not using external-IdP SCIM sync. |

### 8.1 Provider Choice Guidance

Where a resource exists in both the `aws` and `awscc` providers, prefer `aws` for consistency and community support; fall back to `awscc` (generated from CloudFormation's Cloud Control API) only for resources not yet covered — currently, DataZone subscription targets and data sources fall into this category.

---

## 9. Implementation Challenges & Mitigations

| # | Challenge | Impact | Recommended Mitigation |
|---|---|---|---|
| 1 | Unmanaged assets cannot receive automatic grants on subscription approval. | Business users see "approved" but cannot query the data; support tickets and confusion follow. | Enforce Lake Formation management (or hybrid mode registration) as a publishing prerequisite; build and test the EventBridge + Lambda fallback for any assets that must remain unmanaged. |
| 2 | Terraform's `aws` provider lacks a subscription-target resource. | Inconsistent provisioning approach across DataZone objects; harder-to-maintain modules. | Isolate the gap to a single `awscc`-based submodule with clear documentation; track the open GitHub issue for when native `aws` support lands and migrate later. |
| 3 | Lake Formation grant/revoke requires specific IAM permissions on the Terraform execution role; admins and grantees must not overlap. | `AccessDeniedException` failures during apply, with no console access to diagnose in higher environments. | Bake required `lakeformation:*Grant*`/`*Revoke*` and `glue:GetTable`/`GetDatabase` permissions into the pipeline role from day one; keep the pipeline role out of the Lake Formation admin list; validate the full permission model in DEV before promotion. |
| 4 | IAM Identity Center's assignment mode (implicit vs. explicit) is a permanent, domain-level choice. | Choosing implicit assignment early can inadvertently expose the catalog to the entire directory, with no way to reverse it later. | Default to explicit assignment for any domain governing sensitive data; document the decision and rationale before the domain is created. |
| 5 | No console access to inspect Lake Formation or DataZone state in higher environments. | Debugging failed grants or subscriptions is significantly harder without visual inspection. | Use the `aws_lakeformation_permissions` data source and AWS CLI/API calls (read-only) from within the pipeline or a bastion role to inspect state; build CloudWatch/EventBridge-based visibility into subscription and grant events. |
| 6 | LF-TBAC (tag-based access control) is not supported for DataZone-managed Glue assets. | Organizations standardized on tag-based policies cannot use that model for DataZone-published data. | Standardize on named-resource Lake Formation permissions for any asset intended for DataZone publication; reserve LF-TBAC for assets outside the DataZone catalog if still required elsewhere. |
| 7 | Direct IAM-user federation into the Data Portal only works for principals within the domain's own AWS account. | Not viable as the primary access path for a distributed, multi-account business-user population. | Standardize on IAM Identity Center federation for all business users; reserve direct IAM federation strictly for service accounts or administrative/API access. |

---

## 10. Recommendations

### 10.1 Architecture

1. Adopt Amazon DataZone as the single business-facing catalog and subscription layer; do not build a parallel custom catalog.
2. Require Lake Formation management (including hybrid mode where applicable) for any asset intended for business-user publication, to guarantee automatic grant fulfillment.
3. Package related assets as DataZone Data Products where consumption patterns naturally group multiple tables, to reduce subscription friction.

### 10.2 Governance

1. Default to explicit user assignment for IAM Identity Center integration with the DataZone domain.
2. Maintain a clear separation between Lake Formation administrator principals and the principals that receive scoped grants, including the Terraform execution role itself.
3. Surface Glue Data Quality metrics in the catalog from the outset, so business users can assess trust before requesting access.

### 10.3 Infrastructure-as-Code

1. Build Terraform modules per architecture layer (Glue, Lake Formation, DataZone, Identity Center), using the `aws` provider as the default and `awscc` only where required (currently: subscription targets/data sources).
2. Grant the Terraform pipeline execution role the full set of Lake Formation grant/revoke and Glue read permissions upfront, rather than discovering gaps via failed applies in an environment without console access.
3. Prove every module end-to-end in DEV (where console verification is available) before promoting to higher environments.
4. Build lightweight, read-only observability (CLI/API checks, EventBridge-driven logging) into the pipeline so higher-environment issues can be diagnosed without console access.

### 10.4 Rollout Sequencing

1. **Phase 1 — Foundation:** Glue Data Catalog + Lake Formation resource registration and admin model, validated in DEV.
2. **Phase 2 — Identity:** IAM Identity Center integration and DataZone domain creation with explicit assignment.
3. **Phase 3 — Catalog:** DataZone projects, environments, glossary, and initial curated data products published from managed assets only.
4. **Phase 4 — Self-Service:** Enable the subscription/approval workflow for a pilot business-user group; monitor grant fulfillment end-to-end.
5. **Phase 5 — Scale:** Extend to additional business units, address any remaining unmanaged-asset cases, and promote the full Terraform module set to higher environments.

---

## 11. Conclusion

Amazon DataZone, layered on AWS Glue Data Catalog and AWS Lake Formation, and federated through AWS IAM Identity Center, provides a complete, AWS-native path from data lake to business-user consumption — discovery in business terms, governed self-service subscription, automatic permission fulfillment, and immediate query access via Athena, Redshift, or QuickSight. The entire stack is provisionable via Terraform, with only a small, well-defined gap (DataZone subscription targets) requiring the `awscc` provider as an interim measure. The challenges identified — around unmanaged assets, permission model separation, identity assignment permanence, and reduced console visibility in higher environments — are all addressable with the mitigations and phased rollout outlined above, without requiring a departure from the organization's existing environment-promotion model.
