Agentic AI patterns are reusable design patterns for building systems that can **plan, act, observe results, use tools, remember context, and adapt** with some degree of autonomy.
The essential patterns fall into a few layers:
1. **Core agent loop**  
2. **Reasoning and planning patterns**  
3. **Action and tool-use patterns**  
4. **Memory and context patterns**  
5. **Reflection and self-improvement patterns**  
6. **Multi-agent orchestration patterns**  
7. **Safety, control, and governance patterns**  
8. **Observability and evaluation patterns**
---
# 1. The Core Agent Loop
At the heart of most agentic systems is a loop like this:
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
This is the foundational pattern behind most agents.
---
# 2. Reasoning and Planning Patterns
These patterns help an agent decide what to do next.
---
## 2.1 ReAct Pattern
**ReAct = Reason + Act**
The agent alternates between reasoning and taking actions.
Example:
```text  
Thought: I need to find the current weather in London.  
Action: search_weather("London")  
Observation: 18°C, rainy.  
Thought: I now have the answer.  
Final Answer: It is 18°C and rainy in London.  
```
### Best for
- Tool-using assistants.  
- Question answering with external data.  
- Simple autonomous tasks.
### Strengths
- Easy to implement.  
- Works well with function calling.  
- Good for step-by-step reasoning.
### Risks
- Can loop endlessly.  
- Can hallucinate observations.  
- Needs stop conditions and validation.
---
## 2.2 Plan-and-Execute Pattern
The agent first creates a plan, then executes it step by step.
Example:
```text  
Goal: Research competitors and write a summary.
Plan:  
1. Identify top competitors.  
2. Gather pricing information.  
3. Compare features.  
4. Summarize findings.  
5. Draft final report.  
```
Then the agent executes each step.
### Best for
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
---
## 2.3 Replanning Pattern
The agent updates its plan when new information appears or when an action fails.
Example:
```text  
Original plan: Book flight for Friday.  
Observation: No flights available Friday.  
New plan: Check Saturday and Sunday.  
```
### Best for
- Dynamic environments.  
- Tasks with uncertain outcomes.  
- Real-world APIs and data sources.
### Key idea
An agent should not blindly follow an old plan. It should adapt when observations contradict assumptions.
---
## 2.4 Task Decomposition Pattern
The agent breaks a large goal into smaller subtasks.
Example:
```text  
Goal: Launch a marketing campaign.
Subtasks:  
- Define audience  
- Draft messaging  
- Create landing page copy  
- Build email sequence  
- Define metrics  
```
### Best for
- Complex goals.  
- Long-running workflows.  
- Multi-agent systems.
### Key risk
If decomposition is poor, the agent may solve the wrong subproblem.
---
## 2.5 Router Pattern
A router classifies the user request and sends it to the right agent, tool, or workflow.
Example:
```text  
User asks for refund → Refund Agent  
User asks technical question → Support Agent  
User asks for invoice → Billing Agent  
```
### Best for
- Customer support.  
- Enterprise assistants.  
- Multi-agent systems.  
- Choosing between specialized tools.
### Strengths
- Reduces complexity.  
- Improves accuracy.  
- Allows specialized agents.
---
# 3. Action and Tool-Use Patterns
Agents become useful when they can act on the world through tools.
---
## 3.1 Function Calling Pattern
The agent calls structured functions instead of generating free text only.
Example:
```json  
{  
"tool": "search_documents",  
"arguments": {  
"query": "Q2 revenue report"  
}  
}  
```
### Best for
- APIs.  
- Databases.  
- Search.  
- Code execution.  
- Business systems.
### Key requirements
- Clear tool schemas.  
- Input validation.  
- Error handling.  
- Timeouts.  
- Retries.
---
## 3.2 Tool Selection Pattern
The agent chooses the best tool from a set of available tools.
Example:
```text  
Available tools:  
- search_web  
- query_database  
- send_email  
- create_ticket
Task: Find customer order status.
Selected tool: query_database  
```
### Best for
- Agents with many tools.  
- Enterprise workflows.  
- Support agents.
### Risk
Too many tools can confuse the model. Tools should be well named and described.
---
## 3.3 Guarded Action Pattern
The agent proposes an action, but the action is checked before execution.
Example:
```text  
Agent wants to delete a record.  
Policy checks:  
- Is the user authorized?  
- Is the record protected?  
- Is this reversible?  
If approved, execute.  
```
### Best for
- Destructive actions.  
- Financial operations.  
- Sending emails.  
- Modifying production systems.
### Key idea
Not every action should be executed automatically.
---
## 3.4 Human-in-the-Loop Pattern
The agent pauses and asks a human for approval or clarification.
Example:
```text  
Agent: I found three possible matching customers.  
Which one should I update?
1. Jane Doe, ID 1001  
2. Jane Doe, ID 2044  
3. Jane D., ID 8890  
```
### Best for
- High-risk actions.  
- Ambiguous requests.  
- Compliance-sensitive workflows.  
- Irreversible operations.
### Common triggers
- Low confidence.  
- High monetary impact.  
- Safety risk.  
- Missing information.  
- Policy violation.
---
## 3.5 Idempotent Action Pattern
Actions should be safe to retry without causing duplicate effects.
Example:
```text  
send_invoice(invoice_id="INV-123", idempotency_key="abc")  
```
If the request is retried, it should not send the invoice twice.
### Best for
- Payments.  
- Email sending.  
- Ticket creation.  
- Database writes.  
- External API calls.
### Key idea
Agents retry often. Actions must be safe under retry.
---
## 3.6 Sandbox Pattern
The agent executes code or commands in an isolated environment.
Example:
```text  
Agent writes Python code.  
Code runs inside a sandboxed container.  
Only approved libraries and file paths are available.  
```
### Best for
- Code execution.  
- Data analysis.  
- File manipulation.  
- Browser automation.
### Safety controls
- Filesystem restrictions.  
- Network restrictions.  
- Time limits.  
- Resource limits.  
- No secrets in sandbox.
---
# 4. Memory and Context Patterns
Agents need memory to maintain state across steps and conversations.
---
## 4.1 Working Memory Pattern
Short-term memory for the current task.
Example:
```text  
Current goal: Book travel.  
Known constraints:  
- Destination: Berlin  
- Dates: Sep 3 to Sep 6  
- Budget: $1200  
```
### Best for
- Single task execution.  
- Conversational agents.  
- Multi-step reasoning.
### Limitation
It disappears or gets summarized after the task ends.
---
## 4.2 Long-Term Memory Pattern
Persistent memory stored outside the model.
Example:
```text  
User prefers window seats.  
User timezone is Europe/Berlin.  
User usually travels for business.  
```
### Storage options
- Vector database.  
- SQL database.  
- Key-value store.  
- Graph database.
### Best for
- Personal assistants.  
- Customer support.  
- Enterprise agents.  
- Long-running projects.
---
## 4.3 Episodic Memory Pattern
Memory of past events or interactions.
Example:
```text  
On 2026-08-01, user asked to summarize Q2 sales.  
The preferred format was bullet points.  
```
### Best for
- Personalization.  
- Learning from prior tasks.  
- Avoiding repeated mistakes.
---
## 4.4 Semantic Memory Pattern
Memory of facts, concepts, or domain knowledge.
Example:
```text  
Customer A has an enterprise contract.  
Refunds above $500 require manager approval.  
```
### Best for
- Business rules.  
- Domain ontologies.  
- Product knowledge.  
- Policy-aware agents.
---
## 4.5 Procedural Memory Pattern
Memory of how to do things.
Example:
```text  
To process a refund:  
1. Verify order.  
2. Check refund window.  
3. Check payment method.  
4. Issue refund.  
5. Send confirmation.  
```
### Best for
- Standard operating procedures.  
- Workflow automation.  
- Enterprise agents.
---
## 4.6 Context Compression Pattern
The agent summarizes old context to avoid exceeding context limits.
Example:
```text  
Raw conversation:  
[50 messages]
Compressed context:  
User is troubleshooting login issue.  
Password reset email was sent.  
MFA is enabled.  
User still cannot access account.  
```
### Best for
- Long conversations.  
- Long-running tasks.  
- Multi-step agents.
### Risk
Important details may be lost if summarization is poor.
---
# 5. Retrieval and Grounding Patterns
Agents often need external knowledge.
---
## 5.1 Retrieval-Augmented Generation Pattern
The agent retrieves relevant documents before answering.
Example:
```text  
User asks: What is our refund policy?  
Agent retrieves policy documents.  
Agent answers using retrieved text.  
```
### Best for
- Enterprise knowledge assistants.  
- Legal, finance, HR, and support agents.  
- Document-heavy workflows.
---
## 5.2 Citation Pattern
The agent cites the source of important claims.
Example:
```text  
Answer: Refunds are allowed within 30 days.  
Source: refund-policy.pdf, section 3.  
```
### Best for
- High-stakes domains.  
- Compliance.  
- Research agents.  
- Customer support.
---
## 5.3 Verification Pattern
The agent checks whether its answer is supported by evidence.
Example:
```text  
Generated answer: The API rate limit is 1000 requests per minute.  
Verification: Search documentation for rate limit.  
Result: Confirmed.  
```
### Best for
- Reducing hallucination.  
- Fact checking.  
- Critical business answers.
---
# 6. Reflection and Self-Correction Patterns
These patterns help agents improve their own outputs.
---
## 6.1 Self-Critique Pattern
The agent reviews its own response before finalizing it.
Example:
```text  
Draft answer: ...  
Critique: The answer does not mention pricing restrictions.  
Revision: Add missing restrictions.  
```
### Best for
- Writing agents.  
- Coding agents.  
- Analysis agents.  
- Research agents.
---
## 6.2 Verifier Pattern
A separate verifier model or rule checks the output.
Example:
```text  
Generator: Produces SQL query.  
Verifier: Checks syntax, permissions, and safety.  
```
### Best for
- Code generation.  
- SQL generation.  
- Structured output.  
- High-risk operations.
---
## 6.3 Test-Driven Agent Pattern
The agent generates output and then tests it.
Example:
```text  
1. Write code.  
2. Run unit tests.  
3. If tests fail, revise code.  
4. Repeat until tests pass.  
```
### Best for
- Software engineering agents.  
- Data pipelines.  
- Automation tasks.
---
## 6.4 Reflexion Pattern
The agent reflects on failures and stores lessons for future attempts.
Example:
```text  
Failure: API call failed because date format was wrong.  
Lesson: Use ISO 8601 dates for this API.  
```
### Best for
- Iterative problem solving.  
- Long-running agents.  
- Learning from errors.
---
# 7. Multi-Agent Orchestration Patterns
For complex tasks, multiple agents may work together.
---
## 7.1 Single Agent Pattern
One agent handles the task end to end.
Example:
```text  
User → Agent → Tools → Result  
```
### Best for
- Simple assistants.  
- Prototypes.  
- Low-complexity workflows.
### Advantage
Simple to build and debug.
---
## 7.2 Supervisor Pattern
A supervisor agent coordinates worker agents.
Example:
```text  
Supervisor Agent  
├── Research Agent  
├── Writing Agent  
└── Review Agent  
```
### Best for
- Complex workflows.  
- Task assignment.  
- Quality control.
### Strengths
- Clear control.  
- Easier to monitor.  
- Good for enterprise systems.
---
## 7.3 Orchestrator-Worker Pattern
An orchestrator breaks work into tasks and assigns them to workers.
Example:
```text  
Orchestrator:  
- Task 1: Collect financial data  
- Task 2: Analyze trends  
- Task 3: Write executive summary
Workers execute tasks.  
```
### Best for
- Parallel work.  
- Research pipelines.  
- Report generation.  
- Data processing.
---
## 7.4 Hierarchical Agent Pattern
Multiple levels of managers and workers.
Example:
```text  
Executive Agent  
├── Operations Manager Agent  
│   ├── Scheduling Agent  
│   └── Inventory Agent  
└── Finance Manager Agent  
├── Invoice Agent  
└── Expense Agent  
```
### Best for
- Large enterprises.  
- Complex organizations.  
- Multi-department workflows.
### Risk
More complexity and coordination overhead.
---
## 7.5 Pipeline Pattern
Agents or steps run in a fixed sequence.
Example:
```text  
Intake Agent → Classification Agent → Draft Agent → Review Agent → Send Agent  
```
### Best for
- Predictable workflows.  
- Document processing.  
- Customer support triage.  
- Compliance processes.
### Strength
Easier to control than fully autonomous agents.
---
## 7.6 Debate Pattern
Multiple agents propose answers, then critique each other.
Example:
```text  
Agent A: Proposal 1  
Agent B: Proposal 2  
Agent C: Critique both  
Judge Agent: Select best answer  
```
### Best for
- Complex reasoning.  
- Decision support.  
- Reducing bias.  
- High-quality analysis.
### Tradeoff
Higher cost and latency.
---
## 7.7 Blackboard Pattern
Agents share a common workspace and contribute to it.
Example:
```text  
Shared state:  
- Customer issue summary  
- Detected intent  
- Draft response  
- Risk flags  
```
Different agents read and update the shared state.
### Best for
- Collaborative problem solving.  
- Event-driven systems.  
- Complex case management.
---
## 7.8 Swarm Pattern
Many agents operate semi-independently, often with local rules.
Example:
```text  
Multiple crawler agents collect data.  
Multiple analyst agents summarize findings.  
Aggregator combines results.  
```
### Best for
- Large-scale exploration.  
- Distributed data collection.  
- Simulation.
### Risk
Harder to govern and debug.
---
# 8. Safety and Control Patterns
Agentic systems need strict boundaries.
---
## 8.1 Policy Pattern
The agent follows explicit rules.
Example:
```text  
Do not expose customer PII.  
Do not approve refunds over $500.  
Do not delete production data.  
Always cite policy documents for compliance questions.  
```
### Best for
- Enterprise agents.  
- Regulated industries.  
- Customer-facing systems.
---
## 8.2 Least Privilege Pattern
The agent only gets the permissions it needs.
Example:
```text  
Support agent can read orders.  
Support agent cannot modify payment methods.  
Support agent can issue refunds only under $50.  
```
### Best for
- Tool access.  
- API permissions.  
- Database access.  
- Filesystem access.
---
## 8.3 Budget Pattern
The agent has limits on time, cost, and actions.
Example:
```text  
Max tokens: 100,000  
Max tool calls: 25  
Max runtime: 5 minutes  
Max spend: $2.00  
```
### Best for
- Preventing runaway agents.  
- Cost control.  
- Production reliability.
---
## 8.4 Allowlist Pattern
The agent can only use approved tools, domains, files, or actions.
Example:
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
### Best for
- Enterprise security.  
- Production agents.  
- Sensitive workflows.
---
## 8.5 Approval Gate Pattern
Certain actions require explicit approval.
Example:
```text  
Agent wants to send email to customer.  
Manager approval required.  
```
### Best for
- Sending external communications.  
- Financial transactions.  
- Legal actions.  
- Data deletion.  
- Production deployments.
---
## 8.6 Rollback Pattern
The system can undo an action if needed.
Example:
```text  
Agent updates CRM record.  
System stores previous state.  
If error detected, restore old state.  
```
### Best for
- Data mutation.  
- Workflow automation.  
- High-risk operations.
---
## 8.7 Audit Trail Pattern
Every decision and action is logged.
Example:
```text  
Timestamp: 2026-08-17T10:00:00Z  
Agent: support-agent-1  
Action: get_order_status  
Input: order_id=12345  
Output: delivered  
Reason: User asked about delivery status.  
```
### Best for
- Compliance.  
- Debugging.  
- Incident response.  
- Enterprise trust.
---
# 9. Observability and Evaluation Patterns
Agents are hard to improve unless you can observe them.
---
## 9.1 Tracing Pattern
Record the full sequence of agent steps.
Example:
```text  
Trace:  
1. User request  
2. Planner output  
3. Tool call  
4. Tool result  
5. Reflection  
6. Final answer  
```
### Best for
- Debugging.  
- Performance monitoring.  
- Compliance.  
- Cost analysis.
---
## 9.2 Evaluation Pattern
Use test cases to measure agent quality.
Example:
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
### Best for
- Production readiness.  
- Regression testing.  
- Model upgrades.  
- Prompt changes.
---
## 9.3 Simulation Pattern
Test agents in simulated environments before production.
Example:
```text  
Simulate:  
- Angry customer  
- Missing order number  
- Refund request above limit  
- Ambiguous product name  
```
### Best for
- Customer support agents.  
- Autonomous workflows.  
- Safety testing.
---
## 9.4 Feedback Loop Pattern
Collect user feedback and use it to improve the agent.
Example:
```text  
User thumbs down response.  
System logs:  
- Prompt  
- Retrieved documents  
- Agent answer  
- User correction  
```
### Best for
- Continuous improvement.  
- Personalization.  
- Fine-tuning datasets.  
- Prompt refinement.
---
# 10. Common Agentic Architectures
Here are the most common high-level architectures.
---
## 10.1 Simple Tool-Using Assistant
```text  
User → LLM → Tool → LLM → Answer  
```
Best for:
- Chatbots.  
- Search assistants.  
- Basic automation.
---
## 10.2 Plan-and-Execute Agent
```text  
User → Planner → Executor → Observer → Result  
```
Best for:
- Multi-step tasks.  
- Research.  
- Document workflows.
---
## 10.3 Router Architecture
```text  
User → Router → Specialist Agent A/B/C  
```
Best for:
- Customer support.  
- Enterprise assistants.  
- Multi-domain systems.
---
## 10.4 Supervisor Architecture
```text  
User → Supervisor → Worker Agents → Supervisor → Result  
```
Best for:
- Complex task coordination.  
- Quality control.  
- Enterprise workflows.
---
## 10.5 Pipeline Architecture
```text  
Step 1 → Step 2 → Step 3 → Step 4  
```
Best for:
- Deterministic workflows.  
- Compliance-sensitive processes.  
- Document processing.
---
## 10.6 Human-Gated Agent
```text  
User → Agent → Proposed Action → Human Approval → Execution  
```
Best for:
- High-risk actions.  
- Regulated environments.  
- Irreversible operations.
---
# 11. The Most Essential Patterns, Simplified
If you only need the core list, these are the essential agentic AI patterns:

| Pattern | Purpose |
|---|---|
| ReAct | Alternate reasoning and action |
| Plan-and-Execute | Create a plan, then execute it |
| Task Decomposition | Break large goals into smaller tasks |
| Tool Use | Let the agent call APIs and functions |
| Human-in-the-Loop | Pause for approval or clarification |
| Memory | Store short-term and long-term context |
| Retrieval-Augmented Generation | Ground answers in external knowledge |
| Reflection | Critique and improve outputs |
| Verifier | Check outputs before acting |
| Router | Direct requests to the right agent or tool |
| Supervisor | Coordinate multiple worker agents |
| Orchestrator-Worker | Assign subtasks to specialized agents |
| Guardrails | Enforce policies, limits, and safety rules |
| Budgeting | Limit tokens, time, cost, and tool calls |
| Audit Trail | Log decisions and actions |
| Evaluation | Test agent behavior systematically |
---
# 12. Minimum Viable Agent Pattern
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
A good production agent is usually not “fully autonomous” from the start. It is a controlled loop with clear limits.
---
# 13. Key Design Principles
Good agentic systems usually follow these principles:
1. **Start narrow**  
Do not build a general autonomous agent first. Solve one workflow well.
2. **Prefer structure over free-form autonomy**  
Use schemas, tools, and defined states.
3. **Make actions inspectable**  
Every action should have a reason, input, output, and result.
4. **Limit permissions**  
Agents should have only the access they need.
5. **Design for failure**  
Tools fail, models hallucinate, and plans become outdated. Build retries, fallbacks, and escalation.
6. **Keep humans in control for high-risk actions**  
Especially for payments, deletion, external communication, and legal actions.
7. **Evaluate continuously**  
Agents need regression tests just like software.
8. **Optimize for observability**  
If you cannot trace the agent’s reasoning and actions, you cannot safely operate it.
---
# 14. Anti-Patterns to Avoid
These are common mistakes in agentic AI systems:
Anti-Pattern	Problem
Unlimited autonomy	Agent can take unsafe actions
Too many tools	Model gets confused about which tool to use
No stop condition	Agent loops forever
No memory strategy	Context gets lost or overloaded
No validation	Agent acts on hallucinated inputs
No audit trail	Impossible to debug or comply
No human approval	High-risk actions happen unintentionally
Monolithic agent	Hard to test, scale, and debug
No evaluation	Quality silently regresses
Treating LLM output as executable truth	Structured outputs must still be validated
---
# Summary
The essential agentic AI patterns are:
- **ReAct**: reason and act iteratively.  
- **Plan-and-Execute**: make a plan, then execute it.  
- **Task Decomposition**: break goals into subtasks.  
- **Tool Use**: call external functions and APIs.  
- **Memory**: maintain short-term and long-term context.  
- **Retrieval/Grounding**: use external knowledge to reduce hallucination.  
- **Reflection**: critique and improve outputs.  
- **Verification**: validate results before acting.  
- **Routing**: direct tasks to the right agent or workflow.  
- **Supervision/Orchestration**: coordinate multiple agents.  
- **Human-in-the-Loop**: require approval for risky or ambiguous actions.  
- **Guardrails**: enforce policies, budgets, and permissions.  
- **Observability**: trace every decision and action.  
- **Evaluation**: test agent behavior systematically.
A practical way to think about it:
```text  
Agentic AI = Goal + Planning + Tools + Memory + Reflection + Control + Observability  
```
