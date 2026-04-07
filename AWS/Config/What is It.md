**AWS Config** is a **governance and compliance service** used to **continuously track, evaluate, and audit the configuration of AWS resources** in your account.

In short:  
👉 *AWS Config answers the question*: **“What changed, who changed it, and does it comply with my rules?”**

***

## What AWS Config Is Used For

### 1️⃣ **Resource Configuration Tracking (Change History)**

AWS Config records:

*   What resources exist (EC2, S3, IAM, VPC, RDS, etc.)
*   Their configuration details (security groups, encryption, tags, public access, etc.)
*   **Every change over time**

✅ You can see:

*   When a resource was created, modified, or deleted
*   The *exact before/after values*

***

### 2️⃣ **Compliance Monitoring (Rules)**

AWS Config allows you to define **rules** to check whether resources comply with policies.

Examples:

*   “S3 buckets must not be publicly accessible”
*   “EBS volumes must be encrypted”
*   “EC2 instances must have specific tags”
*   “IAM users must not have access keys older than 90 days”

Rules can be:

*   **Managed rules** (prebuilt by AWS)
*   **Custom rules** (Lambda-based logic)

📌 Each resource is marked as:

*   **Compliant**
*   **Non-compliant**

***

### 3️⃣ **Audit & Governance (Who Changed What)**

AWS Config is heavily used for:

*   **Security audits**
*   **Regulatory compliance** (HIPAA, PCI-DSS, SOC, ISO)
*   **Change management investigations**

Example questions it answers:

*   *Who opened this security group to the internet?*
*   *Was encryption disabled at any point last month?*
*   *Which resources were non-compliant on March 20th?*

***

### 4️⃣ **Continuous Evaluation (Near Real-Time)**

AWS Config:

*   Evaluates resources **continuously**, not once
*   Re-evaluates compliance whenever:
    *   A resource changes
    *   A rule changes

This makes it ideal for **continuous compliance**, not point-in-time checks.

***

### 5️⃣ **Multi‑Account & Multi‑Region Governance**

Using:

*   **AWS Organizations**
*   **Aggregator accounts**

You can:

*   Collect config data from all accounts
*   Enforce centralized compliance rules
*   View org-wide compliance dashboards

***

## What AWS Config Is *NOT*

This is important for exam & architecture clarity:

| Service        | What it Does                       |
| -------------- | ---------------------------------- |
| **AWS Config** | Configuration state & compliance   |
| CloudTrail     | API activity (who called what API) |
| CloudWatch     | Metrics, logs, alarms              |
| Security Hub   | Aggregated security findings       |
| GuardDuty      | Threat & anomaly detection         |

📌 **Config ≠ monitoring performance ≠ threat detection**

***

## Common Real‑World Use Cases

✅ Security posture management  
✅ Detect drift from approved architecture  
✅ Support audit evidence automatically  
✅ Enforce tagging and encryption standards  
✅ Troubleshoot “how did this become public?” incidents

***

## Simple Mental Model (Exam‑Friendly)

> **AWS Config = “AWS Timeline + Compliance Checker for resource configurations”**

***

## One‑Line Exam Answer

> **AWS Config is used to continuously record AWS resource configurations, detect configuration changes, and evaluate compliance against defined rules.**

