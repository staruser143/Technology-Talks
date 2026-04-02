AWS supports a **standard set of disaster recovery (DR) scenarios (patterns)** that are deliberately reused across **architecture whitepapers, Well‑Architected Framework, and certification exams**.

Below is the **canonical, exam‑accurate view** — with **RTO/RPO expectations, AWS services commonly used, and when each pattern is correct vs a distractor**.

***

# ✅ AWS Disaster Recovery (DR) Scenarios

## The 4 Canonical AWS DR Patterns

AWS officially defines **four DR strategies**, ordered from **lowest cost / highest RTO** → **highest cost / lowest RTO**.

    Backup & Restore
    → Pilot Light
    → Warm Standby
    → Multi‑Site Active‑Active

***

## 1️⃣ Backup & Restore

### What it is

*   Store backups in a **separate Region**
*   Rebuild everything **after** a disaster

### Typical RTO / RPO

| Metric | Value                              |
| ------ | ---------------------------------- |
| RTO    | Hours → Days                       |
| RPO    | Snapshot / backup interval (hours) |

### AWS Services Used

*   AWS Backup
*   EBS / RDS / Aurora snapshots
*   S3 backups
*   IaC (CloudFormation / Terraform) for rebuild

### When this is **Correct**

✅ Non‑critical workloads  
✅ Compliance / archive systems  
✅ Cost‑optimized DR

### When this is a **Distractor**

❌ “Near‑zero data loss”  
❌ “Sub‑minute recovery”

📌 **Exam Clue Phrase**

> *“Backups are taken and stored in another AWS Region”* → **Backup & Restore**

***

## 2️⃣ Pilot Light

### What it is

*   **Minimal core infrastructure always running**
*   Full environment is scaled **after** disaster

### Typical RTO / RPO

| Metric | Value           |
| ------ | --------------- |
| RTO    | Minutes → Hours |
| RPO    | Minutes         |

### AWS Services Used

*   Minimal EC2 / ECS
*   RDS Read Replicas
*   DynamoDB backups
*   S3 CRR
*   IaC + Auto Scaling for scale‑up

### When this is **Correct**

✅ Enterprise applications  
✅ Moderate criticality  
✅ Balance between cost and recovery speed

### When this is a **Distractor**

❌ “Instant failover”  
❌ “No manual steps”

📌 **Exam Clue Phrase**

> *“Core services are always running; full capacity is launched during failover”*

***

## 3️⃣ Warm Standby

### What it is

*   **Fully functional but scaled‑down environment**
*   Increase capacity during disaster

### Typical RTO / RPO

| Metric | Value             |
| ------ | ----------------- |
| RTO    | Minutes           |
| RPO    | Seconds → Minutes |

### AWS Services Used

*   Aurora Global Database
*   DynamoDB Global Tables
*   Auto Scaling groups (low capacity)
*   Route 53 failover routing
*   S3 Cross‑Region Replication

### When this is **Correct**

✅ Customer‑facing apps  
✅ Low tolerance for downtime  
✅ Predictable traffic patterns

### When this is a **Distractor**

❌ Zero‑RTO or zero‑RPO claims

📌 **Exam Clue Phrase**

> *“A scaled‑down but fully operational environment exists in another Region”*

***

## 4️⃣ Multi‑Site Active‑Active

### What it is

*   **Full production environments in multiple Regions**
*   Traffic flows to **all Regions simultaneously**

### Typical RTO / RPO

| Metric | Value     |
| ------ | --------- |
| RTO    | Seconds   |
| RPO    | Near‑zero |

### AWS Services Used

*   DynamoDB Global Tables
*   Aurora Global DB (read‑mostly)
*   Route 53 latency / geolocation routing
*   CloudFront
*   Anycast IPs (Global Accelerator)

### When this is **Correct**

✅ Mission‑critical systems  
✅ Trading, payments, healthcare  
✅ Global SaaS platforms

### When this is a **Distractor**

❌ Cost‑sensitive workloads

📌 **Exam Clue Phrase**

> *“Users are routed to the nearest healthy Region automatically”*

***

## ⚠️ Important Exam Distinction: Availability ≠ DR

| Pattern                | DR?                 |
| ---------------------- | ------------------- |
| Multi‑AZ (same Region) | ❌ Availability only |
| Auto Scaling           | ❌ Availability      |
| Load Balancers         | ❌ Availability      |
| Cross‑Region           | ✅ DR                |

📌 **Golden Exam Rule**

> **If the Region fails and the app still works → DR is involved**

***

## 🔗 Mapping DR Patterns → AWS Datastores (Quick Recall)

| Pattern          | RDS           | Aurora               | DynamoDB          |
| ---------------- | ------------- | -------------------- | ----------------- |
| Backup & Restore | Snapshots     | Snapshots            | Backups           |
| Pilot Light      | Read Replica  | Cross‑Region Reader  | PITR              |
| Warm Standby     | RR + promoted | **Aurora Global DB** | Global Tables     |
| Active‑Active    | ❌             | Limited              | **Global Tables** |

***

## 🧠 Final Exam Memory Anchors

*   **Backups alone = highest RTO**
*   **Aurora Global DB = best relational DR**
*   **DynamoDB Global Tables = lowest RTO/RPO**
*   **Multi‑AZ ≠ Disaster Recovery**

***

