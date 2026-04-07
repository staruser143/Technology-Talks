Below are **multiple, exam‑ready “Correct vs Works vs Distractor” tables** for **AWS DataSync**, organized by **scenario type** the way SAP‑C02 / Pro‑level questions are framed.

No fluff. Clear eliminations.

***

## 1️⃣ On‑Prem → AWS File Migration

| Requirement                                                | ✅ **Correct**    | ⚠️ **Works (Sub‑optimal)**     | ❌ **Distractor** |
| ---------------------------------------------------------- | ---------------- | ------------------------------ | ---------------- |
| Migrate tens of TB from on‑prem **NFS** to **S3** over WAN | **AWS DataSync** | rsync on EC2 (slow, ops heavy) | AWS DMS          |
| Preserve file metadata & permissions                       | **AWS DataSync** | Custom scripts                 | AWS Glue         |
| Incremental follow‑up sync after initial migration         | **AWS DataSync** | Snowball Edge (manual repeat)  | S3 Replication   |
| Secure, encrypted transfer without managing servers        | **AWS DataSync** | VPN + scripts                  | SCP / FTP        |

***

## 2️⃣ Ongoing Synchronization (Hybrid)

| Requirement                            | ✅ **Correct**    | ⚠️ **Works (Context‑dependent)** | ❌ **Distractor** |
| -------------------------------------- | ---------------- | -------------------------------- | ---------------- |
| Daily sync of **SMB share → EFS**      | **AWS DataSync** | Storage Gateway (latency)        | Transfer Family  |
| Sync files nightly for analytics in S3 | **AWS DataSync** | Storage Gateway                  | Kinesis          |
| One‑direction scheduled file copy      | **AWS DataSync** | S3 Replication (S3‑only)         | CloudFormation   |
| Validate checksums and retry failures  | **AWS DataSync** | Custom rsync                     | Lambda           |

***

## 3️⃣ AWS → AWS (Cross‑Account / Cross‑Region)

| Requirement                                | ✅ **Correct**    | ⚠️ **Works**               | ❌ **Distractor** |
| ------------------------------------------ | ---------------- | -------------------------- | ---------------- |
| Copy **S3 data across accounts**           | **AWS DataSync** | S3 Replication             | VPC Peering      |
| Replicate **EFS across Regions**           | **AWS DataSync** | EFS Replication (EFS‑only) | AWS Backup       |
| Migrate FSx → S3                           | **AWS DataSync** | EC2 copy jobs              | Snowball         |
| Encrypted, auditable inter‑region transfer | **AWS DataSync** | Custom pipelines           | Public S3        |

***

## 4️⃣ Large‑Scale One‑Time Transfers

| Requirement                              | ✅ **Correct**    | ⚠️ **Works**      | ❌ **Distractor** |
| ---------------------------------------- | ---------------- | ----------------- | ---------------- |
| Network‑based migration (100 TB+)        | **AWS DataSync** | VPN + rsync       | Transfer Family  |
| No network / data center exit restricted | ❌                | **Snowball Edge** | AWS DataSync     |
| Fast turnaround without infra mgmt       | **AWS DataSync** | EC2 fleet         | Glue             |

👉 **Exam trap:** Snowball is correct **only** when network cannot be used.

***

## 5️⃣ What Looks Similar but Is WRONG

| Requirement                          | ✅ **Correct Tool**  | ❌ **Why DataSync Is Wrong**       |
| ------------------------------------ | ------------------- | --------------------------------- |
| Replicate databases (Oracle, MySQL)  | AWS DMS             | DataSync is file‑level only       |
| Real‑time event streaming            | Kinesis / MSK       | DataSync is batch/scheduled       |
| App needs low‑latency shared storage | EFS / FSx           | DataSync ≠ live filesystem        |
| External partner SFTP access         | AWS Transfer Family | DataSync is not a protocol server |
| Backup & point‑in‑time recovery      | AWS Backup          | DataSync ≠ backup service         |

***

## 6️⃣ Elimination Table (Fast Exam Reflex)

| If the question says…         | Eliminate  | Pick         |
| ----------------------------- | ---------- | ------------ |
| “Database replication”        | DataSync   | DMS          |
| “Streaming / real‑time”       | DataSync   | Kinesis      |
| “Offline transfer”            | DataSync   | Snowball     |
| “File sync / NFS / SMB / TBs” | DMS / Glue | **DataSync** |
| “Hybrid + incremental sync”   | Snowball   | **DataSync** |

***

## 🔑 One‑Line Memory Hook

> **Files + NFS/SMB + Large volume + Sync/Migrate → AWS DataSync**

***

