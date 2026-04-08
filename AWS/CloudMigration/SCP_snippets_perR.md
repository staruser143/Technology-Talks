Perfect — below are **exam‑safe, conceptual SCP “snippets” per R**.  
✅ **No JSON**  
✅ **No syntax noise**  
✅ Written exactly the way AWS expects you to *reason* in **SAP‑C02**

Think of these as **“policy intent cards”**, not copy‑paste SCPs.

***

# ✅ Sample SCP Snippets per R (Exam‑Safe)

> **Reminder (exam critical):**  
> SCPs **do NOT grant permissions**.  
> They define **what actions must never be allowed**, regardless of IAM.

***

## 1️⃣ REHOST — EC2‑Heavy, Lift‑and‑Shift

**Risk profile:**  
Legacy workloads, fast migration, minimal controls inside the app

### SCP Intent (What to deny / enforce)

*   ❌ Deny launching EC2 instances with public IPs
*   ❌ Deny use of non‑approved AWS Regions
*   ❌ Deny removal or disabling of:
    *   CloudTrail
    *   AWS Config
*   ❌ Deny EC2 instances without required tags (`env`, `app`, `costCenter`)

### Exam phrasing match

> “Prevent accidental exposure during lift‑and‑shift”

***

## 2️⃣ REPLATFORM — Managed Services, Minor Changes

**Risk profile:**  
Operational simplification, shared responsibility increases

### SCP Intent

*   ❌ Deny creation of self‑managed databases on EC2
*   ❌ Deny unencrypted RDS / Aurora instances
*   ❌ Deny disabling automated backups
*   ❌ Deny creation of unsupported instance families

### Exam phrasing match

> “Enforce use of approved managed services”

***

## 3️⃣ REFACTOR — Cloud‑Native / Serverless

**Risk profile:**  
Highly distributed, security must be enforced centrally

### SCP Intent

*   ❌ Deny public S3 buckets
*   ❌ Deny ALBs or API Gateways that are internet‑facing without WAF
*   ❌ Deny unencrypted:
    *   DynamoDB tables
    *   Lambda environment variables
*   ❌ Deny use of legacy compute services

### Allowed by design

*   ✅ Lambda
*   ✅ API Gateway
*   ✅ EventBridge / SQS / SNS

### Exam phrasing match

> “Enforce security guardrails for serverless workloads”

***

## 4️⃣ REPURCHASE — SaaS Replacement

**Risk profile:**  
Minimal infrastructure, identity‑centric

### SCP Intent

*   ❌ Deny creation of:
    *   EC2
    *   RDS
    *   EKS / ECS
*   ❌ Deny large data storage services
*   ❌ Deny public network resources

### Explicitly Allowed

*   ✅ IAM Identity Center
*   ✅ Directory Service
*   ✅ Networking for SaaS integration

### Exam phrasing match

> “Prevent unnecessary infrastructure creation in SaaS accounts”

***

## 5️⃣ RETIRE — No Cloud Landing

**Risk profile:**  
None (application does not migrate)

### SCP Intent

*   ✅ None required
*   ✅ App never receives an AWS account

### Exam phrasing match

> “Application is decommissioned and does not enter the Landing Zone”

⚠️ **Trap:** Do NOT create an OU just to retire something

***

## 6️⃣ RETAIN — Hybrid / Not Ready to Move

**Risk profile:**  
Regulated, latency‑sensitive, partially cloud‑connected

### SCP Intent

*   ❌ Deny provisioning of:
    *   Internet‑facing load balancers
    *   Public S3 buckets
*   ❌ Deny non‑approved Regions
*   ❌ Deny services not approved for hybrid use
*   ✅ Allow Direct Connect, VPN, Outposts, Storage Gateway

### Exam phrasing match

> “Maintain strict control over hybrid connectivity and exposure”

***

## 7️⃣ RELOCATE — VMware Cloud on AWS (⭐ exam favorite)

**Risk profile:**  
Guest OS unchanged, infra controlled by VMware

### SCP Intent

*   ❌ Deny native AWS compute services:
    *   Lambda
    *   ECS / EKS
*   ❌ Deny creation of standard EC2 outside VMware
*   ❌ Deny privilege escalation actions
*   ✅ Enforce logging and encryption

### Explicitly NOT enforced

*   App‑level changes
*   OS‑level settings (outside AWS control)

### Exam phrasing match

> “Govern the AWS account, not the guest OS”

***

# 📊 One‑Page Reflex Table (Very Exam Friendly)

| R          | SCP Focus                          |
| ---------- | ---------------------------------- |
| Rehost     | Network exposure, regions, logging |
| Replatform | Enforce managed services           |
| Refactor   | Security by default, serverless    |
| Repurchase | Block infrastructure entirely      |
| Retire     | No SCP (no account)                |
| Retain     | Hybrid safety, limited services    |
| Relocate   | Prevent native cloud drift         |

***

# 🔥 SAP‑C02 Trap Warnings

### Trap 1: “SCPs configure security settings”

❌ Wrong  
✅ SCPs only **deny actions**

***

### Trap 2: “All OUs share same SCPs”

❌ Wrong  
✅ SCPs are **strategy‑specific**

***

### Trap 3: “VMware workloads don’t need SCPs”

❌ Wrong  
✅ SCPs still control the **AWS account perimeter**

***

# ✅ Final Exam Sound‑Bite

> **Different R’s → Different risk → Different OU → Different SCP intent**

***

