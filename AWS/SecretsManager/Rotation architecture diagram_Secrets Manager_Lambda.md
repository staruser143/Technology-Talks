Below is a **rotation architecture diagram** for **AWS Secrets Manager + Lambda** (works great for **RDS/Aurora** and also for **custom secrets** like API keys), plus a **step-by-step flow** and **key IAM/KMS guardrails**.

***

## 1) High-level architecture (who talks to whom)



**What this shows**

*   Apps **read secrets at runtime** from Secrets Manager.
*   Secrets Manager **encrypts/decrypts using KMS**.
*   A **rotation schedule** triggers a **rotation Lambda**.
*   Rotation Lambda updates the **database** (or external system), tests it, then **promotes** the new secret.

***

## 2) Rotation sequence diagram (the 4 rotation steps)

Secrets Manager rotation uses **four phases** (these map to Lambda invocation steps):



✅ **Key idea**: the secret is versioned with stages:

*   **AWSCURRENT** = active secret applications should use
*   **AWSPENDING** = newly generated candidate secret during rotation
*   **AWSPREVIOUS** = last secret (often retained for rollback)

***

## 3) Recommended “enterprise” deployment pattern

### A. App-side access pattern (best practice)

**Do not cache secrets forever**—cache with TTL and refresh gracefully.



**Why TTL caching matters**

*   Reduces API calls and throttling risk
*   Still picks up rotated secrets quickly

***

### B. Networking options for Rotation Lambda → DB



✅ For private environments:

*   Put rotation Lambda in the **same VPC** as DB
*   Use **VPC endpoints** (PrivateLink) so Lambda reaches Secrets Manager **without public internet**

***

## 4) IAM + KMS permissions (minimum you must think about)

### A. App IAM Role (read-only)

App role typically needs:

*   `secretsmanager:GetSecretValue`
*   `kms:Decrypt` (if using a customer-managed KMS key policy that requires it)

### B. Rotation Lambda IAM Role (rotation + DB updates)

Rotation Lambda typically needs:

*   Secrets Manager: `GetSecretValue`, `PutSecretValue`, `DescribeSecret`, `UpdateSecretVersionStage`
*   KMS: `Decrypt`, `Encrypt`, `GenerateDataKey`
*   CloudWatch Logs permissions
*   Network access to DB (security group rules)

> **Exam trap**: Many failures are not “Secrets Manager bugs”—they’re **KMS key policy** or **Lambda VPC networking** misconfigurations.

***

## 5) Two common variants you’ll see in real projects

### Variant 1 — RDS/Aurora “single user” rotation

*   Same DB user password is rotated
*   Simplest, but may cause brief auth failures if clients cache too long

### Variant 2 — “multi-user” / blue-green credential rotation

*   Keep a stable “owner/admin” and rotate a separate “app user”
*   Better separation of duties, safer operations

***

## 6) Operational checklist (what to monitor)

*   ✅ CloudWatch logs for rotation Lambda
*   ✅ Rotation failures alarms (via EventBridge → SNS)
*   ✅ DB auth failures spike (app metrics)
*   ✅ Ensure app handles **credential refresh** on auth failure

***

