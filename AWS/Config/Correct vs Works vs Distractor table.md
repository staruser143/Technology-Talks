Here’s the **exam‑grade “Correct vs Works vs Distractor” table for AWS Config**, written the way SAP‑C02 / SAA / DOP questions are designed.

***

## ✅ AWS Config — Correct vs Works vs Distractor

### **Scenario Theme**

> You need to **track resource configuration changes over time** and **continuously evaluate compliance** against defined rules across AWS accounts.

***

### 🟢 **CORRECT (Best Answer – What AWS Config Is For)**

| Option         | Why this is CORRECT                                                                                                                                                                |
| -------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **AWS Config** | Records **resource configuration states**, keeps **configuration history**, and **evaluates compliance** using rules. Enables audits like *“Was this S3 bucket public last week?”* |

📌 Keywords examiners look for:

*   *Configuration history*
*   *Compliance rules*
*   *Drift detection*
*   *Audit / governance*

***

### 🟡 **WORKS (Technically Possible but NOT Ideal)**

| Option                                | Why it “works” but is NOT the best                                                                                        |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **AWS CloudTrail**                    | Can show *who changed a resource*, but **does not track state or compliance**. No “compliant vs non‑compliant” view.      |
| **Custom Lambda + CloudWatch Events** | Possible to build DIY compliance checks, but **reinventing AWS Config**. High maintenance, no native compliance timeline. |
| **Security Hub alone**                | Aggregates findings, but **does not independently track configuration history**. Depends on Config under the hood.        |

📌 These are **partial solutions**, often traps in exams.

***

### 🔴 **DISTRACTOR (Sounds Right but Completely Wrong)**

| Option                  | Why this is a Distractor                                                   |
| ----------------------- | -------------------------------------------------------------------------- |
| **Amazon GuardDuty**    | Detects **threats & anomalies**, NOT configuration compliance.             |
| **Amazon Inspector**    | Scans **vulnerabilities**, not resource configuration state.               |
| **CloudWatch Metrics**  | Monitors performance & health, **no config history**.                      |
| **AWS Trusted Advisor** | Provides **recommendations**, not continuous evaluation or audit trails.   |
| **IAM Access Analyzer** | Focused on **resource sharing & access**, not full configuration tracking. |

📌 These services answer **different security questions**.

***

## ⚠️ Classic Exam Trap Examples

**Trap Question**

> “A security team wants to know *who opened* an S3 bucket last Tuesday and *whether this violates company policy*.”

✅ **Correct**: AWS Config  
🟡 **Works**: CloudTrail (only tells who, not compliance)  
❌ **Distractor**: GuardDuty (no config tracking)

***

**Trap Question**

> “The auditor asks for proof that all EC2 instances were encrypted for the last 6 months.”

✅ **Correct**: AWS Config (historical compliance view)  
🟡 **Works**: Manual snapshots + scripts  
❌ **Distractor**: Inspector / CloudWatch

***

## 🧠 One‑Line Memory Hook

> **AWS Config = “What is my resource state, how did it change, and is it compliant?”**

***

