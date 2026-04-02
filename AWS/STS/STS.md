**AWS Security Token Service (AWS STS)** is used to **issue temporary, short‑lived security credentials** (access key, secret key, and session token) that allow trusted users, applications, or services to **access AWS resources securely without long‑term credentials**.

***

## Core Purpose of AWS STS

👉 **Enable secure, temporary access to AWS resources**

Instead of distributing permanent IAM user credentials, STS lets you grant **time‑bound, limited‑scope access**.

***

## What Problems Does STS Solve?

| Problem                              | How STS Helps                           |
| ------------------------------------ | --------------------------------------- |
| Long‑term credentials are risky      | Temporary credentials auto‑expire       |
| Cross‑account access needed          | Assume roles across AWS accounts        |
| External users need AWS access       | Federated login (IdP, SSO, OAuth, SAML) |
| AWS services need scoped permissions | Service-to-service access using roles   |

***

## Key Capabilities of AWS STS

### 1. Assume IAM Roles (Most Common Use)

*   Users or services **assume a role**
*   STS returns **temporary credentials**
*   Role defines permissions via IAM policies

📌 Used heavily in:

*   Cross‑account access
*   EC2 / Lambda execution roles
*   EKS IRSA (IAM Roles for Service Accounts)

***

### 2. Federation (Identity Federation)

Allows users authenticated **outside AWS** to access AWS resources.

Supported sources:

*   Corporate Active Directory
*   SAML 2.0 providers
*   Web identity providers (Google, Facebook, OIDC)

Example:

    User → Corporate IdP → STS → Temporary AWS credentials

***

### 3. Cross‑Account Access

*   Account A trusts Account B
*   Account B assumes a role in Account A
*   No need to share IAM users or keys

✅ Clean, auditable, secure access model

***

### 4. Temporary and Scoped Credentials

*   Credentials exist for **minutes to hours**
*   Automatically expire
*   Can be restricted via:
    *   Role policies
    *   Session policies
    *   External ID (for third-party access)

***

## Common STS API Actions (High Level)

| API                         | Purpose                                    |
| --------------------------- | ------------------------------------------ |
| `AssumeRole`                | Assume a role in same or different account |
| `AssumeRoleWithSAML`        | Federation using SAML                      |
| `AssumeRoleWithWebIdentity` | Federation using OIDC (EKS, mobile apps)   |
| `GetSessionToken`           | Temporary creds for IAM users              |
| `GetFederationToken`        | Temporary creds for federated users        |

***

## Typical Architecture Examples

### ✅ EC2 / Lambda Access

*   Instance uses **IAM role**
*   Role backed by STS credentials
*   No hard‑coded secrets

***

### ✅ EKS (Kubernetes) IRSA

*   Pod assumes IAM role using OIDC
*   STS issues credentials
*   Fine‑grained access per pod

***

### ✅ Third‑Party Vendor Access

*   Vendor assumes role with External ID
*   Short‑lived credentials
*   Full audit trail in CloudTrail

***

## Exam‑Friendly One‑Line Definition ✅

> **AWS STS provides temporary, limited‑privilege credentials to securely access AWS resources without long‑term credentials.**

***

## Key Exam Clues → Think STS When You See

✅ “Temporary credentials”  
✅ “Cross‑account access”  
✅ “Federation / SAML / OIDC”  
✅ “No long‑term access keys”  
✅ “Secure access for workloads or external users”

***
