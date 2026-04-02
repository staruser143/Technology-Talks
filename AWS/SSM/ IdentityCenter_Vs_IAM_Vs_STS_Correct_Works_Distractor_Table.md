Absolutely — here’s a **SAP‑C02 style “Correct ✅ / Works ⚠️ / Distractor ❌” table** for **AWS IAM Identity Center (SSO) vs IAM vs STS**, written the way exam questions are designed (where multiple answers *can* work, but only one is best).

***

# ✅ Identity Center vs IAM vs STS — Correct / Works / Distractor Table

## Legend

*   ✅ **Correct** = Best answer (AWS‑recommended / most scalable / most secure / least ops)
*   ⚠️ **Works** = Technically valid, but not best (more ops, weaker governance, not aligned to requirement keywords)
*   ❌ **Distractor** = Doesn’t meet key requirements or misuses the service

***

## 1) Workforce SSO to AWS accounts (central control)

**Scenario:** A company wants employees to sign in **using corporate identity (Entra ID/AD/Okta)** and access **multiple AWS accounts** with **centralized permissions** and **MFA**.

| Option                               | Verdict      | Why                                                                                   |
| ------------------------------------ | ------------ | ------------------------------------------------------------------------------------- |
| **IAM Identity Center**              | ✅ Correct    | Built for **workforce SSO**, centralized permission sets, multi‑account access        |
| IAM Users in each account            | ⚠️ Works     | Works but **high ops**, credential sprawl, hard governance                            |
| STS AssumeRole only                  | ⚠️ Works     | Still needs an identity front‑door (SSO/IdP) + role assumption; alone it’s incomplete |
| Long‑term access keys + IAM policies | ❌ Distractor | Violates best practice for workforce access                                           |

**Exam keyword triggers:** “employees”, “SSO”, “centralized access”, “MFA”, “multiple accounts”

***

## 2) Temporary cross‑account admin access (break‑glass)

**Scenario:** A security admin in Account A needs **time‑bound** access to remediate an incident in Account B, with **least privilege** and **no permanent credentials**.

| Option                                  | Verdict      | Why                                                                                                   |
| --------------------------------------- | ------------ | ----------------------------------------------------------------------------------------------------- |
| **STS AssumeRole (cross‑account role)** | ✅ Correct    | Purpose‑built: **temporary credentials**, cross‑account, controllable session duration                |
| IAM Identity Center                     | ⚠️ Works     | Can provide access, but for incident “temporary admin” the core primitive is still **STS AssumeRole** |
| IAM user created in Account B           | ⚠️ Works     | Possible but violates “no permanent creds”, high audit overhead                                       |
| Sharing root credentials                | ❌ Distractor | Explicitly wrong and unsafe                                                                           |

**Exam keyword triggers:** “temporary”, “cross‑account”, “break‑glass”, “no long-term credentials”

***

## 3) Application running on EC2 needs AWS API access (no stored keys)

**Scenario:** An app on EC2 must read from S3 and write to DynamoDB without storing access keys.

| Option                                          | Verdict      | Why                                                                        |
| ----------------------------------------------- | ------------ | -------------------------------------------------------------------------- |
| **IAM Role attached to EC2 (Instance Profile)** | ✅ Correct    | Standard pattern: short‑lived creds via instance metadata, least privilege |
| STS AssumeRole from the app                     | ⚠️ Works     | Can be done but unnecessary if instance role suffices; adds complexity     |
| IAM Identity Center                             | ❌ Distractor | For workforce identities, not workload auth                                |
| Hardcoded IAM access keys                       | ❌ Distractor | Violates security best practices                                           |

**Exam keyword triggers:** “EC2 app”, “no stored keys”, “workload access”

***

## 4) External partner needs access to your AWS resources

**Scenario:** A partner company needs access to upload files to your S3 bucket, but you must avoid creating IAM users for them.

| Option                                              | Verdict      | Why                                                                                           |
| --------------------------------------------------- | ------------ | --------------------------------------------------------------------------------------------- |
| **STS AssumeRole with external ID (cross‑account)** | ✅ Correct    | Best practice for third‑party access; avoids IAM users                                        |
| IAM Identity Center                                 | ⚠️ Works     | Possible if you onboard them into your SSO model, but often not desired for external partners |
| Create IAM user for partner                         | ⚠️ Works     | Works but poor governance, long‑term creds                                                    |
| Share your access keys                              | ❌ Distractor | Unsafe                                                                                        |

**Exam keyword triggers:** “third party”, “partner”, “avoid IAM users”, “temporary”

***

## 5) Centralized access governance for 50 AWS accounts (Organizations)

**Scenario:** A company has 50 accounts in AWS Organizations and wants **central onboarding/offboarding**, **permission sets**, **auditability**, and **consistent access**.

| Option                        | Verdict      | Why                                                                                              |
| ----------------------------- | ------------ | ------------------------------------------------------------------------------------------------ |
| **IAM Identity Center**       | ✅ Correct    | Centralized SSO + permission sets across org accounts                                            |
| STS AssumeRole only           | ⚠️ Works     | Cross-account roles work but become a role sprawl management problem without a central SSO layer |
| IAM users per account         | ❌ Distractor | Massive operational burden, inconsistent governance                                              |
| Access keys in password vault | ❌ Distractor | Still long-lived keys and messy lifecycle                                                        |

**Exam keyword triggers:** “Organizations”, “many accounts”, “onboarding/offboarding”, “central governance”

***

## 6) CI/CD pipeline needs temporary credentials to deploy into AWS

**Scenario:** GitHub Actions/Jenkins needs to deploy to AWS with no static long‑term keys.

| Option                                         | Verdict      | Why                                                                                         |
| ---------------------------------------------- | ------------ | ------------------------------------------------------------------------------------------- |
| **STS AssumeRole (often via OIDC federation)** | ✅ Correct    | CI/CD = temporary credentials; STS is the credential vending primitive                      |
| IAM user access keys (rotated)                 | ⚠️ Works     | Works but still long‑lived credentials and rotation burden                                  |
| IAM Identity Center                            | ⚠️ Works     | It’s workforce SSO; sometimes used indirectly, but CI/CD best practice is federation to STS |
| Hardcode keys in pipeline                      | ❌ Distractor | Security anti‑pattern                                                                       |

**Exam keyword triggers:** “CI/CD”, “no long term keys”, “temporary credentials”, “federation/OIDC”

***

# 🧠 Super‑High‑Yield “Pick the Service” Mini Rules (3‑second recall)

## ✅ Identity Center (Workforce SSO)

Choose when you see:

*   “employees”
*   “SSO”
*   “central onboarding/offboarding”
*   “permission sets”
*   “multiple AWS accounts”

## ✅ IAM (Permissions & Roles)

Choose when you see:

*   “define permissions”
*   “role attached to EC2/Lambda”
*   “least privilege policies”
*   “resource access controls”

## ✅ STS (Temporary Credentials / AssumeRole)

Choose when you see:

*   “temporary”
*   “cross-account”
*   “federation”
*   “partners / third-party”
*   “CI/CD”

***

# ⭐ Final Exam Summary (Memorize Verbatim)

> **Identity Center** is for workforce SSO and centralized access management.  
> **IAM** is the core permission system (users/roles/policies).  
> **STS** issues temporary credentials and enables AssumeRole (cross‑account, federation, CI/CD).

***

If you want, I can convert this into **“Correct vs Works vs Distractor”** for *a single brutal scenario* where **two answers work** (classic SAP‑C02 ambiguity), and show the elimination logic step-by-step.
