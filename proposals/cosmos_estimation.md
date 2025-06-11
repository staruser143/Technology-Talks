To estimate costs for **Azure Cosmos DB for NoSQL**, Microsoft provides a detailed guide and tools. Here's how you can approach it:

### 🔹 1. Use the Azure Cosmos DB Capacity Planner
This is the primary tool for estimating costs based on your workload. It helps you calculate the required **Request Units per second (RU/s)** and associated costs.

- **Basic Mode**: Quick, high-level estimates using default settings.
- **Advanced Mode**: More detailed estimates where you can adjust:
  - Indexing policy
  - Consistency level
  - Multi-region writes
  - Analytical store usage

🔗 [Use the Capacity Planner](https://learn.microsoft.com/en-us/azure/cosmos-db/nosql/estimate-ru-with-capacity-planner) [1](https://learn.microsoft.com/en-us/azure/cosmos-db/nosql/estimate-ru-with-capacity-planner)

---

### 🔹 2. Understand the Pricing Models
Azure Cosmos DB offers two main pricing models:

- **Provisioned Throughput**: You reserve RU/s and are billed for the provisioned amount, regardless of usage.
- **Serverless**: You pay per operation, ideal for unpredictable or low-traffic workloads.

🔗 [Plan and manage costs](https://learn.microsoft.com/en-us/azure/cosmos-db/plan-manage-costs) [2](https://learn.microsoft.com/en-us/azure/cosmos-db/plan-manage-costs)

---

### 🔹 3. Key Inputs for Estimation
When using the planner, you'll need to provide:

- **API type** (choose NoSQL)
- **Number of regions**
- **Multi-region writes** (on/off)
- **Data size (GB)**
- **Item size and request patterns** (reads/writes per second)

---

### 🔹 4. Monitor and Optimize
After deployment, use **Azure Cost Management** to:

- Set budgets
- Monitor usage trends
- Forecast future costs

🔗 [Optimize your costs](https://learn.microsoft.com/en-us/azure/cosmos-db/optimize-costs) [3](https://learn.microsoft.com/en-us/azure/cosmos-db/optimize-costs)

---

Would you like help walking through the capacity planner with your specific workload details? I can guide you step-by-step or help simulate an estimate.



Great! Let's walk through the **Azure Cosmos DB Capacity Planner** together to estimate your NoSQL costs. I’ll guide you step-by-step. To begin, I need a few details about your workload:

### 🔹 Step 1: Basic Setup
1. **API Type**: We'll use **NoSQL**.
2. **Regions**:
   - How many Azure regions will your database be replicated to?
   - Will you enable **multi-region writes**?

### 🔹 Step 2: Data Characteristics
3. **Total data size** (in GB):
4. **Average item size** (in KB or bytes):
5. **Number of items** (if known):

### 🔹 Step 3: Request Patterns
6. **Reads per second**:
7. **Writes per second**:
8. **Query operations per second** (if applicable):

### 🔹 Step 4: Additional Options
9. Do you plan to use the **analytical store**?
10. What **consistency level** do you plan to use? (e.g., Session, Strong, Eventual)

Once you provide these details, I’ll simulate the estimate for you and explain the cost breakdown. Let’s start with what you know—feel free to leave any field blank if you're unsure, and I’ll help you estimate it.


Thanks for the details! Based on what you've shared, here's how we can estimate your **Azure Cosmos DB for NoSQL** costs using the capacity planner logic.

---

### 🔹 Summary of Your Workload

| Parameter                        | Value                        |
|----------------------------------|------------------------------|
| **Region**                       | Single region                |
| **Multi-region writes**          | No                           |
| **Average item size**            | 10 KB                        |
| **Reads per second**             | 1,000                        |
| **Writes per second**            | 1,000                        |
| **Query operations per second**  | 500                          |
| **Analytical store**             | No                           |
| **Consistency**                 | Strong (writes), Eventual (reads) |

---

### 🔹 Estimating Request Units (RU/s)

Here’s a rough estimate of RU/s based on typical Cosmos DB behavior:

- **Point Read (1 KB)** ≈ 1 RU  
  → For 10 KB items: **~10 RU per read**  
  → 1,000 reads/sec × 10 RU = **10,000 RU/s**

- **Write (1 KB)** ≈ 5 RU  
  → For 10 KB items: **~50 RU per write**  
  → 1,000 writes/sec × 50 RU = **50,000 RU/s**

- **Query**: Highly variable, but let’s assume **15 RU per query**  
  → 500 queries/sec × 15 RU = **7,500 RU/s**

**🔸 Total Estimated RU/s: ~67,500 RU/s**

---

### 🔹 Cost Estimation (Provisioned Throughput)

- **Provisioned RU/s**: 67,500
- **Single region**: Approx. **\$0.008 per RU/s per hour**

**Monthly Cost**:
- 67,500 RU/s × \$0.008 × 730 hours ≈ **\$3936/month**

> 💡 This is a rough estimate. Actual costs may vary based on indexing, query complexity, and usage patterns.

---

Would you like me to generate a downloadable cost breakdown or simulate different scenarios (e.g., serverless pricing or reduced throughput)?

Yes, **indexing in Azure Cosmos DB** does have an impact on costs, but it's not billed separately as a line item. Instead, it affects the **Request Units (RU/s)** consumed by your operations, which in turn affects your overall cost.

### 🔹 How Indexing Affects Costs

1. **Write Operations**:
   - Every write (insert, update, delete) triggers indexing.
   - The more complex your indexing policy (e.g., indexing all paths, composite indexes), the **higher the RU cost per write**.

2. **Query Operations**:
   - A well-optimized index can **reduce RU consumption** for queries.
   - If your query requires a full scan due to missing indexes, RU usage increases.

3. **Indexing Policy**:
   - You can customize the indexing policy to:
     - **Exclude certain paths** (to reduce write costs)
     - **Use lazy indexing** (delays index updates to reduce immediate RU usage)
     - **Disable indexing** (for write-heavy, read-light workloads)

---

### 🔹 Cost Optimization Tips

- **Exclude unused fields** from indexing.
- Use **included/excluded paths** to fine-tune what gets indexed.
- Consider **manual indexing** if you have predictable query patterns.
- Use **composite indexes** for multi-property queries to reduce RU usage.

Would you like help designing an optimized indexing policy for your workload? I can show you examples or simulate the RU impact of different policies.

Yes, **storage costs in Azure Cosmos DB** are **charged separately** from the RU/s (Request Units per second) costs.

---

### 🔹 Storage Cost Breakdown

1. **Data Storage**:
   - You are billed for the **total amount of data stored**, including:
     - Documents
     - Indexes
     - Metadata
   - **Rate**: As of the latest pricing, it's approximately **\$0.25 per GB per month** for standard (SSD-backed) storage in a single region.

2. **Index Storage**:
   - Indexes are stored alongside your data and **contribute to the total storage size**.
   - The more fields you index, the more storage is used.

3. **Backup Storage**:
   - **Automatic backups** are included, but **long-term retention** or **manual backups** may incur additional costs.

---

### 🔹 Example Estimate

If you store:
- 100 GB of data (including indexes)
- In a single region

**Monthly storage cost** ≈ 100 GB × \$0.25 = **\$25/month**

---

Would you like help estimating your storage size based on item size and count, or simulating how indexing affects storage? I can calculate that for you.

---

Yes, by **default**, Azure Cosmos DB **indexes all fields** in every item in a container. This is known as the **automatic indexing policy**.

---

### 🔹 Default Indexing Behavior

- **All properties** of all items are indexed.
- This allows for **fast and flexible queries** without needing to define indexes manually.
- The default policy uses **consistent indexing**, meaning indexes are updated synchronously with writes.

---

### 🔹 Implications

- ✅ **Pros**:
  - No need to manage indexes manually.
  - Queries on any field are supported out of the box.

- ⚠️ **Cons**:
  - **Higher RU consumption** for write operations.
  - **Increased storage usage** due to indexing all fields.

---

### 🔹 Custom Indexing Policy

You can override the default by defining a **custom indexing policy** to:

- **Exclude specific paths** (fields) from indexing.
- Use **lazy indexing** (asynchronous updates).
- Define **composite** or **spatial indexes** for advanced scenarios.

Here’s a simple example of a custom policy that excludes a field:

```json
{
  "indexingMode": "consistent",
  "includedPaths": [
    {
      "path": "/*"
    }
  ],
  "excludedPaths": [
    {
      "path": "/largeBlobField/*"
    }
  ]
}
```

Would you like help crafting a custom indexing policy for your workload? I can generate one based on your data model.

====
Based on your Cosmos DB workload, here's how you can compare it with **MongoDB Atlas** pricing:

---

### 🔹 Your Workload Recap

- **Single region**
- **10 KB item size**
- **1,000 reads/sec**
- **1,000 writes/sec**
- **500 queries/sec**
- **No analytical store**
- **Strong consistency for writes, eventual for reads**

---

### 🔹 MongoDB Atlas Pricing Overview

MongoDB Atlas offers **dedicated clusters** with pricing based on:

- **Cluster tier** (e.g., M10, M20, M30, etc.)
- **vCPUs, RAM, and storage**
- **Cloud provider and region**
- **IOPS and storage speed (optional upgrades)**

#### Example Cluster Tiers (Single Region, AWS/Azure/GCP):

| Tier | RAM | vCPUs | Storage | Price (approx) |
|------|-----|--------|---------|----------------|
| M30  | 8 GB | 2      | 40 GB   | \$0.54/hr (~\$394/month) |
| M40  | 16 GB | 4     | 80 GB   | \$1.04/hr (~\$759/month) |
| M50  | 32 GB | 8     | 160 GB  | \$2.00/hr (~\$1,460/month) |

> These prices include compute, storage, and IOPS within the tier limits [1](https://www.mongodb.com/pricing).

---

### 🔹 Estimating for Your Workload

Given your **high throughput (2,500 ops/sec)** and **10 KB item size**, you'd likely need at least an **M50 or M60** tier to handle the load with low latency and high availability.

- **M50**: \$2.00/hr → ~\$1,460/month
- **M60**: \$3.95/hr → ~\$2,883/month

This is **comparable to Cosmos DB’s \$3,936/month** estimate for 67,500 RU/s.

---

### 🔹 Key Differences

| Feature                  | Cosmos DB                          | MongoDB Atlas                     |
|--------------------------|------------------------------------|-----------------------------------|
| **Billing Model**        | RU/s-based                         | Cluster-based (compute + storage) |
| **Indexing**             | Automatic (customizable)           | Automatic (customizable)          |
| **Consistency**          | Tunable per operation              | Tunable per read/write            |
| **Scaling**              | Per-container                      | Per-cluster                       |
| **Serverless Option**    | Yes                                | Flex Tier (limited)               |

---

Would you like a side-by-side cost comparison chart or a downloadable summary? I can also help simulate different cluster sizes or workloads in MongoDB Atlas.

Based on your workload, **Azure Cosmos DB** appears to be **slightly more expensive** than **MongoDB Atlas**, but the difference depends on several factors:

---

### 🔹 Estimated Monthly Costs for Your Workload

| Platform         | Estimated Monthly Cost | Notes |
|------------------|------------------------|-------|
| **Cosmos DB**    | ~$3,936                | Based on 67,500 RU/s |
| **MongoDB Atlas**| ~$1,460–$2,883         | Based on M50–M60 cluster |

---

### 🔹 Key Cost Drivers

| Factor               | Cosmos DB                          | MongoDB Atlas                     |
|----------------------|------------------------------------|-----------------------------------|
| **Billing Model**    | RU/s (provisioned throughput)      | Cluster-based (compute + storage) |
| **Write Cost**       | Higher due to indexing and RU model| Lower unless write-heavy workload |
| **Scaling**          | Per-container                     | Per-cluster                       |
| **Storage**          | \$0.25/GB/month                    | Included in cluster tier          |
| **Indexing**         | All fields by default              | All fields by default             |

---

### 🔹 Summary

- **Cosmos DB** is more predictable and granular in scaling but can be **costlier for high-throughput workloads** due to RU-based billing.
- **MongoDB Atlas** is often **more cost-effective** for similar workloads, especially if you can optimize cluster size and indexing.

Would you like a **visual comparison chart** of these costs and features? I can generate one for clarity.


Great question! When we say **Azure Cosmos DB offers more predictable and granular scaling**, here's what that means:

---

### 🔹 **Predictable Scaling**

Cosmos DB uses a **Request Unit (RU/s)** model, where:

- You **provision exactly how much throughput** (RU/s) you need.
- You are billed **based on what you provision**, not what you use (unless you're on serverless).
- This makes costs **highly predictable**—you know your monthly bill if your RU/s stays constant.

✅ **Example**: If you provision 10,000 RU/s, you know you'll pay around \$584/month (single region), regardless of whether you use all of it or not.

---

### 🔹 **Granular Scaling**

Cosmos DB allows you to:

- Scale **per container** (e.g., per collection or table), not just per cluster.
- Adjust RU/s in **small increments** (e.g., 100 RU/s at a time).
- Apply **auto-scaling** to containers individually.

This means you can **fine-tune performance and cost** at a very detailed level.

---

### 🔸 In Contrast: MongoDB Atlas

- You scale by **cluster tiers** (e.g., M10 → M20 → M30).
- Each tier includes a fixed amount of CPU, RAM, and storage.
- You **can’t scale just one collection**—you scale the whole cluster.
- Costs jump in **larger steps** when you move to a higher tier.

---

### 🔹 Summary

| Feature                  | Cosmos DB                         | MongoDB Atlas                    |
|--------------------------|-----------------------------------|----------------------------------|
| **Scaling Unit**         | Per container (fine-grained)      | Per cluster (coarse-grained)     |
| **Billing Model**        | RU/s (predictable)                | Cluster tier (less granular)     |
| **Auto-scaling**         | Per container                     | Per cluster                      |

Would you like a visual chart comparing these scaling models? I can generate one for you.

If your application **exceeds the provisioned RU/s** in Azure Cosmos DB, here's what happens:

---

### 🔹 1. **Requests Are Throttled (Rate-Limited)**

- Cosmos DB will **not allow more throughput** than what you've provisioned.
- Excess requests are **throttled** and return an HTTP **429 (Too Many Requests)** status code.
- The response includes a **`Retry-After`** header, telling the client how long to wait before retrying.

---

### 🔹 2. **Client SDKs Handle Retries Automatically**

- Most Cosmos DB SDKs (e.g., for .NET, Java, Node.js) have **built-in retry logic**.
- They will **automatically retry** throttled requests after the `Retry-After` duration.
- However, this can increase **latency** and **application response time**.

---

### 🔹 3. **Options to Handle Throttling**

#### ✅ **Auto-Scale RU/s**
- Cosmos DB can automatically scale RU/s up to **10× the baseline**.
- Example: If you set 10,000 RU/s, it can scale up to 100,000 RU/s during spikes.
- You pay only for the **RU/s used**, not the max.

#### ✅ **Manual Scaling**
- You can manually increase RU/s via the portal, CLI, or SDKs.

#### ✅ **Queue and Retry**
- Use a **message queue** (e.g., Azure Service Bus) to buffer requests and retry later.

#### ✅ **Optimize RU Usage**
- Reduce RU consumption by:
  - Optimizing queries
  - Reducing indexed fields
  - Using point reads instead of queries

---

Would you like a diagram or flowchart showing how Cosmos DB handles throttling and retries? I can generate one for you.


Great question! Let's break it down:

---

### 🔹 What is a **Point Read** in Azure Cosmos DB?

A **point read** is the **most efficient and lowest-cost operation** in Cosmos DB. It retrieves a single item by its:

- **`id`** (the unique identifier of the item)
- **`partition key`** (used to locate the item in the correct partition)

#### ✅ Example:
```json
GET /dbs/mydb/colls/mycoll/docs/mydocid
```

This operation is **direct**, doesn't require a query engine, and typically costs **1 RU** for items up to 1 KB.

---

### 🔹 What is a **Query**?

A **query** is used when you want to:

- Retrieve **multiple items**
- Filter on **non-ID fields**
- Use **SQL-like syntax** (e.g., `SELECT * FROM c WHERE c.status = 'active'`)

Queries are **more flexible** but also **more expensive** in terms of RU consumption, especially if:

- The query scans many items
- The fields are not indexed
- The result set is large

---

### 🔹 Should You Use Point Reads Instead of Queries?

| Use Case                        | Recommendation         |
|----------------------------------|-------------------------|
| You know the `id` and `partition key` | ✅ Use **point read** |
| You need to filter/search items | ❌ Use **query**        |
| You want to reduce RU costs     | ✅ Prefer **point reads** where possible |
| You need to retrieve multiple items by condition | ❌ Use **query** or batch |

---

### 🔹 Optimization Tip

If your application frequently retrieves items by ID and partition key (e.g., user profiles, product details), **design your data model** to take advantage of point reads. This can **significantly reduce costs** and improve performance.

Would you like a visual comparison chart between point reads and queries? I can generate one for you.


Yes, to perform a **point read** in Azure Cosmos DB, you **must provide both**:

1. **`id`** – the unique identifier of the item.
2. **`partition key`** – the value used to distribute data across partitions.

---

### 🔹 Why Both Are Required

Cosmos DB uses a **partitioned architecture** for scalability. The `partition key` helps Cosmos DB:

- Locate the **physical partition** where the item resides.
- Perform a **direct lookup** instead of scanning multiple partitions.

Without both values, Cosmos DB cannot efficiently locate the item, and a **query** would be required instead, which is more expensive.

---

### 🔹 Example: Point Read

```csharp
container.ReadItemAsync<MyItem>(
    id: "user123",
    partitionKey: new PartitionKey("India")
);
```

Here, `"user123"` is the `id`, and `"India"` is the partition key value.

---

### 🔸 If You Only Have the `id`?

- You **cannot** do a point read.
- You must use a **query** like:
  ```sql
  SELECT * FROM c WHERE c.id = 'user123'
  ```
  But this will **scan all partitions**, increasing RU cost and latency.

---

Would you like a visual diagram showing how point reads work vs. queries in Cosmos DB?


Here’s a visual diagram comparing **Point Reads** and **Queries** in Azure Cosmos DB:

![Diagram comparing point reads and queries in Cosmos DB](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA+gAAAJYCAYAAADxHswlAAAAOXRFWHRTb2Z0d2FyZQBNYXRwbG90bGliIHZlcnNpb24zLjkuMiwgaHR0cHM6Ly9tYXRwbG90bGliLm9yZy8hTgPZAAAACXBIWXMAAA9hAAAPYQGoP6dpAACxj0lEQVR4nOzdd1QU19sH8O9Sli4CUoRIEQugInax0VSk2LtGxW7Err/YoogmFjSWxIIV7LH3FlQsiagYe48FjQVQlIhgodz3D8/O68rSFGQ13885ew57586dZ3aX2X3m3rkjE0IIEBEREREREVGx0ijuAIiIiIiIiIiICToRERERERGRWmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk5ERERERESkBpigExEREREREakBJuhEREREREREaoAJOhEREREREZEaYIJOREREREREpAaYoBMRERERERGpASboRERERERERGqACToRERERERGRGmCCTkRERERERKQGmKATERERERERqQEm6ERERERERERqgAk6ERERERERkRpggk6kpuzt7SGTySCTyTBp0qTiDuer4OnpKb2mQUFBxR0OEREREZESJuhE+XTkyBEpufvwYWhoCBcXFwwePBh37twp7lBViouLU4r5yJEjBVo/p/3X1NREyZIlUb16dYwePRrx8fFFswNERGro4sWLCA4ORpUqVVCyZEnI5XJYWlrCy8sLYWFhSE5OLu4QiYjoC6JV3AEQfQ1SU1Nx7do1XLt2DStWrMCOHTvQuHHjT2pz/Pjx+PfffwEA9erVK4wwi0RWVhb+/fdfnDt3DufOncOqVatw+vRplClTprhDIyIqMhkZGfj+++8xZ86cbMsSExORmJiII0eOYMaMGVizZg38/PyKIUoiIvrSMEEn+kgdO3ZEzZo18fbtW8TExGD37t0AgLS0NHTr1g1xcXHQ0dH56Pb79u1bWKEWCcX+v3jxAtu3b8elS5cAAPHx8ZgzZw5mz55dzBESERWdYcOGYcGCBdJzGxsbdOjQAWZmZrh8+TI2bdqEzMxMPHv2DC1btsTBgwfRqFGjYoxYtdevX0NTUxPa2trFHQoREYFD3Ik+WrNmzTBq1CiMGzcOu3btQteuXaVl8fHx+PPPP5Xqx8bGolu3brC3t4eOjg6MjIxQ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1apVq1S7dm15eHioatWqDnczzJo1S126dJEkhYWF2W+XnjVrVpbzc+XKFc2YMUPdu3e3h8uDBw/aP2AZMWKEfZyIiAh9//33stlsmj9/foaxPvvsM9lsNm3ZssU+T15eXtq9e7eaNWumwoULy8/PT88//3yG39PMbnE/c+aMXnnlFZUvX15ubm4qUaKEWrdurX379tn7jBgxQvXr15ePj4+KFi2q2rVra8aMGcrrQzwOHjwoZ2dnjRkzJsN7GzZskM1m06JFi7IdI33+8lOdOnUkScePH7/psXr06KGzZ8/qiy++uOmxAAAA8oqADtwClStX1iOPPKK5c+cqJSXF4b2YmBiVLl1aLVu2lCRFRkbqxRdfVPPmzbVkyRJNnjzZ/qzm6wPH0aNH1bNnT3Xv3l0rVqzQoEGDNGTIEJ0+fVrz5s1z6Ltnzx7Fxsbqueeey7bWuXPnqkWLFipatKhmz56thQsXysfHRy1btrSH9GHDhumTTz6R9L/bi9NvRX/77bft1xUXF5ftLdgREREaMmSI6tatqwULFuiLL75Q+/bts73VPC0tTR06dNDYsWPVvXt3LV++XGPHjtV3332n0NBQXbx40aH/zp079corr+ill17S0qVL9dBDD6lfv37asGGDpGtfQXj//fclSZ988oni4uIUFxenNm3aZFnDjz/+qFOnTiksLMzeFhAQoFWrVkm69oFM+jjDhg1TkyZNVKtWLfuc/dOkSZNUt25d1a1b196WkpKi1q1bq1mzZlqyZImef/55TZs2Td26dcuyJkk6d+6cHnnkEU2bNk19+vTRN998o6lTp6py5co6evSovd/BgwcVGRmphQsX6uuvv1anTp00ePBgjRo1Ktvxr1e2bFm1b99eU6dOVWpqaobrKlWqlDp27JinMfPDgQMHJF37e3ez/P39VbVqVS1fvvymxwIAAMgzA+CWiImJMZLM119/bW/btWuXkWTeeustY4wxcXFxRpIZP368w7F//vmn8fDwMEOHDrW3hYSEGElmzZo1Gc4VEhJigoODHdoGDhxoihYtas6dO5dljRcuXDA+Pj6mXbt2Du2pqammZs2apl69eva22NhYI8ksWrQo0+vcsmWLQ3t4eLgJCgqy/7xhwwaHa89KSEiICQkJsf88f/58I8l89dVXDv22bNliJJnJkyfb24KCgoy7u7s5dOiQve3ixYvGx8fHREZG2tsWLVpkJJnY2Nhsa0n3wQcfGEnm2LFjDu2JiYlGkhk+fHiGY9LnZfv27fa2+Ph4I8nMnj3b3hYeHm4kmYkTJzocP3r0aCPJ/PDDDw7XFx4ebv955MiRRpL57rvvcnUdxlz7vU1JSTEjR440vr6+Ji0tzf7e9XNvjMlwfel/DhYvXmxvO3z4sHF2djYjRozIdR3GGFOjRo0M58tO+rkXLFhgUlJSTHJystm4caOpUqWKqV69ujl9+rRD/5CQEFOjRo1Mx8ru965Hjx6mZMmSebgSAACA/MEKOpBLV69edXiZHG4P7tq1q4oUKeJwe/XMmTNls9nUp08fSdKyZctks9nUs2dPh7H9/f1Vs2bNDJuP3XfffWratGmGcw0ZMkQ7duzQxo0bJUlnz57VnDlzFB4eLi8vryxr3LRpk5KSkhQeHu5w/rS0NLVq1UpbtmzJtx2tV65cKUk5ruhfb9myZfL29la7du0cagwODpa/v3+GOQoODlZgYKD9Z3d3d1WuXFmHDh264dqPHDkim82m4sWL5/qYp59+WiVKlHBYRY+KipKfn1+mK+M9evRw+Ll79+6SpNjY2CzPsXLlSlWuXFnNmzfPtpa1a9eqefPmKlasmJycnOTi4qJ33nlHp06d0okTJ3J9TdK1ryDUrFnT4bqmTp0qm82mAQMG5GmsG9WtWze5uLjYv45x9uxZLV++XN7e3vkyfokSJXTixAldvXo1X8YDAADILQI6kAsHDx6Ui4uLw2v9+vXZHuPp6amnnnpKq1at0rFjx3T16lXNnTtXISEh9g3Ujh8/LmOMSpYsmWH8zZs36+TJkw5jBgQEZHquDh06qGzZsvbQNGvWLF24cCHHMJx+C33nzp0znP+DDz6QMUZJSUm5mqOcJCYmysnJSf7+/nk67vjx4zpz5oxcXV0z1Hjs2LEMc+Tr65thDDc3twy3wufFxYsX5eLiIicnp1wf4+bmpsjISM2bN09nzpxRYmKiFi5cqP79+8vNzc2hr7Ozc4a60+fp1KlTWZ4jMTFRZcqUybaO+Ph4tWjRQpIUHR2tjRs3asuWLXrrrbfs15ZXL7zwgtasWaNffvlFKSkpio6OVufOnfP8e3ujPvjgA23ZskXr16/XW2+9pePHj+uJJ57Q5cuXHfo5OztnuBU/XXr4dnFxyfCeu7u7jDG6dOlS/hcPAACQDXZxB3KhVKlS9k290lWpUiXH4/r166fo6Gh99tlnqly5sk6cOKHx48fb3y9evLhsNpu+//77DKFNUoY2m82W6XkKFSqk5557Tm+++abGjx+vyZMnq1mzZjnWmL4iHBUVZd8V+3olS5bMdozc8vPzU2pqqo4dO5blBw1Z1ejr62v/vvf1ihQpki/15VTDlStXdOHCBRUuXDjXxw0cOFBjx47VzJkzdenSJV29elXPPvtshn5Xr17VqVOnHEL6sWPHJGX+gUM6Pz8//fXXX9nW8MUXX8jFxUXLli2Tu7u7vX3JkiW5vo7rde/eXa+//ro++eQTNWjQQMeOHcvznRE3o3z58vaN4R599FF5eHjo7bffVlRUlF599VV7v5IlS2rLli0yxmT4u3P48GF7n+slJSXJzc0t27tPAAAAbgVW0IFccHV1VZ06dRxeuQmG9evX1wMPPKCYmBjFxMSoWLFievLJJ+3vt23bVsYYHT58OMP4derU0YMPPpjrGvv37y9XV1f16NFDv/zyi55//vkcj2ncuLG8vb21Z8+eTM9fp04dubq65rqG7Dz++OOSpClTpuTpuLZt2+rUqVNKTU3NtL7cfFByvfQPPnK7ely1alVJ0u+//56ncQICAtSlSxdNnjxZU6dOVbt27Rxuv/+nzz//3OHn9E3/rt/R/p8ef/xx/frrr1q7dm2WfWw2m5ydnR1W/y9evKg5c+ZkeUxO3N3dNWDAAM2ePVsTJkxQcHCwGjdufMPj3ayhQ4eqYsWKGjt2rM6dO2dvb968uc6ePZvphzsLFy5UoUKFMv3KyB9//GHZx/0BAIC7GyvowC3Wt29fvfzyy/rll18UGRkpDw8P+3uNGzfWgAED1KdPH23dulWPPvqoChcurKNHj+qHH37Qgw8+qIEDB+bqPN7e3urdu7emTJmioKAgtWvXLsdjvLy8FBUVpfDwcCUlJalz584qUaKEEhMTtXPnTiUmJuY5UGelSZMm6tWrl9577z0dP35cbdu2lZubm7Zv3y5PT08NHjw40+Oeeuopff7552rdurWGDBmievXqycXFRX/99ZdiY2PVoUOHPO8cnv787U8//VRFihSRu7u7ypUrl+VqdXpI3rx5sx566CF7e5EiRRQUFKSlS5eqWbNm8vHxUfHixR0eLzdkyBDVr19fUuaP3ZOufQA0fvx4nT9/XnXr1tWmTZv03nvv6fHHH7c/3i4zL774ohYsWKAOHTroX//6l+rVq6eLFy9q/fr1atu2rcLCwtSmTRtNmDBB3bt314ABA3Tq1Cl99NFHmd6xkReDBg3SuHHj9NNPP2n69Om5Pm7r1q32XfvPnj0rY4y+/PJLSVLdunUVFBSU51pcXFz0/vvvq2vXrpo4caL9yQI9evTQ5MmT1bVrV/3rX/9S3bp1dfHiRa1YsULR0dEaPHiwypcv7zBWWlqa4uPj7Y9KBAAAuK0KcIM64J6QmJhoXF1djSQTHx+faZ+ZM2ea+vXrm8KFCxsPDw9ToUIF07t3b7N161Z7n+x2pE63bt06I8mMHTs2TzWuX7/etGnTxvj4+BgXFxdTunRp06ZNG4cd2292F3djru0g/u9//9s88MADxtXV1RQrVsw0bNjQfPPNNw7Xef3O3ikpKeajjz4yNWvWNO7u7sbLy8tUrVrVREZGmt9++83eLygoyLRp0ybD9WU25n/+8x9Trlw54+TkZCSZmJiYbOeoSZMmpnXr1hnaV69ebWrVqmXc3NyMJIdd1tOVLVvWVKtWLdNxw8PDTeHChc3PP/9sQkNDjYeHh/Hx8TEDBw4058+fd+h7/S7uxhhz+vRpM2TIEBMYGGhcXFxMiRIlTJs2bcy+ffvsfWbOnGmqVKli3NzcTPny5c2YMWPMjBkzjCRz4MCBbOdJWex0bowxoaGhxsfHxyQnJ2f6flbXKynTV06/B1n9GUxXv359c99995kzZ87Y286ePWuGDh1qKlWqZFxdXY2np6epU6eOmTp1qsMO9unWrFljJJmffvop19cEAACQX2zG5LAVNYA7xiuvvKIpU6bozz//zPa7y8i7r776St26ddOhQ4dUunTpXB/3888/23c9HzRoUIb3IyIi9OWXX+r8+fP5We4td+LECQUFBWnw4MEaN25cQZeTb3r16qU//vjD/kQEAACA24lb3IG7wObNm/Xrr79q8uTJioyMJJzfAp06dVLdunU1ZswYTZo0Kcf+v//+uw4dOqQ333xTAQEBioiIuPVF3gZ//fWX/vjjD3344YcqVKiQhgwZUtAl5Zvff/9dCxYsyPY7/QAAALcSm8QBd4GGDRtq4MCBatu2rd57772CLueuZLPZFB0drVKlSiktLS3H/qNGjdJjjz2m8+fPa9GiRfL09LwNVd5606dPV2hoqHbv3q3PP/88T3cTWF1CQoImTZqU7ff+AQAAbiVucQcAAAAAwAJYQQcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAKcC7oAAABuVkJCgk6ePFnQZdxxLl++LDc3t4Iu447CnN0Y5i3vihcvrsDAwIIuA8BtRkAHANzREhISVK1aNSUnJxd0KQCQbzw9PbV3715COnCPIaADAO5oJ0+eVHJysubOnatq1aoVdDl3jBUrVmjYsGEaNWqUWrduXdDl3BGYsxvDvOXd3r171bNnT508eZKADtxjCOgAgLtCtWrVVLt27YIu446xd+9eSVK5cuWYt1xizm4M8wYAuccmcQAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAQR0AAAAAAAsgIAOAAAAAIAFENABAAAAALAAAjoAAAAAABZAQAcAAAAAwAII6AAAAAAAWAABHQAAAAAACyCgAwAAAABgAc4FXQAAAPlh7969BV3CHeXAgQP2/922bVsBV3NnYM5uDPOWd/z3DLh32YwxpqCLAADgRiUkJKhatWpKTk4u6FIAIN94enpq7969CgwMLOhSANxGBHQAwB0vISFBJ0+eLOgy7jiXL1+Wm5tbQZdxR2HObgzzlnfFixcnnAP3IAI6AAAAAAAWwCZxAAAAAABYAAEdAAAAAAALIKADAAAAAGABBHQAAAAAACyAgA4AAAAAgAUQ0AEAAAAAsAACOgAAAAAAFkBABwAAAADAAgjoAAAAAABYAAEdAIDbzGazacmSJdn2iYiI0BNPPHFb6rlTMG83hnnLO+YMQEEhoAMA7nkRERGy2Wyy2WxycXFR+fLl9eqrr+rChQs3Ne67776r4ODgDO1Hjx7V448/Lkk6ePCgbDabduzY4dBn4sSJmjVr1k2dPzdGjx6tRo0aydPTU97e3nk69l6dt4MHD6pfv34qV66cPDw8VKFCBQ0fPlxXrlzJ1fH36rxJUvv27RUYGCh3d3cFBASoV69eOnLkSI7H3ctzlu7y5csKDg7OtBYAdw/ngi4AAAAraNWqlWJiYpSSkqLvv/9e/fv314ULFzRlypQ8j2WMUWpqapbv+/v75zhGsWLF8nzeG3HlyhV16dJFDRs21IwZM/J8/L04b/v27VNaWpqmTZumihUrateuXXrmmWd04cIFffTRR7ka416cN0kKCwvTm2++qYCAAB0+fFivvvqqOnfurE2bNuV47L06Z+mGDh2qUqVKaefOnbf1vABuMwMAwD0uPDzcdOjQwaGtf//+xt/f3xhjzJw5c8zDDz9svLy8TMmSJc3TTz9tjh8/bu8bGxtrJJlVq1aZhx9+2Li4uJiZM2caSQ6vmJgYY4wxkszixYvtv/7nKyQkJNOaLl26ZAYPHmz8/PyMm5ubady4sYmPj89Qw+rVq83DDz9sPDw8TMOGDc2+fftyNQcxMTGmWLFizFse5y3duHHjTLly5Zi3PM7b0qVLjc1mM1euXGHOsrFixQpTtWpVs3v3biPJbN++PcdjANyZuMUdAIBMeHh4KCUlRdK1VeZRo0Zp586dWrJkiQ4cOKCIiIgMxwwdOlRjxozR3r171aJFC73yyiuqUaOGjh49qqNHj6pbt24ZjomPj5ckrV69WkePHtXXX3+daT1Dhw7VV199pdmzZ2vbtm2qWLGiWrZsqaSkJId+b731lsaPH6+tW7fK2dlZffv2vcmZyJt7dd7+/vtv+fj45OmYf7oX5y0pKUmff/65GjVqJBcXl1wfl+5embPjx4/rmWee0Zw5c+Tp6ZmbqQFwJyvoTwgAACho16+E/fjjj8bX19d07do10/7x8fFGkjl37pwx5n8rY0uWLHHoN3z4cFOzZs0Mx+sfq3MHDhzIdEXsnzWdP3/euLi4mM8//9z+/pUrV0ypUqXMuHHjHGpYvXq1vc/y5cuNJHPx4sUc5yA/VtDvxXkzxpj9+/ebokWLmujo6Fz1v9fnbejQocbT09NIMg0aNDAnT57Mtv/19Rlz78xZWlqaadWqlRk1alS2tQC4e7CCDgCApGXLlsnLy0vu7u5q2LChHn30UUVFRUmStm/frg4dOigoKEhFihRRaGioJCkhIcFhjDp16tyS2n7//XelpKSocePG9jYXFxfVq1dPe/fudej70EMP2X8dEBAgSTpx4sQtqUti3o4cOaJWrVqpS5cu6t+/f65ru5fn7bXXXtP27dv17bffysnJSb1795YxJse67sU5i4qK0tmzZ/XGG2/cgqoBWBGbxAEAoGubV02ZMkUuLi4qVaqU/ZbbCxcuqEWLFmrRooXmzp0rPz8/JSQkqGXLlhl27S5cuPAtqS09vNhstgzt17f981bh9PfS0tJuSV3SvT1vR44cUVhYmBo2bKhPP/00T7Xdy/NWvHhxFS9eXJUrV1a1atV0//33a/PmzWrYsGG2x92Lc7Z27Vpt3rxZbm5uDu116tRRjx49NHv27JuuHYC1sIIOAICu/cO9YsWKCgoKcvgH9L59+3Ty5EmNHTtWTZo0UdWqVXO9Iu3q6prtTtHpfSRl269ixYpydXXVDz/8YG9LSUnR1q1bVa1atVzVcqvcq/N2+PBhhYaGqnbt2oqJiVGhQnn7J9W9Om/XSw+2ly9fzrHvvThnH3/8sXbu3KkdO3Zox44dWrFihSRpwYIFGj169A2PC8C6WEEHACAbgYGBcnV1VVRUlJ599lnt2rVLo0aNytWxZcuW1YEDB7Rjxw6VKVNGRYoUybASVqJECXl4eGjVqlUqU6aM3N3dMzy+qXDhwho4cKBee+01+fj4KDAwUOPGjVNycrL69et3U9eXkJCgpKQkJSQkKDU11f585YoVK8rLy+uGx72b5+3IkSMKDQ1VYGCgPvroIyUmJtrfy83jubJzN89bfHy84uPj9cgjj+i+++7TH3/8oXfeeUcVKlTIcfU8O3fznAUGBjr8nP53skKFCipTpswNjwvAulhBBwAgG35+fpo1a5YWLVqk6tWra+zYsbl+1vWTTz6pVq1aKSwsTH5+fpo/f36GPs7Ozvr44481bdo0lSpVSh06dMh0rLFjx+rJJ59Ur169VLt2be3fv1//93//p/vuu++mru+dd95RrVq1NHz4cJ0/f161atVSrVq1tHXr1psa926et2+//Vb79+/X2rVrVaZMGQUEBNhfN+tunjcPDw99/fXXatasmapUqaK+ffvqgQce0Pr16zOE4ry4m+cMwL3HZnKzKwcAAAAAALilWEEHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABRDQAQAAAACwAAI6AAAAAAAWQEAHAAAAAMACCOgAAAAAAFgAAR0AAAAAAAsgoAMAAAAAYAEEdAAAAAAALICADgAAAACABfw/Ei4u1k7CsoQAAAAASUVORK5CYII=)

### 🔹 Key Takeaways from the Diagram:

- **Point Read**:
  - Requires both `id` and `partition key`.
  - Performs a **direct lookup** in the correct partition.
  - Very efficient—typically costs **1 RU**.

- **Query**:
  - Uses a query string (e.g., filter on fields).
  - May need to **scan multiple partitions**.
  - Less efficient—**higher RU cost** depending on complexity.

Let me know if you'd like a downloadable version or a similar diagram for MongoDB Atlas!

In **MongoDB Atlas**, the way you retrieve documents is conceptually similar to Cosmos DB, but there are some key differences in how **data access, performance, and cost** are handled.

---

### 🔹 1. **Point Reads in MongoDB Atlas**

MongoDB doesn't use the term "point read" explicitly, but the equivalent is:

- A **find operation** using the **`_id` field**, which is automatically indexed.
- Example:
  ```js
  db.collection.find({ _id: ObjectId("...") })
  ```

✅ This is **very efficient** and fast, especially if you're querying by `_id`.

---

### 🔹 2. **Partitioning and Sharding**

- In **Cosmos DB**, you must provide both `id` and `partition key` for a point read.
- In **MongoDB Atlas**, if your cluster is **sharded**, you must provide the **shard key** (similar to Cosmos DB's partition key) for efficient access.

  - If you **don’t provide the shard key**, MongoDB must **scatter the query** across all shards, which is slower and more resource-intensive.

---

### 🔹 3. **Indexing and Query Efficiency**

- MongoDB automatically indexes `_id`, and you can define additional indexes.
- Queries that **don’t use indexed fields** will result in **collection scans**, which are slower and more costly in terms of compute.

---

### 🔹 4. **Cost Model Differences**

| Feature                  | Cosmos DB                          | MongoDB Atlas                     |
|--------------------------|------------------------------------|-----------------------------------|
| **Point Read**           | Requires `id` + partition key      | Requires `_id` or shard key       |
| **Query Cost**           | Based on RU consumption            | Based on compute time and IOPS    |
| **Scaling**              | Per container (fine-grained)       | Per cluster (coarse-grained)      |
| **Sharding**             | Required for large scale           | Optional, but needed for scale    |

---

Would you like a **side-by-side visual diagram** comparing point reads and queries in Cosmos DB vs MongoDB Atlas? I can generate that for you.


Yes, **partition keys in Azure Cosmos DB** are conceptually similar to **shard keys in MongoDB Atlas**, but there are some important differences in how they work and are used:

---

### 🔹 Similarities

| Feature              | Cosmos DB Partition Key         | MongoDB Shard Key               |
|----------------------|----------------------------------|----------------------------------|
| **Purpose**          | Distribute data across partitions | Distribute data across shards    |
| **Required for Scale** | Yes                             | Yes                              |
| **Affects Query Routing** | Yes – needed for efficient point reads | Yes – needed for targeted queries |
| **Immutable**        | Yes – cannot be changed after creation | Yes – cannot be changed after creation |

---

### 🔹 Key Differences

| Aspect                  | Cosmos DB                          | MongoDB Atlas                     |
|--------------------------|------------------------------------|-----------------------------------|
| **Terminology**         | Partition Key                      | Shard Key                         |
| **Granularity**         | Per container                      | Per collection                    |
| **Routing**             | Requires both `id` and partition key for point reads | Requires shard key for targeted queries |
| **Automatic vs Manual** | You must define a partition key    | You must define a shard key when sharding is enabled |
| **Scaling Model**       | Horizontal scaling is automatic    | Sharding must be explicitly enabled and managed |

---

### 🔹 Example

- **Cosmos DB**:
  ```json
  {
    "id": "user123",
    "country": "India"  // partition key
  }
  ```

- **MongoDB**:
  ```js
  db.users.createIndex({ country: 1 })  // shard key
  ```

In both cases, the value of `country` determines **where the data is stored** and how efficiently it can be retrieved.

---

Would you like a **visual comparison diagram** of partition keys vs shard keys? I can generate one to make this clearer.

