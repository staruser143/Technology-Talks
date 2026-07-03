## AWS Options for Business-User Data Discovery & Self-Service Access to the Data Lake

### 1. Amazon DataZone (the primary purpose-built answer)
This is AWS's dedicated service for exactly this use case — giving business users a catalog to browse, request, and consume data governed centrally. Key pieces:

- **Business Data Catalog** – a federated registry where technical metadata (from Glue Data Catalog, Redshift, S3, on-prem/third-party sources) is enriched with business names, descriptions, glossaries, and metadata forms so non-technical users can search in business terms rather than table/column names. Faceted search works on top of the business data catalog to help data consumers and producers find data assets using familiar structural information, such as table and column names, as well as business terms.
- **Automated/AI-assisted cataloging** – Amazon DataZone announced the general availability release of the new generative AI-based capability to improve data discovery, data understanding and data usage by enriching the business data catalog, allowing data producers to generate comprehensive business data descriptions and context, highlight impactful columns, and include recommendations on analytical use cases with a single click.
- **Data Products** – producers can group data assets into defined packages tailored for specific business use cases, so consumers can subscribe to all assets within a data product through a single approval workflow instead of requesting each asset separately.
- **Data quality visibility** – consumers can see data quality metrics from AWS Glue data quality or third-party systems as they search for assets, so they can trust the data before requesting it.
- **Self-service subscription/approval workflow** – once assets are published to the catalog, domain users can discover them, request and gain access by subscribing to an asset on behalf of a project; the asset owner is notified and can approve or reject the request, and on approval DataZone runs a fulfillment workflow that automatically creates the necessary grants in AWS Lake Formation or Amazon Redshift.
- **Data Portal** – a browser-based web application where users can catalog, discover, govern, share, and analyze data in a self-service fashion, authenticated via IAM Identity Center or IAM credentials.
- **Consumption tools** – once subscribed, users access and analyze data via Amazon Athena or Amazon Redshift query editors. DataZone also connects out to QuickSight for visualization, and it makes data accessible to everyone in the organization by connecting people and data through shared tools to drive business insights.

**A practical caveat**: for lake data to actually be queryable after approval, it must be Lake Formation-managed. If a JDBC connection is set up and the Glue Data Catalog is filled via a crawler without Lake Formation management, that dataset is "unmanaged" — users can request access but DataZone's fulfillment process won't grant query access, only metadata visibility. When a project is created, DataZone automatically sets up two Athena databases — a "_pub_db" for producers to share tables and a "_sub_db" holding data the user has subscribed to; if the data sits in S3 and is Lake Formation-managed, subscribers can query it via Athena.

### 2. Underlying/supporting services (what DataZone sits on top of)
- **AWS Glue Data Catalog** – the technical metadata store (schemas, table definitions) that DataZone, Athena, Redshift Spectrum, and EMR all read from.
- **AWS Lake Formation** – handles the actual fine-grained (table/column/row-level) permission grants and centralized access control on S3 data; DataZone leverages Lake Formation for fine-grained data access controls and, per the latest updates, integrates in "hybrid mode."
- **Amazon Athena / Amazon Redshift** – the actual query/consumption engines business/analyst users hit once access is granted.
- **Amazon QuickSight** – for users who want dashboards/visual exploration rather than writing SQL.

### How it typically fits together
1. Data engineers register data in S3 → cataloged via Glue crawlers → access governed by Lake Formation.
2. Data owners publish curated assets (with business context) into the DataZone catalog, optionally packaged as Data Products.
3. Business users browse/search the DataZone Data Portal using business terms, see data quality scores and descriptions, and request (subscribe to) an asset.
4. Owner approves → Lake Formation grants are auto-provisioned → user queries via Athena/Redshift or visualizes in QuickSight.

This is essentially AWS's version of a **data marketplace / data mesh** pattern — DataZone functions like a marketplace for your organization's data, where producers publish datasets with descriptions and metadata and consumers browse, request access, and subscribe, with an approval workflow in the middle for governance.

