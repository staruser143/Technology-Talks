# Impact on Atlas Search and MongoDB DB processes
- If Atlas Search and the MongoDB database processes are co-located on the same nodes, there is indeed a performance impact due to resource contention.
- However, MongoDB provides a direct solution for this: dedicated Search Nodes, which separate the two workloads onto different hardware.

## The Impact of Co-located Deployment (Default for Small Clusters)

By default (especially on free, Flex, or lower-tier dedicated clusters), the search process (mongot) and the database process (mongod) run on the same node and share CPU, RAM, and I/O resources . This can lead to:

 **Resource Contention**:
- Heavy search queries or index building can compete for CPU and memory with your transactional writes and reads, potentially increasing database latency .
- **Performance Degradation**:
 If the search index is larger than the memory available to mongot, queries may read from disk, causing high page faults and slower responses .

## The Solution: Dedicated Search Nodes (Recommended for Production)

- For a production workload with millions of documents and identity matching, we should enable dedicated Search Nodes (available on M10+ clusters).
- This architecture physically separates the search index and query processing from the main database nodes .

The benefits directly address your concern:

**Workload Isolation**:
- Search queries and indexing no longer steal CPU cycles or memory from the critical write path (enrollment data ingestion) and read path (transactional lookups).
- MongoDB reports 40%–60% decreases in query time for complex search queries when using isolated nodes .
**Independent Scaling**: 
- We can scale the database cluster and the search cluster independently based on their specific needs 
**No ETL Overhead**:
-  Even with separate nodes, MongoDB automatically syncs data via change streams, so we avoid building a custom pipeline (unlike OpenSearch) while still getting hardware isolation .

## A Note on Query Design

- Independent of the hardware setup, we need to ensure the identity matching queries use the $search stage efficiently.
- Applying AOR filters inside the $search compound query (using filter or must clauses) rather than in a subsequent $match stage is critical.
- A $match after $search forces the engine to retrieve a massive result set first, which causes significant slowdowns and memory pressure .


## Bottom line:
-  For the scale we need , we should not  run Atlas Search on co-located nodes for production.
-  Use dedicated Search Nodes to eliminate the write/read contention while still avoiding the operational overhead of an external search engine.