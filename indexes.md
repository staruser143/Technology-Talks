✅ Primary Index on _id

MongoDB automatically indexes _id, making it the fastest lookup option.

If _id is a combination of aggregate_id + event_id, you won't need a separate index for event_id.

✅ Compound Index { aggregate_id: 1, timestamp: 1 }

✅ Use Case: Fast event playback, efficient querying, and minimal sorting overhead. 

❌ Avoid { aggregate_id: 1 } alone if sorting is required post-query.


❌ Skip event_id index unless it’s frequently used in queries
