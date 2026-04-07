Below is an **exam‑grade, architecture‑friendly decision tree** to help you **choose between AWS Config, CloudTrail, and Security Hub**—and to avoid classic trap answers.

***

# ✅ AWS Config vs CloudTrail vs Security Hub

## Decision Tree (Architect + Exam Ready)

    START
    │
    ├─ Q1: What is the PRIMARY question you are trying to answer?
    │
    ├─► A) "What is the CURRENT or PAST CONFIGURATION state of resources?"
    │         │
    │         ├─ Do I need:
    │         │    - Resource configuration history?
    │         │    - Drift detection?
    │         │    - Compliance against rules over time?
    │         │
    │         ├─ YES ───► ✅ AWS CONFIG
    │         │
    │         └─ NO ──► (go back – wrong branch)
    │
    ├─► B) "WHO did WHAT action in AWS (API call)?"
    │         │
    │         ├─ Do I need:
    │         │    - API activity records?
    │         │    - IAM user / role attribution?
    │         │    - Forensics or investigation?
    │         │
    │         ├─ YES ───► ✅ AWS CLOUDTRAIL
    │         │
    │         └─ NO ──► (go back – wrong branch)
    │
    ├─► C) "Is my ACCOUNT SECURE and COMPLIANT overall?"
    │         │
    │         ├─ Do I need:
    │         │    - Single security dashboard?
    │         │    - Aggregated findings?
    │         │    - CIS / AWS Foundational best practices?
    │         │
    │         ├─ YES ───► ✅ AWS SECURITY HUB
    │         │
    │         └─ NO ──► (go back – wrong branch)
    │
    └─ END

***

## 🧠 Second‑Level Clarification (Exam Traps Exposed)

### If you answered **A – Configuration / Drift / Compliance**

✅ **AWS Config**

Use when the question mentions:

*   *configuration state*
*   *resource drift*
*   *compliance over time*
*   *history of settings*
*   *“was this bucket public yesterday?”*

❌ **NOT CloudTrail** (that shows the API call, not compliance)  
❌ **NOT Security Hub** (that consumes findings, not config history)

***

### If you answered **B – Who did what**

✅ **AWS CloudTrail**

Use when the question mentions:

*   *who modified / deleted / created*
*   *API call investigation*
*   *security incident forensics*
*   *IAM attribution*

❌ **NOT AWS Config** (Config doesn’t tell who made the change)  
❌ **NOT Security Hub** (doesn’t store raw API logs)

***

### If you answered **C – Overall Security Posture**

✅ **AWS Security Hub**

Use when the question mentions:

*   *central dashboard*
*   *security posture management*
*   *CIS / NIST / Foundational checks*
*   *aggregating GuardDuty, Macie, Config findings*

❌ **NOT GuardDuty alone** (threat detection ≠ governance)  
❌ **NOT Config alone** (no unified dashboard)

***

## ⚡ One‑Look Comparison (Mental Anchor)

| Question asked by scenario                   | Correct Service |
| -------------------------------------------- | --------------- |
| “What changed in this SG over time?”         | ✅ AWS Config    |
| “Who opened port 22 publicly?”               | ✅ CloudTrail    |
| “Are we compliant with CIS benchmarks?”      | ✅ Security Hub  |
| “Is encryption enforced on all EBS volumes?” | ✅ AWS Config    |
| “Show all security issues in one place”      | ✅ Security Hub  |

***

## 🔁 Real Enterprise Pattern (Exam Gold)

    CloudTrail  →  Who changed what?
    AWS Config  →  Is it compliant now (and before)?
    Security Hub → Aggregate + dashboard + audit evidence

Most **correct answers involve MORE THAN ONE service**, but the question asks for the **primary one**.

***

## 🪤 Classic Exam Trap Examples

### Trap 1

> “Track configuration changes for compliance reporting”

✅ AWS Config  
❌ CloudTrail (only API calls)  
❌ Security Hub (no config history)

***

### Trap 2

> “Investigate unauthorized deletion of an IAM role”

✅ CloudTrail  
❌ Config (won’t show actor)  
❌ Security Hub (too high‑level)

***

### Trap 3

> “Centralize findings from GuardDuty, Macie, and Config”

✅ Security Hub  
❌ Config  
❌ CloudTrail

***

## 🧩 Final Exam Mnemonics

*   **CloudTrail** 👉 *Who did what*
*   **AWS Config** 👉 *What is the state*
*   **Security Hub** 👉 *Are we secure overall*

***
