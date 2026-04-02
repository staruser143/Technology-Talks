Here’s a **clean, exam‑recall–oriented Security Decision Tree** for choosing between  
**AWS Shield**, **AWS WAF**, and **AWS Firewall Manager**.

This is tuned exactly for **SAP‑C02 / Pro‑level elimination** and mirrors how AWS frames **Infrastructure Protection** questions.

***

# ✅ Security Decision Tree

## Shield vs WAF vs Firewall Manager

***

## 🔶 STEP 1 — What type of attack are you protecting against?

### ❓ Is the threat **volumetric or network‑level**?

Examples:

*   SYN floods
*   UDP amplification
*   Large traffic spikes overwhelming capacity

✅ **YES** → Go to **STEP 2A**  
❌ **NO (Application‑layer threats)** → Go to **STEP 2B**

***

## 🔷 STEP 2A — Network / DDoS Attacks

### ✅ Do you need **basic, automatic protection** with no setup?

→ **AWS Shield Standard ✅ (CORRECT)**

*   Enabled by default
*   Protects CloudFront, Route 53, ALB, Global Accelerator

📌 If the question **does not** mention cost protection or DRT, stop here.

***

### ✅ Do you need **advanced protection**, such as:

*   DDoS cost protection
*   24×7 DDoS Response Team (DRT)
*   Advanced attack metrics

→ **AWS Shield Advanced ✅**

📌 **Exam Cue**

> “Mission‑critical workloads” or “financial impact of DDoS”

***

## 🔷 STEP 2B — Application‑Layer (L7) Attacks

Examples:

*   SQL injection
*   XSS
*   HTTP floods
*   Bots
*   Malicious user agents

### ✅ Need to **inspect HTTP requests** and block them?

→ Go to **STEP 3**

***

## 🔶 STEP 3 — How is security managed?

### ❓ Single account / single resource protection?

→ **AWS WAF ✅**

*   Attach to CloudFront / ALB / API Gateway
*   Managed rules, custom rules, rate‑based rules

📌 **Exam Cue**

> “Block SQLi/XSS before reaching origin”

***

### ❓ Multi‑account / centralized governance required?

Examples:

*   AWS Organizations
*   Security team enforces policies across many accounts
*   Automatic attachment of WAF + Shield

→ **AWS Firewall Manager ✅**

📌 **Important**
Firewall Manager does **not** block traffic itself  
It **deploys and enforces** WAF & Shield policies

***

## 🧠 Ultra‑Condensed Exam Recall (10 Seconds)

    Is it DDoS?
      ├─ Yes → Shield
      │    ├─ Advanced features needed? → Shield Advanced
      │    └─ Otherwise → Shield Standard
      │
      └─ No (HTTP attack)
           ├─ Single account → WAF
           └─ Multi-account → Firewall Manager

***

## 🚦 Hard Elimination Rules (Exam Gold)

| Requirement Mentions  | Choose           | Eliminate        |
| --------------------- | ---------------- | ---------------- |
| Volumetric attack     | Shield           | WAF              |
| SQLi / XSS            | WAF              | Shield           |
| Central governance    | Firewall Manager | WAF alone        |
| DRT / cost protection | Shield Advanced  | Shield Std       |
| HTTP rate limiting    | WAF              | Shield           |
| Org‑wide enforcement  | Firewall Manager | Shield/WAF alone |

***

## ❌ Common Exam Traps

❌ Choosing **WAF** for volumetric DDoS  
❌ Choosing **Shield** for SQL injection  
❌ Choosing **Firewall Manager** as if it blocks traffic directly  
❌ Forgetting Shield Standard is **always on**

***

## ✅ One‑Line SAP‑C02 Summary (Perfect Memorization)

> **Use AWS Shield for DDoS protection, AWS WAF for Layer‑7 application threats, and AWS Firewall Manager to enforce Shield and WAF policies centrally across multiple AWS accounts.**

***

