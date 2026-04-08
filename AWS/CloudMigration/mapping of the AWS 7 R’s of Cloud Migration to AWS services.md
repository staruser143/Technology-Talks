Below is a **clean, exam‑oriented mapping of the AWS 7 R’s of Cloud Migration to AWS services**, exactly how **AWS expects you to reason in SAP‑C02 and real migration planning**.

I’ll give you:

1.  **Primary service mapping**
2.  **Supporting services**
3.  **Why AWS uses those services**
4.  **Exam trap signals**

***

# ✅ AWS 7 R’s → AWS Services Mapping

## 1️⃣ REHOST (Lift‑and‑Shift)

**Goal:** Move fast with *no or minimal code change*

### 🔧 Primary AWS Services

*   **Amazon EC2**
*   **AWS Application Migration Service (MGN)**

### 🔁 Supporting Services

*   Amazon EBS
*   Amazon ELB
*   Auto Scaling (optional)
*   Amazon S3 (for migration staging)

### ✅ Why

*   MGN performs block‑level replication from on‑prem → AWS
*   EC2 mirrors on‑prem VM architecture

### 🧪 Exam Signals

*   “Fastest migration”
*   “As‑is”
*   “Minimal changes”
*   “Lift and shift”

➡️ **Answer:** Rehost

***

## 2️⃣ REPLATFORM (Lift‑Tinker‑Shift)

**Goal:** Minor optimization, no architectural redesign

### 🔧 Primary AWS Services

*   **Amazon RDS / Aurora**
*   **Amazon ECS / EKS**
*   **Elastic Beanstalk**

### 🔁 Supporting Services

*   Amazon ElastiCache
*   Amazon ALB
*   Amazon CloudWatch

### ✅ Why

*   Removes undifferentiated heavy lifting
*   Keeps app logic mostly unchanged

### 🧪 Exam Signals

*   “Small changes”
*   “Move DB to RDS”
*   “Reduce operational overhead”

➡️ **Answer:** Replatform

***

## 3️⃣ REFACTOR (Re‑architect)

**Goal:** Cloud‑native scalability, agility, resilience

### 🔧 Primary AWS Services

*   **AWS Lambda**
*   **Amazon API Gateway**
*   **Amazon DynamoDB / Aurora Serverless**

### 🔁 Supporting Services

*   Amazon EventBridge
*   Amazon SQS / SNS
*   AWS Step Functions
*   AWS App Mesh

### ✅ Why

*   Enables microservices
*   Event‑driven & serverless architectures
*   Horizontal scaling

### 🧪 Exam Signals

*   “Microservices”
*   “Event‑driven”
*   “Auto scaling without servers”

➡️ **Answer:** Refactor

***

## 4️⃣ REPURCHASE (Drop‑and‑Shop)

**Goal:** Replace with SaaS

### 🔧 Primary AWS / SaaS Services

*   **Amazon WorkMail**
*   **Amazon Chime**
*   **Amazon Connect**
*   3rd‑party SaaS (Salesforce, Workday, etc.)

### 🔁 Supporting Services

*   AWS IAM Identity Center
*   AWS Directory Service
*   AWS SSO integrations

### ✅ Why

*   Eliminates application ownership
*   Faster time to value

### 🧪 Exam Signals

*   “Replace custom app”
*   “Move to SaaS”
*   “No longer want to maintain software”

➡️ **Answer:** Repurchase

***

## 5️⃣ RETIRE

**Goal:** Eliminate unused systems

### 🔧 Primary AWS Services

*   **AWS Application Discovery Service**
*   **AWS Migration Hub**

### 🔁 Supporting Services

*   AWS Cost Explorer
*   AWS Trusted Advisor

### ✅ Why

*   Discovery identifies unused or low‑value apps
*   Reduces migration cost and scope

### 🧪 Exam Signals

*   “Low business value”
*   “No longer required”
*   “Duplicate functionality”

➡️ **Answer:** Retire

***

## 6️⃣ RETAIN (Revisit Later)

**Goal:** Keep on‑prem for now

### 🔧 Primary AWS Services

*   **AWS Outposts**
*   **AWS Storage Gateway**

### 🔁 Supporting Services

*   Amazon Direct Connect
*   Amazon VPN
*   Hybrid DNS (Route 53 Resolver)

### ✅ Why

*   Regulatory or latency constraints
*   Hybrid models

### 🧪 Exam Signals

*   “Cannot migrate now”
*   “Regulatory requirement”
*   “Latency‑sensitive”

➡️ **Answer:** Retain

***

## 7️⃣ RELOCATE (VMware Bulk Migration)

**Goal:** Move VMware environments *without any changes*

### 🔧 Primary AWS Services

*   **VMware Cloud on AWS**

### 🔁 Supporting Services

*   AWS Direct Connect
*   NSX‑T
*   VMware HCX

### ✅ Why

*   No OS conversion
*   No refactoring
*   Same VMware tooling

### 🧪 Exam Signals (VERY IMPORTANT)

*   “VMware workloads”
*   “No application changes”
*   “Move entire data center quickly”

➡️ **Answer:** Relocate ✅

***

# 🎯 One‑Page Exam Mapping Cheat Sheet

| R          | Key AWS Services              |
| ---------- | ----------------------------- |
| Rehost     | EC2, AWS MGN                  |
| Replatform | RDS, Aurora, Beanstalk        |
| Refactor   | Lambda, API Gateway, DynamoDB |
| Repurchase | WorkMail, SaaS                |
| Retire     | Migration Hub, Discovery      |
| Retain     | Outposts, Storage Gateway     |
| Relocate   | VMware Cloud on AWS           |

***

# ⚠️ SAP‑C02 High‑Probability Traps

### Trap 1: Rehost vs Relocate

*   **EC2 mentioned?** → Rehost
*   **VMware Cloud on AWS?** → Relocate

***

### Trap 2: Replatform vs Refactor

*   Managed DB only → Replatform
*   Microservices/serverless → Refactor

***

### Trap 3: Retain ≠ Retire

*   “Not now” → Retain
*   “No longer needed” → Retire

***

