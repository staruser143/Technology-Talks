Below is a **clear, exam‑grade decision matrix** that AWS expects you to **mentally apply and eliminate options** across **CloudFront vs Global Accelerator vs Route 53**.  
This is tuned for **Solutions Architect Professional (SAP‑C02)** and difficult scenario wording.

***

# ✅ CloudFront vs Global Accelerator vs Route 53

## Exam Decision Matrix

***

## 1️⃣ Primary Purpose (Anchor Row – Always Start Here)

| Dimension | **CloudFront**                  | **Global Accelerator**   | **Route 53**             |
| --------- | ------------------------------- | ------------------------ | ------------------------ |
| Core role | **Content delivery & security** | **Network acceleration** | **DNS routing**          |
| OSI layer | **Layer 7 (HTTP/HTTPS)**        | **Layer 4 (TCP/UDP)**    | **DNS (pre‑connection)** |
| Acts on   | Requests & responses            | Network packets          | DNS queries only         |

✅ *This row alone eliminates 2 options in most questions.*

***

## 2️⃣ Type of Traffic

| Traffic Type                  | Best Choice            | Why                |
| ----------------------------- | ---------------------- | ------------------ |
| Static web content            | **CloudFront**         | Edge caching       |
| Dynamic web pages             | **CloudFront**         | HTTP optimization  |
| REST / HTTP APIs              | **CloudFront**         | Edge TLS + caching |
| TCP / UDP apps (gaming, VoIP) | **Global Accelerator** | L4 optimization    |
| DNS‑only routing              | **Route 53**           | Domain resolution  |

***

## 3️⃣ Latency Optimization Model

| Aspect                   | CloudFront     | Global Accelerator | Route 53             |
| ------------------------ | -------------- | ------------------ | -------------------- |
| Where latency is reduced | Edge locations | AWS global network | Closest DNS resolver |
| Caching involved         | ✅ Yes          | ❌ No               | ❌ No                 |
| Connection reuse         | ✅ Yes          | ✅ Yes              | ❌ No                 |

📌 **Key Exam Insight**

> *Only CloudFront reduces latency by avoiding origin calls.*

***

## 4️⃣ Routing Intelligence Capability

| Capability                 | CloudFront | Global Accelerator | Route 53   |
| -------------------------- | ---------- | ------------------ | ---------- |
| Path‑based routing         | ✅ Yes      | ❌ No               | ❌ No       |
| Header / cookie inspection | ✅ Yes      | ❌ No               | ❌ No       |
| Geo‑based request logic    | ✅ Yes      | ⚠️ Limited         | ✅ DNS only |
| Weighted traffic split     | ⚠️ Limited | ✅ Yes              | ✅ Yes      |

✅ *If routing depends on request contents → CloudFront wins immediately.*

***

## 5️⃣ Security Integration

| Security Feature  | CloudFront | Global Accelerator | Route 53 |
| ----------------- | ---------- | ------------------ | -------- |
| AWS WAF           | ✅ Yes      | ❌ No               | ❌ No     |
| Shield DDoS       | ✅ Yes      | ✅ Yes              | ✅ Yes    |
| Geo blocking      | ✅ Yes      | ❌ No               | ❌ No     |
| Private S3 access | ✅ Yes      | ❌ No               | ❌ No     |

📌 **Exam rule**

> *If WAF is mentioned, Route 53 and Global Accelerator are eliminated.*

***

## 6️⃣ Origin / Backend Compatibility

| Backend Type | CloudFront | Global Accelerator | Route 53 |
| ------------ | ---------- | ------------------ | -------- |
| S3           | ✅ Native   | ❌ No               | ❌ No     |
| ALB          | ✅ Yes      | ✅ Yes              | ❌ No     |
| NLB          | ❌ No       | ✅ Yes              | ❌ No     |
| EC2          | ✅ Yes      | ✅ Yes              | ❌ No     |
| On‑prem HTTP | ✅ Yes      | ❌ No               | ❌ No     |

***

## 7️⃣ Cost & Usage Pattern (Exam Expectations)

| Pattern                     | CloudFront | Global Accelerator | Route 53 |
| --------------------------- | ---------- | ------------------ | -------- |
| Pay per request             | ✅ Yes      | ❌ No               | ✅ Yes    |
| Pay per accelerator         | ❌ No       | ✅ Yes              | ❌ No     |
| Cheapest for static content | ✅ Yes      | ❌ No               | ❌ No     |
| Cheapest for DNS routing    | ❌ No       | ❌ No               | ✅ Yes    |

***

## 8️⃣ High‑Probability Exam Scenarios

| Scenario                                  | Correct Choice         |
| ----------------------------------------- | ---------------------- |
| “Global users accessing static assets”    | **CloudFront**         |
| “Protect application with WAF globally”   | **CloudFront**         |
| “Accelerate TCP‑based application”        | **Global Accelerator** |
| “Route users to closest region using DNS” | **Route 53**           |
| “Route based on URL/cookie/header”        | **CloudFront**         |
| “Global failover without HTTP context”    | **Route 53**           |

***

## 9️⃣ Elimination Table (Pro‑Level Exam Use)

| If the question says… | Eliminate         | Keep         |
| --------------------- | ----------------- | ------------ |
| DNS only              | CloudFront, GA ❌  | Route 53 ✅   |
| TCP / UDP             | CloudFront, R53 ❌ | GA ✅         |
| Caching               | GA, R53 ❌         | CloudFront ✅ |
| WAF / XSS / SQLi      | GA, R53 ❌         | CloudFront ✅ |
| Header‑based routing  | GA, R53 ❌         | CloudFront ✅ |

***

## 🧠 10‑Second Exam Recall Formula

    DNS decision? → Route 53
    TCP/UDP speed? → Global Accelerator
    HTTP content + security + cache? → CloudFront

***

## ✅ One‑Line SAP‑C02 Summary

> **Route 53 decides *where* traffic goes, Global Accelerator decides *how fast packets travel*, and CloudFront decides *how content is delivered and protected*.**

***
