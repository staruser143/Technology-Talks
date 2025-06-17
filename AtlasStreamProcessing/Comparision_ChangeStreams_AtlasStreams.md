Let’s walk through a **side-by-side example** of how you'd implement a simple stream processing task using:

- **MongoDB Change Streams with custom code**
- **Atlas Stream Processing with a declarative pipeline**

---

### 🧩 **Use Case**
**Goal**: Monitor a `sales` collection for new documents where `amount > 1000`, and store them in a `highValueSales` collection.

---

### 🧑‍💻 **Option 1: MongoDB Change Streams (Node.js Example)**

```javascript
const { MongoClient } = require('mongodb');

async function monitorSales() {
  const client = new MongoClient('mongodb://localhost:27017');
  await client.connect();

  const db = client.db('store');
  const sales = db.collection('sales');

  const changeStream = sales.watch();

  changeStream.on('change', async (change) => {
    if (change.operationType === 'insert') {
      const doc = change.fullDocument;
      if (doc.amount > 1000) {
        await db.collection('highValueSales').insertOne(doc);
      }
    }
  });
}

monitorSales();
```

🔹 **Pros**: Full control, flexible logic  
🔹 **Cons**: You manage infrastructure, error handling, scaling, and recovery

---

### 🧠 **Option 2: Atlas Stream Processing (Declarative Pipeline)**

```json
[
  { "$source": { "collection": "sales", "db": "store" } },
  { "$match": { "amount": { "$gt": 1000 } } },
  { "$merge": { "into": { "db": "store", "coll": "highValueSales" } } }
]
```

🔹 **Pros**: No code, fully managed, scalable, resilient  
🔹 **Cons**: Less flexible for complex custom logic

---

### 🧾 Summary

| Feature | Change Streams | Atlas Stream Processing |
|--------|----------------|--------------------------|
| **Setup** | Manual | Declarative |
| **Code Required** | Yes | No |
| **Error Handling** | Manual | Built-in |
| **Scaling** | Manual | Automatic |
| **Recovery** | Manual | Checkpointed |
| **Flexibility** | High | Moderate |

---

###
Let’s look at a **complex example** using **Atlas Stream Processing** that involves:

- **Reading from Kafka**
- **Joining with a MongoDB collection**
- **Applying a windowed aggregation**
- **Emitting results to another Kafka topic**

---

### 🧩 **Use Case: Real-Time Order Enrichment and Analytics**

**Goal**:  
- Read real-time `orders` from Kafka  
- Join with `customers` collection in MongoDB to enrich with customer info  
- Calculate total order value per customer in **5-minute windows**  
- Emit the results to a Kafka topic for downstream analytics

---

### 🧠 **Atlas Stream Processing Pipeline**

```json
[
  {
    "$source": {
      "connectionName": "kafkaOrders",
      "topic": "orders"
    }
  },
  {
    "$lookup": {
      "from": {
        "connectionName": "mongoProd",
        "db": "retail",
        "coll": "customers"
      },
      "localField": "customerId",
      "foreignField": "_id",
      "as": "customerInfo"
    }
  },
  {
    "$unwind": "$customerInfo"
  },
  {
    "$setWindowFields": {
      "partitionBy": "$customerId",
      "sortBy": { "orderTime": 1 },
      "output": {
        "totalSpent": {
          "$sum": "$amount",
          "window": {
            "range": [ -5, "minute" ],
            "unit": "minute"
          }
        }
      }
    }
  },
  {
    "$project": {
      "customerId": 1,
      "customerName": "$customerInfo.name",
      "totalSpent": 1,
      "windowEnd": "$orderTime"
    }
  },
  {
    "$emit": {
      "connectionName": "kafkaAnalytics",
      "topic": "customer-spending"
    }
  }
]
```

---

### 🔍 **What’s Happening Here**

| Stage | Purpose |
|-------|---------|
| `$source` | Reads real-time orders from Kafka |
| `$lookup` | Joins with MongoDB `customers` collection |
| `$unwind` | Flattens the joined customer info |
| `$setWindowFields` | Aggregates total spending per customer in 5-minute windows |
| `$project` | Shapes the output |
| `$emit` | Sends enriched, aggregated data to another Kafka topic |

---

This pipeline is **declarative**, **scalable**, and **resilient**—no custom code or infrastructure needed.

