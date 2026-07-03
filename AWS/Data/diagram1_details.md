The flow reads top to bottom: raw sources feed into the technical layer (Glue Data Catalog + Lake Formation), which publishes into DataZone's business catalog. Business users browse and request access there (dashed arrow = subscription request going up), and once approved, they query the data through Athena, Redshift, or QuickSight.

A couple of details worth calling out that don't fit cleanly in the boxes:

- The **solid down-arrow and dashed up-arrow** between DataZone and business users represent the two-way interaction: discovering/browsing flows down, subscription requests flow up to the asset owner for approval.
- The **grant fulfillment** step (Lake Formation actually creating the underlying permissions once a subscription is approved) happens behind the scenes inside the "subscription workflow" box — it's not a separate visible hop, but it's what makes the data queryable afterward.
- If a source is registered in Glue but *not* Lake Formation-managed, it shows up in the catalog as "unmanaged" — visible in the business catalog, but the approval workflow can't actually grant query access, only metadata visibility.

