Below is a **mega‑scenario “Correct vs Works vs Distractor” answer table** for the **6 mega scenarios** I gave (crossing **4–5 branches**: DataSync, DMS, Snowball, Storage Gateway, Transfer Family, S3 Replication, EFS Replication, Kinesis, Glue).

> **Legend**
>
> *   ✅ **Correct** = best-fit service(s) for the stated requirements
> *   ⚠️ **Works** = could work, but not the best / misses key constraints
> *   ❌ **Distractor** = wrong class of tool for the problem

***

# Q1) Data center exit + unreliable WAN + nightly sync + partner SFTP (Select 3)

| Option                                | Verdict      | Why (1‑liner)                                                                  |
| ------------------------------------- | ------------ | ------------------------------------------------------------------------------ |
| **A. AWS DataSync**                   | ✅ Correct    | Handles **incremental nightly sync** SMB/NFS ↔ S3 with verification.           |
| **B. AWS Snowball Edge**              | ✅ Correct    | **600 TB + unreliable/insufficient WAN + deadline** ⇒ offline bulk transfer.   |
| **C. Storage Gateway (File Gateway)** | ⚠️ Works     | Provides SMB/NFS access to S3, but **doesn’t solve fast bulk exit** by itself. |
| **D. AWS Transfer Family**            | ✅ Correct    | Managed **SFTP** endpoint for partners landing into S3.                        |
| **E. AWS DMS**                        | ❌ Distractor | Database replication tool, not file shares.                                    |
| **F. S3 Replication**                 | ⚠️ Works     | Only **S3→S3**; doesn’t ingest from on‑prem SMB/NFS.                           |

✅ **Correct set:** **B + A + D**

***

# Q2) Hybrid app file share + nightly copy to EFS + DB replication + cross‑account DR (Select 4)

| Option                                | Verdict      | Why (1‑liner)                                                           |
| ------------------------------------- | ------------ | ----------------------------------------------------------------------- |
| **A. Storage Gateway (File Gateway)** | ✅ Correct    | Keeps legacy app using **SMB semantics** while backing store is **S3**. |
| **B. AWS DataSync**                   | ✅ Correct    | Scheduled file copy/sync (e.g., **S3↔EFS** or file systems).            |
| **C. AWS DMS**                        | ✅ Correct    | **PostgreSQL → Aurora PostgreSQL** minimal‑downtime replication.        |
| **D. S3 Replication**                 | ✅ Correct    | Native **bucket replication** for DR (cross‑account/region).            |
| **E. AWS Transfer Family**            | ❌ Distractor | Partner protocol endpoint not requested.                                |
| **F. AWS Snowball Edge**              | ❌ Distractor | Offline shipping not required by constraints.                           |

✅ **Correct set:** **A + B + C + D**

***

# Q3) On‑prem NFS → EFS + keep EFS synced cross‑region (Select 2)

| Option                     | Verdict      | Why (1‑liner)                                                         |
| -------------------------- | ------------ | --------------------------------------------------------------------- |
| **A. AWS DataSync**        | ✅ Correct    | Best for **on‑prem NFS → EFS** managed migration + verification.      |
| **B. EFS Replication**     | ✅ Correct    | Purpose‑built **EFS→EFS cross‑region** replication (simple, managed). |
| **C. AWS Snowball Edge**   | ⚠️ Works     | Only if WAN is constrained; not stated here.                          |
| **D. AWS Transfer Family** | ❌ Distractor | Not a migration/sync engine; it’s a protocol endpoint service.        |
| **E. AWS DMS**             | ❌ Distractor | Database‑only.                                                        |

✅ **Best set:** **A + B**  
⚠️ Alternative: **A + A** (DataSync can do EFS↔EFS), but **B** is the clean EFS‑native answer.

***

# Q4) Hourly NFS→S3 + Parquet transform + real‑time clickstream (Select 3)

| Option                                | Verdict      | Why (1‑liner)                                                              |
| ------------------------------------- | ------------ | -------------------------------------------------------------------------- |
| **A. AWS DataSync**                   | ✅ Correct    | Hourly **NFS→S3** file transfer with integrity/verification.               |
| **B. AWS Glue**                       | ✅ Correct    | ETL: convert to **Parquet**, partition, catalog for Athena.                |
| **C. Kinesis Data Streams**           | ✅ Correct    | **Sub‑second streaming** ingestion for click events.                       |
| **D. AWS DMS**                        | ❌ Distractor | Not for file logs or event streams.                                        |
| **E. Storage Gateway (File Gateway)** | ⚠️ Works     | Could expose S3 as a share, but not the best for scheduled bulk sync jobs. |
| **F. AWS Transfer Family**            | ❌ Distractor | Protocol endpoint (SFTP/FTPS), not internal hourly ingestion/sync.         |

✅ **Correct set:** **A + B + C**

***

# Q5) No outbound internet + cross‑account S3 consolidation + 30‑day incremental sync + FTPS (Select 4)

| Option                                | Verdict      | Why (1‑liner)                                                            |
| ------------------------------------- | ------------ | ------------------------------------------------------------------------ |
| **A. AWS Snowball Edge**              | ✅ Correct    | **No outbound internet + 200 TB** ⇒ offline device transfer.             |
| **B. AWS DataSync**                   | ✅ Correct    | After seeding/connectivity: **incremental sync** for 30 days.            |
| **C. S3 Replication**                 | ✅ Correct    | Native **S3→S3** copy into combined account (governed DR/consolidation). |
| **D. AWS Transfer Family**            | ✅ Correct    | Managed **FTPS** endpoint for third‑party exchange into S3.              |
| **E. Storage Gateway (File Gateway)** | ⚠️ Works     | Useful for hybrid access patterns, but not explicitly required.          |
| **F. AWS DMS**                        | ❌ Distractor | No database replication requirement.                                     |

✅ **Correct set:** **A + B + C + D**

***

# Q6) Oracle DB + SMB files + 2‑hour cutover + keep file share alive while S3 becomes system of record (Select 3)

| Option                                | Verdict      | Why (1‑liner)                                                  |
| ------------------------------------- | ------------ | -------------------------------------------------------------- |
| **A. AWS DMS**                        | ✅ Correct    | **Oracle replication** into AWS with minimal downtime.         |
| **B. AWS DataSync**                   | ✅ Correct    | **SMB file migration + incremental** sync until cutover.       |
| **C. Storage Gateway (File Gateway)** | ✅ Correct    | Maintains **file share access** while backing store is **S3**. |
| **D. AWS Snowball Edge**              | ⚠️ Works     | Only if network constraints exist (not stated).                |
| **E. AWS Transfer Family**            | ❌ Distractor | Partner protocol endpoint not requested.                       |
| **F. AWS Backup**                     | ❌ Distractor | Backup/restore, not migration + sync + cutover orchestration.  |

✅ **Correct set:** **A + B + C**

***

## Ultra‑Compact “Reflex Map” (for elimination)

| Keyword in question                                  | Reflex pick                        |
| ---------------------------------------------------- | ---------------------------------- |
| “**NFS/SMB**, TBs, migrate/sync, incremental”        | **DataSync**                       |
| “**Database** replication/migration”                 | **DMS**                            |
| “No bandwidth / no internet / can’t transfer online” | **Snowball**                       |
| “Keep **file share interface** but store in S3”      | **Storage Gateway (File Gateway)** |
| “Partner **SFTP/FTPS/FTP** endpoint”                 | **Transfer Family**                |
| “Bucket DR, S3→S3”                                   | **S3 Replication**                 |
| “EFS→EFS cross‑region”                               | **EFS Replication**                |
| “Real‑time events, sub‑second”                       | **Kinesis**                        |
| “Transform files (Parquet, partitions, catalog)”     | **Glue**                           |

***
