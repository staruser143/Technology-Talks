**Scenario**

A marketing team wants Claude to run a "campaign launch assistant." Given a campaign brief, the top-level task is genuinely open-ended: Claude has to decide what content to produce (could be a blog post, could be three social posts, could be an email — depends on the brief), and it needs to run through as many revision cycles as it takes to get something publish-ready, which varies unpredictably by campaign. But two parts of the process are locked down by company policy: whenever any piece of content needs review, it must always go through three specific checks in this exact order — brand voice check, legal compliance check, SEO check — never skipped, never reordered. And once something is approved, publishing is always the same fixed sequence: format for the CMS, schedule the post, notify the marketing lead.

**Question**: Which architecture best fits this system?

A) Pure workflow — since review and publishing are fixed sequences, force the entire system, including content creation, into one fixed pipeline.

B) Agentic with embedded workflow steps — the top-level agent dynamically decides what content to create and how many revision cycles are needed, but whenever it needs to run a review or publish something, it calls a fixed-sequence workflow (as a tool/subroutine) to do that part.

C) Multi-agent orchestration — one subagent handles content creation, one handles review, one handles publishing, coordinated by an orchestrator that manages all three.

D) Pure agentic — everything, including the review checks and publishing steps, should be dynamically decided by the agent each time, since the system needs to stay flexible.

Take your best guess and I'll walk through it.



Correct — and this is the mirror image of the last scenario, which is exactly the point. Good catch.

**Why B is right**

Same per-component analysis, just with the nesting flipped:

- **The outer shape is agentic.** "What content to produce" and "how many revision cycles" are both explicitly unpredictable and vary by campaign — that's the same "path itself is unknown in advance" signal from your incident-investigation scenario. So the top level needs to be a loop where Claude decides what to do next.
- **The inner shapes are workflows, called as subroutines.** Review and publishing are both described with strong, explicit language — "always," "never skipped, never reordered," "always the same fixed sequence." That's a workflow's signature: fixed order, no exceptions. So whenever the top-level agent needs to run a review or publish something, it invokes that fixed pipeline as a tool/subroutine, rather than reasoning its way through those steps fresh each time.

Compare this to your refund scenario: there, the *outer* shape was fixed (workflow) and one *inner* stage was unpredictable (agentic). Here it's inverted — the *outer* shape is unpredictable (agentic) and the *inner* pieces are fixed (workflow). Both are "combined pattern" answers, but which piece is the outer shell and which is nested depends entirely on which part of the system is actually unpredictable. The exam wants you to trace that boundary case by case, not memorize "workflow always wraps agentic" or vice versa.

**Why the others are wrong**

- **A) Pure workflow for the whole thing** breaks the moment you try to force "what content to create" and "how many revisions" into a fixed sequence — those genuinely vary per campaign, so a rigid pipeline either can't handle the variation or has to over-generalize its steps to the point of being useless.
- **C) Multi-agent** overengineers this the same way it did in your refund scenario. Review and publishing aren't independent, parallelizable subtasks needing their own scoped expertise — they're fixed, sequential, deterministic procedures. Giving them each a subagent plus an orchestrator adds coordination cost for something that's just "run these three steps in this exact order," which a workflow already does more simply and more reliably.
- **D) Pure agentic for everything** is the most important wrong answer to understand, because it's the one that looks "safest" (maximum flexibility) but is actually the riskiest. Company policy says the three review checks must run "never skipped, never reordered" — that's a compliance requirement, not a preference. Leaving that to model discretion each time means there's a nonzero chance the agent decides to skip a step, reorder it, or interpret "review" loosely under time pressure. Locking compliance-critical sequences into a workflow — instead of trusting an agent's judgment every single run — is exactly why Domain 5 (Governance) cares so much about this distinction.

**Pattern to remember**:
- Nesting direction isn't fixed — trace where the actual unpredictability lives in the system, and that's your outer shell;
- everything fixed and rule-bound around it becomes a subroutine, regardless of whether that subroutine sits inside an agent's loop or an agent's loop sits inside a subroutine.



