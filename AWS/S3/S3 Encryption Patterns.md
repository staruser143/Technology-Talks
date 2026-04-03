Below is a **clean, exam‑ready decision tree for S3 Encryption Patterns**, structured the way **AWS expects architects to reason**, especially for **SAP‑C02 / security / data‑protection scenarios**.

This is **not a feature list** — it’s a **decision flow**.

***

# 🌳 Decision Tree: Amazon S3 Encryption Patterns

***

## **START**

### ❓ Q1. Must data be encrypted at rest in S3?

✅ **Yes** → (Almost always Yes in exams)

➡️ Move to **Encryption Ownership Decision**

***

## 🔐 ENCRYPTION OWNERSHIP

### ❓ Q2. Who must control the encryption keys?

***

### ➤ AWS can manage keys fully

➡️ **SSE‑S3**

**Use when:**

*   Low‑risk data
*   No compliance requirement
*   Simplicity preferred

❌ No audit logs for key usage  
❌ Weak exam answer if compliance is mentioned

> **If compliance / audit mentioned → stop using SSE‑S3**

***

### ➤ Customer must control keys

➡️ Continue ↓

***

## 🗝️ KEY MANAGEMENT MODEL

### ❓ Q3. Should AWS store and manage the keys for you?

***

### ➤ Yes (centralized, auditable keys)

➡️ **SSE‑KMS (Customer‑Managed CMK)** ✅

**This is the DEFAULT ENTERPRISE PATH**

**Use when:**

*   HIPAA / PCI / SOX / regulated data
*   Need audit logs
*   Need key rotation
*   Need fine‑grained access control

✅ CloudTrail logs on key usage  
✅ IAM + KMS dual authorization

> **If the exam says “audit”, “compliance”, “regulatory” — this is almost always correct**

***

### ➤ No (AWS must not store keys)

➡️ Continue ↓

***

## 🔒 EXTREME KEY CONTROL

### ❓ Q4. Is the application responsible for encryption before upload?

***

### ➤ Yes

➡️ **Client‑Side Encryption** ✅

**Use when:**

*   Zero‑trust cloud model
*   AWS must never see plaintext
*   Data encrypted before leaving client

Tradeoffs:

*   App complexity
*   Key distribution handled by you
*   Limited S3‑side features (S3 Select, etc.)

> **Exam phrase:** “AWS must never see unencrypted data”

***

### ➤ No, but customer still provides key per request

➡️ **SSE‑C**

⚠️ Rare in real life  
⚠️ Operationally risky  
⚠️ Often an exam **distractor**

***

## 🚫 ENCRYPTION ENFORCEMENT (CRITICAL BRANCH)

> Encryption choice alone is **NOT enough**

### ❓ Q5. Must unencrypted uploads be blocked entirely?

***

### ➤ Yes

➡️ **Bucket Policy: Deny PUT without encryption headers** ✅

```text
IF request lacks x-amz-server-side-encryption
THEN deny PutObject
```

✅ True enforcement  
✅ Prevents developer mistakes  
✅ Strong exam answer

***

### ➤ No, encryption just needs to happen automatically

➡️ **Enable Default Bucket Encryption**

✅ Works  
❌ Does NOT block plaintext PUT requests

> **Classic exam trap:**  
> “Default encryption” ≠ “Enforcement”

***

## 🌐 ENCRYPTION IN TRANSIT (SIDE BRANCH – ALWAYS CHECK)

### ❓ Q6. Must data be protected in transit?

➡️ **Always YES**

✅ Enforce HTTPS‑only via bucket policy (`aws:SecureTransport = false` → Deny)

> Almost never the primary answer — but often required in combination

***

## 🧱 COMPLIANCE & IMMUTABILITY (ADVANCED BRANCH)

### ❓ Q7. Must data be immutable or protected from ransomware?

***

### ➤ Yes

➡️ **S3 Object Lock + Encryption** ✅

**Modes:**

*   Governance
*   Compliance (even root cannot delete)

✅ Regulatory archives  
✅ Financial/legal records

***

### ➤ No, rollback is sufficient

➡️ **Versioning + Encryption**

✅ Accidental delete recovery  
✅ Ransomware mitigation (soft)

***

## 🤝 CROSS‑ACCOUNT ACCESS (COMMON FAILURE POINT)

### ❓ Q8. Are encrypted objects accessed cross‑account?

➡️ **Two permissions required** ✅

1.  Bucket policy / IAM → `s3:GetObject`
2.  **KMS CMK policy → `kms:Decrypt`**

❌ Forgetting KMS policy = **AccessDenied**

> Extremely common SAP‑C02 trick

***

## 🧠 FINAL EXAM MENTAL MODEL

    S3 Encryption = 
      Algorithm
    + Key Ownership
    + Enforcement
    + Access Control (IAM + KMS)
    + Audit

***

## 🏁 ONE‑LOOK SUMMARY

| Requirement                    | Correct Pattern        |
| ------------------------------ | ---------------------- |
| Simple encryption              | SSE‑S3                 |
| Compliance / audit             | ✅ SSE‑KMS              |
| AWS must not see data          | Client‑side encryption |
| Enforce encryption             | Bucket policy deny PUT |
| HTTPS enforcement              | SecureTransport policy |
| Immutable records              | Object Lock + KMS      |
| Ransomware recovery            | Versioning + KMS       |
| Cross‑account encrypted access | IAM + KMS policy       |

***

