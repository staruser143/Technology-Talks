Here’s a **compact, exam‑recall decision tree** for **AWS CloudFront**, designed so you can **mentally walk it in 10–15 seconds** during the **SAP‑C02 / Pro‑level** exam.

***

# ✅ AWS CloudFront – Exam Recall Decision Tree

Use this **top‑down**, **yes/no elimination path**.

***

## 🔶 STEP 1 — Are users geographically distributed (global or multi‑region)?

**YES ✅** → Go to Step 2  
**NO ❌** → CloudFront usually **not required** (consider ALB, NLB, regional services)

> 📌 *Global users is the #1 CloudFront trigger.*

***

## 🔶 STEP 2 — Is the traffic HTTP / HTTPS (L7)?

**YES ✅** → Go to Step 3  
**NO ❌** → CloudFront is **eliminated** → consider **Global Accelerator (L4)**

> 📌 *CloudFront ≠ TCP/UDP acceleration.*

***

## 🔶 STEP 3 — Is the workload content‑based?

(Any of the following)

*   Static files
*   Web app pages
*   APIs
*   Media streams
*   Downloads
*   Authenticated HTTP responses

**YES ✅** → Go to Step 4  
**NO ❌** → CloudFront usually **not optimal**

***

## 🔶 STEP 4 — What is the PRIMARY requirement?

### ✅ A. Reduce latency close to users

→ **CloudFront**

### ✅ B. Reduce origin load / caching

→ **CloudFront**

### ✅ C. Edge‑level security (WAF, DDoS, geo‑block)

→ **CloudFront**

### ✅ D. Smart routing (headers, cookies, URL)

→ **CloudFront Functions / Lambda\@Edge**

📌 *If any answer here is YES → CloudFront stays IN.*

***

## 🔶 STEP 5 — What is the origin?

| Origin       | CloudFront Fit  |
| ------------ | --------------- |
| S3           | ✅ Perfect fit   |
| ALB          | ✅ Very common   |
| API Gateway  | ✅ Exam favorite |
| EC2          | ✅ Supported     |
| On‑prem HTTP | ✅ Supported     |

👉 **Origin type almost never disqualifies CloudFront**

***

## 🔶 STEP 6 — Special Exam Forks (High‑Value)

### ❓ Need DNS‑based routing only?

✅ **Route 53**

### ❓ Need L4 acceleration (gaming, TCP apps)?

✅ **Global Accelerator**

### ❓ Need request inspection or caching?

✅ **CloudFront**

***

## 🔶 STEP 7 — Security Requirement Check

If the question mentions:

*   DDoS
*   SQL injection
*   Bots
*   Geo blocking
*   Private S3 access
*   Signed URLs / cookies

✅ **CloudFront becomes the BEST answer**

***

# 🧠 Ultra‑Short Memory Version (What to Recall in Exam)

    Global users?
      └─ Yes
         └─ HTTP/HTTPS?
            └─ Yes
               └─ Content / API / Web?
                  └─ Yes → CLOUD FRONT ✅

***

# 🔥 Elimination Cheats (Extremely Exam‑Useful)

| If you see this          | Eliminate    |
| ------------------------ | ------------ |
| DNS routing only         | CloudFront ❌ |
| TCP/UDP latency          | CloudFront ❌ |
| Regional intranet app    | CloudFront ❌ |
| Need header/cookie logic | Route 53 ❌   |
| Need caching + security  | ALB only ❌   |

***

# ✅ Exam‑Perfect One‑Liner

> **If the problem involves global users accessing HTTP‑based content or APIs and mentions latency, security, or scaling — CloudFront is either the answer or part of the answer.**

Just say which one 👋
