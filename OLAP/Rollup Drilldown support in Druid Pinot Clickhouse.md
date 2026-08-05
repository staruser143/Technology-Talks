Yes — rollup and drill-down are core to how both are designed, though they implement it a bit differently from each other and from traditional OLAP cubes.

## Rollup — built into ingestion, not just query time

This is one of the more distinctive things about Druid especially: **rollup can happen at ingestion**, not only when you query.

**Druid**
- At ingest time, you configure a "granularity" (e.g., minute, hour, day) and Druid automatically pre-aggregates rows that share the same dimension values within that time bucket, summing/counting the metrics
- Example: 500 individual commission events for the same broker in one minute collapse into a single stored row with `commission_sum` and `event_count`
- Trade-off: you lose row-level detail (can't get back the individual event) in exchange for far smaller storage and faster scans
- You choose the granularity based on how much detail you're willing to sacrifice — rollup is optional and configurable per data source

**Pinot**
- Also supports rollup at ingestion, called **star-tree indexing** — it pre-aggregates combinations of dimensions into a tree structure, materializing common aggregation paths in advance
- This is conceptually close to a traditional OLAP cube's pre-aggregation, just built automatically from your data rather than manually defined

**At query time, both also do standard rollup** — a `GROUP BY broker_id, carrier_id` aggregating on the fly, same as any SQL engine. So you get rollup both ways: pre-computed (fast, less flexible) and on-the-fly (flexible, still fast because columnar).

## Drill-down — fully supported, and it's their bread and butter

Drill-down (going from a summary number down to more granular detail) is exactly the query pattern these stores are optimized for:

- "Total commission this hour" → drill into "commission by broker this hour" → drill into "commission by broker by carrier this hour"
- Because storage is columnar and heavily indexed (inverted indexes on dimension columns), adding a `GROUP BY` dimension or a filter doesn't require rescanning irrelevant data — it's designed for exactly this kind of interactive slicing
- This is what powers the "click a bar in the dashboard to see it broken down further" interaction pattern

## The catch: rollup and drill-down granularity are in tension

If you configure Druid to roll up commission events to **hourly** granularity at ingest, you **permanently lose** the ability to drill down to individual transactions or minute-level detail — that data is gone, only the aggregate remains. So the granularity decision at ingest time sets a floor on how far you can drill down later.

**Practical implication for the broker dashboard:** if brokers need to drill into "show me the individual commission line items that make up this total," you either:
1. Don't roll up (or roll up only to a fine granularity like per-minute), keeping row-level detail in Druid/Pinot, or
2. Keep row-level detail in Snowflake/Redshift (the batch path) and have the dashboard fall back to querying there when someone drills past what the real-time store retains

Most production setups use option 2 — real-time store for fast aggregate views, batch warehouse for the "give me every row" drill-through — since keeping full granularity in Druid/Pinot indefinitely defeats some of the storage/speed benefit that made you pick them in the first place.