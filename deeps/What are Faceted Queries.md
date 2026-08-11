In MongoDB, faceted queries (built using the $facet aggregation stage) let you run multiple independent aggregations on the same dataset in a single database request. It's like getting several different analytical views of your data all at once, without needing to make multiple round trips to the database.

How $facet Works

The $facet stage processes your data through several "sub-pipelines" simultaneously. Each sub-pipeline can contain its own operations like $match, $group, or $sort, and the results for each are returned as separate fields in a single output document.

```javascript
db.collection.aggregate([
  {
    $facet: {
      "categoryCounts": [ { $group: { _id: "$category", count: { $sum: 1 } } } ],
      "topProducts": [ { $sort: { sales: -1 } }, { $limit: 10 } ],
      "totalRevenue": [ { $group: { _id: null, total: { $sum: "$price" } } } ]
    }
  }
])
```

When to Use Faceted Queries

· Building Analytics Dashboards: When you need multiple metrics in one view, like total orders, average order value, and sales by product category, all calculated at the same time.
· Powering E-commerce Filtering (Faceted Navigation): This is a classic use case, enabling UI filters for categories, price ranges, brands, and ratings to update dynamically.
· Optimizing Multiple Operations: Replacing multiple separate queries with a single $facet stage to save resources and streamline your code, such as getting paginated results and a total count simultaneously.

Important Performance Consideration

While powerful, $facet can be resource-intensive. Since it processes the entire set of input documents for each sub-pipeline, it's crucial to filter your data as early as possible (using $match) before the $facet stage. Additionally, $facet itself doesn't use indexes directly, and the final output document is subject to the 16MB BSON size limit.

Is there a specific use case you're considering where you might want to use a faceted query? I can help you think through the structure.
