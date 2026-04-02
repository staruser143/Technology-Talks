Here’s a **crisp, exam‑grade decision tree** clarifying **AWS IAM Identity Center vs IAM vs STS**—focused on **who authenticates, who authorizes, and who issues credentials**.

This tree removes the most common confusion seen in **SAA / SAP‑C02**.

***

# 🌳 Decision Tree: Identity Center vs IAM vs STS

    START
    │
    ├─▶ Are we dealing with HUMAN users (employees, admins, developers)?
    │     │
    │     ├─ YES
    │     │   └─▶ Do they authenticate via an external IdP (Entra ID, Okta)?
    │     │         │
    │     │         ├─ YES
    │     │         │   └─▶ Use IAM Identity Center
    │     │         │         ├─ SSO experience
    │     │         │         ├─ User & group assignments
    │     │         │         └─ Maps users to IAM roles
    │     │         │
    │     │         └─ NO
    │     │             └─▶ Use IAM Users (legacy / small setups)
    │     │
    │     └─ NO
    │
    ├─▶ Are we defining WHAT actions are allowed in AWS?
    │     │
    │     └─▶ Use IAM
    │           ├─ Policies (Allow / Deny)
    │           ├─ Roles (trusted entities)
    │           └─ Conditions (MFA, IP, tags, time)
    │
    ├─▶ Do we need TEMPORARY credentials?
    │     │
    │     └─▶ Use STS
    │           ├─ AssumeRole
    │           ├─ Federation (SAML / OIDC)
    │           └─ Short‑lived access tokens
    │
    └─▶ Reality Check
          └─▶ Identity Center + IAM + STS are usually used TOGETHER

***

## ✅ 3‑Second Mental Model (EXAM GOLD)

| Service                 | Primary Responsibility               |
| ----------------------- | ------------------------------------ |
| **IAM Identity Center** | *Who signs in*                       |
| **IAM**                 | *What they are allowed to do*        |
| **STS**                 | *How they get temporary credentials* |

***

## 🧠 Layered Responsibility Model (AWS Loves This)

    Authentication (Humans)  → Identity Center
    Authorization (Policies) → IAM
    Credentials (Temporary)  → STS

None of these **replace** each other. They **stack**.

***

## 🔍 Deep Dive with Exam Scenarios

### 1️⃣ IAM Identity Center — *Authentication Layer*

✅ Use it when:

*   Human users
*   Corporate identity provider (Entra ID / Okta)
*   Centralized SSO
*   Multi‑account AWS Organizations

✅ What it does:

*   Authenticates users
*   Assigns users/groups to **IAM roles**
*   Initiates role assumption

❌ What it does NOT do:

*   Does **not** define permissions
*   Does **not** issue AWS API keys

🔑 Exam phrase:

> *“Users authenticate via corporate directory”*

***

### 2️⃣ IAM — *Authorization Layer*

✅ Use it when:

*   You define permissions
*   You control AWS access
*   You need fine‑grained policies

✅ What it does:

*   Defines:
    *   Roles
    *   Policies
    *   Trust relationships
*   Applies to:
    *   Users
    *   Roles
    *   Services

❌ What it does NOT do:

*   Does not authenticate users
*   Does not provide SSO UI
*   Does not issue temporary creds by itself

🔑 Exam phrase:

> *“Control which AWS API actions are allowed”*

***

### 3️⃣ STS — *Temporary Credential Layer*

✅ Use it when:

*   No long‑lived credentials allowed
*   Cross‑account access
*   Federation
*   Role assumption

✅ What it does:

*   Issues:
    *   Temporary access key
    *   Secret key
    *   Session token
*   Enforces short lifetimes

❌ What it does NOT do:

*   Does not store policies
*   Does not manage users

🔑 Exam phrase:

> *“Temporary credentials”, “AssumeRole”, “Federation”*

***

## 🧠 MOST IMPORTANT EXAM RULE

> **Identity Center authenticates → STS issues credentials → IAM enforces permissions**

If any one is missing, the architecture is incomplete.

***

## ⚠️ Ultra‑Common Exam Traps

| Wrong Thought                    | Why It’s Wrong                  |
| -------------------------------- | ------------------------------- |
| Identity Center replaces IAM     | ❌ IAM still defines permissions |
| STS replaces IAM                 | ❌ STS relies on IAM roles       |
| IAM users are best practice      | ❌ Identity Center preferred     |
| Identity Center gives EC2 access | ❌ That’s SSM’s job              |

***

## ✅ Typical Enterprise Flow (Memorize)

    User logs in (Identity Center)
       ↓
    Mapped to IAM Role
       ↓
    STS AssumeRole
       ↓
    Temporary Credentials
       ↓
    AWS API Access (SSM, S3, EC2, etc.)

***

## 📌 One‑Line Exam Summary (High Confidence)

> **IAM Identity Center handles human authentication, IAM defines authorization policies, and STS issues temporary credentials to access AWS services.**

***

Just say the word.
