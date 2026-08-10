**Scenario**

A healthcare scheduling company wants Claude to handle patient appointment rescheduling requests that come in via chat. The technically best approach, engineering determined, would be a multi-agent system: one agent checks provider availability, one checks patient history for scheduling conflicts, one handles insurance pre-authorization requirements, and one drafts the confirmation message — coordinated by an orchestrator. This design produces the most accurate, thorough scheduling decisions in testing. However, the business has stated a hard requirement: patients are on hold for a live chat session, and any reply taking longer than 4 seconds causes a measurable drop in patient satisfaction and increased abandonment. The multi-agent design, in testing, averages 11 seconds per request due to the orchestration and sequential handoffs between agents.

**Question**: What should the team do?

A) Deploy the multi-agent system anyway — it produces the most accurate results, and accuracy should always take priority over speed in healthcare-adjacent systems.

B) The multi-agent design conflicts with a stated performance SLA (4-second response requirement); the team should redesign toward a faster architecture (e.g., a leaner workflow or parallelized checks) that meets the latency constraint, even if it means accepting a possible reduction in per-request thoroughness, or offload the heavier checks to run asynchronously after an initial fast response.

C) The SLA should be renegotiated upward to accommodate the multi-agent system, since the SLA was likely set arbitrarily and the more accurate system should take precedence.

D) The problem is unsolvable — since accurate scheduling requires checking availability, history, and insurance, and that inherently takes time, Claude shouldn't be used for this task at all.

Take your best guess and I'll walk through it.





Correct — and this scenario is the clearest illustration of why business value pillars aren't a side note to architecture, they're a constraint that overrides "best" on a technical merits basis alone.

**Why B is right**

This goes back to the core lesson at the end of the original domain overview: **the "technically best" architecture is wrong if it doesn't fit the stated business constraints.** Here, the constraint isn't a nice-to-have — it's a stated SLA with a measurable business consequence attached (patient satisfaction drop, increased abandonment past 4 seconds). Engineering's own testing shows the multi-agent design blows past that SLA by nearly 3x (11s vs. 4s). At that point, "most accurate design" and "correct design for this business" have diverged, and the exam wants you to side with the business constraint.

The answer gives two concrete paths that both respect the SLA without abandoning the task:
- **Redesign toward something leaner** — this could mean parallelizing the independent checks (availability, history, insurance) instead of running them sequentially through an orchestrator, or collapsing to a simpler workflow if the checks don't actually need agent-level judgment.
- **Split into fast-path + async** — give the patient an immediate acknowledgment/tentative confirmation within the SLA, then run the heavier verification (insurance pre-auth, etc.) asynchronously and follow up if something doesn't clear. This is a common real-world pattern for exactly this kind of tension: instant response now, thorough verification without blocking the user.

Either way, the point is: **the SLA is a hard input to the design, not a target to argue against.**

**Why the others are wrong**

- **A)** treats accuracy as an absolute good that should always win, but that's not how the business-value framing works. SLAs exist because the business has already weighed accuracy against experience and decided where the line is — "healthcare-adjacent" doesn't automatically mean "accuracy trumps everything," especially when the task here is scheduling logistics, not a clinical decision. Ignoring a stated, measured SLA because your system did better on one axis (accuracy) is exactly the trap this domain tests.
- **C)** is the "argue with the constraint instead of solving the problem" trap. Nothing in the scenario suggests the SLA is arbitrary — it's explicitly tied to a measured satisfaction/abandonment effect. Treating a stated business constraint as negotiable by default, without evidence it's actually flexible, is a bad instinct the exam is checking for.
- **D)** gives up too early. The scenario doesn't say fast *and* accurate is impossible — it says the *specific* multi-agent design as tested doesn't hit both. Parallelizing independent checks (which, notably, look independent the way your legal-playbook scenario did — availability, history, and insurance don't depend on each other) or fast-path/async splitting are both realistic ways to preserve real accuracy within the SLA. "Unsolvable" ignores design alternatives that exist between "the current 11-second design" and "don't build it."

**Pattern to remember**: stated business constraints (SLAs, cost ceilings, compliance requirements) function as hard boundaries on the design space, not as one more factor to weigh against "the best possible architecture." When a technically superior design violates a stated constraint, the answer is almost always to find an architecture that fits the constraint — not to keep the superior design and hope the constraint bends, and not to declare the problem unsolvable without exploring whether the independent sub-checks can be restructured (parallelized, staged, made async) to buy back speed.

