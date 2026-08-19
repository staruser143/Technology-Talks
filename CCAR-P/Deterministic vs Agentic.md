I have read **Section 6: Exam Content Outline — Domain 1: Solution Design & Architecture (17%)**, specifically the objective *"Select appropriate architectural patterns (workflow, agentic, augmented LLM)"*, and cross-referenced **Domain 3: Integration (19%)** objectives on connection protocols and progressive discovery from the Exam Guide PDF.

Here is your `$learn` breakdown for **deterministic vs agentic workflows**.

### What it is
Deterministic workflows are orchestrated, code-defined state machines where the control flow and decision points are hardcoded by engineers, whereas agentic workflows delegate the control flow to the LLM, which autonomously selects tools and sequences actions in a loop until the task is complete.

### Why it matters for the exam
It directly tests **P4 (proportionate & business-value-aligned)** and **P1 (fix the failing component, not a proxy)**. The exam wants to see that you can correctly classify a business problem as either a *predictable, rules-based process* (requiring determinism) or an *open-ended, exploratory task* (where agency adds value), rather than defaulting to the most advanced-sounding pattern.

### How it works
*   **Concrete Architecture — Deterministic (Workflow):** An invoice-processing pipeline. Step 1: Claude extracts fields from the PDF (LLM as a function). Step 2: A hardcoded `if/else` block validates the vendor ID against a database. Step 3: A rules engine routes invoices above $10,000 to a Human-in-the-Loop (HITL) approval queue. Step 4: An API call posts the payment. The LLM *never* decides the control flow; code does.
*   **Concrete Architecture — Agentic:** A debugging assistant. The user reports a bug; the agent is given `read_file`, `search_codebase`, and `run_tests` tools. The LLM autonomously decides: search for the relevant file → read it → form a hypothesis → run a test → observe the failure → iterate. The sequence cannot be pre-hardcoded because the path depends on intermediate discoveries (progressive discovery).

### What breaks without it (the trap)
If you mismatch the pattern to the problem, you fall into **Distractor Class 5 (Over-Engineering)** or **Distractor Class 1 (Guard-Instead-of-Remove)**.
*   *Trap A (Agentic overuse):* Using a fully autonomous agent for a compliance-critical, rule-based process (like sanctions screening). The LLM hallucinates a "clear" decision on a fuzzy name match, and the guardrail is only a prompt saying "be careful." This destroys **safety** and regulatory **SLA** compliance. The fix is deterministic logic for the matching step, with the LLM confined to data extraction.
*   *Trap B (Deterministic overuse):* Forcing a rigid, pre-scripted workflow on an exploratory research task. Every time the user's query deviates from the happy path, the workflow fails silently or returns irrelevant canned output, destroying **accuracy** and business value — when an agentic loop with progressive discovery would have handled the ambiguity.

**Trade-off impact:** Deterministic workflows optimize **latency** (predictable step counts), **cost** (bounded LLM calls), **safety** (auditable, repeatable paths), and **SLA** reliability, but sacrifice flexibility. Agentic workflows optimize **accuracy** on open-ended tasks and adaptability, but introduce variable **latency** (multi-turn loops), higher **cost** (repeated tool calls), and expanded **safety** risk (autonomous action-taking) that must be mitigated with scoped tools and HITL gates.

[Domain 1] & [Domain 3]

Does this make sense before we look at trade-offs?