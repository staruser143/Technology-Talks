# Redshift precomputing aggregates vs request time computation


With **Amazon Redshift**,  **it depends on the metric and the expected query latency**. In practice, most organizations use a **hybrid approach** rather than computing everything either ahead of time or on demand.

### Option 1: Compute at Request Time

The dashboard executes SQL queries directly against fact tables.

Example:

```sql
SELECT
    agency_id,
    SUM(written_premium) AS total_premium
FROM fact_policy
WHERE policy_date BETWEEN '2026-01-01' AND '2026-01-31'
GROUP BY agency_id;
```

**Suitable when:**

* Data volume is moderate (millions to low billions of rows).
* Users need flexible, ad hoc filtering.
* Metrics are relatively simple (SUM, COUNT, AVG, MIN, MAX).
* Freshness is more important than the absolute lowest latency.

**Advantages**

* No duplicate data.
* Business logic is defined in one place.
* Supports arbitrary filters and drill-downs.

**Disadvantages**

* Complex aggregations can take longer to execute.
* High concurrency may require more cluster resources.

---

### Option 2: Precompute Metrics

Compute metrics periodically (for example, hourly or daily) and store them in summary tables or materialized views.

Example summary table:

| Date       | Agency   | Product | Premium | Claims | Members |
| ---------- | -------- | ------- | ------- | ------ | ------- |
| 2026-08-01 | Agency A | Medical | 2.5M    | 350    | 12,000  |

The dashboard reads directly from these pre-aggregated tables.

**Suitable when:**

* Dashboards show standard KPIs.
* Large datasets make on-the-fly aggregation expensive.
* Users expect very fast response times.

**Advantages**

* Very fast dashboard performance.
* Reduced load on detailed fact tables.

**Disadvantages**

* Additional ETL/ELT complexity.
* Data freshness depends on the refresh schedule.

---

### Option 3: Materialized Views (Often the Best Compromise)

Redshift supports **materialized views**, which store the results of a query and can be refreshed incrementally or on a schedule.

Example:

```sql
CREATE MATERIALIZED VIEW mv_agency_premium AS
SELECT
    agency_id,
    month,
    SUM(written_premium) AS premium
FROM fact_policy
GROUP BY agency_id, month;
```

Dashboards query the materialized view instead of the base table.

This provides:

* Faster queries than recomputing everything.
* Simpler maintenance than manually managing summary tables.

---

## Recommendation for a Broker Dashboard

Not every metric should be precomputed.

**Compute on demand:**

* Broker-specific searches
* Drill-down from agency → broker → policy
* User-selected filters (product, state, carrier, employer)
* Exploratory analytics

**Precompute or use materialized views:**

* Total Written Premium
* Total Earned Premium
* Claims by Month
* Commission by Agency
* Member Counts
* Loss Ratio
* Quote-to-Bind Rate
* Renewal Rate

---

## Example Architecture

```text
                   Operational Systems
                           │
                     ETL / ELT Pipeline
                           │
              ┌────────────┴────────────┐
              │                         │
        Fact Tables              Materialized Views
              │                         │
              └────────────┬────────────┘
                           │
                   Power BI / Tableau
```

The dashboard can decide which source to query:

* KPI cards and high-level summaries use **materialized views** for speed.
* Drill-downs use **fact tables** to provide detailed records.

### For your healthcare insurance broker scenario

Given the need to aggregate metrics across **broker hierarchies (Agency → Large Agency → Regional Agency → National Agency)** and support drill-downs, I would recommend:

* Keep **atomic policy, claim, commission, and membership facts** in Redshift.
* Create **materialized views or summary tables** for commonly used roll-ups (for example, daily/monthly metrics by broker hierarchy, product, geography, and carrier).
* Use **on-demand SQL** only for drill-downs and less frequently used analytical queries.

This approach balances performance, flexibility, and maintainability while avoiding the overhead of precomputing every possible metric.
