# Agent Harness

For LangGraph + AgentCore healthcare architecture, the most important thing to understand is:
- AgentCore Harness is not another agent capability like Gateway or Memory.
- It is a fully managed agent execution environment that eliminates the need to write the agent orchestration loop yourself.

## What is AgentCore Harness?

AWS describes Harness as a managed agent loop where you declare:
```
Model
Instructions/System Prompt
Tools
Skills
Memory
Environment settings
```
and AgentCore handles:

```
Agent reasoning loop
Tool invocation
Context management
Session management
Compute
Scaling
Identity
Observability
Runtime environment
```
for us

**Without Harness:**

We write:
```
while(not_done):
    call LLM
    inspect tool calls
    execute tools
    return results
    update state
    handle failures
```

**With Harness:**

We configure:
```
Model
Prompt
Tools
```
- AgentCore runs the loop.

## AgentCore Harness vs Runtime

### AgentCore Runtime

We own the orchestration.
```
LangGraph
CrewAI
OpenAI Agents SDK
Custom Framework
```

We write the agent logic.
```
+-----------------------+
| LangGraph             |
| State Machine         |
| Graph Routing         |
| Node Logic            |
+-----------------------+
            |
```
         v
## AgentCore Runtime
Runtime provides secure hosting and scaling.

AgentCore Harness

AWS owns the orchestration.
```
Model
Prompt
Tools
Memory


↓

AgentCore Harness


↓

Reasoning Loop
Tool Calls
Context Management
Observability
```

No LangGraph required.

**What Harness Includes**

Each Harness session runs in an isolated microVM with:
```
Stateful sessions
Filesystem
Shell access
Tool access
Memory
Identity integration
Observability integration
```

AWS states that each session is stateful and runs in a secure isolated microVM backed by AgentCore Runtime infrastructure.

Conceptually:
```
Harness Session

├── Agent
├── Filesystem
├── Shell
├── Memory
├── Tool Access
├── Gateway Access
└── Observability
```

Tools Available Inside Harness

Harness can directly use:

**AgentCore Gateway**
```
quote.calculate
enrollment.submit
billing.getInvoice
```
**MCP Servers**
```
Salesforce MCP
Jira MCP
Custom Producer MCP
```
**Browser**
```
Web navigation
Data extraction
```
**Code Interpreter**
```
Python
JavaScript
TypeScript
```

**Inline Functions**

Useful for:
```
Human approval
Custom callbacks
External integrations
```

## Where Harness Fits in Healthcare Architecture

In a  multi-agent platform:
```
Producer
Quote
Enrollment
Billing
Servicing
```

We have two options.

## Option A: LangGraph + AgentCore Runtime

This is the recommended option.

```
LangGraph Supervisor

 |
 +--> Producer Agent
 +--> Quote Agent
 +--> Enrollment Agent
 +--> Billing Agent
 +--> Servicing Agent

                |
                v

        AgentCore Gateway

```

**Benefits:**
```
Explicit workflow state
Multi-agent routing
Human approval nodes
Deterministic orchestration
Fine-grained control
```

**Best for:**

Enterprise Healthcare Payer

## Option B: AgentCore Harness
```
Producer Harness
Quote Harness
Enrollment Harness
Billing Harness
```

Each is largely self-contained.

**Benefits:**
```
Very rapid development
Minimal orchestration code
Fast prototyping
```
**Best for:**
```
Knowledge assistants
CSR copilots
Producer copilots
Simple workflows
```

# Should Harness be used with LangGraph?

Usually No.

Typical pattern:
```
LangGraph
      +
AgentCore Runtime
```

OR

```
AgentCore Harness
```

but not:
```
LangGraph
      +
AgentCore Harness
```

because both solve orchestration.

Harness already provides the agent loop. LangGraph provides the agent loop plus graph orchestration. Using both generally creates duplication.

# Where Harness Makes Sense in the  Platform

Harness can be selectively for:

**Producer Knowledge Assistant**
- Commissions
- Licensing
- Appointments
- Product FAQs


**Perfect fit.**

**CSR Copilot**
- Summarize member history
- Suggest responses
- Generate communication drafts

**Excellent fit.**

**Document Analysis Assistant**
- Analyze enrollment package
- Extract fields
- Generate summary

**Good fit.**

# Where NOT to Use Harness

For core payer workflows:
```
Quote → Enrollment

Enrollment → Billing

Life Event Change

Coverage Termination

Coverage Reinstatement
```

These require:
```
Explicit state transitions
Human approval
Auditable workflow
Compensation logic
Deterministic routing
```

**LangGraph (or Step Functions + LangGraph) is a much better fit.**

# Recommendation for Healthcare Insurance Architecture

**Use:**
```
LangGraph
     +
AgentCore Runtime
     +
AgentCore Gateway
     +
AgentCore Identity
     +
AgentCore Policy
     +
Guardrails
```

for:
```
Producer
Quote
Enrollment
Billing
Servicing
```

Use AgentCore Harness only for standalone copilots and assistants where we want rapid development and do not need complex multi-agent workflow orchestration.

**Rule of Thumb**

**Need complex workflow?**

```
    → LangGraph + AgentCore Runtime
```
**Need a smart assistant quickly?**
```
    → AgentCore Harness
```

For a healthcare payer platform, Harness is excellent for CSR/Producer copilots, but the core multi-agent business journeys should remain in a LangGraph-orchestrated architecture.
