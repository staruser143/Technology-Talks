# SLI vs SLO vs SLA vs KPI

This is a very common source of confusion in Site Reliability Engineering (SRE), IT Operations, and DevOps.

Think of them as layers: SLI → SLO → SLA while KPI is a broader business or operational measurement.

## Quick comparison

| Term | Full form | Purpose | Audience | Example |
|---|---|---|---|---|
| SLI | Service Level Indicator | Measures actual service performance | Engineering | API success rate = 99.95% |
| SLO | Service Level Objective | Target you want to achieve | Engineering / Product | API success rate should be ≥ 99.9% |
| SLA | Service Level Agreement | Contractual commitment with consequences | Customers | If availability drops below 99.9%, customer gets service credits |
| KPI | Key Performance Indicator | Measures business or operational success | Business / Leadership | Customer retention = 95% |

---

## 1. SLI (Service Level Indicator)

An SLI is the actual measurement of a service. It answers: "How is the system performing right now?"

Examples:

- Availability = 99.95%
- Request latency = 180 ms
- Error rate = 0.05%
- Order processing success = 99.98%

For instance:

- Total Requests = 1,000,000
- Successful Requests = 999,500

SLI = 999,500 / 1,000,000 = 99.95%

An SLI is simply a metric.

---

## 2. SLO (Service Level Objective)

An SLO is the target value for an SLI. It answers: "What level of reliability do we intend to provide?"

Example:

- SLI: API availability
- SLO: 99.9% availability per month

Another example:

- SLI: Response time
- SLO: 95% of requests complete within 300 ms

**Relationship**

- SLI = Actual Performance
- SLO = Desired Performance

Example:

- Actual Availability (SLI) = 99.95%
- Target Availability (SLO) = 99.9%

Result: SLO met

---

## 3. SLA (Service Level Agreement)

An SLA is a business or contractual agreement based on one or more SLOs. It answers: "What happens if we fail?"

Example:

**SLA:** Availability must be >= 99.9%

**If violated:** Customer receives 10% service credit.

Unlike SLOs, SLAs usually involve legal commitments, financial penalties, service credits, and escalation procedures.

**Relationship of SLI, SLO, and SLA**

- SLI → Measurement
- SLO → Target based on measurement
- SLA → Contract based on target

Example:

- SLI: Current availability = 99.92%
- SLO: Target availability >= 99.9%
- SLA: Customer guaranteed 99.5%

Notice:

- SLI = 99.92%
- SLO = 99.9%
- SLA = 99.5%

A common pattern is: SLO > SLA. Companies keep some buffer.

Example:

- Internal SLO = 99.95%
- Customer SLA = 99.9%

This gives engineering teams room before breaching customer commitments.

---

## 4. KPI (Key Performance Indicator)

A KPI measures whether a business or organization is successful in achieving goals. It answers: "Is the business achieving its objectives?"

Examples:

**Business KPIs**

- Revenue Growth
- Customer Retention
- Conversion Rate
- Net Promoter Score (NPS)
- Renewal Rate

**IT KPIs**

- Mean Time To Recovery (MTTR)
- Deployment Frequency
- Change Failure Rate
- Ticket Resolution Time

Unlike SLIs/SLOs/SLAs, KPIs are not restricted to reliability.

Example: E-Commerce Website

- SLI: Checkout API success rate = 99.95%
- SLO: Checkout API success rate >= 99.9%
- SLA: If monthly availability drops below 99.5%, customers receive credits.
- KPI: Online revenue growth = 15%

Notice:

Reliable checkout system

    ↓

Higher conversion rate

    ↓

Higher revenue KPI

---

## Architect's view

For an enterprise application:

Business Goal
    ↓
KPIs
    ↓
Customer Experience
    ↓
SLAs
    ↓
SLOs
    ↓
SLIs

Example:

- KPI: Customer retention > 95%
- SLA: 99.9% availability
- SLO: 99.95% availability
- SLI: Measured availability = 99.97%

---

## Easy exam trick

Remember:

- SLI = Indicator (Actual Measurement)
- SLO = Objective (Target)
- SLA = Agreement (Contract)
- KPI = Business Success Metric

A useful mnemonic:

Measure → Target → Contract

SLI → SLO → SLA

And:

KPI = "Are we achieving business outcomes?"

This distinction is frequently tested in SRE, DevOps, cloud architecture, and platform engineering interviews.
