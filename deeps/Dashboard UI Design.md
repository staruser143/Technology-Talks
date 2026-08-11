Given your preference for on-demand aggregation from MongoDB and the need for drill-down across Agency → Sub-agency → Broker → Producer, I would think of the dashboard page as having three zones:

+------------------------------------------------------+
|  FILTER PANEL                                        |
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
| Agency               | Trend Charts                 |
|  -> Sub Agency       | Commission Charts            |
|    -> Broker         | Renewal Charts               |
|      -> Producer     |                               |
+----------------------+-------------------------------+

+------------------------------------------------------+
| DETAIL GRID                                           |
|------------------------------------------------------|
| Search | Export | Filter                              |
| Agency | Applications | Premium | Commission | ...    |
+------------------------------------------------------+

1. User Input Capture Layer

This is where user selections drive aggregations.

Global Filters

Typically:

Time
Today
Yesterday
Last 7 Days
Last 30 Days
Month To Date
Year To Date
Custom Range
Organization
Region
State
Agency
Sub Agency
Broker
Producer
Business
Product
Carrier
Market Segment
New Business
Renewal
Search

Free text search:

Agency Name
Broker Name
Producer Name
Application Number

2. KPI Cards

At top of page.

Example:

Applications      25,432
Renewals           9,842
Premium         $125.4M
Commission       $11.2M
Conversion Rate   68%


These should update immediately after filter selection.

Query Example:

{
   state:"NJ",
   agencyId:"A123",
   product:"Senior"
}


Backend executes aggregation pipeline.

3. Hierarchy Navigation Panel

This is critical for broker organizations.

Instead of thousands of rows immediately.

Show:

Agency
 ├─ Sub Agency
     ├─ Broker
          ├─ Producer


Clicking a node pushes hierarchy filters.

Example:

Agency ABC

returns:

Applications: 1200
Premium: $4.5M


Click Broker X:

Applications: 230
Premium: $900K


Same metrics recalculated.

4. Visual Analytics Section
Applications Trend
Jan ████
Feb █████
Mar ███████
Apr ████████


Query:

group by month

Commission By Agency
Agency A   █████████
Agency B   ██████
Agency C   ████


Query:

group by agency
sum(commission)

New vs Renewal
           New
           Renewal


Pie/Donut Chart.

Product Mix
Senior
Commercial
Individual
Group


Useful for leadership.

5. Detail Grid

This is often where users spend most time.

Example:

Agency	Apps	Premium	CommissionABC	100	500k	40k
XYZ	200	900k	80k

Capabilities:

Sort
Search
Export CSV
Pagination
Drill Down

Clicking row:

Agency
 -> Brokers
 -> Producers

6. Example User Journey
Regional Manager

Selects:

State = NJ
Month = July


Dashboard shows:

Applications = 5,200
Premium = $12M
Commission = $1.4M


Clicks:

Agency ABC


Now query becomes:

{
  state:"NJ",
  agency:"ABC"
}


Metrics refresh.

Clicks:

Broker John


Now:

{
   state:"NJ",
   agency:"ABC",
   broker:"John"
}


Everything refreshes dynamically.

7. API Design Pattern

Frontend never asks for precomputed metrics.

Instead sends filter context.

Example:

POST /metrics/query

{
  "dateFrom":"2026-08-01",
  "dateTo":"2026-08-31",
  "state":"NJ",
  "agency":"ABC",
  "broker":"John"
}


Backend generates Mongo aggregation pipeline dynamically.

Response:

{
   "kpis":{},
   "charts":{},
   "table":[]
}

8. Architecture I Would Recommend

For your specific requirement:

React Dashboard
      |
      v
Metrics API
      |
      v
MongoDB Aggregation Framework
      |
      v
Applications Collection
(enriched with hierarchy data)


Since you've already indicated that:

Data changes frequently
You prefer on-demand aggregations
You are considering enriching hierarchy attributes into each document

I would denormalize hierarchy attributes into the application document:

{
  "applicationId":"A123",
  "state":"NJ",
  "agencyId":"AG01",
  "agencyName":"ABC",
  "subAgencyId":"SUB01",
  "brokerId":"BR01",
  "producerId":"PR01",
  "marketCategory":"Senior",
  "premium":1200,
  "commission":150
}


This allows a single aggregation pipeline to power:

KPI Cards
Drill-downs
Charts
Grid

without joins, hierarchy lookups, or cache refresh concerns.

For an enterprise broker dashboard, this "filter bar + KPI cards + drill-down hierarchy + charts + detail grid" pattern is typically the most scalable and user-friendly design.
