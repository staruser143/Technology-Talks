Below is a **Pro‑level “Correct vs Works vs Distractor” exam table for AWS STS**, derived directly from the STS decision tree and aligned to **SAP‑C02 / SA‑Pro / Security‑Specialty** expectations.

This format reflects **real exam patterns** where **more than one option may technically work**, but only **one is architecturally correct**.

***

# ✅ AWS STS — Correct vs Works vs Distractor Exam Table

| Scenario                                                    | ✅ **Correct (Best Answer)**                   | ⚠️ **Works but Not Ideal**                         | ❌ **Distractor (Wrong)**            |
| ----------------------------------------------------------- | --------------------------------------------- | -------------------------------------------------- | ----------------------------------- |
| EC2 instance needs access to S3 without storing credentials | **IAM Role attached to EC2 (STS AssumeRole)** | IAM user credentials stored in AWS Secrets Manager | Hard‑coded access keys in user‑data |
| Lambda function accessing DynamoDB                          | **Lambda execution role (STS)**               | Lambda reads access keys from Parameter Store      | IAM user with access keys           |
| Cross‑account access between Account A and Account B        | **AssumeRole using STS (trust policy)**       | Resource‑based policy allowing root account        | Sharing IAM user credentials        |
| Employee signs in using corporate SSO (AD/Okta)             | **STS AssumeRoleWithSAML**                    | IAM user for each employee                         | Long‑lived access keys              |
| EKS pod needs access to DynamoDB                            | **IRSA (OIDC → AssumeRoleWithWebIdentity)**   | Node instance role                                 | Embedded credentials in container   |
| Third‑party vendor needs temporary access                   | **AssumeRole + External ID (STS)**            | IAM user rotated manually                          | Permanent access keys               |
| Mobile app needs access to S3                               | **STS AssumeRoleWithWebIdentity (OIDC)**      | Amazon Cognito User Pools only                     | Static credentials embedded in app  |
| Developer wants elevated access temporarily                 | **IAM role + STS AssumeRole**                 | Separate admin IAM user                            | Modify user policy permanently      |
| CI/CD pipeline deploys to another AWS account               | **STS AssumeRole (cross‑account role)**       | IAM user replicated across accounts                | Copying access keys                 |
| Temporary credentials for an IAM user                       | **STS GetSessionToken**                       | AssumeRole to self                                 | Long‑lived keys                     |
| Federated user session needing temporary AWS access         | **STS GetFederationToken**                    | Create IAM user on the fly                         | Root account credentials            |
| Kubernetes without IRSA needs AWS access                    | **Cluster‑level AssumeRole via STS**          | Node role shared by all pods                       | Hard‑coded secrets                  |

***

# ✅ How the Exam Thinks (Critical Insight)

### ✅ **Correct**

*   Uses **STS**
*   **No long‑term credentials**
*   **Least privilege**
*   **Auditable**
*   **Scales cleanly**

### ⚠️ **Works**

*   Technically functions
*   Fails on **security, scalability, or AWS best practice**
*   Often a **tempting distractor**

### ❌ **Distractor**

*   Violates AWS security model
*   Breaks exam keywords like:
    *   *temporary*
    *   *cross‑account*
    *   *federated*
    *   *no credential rotation*

***

# ✅ Ultra‑Fast Exam Elimination Rules

✅ If you see **temporary access** → **STS must appear**  
✅ If you see **cross‑account** → **AssumeRole**  
✅ If you see **SAML / OIDC** → **Federated STS API**  
✅ If you see **access keys stored anywhere** → **Eliminate**

***

# ✅ One‑Line Memory Hook

> **If IAM users are involved where roles would work → it’s almost always wrong in the exam.**

***

