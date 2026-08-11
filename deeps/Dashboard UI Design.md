# Dashboard UI Design

This document describes a recommended dashboard layout and API pattern for on-demand aggregation from MongoDB, with drill-down across Agency → Sub-agency → Broker → Producer.

## Layout overview

Top-level zones on the dashboard:

- Filter panel (left or top)
- KPI summary cards (top)
- Drill-down hierarchy (left column)
- Visual analytics (right column)
- Detail grid (bottom)

### Visual layout (conceptual)

```
+------------------------------------------------------+ 
| FILTER PANEL                                        |
|------------------------------------------------------|
| Date Range | State | Agency | Broker | Product | ... |
+------------------------------------------------------+ 

+------------------------------------------------------+ 
| KPI SUMMARY CARDS                                    |
|------------------------------------------------------|
| Applications | Renewals | Premium | Commission | ... |
+------------------------------------------------------+ 

+----------------------+-------------------------------+
| DRILL-DOWN HIERARCHY | VISUAL ANALYTICS              |
|                      |                               |
| Agency               | Trend Charts                  |
|  -> Sub Agency       | Commission Charts             |
|    -> Broker         | Renewal Charts                |
|      -> Producer     |                               |
+----------------------+-------------------------------+

+------------------------------------------------------+ 
| DETAIL GRID                                           |
|------------------------------------------------------|
| Search | Export | Filter                              |
| Agency | Applications | Premium | Commission | ...    |
+------------------------------------------------------+ 
```

> Note: The ASCII layout above is a conceptual guide — implement using responsive UI components so the hierarchy panel and analytics rearrange on smaller screens.

---

## 1. User Input / Filter layer

This layer captures user selections that drive aggregations.

Common global filters:

- Time: Today, Yesterday, Last 7 Days, Last 30 Days, Month To Date, Year To Date, Custom Range
- Organization / Region / State
- Agency / Sub Agency / Broker / Producer
- Business / Product / Carrier / Market Segment
- New Business / Renewal
- Search (free text)

Free text search fields (examples):

- Agency Name
- Broker Name
- Producer Name
- Application Number

All filter selections should be part of a single filter context object that the frontend sends to the metrics API.

---

## 2. KPI Cards

Show top-level KPIs grouped as cards at the top of the page. They should update immediately after filter selection.

Example KPIs:

- Applications: 25,432
- Renewals: 9,842
- Premium: $125.4M
- Commission: $11.2M
- Conversion Rate: 68%

Example query context:

```json
{
  "state": "NJ",
  "agencyId": "A123",
  "product": "Senior"
}
```

Backend executes a MongoDB aggregation pipeline for the provided filter context and returns metric values.

---

## 3. Hierarchy navigation panel

A critical control for broker organizations to avoid showing thousands of rows immediately. Use a tree view with the following levels:

- Agency
  - Sub Agency
    - Broker
      - Producer

Clicking a node should push hierarchy filters into the global filter context and refresh all metrics/charts.

Example flows:

- Select Agency ABC → show aggregated KPIs for that agency
- Drill into Broker John → show KPIs filtered by broker

Each node click should trigger a new aggregation with the updated filter context.

---

## 4. Visual analytics section

Charts to include (examples):

- Applications trend (group by month)
- Commission by Agency (bar chart, grouped by agency, sum commission)
- New vs Renewal (pie/donut)
- Product mix (stacked bar or pie)

These charts are driven by the same filter context and should update on filter/node selection.

---

## 5. Detail grid

This table is where users spend a lot of time. Provide these capabilities:

- Sort
- Search (within table)
- Export CSV
- Pagination
- Row-level drill-down (click an agency row to open brokers, then producers)

Example columns:

- Agency | Applications | Premium | Commission | ...

Example rows:

- ABC | 100 | $500k | $40k
- XYZ | 200 | $900k | $80k

---

## 6. Example user journey

Regional manager selects:

- State = NJ
- Month = July

Dashboard shows:

- Applications = 5,200
- Premium = $12M
- Commission = $1.4M

Clicks Agency ABC, filter context becomes:

```json
{
  "state": "NJ",
  "agency": "ABC"
}
```

Clicks Broker John, filter context becomes:

```json
{
  "state": "NJ",
  "agency": "ABC",
  "broker": "John"
}
```

All metrics and charts refresh dynamically with each selection.

---

## 7. API design pattern

Frontend never asks for precomputed metrics. Instead it sends the filter context and the backend generates aggregation pipelines on demand.

Example request:

```http
POST /metrics/query
Content-Type: application/json

{
  "dateFrom": "2026-08-01",
  "dateTo": "2026-08-31",
  "state": "NJ",
  "agency": "ABC",
  "broker": "John"
}
```

Example response shape:

```json
{
  "kpis": { /* numeric KPI values */ },
  "charts": { /* series / chart data */ },
  "table": [ /* rows for the detail grid */ ]
}
```

---

## 8. Recommended architecture

- React dashboard (frontend)
- Metrics API (backend) — single endpoint that accepts filter context and returns KPIs, chart series, and table data
- MongoDB aggregation framework performing on-demand aggregations against an Applications collection (denormalized with hierarchy attributes)

Example denormalized document structure (recommended):

```json
{
  "applicationId": "A123",
  "state": "NJ",
  "agencyId": "AG01",
  "agencyName": "ABC",
  "subAgencyId": "SUB01",
  "brokerId": "BR01",
  "producerId": "PR01",
  "marketCategory": "Senior",
  "premium": 1200,
  "commission": 150
}
```

Denormalizing hierarchy attributes into each application document allows a single aggregation pipeline to power KPI cards, drill-downs, charts and the grid without joins or cache refresh concerns.

---

## Implementation notes

- Because data changes frequently and you prefer on-demand aggregations, avoid long-lived caches for core KPIs unless you identify a clear performance bottleneck.
- Index fields commonly used for filtering (dates, state, agencyId, brokerId, producerId) to help aggregation performance.
- Consider pagination and server-side sorting for the detail grid to avoid loading very large result sets into the browser.

---

For readability I reformatted the document into clear sections, aligned the ASCII layout, and converted inline JSON and API examples into fenced code blocks. If you'd like, I can also:

- Convert the ASCII layout into a responsive UI mock (HTML/CSS/React prototype)
- Add example MongoDB aggregation pipeline snippets for the KPIs and charts
- Add a sample TypeScript client for calling the /metrics/query endpoint

Tell me which of the above you'd like next.