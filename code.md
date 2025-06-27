```javascript
db.domainCollection.updateOne(
  {
    _id: domainId,
    sequence: event.sequence - 1 // Ensure strict ordering
  },
  {
    $addToSet: { dependents: event.dependent },
    $set: {
      sequence: event.sequence,
      lastEventTimestamp: event.timestamp
    }
  }
);
```

```javascript
hash(partitionKey) % totalInstances == instanceIndex
```


```javascript
const instanceIndex = parseInt(process.env.INSTANCE_INDEX, 10);
const totalInstances = parseInt(process.env.TOTAL_INSTANCES, 10);

function shouldProcessEvent(partitionKey) {
  const hash = Math.abs(crc32.str(partitionKey));
  return hash % totalInstances === instanceIndex;
}

async function startListener() {
  const client = new MongoClient(process.env.MONGO_URI);
  await client.connect();

  const db = client.db(process.env.DB_NAME);
  const collection = db.collection(process.env.COLLECTION_NAME);

  const changeStream = collection.watch([], { fullDocument: 'updateLookup' });

  console.log(`Listener ${instanceIndex} started...`);

  changeStream.on('change', (change) => {
    const doc = change.fullDocument;
    const partitionKey = doc.tenantId; // or userId, etc.

    if (shouldProcessEvent(partitionKey)) {
      console.log(`Instance ${instanceIndex} processing:`, doc);
      // Your processing logic here
    }
  });
}

startListener().catch(console.error);
```
