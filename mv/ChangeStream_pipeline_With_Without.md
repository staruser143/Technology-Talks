Using an aggregation pipeline is not mandatory when setting up MongoDB Change Streams. By default, you can create a change stream without specifying a pipeline, which will capture all changes in the monitored collection, database, or deployment.

### Basic Change Stream Without Pipeline

Here's an example of setting up a basic change stream without any filters:

```javascript
const { MongoClient } = require('mongodb');

async function monitorChanges() {
  const uri = 'your_mongodb_connection_string';
  const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });

  try {
    await client.connect();
    const database = client.db('your_database_name');
    const collection = database.collection('your_collection_name');

    const changeStream = collection.watch();

    changeStream.on('change', (change) => {
      console.log('Change detected:', change);
      // Execute your custom logic here
    });
  } finally {
    // Do not close the client to keep listening for changes
  }
}

monitorChanges().catch(console.error);
```

### Using an Aggregation Pipeline

While not mandatory, using an aggregation pipeline allows you to filter and transform the change events, making the change stream more efficient and tailored to your needs. For example, you can filter for specific fields or operation types.

### Example with Pipeline

```javascript
const { MongoClient } = require('mongodb');

async function monitorChanges() {
  const uri = 'your_mongodb_connection_string';
  const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });

  try {
    await client.connect();
    const database = client.db('your_database_name');
    const collection = database.collection('your_collection_name');

    // Define the aggregation pipeline to filter events
    const pipeline = [
      {
        $match: {
          $or: [
            { "updateDescription.updatedFields.fieldA": { $exists: true } },
            { "updateDescription.updatedFields.fieldB": { $exists: true } },
            { operationType: "insert" }
          ]
        }
      }
    ];

    const changeStream = collection.watch(pipeline);

    changeStream.on('change', (change) => {
      console.log('Change detected:', change);
      // Execute your custom logic here
    });
  } finally {
    // Do not close the client to keep listening for changes
  }
}

monitorChanges().catch(console.error);
```

### Summary
- **Without Pipeline**: Captures all changes in the monitored collection, database, or deployment.
- **With Pipeline**: Allows filtering and transforming change events for more efficient and tailored monitoring[1](https://www.mongodb.com/docs/manual/changeStreams/)[2](https://www.mongodb.com/docs/manual/reference/operator/aggregation/changeStream/).

Would you like more details on any specific aspect of using Change Streams?
