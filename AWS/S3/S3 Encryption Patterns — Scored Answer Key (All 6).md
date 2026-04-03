Got it Sridhar — here is the **scored answer key** for the **same 6 S3‑encryption scenario questions**, formatted exactly in your preferred **Correct vs Works vs Distractor** style.

This version aligns **directly with the decision tree** you approved.

***

# ✅ S3 Encryption Patterns — Scored Answer Key (All 6)

***

# **Q1 — Enforce encryption for every upload**

**Requirement:** No plaintext object must ever be stored.

| Option                                         | Type        | Score | Reason                                                   |
| ---------------------------------------------- | ----------- | ----- | -------------------------------------------------------- |
| **Bucket Policy: Deny PUT without SSE header** | **Correct** | +10   | True enforcement; blocks unencrypted uploads             |
| Enable Default Encryption (SSE‑KMS)            | Works       | +6    | Encrypts automatically but doesn’t prevent plaintext PUT |
| Client‑side encryption in SDK                  | Distractor  | 0     | Cannot enforce across all clients                        |
| HTTPS‑only access                              | Distractor  | 0     | Transit only                                             |

***

# **Q2 — Compliance, auditability, regulated data**

**Requirement:** HIPAA / PCI / SOX; need key‑use logs & key control.

| Option                 | Type        | Score | Reason                                          |
| ---------------------- | ----------- | ----- | ----------------------------------------------- |
| **SSE‑KMS (CMK)**      | **Correct** | +10   | Key policies, CloudTrail logs, customer control |
| Client‑side encryption | Works       | +5    | Meets requirement, but unnecessary overhead     |
| SSE‑S3                 | Distractor  | 0     | No customer key control; no audit logs          |
| SSE‑C                  | Distractor  | 0     | High operational burden; not best practice      |

***

# **Q3 — Cross‑account encrypted object access failing**

**Requirement:** External AWS account must read S3 object.

| Option                                              | Type        | Score | Reason                                          |
| --------------------------------------------------- | ----------- | ----- | ----------------------------------------------- |
| **Update CMK key policy to allow external account** | **Correct** | +10   | KMS is second lock; without this → AccessDenied |
| Add bucket policy only                              | Distractor  | 0     | IAM alone insufficient without CMK permissions  |
| Use pre‑signed URL                                  | Works       | +4    | Works but bypasses intended access model        |
| Disable encryption                                  | Distractor  | −5    | Violates security requirement                   |

***

# **Q4 — Ransomware protection and immutable data**

**Requirement:** Prevent delete/overwrite; preserve records.

| Option                                         | Type        | Score | Reason                                     |
| ---------------------------------------------- | ----------- | ----- | ------------------------------------------ |
| **Object Lock (Compliance Mode) + encryption** | **Correct** | +10   | WORM; even root cannot delete              |
| Versioning + encryption                        | Works       | +6    | Good protection but deletes still possible |
| Replicate to another bucket                    | Distractor  | 0     | Copies corruption too                      |
| Lifecycle policies                             | Distractor  | 0     | Doesn’t prevent overwrite/delete           |

***

# **Q5 — AWS must never see plaintext**

**Requirement:** Zero‑trust cloud; no plaintext inside AWS.

| Option                           | Type        | Score | Reason                                                         |
| -------------------------------- | ----------- | ----- | -------------------------------------------------------------- |
| **Client‑side encryption (CSE)** | **Correct** | +10   | Data encrypted before upload; AWS never sees plaintext         |
| SSE‑C                            | Works       | +6    | AWS doesn’t store keys but still sees plaintext for processing |
| SSE‑KMS                          | Distractor  | 0     | AWS decrypts inside service                                    |
| HTTPS‑only                       | Distractor  | 0     | Transit‑only encryption                                        |

***

# **Q6 — Detect PII in encrypted S3 buckets**

**Requirement:** Classify sensitive data while encryption remains enabled.

| Option                           | Type        | Score | Reason                               |
| -------------------------------- | ----------- | ----- | ------------------------------------ |
| **Macie + SSE‑KMS bucket**       | **Correct** | +10   | Macie can inspect KMS‑encrypted data |
| Unencrypted enforcement policies | Distractor  | 0     | Not classification                   |
| CloudTrail data events           | Distractor  | 0     | Audit only; cannot inspect contents  |
| S3 Inventory                     | Distractor  | 0     | Metadata only                        |

***

# ⭐ Final Master Score Patterns

**Correct = +10**  
AWS‑preferred, best‑architected, meets *all* constraints.

**Works = +4 to +6**  
Valid but suboptimal OR fails a hidden constraint.

**Distractor = 0 or −5**  
Common trap; violates requirement or exam intent.

***



Just tell me what you want next.
