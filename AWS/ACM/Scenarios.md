Alright — here are **brutal, exam‑grade multi‑select trap questions** on **AWS Certificate Manager (ACM)**.  
Each question explicitly says **“Choose ALL that apply”** (the most dangerous wording in SAP‑C02 / SA‑Pro).

I’ll give:

*   The **question**
*   ✅ **Correct answers**
*   ⚠️ **Why each distractor is wrong** (this is where the traps are)

***

## 🔥 Question 1: ACM Scope & Capabilities (Foundational Trap)

**Which of the following are valid capabilities of AWS Certificate Manager? (Choose ALL that apply)**

A. Automatically renew public TLS certificates issued by ACM  
B. Export private keys of ACM‑issued public certificates  
C. Provide free publicly trusted certificates  
D. Centralize certificate lifecycle management for supported AWS services  
E. Install certificates directly on EC2 instances via SSM

### ✅ Correct answers

✅ A  
✅ C  
✅ D

### ❌ Traps explained

*   ❌ **B** – You **cannot export private keys** for ACM‑issued public certs (absolute exam rule)
*   ❌ **E** – ACM does **not install certs on EC2**; it only attaches to integrated services (ALB, CF, API GW)

***

## 🔥 Question 2: “What Happens If You Don’t Use ACM?” Trap

**A production web application uses an ALB with HTTPS but does not use AWS Certificate Manager. What consequences apply? (Choose ALL that apply)**

A. You must manually renew certificates  
B. The application cannot use HTTPS  
C. Certificate expiration can cause downtime if not monitored  
D. You must store private keys securely yourself  
E. AWS will automatically rotate the certificates

### ✅ Correct answers

✅ A  
✅ C  
✅ D

### ❌ Traps explained

*   ❌ **B** – HTTPS still works with imported/self‑managed certs
*   ❌ **E** – Automatic rotation happens **only for ACM‑issued certs**

***

## 🔥 Question 3: Integrated Services Trap (Very Common)

**Which AWS services can directly use ACM‑issued public certificates? (Choose ALL that apply)**

A. Application Load Balancer  
B. CloudFront  
C. Amazon EC2  
D. API Gateway  
E. Amazon S3 static website hosting

### ✅ Correct answers

✅ A  
✅ B  
✅ D

### ❌ Traps explained

*   ❌ **C** – EC2 has **no direct ACM integration**
*   ❌ **E** – S3 static website endpoints **do not support ACM** (must use CloudFront)

***

## 🔥 Question 4: Private CA vs Public ACM (High‑Difficulty)

**Which scenarios require using ACM Private CA instead of standard ACM public certificates? (Choose ALL that apply)**

A. Issuing certificates for internal microservices  
B. Using private DNS names (e.g., `service.corp.local`)  
C. Enforcing mutual TLS (mTLS) within a VPC  
D. Publishing a public e‑commerce website  
E. Encrypting traffic between on‑prem services and AWS

### ✅ Correct answers

✅ A  
✅ B  
✅ C

### ❌ Traps explained

*   ❌ **D** – Public websites use **ACM public certificates**
*   ❌ **E** – TLS itself ≠ private CA; public certs can encrypt on‑prem ↔ AWS traffic

***

## 🔥 Question 5: Import vs Native ACM Certs (Sneaky)

**Which statements are true for imported third‑party certificates in ACM? (Choose ALL that apply)**

A. ACM will automatically renew imported certificates  
B. You can associate imported certificates with ALB  
C. You are responsible for monitoring certificate expiration  
D. Private keys are stored by ACM  
E. Imported certs cannot be used with CloudFront

### ✅ Correct answers

✅ B  
✅ C  
✅ D

### ❌ Traps explained

*   ❌ **A** – ACM **does NOT renew imported certs**
*   ❌ **E** – Imported certs **can** be used with CloudFront

***

## 🔥 Question 6: Architecture Decision Trap (SAP‑C02 Level)

**A company requires HTTPS for a global application, automatic certificate renewal, zero downtime rotation, and minimal operational overhead. Which components must be used? (Choose ALL that apply)**

A. CloudFront  
B. Application Load Balancer  
C. AWS Certificate Manager  
D. AWS Secrets Manager  
E. Self‑managed OpenSSL certificates

### ✅ Correct answers

✅ A  
✅ B  
✅ C

### ❌ Traps explained

*   ❌ **D** – Secrets Manager stores secrets, **not TLS lifecycle**
*   ❌ **E** – Self‑managed certs violate “minimal operational overhead”

***

## 🧠 Ultra‑Condensed Exam Kill Rules (Memorize)

*   **ACM public certs = free + auto‑renew + not exportable**
*   **If private key is needed → NOT ACM public**
*   **HTTPS on ALB / CF / API GW → ACM**
*   **EC2 / on‑prem TLS → self‑managed or imported**
*   **Internal domains / mTLS → ACM Private CA**
*   **Imported certs → you own renewal**

***


Just say the word.
