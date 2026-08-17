Moving to sub-topic 4: documenting architectures and providing implementation guidance.

**Scenario**

An architect delivers a completed Claude-powered financial-reporting system to the client's internal engineering team, along with documentation describing the final architecture: a workflow pipeline with five sequential steps, a specific model tier chosen for each step, and one seemingly odd design choice — step 3 deliberately re-verifies a calculation that step 2 already computed, which looks redundant. The documentation describes *what* each component does, but not *why* any particular choice was made.

Eight months later, a new engineer on the client's team, reviewing the system for a performance optimization pass, notices the redundant-looking verification in step 3, concludes it's unnecessary overhead, and removes it to improve latency. This reintroduces a subtle calculation-drift bug that the original architect had specifically discovered and fixed with that verification step during initial development — a fix that was never documented as intentional, only implemented.

**Question**: What's the core documentation gap here, and what should the architect have done differently?


**Question**: What's the core documentation gap here, and what should the architect have done differently?

A) There's no real gap — the documentation accurately described what the system does, and it's reasonable to expect future engineers to thoroughly test any change before deploying it regardless of what the documentation says or doesn't say.

B) The core gap is documenting **what** was built without documenting **why** — the redundant-looking verification step wasn't actually redundant, it was a deliberate fix for a real, previously-discovered problem, but nothing in the documentation captured that reasoning. To a future engineer without that context, it looks like unnecessary overhead rather than a load-bearing safeguard. The architect should have documented the rationale behind non-obvious design decisions specifically — what problem this step solves, what happens if it's removed, and ideally a reference to the original issue it was built to prevent — so that future maintainers can make informed decisions about changes rather than unknowingly undoing a fix whose purpose was invisible to them.

C) The issue is that the architecture itself was flawed for including a step that could be misunderstood; a good architecture should never include anything that might look unnecessary to someone unfamiliar with its history.

D) The issue is entirely the new engineer's fault for not consulting the original architect before making the change; the documentation format is irrelevant since any responsible engineer should always ask before modifying unfamiliar code regardless of what the documentation says.


Correct — and this scenario is a clean, direct illustration of exactly the gap named in the sub-topic's description: documentation that captures the *what* without the *why* leaves future maintainers unable to distinguish "unnecessary complexity that's safe to simplify" from "a load-bearing decision that looks unnecessary only because its history is invisible."

**Why B is right**

The failure here isn't in the architecture itself — step 3's verification was a genuinely correct, deliberate fix for a real problem the original architect had discovered. The failure is entirely in what got carried forward into documentation: a description of *what* the pipeline does (five steps, specific models, a verification step) with no record of *why* step 3 exists in its current form. To the new engineer, doing exactly what a competent engineer should do — reviewing the system, looking for optimization opportunities, questioning apparent redundancy — step 3 reads as inefficiency to be cleaned up, because nothing in front of her signals otherwise. She's not being careless; she's making a reasonable inference from incomplete information, and the documentation is what made that information incomplete.

This connects directly to your Domain 5 governance material, worth naming explicitly: this is structurally the same pattern as a guardrail added after a specific incident — the guardrail (or here, the verification step) looks unnecessary to someone who wasn't present for the incident that justified it, and without a documented record of *why* it exists, it's vulnerable to being quietly removed by someone acting in good faith, reintroducing the exact problem it was built to prevent. The fix, correctly named in B, is documenting rationale specifically for non-obvious decisions: not just "step 3 re-verifies the step 2 calculation" but "step 3 exists because [specific calculation drift issue] was discovered during development; removing this step would reintroduce that bug; see [reference] for details." This turns an opaque, easily-misread design choice into a self-explaining one — a future engineer encountering the same optimization instinct would immediately understand why it's there and what removing it would cost, rather than having to rediscover the problem the hard way, in production.

**Why the others are wrong**

- **A)** places the entire burden of avoiding this failure on "thorough testing before deploying any change," but this assumes the new engineer would have any reason to test specifically for a calculation-drift edge case she has no knowledge exists — you can't design a targeted test for a failure mode you don't know to look for. Good documentation exists precisely to prevent this kind of blind spot; expecting exhaustive testing to substitute for documented context asks engineers to rediscover institutional knowledge that should have been written down in the first place.
- **C)** overcorrects into an unrealistic standard — some legitimate, necessary design decisions will inevitably look non-obvious or even redundant to someone without context (this is true of good engineering generally, not just AI systems), and demanding an architecture contain nothing that could ever be misread by an uninformed reader isn't a realistic or even coherent bar. The actual fix isn't eliminating anything that looks non-obvious — it's documenting the reasoning behind it so it stops being opaque.
- **D)** shifts full responsibility onto the new engineer for not consulting the original architect, but this isn't a realistic or scalable expectation for how documentation-driven handoffs are supposed to work — the entire purpose of writing things down is so that future engineers, who may be working long after the original architect has moved to other projects or left the company entirely, aren't dependent on personally tracking down and asking someone who may no longer be reachable. Documentation exists specifically to make that kind of institutional-memory dependency unnecessary.

**Pattern to remember, opening the documentation arc**: documentation that describes *what* a system does is necessary but not sufficient — the specific, high-value content to capture is the *reasoning* behind non-obvious decisions, especially ones that exist because of a previously-discovered problem, a rejected alternative, or a subtle trade-off that isn't visible just from reading the resulting architecture. Without that rationale, anything that looks unnecessary to a future reader is at genuine risk of being "cleaned up" by someone with no way of knowing what they're actually removing.

Want another documentation scenario, or move to sub-topic 5 (supporting lifecycle phases: discovery, design, handoff, monitoring, iteration)?