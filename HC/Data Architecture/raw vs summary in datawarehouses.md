# query
When we use ETL jobs to move from RDS/Cloud storage to Redshift do we store the complete raw data there or only the summaries/aggregated data like average, median and so on, so that we have instantaneous access to them to derive business insights and power business dashboards.

# response
You generally **do not store *only* summaries or aggregates** in the data warehouse (Redshift). The goal of the ETL (or more commonly **ELT**) process is to load the **detailed, historical, and cleaned data** into Redshift.

The summaries and aggregates are usually **derived from this detailed data** either during the final stage of the ETL process or, more commonly, when a dashboard/BI tool queries Redshift.

Here's a breakdown of what and why:

***

## Data Stored in Redshift (The Data Warehouse)

### 1. Detailed, Cleaned Data (The Core)
The primary function of Redshift is to store the lowest practical level of detail for analysis, often referred to as the **"Fact"** and **"Dimension"** tables in a dimensional model (Star Schema).

* **What is stored:** The individual enrollment records, one row per application or one row per policy member, but with the data **cleaned, standardized, and integrated** from its raw form.
    * *Example:* Instead of the raw PDF or messy JSON, you store fields like `Policy_ID`, `Member_Name`, `Premium_Amount`, `Enrollment_Date`, and `Broker_ID`. This is the level needed to answer specific, detailed questions.

### 2. Derived and Aggregated Tables (Performance Boosters)
To ensure instantaneous dashboard performance, you often create **pre-aggregated tables** within Redshift itself.

* **What is stored:** Summary tables that materialize frequently calculated metrics.
    * *Example:* A table called `Monthly_Enrollment_Summary` that stores the total number of enrollments, the average premium, and the median age **per month, per state, per plan**.
* **Why it's necessary:** Dashboards often ask the same aggregate questions repeatedly. Querying a pre-calculated table with 1,000 rows (monthly summaries) is near-instantaneous, compared to calculating the same metric from a detailed table with 1 billion rows.

***

## Why Raw Data Detail is Essential

* **Drill-Down Capability:** Business users need to see the summary (e.g., "Enrollment is down 5% this week") and then **drill down** to see the individual detailed policies causing the drop. If only the aggregate is stored, the drill-down stops.
* **Ad-Hoc Queries:** Analysts often ask questions that weren't anticipated when the summaries were created. Only the detailed data can support **unpredictable ad-hoc analysis** (e.g., finding the median premium for female applicants over 50 who enrolled via a specific marketing campaign).
* **Historical Accuracy:** The data warehouse is the **immutable, long-term historical record**. Storing only summaries loses the context needed for future regulatory reporting or re-analysis based on new business definitions.

**Conclusion:**

The process is generally **ETL/ELT $\rightarrow$ Detailed Data in Redshift $\rightarrow$ Queries/BI Tools $\rightarrow$ Summaries/Aggregates on the fly**. For frequently accessed dashboards, an extra step is added to create **pre-calculated Aggregate Tables** within Redshift to ensure speed. 