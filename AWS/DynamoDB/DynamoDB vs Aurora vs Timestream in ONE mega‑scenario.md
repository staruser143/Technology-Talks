Here is a **single integrated mega‑scenario** that forces you to choose between **DynamoDB, Aurora, and Timestream** within *one* architecture.  
This mimics exactly how SAP‑C02 multi‑select traps are structured.

***

# Mega‑Scenario: “Global Industrial IoT Platform”

A global industrial automation company runs **200,000 sensors** across factories worldwide. These sensors continuously produce data used by **operations**, **analytics**, and **customer‑facing dashboards**.

The company is modernizing its platform on AWS.

## Workload Characteristics (read carefully — all three database choices appear)

1.  **High‑volume telemetry ingestion**  
    Each sensor generates **1 KB messages every 2 seconds**.  
    Total throughput peaks at **300K writes/second** globally.

2.  **Two patterns of storage emerge**
    *   **Hot operational metrics** (last 30 days) must be queryable with millisecond latency for dashboards
    *   **Historical time‑series data** (5 years) used for trend analysis and ML

3.  **Application Layer Needs**
    *   A **multi‑tenant SaaS portal** allows enterprise customers to:
        *   manage users
        *   configure sensors
        *   run reports
        *   integrate with their ERP

    *   The SaaS app requires:
        *   relational schema
        *   foreign keys (e.g., tenants → devices → device profiles)
        *   transactional integrity

4.  **Query Types**
    *   Dashboard:  “Give me the last 15 minutes of CPU, temperature, vibration for a device”
    *   Analytics: “Show last 5 years of temperature readings grouped hourly”
    *   App metadata: “List all devices for Tenant A sorted by install date”

5.  **Global deployment**
    *   Factories across US, EU, APAC
    *   Dashboards must show near‑real‑time updates for local users
    *   Cross‑region writes are required

***

# Correct Overlay: Which Database Handles Which Part?

## 1. **Sensor Telemetry Ingestion (hot path, extreme scale)**

✔ **DynamoDB with Global Tables**

Reasoning:

*   Writes per second far exceed typical RDS/Aurora scaling
*   Millisecond latency for dashboards
*   Predictable PK/SK:  
    PK = device\_id, SK = timestamp
*   Global replication built‑in
*   TTL can purge > 30‑day data automatically

***

## 2. **Long‑term Time‑Series Storage (years of data)**

✔ **Amazon Timestream**

Reasoning:

*   Time‑series optimized
*   Built‑in tiering to memory store → magnetic store
*   Aggregation queries: hourly, daily, weekly
*   Purpose‑built for 5‑year analytics
*   SQL‑like time‑series functions included

***

## 3. **SaaS Tenant Metadata + Business App Schema**

✔ **Amazon Aurora (PostgreSQL)**

Reasoning:

*   Multi‑tenant relational models
*   Foreign keys, constraints
*   Transactions for creating tenants, devices, user permissions
*   Supports reporting queries and schema‑driven functionality
*   Horizontal read scaling with read replicas

***

# Unified Architecture Diagram (Textual)

                     ┌────────────────────────────┐
                     │   Sensor Devices (Global)   │
                     └──────────────┬─────────────┘
                                    |
                              Ingestion Layer
                                    |
                            Amazon Kinesis Data Streams
                                    |
                    ┌──────────────┴──────────────┐
                    |                             |
            DynamoDB Global Tables         Timestream
          (Hot operational metrics)   (Long‑term historical data)
                    |                             |
            Millisecond dashboards           Analytics, ML
                    |
             App Microservices (EKS/Lambda)
                    |
              Multi‑tenant SaaS Portal
                    |
                Aurora PostgreSQL
             (Users, Tenants, Devices)

***

# Why Each Database Is Not Replacing the Others

## Why NOT Aurora for telemetry?

*   Cannot scale to 300K writes/sec globally
*   Sharding manually is complex
*   Cross‑region writes need custom logic
*   Storage cost/time‑series compression inferior

## Why NOT DynamoDB for relational app metadata?

*   No foreign keys
*   No relational constraints
*   Multi‑table joins cannot be done efficiently
*   Query flexibility required for reports

## Why NOT Timestream for hot dashboards?

*   Timestream memory store is fast but not as fast or predictable as DynamoDB
*   Global low-latency replication is not built-in
*   High write throughput is expensive vs DynamoDB on‑demand

***

# Final Overlay Summary (Exam‑Ready)

| Requirement                  | Best Fit       | Why                                                 |
| ---------------------------- | -------------- | --------------------------------------------------- |
| High‑TPS operational metrics | **DynamoDB**   | Massive scale, predictable PK access, global tables |
| Long‑term time‑series trends | **Timestream** | Purpose‑built time aggregation, cheap tiering       |
| Multi‑tenant app data        | **Aurora**     | Relational schema, ACID, joins                      |

***

