# Using LangGraph with AgentCore

- If we use LangGraph, we should explicitly design a structured state model.
- In fact, one of the biggest advantages of LangGraph compared to many agent frameworks is that it encourages treating state as a first-class architectural component rather than hiding everything in conversation history.

For healthcare insurance platform, a well-defined LangGraph state model is mandatory.

## Why LangGraph State Matters

A common misconception is:
```
Agent State = Chat History
```

In enterprise workflows, especially healthcare insurance, that's insufficient.

Instead:
```
Agent State
=
Conversation Context
+
Workflow State
+
Retrieved Business Context
+
Execution Metadata
+
Human Approval State
```

With LangGraph, each node reads and updates a shared state object.

**Example:**
```
Producer Agent
      |
      v
Quote Agent
      |
      v
Enrollment Agent
      |
      v
Billing Agent
```

All agents operate on a common structured state.

# Recommended State Layers

Separate state into 5 sections.
```
class InsuranceJourneyState:
    conversation
    journey
    customer
    business
    execution
```

## 1. Conversation State

Contains only information needed to maintain the conversation.
```
conversation = {
    "current_message": "...",
    "summary": "...",
    "language": "en",
    "channel": "portal"
}
```

Do not store 500 turns indefinitely.

Instead maintain:
```
Recent Messages
+
Conversation Summary
```

## 2. Journey State

This is the most important layer.
```
journey = {
    "journey_id": "J123",
    "journey_type": "QUOTE_TO_ENROLLMENT",
    "current_step": "QUOTE_SELECTED",
    "status": "IN_PROGRESS"
}
```

This is what allows the graph to resume correctly.

**Example:**
```
Producer left yesterday

returns today

LangGraph reloads state

resumes from:
QUOTE_SELECTED

```
instead of replaying the entire conversation.

## 3. Customer/Actor Context

```
customer = {
    "actor_type": "PRODUCER",
    "producer_id": "P123",
    "group_id": "ABC",
    "member_id": None
}
```

This typically comes from:
```
Cognito
AgentCore Identity
SSO
JWT Claims
```
The LLM should never determine this.

## 4. Business State

Represents verified business facts.
```
business = {
    "quote_id": "Q1001",
    "employee_count": 250,
    "selected_plan": "PPO-500",
    "effective_date": "2027-01-01"
}
```

**Important:**
```
Business State
≠
Memory
```

Business state is usually pulled from:
```
PAS
Enrollment systems
Billing systems
CRM
```
and cached during workflow execution.

## 5. Execution State

Supports graph orchestration.
```
execution = {
    "current_agent": "QuoteAgent",
    "approval_required": False,
    "confidence_score": 0.91,
    "last_tool_called": "quote.calculate",
    "retry_count": 0
}

```
This is critical for production reliability.

**Example Quote-to-Enrollment Graph**
```
Start
  |
  v
Intent Classification
  |
  v
Producer Validation
  |
  v
Quote Creation
  |
  v
Plan Selection
  |
  v
Enrollment Validation
  |
  v
Human Approval
  |
  v
Enrollment Submission
  |
  v
Complete
```


**State evolves as:**
```
state["journey"]["current_step"]
```

changes from
```
"QUOTE_CREATION"
```

to
```
"PLAN_SELECTION"
```

to
```
"ENROLLMENT"
```
## Where AgentCore Memory Fits

When using LangGraph, AgentCore Memory becomes much less critical.

Many architects initially think:
```
Need AgentCore Memory
because state must be persisted
```

Not necessarily.

LangGraph already gives you a state model.

You can persist that state in:
```
DynamoDB
Aurora
MongoDB Atlas
Redis
Postgres
```

**For example:**
```
state_store
    journey_id -> state
```

When a conversation resumes:
```
Load State
Resume Graph
Continue Workflow
```

No need to retrieve hundreds of chat messages.

## Healthcare Architecture Pattern Recommended

For the domains:
```
Producer
Quote
Enrollment
Billing
Servicing
```

Use:
```
LangGraph State
    =
    Workflow State

AgentCore Memory
    =
    Optional Personalization Layer

```

**Example:**

**LangGraph State**
```json
{
    "quote_id": "Q123",
    "selected_product": "PPO500",
    "status": "PENDING_ENROLLMENT"
}
```

**AgentCore Memory**
```json
{
    "preferred_language": "Spanish",
    "preferred_contact_method": "SMS"
}

```
**Notice:**
```
Workflow state belongs in LangGraph.
Preferences can live in AgentCore Memory.
Multi-Agent State Model
```
For federated architecture:
```
Journey Supervisor
         |
         +---- Producer Agent
         |
         +---- Quote Agent
         |
         +---- Enrollment Agent
         |
         +---- Billing Agent
         |
         +---- Servicing Agent
```

the recommendation is a a shared parent state plus agent-specific substate.

**Example:**
```
{
  "journey": {...},

  "producer": {...},

  "quote": {...},

  "enrollment": {...},

  "billing": {...},

  "servicing": {...}
}

```

That prevents agents from overwriting each other's context.

**Example:**
```
state["quote"]["quote_id"]

state["enrollment"]["application_id"]

state["billing"]["invoice_id"]
```

**instead of dumping everything into:**
```
state["memory"]
```

**Architect Recommendation**

For a healthcare insurance platform using:
```
LangGraph
AgentCore Runtime
AgentCore Gateway
AgentCore Policy
AgentCore Identity
Bedrock Guardrails
```

**Design state as:**
```
LangGraph State
│
├── Identity Context
├── Journey State
├── Business Context
├── Domain States
├── Approval State
├── Execution Metadata
└── Conversation Summary
```

and persist it in:
```
DynamoDB / MongoDB / Aurora
```

while using:
```
AgentCore Memory
```

only for:
```
Preferences
User personalization
Long-term conversational memory
```

# Summary
- When using LangGraph, we should intentionally model and persist structured workflow state.
- In fact, a well-designed LangGraph state model significantly reduces the need to rely on AgentCore Memory because the graph can resume from durable business state rather than reconstructing context from conversation history.
