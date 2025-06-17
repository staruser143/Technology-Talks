Thanks for the details! Based on your setup, here's a tailored **Atlas Stream Processing pipeline** design:

---

### 🧩 **Use Case Summary**

- **Source**: `quote_events_collection` (MongoDB)
- **Events**: Various quote lifecycle events
- **Target**: `quote_domain_collection` (MongoDB)
- **Processing Goals**:
  - **Deduplication**
  - **Ordering**
- **Partition Key**: `quoteId`

---

### 🛠️ **Pipeline Design**

```json
[
  {
    "$source": {
      "collection": "quote_events_collection",
      "db": "quotes"
    }
  },
  {
    "$setWindowFields": {
      "partitionBy": "$quoteId",
      "sortBy": { "eventTimestamp": 1 },
      "output": {
        "latestEvent": {
          "$last": "$$ROOT",
          "window": {
            "range": [ -1, "minute" ],
            "unit": "minute"
          }
        }
      }
    }
  },
  {
    "$replaceRoot": {
      "newRoot": "$latestEvent"
    }
  },
  {
    "$group": {
      "_id": "$quoteId",
      "latest": { "$last": "$$ROOT" }
    }
  },
  {
    "$merge": {
      "into": {
        "db": "quotes",
        "coll": "quote_domain_collection"
      },
      "on": "_id",
      "whenMatched": "merge",
      "whenNotMatched": "insert"
    }
  }
]
```

---

### 🔍 **Explanation of Key Stages**

- **`$setWindowFields`**: Ensures events are processed in order per `quoteId`.
- **`$last`**: Picks the most recent event in a time window (deduplication).
- **`$replaceRoot`**: Restructures the document to focus on the latest event.
- **`$group`**: Ensures only one document per `quoteId` is upserted.
- **`$merge`**: Writes to the domain collection with idempotent logic.

---

