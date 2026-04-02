In **AWS**, a **placement group** is an **EC2 feature** that controls **how EC2 instances are physically placed on underlying AWS infrastructure**, mainly to influence **latency, throughput, and fault tolerance**.

Think of it as telling AWS *where* (relative to each other) your instances should live inside a Region.

***

## Why placement groups exist

By default, AWS spreads EC2 instances across hardware to balance load and reduce correlated failures.  
A **placement group lets you override that default** for specific performance or resilience needs.

***

## Types of placement groups (exam‑critical)

AWS supports **three types** of placement groups:

***

### 1️⃣ Cluster Placement Group

**What it does**

*   Places instances **close together** in the same rack (or very few racks)
*   Optimized for **low latency and high network throughput**

**Key characteristics**

*   Single Availability Zone only
*   Very low network latency (single‑digit microseconds)
*   High bandwidth (up to 100+ Gbps depending on instance type)
*   **Higher blast radius**: hardware failure can impact many instances

**Typical use cases**

*   High‑performance computing (HPC)
*   Distributed in‑memory databases
*   Big data, ML training, tightly coupled workloads

✅ **Exam clue words**

> “Low latency”, “high throughput”, “HPC”, “tightly coupled”, “single AZ”

***

### 2️⃣ Spread Placement Group

**What it does**

*   Places instances on **distinct underlying hardware**
*   Maximizes **fault isolation**

**Key characteristics**

*   Can span multiple Availability Zones
*   Each instance uses separate racks
*   **Instance limit** per AZ (currently small – traditionally \~7 per AZ)
*   Best for critical instances, not large fleets

**Typical use cases**

*   Core application servers
*   License servers
*   Critical nodes where simultaneous failure is unacceptable

✅ **Exam clue words**

> “Minimize correlated failures”, “high availability”, “critical small number of instances”

***

### 3️⃣ Partition Placement Group

**What it does**

*   Divides instances into **logical partitions**
*   Each partition uses **independent racks**
*   AWS spreads partitions across AZs if needed

**Key characteristics**

*   Designed for **large distributed systems**
*   You control fault domains via partitions
*   Failures in one partition **don’t impact others**
*   Supports **hundreds or thousands** of instances

**Typical use cases**

*   HDFS
*   Cassandra
*   Kafka
*   Large-scale distributed databases

✅ **Exam clue words**

> “Large distributed system”, “rack-aware”, “fault isolation at scale”

***

## Quick comparison table (exam-ready)

| Feature      | Cluster     | Spread          | Partition                |
| ------------ | ----------- | --------------- | ------------------------ |
| Goal         | Performance | Fault isolation | Fault isolation at scale |
| Instances    | Many        | Few             | Hundreds+                |
| AZ scope     | Single AZ   | Multi-AZ        | Multi-AZ                 |
| Latency      | Lowest      | Normal          | Normal                   |
| Blast radius | High        | Very low        | Partition-limited        |
| Typical use  | HPC         | Critical apps   | Big data systems         |

***

## Important rules & constraints (often tested)

*   Placement groups are **Region-specific**
*   An instance can belong to **only one placement group**
*   You **cannot move** a running instance into or out of a placement group  
    → must **stop and start** (or recreate)
*   Not all instance types are supported in all placement group types
*   Best practice: **launch instances into the placement group from the start**

***

## Common exam traps

❌ “High availability across AZs → Cluster placement group”  
✅ **Wrong** — Cluster is single‑AZ only

❌ “Need lowest latency and high bandwidth across Regions”  
✅ **Wrong** — Placement groups are **within a single Region**

❌ “Large distributed workload + spread placement group”  
✅ **Wrong** — Spread is for **small numbers**, use **Partition**

***

## One‑line summary (perfect for revision)

> **A placement group controls how EC2 instances are physically placed to optimize for performance (Cluster), fault isolation (Spread), or fault isolation at scale (Partition).**

