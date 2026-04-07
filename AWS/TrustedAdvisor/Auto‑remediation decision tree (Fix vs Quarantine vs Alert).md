Below is an **exam‑grade Auto‑Remediation Decision Tree** that helps you choose **Fix vs Quarantine vs Alert** — exactly how AWS expects you to reason in **SAP‑C02** and **enterprise security design**.

***

# Auto‑Remediation Decision Tree

## Fix vs Quarantine vs Alert

    DETECTION (Config / GuardDuty / Security Hub)
     │
     ├─► Is the issue LOW RISK, deterministic, and SAFE to auto-fix?
     │      (No data loss, no outage, reversible)
     │
     │        YES ─► AUTO‑FIX ✅
     │        NO
     │
     ├─► Is the issue HIGH RISK or ACTIVE THREAT,
     │     but fix is NOT deterministic?
     │
     │        YES ─► QUARANTINE ✅
     │        NO
     │
     ├─► Does the issue require human judgment,
     │     business context, or approval?
     │
     │        YES ─► ALERT / APPROVAL ✅
     │
     END

***

## 1️⃣ AUTO‑FIX (Remediate Immediately)

### Use when:

✅ Safe  
✅ Reversible  
✅ Deterministic  
✅ No business context required

### Typical services

*   **AWS Config + Lambda remediation**
*   **EventBridge → Lambda**
*   **Systems Manager Automation**

### Examples (EXAM FAVORITES)

| Scenario                                  | Action                       |
| ----------------------------------------- | ---------------------------- |
| S3 bucket made public                     | Remove public access         |
| Security Group has `0.0.0.0/0` on port 22 | Remove rule                  |
| IAM user without MFA                      | Enforce MFA                  |
| EBS volume unencrypted                    | Snapshot → encrypt → replace |

✅ **Best choice when the question says**:

> “Automatically remediate”

✅ **Unless explicitly warned** about business impact — auto‑fix is preferred in exams

***

## 2️⃣ QUARANTINE (Contain First, Fix Later)

### Use when:

⚠️ Active threat  
⚠️ High risk  
⚠️ Root cause unknown  
⚠️ Fix may destroy evidence

### Quarantine actions (NOT full fixes)

*   Move resource to **isolated security group**
*   **Detach from network**
*   Apply **restrictive IAM policy**
*   Isolate EC2 in a **forensic VPC**
*   Disable access **without deletion**

### Typical services

*   GuardDuty → EventBridge → Lambda
*   Security Group swap
*   SCP / IAM policy override

### Examples (EXAM FAVORITES)

| Scenario                   | Quarantine Action         |
| -------------------------- | ------------------------- |
| EC2 showing crypto mining  | Isolate instance          |
| IAM key compromised        | Disable key               |
| Unusual API calls detected | Restrict role permissions |
| Malware suspected          | Remove internet access    |

✅ **Exam hint**:  
If the words **“potential compromise”, “suspected breach”, “forensics”** appear → **QUARANTINE**

***

## 3️⃣ ALERT / APPROVAL (Humans Decide)

### Use when:

🧠 Business context needed  
🧠 Risk of downtime  
🧠 Destructive actions  
🧠 Compliance exceptions may exist

### Examples

| Scenario                      | Why Alert         |
| ----------------------------- | ----------------- |
| Deleting non‑compliant RDS    | Risk of data loss |
| Shutting down prod EC2        | Service outage    |
| Removing wide IAM permissions | App may break     |
| Regulatory exception required | Needs approval    |

### Typical Flow

    Detection
     → Security Hub
     → SNS / Slack / Ticket
     → Manual approval
     → Runbook / Automation

✅ **Exam trap clarity**

> “Notify the security team” or “require approval”  
> → **ALERT**, not Auto‑Fix

***

## Combined Mega Decision Tree (Reality + Exam)

    Violation Detected
     │
     ├─► Is it SAFE + REVERSIBLE?
     │        └─► YES → AUTO‑FIX
     │
     ├─► Is it an ACTIVE SECURITY THREAT?
     │        └─► YES → QUARANTINE
     │
     ├─► Does it risk OUTAGE / DATA LOSS?
     │        └─► YES → ALERT
     │
     └─► Default → ALERT

***

## Service Mapping Cheat Card (SAP‑C02 Gold)

| Service        | Role                  |
| -------------- | --------------------- |
| AWS Config     | Detect + evaluate     |
| EventBridge    | Route findings        |
| Lambda         | Fix / Quarantine      |
| Security Hub   | Central visibility    |
| SNS            | Alert humans          |
| SSM Automation | Safe auto-remediation |

***

## Common SAP‑C02 Traps

❌ Auto‑fix for **active intrusion**  
✅ Quarantine first

❌ Alert for **simple misconfigurations**  
✅ Auto‑fix

❌ Quarantine for **cost optimization**  
✅ Auto‑fix or recommendation

***

## One‑Line Exam Memory Rule

> **Fix** when safe, **Quarantine** when threatened, **Alert** when humans must decide.

***

## Ultra‑Fast Reflex Cards

    SAFE? → FIX
    THREAT? → QUARANTINE
    RISKY? → ALERT

***
