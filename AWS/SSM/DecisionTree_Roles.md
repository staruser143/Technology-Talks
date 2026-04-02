Below is a **clean, exam‑ready decision tree for IAM roles** focused on **AWS SSM**, but general enough to apply across **SAP‑C02 / SAA / real enterprise architecture**.

This directly answers *when you need a user role, instance role, service role, or STS* — and **why**.

***

# 🌳 IAM Roles Decision Tree (SSM‑Centric)

    START
    │
    ├─▶ Who is initiating the action?
    │
    ├─▶ A HUMAN (admin / operator / engineer)?
    │     │
    │     ├─▶ Is the human performing actions in AWS?
    │     │     │
    │     │     ├─▶ YES
    │     │     │     └─▶ Use an IAM User or Assumed Role (via IAM / SSO)
    │     │     │           │
    │     │     │           ├─ Controls API permissions (ssm:StartSession, SendCommand)
    │     │     │           ├─ Limits which instances can be managed
    │     │     │           └─ Enforces least privilege & audit
    │     │     │
    │     │     └─▶ NO (just accessing application)
    │     │
    │     └─▶ END
    │
    ├─▶ A COMPUTE RESOURCE (EC2, Lambda, ECS, On‑prem server)?
    │     │
    │     ├─▶ Does it need to call AWS services?
    │     │     │
    │     │     ├─▶ YES
    │     │     │     └─▶ Attach an IAM ROLE TO THE RESOURCE
    │     │     │           │
    │     │     │           ├─ EC2 / On‑prem → Instance Role
    │     │     │           │     └─ (Required for SSM agent)
    │     │     │           ├─ Lambda → Execution Role
    │     │     │           └─ ECS / EKS → Task / Pod Role
    │     │     │
    │     │     └─▶ NO → No role required
    │     │
    │     └─▶ END
    │
    ├─▶ Is a SERVICE acting on your behalf?
    │     │
    │     ├─▶ YES
    │     │     └─▶ Use a SERVICE ROLE
    │     │           │
    │     │           ├─ SSM Automation
    │     │           ├─ EventBridge
    │     │           ├─ Backup
    │     │           └─ CloudFormation
    │     │
    │     └─▶ NO
    │
    ├─▶ Is this CROSS‑ACCOUNT or TEMPORARY access?
    │     │
    │     ├─▶ YES
    │     │     └─▶ Use STS AssumeRole
    │     │           │
    │     │           ├─ Cross‑account SSM operations
    │     │           ├─ Time‑bound access
    │     │           └─ Just‑in‑time permissions
    │     │
    │     └─▶ NO
    │
    └─▶ END

***

# 🔍 Now Map This Directly to **SSM**

## ✅ Why SSM Needs **Two Roles**

### 1️⃣ **User (Caller) Role**

**Answers:** *“Is the human allowed to do this?”*

| Controls      | Example                           |
| ------------- | --------------------------------- |
| Start session | `ssm:StartSession`                |
| Run commands  | `ssm:SendCommand`                 |
| Automation    | `ssm:StartAutomationExecution`    |
| Scope         | Which instances (via tags / ARNs) |

✅ Used by:

*   IAM users
*   IAM roles assumed via SSO / STS

***

### 2️⃣ **Instance Role**

**Answers:** *“Is the instance allowed to receive this?”*

| Purpose           | Why               |
| ----------------- | ----------------- |
| SSM Agent auth    | Instance identity |
| Command execution | Run Command       |
| Session channels  | Session Manager   |
| Patch reporting   | Patch Manager     |

✅ Required policy:

    AmazonSSMManagedInstanceCore

🚫 No instance role → **SSM does NOT work**, even if user is admin.

***

## 🧠 Exam‑Critical Rule (Memorize)

> **SSM enforces dual trust**  
> User must be authorized **AND** instance must trust SSM.

***

# 🧪 Quick Role Selection Table (High Yield)

| Scenario                      | Role Needed            |
| ----------------------------- | ---------------------- |
| Admin starts SSM session      | User role              |
| EC2 receives SSM command      | Instance role          |
| SSM Automation runs           | Service role           |
| Cross‑account SSM             | STS AssumeRole         |
| On‑prem server managed by SSM | Hybrid activation role |

***

# ⚠️ Common SAP‑C02 Traps

### ❌ “User has AdministratorAccess, but SSM doesn’t work”

✅ Root cause: **Missing instance role**

***

### ❌ “SSM permissions added to EC2”

✅ EC2 needs **instance role**, not user policy

***

### ❌ “Use STS instead of instance role”

✅ STS is for **assuming roles**, not instance identity

***

# ✅ Final Exam Summary (One‑Liner)

> **Use a user role to authorize SSM actions, an instance role to allow execution, a service role for automation, and STS for cross‑account or temporary access.**

***

