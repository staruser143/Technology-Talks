Below is the **exam‑grade “Correct vs Works vs Distractor” table** for **TGW Route Table design (Prod vs Shared vs Inspection)**.  
This mirrors **real SAP‑C02 elimination logic** — not theory.

***

## TGW Route Table – Correct vs Works vs Distractor

### **Scenario**

> A company uses AWS Transit Gateway with centralized inspection (AWS Network Firewall or 3rd‑party firewall). They want isolation between Prod, Non‑Prod, and Shared Services VPCs while ensuring *all traffic is inspected*.

***

### ✅ **Correct Architectures**

| Option                                                                        | Why it is CORRECT                                |
| ----------------------------------------------------------------------------- | ------------------------------------------------ |
| **Separate TGW route tables for Prod, Shared, Inspection**                    | ✅ Required for traffic steering and segmentation |
| Workload VPCs attach to **Prod / Shared RTs**                                 | ✅ Keeps blast radius isolated                    |
| All routes (0.0.0.0/0, on‑prem, east‑west) point to **Inspection attachment** | ✅ Guarantees centralized inspection              |
| Inspection VPC has **its own return‑only RT**                                 | ✅ Prevents routing loops                         |
| TGW shared via AWS RAM                                                        | ✅ Enables multi‑account hub‑and‑spoke            |
| No direct Prod ↔ Shared routes                                                | ✅ Prevents inspection bypass                     |

👉 **Keywords to spot in exam**:  
“dedicated inspection route table”, “multiple TGW route tables”, “no direct routing”

***

### ⚠️ **Works (But Not Best Practice)**

| Option                                    | Why it WORKS but is sub‑optimal            |
| ----------------------------------------- | ------------------------------------------ |
| Prod and Non‑Prod share one RT            | ⚠️ Less isolation, but traffic still flows |
| Shared Services has limited direct routes | ⚠️ Can work if firewall rules compensate   |
| One inspection RT + one workload RT       | ⚠️ Reduced flexibility                     |
| Firewall enforced mostly at NACL/SG       | ⚠️ Security shifts away from network layer |

👉 **Exam hint**:  
These options **function**, but **violate “strong isolation” wording**.

***

### ❌ **Distractors (Wrong / Exam Traps)**

| Option                                      | Why it is WRONG                    |
| ------------------------------------------- | ---------------------------------- |
| **Single TGW route table for all VPCs**     | ❌ No segmentation, no steering     |
| Prod routes directly to Shared VPC CIDRs    | ❌ Bypasses inspection              |
| Inspection VPC attached to same RT as Prod  | ❌ Routing loops / blackholes       |
| Rely only on VPC route tables (not TGW RTs) | ❌ TGW traffic ignored              |
| Use VPC peering instead of TGW              | ❌ Not scalable, no central control |
| Use PrivateLink for VPC‑to‑VPC inspection   | ❌ Wrong service purpose            |

👉 **Classic SAP‑C02 red flags**:

*   “single route table”
*   “direct communication”
*   “simplified routing”

***

## One‑Look Elimination Cheat Sheet

    Single TGW RT?            → ❌ Distractor
    Direct Prod ↔ Shared?     → ❌ Distractor
    Inspection mentioned?     → ✅ Likely correct
    Multiple TGW RTs?         → ✅ Strong signal

***

## Exam Memory Hook (30‑second recall)

> **Workload VPCs attach to their own route tables, but traffic always flows via the Inspection route table.**

***

