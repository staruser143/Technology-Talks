Perfect. Below is a **fresh set of SAP‑C02‑level multi‑select traps** where **each scenario has ONE very attractive wrong option**, and **you must choose EXACTLY TWO services**.

The traps intentionally mimic:

*   **File Gateway vs DataSync**
*   **S3 Replication vs DataSync**
*   **Snowball vs DataSync**
*   **DataSync vs Transfer Family**
*   **Storage Gateway vs direct AWS service**

👉 I’ll give you the **questions first**.  
👉 **Answer key + elimination logic comes AFTER** (so you can self‑test like the real exam).

***

# 🎯 Mega‑Scenario Set — Choose **EXACTLY TWO**

***

## 🔹 Scenario 1 — File Gateway vs DataSync (classic killer)

A company wants to migrate **180 TB of on‑prem NFS data** to **Amazon EFS**.

Requirements:

*   Preserve **file permissions and metadata**
*   Perform **initial migration + nightly incremental sync**
*   On‑prem applications will be **retired after migration**
*   No requirement for local access after cutover

Which TWO solutions should be used?

A. AWS DataSync  
B. AWS Storage Gateway (File Gateway)  
C. AWS Snowball  
D. Amazon EFS  
E. Amazon S3 Replication

✅ Choose EXACTLY TWO

***

## 🔹 Scenario 2 — S3 Replication vs DataSync (very common exam trap)

A company stores log files on‑premises using **SMB shares**.  
They want to:

*   Move data continuously to **Amazon S3**
*   Preserve **file metadata**
*   Avoid writing custom scripts
*   Enable **incremental transfers**

Which TWO solutions meet the requirements?

A. AWS DataSync  
B. Amazon S3 Replication  
C. AWS Storage Gateway (File Gateway)  
D. AWS Transfer Family  
E. Amazon EventBridge

✅ Choose EXACTLY TWO

***

## 🔹 Scenario 3 — Snowball temptation

A media company must migrate **400 TB of video files** from an on‑prem data center to AWS.

Requirements:

*   Migration must complete ASAP
*   WAN bandwidth is **reliable but limited**
*   Future **weekly file updates** are required
*   Metadata must be preserved

Which TWO services should be used?

A. AWS Snowball  
B. AWS DataSync  
C. AWS Storage Gateway  
D. Amazon S3 Replication  
E. AWS Transfer Family

✅ Choose EXACTLY TWO

***

## 🔹 Scenario 4 — Transfer Family deception

A business partner uploads files once per day to the company’s on‑prem **NFS server**.  
The company wants to:

*   Migrate these files to **Amazon S3**
*   Keep uploads working during migration
*   Avoid modifying partner workflows
*   Preserve permissions

Which TWO services are MOST appropriate?

A. AWS Transfer Family  
B. AWS DataSync  
C. AWS Storage Gateway (File Gateway)  
D. Amazon S3 Replication  
E. AWS Snowball

✅ Choose EXACTLY TWO

***

## 🔹 Scenario 5 — Storage Gateway “sounds right” but isn’t

A company wants to move **90 TB of archived files** from on‑prem SMB shares to **S3 Glacier**.

Constraints:

*   One‑time migration
*   No on‑prem applications will access files afterward
*   Network bandwidth is sufficient
*   Minimal operational overhead required

Which TWO should be selected?

A. AWS DataSync  
B. AWS Storage Gateway (File Gateway)  
C. Amazon S3 Lifecycle policies  
D. AWS Snowball  
E. AWS Transfer Family

✅ Choose EXACTLY TWO

***

## 🔹 Scenario 6 — Cross‑Region red herring

A company generates files on‑prem and needs them available in **two AWS Regions**.

Requirements:

*   Files originate on‑prem (NFS)
*   Must continuously sync changes
*   No refactoring of file formats
*   Durability and cost efficiency required

Which TWO solutions satisfy the requirements?

A. AWS DataSync  
B. Amazon S3 Replication  
C. AWS Storage Gateway (File Gateway)  
D. AWS DMS  
E. AWS Snowball

✅ Choose EXACTLY TWO

***

## 🔹 Scenario 7 — DataSync vs File Gateway knife‑edge

A company is sunsetting its on‑prem data center.

They need to:

*   Migrate 60 TB from SMB to Amazon S3
*   Ensure **no application uses the data after migration**
*   Complete migration within 2 weeks
*   Avoid maintaining hybrid infrastructure

Which TWO services are the BEST fit?

A. AWS DataSync  
B. AWS Storage Gateway (File Gateway)  
C. Amazon S3  
D. AWS Transfer Family  
E. AWS Snowball

✅ Choose EXACTLY TWO

***

# ⏸️ STOP HERE (Self‑Test)

Try answering **before** looking at explanations.

***

## ✅ Answer Key + Elimination Logic

### Scenario 1

✅ **A + D**  
❌ File Gateway = wrong (no hybrid access needed)

***

### Scenario 2

✅ **A + C**  
❌ S3 Replication = WRONG (source isn’t S3)

***

### Scenario 3

✅ **A + B**  
❌ Storage Gateway = unnecessary hybrid layer

***

### Scenario 4

✅ **B + C**  
❌ Transfer Family = tempting, but missing migration + metadata logic

***

### Scenario 5

✅ **A + C**  
❌ Storage Gateway = hybrid access not required

***

### Scenario 6

✅ **A + B**  
❌ File Gateway = hybrid not needed; DataSync feeds S3 → Replication

***

### Scenario 7

✅ **A + C**  
❌ File Gateway = violates “avoid hybrid infra” constraint

***

# 🧠 Final Exam Reflex (THIS is what AWS is testing)

    If apps are RETIRED → DataSync
    If apps are ACTIVE  → Storage Gateway
    If source is NOT S3 → S3 Replication is NEVER correct
    Seed huge + then deltas → Snowball + DataSync

***

