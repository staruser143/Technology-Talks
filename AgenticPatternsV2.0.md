# Agentic AI Patterns: Enterprise Architecture Guide

## Purpose

Agentic AI patterns are reusable design patterns for building systems that can **plan, act, observe results, use tools, remember context, adapt, and operate safely** with some degree of autonomy.

This guide organizes agentic AI patterns from a practical enterprise architecture perspective. It is intended for solution architects, enterprise architects, platform teams, AI engineering teams, and governance stakeholders who need to design agentic systems that are useful, reliable, secure, observable, and cost controlled.

---

## 1. Executive Summary

A practical agentic AI system is not simply an LLM with tools. It is a controlled decision-and-action loop with explicit goals, available tools, state, memory, safety controls, observability, and evaluation.

A useful way to think about it:

```text
Agentic AI = Goal + Planning + Tools + State + Memory + Reflection + Control + Observability + Evaluation
```

The essential pattern families are:

1. **Core agent loop**
2. **Reasoning and planning patterns**
3. **Action and tool-use patterns**
4. **Memory and context patterns**
5. **Retrieval and grounding patterns**
6. **Reflection and self-correction patterns**
7. **State, checkpointing, and resilience patterns**
8. **Multi-agent orchestration patterns**
9. **Integration and tool federation patterns**
10. **Safety, control, and governance patterns**
11. **Observability and evaluation patterns**
12. **Cost, latency, and performance optimization patterns**
13. **Enterprise reference architectures**
14. **Pattern selection guide**
15. **Anti-patterns**

---

## 2. Pattern Classification Matrix

| Category | Patterns |
|---|---|
| Core Loop | Goal-Plan-Act-Observe-Reflect, Stop Conditions |
| Reasoning and Planning | ReAct, Plan-and-Execute, Replanning, Task Decomposition, Router |
| Tooling | Function Calling, Tool Selection, Guarded Action, Sandbox, Idempotent Action |
| Memory and Context | Working Memory, Long-Term Memory, Episodic Memory, Semantic Memory, Procedural Memory, Context Compression |
| Retrieval and Grounding | RAG, Citation, Verification, Evidence-Grounded Answering |
| Reflection and Correction | Self-Critique, Verifier, Test-Driven Agent, Reflexion |
| State and Resilience | Agent State Machine, Checkpointing, Saga/Compensation, Timeout and Retry |
| Multi-Agent | Single Agent, Supervisor, Orchestrator-Worker, Hierarchical, Pipeline, Debate, Blackboard, Swarm |
| Integration | MCP Tool Federation, API Gateway Tool Access, Event-Driven Agent, Workflow Integration |
| Governance | Policy, Least Privilege, Budget, Allowlist, Approval Gate, Rollback, Audit Trail |
| Operations | Tracing, Evaluation, Simulation, Feedback Loop, Shadow Testing |
| Optimization | Prompt Caching, Streaming, Parallel Tool Execution, Model Routing |

---

# Part I: Core Agent Foundations

---

## 3. The Core Agent Loop

At the heart of most agentic systems is a loop:

```text
Goal → Plan → Act → Observe → Reflect → Update → Continue or Stop
```

A minimal agentic system should be able to:

- Understand a goal or task.
- Break it into steps.
- Choose an action or tool.
- Execute the action.
- Observe the result.
- Decide whether the goal is complete.
- Retry, revise, escalate, or stop.

### Key Design Requirement

The loop must not run indefinitely. A production-grade agent needs:

- Maximum iteration limits.
- Maximum tool-call limits.
- Maximum runtime limits.
- Maximum cost limits.
- Explicit completion criteria.
- Escalation rules.
- Failure states.

### Minimal Agent Loop

```text
Input
  ↓
Interpret Goal
  ↓
Create or Update Plan
  ↓
Select Tool or Response
  ↓
Execute Action
  ↓
Observe Result
  ↓
Validate Result
  ↓
Continue, Complete, Escalate, or Fail
```

---

## 4. Workflow vs Agent Decision Framework

- Not every automation needs an agent.
- One of the most important architecture decisions is whether the problem needs deterministic workflow automation, an LLM assistant, or an agentic system.

| Situation | Prefer Workflow | Prefer Agent | Hybrid Approach |
|---|---:|---:|---:|
| Fixed sequence of steps | Yes | No | Sometimes |
| Deterministic business process | Yes | No | Sometimes |
| High uncertainty in next step | No | Yes | Yes |
| Dynamic planning required | No | Yes | Yes |
| Needs natural language reasoning | No | Yes | Yes |
| High regulatory control | Yes | Sometimes | Yes |
| Requires human approval | Yes | Sometimes | Yes |
| Mostly API orchestration | Yes | Sometimes | Yes |
| Requires tool discovery or flexible tool use | No | Yes | Yes |
| Needs explainability and auditability | Yes | Yes, with controls | Yes |

### Decision Rule

- Use a **workflow** when the path is known.

- Use an **agent** when the system must reason about what to do next.

- Use a **hybrid** when the overall business process is controlled, but certain steps require reasoning, tool selection, summarization, or exception handling.

---

# Part II: Reasoning and Planning Patterns

---

## 5. ReAct Pattern

**ReAct = Reason + Act**

The agent alternates between reasoning and taking actions.

```text
Thought: I need to find the current weather in London.
Action: search_weather("London")
Observation: 18°C, rainy.
Thought: I now have the answer.
Final Answer: It is 18°C and rainy in London.
```

### Best For

- Tool-using assistants.
- Question answering with external data.
- Troubleshooting tasks.
- Simple autonomous tasks.

### Strengths

- Easy to implement.
- Works well with function calling.
- Good for iterative reasoning.

### Risks

- Can loop endlessly.
- Can hallucinate observations if tool results are not enforced.
- Needs stop conditions and validation.

### Enterprise Guidance

Use ReAct for bounded tasks where the agent can choose tools, but combine it with tool validation, audit logging, iteration limits, and escalation rules.

---

## 6. Plan-and-Execute Pattern

The agent first creates a plan, then executes it step by step.

```text
Goal: Research competitors and write a summary.

Plan:
1. Identify top competitors.
2. Gather pricing information.
3. Compare features.
4. Summarize findings.
5. Draft final report.
```

### Best For

- Multi-step workflows.
- Research tasks.
- Document generation.
- Complex business processes.

### Strengths

- More structured than pure ReAct.
- Easier to audit.
- Better for long tasks.

### Risks

- Plans can become outdated.
- Requires replanning when conditions change.
- Poor plans can cause the agent to optimize for the wrong objective.

### Enterprise Guidance

Ask the agent to produce a plan, validate the plan, execute one step at a time, and require replanning when observations conflict with assumptions.

---

## 7. Replanning Pattern

The agent updates its plan when new information appears or when an action fails.

```text
Original plan: Book flight for Friday.
Observation: No flights available Friday.
New plan: Check Saturday and Sunday.
```

### Best For

- Dynamic environments.
- Tasks with uncertain outcomes.
- Real-world APIs and data sources.
- Long-running workflows.

### Key Idea

An agent should not blindly follow an old plan. It should adapt when observations contradict assumptions.

### Risks

- Excessive replanning can cause non-termination.
- Frequent replanning can increase latency and cost.

### Controls

- Limit number of replans.
- Require reason for replan.
- Log old plan, new plan, and trigger condition.
- Escalate if repeated replanning occurs.

---

## 8. Task Decomposition Pattern

The agent breaks a large goal into smaller subtasks.

```text
Goal: Launch a marketing campaign.

Subtasks:
- Define audience.
- Draft messaging.
- Create landing page copy.
- Build email sequence.
- Define metrics.
```

### Best For

- Complex goals.
- Long-running workflows.
- Multi-agent systems.
- Enterprise process automation.

### Key Risk

If decomposition is poor, the agent may solve the wrong subproblem.

### Enterprise Guidance

For high-value workflows, validate decomposition before execution. Store subtasks as explicit state so they can be traced, resumed, reassigned, or audited.

---

## 9. Router Pattern

A router classifies the user request and sends it to the right agent, tool, model, workflow, or queue.

```text
User asks for refund      → Refund Agent
User asks technical issue → Support Agent
User asks for invoice     → Billing Agent
User asks legal question  → Legal Knowledge Agent
```

### Best For

- Customer support.
- Enterprise assistants.
- Multi-agent systems.
- Choosing between specialized tools or models.

### Strengths

- Reduces complexity.
- Improves accuracy through specialization.
- Enables different controls for different domains.

### Risks

- Misrouting can lead to poor answers or unsafe actions.
- Routing logic can become hard to govern if prompts are used without tests.

### Enterprise Guidance

Use router evaluation datasets. Track route accuracy, fallback rate, escalation rate, and unsafe routing decisions.

---

# Part III: Action and Tool-Use Patterns

---

## 10. Function Calling Pattern

The agent calls structured functions instead of generating free text only.

```json
{
  "tool": "search_documents",
  "arguments": {
    "query": "Q2 revenue report"
  }
}
```

### Best For

- APIs.
- Databases.
- Search.
- Code execution.
- Business systems.

### Key Requirements

- Clear tool schemas.
- Input validation.
- Output validation.
- Error handling.
- Timeouts.
- Retries.
- Authorization.
- Audit logging.

---

## 11. Tool Selection Pattern

The agent chooses the best tool from a set of available tools.

```text
Available tools:
- search_web
- query_database
- send_email
- create_ticket

Task: Find customer order status.
Selected tool: query_database
```

### Best For

- Agents with many tools.
- Enterprise workflows.
- Support agents.
- Knowledge assistants.

### Risk

Too many tools can confuse the model. Tools should be well named, well described, and scoped to the agent's role.

### Enterprise Guidance

Avoid exposing hundreds of tools to a single agent. Use routing, tool grouping, lazy loading, or MCP toolsets to expose only relevant tools.

---

## 12. Guarded Action Pattern

The agent proposes an action, but the action is checked before execution.

```text
Agent wants to delete a record.

Policy checks:
- Is the user authorized?
- Is the record protected?
- Is this reversible?
- Is human approval required?

If approved, execute.
```

### Best For

- Destructive actions.
- Financial operations.
- Sending emails.
- Modifying production systems.

### Key Idea

Not every action should be executed automatically. High-impact actions require policy checks, approvals, and traceability.

---

## 13. Human-in-the-Loop Pattern

The agent pauses and asks a human for approval, clarification, or decision.

```text
Agent: I found three possible matching customers.
Which one should I update?

1. Jane Doe, ID 1001
2. Jane Doe, ID 2044
3. Jane D., ID 8890
```

### Best For

- High-risk actions.
- Ambiguous requests.
- Compliance-sensitive workflows.
- Irreversible operations.

### Common Triggers

- Low confidence.
- High monetary impact.
- Safety risk.
- Missing information.
- Policy violation.
- External customer communication.

### Design Tip

Human-in-the-loop should be used selectively. Too many approvals create bottlenecks and reduce automation value.

---

## 14. Idempotent Action Pattern

Actions should be safe to retry without causing duplicate effects.

```text
send_invoice(invoice_id="INV-123", idempotency_key="abc")
```

If the request is retried, it should not send the invoice twice.

### Best For

- Payments.
- Email sending.
- Ticket creation.
- Database writes.
- External API calls.

### Key Idea

Agents retry often. Actions must be safe under retry.

### Enterprise Guidance

Use idempotency keys, operation logs, deduplication tables, and unique business transaction identifiers.

---

## 15. Sandbox Pattern

The agent executes code or commands in an isolated environment.

```text
Agent writes Python code.
Code runs inside a sandboxed container.
Only approved libraries and file paths are available.
```

### Best For

- Code execution.
- Data analysis.
- File manipulation.
- Browser automation.

### Safety Controls

- Filesystem restrictions.
- Network restrictions.
- Time limits.
- Resource limits.
- No secrets in sandbox.
- No production write access by default.

---

# Part IV: Memory and Context Patterns

---

## 16. Working Memory Pattern

Short-term memory for the current task.

```text
Current goal: Book travel.
Known constraints:
- Destination: Berlin
- Dates: Sep 3 to Sep 6
- Budget: $1200
```

### Best For

- Single task execution.
- Conversational agents.
- Multi-step reasoning.

### Limitation

It disappears or gets summarized after the task ends.

---

## 17. Long-Term Memory Pattern

Persistent memory stored outside the model.

```text
User prefers window seats.
User timezone is Europe/Berlin.
User usually travels for business.
```

### Storage Options

- SQL database.
- Document database.
- Key-value store.
- Vector database.
- Graph database.

### Best For

- Personal assistants.
- Customer support.
- Enterprise agents.
- Long-running projects.

### Governance Requirements

- Consent.
- Data minimization.
- Retention policy.
- Right to delete.
- Access control.
- Audit trail.

---

## 18. Episodic Memory Pattern

Memory of past events or interactions.

```text
On 2026-08-01, user asked to summarize Q2 sales.
The preferred format was bullet points.
```

### Best For

- Personalization.
- Learning from prior tasks.
- Avoiding repeated mistakes.
- Case history tracking.

---

## 19. Semantic Memory Pattern

Memory of facts, concepts, or domain knowledge.

```text
Customer A has an enterprise contract.
Refunds above $500 require manager approval.
```

### Best For

- Business rules.
- Domain ontologies.
- Product knowledge.
- Policy-aware agents.

---

## 20. Procedural Memory Pattern

Memory of how to do things.

```text
To process a refund:
1. Verify order.
2. Check refund window.
3. Check payment method.
4. Issue refund.
5. Send confirmation.
```

### Best For

- Standard operating procedures.
- Workflow automation.
- Enterprise agents.
- Reusable runbooks.

---

## 21. Context Compression Pattern

The agent summarizes old context to avoid exceeding context limits.

```text
Raw conversation:
[50 messages]

Compressed context:
User is troubleshooting login issue.
Password reset email was sent.
MFA is enabled.
User still cannot access account.
```

### Best For

- Long conversations.
- Long-running tasks.
- Multi-step agents.

### Risk

Important details may be lost if summarization is poor.

### Enterprise Guidance

Use structured summaries, retain source references, and keep decision-critical facts separately from narrative summaries.

---

# Part V: Retrieval and Grounding Patterns

---

## 22. Retrieval-Augmented Generation Pattern

The agent retrieves relevant documents before answering.

```text
User asks: What is our refund policy?
Agent retrieves policy documents.
Agent answers using retrieved text.
```

### Best For

- Enterprise knowledge assistants.
- Legal, finance, HR, and support agents.
- Document-heavy workflows.
- Policy-aware systems.

### Enterprise Guidance

Use RAG when answers must be grounded in current enterprise knowledge. Add citation, verification, and freshness controls for high-stakes use cases.

---

## 23. Citation Pattern

The agent cites the source of important claims.

```text
Answer: Refunds are allowed within 30 days.
Source: refund-policy.pdf, section 3.
```

### Best For

- High-stakes domains.
- Compliance.
- Research agents.
- Customer support.
- Legal and policy workflows.

### Enterprise Guidance

Citations should refer to retrievable sources, not vague statements such as "according to policy." For regulated use cases, preserve source document ID, version, section, timestamp, and retrieval score.

---

## 24. Verification Pattern

The agent checks whether its answer is supported by evidence.

```text
Generated answer: The API rate limit is 1000 requests per minute.
Verification: Search documentation for rate limit.
Result: Confirmed.
```

### Best For

- Reducing hallucination.
- Fact checking.
- Critical business answers.
- Legal, finance, and compliance workflows.

### Enterprise Guidance

For high-risk domains, separate answer generation from answer verification. Verification may be rule-based, retrieval-based, model-based, or human-reviewed.

---

# Part VI: Reflection and Self-Correction Patterns

---

## 25. Self-Critique Pattern

The agent reviews its own response before finalizing it.

```text
Draft answer: ...
Critique: The answer does not mention pricing restrictions.
Revision: Add missing restrictions.
```

### Best For

- Writing agents.
- Coding agents.
- Analysis agents.
- Research agents.

### Risk

Self-critique can create false confidence if the same model validates its own mistaken assumptions.

---

## 26. Verifier Pattern

A separate verifier model or rule checks the output.

```text
Generator: Produces SQL query.
Verifier: Checks syntax, permissions, and safety.
```

### Best For

- Code generation.
- SQL generation.
- Structured output.
- High-risk operations.

### Enterprise Guidance

Use deterministic checks where possible. Model-based verification is useful, but not a substitute for schema validation, policy checks, unit tests, and authorization.

---

## 27. Test-Driven Agent Pattern

The agent generates output and then tests it.

```text
1. Write code.
2. Run unit tests.
3. If tests fail, revise code.
4. Repeat until tests pass.
```

### Best For

- Software engineering agents.
- Data pipelines.
- Automation tasks.
- Infrastructure-as-code generation.

---

## 28. Reflexion Pattern

The agent reflects on failures and stores lessons for future attempts.

```text
Failure: API call failed because date format was wrong.
Lesson: Use ISO 8601 dates for this API.
```

### Best For

- Iterative problem solving.
- Long-running agents.
- Learning from errors.
- Repeated workflows.

### Governance Note

Stored lessons should be reviewed before being promoted to shared procedural memory. Otherwise, agents may learn incorrect or unsafe shortcuts.

---

# Part VII: State, Checkpointing, and Resilience Patterns

---

## 29. Agent State Machine Pattern

Production agents should have explicit states rather than operating as unstructured conversations.

```text
Created
  ↓
Planning
  ↓
Executing
  ↓
WaitingForTool
  ↓
WaitingForHuman
  ↓
Completed
```

Failure states should also be explicit:

```text
Failed
Cancelled
TimedOut
Escalated
RollbackRequired
```

### Best For

- Long-running workflows.
- Human approval flows.
- Enterprise business processes.
- Auditable agent execution.

### Strengths

- Improves reliability.
- Enables resumability.
- Supports monitoring.
- Makes failure handling explicit.
- Simplifies audit trails.

### Enterprise Guidance

Represent agent state in a durable store. Do not rely only on conversation history to determine execution state.

---

## 30. Checkpointing Pattern

The agent saves progress after meaningful steps so execution can resume after interruption.

```text
Step 1: Extract chapter 1 to 5 → Save checkpoint
Step 2: Extract chapter 6 to 10 → Save checkpoint
Step 3: Tool failure occurs
Resume from checkpoint after chapter 10
```

### Best For

- Long-document processing.
- Multi-hour workflows.
- Batch extraction.
- Human approval pauses.
- Unreliable external APIs.

### Strengths

- Avoids starting from scratch.
- Reduces cost after failure.
- Improves resilience.
- Supports partial completion.

### Risks

- Poor checkpoint design can preserve corrupted state.
- Overly frequent checkpointing can add complexity.

### Enterprise Guidance

Checkpoint after irreversible actions, expensive operations, and logical milestones. Store input, output, status, timestamp, and resumability metadata.

---

## 31. Timeout, Retry, and Backoff Pattern

Agents must handle tool failures, slow APIs, network errors, and rate limits.

```text
Tool call fails
  ↓
Retry with exponential backoff
  ↓
Fallback to alternate tool
  ↓
Escalate if still failing
```

### Best For

- External API calls.
- Database queries.
- Web automation.
- Long-running tasks.

### Enterprise Guidance

Retries should be bounded and idempotent. Log every retry and include reason, tool name, error type, and outcome.

---

## 32. Saga and Compensation Pattern

For multi-step actions, the system defines compensating actions for failures.

```text
1. Reserve inventory
2. Charge payment
3. Create shipment

If shipment creation fails:
- Refund payment
- Release inventory
```

### Best For

- Distributed business transactions.
- Financial operations.
- Order management.
- Multi-system workflows.

### Enterprise Guidance

Do not assume rollback is always possible. Some actions require compensation rather than reversal.

---

# Part VIII: Multi-Agent Orchestration Patterns

---

## 33. Single Agent Pattern

One agent handles the task end to end.

```text
User → Agent → Tools → Result
```

### Best For

- Simple assistants.
- Prototypes.
- Low-complexity workflows.

### Advantage

Simple to build and debug.

---

## 34. Supervisor Pattern

A supervisor agent coordinates worker agents.

```text
Supervisor Agent
├── Research Agent
├── Writing Agent
└── Review Agent
```

### Best For

- Complex workflows.
- Task assignment.
- Quality control.
- Enterprise systems.

### Strengths

- Clear control.
- Easier to monitor.
- Good for enterprise systems.

### Risks

- Supervisor becomes a bottleneck.
- Poor task assignment affects all downstream agents.

---

## 35. Orchestrator-Worker Pattern

An orchestrator breaks work into tasks and assigns them to workers.

```text
Orchestrator:
- Task 1: Collect financial data
- Task 2: Analyze trends
- Task 3: Write executive summary

Workers execute tasks.
```

### Best For

- Parallel work.
- Research pipelines.
- Report generation.
- Data processing.

---

## 36. Hierarchical Agent Pattern

Multiple levels of managers and workers.

```text
Executive Agent
├── Operations Manager Agent
│   ├── Scheduling Agent
│   └── Inventory Agent
└── Finance Manager Agent
    ├── Invoice Agent
    └── Expense Agent
```

### Best For

- Large enterprises.
- Complex organizations.
- Multi-department workflows.

### Risk

More complexity and coordination overhead.

---

## 37. Pipeline Pattern

Agents or steps run in a fixed sequence.

```text
Intake Agent → Classification Agent → Draft Agent → Review Agent → Send Agent
```

### Best For

- Predictable workflows.
- Document processing.
- Customer support triage.
- Compliance processes.

### Strength

Easier to control than fully autonomous agents.

---

## 38. Debate Pattern

Multiple agents propose answers, then critique each other.

```text
Agent A: Proposal 1
Agent B: Proposal 2
Agent C: Critique both
Judge Agent: Select best answer
```

### Best For

- Complex reasoning.
- Decision support.
- Reducing bias.
- High-quality analysis.

### Tradeoff

Higher cost and latency.

---

## 39. Blackboard Pattern

Agents share a common workspace and contribute to it.

```text
Shared state:
- Customer issue summary
- Detected intent
- Draft response
- Risk flags
```

Different agents read and update the shared state.

### Best For

- Collaborative problem solving.
- Event-driven systems.
- Complex case management.

### Enterprise Guidance

Use versioning and concurrency controls. Shared state should not become an uncontrolled memory dump.

---

## 40. Swarm Pattern

Many agents operate semi-independently, often with local rules.

```text
Multiple crawler agents collect data.
Multiple analyst agents summarize findings.
Aggregator combines results.
```

### Best For

- Large-scale exploration.
- Distributed data collection.
- Simulation.

### Risk

Harder to govern and debug.

### Enterprise Guidance

Avoid swarm-style autonomy for regulated or high-impact workflows unless governance and observability are mature.

---

# Part IX: Integration and Tool Federation Patterns

---

## 41. MCP Tool Federation Pattern

A tool federation layer exposes enterprise tools, prompts, and resources to agents through a standard interface.

```text
Agent or Agent Runtime
  ↓
MCP Client / Connector
  ↓
MCP Server or Tool Gateway
  ↓
Enterprise Systems
```

### Best For

- Standardized access to enterprise tools.
- Tool discovery.
- Multi-system integration.
- Separating agent logic from backend integrations.
- Governed tool exposure.

### Strengths

- Reduces point-to-point integration.
- Enables reusable tool servers.
- Allows specialized teams to own tool interfaces.
- Supports consistent governance across tools.

### Risks

- Tool sprawl if not governed.
- Authorization ambiguity if identity propagation is poorly designed.
- Sensitive tools may be exposed too broadly.

### Enterprise Guidance

Use MCP-style access when multiple agents need reusable access to enterprise systems. Place authentication, authorization, logging, schema validation, and policy enforcement close to the tool boundary.

---

## 42. API Gateway Tool Access Pattern

Agents access business systems through an API gateway rather than direct backend connections.

```text
Agent
  ↓
Tool Runtime
  ↓
API Gateway
  ↓
Business APIs
```

### Best For

- Enterprise API governance.
- Rate limiting.
- Authentication.
- Centralized logging.
- Existing API management platforms.

### Enterprise Guidance

The gateway should not blindly trust the agent. It should validate user identity, scopes, payloads, policy, and rate limits.

---

## 43. Event-Driven Agent Pattern

An agent is triggered by business events rather than only by chat messages.

```text
Kafka / Event Bus / Queue
  ↓
Agent Trigger
  ↓
Reasoning and Tool Use
  ↓
Business Action
  ↓
Publish Outcome Event
```

### Best For

- Claims processing.
- Fraud investigation.
- Customer lifecycle automation.
- Supply chain exceptions.
- IT operations.
- Compliance monitoring.

### Strengths

- Integrates agents into enterprise event flows.
- Enables asynchronous processing.
- Supports event replay and auditability.
- Works well with scalable backend systems.

### Risks

- Event storms can cause runaway cost.
- Duplicate events can lead to duplicate actions.
- Requires idempotency, throttling, and dead-letter handling.

### Enterprise Guidance

Use event-driven agents when business events need interpretation, prioritization, enrichment, or exception handling. Combine with idempotent actions, budgets, and observability.

---

## 44. Workflow Integration Pattern

The agent operates inside a deterministic workflow engine.

```text
Workflow Step 1
  ↓
Agent Reasoning Step
  ↓
Approval Step
  ↓
System Update Step
```

### Best For

- Regulated business processes.
- Enterprise approvals.
- Document processing.
- Claims, underwriting, finance, and HR workflows.

### Strengths

- Keeps the business process controlled.
- Uses agents only where reasoning is needed.
- Improves auditability.

---

# Part X: Safety, Control, and Governance Patterns

---

## 45. Policy Pattern

The agent follows explicit rules.

```text
Do not expose customer PII.
Do not approve refunds over $500.
Do not delete production data.
Always cite policy documents for compliance questions.
```

### Best For

- Enterprise agents.
- Regulated industries.
- Customer-facing systems.

### Enterprise Guidance

Policies should be machine-enforceable where possible. Natural language policy alone is not enough for high-risk actions.

---

## 46. Least Privilege Pattern

The agent only gets the permissions it needs.

```text
Support agent can read orders.
Support agent cannot modify payment methods.
Support agent can issue refunds only under $50.
```

### Best For

- Tool access.
- API permissions.
- Database access.
- Filesystem access.

### Enterprise Guidance

Use user-context-aware authorization. The agent should not be able to perform actions that the user is not authorized to perform.

---

## 47. Budget Pattern

The agent has limits on time, cost, tokens, and actions.

```text
Max tokens: 100,000
Max tool calls: 25
Max runtime: 5 minutes
Max spend: $2.00
```

### Best For

- Preventing runaway agents.
- Cost control.
- Production reliability.
- Multi-tenant systems.

### Enterprise Guidance

Budgets should exist at multiple levels: request, user, tenant, application, environment, and organization.

---

## 48. Allowlist Pattern

The agent can only use approved tools, domains, files, or actions.

```text
Allowed tools:
- search_internal_docs
- create_ticket
- get_order_status

Not allowed:
- delete_user
- export_database
- run_shell_command
```

### Best For

- Enterprise security.
- Production agents.
- Sensitive workflows.

---

## 49. Approval Gate Pattern

Certain actions require explicit approval.

```text
Agent wants to send email to customer.
Manager approval required.
```

### Best For

- Sending external communications.
- Financial transactions.
- Legal actions.
- Data deletion.
- Production deployments.

### Enterprise Guidance

Approval gates should include enough evidence for reviewers to decide quickly: proposed action, rationale, impacted entity, risk level, policy basis, and rollback/compensation plan.

---

## 50. Rollback Pattern

The system can undo an action if needed.

```text
Agent updates CRM record.
System stores previous state.
If error detected, restore old state.
```

### Best For

- Data mutation.
- Workflow automation.
- High-risk operations.

### Enterprise Guidance

For actions that cannot be truly rolled back, define compensating actions instead.

---

## 51. Audit Trail Pattern

Every decision and action is logged.

```text
Timestamp: 2026-08-17T10:00:00Z
Agent: support-agent-1
Action: get_order_status
Input: order_id=12345
Output: delivered
Reason: User asked about delivery status.
```

### Best For

- Compliance.
- Debugging.
- Incident response.
- Enterprise trust.

### Enterprise Guidance

Audit logs should capture user identity, agent identity, model version, prompt version, tool call, tool result, policy decision, approval decision, and final outcome.

---

# Part XI: Observability and Evaluation Patterns

---

## 52. Tracing Pattern

Record the full sequence of agent steps.

```text
Trace:
1. User request
2. Planner output
3. Tool call
4. Tool result
5. Reflection
6. Final answer
```

### Best For

- Debugging.
- Performance monitoring.
- Compliance.
- Cost analysis.

### Enterprise Guidance

A trace should connect the user request, model calls, tool calls, retrieval calls, policy checks, approvals, cost, latency, and final result.

---

## 53. Evaluation Pattern

Use test cases to measure agent quality.

```text
Test: User asks for refund policy.
Expected: Agent retrieves policy and answers correctly.
Metrics:
- Correct tool used
- Correct source cited
- Answer accuracy
- Latency
- Cost
```

### Best For

- Production readiness.
- Regression testing.
- Model upgrades.
- Prompt changes.
- Tool changes.

### Enterprise Evaluation Metrics

Measure agentic systems across multiple dimensions:

| Metric | Purpose |
|---|---|
| Task Success Rate | Did the agent complete the intended task? |
| Answer Accuracy | Was the response correct? |
| Groundedness | Was the response supported by retrieved evidence? |
| Citation Accuracy | Did citations support the claim? |
| Tool Selection Accuracy | Did the agent choose the right tool? |
| Tool Execution Success Rate | Did tool calls complete successfully? |
| Hallucination Rate | Did the agent produce unsupported claims? |
| Policy Violation Rate | Did the agent violate safety or compliance policies? |
| Human Escalation Rate | How often did the agent require human support? |
| Latency | How long did the full task take? |
| Time to First Token | How quickly did the response start? |
| Cost Per Task | What was the average cost to complete the task? |
| Retry Rate | How often did the agent retry actions? |
| Replanning Rate | How often did the agent change plans? |
| User Satisfaction | Did users accept or approve the result? |

---

## 54. Simulation Pattern

Test agents in simulated environments before production.

```text
Simulate:
- Angry customer
- Missing order number
- Refund request above limit
- Ambiguous product name
```

### Best For

- Customer support agents.
- Autonomous workflows.
- Safety testing.
- Production readiness.

---

## 55. Shadow Testing Pattern

Run the agent in parallel with the existing process without allowing it to take real actions.

```text
Production request
  ↓
Existing human/process handles request
  ↓
Agent produces recommendation in shadow mode
  ↓
Compare agent output with real outcome
```

### Best For

- Production-like validation.
- High-risk workflows.
- Measuring readiness before automation.
- Comparing agent recommendations with human decisions.

### Enterprise Guidance

Shadow testing is valuable when stakeholders need evidence before granting action permissions. Capture decisions, confidence, suggested actions, policy flags, and comparison against actual outcomes.

---

## 56. Feedback Loop Pattern

Collect user feedback and use it to improve the agent.

```text
User thumbs down response.
System logs:
- Prompt
- Retrieved documents
- Agent answer
- User correction
```

### Best For

- Continuous improvement.
- Personalization.
- Fine-tuning datasets.
- Prompt refinement.

### Risk

Feedback loops can reinforce bad behavior if the system trains on its own prior decisions without independent review.

---

# Part XII: Cost, Latency, and Performance Optimization Patterns

---

## 57. Prompt Caching Pattern

Stable prompt content is cached and reused across requests.

```text
Cached content:
- System instructions
- Tool definitions
- Enterprise policies
- Long reference context

Per-request content:
- User task
- Current state
- Retrieved evidence
```

### Best For

- Large system prompts.
- Long policy instructions.
- Repeated task patterns.
- Multi-turn sessions.
- High-volume enterprise assistants.

### Strengths

- Reduces cost.
- Reduces latency.
- Improves scalability.

### Risks

- Cached content can become stale.
- Prompt changes may invalidate caches.
- Dynamic content should not be cached accidentally.

### Enterprise Guidance

Separate stable context from dynamic context. Version cached prompt blocks and invalidate cache when policy, tool definitions, or safety instructions change.

---

## 58. Streaming Response Pattern

The agent streams partial output while processing continues.

```text
User asks a long question
  ↓
Agent starts response quickly
  ↓
Remaining answer streams over time
```

### Best For

- Improving perceived responsiveness.
- Long-form answers.
- Interactive assistants.
- User-facing chat experiences.

### Important Metric

**Time to First Token (TTFT)** measures how long it takes before the first visible response token is returned to the user.

### Enterprise Guidance

Streaming improves user experience, but it does not automatically reduce total task time. Use it for conversational responsiveness, not as a substitute for backend performance optimization.

---

## 59. Parallel Tool Execution Pattern

The agent or runtime executes independent tool calls in parallel.

```text
Task: Prepare account summary

Parallel calls:
- Get customer profile
- Get open tickets
- Get recent orders
- Get contract status

Merge results and respond
```

### Best For

- Independent data retrieval.
- Dashboard-style summaries.
- Multi-source enrichment.
- Reducing latency.

### Risks

- Harder error handling.
- Higher burst load.
- More complex tracing.

---

## 60. Model Routing Pattern

Requests are routed to different models based on complexity, risk, cost, or latency requirements.

```text
Simple classification → smaller model
Complex reasoning     → larger model
High-risk decision    → larger model + verifier
```

### Best For

- Cost optimization.
- Latency optimization.
- Large-scale enterprise AI platforms.
- Multi-model architectures.

### Enterprise Guidance

Track model routing quality. Incorrectly routing complex tasks to smaller models can silently reduce quality.

---

# Part XIII: Common Agentic Architectures

---

## 61. Simple Tool-Using Assistant

```text
User → LLM → Tool → LLM → Answer
```

### Best For

- Chatbots.
- Search assistants.
- Basic automation.

---

## 62. Plan-and-Execute Agent

```text
User → Planner → Executor → Observer → Result
```

### Best For

- Multi-step tasks.
- Research.
- Document workflows.

---

## 63. Router Architecture

```text
User → Router → Specialist Agent A/B/C
```

### Best For

- Customer support.
- Enterprise assistants.
- Multi-domain systems.

---

## 64. Supervisor Architecture

```text
User → Supervisor → Worker Agents → Supervisor → Result
```

### Best For

- Complex task coordination.
- Quality control.
- Enterprise workflows.

---

## 65. Pipeline Architecture

```text
Step 1 → Step 2 → Step 3 → Step 4
```

### Best For

- Deterministic workflows.
- Compliance-sensitive processes.
- Document processing.

---

## 66. Human-Gated Agent

```text
User → Agent → Proposed Action → Human Approval → Execution
```

### Best For

- High-risk actions.
- Regulated environments.
- Irreversible operations.

---

## 67. Event-Driven Agent Architecture

```text
Event Bus → Agent Trigger → Tool Use → Decision → Action/Event Output
```

### Best For

- Backend automation.
- Exception handling.
- Fraud, claims, operations, and monitoring workflows.

---

## 68. MCP-Based Enterprise Tool Architecture

```text
Agent Runtime
  ↓
MCP Client / Connector
  ↓
MCP Tool Servers
  ↓
Enterprise APIs, Databases, SaaS Platforms, Knowledge Systems
```

### Best For

- Enterprise tool reuse.
- Cross-agent integration.
- Standardized tool governance.
- Platform engineering teams.

---

# Part XIV: Enterprise Reference Architectures

---

## 69. Knowledge Agent Reference Architecture

```text
User
  ↓
Agent
  ↓
Retriever
  ↓
Vector Index / Search Index
  ↓
Enterprise Content Sources
  ↓
Citation and Verification
  ↓
Answer
```

### Use Cases

- HR policy assistant.
- Legal research assistant.
- Technical documentation assistant.
- Enterprise knowledge assistant.

### Core Patterns

- RAG.
- Citation.
- Verification.
- Context compression.
- Evaluation.

---

## 70. Business Process Agent Reference Architecture

```text
User or Workflow Trigger
  ↓
Agent Planner
  ↓
Tool Execution Layer
  ↓
Policy and Approval Layer
  ↓
Business Systems
  ↓
Audit Trail and Monitoring
```

### Use Cases

- Customer refund processing.
- Claims triage.
- Contract review workflow.
- IT service management automation.

### Core Patterns

- Plan-and-Execute.
- Guarded Action.
- Human-in-the-Loop.
- Least Privilege.
- Audit Trail.
- Checkpointing.

---

## 71. Event-Driven Agent Reference Architecture

```text
Kafka / Queue / Event Grid
  ↓
Agent Trigger
  ↓
State Store and Checkpoint
  ↓
Reasoning and Tool Execution
  ↓
Outcome Event
  ↓
Monitoring and Dead Letter Handling
```

### Use Cases

- Fraud alerts.
- Supply chain exceptions.
- Customer churn intervention.
- Monitoring and incident enrichment.

### Core Patterns

- Event-Driven Agent.
- Idempotent Action.
- State Machine.
- Checkpointing.
- Budget.
- Tracing.

---

## 72. Multi-Agent Enterprise Assistant Reference Architecture

```text
User
  ↓
Router
  ↓
Specialist Agents
  ├── HR Agent
  ├── Finance Agent
  ├── Legal Agent
  └── Support Agent
  ↓
Supervisor / Verifier
  ↓
Final Response or Action
```

### Use Cases

- Enterprise copilot.
- Department-specific assistant.
- Internal service desk assistant.
- Knowledge + action assistant.

### Core Patterns

- Router.
- Supervisor.
- Tool Selection.
- RAG.
- Least Privilege.
- Audit Trail.
- Evaluation.

---

# Part XV: Pattern Selection Guide

---

## 73. Quick Decision Guide

| Need | Recommended Pattern |
|---|---|
| Need external knowledge | RAG + Citation + Verification |
| Need dynamic tool use | ReAct + Function Calling |
| Need multi-step control | Plan-and-Execute |
| Need adaptation after failures | Replanning |
| Need reusable business process automation | Workflow Integration + Agent Reasoning Step |
| Need high-risk action control | Guarded Action + Approval Gate |
| Need safe retries | Idempotent Action |
| Need long-running execution | State Machine + Checkpointing |
| Need human review | Human-in-the-Loop |
| Need many specialist capabilities | Router + Supervisor |
| Need enterprise tool reuse | MCP Tool Federation |
| Need event-based automation | Event-Driven Agent |
| Need lower cost | Prompt Caching + Model Routing |
| Need better responsiveness | Streaming Response |
| Need production readiness | Evaluation + Simulation + Shadow Testing |
| Need compliance | Least Privilege + Audit Trail + Policy Pattern |

---

## 74. Recommended Pattern Bundles

### Knowledge Assistant Bundle

```text
RAG + Citation + Verification + Context Compression + Evaluation
```

### Business Action Agent Bundle

```text
Plan-and-Execute + Function Calling + Guarded Action + Approval Gate + Audit Trail
```

### Long-Running Agent Bundle

```text
State Machine + Checkpointing + Timeout/Retry + Idempotent Action + Tracing
```

### Enterprise Multi-Agent Bundle

```text
Router + Supervisor + Specialist Agents + Least Privilege + Evaluation
```

### Event-Driven Automation Bundle

```text
Event-Driven Agent + Idempotent Action + Budget + State Store + Dead Letter Handling
```

### Cost-Optimized Agent Bundle

```text
Prompt Caching + Model Routing + Parallel Tool Execution + Budget Pattern
```

---

# Part XVI: Minimum Viable Agent Pattern

---

## 75. Minimum Viable Agent

If you are building your first agent, start with this pattern:

```text
1. Define goal and constraints.
2. Give the agent a small set of tools.
3. Use a planning loop.
4. Require structured tool calls.
5. Validate tool inputs and outputs.
6. Add memory for task state.
7. Add stop conditions.
8. Add human approval for risky actions.
9. Log every step.
10. Evaluate with test cases.
```

A good production agent is usually not fully autonomous from the start. It is a controlled loop with clear limits.

### Minimum Production Controls

```text
- Identity and authorization
- Tool schema validation
- Input/output validation
- Stop conditions
- Token and cost budgets
- Audit trail
- Error handling
- Human escalation
- Evaluation suite
- Observability dashboard
```

---

# Part XVII: Key Design Principles

---

## 76. Design Principles

Good agentic systems usually follow these principles:

1. **Start narrow**
   - Do not build a general autonomous agent first. Solve one workflow well.

2. **Prefer structure over free-form autonomy**
   - Use schemas, tools, states, and defined workflows.

3. **Make actions inspectable**
   - Every action should have a reason, input, output, result, and policy decision.

4. **Limit permissions**
   - Agents should have only the access they need.

5. **Design for failure**
   - Tools fail, models hallucinate, and plans become outdated. Build retries, fallbacks, checkpoints, and escalation.

6. **Keep humans in control for high-risk actions**
   - Especially for payments, deletion, external communication, and legal actions.

7. **Evaluate continuously**
   - Agents need regression tests just like software.

8. **Optimize for observability**
   - If you cannot trace the agent's reasoning and actions, you cannot safely operate it.

9. **Separate reasoning from execution**
   - Let the agent propose actions, but let controlled systems validate and execute them.

10. **Govern memory explicitly**
    - Memory should be consented, scoped, auditable, and deletable.

11. **Treat prompts as versioned software artifacts**
    - Prompt changes can break behavior and must be tested, reviewed, and tracked.

12. **Prefer hybrid architectures for enterprise workflows**
    - Combine deterministic workflows with agentic reasoning where it adds value.

---

# Part XVIII: Anti-Patterns to Avoid

---

## 77. Common Anti-Patterns

| Anti-Pattern | Problem |
|---|---|
| Unlimited autonomy | Agent can take unsafe actions. |
| Agent everywhere | Uses agents for deterministic workflows that do not need reasoning. |
| Too many tools | Model gets confused about which tool to use. |
| Tool explosion | Hundreds of exposed tools reduce selection accuracy and governance. |
| No stop condition | Agent loops forever. |
| Unlimited replanning | Agent keeps changing plans and never completes. |
| No memory strategy | Context gets lost or overloaded. |
| Hidden memory | Agent behavior changes unexpectedly because memory is not transparent. |
| No validation | Agent acts on hallucinated or malformed inputs. |
| No grounding | Hallucination risk increases. |
| No audit trail | Impossible to debug or comply. |
| No human approval | High-risk actions happen unintentionally. |
| Human approval bottleneck | Overuse of approvals prevents scale. |
| Monolithic agent | Hard to test, scale, and debug. |
| Autonomous write access | The agent can mutate systems without sufficient control. |
| No evaluation | Quality silently regresses. |
| Treating LLM output as executable truth | Structured outputs must still be validated. |
| Training on own mistakes | Feedback loops reinforce incorrect decisions. |
| Caching dynamic context | Stale or sensitive data may be reused incorrectly. |

---

# Part XIX: Final Summary

The essential agentic AI patterns are:

- **ReAct**: reason and act iteratively.
- **Plan-and-Execute**: make a plan, then execute it.
- **Task Decomposition**: break goals into subtasks.
- **Tool Use**: call external functions and APIs.
- **Memory**: maintain short-term and long-term context.
- **Retrieval and Grounding**: use external knowledge to reduce hallucination.
- **Reflection**: critique and improve outputs.
- **Verification**: validate results before acting.
- **Routing**: direct tasks to the right agent or workflow.
- **Supervision and Orchestration**: coordinate multiple agents.
- **State Machine**: manage explicit execution states.
- **Checkpointing**: resume long-running work safely.
- **Event-Driven Agents**: respond to asynchronous business events.
- **MCP Tool Federation**: expose governed enterprise tools through reusable interfaces.
- **Human-in-the-Loop**: require approval for risky or ambiguous actions.
- **Guardrails**: enforce policies, budgets, and permissions.
- **Prompt Caching**: reduce latency and cost for repeated stable context.
- **Observability**: trace every decision and action.
- **Evaluation**: test agent behavior systematically.

A practical enterprise formula:

```text
Production Agent = Controlled Reasoning Loop
                 + Governed Tools
                 + Explicit State
                 + Durable Memory
                 + Human Oversight
                 + Policy Enforcement
                 + Observability
                 + Evaluation
                 + Cost Controls
```

For enterprise systems, the best architecture is usually not a fully autonomous agent. It is a **hybrid system** where deterministic workflows provide control, and agentic reasoning is applied selectively where uncertainty, language understanding, planning, or tool selection is required.
