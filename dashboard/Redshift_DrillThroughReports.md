# Redshift is better for drill through reports and historical analyses


This is an important distinction because **drill-down** and **drill-through** are often confused, and they have different implications for the underlying data platform.

## Drill-down vs. Drill-through

**Drill-down** means moving through levels of aggregation.

Example:

```text
National Agency
      ↓
Regional Agency
      ↓
Large Agency
      ↓
Agency
      ↓
Broker
```

Each level still shows **aggregated metrics**.

---

**Drill-through** means navigating from an aggregated metric to the underlying transaction records.

Example:

```text
Agency Premium = $15.2M
          ↓
Show all 18,745 policies
          ↓
Select one policy
          ↓
Show policy details
          ↓
Show member details
          ↓
Show claims
```

This is where Redshift has a significant advantage.

---

# Example 1 – Broker Performance Dashboard

Suppose your dashboard shows:

| Agency     | Premium | Members |
| ---------- | ------- | ------- |
| ABC Agency | $45M    | 12,000  |

The executive clicks on **ABC Agency**.

### In Apache Pinot

Pinot efficiently returns another aggregation:

| Broker | Premium |
| ------ | ------- |
| John   | $8M     |
| Lisa   | $6M     |
| David  | $5M     |

This is another `GROUP BY` query, which Pinot excels at.

Now the executive wants:

> Show me every policy written by John.

Suppose John has 52,000 policies.

Returning tens of thousands of detailed records is not what Pinot is optimized for.

---

### In Redshift

The query is straightforward:

```sql
SELECT *
FROM fact_policy
WHERE broker_id='BROKER123'
ORDER BY effective_date DESC;
```

Redshift is designed to scan and return large detailed datasets efficiently.

---

# Example 2 – Claims Investigation

A dashboard shows:

```
Texas

Claims = $142 Million
```

The CFO asks:

> Why is Texas so high?

They drill through.

First:

```
Claims by County
```

Then:

```
Dallas County
```

Then:

```
Provider
```

Then:

```
Hospital
```

Finally:

```
Show every claim over $100,000
```

Result:

| Claim ID | Member | Provider | Amount |
| -------- | ------ | -------- | ------ |
| ...      | ...    | ...      | ...    |

This kind of investigation involves detailed records and multiple dimensions, which fits Redshift well.

---

# Example 3 – Historical Analysis

Suppose leadership asks:

> Compare broker performance over the last five years.

They want:

```
2022
2023
2024
2025
2026
```

For each year:

* Premium
* Members
* Claims
* Commission
* Loss Ratio
* Retention

Then they ask:

> Show only brokers selling Medicare products.

Then:

> Compare COVID years versus post-COVID years.

These queries scan large historical datasets and join multiple dimensions, which is a core strength of Redshift.

---

# Example 4 – Trend Analysis

Suppose you want to understand:

```
Monthly Premium

Jan
Feb
Mar
...
```

Now compare against:

* Previous Year
* Previous Quarter
* Same Month Last Year

Then calculate:

* Growth %
* Moving Average
* Running Total
* Year-to-Date Premium

Example SQL:

```sql
SELECT
    month,
    SUM(premium),
    LAG(SUM(premium)) OVER (...)
FROM fact_policy;
```

Redshift's support for SQL window functions makes these analyses straightforward.

---

# Example 5 – Cross-Domain Analysis

Suppose executives ask:

```
Do brokers with higher commissions
also have lower loss ratios?
```

You need to combine:

* Policy
* Claims
* Commission
* Member
* Product
* Employer

This involves multiple fact and dimension tables.

Example:

```
Policy
      \
       \
Claims ---- Broker ---- Commission
       /
      /
Employer
```

Complex joins across these datasets are a natural fit for a data warehouse.

---

# Example 6 – Regulatory Reporting

Healthcare insurers frequently need reports such as:

```
Claims
by
Product
by
State
by
Quarter
by
Age Band
```

These reports often involve:

* Hundreds of columns
* Many joins
* Business rules
* Auditability
* Large result sets

This is much better suited to Redshift than a real-time OLAP store.

---

# Example 7 – Ad Hoc Business Questions

Suppose the CEO asks:

> Which agencies improved retention by more than 15% while reducing commissions over the last three years?

The SQL might:

* Join six tables
* Use Common Table Expressions (CTEs)
* Apply window functions
* Aggregate by year
* Calculate growth rates
* Rank agencies

Redshift is designed for this style of analytical SQL.

---

# Why Pinot Is Less Suited for These Workloads

Pinot's design optimizes for:

* Fast filtering
* Fast aggregation
* Top-N queries
* Time-series analytics
* Dashboard responsiveness

It intentionally limits the complexity of joins and large transactional result sets to maintain low latency.

---

# Why Redshift Excels

Redshift provides:

* Efficient execution of complex joins across many tables.
* Advanced SQL features such as window functions, recursive queries, and CTEs.
* Columnar storage and massively parallel processing for scanning large historical datasets.
* Integration with BI tools for drill-through actions that return detailed records.
* Support for materialized views to accelerate frequently used summaries while still allowing access to underlying facts.

---

## Applying This to the Broker Dashboard

For the healthcare insurance broker domain , a common pattern is:

1. **Executive dashboard**

   * Total Premium
   * Total Claims
   * Loss Ratio
   * Commission
   * Member Count

2. **Drill-down**

   * National Agency → Regional Agency → Agency → Broker

3. **Drill-through**

   * Broker → Policies
   * Policy → Members
   * Member → Claims
   * Claim → Payment details

4. **Historical analysis**

   * Compare the last 5 years
   * Analyze seasonal trends
   * Evaluate broker performance before and after compensation plan changes
   * Measure retention and loss ratio trends by product and geography

- This is why many organizations use **Redshift as the analytical system of record**.
- If we also require highly responsive, near real-time dashboards with rapid filter changes and slice-and-dice exploration, we can add **Apache Pinot** as a specialized -  serving layer rather than replacing the warehouse. T
ogether, the two platforms complement each other by addressing different classes of analytical queries.
