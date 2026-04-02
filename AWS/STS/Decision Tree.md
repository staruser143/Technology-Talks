Here’s a **clear, exam‑ready AWS STS decision tree** you can **mentally replay in seconds** during the exam or design reviews.

I’ll start with the **core tree**, then add **exam call‑outs + common traps**.

***

# ✅ AWS STS – Decision Tree

    START
     |
     |-- Do you need TEMPORARY AWS CREDENTIALS?
     |        |
     |        |-- NO → Use IAM User / Access Keys (NOT STS)
     |        |
     |        |-- YES
     |              |
     |              |-- Is the identity INSIDE AWS?
     |              |        |
     |              |        |-- YES
     |              |        |      |
     |              |        |      |-- Is it an AWS service / workload (EC2, Lambda, ECS, EKS)?
     |              |        |      |        |
     |              |        |      |        |-- YES → Use IAM ROLE → STS AssumeRole
     |              |        |      |
     |              |        |      |-- Is it a user needing elevated or cross-account access?
     |              |        |               |
     |              |        |               |-- YES → STS AssumeRole
     |              |
     |              |-- Is the identity OUTSIDE AWS?
     |                       |
     |                       |-- Corporate / Enterprise IdP (SAML 2.0)?
     |                       |         |
     |                       |         |-- YES → AssumeRoleWithSAML
     |                       |
     |                       |-- Web / Mobile / Kubernetes (OIDC)?
     |                       |         |
     |                       |         |-- YES → AssumeRoleWithWebIdentity
     |                       |
     |                       |-- Third‑party vendor?
     |                                 |
     |                                 |-- Use AssumeRole + External ID
     |
     END

***

# ✅ Simplified Exam Recall Version (Ultra‑Fast)

    Temp creds needed?
    |
    |-- AWS workload → IAM Role → STS
    |
    |-- Cross-account → AssumeRole
    |
    |-- Corporate SSO → SAML → AssumeRoleWithSAML
    |
    |-- Web / EKS / Mobile → OIDC → AssumeRoleWithWebIdentity
    |
    |-- Vendor access → AssumeRole + External ID

***

# ✅ Decision Tree with **STS API Mapping**

    WHO needs access?
     |
     |-- EC2 / Lambda / ECS / EKS Pod
     |        → AssumeRole
     |
     |-- User switching roles (same/different account)
     |        → AssumeRole
     |
     |-- Enterprise SSO (AD / Okta / ADFS)
     |        → AssumeRoleWithSAML
     |
     |-- EKS IRSA / Mobile apps / Web identity
     |        → AssumeRoleWithWebIdentity
     |
     |-- IAM User wants short‑lived creds
     |        → GetSessionToken
     |
     |-- Temporary access for federated user
     |        → GetFederationToken

***

# ✅ Exam Clues → Which Branch Lights Up?

| Exam Phrase                   | Decision                  |
| ----------------------------- | ------------------------- |
| “Temporary credentials”       | STS                       |
| “No long‑term access keys”    | STS                       |
| “Cross‑account access”        | AssumeRole                |
| “Federated users / SAML”      | AssumeRoleWithSAML        |
| “OIDC / EKS / IRSA”           | AssumeRoleWithWebIdentity |
| “Third‑party access securely” | AssumeRole + External ID  |

***

# ❌ Common Exam Traps (Very Important)

### ❌ Trap 1: IAM User for EC2 Access

> **Wrong** – EC2 should always use **IAM Roles backed by STS**

***

### ❌ Trap 2: Sharing access keys across accounts

> **Wrong** – Use **cross‑account role + STS**

***

### ❌ Trap 3: SAML users need IAM users

> **Wrong** – Use **STS federation, no IAM users required**

***

### ❌ Trap 4: EKS pods using node role

> **Sub‑optimal** – Use **IRSA → STS + OIDC**

***

# ✅ One‑Line Exam Memory Hook

> **If access is temporary, cross‑account, federated, or service‑driven → STS is involved.**

***


Just tell me which one.
