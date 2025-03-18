# MongoDB Materialized Views
* MongoDB Materialized Views are **precomputed, stored query results** that improve query performance by reducing the need for expensive real-time aggregations. 
* They are typically created **using the $merge stage in an Aggregation Pipeline**, which writes the results of the aggregation into a separate collection.
* Unlike standard views in MongoDB (which are virtual and recompute results at query time), materialized views persist data in a collection and **need to be manually refreshed to stay up to date**.


## When to Use MongoDB Materialized Views

✅ <b>Use Cases:</b>

|Use Case   | Details    |
|------------|-----------|
| **Precomputed Aggregations**  | When running frequent and expensive aggregation queries on large datasets (e.g., sum, count, avg).  |
| **Faster Read Performance**  | When queries involve complex joins ($lookup), filtering, and transformations  |
| **Dashboards & Analytics**  | For real-time reporting where the data does not need to be updated instantly.  |
| **Data Denormalization**  | When you need to store pre-aggregated or transformed data to optimize retrieval.  |
| **Archiving Historical Data**  | Storing snapshots of past calculations while keeping the main dataset smaller.  |



❌ <b>When NOT to Use</b>:

|Use Case   | Details    |
|------------|-----------|
| **Highly Dynamic Data**  |If the source data changes frequently and materialized views need frequent refreshing, it may not be worth it  |
| **Storage Constraints**  | Materialized views take up additional disk space as they store redundant data.  |
| **Real-time Updates**  |If you need up-to-the-second accuracy, querying the source collection dynamically might be better.  |


## How to Create a Materialized View in MongoDB

### Using the $merge stage:

```javascript
db.orders.aggregate([
  { $group: { _id: "$customerId", totalSpent: { $sum: "$amount" } } },
  { $merge: { into: "customer_spending", whenMatched: "merge", whenNotMatched: "insert" } }
]);
```

* This creates a materialized view (customer_spending) that stores total spending per customer.
* For refreshing, you must re-run the aggregation periodically (e.g., via a cron job or a scheduled trigger).


