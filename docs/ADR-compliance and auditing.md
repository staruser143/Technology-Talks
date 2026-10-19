# ADR-001: Centralized Auditing and Compliance Control Plane for the Broker-Payer Multi-Agent System

- **Status:** Proposed
- **Date:** 2026-10-19
- **Decision owners:** Enterprise Architecture, Security Architecture, Compliance, and Broker-Payer Product Engineering
- **Technical scope:** AWS Bedrock AgentCore multi-agent platform
- **Business domain:** Broker-Payer
- **Related solution:** Auditing and Compliance Architecture for an AWS Bedrock AgentCore Multi-Agent System

## Context

A broker request can cross authentication, supervisor and specialist agents, searches, client selection, authorization, model invocations, tools, and payer systems. Application logs alone do not reliably prove who initiated an action, which agent acted on the broker's behalf, what policy was evaluated, which client was selected, or whether immutable evidence is complete.

The required outcomes are:

1. **100% of accepted interactions are auditable.**
2. **100% of protected authorization attempts are traceable.**
3. **100% of observed failures contain a root Correlation ID.**

Authorization traceability includes `ALLOW`, `DENY`, `ERROR`, and `TIMEOUT` outcomes.

## Decision Drivers

- End-to-end traceability across broker, agent, policy, gateway, tool, and backend boundaries.
- Deterministic authorization outside the LLM.
- Prevention of unmonitored access to protected broker-payer systems.
- Tamper-resistant, retention-controlled evidence.
- Separation of operational telemetry from authoritative compliance evidence.
- Data minimization for broker, payer, client, PII, and regulated information.
- Support for investigations, attestations, legal holds, and evidence exports.
- Compatibility with CloudWatch, Splunk, Datadog, OpenSearch, and Athena.
- Measurable proof of the required coverage levels.

## Decision

Implement a **centralized, append-only auditing and compliance control plane** for the AWS Bedrock AgentCore multi-agent platform.

### 1. Use two complementary evidence paths

#### Operational telemetry

Instrument ingress, agents, gateways, policies, and tools with OpenTelemetry-compatible logs, metrics, and traces. Route them to approved monitoring platforms such as CloudWatch, Splunk, or Datadog.

#### Compliance evidence

Publish canonical audit events through a durable pipeline:

```text
Ingress, agents, policies, gateways, and tools
                    |
                    v
             Audit Event API
                    |
                    v
         EventBridge or Kinesis
                    |
                    v
 Validation, enrichment, deduplication
                    |
         +----------+----------+
         |                     |
         v                     v
Immutable S3 archive    Searchable audit index
Object Lock / WORM      Splunk / OpenSearch
KMS encryption          Athena / S3 Tables
```

The immutable S3 archive is the authoritative compliance record. Monitoring products are investigation and operational interfaces, not the sole system of record.

### 2. Establish correlation context at trusted ingress

Before authentication, model invocation, orchestration, authorization, or tool execution, create or validate:

- `sessionId`
- `conversationId`
- `interactionId`
- `correlationId`
- W3C `traceparent`
- `brokerId`, after identity verification

The same root Correlation ID must be propagated through logs, traces, audit events, authorization decisions, agent delegations, tool events, errors, and client-safe error responses.

### 3. Use purpose-specific identifiers

| Identifier | Purpose |
|---|---|
| `brokerId` | Authenticated human or business principal |
| `sessionId` | Login or runtime session |
| `conversationId` | Full conversation |
| `interactionId` | One request and terminal response |
| `correlationId` | End-to-end processing chain |
| `traceId` / `spanId` | Distributed tracing |
| `agentInvocationId` | One agent execution |
| `agentDelegationId` | One agent handoff |
| `authorizationDecisionId` | One authorization evaluation |
| `toolInvocationId` | One tool attempt |
| `searchId` | Search and result-selection lifecycle |
| `errorId` | One failure occurrence |
| `feedbackId` | One feedback record |

### 4. Make AgentCore Gateway the mandatory protected-tool path

Agents must not directly call protected payer, claims, eligibility, CRM, commission, document, or service-request systems.

Gateway request interceptors will:

- Validate correlation fields.
- Match Broker ID against the verified principal.
- Reject prompt- or tool-supplied identity overrides.
- Add human and workload identities.
- Generate Tool Invocation IDs.
- Invoke deterministic authorization.
- Emit authorization and tool-request events.

Response interceptors will:

- Capture outcomes and latency.
- Record result counts and data classification.
- Apply masking and output filtering.
- Emit terminal tool events.

### 5. Keep authorization deterministic and external to the LLM

The LLM may propose an action but cannot grant access. AgentCore Policy or an approved deterministic policy service will evaluate verified context including:

- Human and workload principal
- Action and target resource
- Broker permissions and book of business
- Payer or client relationship
- Licensing, appointment, and jurisdiction
- Data classification and session risk
- Compliance restrictions

The policy path will fail closed for protected operations.

```text
No Authorization Decision ID -> No protected tool execution
```

Every authorization record will include the decision identifier, outcome, policy identifier and version, principal, action, resource reference, reason code, entitlement snapshot reference, correlation identifiers, timestamp, and duration.

### 6. Preserve human and workload identity

The audit trail must distinguish:

```text
Human principal:       broker-12345
Workload principal:    payer-agent-prod-v3
Acting on behalf of:   broker-12345
```

Distinct least-privilege identities will be used for supervisor agents, specialist agents, gateways, tools, event publishers, processors, investigators, and evidence administrators.

### 7. Emit append-only lifecycle events

Required event families include interaction, authentication, intent, agent invocation, delegation, search, client selection, authorization, tool execution, response filtering, errors, timeouts, feedback, and corrective actions.

Every `INTERACTION_RECEIVED` event must have exactly one terminal event:

- `INTERACTION_COMPLETED`
- `INTERACTION_DENIED`
- `INTERACTION_FAILED`
- `INTERACTION_ABANDONED`
- `INTERACTION_TIMED_OUT`

Every protected tool request must have an authorization record and a terminal tool outcome, including when execution is denied or never starts.

### 8. Store authoritative evidence immutably

The compliance archive will use S3 with:

- Versioning
- Object Lock
- Compliance-mode retention where required
- Customer-managed KMS encryption
- Restrictive bucket and key policies
- Legal hold capability
- Lifecycle and archival controls
- Cross-Region replication where justified
- Separation of duties
- Purpose-limited, read-only auditor access

A dedicated security or compliance account is preferred.

### 9. Minimize sensitive content

Standard events will retain metadata such as payload hash, classification, redaction status, PII or PHI indicator, retention class, and restricted evidence reference.

Full prompts, responses, search terms, retrieved documents, and tool payloads will be stored only when justified, under separate retention and access controls.

### 10. Prove coverage continuously

#### Auditable interaction coverage

```text
Interactions with one valid start event and one terminal event
---------------------------------------------------------------- x 100
All accepted interactions
```

Target: **100%**

#### Authorization traceability

```text
Protected tool attempts with an Authorization Decision ID
---------------------------------------------------------- x 100
All protected tool attempts
```

Target: **100%**

#### Failure correlation coverage

```text
Observed failures with a valid root Correlation ID
-------------------------------------------------- x 100
All observed failures
```

Target: **100%**

Any gap is a compliance exception and must create an alert, investigation record, and remediation workflow.

## Alternatives Considered

### Application logs only

Rejected because logs may be sampled, dropped, mutated, inconsistently structured, or controlled by application teams. They cannot independently prove lifecycle completeness or immutable retention.

### AgentCore observability and CloudWatch only

Rejected as the complete solution because operational telemetry does not inherently capture all business semantics, including normalized intent, client selection, entitlement snapshot, authorization reason, and feedback lifecycle. It remains part of the selected operational path.

### CloudTrail only

Rejected as the complete solution because infrastructure API evidence cannot replace domain events for interaction lifecycle, intent, searches, agent delegation, selected client, or broker feedback. It remains supporting evidence.

### Splunk or Datadog as the sole evidence store

Rejected because operational indexes should not be the only immutable regulatory record, and doing so creates unnecessary vendor dependence. They remain approved search and monitoring destinations.

### Direct agent access to backends

Rejected because distributed tool-level authorization and auditing are harder to standardize, easier to bypass, and difficult to reconcile to 100% coverage.

### Retain every prompt and response

Rejected as the default because it increases privacy, discovery, storage, and security exposure. Conditional retention remains available under an approved policy.

## Consequences

### Positive

- End-to-end traceability across human and agent actions.
- Deterministic, auditable authorization.
- Defensible measurement of all three acceptance criteria.
- Immutable and retention-controlled evidence.
- Monitoring-tool portability through OpenTelemetry and canonical event contracts.
- Faster investigations through common correlation identifiers.
- Lower risk of unauthorized direct backend access.
- Reusable compliance pattern for other multi-agent domains.

### Negative and trade-offs

- Additional latency from interception, policy evaluation, and durable publication.
- Increased platform and operational complexity.
- More IAM identities, schemas, reason codes, and retention classes to govern.
- Storage and processing cost.
- Need to handle duplicate and out-of-order events.
- Fail-closed behavior can reduce availability during control-plane outages.
- Shared ownership across product, platform, security, SRE, and compliance teams.

### Risk mitigations

| Risk | Mitigation |
|---|---|
| Added latency | Measure policy and publication latency; buffer only approved low-risk events |
| Pipeline outage | Multi-AZ services, retries, DLQs, and fail-closed handling for high-risk actions |
| Duplicate events | Globally unique Event ID and idempotent consumers |
| Out-of-order delivery | Event time, ingestion time, sequence information, and lifecycle reconciliation |
| Sensitive-data leakage | Allow-listed schema, redaction, tokenization, classification, and restricted references |
| Missing evidence | Start-terminal, tool-authorization, and trace-audit reconciliation |
| Unauthorized access | Separate accounts, least privilege, KMS controls, access logs, and break-glass reviews |
| Vendor lock-in | OpenTelemetry, canonical events, replaceable indexes, and S3 as the authoritative store |

## Implementation Requirements

- At-least-once event delivery with idempotent consumption.
- Schema registry and versioned canonical audit events.
- Dead-letter handling and alerts for invalid events.
- Encryption in transit and at rest.
- Broker ID derived only from verified identity claims.
- No direct access to protected backends from agent roles.
- Audit-evidence access and export operations must themselves be logged.
- Retention, legal hold, residency, and defensible-deletion policies must be defined.
- Reconciliation results must be retained as compliance evidence.

## Validation

Automated tests must cover:

- Successful interaction
- Invalid request and authentication failure
- Broker ID mismatch
- Authorization allow, deny, error, and timeout
- Supervisor and specialist-agent failures
- Agent delegation rejection
- Model failure or throttling
- Search and client-selection failure
- Gateway and tool failure
- Downstream 4xx and 5xx outcomes
- Response filtering
- Duplicate and invalid audit events
- Pipeline delays and dead-letter routing
- Abandoned interactions and missing terminal events

Each test must prove that the root Correlation ID exists, human and workload identities are distinguishable, the agent chain is reconstructable, protected actions have authorization evidence, the interaction has exactly one terminal state, and immutable evidence is discoverable.

## Ownership

| Area | Accountable owner |
|---|---|
| Correlation contract | Platform Architecture |
| Agent instrumentation | Agent Engineering teams |
| Gateway and protected tools | Integration Platform team |
| Authorization policies | Security Architecture and entitlement owners |
| Identity | Identity and Access Management team |
| Audit schema | Enterprise Architecture and Compliance |
| Event pipeline | Cloud Platform team |
| Immutable archive | Security Operations / Compliance Technology |
| Dashboards and alerts | SRE / Observability team |
| Retention and legal hold | Data Governance and Legal/Compliance |
| Reconciliation | Compliance Engineering |

## Rollout Plan

1. **Correlation foundation:** Define identifiers, ingress middleware, W3C tracing, and canonical schemas.
2. **Agent instrumentation:** Add invocation, delegation, model, retrieval, and terminal events.
3. **Protected tool enforcement:** Route tools through Gateway, deploy interceptors and policy, and remove direct access.
4. **Compliance event platform:** Deploy transport, validation, deduplication, DLQs, immutable storage, and searchable indexes.
5. **Coverage proof:** Implement reconciliation, dashboards, failure injection, and compliance evidence reports.

## Revisit Conditions

Review this ADR when:

- AgentCore capabilities materially change.
- Regulatory or contractual retention requirements change.
- A different enterprise policy engine is adopted.
- High-risk write or financial transactions are introduced.
- Multi-region active-active operation becomes necessary.
- Audit volume, latency, or cost exceeds agreed thresholds.
- The implementation fails to maintain coverage targets.
- New consent, delegation, residency, or identity requirements apply.

## Final Decision Statement

The broker-payer multi-agent platform will use a centralized, append-only auditing and compliance control plane. Correlation context will be established at trusted ingress, protected tools will be accessible only through a policy-enforced AgentCore Gateway path, authorization will be deterministic and external to the LLM, and authoritative evidence will be stored in an immutable S3 archive.

The decision is considered implemented only when the platform continuously demonstrates:

- One valid start event and one terminal event for every accepted interaction.
- An Authorization Decision ID for every protected tool attempt.
- A root Correlation ID for every observed failure.
