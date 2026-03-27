Below are **Pro‑level AWS exam questions focused specifically on “inspection bypass” traps**, written exactly in the style AWS uses for **Solutions Architect Professional / Advanced Networking** exams.

Each question includes:

*   ✅ The **correct architecture**
*   ❌ **Subtle bypass traps** AWS loves to test
*   🧠 **Explicit elimination logic** (how to think under time pressure)
*   🎯 **Why AWS marks other options wrong**

***

# ✅ Pro‑Level Exam Questions

## **Inspection VPC – Traffic Bypass Traps**

***

## ✅ Question 1: Hidden Internet Egress Bypass (Very Common Trap)

### **Question**

A company uses **AWS Control Tower** and a **hub‑and‑spoke architecture with an Inspection VPC**.  
All workload VPCs are attached to an **AWS Transit Gateway (TGW)**.  
A security requirement mandates that **all internet‑bound traffic must be inspected** by centralized firewalls deployed in the Inspection VPC.

During a security review, the team discovers that **some workloads can still access the internet directly without inspection**.

Which action MOST LIKELY fixes the issue?

### **Options**

A. Disable Internet Gateways (IGWs) in all workload VPCs  
B. Add outbound rules to security groups to restrict internet access  
C. Update workload VPC route tables to point default routes to the Inspection VPC via TGW  
D. Enable AWS Network Firewall logging in the Inspection VPC

***

### ✅ Step‑by‑Step Elimination

**Step 1 – Key signal**

> *“Workloads can access the internet directly”*

This implies:

*   A **routing** problem, not a firewall or logging issue

***

**❌ Option B (Security Groups)**

*   SGs are **stateful**, not routing controls
*   Cannot force traffic through inspection  
    ➡ **Eliminate**

***

**❌ Option D (Logging)**

*   Observability only
*   Does NOT prevent bypass  
    ➡ **Eliminate**

***

**❌ Option A (Disable IGW)**

*   Technically works, but:
    *   Breaks workloads
    *   Not how AWS expects the fix
        ➡ **Not best answer**

***

### ✅ Correct Answer

✅ **C. Update workload VPC route tables to send 0.0.0.0/0 to TGW → Inspection VPC**

### 🎯 Pro‑Level Insight

> **Inspection bypass is almost always a ROUTE TABLE mistake**, not a firewall one.

***

***

## ✅ Question 2: TGW Route Table Association Trap (Classic Pro Trick)

### **Question**

A company deploys an **Inspection VPC with firewalls** and uses **Transit Gateway** to route traffic from workload VPCs.  
Traffic is expected to flow:

    Workload VPC → TGW → Inspection VPC → Internet

However, inspection logs show **intermittent traffic missing the inspection layer**.

Which configuration issue MOST LIKELY causes this behavior?

### **Options**

A. Workload VPCs are associated with the wrong TGW route table  
B. Security groups allow unrestricted outbound traffic  
C. NAT Gateways are deployed in multiple AZs  
D. AWS Network Firewall endpoints are unhealthy

***

### ✅ Step‑by‑Step Elimination

**Step 1 – Key phrase**

> *“Intermittent traffic missing the inspection layer”*

➡ Strong hint of **TGW routing asymmetry**

***

**❌ Option B (Security Groups)**

*   SGs don’t control routing paths  
    ➡ **Eliminate**

***

**❌ Option C (Multi‑AZ NAT)**

*   Best practice
*   No impact on inspection path  
    ➡ **Eliminate**

***

**⚠️ Option D (Firewall health)**

*   Would cause packet drops, not bypass  
    ➡ **Less likely**

***

### ✅ Correct Answer

✅ **A. Workload VPCs are associated with the wrong TGW route table**

### 🎯 Pro‑Level Insight

> **TGW route table association ≠ propagation**  
> Wrong association = silent inspection bypass

***

***

## ✅ Question 3: PrivateLink as an Inspection Bypass (Very Nasty Trap)

### **Question**

A company uses a centralized **Inspection VPC** to inspect all outbound traffic.  
A development team proposes using **AWS PrivateLink** to access a third‑party SaaS service, claiming it improves security.

The security team is concerned about **inspection bypass**.

Which statement is MOST ACCURATE?

### **Options**

A. PrivateLink traffic is automatically redirected through the Inspection VPC  
B. PrivateLink bypasses the Inspection VPC unless explicitly routed through firewalls  
C. PrivateLink always terminates in the Transit Gateway  
D. PrivateLink cannot be used in Control Tower environments

***

### ✅ Step‑by‑Step Elimination

**Step 1 – Recognize the trap**

> PrivateLink **does not use routing tables**

***

**❌ Option A**

*   False  
    ➡ **Eliminate**

**❌ Option C**

*   PrivateLink **does not traverse TGW**
    ➡ **Eliminate**

**❌ Option D**

*   Control Tower does not restrict PrivateLink
    ➡ **Eliminate**

***

### ✅ Correct Answer

✅ **B. PrivateLink bypasses the Inspection VPC unless explicitly designed for inspection**

### 🎯 Pro‑Level Insight

> **PrivateLink is a DATA‑PLANE shortcut**  
> It bypasses TGW, IGW, NAT, and firewalls unless explicitly inspected.

This is a **favorite Pro‑level trap**.

***

***

## ✅ Question 4: East‑West Bypass Between Workload VPCs

### **Question**

In a Control Tower environment, all workload VPCs attach to a Transit Gateway.  
An Inspection VPC is deployed for centralized inspection.  
However, **workloads in different accounts can communicate directly without inspection**.

What configuration MOST LIKELY causes this issue?

### **Options**

A. VPC peering exists between workload VPCs  
B. Route propagation is enabled between workload attachments  
C. AWS Network Firewall rules allow east‑west traffic  
D. Security groups are too permissive

***

### ✅ Step‑by‑Step Elimination

**Step 1 – Key observation**

> *“Direct workload‑to‑workload communication”*

➡ This is a **network topology mistake**

***

**❌ Option C**

*   Firewall rules apply *after* routing  
    ➡ **Eliminate**

**❌ Option D**

*   SGs don’t create paths  
    ➡ **Eliminate**

***

### ✅ Correct Answer

✅ **B. Route propagation is enabled between workload attachments**

### 🎯 Pro‑Level Insight

> To force inspection of **east‑west traffic**,  
> workload attachments must **NOT propagate routes to each other**

***

***

## ✅ Question 5: Return Path Bypass (Asymmetric Routing)

### **Question**

A company deploys an Inspection VPC for outbound traffic inspection.  
Outbound traffic is inspected correctly, but **return traffic occasionally bypasses the firewalls**, causing connection drops.

What is the MOST LIKELY root cause?

### **Options**

A. Network Firewall is deployed in only one AZ  
B. Stateful firewall rules are incorrectly configured  
C. Return routes in the Inspection VPC bypass the TGW  
D. Security groups do not allow ephemeral ports

***

### ✅ Step‑by‑Step Elimination

**Step 1 – Keyword**

> *“Return traffic bypasses”*

➡ Asymmetric routing problem

***

**❌ Option A**

*   Would cause AZ failures, not bypass  
    ➡ **Eliminate**

**❌ Option D**

*   SG issues = drops, not bypass  
    ➡ **Eliminate**

***

### ✅ Correct Answer

✅ **C. Return routes in the Inspection VPC bypass the TGW**

### 🎯 Pro‑Level Insight

> **Inspection architectures must be symmetric**  
> Same path out, same path back — through TGW and firewalls

***

# 🧠 MASTER RULE (Memorize for Pro Exam)

> If traffic bypasses inspection,  
> **look at ROUTES and ATTACHMENTS first — not firewalls**.

***

