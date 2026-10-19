# Auditing and Compliance Architecture for an AWS Bedrock AgentCore Multi-Agent System

## Broker–Payer Domain

**Document purpose:** Define a production-grade auditing and compliance solution that provides complete traceability of broker interactions, agent actions, authorization decisions, searches, client selections, tool invocations, errors, and feedback in an AWS Bedrock AgentCore multi-agent system.

---

## 1. Objective

Provide complete, end-to-end traceability for every broker interaction and every action performed by the multi-agent system.

The solution must capture, at a minimum:

- Broker ID
- Session ID
- Conversation ID
- Interaction ID
- Timestamp
- Request type
- Intent
- Search identifier
- Authorization outcome
- Client or payer selection
- Agent actions and delegations
- Tool events
- Errors
- Broker feedback
- Correlation ID

### Acceptance criteria

1. **100% auditable interactions**
2. **100% authorization traceability**
3. **Correlation ID available for every failure**

For this document, **100% authorization traceability** means:

> Every authorization attempt, whether allowed, denied, failed, or timed out, has a traceable decision record linked to the originating broker interaction.

The principal design rule is:

> No broker request, agent delegation, authorization decision, or tool invocation may execute outside an instrumented and policy-enforced path.

---

## 2. Architectural Principles

### 2.1 Treat auditing as a compliance control plane

Auditing must not be implemented only as application logging. Establish a separate compliance control plane that receives structured and correlated events from:

- Broker ingress channels
- Authentication components
- Supervisor and specialist agents
- Agent-to-agent delegations
- Authorization services
- AgentCore Gateway
- Business tools and APIs
- Search services
- Client-selection workflows
- Error handlers
- Feedback workflows

### 2.2 Separate operational telemetry from compliance evidence

Use two complementary paths:

- **Operational telemetry:** Logs, metrics, and distributed traces used for monitoring and troubleshooting.
- **Compliance evidence:** Structured, immutable, retention-controlled audit events used for investigations and regulatory evidence.

Operational observability systems should not be the only system of record for regulatory evidence.

### 2.3 Enforce authorization outside the LLM

The LLM may identify an intent or propose a tool action, but it must not make the final authorization decision.

Authorization must be deterministic and based on trusted information such as:

- Authenticated broker identity
- Broker organization
- Book of business
- Payer or client relationship
- Broker appointment and licensing
- Permission or entitlement
- Geography or jurisdiction
- Requested action
- Data classification
- Session risk context

### 2.4 Generate correlation context before processing

Generate the root Correlation ID at the first trusted ingress point, before:

- Authentication
- Intent recognition
- Model invocation
- Agent orchestration
- Policy evaluation
- Tool execution

This ensures that authentication failures and other early failures are also traceable.

### 2.5 Fail closed for protected operations

A protected tool invocation must not proceed when:

- An authorization decision is unavailable
- The decision cannot be audited
- Identity context is missing
- Broker ID conflicts with authenticated identity
- Correlation data is malformed
- Required entitlement data is unavailable
- Policy evaluation times out

---

## 3. Target Architecture

```text
Broker Portal / Contact Center / API Consumer
                       |
                       | Broker JWT and request
                       v
           API Gateway / Enterprise API Layer
           + WAF + JWT validation + rate limits
                       |
                       | Create or validate:
                       | BrokerId
                       | SessionId
                       | ConversationId
                       | InteractionId
                       | CorrelationId
                       | W3C traceparent
                       v
             Audit-Aware Request Interceptor
               |                         |
               | INTERACTION_RECEIVED    | Invalid request or auth failure
               v                         v
       AgentCore Supervisor Runtime   Audit Event Pipeline
               |
               | Delegation context
               +-------------------------------+
               |               |               |
               v               v               v
         Broker Agent      Payer Agent     Search/Claims Agent
         AgentCore         AgentCore       AgentCore Runtime
         Runtime           Runtime
               \               |               /
                \              |              /
                 +------ AgentCore Gateway ---+
                              |
                    Request Lambda Interceptor
                              |
                     AgentCore Policy Engine
                     Deterministic authorization
                              |
                   ALLOW / DENY / ERROR decision
                              |
                   Approved MCP/API/Lambda tools
                              |
             CRM / Payer API / Search / Claims Database

Every component emits:
Logs + traces + metrics + structured compliance events
                              |
                  ADOT / OpenTelemetry Collector
                              |
               +--------------+----------------+
               |                               |
               v                               v
     Operational Observability          Compliance Event Bus
     CloudWatch/Splunk/Datadog          EventBridge or Kinesis
                                               |
                                   Validation and enrichment
                                               |
                                   +-----------+-----------+
                                   |                       |
                                   v                       v
                          Immutable S3 archive      Searchable audit store
                          Object Lock / WORM        OpenSearch / Splunk
                          KMS encryption            Athena / S3 Tables
                                   |
                          Compliance reporting,
                          reconciliation and evidence
```

---

## 4. AgentCore Component Responsibilities

## 4.1 AgentCore Runtime

Deploy the supervisor and sensitive domain agents as separate runtime resources when independent security, scaling, deployment, or failure boundaries are required.

Potential agents include:

- Supervisor or orchestration agent
- Broker-profile agent
- Payer or client-selection agent
- Search agent
- Claims agent
- Eligibility agent
- Compliance-review agent
- Feedback-processing agent

Each runtime must:

1. Accept and propagate the correlation context.
2. Create a child trace span for each internal action.
3. Record agent input and output metadata.
4. Record every delegation.
5. Emit a completion, denial, timeout, or failure event.
6. Access protected systems only through approved gateways and tools.
7. Include the agent name and deployed version in every relevant event.

Sensitive tools must not be directly reachable from an agent runtime when doing so would bypass policy enforcement or audit interception.

## 4.2 AgentCore Gateway

Use AgentCore Gateway as the mandatory access path for protected tools.

Example controlled tools:

- `search_broker`
- `search_payer`
- `get_broker_entitlements`
- `select_client`
- `get_client_summary`
- `get_claim_status`
- `get_member_eligibility`
- `get_commission_details`
- `create_service_request`
- `download_document`

### Request interceptor responsibilities

- Validate mandatory identifiers.
- Validate Broker ID against the authenticated principal.
- Reject caller-supplied identity overrides.
- Add human and workload identity context.
- Add agent and tool identity.
- Classify request sensitivity.
- Mask or tokenize protected fields.
- Generate a Tool Invocation ID.
- Emit `TOOL_AUTHORIZATION_REQUESTED`.

### Response interceptor responsibilities

- Capture outcome and latency.
- Record the number of records returned.
- Apply response filtering and masking.
- Detect unauthorized payer or client data.
- Record response classification.
- Emit `TOOL_COMPLETED`, `TOOL_DENIED`, or `TOOL_FAILED`.

## 4.3 AgentCore Identity

Use distinct identities for:

- Human broker
- Supervisor agent
- Each specialist agent
- Each gateway
- Each backend tool
- Audit-event publisher
- Audit-query and report users

Do not represent all agents through one shared role or generic service account.

The audit trail must distinguish the human principal from the workload principal:

```text
Human principal:       broker-12345
Workload principal:    payer-agent-prod-v3
Acting on behalf of:   broker-12345
```

The system must be able to answer:

> Which authenticated broker caused which agent workload to perform which operation against which payer or client resource?

## 4.4 AgentCore Policy

Use deterministic policy enforcement for agent-to-tool authorization.

A protected request should be evaluated using:

- Principal
- Action
- Resource
- Verified identity claims
- Request context
- Broker entitlements
- Client or payer relationship
- Session state
- Applicable restrictions

### Example policy intent

Permit `get_client_summary` only when:

1. The caller is an authenticated broker.
2. The requested Broker ID equals the authenticated Broker ID.
3. The selected client belongs to the broker's active book of business.
4. The broker has `CLIENT_READ` permission.
5. The broker-client relationship is currently valid.
6. The broker has the required regional appointment.
7. No compliance restriction blocks the request.

### Conceptual Cedar policy

```cedar
permit (
    principal is AgentCore::OAuthUser,
    action == AgentCore::Action::"PayerTools___get_client_summary",
    resource
)
when {
    principal has brokerId &&
    context.input.brokerId == principal.brokerId &&
    principal has permissions &&
    principal.permissions.contains("CLIENT_READ") &&
    context.entitlement.active == true &&
    context.entitlement.clientIds.contains(context.input.clientId)
};
```

Principal attributes must be obtained from verified identity claims or trusted enrichment services, not from model-generated tool arguments.

---

## 5. Canonical Correlation Model

Use separate identifiers because each identifier has a different lifecycle and purpose.

| Identifier | Scope | Example |
|---|---|---|
| `brokerId` | Authenticated human or business principal | `BRK-483921` |
| `sessionId` | Login or AgentCore session | `SES-01J...` |
| `conversationId` | Entire conversational thread | `CONV-01J...` |
| `interactionId` | One broker message and its terminal response | `INT-01J...` |
| `correlationId` | End-to-end business processing chain | `COR-01J...` |
| `traceId` | OpenTelemetry distributed trace | 32-character hexadecimal value |
| `spanId` | Individual distributed-tracing operation | 16-character hexadecimal value |
| `agentInvocationId` | One agent execution | `AINV-01J...` |
| `agentDelegationId` | Supervisor-to-specialist handoff | `DEL-01J...` |
| `toolInvocationId` | One tool invocation attempt | `TINV-01J...` |
| `authorizationDecisionId` | One authorization decision | `AUTH-01J...` |
| `searchId` | Search execution and result-selection lifecycle | `SRCH-01J...` |
| `errorId` | Unique failure occurrence | `ERR-01J...` |
| `feedbackId` | Broker feedback record | `FDBK-01J...` |

### Failure correlation rule

The Correlation ID must be generated before authentication, model invocation, or agent processing. Consequently, the following failures must also have a Correlation ID:

- Invalid or expired token
- Missing Broker ID
- Broker ID mismatch
- Policy denial
- Policy-engine failure
- Agent timeout
- Model throttling
- Search failure
- Gateway failure
- Tool failure
- Response-filtering failure
- Audit-pipeline failure

Return the identifier in the response header:

```http
X-Correlation-Id: COR-01JBCX7V4S93M2R1W8D6HZKQPT
```

Example safe failure response:

```json
{
  "status": "error",
  "errorCode": "AUTHZ_CLIENT_ACCESS_DENIED",
  "message": "You are not authorized to access the selected client.",
  "correlationId": "COR-01JBCX7V4S93M2R1W8D6HZKQPT"
}
```

Do not expose stack traces, policy internals, payer data, credentials, or detailed entitlement information to the broker.

---

## 6. Canonical Audit Event Schema

Use append-only audit events rather than repeatedly updating one large interaction record.

```json
{
  "schemaVersion": "1.0",
  "eventId": "EVT-01JBCYP98MJSX6D2QVA3RF8K07",
  "eventType": "AUTHORIZATION_DECIDED",
  "eventTime": "2026-10-16T09:52:41.381Z",
  "ingestedTime": "2026-10-16T09:52:41.527Z",

  "environment": "production",
  "awsAccountId": "123456789012",
  "awsRegion": "us-east-1",

  "broker": {
    "brokerId": "BRK-483921",
    "organizationId": "BROKER-ORG-1048",
    "authenticatedPrincipal": "broker-483921@broker.example",
    "channel": "BROKER_PORTAL"
  },

  "interaction": {
    "sessionId": "SES-01JBCY5M",
    "conversationId": "CONV-01JBCY61",
    "interactionId": "INT-01JBCYP2",
    "correlationId": "COR-01JBCYP4",
    "requestType": "CLIENT_LOOKUP",
    "intent": "VIEW_CLIENT_SUMMARY"
  },

  "trace": {
    "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
    "spanId": "00f067aa0ba902b7",
    "parentSpanId": "b7ad6b7169203331"
  },

  "agent": {
    "supervisorAgentId": "broker-supervisor",
    "agentId": "payer-agent",
    "agentVersion": "3.4.1",
    "agentInvocationId": "AINV-01JBCYP5",
    "delegationId": "DEL-01JBCYP6"
  },

  "authorization": {
    "decisionId": "AUTH-01JBCYP7",
    "outcome": "DENY",
    "policyEngineId": "broker-payer-policy-prod",
    "policyVersion": "2026.10.14-2",
    "reasonCode": "CLIENT_NOT_IN_BOOK_OF_BUSINESS",
    "principal": "BRK-483921",
    "action": "get_client_summary",
    "resource": "CLIENT-8bff7d21",
    "entitlementSnapshotId": "ENT-20261016-92841"
  },

  "search": {
    "searchId": "SRCH-01JBCYP8",
    "searchType": "CLIENT_SEARCH",
    "queryHash": "sha256:...",
    "resultCount": 4,
    "selectedClientId": "CLIENT-8bff7d21",
    "selectionMethod": "BROKER_SELECTED"
  },

  "tool": {
    "gatewayId": "payer-tools-prod",
    "toolName": "get_client_summary",
    "toolInvocationId": "TINV-01JBCYP9",
    "targetSystem": "PAYER_CLIENT_API",
    "outcome": "NOT_INVOKED"
  },

  "result": {
    "status": "DENIED",
    "durationMs": 47,
    "errorId": null,
    "errorCode": null
  },

  "dataGovernance": {
    "classification": ["CONFIDENTIAL", "BROKER_DATA"],
    "containsPii": false,
    "promptStored": false,
    "payloadReference": null,
    "retentionClass": "BROKER_AUDIT_7Y"
  },

  "integrity": {
    "producer": "agentcore-gateway-interceptor",
    "producerVersion": "2.1.0",
    "previousEventHash": "sha256:...",
    "eventHash": "sha256:..."
  }
}
```

---

## 7. Audit Event Taxonomy

## 7.1 Interaction events

```text
INTERACTION_RECEIVED
AUTHENTICATION_SUCCEEDED
AUTHENTICATION_FAILED
INTENT_CLASSIFIED
INTERACTION_COMPLETED
INTERACTION_DENIED
INTERACTION_FAILED
INTERACTION_ABANDONED
INTERACTION_TIMED_OUT
```

## 7.2 Multi-agent events

```text
AGENT_INVOCATION_STARTED
AGENT_DELEGATION_REQUESTED
AGENT_DELEGATION_ACCEPTED
AGENT_DELEGATION_REJECTED
AGENT_INVOCATION_COMPLETED
AGENT_INVOCATION_FAILED
```

## 7.3 Search and client-selection events

```text
SEARCH_REQUESTED
SEARCH_COMPLETED
SEARCH_FAILED
SEARCH_RESULTS_PRESENTED
CLIENT_SELECTED
CLIENT_SELECTION_REJECTED
```

A `CLIENT_SELECTED` event should capture:

- Search ID
- Selected client or payer ID
- Selection actor: broker, deterministic rule, or agent
- Result rank
- Selection timestamp
- Whether authorization was evaluated before data retrieval
- Authorization Decision ID

Avoid placing full search terms in the audit repository when they may contain personally identifiable or sensitive information. Prefer normalized tokens, hashes, data classifications, and controlled evidence references.

## 7.4 Authorization events

```text
AUTHORIZATION_REQUESTED
AUTHORIZATION_ALLOWED
AUTHORIZATION_DENIED
AUTHORIZATION_ERROR
AUTHORIZATION_TIMEOUT
```

An authorization denial is a successful policy evaluation with a `DENY` outcome. It is not necessarily a system error.

## 7.5 Tool events

```text
TOOL_INVOCATION_REQUESTED
TOOL_INVOCATION_AUTHORIZED
TOOL_INVOCATION_DENIED
TOOL_INVOCATION_STARTED
TOOL_INVOCATION_COMPLETED
TOOL_INVOCATION_FAILED
TOOL_RESPONSE_FILTERED
```

## 7.6 Feedback events

```text
FEEDBACK_SUBMITTED
FEEDBACK_CLASSIFIED
FEEDBACK_LINKED_TO_INTERACTION
FEEDBACK_REVIEWED
CORRECTIVE_ACTION_CREATED
```

---

## 8. Audit Data Pipeline

## 8.1 Operational telemetry path

```text
AgentCore Runtime / Gateway / Identity
                 |
                 v
        ADOT / OpenTelemetry
                 |
                 v
CloudWatch / Splunk / Datadog / OpenSearch
```

This path supports:

- Distributed tracing
- Operational logs
- Metrics and alerting
- Performance analysis
- Error investigation
- Agent execution analysis

## 8.2 Compliance evidence path

```text
Applications and interceptors
               |
               v
         Audit Event API
               |
               v
      EventBridge or Kinesis
               |
               v
Schema validation and enrichment
               |
               v
Deduplication by Event ID
               |
       +-------+--------+
       |                |
       v                v
Immutable S3 archive   Searchable audit index
Object Lock / WORM     OpenSearch / Splunk
KMS encryption         Athena / S3 Tables
```

For high-risk operations, the source component should receive an acknowledgement that the event has been accepted into the durable audit pipeline before completing the operation.

For lower-risk, read-only events, buffered publication may be used if the implementation includes:

- Local buffering
- Exponential retries
- Dead-letter queue
- Sequence numbering
- Delivery-lag monitoring
- Reconciliation
- Alerting for event loss or processing failure

## 8.3 Immutable retention

Use a dedicated security or compliance account for the authoritative audit archive where feasible.

Recommended controls:

- S3 versioning
- S3 Object Lock
- Compliance-mode retention
- Customer-managed KMS key
- Restrictive bucket policy
- Legal hold capability
- Cross-Region replication where required
- Lifecycle transition to archival storage
- Separate administrative roles
- Read-only auditor roles

Splunk, Datadog, OpenSearch, and CloudWatch can be used for search, dashboards, alerts, and investigations. The immutable archive should remain the authoritative evidence repository.

---

## 9. Data Minimization and Sensitive Content

Complete traceability does not require storing every prompt, model response, or backend payload in plain text.

### Always capture

- Human and workload identity references
- Interaction metadata
- Request type
- Normalized intent
- Agent and model version
- Agent delegation chain
- Tool identity
- Backend system identity
- Authorization request and outcome
- Policy version
- Search metadata
- Selected payer or client ID
- Timing and duration
- Error metadata
- Feedback metadata
- Input and output payload hashes
- Redaction status
- Evidence-object reference

### Capture conditionally

- Full broker prompts
- Full model responses
- Tool request and response bodies
- Search terms
- Retrieved documents
- Intermediate model outputs

Store conditional evidence only when legally and operationally justified. Apply:

- Redaction or tokenization
- Field-level encryption where necessary
- Separate retention classes
- Restricted break-glass access
- Access logging
- Purpose-based access control
- PII or PHI detection
- Regional data-residency controls

A safer audit representation is:

```text
payloadHash
payloadClassification
redactionApplied
restrictedEvidenceReference
```

Raw evidence should be stored separately with stronger authorization and retention controls.

---

## 10. Acceptance-Criteria Implementation

## 10.1 AC1: 100% auditable interactions

Implement a start-to-terminal event pairing rule.

Every `INTERACTION_RECEIVED` event must eventually have exactly one terminal event:

```text
INTERACTION_COMPLETED
INTERACTION_DENIED
INTERACTION_FAILED
INTERACTION_ABANDONED
INTERACTION_TIMED_OUT
```

Example reconciliation query:

```sql
SELECT interaction_id
FROM audit_events
GROUP BY interaction_id
HAVING
    SUM(
        CASE
            WHEN event_type = 'INTERACTION_RECEIVED' THEN 1
            ELSE 0
        END
    ) <> 1
 OR SUM(
        CASE
            WHEN event_type IN (
                'INTERACTION_COMPLETED',
                'INTERACTION_DENIED',
                'INTERACTION_FAILED',
                'INTERACTION_ABANDONED',
                'INTERACTION_TIMED_OUT'
            ) THEN 1
            ELSE 0
        END
    ) <> 1;
```

Measurement:

```text
Auditable Interaction Coverage =
Interactions with one valid start event and one terminal event
---------------------------------------------------------------- x 100
All accepted interaction requests
```

Required target: **100%**

## 10.2 AC2: 100% authorization traceability

Every protected tool attempt must have an authorization record.

```text
Authorization Traceability =
Protected tool attempts with an Authorization Decision ID
---------------------------------------------------------- x 100
All protected tool attempts
```

Required target: **100%**

Enforce this rule at runtime:

```text
No Authorization Decision ID -> No tool execution
```

Gateway interception must reject an invocation when:

- The policy service is unavailable.
- The authorization decision cannot be recorded.
- Identity context is missing.
- Broker ID conflicts with the authenticated identity.
- Correlation context is missing or malformed.
- Entitlement information is unavailable for a protected operation.

## 10.3 AC3: Correlation ID for every failure

Test every failure path, including:

- Entry and validation failure
- Authentication failure
- Supervisor failure
- Specialist-agent failure
- Model failure
- Policy denial
- Policy-engine error
- Gateway timeout
- Tool timeout
- Downstream HTTP 4xx or 5xx response
- Audit-pipeline failure
- Response-filter failure

Every error event and every client-safe error response must contain the same root Correlation ID.

```text
Failure Correlation Coverage =
Failures with a valid Correlation ID
------------------------------------ x 100
All observed failures
```

Required target: **100%**

---

## 11. Controls Required to Support a 100% Claim

Observability alone cannot prove complete coverage. Both preventive and detective controls are required.

## 11.1 Preventive controls

1. **Single ingress path**  
   Block direct production invocation except through approved ingress components.

2. **Single protected tool path**  
   Agents cannot directly access payer APIs, databases, or Lambda functions.

3. **Fail-closed authorization**  
   Missing, failed, or unauditable authorization means no invocation.

4. **Mandatory correlation contract**  
   Reject production requests that do not contain valid required identifiers after ingress enrichment.

5. **Trusted identity derivation**  
   Obtain Broker ID from verified identity claims rather than prompt text.

6. **Least-privilege workload identities**  
   Assign distinct permissions to each agent, gateway, tool, and event publisher.

7. **Controlled egress**  
   Prevent unmonitored outbound access from agent runtimes.

8. **Policy versioning and approval**  
   Deploy authorization policies through controlled CI/CD, peer review, and automated testing.

9. **Immutable retention**  
   Prevent alteration or deletion of audit evidence during the required retention period.

10. **Separation of duties**  
    Application developers must not be able to delete production audit evidence. Auditors should have purpose-limited, read-only access.

## 11.2 Detective controls

- Start-versus-terminal event reconciliation
- Tool-call-versus-authorization reconciliation
- Runtime-trace-versus-audit-event reconciliation
- CloudTrail-versus-deployment-inventory reconciliation
- Sequence-gap detection
- Duplicate Event ID detection
- Stale audit-pipeline alerts
- Dead-letter queue alerts
- Schema-validation failure alerts
- Clock-skew detection
- Unauthorized audit-query alerts
- Policy-denial anomaly monitoring
- Break-glass access review
- Unexpected agent-to-tool relationship detection

CloudTrail should provide AWS control-plane and applicable data-event evidence. Business audit events must provide domain semantics such as intent, broker-client relationship, search selection, tool purpose, authorization reason, and feedback.

---

## 12. Recommended Dashboards

## 12.1 Compliance dashboard

- Auditable interaction coverage
- Authorization traceability percentage
- Failure correlation coverage
- Missing terminal events
- Missing authorization decisions
- Schema validation failures
- Audit ingestion lag
- Dead-letter queue count
- Policy denials by broker, client, agent, and tool
- Break-glass access
- Evidence export activity

## 12.2 Security dashboard

- Broker ID and token-subject mismatch
- Cross-payer access attempts
- Repeated denied client selections
- Agent calling an unexpected tool
- Agent invocation without a human principal
- Sudden policy-denial spikes
- Sensitive response-filtering actions
- Prompt-injection detections
- Workload-identity anomalies

## 12.3 Operational dashboard

- Agent latency
- Tool latency
- Model latency and errors
- Supervisor-to-agent handoffs
- Search failures
- Gateway errors
- Policy-evaluation latency
- Audit-pipeline latency
- Runtime and downstream dependency availability

---

## 13. Implementation Sequence

## Phase 1: Correlation foundation

- Define the identifier lifecycle.
- Implement correlation middleware at ingress.
- Propagate W3C `traceparent` context.
- Return the Correlation ID in every response.
- Define the canonical event schema.
- Define and govern the event taxonomy.

## Phase 2: Agent instrumentation

- Instrument the supervisor and specialist agents.
- Add spans for classification, delegation, model invocation, retrieval, tool calls, and response composition.
- Standardize agent and model version tags.
- Validate parent-child trace relationships.
- Add terminal events for success, denial, timeout, abandonment, and failure.

## Phase 3: Tool authorization

- Place protected tools behind AgentCore Gateway.
- Implement request and response interceptors.
- Add deterministic authorization policies.
- Record the Authorization Decision ID.
- Record the policy version and reason code.
- Remove direct backend access from agent roles.

## Phase 4: Immutable audit pipeline

- Publish structured events through EventBridge or Kinesis.
- Add schema validation.
- Add idempotent deduplication.
- Configure retries and a dead-letter queue.
- Archive evidence in an Object-Locked S3 bucket.
- Create searchable indexes and query views.
- Implement retention classes and legal holds.

## Phase 5: Compliance validation

Run automated tests that deliberately cause:

- Authentication failure
- Authorization denial
- Policy timeout
- Agent crash
- Gateway failure
- Tool timeout
- Audit-pipeline delay
- Duplicate delivery
- Invalid schema
- Partial interaction
- Response-filtering failure

For every scenario, verify the presence of:

```text
Broker or principal identity
Correlation ID
Timestamp
Agent execution chain
Authorization outcome
Tool outcome
Error or terminal event
Immutable evidence location
```

---

## 14. Nonfunctional Requirements

### Reliability

- Audit-event durability must meet or exceed the business transaction's durability requirement.
- Events must support at-least-once delivery with idempotent processing.
- High-risk actions should require durable event acceptance before completion.

### Security

- Encrypt audit data in transit and at rest.
- Apply least-privilege access.
- Separate operational and compliance administration.
- Log all access to restricted audit evidence.

### Performance

- Audit controls should add bounded latency.
- Policy-evaluation and event-publication latency must be monitored separately.
- Buffered publication may be used only for approved low-risk pathways.

### Scalability

- Partition events using an appropriate key such as Broker ID, Conversation ID, or Correlation ID.
- Avoid hot partitions caused by generic or constant partition keys.
- Scale ingestion, validation, indexing, and archival independently.

### Data governance

- Define retention by event and evidence class.
- Minimize sensitive payload retention.
- Support legal hold and defensible deletion.
- Maintain a versioned schema registry.

---

## 15. Final Recommendation

Use the following division of responsibility:

- **AgentCore Runtime:** Agent execution and session isolation
- **AgentCore Identity:** Human and workload identity
- **AgentCore Gateway:** Mandatory controlled route to protected tools
- **AgentCore Policy:** Deterministic tool authorization
- **Gateway interceptors:** Validation, context enrichment, redaction, and audit-event emission
- **AgentCore observability and ADOT:** Distributed traces, logs, metrics, and execution visibility
- **CloudTrail:** AWS control-plane and applicable data-event evidence
- **EventBridge or Kinesis:** Durable compliance-event transport
- **S3 Object Lock:** Immutable authoritative audit record
- **Splunk, Datadog, or OpenSearch:** Investigation, dashboards, alerts, and operational search
- **Athena or S3 Tables:** Long-term reconciliation and compliance reporting

The target compliance assertion is:

> Every broker interaction starts with a trusted correlation context; every agent action is represented as a traceable child operation; every protected tool attempt passes through deterministic authorization; and every interaction terminates in an immutable, reconcilable audit record.

---

## 16. Acceptance-Criteria Traceability Matrix

| Acceptance criterion | Primary control | Supporting evidence | Measurement |
|---|---|---|---|
| 100% auditable interactions | Mandatory start and terminal events | Immutable audit events and trace linkage | Start-terminal reconciliation |
| 100% authorization traceability | Gateway-enforced authorization | Authorization Decision ID, policy version, outcome, reason | Protected tool versus authorization reconciliation |
| Correlation ID for failures | Correlation created at trusted ingress | Error event, trace, log, and client-safe response | Failure correlation coverage |
| Traceable agent actions | Per-agent invocation and delegation events | Agent Invocation ID, Delegation ID, trace and span IDs | Trace-versus-audit reconciliation |
| Traceable client selection | Search and client-selection events | Search ID, selected client, actor, rank, authorization decision | Selection-to-authorization reconciliation |
| Traceable tool access | Mandatory protected gateway path | Tool Invocation ID and terminal tool event | Requested-versus-terminal tool reconciliation |
| Tamper-resistant evidence | Immutable compliance archive | Object retention metadata and evidence hash | Archive integrity and retention checks |
