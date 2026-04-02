**RTO** and **RPO** are core **disaster recovery (DR)** and **business continuity** metrics. They define *how quickly* systems must be restored and *how much data loss* is acceptable after a failure.

***

## 1. RTO — *Recovery Time Objective*

**RTO answers:**  
👉 **“How long can this system be down?”**

**Definition:**  
The **maximum acceptable time** to restore a system or application **after an outage**.

**Example:**

*   RTO = **2 hours**
*   An outage occurs at **10:00 AM**
*   System **must be operational by 12:00 PM**

✅ Focus: **Downtime**

***

## 2. RPO — *Recovery Point Objective*

**RPO answers:**  
👉 **“How much data can we afford to lose?”**

**Definition:**  
The **maximum acceptable amount of data loss**, measured in **time**, due to an incident.

**Example:**

*   RPO = **15 minutes**
*   Last successful backup/replication must be **within 15 minutes** of failure
*   If failure at **10:00 AM**, data back to **9:45 AM** is acceptable

✅ Focus: **Data loss**

***

## 3. RTO vs RPO (Side‑by‑Side)

| Aspect           | RTO                         | RPO                            |
| ---------------- | --------------------------- | ------------------------------ |
| What it controls | Downtime                    | Data loss                      |
| Measured in      | Time to restore             | Time worth of data             |
| Key question     | *How fast must we recover?* | *How much data can we lose?*   |
| Driven by        | Restore speed               | Backup / replication frequency |
| Example values   | 10 min, 1 hr, 4 hrs         | 0 min, 5 min, 15 min           |

***

## 4. Practical Examples

### Example 1: Online Trading System

*   **RTO:** 5 minutes
*   **RPO:** 0 minutes  
    ➡️ Requires **active‑active**, synchronous replication, automated failover

### Example 2: HR Payroll System

*   **RTO:** 24 hours
*   **RPO:** 8 hours  
    ➡️ Nightly backups are sufficient

### Example 3: Analytics Platform

*   **RTO:** 4 hours
*   **RPO:** 1 hour  
    ➡️ Asynchronous replication + periodic backups

***

## 5. How RTO & RPO Affect Architecture (Exam & Real‑World)

| Requirement | Architectural Implication            |
| ----------- | ------------------------------------ |
| Low RTO     | Automated failover, warm/hot standby |
| Zero RPO    | Synchronous replication              |
| Higher RTO  | Manual recovery acceptable           |
| Higher RPO  | Periodic backups sufficient          |

***

## 6. Common Exam Traps (AWS / Azure / GCP)

*   ✅ **Multi‑AZ ≠ DR** (Improves availability, not regional DR)
*   ✅ **Backups alone = high RTO**
*   ✅ **Cross‑Region replication lowers RPO**
*   ✅ **Pilot‑light ≠ warm standby** (RTO differs)

***

## One‑Line Memory Hook (Exam Friendly)

> **RTO = Time to Recover**  
> **RPO = Data You’re Willing to Lose**


