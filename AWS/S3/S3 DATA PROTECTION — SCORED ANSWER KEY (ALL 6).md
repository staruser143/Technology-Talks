Got it ✅  
Below is a **scored answer key** in **Correct vs Works vs Distractor** format for **all 6 S3 Data Protection & Encryption scenarios**, written exactly in **SAP‑C02 / security‑architecture exam style**.

Assumption (best‑effort, per your instruction): these are the **6 canonical S3 protection scenarios** we implicitly built toward (encryption at rest, enforcement, cross‑account, compliance, ransomware, client‑side).  
If you want, we can later align this 1:1 with any earlier question text—but this answer stands fully on its own.

***

# ✅ S3 DATA PROTECTION — SCORED ANSWER KEY (ALL 6)

***

## **Q1. Mandatory encryption for all uploaded objects**

**Requirement:** Prevent any unencrypted object from ever being stored in S3.

### Answer Key

| Option                                                                         | Classification | Score | Why                                                                                  |
| ------------------------------------------------------------------------------ | -------------- | ----- | ------------------------------------------------------------------------------------ |
| **Enable S3 default encryption with SSE‑KMS**                                  | ✅ **Works**    | +6    | Encrypts objects automatically—but does **not block** plaintext PUTs without headers |
| **Bucket policy denying PUT unless `x‑amz‑server‑side‑encryption` is present** | ✅ **Correct**  | +10   | **Hard enforcement**; unencrypted uploads are rejected                               |
| Enable client‑side encryption in SDK                                           | ❌ Distractor   | 0     | Cannot enforce across all clients                                                    |
| Enable HTTPS‑only access                                                       | ❌ Distractor   | 0     | Protects transit only, not at‑rest                                                   |

**Exam takeaway:**

> *Default encryption ≠ enforcement.*  
> Only **bucket policy denial** truly enforces encryption.

***

## **Q2. Regulated data with audit & key control**

**Requirement:** HIPAA / PCI data, need audit logs of key usage.

### Answer Key

| Option                                | Classification | Score | Why                                       |
| ------------------------------------- | -------------- | ----- | ----------------------------------------- |
| **SSE‑KMS with customer‑managed CMK** | ✅ **Correct**  | +10   | Key policies, CloudTrail logs, rotation   |
| SSE‑S3                                | ❌ Distractor   | 0     | No customer key control or audit          |
| SSE‑C                                 | ❌ Distractor   | 0     | High operational risk, no rotation        |
| Client‑side encryption                | ✅ **Works**    | +5    | Meets requirement but adds app complexity |

**Exam takeaway:**

> **Compliance + audit = SSE‑KMS** (default enterprise answer).

***

## **Q3. Cross‑account S3 access fails with AccessDenied**

**Requirement:** External account can read encrypted objects.

### Answer Key

| Option                                              | Classification | Score | Why                                      |
| --------------------------------------------------- | -------------- | ----- | ---------------------------------------- |
| Add bucket policy allowing `s3:GetObject`           | ❌ Distractor   | 0     | IAM alone is insufficient                |
| **Update CMK key policy to allow external account** | ✅ **Correct**  | +10   | KMS is the second lock                   |
| Disable encryption                                  | ❌ Distractor   | −5    | Violates security requirement            |
| Use pre‑signed URLs                                 | ✅ **Works**    | +4    | Works but bypasses intended access model |

**Exam takeaway:**

> **S3 access = IAM policy AND KMS key policy**

***

## **Q4. Protect against ransomware and accidental deletion**

**Requirement:** Prevent overwrite or deletion of sensitive objects.

### Answer Key

| Option                                         | Classification | Score | Why                                      |
| ---------------------------------------------- | -------------- | ----- | ---------------------------------------- |
| Enable versioning                              | ✅ **Works**    | +5    | Recovery possible, but deletions allowed |
| **Object Lock (Compliance mode) + encryption** | ✅ **Correct**  | +10   | Immutable, even for root                 |
| Replication to another bucket                  | ❌ Distractor   | 0     | Replicates encrypted damage              |
| Lifecycle rules                                | ❌ Distractor   | 0     | No protection against overwrite          |

**Exam takeaway:**

> **Regulatory immutability = Object Lock + KMS**

***

## **Q5. AWS must never see plaintext data**

**Requirement:** Zero‑trust cloud encryption.

### Answer Key

| Option                                   | Classification | Score | Why                              |
| ---------------------------------------- | -------------- | ----- | -------------------------------- |
| SSE‑KMS                                  | ❌ Distractor   | 0     | AWS decrypts on access           |
| SSE‑C                                    | ✅ **Works**    | +6    | AWS doesn’t store key, but risky |
| **Client‑side encryption before upload** | ✅ **Correct**  | +10   | Plaintext never reaches AWS      |
| HTTPS‑only access                        | ❌ Distractor   | 0     | Transit protection only          |

**Exam takeaway:**

> **“AWS must not see data” → Client‑side encryption**

***

## **Q6. Detect sensitive data inside encrypted S3 buckets**

**Requirement:** Classify PII while data remains encrypted.

### Answer Key

| Option                                | Classification | Score | Why                              |
| ------------------------------------- | -------------- | ----- | -------------------------------- |
| Deny unencrypted uploads              | ❌ Distractor   | 0     | Not classification               |
| CloudTrail data events                | ❌ Distractor   | 0     | Access audit only                |
| **Amazon Macie with SSE‑KMS buckets** | ✅ **Correct**  | +10   | Macie can inspect encrypted data |
| S3 Inventory                          | ❌ Distractor   | 0     | Metadata only                    |

**Exam takeaway:**

> **PII discovery in S3 = Macie** (even with KMS).

***

# 🔢 SCORE INTERPRETATION (Exam‑Style)

*   **Correct (+10):** Best architected, AWS‑recommended answer
*   **Works (+4 to +6):** Technically valid, but incomplete or sub‑optimal
*   **Distractor (0 / −5):** Common exam trap or requirement mismatch

***

# 🧠 FINAL MASTER RULE (Memorize This)

> **S3 security questions always test *enforcement* vs *capability*.**  
> Most wrong answers **sound secure**, but **don’t enforce** the requirement.

***



Say the word.
