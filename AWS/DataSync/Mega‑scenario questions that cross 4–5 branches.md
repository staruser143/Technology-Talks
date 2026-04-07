Absolutely — here are **mega‑scenario, multi‑select “cross 4–5 branches” questions** that force you to choose among **DataSync vs DMS vs Snowball vs Storage Gateway vs Transfer Family** (and sometimes **S3 Replication / EFS Replication**) in one scenario.

**How to use this drill (SAP‑C02 style):**

*   Each mega scenario has **multiple requirements** → often **multiple services** are needed.
*   I’ll label each as **Select TWO / THREE / ALL that apply**.
*   After the questions, you’ll get a **Correct vs Works vs Distractor** answer key (with crisp justification).

***

# 🧨 Mega‑Scenario Set (Cross 4–5 Branches)

## Q1) “Data center exit + ongoing sync + partner exchange”

**Select THREE.**

A company is closing a data center in **6 weeks**. Requirements:

1.  They must migrate **600 TB** of unstructured data from an on‑prem **SMB** share to AWS.
2.  The WAN link is **unreliable and too slow** to move 600 TB in time.
3.  After the initial move, they need **nightly incremental sync** of changes until cutover.
4.  External partners must **upload/download files over SFTP** into the same landing zone.
5.  Data must land in **S3**, with audit logs.

**Options:**  
A. AWS DataSync  
B. AWS Snowball Edge  
C. AWS Storage Gateway (File Gateway)  
D. AWS Transfer Family  
E. AWS DMS  
F. S3 Replication

***

## Q2) “Hybrid app access + analytics copy + DB replication + DR”

**Select FOUR.**

A manufacturing firm runs:

*   A legacy app that expects a **Windows file share (SMB)** with low operational disruption.
*   An on‑prem **PostgreSQL** DB feeding downstream systems.
*   They want to build an analytics platform in AWS.

Requirements:

1.  Keep the app working with minimal refactor; files should be stored in **S3** but accessible like a file share.
2.  **Nightly copy** of those files into **EFS** for a Linux batch workload.
3.  Replicate PostgreSQL into **Amazon Aurora PostgreSQL** with minimal downtime.
4.  DR requirement: copy “analytics landing” **S3 bucket** to a second account in another region.

**Options:**  
A. AWS Storage Gateway (File Gateway)  
B. AWS DataSync  
C. AWS DMS  
D. S3 Replication  
E. AWS Transfer Family  
F. AWS Snowball Edge

***

## Q3) “Massive NFS migration + cross‑region FS sync + minimal admin”

**Select TWO.**

A research org has a **300 TB NFS** dataset on‑prem and wants:

1.  A one‑time migration to **Amazon EFS** in us‑east‑1.
2.  Keep a copy synchronized to **EFS in us‑west‑2** for a second team.
3.  They want **managed** transfer tooling and verification.

**Options:**  
A. AWS DataSync  
B. EFS Replication  
C. AWS Snowball Edge  
D. AWS Transfer Family  
E. AWS DMS

***

## Q4) “On‑prem NAS → S3 + transform + streaming confusion trap”

**Select THREE.**

A retailer has:

*   On‑prem NAS (NFS) producing hourly log files (hundreds of GB/day)
*   Wants analytics + operational dashboards.

Requirements:

1.  Move files from **NFS → S3** every hour with checksum validation.
2.  Convert raw logs to **Parquet** and partition by date for Athena.
3.  A separate system emits **real‑time click events** needing sub‑second ingestion to an analytics stream.

**Options:**  
A. AWS DataSync  
B. AWS Glue  
C. Amazon Kinesis Data Streams  
D. AWS DMS  
E. AWS Storage Gateway (File Gateway)  
F. AWS Transfer Family

***

## Q5) “M\&A cross‑account copy + shared access + offline + regulated audit”

**Select FOUR.**

Two companies merge. They need to consolidate data quickly:

1.  Company A has **200 TB** on‑prem in a locked‑down network with **no outbound internet** allowed.
2.  Company B already stores data in **S3**, but wants a cross‑account governed copy in a new “combined” account.
3.  They need **ongoing incremental sync** from A’s on‑prem file server for 30 days post‑close.
4.  Auditors require evidence that transfers are **complete and verified**.
5.  Business users want to exchange files with third parties using **FTPS** (not SFTP).

**Options:**  
A. AWS Snowball Edge  
B. AWS DataSync  
C. S3 Replication  
D. AWS Transfer Family  
E. AWS Storage Gateway (File Gateway)  
F. AWS DMS

***

## Q6) “DB + files + minimal downtime + keep old system alive”

**Select THREE.**

A healthcare firm is migrating an application:

*   Database: **Oracle** on‑prem
*   Files: **SMB share** (scans, PDFs)
*   Cutover window: **2 hours**

Requirements:

1.  Replicate Oracle DB into AWS with minimal downtime.
2.  Migrate 50 TB of SMB files and keep **incremental sync** until cutover.
3.  During migration, some on‑prem users must continue to access “hot files” with a file share interface while S3 becomes the system of record.

**Options:**  
A. AWS DMS  
B. AWS DataSync  
C. AWS Storage Gateway (File Gateway)  
D. AWS Snowball Edge  
E. AWS Transfer Family  
F. AWS Backup

***

# ✅ Answer Key — Correct vs Works vs Distractor (Mega Edition)

## Q1 — Correct picks: **B, A, D**

*   ✅ **Correct**
    *   **B Snowball Edge**: 600 TB + unreliable WAN + deadline → offline bulk transfer.
    *   **A DataSync**: nightly incremental sync post‑seed until cutover.
    *   **D Transfer Family**: partner SFTP managed endpoint into S3.
*   ⚠️ Works (but not best)
    *   **C File Gateway**: could provide SMB access to S3 but doesn’t solve the “move 600 TB fast” problem.
    *   **F S3 Replication**: S3‑to‑S3 only; doesn’t help on‑prem SMB ingest.
*   ❌ Distractors
    *   **E DMS**: database migration, not file shares.

***

## Q2 — Correct picks: **A, B, C, D**

*   ✅ **Correct**
    *   **A File Gateway**: SMB file share semantics backed by S3.
    *   **B DataSync**: nightly copy S3↔EFS / file sync tasks.
    *   **C DMS**: PostgreSQL → Aurora PostgreSQL replication.
    *   **D S3 Replication**: bucket replication cross‑region/cross‑account (DR).
*   ⚠️ Works
    *   **E Transfer Family**: only if partners needed to upload/download; not requested here.
    *   **F Snowball**: not needed; network is not the stated bottleneck.

***

## Q3 — Correct picks: **A + (B or A again depending on design)**

Best pro‑answer: **A + B**

*   ✅ **Correct**
    *   **A DataSync**: on‑prem NFS → EFS migration + verification.
    *   **B EFS Replication**: simplest managed EFS→EFS cross‑region sync (purpose‑built).
*   ⚠️ Works
    *   **A DataSync** alone can also do EFS↔EFS cross‑region, but replication is cleaner for EFS‑only.
*   ❌ Distractors
    *   **C Snowball**: not needed unless WAN is constrained.
    *   **D Transfer Family / E DMS**: irrelevant.

***

## Q4 — Correct picks: **A, B, C**

*   ✅ **Correct**
    *   **A DataSync**: hourly NFS→S3 with validation and managed transfer.
    *   **B Glue**: transform logs to Parquet + partitioning.
    *   **C Kinesis**: sub‑second streaming ingestion for click events.
*   ⚠️ Works
    *   **E File Gateway**: provides access semantics, not scheduled high‑throughput migration/sync.
*   ❌ Distractors
    *   **D DMS**: database-only.
    *   **F Transfer Family**: partner protocol server, not internal file movement.

***

## Q5 — Correct picks: **A, C, B, D**

*   ✅ **Correct**
    *   **A Snowball Edge**: no outbound internet + 200 TB.
    *   **B DataSync**: ongoing incremental sync for 30 days post‑close (after connectivity exists via Direct Connect/VPN/approved path).
    *   **C S3 Replication**: cross‑account governed copy of S3 data.
    *   **D Transfer Family**: FTPS endpoint for third parties into S3.
*   ⚠️ Works
    *   **E File Gateway**: for shared access patterns, not asked directly.
*   ❌ Distractor
    *   **F DMS**: no DB requirement.

***

## Q6 — Correct picks: **A, B, C**

*   ✅ **Correct**
    *   **A DMS**: Oracle replication for minimal downtime.
    *   **B DataSync**: SMB file migration + incremental sync to cutover.
    *   **C File Gateway**: keep file share access while S3 becomes system of record (hybrid access).
*   ⚠️ Works
    *   **D Snowball**: only if network transfer is constrained (not stated).
*   ❌ Distractors
    *   **E Transfer Family**: partner exchange not requested.
    *   **F AWS Backup**: backup/restore ≠ migration/sync + cutover tooling.

***

# ⚡ Quick Pattern Map (Your “Reflex”)

*   **Bulk offline seed** → **Snowball**
*   **File move/sync (NFS/SMB, TBs, scheduled/incremental)** → **DataSync**
*   **Hybrid file share access backed by S3** → **Storage Gateway (File Gateway)**
*   **DB replication/migration** → **DMS**
*   **Partner-managed SFTP/FTPS/FTP endpoint** → **Transfer Family**
*   **S3-to-S3 DR** → **S3 Replication**
*   **Streaming events** → **Kinesis**

***

