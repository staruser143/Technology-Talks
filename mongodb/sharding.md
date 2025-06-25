# Sharding
Sharding is a database architecture pattern used to improve scalability and performance by splitting a large database into smaller, more manageable pieces called shards. Each shard holds a portion of the data, and together they make up the complete dataset.

# Here's how it works:
**Horizontal Partitioning**:
Sharding typically involves horizontal partitioning, where rows of a table are distributed across multiple database instances.

**:Independent Shards**::
Each shard operates independently and can be hosted on separate servers.

**:Shard Key**::
A shard key is used to determine which shard a particular piece of data belongs to.

**:Example:**:
Imagine a user database with millions of users. Instead of storing all users in one database:

Users with IDs 1–1,000,000 go to Shard A
Users with IDs 1,000,001–2,000,000 go to Shard B
And so on...

**:Benefits:**:
**:Scalability:**: You can handle more data and traffic by adding more shards.

**:Performance:**: Queries are faster because each shard contains less data.

**:Availability**:: If one shard fails, others can still function.

**:Challenges:**:

**:Complexity:**: Managing multiple shards adds complexity.

**:Data Distribution:**: Choosing the right shard key is critical.

**:Joins and Transactions**:: Cross-shard operations can be difficult and slow.



Each box represents a shard, containing a specific range of user IDs. This shows how a large dataset is split across multiple shards to improve scalability and performance.

## MongoDB implements sharding natively to handle large datasets and high throughput operations. Here's a breakdown of how it works:

### Core Components of MongoDB Sharding

## Shard

A shard is a MongoDB server or replica set that holds a subset of the data.
Each shard contains a portion of the sharded data.

## Config Servers

These store metadata and configuration settings for the cluster.
They track the location of data across shards.

## Query Router (mongos)

Acts as an interface between client applications and the sharded cluster.
It routes queries to the appropriate shard(s) based on the shard key.

## Shard Key
A shard key is a field in the documents used to determine how data is distributed across shards.
Choosing the right shard key is crucial for performance and even data distribution.

```javascript
db.users.createIndex({ userId: 1 })
sh.enableSharding("myDatabase")
sh.shardCollection("myDatabase.users", { userId: 1 })
```

### Steps to Implement Sharding in MongoDB
#### Start Config Servers

```sh
mongod --configsvr --replSet configReplSet --port 27019 --dbpath /data/configdb
```

## Initiate Config Server Replica Set
```javascript
rs.initiate({
  _id: "configReplSet",
  configsvr: true,
  members: [{ _id: 0, host: "localhost:27019" }]
})
```

## Start Shard Servers
```sh
mongod --shardsvr --replSet shardReplSet1 --port 27018 --dbpath /data/shard1
```

## Start mongos Router
```sh
mongos --configdb configReplSet/localhost:27019 --port 27017
```

## Connect to mongos and Add Shards
```sh
sh.addShard("shardReplSet1/localhost:27018")
```

## Enable Sharding on Database and Collection

```sh
sh.enableSharding("myDatabase")
sh.shardCollection("myDatabase.users", { userId: 1 })
```
# In the example I see we have an index om userId field. Also, we are sharing the collection on userId field. Does that mean each userId would be in a separate shard which can be a separate server

### Does Each userId Go to a Separate Shard?

No, each individual userId does not go to a separate shard. Instead:

MongoDB uses the shard key (in this case, userId) to determine the range or hash of values that go into each shard.

### A range-based sharding strategy might assign:
userId 1–1,000,000 to Shard A
userId 1,000,001–2,000,000 to Shard B

### A hashed sharding strategy would hash the userId and distribute documents more evenly across shards.

### Why Create an Index on the Shard Key?

MongoDB requires an index on the shard key to efficiently route queries and distribute data.

The index helps the mongos router quickly locate which shard contains the data for a given key.


### Summary
Concept	Explanation
Shard Key	               :Field used to split data across shards (e.g., userId)
Index on Shard Key	      :Required for efficient query routing
Data Distribution	        :Based on ranges or hashes of the shard key
One userId per shard?	    ❌ No — multiple userIds are grouped into shards

## Where is the sharding strategy defined, range based or hashed
In MongoDB, the sharding strategy—whether range-based or hashed—is defined at the time you shard a collection using the sh.shardCollection() command.

### 🔧 How to Define the Sharding Strategy
#### ✅ Range-Based Sharding (Default)
This is the default strategy. Documents are distributed based on contiguous ranges of the shard key.

```sh
sh.shardCollection("myDatabase.users", { userId: 1 })
```

userId: 1 indicates ascending order, which implies range-based sharding.
Good for queries that target specific ranges (e.g., time series data).

### Hashed Sharding
This strategy uses a hashed value of the shard key to distribute documents evenly across shards.
```
sh.shardCollection("myDatabase.users", { userId: "hashed" })
```

Ensures more uniform distribution of data.

Ideal when your queries are randomly distributed across the key space.

### When to Use Which?
Strategy                  	Best For
Range-Based	              Range queries, time-based data
Hashed	                  Uniform distribution, high write throughput

### Range-Based Sharding
-Data is split into contiguous ranges of userId.

-Useful for range queries but can lead to uneven distribution if data is skewed.


### 🟩 Hashed Sharding

Data is distributed based on a hash of the userId.

Ensures more uniform distribution across shards, ideal for high-throughput and random-access workloads.

# Range queries are database queries that retrieve records where a field's value falls within a specified range. They're commonly used with range-based sharding because the data is organized in contiguous blocks.

### 🔍 Example of a Range Query
Suppose you have a collection of users with a userId field:
```
db.users.find({ userId: { $gte: 1000, $lte: 2000 } })
```

This query retrieves all users whose userId is between 1000 and 2000, inclusive.

## Common Use Cases
### Date ranges: Find orders placed between two dates.

```
db.orders.find({ orderDate: { $gte: ISODate("2025-01-01"), $lte: ISODate("2025-01-31") } })
```

### Numeric ranges: Retrieve products priced between $100 and $500.
### Alphabetical ranges: Get names starting between "A" and "M".

## Why It Matters in Sharding

Range-based sharding keeps related data together, making range queries efficient.

Hashed sharding, on the other hand, scatters data randomly, making range queries slower because they may need to scan multiple shards.

## When should a hashed sharding be preferred

### When to Use Hashed Sharding

High Write Volume

Writes are evenly distributed across shards, reducing the chance of any one shard becoming a bottleneck.

Random Access Patterns

If queries target individual records by exact match (e.g., userId = 12345), hashed sharding ensures uniform access.

Avoiding Hotspots

### Range-based sharding can lead to hotspots if many inserts fall into the same range (e.g., time-based data).
### Hashed sharding avoids this by scattering data.

Uniform Load Distribution

Ideal for workloads where data and queries are evenly spread across the key space.

Scalability

Easier to scale horizontally since data is already balanced across shards.

### When Not to Use Hashed Sharding
If your application relies heavily on range queries (e.g., date ranges, numeric ranges), hashed sharding is inefficient because it may require scanning multiple shards.


#### Example Use Case
A social media app where users are accessed by userId and most queries are exact matches

```
db.users.find({ userId: 123456 })
```
Hashed sharding ensures that these queries are fast and evenly distributed.


#### How to Use This Chart
Use Range-Based Sharding when your application relies on range queries or time-series data.
Choose Hashed Sharding when you need even data distribution, high write throughput, or want to avoid hotspots.

## Can shard key be based on more than one field

Yes, in MongoDB, a shard key can be composed of multiple fields—this is called a compound shard key.

###🔧 Example of a Compound Shard Key

```
sh.shardCollection("myDatabase.orders", { customerId: 1, orderDate: 1 })
```

This shards the orders collection using a combination of customerId and orderDate.

### ✅ Benefits of Compound Shard Keys
More control over data distribution: You can fine-tune how data is partitioned.


Improved query targeting: If your queries often filter by both fields, this can improve performance.

Avoids hotspots: Especially useful when one field alone would lead to uneven distribution.

### Considerations

Field order matters: MongoDB uses the fields in the order they are defined in the shard key.

Query patterns must align: To benefit from targeted queries, your queries should include the prefix of the shard key fields.

Example Query That Benefits

Given the shard key { customerId: 1, orderDate: 1 }:

```
db.orders.find({ customerId: 12345, orderDate: { $gte: ISODate("2025-01-01") } })
```

This query can be routed efficiently to the correct shard(s).

## how MongoDB handles queries that don’t include the shard key?

When a query does not include the shard key, MongoDB faces challenges in efficiently routing the query. Here's what happens:

Query Without Shard Key
Example:
```
db.orders.find({ orderDate: { $gte: ISODate("2025-01-01") } })
```

If the shard key is { customerId: 1, orderDate: 1 }, this query does not include customerId, the first field in the shard key.

### What MongoDB Does
The mongos router cannot determine which shard contains the relevant data.
It performs a scatter-gather query:
Sends the query to all shards.
Each shard executes the query and returns results.
Mongos merges the results.



### ⚠️ Downsides of Scatter-Gather
Performance hit: All shards are queried, increasing latency.
Resource usage: More CPU and memory consumed across the cluster.
Scalability issues: As data grows, scatter-gather becomes less efficient.

## Best Practice
Always include the prefix of the shard key in your queries to enable targeted routing.

# What does it mean by using prefix of shard key in queries

In MongoDB, using the prefix of the shard key means including the first field(s) of a compound shard key in your query. This is essential for targeted query routing.

### What Is a Shard Key Prefix?
If your shard key is:
```
{ customerId: 1, orderDate: 1 }
```

Then:
```
The prefix is { customerId }
```

A query must include customerId to be targeted to the correct shard.

### Examples of Queries Using the Prefix

```
db.orders.find({ customerId: 12345 }) // ✅ Uses prefix
db.orders.find({ customerId: 12345, orderDate: { $gte: ISODate("2025-01-01") } }) // ✅ Uses full shard key
```

These queries can be routed directly to the correct shard.

### ❌ Examples That Don’t Use the Prefix

```
db.orders.find({ orderDate: { $gte: ISODate("2025-01-01") } }) // ❌ Missing prefix
```

MongoDB will perform a scatter-gather query across all shards.

## Does that mean use either the prefix or both shard key fields in the queries

Exactly! In MongoDB, when using a compound shard key, your queries should include:

### ✅ Either:
The prefix of the shard key (i.e., the first field)

### ✅ Or:
All fields in the shard key
🔑 Why This Matters

MongoDB uses the prefix fields to determine which shard holds the relevant data. If your query includes:

### Only the first field → MongoDB can route the query to the correct shard.
### All fields → Even better, more precise routing.
### Only the second field or later → ❌ MongoDB cannot route efficiently; it will perform a scatter-gather.


#### Example

### Shard key: { customerId: 1, orderDate: 1 }

Query	                                      Efficient Routing?	        Reason
{ customerId: 123 }	                        ✅ Yes	                      Uses prefix
{ customerId: 123, orderDate: "2025-06-01" }✅ Yes	                    Uses full shard key
{ orderDate: "2025-06-01" }	                ❌ No                    	Missing prefix

## Sharding mandatory or optional?

### Sharding in MongoDB is optional, not mandatory.

#### 🟢 When Sharding Is Not Needed
Your dataset fits comfortably on a single server.
You have low to moderate read/write throughput.
Your queries are simple and don’t require horizontal scaling.
MongoDB works perfectly well as a non-sharded replica set in these cases.

#### When Sharding Is Recommended
Your data size exceeds the capacity of a single server.
You need to scale horizontally across multiple machines.
You experience performance bottlenecks due to high traffic.
You want to distribute data geographically or by workload.

#### Summary
Scenario	                                      Sharding Needed?
Small app with few users	                    ❌ No
Large-scale app with millions of users	      ✅ Yes
High write throughput                        	✅ Yes
Complex analytics across large datasets      	✅ Yes

## Is indexing mandatory on sharded fields or optional

In MongoDB, indexing on sharded fields is mandatory—but with some nuances depending on the type of sharding:

###🔹 Single-Field Shard Key
When you shard a collection on a single field (e.g., userId), MongoDB automatically creates an index on that field if it doesn't already exist.

```
sh.shardCollection("myDatabase.users", { userId: 1 })
```

MongoDB creates an index on { userId: 1 } if it's not present.


### Compound Shard Key
If you're using a compound shard key (e.g., { customerId: 1, orderDate: 1 }), MongoDB requires that an index exists on exactly that compound key.

```
db.orders.createIndex({ customerId: 1, orderDate: 1 })
sh.shardCollection("myDatabase.orders", { customerId: 1, orderDate: 1 })
```



If the index is missing, the shardCollection command will fail.

Summary
Shard Key Type	                Is Index Required?	Automatically Created?
Single Field	                  ✅ Yes	          ✅ Yes (if missing)
Compound Key	                  ✅ Yes	          ❌ No (must be created manually)

## Here’s a checklist to help you ensure proper indexing when working with sharded collections in MongoDB:

###✅ Shard Key Indexing Checklist
####🔹 For Single-Field Shard Keys
 Choose a field with good cardinality (e.g., userId)
 MongoDB will auto-create an index on the shard key if it doesn’t exist
 
####🔸 For Compound Shard Keys
 Manually create an index on the exact compound key (e.g., { customerId: 1, orderDate: 1 })
 Ensure the field order matches the shard key definition
 Avoid using fields with low cardinality as the first field
 
####⚠️ Before Running sh.shardCollection()
 Confirm the index exists on the shard key
 Validate that the shard key supports your query patterns
 Ensure the shard key is immutable (cannot be updated)

 ## Does sharding increase the costs

 Yes, sharding can increase costs, though it also brings significant benefits in scalability and performance. Here's a breakdown of the cost implications:

### 💸 Why Sharding Increases Costs
#### 1.More Infrastructure

Each shard is typically a separate server or replica set.
You also need config servers and mongos routers.
More machines = higher hardware or cloud costs.

#### 2. Operational Complexity

Managing a sharded cluster requires more effort in terms of:
Monitoring
Backups
Maintenance
This can increase DevOps and administrative costs.

#### 3.Network Overhead

Cross-shard queries and data movement (e.g., chunk migrations) can increase network traffic, especially in cloud environments.

#### 4.Storage Overhead

Metadata and indexes are stored on each shard.
Some duplication of data may occur depending on your setup.

## ✅ When the Cost Is Justified
Your application cannot scale vertically (i.e., a single server isn't enough).
You need high availability and geographic distribution.
The cost of downtime or poor performance is higher than the cost of sharding.

Yes, even when using MongoDB Atlas, sharding can increase costs—but the cost structure is different compared to self-managed deployments.

##💰 Cost Implications of Sharding in MongoDB Atlas

## ✅ What Atlas Handles for You
Automatic provisioning of shards, config servers, and routers.
Built-in monitoring, backups, and scaling.
Simplified management, reducing DevOps overhead.
## 🔺 Where Costs Can Increase
More Nodes = Higher Cost

Each shard is a replica set, so more shards = more nodes = higher cost.
You pay per node, including storage, compute, and backup.
Cluster Tier

Sharded clusters require M30 or higher tiers.
Entry-level shared clusters (M0–M20) do not support sharding.
Data Transfer

Cross-region or cross-shard queries may incur network egress charges.
Autoscaling

While convenient, autoscaling can lead to unexpected cost spikes if not monitored.

Factor	                        Self-Managed	          MongoDB Atlas
Hardware Cost	                  You manage	          Atlas charges per node
Ops Overhead	                    High	            Low
Scaling	                        Manual	            Automated
Cost Predictability	          Variable	            More predictable, but can grow with usage




 



