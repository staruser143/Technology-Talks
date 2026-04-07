Perfect — here are **SAP‑C02–style multi‑answer *trick* questions** specifically designed to test **AWS Config mastery**, including **“TWO answers are correct”** traps and **“select ALL that apply”** scenarios.

I’ll include:

*   ✅ **Correct answers**
*   🟡 **Works but not best**
*   ❌ **Distractors**
*   🧠 **Exam‑reasoning callouts**

***

# 🧠 SAP‑C02 Multi‑Answer Trick Questions — **AWS Config**

***

## ✅ QUESTION 1 — *Select TWO answers*

A security auditor asks for proof that **all EC2 instances followed encryption and tagging standards over the last 12 months**, across **multiple AWS accounts**.  
The solution must require **minimal operational overhead**.

Which TWO should you use?

A. AWS CloudTrail  
B. AWS Config with managed rules  
C. AWS Config Aggregator with AWS Organizations  
D. AWS Security Hub  
E. AWS Trusted Advisor

***

### ✅ Correct Answers

✅ **B. AWS Config with managed rules**  
✅ **C. AWS Config Aggregator with AWS Organizations**

**Why**

*   Config provides **historical configuration + compliance**
*   Aggregators enable **multi‑account audit evidence**

***

### 🟡 Works (But Not Best)

🟡 **D. Security Hub** – Aggregates findings but relies on Config; not primary evidence source

***

### ❌ Distractors

❌ **A. CloudTrail** – API activity only, no compliance state  
❌ **E. Trusted Advisor** – Point‑in‑time recommendations

📌 **Exam trap**: “Auditor + historical proof” → **Config ALWAYS involved**

***

***

## ✅ QUESTION 2 — *Select ALL that apply*

You need to **automatically detect non‑compliant S3 buckets** and **trigger remediation**, while ensuring **human approval for sensitive actions**.

Which components should be included?

A. AWS Config rule  
B. EventBridge  
C. AWS Lambda  
D. Amazon GuardDuty  
E. AWS Step Functions

***

### ✅ Correct Answers

✅ **A. AWS Config rule**  
✅ **B. EventBridge**  
✅ **E. AWS Step Functions**

***

### 🟡 Works (Optional / Sometimes Included)

🟡 **C. Lambda** – Used inside Step Functions, but not mandatory by itself

***

### ❌ Distractors

❌ **D. GuardDuty** – Threat detection, not config compliance

📌 **Human approval keyword** → **Step Functions**, not pure Lambda

***

***

## ✅ QUESTION 3 — *Select TWO answers*

A team wants to know:

*   **Who changed a security group**
*   **Whether the change violated policy at the time**

Which TWO services should be combined?

A. AWS CloudTrail  
B. AWS Config  
C. Amazon Inspector  
D. AWS CloudWatch Logs  
E. AWS Shield

***

### ✅ Correct Answers

✅ **A. CloudTrail**  
✅ **B. AWS Config**

***

### 🧠 Reasoning

*   CloudTrail → **Who**
*   Config → **What changed & compliance**

📌 SAP‑C02 loves this **paired‑service pattern**

***

### ❌ Distractors

❌ Inspector – Vulnerabilities  
❌ CloudWatch – Metrics/logs  
❌ Shield – DDoS protection

***

***

## ✅ QUESTION 4 — *Select ALL that apply (Trick Question)*

Which of the following **are valid use cases for AWS Config**?

A. Detect configuration drift  
B. Identify anomalous API usage  
C. Provide evidence for compliance audits  
D. Continuously evaluate resource state  
E. Detect malware activity on EC2

***

### ✅ Correct Answers

✅ **A. Detect configuration drift**  
✅ **C. Provide audit evidence**  
✅ **D. Continuously evaluate resource state**

***

### ❌ Distractors

❌ **B. Anomalous API usage** → GuardDuty  
❌ **E. Malware detection** → Inspector / GuardDuty

📌 Keyword filter:

*   *Drift, rules, audit* → ✅ Config
*   *Threat, anomaly, malware* → ❌ Not Config

***

***

## ✅ QUESTION 5 — *Select TWO answers (Brutal)*

Your company enforces **encryption, tagging, and network controls** across **100+ AWS accounts**.  
You must **prevent non‑compliant resources from persisting** and allow **exception approval workflows**.

Which TWO are REQUIRED?

A. Service Control Policies (SCPs)  
B. AWS Config rules  
C. AWS IAM permission boundaries  
D. EventBridge‑based remediation  
E. AWS WAF

***

### ✅ Correct Answers

✅ **B. AWS Config rules**  
✅ **D. EventBridge‑based remediation**

***

### 🟡 Works (Complementary, Not Required)

🟡 **A. SCPs** – Prevent creation but don’t detect drift after creation

***

### ❌ Distractors

❌ IAM permission boundaries – Identity scope control  
❌ WAF – Web traffic filtering

📌 **Prevention ≠ Detection**  
SAP‑C02 expects you to **layer SCP + Config**, but question asks *required*.

***

***

## ✅ QUESTION 6 — *Select TWO answers*

An architect wants a **near‑real‑time alert** when **any resource becomes non‑compliant**, without polling.

Which TWO services enable this?

A. AWS Config  
B. Amazon SNS  
C. EventBridge  
D. AWS CloudTrail Insights  
E. Amazon Detective

***

### ✅ Correct Answers

✅ **A. AWS Config**  
✅ **C. EventBridge**

***

### 🟡 Works (Notification Only)

🟡 **B. SNS** – Used after EventBridge, not detection source

***

### ❌ Distractors

❌ CloudTrail Insights – API anomalies  
❌ Detective – Investigation, not alerts

***

***

## 🧠 SAP‑C02 Pattern Cheat Sheet

| Exam Keyword     | Think Immediately        |
| ---------------- | ------------------------ |
| Audit, history   | ✅ AWS Config             |
| Who did it       | ✅ CloudTrail             |
| Compliance drift | ✅ Config rules           |
| Auto‑remediation | ✅ Config → EventBridge   |
| Human approval   | ✅ Step Functions         |
| Threats          | ❌ GuardDuty (not Config) |

***

