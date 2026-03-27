An **Inspection VPC** is a **centralized network security VPC** that sits in the **traffic path** between workloads and external destinations (other VPCs, on‑premises networks, or the internet) so that **all traffic can be inspected, filtered, and controlled** before it is allowed to proceed.

In enterprise AWS architectures—especially **Control Tower + Transit Gateway (TGW)** environments—an Inspection VPC is a **best‑practice pattern**.

***

## 🔍 What Problem Does an Inspection VPC Solve?

Without an inspection layer:

*   Every workload team configures its own firewalls
*   Security rules drift across accounts
*   Compliance audits become hard
*   Lateral movement risks increase

✅ **Inspection VPC centralizes security enforcement**

***

## ✅ What Lives Inside an Inspection VPC?

An Inspection VPC typically hosts **network security appliances** and **control services**:

### Common Components

*   🔥 **Next‑Gen Firewalls**
    *   AWS Network Firewall
    *   Palo Alto, Fortinet, Check Point, etc.
*   🤖 **IDS / IPS**
*   🌐 **Egress filtering**
*   🔍 **Deep packet inspection**
*   📊 **Traffic logging & threat detection**

***

## 🧱 Where the Inspection VPC Sits (Big Picture)

```text
Workload VPCs
     |
     v
Transit Gateway
     |
     v
Inspection VPC
     |
     v
Internet / On‑Prem / Other VPCs
```

✅ **All traffic flows through inspection — no bypass**

***

## 🧠 Key Architectural Principle

> **Workloads do NOT talk directly to the internet or on‑premises networks**  
> They must go through the Inspection VPC first.

This enforces:

*   Least privilege networking
*   Central policy management
*   Regulatory compliance

***

## 🧩 Inspection VPC in a Control Tower Environment

### Typical Account Placement

| Account                                | Purpose              |
| -------------------------------------- | -------------------- |
| **Shared Services Account**            | Most common location |
| **Dedicated Network/Security Account** | Large enterprises    |

✅ Avoid placing inspection in workload accounts

***

## 🛣️ How Traffic Is Forced Through the Inspection VPC

### Transit Gateway Route Tables (Critical!)

You **must** use **multiple TGW route tables**

#### Example Routing

| Source                     | Route                 |
| -------------------------- | --------------------- |
| Workload VPC default route | → TGW                 |
| TGW workload RT            | → Inspection VPC      |
| Inspection VPC RT          | → Internet / DX / VPN |
| Return traffic             | → TGW → Workload VPC  |

✅ No direct workload → internet routes exist

***

## 🔐 What Traffic Is Typically Inspected?

### Outbound (Most Common)

*   Internet egress
*   SaaS access
*   API calls

### Inbound

*   On‑prem → AWS traffic
*   Third‑party partner access

### East‑West (Optional, Advanced)

*   VPC‑to‑VPC traffic
*   Usually only for regulated workloads

***

## 🧠 Inspection VPC vs Shared Services VPC

| Aspect               | Shared Services VPC | Inspection VPC   |
| -------------------- | ------------------- | ---------------- |
| Purpose              | Enable services     | Enforce security |
| Hosts                | AD, DNS, CI/CD      | Firewalls, IDS   |
| Data Plane           | Passive             | Inline           |
| Traffic interception | ❌ No                | ✅ Yes            |

✅ They are often **separate VPCs**

***

## 🚫 What an Inspection VPC Is NOT

❌ Not a monitoring‑only VPC  
❌ Not required for every workload  
❌ Not a replacement for IAM or SGs  
❌ Not usually used for service‑only access (use PrivateLink instead)

***

## ✅ When Should You Use an Inspection VPC?

### Strongly Recommended When:

*   You use **AWS Control Tower**
*   You have **TGW hub‑and‑spoke networking**
*   You have **regulatory or compliance requirements**
*   You need **central egress control**
*   You’re migrating a **traditional data‑center firewall model** to AWS

***

## ❌ When an Inspection VPC May Be Overkill

*   Small environments
*   Few VPCs
*   No compliance constraints
*   Internet access via ALB/NLB only

***

## 🎯 AWS Exam Perspective (Very Important)

### Exam keywords that imply an Inspection VPC:

*   *“traffic must be inspected”*
*   *“centralized firewall”*
*   *“security appliances”*
*   *“no direct internet access from workloads”*
*   *“shared inspection layer”*

✅ If you see these **with TGW** → **Inspection VPC is implied**

***

## ✅ One‑Line Summary (Memorize This)

> **An Inspection VPC is a centralized security VPC that inspects and controls traffic for multiple VPCs using TGW‑based routing.**

***

