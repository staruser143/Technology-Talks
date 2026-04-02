Below is an **AWS‑exam‑optimized “Correct vs Works vs Distractor” decision table** for **Transit Gateway vs VPC Peering vs PrivateLink**.

This format matches **how AWS frames multiple‑choice questions**, where **more than one option might technically work**, but **only one aligns with best practice, scalability, and intent**.

***

# ✅ TGW vs VPC Peering vs PrivateLink

## **Correct vs Works vs Distractor Decision Table**

***

## Scenario 1: Shared Services across many VPCs and accounts

**Scenario**  
*A company has multiple AWS accounts (dev, test, prod). All workloads must access centralized DNS, Active Directory, and CI/CD tools hosted in a shared services VPC.*

| Option              | Category                 | Why AWS Classifies It This Way                                               |
| ------------------- | ------------------------ | ---------------------------------------------------------------------------- |
| **Transit Gateway** | ✅ **Correct**            | Designed for hub‑and‑spoke, transitive routing, scalable to hundreds of VPCs |
| VPC Peering         | ⚠️ Works but not Optimal | Requires many peering connections, non‑transitive, hard to manage            |
| PrivateLink         | ❌ Distractor             | Exposes individual services only, not full network access                    |

✅ **Exam logic:** “Shared services + multiple accounts” → TGW

***

## Scenario 2: Expose an internal API to consumer VPCs securely

**Scenario**  
*A platform team wants to expose an internal API running behind an NLB to several consumer AWS accounts without allowing network‑level access.*

| Option          | Category      | Why AWS Classifies It This Way                                    |
| --------------- | ------------- | ----------------------------------------------------------------- |
| **PrivateLink** | ✅ **Correct** | Secure, one‑way service exposure without routing or CIDR concerns |
| Transit Gateway | ❌ Distractor  | Over‑exposes the VPC and enables unnecessary routing              |
| VPC Peering     | ❌ Distractor  | Grants full network access, not service‑level isolation           |

✅ **Exam logic:** “Expose a service, not a VPC” → PrivateLink

***

## Scenario 3: Simple connectivity between two VPCs

**Scenario**  
*Two VPCs in the same region need low‑latency connectivity. No other VPCs are involved, and transitive routing is not required.*

| Option          | Category                 | Why AWS Classifies It This Way                         |
| --------------- | ------------------------ | ------------------------------------------------------ |
| **VPC Peering** | ✅ **Correct**            | Simple, low latency, cheapest for 1‑to‑1 communication |
| Transit Gateway | ⚠️ Works but not Optimal | Adds cost and complexity for a simple case             |
| PrivateLink     | ❌ Distractor             | Not meant for bidirectional VPC networking             |

✅ **Exam logic:** “Two VPCs only” → Peering

***

## Scenario 4: Hybrid connectivity (On‑prem + many VPCs)

**Scenario**  
*A company connects its on‑premises data center to AWS and wants all VPCs across multiple accounts to access on‑prem resources.*

| Option              | Category      | Why AWS Classifies It This Way                      |
| ------------------- | ------------- | --------------------------------------------------- |
| **Transit Gateway** | ✅ **Correct** | Central hub for VPN/DX with scalable VPC attachment |
| VPC Peering         | ❌ Distractor  | Cannot terminate VPN or provide transitive routing  |
| PrivateLink         | ❌ Distractor  | Does not support on‑prem connectivity               |

✅ **Exam logic:** “On‑prem + many VPCs” → TGW

***

## Scenario 5: SaaS provider offering access to customers’ VPCs

**Scenario**  
*A SaaS provider wants customers to access its service without peering or exposing its VPC CIDR.*

| Option          | Category      | Why AWS Classifies It This Way                    |
| --------------- | ------------- | ------------------------------------------------- |
| **PrivateLink** | ✅ **Correct** | Purpose‑built for SaaS service delivery           |
| Transit Gateway | ❌ Distractor  | Customers should not route into provider networks |
| VPC Peering     | ❌ Distractor  | CIDR overlap, scaling, and isolation issues       |

✅ **Exam logic:** “SaaS + customer isolation” → PrivateLink

***

## Scenario 6: Small org, 3 VPCs, no on‑prem

**Scenario**  
*A startup has 3 VPCs and wants them to communicate with each other.*

| Option                 | Category                 | Why AWS Classifies It This Way             |
| ---------------------- | ------------------------ | ------------------------------------------ |
| Transit Gateway        | ⚠️ Works but not Optimal | Overkill at small scale                    |
| **VPC Peering (mesh)** | ✅ **Correct**            | Cheaper, simpler for very small VPC counts |
| PrivateLink            | ❌ Distractor             | Not intended for full mesh connectivity    |

✅ **Exam logic:** AWS avoids TGW unless scale or hub‑and‑spoke is required

***

# 🧠 Exam‑Ready Pattern Recognition

| Keywords in Question                       | Correct Choice  |
| ------------------------------------------ | --------------- |
| Shared services, hub‑and‑spoke, governance | **TGW**         |
| Expose API / service securely              | **PrivateLink** |
| Two VPCs only                              | **VPC Peering** |
| On‑prem + AWS                              | **TGW**         |
| SaaS provider                              | **PrivateLink** |

***

# ✅ Final Exam Tip (Very Important)

> If **TGW and VPC Peering both work**, AWS almost always expects **TGW**  
> once **scale, governance, or hybrid connectivity** is mentioned.

***

