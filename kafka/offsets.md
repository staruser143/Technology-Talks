# A Kafka consumer keeps track of the record position it has read using offsets. Here's a clear breakdown of how this works, including what happens during a restart:


---

🔹 How Kafka Consumer Tracks Record Position

1. **Offset Tracking**
   
* Each message in a Kafka partition has a unique, increasing number called an offset.
* Kafka consumers read messages by offset, and the offset acts as a pointer to the next record to consume.
* The consumer maintains an in-memory position (i.e., the latest offset consumed per partition), which is periodically committed to a durable store.

🔹 **Where is the Offset Stored?**
Kafka provides two main options:

### Option 1: Kafka Internal Offsets Topic (Default)

* By default, Kafka consumers commit offsets to a special topic: __consumer_offsets.

* This allows Kafka to track the last-read offset per (consumer group, topic, partition).


### Option 2: External Store (Custom)

* Advanced users may store offsets in external systems like a database or ZooKeeper, for fine-grained control (not recommended for most cases).



---

🔄 What Happens on Restart?

* When a Kafka consumer restarts, this is what happens:

1. **Rejoin Group:**

* The consumer re-joins its consumer group (with a unique group ID).
* A rebalance may occur where Kafka assigns topic partitions to consumers.

2. **Seek to Committed Offsets:**

* Kafka looks up the last committed offset for each assigned partition (from __consumer_offsets).
* The consumer then resumes consuming from that offset.

---

### 🔧 Offset Commit Strategies

## Auto Commit (default):

### Controlled by:

enable.auto.commit=true
auto.commit.interval.ms=5000

The consumer periodically commits offsets automatically.


## Manual Commit:

* More reliable in scenarios needing at-least-once or exactly-once guarantees.

* Use APIs like:
consumer.commitSync(); // or commitAsync()

---

# 📌 Important Configurations

## Config	                Purpose

group.id	                Identifies the consumer group (enables offset tracking)
enable.auto.commit	      Whether to auto-commit offsets
auto.offset.reset	        Where to start if no offset is committed (earliest, latest, none)

---

### 🧠 Summary

## Event	Action

* Consumer reads message	Tracks current offset in memory
* Offset commit (auto/manual)	Saves offset to __consumer_offsets
* Consumer restarts	Fetches last committed offset and resumes from there
---


