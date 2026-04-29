Below is a **practical, industry‑grade Service Rationalization Scoring Model** that you can directly use in enterprise architecture reviews, cloud migration programs, or portfolio optimization initiatives.

***

## Service Rationalization Scoring Model

### 1. Scoring Overview

Each service is evaluated across **five dimensions**, scored on a **1–5 scale** and multiplied by a **weight**.  
The weighted total determines the rationalization decision.

**Score scale (common across dimensions):**

*   **5** – Excellent / ideal
*   **4** – Good
*   **3** – Acceptable
*   **2** – Weak
*   **1** – Poor / problematic

***

## 2. Dimensions and Weights (Typical Enterprise Default)

| Dimension              |   Weight | What It Measures                                |
| ---------------------- | -------: | ----------------------------------------------- |
| **Business Value**     |      30% | Business criticality, revenue/regulatory impact |
| **Technical Health**   |      20% | Maintainability, scalability, security          |
| **Cost Efficiency**    |      15% | TCO, licensing, operations                      |
| **Usage & Redundancy** |      15% | Actual adoption, overlap                        |
| **Strategic Fit**      |      20% | Alignment to target architecture & roadmap      |
| **Total**              | **100%** |                                                 |

> ✅ You can tune weights (e.g., higher **Cost** during cost-reduction waves, higher **Strategic Fit** before cloud migration).

***

## 3. Detailed Scoring Criteria

### A. Business Value (30%)

| Score | Definition                                         |
| ----- | -------------------------------------------------- |
| 5     | Mission‑critical / regulatory / revenue‑generating |
| 4     | Core business capability                           |
| 3     | Useful but non‑critical                            |
| 2     | Marginal business value                            |
| 1     | Obsolete / no clear owner                          |

***

### B. Technical Health (20%)

| Score | Definition                                |
| ----- | ----------------------------------------- |
| 5     | Modern stack, highly maintainable, secure |
| 4     | Minor tech debt                           |
| 3     | Usable but aging                          |
| 2     | Significant tech debt                     |
| 1     | End‑of‑life / unsupported                 |

***

### C. Cost Efficiency (15%)

| Score | Definition                   |
| ----- | ---------------------------- |
| 5     | Very low TCO                 |
| 4     | Reasonable TCO               |
| 3     | Moderate / acceptable        |
| 2     | High cost                    |
| 1     | Excessive / unjustified cost |

***

### D. Usage & Redundancy (15%)

| Score | Definition                 |
| ----- | -------------------------- |
| 5     | Widely used, no overlap    |
| 4     | Well used, limited overlap |
| 3     | Moderate usage             |
| 2     | Low usage or duplicated    |
| 1     | Rarely used / shelfware    |

***

### E. Strategic Fit (20%)

| Score | Definition                             |
| ----- | -------------------------------------- |
| 5     | Fully aligned with target architecture |
| 4     | Minor gaps                             |
| 3     | Neutral                                |
| 2     | Weak alignment                         |
| 1     | Conflicts with strategy                |

***

## 4. Weighted Score Formula

    Weighted Score =
    (Business Value × 0.30) +
    (Technical Health × 0.20) +
    (Cost Efficiency × 0.15) +
    (Usage & Redundancy × 0.15) +
    (Strategic Fit × 0.20)

**Maximum score = 5.0**

***

## 5. Decision Thresholds (Action Mapping)

| Total Score   | Rationalization Action        |
| ------------- | ----------------------------- |
| **≥ 4.0**     | ✅ **Retain**                  |
| **3.0 – 3.9** | 🔧 **Refactor / Re-platform** |
| **2.0 – 2.9** | 🔁 **Replace or Consolidate** |
| **< 2.0**     | 🗑️ **Retire**                |

***

## 6. Worked Example

**Service: Legacy Enrollment Validation Service**

| Dimension          | Score | Weight | Weighted |
| ------------------ | ----: | -----: | -------: |
| Business Value     |     4 |   0.30 |     1.20 |
| Technical Health   |     2 |   0.20 |     0.40 |
| Cost Efficiency    |     2 |   0.15 |     0.30 |
| Usage & Redundancy |     3 |   0.15 |     0.45 |
| Strategic Fit      |     2 |   0.20 |     0.40 |
| **Total**          |       |        | **2.75** |

**Decision:** 🔁 **Replace / Consolidate**

***

## 7. Portfolio View (What Execs Like)

| Service Name          | Score | Action   |
| --------------------- | ----: | -------- |
| Claims Core Engine    |   4.4 | Retain   |
| Enrollment Validation |  2.75 | Replace  |
| Agent Portal API      |   3.6 | Refactor |
| Legacy Reporting      |   1.8 | Retire   |

***

## 8. Advanced Enhancements (Optional but Powerful)

### ✅ Risk Multiplier

Apply a penalty multiplier for:

*   Compliance risk
*   Security exposure
*   Vendor lock‑in

Example:

    Adjusted Score = Base Score × Risk Factor (0.7 – 1.0)

***

### ✅ Cloud / 6R Mapping Overlay

| 6R Strategy | Typical Score Pattern         |
| ----------- | ----------------------------- |
| Retain      | High value + high health      |
| Rehost      | High value + neutral health   |
| Refactor    | High value + poor health      |
| Re-platform | Medium health + cost pressure |
| Replace     | Functional overlap            |
| Retire      | Low value + low usage         |

***

## 9. One‑Glance Summary

> **The goal of the scoring model is not precision—it is consistency.**  
> It enables objective, defensible decisions across hundreds of services.

***

