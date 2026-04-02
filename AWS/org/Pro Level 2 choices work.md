Excellent — this is a **very realistic SAP‑C02 trap pattern**.  
Below is a **Pro‑level multi‑answer variant** where **two choices technically work**, but **only one is the *best* answer**.

***

# ✅ Pro‑Level Multi‑Answer Exam Variant

**(Two answers work — choose the MOST appropriate)**

***

## 📘 Exam Question (Pro‑Style Wording)

A company uses **AWS Organizations** with multiple AWS accounts. Each account hosts **VPCs, EC2 instances, and containers**.

The company’s **compliance team** has deployed a **security tool on EC2 instances in every VPC**. These resources are tagged with:

    Key: costCenter
    Value: compliance

The company wants to **identify the cost of the security tools** so it can **charge the compliance team’s AWS account**.  
The solution **must be as accurate as possible** and work **across all accounts**.

**Which TWO solutions meet the requirements?**  
(**Select TWO**)

***

## ✅ Pro‑Level Answer Classification Table

| Option | Works?                        | Pro‑Level Analysis                                                                                                                                                                                                                                                                                                               |
| ------ | ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **A**  | ✅ **Works (BEST)**            | **Enable `costCenter` as a cost allocation tag and use AWS Cost & Usage Reports (CUR) with hourly granularity and resource IDs. Query the data with Athena.** <br><br>✅ **Highest accuracy available** <br>✅ Handles RI/SP amortization, partial hours, EBS, data transfer <br>✅ Finance‑grade chargeback <br>➡️ **BEST answer** |
| **B**  | ✅ **Works (but not optimal)** | **Use AWS Cost Explorer filtered by the `costCenter=compliance` tag across the organization.** <br><br>✅ Can attribute costs by tag <br>✅ Cross‑account visibility <br>⚠️ Aggregated data <br>⚠️ Less suitable for strict chargeback than CUR <br>➡️ **Works, but not “as accurate as possible”**                                |
| **C**  | ❌                             | **Use AWS Budgets with tag-based budgets for the compliance team.** <br><br>❌ Budgets are for alerts/forecasting, not actual cost allocation                                                                                                                                                                                     |
| **D**  | ❌                             | **Use CloudWatch metrics to calculate EC2 usage and multiply by pricing.** <br><br>❌ Usage ≠ billing <br>❌ Breaks with RI/SP, Spot, EBS, data transfer                                                                                                                                                                           |
| **E**  | ❌                             | **Use AWS Config to list tagged EC2 instances and calculate costs manually.** <br><br>❌ Inventory only <br>❌ No awareness of billing constructs                                                                                                                                                                                  |
| **F**  | ❌                             | **Rely on consolidated billing totals and divide costs across accounts.** <br><br>❌ Account‑level only <br>❌ No tag/resource granularity                                                                                                                                                                                         |

***

## ✅ Correct Answers (Exam)

✅ **A**  
✅ **B**

***

## 🧠 Why this is a *Pro‑Level* Trap

### Why **B** is allowed as a correct answer

*   Cost Explorer **does** support:
    *   Organization-wide view
    *   Tag-based filtering
    *   Actual cost data (derived from CUR)
*   So technically, it **meets the functional requirement**

### Why **A** is still better

*   The phrase **“as accurate as possible”** elevates CUR
*   SAP‑C02 favors:
    *   **Raw billing data**
    *   **Athena / S3–based analysis**
    *   **Chargeback & FinOps readiness**

👉 **In single-answer questions** → only **A**  
👉 **In multi-answer questions** → **A + B**

***

## 🔍 Pro‑Level Elimination Shortcut

If the question says:

| Phrase                        | What AWS Exam Wants                     |
| ----------------------------- | --------------------------------------- |
| “Identify costs”              | Cost Explorer *might* work              |
| “Chargeback / showback”       | CUR preferred                           |
| **“As accurate as possible”** | ✅ **CUR is mandatory**                  |
| “Select TWO”                  | One CUR + one “works but weaker” option |

***

## ✅ Exam Tip (Very High Yield)

> **Whenever CUR appears in an answer choice, and the question emphasizes accuracy, granularity, or chargeback — CUR is always one of the correct answers.**

***

