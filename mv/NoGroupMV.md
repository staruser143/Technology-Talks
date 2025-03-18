If you don't want to group by any fields and only want to fetch specific fields or documents from the source collection in a materialized view, you can still use the aggregation framework without the `$group` stage. Instead, you can use stages like `$match`, `$project`, and `$out` or `$merge` to filter and project the desired fields.

Here's an example:

1. **Filter Documents**: Use the `$match` stage to filter documents based on specific criteria.
2. **Select Fields**: Use the `$project` stage to select specific fields.
3. **Output the Results**: Use the `$out` or `$merge` stage to write the results to a new collection.

Here's a complete example:
```javascript
db.sourceCollection.aggregate([
   { $match: { status: "active" } }, // Filter documents with 'active' status
   { $project: { name: 1, age: 1, status: 1 } }, // Select specific fields
   { $out: "materializedViewCollection" } // Write results to 'materializedViewCollection'
])
```

In this example:
- The `$match` stage filters documents with an "active" status.
- The `$project` stage selects the `name`, `age`, and `status` fields.
- The `$out` stage writes the results to a new collection called `materializedViewCollection`.

This way, you can create a materialized view that contains only the specific fields and documents you need[1](https://www.mongodb.com/docs/manual/core/materialized-views/)[2](https://tapdata.io/data-engineering-resources/create-manage-materialized-views-mongodb/).

If you have any other questions or need further assistance, feel free to ask!
