# query
Can I use Kafka Connect's MongoDB Kafka Connector to write to more than one kafka topic from a single collection conditionally based on the value of one or more fields in the change stream document received.

# response
Yes, **you can use Kafka Connect's MongoDB Kafka Connector to write data from a single MongoDB collection to multiple Kafka topics conditionally**, based on field values in the change stream document — but **not directly through the connector's out-of-the-box configuration alone**.

The **MongoDB Kafka Connector** (by MongoDB, Inc.) supports **topic routing** via **Single Message Transformations (SMTs)**, and you can use **conditional routing** with SMTs like `Route`, `ExtractField`, and custom logic to dynamically route messages to different Kafka topics.

However, **conditional routing based on field values requires a bit of configuration using SMTs**, and sometimes a **custom SMT** if the built-in ones are insufficient.

---

### ✅ Option 1: Use Built-in SMTs for Conditional Routing (Limited)

You can use the `org.apache.kafka.connect.transforms.RegexRouter` or `org.apache.kafka.connect.transforms.ValueToKey` + `org.apache.kafka.connect.transforms.KeyEnrich` + `Route` in combination, but these are **not designed for deep conditional logic based on arbitrary JSON field values**.

For example, `RegexRouter` only works on topic names, not message content.

So this is **not sufficient** for routing based on field values like `{"type": "user"}` → `users-topic`, `{"type": "order"}` → `orders-topic`.

---

### ✅ Option 2: Use `InsertField` + `Route` (via Document Preprocessing)

You can **preprocess the change stream event** to extract a field and use it in routing.

But Kafka Connect's built-in SMTs don’t support **conditional topic routing based on arbitrary JSON field values** directly.

---

### ✅ Option 3: Use a Custom SMT (Recommended for Conditional Routing)

You can write a **custom Single Message Transformation (SMT)** that:

1. Parses the value (change stream document).
2. Extracts a field (e.g., `fullDocument.type` or `updateDescription.updatedFields.category`).
3. Sets the topic name dynamically using `record.newTopic(...)`.

#### Example Use Case:
```json
// Change stream event
{
  "operationType": "insert",
  "fullDocument": {
    "type": "payment",
    "amount": 100
  }
}
```

→ Route to `payments-topic`

```json
{
  "fullDocument": {
    "type": "user",
    "name": "Alice"
  }
}
```

→ Route to `users-topic`

---

### ✅ How to Implement Custom SMT

1. Create a Java class implementing `Transformation<R>`, e.g., `FieldBasedRouter`.

```java
public class FieldBasedRouter<R extends ConnectRecord<R>> implements Transformation<R> {

    private String fieldName;

    @Override
    public R apply(R record) {
        if (record.value() == null) return record;

        // Parse value (assuming Struct, or use JSON parser)
        Map<String, Object> value = (Map<String, Object>) record.value();
        Object fullDocument = value.get("fullDocument");
        if (fullDocument instanceof Map) {
            String type = (String) ((Map) fullDocument).get("type");
            String newTopic = type != null ? type.toLowerCase() + "s-topic" : "default-topic";
            return record.newRecord(newTopic, record.kafkaPartition(),
                    record.keySchema(), record.key(),
                    record.valueSchema(), record.value(),
                    record.timestamp());
        }
        return record;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        this.fieldName = (String) configs.get("field.name");
    }

    @Override
    public void close() {}

    public static ConfigDef config() {
        return new ConfigDef().define("field.name", Type.STRING, ConfigDef.NO_DEFAULT_VALUE, "Field to route on");
    }
}
```

2. Compile and add the JAR to Kafka Connect’s plugin path.

3. Configure the connector with the SMT:

```json
{
  "name": "mongodb-source-connector",
  "config": {
    "connector.class": "com.mongodb.kafka.connect.MongoSourceConnector",
    "connection.uri": "mongodb://localhost:27017",
    "database": "mydb",
    "collection": "mycollection",
    "publish.full.document.only": "true",
    "topic.prefix": "mongo",

    "transforms": "routeByType",
    "transforms.routeByType.type": "com.example.FieldBasedRouter",
    "transforms.routeByType.field.name": "fullDocument.type"
  }
}
```

> Note: You may need to enable `publish.full.document.only=true` to get the full document in the message value.

---

### ✅ Alternative: Use Kafka Streams or ksqlDB (Post-Connect)

If writing a custom SMT is not feasible, you can:

1. Write all change events to a **single Kafka topic**.
2. Use **Kafka Streams** or **ksqlDB** to **branch** the stream based on field values and write to multiple topics.

#### Example in ksqlDB:
```sql
CREATE STREAM all_changes (fullDocument STRUCT<type VARCHAR, ...>) 
    WITH (KAFKA_TOPIC='mongo-mycollection', VALUE_FORMAT='JSON');

CREATE STREAM payments WITH (KAFKA_TOPIC='payments-topic') AS
    SELECT * FROM all_changes
    WHERE fullDocument->type = 'payment';

CREATE STREAM users WITH (KAFKA_TOPIC='users-topic') AS
    SELECT * FROM all_changes
    WHERE fullDocument->type = 'user';
```

This is **simpler to implement** and very flexible.

---

### ✅ Summary

| Approach | Can Route Conditionally? | Difficulty |
|--------|--------------------------|----------|
| Built-in SMTs | ❌ Limited/no support | Low |
| Custom SMT | ✅ Yes, full control | Medium |
| Kafka Streams / ksqlDB | ✅ Yes, recommended | Low to Medium |

> ✅ **Recommended**: Use **a single topic + ksqlDB/Kafka Streams branching** unless you need routing at ingestion time for performance or operational reasons.


