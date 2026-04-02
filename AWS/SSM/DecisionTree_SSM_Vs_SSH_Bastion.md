Here’s a **clean, exam‑ready decision tree** to choose between **AWS SSM, SSH, and Bastion Hosts**, optimized for **SAA / SAP / real architecture recall**.

***

# 🌳 SSM vs SSH vs Bastion — Decision Tree

    START
    │
    ├─▶ Do you need to access/manage EC2 or servers?
    │
    ├─▶ Do you want to AVOID opening inbound ports (22/3389)?
    │     │
    │     ├─ YES
    │     │   └─▶ Use AWS Systems Manager (SSM)
    │     │         │
    │     │         ├─ Secure shell access? → SSM Session Manager
    │     │         ├─ Run commands at scale? → SSM Run Command
    │     │         ├─ Automation / remediation? → SSM Automation
    │     │         └─ Patching / compliance? → Patch Manager
    │     │
    │     └─ NO
    │
    ├─▶ Is this a SMALL, non‑production, short‑lived workload?
    │     │
    │     ├─ YES
    │     │   └─▶ Direct SSH/RDP
    │     │         ├─ Fast setup
    │     │         ├─ Manual key management
    │     │         └─ Not scalable / weak auditability
    │     │
    │     └─ NO
    │
    ├─▶ Do multiple admins need controlled access via one entry point?
    │     │
    │     ├─ YES
    │     │   └─▶ Bastion Host
    │     │         ├─ Central jump server
    │     │         ├─ Needs patching, hardening
    │     │         └─ Still exposes SSH/RDP
    │     │
    │     └─ NO
    │
    └─▶ Default / Best Practice
          └─▶ Use AWS SSM

***

# ✅ One‑Line Rule (EXAM GOLD)

> **If AWS mentions security, auditability, scale, or “no inbound ports” → SSM is the answer.**

***

# 🧠 Quick Mental Shortcut (3‑Second Recall)

| Question            | Answer        |
| ------------------- | ------------- |
| No SSH keys?        | ✅ **SSM**     |
| No open ports?      | ✅ **SSM**     |
| Needs logging & IAM | ✅ **SSM**     |
| Quick dev box       | ✅ **SSH**     |
| Legacy access model | ✅ **Bastion** |

***

# 🔍 Expanded Decision Logic (Why AWS Wants SSM)

## 1️⃣ Choose **AWS SSM** when:

*   Security-first architecture
*   IAM‑controlled access
*   Fleet‑scale management
*   Hybrid (AWS + on‑prem)
*   Audit & compliance required

✅ **AWS Preferred Architecture**

***

## 2️⃣ Choose **SSH/RDP** when:

*   Single EC2
*   Temporary debugging
*   No compliance requirements
*   Dev / sandbox only

⚠️ **Never recommended for prod**

***

## 3️⃣ Choose **Bastion Host** when:

*   Legacy pattern
*   SSH tools required
*   No SSM Agent allowed
*   Transitional architecture

⚠️ **Operational & security overhead**

***

# ⚠️ Exam Trap Scenarios

### ❌ WRONG

> “Use a Bastion Host for secure access”

### ✅ CORRECT

> “Use **SSM Session Manager** with IAM and CloudWatch logging”

***

### ❌ WRONG

> “Allow SSH from corporate IP range”

### ✅ CORRECT

> “No inbound access — use **SSM**”

***

# 🧪 SAP‑C02 / SAA Trick Question Pattern

**Question stem keywords → SSM**

*   “No inbound rules”
*   “Auditable access”
*   “At scale”
*   “Hybrid”
*   “IAM‑based control”

***

# 📌 Final Exam Summary (Memorize)

| Method  | When to Use        |
| ------- | ------------------ |
| **SSM** | **Default choice** |
| SSH     | Dev / temporary    |
| Bastion | Legacy only        |

***

Just say the word.
