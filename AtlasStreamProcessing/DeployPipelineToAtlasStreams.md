To deploy an **Atlas Stream Processing pipeline** in MongoDB Atlas, you’ll follow a few structured steps using the Atlas UI or API. Here's a step-by-step guide:

---

### 🛠️ **Step-by-Step Deployment in MongoDB Atlas**

#### **1. Log in to MongoDB Atlas**
- Go to https://cloud.mongodb.com
- Select your project or create a new one.

---

#### **2. Navigate to Stream Processing**
- In the left-hand menu, find **“Stream Processing”**.
- Click **“Create Stream Processor”**.

---

#### **3. Define Your Pipeline**
- Use the **pipeline editor** to define your stages.
- You can paste the JSON pipeline we discussed earlier:
```json
[
  { "$source": { "connectionName": "kafkaOrders", "topic": "orders" } },
  { "$lookup": { "from": { "connectionName": "mongoProd", "db": "retail", "coll": "customers" }, "localField": "customerId", "foreignField": "_id", "as": "customerInfo" } },
  { "$unwind": "$customerInfo" },
  { "$setWindowFields": { "partitionBy": "$customerId", "sortBy": { "orderTime": 1 }, "output": { "totalSpent": { "$sum": "$amount", "window": { "range": [ -5, "minute" ], "unit": "minute" } } } } },
  { "$project": { "customerId": 1, "customerName": "$customerInfo.name", "totalSpent": 1, "windowEnd": "$orderTime" } },
  { "$emit": { "connectionName": "kafkaAnalytics", "topic": "customer-spending" } }
]
```

---

#### **4. Configure Connections**
- Set up **Kafka connections** (source and sink) under **“Connections”**.
- Set up **MongoDB connections** if you're joining or writing to collections.

---

#### **5. Set Security and Permissions**
- Ensure your stream processor has access to the necessary Kafka topics and MongoDB collections.
- Use **IAM roles** or **API keys** as needed.

---

#### **6. Deploy and Monitor**
- Click **“Deploy”** to start the processor.
- Use the **monitoring dashboard** to view throughput, errors, and performance.
- You can pause, resume, or update the pipeline anytime.

---

