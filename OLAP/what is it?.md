# OLAP (Online Analytical Processing)

OLAP is a category of technology designed for fast, complex analysis of large volumes of data — typically used in business intelligence, reporting, and decision support.

## Key idea
Instead of processing one transaction at a time (like a bank deposit), OLAP systems let you slice, dice, and aggregate data across many dimensions at once — like "total sales by region, by quarter, by product category."

## Core concepts

**Multidimensional data (the "cube")**
Data is modeled as a cube with dimensions like Time, Geography, Product, and Customer, and measures like Revenue or Units Sold. You can view any combination of these.

**Common operations**
- **Slice** — filter to one value of a dimension (e.g., only 2025 data)
- **Dice** — filter across multiple dimensions at once
- **Drill down / roll up** — move from summary (yearly) to detail (monthly, daily) or vice versa
- **Pivot** — rotate the view to look at data from a different angle

## OLAP vs OLTP
| | OLTP | OLAP |
|---|---|---|
| Purpose | Run the business (transactions) | Analyze the business (insights) |
| Example | Processing an order | "Show me sales trends by region over 5 years" |
| Data | Current, detailed, frequently updated | Historical, aggregated, read-heavy |
| Query pattern | Simple, fast, many small writes | Complex, fewer but heavier reads |

## Types of OLAP
- **MOLAP** (Multidimensional) — pre-aggregated data stored in cube structures; very fast but less flexible
- **ROLAP** (Relational) — queries run directly against relational databases; more flexible, can be slower
- **HOLAP** (Hybrid) — combines both approaches

## Common tools
Microsoft SQL Server Analysis Services, Oracle OLAP, Apache Kylin, and modern cloud data warehouses like Snowflake, BigQuery, and Redshift (which support OLAP-style analytical queries even without a traditional "cube").

