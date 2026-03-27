Amazon **Route 53 patterns** are **recurring DNS design patterns** that AWS expects architects to understand for **availability, latency optimization, disaster recovery, traffic shaping, and hybrid networking**.  
These patterns are heavily tested in **AWS certification exams** and used in real-world architectures.

Below is a **structured, exam‑oriented, and practical guide**.

***

# Amazon Route 53 Design Patterns

## 1. Simple Routing Pattern

**Use case:** Single endpoint or basic DNS resolution

**How it works**

*   One DNS record → one resource (ALB, EC2, CloudFront, S3 website)

**When to use**

*   Dev/test environments
*   Single‑region, non‑HA apps

**Key traits**

*   No health checks
*   No traffic control

✅ **Correct**

> One website hosted behind a single ALB.

❌ **Not suitable**

> High availability or DR requirements.

***

## 2. Weighted Routing Pattern

**Use case:** Controlled traffic distribution

**How it works**

*   Multiple records for same name
*   Each record gets a percentage (weight)

**Common patterns**

*   Canary releases (90% old, 10% new)
*   Blue/Green deployments
*   A/B testing

**Key traits**

*   Supports health checks
*   Traffic split is probabilistic, not guaranteed

✅ **Correct**

> Gradually shift users to a new microservice version.

❌ **Distractor**

> Choosing the *nearest* region (Latency routing does that).

***

## 3. Latency-Based Routing Pattern

**Use case:** Lowest latency for global users

**How it works**

*   User routed to AWS region with lowest latency
*   Uses AWS global network measurements

**Common patterns**

*   Active‑active multi‑region apps
*   Global SaaS platforms

**Key traits**

*   Health check aware
*   Region‑centric (not IP‑centric)

✅ **Correct**

> Users in Asia routed to ap‑south‑1, users in US → us‑east‑1.

❌ **Distractor**

> Country‑based routing (that’s Geolocation).

***

## 4. Failover Routing Pattern

**Use case:** Disaster recovery

**How it works**

*   Primary record + Secondary record
*   Route 53 health checks decide failover

**DR patterns supported**

*   Active‑Passive
*   Pilot Light
*   Warm Standby (DNS‑based)

**Key traits**

*   Binary decision: healthy vs unhealthy
*   DNS‑level failover (not load balancing)

✅ **Correct**

> Primary ALB in us‑east‑1, DR ALB in us‑west‑2.

❌ **Works but not ideal**

> Active‑active DR (Latency routing fits better).

***

## 5. Geolocation Routing Pattern

**Use case:** Location‑based business rules

**How it works**

*   Routes traffic based on **country / continent**
*   Not distance or latency based

**Common patterns**

*   Legal/regulatory requirements
*   Country‑specific content
*   Regional landing pages

**Key traits**

*   Supports default record
*   Explicit geography mapping

✅ **Correct**

> EU users → EU endpoint due to GDPR.

❌ **Distractor**

> Nearest region routing (Latency routing).

***

## 6. Geoproximity Routing Pattern (Traffic Flow only)

**Use case:** Distance‑based routing with control

**How it works**

*   Routes based on **physical proximity**
*   Allows *bias* to shift traffic toward / away from regions

**Advanced scenarios**

*   Gradual regional traffic migration
*   Capacity‑aware routing

**Key traits**

*   Requires **Route 53 Traffic Flow**
*   Works across AWS + on‑prem locations

✅ **Correct**

> Prefer Mumbai users to ap‑south‑1 but bias some to Singapore.

⚠️ **Exam tip**

> If bias or distance adjustment is mentioned → **Geoproximity**

***

## 7. Multi‑Value Answer Routing Pattern

**Use case:** DNS‑level load balancing without ELB

**How it works**

*   Route 53 returns multiple healthy IPs
*   Client chooses one

**Common patterns**

*   Legacy apps
*   On‑prem endpoints
*   Low‑cost HA

**Key traits**

*   Health checks supported
*   Not true load balancing

✅ **Correct**

> Multiple EC2 instances without ALB.

❌ **Distractor**

> High‑scale, fine‑grained traffic distribution (use ALB/NLB).

***

## 8. Alias Record Pattern

**Use case:** AWS‑native DNS integration

**How it works**

*   DNS name points to AWS resource without IP
*   Free queries for AWS targets

**Supports**

*   ALB / NLB
*   CloudFront
*   S3 static websites
*   API Gateway
*   Elastic Beanstalk

✅ **Correct**

> example.com → CloudFront distribution.

❌ **Exam trap**

> CNAME at zone apex (not allowed; use Alias).

***

# Higher‑Level Route 53 Architecture Patterns

## 9. Active‑Active Multi‑Region Pattern

**Combination**

*   Latency routing + health checks

**Benefit**

*   High availability
*   Low latency
*   No cold starts

***

## 10. Active‑Passive DR Pattern

**Combination**

*   Failover routing + health checks

**Benefit**

*   Lower cost
*   Clear DR posture

***

## 11. Hybrid DNS Pattern

**Combination**

*   Route 53 public + private hosted zones
*   On‑prem DNS forwarding

**Use case**

*   Hybrid cloud
*   Split‑horizon DNS

***

# Exam‑Friendly Pattern Selection Matrix

| Requirement                | Correct Pattern |
| -------------------------- | --------------- |
| Canary release             | Weighted        |
| Lowest latency globally    | Latency         |
| Disaster recovery          | Failover        |
| Country‑specific routing   | Geolocation     |
| Distance + bias control    | Geoproximity    |
| Multiple endpoints w/o ELB | Multi‑Value     |
| AWS service DNS names      | Alias           |

***

# Pro‑Level Exam Trap Signals 🧠

| Wording in question       | Meaning           |
| ------------------------- | ----------------- |
| “Gradually shift traffic” | Weighted          |
| “Closest region”          | Latency (not Geo) |
| “Legal/regulatory”        | Geolocation       |
| “Bias traffic away”       | Geoproximity      |
| “DR failover”             | Failover          |
| “Zone apex”               | Alias             |

***
