# OpenSearch Index Alias
- An index alias in Amazon OpenSearch Service functions as an abstraction layer—a virtual pointer that maps to one or more physical indices. 
- When integrated with Amazon Bedrock Knowledge Bases, index aliases decouple the Bedrock Knowledge Base configuration from the physical OpenSearch index.

## Why Aliases Are Used with Bedrock Knowledge Bases
 * **Zero-Downtime Index Migrations & Schema Updates**:
   * Fundamental vector search settings—such as HNSW parameters (m, ef_construction), vector dimension sizes, or distance space metrics (cosinesimil vs. innerproduct)—are immutable once an index is created.
   * Changing these parameters requires building a new index and reindexing the data.
   * By pointing the Bedrock Knowledge Base configuration to an alias (e.g., kb-vectors-live) rather than a hardcoded index name (e.g., kb-vectors-v1), you can point the alias to a newly created and populated index without recreating or reconfiguring the Knowledge Base.
 * **Instant Rollback Safety**:
   * If a newly tuned index or updated vector mapping yields retrieval regressions, the alias can be atomically repointed back to the prior index version within seconds without interrupting production query traffic.
 * Segment Consolidation & Data Synchronization:
   * Ingestion workflows and heavy data mutations create Lucene segment fragmentation and tombstoned vectors.
   * When rebuilding indices to purge tombstones or optimize shard layout, swapping the alias post-sync ensures seamless transition between versions.

## How the Workflow Operates
 ```
 ┌─────────────────────────────────────────────────────────┐
 │             Amazon Bedrock Knowledge Base               │
 │           (Configured target: "kb-vectors-live")         │
 └────────────────────────────┬────────────────────────────┘
                              │
                    Points to Stable Alias
                              │
                              ▼
                   [ Alias: kb-vectors-live ]
                              │
            ┌─────────────────┴─────────────────┐
     Before Swap                        After Atomic Swap
            │                                   │
            ▼                                   ▼
 ┌──────────────────────┐            ┌──────────────────────┐
 │   kb-vectors-v1      │            │   kb-vectors-v2      │
 │ (Original HNSW Index)│            │ (Optimized / Retuned)│
 └──────────────────────┘            └──────────────────────┘
```
### 1. Knowledge Base Initialization
 * During Knowledge Base setup (or when configuring OpenSearch Service as the vector store), configure the Bedrock vector store indexName to use the alias name (e.g., kb-vectors-live).
 * In OpenSearch, map this alias to the initial physical index (e.g., kb-vectors-v1).
### 2. Reindexing to a New Target
 * When you need to adjust parameters (e.g., boosting ef_construction from 256 to 512 or re-running embeddings):
   * Create the new physical index kb-vectors-v2 with updated settings.
   * Populate kb-vectors-v2 using the OpenSearch _reindex API or an external data pipeline.
   * Validate retrieval accuracy and document counts on kb-vectors-v2.
### 3. Zero-Downtime Atomic Swap
 * Execute an atomic alias switch via the OpenSearch _aliases API:
 ```
POST /_aliases
```
```json
{
  "actions": [
    {
      "remove": {
        "index": "kb-vectors-v1",
        "alias": "kb-vectors-live"
      }
    },
    {
      "add": {
        "index": "kb-vectors-v2",
        "alias": "kb-vectors-live"
      }
    }
  ]
}
```

 * Because OpenSearch executes all actions in the actions array as a single atomic transaction, incoming queries from Bedrock's Retrieve or RetrieveAndGenerate APIs are redirected instantly with zero connection drops or 404 index errors.
## Key Operational Considerations
 * **Write Index Designation**: If an alias references multiple indices (such as in time-series rollovers or Index State Management setups), exactly one index must have "is_write_index": true. For Bedrock data ingestion syncs, ensure writes route to the active, write-designated index.
 * **Permissions & IAM Alignment**: Ensure the IAM execution role assumed by Amazon Bedrock has permissions covering both the alias and the underlying versioned index ARN patterns (e.g., arn:aws:es:*:*:domain/*/* or arn:aws:aoss:*:*:index/*/*).
 * **Field Mapping Parity**: The new index must keep identical vector and metadata field names (e.g., embedding_vector, text_chunk, and metadata attributes) as defined in Bedrock Knowledge Base ingestion and retrieval field mappings.
