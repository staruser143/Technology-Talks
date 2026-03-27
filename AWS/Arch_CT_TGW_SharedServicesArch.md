Below is a **reference‑grade Control Tower + Transit Gateway (TGW) + Shared Services architecture**, written the way an **enterprise architect** or **AWS exam solution** would expect—clear separation of concerns, governance-first, and scalable to hundreds of accounts.

I’ll cover:

1.  **High‑level logical architecture**
2.  **Account & OU structure (Control Tower)**
3.  **Network architecture with TGW**
4.  **Shared Services design**
5.  **Traffic flows (east‑west & north‑south)**
6.  **Guardrails & security model**
7.  **Common variations & exam traps**

***

# 1. High‑Level Architecture (Bird’s‑Eye View)

```text
                    ┌────────────────────────┐
                    │   Management Account   │
                    │ (AWS Organizations +   │
                    │  Control Tower Mgmt)   │
                    └──────────┬─────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
   ┌──────────▼───────┐ ┌──────▼────────┐ ┌────▼───────────┐
   │  Log Archive     │ │  Audit / Sec   │ │  Shared Svcs   │
   │  Account         │ │  Tooling       │ │  Account       │
   │ (CloudTrail,     │ │ (Security Hub, │ │ (AD, DNS, CI/CD│
   │  Config Logs)    │ │  GuardDuty)    │ │  etc.)         │
   └──────────┬───────┘ └──────┬────────┘ └────┬───────────┘
              │                 │                 │
              └─────────────────┼─────────────────┘
                                │
                       ┌────────▼────────┐
                       │ Transit Gateway │
                       │ (Central Hub)   │
                       └───────┬─────────┘
               ┌───────────────┼────────────────┐
               │               │                │
     ┌─────────▼────────┐ ┌────▼────────┐ ┌────▼──────────┐
     │ Dev Accounts     │ │ Test Accts   │ │ Prod Accounts │
     │ (Spoke VPCs)     │ │ (Spoke VPCs) │ │ (Spoke VPCs)  │
     └──────────────────┘ └──────────────┘ └───────────────┘
```

***

# 2. Control Tower Account & OU Structure

Control Tower **mandates some accounts** and **strongly encourages OU‑based governance**.

## Required / Core Accounts

| Account                      | Purpose                                         |
| ---------------------------- | ----------------------------------------------- |
| **Management Account**       | AWS Organizations + Control Tower orchestration |
| **Log Archive Account**      | Centralized CloudTrail, Config, VPC Flow Logs   |
| **Audit (Security) Account** | Security Hub, GuardDuty, IAM Access Analyzer    |

## Custom / Enterprise Accounts

| Account                     | Purpose                                     |
| --------------------------- | ------------------------------------------- |
| **Shared Services Account** | Network hub services & enterprise platforms |
| **Networking (optional)**   | TGW ownership (large-scale enterprises)     |
| **Workload Accounts**       | Dev / Test / UAT / Prod                     |

***

## OU Structure (Best Practice)

```text
Root
├── Security OU
│   ├── Audit Account
│   └── Log Archive Account
│
├── Shared-Services OU
│   └── Shared Services Account
│
├── Infrastructure OU
│   └── Networking Account (optional)
│
├── Non-Prod OU
│   ├── Dev Accounts
│   └── Test Accounts
│
└── Prod OU
    └── Production Accounts
```

✅ **Guardrails are applied at OU level**, not account level  
✅ Dev/Test get looser guardrails than Prod

***

# 3. Network Architecture (TGW‑Centric Hub‑and‑Spoke)

### Why TGW here?

Because:

*   Hundreds of VPCs
*   Shared inspection & services
*   Predictable routing
*   Centralized control

***

## TGW Placement

**Transit Gateway lives in:**

*   ✅ Shared Services account (most common)
*   ✅ OR dedicated Networking account (enterprises)

***

## VPC Types

### 1. **Shared Services VPC (Hub)**

Hosts:

*   AD / Azure AD DS / AWS Managed AD
*   Internal DNS (Route 53 Resolver)
*   Central CI/CD tooling
*   Bastion / Session Manager endpoints
*   Shared APIs / utilities

Attached to TGW ✅

***

### 2. **Spoke VPCs (Workload Accounts)**

Each workload account:

*   Owns its VPC(s)
*   Attaches VPC to TGW
*   Uses TGW for:
    *   East‑west traffic
    *   Access to shared services
    *   On‑prem connectivity

No VPC peering ❌  
No cross‑account routing hacks ❌

***

## TGW Route Tables (Critical Design Choice)

**Do NOT use a single TGW route table**

✅ Typical pattern:

| TGW Route Table        | Purpose                                 |
| ---------------------- | --------------------------------------- |
| **Shared‑Services‑RT** | Routes from workloads → shared services |
| **Prod‑RT**            | Isolated prod traffic                   |
| **NonProd‑RT**         | Dev/Test isolation                      |
| **OnPrem‑RT**          | Controlled hybrid routing               |

✅ Attachment‑per‑RT enables blast‑radius isolation

***

# 4. Shared Services Design (What Lives Here?)

Shared Services is **not a dumping ground** — it hosts **cross‑cutting enterprise capabilities**.

### Common Services

| Category          | Examples                                     |
| ----------------- | -------------------------------------------- |
| **Identity**      | AWS Managed AD, AD Connector                 |
| **DNS**           | Route 53 Resolver inbound/outbound endpoints |
| **Security**      | Central firewall, inspection VPC             |
| **DevOps**        | CI/CD runners, artifact repos                |
| **Networking**    | NAT, egress filtering                        |
| **Jump / Access** | Bastion (or SSM Session Manager)             |

***

## DNS Pattern (Very Common Exam Topic)

```text
On‑Prem DNS
   ↕  (VPN / DX)
Route53 Resolver (Inbound Endpoint)
   ↕
Shared Services VPC
   ↕
TGW
   ↕
Spoke VPCs
```

✅ Centralized DNS  
✅ No per‑account duplication

***

# 5. Traffic Flows

## East‑West (Account‑to‑Account)

```text
Dev VPC → TGW → Shared Services VPC
Dev VPC → TGW → Prod VPC (only if explicitly allowed)
```

Controlled by:

*   TGW route tables
*   Security Groups
*   NACLs

***

## North‑South (Internet / On‑Prem)

### Internet Ingress

*   ALB / NLB in workload VPC
*   OR centralized ingress (less common now)

### Internet Egress

*   Central NAT / firewall in Shared Services VPC
*   Route: Workload → TGW → Egress VPC → Internet

***

### On‑Prem Connectivity

```text
On‑Prem → DX / VPN → TGW → Workload VPCs
```

✅ Single connection  
✅ No VPN sprawl

***

# 6. Governance & Security (Control Tower Layer)

## Guardrails (Examples)

### Preventive (SCPs)

*   Disallow VPC peering
*   Prevent disabling CloudTrail
*   Block public S3 buckets
*   Restrict regions

### Detective (Config Rules)

*   Unencrypted EBS
*   Unapproved security group rules
*   Non‑compliant IAM usage

***

## Centralized Security Services

| Service             | Where            |
| ------------------- | ---------------- |
| CloudTrail          | Log Archive      |
| AWS Config          | Log Archive      |
| GuardDuty           | Security / Audit |
| Security Hub        | Security / Audit |
| IAM Access Analyzer | Audit            |

***

# 7. Common Variations & Gotchas (Very Exam‑Relevant)

### ✅ Variation: Separate Networking Account

Used when:

*   Very large enterprises
*   Team separation (NetOps vs Platform)

TGW owned by Networking account

***

### ❌ Anti‑Patterns

| Anti‑Pattern                     | Why It’s Bad      |
| -------------------------------- | ----------------- |
| VPC Peering everywhere           | Hard to scale     |
| Single TGW route table           | No isolation      |
| Shared Services in every account | Drift & cost      |
| No Control Tower                 | Manual governance |

***

# 8. One‑Line Executive Summary

> **AWS Control Tower provides governance, TGW provides scalable connectivity, and Shared Services centralize enterprise capabilities—together forming the standard enterprise multi‑account AWS architecture.**

***
