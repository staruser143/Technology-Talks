Restricting interactive AWS Console access does not mean we cannot use Amazon CloudWatch or AgentCore telemetry. We can export, stream, or forward telemetry programmatically to enterprise tools such as Splunk and Datadog.

AgentCore emits OpenTelemetry-compatible telemetry, allowing integration with an existing observability stack. However, AgentCore-managed metrics, spans, and logs are stored in CloudWatch by default, so the most practical design is usually to retain CloudWatch as the AWS-native telemetry backend while using Splunk or Datadog as the primary operations interface.

Important distinction

There are two separate restrictions:

- AWS Console access is prohibited
- Use of the CloudWatch service is prohibited

If only the console is restricted, continue using CloudWatch through:

APIs
AWS SDK
OpenTelemetry exporters
CloudWatch Metric Streams
Subscription filters
Splunk or Datadog AWS integrations
Infrastructure-as-code

Operators never need to log into the AWS Console.

If CloudWatch ingestion itself is prohibited, configure the application instrumentation to export telemetry directly to the enterprise observability platform.

Recommended architecture
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

Preferred enterprise pattern

For a regulated healthcare environment, I recommend:

AgentCore and ADOT
        |
        v
CloudWatch as controlled landing zone
        |
        v
Splunk or Datadog as enterprise interface


This preserves AWS-native compatibility while eliminating day-to-day dependence on AWS Console access.

Option 1: CloudWatch backend with Datadog frontend

This is usually the fastest and lowest-risk option.

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


Datadog has published an AgentCore observability pattern combining Amazon Bedrock AgentCore with Datadog Agent Observability. The pattern uses AgentCore for execution and governance while Datadog provides enterprise-level agent monitoring and operational analysis.

We can use Datadog for:

Agent execution traces
LLM calls
Tool calls
Token usage
Latency
Error analysis
Agent workflow visualization
Cost dashboards
Alerting
Service-level objectives
Cross-platform correlation

Recommended tags:

environment:production
application:healthcare-agent-platform
agent:enrollment-agent
domain:enrollment
journey:quote-to-enrollment
model:claude
prompt_version:v17
tool:enrollment-submit
policy_decision:allow

Option 2: CloudWatch backend with Splunk frontend
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


Splunk supports collecting Amazon Bedrock metrics through its AWS and CloudWatch integration, including invocation counts, input/output token counts, invocation latency, authentication errors, and throttling-related metrics.

Use Splunk for:

Operational dashboards
Security analytics
Audit trails
Tool call monitoring
Policy-denial monitoring
PHI/DLP events
Authentication failures
Cross-agent journey correlation
SOC integration
Compliance reporting

For example:

Journey ID: J-10027

Supervisor Agent
  -> Producer Agent
  -> Quote Agent
  -> Compliance Agent
  -> Enrollment Agent
  -> Enterprise Enrollment API


Splunk can reconstruct this journey when all events use the same trace_id and journey_id.

Option 3: Direct OTLP export

If the organization does not permit CloudWatch ingestion, send OpenTelemetry telemetry directly to an enterprise collector:

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


AgentCore telemetry uses OpenTelemetry-compatible formats, and AWS also documents ADOT-based instrumentation for agents.

A simplified OpenTelemetry configuration would look conceptually like this:

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

e
The exact authentication headers and exporter configuration depend on whether we use Datadog SaaS, Splunk Observability Cloud, Splunk Enterprise, or an internal collector.

Important limitation

If we completely bypass CloudWatch, we may not get the full native AgentCore Observability experience.

AWS states that AgentCore-generated metrics, spans, and logs are stored in CloudWatch, while the CloudWatch console provides AgentCore-specific execution visualizations and error analysis. AgentCore Evaluations also expects AgentCore telemetry to be exported to CloudWatch and requires CloudWatch Transaction Search for session reconstruction.

Therefore:

No AWS Console access: Not a problem.
No CloudWatch API access for users: Also manageable.
No CloudWatch telemetry ingestion at all: Possible for custom observability, but some native AgentCore Observability and Evaluations capabilities may be unavailable.
CloudWatch without console access

The production support teams can operate completely through Splunk or Datadog while a tightly controlled AWS service role performs telemetry integration.

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


Use a read-only integration role with:

External ID
Least-privilege IAM permissions
No console login
CloudTrail auditing
Scoped resource access
Cross-account access through a central observability account
Infrastructure-as-code deployment
Application-level telemetry is still required

AgentCore's built-in telemetry primarily provides operational information. We should additionally instrument LangGraph nodes and domain workflows with business events.

Example span attributes:

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


Do not include:

Member name
Member ID
Social Security number
Date of birth
Medical details
Raw prompts containing PHI
Raw tool responses
Access tokens
Payment details


Use pseudonymous identifiers or keyed hashes when journey-level correlation is needed.

Mapping to the enterprise metrics
Operational metrics

Send to Splunk or Datadog:

Agent invocation count
Session count
P50/P95/P99 latency
Model latency
Tool latency
Token consumption
Error and timeout rates
Gateway failures
Policy denials
Throttles
Memory retrieval latency
Human-approval wait time
Adoption metrics

Publish custom events:

Active producers
Active CSRs
Unique member sessions
Sessions per user
Agent feature usage
Repeat usage
Abandonment rate
Human escalation rate
AI acceptance rate
Suggested-action acceptance rate
Business metrics

Publish domain events from systems of record:

QuoteCreated
QuotePresented
QuoteAccepted
EnrollmentStarted
EnrollmentSubmitted
EnrollmentCompleted
InvoiceExplained
PaymentArrangementCreated
ServiceRequestResolved


Join these with agent traces using:

journey_id
correlation_id
quote_id
enrollment_id
case_id


Do not calculate authoritative business outcomes solely from AgentCore logs. Confirm outcomes from Producer, Quote, Enrollment, Billing, and Servicing systems of record.

Recommended design for the payer platform
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

Final recommendation

For the enterprise healthcare architecture:

Retain CloudWatch as the restricted AWS-native telemetry landing zone.
Give production support teams access only to Splunk or Datadog.
Use OpenTelemetry and ADOT for application, LangGraph, model, and tool spans.
Forward telemetry through a centrally managed collector or AWS integration.
Emit business events separately from each domain workflow.
Correlate AgentCore traces and business events with a common journey_id.
Apply redaction before telemetry leaves the agent runtime.
Keep AgentCore Evaluations' CloudWatch dependency in mind before deciding to bypass CloudWatch entirely.

Splunk and Datadog are viable and often preferable enterprise-facing observability platforms. Restricted AWS Console access does not prevent us from retaining and leveraging CloudWatch behind the scenes.