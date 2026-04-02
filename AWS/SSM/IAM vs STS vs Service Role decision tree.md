Below is a **clean, exam‑grade decision tree** to choose between **IAM (identity/permissions), STS (temporary credentials / role assumption), and Service Roles (AWS services acting on your behalf)**.

This is **high‑yield for SAP‑C02** and directly clears the “which one and why” confusion.

***

# 🌳 IAM vs STS vs Service Role — Decision Tree

    START
    │
    ├─▶ Who needs to perform the AWS action?
    │
    ├─▶ A HUMAN (admin, developer, operator)?
    │     │
    │     ├─▶ Is the access long‑term and identity‑based?
    │     │     │
    │     │     ├─▶ YES
    │     │     │     └─▶ Use IAM (User or Role)
    │     │     │           • Direct permissions
    │     │     │           • Managed / inline policies
    │     │     │
    │     │     └─▶ NO (temporary or cross‑account)
    │     │           └─▶ Use STS AssumeRole
    │     │                 • Time‑bound credentials
    │     │                 • Cross‑account / SSO / federation
    │     │
    │     └─▶ END
    │
    ├─▶ A COMPUTE RESOURCE (EC2, Lambda, ECS, on‑prem)?
    │     │
    │     ├─▶ Does it need to call AWS services?
    │     │     │
    │     │     ├─▶ YES
    │     │     │     └─▶ Attach an IAM ROLE to the resource
    │     │     │           • EC2 → Instance Role
    │     │     │           • Lambda → Execution Role
    │     │     │           • ECS/EKS → Task/Pod Role
    │     │     │
    │     │     └─▶ NO → No IAM role needed
    │     │
    │     └─▶ END
    │
    ├─▶ An AWS SERVICE (SSM, Lambda, EventBridge, CFN)?
    │     │
    │     ├─▶ Does the service need to call AWS APIs?
    │     │     │
    │     │     ├─▶ YES
    │     │     │     └─▶ Use a SERVICE ROLE
    │     │     │           • Trusted by service principal
    │     │     │           • Least‑privilege delegation
    │     │     │
    │     │     └─▶ NO → No service role required
    │     │
    │     └─▶ END
    │
    └─▶ END

***

# 🧠 Core Concept (Exam Gold)

> **IAM defines WHO you are and WHAT you can do.  
> STS provides HOW you temporarily become another role.  
> Service Roles define WHAT AWS services can do on your behalf.**

***

# 🔍 Clear Differentiation (No Ambiguity)

## ✅ IAM (Identity & Permissions)

**What it is**

*   Identity system
*   Permanent permissions model

**Used when**

*   You need to define *who* can do *what*

**Examples**

*   Admin can start SSM sessions
*   Developer can deploy stacks

✅ **Exam keywords**

> “permissions”, “access control”, “least privilege”

***

## ✅ STS (Temporary Credentials & Role Assumption)

**What it is**

*   Token service
*   Issues **short‑lived credentials**

**Used when**

*   Cross‑account access
*   SSO / federation
*   Just‑in‑time access

**Examples**

*   User in Account A manages resources in Account B
*   Identity Center → AssumeRole
*   CI/CD pipelines

✅ **Exam keywords**

> “temporary”, “cross‑account”, “assume role”

***

## ✅ Service Role (Delegation to AWS Services)

**What it is**

*   IAM role **assumed by an AWS service**
*   Enables AWS services to call AWS APIs

**Used when**

*   AWS service must act for you

**Examples**

*   SSM Automation rebooting EC2
*   Lambda writing CloudWatch Logs
*   CloudFormation creating resources
*   EventBridge starting Step Functions

✅ **Exam keywords**

> “on your behalf”, “AWS service needs permissions”

***

# 📊 One‑Look Comparison Table (High‑Yield)

| Dimension       | IAM                    | STS                | Service Role              |
| --------------- | ---------------------- | ------------------ | ------------------------- |
| Purpose         | Identity & permissions | Temporary access   | Service delegation        |
| Used by         | Humans / apps          | Humans & workloads | AWS services              |
| Credential type | Long‑lived             | Short‑lived        | Temporary (assumed)       |
| Cross‑account   | ❌                      | ✅                  | ✅                         |
| Typical SAP cue | “Permissions”          | “AssumeRole”       | “Service performs action” |

***

# 🔥 SAP‑C02 Trap Patterns (Very Important)

### ❌ Trap: “Give EC2 permissions so SSM Automation can stop instances”

✅ Correct: **Service Role**, not instance role

***

### ❌ Trap: “Use STS instead of IAM roles”

✅ Correct: STS **assumes** roles — it does NOT replace IAM

***

### ❌ Trap: “Give admin access to users so EventBridge can invoke Lambda”

✅ Correct: **EventBridge Service Role**

***

# 🧩 SSM Example (All Three Together)

| Step                          | Role Used               |
| ----------------------------- | ----------------------- |
| Admin starts automation       | IAM user / assumed role |
| SSM Automation calls EC2 APIs | **Service Role**        |
| Instance executes commands    | Instance Role           |

✅ This **three‑way role interaction** is classic SAP‑C02 material.

***

# ✅ Final Decision Rule (Memorize Verbatim)

> **Use IAM to define identity and permissions, STS to grant temporary or cross‑account access, and Service Roles when AWS services need to act on your behalf.**

***

