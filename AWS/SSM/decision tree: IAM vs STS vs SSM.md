Here’s a **clear, exam‑grade decision tree** that helps you decide **when to use IAM vs STS vs SSM**, and—more importantly—how AWS expects you to **mentally separate their responsibilities**.

This is one of the **highest‑value trees** for SAA / SAP.

***

# 🌳 Decision Tree: IAM vs STS vs SSM

    START
    │
    ├─▶ Are you defining WHO can do WHAT in AWS?
    │     │
    │     ├─ YES
    │     │   └─▶ Use IAM
    │     │         ├─ Users, Groups, Roles
    │     │         ├─ Policies (Allow / Deny)
    │     │         └─ Conditions (MFA, time, tags)
    │     │
    │     └─ NO
    │
    ├─▶ Do you need TEMPORARY credentials?
    │     │
    │     ├─ YES
    │     │   └─▶ Use STS
    │     │         ├─ AssumeRole
    │     │         ├─ Federation (SAML/OIDC)
    │     │         └─ No long‑lived credentials
    │     │
    │     └─ NO
    │
    ├─▶ Do you need to ACCESS or OPERATE ON EC2 / servers?
    │     │
    │     ├─ YES
    │     │   └─▶ Use AWS Systems Manager (SSM)
    │     │         ├─ Session Manager (shell access)
    │     │         ├─ Run Command (execute scripts)
    │     │         ├─ Patch Manager (OS patching)
    │     │         └─ Automation (runbooks)
    │     │
    │     └─ NO
    │
    └─▶ Re-check problem statement
          └─▶ Most real scenarios use ALL THREE together

***

## ✅ Ultra‑Short Mental Model (Memorize)

| Service | Primary Question It Answers              |
| ------- | ---------------------------------------- |
| **IAM** | *Who is allowed to do what?*             |
| **STS** | *How do they get temporary credentials?* |
| **SSM** | *How are actions executed on servers?*   |

***

## 🧠 EXAM GOLD: Layered Responsibility Model

Think in **layers**, not substitutes:

    Identity & Authorization → IAM
    Temporary Credentials    → STS
    Execution on Servers     → SSM

AWS exams LOVE this separation.

***

## 🔍 Deepening the Decision Tree (Exam Scenarios)

### 1️⃣ “We need to control access using policies”

✅ **IAM**

*   Defining:
    *   Permissions
    *   Role boundaries
    *   Conditions (MFA, IP, tags)
*   IAM **does NOT execute anything**

❌ Trap:

> “IAM lets you log into EC2”

→ IAM **authorizes**, it does NOT **operate**

***

### 2️⃣ “Users should not have long‑lived credentials”

✅ **STS**

*   Role assumption
*   Federation (Entra ID, Okta, ADFS)
*   Short‑lived tokens

❌ Trap:

> “STS gives access to EC2 shells”

→ STS gives **credentials**, not **execution**

***

### 3️⃣ “Admins need shell access without SSH”

✅ **SSM Session Manager**

*   No inbound ports
*   IAM‑controlled
*   Fully logged

❌ Trap:

> “Use STS to access EC2”

→ EC2 does **not** consume STS directly

***

## 🎯 HIGH‑CONFIDENCE COMBINED DECISION RULE

> If the scenario includes **humans + temporary access + EC2 operations**,  
> **the correct answer uses IAM + STS + SSM together.**

***

## 🚨 Exam Trap Matrix

| Misconception    | Why It’s Wrong                  |
| ---------------- | ------------------------------- |
| IAM replaces SSM | ❌ IAM can’t execute commands    |
| STS replaces IAM | ❌ STS relies on IAM roles       |
| SSM replaces STS | ❌ Humans still need credentials |
| Bastion needed   | ❌ SSM removes inbound access    |

***

## ✅ Typical Real‑World / Exam Flow

    User authenticates (SSO)
      → IAM Role defined
        → STS issues temporary creds
          → User calls SSM APIs
            → SSM Agent runs commands on EC2

***

## 📌 One‑Line Exam Summary (Memorize Word‑for‑Word)

> **IAM defines permissions, STS provides temporary credentials, and SSM securely executes operational actions on EC2 and hybrid servers.**

***

