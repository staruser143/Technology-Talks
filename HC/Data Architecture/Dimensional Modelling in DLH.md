# Query 

Do we still have to follow dimensional modelling for analytical data which is now in s3/ADLS Gen2?

# Response
Yes, you absolutely **still have to follow dimensional modeling** for your analytical data, even though it's stored in S3/ADLS Gen2 within a Datalakehouse architecture.

The physical location of the data (S3/ADLS Gen2) and the underlying storage format (Parquet/Delta Lake/Iceberg) are **technical details** that optimize cost and scalability. Dimensional modeling (Star Schema) is a **logical design layer** that optimizes data for **human understanding and query performance** in analytics.

Here's why dimensional modeling is essential for the consumption layer (the **Gold layer** of your Medallion Architecture):

-----

## 1\. Dimensional Modeling Optimizes for OLAP

Data Warehouses and Datalakehouses are designed for **Online Analytical Processing (OLAP)**, which involves heavy querying, filtering, and aggregation across massive historical datasets.

  * **Simplicity for Business Users:** A Star Schema, with its central **Fact** table and radiating **Dimension** tables, is intuitive. Business users can easily understand how to link `Sales` (Fact) to `Customer` (Dimension), `Time` (Dimension), and `Product` (Dimension).
  * **Query Performance:** The structure minimizes the number of joins required for common BI queries (e.g., "Sales by Region over Time"). When using columnar storage like Parquet on S3, this denormalized structure maximizes the efficiency of the query engine (like Spark, Athena, or Synapse SQL) by reading only the necessary columns.

-----

## 2\. It Provides a Conformed, Single Source of Truth

As established earlier, you're pulling data from multiple operational systems. Dimensional modeling is the discipline that ensures the data is unified and consistent.

  * **Conformed Dimensions:** This is where you create master lists for entities (e.g., a single `Customer` table or `Product` table) that link back to multiple **Fact** tables (e.g., `Enrollment Fact` and `Billing Fact`). This is what guarantees that "Customer ID 123" means the same person across all reports, regardless of which source system it originated from.
  * **Business Context:** It explicitly organizes data around **business processes** and their **measurements**, creating a common language for the organization.

-----

## 3\. Dimensional Modeling in the Datalakehouse (Medallion Architecture)

In the Datalakehouse model, dimensional modeling applies specifically to the highest quality tier:

| Layer | Modeling Strategy | Purpose |
| :--- | :--- | :--- |
| **Bronze** (Raw) | None (Raw Ingestion) | Immutable, perfect copy of the source data. |
| **Silver** (Clean) | **Data Vault or 3rd Normal Form (Optional)** | Integrates and cleanses data without losing history or granularity. This is the **Enterprise Data Warehouse** core. |
| **Gold** (Consumption) | **Dimensional Model (Star Schema)** | Highly denormalized, aggregated, and modeled specifically for BI tools, reporting, and dashboard consumption. |

### The Modern Twist

While the **principles** of dimensional modeling (Facts and Dimensions) remain critical, the **implementation** adapts to cloud storage:

1.  **Denormalization:** You are more likely to slightly denormalize the dimensions (often using a Star Schema instead of a highly normalized Snowflake Schema) to reduce the number of joins, which improves performance on distributed cloud compute engines.
2.  **Schema Enforcement:** Tools like Delta Lake and Apache Iceberg, which act as the metadata layer, help enforce the schemas for your dimensional tables in the Silver and Gold layers, providing the reliability traditionally found only in a relational database.

In short, you are using the flexibility and cost-effectiveness of S3/ADLS Gen2, but you still need the structure and analytical power provided by the **dimensional model** for the final consumers of the data.

The video below discusses the differences between dimensional modeling and other approaches like Data Vault in the context of modern data stacks.

[Data Vault vs Dimensional Modeling](https://www.google.com/search?q=https://www.youtube.com/watch%3Fv%3DJ9y58d4T_3I) compares dimensional modeling to another advanced technique often used in the Silver layer of a Datalakehouse.