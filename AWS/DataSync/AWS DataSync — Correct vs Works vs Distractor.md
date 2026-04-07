Below is an **exam‑oriented “Correct vs Works vs Distractor” table** specifically for **AWS DataSync**, framed the way it appears in **SAP‑C02 / Pro‑level scenario questions**.

***

## AWS DataSync — Correct vs Works vs Distractor

| Requirement / Problem Statement                                                     | ✅ **Correct (Use DataSync)** | ⚠️ **Works (But Not Best)**                      | ❌ **Distractor (Wrong Tool)**    |
| ----------------------------------------------------------------------------------- | ---------------------------- | ------------------------------------------------ | -------------------------------- |
| Migrate **100+ TB** of files from on‑prem **NFS** to **Amazon S3** over the network | **AWS DataSync**             | Snowball Edge (offline, slower to repeat)        | AWS DMS (DB‑only, not file data) |
| Keep **on‑prem SMB share** incrementally synced to **Amazon EFS** daily             | **AWS DataSync**             | Storage Gateway (file gateway adds latency)      | S3 Replication (S3‑only)         |
| High‑throughput, automated, fault‑tolerant data transfer                            | **AWS DataSync**             | rsync on EC2 (manual, fragile)                   | SCP / FTP scripts                |
| Preserve **file metadata, ownership, timestamps** during migration                  | **AWS DataSync**             | Custom rsync flags (error‑prone)                 | AWS Glue (ETL ≠ file copy)       |
| Sync data from **S3 (Account A)** to **S3 (Account B)**                             | **AWS DataSync**             | S3 Replication (single‑direction, bucket‑scoped) | VPC Peering                      |
| Replicate file data from **AWS → AWS** across Regions                               | **AWS DataSync**             | EFS Replication (EFS only)                       | CloudFormation                   |
| Repeated, scheduled file synchronization                                            | **AWS DataSync**             | Snowball (manual, batch only)                    | AWS Transfer Family              |
| Secure transfer with **encryption in transit + audit logs**                         | **AWS DataSync**             | Custom VPN + scripts                             | Public S3 access                 |
| Migrate data from **FSx for Windows** to **S3**                                     | **AWS DataSync**             | Manual copy via EC2                              | AWS Backup (restore ≠ migration) |
| Move large datasets to S3 for **Athena analytics**                                  | **AWS DataSync**             | Storage Gateway (adds caching layer)             | Kinesis Data Streams             |
| Minimal ops effort, no servers to manage                                            | **AWS DataSync**             | EC2‑based copy tools                             | Self‑hosted NAS mirroring        |

***

## Common Exam Traps (Why Options Fail)

| Trap Option         | Why It’s a Distractor                               |
| ------------------- | --------------------------------------------------- |
| **AWS DMS**         | Only for databases (logical replication), not files |
| **Storage Gateway** | Hybrid access layer, not optimized bulk migration   |
| **Snowball**        | Physical transfer; wrong for recurring sync         |
| **AWS Backup**      | Backup/restore, not flexible sync or migration      |
| **Transfer Family** | Protocol access (SFTP/FTPS), not bulk sync          |
| **S3 Replication**  | S3‑only, no on‑prem or file system support          |
| **Glue**            | ETL processing, not raw file movement               |

***

## One‑Glance Exam Rule

> **If the problem says:** *large file sets, NFS/SMB, sync, migrate, incremental, on‑prem ↔ AWS*  
> **Your reflex answer:** ✅ **AWS DataSync**

***



Just say which one.
