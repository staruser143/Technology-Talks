# Query
Like enrollment system there could be other operational data store systems and it seems we might need to ETL the data from those stores too, to the  datawarehouse. But, how do we design the data model of datawarehouse which would now be fed with similar data from different operational data systems. Would it be different from modeling for traditional operational data stores like aws rds or DocumentDB?

# Response
That's an excellent next step in data architecture, as integrating data from multiple operational systems is a core reason why data warehouses exist.

Yes, the data model for your data warehouse (like Redshift) will be **fundamentally different** from the normalized models used in your operational stores (like AWS RDS or DocumentDB). This difference is driven by the switch from optimizing for **writes and consistency (OLTP)** to optimizing for **complex reads and aggregation (OLAP)**.

The standard approach for data warehouse modeling is the **Dimensional Model**, most commonly the **Star Schema**.

---

## 1. The Core Difference in Modeling Philosophy

| Feature | Operational DB Model (RDS/DocumentDB) | Data Warehouse Model (Redshift - Star Schema) |
| :--- | :--- | :--- |
| **Purpose** | **OLTP:** Supports fast *writes* and ensures *data integrity* (ACID). | **OLAP:** Supports fast *reads* and *aggregations* for reporting. |
| **Normalization** | **Highly Normalized:** Data is split into many small tables to eliminate redundancy (e.g., store customer name once). | **Denormalized:** Data is deliberately duplicated and flattened to minimize joins during queries. |
| **Schema Type** | **Entity-Relationship (ER) Model:** Focuses on entities and relationships. | **Dimensional Model (Star Schema):** Focuses on **Facts** (metrics) and **Dimensions** (context). |

---

## 2. Designing for Multiple Operational Systems (The Star Schema)

The Star Schema provides the framework for integrating data from different operational sources that hold similar entities (e.g., two different enrollment systems or an enrollment system and a CRM).

### A. The Central Fact Table (The Metrics)

The Fact Table is the central table where you store your **quantitative metrics** (or facts). In a CQRS/Event Sourcing system, this table is often built from your **Event Stream** (the "what happened" data).

* **Content:** Numerical, additive data points you want to analyze.
* **Example for Enrollment:**
    * `Premium_Amount`
    * `Enrollment_Count` (always 1 per enrollment record)
    * `Processing_Time_Days`
* **Keys:** It contains only foreign keys that link out to the dimension tables.

### B. The Dimension Tables (The Context)

Dimension Tables provide the descriptive context (the "who, what, where, when, and how") for the metrics in the fact table. This is where you solve the problem of multiple source systems holding similar data.

* **Content:** Descriptive attributes used to filter, group, or slice your facts.
* **Example Dimension Tables:**
    * **Time Dimension:** Dates, years, months, weekdays (crucial for time-series analysis).
    * **Customer Dimension:** All attributes of the customer (Name, Address, Age, etc.).
    * **Plan Dimension:** All attributes of the policy plan (Plan Name, Metal Tier, Deductible, etc.).

### C. The Integration Step: Conformed Dimensions

This is the critical step for merging data from multiple sources: **Creating Conformed Dimensions.**

Imagine you have two source systems:

1.  **System A (Enrollment):** Uses `Plan_Code_A` for its product catalog.
2.  **System B (Billing):** Uses `Product_ID_B` for its billing catalog.

If you want to analyze `Total Revenue` (from System B) by `Plan Type` (from System A), you cannot join them directly.

**Solution:**

You build a single **Conformed Plan Dimension** table in your data warehouse.

* This table is created by the ETL process, which maps and consolidates the keys from both source systems.
* It contains: `DW_Plan_Key` (the new master key), `Plan_Code_A`, `Product_ID_B`, `Plan_Name`, and `Metal_Tier`.

Now, both the **Enrollment Fact Table** and the **Billing Fact Table** in your data warehouse link to the exact same **Conformed Plan Dimension** using the `DW_Plan_Key`. This allows analysts to query across both data domains seamlessly.

---

## 3. Usage of the Domain Data Store Collection

To be clear, the primary usage of the operational **Domain Data Store Collection** (the Read Model in CQRS) remains:

1.  **Serving the Application UI:** Fast display of a single customer's current policy details.
2.  **Operational Lookups:** Simple, high-speed checks used by the live application (e.g., quickly checking policy status for an API call).
3.  **Real-Time, Simple Analytics:** Operational dashboards showing *today's* metrics (e.g., "new sign-ups in the last hour").

The **Data Warehouse** takes over when you need to answer a question that requires **historical data, complex joins, and cross-system context**.