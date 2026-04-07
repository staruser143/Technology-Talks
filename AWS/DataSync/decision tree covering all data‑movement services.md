Below is a **single, unified decision tree** that covers **ALL commonly‑tested AWS data‑movement services**, centered on **SAP‑C02 thinking**.  
This is meant to be a **one‑glance master selector** you can mentally run during the exam.

I’ve grouped services by **data type + movement pattern**, which is how AWS expects you to reason.

***

# ✅ Master AWS Data‑Movement Decision Tree (SAP‑C02)

    START
     │
     ├── What kind of data are you moving?
     │
     ├── DATABASE data (tables, rows, CDC)?
     │     │
     │     ├── Need ongoing replication / minimal downtime?
     │     │        ├── YES → AWS DMS (+ SCT if heterogeneous)
     │     │        └── NO  → Logical backup / restore (outside this tree)
     │     │
     │     └── (END)
     │
     └── FILES / OBJECTS / FILE SYSTEM data
           │
           ├── Is the network INADEQUATE or transfer OFFLINE?
           │     │
           │     ├── YES → AWS Snowball / Snowcone / Snowmobile
           │     │        - One‑time bulk transfer
           │     │        - 10s TB → PB scale
           │     │
           │     └── NO  → Network transfer is feasible
           │
           ├── Do ON‑PREM applications need LIVE, LOW‑LATENCY access
           │   using NFS / SMB / iSCSI with no refactor?
           │     │
           │     ├── YES → AWS Storage Gateway
           │     │        - File Gateway (NFS / SMB → S3)
           │     │        - Volume Gateway (iSCSI block)
           │     │        - Tape Gateway (backup / archive)
           │     │
           │     └── NO  → No live hybrid access needed
           │
           ├── Is this LARGE‑SCALE FILE MIGRATION or ONGOING FILE SYNC
           │   with metadata preservation?
           │     │
           │     ├── YES → AWS DataSync
           │     │        - NFS/SMB ↔ S3/EFS/FSx
           │     │        - One‑time + incremental
           │     │
           │     └── NO  → Continue
           │
           ├── Is the source ALREADY S3?
           │     │
           │     ├── YES →
           │     │     ├── Need cross‑account / cross‑region replication?
           │     │     │        └── Amazon S3 Replication
           │     │     │
           │     │     └── Need event‑based or filtered movement?
           │     │              └── S3 + Lambda / EventBridge
           │     │
           │     └── NO  → Continue
           │
           ├── Are users or partners UPLOADING / DOWNLOADING files
           │   using standard file‑transfer protocols?
           │     │
           │     ├── YES → AWS Transfer Family
           │     │        - SFTP / FTPS / FTP
           │     │        - Backed by S3 or EFS
           │     │
           │     └── NO  → Continue
           │
           ├── Is the data CONTINUOUSLY STREAMING in real time?
           │     │
           │     ├── YES → Kinesis / MSK / IoT Core
           │     │
           │     └── NO  → Custom / edge‑case solution
           │
           └── (END)

***

# 🧠 How SAP‑C02 EXPECTS You to Eliminate

## Layer‑based thinking (very important)

| Layer                           | If you see this…           | Immediately think…  |
| ------------------------------- | -------------------------- | ------------------- |
| **Database layer**              | Tables, schemas, CDC       | **DMS**             |
| **Offline / bandwidth‑limited** | PBs, weeks over WAN        | **Snowball**        |
| **Hybrid access layer**         | NFS/SMB/iSCSI, no refactor | **Storage Gateway** |
| **Movement / sync layer**       | Migration or deltas        | **DataSync**        |
| **S3 → S3 logistics**           | Replication rules          | **S3 Replication**  |
| **Human file transfers**        | SFTP / FTP                 | **Transfer Family** |
| **Events / streams**            | Real‑time ingestion        | **Kinesis / MSK**   |

***

# 🚨 Ultra‑Common SAP‑C02 Traps (Burn These In)

❌ DataSync for databases  
✅ DMS

❌ Snowball for weekly sync  
✅ DataSync

❌ Storage Gateway for one‑time migration  
✅ DataSync or Snowball

❌ Transfer Family for large‑scale migration  
✅ DataSync / Snowball

❌ DataSync when apps need live access  
✅ Storage Gateway

***

# ✅ One‑Line Master Reflex (Exam Gold)

    Database? → DMS
    Offline or PB‑scale? → Snowball
    Hybrid live access? → Storage Gateway
    File migration or sync? → DataSync
    S3 → S3? → S3 Replication
    User file transfers? → Transfer Family
    Streaming? → Kinesis / MSK

***
