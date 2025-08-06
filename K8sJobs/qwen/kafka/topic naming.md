Here's a **comprehensive Topic Naming Convention Policy Document** tailored for your architecture — using **NestJS consumers**, **Kafka Connect (Debezium)**, and multiple data sources. This policy ensures **consistency, discoverability, scalability, and team alignment** across services.

---

# 📄 **Kafka Topic Naming Convention Policy**

**Version:** 1.0  
**Owner:** Data Engineering & Platform Team  
**Effective Date:** 2025-04-05  
**Applicable To:** All teams producing or consuming events from Apache Kafka

---

## 🔍 Purpose

To standardize the naming of Kafka topics across all environments and services, ensuring:
- Clear understanding of topic content
- Predictable routing and filtering
- Easier monitoring, governance, and access control
- Interoperability between Kafka Connect, microservices (e.g., NestJS), and stream processing applications

---

## 🧭 Scope

This policy applies to:
- All new topics created in **development, staging, and production** environments
- Teams using **custom producers** (e.g., NestJS, Spring Boot)
- Teams using **Kafka Connect** (Debezium, JDBC, etc.)
- Data pipelines involving **CDC, ETL, event sourcing, and integration**

> ❌ This policy does **not** apply retroactively to legacy topics without approval.

---

## ✅ Naming Convention Rules

All Kafka topic names **MUST** follow this format:

```
<domain>[.<subdomain>].<entity>.<event>[-<suffix>]
```

### 🔤 Components

| Component | Description | Example |
|--------|-------------|--------|
| `domain` | Business domain (bounded context) | `user`, `order`, `payment`, `inventory` |
| `subdomain` (optional) | Sub-area within domain | `auth`, `profile`, `billing` |
| `entity` | The main business object | `account`, `cart`, `transaction` |
| `event` | Type of event or action | `created`, `updated`, `deleted`, `validated` |
| `suffix` (optional) | Specialization or routing hint | `-dlq`, `-retry`, `-snapshot` |

> ✅ **All names must be lowercase**  
> ✅ **Use dots (`.`) as separators**  
> ❌ No spaces, underscores, or special characters

---

### ✅ Examples

| Use Case | Topic Name |
|--------|-----------|
| User account created | `user.account.created` |
| Payment processed | `payment.transaction.processed` |
| Order shipped | `order.fulfillment.shipped` |
| Auth login attempt | `user.auth.login.attempted` |
| Inventory stock updated | `inventory.product.updated` |
| Debezium CDC from PostgreSQL | `cdc.postgres.user_service.accounts` |
| Debezium CDC from MongoDB | `cdc.mongodb.analytics.events` |
| Dead-letter queue | `user.account.created-dlq` |
| Retry queue | `payment.transaction.processed-retry` |

---

## 🏷️ Domain & Event Taxonomy

### Standard Domains
Use one of these standard domains when possible:

| Domain | Description |
|-------|-------------|
| `user` | User management, authentication, profiles |
| `order` | Orders, carts, checkout |
| `payment` | Payments, refunds, invoicing |
| `inventory` | Product stock, warehouse |
| `notification` | Emails, SMS, alerts |
| `audit` | Security, access logs |
| `analytics` | Behavioral or business analytics |
| `fraud` | Risk detection, fraud alerts |
| `cdc` | Change Data Capture (Kafka Connect) |

> ✅ Teams may propose new domains via RFC to the Platform Team.

---

### Standard Event Types

| Event | Meaning |
|------|--------|
| `.created` | A new entity was created |
| `.updated` | An entity was modified |
| `.deleted` | An entity was removed (soft/hard) |
| `.validated` | Validation completed |
| `.confirmed` | User or system confirmed action |
| `.failed` | Operation failed |
| `.recovered` | System recovered from error |
| `.expired` | TTL or time-based expiry |
| `.snapshot` | Full state dump (e.g., compaction) |

> Avoid ambiguous verbs like `.send`, `.process`, `.handle`

---

## 🛠 Kafka Connect Special Cases

When using **Debezium or Kafka Connect**, follow these **extended patterns**:

### CDC Topics (Debezium)
```
cdc.<source-type>.<database>.<schema>.<table>
```
- `source-type`: `postgres`, `mysql`, `mongodb`, `oracle`
- `database`: logical database name
- `schema`: schema name (e.g., `public`)
- `table`: table name

✅ Example:  
- `cdc.postgres.user_service.public.users`
- `cdc.mongodb.analytics_db.events`

> ⚠️ Use `cdc.*` prefix to isolate CDC streams from business events.

---

## 🌐 Environment Handling

Avoid including environment (`dev`, `prod`) in topic names. Instead:

- Use **separate Kafka clusters per environment**
- Or use **prefixes in access control**, not topic names

❌ **Do NOT** use:
- `dev.user.account.created`
- `prod-order-created`

✅ **Instead**, rely on:
- Cluster-level separation
- Monitoring tags
- ACLs by environment

---

## 🔒 Governance & Approval

### New Topic Registration
Before creating a new topic, teams **MUST**:

1. Check if an existing topic can be reused
2. Submit a **Topic Registration Form** (see template below)
3. Get approval from the **Data Governance Team**

### Required Metadata
Each topic must be documented with:
- Purpose
- Owner team
- Schema (Avro/Protobuf reference)
- Retention period
- Sensitivity level (PII, PCI, etc.)

---

## 📝 Topic Registration Template

```markdown
- **Topic Name**: `user.account.created`
- **Domain**: `user`
- **Entity**: `account`
- **Event Type**: `created`
- **Producer(s)**: `auth-service`, `NestJS`
- **Consumer(s)**: `notification-service`, `analytics-ingest`
- **Schema Registry Subject**: `user.account.created-value`
- **Retention**: 7 days
- **Sensitivity**: PII (contains email)
- **Description**: Published when a new user completes registration.
- **Contact**: team-auth@company.com
```

> Store in internal wiki or data catalog (e.g., DataHub, Confluent Control Center).

---

## 🚫 Anti-Patterns (Do NOT Do This)

| ❌ Bad Example | ❌ Why It’s Bad | ✅ Fix |
|---------------|----------------|-------|
| `UserCreatedEvent` | Mixed case, no structure | `user.account.created` |
| `myapp_user_events` | Underscore, app-centric | `user.account.created` |
| `kafka_data_stream_1` | Ambiguous, non-descriptive | Use domain-based naming |
| `prod_payment_success` | Environment in name | Use separate cluster |
| `send_email_queue` | Imperative verb, not event | `user.account.created` |

---

## 🔄 Migration Plan for Legacy Topics

| Step | Action |
|------|--------|
| 1 | Inventory all existing topics |
| 2 | Classify by domain and purpose |
| 3 | Map legacy → new naming |
| 4 | Run dual-write (old + new) during transition |
| 5 | Deprecate old topic with 30-day notice |
| 6 | Update documentation and consumers |

> Use **Kafka Streams** or **MirrorMaker** if needed.

---

## 📚 References

- [Confluent: Event-Driven Microservices](https://docs.confluent.io/platform/current/event-driven-architecture.html)
- [Domain-Driven Design](https://domaindrivendesign.org/)
- [Debezium Topic Naming](https://debezium.io/documentation/reference/stable/connectors/postgresql.html#postgresql-topic-names)

---

## 📣 Approval

| Role | Name | Signature | Date |
|------|------|----------|------|
| Chief Data Officer | Jane Doe | ✅ | 2025-04-05 |
| Head of Platform Engineering | John Smith | ✅ | 2025-04-05 |

---

## 📎 Appendix: Example Ecosystem

| System | Topic Produced |
|-------|----------------|
| Auth Service (NestJS) | `user.account.created`, `user.auth.login.attempted` |
| Debezium (PostgreSQL) | `cdc.postgres.orders.public.purchases` |
| Payment Gateway | `payment.transaction.processed`, `payment.transaction.failed` |
| Inventory Sync | `inventory.product.updated` |
| Analytics Pipeline | `analytics.user.behavior.clicked` |

---

Let me know if you'd like:
- A downloadable PDF version
- A Confluence/Notion template
- Integration with OpenAPI or AsyncAPI docs
- Automation script to validate topic names

This policy will help your team build a **clean, maintainable, and scalable event-driven architecture**. 🚀