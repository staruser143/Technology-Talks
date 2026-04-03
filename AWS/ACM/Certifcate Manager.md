**AWS Certificate Manager (ACM)** is a managed service that helps you **provision, manage, and automatically renew SSL/TLS certificates** for use with AWS services. Its main purpose is to **secure application communications** by enabling **HTTPS (encryption in transit)** without the operational burden of managing certificates manually.

***

## Why AWS Certificate Manager Exists

Managing SSL/TLS certificates yourself typically involves:

*   Generating CSRs
*   Buying certificates
*   Installing them correctly
*   Tracking expiration dates
*   Rotating and renewing certificates

ACM **eliminates this operational overhead** by handling most of these steps automatically.

***

## Core Purposes of ACM

### 1. Enable HTTPS / TLS Encryption

ACM provides SSL/TLS certificates so that:

*   Data in transit is encrypted
*   Applications use **HTTPS** instead of HTTP
*   Clients trust your domain (via public CA–issued certificates)

✅ Protects against man-in-the-middle attacks  
✅ Meets security and compliance requirements

***

### 2. Centralized Certificate Management

ACM allows you to:

*   Request certificates from AWS-managed public CA
*   Import third‑party certificates
*   View, monitor, and manage certificates in one place

This avoids certificate sprawl across teams and accounts.

***

### 3. Automatic Certificate Renewal

For **ACM‑issued public certificates**:

*   AWS **automatically renews** them before expiration
*   No downtime
*   No manual action required

This is one of ACM’s **biggest value propositions**.

***

### 4. Native Integration with AWS Services

ACM certificates integrate **natively** with several AWS services:

| Service                                  | Use case                          |
| ---------------------------------------- | --------------------------------- |
| **Application Load Balancer (ALB)**      | HTTPS for web apps                |
| **Network Load Balancer (TLS listener)** | TLS termination                   |
| **CloudFront**                           | HTTPS for global content delivery |
| **API Gateway**                          | Secure REST / HTTP APIs           |
| **Elastic Beanstalk**                    | HTTPS for managed apps            |

➡️ You **cannot download private keys** for ACM public certs — they are meant to be attached directly to supported AWS services.

***

### 5. Support for Public and Private Certificates

ACM supports:

*   **Public certificates** (free, AWS-issued)
*   **Private certificates** via **ACM Private CA**

Private CA is commonly used for:

*   Internal microservices
*   mTLS
*   Internal domains (e.g., `*.corp.internal`)

***

## What ACM Is *Not* Used For (Exam Traps)

❌ **Direct EC2 or on‑prem certificate installation**  
→ Use imported certs or self‑managed solutions (e.g., OpenSSL)

❌ **Client-side certificate management**  
→ ACM is server-side only

❌ **Long-term offline certificate storage**  
→ Use AWS Secrets Manager or Parameter Store if you need cert files

***

## Key Exam-Style Summary

**If the question asks about:**

| Requirement                          | Correct Answer            |
| ------------------------------------ | ------------------------- |
| HTTPS for ALB / CloudFront           | ✅ AWS Certificate Manager |
| Automatic renewal of TLS certs       | ✅ AWS Certificate Manager |
| Managing public SSL certs at no cost | ✅ ACM                     |
| Private CA for internal services     | ✅ ACM Private CA          |
| Storing downloaded cert + key        | ❌ Not ACM                 |

***

## One‑Line Definition (Perfect for Exams)

> **AWS Certificate Manager is a managed service that provisions, deploys, and automatically renews SSL/TLS certificates for AWS services to enable encrypted communications.**

***

