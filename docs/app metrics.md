For an enterprise deployment, the recommendation is to treat metrics as a three-layer measurement system:

Layer 1 -> Operational Metrics
Layer 2 -> Adoption Metrics
Layer 3 -> Business Outcome Metrics


Many organizations stop at Layer 1 (latency, tokens, errors). Leadership, however, ultimately cares about Layer 3.

Recommended Metrics Architecture
Users
  |
  v

Producer Agent
Quote Agent
Enrollment Agent
Billing Agent
Servicing Agent

  |
  v

AgentCore Observability
  |
  +--> CloudWatch Metrics
  +--> CloudWatch Logs
  +--> CloudWatch Dashboards
  +--> OpenTelemetry

  |
  +--> Business Event Pipeline
           |
           +--> EventBridge
           +--> Kinesis
           +--> Kafka
           +--> Snowflake/Redshift

  |
  +--> Executive Dashboards
           |
           +--> Tableau
           +--> Power BI
           +--> QuickSight


Amazon Bedrock AgentCore Observability provides built-in metrics including session count, latency, token usage, duration, error rates, traces, spans and workflow execution visibility, and can emit OpenTelemetry-compatible telemetry into broader enterprise observability platforms.

Layer 1: Operational Metrics

These come directly from AgentCore.

Agent Metrics

Track per agent:

Producer Agent
Quote Agent
Enrollment Agent
Billing Agent
Servicing Agent


Metrics:

Invocations
Sessions
Latency
Errors
Throttles
Availability


AgentCore provides invocation count, session count, latency, user errors, system errors, throttles, and total error metrics.

Example dashboard:

Enrollment Agent

Daily Requests      : 18,230
P95 Latency         : 2.1 sec
Success Rate        : 99.3%
Error Rate          : 0.7%

Tool Metrics

Track:

quote.calculate

enrollment.submit

billing.getInvoice


Examples:

Tool Calls

Success %

Timeout Rate

Policy Denials

Average Duration


Useful for identifying backend bottlenecks.

Model Metrics

Track:

Claude
Nova
GPT
Gemini


Metrics:

Input Tokens
Output Tokens
Token Cost
Latency
Hallucination Rate


CloudWatch GenAI Observability supports monitoring token usage, latency, invocation counts and errors across AI workloads.

Layer 2: Adoption Metrics

Operational metrics tell you:

"The platform is running."

Adoption metrics tell you:

"People are actually using it."

Producer Adoption

Track:

Licensed Producers
Active Producers
Monthly Active Producers
Daily Active Producers


Formula:

Producer Adoption %

=
Monthly Active Producers
/
Total Producers


Example:

15,000 Producers

9,000 Active

60% Adoption

CSR Adoption

Track:

CSR users

Active CSRs

Sessions per CSR

Tasks assisted


Example:

2400 CSRs

1900 Active

79% Adoption

Channel Adoption

Compare:

Portal
Call Center
Agent
Human


Example:

60% AI Assisted

40% Human Only

Layer 3: Business Metrics

These are the metrics executives care about most.

Producer Domain
Book of Business Growth
Policies Sold

Quote Conversion
Submitted Quotes

↓

Sold Policies


Formula:

Conversion %

=
Sold Policies
/
Quotes Generated


Example:

Before AI = 17%

After AI = 23%

Quote Domain
Quote Throughput
Quotes Generated Per Day

Quote Turnaround Time
Request

to

Quote Delivered


Example:

Before AI = 2 Days

After AI = 15 Minutes

Enrollment Domain

Measure:

Enrollment Completion Rate


Formula:

Completed Enrollments
/
Started Enrollments


Example:

Before AI
 72%

After AI
 88%

Billing Domain

Measure:

Billing Issues Resolved


and

Average Resolution Time


Example:

Before
13 Minutes

After
3 Minutes

Servicing Domain

Track:

Address Changes

ID Card Requests

Life Event Updates

Policy Changes


Example:

75% handled autonomously

Human Escalation Metrics

This is extremely important.

Track:

Agent Resolved

vs

Escalated


Example:

Total Requests : 100,000

Resolved by AI : 65,000

Escalated      : 35,000


AI containment rate:

65%


This becomes a key executive KPI.

Healthcare-Specific Quality Metrics

I strongly recommend creating metrics not available out-of-the-box.

Enrollment Accuracy
Correct Enrollment Submission %

Incorrect Enrollment %

Compliance Accuracy

Track:

Required Disclosure Present

Required Disclosure Missing


Example:

99.92%

Producer Eligibility Accuracy
Correct Authorization Decisions


Example:

Producer can quote

Producer can't quote

Decision accuracy

Cost Metrics

Almost every enterprise eventually asks:

What's the ROI?

Track:

Cost per Session

Cost per Quote

Cost per Enrollment

Cost per Billing Inquiry


Example:

Quote Agent

Monthly Cost
$8,000

Quotes Generated
40,000

Cost per Quote
$0.20

Recommended Executive Scorecard

For the  healthcare insurance platform:

Adoption
Monthly Active Producers

Monthly Active Members

Monthly Active CSRs

AI Usage Rate

Business
Quote Conversion %

Enrollment Completion %

Service Resolution %

Call Deflection %

Producer Productivity

Operational
Session Count

P95 Latency

Error Rate

Availability

Token Usage


AgentCore Observability and CloudWatch provide built-in metrics for session counts, latency, token usage, invocations, and error tracking.

Financial
Cost per Interaction

Cost per Enrollment

Cost per Quote

Total Savings

ROI

Healthcare Insurance KPI Framework

Recommended formula:

North Star Metric

=
Policies Successfully Bound
+
Members Successfully Enrolled
+
Servicing Requests Completed
-
Compliance Incidents


Then build supporting KPIs:

Business
   ↓

Adoption
   ↓

Operational


The key architectural principle is:

Use AgentCore Observability for operational telemetry, but generate adoption and business metrics from domain events emitted by Producer, Quote, Enrollment, Billing and Servicing workflows into your enterprise data platform (Redshift, Snowflake, Data Lake, Tableau, Power BI, or QuickSight). Operational metrics tell you how the AI is performing; business metrics tell you whether the AI is creating value.