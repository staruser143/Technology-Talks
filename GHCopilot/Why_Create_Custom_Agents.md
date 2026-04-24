You should create **custom GitHub Copilot agents** only when the built‑in agents stop being *safely or consistently effective* for the task you care about.

A good way to think about this is:

> **Custom agents are for specialization, control, and repeatability — not for general coding.**

Below is a clear, **decision‑oriented guide** that maps directly to real enterprise usage (and avoids premature complexity).

***

## ✅ Do **NOT** create custom agents when…

Use built‑in agents (Plan / Local / Background / Cloud) if:

*   The task is **general coding** (CRUD, refactors, tests, debugging)
*   You only need **better prompts**, not behavior changes
*   You just want consistent style → use **copilot‑instructions.md**
*   The work is **one‑off or ad‑hoc**

> 80–85% of developer productivity gains come from *not* creating custom agents.

***

## ✅ Create a custom agent **when one or more of these are true**

### 1. You need a **specialized role**, not a general assistant

Create a custom agent when Copilot must **act like a specific persona** repeatedly.

Examples:

*   *“Java 21 + Spring Boot security architect”*
*   *“Healthcare payer claims SME”*
*   *“SOC2 / HIPAA compliance reviewer”*
*   *“Frontend accessibility auditor (WCAG 2.2)”*

**Signal:**  
You keep writing long prompts like *“Act as a senior \_\_\_ engineer and follow \_\_\_ rules.”*

✅ Create a custom agent instead.

***

### 2. You must **restrict or expand tool permissions**

Custom agents shine when **tool access matters**.

Examples:

*   Agent **must not**:
    *   Run terminal commands
    *   Modify prod configs
*   Agent **must**:
    *   Run tests
    *   Generate PRs
    *   Interact with MCP servers (Jira, Confluence, DB schemas)

This is impossible to enforce reliably with prompts alone.

✅ Use a custom agent to:

*   Explicitly allow/deny tools
*   Control autonomy boundaries

***

### 3. The task is **repeatable and procedural**

If the same workflow repeats across repos or teams:

Examples:

*   “Create production‑ready Spring Boot REST endpoints”
*   “Generate ADRs from code changes”
*   “Standardize logging & security hardening”
*   “Migrate services to Java 21 & Spring Boot 3”

✅ Custom agents turn fragile human prompts into **institutional knowledge**.

***

### 4. You want **predictable behavior across a team**

Teams create custom agents when they want:

*   Consistent decision‑making
*   Fewer “creative” answers
*   Reduced variance between developers

This is especially important in:

*   Regulated environments
*   Platform teams
*   Inner‑source repos

✅ Custom agents act as **guardrails**, not helpers.

***

### 5. You need **agent chaining or delegation**

Once tasks naturally split into phases:

Example pipeline:

1.  Plan agent → design
2.  Domain agent → implementation
3.  Security agent → validation
4.  Doc agent → documentation

✅ Custom agents enable **role separation**, which is critical for:

*   Accuracy
*   Auditability
*   Long‑running autonomous workflows

***

### 6. You want to encode **enterprise policy or domain rules**

Examples:

*   PHI handling rules
*   Logging redaction policies
*   Naming and layering conventions
*   Cloud service allow/deny lists

Prompts can’t enforce these reliably.

✅ Custom agents make these constraints *default behavior*.

***

## ❌ Do NOT create a custom agent just because…

These are common anti‑patterns:

| Anti‑pattern                            | Better option                  |
| --------------------------------------- | ------------------------------ |
| “We want better responses”              | Improve prompts / instructions |
| “We want tests generated”               | Instructions + slash commands  |
| “We want faster coding”                 | Built‑in Local Agent           |
| “We want everyone to code the same way” | `copilot-instructions.md`      |

***

## ✅ Decision tree (quick mental model)

```text
Is this general coding?
  └─ Yes → Built‑in agent

Is this role‑specific, repeatable, or risky?
  └─ Yes → Custom agent

Does it require tool restrictions or autonomy?
  └─ Yes → Custom agent

Is it a one‑off task?
  └─ Yes → Prompt or prompt file
```

***

## ✅ Practical examples (enterprise‑grade)

You should create custom agents for things like:

*   **“Spring Boot Security Reviewer Agent”**
*   **“Healthcare Enrollment Rules Agent”**
*   **“Cloud Cost‑Optimization Agent”**
*   **“SOC2 Evidence Generator”**
*   **“Java Modernization Agent”**

You should *not* create agents for:

*   Writing unit tests casually
*   Refactoring code ad‑hoc
*   Exploring unfamiliar libraries

***

## ✅ Final rule of thumb

> **Create custom agents only when you want to replace *thinking effort* with *encoded expertise*.**


