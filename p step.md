### Target architecture: Real-time Quote-to-Member Matching

Based on your requirements, I recommend a low-latency synchronous matching service backed by an optimized member search index, combined with asynchronous event-driven reconciliation.

The key architectural decision is that we should not run a complex member search inside the quote creation transaction.

Instead, we should separate:

* Quote creation and link record persistence.

* Fast member identification.

* Complex matching and future member resolution.

Your expected workload is quite manageable for a well-designed microservices architecture:

| Requirement       | Target                                                          |
| ----------------- | --------------------------------------------------------------- |
| Quote volume      | 10,000–100,000/day                                              |
| Matching latency  | Under 100 ms                                                    |
| Member population | 100,000–1 million                                               |
| Architecture      | Microservices / API-based                                       |
| Consistency       | Eventual consistency for unresolved matches                     |
| Availability      | Quote creation must not depend on matching service availability |

At 100,000 quotes per day, the average throughput is only approximately 1.16 quotes/second. However, the architecture should be sized for peak bursts, potentially 50–100 requests/second or higher depending on your actual traffic distribution.

I would design the system to target P95 matching latency below 100 ms, with P99 monitored separately. This is more meaningful than guaranteeing every request under 100 ms.

### 1\. Target solution architecture

### Logical architecture

Quote API / Quote Service

Receives quote creation request

Quote Transaction

Persist Quote + QuoteMemberLink + Outbox event

Member Matching API

Lightweight synchronous lookup

Member Matching Index

Redis / optimized relational read model

Unique match

Return Member ID

MATCHED

No / uncertain match

Keep Member ID null

PENDING

Asynchronous processing

Message Broker

Kafka / RabbitMQ / Cloud messaging

Advanced Matching & Reconciliation Service

Fuzzy matching, retries, future member resolution

Update QuoteMemberLink

Member_ID populated when confidently resolved

There is one important refinement to this architecture.

I would not make the matching API a mandatory synchronous dependency of quote creation. That would introduce a failure dependency and could violate your performance requirement.

Instead, the quote service should persist the quote and link first. It can then invoke the lightweight matching service within the overall request budget, or return immediately and let an asynchronous consumer perform matching.

For your stated latency requirement, I recommend the first approach with a strict timeout and asynchronous fallback.

### 2\. Detailed request processing flow

I would implement the quote creation flow in the following sequence.

### Step 1: Quote creation request

The Quote Service receives a request such as:

```json
{
  "quoteId": "Q10001",
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "1985-05-10",
  "email": "john@example.com",
  "phone": "9876543210",
  "nationalIdToken": "encrypted-or-tokenized-value"
}
```

The Quote Service should:

* Validate the request.

* Create the quote.

* Create a QuoteMemberLink with Member_ID = NULL.

* Set Match_Status = PENDING.

* Create an outbox event in the same database transaction.

All four operations should be atomic where they share the same database.

### Step 2: Invoke Member Matching Service

The Quote Service calls:

`POST /api/v1/member-matching/match`

Request:

```json
{
  "quoteId": "Q10001",
  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "1985-05-10",
  "email": "john@example.com",
  "phone": "9876543210",
  "nationalIdToken": "encrypted-or-tokenized-value"
}
```

The matching service queries its optimized matching index.

I recommend a dedicated relational read model initially, rather than introducing Elasticsearch or Redis immediately.

With 1 million members, a properly indexed relational database can handle this workload comfortably in many environments.

Possible matching indexes:

* National ID token

* Date of birth + normalized first name + normalized last name

* Normalized email

* Normalized phone

The service returns:

```json
{
  "quoteId": "Q10001",
  "matchStatus": "MATCHED",
  "memberId": "M50001",
  "matchType": "EXACT",
  "confidenceScore": 100
}
```

If no match:

```json
{
  "quoteId": "Q10001",
  "matchStatus": "UNMATCHED",
  "memberId": null,
  "matchType": null,
  "confidenceScore": 0
}
```

If multiple potential matches are found:

```json
{
  "quoteId": "Q10001",
  "matchStatus": "REVIEW_REQUIRED",
  "memberId": null,
  "matchType": "AMBIGUOUS",
  "confidenceScore": null
}
```

### Step 3: Update QuoteMemberLink

The Quote Service updates the existing link record.

For a successful match:

```sql
UPDATE QuoteMemberLink
SET
    Member_ID = 'M50001',
    Match_Status = 'MATCHED',
    Match_Type = 'EXACT',
    Matched_Date = CURRENT_TIMESTAMP
WHERE Quote_ID = 'Q10001';
```

For an unmatched quote, the existing record remains with Member_ID = NULL.

Important: The initial link creation and the member association are separate operations. This allows the quote creation process to succeed even if the matching service is temporarily unavailable.

### 3\. Database design

I recommend three main data structures.

### A. Quote table

Stores the quote information.

| Column       | Description          |
| ------------ | -------------------- |
| Quote_ID     | Primary key          |
| First_Name   | Applicant first name |
| Last_Name    | Applicant last name  |
| DOB          | Date of birth        |
| Email        | Email                |
| Phone        | Phone                |
| Created_Date | Creation timestamp   |

### B. QuoteMemberLink table

This is the central relationship table.

| Column        | Description                                     |
| ------------- | ----------------------------------------------- |
| Link_ID       | Primary key                                     |
| Quote_ID      | Foreign key to Quote                            |
| Member_ID     | Nullable foreign key to Member                  |
| Match_Status  | PENDING / MATCHED / UNMATCHED / REVIEW_REQUIRED |
| Match_Type    | EXACT / COMPOSITE / FUZZY / MANUAL              |
| Match_Score   | Confidence score                                |
| Match_Version | Matching rules version                          |
| Created_Date  | Created timestamp                               |
| Updated_Date  | Last updated timestamp                          |

Recommended database constraints:

```sql
CREATE UNIQUE INDEX UX_QuoteMemberLink_Quote
ON QuoteMemberLink(Quote_ID);

CREATE INDEX IX_QuoteMemberLink_Status
ON QuoteMemberLink(Match_Status);

CREATE INDEX IX_QuoteMemberLink_Member
ON QuoteMemberLink(Member_ID);
```

The unique Quote_ID constraint ensures that retries do not create duplicate link records.

### C. MemberMatchingIndex table

This is a read-optimized representation of member information.

| Column            | Description                |
| ----------------- | -------------------------- |
| Member_ID         | Member reference           |
| DOB               | Date of birth              |
| First_Name_Key    | Normalized first name      |
| Last_Name_Key     | Normalized last name       |
| National_ID_Token | Tokenized identifier       |
| Email_Key         | Normalized/tokenized email |
| Phone_Key         | Normalized/tokenized phone |
| Record_Status     | Active / Inactive          |
| Updated_Date      | Last updated timestamp     |

The matching service reads this table rather than repeatedly querying the primary Member table.

The matching index should be updated whenever member information changes, using member lifecycle events.

My technology recommendation: Start with an indexed relational read model. Introduce Redis only if actual performance testing demonstrates a need for it. Redis introduces cache invalidation and consistency challenges that may not be justified for your current population.

### 4\. Matching algorithm and business rules

I would use a deterministic matching engine first, followed by probabilistic matching where necessary.

A suggested matching hierarchy:

| Priority | Matching rule                            | Outcome      |
| -------- | ---------------------------------------- | ------------ |
| 1        | National ID token + DOB                  | Strong match |
| 2        | Existing Member ID                       | Exact match  |
| 3        | First name + Last name + DOB             | Candidate    |
| 4        | Email + DOB                              | Candidate    |
| 5        | Phone + DOB                              | Candidate    |
| 6        | Fuzzy name + DOB + additional attributes | Review       |

One important consideration is that an exact match on one attribute does not necessarily establish identity.

For example, email addresses and phone numbers can be shared or reassigned. Similarly, names and DOBs can be identical across family members.

I would define the matching engine to return one of three outcomes:

* `MATCHED`: One uniquely identified member meets the approved matching rules.

* `UNMATCHED`: No candidate satisfies the matching rules.

* `REVIEW_REQUIRED`: Multiple candidates or insufficient identity confidence.

Do not automatically select the first result returned by the database.

Also, maintain a configurable matching rules engine so the business can change matching rules without changing the Quote Service.

### 5\. Future member resolution

This is particularly important for your requirement.

Imagine the following:

| Date  | Event                                   |
| ----- | --------------------------------------- |
| Day 1 | Quote Q10001 created                    |
| Day 1 | No member found                         |
| Day 1 | Link L10001 created with NULL Member_ID |
| Day 3 | New member M50001 created               |
| Day 3 | MemberCreated event published           |
| Day 3 | Matching engine identifies Q10001       |
| Day 3 | Existing link L10001 updated            |

The architecture should support this without having to scan all historical quotes.

### How to achieve that?

Create matching keys for unmatched quotes.

For example:

`DOB + Normalized First Name + Normalized Last Name`

Maintain these keys in a searchable unmatched-quote index.

When a member is created:

* Member Service publishes `MemberCreated`.

* Matching Service consumes the event.

* Matching Service derives the corresponding matching keys.

* It searches only the unmatched quote index.

* It evaluates the candidates.

* It updates the existing QuoteMemberLink record.

This makes future resolution efficient.

You should also process `MemberUpdated` events because a member's demographic information might change and establish a match with a previously unmatched quote.

### Important concurrency scenario

Suppose a quote is being matched at exactly the same time that a new member is created.

Both real-time and background processing might attempt to update the same link.

To handle this:

* Use optimistic locking/version numbers.

* Make matching operations idempotent.

* Use a conditional update that only modifies eligible link statuses.

* Revalidate uniqueness before assigning Member_ID.

* Route conflicting candidate assignments to review.

For example:

```sql
UPDATE QuoteMemberLink
SET
    Member_ID = :memberId,
    Match_Status = 'MATCHED',
    Updated_Date = CURRENT_TIMESTAMP
WHERE
    Quote_ID = :quoteId
    AND Member_ID IS NULL
    AND Match_Status IN ('PENDING', 'UNMATCHED');
```

This is a simplified example; production logic should also verify the current matching version and candidate validity.

### 6\. Technology stack recommendation

For your workload, I would avoid overengineering the solution.

| Component           | Recommended technology                 |
| ------------------- | -------------------------------------- |
| Quote API           | Java Spring Boot / .NET                |
| Matching API        | Independent Spring Boot / .NET service |
| Primary database    | PostgreSQL / Oracle / SQL Server       |
| Matching read model | Indexed relational tables              |
| Messaging           | Kafka / RabbitMQ / Cloud messaging     |
| Cache (optional)    | Redis                                  |
| Monitoring          | Prometheus / Grafana / OpenTelemetry   |

A possible deployment could look like this:

Application layer

Quote Service

Matching Service

Data layer

Quote Database

Member Matching Read Model

Integration layer

Event Broker

Quote Matching Consumer

Member Change Consumer

I would initially deploy two independently scalable services:

* Quote Service.

* Member Matching Service.

The background consumers can be separate workloads within the Matching Service deployment.

You do not necessarily need a separate microservice for every component.

### 7\. Performance targets and latency budget

Since you have specified a 100 ms requirement, I would establish an explicit latency budget.

An illustrative budget for the synchronous matching path:

| Operation                               | Target |
| --------------------------------------- | ------ |
| Request validation                      | 5 ms   |
| Matching API invocation                 | 5 ms   |
| Indexed member lookup                   | 25 ms  |
| Matching rule evaluation                | 15 ms  |
| Link record update                      | 15 ms  |
| Response serialization/network overhead | 15 ms  |
| Contingency                             | 20 ms  |
| Total                                   | 100 ms |

These are engineering targets, not guaranteed timings. They need to be validated through load testing in your actual deployment environment.

I would monitor:

* P50, P95 and P99 matching latency.

* Percentage of quotes matched synchronously.

* Percentage of unmatched quotes.

* Matching service timeout rate.

* Message queue backlog.

* Average time to resolve an unmatched quote.

* Incorrect member association rate.

Important distinction: If the 100 ms requirement applies to the entire quote creation API, the quote database write, link creation and matching lookup must all fit within that budget. If it applies only to matching, the budget can be allocated differently.

### 8\. Failure handling and resilience

Consider these scenarios:

| Scenario                     | Expected behavior                                  |
| ---------------------------- | -------------------------------------------------- |
| Matching service unavailable | Quote and link are created; status remains PENDING |
| Member index unavailable     | Fall back to asynchronous matching                 |
| Message broker unavailable   | Outbox event remains available for retry           |
| Duplicate quote event        | Idempotent processing                              |
| Multiple members found       | REVIEW_REQUIRED                                    |
| Member updated later         | Trigger rematching                                 |
| Matching consumer failure    | Retry and dead-letter handling                     |
| Incorrect match detected     | Controlled unlink/reassignment with audit          |

I would also introduce a circuit breaker around the synchronous matching API. If the matching service starts responding slowly or becomes unavailable, the Quote Service should stop waiting and continue with asynchronous resolution.

### 9\. Healthcare-specific security considerations

Because you are dealing with member demographics and potentially PHI:

* Encrypt sensitive data at rest and in transit.

* Use keyed HMAC/tokenization for sensitive matching identifiers where appropriate.

* Restrict access to member matching data.

* Avoid including DOB, SSN, email or phone in Kafka messages unless necessary.

* Mask sensitive fields in application logs.

* Maintain an audit trail of every member association and reassignment.

* Apply appropriate retention policies to quote matching data.

* Ensure the matching process follows applicable healthcare privacy and security regulations.

Also, distinguish between a match and an eligibility determination. Identifying an existing member does not automatically mean that member is eligible for the quoted product or coverage.

### 10\. Suggested implementation roadmap

I would implement this in four phases.

* Phase 1 – Core functionality

    * Create QuoteMemberLink table.

    * Implement matching API.

    * Build indexed member matching read model.

    * Implement exact and composite matching rules.

    * Create link records for all quotes.

* Phase 2 – Event-driven architecture

    * Introduce message broker.

    * Implement transactional outbox.

    * Implement MemberCreated and MemberUpdated consumers.

    * Build asynchronous matching and retry mechanisms.

* Phase 3 – Advanced matching

    * Introduce confidence-based matching.

    * Implement ambiguous match handling.

    * Add manual resolution capabilities.

    * Implement unmatched quote reverse matching.

* Phase 4 – Performance and production readiness

    * Load testing at expected peak volume.

    * Validate P95 under 100 ms.

    * Test service failures and event recovery.

    * Implement monitoring and reconciliation.

    * Validate security and audit requirements.

### 11\. Three important architectural decisions

Before finalizing the solution, I would make these decisions explicitly:

| Decision                                 | My recommendation                                              |
| ---------------------------------------- | -------------------------------------------------------------- |
| Should quote creation wait for matching? | Only for a short, bounded lookup; otherwise fall back to async |
| Should we use Redis or Elasticsearch?    | Not initially; indexed relational read model should suffice    |
| Should future matching scan all quotes?  | No; maintain an indexed unmatched-quote matching key           |

One additional consideration: if a member is created after a quote, but another member is subsequently identified as a better match, you need a controlled reassignment process rather than blindly overwriting the existing Member_ID.

### My overall recommendation

I would implement this as a two-stage matching architecture:

* Stage 1 – Fast matching: Optimized synchronous lookup with a strict latency budget.

* Stage 2 – Continuous matching: Event-driven background matching for unmatched, ambiguous and subsequently created members.

This gives you low-latency quote processing, scalability, and the ability to resolve historical unmatched quotes without impacting the primary quote transaction.

The architecture also avoids introducing unnecessary technology for a population of up to one million members.

Suggested next step: I can turn this architecture into a technical implementation specification containing detailed API contracts, SQL DDL, Kafka event schemas, sequence diagrams and component-level responsibilities.

Prepare implementation specification
