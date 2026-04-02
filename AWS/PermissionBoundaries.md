**Permission boundaries** in AWS are an **advanced IAM safety mechanism** used to **limit the maximum permissions** that an IAM **user or role** can ever have—*regardless* of what permissions are later granted to them.

Think of them as a **“guard rail” or “ceiling”** on permissions.

***

## Core Idea (1‑line definition)

> **A permission boundary defines the maximum permissions an IAM principal can exercise, even if other policies grant more.**

***

## How Permission Boundaries Work

AWS evaluates permissions using **multiple policy types together**.

For an IAM user or role:

    Effective permissions =
    (Identity-based policies)
    ∩ (Permissions boundary)

✅ **Allowed only if BOTH allow it**  
❌ **Denied if EITHER denies or does not allow it**

***

## What Permission Boundaries Are (and Are Not)

### ✅ They ARE

*   A **managed IAM policy** attached as a boundary
*   Enforced at **request evaluation time**
*   Used to **delegate IAM creation safely**
*   Excellent for **least privilege at scale**

### ❌ They are NOT

*   A replacement for IAM policies
*   A way to grant permissions
*   Applicable to resource-based policies (S3, KMS, etc.)
*   Automatically inherited (must be explicitly attached)

***

## Simple Example

### Permission Boundary Policy

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "*"
    }
  ]
}
```

### Identity Policy Attached to Role

```json
{
  "Effect": "Allow",
  "Action": "s3:DeleteObject",
  "Resource": "*"
}
```

### Result

❌ **DeleteObject is denied**

Why?

    DeleteObject ∉ Permission Boundary
    → Effective permission = DENY

***

## Typical Real‑World Use Cases

### 1. **Delegating IAM Role/User Creation**

Used when you want a team (e.g., DevOps) to:

*   Create roles
*   Attach policies  
    …but **not exceed approved permissions**

> Very common in large enterprises and Control Tower environments.

***

### 2. **Multi‑Team / Multi‑Account Governance**

Platform team:

*   Defines **permission boundaries**
    Application teams:
*   Manage policies **within those limits**

***

### 3. **Preventing Privilege Escalation**

Boundary can explicitly **exclude**:

*   `iam:*`
*   `organizations:*`
*   `sts:AssumeRole` to sensitive roles

Even if accidentally added later—boundary blocks it.

***

### 4. **AWS Control Tower**

Control Tower uses permission boundaries to:

*   Restrict what roles in managed accounts can ever do
*   Enforce consistent security posture

***

## Permission Boundaries vs SCPs vs IAM Policies

| Feature            | IAM Policy     | Permission Boundary | SCP                  |
| ------------------ | -------------- | ------------------- | -------------------- |
| Grants permissions | ✅              | ❌                   | ❌                    |
| Limits permissions | ❌              | ✅                   | ✅                    |
| Scope              | User / Role    | User / Role         | Account / OU         |
| Who evaluates      | IAM            | IAM                 | AWS Organizations    |
| Use case           | Access control | Delegation safety   | Org‑level governance |

📌 **Exam trap to remember**

*   *“Limit maximum permissions for a role”* → **Permission Boundary**
*   *“Prevent accounts from using a service”* → **SCP**

***

## Evaluation Order (Important for Exams)

For a request to succeed:

1.  ✅ SCP must allow (if applicable)
2.  ✅ Permission boundary must allow
3.  ✅ Identity-based policy must allow
4.  ✅ Resource-based policy must allow (if used)
5.  ❌ No explicit deny anywhere

***

## What You Cannot Do with Permission Boundaries

*   You **cannot attach** them to:
    *   Root user
    *   AWS service principals
*   You **cannot use them alone** (they don’t grant permissions)
*   You **cannot bypass** them with inline policies

***

## Key Exam Phrases to Watch For ✅

If you see wording like:

*   *“Delegate IAM role creation safely”*
*   *“Ensure roles don’t exceed approved permissions”*
*   *“Prevent future privilege escalation”*
*   *“Limit maximum allowed permissions”*

👉 **Permission Boundary is almost always the correct answer**

***

## One‑Sentence Exam Summary

> **Permission boundaries define the upper limit of permissions an IAM user or role can ever have, regardless of future policy changes.**

