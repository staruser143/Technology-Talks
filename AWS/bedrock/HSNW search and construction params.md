In the Hierarchical Navigable Small World (HNSW) algorithm, both ef_construction and ef_search define the size of the dynamic priority queue (the candidate list) used to explore neighbor nodes, but they operate at completely different lifecycle stages.
Parameter Breakdown
| Feature | ef_construction | ef_search |
|---|---|---|
| Lifecycle Phase | Index Build / Ingestion Time | Query / Runtime |
| Mutability | Immutable at index mapping; changing it requires a full re-index. | Dynamic; can be altered via cluster/index settings or overridden per query payload. |
| Primary Role | Controls the candidate search depth when finding neighbors to connect newly inserted vectors. | Controls the candidate search depth when traversing Layer 0 to retrieve the nearest neighbors to a query. |
| Typical Range | 128 – 512 (OpenSearch standard: 100–512) | 64 – 256 (OpenSearch standard: 16–100+) |
| Resource Trade-off | Higher values increase indexing CPU time and slow ingestion throughput, but build a higher-quality graph topology. Minimal direct RAM footprint impact. | Higher values increase search recall, but directly increase query latency and CPU overhead per search. |
ef_construction: Deep-Dive & Scenarios
When a new vector is added to the HNSW graph, the algorithm searches for the best candidate nodes to connect to it using bidirectional links (m). ef_construction defines how many nearest candidates are tracked in the priority queue during this insertion search.
 * A higher ef_construction constructs more accurate, well-connected shortcut paths across graph layers.
 * Once the index is built, ef_construction is locked. Setting it too low builds a brittle graph with poor recall that cannot be repaired without rebuilding the index.
When to use a High ef_construction (256 – 512+)
 * Static or Read-Heavy Knowledge Bases: When documents are indexed infrequently (e.g., standard RAG enterprise policies, product manuals) and searched millions of times, indexing speed does not matter, but retrieval accuracy is paramount.
 * Complex, Clustered, High-Dimensional Spaces: When vector distributions form tight, intricate semantic clusters where sub-optimal neighbor connections would cause searches to get trapped in local minima.
 * High Target Recall (>95–98%): If your downstream task relies on finding the absolute best nearest neighbors (e.g., legal or compliance document search).
When to use a Low/Moderate ef_construction (100 – 128)
 * High-Throughput Streaming Ingestion: Fast streaming write workloads (e.g., streaming social media, real-time logging, live CDC updates) where ingest throughput and indexing lag must be minimized to keep CPU usage sustainable.
 * Frequent Batch Re-indexing: Systems where indices are rebuilt frequently from scratch via nightly batch jobs and indexing runtime must fit within a tight maintenance window.
ef_search: Deep-Dive & Scenarios
ef_search controls the size of the dynamic candidate list evaluated when traversing the base layer (Layer 0) during a query.
 * Unlike ef_construction, ef_search has no impact on storage or index structure.
 * It must always be greater than or equal to k (the number of requested top results).
 * Because it is dynamically tunable per query, you can adapt the search depth based on user tier, latency budget, or query complexity.
When to use a High ef_search (128 – 256+)
 * High-Precision RAG Applications: Before feeding chunks into a foundation model context window, retrieving top-quality semantic matches prevents hallucinations. A modest latency increase (e.g., 10–20 ms) is acceptable compared to downstream LLM generation latency.
 * Post-Filtering or Filter-Aware k-NN: When applying metadata filters (e.g., filtering by tenant ID, department, or date). Because filtered nodes are skipped or eliminated from the final candidate pool, a larger queue is needed so the algorithm does not return fewer than k results or get trapped on sparse graph paths.
 * Offline Analysis / Forensic Search: Deep internal discovery, batch evaluation, or audit tooling where latency is not a factor and maximum recall is required.
When to use a Low ef_search (32 – 64)
 * Ultra-Low Latency SLAs (<10–20 ms): Real-time autosuggest, search-as-you-type, or high-concurrency recommendation services where responsiveness matters more than finding every single edge case.
 * Two-Stage Retrieval with Rerankers: If you are retrieving candidate chunks (e.g., top 50) and immediately passing them to a cross-encoder reranker model (like Cohere Rerank or Amazon Bedrock Rerank), the initial HNSW search only needs to produce a reasonable candidate pool, keeping search lightweight.
Example OpenSearch Configuration
1. Configuring ef_construction in the Mapping
Set during index creation as part of the knn_vector method definition:
PUT /enterprise-knowledge-base
{
  "settings": {
    "index.knn": true,
    "index.knn.algo_param.ef_search": 100
  },
  "mappings": {
    "properties": {
      "text_chunk": { "type": "text" },
      "embedding": {
        "type": "knn_vector",
        "dimension": 1024,
        "method": {
          "name": "hnsw",
          "space_type": "innerproduct",
          "engine": "faiss",
          "parameters": {
            "m": 16,
            "ef_construction": 256
          }
        }
      }
    }
  }
}

2. Overriding ef_search Dynamically at Query Time
Tune the search depth per request without touching index settings:
GET /enterprise-knowledge-base/_search
{
  "size": 5,
  "query": {
    "knn": {
      "embedding": {
        "vector": [0.018, -0.042, 0.091, 0.005],
        "k": 5,
        "method_parameters": {
          "ef_search": 128
        }
      }
    }
  }
}

Would you like to explore how adjusting the neighbor links parameter (m) interacts with both ef_construction and memory sizing?
