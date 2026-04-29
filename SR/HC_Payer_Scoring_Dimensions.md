Below is a **healthcare payer–specific service rationalization scoring model**, tuned for **claims, enrollment, eligibility, billing, broker/agent**, and **regulatory-heavy** environments (HIPAA, CMS, state rules).

This extends a generic model with **payer‑unique dimensions** that materially affect rationalization decisions.

***

## Healthcare Payer–Specific Scoring Dimensions

### Recommended Dimension Set (8 Dimensions)

|         # | Dimension                                  | Typical Weight |
| --------: | ------------------------------------------ | -------------: |
|         1 | **Regulatory & Compliance Criticality**    |            20% |
|         2 | **Business Capability Criticality**        |            15% |
|         3 | **Member / Provider Impact**               |            10% |
|         4 | **Data Sensitivity & PHI Exposure**        |            10% |
|         5 | **Operational Cost & Efficiency**          |            15% |
|         6 | **Technical Health & Maintainability**     |            10% |
|         7 | **Interoperability & Ecosystem Fit**       |            10% |
|         8 | **Strategic Fit (Future-State Alignment)** |            10% |
| **Total** |                                            |       **100%** |

***

## 1. Regulatory & Compliance Criticality (20%)

**Why it’s payer-specific:**  
Healthcare payers operate under **CMS, HIPAA, ACA, state DOI**, and audit mandates.

**What you score**

*   Required for CMS reporting or compliance?
*   Risk exposure if unavailable?
*   Manual regulatory workarounds?

**Scoring**

*   **5** – Directly required for compliance (claims, eligibility, grievances)
*   **3** – Supports regulated processes
*   **1** – No regulatory impact

✅ *High score → rarely retired; usually modernized*

***

## 2. Business Capability Criticality (15%)

**Focus**

*   Supports **core payer capabilities**:
    *   Enrollment
    *   Claims adjudication
    *   Premium billing
    *   Provider contracting
    *   Broker/agent management

**Scoring**

*   **5** – Core revenue or risk process
*   **3** – Supporting capability
*   **1** – Peripheral / optional

✅ *High score + low tech health → Refactor*

***

## 3. Member / Provider Impact (10%)

**Payer nuance**

*   Member and provider experience has **regulatory and brand implications**.

**Questions**

*   Does downtime impact member access?
*   Provider payment delays?

**Scoring**

*   **5** – Member‑ or provider‑facing critical path
*   **3** – Indirect impact
*   **1** – Back-office only

✅ *High score → higher availability and modernization priority*

***

## 4. Data Sensitivity & PHI Exposure (10%)

**Why critical**

*   PHI, PII, claims data, eligibility records
*   Breach risk directly impacts fines, trust

**Scoring**

*   **5** – Heavy PHI processing, broad access
*   **3** – Mixed or limited PHI
*   **1** – No sensitive data

✅ *Low score + high cost → retire first*

***

## 5. Operational Cost & Efficiency (15%)

**Beyond IT spend**

*   Manual touches
*   Call center deflection impact
*   Analyst hours
*   Rework due to errors

**Scoring**

*   **5** – Highly automated, low ops overhead
*   **3** – Moderate effort
*   **1** – Labor-heavy, manual

✅ *Low efficiency → Replace or replatform*

***

## 6. Technical Health & Maintainability (10%)

**Typical payer realities**

*   Mainframes
*   Vendor‑locked platforms
*   COBOL/PowerBuilder stacks

**Scoring**

*   **5** – Cloud-ready, API-first
*   **3** – Maintainable but aging
*   **1** – EOL tech, scarce skills

✅ *Low tech health + high business value → Refactor*

***

## 7. Interoperability & Ecosystem Fit (10%)

**Unique payer concern**

*   Integrates with:
    *   EDI (834/835/837)
    *   Clearinghouses
    *   State/Federal exchanges
    *   Broker systems
    *   Provider systems

**Scoring**

*   **5** – Standards-based, API‑driven
*   **3** – Mixed interfaces
*   **1** – Point-to-point legacy

✅ *Low score → Replace or consolidate*

***

## 8. Strategic Fit (Future-State Alignment) (10%)

**Looks ahead**

*   Aligns with:
    *   Target domain model
    *   Cloud & SaaS adoption
    *   Event-driven architecture
    *   Agentic / GenAI layers

**Scoring**

*   **5** – Target-state aligned
*   **3** – Neutral
*   **1** – Actively blocks roadmap

✅ *Low score → Retire or replace*

***

## Example: Enrollment Rules Engine

| Dimension              | Score | Weight | Weighted |
| ---------------------- | ----: | -----: | -------: |
| Regulatory Criticality |     5 |   0.20 |     1.00 |
| Business Capability    |     5 |   0.15 |     0.75 |
| Member Impact          |     4 |   0.10 |     0.40 |
| PHI Exposure           |     4 |   0.10 |     0.40 |
| Operational Efficiency |     2 |   0.15 |     0.30 |
| Technical Health       |     2 |   0.10 |     0.20 |
| Interoperability       |     2 |   0.10 |     0.20 |
| Strategic Fit          |     3 |   0.10 |     0.30 |
| **Total**              |       |        | **3.55** |

**Decision:** 🔧 **Refactor / Re-platform**

***

## Decision Heuristics (Payer-Specific)

| Pattern                           | Likely Action |
| --------------------------------- | ------------- |
| High compliance + low tech health | Refactor      |
| Low member impact + high cost     | Retire        |
| Redundant eligibility services    | Consolidate   |
| Manual grievance systems          | Replace       |
| Broker tools not ACA‑ready        | Re-platform   |

***

## Executive Summary (One Line)

> **In healthcare payers, service rationalization must prioritize regulatory survivability, member impact, and PHI risk—before cost optimization.**

***

