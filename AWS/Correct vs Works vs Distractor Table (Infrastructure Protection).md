Below is a **Solutions Architect Professional–grade** **✅ Correct vs ⚠️ Works vs ❌ Distractor table** covering **security and infrastructure‑protection services used with CloudFront**.

This is written exactly the way **AWS exam writers think**: *many answers “work”, but only one is architecturally correct and best*.

***

# ✅ CloudFront Security

## Correct vs Works vs Distractor Table (Infrastructure Protection)

***

## 1️⃣ Protect a Global CloudFront Distribution from DDoS Attacks

| Option                    | Classification | Why                                                        |
| ------------------------- | -------------- | ---------------------------------------------------------- |
| **AWS Shield (Standard)** | ✅ **CORRECT**  | Automatically protects CloudFront from L3/L4 DDoS at edge  |
| AWS Shield Advanced       | ⚠️ WORKS       | Adds cost protection + DRT, but not required unless stated |
| AWS WAF                   | ❌ DISTRACTOR   | WAF is L7 only, not volumetric DDoS                        |
| Security Groups           | ❌ DISTRACTOR   | Not applied at CloudFront edge                             |

📌 **Exam Cue**

> “DDoS protection for CloudFront” → **Shield**

***

## 2️⃣ Block SQL Injection & XSS Before Requests Reach the Origin

| Option                             | Classification | Why                                   |
| ---------------------------------- | -------------- | ------------------------------------- |
| **AWS WAF attached to CloudFront** | ✅ **CORRECT**  | Blocks L7 attacks at the edge         |
| WAF on ALB                         | ⚠️ WORKS       | Protects origin, not globally at edge |
| Shield Advanced                    | ❌ DISTRACTOR   | DDoS only, no L7 inspection           |
| Security Groups                    | ❌ DISTRACTOR   | L3/L4 only                            |

📌 **Exam Cue**

> “Block malicious HTTP requests globally” → **CloudFront + WAF**

***

## 3️⃣ Centralized Security Management Across Multiple AWS Accounts

| Option                   | Classification | Why                                                  |
| ------------------------ | -------------- | ---------------------------------------------------- |
| **AWS Firewall Manager** | ✅ **CORRECT**  | Centralized WAF & Shield enforcement across accounts |
| AWS WAF alone            | ⚠️ WORKS       | Manual per‑account management                        |
| AWS Organizations SCPs   | ❌ DISTRACTOR   | Governance, not traffic protection                   |
| IAM Permissions          | ❌ DISTRACTOR   | Identity ≠ infrastructure protection                 |

📌 **Exam Cue**

> “Multi‑account centralized security” → **Firewall Manager**

***

## 4️⃣ Prevent Direct Access to S3 Origin (Force Traffic via CloudFront)

| Option                          | Classification | Why                                   |
| ------------------------------- | -------------- | ------------------------------------- |
| **Origin Access Control (OAC)** | ✅ **CORRECT**  | S3 accessible only through CloudFront |
| Bucket policy (public)          | ⚠️ WORKS       | Weaker, can be bypassed               |
| Signed URLs only                | ❌ DISTRACTOR   | Does not stop direct S3 access        |
| IAM user access                 | ❌ DISTRACTOR   | Not applicable for public viewers     |

📌 **Exam Cue**

> “Prevent origin bypass” → **OAC**

***

## 5️⃣ Restrict Content Access by Geography

| Option                         | Classification | Why                            |
| ------------------------------ | -------------- | ------------------------------ |
| **CloudFront Geo‑restriction** | ✅ **CORRECT**  | Native, enforced at edge       |
| AWS WAF Geo rules              | ⚠️ WORKS       | More complex and costlier      |
| Route 53 Geo routing           | ❌ DISTRACTOR   | DNS routing ≠ request blocking |
| Shield Advanced                | ❌ DISTRACTOR   | No geo filtering               |

📌 **Exam Cue**

> “Block users from specific countries” → **CloudFront geo restriction**

***

## 6️⃣ Encrypt Traffic End‑to‑End (TLS Protection)

| Option                    | Classification | Why                                |
| ------------------------- | -------------- | ---------------------------------- |
| **ACM + CloudFront**      | ✅ **CORRECT**  | TLS termination at edge            |
| Self‑managed certs on EC2 | ⚠️ WORKS       | Higher ops burden                  |
| AWS KMS                   | ❌ DISTRACTOR   | Encryption at rest, not in transit |
| VPN                       | ❌ DISTRACTOR   | Not for public CDN traffic         |

📌 **Exam Cue**

> “Secure HTTPS globally at edge” → **ACM + CloudFront**

***

## 7️⃣ Protect Against HTTP Floods / Bots Globally

| Option                       | Classification | Why                                 |
| ---------------------------- | -------------- | ----------------------------------- |
| **AWS WAF rate‑based rules** | ✅ **CORRECT**  | Blocks abusive patterns at edge     |
| Shield Advanced              | ⚠️ WORKS       | Helps with scale attacks, not logic |
| Auto Scaling                 | ❌ DISTRACTOR   | Handles load, not attack            |
| CloudWatch alarms            | ❌ DISTRACTOR   | Detects issues, no prevention       |

📌 **Exam Cue**

> “HTTP flood / bot mitigation” → **WAF**

***

## 8️⃣ Protect the DNS Layer for CloudFront

| Option              | Classification | Why                                 |
| ------------------- | -------------- | ----------------------------------- |
| **Amazon Route 53** | ✅ **CORRECT**  | Highly resilient DNS, Anycast-based |
| CloudFront only     | ⚠️ WORKS       | Does not replace DNS                |
| Global Accelerator  | ❌ DISTRACTOR   | Traffic accel, not DNS              |
| Security Groups     | ❌ DISTRACTOR   | Do not apply to DNS                 |

📌 **Exam Cue**

> “DNS resilience / global name resolution” → **Route 53**

***

## 🧠 Mega Exam Elimination Table

| If the requirement mentions… | Correct Service      | Eliminate         |
| ---------------------------- | -------------------- | ----------------- |
| Volumetric DDoS              | **Shield**           | WAF, SG           |
| SQLi/XSS                     | **WAF**              | Shield            |
| Central governance           | **Firewall Manager** | WAF alone         |
| Prevent S3 bypass            | **OAC**              | Signed URLs alone |
| Geo blocking                 | **CloudFront Geo**   | Route 53          |
| TLS at edge                  | **ACM**              | KMS               |
| DNS protection               | **Route 53**         | CloudFront        |
| HTTP floods                  | **WAF**              | Auto Scaling      |

***

## ✅ One‑Line SAP‑C02 Summary

> **CloudFront relies on AWS Shield for DDoS protection, AWS WAF for Layer‑7 threats, Firewall Manager for centralized enforcement, Route 53 for DNS resilience, ACM for TLS, and OAC to protect origins from direct access.**

***

