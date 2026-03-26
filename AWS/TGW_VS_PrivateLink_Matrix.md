Below is a **clean, exam‑oriented side‑by‑side matrix** comparing **Transit Gateway (TGW)**, **AWS PrivateLink**, and **VPC Peering**, written exactly in the way **AWS exams expect you to reason**.

This is meant to be a **mental lookup table** during elimination.

***

# ✅ AWS Exam Matrix: TGW vs PrivateLink vs VPC Peering

## 1. Core Purpose (most important exam differentiator)

| Dimension      | **Transit Gateway (TGW)**          | **AWS PrivateLink**              | **VPC Peering**                  |
| -------------- | ---------------------------------- | -------------------------------- | -------------------------------- |
| Primary intent | **Network‑level connectivity hub** | **Service‑level private access** | **Simple VPC‑to‑VPC networking** |
| Exam keyword   | *Centralized routing*              | *Least‑privilege service access* | *Direct connectivity*            |
| Think of it as | Virtual router                     | Private SaaS endpoint            | Flat network bridge              |

👉 **If the question says “service” → PrivateLink**  
👉 **If it says “routing / hub‑and‑spoke” → TGW**

***

## 2. Scope of Access (big exam giveaway)

| Dimension               | TGW                  | PrivateLink             | Peering    |
| ----------------------- | -------------------- | ----------------------- | ---------- |
| Access granularity      | Entire VPC / subnets | **Single service only** | Entire VPC |
| Provider VPC visibility | Visible (via routes) | **Completely hidden**   | Visible    |
| Blast radius            | Large                | **Very small**          | Medium     |

✅ **“Must not access other resources” → PrivateLink**

***

## 3. CIDR & Routing Behavior

| Dimension                    | TGW   | PrivateLink | Peering |
| ---------------------------- | ----- | ----------- | ------- |
| CIDR overlap supported       | ❌ No  | ✅ **Yes**   | ❌ No    |
| Requires route table changes | ✅ Yes | ❌ No        | ✅ Yes   |
| Transitive routing           | ✅ Yes | ❌ No        | ❌ No    |

✅ **Overlapping CIDR → PrivateLink (or NAT hacks, which exams never want)**

***

## 4. Scale & Operations (exam loves “dozens / hundreds”)

| Dimension                  | TGW         | PrivateLink | Peering               |
| -------------------------- | ----------- | ----------- | --------------------- |
| Scale across many accounts | ✅ Excellent | ✅ Excellent | ❌ Poor                |
| Operational overhead       | Medium–High | **Low**     | High (peering sprawl) |
| Central governance         | ✅ Strong    | ✅ Strong    | Weak                  |

✅ **“Dozens of accounts with minimal ops” → TGW or PrivateLink**  
👉 Then look at **service vs network** to choose.

***

## 5. Security & Isolation Model

| Dimension                | TGW          | PrivateLink          | Peering               |
| ------------------------ | ------------ | -------------------- | --------------------- |
| Isolation strength       | Medium       | **Very strong**      | Weak                  |
| Security boundary        | Routes + SGs | **Service boundary** | SGs only              |
| Risk of lateral movement | Possible     | **Near zero**        | High if misconfigured |

✅ **Exams prefer PrivateLink when isolation is emphasized**

***

## 6. Internet Exposure & Compliance

| Dimension             | TGW | PrivateLink | Peering |
| --------------------- | --- | ----------- | ------- |
| Traffic over internet | ❌   | ❌           | ❌       |
| NAT Gateway required  | ❌   | ❌           | ❌       |
| Compliance‑friendly   | ✅   | ✅✅✅         | ✅       |

⚠️ **“No internet” alone is NOT enough** — all three satisfy this.

***

## 7. Typical Exam Use Cases (memorize this)

### ✅ Transit Gateway (TGW)

Choose TGW when the question says:

*   “**Hub‑and‑spoke** network”
*   “**Centralized routing**”
*   “**On‑premises + multiple VPCs**”
*   “**East‑west traffic**”
*   “**Shared inspection / firewall VPC**”

🚫 Avoid TGW if:

*   Only **one service** needs to be exposed
*   Isolation is the top priority

***

### ✅ AWS PrivateLink

Choose PrivateLink when the question says:

*   “**Shared services**”
*   “**Only access a specific API**”
*   “**Provider VPC must be hidden**”
*   “**SaaS‑like access**”
*   “**Cross‑account service consumption**”
*   “**Overlapping CIDRs**”

✅ **Most common correct answer for service exposure questions**

***

### ✅ VPC Peering

Choose peering when:

*   Same or few accounts
*   Simple topology
*   Full VPC access is acceptable
*   Cost and simplicity matter more than isolation

🚫 Exams rarely pick peering for complex enterprise scenarios.

***

## 8. One‑Look Exam Decision Matrix (TL;DR)

| Question Signal           | Correct Choice      |
| ------------------------- | ------------------- |
| “Expose a service”        | **PrivateLink**     |
| “Least privilege access”  | **PrivateLink**     |
| “Shared services account” | **PrivateLink**     |
| “Central routing hub”     | **Transit Gateway** |
| “On‑prem + AWS”           | **Transit Gateway** |
| “Overlapping CIDRs”       | **PrivateLink**     |
| “Simple VPC‑to‑VPC”       | **VPC Peering**     |

***

## 9. Classic Exam Trap (very important)

> “Use Transit Gateway with route table isolation to restrict access”

🚫 **Wrong when the requirement is service‑only access**

AWS exams expect you to know:

*   **Routes ≠ isolation**
*   **PrivateLink = service boundary**
*   **TGW = network boundary**

***

## 10. Mental One‑Liner (exam gold)

> **If AWS wants you to think like a SaaS provider → PrivateLink**  
> **If AWS wants you to think like a network engineer → TGW**

***

