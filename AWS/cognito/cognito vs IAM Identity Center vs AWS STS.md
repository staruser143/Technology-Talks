Below is a **clear, exam‑grade comparison of AWS Cognito vs IAM Identity Center vs AWS STS** — framed the way **AWS expects you to reason** in **Solution Architect / Security exams** and **enterprise designs**.

***

# ✅ Cognito vs IAM Identity Center vs STS

### *(Authentication vs Workforce SSO vs Runtime Credentials)*

***

## 🧠 One‑line mental model (memorize this first)

| Service                 | One‑line purpose                       |
| ----------------------- | -------------------------------------- |
| **Cognito**             | *End‑user identities for applications* |
| **IAM Identity Center** | *Workforce SSO to AWS accounts & apps* |
| **STS**                 | *Temporary credentials for workloads*  |

***

# 🧩 Primary Comparison Table (Exam‑Ready)

| Dimension                  | **Cognito**            | **IAM Identity Center (IC)** | **AWS STS**             |
| -------------------------- | ---------------------- | ---------------------------- | ----------------------- |
| Primary audience           | 👤 App users (B2C/B2B) | 👨‍💼 Employees / workforce  | 🤖 Services & workloads |
| Login UI                   | ✅ Built‑in             | ✅ Redirect/portal            | ❌ None                  |
| User directory             | ✅ Yes                  | ❌ Uses external IdP          | ❌ No users              |
| Authentication             | ✅ Yes                  | ✅ Yes                        | ❌ No                    |
| Authorization              | ✅ Indirect (via roles) | ✅ Yes                        | ✅ Yes                   |
| Issues JWT tokens          | ✅ Yes                  | ❌ No                         | ❌ No                    |
| Issues AWS creds           | ✅ via Identity Pool    | ✅ Yes                        | ✅ Yes                   |
| IAM role assumption        | ✅ Yes                  | ✅ Yes                        | ✅ Core function         |
| Federation                 | Social, OIDC, SAML     | SAML / SCIM                  | SAML, OIDC, AWS         |
| Human login to AWS Console | ❌                      | ✅                            | ❌                       |
| Mobile / SPA friendly      | ✅                      | ❌                            | ❌                       |
| Exam classification        | App Identity           | Workforce Identity           | Credential Broker       |

***

# 🔐 AWS Cognito

### ✅ What Cognito is used for

*   User **sign‑up / sign‑in**
*   OAuth2 / OIDC authentication
*   Token‑based auth for APIs
*   Mobile, SPA, and customer‑facing apps

### ✅ Cognito answers:

> “Who is this user and are they authenticated?”

### ✅ Components recap

*   **User Pool** → Authentication (JWT)
*   **Identity Pool** → Authorization (IAM creds)

📌 Cognito **does not replace IAM** — it *feeds IAM*.

### ✅ Typical usage

*   Customer portals
*   Mobile apps
*   SaaS applications
*   API Gateway authorization

***

# 👨‍💼 IAM Identity Center (formerly AWS SSO)

### ✅ What Identity Center is used for

*   **Human workforce access** to:
    *   AWS Console
    *   Multiple AWS accounts
    *   Business apps (e.g., Salesforce)

### ✅ Identity Center answers:

> “Which employees can access which AWS accounts, with which roles?”

### ✅ Key traits

*   Centralized access across AWS Organization
*   Integrates with:
    *   Azure AD / Entra ID
    *   Okta
    *   On‑prem AD
*   SCIM provisioning
*   Permission sets (role abstraction)

📌 **No tokens, no mobile auth, no app login**

***

# 🔄 AWS STS (Security Token Service)

### ✅ What STS is used for

*   **Temporary AWS credentials**
*   Secure role assumption
*   Cross‑account access

### ✅ STS answers:

> “How can this workload safely get short‑lived credentials?”

### ✅ Core APIs (exam critical)

| API                         | Purpose               |
| --------------------------- | --------------------- |
| `AssumeRole`                | Cross‑account access  |
| `AssumeRoleWithSAML`        | Enterprise federation |
| `AssumeRoleWithWebIdentity` | Cognito / OIDC        |
| `GetSessionToken`           | Temporary MFA creds   |

📌 STS has **no concept of users** — only identities & sessions.

***

# 🧠 HOW THEY WORK TOGETHER (Very Exam‑Important)

***

## ✅ Pattern 1: Mobile App → S3

    User → Cognito User Pool (JWT)
         → Cognito Identity Pool
         → STS
         → IAM Role
         → S3

✅ Cognito authenticates  
✅ STS issues credentials  
✅ IAM enforces permissions

***

## ✅ Pattern 2: Employee → AWS Console

    Employee → Azure AD
             → IAM Identity Center
             → STS
             → IAM Role
             → AWS Console

✅ Identity Center handles SSO  
✅ STS assumes roles

***

## ✅ Pattern 3: EC2 → Cross‑Account Access

    EC2 Instance
     → STS AssumeRole
     → IAM Role
     → Target Account Resources

✅ No Cognito  
✅ No Identity Center

***

# 🚦 Decision Guide (Fast Recall)

    Are they app users? → Cognito
    Are they employees? → Identity Center
    Is this a workload? → STS

***

# ⚠️ Exam Traps to Avoid

### ❌ “Cognito replaces IAM”

Wrong  
→ Cognito **depends on IAM**

***

### ❌ “Identity Center is for app users”

Wrong  
→ It’s for **employees only**

***

### ❌ “STS does authentication”

Wrong  
→ STS only **vends credentials**

***

# ✅ Final Comparison Summary

| If the question says…          | Answer                  |
| ------------------------------ | ----------------------- |
| End users log in to app        | **Cognito**             |
| Employees need SSO             | **IAM Identity Center** |
| Temporary AWS creds            | **STS**                 |
| Mobile app + AWS access        | **Cognito + STS**       |
| Cross‑account access           | **STS**                 |
| Multi‑account workforce access | **IAM Identity Center** |

***
