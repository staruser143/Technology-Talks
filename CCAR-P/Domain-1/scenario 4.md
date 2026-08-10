**Scenario**

An e-commerce company processes refund requests through a system with four stages: (1) verify the order exists and falls within the refund policy window — a deterministic database lookup, (2) investigate the reason for the request — this can mean checking order status, shipping tracking, prior support tickets, and product review history, with the specific combination and number of checks varying a lot depending on what the customer said and what's found along the way, (3) apply the fixed refund calculation formula once the investigation concludes, (4) log the decision and notify the customer. Stages 1, 3, and 4 always happen in that exact order for every request. Stage 2 is the only part where the number and choice of checks can't be predicted in advance.

**Question**: Which architecture best fits this system?

A) Pure agentic — since stage 2 requires dynamic decision-making, the whole system should be one continuous agent loop from intake to notification.

B) Pure workflow — since three of the four stages are fixed and deterministic, the whole system should be a fixed sequence, with stage 2 given a fixed checklist of checks to run every time.

C) Workflow with an embedded agentic step — stages 1, 2, 3, 4 run in a fixed sequence (that's the workflow), but stage 2 itself is implemented as a small agentic loop that decides which checks to run and how many, then hands its findings back to the fixed pipeline.

D) Multi-agent orchestration — each of the four stages should be its own independent subagent, coordinated by an orchestrator.

Take your best guess and I'll walk through it.



Correct — and this is the pattern the exam wants you to recognize as the "adult" answer, because in real production systems, pure single-pattern designs are actually the exception. Let's break down why.

**Why C is right**

The scenario is engineered to have two different shapes nested inside each other, and the skill being tested is noticing that architecture decisions don't have to apply uniformly across an entire system:

- **The outer shape is a workflow.** Stages 1, 2, 3, 4 always happen in that exact order, every single time. That fixed backbone — verify, investigate, calculate, notify — is exactly what a workflow is for, and it should stay a workflow because stages 1, 3, and 4 gain nothing from autonomy (a database lookup and a fixed formula don't benefit from an LLM "deciding" how to do them).
- **The inner shape, just for stage 2, is agentic.** Within that one stage, the number and choice of checks genuinely can't be predicted in advance — that's the same "path itself is unknown" signal from your incident-investigation scenario. So stage 2 is implemented as its own small agent loop, scoped just to investigation, that hands a clean result (its findings) back to the workflow once it's done.

The key insight: **the pattern is chosen per-component, not per-system.** You wrap the unpredictable part in a loop, and let the fixed, predictable parts stay fixed. This also happens to be good practice for testability and cost — stages 1, 3, 4 stay cheap and easy to eval, and the expensive, variable-cost agentic loop is contained to only where it's actually needed.

**Why the others are wrong**

- **A) Pure agentic for the whole thing** wastes reliability and cost on stages that don't need it. Giving an agent autonomy over "verify the order exists" adds failure modes (it could get creative about interpreting the refund policy window) with zero benefit, since that check is genuinely deterministic.
- **B) Pure workflow with a fixed checklist for stage 2** is the trap this scenario shares with your incident-investigation scenario: forcing "always check status, then tracking, then tickets, then reviews, in that order" is wasteful and rigid when the right combination of checks varies by case — some refund reasons only need one check, others need all four, and the order that makes sense to check them in isn't fixed either.
- **D) Multi-agent for all four stages** overengineers this. Multi-agent is for independent, parallelizable subtasks (like your four legal playbooks). Here the stages are sequentially dependent — stage 3's calculation depends on stage 2's findings — so there's no independence to exploit, and adding an orchestrator for four sequential steps adds coordination overhead for nothing.

**Pattern to remember**: don't ask "which single pattern fits this whole system" — ask "which pattern fits *each part*." A workflow can contain an agentic step. An agentic loop can call a workflow as one of its tools. The exam rewards recognizing where the boundary between predictable and unpredictable actually sits inside a larger system, not picking one label for the whole thing.
