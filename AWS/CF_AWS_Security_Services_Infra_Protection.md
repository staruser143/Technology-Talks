For **infrastructure protection when using Amazon CloudFront**, AWS expects you to think in **layers**. CloudFront is the **edge entry point**, and several AWS services work *with* it to protect your application, network, and backend infrastructure from attacks.

Below is an **exam‑ready, architect‑grade breakdown** aligned to the **AWS Well‑Architected Security Pillar – Infrastructure Protection**.

***

# ✅ AWS Services Used for Infrastructure Protection Along with CloudFront

***

## 1️⃣ AWS Shield (Standard & Advanced) – **DDoS Protection**

### ✅ What it protects against

*   Volumetric DDoS attacks (L3/L4)
*   Reflection/amplification attacks
*   SYN floods

### ✅ How it works with CloudFront

*   **Automatically enabled** for CloudFront (Shield **Standard**)
*   Absorbs traffic at AWS edge locations **before it reaches CloudFront or origins**

### Shield Standard vs Shield Advanced

| Feature                  | Shield Standard | Shield Advanced |
| ------------------------ | --------------- | --------------- |
| Enabled by default       | ✅ Yes           | ❌ No (paid)     |
| L3/L4 DDoS protection    | ✅ Yes           | ✅ Yes           |
| Cost protection          | ❌ No            | ✅ Yes           |
| DDoS Response Team (DRT) | ❌ No            | ✅ Yes           |
| Advanced metrics         | ❌ No            | ✅ Yes           |

📌 **Exam Cue**

> “DDoS protection for CloudFront” → **AWS Shield**

***

## 2️⃣ AWS WAF (Web Application Firewall) – **L7 Protection**

### ✅ What it protects against

*   SQL injection
*   Cross‑site scripting (XSS)
*   HTTP flood attacks
*   Bot traffic
*   OWASP Top 10 threats

### ✅ How it integrates

*   AWS WAF is **directly associated with CloudFront**
*   Rules are enforced **at edge locations**

### Typical WAF Rules

*   Managed rule groups (AWS / Marketplace)
*   Rate‑based rules
*   IP reputation rules
*   Geo‑blocking

📌 **Exam Cue**

> “Block malicious HTTP requests globally before reaching origin”  
> ✅ **CloudFront + AWS WAF**

***

## 3️⃣ AWS Firewall Manager – **Centralized Policy Enforcement**

### ✅ What it does

*   Centralized management of:
    *   AWS WAF rules
    *   Shield Advanced protections
    *   Security policies across **multiple accounts**

### ✅ How it fits with CloudFront

*   Automatically applies WAF and Shield rules to:
    *   CloudFront distributions
    *   ALBs
    *   API Gateways

📌 **Used when**

> “Multi‑account environment with centralized security governance”

📌 **Exam Cue**
✅ **Firewall Manager** + **Organizations** + **CloudFront**

***

## 4️⃣ Amazon Route 53 – **DNS‑Level Infrastructure Protection**

### ✅ Why Route 53 matters for protection

*   Highly resilient DNS (built on Anycast)
*   Resistant to DNS‑level DDoS attacks
*   Health checks + DNS failover

### ✅ Works *before* CloudFront

*   Users resolve DNS via Route 53
*   Then traffic enters CloudFront

📌 **Exam Cue**

> “Protect DNS layer and provide resilient name resolution”  
> ✅ **Route 53 + CloudFront**

***

## 5️⃣ AWS ACM (AWS Certificate Manager) – **TLS / Encryption Protection**

### ✅ What it protects

*   Data in transit
*   Man‑in‑the‑middle attacks

### ✅ How it works with CloudFront

*   ACM provides **free TLS certificates**
*   Certificates are deployed **at the edge**
*   Enforces HTTPS end‑to‑end

📌 **Exam Cue**

> “Secure global HTTPS access with TLS at edge”  
> ✅ **CloudFront + ACM**

***

## 6️⃣ CloudFront Native Security Features (Often Forgotten)

These are part of CloudFront but expected to be mentioned in exams as *infrastructure protection*.

### ✅ Signed URLs & Signed Cookies

*   Prevent unauthorized access
*   Protect private content

### ✅ Origin Access Control (OAC)

*   Prevent direct access to S3 origins
*   Ensures traffic flows **only through CloudFront**

### ✅ Geo‑restriction

*   Block access from specific countries

📌 **Exam Cue**

> “Prevent bypass of CloudFront” → **OAC + CloudFront**

***

## 7️⃣ Putting It All Together – Layered Protection Model

    User
     │
     ▼
    Route 53 (DNS resilience, Anycast)
     │
     ▼
    CloudFront (Edge)
     ├─ AWS Shield (DDoS)
     ├─ AWS WAF (L7 attacks)
     ├─ ACM (TLS)
     ├─ Geo restriction / Signed URLs
     │
     ▼
    Firewall Manager (Org-wide enforcement)
     │
     ▼
    Origin (ALB / EC2 / S3 / API Gateway)

***

## 🧠 Exam‑Focused Summary Table

| Threat Type             | AWS Service           |
| ----------------------- | --------------------- |
| L3/L4 DDoS              | **AWS Shield**        |
| HTTP attacks (SQLi/XSS) | **AWS WAF**           |
| Multi‑account rule mgmt | **Firewall Manager**  |
| DNS‑based attacks       | **Route 53**          |
| MITM / plaintext        | **ACM + CloudFront**  |
| Origin bypass           | **OAC / Signed URLs** |

***

## ✅ One‑Line SAP‑C02 Summary (Perfect for the Exam)

> **CloudFront is protected using AWS Shield for DDoS attacks, AWS WAF for Layer‑7 threats, Firewall Manager for centralized enforcement, Route 53 for DNS resilience, and ACM for secure TLS termination at the edge.**

***

