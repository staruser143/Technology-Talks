This is a **classic SAP‑C02 trap**, and you’re right to drill into it.

Short answer up front:

> **RDS Read Replicas cannot replace Multi‑AZ because they solve a *different problem*.**  
> **Multi‑AZ = availability & fast failover**  
> **Read replicas = scalability & optional DR**

Let’s break it down cleanly, *exam‑style*, with **failure modes**, **RTO/RPO**, and **why “it works” ≠ “it’s correct”**.

***

## 1️⃣ What Multi‑AZ Actually Does (Why It Exists)

### RDS Multi‑AZ is about **availability**, not scaling or DR

**Architecture**

*   Primary DB in AZ‑A
*   Standby DB in AZ‑B
*   **Synchronous, block‑level replication**
*   Storage is always in sync

### Failure Behavior

| Failure          | Outcome            |
| ---------------- | ------------------ |
| Instance failure | Automatic failover |
| AZ failure       | Automatic failover |
| Storage failure  | Automatic failover |

### RTO / RPO

| Metric  | Value            |
| ------- | ---------------- |
| **RTO** | \~60–120 seconds |
| **RPO** | Near‑zero        |

📌 **Key Insight**

> Multi‑AZ is designed so the application **barely notices** the failure.

✅ **No application changes required**  
✅ **No DNS changes required**  
✅ **Same endpoint remains**

***

## 2️⃣ What Read Replicas Actually Do (And Don’t)

### RDS Read Replicas are about **read scalability and analytics**

**Architecture**

*   Primary DB
*   One or more read replicas
*   **Asynchronous, logical replication** (SQL‑level)

### Failure Behavior

| Failure       | Outcome                   |
| ------------- | ------------------------- |
| Primary crash | Manual promotion required |
| AZ failure    | Replica might fail too    |
| Replica lag   | Data loss possible        |

### RTO / RPO

| Metric  | Value                                   |
| ------- | --------------------------------------- |
| **RTO** | Minutes → hours                         |
| **RPO** | Seconds → minutes (or worse under load) |

📌 **Critical Insight**

> Replicas **may not be consistent** with the primary at the moment of failure.

***

## 3️⃣ Why Read Replicas CANNOT Replace Multi‑AZ

(**This is the exam crux**)

### 🚫 1. Failover Is **Manual**

*   You must:
    *   Detect failure
    *   Promote replica
    *   Update application endpoint / DNS
*   ❌ No automatic failover

🧠 **Exam phrase to watch for:**

> *“without manual intervention”* → **Read replicas eliminated immediately**

***

### 🚫 2. Replication Is **Asynchronous**

*   Transactions can be lost
*   Replica lag increases during:
    *   Write spikes
    *   Network jitter
    *   Lock contention

❌ This breaks **near‑zero RPO**

***

### 🚫 3. Endpoint Changes Are Required

*   Multi‑AZ keeps the **same DNS endpoint**
*   Read replicas require:
    *   Connection string changes **or**
    *   DNS updates **or**
    *   Application redeploy

❌ Increased RTO

***

### 🚫 4. Replicas Can Lag or Be Broken

*   Replication can:
    *   Fall behind
    *   Stop entirely
    *   Be delayed silently

📌 **Multi‑AZ doesn’t have “lag”** — it’s storage‑level.

***

### 🚫 5. Replicas Are Not Guaranteed to Be in Another AZ

*   You *can* place them in another AZ
*   But:
    *   It’s not enforced like Multi‑AZ
    *   Failover is still manual

***

## 4️⃣ Side‑by‑Side Comparison (Memory‑Grade)

| Dimension       | Multi‑AZ     | Read Replica        |
| --------------- | ------------ | ------------------- |
| Primary goal    | Availability | Read scaling / DR   |
| Replication     | Synchronous  | Asynchronous        |
| Failover        | Automatic    | Manual              |
| Data loss       | Near‑zero    | Possible            |
| Endpoint change | No           | Yes                 |
| RTO             | Minutes      | Minutes–hours       |
| RPO             | Near‑zero    | Seconds–minutes     |
| Exam category   | ✅ Correct    | ❌ “Works but wrong” |

***

## 5️⃣ SAP‑C02 “Works vs Correct” Thinking

### Scenario

> “The workload must automatically recover from an AZ failure with minimal data loss.”

| Option                     | Verdict       | Why                                  |
| -------------------------- | ------------- | ------------------------------------ |
| RDS Multi‑AZ               | ✅ **Correct** | Automatic failover, sync replication |
| Read replica in another AZ | ❌ *Works*     | Manual promotion, async lag          |
| Cross‑Region replica       | ❌ Distractor  | Higher RTO                           |
| Snapshots                  | ❌ Obvious no  | Restore time                         |

🧠 **Rule**

> If AWS says **“automatic failover”**, **only Multi‑AZ qualifies**.

***

## 6️⃣ When Read Replicas ARE the Right Answer

To be crystal clear (and exam‑safe):

✅ Read replicas are correct when the question says:

*   “Offload read traffic”
*   “Scale reads”
*   “Analytics queries”
*   “Disaster recovery with minutes of RPO”
*   “Manual promotion acceptable”

❌ They are **never** correct when the requirement includes:

*   Automatic failover
*   Minimal downtime
*   Near‑zero RPO

***

## 7️⃣ One‑Line Exam Anchors (Burn These In)

*   **Multi‑AZ = Availability**
*   **Read Replica = Scaling**
*   **Availability ≠ DR**
*   **Async replication ≠ zero data loss**
*   **Manual promotion ≠ low RTO**

***

