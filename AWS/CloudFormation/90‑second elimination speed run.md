Perfect — here’s your **SAP‑C02 90‑SECOND ELIMINATION SPEED RUN**  
Designed to be **read once, memorized, and executed under exam pressure**.

***

# ⚡ SAP‑C02 — 90‑Second Elimination Speed Run

**CloudFormation + AWS Config + SCPs**

***

## 🧠 FIRST 10 SECONDS — Identify the DOMAIN

Ask yourself **one question**:

> Is the problem about **BUILDING**, **DETECTING**, or **PREVENTING**?

| If the question mentions…       | Lock onto          |
| ------------------------------- | ------------------ |
| Provisioning, templates, stacks | **CloudFormation** |
| Compliance, audit, drift        | **AWS Config**     |
| Blocking actions, guardrails    | **SCPs**           |

***

## ⛔ FAST ELIMINATION RULES (Auto‑Discard)

❌ If answer says **“manual review”** → **ELIMINATE**  
❌ If answer uses **IAM instead of SCPs** → **ELIMINATE**  
❌ If answer blocks prod deployments unnecessarily → **ELIMINATE**  
❌ If answer detects but doesn’t prevent when prevention is required → **ELIMINATE**

***

## 🔁 ROLE CLARITY (15‑Second Brain Lock)

Memorize this **exact mapping**:

    CloudFormation → create & update
    ChangeSets → preview changes
    StackSets → multi-account deploy
    AWS Config → detect violations
    SCPs → block violations
    EventBridge + Lambda → auto-remediate
    Drift Detection → template mismatch

If an option crosses roles → **WRONG**

***

## 🔥 WHEN SCPs ARE WRONG (Very Testable)

SCPs are **NOT correct** if:

*   CloudFormation stacks must **continue deploying**
*   You need **visibility/reporting**
*   You need **remediation**, not rejection

📌 If stacks are failing → SCPs are probably the villain

***

## ✅ WHEN SCPs ARE MANDATORY

Choose SCPs instantly if you see:

*   “Prevent usage of regions”
*   “Ensure no public S3 buckets **can ever be created**”
*   “Block root user access”
*   “Stop non-encrypted resources from being created”

➡️ **SCPs = hard wall**

***

## 🕵️ AWS Config INSTANT PICK CONDITIONS

Choose **Config** if question mentions:

*   Audit
*   Compliance reports
*   Manual changes
*   Historical view
*   Continuous monitoring

⚠️ If Config is alone and **prevention is required** → incomplete

***

## 🧱 CloudFormation PATTERN FILTER

| Requirement       | Must include                  |
| ----------------- | ----------------------------- |
| Production update | **Change Sets**               |
| Multi-account     | **StackSets**                 |
| Future accounts   | **Service‑managed StackSets** |
| Manual drift      | **Drift Detection**           |

Missing any → **ELIMINATE**

***

## 🚀 CLASSIC SPEED‑RUN QUESTIONS

### Q: *“Stacks fail due to AccessDenied”*

✅ Fix SCPs  
❌ Don’t blame IAM

***

### Q: *“Ensure compliance without blocking deployments”*

✅ CloudFormation + Config + remediation  
❌ SCPs alone

***

### Q: *“Detect AND fix manual changes”*

✅ Drift Detection + Config  
❌ Change Sets

***

### Q: *“Apply standards to all accounts automatically”*

✅ StackSets + Organizations  
❌ Individual stacks

***

## 🧠 FINAL 15‑SECOND MASTER RULE

> **Prevention + Detection + Scale = SCPs + Config + CloudFormation**

If an answer **only does one**, and the question asks for **enterprise governance**, it’s **wrong**.

***

## ✅ LAST‑SECOND ELIMINATION CHECK

Pick the option that:
✔ Uses **native AWS services**  
✔ Scales across accounts  
✔ Separates build / detect / prevent  
✔ Requires **least operational effort**

***

