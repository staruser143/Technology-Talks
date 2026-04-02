Perfect — below are **hard, SAP‑C02–level *trick* questions** focused on **Disaster Recovery (RTO/RPO)**.  
Each question is crafted to **trap “works but wrong” thinking**, exactly how the **Solutions Architect Professional exam** does it.

I’ll include:

*   ✅ **Correct answer**
*   ❌ **Why the others are deceptive distractors**
*   🧠 **Exam elimination logic**

***

# 🔥 SAP‑C02 HARD DR TRICK QUESTIONS (RTO / RPO)

***

## **Question 1 – Multi‑AZ Trap**

A financial services application runs on Amazon Aurora MySQL with Multi‑AZ enabled.  
The compliance team requires **near‑zero data loss** and the ability to **withstand a complete AWS Region failure**.

Which architecture **meets the requirements MOST cost‑effectively**?

**A.** Enable automated backups and store snapshots in another Region  
**B.** Deploy Aurora with Multi‑AZ and reader instances  
**C.** Use Aurora Global Database with a secondary Region  
**D.** Enable cross‑Region read replicas and promote during failover

### ✅ **Correct Answer: C**

### ❌ Why Others Look Tempting

| Option | Why It’s a Trap                                   |
| ------ | ------------------------------------------------- |
| A      | Backups = **high RTO**                            |
| B      | Multi‑AZ = **availability only**, not regional DR |
| D      | RDS‑style replicas → higher replication lag       |

🧠 **Key Exam Phrase:** *“Region failure”*  
➡️ immediately eliminates **Multi‑AZ‑only** solutions.

***

## **Question 2 – Backup ≠ Low RTO**

A company uses Amazon RDS PostgreSQL for a reporting system.  
Backups are taken every 5 minutes using AWS Backup and copied to a second Region.

The business requires:

*   RPO ≤ 5 minutes
*   RTO ≤ 15 minutes

Which statement is TRUE?

**A.** The solution meets both RTO and RPO goals  
**B.** RPO is met but RTO is not  
**C.** RTO is met but RPO is not  
**D.** Neither RTO nor RPO is met

### ✅ **Correct Answer: B**

### 🧠 Elimination Logic

*   ✅ **RPO**: 5‑minute backups ✔
*   ❌ **RTO**: Restore time is **> 15 minutes**

📌 **Exam Rule:**

> *Backups never give low RTO*

***

## **Question 3 – DynamoDB Global Tables Subtlety**

A globally distributed application uses DynamoDB Global Tables across three Regions.  
The application allows users to submit updates from any Region.

Which failure scenario has the **lowest RTO and RPO**?

**A.** Loss of a single availability zone  
**B.** Loss of a Regional write endpoint  
**C.** Loss of all Regions except one  
**D.** Loss of the global control plane

### ✅ **Correct Answer: B**

### Why?

*   DynamoDB Global Tables are **active‑active**
*   Client traffic automatically continues to other Regions

| Option | Why It’s Wrong                                           |
| ------ | -------------------------------------------------------- |
| A      | AZ failures hardly matter — DynamoDB is already Multi‑AZ |
| C      | Larger blast radius → slower recovery                    |
| D      | Control plane isn't the data path                        |

🧠 **Exam Nugget:**

> Global Tables = **seconds RTO**, **near‑zero RPO**

***

## **Question 4 – S3 Replication Illusion**

A media platform uses Amazon S3 with **Replication Time Control (RTC)** enabled to another Region.

Management claims this ensures **low‑RTO disaster recovery**.

What is the **correct interpretation**?

**A.** RTC ensures low RTO and low RPO  
**B.** RTC ensures low RPO only; RTO is still manual  
**C.** RTC enables automatic Region failover  
**D.** RTC replaces the need for Route 53 health checks

### ✅ **Correct Answer: B**

📌 **Key Distinction**

*   **RTC → SLA on replication time**
*   **No automatic application failover**

🧠 **Exam Trick:**

> Data present ≠ Application recovered

***

## **Question 5 – Pilot Light vs Warm Standby**

A healthcare application must recover from a Region failure within **30 minutes**.  
Data loss of a few minutes is acceptable.

Which design BEST balances cost and requirements?

**A.** Multi‑site active‑active Regions  
**B.** Warm standby with scaled‑down services  
**C.** Pilot light with core services running  
**D.** Backup and restore

### ✅ **Correct Answer: B**

### Elimination

*   A → Overkill / cost ❌
*   D → RTO too high ❌
*   C → Scale‑up time may exceed 30 mins ❌

📌 **Exam Clue:**

> *“Within minutes, but not instantly”* → **Warm standby**

***

## **Question 6 – RDS Read Replica Misuse**

An architect proposes using **cross‑Region RDS read replicas** for disaster recovery, citing low RPO.

Which limitation must be explicitly communicated?

**A.** Read replicas cannot be promoted  
**B.** Replication is asynchronous and can lag  
**C.** Read replicas require Global Accelerator  
**D.** Replicas do not support Multi‑AZ

### ✅ **Correct Answer: B**

🧠 **Professional‑Level Insight:**

> RDS Read Replicas are **logical replication**, not storage‑level.

***

## **Question 7 – Multi‑AZ vs DR (Classic SAP Trap)**

A workload runs in a VPC with:

*   Auto Scaling
*   Application Load Balancer
*   RDS Multi‑AZ

The architecture team claims the workload is **fully protected against disasters**.

Which response is MOST accurate?

**A.** Correct, because Multi‑AZ handles Region failure  
**B.** Correct, because Auto Scaling replaces failed resources  
**C.** Incorrect; this only protects against AZ failures  
**D.** Incorrect; backups are also required

### ✅ **Correct Answer: C**

📌 **Exam Gold Rule**

> **If the Region is gone and the app is gone → no DR**

***

## **Question 8 – Aurora vs DynamoDB Choice**

A payments system requires:

*   <1 second RPO
*   <1 minute RTO
*   Strong consistency for writes

Which datastore is MOST appropriate?

**A.** Aurora Global Database  
**B.** DynamoDB Global Tables  
**C.** RDS Multi‑AZ  
**D.** RDS cross‑Region replica

### ✅ **Correct Answer: A**

### Why DynamoDB Is NOT Correct

*   Eventual consistency across Regions
*   Last‑writer‑wins conflict resolution

🧠 **Exam Differentiator**

> Relational + strong consistency → **Aurora Global DB**

***

## 🧠 Final SAP‑C02 Memory Anchors

*   **Multi‑AZ ≠ Disaster Recovery**
*   **Backups define RPO, not RTO**
*   **Aurora Global DB = best relational DR**
*   **DynamoDB Global Tables = lowest RTO/RPO overall**
*   **S3 replication ≠ app failover**

***

