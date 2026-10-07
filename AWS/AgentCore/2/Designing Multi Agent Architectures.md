# Reference design for a healthcare payer multi-agent platform 
- A platform spanning ** Producer, Quote, Enrollment, Billing, and Servicing,** using **Amazon Web Services Amazon Bedrock AgentCore**.
- The key recommendation is to organize agents around bounded business capabilities, while keeping transactions, authorization, workflow state, and regulatory decisions deterministic outside the LLM.

## Compliance note:
- Amazon Bedrock AgentCore is listed as HIPAA eligible, but this does not automatically make the overall solution HIPAA compliant.
- We still need an AWS Business Associate Addendum, appropriate configuration, access controls, encryption, auditability, retention rules, and validation of every service that processes ePHI.

# 1. Recommended architectural style

Use a federated supervisor architecture rather than one large agent that has access to every healthcare system.
```
                    Digital Channels
       Producer Portal | Member Portal | CSR Desktop
       Mobile App | Contact Center | Partner APIs
                              |
                    API and Experience Layer
                              |
                 Identity and Consent Context
                              |
                  Journey Supervisor Agent
                              |
        +---------------------+----------------------+
        |                     |                      |
 Producer Supervisor    Member Journey         Servicing Supervisor
        |                Supervisor                    |
   +----+----+       +-------+--------+          +-----+------+
   |         |       |       |        |          |            |
Producer   Quote  Enrollment Billing  Payment   Policy      Case
Agent      Agent     Agent    Agent    Agent     Service     Agent
   |         |         |        |        |          |           |
   +---------+---------+--------+--------+----------+-----------+
                              |
                    AgentCore Gateway Layer
                              |
        +---------------------+-----------------------+
        |                     |                       |
   Domain APIs           Knowledge APIs       Workflow/Rule APIs
 CRM, Rating, PAS,      Product documents,    Rules engine,
 Enrollment, Billing,   SOPs, contracts,      human approval,
 Payments, Claims       regulatory content    durable workflow

```
The supervisor should:
- Identify the user and business persona.
- Classify the journey.
- build a minimum necessary context package.
- Delegate to the correct domain agent.
- Track the workflow, but not execute unrestricted transactions.
- Consolidate responses and explain next steps.
- Escalate to a human when confidence, policy, or risk thresholds are breached.

Do not allow agents to call each other arbitrarily. Inter-agent communication should follow an explicit graph with approved transitions.

# 2. Agent hierarchy
## 2.1 Experience-level agents
### Producer Journey Supervisor

Responsible for:
- Producer onboarding
- Appointment and licensing status
- Carrier and product eligibility
- Book-of-business questions
- Quote initiation
- Commission inquiry routing
- Producer servicing

It should delegate calculations and transactions to specialist agents rather than calculate rates or update producer records itself.

### Member Journey Supervisor

Responsible for:
- Quote-to-enrollment journeys
- Enrollment status
- Premium and payment questions
- ID-card and benefit questions
- Life-event servicing
- Complaint and appeal initiation

### CSR Assist Agent

A separate employee-facing agent that:
- Summarizes member history
- Recommends next-best actions
- Retrieves authorized procedures
- Drafts responses
- Requires CSR approval before material changes

This separation is useful because a CSR's permissions and available information differ materially from a member's.

## 2.2 Domain agents
| Domain agent           | Core responsibilities                                                      | Typical tools                                                        | High-risk actions                                         |
| ---------------------- | -------------------------------------------------------------------------- | -------------------------------------------------------------------- | --------------------------------------------------------- |
| Producer Agent         | Licensing, appointments, hierarchy, contracts, commissions                 | Producer management, CRM, licensing verification, commission APIs    | Changing appointments, banking or hierarchy               |
| Quote Agent            | Product discovery, census validation, quote preparation, comparison        | Product catalog, rating engine, provider network, formulary          | Producing a binding quote or misrepresenting coverage     |
| Enrollment Agent       | Application assistance, eligibility checks, evidence collection, status    | Enrollment platform, eligibility rules, document service, CRM        | Submitting enrollment or changing coverage                |
| Billing Agent          | Invoice explanation, balance, premium reconciliation, payment arrangements | Billing ledger, payment service, receivables, finance rules          | Refunds, payment-method changes, write-offs               |
| Servicing Agent        | Demographic updates, ID cards, dependents, life events, cases              | Policy administration, document generation, case management          | Termination, reinstatement, retroactive changes           |
| Compliance Agent       | Pre-action policy check, disclosure requirements, communication review     | Policy service, consent store, jurisdiction rules                    | Should advise or block, not execute business transactions |
| Document Agent         | Classifies documents and extracts validated structured fields              | OCR/document-processing service, malware scanning, schema validation | Must not independently commit extracted information       |
| Communication Agent    | Generates approved communications and channel-specific summaries           | Template service, notification APIs                                  | Sending communications without consent or approval        |
| Human Escalation Agent | Creates a structured case and routes it to the appropriate queue           | Case management, workflow, contact center                            | Must not invent the disposition                           |

## 3. How AgentCore capabilities map to the architecture

- Amazon Bedrock AgentCore provides modular production capabilities including **Runtime, Gateway, Memory, Identity, Browser, Code Interpreter, and Observability**.
- AWS positions it as framework- and model-flexible infrastructure, so the domain agents can be implemented using Strands, LangGraph, or another preferred orchestration framework without binding all business logic to one agent framework.

## AgentCore Runtime

Deploy each bounded domain agent as a separately versioned runtime:
- producer-supervisor-runtime
- quote-agent-runtime
- enrollment-agent-runtime
- billing-agent-runtime
- servicing-agent-runtime
- compliance-agent-runtime
- document-agent-runtime


Benefits:
- Independent scaling
- Separate deployment cycles
- Smaller tool exposure
- Failure isolation
- Domain-specific model selection
- Independent evaluation and rollback

For lower-risk conversational agents, a smaller, lower-cost model may be sufficient. Complex plan comparison or policy interpretation may require a more capable reasoning model. Keep model selection configurable rather than hard-coded.

## AgentCore Gateway

Make Gateway the only approved agent-to-enterprise integration path.

Register narrowly scoped tools such as:
- producer.getProfile
- producer.checkAppointment
- quote.validateCensus
- quote.calculate
- quote.comparePlans
- enrollment.validateApplication
- enrollment.submit
- billing.getInvoice
- billing.createPaymentArrangement
- servicing.requestIdCard
- servicing.updateAddress
- case.create
- approval.request


Gateway can **expose APIs, Lambda functions, and MCP servers to agents** at scale.
It also separates inbound authorization to the gateway from outbound credentials used to access downstream targets.

Avoid generic tools such as:
- executeSql
- callAnyApi
- updateMember
- runLambda


Those interfaces are too broad to govern safely.

## AgentCore Identity

Use Identity for both:
**Inbound identity**: who invoked the agent
**Outbound identity**: which downstream system the agent may access

AgentCore Identity is designed for agent workload identity, credential management, user-delegated access, third-party services, and audit trails.

Every request should carry a signed context such as:
```json
{
  "actorType": "PRODUCER",
  "actorId": "PRD-18472",
  "tenantId": "GROUP-9384",
  "memberId": null,
  "jurisdiction": "TN",
  "purposeOfUse": "QUOTE_CREATION",
  "delegationId": "DLG-22914",
  "correlationId": "9d31f34e-..."
}
```

Do not let the LLM construct or modify these security claims.

## AgentCore Policy
- Attach policy engines to the Gateway so that authorization is enforced outside agent prompts and code.
- AgentCore Policy evaluates agent-to-tool traffic before access, supports deterministic Cedar policies, and can evaluate identity claims and tool-input parameters.

Example conceptual controls:
```
Producer can quote only appointed products and authorized groups.
Member can view only their own billing information.
CSR can view members only within the assigned tenant.
Billing agent cannot issue a refund above a threshold.
Enrollment submission requires validated consent.
Coverage termination always requires human approval.
Payment tools cannot receive bank details in free-text parameters.
Quote agent cannot invoke enrollment submission directly.
An agent cannot invoke the same payment-changing operation twice in a session.
```

An illustrative Cedar-style rule:
```
permit (
    principal,
    action == Action::"billing.createPaymentArrangement",
    resource
)
when {
    principal.role == "CSR" &&
    context.memberConsent == true &&
    context.amount <= 500 &&
    context.caseId != ""
};
```

Treat this only as a design example. Production policies must align with the actual Gateway-generated Cedar schema and be validated through negative and boundary tests.

## AgentCore Memory

- Separate memory into distinct categories.

### Session memory

Suitable for:
```
Current journey
Previously answered questions
Collected non-sensitive fields
Outstanding tasks
Tool results needed within the active session
```

### Long-term preference memory

Potentially suitable for:
```
Preferred communication channel
Preferred language
Accessibility preferences
Producer's preferred quoting workflow

```

**Authoritative business state**

Not suitable for AgentCore conversational memory:
```
Enrollment status
Premium balance
Policy effective date
Coverage election
Payment status
Producer appointment
Consent evidence
```

These must remain in authoritative systems of record. Memory can hold references or summaries but must re-read critical facts before executing an action.

Use separate namespaces by:
```
tenant / actor-type / actor-id / journey-id / purpose-of-use
```

Do not create a universal cross-domain memory pool containing Producer, Enrollment, Billing, and Servicing histories.

## AgentCore Observability

- Supports CloudWatch-backed dashboards and traces for execution paths, tool calls, latency, session counts, token consumption, failures, and custom telemetry.
- Emits OpenTelemetry-compatible information that can integrate with existing monitoring platforms.

Capture:
```
Correlation and journey IDs
Agent and prompt version
Model ID
Retrieved-document identifiers
Tool requested
Policy decision
Approval reference
Latency and token usage
Hallucination or grounding score
Final disposition
```

Do not place raw PHI, payment details, secrets, access tokens, or full documents in trace attributes.

## Browser and Code Interpreter

- Use Browser only for approved external sites where APIs are unavailable, for example public regulatory or provider-directory verification.
- Browser-driven transactions should be a last resort because page changes and ambiguous UI state make them less deterministic.

Use Code Interpreter for isolated calculations or file transformation, but not as an alternative to a certified rating engine. For example:

- **Allowed**: analyze a sanitized group census and produce aggregate statistics.
- **Not allowed**: invent premium rates or make authoritative eligibility decisions.
- **Not allowed**: process unrestricted workbooks containing PHI without approved data controls.
- 
#  4. Domain interaction patterns

## Quote-to-enrollment journey

1. Producer asks to quote a group.
2. Producer Supervisor authenticates the producer context.
3. Producer Agent verifies license, appointment, market, and product authority.
4. Document Agent validates and structures the census.
5. Quote Agent validates census completeness.
6. Quote Agent invokes the deterministic rating engine.
7. Compliance Agent checks mandatory disclosures and jurisdiction rules.
8. Quote Agent produces a grounded comparison with quote identifiers.
9. Producer selects a quote.
10. Journey Supervisor starts an enrollment workflow.
11. Enrollment Agent validates consent and required evidence.
12. Human approval occurs where required.
13. Enrollment API commits the transaction idempotently.
14. Communication Agent sends an approved confirmation template.


The rate comes from the rating system. The LLM may explain the rate but must not generate it.

**Billing inquiry**
1. Member asks why the premium changed.
2. Billing Agent retrieves current and previous invoices.
3. It retrieves approved product/change reason codes.
4. It computes no financial values independently unless explicitly permitted.
5. It explains the differences using ledger and invoice evidence.
6. Any adjustment or refund is routed through approval and deterministic APIs.

**Life-event servicing**
1. Member reports marriage, birth, relocation, or loss of other coverage.
2. Servicing Agent identifies the event type.
3. Rules service calculates qualifying dates and evidence requirements.
4. Document Agent validates supporting evidence.
5. Compliance Agent checks consent and policy constraints.
6. Servicing Agent generates a proposed change.
7. Member or CSR reviews the structured change.
8. Policy administration API commits it.

## 5. Critical design principle: agents plan, workflows commit

A healthcare transaction should use three distinct layers:
```
**Agent reasoning:**
    Understands intent and proposes a plan

**Deterministic orchestration:**
    Executes workflow states, retries, deadlines and compensation

**System of record:**
    Validates and commits the transaction

```
Use durable workflow services for long-running business processes:
```
Producer appointment
Group quote
Enrollment
Payment arrangement
Reinstatement
Retroactive coverage adjustment
Complaint and appeal
```

The agent should never be the authoritative workflow database.

# 6. Human-in-the-loop control matrix

Require human approval when an action is:
```
Legally or financially binding
Irreversible
Based on ambiguous evidence
Outside a confidence threshold
An exception to standard policy
A retroactive coverage change
A policy termination or reinstatement
A high-value refund or write-off
A complaint, grievance, or appeal outcome
A potential adverse determination
A change to producer banking, commission, or appointment data
```

The approval screen should show:
```
Requested action
Before-and-after values
Evidence and source records
Agent rationale
Applicable policy
Confidence and validation results
Downstream systems affected
Rollback or compensation method
```

# 7. Data and knowledge architecture

Use separate retrieval collections by domain and audience:
```
/producer/contracts
/producer/commission-guides
/quote/product-rules
/quote/benefit-summaries
/enrollment/eligibility
/enrollment/evidence-requirements
/billing/payment-policies
/servicing/sops
/compliance/jurisdiction-rules
```

Each knowledge item should include:
```json
{
  "documentId": "PLAN-RULE-8831",
  "version": "2026.09",
  "effectiveFrom": "2026-01-01",
  "effectiveTo": "2026-12-31",
  "jurisdiction": "TN",
  "marketSegment": "SMALL_GROUP",
  "productId": "PPO-250",
  "audience": ["PRODUCER", "CSR"],
  "approvalStatus": "APPROVED",
  "classification": "CONFIDENTIAL",
  "sourceSystem": "PRODUCT_CATALOG"
}
```

- Retrieval must **filter by effective date, product, state, market, role, tenant, and approval status. **
- Semantic similarity alone is insufficient for insurance rules.
- For portability, define domain tools through OpenAPI or MCP-style contracts and keep the orchestration framework behind an internal abstraction.
-  This fits our  preference to avoid unnecessary platform or vendor lock-in while still using AgentCore as the AWS operational layer.

# 8. Reliability patterns
## Transaction controls

Every write tool should support:
```
Idempotency key
Optimistic concurrency/version check
Dry-run or preview mode
Validation-only**** mode
Explicit confirmation
Compensation method
Before-and-after audit snapshot
```
Example:
```json
{
  "operation": "servicing.updateAddress",
  "memberId": "MBR-12891",
  "expectedRecordVersion": 17,
  "idempotencyKey": "journey-7721-step-4",
  "mode": "PREVIEW",
  "proposedAddress": {
    "postalCode": "600045"
  }
}
```
Failure behavior
```
Gateway timeout: do not assume the transaction failed.
Unknown commit status: query by idempotency key before retrying.
Policy denial: return the policy-safe reason and escalation option.
Low retrieval confidence: ask for information or transfer to a human.
Agent timeout: resume from workflow state, not conversation reconstruction.
Model unavailable: use a fallback model only for approved capabilities.
Downstream outage: create a pending task without promising completion.
```

# 9. Security and compliance architecture

**Use:**
```
Separate AWS accounts for development, test, pre-production, and production
Private connectivity where available
VPC endpoints and PrivateLink for controlled network paths
Customer-managed encryption keys
Secrets Manager for downstream credentials
Tokenization or masking before model invocation
IAM least privilege
Attribute-based access control
CloudTrail and immutable audit retention
Data-loss prevention on input and output
Prompt-injection detection and untrusted-content isolation
Separate PHI and non-PHI logs
Regional data-residency controls
Explicit retention and deletion processes
Break-glass access with elevated audit controls
```

AgentCore GA includes production capabilities such as VPC, PrivateLink, CloudFormation, and tagging support, which are important for healthcare network isolation, infrastructure-as-code, inventory, and cost allocation.

Also maintain a strict distinction between:

**Authentication**: who is calling
**Authorization**: what they may do
**Consent**: whether they may act for this person and purpose
**Business** eligibility: whether the requested transaction is allowed
**Agent confidence**: whether the model understood the request

High model confidence cannot override any of the first four.

# 10. Evaluation scorecard

Evaluate every agent independently and as an end-to-end journey.
```
Dimension	Example metricIntent accuracy	Correct domain and sub-intent classification
Groundedness	Claims supported by approved evidence
Tool selection	Correct tool and parameter schema
Policy compliance	No unauthorized tool invocation
Transaction safety	No duplicate or unconfirmed write
PHI protection	No sensitive-data leakage
Business accuracy	Correct eligibility, dates, status, and amounts
Completeness	Required disclosures and next steps included
Escalation quality	Correctly identifies when human review is needed
Operational quality	Latency, errors, token use, retries
Fairness	Comparable outcomes across protected cohorts
Explainability	Decision and source trace available for review
```

Build scenario suites for:
```
Missing or contradictory member data
Prompt injection in uploaded documents
Cross-member data-access attempts
Expired producer appointment
Retroactive enrollment
Duplicate payment submission
Conflicting product documents
Jurisdiction mismatch
Downstream timeout after commit
Policy or benefit changes by effective date
11. Suggested implementation phases
Phase 1: Read-only assist
```

**Start with:**
```
Producer knowledge assistant
Quote explanation
Enrollment status
Billing explanation
Servicing procedure guidance
```

No direct business-system writes.

## Phase 2: Draft and preview

Add:
```
Quote-request preparation
Enrollment application prefill
Proposed member changes
Draft communication
Case creation
```
All material changes require review.

### Phase 3: Controlled transactions
**Enable low-risk transactions:**

```
Request an ID card
Update communication preference
Create a service case
Upload supporting documentation
```

Use Policy, consent, idempotency, and audit controls.

# Phase 4: Cross-domain journeys

## Implement:
```
Producer-to-quote
Quote-to-enrollment
Enrollment-to-first-bill
Life-event servicing
Billing-to-case-resolution
```

# Phase 5: Selective autonomy

Only automate a journey when:
```
Evaluation thresholds are sustained
Policy controls are externally enforced
Rollback or compensation exists
Regulatory and compliance owners approve
Production telemetry proves stable behavior
```

# 12. Final recommendation

The target architecture should have:
```
One experience supervisor per major persona, not one enterprise-wide super-agent.
One independently deployable agent per bounded domain capability.
AgentCore Gateway as the mandatory tool-control plane.
AgentCore Identity for user delegation and workload credentials.
AgentCore Policy for deterministic, externally enforced authorization.
AgentCore Memory only for conversational context and permitted preferences.
Systems of record as the authority for policies, enrollment, premium, payment, and producer status.
Durable workflows for long-running and compensatable transactions.
Human approval for adverse, financial, irreversible, or legally significant decisions.
AgentCore Observability plus domain audit events for end-to-end evidence.
OpenAPI/MCP contracts and framework abstraction to reduce lock-in.
A gradual path from read-only assistance to policy-controlled autonomy.
```

The most important architectural boundary is:

**Agents may interpret, plan, retrieve, explain, and propose. Deterministic systems must authorize, calculate, approve, and commit.**
