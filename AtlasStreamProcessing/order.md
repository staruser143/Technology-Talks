
Here’s a **complete MongoDB Atlas Stream Processor definition** that reads events from a source collection (`events`) and safely upserts them into a target domain collection (`domain`) using `$merge` logic that handles **out-of-order events**.

---

### 🧩 **Stream Processor Definition**

```json
{
  "source": {
    "type": "mongodb",
    "database": "eventDb",
    "collection": "events"
  },
  "pipeline": [
    {
      "$project": {
        "_id": "$fullDocument.entityId",  // Domain key
        "status": "$fullDocument.status",
        "data": "$fullDocument.data",
        "timestamp": "$fullDocument.timestamp"
      }
    },
    {
      "$merge": {
        "into": {
          "database": "domainDb",
          "collection": "domain"
        },
        "on": "_id",
        "whenMatched": [
          {
            "$replaceRoot": {
              "$cond": [
                { "$gt": ["$$new.timestamp", "$$existing.timestamp"] },
                "$$new",
                "$$existing"
              ]
            }
          }
        ],
        "whenNotMatched": "insert"
      }
    }
  ]
}
```

---

### 🔍 Explanation

- **`_id`**: Uses `entityId` from the event as the unique key in the domain store.
- **`$project`**: Extracts relevant fields from the event.
- **`$merge`**:
  - **`on: _id`**: Ensures upserts are keyed by entity.
  - **`whenMatched`**: Compares timestamps to prevent stale updates.
  - **`whenNotMatched: insert`**: Inserts new entities if they don’t exist.

---

### ✅ Benefits

- **Handles out-of-order events**: Only newer events update the domain store.
- **Real-time upserts**: No windowing delay.
- **Safe synchronization**: Prevents overwriting with stale data.

---



=======
Perfect! Here's how you can extend your **MongoDB Atlas Stream Processor** to include:

1. **Schema validation** — to ensure incoming events meet expected structure.
2. **Dead Letter Queue (DLQ)** — to capture invalid or malformed events for later inspection.

---

### 🧩 Extended Stream Processor Definition

```json
{
  "source": {
    "type": "mongodb",
    "database": "eventDb",
    "collection": "events"
  },
  "pipeline": [
    {
      "$validate": {
        "schema": {
          "bsonType": "object",
          "required": ["entityId", "status", "timestamp", "data"],
          "properties": {
            "entityId": { "bsonType": "string" },
            "status": { "bsonType": "string" },
            "timestamp": { "bsonType": "date" },
            "data": { "bsonType": "object" }
          }
        },
        "onError": {
          "action": "writeToSink",
          "sink": {
            "type": "mongodb",
            "database": "eventDb",
            "collection": "deadLetterQueue"
          }
        }
      }
    },
    {
      "$project": {
        "_id": "$fullDocument.entityId",
        "status": "$fullDocument.status",
        "data": "$fullDocument.data",
        "timestamp": "$fullDocument.timestamp"
      }
    },
    {
      "$merge": {
        "into": {
          "database": "domainDb",
          "collection": "domain"
        },
        "on": "_id",
        "whenMatched": [
          {
            "$replaceRoot": {
              "$cond": [
                { "$gt": ["$$new.timestamp", "$$existing.timestamp"] },
                "$$new",
                "$$existing"
              ]
            }
          }
        ],
        "whenNotMatched": "insert"
      }
    }
  ]
}
```

---

### ✅ What This Adds

- **`$validate`**: Ensures each event has the required fields and correct types.
- **`onError.writeToSink`**: Redirects invalid events to a **Dead Letter Queue** (`deadLetterQueue` collection).
- **Rest of the pipeline**: Continues to upsert valid events into the domain store with timestamp-based conflict resolution.

---

### 🧠 Benefits

- **Data integrity**: Prevents malformed events from corrupting domain state.
- **Observability**: DLQ allows you to monitor and debug schema violations.
- **Scalability**: Keeps your domain store clean and consistent even under high event volume.

---

Would you like help setting up monitoring or alerting on the DLQ collection to notify you when schema violations occur?Perfect! Here's how you can extend your **MongoDB Atlas Stream Processor** to include:

1. **Schema validation** — to ensure incoming events meet expected structure.
2. **Dead Letter Queue (DLQ)** — to capture invalid or malformed events for later inspection.

---

### 🧩 Extended Stream Processor Definition

```json
{
  "source": {
    "type": "mongodb",
    "database": "eventDb",
    "collection": "events"
  },
  "pipeline": [
    {
      "$validate": {
        "schema": {
          "bsonType": "object",
          "required": ["entityId", "status", "timestamp", "data"],
          "properties": {
            "entityId": { "bsonType": "string" },
            "status": { "bsonType": "string" },
            "timestamp": { "bsonType": "date" },
            "data": { "bsonType": "object" }
          }
        },
        "onError": {
          "action": "writeToSink",
          "sink": {
            "type": "mongodb",
            "database": "eventDb",
            "collection": "deadLetterQueue"
          }
        }
      }
    },
    {
      "$project": {
        "_id": "$fullDocument.entityId",
        "status": "$fullDocument.status",
        "data": "$fullDocument.data",
        "timestamp": "$fullDocument.timestamp"
      }
    },
    {
      "$merge": {
        "into": {
          "database": "domainDb",
          "collection": "domain"
        },
        "on": "_id",
        "whenMatched": [
          {
            "$replaceRoot": {
              "$cond": [
                { "$gt": ["$$new.timestamp", "$$existing.timestamp"] },
                "$$new",
                "$$existing"
              ]
            }
          }
        ],
        "whenNotMatched": "insert"
      }
    }
  ]
}
```

---

### ✅ What This Adds

- **`$validate`**: Ensures each event has the required fields and correct types.
- **`onError.writeToSink`**: Redirects invalid events to a **Dead Letter Queue** (`deadLetterQueue` collection).
- **Rest of the pipeline**: Continues to upsert valid events into the domain store with timestamp-based conflict resolution.

---

### 🧠 Benefits

- **Data integrity**: Prevents malformed events from corrupting domain state.
- **Observability**: DLQ allows you to monitor and debug schema violations.
- **Scalability**: Keeps your domain store clean and consistent even under high event volume.

---

Would you like help setting up monitoring or alerting on the DLQ collection to notify you when schema violations occur?Perfect! Here's how you can extend your **MongoDB Atlas Stream Processor** to include:

1. **Schema validation** — to ensure incoming events meet expected structure.
2. **Dead Letter Queue (DLQ)** — to capture invalid or malformed events for later inspection.

---

### 🧩 Extended Stream Processor Definition

```json
{
  "source": {
    "type": "mongodb",
    "database": "eventDb",
    "collection": "events"
  },
  "pipeline": [
    {
      "$validate": {
        "schema": {
          "bsonType": "object",
          "required": ["entityId", "status", "timestamp", "data"],
          "properties": {
            "entityId": { "bsonType": "string" },
            "status": { "bsonType": "string" },
            "timestamp": { "bsonType": "date" },
            "data": { "bsonType": "object" }
          }
        },
        "onError": {
          "action": "writeToSink",
          "sink": {
            "type": "mongodb",
            "database": "eventDb",
            "collection": "deadLetterQueue"
          }
        }
      }
    },
    {
      "$project": {
        "_id": "$fullDocument.entityId",
        "status": "$fullDocument.status",
        "data": "$fullDocument.data",
        "timestamp": "$fullDocument.timestamp"
      }
    },
    {
      "$merge": {
        "into": {
          "database": "domainDb",
          "collection": "domain"
        },
        "on": "_id",
        "whenMatched": [
          {
            "$replaceRoot": {
              "$cond": [
                { "$gt": ["$$new.timestamp", "$$existing.timestamp"] },
                "$$new",
                "$$existing"
              ]
            }
          }
        ],
        "whenNotMatched": "insert"
      }
    }
  ]
}
```

---

### ✅ What This Adds

- **`$validate`**: Ensures each event has the required fields and correct types.
- **`onError.writeToSink`**: Redirects invalid events to a **Dead Letter Queue** (`deadLetterQueue` collection).
- **Rest of the pipeline**: Continues to upsert valid events into the domain store with timestamp-based conflict resolution.

---

### 🧠 Benefits

- **Data integrity**: Prevents malformed events from corrupting domain state.
- **Observability**: DLQ allows you to monitor and debug schema violations.
- **Scalability**: Keeps your domain store clean and consistent even under high event volume.

---
============
To set up **monitoring and alerting** for the **Dead Letter Queue (DLQ)** collection in MongoDB Atlas, you can use **Atlas Alerts** and optionally integrate with external tools like **Slack**, **PagerDuty**, or **email**. Here's a step-by-step guide:

---

## ✅ Step 1: Enable Monitoring on the DLQ Collection

MongoDB Atlas does not monitor individual collections directly, but you can:

- Monitor **insert rates** into the DLQ collection.
- Use **custom metrics** or **triggers** to detect abnormal activity.

---

## ✅ Step 2: Create a Trigger on the DLQ Collection

1. Go to your **MongoDB Atlas Project**.
2. Navigate to **Triggers** → **Database Triggers**.
3. Create a new trigger:
   - **Source Database**: `eventDb`
   - **Collection**: `deadLetterQueue`
   - **Event Type**: `Insert`
   - **Condition** (optional): Add logic to filter critical schema violations.
   - **Function**: Write a function to send alerts.

### Sample Trigger Function (Email or Webhook)

```js
exports = function(changeEvent) {
  const doc = changeEvent.fullDocument;
  const message = `DLQ Alert: Invalid event detected for entity ${doc.entityId || 'unknown'} at ${doc.timestamp}`;

  // Send to Slack, email, or webhook
  const url = "https://hooks.slack.com/services/your/webhook/url";
  const payload = { text: message };

  const http = context.services.get("http");
  return http.post({ url, body: JSON.stringify(payload), headers: { "Content-Type": "application/json" } });
};
```

---

## ✅ Step 3: Set Up Atlas Alerts (Optional)

You can also configure **Atlas Alerts** for:

- **High insert rate** into the DLQ collection.
- **Custom metrics** via **Atlas Data Federation** or **Atlas Charts**.

Go to **Alerts** → **Create Alert** and choose metrics like:
- **Insert Ops**
- **Document Count**
- **Custom thresholds**

---

## ✅ Step 4: Visualize with Atlas Charts (Optional)

Create a dashboard to:
- Track DLQ growth over time.
- Identify patterns in schema violations.

---


