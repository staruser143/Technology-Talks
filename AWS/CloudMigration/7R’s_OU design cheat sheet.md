Here is an **exam‑grade, single‑page “7 R’s × AWS Organizations / OU Design” cheat sheet**, tuned for **SAP‑C02** and real enterprise migrations.

***

# ✅ 7 R’s × OU Design Cheat Sheet (AWS)

> **Goal:** Align **migration strategy** with **AWS Organizations OU layout, guardrails, billing, and blast‑radius control**

***

## 1️⃣ Rehost (Lift & Shift)

**OU Pattern**

    Root
    └── Workloads
        └── Rehosted
            ├── Prod
            └── NonProd

**Design Intent**

*   Fast migration
*   Minimal change
*   Temporary landing zone

**Controls**

*   ✅ SCPs: *Prevent drift only*
    *   Deny deleting CloudTrail, Config
    *   Prevent disabling GuardDuty
*   ❌ Avoid restrictive SCPs (migration friction)

**Billing**

*   Cost center = legacy app
*   Short‑term cost visibility

**Exam Cue**

> “Company wants fastest migration with minimal changes”

✅ **Rehost + loosely governed OU**

***

## 2️⃣ Replatform (Lift, Tinker & Shift)

**OU Pattern**

    Root
    └── Workloads
        └── Replatformed
            ├── Prod
            └── NonProd

**Design Intent**

*   Managed services adoption (RDS, ALB, SQS)
*   Still close to legacy

**Controls**

*   SCPs to:
    *   ✅ Enforce approved regions
    *   ✅ Restrict unsupported DB engines
*   Service Control is moderate

**Billing**

*   App + platform costs split
*   Tagging SCPs often enabled

**Exam Cue**

> “Small optimizations without refactoring”

✅ **Replatform + moderate SCP enforcement**

***

## 3️⃣ Refactor / Re‑architect

**OU Pattern**

    Root
    └── Workloads
        └── Cloud-Native
            ├── Prod
            └── NonProd

**Design Intent**

*   Long‑term cloud maturity
*   Microservices, serverless, events

**Controls**

*   ✅ Strong SCPs:
    *   Enforce IAM roles only (no users)
    *   Mandatory logging & encryption
    *   Restrict EC2 instance types
*   Organizational guardrails are strict

**Billing**

*   Fine‑grained tagging
*   Chargeback / showback mature

**Exam Cue**

> “Modern, scalable, event‑driven architecture”

✅ **Refactor + strongly governed OU**

***

## 4️⃣ Repurchase (SaaS / Drop & Shop)

**OU Pattern**

    Root
    └── SaaS-Only

**Design Intent**

*   Minimal AWS usage
*   Identity & cost only

**Controls**

*   SCPs:
    *   ❌ Deny EC2, RDS, VPC
    *   ✅ Allow IAM, SSO, billing only

**Billing**

*   Mostly SaaS subscription
*   Minimal AWS spend

**Exam Cue**

> “Replace custom app with SaaS”

✅ **Repurchase + locked‑down OU**

***

## 5️⃣ Retire

**OU Pattern**

    Root
    └── Suspended / Retired

**Design Intent**

*   Kill cost
*   Preserve audit trail

**Controls**

*   SCP:
    *   ❌ Deny *all* actions except billing, support
*   No workloads allowed

**Billing**

*   Near zero

**Exam Cue**

> “Application no longer needed”

✅ **Retire + quarantine / suspended OU**

***

## 6️⃣ Retain (Revisit Later)

**OU Pattern**

    Root
    └── On-Prem / Retained

*(Often **outside AWS** or empty placeholder OU)*

**Design Intent**

*   Compliance / latency / licensing issues
*   Future migration planning

**Controls**

*   If AWS accounts exist:
    *   SCP denies provisioning new resources
*   Often connected via:
    *   Direct Connect
    *   VPN

**Billing**

*   Minimal or none

**Exam Cue**

> “Not ready for cloud yet”

✅ **Retain + placeholder or no OU**

***

## 7️⃣ Relocate (VMware Cloud on AWS)

**OU Pattern**

    Root
    └── VMware
        └── VMC-Prod
        └── VMC-NonProd

**Design Intent**

*   Bulk VM migration
*   No refactoring
*   Fast exit from data center

**Controls**

*   SCPs:
    *   ✅ Allow VMC‑specific services
    *   ❌ Block EC2 to avoid parallel stacks

**Billing**

*   Separate OU strongly recommended
*   Easy cost isolation

**Exam Cue**

> “Move vSphere environment without changes”

✅ **Relocate + dedicated VMware OU**

***

# 🔎 Mega Summary Table

| R          | OU Style               | SCP Strength | Exam Key Phrase     |
| ---------- | ---------------------- | ------------ | ------------------- |
| Rehost     | Workloads/Rehosted     | Light        | Fastest migration   |
| Replatform | Workloads/Replatformed | Medium       | Small optimizations |
| Refactor   | Cloud‑Native           | Strong       | Cloud‑native        |
| Repurchase | SaaS‑Only              | Very Strong  | SaaS replacement    |
| Retire     | Suspended              | Full deny    | Decommission        |
| Retain     | Placeholder            | Minimal      | Not ready           |
| Relocate   | VMware                 | Medium       | VMC lift            |

***

## 🧠 SAP‑C02 Exam Traps to Remember

*   ❌ Putting **Rehost + strict SCPs** = wrong
*   ✅ **Relocate ≠ Rehost**
    *   Relocate = VMware Cloud on AWS
*   ❌ Mixing **VMware + EC2** in same OU
*   ✅ Separate OU = **blast radius + billing clarity**

***

