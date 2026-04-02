Perfect — here are **exam‑ready “Correct vs Works vs Distractor” tables** specifically for **SCP vs Permission Boundaries**, written in the exact style AWS **Solutions Architect Professional** questions use.

I’ll give you **4 high‑value tables** covering the most common (and tricky) patterns.

***

# ✅ Table 1: Delegating IAM Role Creation

### Scenario

*A platform team wants developers to create IAM roles and attach policies, but must ensure the roles never exceed approved permissions.*

| Option                                                      | Classification                | Why                                                                                |
| ----------------------------------------------------------- | ----------------------------- | ---------------------------------------------------------------------------------- |
| **Attach a permission boundary to all newly created roles** | ✅ **Correct**                 | Permission boundaries cap maximum privileges while allowing delegation             |
| Use SCP to restrict IAM actions                             | ⚠️ **Works, but Not Optimal** | SCPs apply broadly and are coarse; they don’t scale well for role‑level delegation |
| Attach inline deny policies to roles                        | ❌ **Distractor**              | Inline policies are error‑prone and can be removed by privileged users             |
| Use AWS Config rules to detect violations                   | ❌ **Distractor**              | Detection ≠ prevention                                                             |

📌 **Exam Keyword Trigger:** *“Delegate IAM safely”* → **Permission Boundary**

***

# ✅ Table 2: Prevent Privilege Escalation

### Scenario

*A security team wants to ensure no IAM role can ever gain admin privileges—even if AdminAccess is accidentally attached later.*

| Option                                                                  | Classification                 | Why                                                             |
| ----------------------------------------------------------------------- | ------------------------------ | --------------------------------------------------------------- |
| **Define a permission boundary excluding `iam:*` and `sts:AssumeRole`** | ✅ **Correct**                  | Boundaries enforce an upper privilege ceiling permanently       |
| Explicit deny in identity policies                                      | ⚠️ **Works, but Fragile**      | Denies can be removed or bypassed with new policies             |
| SCP denying `iam:*` at account level                                    | ⚠️ **Works, but Heavy‑handed** | Affects all principals including legitimate platform automation |
| Periodic IAM Access Analyzer scans                                      | ❌ **Distractor**               | Auditing does not prevent escalation                            |

📌 **Exam Trap:** *“Even if future policies are attached”* → **Permission Boundary**

***

# ✅ Table 3: Organization‑Wide Service Restrictions

### Scenario

*The company wants to ensure that **no account** in the organization can launch resources in non‑approved AWS regions.*

| Option                                                        | Classification               | Why                                                          |
| ------------------------------------------------------------- | ---------------------------- | ------------------------------------------------------------ |
| **Apply an SCP denying EC2 actions outside approved regions** | ✅ **Correct**                | SCPs enforce org‑wide guardrails across all accounts         |
| Use permission boundaries on all roles                        | ⚠️ **Works, but Unscalable** | Requires managing boundaries on every role in every account  |
| Add region‑based conditions to IAM policies                   | ❌ **Distractor**             | IAM policies don’t cover all principals and are easy to miss |
| Enforce via AWS Config remediation                            | ❌ **Distractor**             | Reactive, not preventive                                     |

📌 **Exam Keyword Trigger:** *“Across all accounts / organization‑wide”* → **SCP**

***

# ✅ Table 4: AWS Control Tower Environment (Very Common on Exam)

### Scenario

*An organization uses AWS Control Tower and wants strong governance without affecting workload agility.*

| Requirement                                         | Correct Choice            | Why                                                          |
| --------------------------------------------------- | ------------------------- | ------------------------------------------------------------ |
| Enforce global guardrails for services and regions  | ✅ **SCP**                 | Control Tower preventive controls are implemented using SCPs |
| Limit what roles inside member accounts can ever do | ✅ **Permission Boundary** | Fine‑grained IAM‑level limits                                |
| Grant application permissions                       | ❌ **Neither**             | IAM identity policies handle granting                        |
| Audit violations after deployment                   | ❌ **Neither**             | Use AWS Config or Security Hub                               |

📌 **Exam Insight:**  
Control Tower almost always implies **SCP + Permission Boundaries together**

***

# 🧠 Ultra‑Fast Elimination Logic (Exam Gold)

When stuck between answers, ask **only one question**:

> **Is the restriction about an ACCOUNT or a ROLE?**

| If the question mentions…   | Choose                |
| --------------------------- | --------------------- |
| Account / OU / Organization | ✅ SCP                 |
| Role / User / Delegation    | ✅ Permission Boundary |

***

# 🚫 High‑Frequency Distractors to Auto‑Eliminate

| Option                | Why It’s Almost Never Correct     |
| --------------------- | --------------------------------- |
| AWS Config            | Monitors, does not prevent        |
| IAM Access Analyzer   | Visibility only                   |
| Inline deny policy    | Not enforceable at scale          |
| Resource‑based policy | Does not cap identity permissions |

***

## ✅ One‑Sentence Exam Rule (Memorize This)

> **SCPs control what ACCOUNTS can ever do.  
> Permission boundaries control what ROLES can ever become.**

***
