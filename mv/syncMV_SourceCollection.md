To keep a materialized view in sync with its source collection in MongoDB, we can use a combination of the aggregation framework and change streams. Here are the steps to achieve this:

1. **Initial Creation**: Create the materialized view using an aggregation pipeline with the `$merge` or `$out` stage.
2. **Set Up Change Streams**: Use change streams to monitor changes in the source collection and update the materialized view accordingly.

### Step-by-Step Guide

1. **Create the Initial Materialized View**:
   ```javascript
   db.sourceCollection.aggregate([
      { $match: { status: "active" } },
      { $project: { name: 1, age: 1, status: 1 } },
      { $merge: { into: "materializedViewCollection", whenMatched: "merge", whenNotMatched: "insert" } }
   ])
   ```

2. **Set Up Change Streams**:
   ```javascript
   const changeStream = db.sourceCollection.watch();

   changeStream.on('change', (change) => {
      // Handle the change event and update the materialized view
      db.sourceCollection.aggregate([
         { $match: { status: "active" } },
         { $project: { name: 1, age: 1, status: 1 } },
         { $merge: { into: "materializedViewCollection", whenMatched: "merge", whenNotMatched: "insert" } }
      ]);
   });
   ```

### Explanation
- **Initial Creation**: The aggregation pipeline filters and projects the desired fields, then uses `$merge` to write the results to the `materializedViewCollection`.
- **Change Streams**: The change stream watches for changes in the `sourceCollection`. When a change occurs, it triggers the aggregation pipeline to update the materialized view.

This approach ensures that the materialized view stays in sync with the source collection in real-time
