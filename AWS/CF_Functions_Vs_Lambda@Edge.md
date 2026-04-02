Here’s a **clear, exam‑oriented, architect‑level comparison** of **CloudFront Functions vs Lambda\@Edge**, optimized for **AWS Solutions Architect Professional (SAP‑C02)** and real‑world decision‑making.

***

# CloudFront Functions vs Lambda\@Edge

## ✅ Side‑by‑Side Comparison

***

## 1️⃣ Core Purpose (Start Here in the Exam)

| Dimension        | **CloudFront Functions**                     | **Lambda\@Edge**            |
| ---------------- | -------------------------------------------- | --------------------------- |
| Primary goal     | **Ultra‑fast request/response manipulation** | **Full edge compute**       |
| Complexity level | Simple logic                                 | Complex logic               |
| Latency impact   | **Microseconds**                             | Milliseconds                |
| Execution layer  | **CloudFront edge**                          | CloudFront + Lambda service |

✅ **Exam Insight**  
If the logic is *simple and latency‑sensitive* → CloudFront Functions  
If logic is *complex or stateful* → Lambda\@Edge

***

## 2️⃣ Execution Model

| Aspect             | CloudFront Functions     | Lambda\@Edge     |
| ------------------ | ------------------------ | ---------------- |
| Runtime            | JavaScript (lightweight) | Node.js / Python |
| Cold starts        | ❌ None                   | ✅ Possible       |
| Max execution time | \~1 ms (very small)      | Up to 5 seconds  |
| Memory control     | ❌ No                     | ✅ Yes            |

📌 **Key Differentiator**  
CloudFront Functions are **not Lambda** — they are embedded directly in CloudFront.

***

## 3️⃣ Event Trigger Points (Very Exam‑Important)

| Event           | CloudFront Functions | Lambda\@Edge |
| --------------- | -------------------- | ------------ |
| Viewer request  | ✅ Yes                | ✅ Yes        |
| Viewer response | ✅ Yes                | ✅ Yes        |
| Origin request  | ❌ No                 | ✅ Yes        |
| Origin response | ❌ No                 | ✅ Yes        |

✅ **Exam Rule**  
If the requirement mentions **origin requests/responses**, CloudFront Functions are **eliminated**.

***

## 4️⃣ Capabilities Comparison

| Capability           | CloudFront Functions | Lambda\@Edge |
| -------------------- | -------------------- | ------------ |
| Modify headers       | ✅ Yes                | ✅ Yes        |
| Redirects / rewrites | ✅ Yes                | ✅ Yes        |
| Cookie inspection    | ✅ Yes                | ✅ Yes        |
| URL normalization    | ✅ Yes                | ✅ Yes        |
| Make network calls   | ❌ No                 | ✅ Yes        |
| Access AWS services  | ❌ No                 | ✅ Yes        |
| Heavy computation    | ❌ No                 | ✅ Yes        |

📌 **Exam Trap**  
If the logic requires **calling DynamoDB, S3, APIs, or secrets** → **Lambda\@Edge only**

***

## 5️⃣ Performance & Cost (Frequently Tested)

| Dimension    | CloudFront Functions | Lambda\@Edge              |
| ------------ | -------------------- | ------------------------- |
| Latency      | **Lowest possible**  | Higher                    |
| Cost         | Extremely low        | Higher                    |
| Billing unit | Per request          | Per invocation + duration |
| Scalability  | Automatic            | Automatic (but heavier)   |

✅ **Best Practice**  
AWS explicitly recommends using **CloudFront Functions when possible** and **Lambda\@Edge only when needed**.

***

## 6️⃣ Security & Isolation

| Aspect          | CloudFront Functions | Lambda\@Edge   |
| --------------- | -------------------- | -------------- |
| Access to VPC   | ❌ No                 | ❌ No           |
| IAM role        | ❌ No                 | ✅ Yes          |
| Secrets Manager | ❌ No                 | ✅ Yes          |
| WAF dependency  | Works with WAF       | Works with WAF |

📌 Neither runs inside your VPC — both execute at the **edge**.

***

## 7️⃣ Common Use Cases (Exam Gold)

### ✅ CloudFront Functions — Correct Use Cases

*   URL redirects (HTTP → HTTPS)
*   Domain or path normalization
*   Country‑based redirects
*   Header insertion/removal
*   Bot blocking logic
*   A/B testing (basic)

👉 **If it looks like “if‑else logic” → Functions**

***

### ✅ Lambda\@Edge — Correct Use Cases

*   Authentication & authorization
*   Token validation (JWT, OAuth)
*   Personalized responses
*   Calling backend APIs
*   Complex rewrites involving origin logic
*   Dynamic image manipulation

👉 **If it looks like “mini backend” → Lambda\@Edge**

***

## 8️⃣ Correct vs Works vs Distractor (Exam View)

| Scenario             | Correct                  | Works        | Distractor           |
| -------------------- | ------------------------ | ------------ | -------------------- |
| Simple redirect      | **CloudFront Functions** | Lambda\@Edge | Route 53             |
| Header‑based routing | **CloudFront Functions** | Lambda\@Edge | ALB                  |
| Auth at edge         | **Lambda\@Edge**         | —            | CloudFront Functions |
| Call DynamoDB        | **Lambda\@Edge**         | —            | CloudFront Functions |
| Lowest latency logic | **CloudFront Functions** | —            | Lambda\@Edge         |

***

## 9️⃣ Decision Heuristic (Memorize This)

    Is logic simple?
      └─ Yes → CloudFront Functions
      └─ No
         └─ Needs AWS services / network calls?
               └─ Yes → Lambda@Edge

***

## 🔟 One‑Line SAP‑C02 Definitions

**CloudFront Functions**

> Ultra‑low‑latency JavaScript functions for lightweight request/response manipulation at the CloudFront edge.

**Lambda\@Edge**

> Full‑fledged Lambda functions deployed to CloudFront edge locations for complex, stateful, or service‑integrated logic.

***

## ✅ Most Common Exam Trap

> **Choosing Lambda\@Edge when CloudFront Functions is sufficient**

AWS exam writers almost always expect:

*   ✅ CloudFront Functions for **simple, fast logic**
*   ✅ Lambda\@Edge **only when explicitly required**

***

