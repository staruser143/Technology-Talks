Below is a **clear, exam‑grade decision tree** that tells you **WHEN to use Auto‑fix vs Quarantine vs Approval vs Runbook** in AWS Config–driven remediation architectures.

This is exactly how **SAP‑C02 / enterprise security design** questions expect you to reason.

***

## ✅ Remediation Decision Tree

**(Config → EventBridge → What next?)**

    START
    │
    ├─ Q1: Is the issue SECURITY‑CRITICAL and ACTIVELY RISKY?
    │      (public exposure, suspected compromise, data exfil risk)
    │
    │   ├─ YES
    │   │    │
    │   │    ├─ Q1a: Is immediate containment required to reduce blast radius?
    │   │    │
    │   │    ├─ YES ───► 🚨 QUARANTINE
    │   │    │             (contain first, fix later)
    │   │    │
    │   │    └─ NO ───► Continue to Q2
    │   │
    │   └─ NO ───► Continue to Q2
    │
    ├─ Q2: Is the remediation SAFE, REVERSIBLE, and DETERMINISTIC?
    │      (no downtime, no data loss, no business ambiguity)
    │
    │   ├─ YES
    │   │    │
    │   │    ├─ Q2a: Is this a SIMPLE, SINGLE‑STEP action?
    │   │    │
    │   │    ├─ YES ───► ⚡ AUTO‑FIX
    │   │    │             (direct Lambda remediation)
    │   │    │
    │   │    └─ NO ───► 📘 RUNBOOK
    │   │                  (SSM Automation / Step Functions)
    │   │
    │   └─ NO ───► Continue to Q3
    │
    ├─ Q3: Can remediation cause DOWNTIME, SERVICE IMPACT, or POLICY CHANGE?
    │
    │   ├─ YES ───► 🧑‍⚖️ APPROVAL
    │   │             (human‑in‑the‑loop gate)
    │   │
    │   └─ NO ───► 📘 RUNBOOK
    │                  (standardized multi‑step fix)
    │
    └─ END

***

## 🧠 How to Interpret Each Branch

### 🚨 **QUARANTINE**

**Use when:**

*   Threat is **real and active**
*   You must **reduce exposure immediately**

**Typical actions**

*   Move EC2 to quarantine security group
*   Revoke public SG rules temporarily
*   Disable IAM permissions via boundary or policy detach
*   Block S3 bucket access immediately

**Key principle**

> *Contain first. Investigate and remediate after.*

✅ Exam keywords  
`suspected compromise`, `limit blast radius`, `containment`, `zero trust response`

***

### ⚡ **AUTO‑FIX**

**Use when all are true:**

*   Safe
*   Reversible
*   No downtime
*   Single, obvious action

**Examples**

*   Block public S3 access
*   Remove `0.0.0.0/0` from SG port 22
*   Enforce required tags
*   Enable encryption-at-rest flag

**Architecture**

    Config → EventBridge → Lambda → API fix

✅ Exam keywords  
`auto-remediate`, `low-risk`, `enforce policy automatically`

❌ Trap: auto-fix when downtime is possible → wrong

***

### 🧑‍⚖️ **APPROVAL (Human‑in‑the‑Loop)**

**Use when remediation:**

*   Might break production
*   Changes access or security posture materially
*   Has business ambiguity

**Examples**

*   Rotating or disabling IAM credentials
*   Closing ports used by external partners
*   Deleting or replacing resources
*   Detaching IAM policies

**Architecture**

    Config → EventBridge → Step Functions / SSM Automation
                  ↓
              Approval Step
                  ↓
             Lambda / Runbook

✅ Exam keywords  
`requires approval`, `change management`, `manual review`, `production impact`

***

### 📘 **RUNBOOK (SSM / Step Functions)**

**Use when:**

*   Multi‑step remediation
*   Need audit trail
*   Need rollback / retries
*   Want standardized, reusable remediation

**Examples**

*   Snapshot → encrypt → replace EBS volume
*   Remove S3 public access → update policy → notify owner
*   Security group cleanup with validations
*   Tag fix + CMDB lookup

**Why enterprise‑preferred**

*   Auditable
*   Consistent
*   Safer than custom code sprawl

✅ Exam keywords  
`standardized remediation`, `SSM Automation`, `operational maturity`

***

## 🧩 Quick Mapping Table (Memory Anchor)

| Scenario                       | Correct Choice |
| ------------------------------ | -------------- |
| S3 bucket turned public        | ⚡ Auto‑fix     |
| EC2 suspected compromise       | 🚨 Quarantine  |
| IAM access key too old         | 🧑‍⚖️ Approval |
| Missing encryption on EBS      | 📘 Runbook     |
| SG allows SSH from internet    | ⚡ Auto‑fix     |
| Closing prod DB inbound access | 🧑‍⚖️ Approval |
| Multi‑step remediation needed  | 📘 Runbook     |

***

## 🪤 Exam Trap Patterns to Avoid

❌ Auto‑fix for credential revocation  
❌ Quarantine for simple misconfigurations  
❌ Approval for low‑risk enforcement  
❌ Lambda‑only remediation for complex workflows  
❌ Security Hub alone as “remediation”

***

## 🎯 One‑Line Exam Rule

> **If it’s risky → Quarantine.  
> If it’s safe → Auto‑fix.  
> If it’s complex → Runbook.  
> If it can break prod → Approval.**

***

