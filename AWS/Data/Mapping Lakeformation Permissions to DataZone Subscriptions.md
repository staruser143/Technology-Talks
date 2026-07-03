Here's how the two systems connect under the hood.

**The core mechanism: DataZone doesn't replace Lake Formation, it orchestrates it.** DataZone uses Lake Formation to manage permissions and facilitate sharing of data products, and when a producer makes data available for a subscription, it has to be registered in the Glue Data Catalog; when a subscription is granted, Amazon DataZone orchestrates the creation of Lake Formation grants. So a "subscription" in DataZone is really a business-friendly wrapper around a Lake Formation grant/revoke operation.

**Who does the granting.** DataZone creates and manages its own IAM roles for both producers and subscribers, and assumes these roles to grant or revoke Lake Formation permissions during the sharing process. Notably, when sharing data, DataZone only shares read-only Lake Formation permissions to consumer personas, so subscribers can query but not modify shared data — write/owner-level permissions stay with the producer project.

**The trigger and the object being granted.** Once a subscription request is approved, DataZone kicks off a fulfillment workflow that automatically adds the asset to every applicable environment within the subscribing project, by creating the necessary grants in Lake Formation (for Glue tables) or directly in Redshift (for Redshift assets) — which is what lets project members immediately query the asset via Athena or the Redshift query editor.

**Managed vs. unmanaged — this is the key branch point.** DataZone can only trigger this automated grant-creation for *managed assets*, defined as Lake Formation-managed Glue Data Catalog tables and Redshift tables/views. For anything else (an unmanaged asset — say, a JDBC source cataloged via crawler but never registered in Lake Formation), DataZone can't create the grant itself. Instead it publishes a "Subscription Request Created" (or "approved") event to EventBridge with the full payload, and it's on you to build a Lambda or Step Functions handler that does the actual grant outside of DataZone.

**What has to be true on the Lake Formation side beforehand.** For a Glue table to be eligible for automatic grants, the table must actually be Lake Formation-managed, and the IAM role DataZone uses for the publishing environment needs `DESCRIBE`/`DESCRIBE GRANTABLE` on the database and `DESCRIBE`, `SELECT`, `DESCRIBE GRANTABLE`, `SELECT GRANTABLE` on the table itself — DataZone essentially needs grantable permissions so it can re-grant them downstream to subscribers.

**Hybrid mode removed a lot of the friction.** Previously you had to fully onboard data to Lake Formation before DataZone could manage it. With Lake Formation hybrid access mode integration, DataZone can publish IAM-managed Glue tables directly — when a consumer subscribes, DataZone registers the table's S3 location in hybrid mode, adds the consumer's IAM role as an "opt-in principal," and grants access to that role through Lake Formation, all without you pre-registering anything.

**One nuance:** LF-TBAC (tag-based access control) isn't supported for DataZone-managed Glue assets — only the standard named-resource permission model.

So the mapping, end to end, looks like: **DataZone project/environment → an IAM role DataZone owns → that role receives a Lake Formation grant on the specific table → the grant is scoped read-only → subscribers in that project's environment can query it.** Revoking a subscription reverses the same chain.

