To generate these business, adoption, and operational metrics in an enterprise environment—especially one with restricted AWS Console access—you must map each metric to specific **Amazon Bedrock AgentCore primitives** (Identity, Runtime, Gateway, Evaluations) and export the data to your enterprise’s existing BI and observability tools (e.g., Snowflake, Datadog, Splunk, Tableau).

Because AI agents are non-deterministic, traditional API metrics are not enough. You must combine **infrastructure telemetry** (OpenTelemetry) with **business event tagging** (custom code) and **AI-driven evaluations** (AgentCore Evaluations). 

Here is exactly how to instrument, capture, and calculate each metric category.

---

### 1. Adoption Metrics (WAU, MAU, Returning Users)
*These measure how frequently members, providers, or internal staff are using the AI agent.*

* **Data Source:** **AgentCore Identity** and **AgentCore Runtime**.
* **How to Capture:** 
  * When a user authenticates, AgentCore Identity extracts their `userId` (e.g., the `sub` claim from their JWT) and attaches it to the OpenTelemetry trace of every agent invocation.
  * Ensure your AgentCore Runtime is configured to export traces to your enterprise data warehouse (e.g., via OpenTelemetry Protocol (OTLP) to Datadog, or CloudWatch Logs -> Kinesis Firehose -> Snowflake/S3).
* **How to Calculate:**
  * **WAU / MAU:** Query your data warehouse for `COUNT(DISTINCT userId)` grouped by 7-day or 30-day rolling windows where `trace_name = 'Agent Invocation'`.
  * **Returning Users:** A "returning" user is one who initiates a *new* session (new `sessionId`) within the time window, having had at least one previous session. 
* **Enterprise Tip:** Segment these metrics using AgentCore Identity metadata (e.g., WAU by "Member" vs. "Healthcare Provider" vs. "Internal Claims Adjuster").

---

### 2. Experience Metrics (Thumbs Up %, Thumbs Down %)
*These measure explicit user satisfaction with the agent’s answers.*

* **Data Source:** **Your Application Frontend** + **AgentCore Evaluations / Langfuse / LangSmith**.
* **How to Capture:**
  * **Explicit Feedback (UI):** When a user clicks "Thumbs Up" or "Thumbs Down" on a response, your frontend must send an API call to your backend with the `sessionId`, `messageId` (or trace ID), and the `feedback_score`.
  * **Instrumentation:** Your backend logs this feedback event as a custom OpenTelemetry span or attribute, explicitly linking it to the original AgentCore trace ID. If you use an LLM observability tool like **Langfuse** or **LangSmith** (which integrate natively with AgentCore), you can use their native feedback APIs to tag traces directly.
  * **Implicit Feedback (AgentCore Evaluations):** You can enable **AgentCore Evaluations**, which uses a secondary LLM to automatically score the primary agent's output on "Helpfulness," "Safety," and "Coherence" without requiring the user to click a button.
* **How to Calculate:**
  * **CSAT %:** `(Count of Thumbs Up / Total Feedback Events) * 100`.
  * **AI-Assisted CSAT:** Use the P50/P90 scores of AgentCore Evaluations' "Helpfulness" metric as a proxy for user satisfaction across sessions where no explicit feedback was given.

---

### 3. Efficiency Metrics
*These measure how well the agent solves problems without human intervention and how fast it responds.*

#### A. Time-to-Answer (Latency)
* **Data Source:** **AgentCore Observability (OpenTelemetry Traces)**.
* **How to Capture:** AgentCore Runtime automatically generates OTel spans for every execution. The root span captures the total end-to-end time, while child spans capture LLM inference time, Gateway tool-calling time, and Memory retrieval time.
* **How to Calculate:** Query your APM tool (Datadog/Splunk) for the `duration` of the root span. Calculate **Time to First Token (TTFT)** (crucial for streaming UIs) and **Total End-to-End Latency** at the P50, P90, and P99 percentiles.

#### B. Search Success Rate (Tool / Knowledge Base Accuracy)
* **Data Source:** **AgentCore Gateway Logs** and **AgentCore Evaluations**.
* **How to Capture:** 
  * **Infrastructure Level:** When the agent uses the Gateway to search a Knowledge Base or query an internal claims database, the Gateway logs the tool execution status (`200 OK` vs `500 Error` vs `Empty Result`).
  * **Semantic Level:** Enable the **"Context Relevance"** evaluator in AgentCore Evaluations. This uses a judge model to score whether the data retrieved by the search tool was actually relevant to the user's prompt.
* **How to Calculate:** `(Successful Tool Executions + High Context Relevance Scores) / Total Search Tool Calls`.

#### C. Containment Rate (Deflection / Resolution)
*This is the most critical ROI metric for insurance (e.g., "Did the AI resolve the claim status inquiry, or did we have to pay a human call-center agent to do it?")*
* **Data Source:** **Custom Business Events (LangGraph/Strands Code)**.
* **How to Capture:** Containment cannot be measured purely by infrastructure metrics; it requires tracking the *outcome* of the graph. 
  * In your agent framework (e.g., LangGraph), define specific terminal nodes: `Node: Resolved Successfully`, `Node: Escalated to Human`, `Node: User Abandoned`.
  * When the agent reaches one of these nodes, emit a custom OTel event (e.g., `session_outcome: resolved`) with the `sessionId` and `userId`.
  * Track when the `escalate_to_human_agent` Gateway tool is invoked.
* **How to Calculate:** `Total Sessions ending in "Resolved" / Total Initiated Sessions`. 
  * *Note:* You must subtract "abandoned" sessions (where the user simply closed the browser mid-query) to get a true business containment rate.

---

### The Enterprise Analytics Pipeline Architecture
Since your enterprise restricts AWS Console access, you should build an automated pipeline that extracts AgentCore telemetry into your approved data ecosystem.

1. **The Source (AgentCore):**
   * AgentCore Runtime, Gateway, and Identity emit OpenTelemetry traces and CloudWatch Metrics.
   * AgentCore Evaluations generates JSON scores for every interaction.

2. **The Extraction (Zero-Console):**
   * **Real-Time APM:** Configure the AgentCore SDK to export OTLP traces directly to your corporate **Datadog**, **Dynatrace**, or **Splunk** instance. Your operations teams can build dashboards for Latency, Tool Errors, and TTFT here.
   * **Batch Analytics:** Use **Amazon CloudWatch Logs Subscription Filters** or **Kinesis Data Firehose** to stream AgentCore logs and evaluation scores directly into **Amazon S3**.

3. **The Transformation & Visualization:**
   * Use **Amazon Athena** or **dbt** to query the raw JSON traces in S3.
   * Connect **Tableau**, **PowerBI**, or **Amazon QuickSight** (embedded in your internal enterprise portal) to these transformed data models.
   * *Example BI Query for Containment:* `SELECT COUNT(session_id) FROM agent_traces WHERE business_event = 'resolved' AND date = CURRENT_DATE`.

---

### Summary Recommendation: The "Three-Layer" Metrics Strategy
To accurately report on business impact to stakeholders, implement a three-layer metrics strategy:
1. **Layer 1: Infrastructure Health (APM):** Use Datadog/Splunk to monitor AgentCore Gateway latency, tool error rates, and microVM scaling.
2. **Layer 2: Agent Quality (AI Evaluations):** Use **AgentCore Evaluations** to continuously score "Goal Completion" and "Context Relevance" in the background, catching hallucinations before users report them.
3. **Layer 3: Business Value (Data Warehouse):** Use your application code to emit `session_outcome` (containment) and `feedback_score` (CSAT) events to Snowflake/S3, allowing your BI tools to calculate WAU, MAU, and ROI (e.g., *$X saved in call center costs per 1% increase in Containment Rate*).