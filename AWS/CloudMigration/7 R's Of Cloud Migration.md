As per **AWS**, the **7 R’s of Cloud Migration** are a standard framework used to categorize application migration strategies when moving workloads to the cloud.

Here they are, **exactly as AWS defines them**, with exam‑friendly explanations.

***

## The 7 R’s of AWS Cloud Migration

### 1. **Rehost** (“Lift and Shift”)

*   Move applications **as-is** to AWS with minimal or no changes.
*   Commonly done using **AWS Application Migration Service (MGN)** or EC2.
*   ✅ Fastest migration
*   ❌ Does not optimize for cloud-native benefits

**Example:**  
On‑prem VM → EC2 instance

***

### 2. **Replatform** (“Lift, Tinker, and Shift”)

*   Make **small optimizations** without changing the core architecture.
*   Often involves moving to **managed services**.

**Example:**  
Self‑managed database → Amazon RDS  
Tomcat on EC2 → Same app but with RDS backend

***

### 3. **Refactor / Re-architect**

*   **Redesign the application** to be cloud‑native.
*   High effort, **maximum long‑term benefit**.
*   Common for scalability and resilience requirements.

**Example:**  
Monolith → Microservices using

*   Lambda
*   API Gateway
*   DynamoDB / Aurora
*   Event‑driven architecture

***

### 4. **Repurchase**

*   Replace the application with a **SaaS** solution.
*   Also known as **“Drop and Shop.”**

**Example:**

*   Custom CRM → Salesforce
*   On‑prem email → Amazon WorkMail / Microsoft 365

***

### 5. **Retire**

*   **Decommission** applications that are no longer needed.
*   Reduces cost and complexity.

**Example:**  
Legacy reporting app no longer used by business

***

### 6. **Retain** (or “Revisit”)

*   Keep the application **on-premises for now**.
*   Used when migration is not yet feasible due to:
    *   Compliance
    *   Latency
    *   Licensing
    *   Business constraints

**Example:**  
Mainframe or latency-sensitive trading system

***

### 7. **Relocate**

*   Move **entire environments** to AWS **without refactoring**, usually VMware-based.
*   Enabled by **VMware Cloud on AWS**.
*   No application changes and no OS‑level conversions.

**Example:**  
On‑prem vSphere cluster → VMware Cloud on AWS

✅ This is the **newest R** and frequently tested in **SAP‑C02**.

***

## One‑Line Exam Cheat Sheet

| R          | Key Reason                |
| ---------- | ------------------------- |
| Rehost     | Fastest migration         |
| Replatform | Small cloud optimizations |
| Refactor   | Cloud‑native scalability  |
| Repurchase | Move to SaaS              |
| Retire     | Eliminate unused apps     |
| Retain     | Not ready to move         |
| Relocate   | VMware bulk migration     |

***

## SAP‑C02 / Exam Pointer

*   **Rehost vs Relocate** is a common trap:
    *   **Rehost** → EC2, OS conversion
    *   **Relocate** → VMware Cloud on AWS, no conversion

