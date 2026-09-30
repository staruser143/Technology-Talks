# AgentCore Memory

If we choose not to use AgentCore Memory, we do not automatically need to send the entire conversation history with every request.

In healthcare insurance, recommendation is to think about 4 different kinds of memory, because they are often confused.

1. LLM Conversation Memory
2. Agent Working Memory
3. Workflow State
4. Business System State


Most enterprises only need AgentCore Memory for ** #1** and part of **#2.**

## Option 1: Pass Entire Conversation Each Time

This is the simplest pattern.
```
User:
I need a quote for ABC Corp.

Agent:
How many employees?

User:
250 employees.

Agent:
Any dependents?

User:
Yes.

``
Request #4 may look like:
```json
{
  "messages": [
    {"role":"user","content":"I need a quote for ABC Corp"},
    {"role":"assistant","content":"How many employees?"},
    {"role":"user","content":"250 employees"},
    {"role":"assistant","content":"Any dependents?"},
    {"role":"user","content":"Yes"}
  ]
}
```

**Pros**:

- Simple
- No memory infrastructure

**Cons**:

- Expensive
- Large token usage
- Doesn't scale well

This approach is not recommended for long-running insurance journeys.

## Option 2: Conversation Summary Pattern (My Preferred Approach)

Instead of sending everything:
```
Turn 1
Turn 2
Turn 3
...
Turn 100
```

Store a summary.

Example:
```json
{
  "journeySummary": {
    "producerId": "P123",
    "groupName": "ABC Corp",
    "employeeCount": 250,
    "dependentsIncluded": true,
    "requestedProducts": ["PPO","HDHP"]
  }
}
```

Then each request becomes:
```
Current User Message
+
Journey Summary
```

This reduces tokens dramatically.

### Option 3: Workflow State Store (Recommended for Healthcare)

```
For Producer → Quote → Enrollment journeys:
```

Don't rely on conversation history.

Instead maintain structured state.
```json
{
  "journeyId": "J1001",
  "state": "EnrollmentPending",
  "groupId": "ABC",
  "quoteId": "Q123",
  "selectedPlan": "PPO-500"
}
```

Store this in:
```
DynamoDB
Aurora
MongoDB Atlas
Redis
```
Then the agent simply retrieves current state.

```
User:
What's my enrollment status?


Agent:

Read Journey State


rather than:

Search through 500 chat messages


This is much more reliable.
```

## Option 4: AgentCore Memory

This is where AgentCore Memory becomes useful.

AgentCore Memory provides managed storage for:

- Session memory
- Long-term memory
- Preferences
- Historical context

AWS positions it as a context layer for delivering personalized, context-aware agent interactions.

Examples:

- Preferred language = Spanish
- Preferred communication = SMS
- Producer prefers PPO first
- Member prefers email notifications


This is useful context, but not authoritative business data.

# Recommendation for a Payer Platform
## Producer Domain

Store:
```
Producer preferences
Recent interactions
Working context
```

in AgentCore Memory.

Store:
```
License status
Appointments
Commissions
Hierarchy
```

in Producer systems.

## Quote Domain

Store:

Current quote discussion


in AgentCore Memory or session storage.

Store:
```
Quote
Rates
Products
```

in Rating Engine / PAS.

## Enrollment Domain

Do NOT rely on AgentCore Memory.

Store:
```
Enrollment Status
Application Data
Effective Dates
Eligibility
```

in authoritative systems.

## Billing Domain

Never trust memory.

Always read:
```
Balance
Premium
Invoice
Payment Status
```

from Billing systems.

Typical Enterprise Pattern

Most enterprises are  moving towards: 

```

                     User
                       |
                       v

            Agent Runtime (AgentCore)

                       |

          Retrieve Conversation Summary
                       +
            Retrieve Workflow State
                       +
         Retrieve System-of-Record Data

                       |

                Generate Response

```
Not:

-Send 800 chat messages every turn

# Recommendation for Healthcare Architecture

For your:
```
Producer
Quote
Enrollment
Billing
Servicing
```
platform:
```
AgentCore Memory
    -> Optional

Conversation Summary Store
    -> Recommended

Workflow State Store
    -> Mandatory

Systems of Record
    -> Mandatory

```
A practical design would be:
```
AgentCore Memory
    = Preferences + conversational context

DynamoDB/MongoDB
    = Journey state

PAS/Enrollment/Billing
    = Authoritative truth
```

So the answer is:

- No, if we don't use AgentCore Memory, we do not need to pass the entire conversation history every request.
- A healthcare payer platform should instead maintain a structured journey state and conversation summary, then retrieve only the relevant context for each interaction. In fact, for Enrollment, Billing, and Servicing workflows, structured state is usually more important than conversational memory.
