
---

**Scenario**

A mid-sized insurance company wants Claude to handle incoming claims documents. For every claim, the process is always the same: (1) extract policy number, claimant name, and incident date from the uploaded PDF, (2) look up the policy in their database via an internal API to confirm it's active, (3) classify the claim type against a fixed list of categories, (4) draft a summary for the human adjuster. The steps and their order never change from claim to claim, but the *content* extracted and classified varies with each document. The company wants predictable latency and wants to be able to test each stage in isolation.

**Question**: Which architectural pattern best fits this system, and why?

A) Agentic — because the system needs to read a document and call an external API, which requires autonomous tool use.

B) Workflow — because the sequence of steps is fixed and known in advance; Claude generates the content at each step, but your code controls the path.

C) Augmented LLM — because a single call with document input and tool access can handle extraction, lookup, and classification together.

D) Multi-agent orchestration — because there are four distinct subtasks, so each should be delegated to a specialized subagent.



Correct — and your reasoning instinct is on the right track. Let's confirm why B is right and why the others are tempting traps, since the exam leans hard on this exact confusion.

**Why B is right**

The two words in the scenario that should jump out are "the steps and their order never change" and "test each stage in isolation." That's the signature of a **workflow**: a fixed, known sequence where your code owns the control flow (step 1 always leads to step 2, always to step 3, always to step 4), while Claude generates the *content* at each step — reading the PDF, writing the summary, deciding the category. Fixed path, variable content, is the definition of workflow. It also directly satisfies two things the company explicitly asked for: predictable latency (no open-ended looping) and isolated testability (you can eval "does step 3 classify correctly" without worrying about steps 1, 2, 4 at all).

**Why the others are wrong — and why they're tempting**

- **A) Agentic** is the most common wrong answer here, and it's a trap the exam sets deliberately. The presence of tool use (calling the policy API) makes people think "tools = agent." But tool use alone doesn't make something agentic — what makes something agentic is that *the LLM decides what to do next and in what order*. Here, the order is fixed by the business process, not decided by Claude. Agentic would cost more, add latency variance, and make testing harder — the opposite of what the company wants.

- **C) Augmented LLM** fails because the task isn't one step. A single call can't cleanly do "extract → look up → classify → draft" — you'd lose the ability to test each stage independently, and you'd have no clean point to insert the "is policy active" check as a hard gate before classification proceeds.

- **D) Multi-agent** overcomplicates this. Multiple agents/subagents are for when subtasks are complex enough to need independent judgment and possibly parallel work. Here the subtasks are simple, sequential, and don't benefit from autonomous coordination — a workflow with four steps handles it with far less overhead.

**The pattern to remember for the exam**: ask "does the *path* vary, or just the *content*?" Path fixed → workflow. Path must be decided dynamically by the model → agentic. One step only → augmented LLM. Multiple independent complex subtasks needing coordination → multi-agent.


