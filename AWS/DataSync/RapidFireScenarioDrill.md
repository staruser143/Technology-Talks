Alright — **rapid‑fire, reflex‑training mode**.  
No explanations unless absolutely needed.  
Read → decide → check answer.

***

## 🚀 AWS DataSync — Rapid‑Fire Scenario Drill

### 1️⃣

**Scenario:**  
Move 80 TB from on‑prem **NFS** to **Amazon S3** over a VPN within a week.

✅ **Answer:** AWS **DataSync**

***

### 2️⃣

**Scenario:**  
Continuously replicate an **Oracle database** from on‑prem to Amazon RDS.

✅ **Answer:** ❌ **Not DataSync** → **AWS DMS**

***

### 3️⃣

**Scenario:**  
Daily sync of **SMB file share** to **Amazon EFS**, preserving permissions.

✅ **Answer:** AWS **DataSync**

***

### 4️⃣

**Scenario:**  
One‑time migration of 500 TB with no network bandwidth available.

✅ **Answer:** ❌ **Not DataSync** → **Snowball Edge**

***

### 5️⃣

**Scenario:**  
Copy files between **S3 buckets in different AWS accounts** on a schedule.

✅ **Answer:** AWS **DataSync**  
⚠️ S3 Replication *may* work but is less flexible

***

### 6️⃣

**Scenario:**  
Need real‑time ingestion of clickstream events into analytics.

✅ **Answer:** ❌ **Not DataSync** → **Kinesis**

***

### 7️⃣

**Scenario:**  
Hybrid environment where on‑prem apps need **ongoing read/write access** to files stored in S3.

✅ **Answer:** ❌ **Not DataSync** → **Storage Gateway (File Gateway)**

***

### 8️⃣

**Scenario:**  
Sync large log files from on‑prem to S3 every night for Athena queries.

✅ **Answer:** AWS **DataSync**

***

### 9️⃣

**Scenario:**  
Application writes blocks directly and requires low‑latency block access in AWS.

✅ **Answer:** ❌ **Not DataSync** → **EBS / FSx**

***

### 🔟

**Scenario:**  
Regulatory audit requires **checksum validation** of transferred files.

✅ **Answer:** AWS **DataSync**

***

### 1️⃣1️⃣

**Scenario:**  
Transform CSV files during migration before loading to a data warehouse.

✅ **Answer:** ❌ **Not DataSync** → **Glue**

***

### 1️⃣2️⃣

**Scenario:**  
Replicate **EFS data** from us‑east‑1 to us‑west‑2.

✅ **Answer:** ✅ **DataSync**  
⚠️ *EFS Replication also works but is EFS‑only*

***

### 1️⃣3️⃣

**Scenario:**  
Secure file transfer for external partners over **SFTP**.

✅ **Answer:** ❌ **Not DataSync** → **AWS Transfer Family**

***

### 1️⃣4️⃣

**Scenario:**  
Migrate **FSx for Windows File Server** data into S3.

✅ **Answer:** AWS **DataSync**

***

### 1️⃣5️⃣

**Scenario:**  
Backup and restore EFS file systems with point‑in‑time recovery.

✅ **Answer:** ❌ **Not DataSync** → **AWS Backup**

***

## 🧠 Reflex Rule (Burn This In)

> **Files + NFS/SMB + Large volume + Sync/Migrate = DataSync**

***

