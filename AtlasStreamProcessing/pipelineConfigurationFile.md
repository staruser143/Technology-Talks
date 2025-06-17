A **configuration file** in the context of **Atlas Stream Processing** is a structured document—typically in **JSON** or **YAML** format—that defines the setup and behavior of a stream processor. It allows you to **deploy, manage, and automate** stream processing pipelines programmatically or via CLI/API.

---

### 🧾 **What Does a Configuration File Contain?**

Here’s what you typically define:

#### ✅ **1. Processor Metadata**
- Name of the stream processor
- Description (optional)

#### ✅ **2. Source Definition**
- Type: MongoDB collection or Kafka topic
- Connection details
- Database and collection names

#### ✅ **3. Pipeline Stages**
- Aggregation pipeline stages like `$match`, `$setWindowFields`, `$group`, `$merge`, etc.
- Logical partitioning (e.g., `partitionBy: "$quoteId"`)

#### ✅ **4. Sink Definition**
- Target MongoDB collection or Kafka topic
- Merge behavior (`on`, `whenMatched`, `whenNotMatched`)

#### ✅ **5. Security and Permissions**
- IAM roles or API keys
- Access control settings

---

### 📄 **Example Configuration File (JSON)**

```json
{
  "name": "quoteProcessor",
  "source": {
    "type": "mongodb",
    "db": "quotes",
    "collection": "quote_events_collection"
  },
  "pipeline": [
    {
      "$setWindowFields": {
        "partitionBy": "$quoteId",
        "sortBy": { "eventTimestamp": 1 },
        "output": {
          "latestEvent": {
            "$last": "$$ROOT",
            "window": {
              "range": [-1, "minute"],
              "unit": "minute"
            }
          }
        }
      }
    },
    { "$replaceRoot": { "newRoot": "$latestEvent" } },
    { "$group": { "_id": "$quoteId", "latest": { "$last": "$$ROOT" } } },
    {
      "$merge": {
        "into": { "db": "quotes", "coll": "quote_domain_collection" },
        "on": "_id",
        "whenMatched": "merge",
        "whenNotMatched": "insert"
      }
    }
  ]
}
```

---

### 🧠 Why Use a Configuration File?

- **Automation**: Deploy pipelines via CLI or CI/CD.
- **Versioning**: Track changes to stream logic.
- **Portability**: Share and reuse across environments.
- **Consistency**: Avoid manual errors in UI setup.

---

