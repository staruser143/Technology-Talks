# ADR: Enterprise Metrics and Observability Architecture for AWS Bedrock AgentCore Multi-Agent Platform

**Status:** Proposed  
**Date:** 19 October 2026  
**Decision Owners:** Enterprise Architecture / AI Platform / Cloud Platform / SRE / Security & Compliance  
**Scope:** Healthcare Insurance Multi-Agent Platform  
**Domains:** Producer, Quote, Enrollment, Billing, Servicing

---

## 1. Context

The healthcare insurance platform uses a multi-agent architecture implemented using **LangGraph deployed on Amazon Bedrock AgentCore Runtime**, with domain-specific agents supporting:

- Producer
- Quote
- Enrollment
- Billing
- Servicing

Cross-cutting AgentCore capabilities include:

- AgentCore Runtime
- AgentCore Gateway
- AgentCore Identity
- AgentCore Policy
- AgentCore Observability
- AgentCore Memory where appropriate

The platform requires enterprise-grade monitoring across three primary metric categories:

```text
Business Metrics
     +
Adoption Metrics
     +
Operational / Technical Metrics
```

Production AWS Console access is restricted. Therefore, production support, SRE, operations, compliance, and business stakeholders must not depend on CloudWatch Console access for day-to-day monitoring.

AgentCore Observability provides native telemetry capabilities including agent execution traces, session counts, latency, duration, token usage, errors, custom metrics, logs, and spans. AgentCore emits OpenTelemetry-compatible telemetry, allowing integration with enterprise observability platforms. AgentCore-native telemetry is stored in Amazon CloudWatch by default.

A production observability strategy is required that:

1. Preserves AgentCore-native observability capabilities.
2. Does not require interactive AWS Console access.
3. Integrates with enterprise observability platforms such as Splunk or Datadog.
4. Captures business, adoption, AI quality, security, and operational metrics.
5. Provides traceability across multi-agent workflows.
6. Protects PHI, PII, credentials, and other sensitive information.
7. Provides appropriate views for executives, business teams, operations, SRE, AI engineering, security, and compliance.

---

## 2. Decision Drivers

### 2.1 Business

- Measure business value delivered by AI agents.
- Calculate quote, enrollment, billing, and servicing outcomes.
- Measure productivity improvements and automation rates.
- Support cost-benefit and ROI analysis.

### 2.2 Adoption

- Measure usage by producers, members, and CSRs.
- Understand which agents and capabilities are being adopted.
- Measure repeat usage, abandonment, acceptance, and escalation.

### 2.3 Operations

- End-to-end agent tracing.
- Tool-call tracing.
- Model and agent latency.
- Token consumption.
- Error, retry, and throttle monitoring.
- SLO and SLA monitoring.
- Cost monitoring.

### 2.4 AI Quality

- Groundedness.
- Response correctness.
- Tool-selection accuracy.
- Task completion.
- Escalation correctness.
- Policy adherence.

AgentCore Evaluations may use agent telemetry to reconstruct and score sessions. Native AgentCore evaluation capabilities require compatible telemetry in CloudWatch and CloudWatch Transaction Search.

### 2.5 Security and Compliance

- Protect PHI and PII.
- Prevent sensitive data from entering telemetry unnecessarily.
- Provide immutable and searchable audit trails.
- Track policy denials and authorization failures.
- Track human approvals and overrides.
- Support incident investigations and regulatory audits.
- Apply healthcare-specific data retention and access rules.

### 2.6 Enterprise Operations

- No dependency on AWS Console access for production operations.
- Integration with Splunk and/or Datadog.
- Cross-application and cross-domain correlation.
- Centralized enterprise dashboards and alert management.
- Portability through OpenTelemetry standards.

---

## 3. Decision

### Adopt a hybrid, OpenTelemetry-based observability architecture

The platform will use:

> **AgentCore Observability and CloudWatch as the AWS-native telemetry foundation, OpenTelemetry/ADOT as the standardized instrumentation and correlation layer, and the organization's approved enterprise platform such as Splunk or Datadog as the primary operational visualization, alerting, and investigation interface.**

Business and adoption metrics will additionally be generated from authoritative domain events rather than inferred exclusively from agent telemetry.

### 3.1 Logical Architecture

```text
                           Healthcare Channels
                                  |
                                  v

                         LangGraph Supervisor
                                  |
              +-------------------+-------------------+
              |          |        |        |          |
          Producer     Quote  Enrollment Billing  Servicing
            Agent      Agent     Agent     Agent     Agent

              |          |        |        |          |
              +-------------------+-------------------+
                                  |
                                  v
                         AgentCore Runtime
                                  |
                    +-------------+-------------+
                    |                           |
                    v                           v
          AgentCore Observability      Application Telemetry
                                            OTEL / ADOT
                    |                           |
                    +-------------+-------------+
                                  |
                                  v
                         CloudWatch / OTEL
                        Telemetry Foundation
                                  |
                    +-------------+-------------+
                    |                           |
                    v                           v
               Splunk                      Datadog
          Logging / Security /          APM / Agent Monitoring /
          Audit / Compliance             SRE / Operations
```

### 3.2 Business Telemetry Path

```text
Producer System ----+
Quote System -------+
Enrollment System --+
Billing System -----+----> Business Events
Servicing System ---+           |
                                v
                       Enterprise Event Bus
                                |
                                v
                       Enterprise Data Platform
                                |
                     +----------+----------+
                     |                     |
                  Tableau              Power BI
                     |                     |
                     +----------+----------+
                                |
                         Executive KPIs
```

---

## 4. Metrics Classification

A common enterprise telemetry schema will classify metrics into five areas:

1. Operational metrics
2. Adoption metrics
3. Business metrics
4. AI quality metrics
5. Security and compliance metrics

---

## 5. Operational Metrics

Operational metrics will be captured primarily through AgentCore Observability, OpenTelemetry, ADOT, CloudWatch, and the selected enterprise observability platform.

| Metric | Primary dimensions |
|---|---|
| Agent invocations | Agent, domain, environment |
| Sessions | Agent, domain, channel |
| P50/P95/P99 latency | Agent, model, tool, journey |
| Error rate | Agent, tool, error category |
| Throttles | Runtime, model, API |
| Tool invocation latency | Tool, domain, target system |
| Tool failure rate | Tool, operation, dependency |
| Input and output tokens | Agent, model, prompt version |
| Model invocation latency | Model, inference profile |
| Gateway failures | Gateway, target, policy outcome |
| Policy denials | Policy, tool, actor type |
| Memory retrieval latency | Memory namespace, agent |
| Human approval latency | Journey, action, approval queue |

---

## 6. Adoption Metrics

Adoption metrics will be generated using application and domain events rather than relying solely on AgentCore runtime telemetry.

### 6.1 Producer Adoption

- Monthly active producers
- Percentage of eligible producers using AI
- Quotes initiated through AI
- AI-assisted quote completion rate
- Repeat usage by producer
- AI recommendation acceptance rate

```text
Producer AI Adoption %

Monthly Producers Using AI
-------------------------- x 100
Eligible Producers
```

### 6.2 CSR Adoption

- Active CSR users
- Sessions per CSR
- Suggested actions accepted
- Draft responses accepted or modified
- AI-assisted cases
- Average handling-time reduction

### 6.3 Member Adoption

- Active member sessions
- Self-service completion rate
- Repeat AI usage
- Conversation abandonment rate
- Human escalation rate
- Digital-channel migration rate

---

## 7. Business Metrics

Business KPIs must originate primarily from systems of record.

Agent traces are not authoritative evidence that a business transaction completed.

```text
Agent Event != Business Outcome
```

For example, a successful `enrollment.submit` tool call does not necessarily mean enrollment completed successfully. The authoritative `EnrollmentCompleted` event must originate from the Enrollment or Policy Administration System.

### 7.1 Producer Domain

- Producer productivity
- Book-of-business growth
- Producer onboarding duration
- Commission inquiry resolution rate
- Appointment-validation cycle time

### 7.2 Quote Domain

- Quote turnaround time
- Quotes generated per producer
- Quote completion rate
- Quote abandonment rate
- Quote-to-enrollment conversion rate

### 7.3 Enrollment Domain

- Enrollment completion rate
- Enrollment cycle time
- Enrollment exception rate
- Straight-through processing rate
- Evidence rejection or resubmission rate

### 7.4 Billing Domain

- Billing inquiry resolution rate
- Average handling time
- Payment arrangement completion rate
- First-contact resolution rate
- Refund and adjustment exception rate

### 7.5 Servicing Domain

- Service request completion rate
- Average servicing time
- Self-service completion rate
- Human escalation rate
- First-contact resolution rate

---

## 8. AI Quality Metrics

AI quality measurements will remain separate from infrastructure health metrics.

Recommended measurements include:

- Groundedness
- Response correctness
- Tool-selection accuracy
- Tool-parameter accuracy
- Task completion
- Retrieval relevance
- Policy adherence
- Escalation appropriateness
- Hallucination rate
- Required disclosure completeness

The metric hierarchy is:

```text
Operational Health
        |
        | Is the service working?
        v

AI Quality
        |
        | Is the agent behaving correctly?
        v

Business Outcome
        |
        | Did the transaction complete correctly?
        v

Business KPI
        |
        | Is the platform producing measurable value?
        v
```

---

## 9. Standard Trace and Correlation Model

Every interaction will receive globally unique identifiers:

```text
trace_id
correlation_id
journey_id
session_id
```

Additional identifiers may include:

```text
quote_id
enrollment_id
billing_case_id
service_request_id
approval_request_id
```

The same `journey_id` must propagate through:

```text
Channel
   |
   v
Supervisor Agent
   |
   v
Domain Agent
   |
   v
AgentCore Gateway
   |
   v
Enterprise API
   |
   v
System of Record
   |
   v
Business Event
```

This permits reconstruction of journeys such as:

```text
Producer
   |
   v
SupervisorAgent
   |
   v
ProducerAgent
   license_verified
   |
   v
QuoteAgent
   quote_generated
   |
   v
HumanApproval
   approved
   |
   v
EnrollmentAgent
   enrollment_submitted
   |
   v
Enrollment System
   enrollment_completed
```

---

## 10. Standard Telemetry Attributes

Common OpenTelemetry attributes will be standardized across all agents and domains.

```json
{
  "service.name": "healthcare-agent-platform",
  "deployment.environment": "production",
  "agent.name": "EnrollmentAgent",
  "agent.version": "2.3",
  "agent.domain": "Enrollment",
  "journey.id": "<pseudonymous-id>",
  "journey.type": "QUOTE_TO_ENROLLMENT",
  "tool.name": "enrollment.submit",
  "tool.status": "SUCCESS",
  "policy.decision": "ALLOW",
  "approval.required": true,
  "approval.status": "APPROVED",
  "model.id": "<model-id>",
  "prompt.version": "enrollment-v17",
  "input.tokens": 3500,
  "output.tokens": 620,
  "duration.ms": 2350
}
```

Attributes must use controlled vocabularies to prevent metric cardinality growth and inconsistent reporting.

---

## 11. Sensitive Data Policy

Raw PHI, PII, credentials, payment details, and secrets will not be used as standard metric dimensions, log fields, or trace tags.

The following must not become standard telemetry attributes:

```text
Member name
Social Security number
Date of birth
Diagnosis or medical record details
Bank account or payment-card information
Access token or API secret
Raw member prompt containing PHI
Raw tool output containing PHI
```

The platform will instead use:

```text
journey_id
trace_id
tenant_id
domain
agent
tool
status
classification
pseudonymous_actor_id
```

When an investigation legitimately requires sensitive interaction content, access will occur through a separate governed evidence path with:

- Role-based access control
- Masking or tokenization
- Purpose-of-use validation
- Time-bounded access
- Retention controls
- Complete audit logging

Payload-bearing telemetry used for agent evaluation must have separate classification, access, and retention controls from ordinary operational telemetry.

---

## 12. Splunk Role

Splunk may be used as the enterprise-facing platform for:

- Centralized logging
- Security analytics
- Audit reporting
- Compliance investigations
- Policy violations
- Authentication events
- Agent security events
- Cross-system event correlation

Recommended dashboards include:

### Security and Compliance

- Agent policy denials
- Unauthorized tool attempts
- Authentication failures
- Sensitive-data events
- Human approval overrides
- Agent privilege violations

### Audit Journey

```text
Journey
 -> Agent
 -> Tool
 -> Policy
 -> Approval
 -> Business Transaction
```

---

## 13. Datadog Role

If Datadog is the enterprise SRE and APM standard, Datadog may be used for:

- Agent APM
- Distributed tracing
- Latency analysis
- Error analysis
- LLM monitoring
- Tool performance
- Agent dependency mapping
- SLO monitoring
- Infrastructure correlation
- Operations dashboards and alerts

---

## 14. Dashboard Architecture

### 14.1 Executive Dashboard

- AI adoption percentage
- Quote conversion
- Enrollment conversion
- Self-service percentage
- Human escalation percentage
- Productivity improvement
- Cost savings
- AI operating cost
- ROI

### 14.2 Business Operations Dashboard

- Producer activity
- Quote turnaround time
- Enrollment completion
- Billing resolution
- Servicing completion
- Exceptions
- Escalations

### 14.3 AI Engineering Dashboard

- Groundedness
- Tool accuracy
- Task completion
- Prompt version
- Model version
- Retrieval quality
- Token consumption
- Evaluation scores

### 14.4 SRE Dashboard

- Availability
- P95 and P99 latency
- Error rate
- Throttles
- Agent failures
- Tool failures
- Gateway latency
- Model latency
- SLO compliance

### 14.5 Security and Compliance Dashboard

- Policy denials
- Unauthorized tool attempts
- PHI and DLP events
- High-risk actions
- Human approvals
- Privilege violations
- Audit evidence

---

## 15. Alerting Strategy

Alerts will be classified by business impact rather than generated for every technical anomaly.

### SEV1

- Potential PHI exposure
- Cross-member data access
- Unauthorized financial transaction
- System-wide AgentCore outage

### SEV2

- Enrollment submission failures
- High policy-denial anomaly
- Critical Gateway dependency failure
- Significant error-rate increase

### SEV3

- P95 latency threshold violated
- Token-usage anomaly
- Individual tool degradation
- Agent evaluation score degradation

Alert thresholds must be tuned using production baselines to avoid alert fatigue.

---

## 16. SLO Framework

Each production agent will have documented SLOs.

Example for the Enrollment Agent:

```text
Availability >= 99.9%

P95 response latency <= 5 seconds,
excluding human approval wait time

Tool invocation success >= 99.5%

Unauthorized transaction rate = 0

Duplicate enrollment submission rate = 0

Evaluation score >= approved quality threshold
```

The SLO must distinguish between:

- Model latency
- Agent orchestration latency
- Tool and downstream-system latency
- Human approval wait time
- End-to-end journey duration

---

## 17. Options Considered

### 17.1 Option A: CloudWatch Only

**Decision:** Rejected as the enterprise operating model.

**Advantages:**

- Native AgentCore integration
- Lowest integration complexity
- Native AgentCore dashboards and evaluation integration

**Disadvantages:**

- Requires AWS-oriented operational workflows
- Conflicts with the restricted-console operating model
- Remains fragmented from broader enterprise monitoring

CloudWatch remains part of the technical architecture but will not be the primary human-facing operations platform.

### 17.2 Option B: Direct Splunk or Datadog Only

**Decision:** Not selected as the default architecture.

**Advantages:**

- Unified enterprise operational experience
- Potentially simpler telemetry pipeline

**Disadvantages:**

- Potential loss of native AgentCore observability and evaluation capabilities
- Greater coupling to the enterprise observability vendor
- Additional effort to reproduce AgentCore-specific telemetry semantics

This option may be used selectively for application-specific custom telemetry, but not as the only AgentCore telemetry path.

### 17.3 Option C: CloudWatch + OpenTelemetry + Splunk/Datadog

**Decision:** Selected.

```text
AgentCore
   |
   +--> CloudWatch
   |
   +--> OpenTelemetry
           |
           +--> Splunk
           |
           +--> Datadog
```

**Benefits:**

- Retains native AgentCore capabilities
- Removes AWS Console dependency for operators
- Enables enterprise-wide correlation
- Reduces vendor lock-in through OpenTelemetry
- Supports AI, operations, security, and compliance use cases

---

## 18. Consequences

### 18.1 Positive Consequences

- Production teams do not require AWS Console access.
- AgentCore-native capabilities remain available.
- Multi-agent journeys become traceable end to end.
- OpenTelemetry reduces observability vendor lock-in.
- Business metrics remain aligned with authoritative systems.
- AI and traditional application telemetry can be correlated.
- Splunk can support SOC, audit, and compliance use cases.
- Datadog can support SRE, APM, and agent operations.
- AgentCore telemetry can support debugging and AI evaluations.

### 18.2 Negative Consequences

- Telemetry may exist in more than one platform.
- Additional observability ingestion and retention costs are expected.
- Retention policies must be coordinated across platforms.
- Centralized telemetry-schema governance is required.
- PHI and PII redaction becomes a critical platform responsibility.
- Correlation IDs must be propagated consistently across agents and legacy systems.
- Duplicate dashboards must be avoided through clear ownership.

---

## 19. Governance and Ownership

### AI Platform Team

- Agent telemetry schema
- OpenTelemetry standards
- Agent naming conventions
- Trace propagation
- Agent-specific instrumentation
- AI evaluation metrics

### Enterprise Observability / SRE Team

- Splunk and Datadog integration
- Telemetry collection and routing
- Retention policies
- Operational dashboards
- Alert lifecycle management
- SLO infrastructure
- Incident management

### Business Domain Teams

- Business events
- Business KPI definitions
- Business outcome ownership
- System-of-record reconciliation

### Security and Compliance

- Sensitive-data classification
- Telemetry access policies
- Audit retention
- PHI and PII controls
- Compliance reporting
- Investigation access procedures

---

## 20. Implementation Controls

The implementation must include:

1. A common OpenTelemetry semantic convention for all agents.
2. Automated redaction at or before the telemetry collector.
3. Controlled-cardinality attributes.
4. End-to-end propagation of `trace_id`, `correlation_id`, and `journey_id`.
5. Separate retention policies for operational metadata and payload-bearing evaluation data.
6. Least-privilege, non-interactive integration roles for Splunk and Datadog.
7. Infrastructure-as-code for CloudWatch, collectors, integrations, dashboards, and alerts.
8. Reconciliation between agent completion events and system-of-record outcomes.
9. SLOs for every production agent and critical tool.
10. Periodic audits of telemetry for PHI, PII, secrets, and uncontrolled high-cardinality data.

---

## 21. Final Decision Architecture

```text
                           MULTI-AGENT PLATFORM
                                   |
           +----------+------------+-------------+----------+
           |          |            |             |          |
       Producer     Quote      Enrollment     Billing    Servicing
        Agent       Agent         Agent         Agent      Agent
           |          |            |             |          |
           +----------+------------+-------------+----------+
                                   |
                            AgentCore Runtime
                                   |
                        AgentCore Observability
                                   |
                             OTEL / ADOT
                                   |
                    +--------------+--------------+
                    |                             |
              CloudWatch                    Custom OTEL
          AWS-Native Backend                    Events
                    |                             |
          No operator console                    |
             dependency                          |
                    |                             |
              +-----+-----------------------------+----+
              |                                       |
              v                                       v
            Splunk                                  Datadog
      Security / Audit /                       SRE / APM /
         Compliance                         Agent Operations
              |                                       |
              +------------------+--------------------+
                                 |
                       Operational & AI Metrics
                                 |
                                 v
                     Enterprise Data Platform
                                 ^
                                 |
              +------------------+-------------------+
              |          |          |         |      |
           Producer    Quote    Enrollment  Billing Servicing
                               Business Events
                                 |
                                 v
                     Tableau / Power BI / BI
                                 |
                                 v
                         Business Metrics
                         Adoption Metrics
                         Executive KPIs
```

---

## 22. Decision Summary

> **The healthcare multi-agent platform will adopt a layered observability strategy in which Amazon Bedrock AgentCore Observability provides AgentCore-native telemetry, CloudWatch remains the restricted AWS-native telemetry foundation required for native AgentCore capabilities, OpenTelemetry/ADOT provides standardized instrumentation and portability, and Splunk and/or Datadog provide the enterprise-facing monitoring experience. Business and adoption metrics will be generated primarily from domain and system-of-record events and correlated with agent telemetry using common journey and trace identifiers. Production users will not require AWS Console access.**

This separates three concerns:

```text
AgentCore Observability
        =
How is the AI agent behaving?

Splunk / Datadog
        =
How is the production service operating?

Enterprise Data Platform
        =
Is the AI platform creating measurable business value?
```

For a regulated healthcare environment, this separation ensures that agent telemetry does not become the system of record for business outcomes and operational telemetry does not become an uncontrolled repository of PHI.

---

## 23. References

1. AWS, *Amazon Bedrock AgentCore Observability Developer Guide*: https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/observability.html
2. AWS, *AgentCore Telemetry Setup and Delivery*: https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/supported-frameworks-telemetry.html
3. AWS, *CloudWatch GenAI Observability and AgentCore Evaluations*: https://aws.amazon.com/about-aws/whats-new/2025/12/cloudwatch-genai-observability-agentcore-evaluations/
4. Datadog, *AWS Integration Documentation*: https://docs.datadoghq.com/integrations/amazon-web-services/
5. Splunk, *AWS Integration Documentation*: https://dev.splunk.com/observability/docs/integrations/aws_integration_overview/
6. Splunk, *Amazon Bedrock Monitoring*: https://help.splunk.com/en/splunk-observability-cloud/observability-for-ai/splunk-ai-infrastructure-monitoring/set-up-ai-infrastructure-monitoring/amazon-bedrock
