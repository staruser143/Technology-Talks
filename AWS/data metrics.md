The architecture can support it, but it's important to separate two very different things that get lumped together as "usage metrics":

**Data lineage — this is a native, first-class DataZone capability.**
Data lineage in Amazon DataZone is an OpenLineage-compatible feature, now generally available, that captures and visualizes lineage events to trace data origins, track transformations, and view cross-organizational data consumption. The good news for our architecture specifically:

- Lineage can be set up to be automatically captured from AWS Glue and Amazon Redshift databases when added to Amazon DataZone — so if our assets are already flowing through the Glue Data Catalog and Redshift as we've architected, lineage capture is largely automatic, not something we have to bolt on separately.
- For Spark-based transformations, Glue ETL jobs (v5.0+) can be configured to send lineage events to DataZone via an OpenLineage Spark listener, giving us table- and column-level lineage through actual transformation logic, not just catalog metadata.
- Lineage is versioned with each event, letting we compare an asset's transformations across its history — useful for the "why did this number change" investigations, not just a static diagram.
- On the asset details page, we get a graphical upstream/downstream view, including column-level lineage — this is what a business user sees directly in the same Data Portal we've already mapped out.

**Query-count / "how many times was this queried" — this does NOT come from DataZone itself.**
This is the important nuance: DataZone sits at the catalog/subscription layer, not in the query execution path. It knows *who subscribed* and *when access was granted*, but the actual query execution happens in Athena or Redshift, outside DataZone's own telemetry. That said, DataZone does gives us **usage transparency at the subscription/asset level** — Amazon DataZone lets us provide transparency on data asset usage and monitor data assets across projects through usage auditing capabilities — meaning we can see subscriber counts, which projects consume an asset, and approval history natively. For actual *query-execution* counts, we need to pull from the layers underneath:

| Metric we want | Where it actually lives |
|---|---|
| Who subscribed / how many subscribers per asset | DataZone (native) |
| Data origin, transformations, upstream/downstream | DataZone Data Lineage (native) |
| Row/column-level access checks, permission audit trail | AWS CloudTrail — Lake Formation provides comprehensive audit logs with CloudTrail to monitor access and demonstrate compliance, tracking data interactions by role and user |
| Athena query count/history per table | Athena's `ListQueryExecutions` API / CloudWatch metrics, cross-referenced against Glue table names in the query text |
| Redshift query count per table | Redshift system tables (`SYS_QUERY_HISTORY`/`STL_QUERY`) or Redshift audit logging to CloudWatch/S3 |
| Aggregated, SQL-queryable audit trail across all of the above | CloudTrail Lake — a managed, SQL-queryable event data store that can aggregate CloudTrail events across accounts/regions with up to 7-year retention |
| Dashboard/report view counts | QuickSight's own usage/activity metrics |

**The practical pattern:** most organizations who want a genuine "asset X was queried Y times by Z distinct users last month" metric build a lightweight aggregation layer — CloudTrail (or CloudTrail Lake) capturing Lake Formation and Glue data-access events, joined with Athena/Redshift query logs, rolled up into a small Glue table or QuickSight dashboard. This sits *alongside* DataZone rather than inside it, but it plugs cleanly into the same architecture we've already built, since the Lake Formation grants we mapped out are exactly what CloudTrail is auditing.

