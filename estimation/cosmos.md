### Request Units/Second (RU/s) consumed depends on,
* Type of Operation:   **insert,update,delete,query**
* Size & Complexity of data


**OpType**      **RU/s Consumption (Approx)**

**Insert**      Depends on Item Size ; typically 5-10 RU for small items(~1 Kb)
**Update**      Similar to insert; if replacing the whole document, its like write
**Delete**      Usually cheaper;around 5 RU for small items

**Factors that influence RU/s usage**
**Item Size**    
  * Larger docs consume more RU/s
  * RU/s scales roughly linearly with size
**Query Complexity**
  * Simple Queries ( e.g point reads, by ID) are very cheap (~1 RU)
  * Complex Queries with filters,joins or aggregations can consume hundreds of RU/s
**Indexing**
  * CosmosDB automatically indexes all properties by default
  * Custom Index policies  can reduce RU/s for specific queries
**Partitioning**
  * Efficient Partitioning improves performance and reduces RU/s
  * Poor partitioning can lead to cross partition queries, which are more expensive
**Concurrency**
  * High Throughput scenarios (many ops per second) requiring provisioning more RU/s

Example Scenario
Scenario            Estimated RU/s
Insert 1 KB doc     ~5 RU
Update 1 kb doc     ~5-10 RU
Delete 1 kb doc     ~5 RU
Query By ID         ~1 RU
Query with filter on indexed field      ~10-50 RU
Query with aggregation across partitions - ~100+ RU

Tips to optimize RU/s usage
---------------------------
Use Point Reads instead of queries when possible
keep docs small and avoid deeply nested structures
Use custom indexing policies to reduce query costs
Monitor RU/s usage with Azure Monitor or Cosmos DB metrics

Unsure how many RU/s is needed
Start with Autoscale RU/s (Recommended for unpredictable workloads)
Autoscale automatically adjusts RU/s between minimum and maximum(100-400 RU/s)
You only pay for the actual RU/s you use, upto a maximum
Ideal for workloads with variable or unpredictable traffic

Use Azure Cosmos DB Capacity Planner
------------------------------------
based on,
Item Size,
No of Reads/Writes per second
Query Patterns
it gives ballpark estimate to help you provision resources

Start small and monitor
========================
Provision a modest 400 RU/s and use Azure Monitor or Cosmos DB Metrics to,
Track actual RU/s consumption
Identity Throttling (When RU/s is insufficent)
Scale up/down as needed

Use the SDK to measure RU/s per Operation
Cosmos DB SDKs return RU/s consumed per request
WE can log and anlyze this to understand workload's needs


response=container.upsert_item((item)
print("Request Charge:" ,response.headers['x-ms-request-charge']

Similar Workload in Test Environment
Create test container and simulate your expected workload


API Type
Core(SQL) (for Cosmos DB for NoSQL)
Item Characteristics
  Parameter                 value       notes
  Item size                  2 KB       
  properties per item       10
  Indexed properties        5


  workload details
    operation ,value,notes
    Readsper second, 100,
    writesper second,10
    deletes per second, 2

  Partitioning strategy
    PArtition key: cateogry or brand
    ensures even distribution of products to avoid hot partitions

  expected output from planner
  Estimated RU/s: ~800-1200 RU/s
  Storage:depends on the number of items (e.g  1million items, x 2 GB =~ 2GB)
  Cost: varies by region and whether we choose partitioned or autoscale throughput
  

