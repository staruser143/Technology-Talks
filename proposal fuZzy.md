Technical Implementation Specification

Real-Time Quote-to-Member Matching Platform

Domain: Healthcare Payer
Architecture: Microservices, API-based, Event-driven
Version: 1.0
Status: Proposed Technical Design

---

1. Executive Summary

The objective is to identify whether an applicant associated with a newly created insurance quote already exists as a member in the healthcare payer's member management system.

When a quote is created:

1. A QuoteMemberLink record must always be created.
2. If an existing member is confidently identified, the link record must reference that member.
3. If no member is identified, the link record must exist with a NULL Member_ID.
4. If a matching member is created or identified in the future, the existing link record must be updated.
5. Quote creation must not be significantly affected by member matching operations.

The proposed solution uses a hybrid matching architecture:

- A bounded synchronous matching process for low-latency identification.
- An optimized member matching read model for fast member searches.
- Asynchronous event-driven matching for timeouts, unmatched quotes and ambiguous matches.
- Reverse matching when new members are created or updated.
- A reconciliation mechanism to recover from missed events and processing failures.

1.1 Non-functional requirements

Requirement| Target
Daily quote volume| 10,000–100,000
Member population| 100,000–1 million
Matching latency| Under 100 ms target
API latency objective| P95 under 100 ms
Matching availability| Independently scalable
Link creation| Mandatory for every successfully created quote
Member association| Only for approved high-confidence matches
Future resolution| Supported
Data consistency| Strong consistency for quote/link creation; eventual consistency for matching
Duplicate prevention| Mandatory
Auditability| Complete matching decision history

The 100 ms objective is a performance target requiring validation through testing, not a guaranteed latency.

---

2. Scope and Assumptions

2.1 In scope

- Quote creation.
- QuoteMemberLink creation.
- Real-time member identification.
- Exact and composite matching.
- Unmatched and ambiguous member handling.
- Future member identification.
- Member creation/update event processing.
- Retry and reconciliation mechanisms.
- Performance optimization.
- Security and audit controls.

2.2 Out of scope

- Insurance underwriting.
- Product eligibility determination.
- Premium calculation.
- Member enrollment.
- Coverage activation.
- Claims processing.
- Determination of whether an existing member is eligible for a particular insurance product.

Member identification must not be treated as an eligibility determination.

2.3 Working assumptions

1. Quote Service and Member Service are independent microservices.
2. Each service owns its respective transactional database.
3. The Quote Service owns QuoteMemberLink.
4. The Member Service owns the authoritative member information.
5. The Matching Service maintains a read-optimized representation of member demographics.
6. A message broker is available.
7. Member records can be created or modified independently of quote creation.
8. Member matching rules are approved by the payer's business and compliance teams.

---

3. Target Architecture

3.1 Logical architecture

flowchart TB
    A[Client / Distribution Channel]
    B[API Gateway]
    C[Quote Service]
    D[(Quote Database)]
    E[QuoteMemberLink]
    F[Transactional Outbox]
    G[Member Matching API]
    H[(Member Matching Read Model)]
    I[Message Broker]
    J[Quote Matching Consumer]
    K[Member Change Consumer]
    L[Reverse Matching Engine]
    M[Member Service]
    N[(Member Database)]
    O[Reconciliation Job]

    A --> B
    B --> C
    C --> D
    D --> E
    D --> F
    C --> G
    G --> H
    G --> E
    F --> I
    I --> J
    J --> G
    M --> N
    M --> I
    I --> K
    K --> H
    K --> L
    L --> E
    O --> E
    O --> L

3.2 Component responsibilities

Component| Responsibility
API Gateway| Authentication, authorization, rate limiting and request routing
Quote Service| Quote creation and QuoteMemberLink ownership
Quote Database| Persistent quote and link data
Transactional Outbox| Reliable event publication
Member Service| Authoritative member creation and modification
Member Database| Source of truth for members
Matching Service| Matching rules and member identification
Matching Read Model| Optimized member search representation
Message Broker| Reliable event delivery
Quote Matching Consumer| Asynchronous quote resolution
Reverse Matching Engine| Identify previously unmatched quotes when member data changes
Reconciliation Job| Recover missed or unresolved processing
Audit Service/Table| Record matching decisions and changes

3.3 Recommended deployment

Initially, deploy three logical workloads:

1. Quote Service.
2. Member Matching Service.
3. Matching Background Worker.

The Member Service remains an independent existing service.

The matching API and matching worker can share application libraries and business rules while scaling independently.

Do not introduce additional microservices for individual matching rules unless there is a demonstrated operational or scaling requirement.

---

4. End-to-End Processing Design

The proposed design separates persistence from matching.

4.1 Quote creation transaction

The Quote Service performs the following operations in one database transaction:

1. Insert Quote.
2. Insert QuoteMemberLink with Member_ID = NULL.
3. Set Match_Status = PENDING.
4. Insert QuoteCreated event into the outbox.
5. Commit the transaction.

If any database operation fails, the transaction is rolled back.

This ensures that a successfully persisted quote always has its corresponding link record and durable event.

4.2 Synchronous matching

After the database transaction commits:

1. Quote Service invokes Member Matching API.
2. Matching Service searches the optimized member read model.
3. Matching Service evaluates matching rules.
4. It returns a matching decision.
5. Quote Service updates the existing QuoteMemberLink.

The matching API must have a strict timeout.

Recommended initial timeout: 35 ms, subject to deployment topology and performance testing.

If the matching service times out, the quote creation process should not wait indefinitely. The link remains PENDING and asynchronous processing takes over.

4.3 Synchronous sequence diagram

sequenceDiagram
    participant Client
    participant Quote as Quote Service
    participant DB as Quote Database
    participant Match as Matching Service
    participant Index as Member Read Model

    Client->>Quote: Create Quote
    Quote->>DB: Begin Transaction
    Quote->>DB: Insert Quote
    Quote->>DB: Insert QuoteMemberLink (PENDING)
    Quote->>DB: Insert Outbox Event
    Quote->>DB: Commit

    Quote->>Match: Match Member (bounded timeout)
    Match->>Index: Indexed Lookup
    Index-->>Match: Candidate Members
    Match->>Match: Apply Matching Rules

    alt Unique high-confidence match
        Match-->>Quote: MATCHED + Member ID
        Quote->>DB: Update QuoteMemberLink
        DB-->>Quote: Updated
    else No match
        Match-->>Quote: UNMATCHED
        Quote->>DB: Update Match Status
    else Ambiguous or timeout
        Match-->>Quote: REVIEW_REQUIRED / Timeout
        Note over DB: Keep unresolved link
    end

    Quote-->>Client: Quote Created

The Quote Service should return the quote identifier and current matching status.

If matching is unresolved, the API can return PENDING rather than failing quote creation.

---

5. Asynchronous Matching Architecture

The asynchronous processing path is responsible for:

- Synchronous matching timeouts.
- Unmatched quotes.
- Ambiguous matches.
- Member creation.
- Member demographic updates.
- Recovery after processing failures.

5.1 QuoteCreated event processing

The outbox publisher reads committed events and publishes them to the message broker.

The Quote Matching Consumer receives the event and invokes the Matching Service.

The consumer performs idempotent processing.

If the quote was already successfully matched synchronously, the consumer should not unnecessarily overwrite the existing association.

5.2 Asynchronous sequence diagram

sequenceDiagram
    participant DB as Quote Database
    participant Publisher as Outbox Publisher
    participant MQ as Message Broker
    participant Worker as Matching Worker
    participant Match as Matching Service
    participant Index as Member Read Model

    DB->>Publisher: Read unpublished events
    Publisher->>MQ: Publish QuoteCreated
    MQ-->>Publisher: Acknowledge publication
    Publisher->>DB: Mark event published

    MQ->>Worker: Deliver QuoteCreated
    Worker->>Match: Resolve Quote
    Match->>Index: Search Candidates
    Index-->>Match: Candidate Results
    Match->>Match: Evaluate Matching Rules

    alt Unique match
        Match-->>Worker: Member ID
        Worker->>DB: Update Existing Link
    else No match
        Match-->>Worker: UNMATCHED
        Worker->>DB: Update Status
    else Ambiguous
        Match-->>Worker: REVIEW_REQUIRED
        Worker->>DB: Update Status
    end

    Worker-->>MQ: Acknowledge Event

5.3 Future member resolution

Whenever a new member is created or existing member demographics are updated, Member Service publishes an event.

The Matching Service uses the member attributes to search previously unresolved quote records.

sequenceDiagram
    participant Member as Member Service
    participant MQ as Message Broker
    participant Worker as Member Change Consumer
    participant Index as Member Matching Index
    participant Reverse as Reverse Matching Engine
    participant DB as Quote Database

    Member->>MQ: Publish MemberCreated
    MQ->>Worker: Deliver Event
    Worker->>Index: Update Matching Index
    Worker->>Reverse: Trigger Reverse Matching
    Reverse->>DB: Find Candidate Unmatched Quotes
    DB-->>Reverse: Candidate Quote IDs
    Reverse->>Reverse: Apply Matching Rules
    Reverse->>DB: Update Existing QuoteMemberLink
    DB-->>Reverse: Updated
    Worker-->>MQ: Acknowledge Event

This avoids repeatedly scanning all historical quotes.

---

6. API Specifications

6.1 Quote creation API

Endpoint

"POST /api/v1/quotes"

Request

{
  "applicant": {
    "firstName": "John",
    "lastName": "Smith",
    "dateOfBirth": "1985-05-10",
    "email": "john@example.com",
    "phone": "9876543210",
    "nationalIdToken": "TOKEN_VALUE"
  },
  "productId": "PRODUCT001"
}

Response

{
  "quoteId": "Q10001",
  "linkId": "L10001",
  "matchingStatus": "PENDING",
  "memberId": null,
  "createdAt": "2026-11-12T10:00:00Z"
}

When matching completes within the synchronous request:

{
  "quoteId": "Q10001",
  "linkId": "L10001",
  "matchingStatus": "MATCHED",
  "memberId": "M50001",
  "matchType": "EXACT"
}

The actual HTTP status should reflect quote creation success, not whether a member was found.

6.2 Member Matching API

Endpoint

"POST /api/v1/member-matching/match"

Request

{
  "requestId": "MR10001",
  "quoteId": "Q10001",
  "attributes": {
    "firstName": "John",
    "lastName": "Smith",
    "dateOfBirth": "1985-05-10",
    "emailToken": "EMAIL_TOKEN",
    "phoneToken": "PHONE_TOKEN",
    "nationalIdToken": "ID_TOKEN"
  },
  "matchingPolicyVersion": "V1"
}

Successful response

{
  "requestId": "MR10001",
  "matchStatus": "MATCHED",
  "memberId": "M50001",
  "matchType": "EXACT",
  "confidenceScore": 100,
  "rulesApplied": [
    "NATIONAL_ID_DOB"
  ]
}

No-match response

{
  "requestId": "MR10001",
  "matchStatus": "UNMATCHED",
  "memberId": null,
  "matchType": null,
  "confidenceScore": 0
}

Ambiguous response

{
  "requestId": "MR10001",
  "matchStatus": "REVIEW_REQUIRED",
  "memberId": null,
  "candidateCount": 2
}

Candidate member identifiers should not be exposed to callers unless explicitly authorized.

6.3 Matching status API

Endpoint

"GET /api/v1/quotes/{quoteId}/member-link"

Response

{
  "quoteId": "Q10001",
  "linkId": "L10001",
  "memberId": "M50001",
  "matchStatus": "MATCHED",
  "matchType": "EXACT",
  "matchedAt": "2026-11-12T10:00:01Z"
}

This endpoint allows downstream applications to retrieve the latest link status.

6.4 Manual resolution API

Endpoint

"POST /api/v1/quote-member-links/{linkId}/resolve"

This API should be restricted to authorized users or systems.

Request

{
  "memberId": "M50001",
  "resolutionReason": "VERIFIED_IDENTITY",
  "resolvedBy": "USER001",
  "expectedVersion": 3
}

The API must validate the member and maintain an audit trail.

---

7. Database Design

The Quote Service owns the QuoteMemberLink and Outbox tables.

The Member Service owns the authoritative member information.

The Matching Service owns or maintains its matching read model.

The following SQL uses PostgreSQL syntax. Equivalent data types, indexing and locking features should be adapted for Oracle, SQL Server or other databases.

7.1 QuoteMemberLink table

CREATE TABLE quote_member_link (
    link_id UUID PRIMARY KEY,
    quote_id VARCHAR(64) NOT NULL,
    member_id VARCHAR(64),

    match_status VARCHAR(30) NOT NULL,
    match_type VARCHAR(30),

    match_score NUMERIC(5,2),
    matching_policy_version VARCHAR(30),

    match_source VARCHAR(30),
    matched_at TIMESTAMPTZ,

    version BIGINT NOT NULL DEFAULT 1,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_quote_member_link_quote
        UNIQUE (quote_id),

    CONSTRAINT chk_match_status
        CHECK (match_status IN (
            'PENDING',
            'MATCHED',
            'UNMATCHED',
            'REVIEW_REQUIRED'
        )),

    CONSTRAINT chk_member_link_consistency
        CHECK (
            (match_status = 'MATCHED' AND member_id IS NOT NULL)
            OR
            (match_status <> 'MATCHED' AND member_id IS NULL)
        )
);

The Member ID is nullable because a member may not yet be identified.

If Member Service is a separate database, do not introduce a cross-database foreign key. Validate the member through the appropriate service or authoritative integration.

7.2 Indexes

CREATE INDEX idx_qml_status_created
ON quote_member_link(match_status, created_at);

CREATE INDEX idx_qml_member
ON quote_member_link(member_id)
WHERE member_id IS NOT NULL;

The unique Quote_ID constraint already creates an index for quote lookup.

7.3 MemberMatchingIndex table

CREATE TABLE member_matching_index (
    member_id VARCHAR(64) PRIMARY KEY,

    first_name_key VARCHAR(128),
    last_name_key VARCHAR(128),
    dob DATE,

    email_token BYTEA,
    phone_token BYTEA,
    national_id_token BYTEA,

    token_key_version VARCHAR(20),

    record_status VARCHAR(20) NOT NULL,
    source_version BIGINT NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

The tokens should be generated by an approved keyed cryptographic mechanism outside the SQL database.

Do not use unsalted plain hashes of predictable identifiers such as phone numbers, dates of birth or SSNs.

7.4 Matching indexes

CREATE INDEX idx_member_national_id
ON member_matching_index(national_id_token);

CREATE INDEX idx_member_dob_name
ON member_matching_index(
    dob,
    first_name_key,
    last_name_key
);

CREATE INDEX idx_member_email
ON member_matching_index(email_token);

CREATE INDEX idx_member_phone
ON member_matching_index(phone_token);

These indexes support equality lookups and candidate generation.

Actual query plans must be examined using representative data volumes and distribution.

The index should contain only the minimum attributes required for matching.

7.5 Transactional Outbox table

CREATE TABLE transactional_outbox (
    event_id UUID PRIMARY KEY,

    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,

    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL DEFAULT 1,

    payload JSONB NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,

    retry_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'NEW'
);

Indexes:

CREATE INDEX idx_outbox_pending
ON transactional_outbox(created_at)
WHERE status = 'NEW';

CREATE INDEX idx_outbox_aggregate
ON transactional_outbox(aggregate_type, aggregate_id);

The publisher should claim records in batches using appropriate locking, such as "FOR UPDATE SKIP LOCKED", to allow multiple publisher instances without processing the same row concurrently.

For high-volume production deployments, consider outbox partitioning and retention policies.

---

8. Event Specifications

Use a versioned event envelope, preferably following CloudEvents conventions.

All events must include:

- Event ID.
- Event type.
- Event version.
- Source service.
- Event timestamp.
- Aggregate ID.
- Correlation ID.
- Trace ID.
- Payload.

8.1 QuoteCreated event

{
  "specversion": "1.0",
  "id": "EVT10001",
  "type": "com.payer.quote.created.v1",
  "source": "/quote-service",
  "subject": "Q10001",
  "time": "2026-11-12T10:00:00Z",
  "data": {
    "quoteId": "Q10001",
    "linkId": "L10001",
    "matchingStatus": "PENDING",
    "matchingKeyVersion": "V1"
  }
}

The consumer retrieves the required applicant attributes from an authorized source or receives appropriately protected matching attributes through a secure event contract.

8.2 MemberCreated event

{
  "specversion": "1.0",
  "id": "EVT20001",
  "type": "com.payer.member.created.v1",
  "source": "/member-service",
  "subject": "M50001",
  "time": "2026-11-12T10:10:00Z",
  "data": {
    "memberId": "M50001",
    "memberVersion": 1,
    "matchingKeyVersion": "V1",
    "changeType": "CREATE"
  }
}

8.3 MemberUpdated event

{
  "specversion": "1.0",
  "id": "EVT20002",
  "type": "com.payer.member.updated.v1",
  "source": "/member-service",
  "subject": "M50001",
  "time": "2026-11-12T10:20:00Z",
  "data": {
    "memberId": "M50001",
    "memberVersion": 2,
    "matchingKeyVersion": "V1",
    "changeType": "DEMOGRAPHIC_UPDATE"
  }
}

8.4 Event publishing rules

1. Member Service publishes events only after its database transaction commits.
2. Member events should be generated through the Member Service's own transactional outbox.
3. Use Member ID as the partition key where appropriate to preserve ordering of member changes.
4. Use Quote ID as the key for quote matching events.
5. Consumers must support duplicate delivery.
6. Events must be versioned.
7. Schema evolution must be backward compatible where possible.
8. Avoid including raw PHI in event payloads.

---

9. Matching Engine Design

9.1 Matching strategy

Use deterministic matching as the first layer.

Fuzzy matching should be a secondary candidate-generation mechanism, not the primary automatic association mechanism.

Suggested hierarchy:

Priority| Rule| Decision
1| Verified existing Member ID| Exact
2| National ID token + DOB| Strong
3| First name + last name + DOB| Candidate
4| Email token + DOB| Candidate
5| Phone token + DOB| Candidate
6| Fuzzy name + DOB + corroborating attributes| Review or validated match

These are candidate rules, not universal identity-proofing standards. The payer must approve the combinations and required corroborating attributes.

9.2 Matching outcomes

Outcome| Condition| Action
MATCHED| One candidate satisfies approved high-confidence rules| Populate Member_ID
UNMATCHED| No candidate satisfies rules| Keep Member_ID NULL
REVIEW_REQUIRED| Multiple candidates or insufficient confidence| Keep Member_ID NULL
PENDING| Matching not completed| Keep Member_ID NULL

9.3 Confidence scoring

A possible initial scoring model:

Attribute| Illustrative weight
Verified national identifier| 50
Date of birth| 20
First name| 10
Last name| 10
Email| 5
Phone| 5
Total| 100

The scoring model must be calibrated against representative, appropriately governed payer data.

A score alone must never override mandatory identity checks.

For example:

