Below is a **clean, AWS‑exam‑ready decision tree** that helps you **quickly choose between Transit Gateway (TGW), VPC Peering, and AWS PrivateLink** — exactly the way AWS expects you to reason under exam pressure.

I’ll give you:

1.  **1‑page decision tree (textual visual)**
2.  **How exam questions encode these signals**
3.  **Quick memory shortcuts**
4.  **Classic distractor patterns**

***

# ✅ AWS Exam Decision Tree

## **TGW vs VPC Peering vs PrivateLink**

### Step‑by‑step elimination logic (this is the key)

```text
START
 |
 |-- Q1: Is connectivity ONE‑TO‑MANY or MANY‑TO‑MANY
 |       across multiple VPCs/accounts/on‑prem?
 |       (hub‑and‑spoke, shared services, hybrid)
 |        |
 |        ├─ YES → ✅ TRANSIT GATEWAY
 |        |
 |        └─ NO
 |
 |-- Q2: Is the requirement to expose only a
 |       SPECIFIC SERVICE (API, NLB‑based service)
 |       and NOT the entire VPC?
 |        |
 |        ├─ YES → ✅ AWS PRIVATE LINK
 |        |
 |        └─ NO
 |
 |-- Q3: Is it SIMPLE, point‑to‑point connectivity
 |       between TWO VPCs with NO transitivity?
 |        |
 |        ├─ YES → ✅ VPC PEERING
 |        |
 |        └─ NO → ✅ TRANSIT GATEWAY (default)
```

***

# 🧠 How AWS Exam Questions Encode These Choices

## ✅ Transit Gateway (TGW): **“Network Hub”**

### Look for phrases like:

*   *“multiple VPCs across accounts”*
*   *“shared services VPC”*
*   *“on‑premises connectivity”*
*   *“centralized routing”*
*   *“hub‑and‑spoke”*
*   *“hundreds of VPCs”*

### Exam reasoning:

> If AWS wants **central control, scalable routing, or hybrid connectivity**,  
> **TGW is almost always the correct answer**.

✅ Supports:

*   Transitive routing
*   On‑prem VPN / Direct Connect
*   Route isolation via multiple TGW route tables
*   Control Tower architectures

❌ Not used to expose a single service only

***

## ✅ AWS PrivateLink: **“Service Exposure Only”**

### Look for phrases like:

*   *“Expose a service securely”*
*   *“Consumers should not access provider VPC”*
*   *“No overlapping CIDR dependencies”*
*   *“SaaS provider”*
*   *“NLB endpoint service”*

### Exam reasoning:

> If the question says **“consume a service, NOT a network”**,  
> **PrivateLink beats TGW and Peering immediately**.

✅ Supports:

*   One‑way access
*   Service isolation
*   Cross‑account / cross‑org
*   No route sharing

❌ Cannot do:

*   VPC‑to‑VPC routing
*   On‑prem connectivity
*   Broad network access

***

## ✅ VPC Peering: **“Simple & Small”**

### Look for phrases like:

*   *“Two VPCs only”*
*   *“Low latency”*
*   *“No centralized routing needed”*
*   *“Small environment”*

### Exam reasoning:

> If it’s just **two VPCs and simple connectivity**, peering is cheapest and simplest.

✅ Works when:

*   Non‑transitive access is acceptable
*   CIDRs do not overlap
*   Very limited scale

❌ Disqualified if:

*   More than a few VPCs
*   Hub‑and‑spoke needed
*   On‑prem involved

***

# 🧠 Ultra‑Short Memory Rules (For Fast Elimination)

| If you see…                                 | Pick…           |
| ------------------------------------------- | --------------- |
| “Shared services”, “hub”, “central routing” | **TGW**         |
| “Expose API / service securely”             | **PrivateLink** |
| “Two VPCs only”                             | **VPC Peering** |
| “On‑prem + many VPCs”                       | **TGW**         |
| “SaaS access without VPC access”            | **PrivateLink** |

***

# ❌ Classic Exam Distractors (Very Common)

### Distractor 1: *“Use VPC Peering for shared services”*

❌ Wrong  
✅ Peering does not scale and is non‑transitive  
➡ Correct: **TGW**

***

### Distractor 2: *“Use TGW to expose a database to customers”*

❌ Wrong  
✅ Over‑exposes the network  
➡ Correct: **PrivateLink**

***

### Distractor 3: *“Use PrivateLink for on‑prem connectivity”*

❌ Wrong  
✅ PrivateLink is **VPC‑service only**  
➡ Correct: **TGW**

***

### Distractor 4: *“Use a single TGW route table”*

❌ Works but **not optimal**  
✅ AWS expects **multiple route tables** for isolation

***

# ✅ Exam‑Friendly One‑Line Differentiation

| Service             | Mental Model          |
| ------------------- | --------------------- |
| **Transit Gateway** | *Network hub*         |
| **PrivateLink**     | *Service tunnel*      |
| **VPC Peering**     | *Point‑to‑point wire* |

***
