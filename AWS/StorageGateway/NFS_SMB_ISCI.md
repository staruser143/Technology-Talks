Here are the **full forms and meanings**, explained in **plain + exam‑ready language**, since these terms come up constantly in **Storage Gateway / hybrid storage** questions.

***

## NFS

**NFS = Network File System**

**What it is:**  
A **file‑level** network protocol that lets a client access files on a remote server **as if they were local files**.

**Key traits:**

*   Developed by **Sun Microsystems**
*   Common in **Linux / Unix** environments
*   Works at **file level** (files & directories)
*   Uses TCP/IP over the network

**Typical use:**

*   Linux servers mounting shared directories
*   Home directories, shared application data
*   AWS File Gateway (NFS mode)

**Exam cue:**

> “Linux servers mount a shared directory using NFS”

***

## SMB

**SMB = Server Message Block**

*(Originally called CIFS – Common Internet File System)*

**What it is:**  
A **file‑level** network protocol primarily used by **Windows** systems for file and printer sharing.

**Key traits:**

*   Native to **Windows**
*   Works at **file level**
*   Supports file locking, permissions, authentication
*   Used heavily in enterprise file shares

**Typical use:**

*   Windows file servers
*   Active Directory‑integrated file shares
*   AWS File Gateway (SMB mode)

**Exam cue:**

> “Windows users access shared folders using SMB”

***

### NFS vs SMB (Quick Recall)

| Aspect               | NFS          | SMB     |
| -------------------- | ------------ | ------- |
| OS focus             | Linux / Unix | Windows |
| Storage level        | File         | File    |
| Common in AWS        | Yes          | Yes     |
| Used by File Gateway | ✅            | ✅       |

***

## iSCSI

**iSCSI = Internet Small Computer Systems Interface**

**What it is:**  
A **block‑level** storage protocol that sends **SCSI commands over IP networks**.

**Key traits:**

*   Presents storage as a **raw disk**
*   OS formats it (NTFS, ext4, XFS, etc.)
*   Used for databases, VMs, legacy block apps
*   Common in SAN environments

**Typical use:**

*   Mounting remote disks
*   Databases requiring block storage
*   AWS **Volume Gateway**
*   AWS **Tape Gateway**

**Exam cue:**

> “Application requires block storage and connects via iSCSI”

***

## File vs Block (This Is Exam Gold)

| Feature              | File (NFS/SMB)  | Block (iSCSI)       |
| -------------------- | --------------- | ------------------- |
| Access               | Files & folders | Raw disk            |
| OS formats?          | No              | Yes                 |
| Typical apps         | File shares     | Databases           |
| Storage Gateway type | File Gateway    | Volume/Tape Gateway |

***

## One‑Line Memory Trick 🧠

*   **NFS / SMB** → *“Give me a file”*
*   **iSCSI** → *“Give me a disk”*

***

## How This Maps to AWS Storage Gateway

| Gateway Type       | Protocol    |
| ------------------ | ----------- |
| **File Gateway**   | NFS / SMB   |
| **Volume Gateway** | iSCSI       |
| **Tape Gateway**   | iSCSI (VTL) |

***

## SAP‑C02 Exam Signals (Very Common)

If you see:

*   **“Mount a file system”** → NFS / SMB
*   **“Windows file share”** → SMB
*   **“Linux file share”** → NFS
*   **“Block device / disk / LUN”** → iSCSI

***

