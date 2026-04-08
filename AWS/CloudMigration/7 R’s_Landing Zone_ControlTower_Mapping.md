Excellent topic — this mapping is **very frequently tested in SAP‑C02** and also mirrors **how real AWS migration programs are run**.

Below is an **explicit, strategy‑level mapping of the 7 R’s to AWS Landing Zone + Control Tower**, including:

*   Where each **R fits in the Landing Zone**
*   Which **accounts / OUs are involved**
*   What **changes (or non‑changes)** happen to the Landing Zone
*   **Exam traps**

***

# ✅ 7 R’s → AWS Landing Zone + Control Tower Mapping

## Mental Model First (Exam Gold)

> **Landing Zone + Control Tower is NOT about migration mechanics.**  
> It provides:
>
> *   Account structure
> *   Guardrails
> *   Governance
> *   Network baseline
>
> The **7 R’s decide *what* moves and *how***  
> Control Tower decides **where it lands and under what controls**

***

## 1️⃣ REHOST → Landing Zone Mapping

### Migration Nature

*   Fast, large‑scale EC2 moves
*   Minimal app change
*   High volume of workloads

### Control Tower / LZ Impact

✅ **Heavy usage of standard workload accounts**

### Typical Setup

*   **OU:** `Prod`, `NonProd`
*   **Accounts:** One or more workload accounts
*   **Services used:**
    *   AWS Application Migration Service (MGN)
    *   EC2 in workload accounts
*   **Guardrails:** Mandatory + strongly recommended

### Exam Signal

> “Lift and shift hundreds of servers rapidly”

✅ **Control Tower must already exist** before this starts

***

## 2️⃣ REPLATFORM → Landing Zone Mapping

### Migration Nature

*   Minor changes (RDS, Beanstalk, ECS)
*   Reduced ops burden

### Control Tower / LZ Impact

✅ Same Landing Zone as Rehost  
✅ Slightly **more IAM and service‑level permissions**

### Typical Setup

*   **OU:** `Prod`, `NonProd`
*   **Accounts:** App + DB separation sometimes
*   **Services:**
    *   RDS / Aurora
    *   Elastic Beanstalk / ECS
*   **Guardrails:** Especially **data protection** and **logging**

### Exam Signal

> “Move database to RDS while keeping app logic intact”

***

## 3️⃣ REFACTOR → Landing Zone Mapping

### Migration Nature

*   Cloud‑native redesign
*   Microservices / event‑driven
*   Many managed services

### Control Tower / LZ Impact

✅ **Strongest dependency on Control Tower governance**

### Typical Setup

*   **OU:** `Prod`, `NonProd`, sometimes separate `Platform`
*   **Accounts:**
    *   Microservice‑aligned workload accounts
    *   Shared services account heavily used
*   **Services:**
    *   Lambda, API Gateway
    *   DynamoDB, EventBridge, SQS
*   **Guardrails:**
    *   Prevent public S3
    *   Mandatory encryption
    *   Centralized logging (Log Archive)

### Exam Signal

> “Cloud‑native, event‑driven, serverless”

✅ Control Tower is **critical** here

***

## 4️⃣ REPURCHASE → Landing Zone Mapping

### Migration Nature

*   Custom app replaced by SaaS
*   Minimal AWS infrastructure

### Control Tower / LZ Impact

✅ **Minimal workload account usage**  
✅ Mostly **identity + connectivity**

### Typical Setup

*   **OU:** Often `Security` or `Shared Services`
*   **Accounts:**
    *   Identity‑focused account
*   **Services:**
    *   IAM Identity Center
    *   Directory Service
    *   Networking via existing LZ
*   **Guardrails:** Mostly identity and security‑focused

### Exam Signal

> “Replace CRM with SaaS”

⚠️ Trap: **Landing Zone still exists**, but workload accounts may not

***

## 5️⃣ RETIRE → Landing Zone Mapping

### Migration Nature

*   Application is decommissioned
*   No target workload

### Control Tower / LZ Impact

✅ **No new accounts required**
✅ Often happens **before** Landing Zone finalization

### Typical Setup

*   **Tools:**
    *   Application Discovery Service
    *   Migration Hub
*   **Action:**
    *   App never lands in LZ

### Exam Signal

> “App no longer provides business value”

✅ Retire reduces Landing Zone complexity

***

## 6️⃣ RETAIN → Landing Zone Mapping

### Migration Nature

*   App cannot move *now*
*   Hybrid architecture

### Control Tower / LZ Impact

✅ **Landing Zone still created**
✅ Hybrid connectivity becomes critical

### Typical Setup

*   **OU:** `Shared Services`
*   **Accounts:**
    *   Network / connectivity accounts
*   **Services:**
    *   AWS Outposts
    *   Direct Connect
    *   Storage Gateway
*   **Guardrails:** Network + logging heavy

### Exam Signal

> “Regulatory or latency restrictions”

⚠️ Trap: Retain does NOT mean “no Control Tower”

***

## 7️⃣ RELOCATE → Landing Zone Mapping (⭐ SAP‑C02 Favorite)

### Migration Nature

*   VMware environments moved *as‑is*
*   Entire data center blocks

### Control Tower / LZ Impact

✅ **Separate OU is common**
✅ Clear isolation from native AWS workloads

### Typical Setup

*   **OU:** `VMware-Prod`, `VMware-NonProd`
*   **Accounts:** Dedicated for VMware Cloud on AWS
*   **Services:**
    *   VMware Cloud on AWS
    *   Direct Connect
*   **Guardrails:** Network + security required, app controls unchanged

### Exam Signal

> “Move VMware workloads with no changes”

✅ Control Tower governs **accounts**, not guest OS

***

# 📊 One‑Glance Mapping Table (Exam Ready)

| R          | Landing Zone Usage | Control Tower Role          |
| ---------- | ------------------ | --------------------------- |
| Rehost     | Heavy              | Account vending, guardrails |
| Replatform | Heavy              | Service‑level governance    |
| Refactor   | Very heavy         | Security, logging, IAM      |
| Repurchase | Light              | Identity + access           |
| Retire     | None               | Used for discovery          |
| Retain     | Medium             | Hybrid connectivity         |
| Relocate   | Medium‑heavy       | Account & network isolation |

***

# 🔥 SAP‑C02 Exam Traps

### Trap 1: “Do we need Control Tower if refactoring?”

✅ **YES — especially then**

***

### Trap 2: “Retain means skip Landing Zone”

❌ **Wrong** – Landing Zone still needed for hybrid accounts

***

### Trap 3: “Relocate doesn’t need governance”

❌ **Wrong** – Accounts still need guardrails

***

## ✅ Final Exam Sound‑Bite

> **The 7 R’s decide the migration strategy.  
> Landing Zone + Control Tower decide governance, isolation, and security — regardless of the R.**

***

