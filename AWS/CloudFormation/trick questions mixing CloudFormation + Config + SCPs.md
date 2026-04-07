Perfect — this is **prime SAP‑C02 territory**.  
Below are **deliberately tricky exam‑style questions** that **mix CloudFormation + AWS Config + SCPs**, with **multi‑answer traps**, followed by **Correct vs Works vs Distractor explanations**.

✅ These are written exactly the way **SAP‑C02 tries to confuse senior architects**.

***

# 🔥 SAP‑C02 Trick Questions

## CloudFormation + AWS Config + SCPs

***

## 🧠 Question 1 — *“Why did CloudFormation fail?”*

### Scenario

Your organization uses **AWS Organizations** with **Service Control Policies (SCPs)**.  
A central platform team deploys infrastructure using **CloudFormation StackSets** across all accounts.

A new template update **fails** with an `AccessDenied` error during stack creation in multiple member accounts.

The goal is to **prevent non‑compliant resources**, but **CloudFormation should still deploy allowed resources successfully**.

### Which solution is BEST?

**A.** Add AWS Config rules to detect non‑compliant resources  
**B.** Modify SCPs to explicitly allow CloudFormation actions  
**C.** Use CloudFormation Drift Detection  
**D.** Replace SCPs with IAM policies

✅ **Correct Answers:** **A + B**

***

### ✅ Why A is correct

*   **SCPs are preventive & absolute**
*   AWS Config is **detective**, not blocking stack creation
*   You want **visibility + alerting**, not deployment failure

✅ **Why B is correct**

*   SCPs apply to **CloudFormation execution role**
*   Missing permissions will **block stack execution entirely**
*   SCPs must allow:
    *   `cloudformation:*`
    *   Underlying service APIs (EC2, IAM, etc.)

***

### ❌ Why others are traps

| Option | Why wrong                              |
| ------ | -------------------------------------- |
| C      | Drift detection works *after* creation |
| D      | SCPs cannot be replaced by IAM         |

📌 **Exam Insight**

> SCPs can break CloudFormation—even if IAM allows it.

***

***

## 🧠 Question 2 — *Config vs CloudFormation Responsibility*

### Scenario

A security team wants to ensure:

*   No **public S3 buckets**
*   No **unencrypted EBS volumes**

Resources are provisioned using **CloudFormation**.  
Existing teams often make **manual changes** in the console.

### What is the MOST appropriate solution?

**A.** Enforce all rules in CloudFormation templates  
**B.** Use AWS Config managed rules  
**C.** Use SCPs to deny non‑compliant resources  
**D.** Use Change Sets to prevent bad updates

✅ **Correct Answers:** **B + C**

***

### ✅ Why B is correct

*   AWS Config detects:
    *   Drift
    *   Manual changes
    *   Non‑compliant states

✅ **Why C is correct**

*   SCPs **prevent new violations**
*   Best used for:
    *   `s3:PutBucketPublicAccessBlock`
    *   `ec2:CreateVolume` without encryption

***

### ❌ Why others fail

| Option | Why wrong                                     |
| ------ | --------------------------------------------- |
| A      | Cannot stop console/manual changes            |
| D      | ChangeSets only affect CloudFormation updates |

📌 **Exam Rule**

> CloudFormation = desired state  
> Config = detection  
> SCPs = prevention

***

***

## 🧠 Question 3 — *“Compliance without Breaking Deployments”*

### Scenario

Your organization requires:

*   Continuous compliance monitoring
*   Automated remediation
*   **CloudFormation stacks must not fail**

### Which architecture BEST meets this?

**A.** SCPs only  
**B.** CloudFormation rules only  
**C.** AWS Config + EventBridge + Lambda  
**D.** Drift detection + Change Sets

✅ **Correct Answer:** **C**

***

### ✅ Why C is correct

This is the **canonical SAP‑C02 pattern**:

    CloudFormation → Creates resources
    AWS Config → Detects non‑compliance
    EventBridge → Triggers remediation
    Lambda / SSM → Fixes issue

✅ Non‑blocking  
✅ Continuous compliance  
✅ Works for manual changes

***

### ❌ Why others are traps

| Option | Why wrong                     |
| ------ | ----------------------------- |
| A      | SCPs block deployments        |
| B      | CF rules only validate inputs |
| D      | Drift detection is passive    |

***

***

## 🧠 Question 4 — *SCP vs Config Confusion Trap*

### Scenario

You must ensure:

*   Non‑approved regions cannot be used
*   Teams receive audit reports of violations

### Which combination is BEST?

**A.** AWS Config only  
**B.** SCP only  
**C.** SCP + CloudFormation  
**D.** SCP + AWS Config

✅ **Correct Answer:** **D**

***

### ✅ Why D is correct

*   SCPs → **Prevent usage** of regions
*   Config → **Audit + visibility**

📌 **Exam rule**

> Prevention + visibility → SCP + Config

***

### ❌ Why others fail

| Option | Why wrong               |
| ------ | ----------------------- |
| A      | Config does not prevent |
| B      | No reporting            |
| C      | No monitoring           |

***

***

## 🧠 Question 5 — *The Ultimate Trap Question*

### Scenario

A developer manually modifies a security group created by CloudFormation, violating compliance rules.

Security wants:

*   Detection
*   Automated remediation
*   Template integrity preserved

### What should you use?

**A.** CloudFormation Drift Detection  
**B.** AWS Config  
**C.** SCPs  
**D.** Change Sets

✅ **Correct Answers:** **A + B**

***

### ✅ Why A is correct

*   Detects drift from template

✅ **Why B is correct**

*   Detects compliance violation
*   Can auto‑remediate

***

### ❌ Why others fail

| Option | Why wrong                      |
| ------ | ------------------------------ |
| C      | Doesn’t fix existing resources |
| D      | Irrelevant to manual changes   |

📌 **Deep Exam Insight**

> Drift ≠ Compliance  
> You often need **both**

***

# 🧠 FINAL MEMORY MODEL (Write This on Exam Brain)

    CloudFormation → Build
    ChangeSets → Safe change
    StackSets → Scale
    SCPs → Prevent
    Config → Detect
    EventBridge + Lambda → Remediate
    Drift Detection → Template integrity

***


