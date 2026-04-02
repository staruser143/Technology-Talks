Perfect — this is a **classic AWS‑exam pattern**, and you’re right to ask for it this way.

Below is a **Correct vs Works vs Distractor** table specifically for **Cognito vs IAM Identity Center vs STS**, exactly in the **SAP‑C02 / Security / Pro mindset**.

***

# ✅ Cognito vs IAM Identity Center vs STS

## Correct vs Works vs Distractor (Exam‑Grade)

***

## ✅ Scenario 1

**A company is building a mobile application where customers sign up, sign in, and access protected REST APIs.**

| Option              | Classification   | Why                                                                |
| ------------------- | ---------------- | ------------------------------------------------------------------ |
| **Amazon Cognito**  | ✅ **CORRECT**    | Built for end‑user authentication, OAuth/OIDC, JWT tokens for APIs |
| IAM Identity Center | ❌ **DISTRACTOR** | Workforce SSO only; not for customer app login                     |
| AWS STS             | ❌ **DISTRACTOR** | Issues credentials only; no authentication or UI                   |

***

## ✅ Scenario 2

**Users of a mobile app must upload photos directly to an S3 bucket, without backend servers.**

| Option                                | Classification   | Why                                                                    |
| ------------------------------------- | ---------------- | ---------------------------------------------------------------------- |
| **Cognito User Pool + Identity Pool** | ✅ **CORRECT**    | User Pool authenticates; Identity Pool + STS grant temporary IAM creds |
| Cognito User Pool only                | ❌ **DISTRACTOR** | JWT tokens cannot access S3 directly                                   |
| AWS STS only                          | ❌ **DISTRACTOR** | No authentication; identity source missing                             |

📌 **Key exam insight**: **Direct AWS access = Identity Pool (IAM roles)**

***

## ✅ Scenario 3

**Employees should access multiple AWS accounts using corporate credentials via Azure AD.**

| Option                  | Classification   | Why                                                         |
| ----------------------- | ---------------- | ----------------------------------------------------------- |
| **IAM Identity Center** | ✅ **CORRECT**    | Workforce SSO, federates with Azure AD, manages role access |
| Amazon Cognito          | ❌ **DISTRACTOR** | Designed for customers, not employees                       |
| AWS STS alone           | ❌ **DISTRACTOR** | Credential vending only; no workforce identity management   |

***

## ✅ Scenario 4

**A Lambda function in one AWS account needs to access DynamoDB in another account securely.**

| Option                   | Classification   | Why                                                           |
| ------------------------ | ---------------- | ------------------------------------------------------------- |
| **AWS STS (AssumeRole)** | ✅ **CORRECT**    | Designed for cross‑account access using temporary credentials |
| IAM Identity Center      | ❌ **DISTRACTOR** | Human access only                                             |
| Amazon Cognito           | ❌ **DISTRACTOR** | No role for machine workloads                                 |

***

## ✅ Scenario 5

**An application already authenticates users using Google login and now needs AWS credentials dynamically.**

| Option                    | Classification   | Why                                            |
| ------------------------- | ---------------- | ---------------------------------------------- |
| **Cognito Identity Pool** | ✅ **CORRECT**    | Accepts external IdPs and maps to IAM roles    |
| Cognito User Pool         | ❌ **DISTRACTOR** | Authentication already handled externally      |
| IAM Identity Center       | ❌ **DISTRACTOR** | Does not integrate with app‑level social login |

***

## ✅ Scenario 6

**A serverless API must validate JWT access tokens before allowing users to invoke endpoints.**

| Option                       | Classification   | Why                                                   |
| ---------------------------- | ---------------- | ----------------------------------------------------- |
| **Amazon Cognito User Pool** | ✅ **CORRECT**    | Issues and validates JWT tokens for API Gateway / ALB |
| AWS STS                      | ❌ **DISTRACTOR** | Does not issue JWTs                                   |
| IAM Identity Center          | ❌ **DISTRACTOR** | No API token support                                  |

***

## ✅ Scenario 7

**A company wants temporary credentials for EC2 instances instead of long‑term access keys.**

| Option              | Classification   | Why                                            |
| ------------------- | ---------------- | ---------------------------------------------- |
| **AWS STS**         | ✅ **CORRECT**    | Provides short‑lived credentials via IAM roles |
| Amazon Cognito      | ❌ **DISTRACTOR** | No role in workload auth                       |
| IAM Identity Center | ❌ **DISTRACTOR** | Human identity system only                     |

***

## ✅ Scenario 8 (Multi‑Answer Trap)

**A mobile app requires user login AND direct access to S3 and DynamoDB.**

| Option                                | Classification            | Why                                    |
| ------------------------------------- | ------------------------- | -------------------------------------- |
| **Cognito User Pool + Identity Pool** | ✅ **CORRECT**             | Authentication + AWS authorization     |
| Cognito User Pool only                | ⚠️ **WORKS (Incomplete)** | Authentication works, AWS access fails |
| AWS STS only                          | ❌ **DISTRACTOR**          | No user authentication                 |

📌 **Exam hint**: When you see **login + AWS access**, the answer is usually **BOTH**.

***

## 🧠 Fast Elimination Rules (Use in Exam)

*   **JWT mentioned?** → Cognito User Pool
*   **Employees / SSO / AWS Console?** → IAM Identity Center
*   **Temporary credentials / AssumeRole?** → STS
*   **Mobile app + S3/DynamoDB?** → Cognito + STS

***

## ✅ Ultra‑Short Exam Mapping

| Phrase in question      | Auto‑select         |
| ----------------------- | ------------------- |
| “End users”             | Cognito             |
| “Workforce”             | IAM Identity Center |
| “Temporary credentials” | STS                 |
| “Cross‑account”         | STS                 |
| “Mobile / SPA”          | Cognito             |
| “AWS Console access”    | Identity Center     |

***

