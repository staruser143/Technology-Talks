# Agent Frameworks — Architecture Comparison

Below is an architect-level comparison of Neuro SAN Studio, CrewAI, LangGraph, and Microsoft AutoGen. The focus is on architectural paradigms, orchestration control, enterprise suitability, governance, observability, failure modes, portability, and recommended use cases.

---

## 1. Executive summary

| Framework | Core architectural paradigm | Primary strength | Best fit |
|---|---|---|---|
| Neuro SAN Studio | Declarative, adaptive network of agents | Configuration-driven agent networks with dynamic delegation | Rapid composition of specialist networks and adaptive delegation |
| CrewAI | Role-based teams plus event-driven Flows | Developer productivity and business-task modeling | Departmental automation, research, content and operational workflows |
| LangGraph | Stateful graph and durable orchestration runtime | Explicit control, persistence, recovery, and human-in-the-loop | Production-grade, regulated, long-running, auditable workflows |
| AutoGen (Core) | Actor-model, message-driven multi-agent system | Distributed, asynchronous agent communication | Complex multi-agent collaboration, event-driven systems, distributed architectures |

Short recommendations:

- Choose Neuro SAN when adaptive delegation and declarative agent-network configuration are central.
- Choose CrewAI when development speed and role-based business automation matter most.
- Choose LangGraph when predictability, state control, auditability, and recovery are non-negotiable.
- Choose AutoGen Core when agents are independently deployable actors communicating asynchronously.

---

## 2. Architectural philosophies

### Neuro SAN Studio — Declarative agent network

Neuro SAN treats the solution as a network of specialized agents described primarily by HOCON configuration rather than imperative orchestration. The runtime gives agents latitude to decide how to delegate work.

Conceptual flow:

```
User
  |
  v
Frontman / Entry Agent
  |
  +--> Policy Agent
  |
  +--> Claims Agent
  |       +--> Fraud Agent
  |
  +--> Customer Agent
```

The developer describes:

- available agents
- agent instructions
- tools
- sub-networks
- delegation relationships
- LLM configurations

Architectural character: Neuro SAN sits toward the declarative/adaptive end of the spectrum — attractive when exact execution paths cannot be fully designed in advance. The tradeoff is decreased predictability unless guarded by outer validation and budget/guardrail controls.


### CrewAI — Organizational metaphor (Crews, Tasks, Flows)

CrewAI models agent systems as teams:

- Agent: role, goal, tools, knowledge, behavioral config
- Task: assigned unit of work
- Crew: coordinates agents (sequential/hierarchical)
- Flow: structured, event-driven workflow wrapping agents and crews

Crews enable collaboration; Flows add shared state, events, routing, loops and branching. Production guidance is Flow-first: Flows control state and execution, Crews act as work units.

Conceptual flow:

```
Claims Review Crew
  |-- Claims Researcher
  |-- Coverage Analyst
  |-- Fraud Reviewer
  |-- Recommendation Writer
```

A recommended production architecture is a Flow that orchestrates deterministic validation, crews for work, decision gateways, human approval, and action crews.


### LangGraph — Explicit stateful execution graph

LangGraph models applications as graphs of nodes (agents, functions, tools) and edges (transitions). Graphs include explicit state, conditional routing, checkpoints and interrupts supporting persistence and human review. It is designed for long-running, stateful, auditable processes.

Conceptual flow:

```
START
  v
Classify Request
  +-- simple --> Answer
  +-- complex --> Research Agent
                   v
                 Validate
                /      \
             retry     approve
                \        v
                Human Review
                    |
                   END
```

LangGraph is the best fit when you need answers to questions such as: resume-after-crash, replay/intermediate state inspection, who approved what, and why a branch was chosen.


### AutoGen — Actors and asynchronous messages

AutoGen Core models agents as independent actors that maintain local state and exchange typed messages via an agent runtime. It's explicitly actor-model oriented and supports distributed execution, language interoperability (Python/.NET), modular components and observability.

Conceptual flow:

```
Event Bus / Agent Runtime

Request ---> Planner Agent --message--> Research Agent
                 |                          |
                 +--message------------> Reviewer Agent --> Action Agent
```

AutoGen is natural when treating agents as independently deployable services and when asynchronous, message-driven collaboration is central.

---

## 3. Detailed capability comparison

### Orchestration and execution control (summary)

- Primary abstraction: Neuro SAN (agent network) | CrewAI (agents, tasks, crews, flows) | LangGraph (nodes, edges, state) | AutoGen (actors, messages, runtime)
- Control model: Adaptive delegation | Role/task delegation + event-driven flows | Explicit graph transitions | Message protocols
- Deterministic routing: Moderate | Strong with Flows | Very strong | Possible but developer-defined
- Dynamic collaboration: Very strong | Strong | Supported (explicit) | Very strong
- Shared state model: Conversation/network-oriented | Flow state & crew memory | Explicit typed graph state | Agent-local state + messages
- Long-running execution: Supported | Flows can persist/resume | Core design strength | Runtime-dependent
- Distributed agents: Integration-oriented | Not central | Graph/runtime deployment | Core architectural strength
- Business-user accessibility: Relatively high | Medium–high | Low | Low–medium

Central distinctions:
- LangGraph controls paths.
- CrewAI organizes teams and business processes.
- AutoGen controls communication infrastructure.
- Neuro SAN describes capabilities and lets the network delegate adaptively.


## 4. State, memory and persistence

### Neuro SAN

Neuro SAN is suited to adaptive work-passing, but architects should not treat LLM context as the system of record. Define durable business state, idempotency, transaction boundaries, persistence, replay, compensation, retention and audit history externally.

### CrewAI

Crews support execution memory (short/long-term, entity-oriented). Flows provide typed workflow state. Still, core business state should live in an enterprise persistence layer rather than in-framework memory.

### LangGraph

LangGraph makes state explicit and provides checkpointing/persistence. This is favorable for: approval workflows, long-running cases, resume-after-failure, multi-session conversations, auditing, debugging, and human-modifiable workflow state.

### AutoGen

AutoGen agents own state and exchange messages by contract. For distributed deployments combine AutoGen with durable message infrastructure, an external state store, idempotency repositories, telemetry/trace correlation and dead-letter handling.

---

## 5. Determinism versus autonomy

- High determinism: LangGraph — use when you require prescribed approvals, bounded retries, explicit tool authorization, recoverable steps and auditable AI boundaries.
- Balanced approach: CrewAI Flow + Crews — Flows define process boundaries; Crews add collaboration where useful.
- High adaptive autonomy: Neuro SAN — use when dynamic specialist selection, sub-network invocation and decomposition matter (add strict guardrails for transactional work).
- High decentralized autonomy: AutoGen — use when collaboration and asynchronous actor semantics are the architecture.

---

## 6. Governance and regulated-enterprise suitability

Relative architectural fit (high level):

- Explicit approval gates: LangGraph (interrupts & graph boundaries)
- Traceable business workflow: LangGraph / CrewAI Flow
- Dynamic specialist delegation: Neuro SAN
- Independently owned agents: AutoGen
- Domain stakeholder configuration: Neuro SAN / CrewAI
- Distributed ecosystems: AutoGen
- Predictable transactional workflow: LangGraph
- Rapid business automation: CrewAI

Enterprise controls that should accompany any choice:

- Identity propagation across agents and tools
- Policy enforcement outside prompt text
- Tool gateway for high-risk actions (schema, scope, ownership checks)
- Human-in-the-loop bound to concrete state/action
- Immutable audit trail (agent, model, prompt/config version, tool I/O, policy decisions, approvals)
- Data controls (classification, redaction, retention, regional/process policies)
- Evaluation gates (golden datasets, scenario evaluations, safety/regression thresholds, shadow testing)

---

## 7. Observability and operability

What to capture across frameworks:

- Delegation chain and selected agent + reason
- Model invocations and prompt/config versions
- Tool calls and inputs/outputs
- Token usage/costs
- Handoff count, termination conditions
- Network/configuration version and runtime telemetry

Framework notes:

- Neuro SAN: Studio tooling and configuration-driven deployment settings exist; capture delegation metadata and model/tool usage.
- CrewAI: Callbacks, logs, tracing and enterprise console; Flows provide execution structure for tracing.
- LangGraph: Graph model exposes nodes, checkpoints and streaming events; LangSmith provides tracing, evaluation and prompt management.
- AutoGen: Message-driven systems require strong correlation (trace_id, conversation_id, workflow_id, agent_id, message_id, causation_id, correlation_id, tool_call_id) to be diagnosable.

---

## 8. Failure management

### LangGraph

Strongest for checkpoint & resume, bounded loops, explicit retries, approval interrupts, failure routes, state inspection and compensation branches.

### CrewAI

Flows support production error handling; architects still design retry policies, timeouts, fallbacks, compensation, idempotency and partial-completion handling.

### AutoGen

Architects must design message-delivery guarantees (at-most-once vs at-least-once), duplicate handling, ordering, poison message handling, agent unavailability, backpressure and dead-letter processing.

### Neuro SAN

Adaptive delegation introduces failure modes: cyclic delegation, excessive handoffs, unsuitable specialist selection, uncontrolled context growth, unbounded token usage, unclear termination and inconsistent conclusions. Mitigations:

- maximum delegation depth
- maximum handoffs
- time & token budgets
- agent and tool allowlists
- explicit terminal criteria
- deterministic validation outside the agent network

---

## 9. Vendor lock-in and portability

Recommended layering to reduce lock-in:

Business Application
  |
Agent Orchestration Port
  |
[Neuro SAN Adapter | CrewAI Adapter | LangGraph Adapter | AutoGen Adapter]
  |
Standard Tool / Model Interfaces (MCP, REST, Events, DBs, Model Gateways)

Portability controls:

- Keep domain models independent of framework-specific state classes
- Expose tools via MCP or APIs
- Keep prompts and policies in versioned external repos
- Abstract model selection/credentials
- Export OpenTelemetry traces
- Persist business state outside framework memory
- Keep authorization in a shared policy layer
- Avoid embedding framework-specific objects in business entities

There is no lock-in-free choice; aim to contain coupling at the orchestration adapter boundary.

---

## 10. Use-case-based recommendations

A. Complex insurance or financial case processing

- Example: Intake -> Validation -> Evidence gathering -> Risk analysis -> Human approval -> Transaction -> Notification
- Recommendation: LangGraph (explicit state, approvals, retries, recoverability, auditable routing)
- Use agents only in selected nodes (evidence interpretation, recommendation generation)

B. Research and report-generation automation

- Example: Researcher -> Analyst -> Fact Checker -> Writer -> Reviewer
- Recommendation: CrewAI (maps naturally to roles/tasks; Flow can impose production controls)

C. Enterprise knowledge assistant (many specialist domains)

- Example network: Entry Agent -> HR / Finance / Architecture / Security / Delivery networks
- Recommendation: Neuro SAN (declarative composition and adaptive delegation). Add an external policy gateway before sensitive tools.

D. Cross-domain, distributed agent ecosystem

- Example: Sales Agent <--> Underwriting Agent <--> Compliance Agent <--> External Partner Agent <--> Human Agent
- Recommendation: AutoGen Core (asynchronous, actor-based, runtime-managed message delivery, language interoperability)

E. High-risk transaction execution

- Example: AI recommends changing reservations, policy, payment, or account
- Recommendation: LangGraph as outer control plane — place transactions inside a deterministic workflow (policy validation, human approval, idempotent transaction service). Let agents propose/reason; let deterministic services authorize and commit.

---

## 11. Hybrid architecture

These frameworks can be composed where appropriate. Example enterprise pattern:

API / Channel Layer -> Identity & Policy Gateway -> LangGraph Control Plane -> [Neuro SAN specialist network | CrewAI research crew | AutoGen distributed agent service] -> Validation & Approval -> Enterprise APIs / MCP / Event Bus -> Systems of Record

Responsibilities (example):

- LangGraph: end-to-end process control, durable state, approvals, retries, limited agent autonomy
- Neuro SAN: dynamic discovery and delegation to domain specialists, sub-network coordination
- CrewAI: focused role-based work (research, review, document production)
- AutoGen: independently deployed agents, asynchronous communication and event-driven collaboration

Do not deploy all frameworks unless their execution models solve materially different requirements — otherwise operational complexity increases.

---

## 12. Decision scorecard (relative assessments)

| Criterion | Neuro SAN | CrewAI | LangGraph | AutoGen |
|---|---:|---:|---:|---:|
| Speed to initial prototype | 5 | 5 | 3 | 4 |
| Declarative configuration | 5 | 4 | 3 | 2 |
| Dynamic agent delegation | 5 | 4 | 3 | 5 |
| Deterministic workflow control | 3 | 4 | 5 | 3 |
| Durable state & recovery | 3 | 4 | 5 | 4 |
| Human approval modeling | 3 | 4 | 5 | 3 |
| Distributed agent architecture | 3 | 3 | 3 | 5 |
| Business-role readability | 4 | 5 | 3 | 3 |
| Fine-grained auditability | 3 | 4 | 5 | 4 |
| Low-code / domain-expert accessibility | 5 | 4 | 2 | 2 |
| Complex conditional workflows | 3 | 4 | 5 | 4 |
| Research & experimental collaboration | 4 | 4 | 4 | 5 |

---

## 13. Recommendation for your context

For enterprise scenarios that require governance, security guardrails, HITL, lifecycle management, MCP integration, and auditable workflows, a balanced choice is:

- Primary enterprise orchestration: **LangGraph** (controlled, stateful, explainable, resumable, approval-driven, observable)
- Adaptive specialist layer: **Neuro SAN** (knowledge networks and dynamic delegation)
- Rapid departmental automations: **CrewAI** (bounded research workflows and prototypes)
- Distributed agent ecosystem: **AutoGen Core** (independently deployed agents and asynchronous collaboration)

Bottom line:

- CrewAI: "Define a team and assign work to roles."
- Neuro SAN: "Define a network of capabilities and let agents adaptively delegate."
- LangGraph: "The application controls the workflow."
- AutoGen: "Agents are actors communicating through message protocols."

For regulated production systems, do not rely on an adaptive agent network as the sole transaction controller — place agents inside a deterministic outer workflow for authorization and committing operations.

---

*Edited for readability: reorganized headings, tables, and examples while preserving original recommendations and guidance.*
