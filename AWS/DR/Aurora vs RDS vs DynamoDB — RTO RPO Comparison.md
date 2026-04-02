Below is a **clear, exam‑grade comparison of Aurora vs RDS vs DynamoDB focused purely on RTO/RPO behavior**, with **availability vs disaster‑recovery distinctions**, **service‑level capabilities**, and **common exam traps**.

***

# Aurora vs RDS vs DynamoDB — RTO / RPO Comparison

## 1. High‑Level Summary (One‑Glance Table)

| Service                             | Scope          | Typical RTO       | Typical RPO         | Key Determinant                 |
| ----------------------------------- | -------------- | ----------------- | ------------------- | ------------------------------- |
| **Aurora (Multi‑AZ)**               | AZ failure     | **Seconds**       | **Near‑zero**       | Shared storage + auto failover  |
| **Aurora Global DB**                | Region failure | **< 1–5 min**     | **< 1 sec**         | Storage‑level async replication |
| **RDS Multi‑AZ**                    | AZ failure     | **1–2 min**       | **Near‑zero**       | Synchronous replication         |
| **RDS Read Replica (cross‑Region)** | Region failure | **Minutes–hours** | **Seconds–minutes** | Async logical replication       |
| **DynamoDB (Multi‑AZ)**             | AZ failure     | **Milliseconds**  | **Zero**            | Leaderless quorum               |
| **DynamoDB Global Tables**          | Region failure | **Seconds**       | **Near‑zero**       | Active‑active replication       |

***

## 2. Aurora — RTO/RPO Breakdown

### ✅ Aurora Multi‑AZ (Same Region)

| Failure Type     | RTO     | RPO       |
| ---------------- | ------- | --------- |
| Instance failure | Seconds | Near‑zero |
| AZ failure       | Seconds | Near‑zero |

**Why:**

*   Storage replicated **6 copies across 3 AZs**
*   Compute failover only

✅ **Availability solution**, not regional DR

***

### ✅ Aurora Global Database (Cross‑Region)

| Failure Type    | RTO           | RPO        |
| --------------- | ------------- | ---------- |
| Regional outage | < 1–5 minutes | < 1 second |

**Why:**

*   Storage‑level replication (`~1s latency`)
*   Reader can be promoted quickly

📌 **Exam Truth:**

> Aurora Global DB = **lowest possible RPO for relational DB on AWS**

***

### ❌ Aurora Snapshots Only

| RTO   | RPO               |
| ----- | ----------------- |
| Hours | Snapshot interval |

➡️ Backup & Restore **only**

***

## 3. Amazon RDS (Non‑Aurora)

### ✅ RDS Multi‑AZ

| Failure Type | RTO         | RPO       |
| ------------ | ----------- | --------- |
| Instance/AZ  | 1–2 minutes | Near‑zero |

**Why:**

*   Synchronous replication to standby
*   Storage copy at block level

✅ Good availability  
❌ Not regional DR

***

### ⚠️ RDS Read Replicas (Cross‑Region)

| Capability | Value              |
| ---------- | ------------------ |
| RTO        | Minutes to hours   |
| RPO        | Seconds to minutes |

**Why weaker than Aurora Global DB?**

*   Logical (SQL‑level) async replication
*   Replica lag under load

📌 **Exam Trap:**

> RDS Read Replica **≠** low‑RPO DR

***

### ❌ RDS Snapshots

| Verdict  | Reason         |
| -------- | -------------- |
| High RTO | Manual restore |
| High RPO | Snapshot‑based |

***

## 4. DynamoDB — RTO/RPO King

### ✅ DynamoDB (Single Region)

| Failure    | RTO          | RPO  |
| ---------- | ------------ | ---- |
| AZ failure | Milliseconds | Zero |

**Why:**

*   Leaderless architecture
*   Quorum writes across AZs

✅ No standby, no failover step

***

### ✅ DynamoDB Global Tables

| Failure        | RTO     | RPO       |
| -------------- | ------- | --------- |
| Region failure | Seconds | Near‑zero |

**Why:**

*   Active‑active writes
*   Conflict resolution via last‑writer wins

📌 **Exam Rule:**

> **Lowest RTO/RPO of any AWS datastore**

***

## 5. DR Pattern Mapping (Exam‑Critical)

| Service                | DR Pattern            |
| ---------------------- | --------------------- |
| Aurora Multi‑AZ        | Availability (not DR) |
| Aurora Global DB       | Warm Standby          |
| RDS Multi‑AZ           | Availability          |
| RDS Read Replicas      | Pilot Light           |
| DynamoDB Global Tables | Active‑Active         |
| Snapshots (any DB)     | Backup & Restore      |

***

## 6. “Correct vs Works vs Distractor” (Exam Lens)

| Scenario                  | Correct                | Works            | Distractor |
| ------------------------- | ---------------------- | ---------------- | ---------- |
| Sub‑minute RPO relational | Aurora Global DB       | RDS RR           | Snapshots  |
| Zero‑RTO NoSQL            | DynamoDB Global Tables | Multi‑Region API | Backups    |
| AZ failure only           | RDS Multi‑AZ           | Read Replica     | Snapshot   |
| Region DR, minimal loss   | DynamoDB GT            | Aurora Global DB | RDS RR     |

***

## 7. Decision Cheat Sheet (Memory Friendly)

    Relational?
     ├─ Same Region → Aurora / RDS Multi‑AZ
     └─ Cross Region
        ├─ Seconds RPO → Aurora Global DB
        └─ Minutes RPO → RDS Read Replica

    Non‑Relational?
     ├─ Lowest RTO/RPO → DynamoDB Global Tables
     └─ Backup‑only allowed → On‑demand backups

***

## 8. One‑Line Exam Anchors

> **Aurora Global DB = best relational RPO**  
> **DynamoDB Global Tables = best overall RTO/RPO**  
> **Snapshots never give low RTO**

***

