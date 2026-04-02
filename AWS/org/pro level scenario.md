Perfect — here’s a **SAP‑C02 / Pro‑level version** with **subtle traps**, **“Correct vs Works vs Distractor”** positioning, and **exam elimination logic** baked in.

***

# ✅ Pro‑Level “Correct vs Works vs Distractor” Exam Table

**Topic: Accurate cross‑account chargeback for EC2‑based compliance tools**

***

## ✅ Scenario Reminder (Condensed, Exam Style)

*   Multiple AWS accounts under **AWS Organizations**
*   EC2-based security/compliance tools run **inside each VPC**
*   All compliance resources tagged:
        costCenter = compliance
*   **Goal**: Accurately calculate EC2 costs of compliance tools and **charge back** to a central compliance account
*   Keyword: **“as accurate as possible”**

***

## ✅ Pro‑Level Decision Table

| Option | Classification        | Why It Looks Plausible                                                                | Pro‑Level Verdict (Why / Why Not)                                                                                                                                                                                                                                               |
| ------ | --------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **A**  | ✅ **Correct**         | Activate cost allocation tags + CUR + Athena                                          | ✅ **Best Answer**. CUR with **hourly line items**, **resource IDs**, **RI/SP amortization**, and **cost allocation tags** is the **maximum-fidelity billing dataset** AWS provides. Only option that survives RI/Savings Plans, partial hours, EBS, and shared pricing effects. |
| **B**  | ⚠️ Works, Not Optimal | Cost Explorer filtered by `costCenter=compliance`                                     | ⚠️ **Works but not “as accurate as possible.”** Cost Explorer is derived from CUR but **aggregated**, lacks raw line items, and is not ideal for finance-grade chargeback. Pro exam expects CUR when accuracy is emphasized.                                                    |
| **C**  | ❌ Distractor          | AWS Budgets by tag and account                                                        | ❌ **Forecasting ≠ Billing.** Budgets are preventative controls and alerts, not retrospective cost attribution. Cannot be used as a chargeback source.                                                                                                                           |
| **D**  | ❌ Distractor          | CloudWatch usage metrics mapped to EC2 pricing                                        | ❌ **Classic trap.** CloudWatch ≠ billing. Utilization data does not capture EBS, data transfer, SP/RI amortization, tiered pricing, or partial usage hours.                                                                                                                     |
| **E**  | ❌ Distractor          | AWS Config inventory of tagged EC2 instances + pricing math                           | ❌ **Inventory ≠ cost.** Config can tell *what exists*, not *what was billed*. Breaks immediately with Savings Plans, Reserved Instances, and Spot pricing.                                                                                                                      |
| **F**  | ❌ Distractor          | Consolidated billing + split account-level totals                                     | ❌ **Account-level only.** Cannot isolate only the compliance EC2 instances. Fails the tag‑level granularity requirement.                                                                                                                                                        |
| **G**  | ❌ Distractor          | Centralize compliance tooling into compliance account using VPC sharing / PrivateLink | ❌ **Architectural redesign**, not a cost attribution solution. Also violates stated constraint: tools already run on EC2 **inside each VPC**.                                                                                                                                   |
| **H**  | ❌ Distractor          | SCPs to enforce tagging + monthly invoice analysis                                    | ❌ **Governance ≠ allocation.** SCPs help ensure tags exist but **do not produce cost attribution data**. You still need CUR.                                                                                                                                                    |

***

## 🔍 Pro‑Level Exam Elimination Logic

### Step 1: Spot the **billing fidelity keyword**

> “**as accurate as possible**”

✅ This **immediately elevates CUR above Cost Explorer**

***

### Step 2: Spot the **tag-based chargeback requirement**

*   Resources already tagged
*   Chargeback across **multiple accounts**

✅ Eliminates:

*   Consolidated billing (account-level only)
*   CloudWatch (not billing)
*   Config (inventory only)

***

### Step 3: Consider **RI / Savings Plans trap**

If RI/SP exist:

*   EC2 costs are **amortized**
*   Multiple instances share discounts

✅ **Only CUR captures amortized, blended, and unblended cost dimensions correctly**

***

## 🧠 Pro‑Level Exam “Tell” Pattern

When you see **all three together**:

1.  AWS Organizations
2.  Cost allocation via **tags**
3.  “As accurate as possible” or “finance-grade chargeback”

👉 **The answer is always CUR**, not Cost Explorer.

***

## ✅ Exam‑Ready Final Answer

> **Enable the `costCenter` tag as a cost allocation tag at the AWS Organizations level and generate an AWS Cost and Usage Report (CUR) with hourly granularity and resource IDs. Query the CUR data (for example, with Amazon Athena) to aggregate EC2 costs where `costCenter = compliance`, and use this data to accurately charge back the compliance tool costs to the compliance team’s AWS account.**

***


