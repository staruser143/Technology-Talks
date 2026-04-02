Perfect — here’s the **exam‑optimized “Correct ✅ vs Works ⚠️ vs Distractor ❌” table** for **SSM vs SSH vs Bastion Host**, exactly in the style AWS exams use.

***

## ✅ AWS SSM vs SSH vs Bastion — Correct / Works / Distractor

### Scenario: *Administrators need to securely access EC2 instances*

| Option                                    | Verdict          | Why                                                                     |
| ----------------------------------------- | ---------------- | ----------------------------------------------------------------------- |
| **AWS Systems Manager (Session Manager)** | ✅ **CORRECT**    | No inbound ports, IAM‑based access, full audit logging, AWS‑recommended |
| Bastion Host                              | ⚠️ **WORKS**     | Central access point, but still requires SSH/RDP & patching             |
| Direct SSH/RDP                            | ❌ **DISTRACTOR** | Poor security, key sprawl, no centralized audit                         |

***

### Scenario: *Security team demands no inbound security‑group rules*

| Option                  | Verdict          | Why                                     |
| ----------------------- | ---------------- | --------------------------------------- |
| **SSM Session Manager** | ✅ **CORRECT**    | Outbound‑only traffic, no ports exposed |
| Bastion Host            | ❌ **DISTRACTOR** | Requires inbound SSH/RDP                |
| SSH from corporate IP   | ❌ **DISTRACTOR** | Still inbound access, weaker control    |

***

### Scenario: *Run commands on thousands of EC2 instances*

| Option               | Verdict          | Why                                    |
| -------------------- | ---------------- | -------------------------------------- |
| **SSM Run Command**  | ✅ **CORRECT**    | Fan‑out execution, tag‑based targeting |
| SSH with scripts     | ⚠️ **WORKS**     | Possible but not scalable              |
| Bastion host scripts | ❌ **DISTRACTOR** | Single choke point, failure‑prone      |

***

### Scenario: *Auditable access with IAM and CloudTrail*

| Option       | Verdict          | Why                                  |
| ------------ | ---------------- | ------------------------------------ |
| **SSM**      | ✅ **CORRECT**    | IAM policies + CloudWatch/S3 logging |
| Bastion Host | ⚠️ **WORKS**     | Logging possible but manual          |
| SSH          | ❌ **DISTRACTOR** | Limited audit capability             |

***

### Scenario: *Temporary dev instance needing quick access*

| Option       | Verdict          | Why                         |
| ------------ | ---------------- | --------------------------- |
| SSH          | ✅ **CORRECT**    | Fast, simple, minimal setup |
| SSM          | ⚠️ **WORKS**     | Requires agent + IAM role   |
| Bastion Host | ❌ **DISTRACTOR** | Overkill                    |

***

### Scenario: *Hybrid (AWS + on‑prem) server management*

| Option                     | Verdict          | Why                                    |
| -------------------------- | ---------------- | -------------------------------------- |
| **SSM Hybrid Activations** | ✅ **CORRECT**    | Manages on‑prem & cloud uniformly      |
| SSH                        | ⚠️ **WORKS**     | Manual, inconsistent                   |
| Bastion Host               | ❌ **DISTRACTOR** | Aws‑centric, limited hybrid capability |

***

### Scenario: *Central patching and maintenance windows*

| Option                | Verdict          | Why                   |
| --------------------- | ---------------- | --------------------- |
| **SSM Patch Manager** | ✅ **CORRECT**    | Baselines + schedules |
| Custom SSH scripts    | ⚠️ **WORKS**     | Operationally risky   |
| Bastion Host          | ❌ **DISTRACTOR** | Not a patching tool   |

***

## 🧠 Exam Elimination Rule (Memorize This)

> If the question mentions **security, IAM access, audit logs, no inbound ports, or scale → SSM is CORRECT**

***

## 🚫 Classic Exam Distractor Language

| Phrase in Option                | Why It’s Wrong                |
| ------------------------------- | ----------------------------- |
| “Allow SSH from trusted IPs”    | Still inbound access          |
| “Bastion host in public subnet” | Legacy, higher attack surface |
| “Use PEM keys for admins”       | No IAM centralization         |

***

## ✅ Final Exam Summary Table

| Requirement              | Correct Choice                |
| ------------------------ | ----------------------------- |
| Secure access (no ports) | **SSM**                       |
| Mass operations          | **SSM**                       |
| Compliance & audit       | **SSM**                       |
| Quick dev box            | SSH                           |
| Legacy access            | Bastion (only if unavoidable) |

***

