Perfect — this table is **exam‑style**, **elimination‑oriented**, and tuned for **Solutions Architect Professional** thinking.

***

# AWS CloudFront

## ✅ Correct vs ⚠️ Works vs ❌ Distractor Table

> Use this table to **identify the best answer**, not just something that “functions”.

***

## 1️⃣ Global Static Content Delivery

| Option                        | Classification | Why                                                                       |
| ----------------------------- | -------------- | ------------------------------------------------------------------------- |
| **CloudFront + S3**           | ✅ **CORRECT**  | Global edge caching, lowest latency, lowest cost, AWS‑recommended pattern |
| S3 Static Website only        | ⚠️ WORKS       | Users access a single region → higher latency globally                    |
| ALB + EC2                     | ❌ DISTRACTOR   | No caching, higher cost, unnecessary compute                              |
| Route 53 Latency Routing only | ❌ DISTRACTOR   | DNS routing ≠ content acceleration                                        |

***

## 2️⃣ Global Users Accessing an API

| Option                       | Classification | Why                                           |
| ---------------------------- | -------------- | --------------------------------------------- |
| **CloudFront + API Gateway** | ✅ **CORRECT**  | Edge TLS, API acceleration, optional caching  |
| Multi‑Region API Gateway     | ⚠️ WORKS       | Complex, expensive, operational overhead      |
| Route 53 Geo Routing         | ❌ DISTRACTOR   | DNS routing only, no performance optimization |
| Global Accelerator           | ❌ DISTRACTOR   | L4 transport optimization, no HTTP caching    |

***

## 3️⃣ Need to Reduce Load on Origin Servers

| Option                 | Classification | Why                                                |
| ---------------------- | -------------- | -------------------------------------------------- |
| **CloudFront caching** | ✅ **CORRECT**  | Offloads requests at edge locations                |
| Auto Scaling           | ⚠️ WORKS       | Scales capacity but does not reduce request volume |
| ElastiCache            | ⚠️ WORKS       | Application‑level caching, more invasive           |
| Increase EC2 size      | ❌ DISTRACTOR   | Scales vertically, no architectural benefit        |

***

## 4️⃣ Protect S3 Content from Direct Access

| Option                         | Classification | Why                                        |
| ------------------------------ | -------------- | ------------------------------------------ |
| **CloudFront + OAC (OAI old)** | ✅ **CORRECT**  | Private S3, access only through CloudFront |
| Public S3 + Bucket Policy      | ⚠️ WORKS       | Less secure, public exposure               |
| Signed S3 URLs only            | ❌ DISTRACTOR   | Does not prevent bypass of S3 endpoint     |
| IAM role for users             | ❌ DISTRACTOR   | End users don’t assume IAM roles           |

***

## 5️⃣ Redirect Requests Based on Country / URL / Headers

| Option                   | Classification | Why                                   |
| ------------------------ | -------------- | ------------------------------------- |
| **CloudFront Functions** | ✅ **CORRECT**  | Ultra‑low‑latency edge logic          |
| Lambda\@Edge             | ⚠️ WORKS       | More powerful, but heavier and slower |
| Route 53 Geo Routing     | ❌ DISTRACTOR   | DNS‑level only, no request inspection |
| ALB listener rules       | ❌ DISTRACTOR   | Runs in one region                    |

***

## 6️⃣ DDoS and Web‑Layer Attack Protection (Global)

| Option                        | Classification | Why                                   |
| ----------------------------- | -------------- | ------------------------------------- |
| **CloudFront + WAF + Shield** | ✅ **CORRECT**  | Blocks at edge before reaching origin |
| WAF on ALB only               | ⚠️ WORKS       | Regional protection only              |
| Security Groups               | ❌ DISTRACTOR   | No L7 protection                      |
| Network ACLs                  | ❌ DISTRACTOR   | Stateless, no attack intelligence     |

***

## 7️⃣ Deliver Dynamic Personalized Content Worldwide

| Option                              | Classification | Why                                 |
| ----------------------------------- | -------------- | ----------------------------------- |
| **CloudFront (no/limited caching)** | ✅ **CORRECT**  | TCP optimization + edge termination |
| ALB only                            | ⚠️ WORKS       | Regional latency                    |
| ElastiCache                         | ❌ DISTRACTOR   | Data cache, not content delivery    |
| S3 Transfer Acceleration            | ❌ DISTRACTOR   | Upload acceleration only            |

***

## 8️⃣ Video / Media Streaming at Global Scale

| Option             | Classification | Why                               |
| ------------------ | -------------- | --------------------------------- |
| **CloudFront**     | ✅ **CORRECT**  | Byte‑range requests, edge caching |
| S3 pre‑signed URLs | ⚠️ WORKS       | No edge optimization              |
| EC2 file server    | ❌ DISTRACTOR   | Poor scalability                  |
| MediaStore alone   | ❌ DISTRACTOR   | Origin store, not CDN             |

***

## 9️⃣ CloudFront vs Route 53 (Classic Trap)

| Requirement              | Correct Service  |
| ------------------------ | ---------------- |
| DNS‑based routing        | ✅ Route 53       |
| Content acceleration     | ✅ **CloudFront** |
| Header/cookie inspection | ✅ **CloudFront** |
| Traffic splitting        | ❌ Route 53 alone |

***

## 🔑 Exam Elimination Heuristics (Memorize)

✅ Choose **CloudFront** whenever you see:

*   *Global users*
*   *Low latency delivery*
*   *Static + dynamic content*
*   *Security before origin*
*   *HTTP/HTTPS optimization*
*   *API acceleration*

❌ Eliminate if option only provides:

*   DNS routing (Route 53)
*   Load balancing without caching (ALB)
*   Compute scaling only (Auto Scaling)
*   L4 transport only (Global Accelerator)

***

## 💡 One‑Sentence Exam Rule

> **If the question mentions users, content, security, or APIs at global scale — CloudFront is either the answer or part of the answer.**

***

Say the word.
