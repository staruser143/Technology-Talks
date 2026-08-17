**Scenario**

A legal team wants Claude to review a 200-page vendor contract before signing. The review needs to check the contract against four separate playbooks: intellectual property terms, liability and indemnification, termination clauses, and data privacy compliance. Each playbook has its own detailed checklist and its own risk criteria — an IP specialist and a privacy specialist would flag completely different things in the same paragraph, and neither review depends on what the other finds. The legal team wants a single consolidated report at the end, with findings from all four areas synthesized into an overall risk summary.

**Question**: Which architectural pattern best fits this system, and why?

A) A single agentic system — one agent loops through the entire contract, checking it against all four playbooks in one continuous session before producing the report.

B) Workflow — a fixed sequence: extract clauses, classify each clause by domain, run each through its checklist, then assemble the report.

C) Multi-agent orchestration — an orchestrator delegates the IP review, liability review, termination review, and privacy review to four specialized subagents (each reviewing the full contract against its own playbook), then synthesizes their findings into one report.

D) Augmented LLM — a single call with the contract and all four playbooks provided as context, asking Claude to produce the full risk report in one pass.

Take your best guess and I'll walk through it.


Correct — and this is exactly the shape multi-agent orchestration is designed for. Let's break down why, since this domain gets tested more subtly than "agentic vs. workflow."

**Why C is right**

Three signals in the scenario point straight at multi-agent:

1. **Genuinely independent subtasks.** The IP review and the privacy review don't depend on each other's findings — an IP specialist and a privacy specialist reading the same paragraph would flag entirely different things. When subtasks are independent like this, you can hand each one to its own subagent with its own focused context (just its playbook, just its expertise) rather than forcing one agent to juggle four unrelated rulesets simultaneously.
2. **Specialized "expertise" per subtask.** Each playbook is its own detailed checklist with its own risk criteria — this is the kind of complexity where a subagent scoped to just "IP terms" will do a more focused, reliable job than a generalist agent trying to hold all four frameworks in its head at once for 200 pages.
3. **A synthesis step at the end.** "Consolidated report... synthesized into an overall risk summary" is the orchestrator's job — collect four independent outputs and merge them into one coherent deliverable. That orchestrator-worker shape (one agent delegates and synthesizes, several agents do focused independent work) is the textbook multi-agent pattern.

**Why the others are wrong**

- **A) Single agentic system** would work, technically — but it's the wrong choice because it forces one context window to hold all four playbooks plus 200 pages of contract simultaneously, for the entire session. That's a recipe for the model losing precision on later checklist items as context fills up, and it throws away the natural parallelism available here (four independent reviews could run concurrently instead of sequentially).
- **B) Workflow** fails the same test as your first scenario: the four reviews aren't a fixed *sequence* where step 2 depends on step 1's output — they're independent and parallelizable. Forcing them into "extract, then classify, then check IP, then check liability..." adds unnecessary sequencing where none is needed.
- **D) Augmented LLM** has the same context-overload problem as A, just without even a loop to manage it — one shot, 200 pages, four playbooks, is asking a lot of a single pass and gives you no way to isolate or re-run just the "termination clauses" review if it comes back weak.

**Pattern to remember**: 
- Multi-agent isn't just "the task has multiple parts" — workflows have multiple parts too.
- Multi-agent is specifically for parts that are **independent enough to isolate** (each needs its own scoped context/expertise) and **often parallelizable**, with a synthesis step tying them together.
- If the parts must happen in a strict order and depend on each other's output, that's workflow, not multi-agent — even with several distinct steps.
