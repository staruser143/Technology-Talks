Perfect — this is **exactly how SAP‑C02 / security questions are designed**.

Below is a **Correct vs Works vs Distractor** table for **Amazon S3 Encryption**, focusing on the **most commonly confused answer options** in exams and real architecture reviews.

***

# ✅ Amazon S3 Encryption

## Correct vs Works vs Distractor Table (Exam‑Grade)

### Scenario 1

**“You must encrypt data at rest in S3 and meet compliance, audit, and key‑control requirements.”**

| Option                                         | Classification   | Why                                                                                                                                                             |
| ---------------------------------------------- | ---------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Enable SSE‑KMS with a customer‑managed CMK** | ✅ **Correct**    | Provides encryption at rest, CloudTrail audit logs for key usage, fine‑grained access control, and key rotation. This is the enterprise and compliance default. |
| Enable SSE‑S3                                  | ⚠️ **Works**     | Encrypts at rest but gives **no control or audit visibility over keys**. No IAM‑level control on key usage.                                                     |
| Encrypt files using HTTPS                      | ❌ **Distractor** | HTTPS protects **in transit**, not at rest. Does not meet storage encryption requirements.                                                                      |
| Use EBS encryption                             | ❌ **Distractor** | Irrelevant — S3 does not use EBS.                                                                                                                               |

***

### Scenario 2

**“Prevent developers from accidentally uploading unencrypted objects to S3.”**

| Option                                                   | Classification   | Why                                                                                                                      |
| -------------------------------------------------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------ |
| **Enable default bucket encryption (SSE‑KMS)**           | ✅ **Correct**    | Guardrail pattern — all objects are encrypted automatically, even if devs forget headers.                                |
| Add bucket policy to deny PUT without encryption headers | ⚠️ **Works**     | Strong enforcement, but operationally fragile if tools don’t supply headers. Often layered on top of default encryption. |
| Enable CloudTrail                                        | ❌ **Distractor** | Audits access; does **not prevent** unencrypted uploads.                                                                 |
| Use S3 Access Analyzer                                   | ❌ **Distractor** | Helps analyze access policies — unrelated to encryption enforcement.                                                     |

***

### Scenario 3

**“Security team must control who can read S3 objects even if they have S3 permissions.”**

| Option                                                 | Classification   | Why                                                                                       |
| ------------------------------------------------------ | ---------------- | ----------------------------------------------------------------------------------------- |
| **Use SSE‑KMS and restrict `kms:Decrypt` permissions** | ✅ **Correct**    | Access now requires both S3 permission *and* KMS permission — dual‑authorization pattern. |
| Restrict IAM `s3:GetObject`                            | ⚠️ **Works**     | Controls access, but encryption does not add an extra layer of protection.                |
| Enable bucket versioning                               | ❌ **Distractor** | Protects against overwrites/deletes, not access.                                          |
| Enable S3 Object Lock                                  | ❌ **Distractor** | Prevents deletion; does not control read access.                                          |

***

### Scenario 4

**“AWS must never see plaintext data, even in memory.”**

| Option                                   | Classification   | Why                                                                                                         |
| ---------------------------------------- | ---------------- | ----------------------------------------------------------------------------------------------------------- |
| **Client‑side encryption before upload** | ✅ **Correct**    | Data is encrypted before reaching AWS. AWS only stores ciphertext.                                          |
| SSE‑KMS                                  | ⚠️ **Works**     | Data is decrypted inside AWS infrastructure under controlled conditions. AWS can see plaintext transiently. |
| SSE‑S3                                   | ❌ **Distractor** | Fully AWS‑managed; violates the requirement.                                                                |
| HTTPS upload                             | ❌ **Distractor** | Only protects data in transit.                                                                              |

***

### Scenario 5

**“Audit who accessed encrypted objects and when.”**

| Option                    | Classification   | Why                                                                 |
| ------------------------- | ---------------- | ------------------------------------------------------------------- |
| **CloudTrail + SSE‑KMS**  | ✅ **Correct**    | CloudTrail logs every KMS Decrypt action and S3 data access events. |
| Server access logging     | ⚠️ **Works**     | Logs requests, but **does not capture key usage visibility**.       |
| S3 replication metrics    | ❌ **Distractor** | Replication monitoring only.                                        |
| Enable default encryption | ❌ **Distractor** | Encrypts data but does not provide audit trails.                    |

***

### Scenario 6

**“Share encrypted S3 objects with another AWS account securely.”**

| Option                                                                     | Classification                  | Why                                                                                  |
| -------------------------------------------------------------------------- | ------------------------------- | ------------------------------------------------------------------------------------ |
| **Grant cross‑account access in both S3 bucket policy and KMS key policy** | ✅ **Correct**                   | Both object access *and* key access are required. This is the most common exam trap. |
| Grant only S3 bucket access                                                | ⚠️ **Works (fails at runtime)** | IAM allows access, but KMS denies decryption → AccessDenied.                         |
| Use pre‑signed URLs                                                        | ❌ **Distractor**                | Bypasses IAM but does not bypass KMS permissions.                                    |
| Copy objects to recipient bucket                                           | ❌ **Distractor**                | Data duplication, not secure sharing.                                                |

***

### Scenario 7

**“Protect S3 data from accidental deletion and ransomware.”**

| Option                      | Classification   | Why                                                                    |
| --------------------------- | ---------------- | ---------------------------------------------------------------------- |
| **S3 Versioning + SSE‑KMS** | ✅ **Correct**    | Allows restore of previous versions and protects data with encryption. |
| Enable SSE‑S3 only          | ⚠️ **Works**     | Encrypts data but does not protect against deletion/overwrite.         |
| Enable lifecycle policies   | ❌ **Distractor** | Automates deletion — opposite of protection.                           |
| Use Glacier Deep Archive    | ❌ **Distractor** | Cold storage, not immediate ransomware protection.                     |

***

### Scenario 8

**“Meet regulatory requirement for immutable, encrypted records.”**

| Option                                         | Classification   | Why                                                               |
| ---------------------------------------------- | ---------------- | ----------------------------------------------------------------- |
| **S3 Object Lock (Compliance mode) + SSE‑KMS** | ✅ **Correct**    | WORM storage + encryption; even root users cannot delete objects. |
| Versioning alone                               | ⚠️ **Works**     | Protects from overwrite but allows delete.                        |
| Bucket policy deny delete                      | ❌ **Distractor** | Policies can be changed; not immutable.                           |
| Glacier Vault Lock                             | ❌ **Distractor** | Different service and operational model.                          |

***

## 🧠 Exam Mental Shortcut

If the question mentions:

| Keyword                                 | Think                    |
| --------------------------------------- | ------------------------ |
| Compliance / audit                      | ✅ **SSE‑KMS**            |
| Prevent mistakes                        | ✅ Default encryption     |
| Extra layer of access control           | ✅ KMS permissions        |
| AWS must not see data                   | ✅ Client‑side encryption |
| Cross‑account access fails mysteriously | ✅ Missing KMS policy     |
| Ransomware                              | ✅ Versioning + KMS       |
| Immutability                            | ✅ Object Lock            |

***

