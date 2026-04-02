Here’s a **clean, exam‑oriented decision tree** for choosing between **Service Control Policies (SCPs)** and **Permission Boundaries**, optimized for **SA‑Pro / Control Tower** style questions.

***

# ✅ AWS Decision Tree: SCP vs Permission Boundary

    START
     |
     |-- Are you trying to control permissions at the ORGANIZATION / ACCOUNT level?
     |        |
     |        |-- YES →
     |        |      |
     |        |      |-- Do you want to restrict what services/actions
     |        |      |   an AWS ACCOUNT can ever use?
     |        |              |
     |        |              |-- YES → ✅ Use SCP
     |        |              |
     |        |              |-- NO →
     |        |                     |
     |        |                     |-- Are you trying to limit IAM roles/users inside the account?
     |        |                             |
     |        |                             |-- YES → Go to IAM‑level decision
     |
     |-- Are you delegating IAM ROLE or USER creation?
     |        |
     |        |-- YES →
     |        |      |
     |        |      |-- Do you want developers to create/attach policies
     |        |      |   BUT NEVER exceed approved permissions?
     |        |              |
     |        |              |-- YES → ✅ Use Permission Boundary
     |
     |-- Are you trying to prevent PRIVILEGE ESCALATION
     |   even if someone attaches Admin‑like policies?
     |        |
     |        |-- YES →
     |        |      |
     |        |      |-- Is the scope an IAM principal (role/user)?
     |        |              |
     |        |              |-- YES → ✅ Use Permission Boundary
     |        |              |
     |        |              |-- NO (entire account / OU) → ✅ Use SCP
     |
     |-- Are you using AWS Control Tower / multi‑account governance?
     |        |
     |        |-- YES →
     |        |      |
     |        |      |-- Restrict what accounts can do globally →
     |        |      |          ✅ SCP
     |        |      |
     |        |      |-- Restrict what roles inside accounts can ever do →
     |        |                 ✅ Permission Boundary
     |
     END

***

## 🧠 Mental Model (Exam Shortcut)

Think in **two axes**:

### Axis 1: Scope

| Scope                       | Choose                |
| --------------------------- | --------------------- |
| Entire account / OU         | ✅ SCP                 |
| Individual IAM role or user | ✅ Permission Boundary |

***

### Axis 2: Intent

| Intent                                            | Choose                |
| ------------------------------------------------- | --------------------- |
| “Never allow this service/action in this account” | ✅ SCP                 |
| “This role must never exceed X permissions”       | ✅ Permission Boundary |
| “Delegate IAM creation safely”                    | ✅ Permission Boundary |
| “Organization‑wide guardrails”                    | ✅ SCP                 |

***

## 🔥 High‑Confidence Exam Phrases Mapping

### SCP → Correct Answer When You See:

*   “Ensure **accounts** cannot use…”
*   “Organization‑wide restriction”
*   “Block regions/services across all accounts”
*   “Control Tower preventive guardrail”

### Permission Boundary → Correct Answer When You See:

*   “Delegate IAM role creation”
*   “Limit **maximum permissions**”
*   “Prevent privilege escalation”
*   “Developer can attach policies but not exceed…”

***

## 🚫 Common Exam Traps (Very Important)

| Trap Statement                                        | Why It’s Wrong                         |
| ----------------------------------------------------- | -------------------------------------- |
| “Use SCP to limit a role’s permissions”               | ❌ SCPs don’t apply at role granularity |
| “Use permission boundaries to restrict account usage” | ❌ Boundaries apply only to users/roles |
| “Permission boundary grants access”                   | ❌ Boundaries only *limit*, never grant |

***

## ✅ One‑Line Exam Rule (Memorize This)

> **If the question says ACCOUNT → SCP.  
> If it says ROLE/USER → Permission Boundary.**

***

## 🏗️ Real Enterprise Pattern (Control Tower)

    Organization
     ├─ SCP → what accounts can ever do
     │
     └─ Account
         ├─ Permission Boundary → max role permissions
         │
         └─ IAM Policy → actual granted permissions

***

