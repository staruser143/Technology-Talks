Handling AOR (Agent of Record) changes correctly is critical because an outdated AOR assignment in your search index could cause a broker to match against a prospect or member they no longer represent, violating your scoping rules.

The core challenge is that the search index is a separate materialized view of your data. When the AOR field changes in the source, the index must be updated—and for OpenSearch, this requires an explicit write to the search engine. Here is how to handle it in each system.

🍃 MongoDB Atlas Search: Leverage Views for Automatic Updates

If you use Atlas Search, the most robust approach is to create the search index on a MongoDB View that dynamically projects the current AOR relationship. Because Atlas Search indexes on views apply transformations at both replication-time and query-time, the search index automatically reflects the latest AOR state without you needing to manually update documents .

How it works:

1. Source Collection: Your enrollment collection contains the raw data, including the field that determines the current AOR (e.g., current_aor_id).
2. Create a View: Define a view using an aggregation pipeline. If your AOR relationship is stored in a separate collection (e.g., an aor_assignments collection), you would need to denormalize it first (since $lookup is not supported in views for search indexes ). If the AOR is a direct field in the enrollment document, the view can simply use $addFields or $set to ensure the field is correctly typed for filtering.
3. Index the View: Create your Atlas Search index on this view rather than the raw collection. Configure the aor_id field as a token or string type for filtering.
4. Outcome: When the AOR changes in the source, the view’s materialized result changes, and the Atlas Search index reflects this automatically. Your query applies the AOR filter inside the $search stage, and it will only match against the current AOR.

Caveat: If your AOR changes are high-frequency and you cannot model them as a simple field on the document, you may still need a Change Stream process to update a denormalized current_aor field on the enrollment document itself. However, once that field is updated, Atlas Search picks it up via its normal sync mechanism without a separate index update API call.

🔍 AWS OpenSearch: Explicit Updates via Change Streams

OpenSearch has no automatic link to your MongoDB source. When an AOR changes, you must explicitly update the document in the OpenSearch index. The standard pattern is a CDC pipeline (Change Streams → Lambda/ingestion tool) that propagates every change.

Handling AOR changes specifically:

1. Denormalize AOR into the Indexed Document: Your OpenSearch document should contain the aor_id (or aor_ids array) as a keyword field. This is the field you will filter on during identity matching.
2. Capture the AOR Change Event: Your Change Stream listener should watch for updates to the AOR assignment. This might be on the enrollment collection itself (if AOR is a field) or on a separate aor_assignments collection.
3. Trigger a Re-Index or Partial Update:
   · If AOR is a field on the enrollment document: Your CDC pipeline will naturally capture the update event. You must ensure the pipeline includes the full document (using fullDocument: "updateLookup" in Change Streams) so the latest aor_id is written to OpenSearch .
   · If AOR is in a separate collection: When an assignment changes, your pipeline must find the affected enrollment document(s) and issue an update to OpenSearch. You can use the Update By Query API to update all matching documents (e.g., all enrollments where member_id matches the reassigned member) . For bulk reassignments, use the slices parameter to parallelize the operation.
4. Idempotency: Use the MongoDB _id as the OpenSearch document _id. This ensures that if the AOR change event is processed multiple times, the final state in OpenSearch is consistent.

Alternative for Query-Time Scoping (Not Recommended for High Volume):
You could store the AOR relationship in OpenSearch as a separate document type and use a join field, but this adds query complexity and performance overhead. Denormalizing the aor_id into the main document is simpler and faster for filtering.

💡 Key Recommendation

If minimizing operational complexity is a priority, Atlas Search with a View is the cleaner solution because it eliminates the need for a separate sync pipeline to handle AOR changes. You define the "current AOR" logic once in the view, and the search index stays consistent automatically.

If you are already committed to OpenSearch, you must invest in a reliable CDC pipeline and explicitly handle AOR change events. Do not assume the index will eventually become consistent without a dedicated process to push those updates. Test your pipeline with a simulated AOR reassignment to ensure the old AOR filter no longer returns the prospect before the new AOR filter starts working.