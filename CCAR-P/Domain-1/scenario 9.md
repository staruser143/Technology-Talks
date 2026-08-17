**Scenario**

A fintech company deployed a Claude-powered workflow that reviews loan applications and drafts an approval/denial recommendation with reasoning, which a human loan officer then reviews and finalizes. The system launched three months ago. Since then: input (application data) flows in cleanly, processing runs the fixed pipeline reliably, output (draft recommendations) gets delivered to loan officers on time. The team is proud that uptime has been 99.9% and latency has stayed flat. However, nobody has looked at how often loan officers actually agree with Claude's draft recommendations, no one is tracking which types of applications get overridden most, and there's no process for feeding officer corrections back into prompt or pipeline improvements. Three months in, the team has no idea whether the system's recommendation quality has stayed steady, improved, or quietly degraded.

**Question**: What is this system missing, and what's the risk of leaving it as-is?

A) Nothing critical — 99.9% uptime and flat latency indicate the system is healthy; recommendation quality is a separate, lower-priority concern.

B) The system is missing a feedback loop — without tracking officer agreement/override rates and feeding corrections back into the system, the team has no way to detect quality drift, and problems could go unnoticed indefinitely even while the system appears "healthy" by infrastructure metrics.

C) The system needs to be rebuilt as agentic so Claude can self-correct its own recommendations without waiting for human feedback.

D) The system is fine because a human loan officer reviews every recommendation before it's finalized, so any errors are automatically caught and no additional feedback mechanism is needed.

Take your best guess, and I'll walk through the reasoning.





Correct — and this scenario is designed to expose a blind spot that's easy to miss precisely because everything *looks* fine on the surface.

**Why B is right**

This goes back to the four-layer architecture from earlier: input → processing → output → **feedback loop**. The team has instrumented and is confidently reporting on the first three layers — uptime, latency, delivery — but has built nothing for the fourth. That's the trap: **infrastructure health and output quality are two different things, and monitoring one tells you nothing about the other.** A system can have perfect uptime while its recommendations quietly get worse — a data drift in application patterns, a subtle prompt regression from an unrelated change, a model update that shifts behavior — and none of that would ever show up in latency or uptime dashboards.

The specific missing pieces the scenario calls out are the diagnostic tell:
- **No tracking of officer agreement/override rates** — this is the single cheapest, most direct quality signal available, since a human is already reviewing every output. Ignoring it means throwing away free ground-truth data.
- **No breakdown of which application types get overridden most** — without this, even if someone noticed a decline, they'd have no way to localize *where* the system is failing (a specific loan type? a specific edge case?).
- **No process to feed corrections back** — even if problems were noticed, there's no mechanism to actually act on them and improve the system.

The risk stated plainly: three months in, quality could have degraded and nobody would know. That's the concrete cost of skipping the feedback loop — not a theoretical best practice, but an actual blind spot with real consequences in a regulated, high-stakes domain like lending.

**Why the others are wrong**

- **A)** makes the exact mistake the scenario is testing for — conflating infrastructure metrics with output quality. Uptime and latency tell you the pipes are working; they say nothing about whether what's flowing through them is any good.
- **C)** overcorrects into architecture that doesn't fix the actual gap. Making the system agentic doesn't create a feedback loop by itself — self-correction still requires *something* to measure against (which is exactly what's missing here). You'd have an agent with no more insight into its own drift than the current system has. This is also the same "reach for agentic when it isn't the right fix" trap from your investigation scenario, just applied to a different problem.
- **D)** is the most tempting wrong answer, and worth sitting with. A human reviewing every output does provide a *safety net* — bad recommendations shouldn't reach a final decision unchecked. But a safety net isn't a feedback loop unless someone is actually **measuring what the human does with each output and using it to improve the system.** Right now, the officer's agree/override decision is generated and then thrown away — it's not being captured, aggregated, or fed back anywhere. The presence of human review answers "can errors reach production," not "does anyone know if the system is getting better or worse over time." Those are different questions, and this domain tests both, but they're not the same safeguard.

**Pattern to remember**:
- A feedback loop needs three things to actually function — a **signal** (some measurable proxy for quality, like agreement rate), **aggregation** (tracking it over time and by category, not just anecdotally), and an **action path** (a defined way corrections actually change the prompt, pipeline, or model choice).
- A system missing any of those three doesn't have a real feedback loop, even if it has strong infrastructure metrics or even a human in the loop somewhere in the process.

