Below is a **clear, exam‑ready mapping of RTO/RPO → AWS services and DR patterns**, focusing exactly on **AWS Backup, DR strategies, Aurora, and S3 replication**, with **Correct vs Works thinking baked in**.

***

# RTO / RPO → AWS Services Mapping (Architect + Exam View)

## 1. RTO–RPO Bands → DR Strategy (AWS Canonical)

| RTO      | RPO       | DR Pattern (AWS)             | What This Implies                         |
| -------- | --------- | ---------------------------- | ----------------------------------------- |
| Minutes  | Near‑zero | **Multi‑Site Active‑Active** | Synchronous replication, instant failover |
| < 30 min | < 5 min   | **Warm Standby**             | Pre‑scaled standby, async replication     |
| Hours    | ≤ 1 hr    | **Pilot Light**              | Core components only                      |
| 24+ hrs  | 24 hrs    | **Backup & Restore**         | Lowest cost, slowest recovery             |

📌 **Exam Rule:**

> **Lower RTO/RPO → More automation + higher cost**

***

## 2. AWS Backup (Backups ≠ Low RTO)

### Where AWS Backup Fits

| RTO  | RPO  | AWS Backup Fit?  | Why                     |
| ---- | ---- | ---------------- | ----------------------- |
| High | High | ✅ **Correct**    | Restore takes hours     |
| Low  | Low  | ❌ **Distractor** | Restore is manual, slow |

### AWS Backup Characteristics

*   Point‑in‑time recovery (EBS, RDS, DynamoDB, FSx)
*   Cross‑Region backup vaults (improves **RPO**, not RTO)
*   **Always implies Backup‑and‑Restore DR pattern**

📌 **Exam Trap:**

> “Cross‑Region backups” **DO NOT** give low RTO.

***

## 3. Aurora (RTO/RPO Mapping)

### ✅ Same‑Region (Multi‑AZ)

| Feature         | RTO     | RPO       |
| --------------- | ------- | --------- |
| Aurora Multi‑AZ | Seconds | Near‑zero |

*   Shared storage across AZs
*   Failover is automatic

✅ **Use for Availability**, **NOT Regional DR**

***

### ✅ Cross‑Region Aurora (Global Database)

| Feature          | RTO       | RPO     | DR Pattern   |
| ---------------- | --------- | ------- | ------------ |
| Aurora Global DB | < 1–5 min | < 1 sec | Warm Standby |

*   Storage‑level replication
*   Manual or scripted failover

📌 **Exam Tip:**

> If question says **“seconds of data loss”**, Aurora Global DB is favored.

***

### ❌ Aurora Snapshots Only

| RTO   | RPO             | Verdict                   |
| ----- | --------------- | ------------------------- |
| Hours | ≥ last snapshot | **Backup & Restore only** |

***

## 4. Amazon S3 Replication (RPO‑Driven Service)

### S3 Replication Options

| Option                                | RPO      | RTO    | Notes                |
| ------------------------------------- | -------- | ------ | -------------------- |
| S3 CRR (standard)                     | Minutes  | Manual | Eventual consistency |
| **S3 Replication Time Control (RTC)** | ≤ 15 min | Manual | SLA‑backed RPO       |
| Versioning only                       | High     | Manual | No DR alone          |

📌 **Key Insight:**

> S3 replication **does NOT provide automatic failover** → RTO is still manual.

***

## 5. End‑to‑End Mapping Table (Exam Gold)

| Use Case           | RTO      | RPO     | Correct AWS Services                |
| ------------------ | -------- | ------- | ----------------------------------- |
| Trading / Payments | Minutes  | Zero    | Aurora Global DB, Route 53 failover |
| SaaS App           | < 30 min | < 5 min | Warm Standby + Aurora Global DB     |
| Enterprise App     | Hours    | 1 hr    | Pilot Light + S3 CRR                |
| Compliance Archive | 24 hrs   | 24 hrs  | AWS Backup + S3                     |

***

## 6. “Correct vs Works vs Distractor” Snapshot

| Option                  | Verdict              | Why                        |
| ----------------------- | -------------------- | -------------------------- |
| Multi‑AZ Aurora         | ✅ Correct            | Low RTO/RPO (Availability) |
| Aurora Snapshot Restore | ❌ Distractor         | High RTO                   |
| AWS Backup Cross‑Region | ❌ Distractor         | Backup ≠ fast recovery     |
| S3 RTC                  | ✅ Correct (RPO only) | RTO still manual           |
| Active‑Active regions   | ✅ Correct            | Lowest RTO/RPO             |

***

## 7. Decision Cheat Sheet (Recall in Exam)

    Need RPO ≈ 0?
     ├─ Same Region → Multi‑AZ
     └─ Cross Region → Aurora Global DB

    Need RTO < 10 min?
     ├─ Yes → Warm Standby / Active‑Active
     └─ No → Pilot / Backup

    Only backups mentioned?
     → Backup & Restore DR pattern

***

## 8. ONE‑LINE EXAM MEMORY HOOK

> **Backups define RPO. Architecture defines RTO.**

***


