Perfect — here are **SAP‑C02 / security‑grade multi‑answer trick questions** built **directly from the S3 Encryption decision tree**.

These are designed so that:

*   ✅ **More than one option “works”**
*   ✅ Only a **specific combination is fully correct**
*   ❌ Distractors look *technically valid but incomplete*

I’ll clearly mark **✅ Select ALL that apply**, then explain **why each option is correct / works / distractor**.

***

# 🔥 Brutal Multi‑Answer Trick Questions — S3 Encryption

***

## ❓ Question 1

**A healthcare application stores PHI in Amazon S3. The organization requires:**

*   Encryption at rest
*   Full auditability of key usage
*   Ability to restrict who can decrypt data

**Which options should be selected? (Select ALL that apply)**

A. Enable SSE‑S3  
B. Enable SSE‑KMS with a customer‑managed CMK  
C. Restrict `kms:Decrypt` permissions using IAM  
D. Upload objects over HTTPS only  
E. Enable default S3 bucket encryption

***

### ✅ Correct Answers

✅ **B, C, E**

### Explanation

*   **B (Correct)** – SSE‑KMS provides encryption at rest + CloudTrail audit of key usage
*   **C (Correct)** – KMS permissions control *who* can decrypt data (dual‑lock pattern)
*   **E (Correct)** – Prevents developers from accidentally uploading unencrypted objects

### Why others are wrong

*   **A (Works, but incomplete)** – Encrypts data but gives no key‑level audit or control
*   **D (Distractor)** – HTTPS protects *in transit*, not at rest

📌 **Exam insight:**  
If compliance + audit is mentioned, **SSE‑KMS must be present**.

***

## ❓ Question 2

**An organization wants to ensure that AWS never sees plaintext data at any point in the storage lifecycle.**

**Which approaches satisfy this requirement? (Select ALL that apply)**

A. Use client‑side encryption before uploading data  
B. Use SSE‑KMS with a customer‑managed key  
C. Use SSE‑S3  
D. Encrypt data locally using Envelope Encryption  
E. Enforce HTTPS using bucket policies

***

### ✅ Correct Answers

✅ **A, D**

### Explanation

*   **A (Correct)** – Data is encrypted *before* reaching AWS
*   **D (Correct)** – Envelope encryption performed on the client still prevents plaintext exposure

### Why others are wrong

*   **B (Works, but fails requirement)** – AWS decrypts data transiently inside infrastructure
*   **C (Distractor)** – Fully AWS‑managed encryption
*   **E (Distractor)** – Transit protection only

📌 **Exam insight:**  
“**AWS must never see plaintext**” → Client‑side encryption, always.

***

## ❓ Question 3

**A security team wants to prevent accidental uploads of plaintext objects to S3.**

**Which configurations help achieve this? (Select ALL that apply)**

A. Enable default bucket encryption  
B. Deny `PutObject` unless encryption headers are present  
C. Enable CloudTrail data events  
D. Use SSE‑KMS  
E. Use S3 Access Analyzer

***

### ✅ Correct Answers

✅ **A, B, D**

### Explanation

*   **A (Correct)** – Automatic encryption guardrail
*   **B (Correct)** – Explicit enforcement (hard stop)
*   **D (Correct)** – Ensures encryption uses controlled keys

### Why others are wrong

*   **C (Works, not preventive)** – Logs violations but doesn’t block them
*   **E (Distractor)** – Policy analysis only

📌 **Exam trap:**  
Monitoring ≠ enforcement.

***

## ❓ Question 4

**A company shares encrypted S3 objects with another AWS account. Access fails with `AccessDenied`, even though the bucket policy allows access.**

**Which actions resolve the issue? (Select ALL that apply)**

A. Add `kms:Decrypt` permission for the external account  
B. Enable pre‑signed URLs  
C. Update the KMS key policy to trust the external account  
D. Copy objects to an unencrypted bucket  
E. Use SSE‑S3

***

### ✅ Correct Answers

✅ **A, C**

### Explanation

*   **A (Correct)** – IAM permission to use the key is required
*   **C (Correct)** – KMS key policy must explicitly trust the external account

### Why others are wrong

*   **B (Distractor)** – Pre‑signed URLs do not bypass KMS
*   **D (Distractor)** – Weakens security; not a fix
*   **E (Works, but removes control)** – Avoids KMS entirely (bad architecture)

📌 **Classic exam failure case:**  
**S3 policy ✅ + KMS policy ❌ → AccessDenied**

***

## ❓ Question 5

**You must protect S3 data from ransomware and accidental deletion while maintaining encryption.**

**Which configurations should be selected? (Select ALL that apply)**

A. Enable versioning  
B. Enable SSE‑KMS  
C. Enable lifecycle rules for expiration  
D. Enable S3 Object Lock (Compliance mode)  
E. Enable replication to another region

***

### ✅ Correct Answers

✅ **A, B, D**

### Explanation

*   **A (Correct)** – Allows recovery from overwrite/deletion
*   **B (Correct)** – Protects data confidentiality
*   **D (Correct)** – Prevents deletion, even by root

### Why others are wrong

*   **C (Distractor)** – May delete data automatically
*   **E (Works, but incomplete)** – DR, not ransomware prevention

📌 **Exam clue:**  
Ransomware ≠ disaster recovery.

***

## ❓ Question 6 (Very Brutal)

**A regulated workload stores encrypted S3 data. Administrators should have access to the bucket but must not read the data by default.**

**Which mechanisms support this design? (Select ALL that apply)**

A. Restrict `kms:Decrypt` permissions  
B. Enable server access logging  
C. Use SSE‑KMS  
D. Use IAM policies only  
E. Grant read access via bucket policy

***

### ✅ Correct Answers

✅ **A, C**

### Explanation

*   **A (Correct)** – Encryption acts as a second authorization gate
*   **C (Correct)** – Required to decouple access from decryption

### Why others are wrong

*   **B (Distractor)** – Observability, not control
*   **D (Works, but weak)** – No encryption‑based control
*   **E (Distractor)** – Grants access but doesn’t control decryption

📌 **Architect insight:**  
Encryption is a **security control**, not just confidentiality.

***

## 🧠 Exam Rule of Thumb (Multi‑Answer)

> If **multiple answers encrypt data**,  
> ✅ Choose the set that **also enforces, audits, or restricts access**.

That’s why **SSE‑KMS + policy controls** dominate correct answers.

***

