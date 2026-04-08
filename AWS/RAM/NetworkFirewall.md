Below is an **exam‑grade + real‑world explanation** of **AWS Network Firewall (ANF)** and **exactly how it is used in TGW Prod / Shared / Inspection scenarios**.

Think of this as **“what the service is” + “why it exists” + “how traffic actually flows”**.

***

# What is AWS Network Firewall?

**AWS Network Firewall** is a **managed, stateful, L3–L7 network firewall service** that you deploy **inside a VPC** to **inspect, allow, drop, or alert on traffic** flowing **into, out of, or across VPCs**.

It is AWS’s **native alternative to third‑party firewalls** (Palo Alto, Fortinet, Check Point), fully integrated with:

*   VPC route tables
*   Transit Gateway
*   AWS Organizations
*   Infrastructure as code

***

## What AWS Network Firewall is *not*

❌ Not a WAF (that’s HTTP/S only)  
❌ Not a Security Group/NACL replacement  
❌ Not attached directly to TGW  
❌ Not per‑instance

✅ It sits **in the data plane**, inline with routing

***

## Core capabilities (exam‑relevant)

| Capability              | What it means                |
| ----------------------- | ---------------------------- |
| **Stateful inspection** | Tracks connection state      |
| **Stateless rules**     | Fast L3/L4 filtering         |
| **Suricata rules**      | Deep packet inspection (DPI) |
| **Domain allow/deny**   | FQDN‑based egress control    |
| **Central policy**      | One firewall, many VPCs      |
| **Managed scaling**     | No firewall hosts to manage  |

***

# Where AWS Network Firewall sits in the architecture

**AWS Network Firewall is always deployed in a dedicated VPC**, commonly called:

> **Inspection VPC** / **Firewall VPC**

Inside that VPC:

*   You create **firewall endpoints**
*   One endpoint per AZ
*   Endpoints receive traffic via **VPC routing**

***

## Key rule (often tested)

> **Network Firewall is enforced by routing, not attachment**

If traffic **does not pass through the firewall endpoint**, it is **not inspected**.

***

# How AWS Network Firewall is used with Transit Gateway

This is where your **TGW route table decision tree** comes alive.

***

## Baseline flow (mental model)

    Workload VPC
       |
       |  (TGW attachment)
       v
    Transit Gateway
       |
       |  (TGW inspection route table)
       v
    Inspection VPC
       |
       |  (VPC route table → firewall endpoint)
       v
    AWS Network Firewall

Return traffic follows the **reverse path**.

***

## Step‑by‑step traffic flow (Prod example)

### Scenario

A Prod EC2 instance needs to reach:

*   Internet
*   On‑prem
*   Shared services

***

### 1️⃣ VPC route table (Prod VPC)

    0.0.0.0/0 → Transit Gateway
    10.0.0.0/8 → Transit Gateway

✅ All off‑VPC traffic goes to TGW

***

### 2️⃣ TGW Route Table (Prod RT)

    0.0.0.0/0 → Inspection VPC attachment
    Shared CIDRs → Inspection VPC attachment
    On‑prem → Inspection VPC attachment

✅ **No direct routes to Shared or On‑prem**

***

### 3️⃣ TGW Route Table (Inspection RT)

This table steers **all workloads into the firewall**.

    Prod CIDRs → Prod attachment
    Shared CIDRs → Shared attachment
    On‑prem → DX/VPN attachment

✅ Acts as **traffic director**

***

### 4️⃣ VPC Route Table (Inspection VPC)

This is the **critical firewall enforcement step**.

    0.0.0.0/0 → Firewall endpoint
    Prod CIDRs → Firewall endpoint
    Shared CIDRs → Firewall endpoint

✅ Every packet MUST traverse Network Firewall

***

### 5️⃣ Network Firewall policy evaluates traffic

Policies can:

*   Allow app‑to‑DB traffic
*   Block Dev → Prod
*   Allow internet only on 443
*   Block certain domains (e.g. github.com)

***

# How this maps to Prod / Shared / Inspection design

***

## ✅ Inspection VPC (Firewall VPC)

**Purpose**

*   Security enforcement
*   No workloads

**Characteristics**

*   Hosts AWS Network Firewall
*   Has its own TGW attachment
*   Has **special routing to firewall endpoints**

✅ This VPC is never reused for apps

***

## ✅ Prod VPCs

**Attach to**

*   Prod TGW route table

**Behavior**

*   Cannot talk to anything directly
*   All traffic forced through firewall

✅ Meets compliance + zero‑trust

***

## ✅ Shared Services VPC

**Attach to**

*   Shared TGW route table

**Why still inspected**

*   Shared services are a **high‑value target**
*   Prevent shared VPC becoming a transit hub

✅ DNS, AD, CI/CD still inspected

***

# Why AWS Network Firewall is chosen in these scenarios

### Compared to Security Groups / NACLs

| SG/NACL                        | Network Firewall |
| ------------------------------ | ---------------- |
| Stateless or attachment‑scoped | Centralized      |
| Per‑instance / subnet          | VPC‑level        |
| No DPI                         | Deep inspection  |
| Hard to audit                  | Central logging  |

***

### Compared to AWS WAF

| WAF                 | Network Firewall       |
| ------------------- | ---------------------- |
| Layer 7 HTTP only   | L3–L7                  |
| Public endpoints    | East‑west, north‑south |
| ALB/CloudFront only | Any IP traffic         |

***

### Compared to 3rd‑party firewalls

| 3rd‑Party               | AWS NFW       |
| ----------------------- | ------------- |
| EC2 management overhead | Fully managed |
| License mgmt            | Integrated    |
| Scaling complexity      | Automatic     |

***

# SAP‑C02 EXAM TRAPS

### ❌ Trap 1: “Attach Network Firewall to TGW”

→ **Impossible**  
Firewall is **VPC‑based only**

***

### ❌ Trap 2: “Use Security Groups for centralized inspection”

→ SGs **cannot enforce cross‑VPC inspection**

***

### ❌ Trap 3: “Single TGW route table + firewall”

→ No steering = bypass risk

***

### ✅ Correct answers mention:

*   **Inspection VPC**
*   **TGW route tables**
*   **Firewall endpoints**
*   **Centralized traffic inspection**

***

# One‑line exam definition

> **AWS Network Firewall is a managed, stateful network firewall deployed in an inspection VPC that enforces centralized traffic inspection when used with Transit Gateway and routing controls.**

***
