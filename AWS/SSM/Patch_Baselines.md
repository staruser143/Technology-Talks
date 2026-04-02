**Patch Baselines** are **rulesets in AWS Systems Manager (SSM) Patch Manager** that define **which OS patches are approved, rejected, or auto‑approved** for your servers.

> Think of a Patch Baseline as a **policy that decides “what is allowed to be patched”**, while **Maintenance Windows decide *when* patching happens**.

***

## ✅ What Is a Patch Baseline?

A **Patch Baseline** specifies:

*   ✅ **Which patches are approved**
*   ❌ **Which patches are rejected**
*   ⏱️ **When patches can be auto‑approved**
*   🎯 **Which operating systems it applies to**

It applies to:

*   **Linux instances**
*   **Windows instances**
*   **Hybrid (on‑prem) servers**

***

## 🔧 What Can You Control in a Patch Baseline?

### 1. **Patch Approval Rules**

Control patches based on:

| Control        | Example                      |
| -------------- | ---------------------------- |
| Severity       | Critical, Important          |
| Product        | Amazon Linux, Windows Server |
| Classification | Security, Bugfix             |
| CVE            | CVE‑2025‑xxxx                |
| Age            | Auto‑approve after 7 days    |

✅ Exam phrase:

> *“Automatically approve critical security patches after 7 days”*

***

### 2. **Explicit Overrides**

You can manually:

*   ✅ Approve specific patches
*   ❌ Reject specific patches

Used to:

*   Block bad patches
*   Enforce compliance

***

### 3. **Compliance Levels**

*   **Critical**
*   **High**
*   **Medium**
*   **Low**
*   **Unspecified**

Important for:

*   Audits
*   Reporting
*   Regulatory compliance

***

### 4. **OS‑Specific Baselines**

Patch Baselines are **OS‑specific**:

*   Amazon Linux baseline ≠ Windows baseline
*   Ubuntu baseline ≠ RHEL baseline

✅ You assign baselines by **OS type + tags**

***

## 🔁 Default vs Custom Patch Baselines

| Type                       | Description                      |
| -------------------------- | -------------------------------- |
| **Default patch baseline** | AWS‑managed, generic rules       |
| **Custom patch baseline**  | You fully control approval logic |

✅ Exam tip:

> **Production workloads almost always use custom patch baselines**

***

## 🧠 How Patch Baselines Fit Into the Big Picture

    Patch Baseline  →  WHAT to patch
    Maintenance Window → WHEN to patch
    SSM Agent → HOW it is applied

Patch Baseline **does nothing by itself** unless:

*   It’s associated to instances
*   Executed via Maintenance Window or Run Command

***

## ✅ Patch Baselines for Linux vs Windows

### 🐧 Linux

*   Uses native package managers (`yum`, `apt`, `dnf`)
*   Common rule:
    *   Auto‑approve **Critical security patches**
    *   Delay others

### 🪟 Windows

*   Integrates with Windows Update / Microsoft Update
*   Rules often based on:
    *   Classification (Security Updates, Critical Updates)

✅ One baseline **cannot** cover both OS types.

***

## 🧠 Exam‑Ready One‑Liners (Memorize)

*   ✅ **Patch Baselines define which patches are approved or rejected**
*   ✅ **They control WHAT gets patched, not WHEN**
*   ✅ **Different baselines are required for different OS types**
*   ✅ **Used by SSM Patch Manager**

***

## ⚠️ Common Exam Traps

| Trap                                | Why It’s Wrong                |
| ----------------------------------- | ----------------------------- |
| “Patch Baseline schedules patching” | ❌ Maintenance Windows do that |
| “One baseline for Linux + Windows”  | ❌ OS‑specific                 |
| “Baselines apply automatically”     | ❌ Must be associated          |

***

## ✅ When Patch Baselines Are the CORRECT Answer

Choose **Patch Baselines** when you see:

*   Compliance‑driven patching
*   Approval rules
*   Severity‑based controls
*   Audit requirements
*   OS‑specific patch policy

***

