Below is a **clear, exam‑ready decision tree** answering exactly:

> **When should you use a Cognito User Pool?  
> When should you use a Cognito Identity Pool?  
> When should you use BOTH?**

I’ll give you:

1.  A **primary decision tree** (memorization‑friendly)
2.  A **condensed recall version** (exam mode)
3.  A **mapping table to real AWS architectures**
4.  **Exam traps & clarifications**

***

# ✅ Cognito Decision Tree

### *User Pool vs Identity Pool vs Both*

***

## 🧠 PRIMARY DECISION TREE (Authoritative)

    START
     │
     │ Do you need users to SIGN UP / SIGN IN?
     │
     ├── NO ──► Use Identity Pool only
     │          (Unauthenticated / Guest access)
     │
     └── YES
          │
          │ Do you only need to authenticate users
          │ and protect APIs?
          │
          ├── YES ──► Use USER POOL ONLY
          │
          └── NO
               │
               │ Do users need DIRECT ACCESS
               │ to AWS services (S3, DynamoDB, SNS)?
               │
               ├── YES ──► Use BOTH
               │          User Pool + Identity Pool
               │
               └── NO ──► Use USER POOL ONLY

***

# 🧩 Interpretation (What AWS really means)

### 🔐 **User Pool answers**

> ✅ *Who is the user?*  
> ✅ *Are they authenticated?*

### 🪪 **Identity Pool answers**

> ✅ *What AWS resources can this user access?*  
> ✅ *With which IAM role?*

***

# 🧠 CONDENSED EXAM‑RECALL VERSION

    Need login? → User Pool
    Need AWS access? → Identity Pool
    Need BOTH login + AWS access? → BOTH

Memorize this. It wins exams.

***

# 🛠️ ARCHITECTURE‑MAPPED DECISION TABLE

| Architecture Need                | Correct Choice  |
| -------------------------------- | --------------- |
| User login (email/password)      | ✅ User Pool     |
| Social login (Google, Apple)     | ✅ User Pool     |
| JWT token for APIs               | ✅ User Pool     |
| Protect API Gateway / ALB        | ✅ User Pool     |
| Guest / anonymous access         | ✅ Identity Pool |
| Direct S3 upload from mobile app | ✅ Identity Pool |
| IAM roles per user group         | ✅ Identity Pool |
| Mobile app accessing DynamoDB    | ✅ BOTH          |
| SPA + API Gateway backend        | ✅ User Pool     |
| Mobile app + S3 + API            | ✅ BOTH          |

***

# 🧩 WHY “BOTH” IS SO COMMON (Critical Insight)

Most real apps need **authentication + AWS access**.

### Example:

✅ Mobile App

*   User signs in → **User Pool**
*   Gets JWT ✅
*   Needs to upload files to S3 ✅  
    ➡️ Requires **temporary IAM credentials**  
    ➡️ Issued via **Identity Pool**

📌 **Rule**

> User Pools **DO NOT issue AWS credentials**  
> Identity Pools **DO NOT authenticate users**

Hence → **BOTH**

***

# 🧠 VISUAL FLOW FOR “BOTH”

    User
     │
     ▼
    Cognito User Pool
     │  (AuthN, JWT)
     ▼
    Cognito Identity Pool
     │  (Role mapping)
     ▼
    IAM Role
     │
     ▼
    AWS Services (S3, DynamoDB, SNS)

***

# ⚠️ EXAM TRAPS (Very Important)

### ❌ Trap 1: “User Pool can access S3 directly”

**Wrong**

*   User Pool → JWT only
*   No IAM credentials

✅ Correct: Use Identity Pool

***

### ❌ Trap 2: “Identity Pool replaces User Pool”

**Wrong**

*   Identity Pool assumes identity is already verified

✅ Correct:

*   Identity Pool may use:
    *   Cognito User Pool
    *   Social IdP
    *   SAML IdP
    *   Or *unauthenticated* guest

***

### ❌ Trap 3: “IAM can replace Cognito for users”

**Wrong**

*   IAM ≠ user sign‑in service
*   IAM is for **workloads & services**

***

# ✅ EXAM‑GRADE ONE‑LINERS

*   **User Pool** = Authentication (OIDC / OAuth / JWT)
*   **Identity Pool** = Authorization (IAM roles)
*   **Both** = Most production apps

***

