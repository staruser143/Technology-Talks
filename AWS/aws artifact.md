**Short answer:** ✅ **Yes, AWS Artifact is *in scope* for the SAP‑C02 exam — but only at a *theoretical / awareness* level.**  
❌ **It is *not* a deep‑dive, implementation-heavy topic.**

***

## How AWS Artifact appears in **SAP‑C02 (Solutions Architect – Professional)**

### ✅ What you **should know (exam‑relevant)**

You are expected to understand **what AWS Artifact is used for**, and **when to reference it in an architecture or compliance discussion**.

**Core exam-level understanding:**

*   AWS Artifact is:
    *   A **self‑service portal** for **compliance reports**
    *   Used to **download AWS audit artifacts**
    *   Used to **digitally sign compliance agreements** (e.g., BAA, NDA)
*   Typical documents available:
    *   SOC 1 / SOC 2 / SOC 3
    *   ISO 27001, 27017, 27018
    *   PCI DSS
    *   HIPAA BAA
*   It supports **enterprise compliance validation**, **audits**, and **regulatory assurance**

✅ This aligns with SAP‑C02’s **Security, Compliance & Governance** responsibilities.

***

### ❌ What will **NOT** be asked

You will **not** see questions on:

*   CLI or API usage of AWS Artifact
*   Automation workflows
*   IAM policy design for Artifact
*   EventBridge / CloudTrail integration
*   Operational monitoring

If a question goes that deep — **it’s a distractor path**.

***

## How AWS Artifact is tested (pattern-based)

### ✅ Typical SAP‑C02 exam phrasing

> “A company must provide proof that its cloud infrastructure meets compliance standards such as SOC 2 and ISO 27001 during a regulatory audit. Which AWS service should the solutions architect recommend?”

✅ **Correct answer:** AWS Artifact

***

### ❌ Trap variants (very common)

| Option            | Why it’s wrong                                                       |
| ----------------- | -------------------------------------------------------------------- |
| AWS Config        | Detects configuration drift, **does not provide audit reports**      |
| AWS CloudTrail    | Records API calls, **not compliance certifications**                 |
| AWS Security Hub  | Aggregates findings, **not auditor‑ready documents**                 |
| AWS Audit Manager | Helps prepare internal audits, **does not replace Artifact reports** |

***

## Exam positioning: **Correct vs Works vs Distractor**

| Category         | Role in exam                                 |
| ---------------- | -------------------------------------------- |
| ✅ **Correct**    | Compliance reports, audit evidence           |
| ⚠️ **Works**     | Audit Manager (internal evidence collection) |
| ❌ **Distractor** | GuardDuty, Inspector, Config, CloudTrail     |

***

## Blueprint mapping (important for SAP‑C02 candidates)

AWS Artifact maps to:

*   **Domain: Design for Security**
*   **Sub-area:**
    *   Compliance
    *   Governance
    *   Risk and regulatory requirements

It is often tested **indirectly**, not as the headline service.

***

## How deep should you study AWS Artifact?

**One-slide depth.**  
You should be able to answer:

*   What problem it solves
*   When to recommend it
*   Why other services are incorrect

That’s it.

***

## High‑yield memory hook (exam cheat)

> **“AWS Artifact = auditor asks → download report → show compliance.”**

