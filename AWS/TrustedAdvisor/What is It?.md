**AWS Trusted Advisor** is an AWS service that helps you **optimize your AWS environment** by continuously **analyzing your account** and providing **best‑practice recommendations**.

### In simple terms

> Trusted Advisor tells you **what to fix, what to save, and what to strengthen** in your AWS account.

***

## What is it used for?

Trusted Advisor checks your AWS resources against AWS best practices in **five key categories**:

### 1. **Cost Optimization**

Helps reduce unnecessary spend

*   Idle EC2 instances
*   Unused load balancers
*   Underutilized EBS / RDS  
    ✅ Goal: **Lower AWS bill**

***

### 2. **Performance**

Improves system efficiency

*   High‑latency or under‑utilized resources
*   EC2 instance sizing issues  
    ✅ Goal: **Better performance at the same or lower cost**

***

### 3. **Security**

Identifies security risks

*   Open security groups (0.0.0.0/0)
*   IAM permissions issues (e.g., root access, no MFA)
*   Unrestricted S3 buckets  
    ✅ Goal: **Reduce attack surface**

***

### 4. **Fault Tolerance**

Improves availability and resilience

*   Single‑AZ resources
*   Lack of backups
*   Missing Load Balancer health checks  
    ✅ Goal: **Higher availability**

***

### 5. **Service Limits (Quotas)**

Prevents scaling failures

*   Alerts when approaching AWS service limits (EC2, ELB, VPC, etc.)  
    ✅ Goal: **Avoid outages due to quota exhaustion**

***

## How it works (at a glance)

*   Continuously scans your AWS account
*   Shows findings in the **Trusted Advisor dashboard**
*   Flags items as:
    *   ✅ Green (No issues)
    *   ⚠️ Yellow (Recommendation)
    *   ❌ Red (Action needed)

***

## Who gets full access?

| AWS Support Plan                    | Trusted Advisor Access      |
| ----------------------------------- | --------------------------- |
| Basic / Developer                   | **Limited checks only**     |
| **Business**                        | ✅ All checks                |
| **Enterprise / Enterprise On‑Ramp** | ✅ All checks + API + alerts |

> Full cost, security, and performance checks require **Business or higher** support.

***

## Where it fits (exam‑oriented)

✅ Use Trusted Advisor when the question mentions:

*   “**Best practice recommendations**”
*   “**Account‑wide optimization**”
*   “**Reduce AWS cost**”
*   “**Identify security gaps automatically**”

❌ Do **NOT** confuse it with:

*   **AWS Config** → compliance & configuration tracking
*   **CloudTrail** → API auditing
*   **Security Hub** → security findings aggregation

***

## One‑line summary (SAP‑C02 style)

> **AWS Trusted Advisor provides real‑time best‑practice recommendations to optimize cost, improve security, performance, fault tolerance, and monitor service limits in AWS accounts.**

