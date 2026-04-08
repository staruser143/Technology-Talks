Below is a **clean, exam‑safe Transit Gateway (TGW) route table decision tree** covering **Prod vs Shared vs Inspection** VPCs.  
This is the **exact mental model AWS expects** in SAP‑C02 and real enterprise networks.

***

# TGW Route Table Decision Tree

### (Prod vs Shared vs Inspection)

***

## Core principle (memorize this first)

> **TGW route tables define traffic *between* attachments, not inside VPCs.**  
> You almost always need **multiple TGW route tables**, not one.

***

## Step‑by‑step decision tree

### ✅ Step 1: Do you need centralized inspection (firewall, IDS/IPS, proxy)?

    IF traffic must be inspected (NFW, Palo Alto, proxy)
    → CREATE A DEDICATED INSPECTION ROUTE TABLE
    ELSE
    → SIMPLE HUB‑AND‑SPOKE (skip inspection table)

✅ **Almost always YES in enterprises**

***

### ✅ Step 2: Classify each VPC

For **every VPC**, ask:

    Is this VPC:
    A) Inspection (firewall)
    B) Shared services (AD, DNS, CI/CD, ECR, DX, logging)
    C) Production workload
    D) Non‑prod workload

This classification = **route table attachment logic**

***

### ✅ Step 3: Route table assignment rules

***

## 1️⃣ Inspection Route Table (Traffic Steering Table)

**What attaches here**

*   ❌ NOTHING routes *to* inspection directly
*   ✅ **All workload VPC attachments associate to this table**

**Routes inside**

    0.0.0.0/0 → Inspection VPC attachment
    10.0.0.0/8 → Inspection VPC attachment

✅ Forces **north‑south and east‑west** traffic through inspection  
✅ This table does **ONLY steering**, not workloads

> 📌 Also called: *Ingress / Egress / Security routing table*

***

## 2️⃣ Prod Route Table

**What attaches**

*   ✅ Production workload VPCs

**Routes inside**

    On‑prem CIDRs → Inspection attachment
    Shared VPC CIDRs → Inspection attachment
    Other Prod CIDRs → Inspection attachment

✅ **Never directly route to Shared or On‑prem**  
✅ All traffic leaves via **inspection**

***

## 3️⃣ Shared Services Route Table

**What attaches**

*   ✅ Shared Services VPC

**Routes inside**

    Prod CIDRs → Inspection attachment
    Non‑Prod CIDRs → Inspection attachment
    On‑prem → Inspection attachment

✅ Shared services **do not bypass inspection**
✅ Prevents being a “free transit” VPC

***

## 4️⃣ Optional: Non‑Prod Route Table

Best practice in regulated environments.

**Why separate from Prod**

*   Strong blast‑radius isolation
*   Prevents Dev → Prod lateral movement

**Routes inside**

    Shared CIDRs → Inspection attachment
    Internet / On‑prem → Inspection attachment

***

## ✅ FINAL DECISION TREE (Exam‑optimized)

    START
     |
     |-- Is centralized inspection required?
     |     |-- NO → Single TGW route table (rare)
     |     |-- YES
     |
     |-- Is this an Inspection VPC?
     |     |-- YES → Use its own RT (return traffic only)
     |     |-- NO
     |
     |-- Is this Prod?
     |     |-- YES → Attach to Prod RT
     |
     |-- Is this Shared Services?
     |     |-- YES → Attach to Shared RT
     |
     |-- Is this Non‑Prod?
     |     |-- YES → Attach to Non‑Prod RT

***

## Reference Architecture (text diagram)

    [ Prod VPCs ] ----\
                       >-- [ Inspection RT ] --> [ Firewall VPC ]
    [ Shared VPC ] ---/                              |
                                                       |
    [ Non-Prod VPCs ] -- Prod RT / Shared RT / NP RT <-+

***

## SAP‑C02 TRAPS (very important)

### ❌ Trap 1: “Attach Prod VPCs directly to Shared RT”

→ ❌ Bypasses inspection  
→ ❌ Fails security requirement

***

### ❌ Trap 2: “One TGW route table for everything”

→ ❌ No segmentation  
→ ❌ Impossible to steer traffic selectively

***

### ❌ Trap 3: “Inspection VPC uses same route table as Prod”

→ ❌ Circular routing  
→ ❌ Blackholes traffic

***

### ✅ Correct exam answer usually says:

*   **Multiple TGW route tables**
*   **Dedicated inspection routing**
*   **No direct Prod ↔ Shared routes**

***

## One‑line exam memory hook

> **Workloads attach to Prod/Shared RTs, but traffic always flows via the Inspection RT.**

***

