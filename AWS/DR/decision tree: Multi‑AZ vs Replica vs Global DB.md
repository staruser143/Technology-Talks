Below is a **clean, exam‑ready decision tree** for choosing between **RDS Multi‑AZ**, **RDS Read Replicas**, and **Aurora Global Database**.  
This is optimized for **SAP‑C02 recall + elimination**, not marketing descriptions.

***

# ✅ Decision Tree: Multi‑AZ vs Read Replica vs Global DB

***

## STEP 1 — What type of failure must be tolerated?

### ❓ *Does the requirement include a **full AWS Region failure**?*

                     ┌───────────────┐
                     │ Region failure│
                     │ must be handled? │
                     └───────┬────────┘
                             │
                  YES ───────┴─────── NO

***

## STEP 2A — **NO Region Failure Required**

👉 You are dealing with **availability**, not disaster recovery.

### ❓ *Is automatic failover with minimal downtime required?*

            ┌──────────────────────────┐
            │ Automatic failover needed │
            │ (AZ / instance failure)?  │
            └───────────┬──────────────┘
                        │
              YES ──────┴────── NO

### ✅ **YES → Choose RDS Multi‑AZ**

**Why**

*   Synchronous replication
*   Automatic failover
*   Same endpoint
*   Near‑zero RPO
*   Low RTO (minutes)

✅ **Correct for exam phrases**

*   “Must automatically recover”
*   “Minimal downtime”
*   “AZ failure”
*   “No application change”

❌ **Read replicas eliminated** (manual, async)

***

### ❌ **NO → Read Replicas (for scaling only)**

**Use when**

*   Read traffic offloading
*   Analytics/reporting
*   DR *optional*

📌 **Exam truth**

> Read replicas are **never an availability replacement**.

***

## STEP 2B — **YES, Region Failure Must Be Handled**

👉 You are now firmly in **DR territory**

### ❓ *What RPO is required?*

            ┌──────────────────────────┐
            │ Required RPO near-zero?  │
            │ (seconds or less)        │
            └───────────┬──────────────┘
                        │
              YES ──────┴────── NO

***

## STEP 3A — **YES, Near‑Zero RPO Required**

### ❓ *Is the workload relational?*

            ┌──────────────────────────┐
            │ Relational database?     │
            └───────────┬──────────────┘
                        │
              YES ──────┴────── NO

### ✅ **YES → Aurora Global Database**

**Why**

*   Storage‑level replication
*   Sub‑second RPO
*   Fast promotion
*   Warm‑standby DR

✅ **Exam phrases**

*   “Seconds of data loss”
*   “Global relational application”
*   “Cross‑Region DR with low RPO”

❌ **RDS Read Replicas eliminated**

*   Async
*   SQL‑level replication
*   Lag risk

***

### ✅ **NO → DynamoDB Global Tables**

(outside scope of this tree, but exam‑correct)

***

## STEP 3B — **NO, Minutes of Data Loss OK**

### ❓ *Is manual intervention acceptable during failover?*

            ┌──────────────────────────┐
            │ Manual promotion allowed │
            │ during disaster?         │
            └───────────┬──────────────┘
                        │
              YES ──────┴────── NO

### ✅ **YES → Cross‑Region Read Replicas (Pilot Light)**

**Why**

*   Lower cost
*   Acceptable RPO (minutes)
*   Acceptable RTO (minutes–hours)

📌 **Exam wording**

*   “Manual promotion”
*   “Cost‑optimized”
*   “Minutes of data loss acceptable”

***

### ❌ **NO → Warm Standby Required**

✅ ⇒ **Aurora Global Database**
(read replicas cannot do automatic failover)

***

# 🚨 Compact Exam‑Recall Version (Burn This In)

    Region failure?
     ├─ NO → Need auto‑failover?
     │        ├─ YES → Multi‑AZ
     │        └─ NO  → Read Replica (scaling)
     │
     └─ YES → Near‑zero RPO?
              ├─ YES → Aurora Global DB
              └─ NO  → Read Replica (Pilot light)

***

# 🧠 “Works vs Correct” Summary

| Requirement                  | Correct              | Why                       |
| ---------------------------- | -------------------- | ------------------------- |
| AZ failure, auto recovery    | Multi‑AZ             | Synchronous + automatic   |
| Read scaling                 | Read Replica         | Async, manual             |
| Regional DR, sub‑second loss | Aurora Global DB     | Storage‑level replication |
| Cheap DR, manual OK          | Read Replica         | Pilot light               |
| Zero‑touch failover          | Multi‑AZ / Global DB | Replicas fail here        |

***

# ⚠️ SAP‑C02 Exam Traps to Watch

*   ❌ “Read replica replaces Multi‑AZ” → **Always wrong**
*   ❌ “Backups provide low RTO” → **Never**
*   ❌ “Multi‑AZ protects from Region outage” → **False**
*   ✅ “Automatic failover” → **Multi‑AZ or Aurora Global DB only**

***

## One‑Line Exam Anchor

> **Multi‑AZ = Availability**  
> **Read Replica = Scaling / Optional DR**  
> **Aurora Global DB = Real Cross‑Region DR**

***

