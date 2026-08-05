Yes — star schemas (and their cousin, snowflake schemas) are exactly what you'd typically build inside Redshift, Snowflake, BigQuery, and similar cloud data warehouses. Here's how it plays out in practice.

## Star schema in a modern cloud warehouse

The logical model doesn't change — you still have fact tables and dimension tables — but the physical implementation differs from old-school on-prem OLAP:

**No separate "cube" layer needed**
Traditional OLAP (like SSAS) pre-built physical cubes that stored aggregations. Redshift/Snowflake/BigQuery instead run the aggregation at query time against the star schema tables directly, because they're columnar and built for exactly this kind of scan-and-aggregate workload. So the "cube" becomes a *virtual* concept — often exposed via a BI tool's semantic layer (Looker's LookML, dbt's semantic layer, Power BI's model) rather than a physically materialized structure.

**Columnar storage is what makes this fast**
- Each column (e.g., `commission_amount`) is stored contiguously, so a query like "sum commission by broker" only reads that one column, not entire rows
- This is a big part of why these warehouses can do OLAP-style aggregation over billions of rows without a pre-built cube

**Snowflake and Redshift both explicitly recommend star schemas**
- **Snowflake**: commonly paired with star schema + clustering keys on large fact tables (like your `Fact_Commission` table) to prune which micro-partitions get scanned
- **Redshift**: uses distribution keys and sort keys on fact tables — often distributing by a key like `broker_id` or `carrier_id` so joins to those dimension tables happen without shuffling data across nodes

## Star vs. snowflake schema — which do people actually use?
- **Star schema** (dimensions flattened, no sub-joins) — preferred in these warehouses, because fewer joins = better performance and simpler SQL for BI tools
- **Snowflake schema** (dimensions normalized further, e.g., splitting `Dim_Broker` into `Dim_Broker` + `Dim_Agency`) — sometimes still used for very large or slowly-changing dimensions, but usually avoided in Snowflake/Redshift specifically because extra joins hurt performance and the storage savings from normalization barely matter (storage is cheap in cloud warehouses)

## What's different from 2000s-era OLAP
| Old OLAP (SSAS, Oracle OLAP) | Modern cloud DW (Snowflake, Redshift, BigQuery) |
|---|---|
| Physical MOLAP cubes, pre-aggregated | Virtual/semantic layer over star schema tables |
| Slow cube processing/rebuild jobs | Query-time aggregation, near-instant on new data |
| Cube storage separate from source data | Star schema tables *are* the source of truth |
| Row-based or proprietary storage | Columnar storage (the real enabler) |

## One more common pattern worth knowing
Many teams now build the star schema using **dbt** on top of Snowflake/Redshift/BigQuery — dbt models transform raw data into fact and dimension tables, essentially codifying the star schema as version-controlled SQL rather than a GUI-built cube. This has become the standard way to implement what we sketched for your broker use case.
