A clean mental model here: there are really only three architectural shapes you'll be asked to choose between (plus combinations), and the exam wants you to recognize which one fits which business problem.## Domain 1: Solution Design & Architecture (17% of the exam)

This domain is really about judgment: given a business problem, can you pick the right shape for the solution and defend that choice? Let's build this up piece by piece.

### 1. The three architectural patterns

The exam repeatedly tests whether you know **when to use which pattern**, not just what each one is:

- **Augmented LLM** — a single call to Claude, optionally with tools or retrieval bolted on. Cheapest, fastest, most predictable. Use it when the task is one well-defined step: classify this ticket, summarize this document, extract these fields.
- **Workflow** — your code defines a fixed sequence of LLM calls (and other steps). The *path* is deterministic; only the *content* at each step is generated. Use it when the process itself is well understood and repeatable — a document pipeline, a multi-stage content review.
- **Agentic** — Claude decides its own next steps in a loop (plan → act → observe → repeat) until it judges the task complete. Use it when the path can't be known in advance — open-ended research, debugging, or tasks where the number of steps varies by input.

The exam-relevant rule of thumb: **start with the simplest pattern that could work, and only add autonomy where the task genuinely requires unpredictable branching.** Agentic systems cost more (tokens, latency, failure surface) and are harder to test — so "just use an agent" is often the *wrong* answer on this exam, even though agentic gets top billing in the domain name.

### 2. Translating business problems into solutions

Expect scenario questions like: *"A support team wants to reduce ticket resolution time. What's the appropriate architecture?"* The skill being tested is decomposition:

- What's the actual bottleneck? (routing? drafting responses? looking up account data?)
- Is the task single-step or multi-step?
- Does it need external data (RAG, tools) or just reasoning over what's given?
- Is human review required before an action is taken (governance domain overlaps here)?

### 3. End-to-end architecture: input → processing → output → feedback

Think of every system as having four layers:
- **Input**: how does data get in? (user message, webhook, scheduled job, file upload)
- **Processing**: which pattern (above), which model, which tools/retrieval
- **Output**: structured vs. unstructured, where does it go (API response, database write, human review queue)
- **Feedback loop**: how do you know it worked? (logging, human corrections, evals feeding back into prompt/tool improvements)

A common exam trap is a design that nails input/processing/output but has no feedback loop — meaning you can never detect drift or failure in production.

### 4. Multi-agent orchestration

Once a single agent isn't enough (task is too broad for one context window, or needs specialized sub-skills), you orchestrate multiple agents. Two common shapes:
- **Orchestrator-worker**: one agent plans and delegates to specialized subagents, then synthesizes results.
- **Parallel/independent agents**: multiple agents work on independent subtasks simultaneously, results merged at the end.

Know the trade-off: multi-agent systems increase capability but multiply cost, latency, and coordination failure modes (subagents stepping on each other, conflicting outputs).

### 5. Decomposition techniques

For complex problems, breaking things into smaller LLM-solvable units usually beats one giant prompt. Signs a problem needs decomposition: the prompt requires holding too much context at once, the task has clearly separable phases, or errors in one phase would be easier to catch in isolation.

### 6. Business value pillars

Every design decision should map back to one of: **efficiency** (do the same work faster/cheaper), **transformation** (do something not possible before), **productivity** (augment humans, don't replace), **cost**, and **performance SLAs** (latency/uptime requirements). Expect questions where the "technically best" architecture is wrong because it doesn't fit the SLA or cost constraint stated in the scenario — read the business constraints in the question carefully.

---

