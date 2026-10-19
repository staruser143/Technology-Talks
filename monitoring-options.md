# Monitoring Options for Restricted AWS Environments

## Overview

Restricting interactive AWS Console access does not prevent use of Amazon CloudWatch or AgentCore telemetry. Telemetry can be exported, streamed, or forwarded programmatically to enterprise observability tools such as Splunk and Datadog.

AgentCore emits OpenTelemetry-compatible telemetry, which allows integration with an existing observability stack. However, AgentCore-managed metrics, spans, and logs are stored in CloudWatch by default. The most practical design is usually to keep CloudWatch as the AWS-native telemetry backend while using Splunk or Datadog as the primary operational interface.

> [!IMPORTANT]
> The operational model depends on which controls are restricted:
> - AWS Console access is restricted
> - CloudWatch service access is restricted

These are different constraints and should be treated separately.

---

## Decision Summary

| Scenario | Recommended Approach |
|---|---|
| Console access restricted, CloudWatch allowed | Continue using CloudWatch through APIs, SDKs, OTLP, Metric Streams, and AWS integrations |
| CloudWatch ingestion restricted | Export telemetry directly to the enterprise observability platform |
| Regulated healthcare environment | Keep CloudWatch as the AWS landing zone; use Splunk or Datadog as the operational UI |

---

## Key Distinction

There are two separate restrictions:

- AWS Console access is prohibited
- Use of the CloudWatch service is prohibited

### If only the console is restricted
Use CloudWatch through:

- AWS APIs
- AWS SDK
- OpenTelemetry exporters
- CloudWatch Metric Streams
- Log subscriptions
- Splunk or Datadog AWS integrations
- Infrastructure as code

Operators do not need to log into the AWS Console.

### If CloudWatch ingestion itself is prohibited
Configure the application instrumentation to export telemetry directly to the enterprise observability platform.

---

## Recommended Architecture

```text
LangGraph Agents
running on AgentCore Runtime
        |
        | OpenTelemetry instrumentation
        v
AWS Distro for OpenTelemetry
        |
        +-----------------------------+
        |                             |
        v                             v
CloudWatch                     Enterprise OTLP Collector
AWS-native copy                       |
        |                              +--> Datadog
        |                              |
        +--> Metric Streams            +--> Splunk
        +--> Log subscriptions         |
        +--> API integration           +--> Enterprise APM
```

This architecture provides:
- AWS-native telemetry retention
- enterprise observability integration
- no dependency on interactive AWS Console access
- compatibility with existing SIEM and APM tools

---

## Preferred Enterprise Pattern

For a regulated healthcare environment, the recommended pattern is:

```text
AgentCore and ADOT
        |
        v
CloudWatch as controlled landing zone
        |
        v
Splunk or Datadog as enterprise interface
```

This preserves AWS-native compatibility while removing the need for daily AWS Console use.

---

## Option 1: CloudWatch Backend with Datadog Frontend

This is typically the fastest and lowest-risk option.

```text
AgentCore Runtime
      |
      v
AgentCore Observability
      |
      v
CloudWatch
      |
      v
Datadog AWS Integration
      |
      v
Datadog Dashboards, APM and Alerts
```

Datadog has published a pattern that combines Amazon Bedrock AgentCore with Datadog Agent Observability. The model uses AgentCore for execution and governance, while Datadog provides enterprise-level operational monitoring and analysis.

### Datadog Use Cases

- Agent execution traces
- LLM calls
- Tool calls
- Token usage
- Latency
- Error analysis
- Agent workflow visualization
- Cost dashboards
- Alerting
- Service-level objectives
- Cross-platform correlation

### Recommended Tags

```text
environment:production
application:healthcare-agent-platform
agent:enrollment-agent
domain:enrollment
journey:quote-to-enrollment
model:claude
prompt_version:v17
tool:enrollment-submit
policy_decision:allow
```

These tags help correlate operational behavior to business flows and environment context.

---

## Option 2: CloudWatch Backend with Splunk Frontend

```text
AgentCore Runtime
      |
      v
CloudWatch Metrics, Logs and Traces
      |
      +--> Splunk AWS Integration
      |
      +--> CloudWatch Logs subscription
      |
      +--> Kinesis Data Firehose
      |
      v
Splunk Enterprise or Splunk Cloud
```

Splunk supports collecting Bedrock metrics through AWS and CloudWatch integrations, including:

- invocation counts
- input/output token counts
- invocation latency
- authentication errors
- throttling-related metrics

### Splunk Use Cases

- Operational dashboards
- Security analytics
- Audit trails
- Tool call monitoring
- Policy denial monitoring
- PHI/DLP events
- Authentication failures
- Cross-agent journey correlation
- SOC integration
- Compliance reporting

### Example Journey Reconstruction

```text
Journey ID: J-10027

Supervisor Agent
  -> Producer Agent
  -> Quote Agent
  -> Compliance Agent
  -> Enrollment Agent
  -> Enterprise Enrollment API
```

Splunk can reconstruct this journey when all events share the same trace_id and journey_id.

---

## Option 3: Direct OTLP Export

If the organization does not permit CloudWatch ingestion, send telemetry directly to an enterprise collector:

```text
LangGraph / Agent Application
          |
          v
ADOT or OpenTelemetry SDK
          |
          v
Enterprise OpenTelemetry Collector
          |
          +--> Datadog OTLP endpoint
          +--> Splunk OTLP endpoint
          +--> Grafana Tempo
          +--> Elastic APM
```

AgentCore telemetry is OpenTelemetry-compatible, and AWS also documents ADOT-based instrumentation for agents.

### Conceptual OpenTelemetry Configuration

```yaml
receivers:
  otlp:
    protocols:
      grpc:
      http:

processors:
  memory_limiter:
  batch:
  attributes/redaction:
    actions:
      - key: member.id
        action: delete
      - key: user.prompt
        action: delete
      - key: tool.output
        action: delete

exporters:
  otlphttp/datadog:
    endpoint: ${DATADOG_OTLP_ENDPOINT}

  otlphttp/splunk:
    endpoint: ${SPLUNK_OTLP_ENDPOINT}

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors:
        - memory_limiter
        - attributes/redaction
        - batch
      exporters:
        - otlphttp/datadog
        - otlphttp/splunk
```

> [!NOTE]
> The exact authentication headers and exporter settings depend on the target environment, whether it is Datadog SaaS, Splunk Observability Cloud, Splunk Enterprise, or an internal collector.

---

## Important Limitation

If CloudWatch is completely bypassed, some native AgentCore Observability and Evaluations capabilities may not be available.

AWS documentation indicates:

- AgentCore-generated metrics, spans, and logs are stored in CloudWatch
- CloudWatch console views provide AgentCore-specific execution visualizations and error analysis
- AgentCore Evaluations expects telemetry to be exported to CloudWatch and relies on CloudWatch Transaction Search for session reconstruction

### Practical Impact

| Scenario | Impact |
|---|---|
| No AWS Console access | Not a problem |
| No CloudWatch API access for users | Manageable |
| No CloudWatch telemetry ingestion at all | Possible, but native AgentCore Observability and Evaluations may be limited |

---

## CloudWatch Without Console Access

Production support teams can operate entirely through Splunk or Datadog while a tightly controlled AWS service role handles telemetry integration.

```text
Support Engineer
      |
      v
Splunk / Datadog
      |
      v
Cross-account observability role
      |
      v
CloudWatch APIs and telemetry
```

Use a read-only integration role with:

- External ID
- Least-privilege IAM permissions
- No console login
- CloudTrail auditing
- Scoped resource access
- Cross-account access through a central observability account
- Infrastructure-as-code deployment

---

## Application-Level Telemetry Is Still Required

AgentCore telemetry primarily provides operational insight. We should also instrument LangGraph nodes and domain workflows with business event telemetry.

### Example Span Attributes

```json
{
  "service.name": "healthcare-agent-platform",
  "deployment.environment": "production",
  "gen_ai.agent.name": "EnrollmentAgent",
  "insurance.domain": "enrollment",
  "insurance.journey.type": "quote-to-enrollment",
  "insurance.journey.id": "J-10027",
  "insurance.action": "submit-enrollment",
  "insurance.outcome": "success",
  "human.approval.required": true,
  "human.approval.status": "approved",
  "policy.decision": "allow",
  "tool.name": "enrollment.submit",
  "prompt.version": "enrollment-v17"
}
```

### Fields to Exclude from Telemetry

Do not include:

- Member name
- Member ID
- Social Security number
- Date of birth
- Medical details
- Raw prompts containing PHI
- Raw tool responses
- Access tokens
- Payment details

Use pseudonymous identifiers or keyed hashes when journey-level correlation is needed.

---

## Mapping to Enterprise Metrics

### Operational Metrics

Send to Splunk or Datadog:

- Agent invocation count
- Session count
- P50/P95/P99 latency
- Model latency
- Tool latency
- Token consumption
- Error and timeout rates
- Gateway failures
- Policy denials
- Throttles
- Memory retrieval latency
- Human approval wait time

### Adoption Metrics

Publish custom events:

- Active producers
- Active CSRs
- Unique member sessions
- Sessions per user
- Agent feature usage
- Repeat usage
- Abandonment rate
- Human escalation rate
- AI acceptance rate
- Suggested-action acceptance rate

### Business Metrics

Publish domain events from systems of record:

- QuoteCreated
- QuotePresented
- QuoteAccepted
- EnrollmentStarted
- EnrollmentSubmitted
- EnrollmentCompleted
- InvoiceExplained
- PaymentArrangementCreated
- ServiceRequestResolved

Join these with agent traces using:

- journey_id
- correlation_id
- quote_id
- enrollment_id
- case_id

> [!IMPORTANT]
> Do not calculate authoritative business outcomes from AgentCore logs alone. Confirm outcomes from the Producer, Quote, Enrollment, Billing, and Servicing systems of record.

---

## Recommended Design for the Payer Platform

```text
                                  +-------------------+
                                  | Splunk / Datadog  |
                                  | Primary UI        |
                                  +---------+---------+
                                            ^
                                            |
                           OTLP / AWS integration / streaming
                                            |
+--------------------+           +----------+-----------+
| LangGraph Agents   |---------->| Enterprise OTel      |
| AgentCore Runtime  |           | Collector            |
+---------+----------+           +----------+-----------+
          |                                 |
          | Native telemetry                | Custom metrics
          v                                 |
+--------------------+                      |
| CloudWatch         |<---------------------+
| No human console   |
| access required    |
+--------------------+
```

---

## Final Recommendation

For the enterprise healthcare architecture:

1. Retain CloudWatch as the restricted AWS-native telemetry landing zone.
2. Give production support teams access only to Splunk or Datadog.
3. Use OpenTelemetry and ADOT for application, LangGraph, model, and tool spans.
4. Forward telemetry through a centrally managed collector or AWS integration.
5. Emit business events separately from each domain workflow.
6. Correlate AgentCore traces and business events with a common journey_id.
7. Apply redaction before telemetry leaves the agent runtime.
8. Keep AgentCore Evaluations’ CloudWatch dependency in mind before bypassing CloudWatch entirely.

> [!SUMMARY]
> Splunk and Datadog are viable and often preferable enterprise-facing observability platforms. Restricted AWS Console access does not prevent us from retaining and leveraging CloudWatch behind the scenes.

---

## Appendix: Quick Reference

| Need | Best Fit |
|---|---|
| Lowest risk | CloudWatch + Datadog |
| Strong audit + security workflows | CloudWatch + Splunk |
| No CloudWatch ingestion allowed | Direct OTLP export |
| Best operational UX | Datadog or Splunk as primary UI |
| Best AWS-native retention | CloudWatch retained as landing zone |

If you want, I can also convert this into:
- a more executive-ready version,
- a slide deck outline,
- or a compact wiki page with shorter sections and stronger callouts.
