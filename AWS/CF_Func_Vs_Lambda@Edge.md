Here’s a **clean, exam‑recall decision tree** for choosing between  
**CloudFront Functions** and **Lambda\@Edge** — designed so you can **run it mentally in seconds during SAP‑C02**.

***

# ✅ CloudFront Functions vs Lambda\@Edge

## Exam Decision Tree

***

## 🔶 STEP 1 — Is custom logic required at the CloudFront edge?

**NO ❌**  
→ Use **native CloudFront behaviors** (cache policy, origin selection, redirects)

**YES ✅**  
→ Go to Step 2

***

## 🔶 STEP 2 — Does the logic need to inspect or modify the HTTP request/response?

(Headers, cookies, URI, query strings)

**YES ✅**  
→ Go to Step 3  
**NO ❌**  
→ Likely **not an edge‑compute use case**

***

## 🔶 STEP 3 — Which CloudFront event is involved?

### ✅ Viewer Request / Viewer Response only?

→ Go to Step 4

### ❌ Origin Request / Origin Response?

→ **Lambda\@Edge (ONLY option)**

📌 **Hard rule**

> CloudFront Functions **cannot** run on origin events.

***

## 🔶 STEP 4 — How complex is the logic?

### ✅ Simple, stateless, lightweight logic

Examples:

*   Redirect HTTP → HTTPS
*   URL rewrites
*   Add/remove headers
*   Country‑based redirects
*   Basic A/B testing
*   Bot blocking rules

→ **CloudFront Functions ✅**

***

### ❌ Complex or stateful logic

Examples:

*   Authentication / authorization
*   Token (JWT/OAuth) validation
*   Personalized responses
*   Complex conditional logic
*   Content transformation

→ Go to Step 5

***

## 🔶 STEP 5 — Does the logic need external access?

Needs to:

*   Call an API
*   Access DynamoDB / S3
*   Use Secrets Manager
*   Use IAM permissions
*   Perform network calls

**YES ✅**  
→ **Lambda\@Edge ✅**

**NO ❌**  
→ Re‑evaluate — CloudFront Functions may still be sufficient

***

# 🧠 Ultra‑Compressed Exam Recall (Memorize This)

    Simple + Viewer Request?
      → CloudFront Functions

    Needs origin access or AWS services?
      → Lambda@Edge

***

# 🚦 Hard Elimination Rules (Exam Gold)

| If the requirement mentions… | You MUST choose          |
| ---------------------------- | ------------------------ |
| Origin request/response      | **Lambda\@Edge**         |
| Call DynamoDB / API          | **Lambda\@Edge**         |
| JWT validation               | **Lambda\@Edge**         |
| Ultra‑low latency            | **CloudFront Functions** |
| Simple rewrites/redirects    | **CloudFront Functions** |

***

# ❌ Common Exam Traps (Avoid These)

❌ Choosing **Lambda\@Edge** for simple header manipulation  
❌ Choosing **CloudFront Functions** for authentication  
❌ Forgetting that **Functions cannot call AWS services**  
❌ Forgetting that **Functions only support viewer events**

***

# ✅ One‑Line SAP‑C02 Summary

> **Use CloudFront Functions for simple, ultra‑fast viewer‑level logic; use Lambda\@Edge when you need complex logic, origin access, or AWS service integration.**

***


