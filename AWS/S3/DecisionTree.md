Excellent request — a **decision tree** is exactly how AWS expects you to reason in the exam.

Below is a **clean, hierarchical, SAP‑C02‑ready decision tree** for **Amazon S3 Encryption**, derived directly from the **Correct vs Works vs Distractor** logic we built earlier.

***

# 🌳 Amazon S3 Encryption — Decision Tree (Exam‑Grade)

Start at the top and follow the **first matching condition**.

***

## 1️⃣ Does AWS need to be **unable to see plaintext data at any time**?

*(Zero‑trust / extreme security requirement)*

    YES
     └─▶ Use Client‑Side Encryption
         ├─ Encrypt data before upload
         ├─ AWS stores only ciphertext
         └─ You manage keys completely

✅ **Client‑side encryption**  
❌ SSE‑S3 / SSE‑KMS (AWS can see plaintext transiently)

***

    NO
     └─▶ Continue ↓

***

## 2️⃣ Is **compliance, audit, or fine‑grained access control** required?

*(HIPAA / PCI / SOX / enterprise workloads)*

    YES
     └─▶ Use SSE‑KMS with Customer‑Managed CMK
         ├─ CloudTrail logs for key usage
         ├─ Key rotation
         └─ IAM + KMS dual authorization

✅ **Correct enterprise answer**  
⚠️ SSE‑S3 works but lacks control  
❌ HTTPS‑only answers (in‑transit only)

***

    NO
     └─▶ Continue ↓

***

## 3️⃣ Do you just need **basic at‑rest encryption**, simplicity over control?

    YES
     └─▶ Use SSE‑S3
         ├─ AWS‑managed keys
         ├─ No key visibility
         └─ Lowest operational overhead

⚠️ **Works**  
❌ Not suitable for strict compliance

***

    NO
     └─▶ Continue ↓

***

## 4️⃣ Must encryption be **enforced automatically** (prevent developer mistakes)?

    YES
     └─▶ Enable Default Bucket Encryption
         ├─ Prefer SSE‑KMS (best practice)
         └─ Optional: Deny PUT without encryption headers

✅ **Guardrail pattern**  
❌ Relying on dev discipline alone

***

    NO
     └─▶ Continue ↓

***

## 5️⃣ Do you need **additional access control beyond S3 IAM**?

*(Even admins should not read data accidentally)*

    YES
     └─▶ Use SSE‑KMS and restrict kms:Decrypt
         ├─ S3 permission alone is insufficient
         └─ KMS acts as second lock

✅ **Dual‑authorization pattern**  
❌ Versioning / Object Lock (do not control read access)

***

    NO
     └─▶ Continue ↓

***

## 6️⃣ Is this a **cross‑account S3 access** scenario?

    YES
     └─▶ Configure BOTH:
         ├─ S3 bucket policy (cross‑account access)
         └─ KMS key policy (allow external account)

✅ **Most common exam trap**  
⚠️ S3 policy alone → AccessDenied  
❌ Pre‑signed URLs (don’t bypass KMS)

***

    NO
     └─▶ Continue ↓

***

## 7️⃣ Do you need **protection against deletion, overwrite, or ransomware**?

    YES
     └─▶ Enable Versioning + SSE‑KMS

✅ Recoverable + encrypted  
❌ Lifecycle policies alone (may delete data)

***

    NO
     └─▶ Continue ↓

***

## 8️⃣ Are records required to be **immutable (WORM)** for regulation?

    YES
     └─▶ Use S3 Object Lock (Compliance mode) + Encryption

✅ Even root cannot delete  
❌ Bucket policies (can be changed)

***

## 🔑 One‑Screen Exam Memory Model

    Zero‑trust            → Client‑Side Encryption
    Compliance & audit    → SSE‑KMS
    Simple encryption     → SSE‑S3
    Prevent mistakes      → Default encryption
    Extra access control  → KMS permissions
    Cross‑account         → S3 + KMS policies
    Ransomware            → Versioning + KMS
    Immutability          → Object Lock + KMS

***

## ⚠️ Final Exam Advice

> If **two answers encrypt data**,  
> **choose the one that also controls access, logs usage, or enforces guardrails.**

That is almost **always SSE‑KMS**.

***
