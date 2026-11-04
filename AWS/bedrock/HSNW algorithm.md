Hierarchical Navigable Small World (HNSW) is an approximate nearest neighbor (ANN) graph algorithm designed to find vectors similar to a query vector in sub-linear time (O(\log N)) instead of performing an exhaustive linear scan (O(N)) across the entire dataset.
Core Concept: Multi-Layer "Skip-List" Graph
HNSW adapts the idea of a 1D skip-list into a multi-dimensional geometric graph structure:
 * Hierarchical Layers: The index consists of multiple stacked graph layers:
   * Top Layers: Contain very few nodes connected by long-range links ("express highways").
   * Bottom Layer (Layer 0): Contains all indexed vectors connected via dense, localized, short-distance links ("local neighborhood streets").
 * Probability Distribution: When a new vector is added, it is assigned a maximum layer height probabilistically, ensuring that higher layers remain exponentially sparser than lower layers.
Layer 2 (Express):     [A] ──────────────────────────► [G]
                        │                               │
Layer 1 (Regional):    [A] ──────────► [D] ──────────► [G]
                        │               │               │
Layer 0 (All Vectors): [A] ──► [B] ──► [C] ──► [D] ──► [E] ──► [F] ──► [G]

How Search (Querying) Works
Traversing an HNSW graph employs a top-down greedy search:
 * Top-Layer Entry: The search starts at a predetermined global entry point in the topmost sparse layer.
 * Greedy Traversal: At the current layer, the algorithm measures the distance from the query vector to the current node’s neighbors and hops greedily to whichever neighbor is closest to the query.
 * Layer Transition: Once no neighboring node in the active layer is closer to the query than the current node (a local minimum), the search drops down to the corresponding node in the layer directly below.
 * Refinement at Layer 0: This top-down descent repeats until the search reaches Layer 0. At Layer 0, the algorithm evaluates candidates using a priority queue bounded by the search depth parameter (ef_search) to assemble the final top-k nearest neighbors.
How Construction (Indexing) Works
 * Assign Maximum Layer: For each newly inserted vector, a maximum layer l is drawn from a decaying probability distribution.
 * Find Insertion Points: Starting at the top layer, the algorithm greedily traverses downward to find the closest entry point at layer l.
 * Link Formation: From layer l down to Layer 0, the new node connects to its closest neighbors. The candidate search during indexing is bounded by ef_construction.
 * Neighbor Pruning: Each node connects to a maximum of m bidirectional links. If a node exceeds m edges, a heuristic prunes less optimal links while maintaining clustering diversity.
Key Hyperparameters
| Hyperparameter | Scope | Role & Trade-offs | Typical Range |
|---|---|---|---|
| m | Index Build (Immutable) | Maximum bidirectional connections per node. Higher values boost graph connectivity and search recall, but increase memory footprint (~8 \times m bytes per vector) and build time. | 16–64 |
| ef_construction | Index Build (Immutable) | Priority queue size evaluated when finding nearest neighbors during indexing. Higher values construct higher-quality graphs and improve query recall at the cost of slower ingestion throughput. | 128–512 |
| ef_search | Query Time (Dynamic) | Priority candidate queue size used during runtime search on Layer 0. Higher values explore deeper graph paths to improve recall, but increase search latency. | 64–256 |
Operational Considerations
 * RAM Residency: HNSW graphs must reside entirely in memory for fast random-access traversal; uncompressed 32-bit floating-point vectors consume significant RAM.
 * Logical Deletes & Tombstones: In vector databases and search engines (such as OpenSearch via Apache Lucene), deletions and updates create tombstones. The graph must still traverse these tombstoned paths during search until an explicit segment consolidation or force-merge purges them.
