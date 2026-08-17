**Scenario**

A startup is building a Claude-powered medical symptom-checker chatbot for a consumer health app. The task genuinely requires nuanced reasoning — interpreting ambiguous, informally-described symptoms, weighing differential possibilities, and communicating uncertainty responsibly — which argues for their most capable (Opus-class) model. However, the startup has two hard constraints: a fixed monthly compute budget that would be exceeded within two weeks at current user growth if every conversation ran on Opus-class, and a product requirement that responses feel conversational and fast (under 2 seconds), which Opus-class doesn't reliably hit at their current traffic.

Three options are on the table:
- **Option 1**: Use Opus-class for every conversation, and accept that the budget will run out mid-month, at which point the service would need to shut down until the next billing cycle.
- **Option 2**: Use a Sonnet-class model for every conversation, accepting somewhat less nuanced reasoning on ambiguous cases, in exchange for meeting both the budget and latency targets consistently.
- **Option 3**: Use Sonnet-class as the default for straightforward symptom queries, but add a lightweight initial check that escalates specifically ambiguous, high-uncertainty, or higher-risk-sounding cases to Opus-class for that portion of the conversation only, accepting slightly higher latency for that smaller subset.

**Question**: Given the stated constraints (budget, latency, and the genuine reasoning need), which option best balances the real trade-offs here, and why?

A) Option 1 — since this is a medical application, accuracy must be maximized without compromise, and running out of budget mid-month is an acceptable cost for ensuring every single conversation gets the most capable reasoning available.

B) Option 2 — since it's the simplest to build and meets both hard constraints (budget, latency) reliably, and any accuracy trade-off is an acceptable cost of doing business at this stage.

C) Option 3 — it applies the "route based on actual need" principle from your accuracy-latency trade-off material: most symptom queries are likely straightforward enough for Sonnet-class to handle well, reserving the more expensive, slower Opus-class reasoning specifically for the subset of cases where the ambiguity or risk genuinely warrants it — respecting the budget and latency constraints for the bulk of traffic while still directing deeper reasoning where the stakes and complexity are highest, rather than either applying it uniformly (unaffordable) or withholding it uniformly (a real quality trade-off on exactly the cases where it matters most).

D) None of the options are viable; the startup should not launch the symptom-checker at all until they can afford Opus-class for every conversation.

Take your best guess and I'll walk through it.


Correct — and this scenario is designed to bring together two threads you've now built independently: the early-exit/routing levers from Domain 3's accuracy-latency material, and model-tier selection from this domain. The exam's hardest trade-off questions often ask you to combine sub-topics like this rather than testing them in isolation.

**Why C is right**

The scenario is engineered so that neither pure option actually satisfies all three real constraints simultaneously — and recognizing that a blended approach exists is the actual skill being tested:

- **Option 1 (Opus everywhere)** satisfies the reasoning-quality need but fails the budget constraint outright — and not in some abstract, hand-wavy way: the scenario states plainly that the service would need to shut down mid-month. A symptom checker going dark for two weeks isn't a minor inconvenience for a health app — it's arguably worse for users than slightly less nuanced reasoning on ambiguous cases, since "no service at all" is a harder failure than "somewhat less nuanced answers." This is the same "argue with a stated hard constraint instead of solving within it" trap from your loan-scheduling and fraud-review scenarios, just with budget as the constraint instead of latency.
- **Option 2 (Sonnet everywhere)** satisfies both hard constraints reliably, but pays a real cost exactly where the scenario told you it matters most: "ambiguous, informally-described symptoms" and "weighing differential possibilities" are explicitly named as needing nuanced reasoning. Applying a uniform, lighter-weight model to *every* case means the hardest, most ambiguous, potentially highest-risk symptom descriptions get the same reasoning depth as the straightforward ones — which is the same uniform-downgrade mistake from your fraud-detection scenario's wrong answer, just relocated from "model per pipeline step" to "model per entire conversation."
- **Option 3** is the early-exit/routing pattern from Domain 3, applied here to model selection specifically: a lightweight check (cheap, fast, running on the default Sonnet tier) identifies which cases are ambiguous or higher-risk-sounding, and *only that subset* gets escalated to Opus-class reasoning. This mirrors your fraud-review scenario almost exactly — there, an early cheap risk check let the system reserve expensive full-pipeline processing for the ~15% of transactions that genuinely needed it; here, an early complexity/risk check reserves expensive Opus-class reasoning for the subset of symptom queries that genuinely need it. Budget and latency stay controlled for the bulk of traffic (straightforward queries, likely the majority), while the reasoning depth that matters most is directed precisely at the cases where ambiguity and stakes are highest — not averaged away across all traffic, and not applied at a cost the business can't sustain.

**Why the others are wrong**

- **A)** repeats the "accuracy must always be maximized regardless of constraints" mistake, and this scenario sharpens why that's wrong: the "accuracy-maximizing" choice here doesn't even deliver more accuracy in practice — it delivers a mid-month shutdown, which is a worse outcome for real users than a calibrated trade-off. Treating "this is medical" as an automatic override for cost constraints ignores that the constraint isn't optional or negotiable — it's a stated hard limit the design has to work within, the same as the payments-checkout SLA in your fraud-review scenario.
- **B)** treats "simplest to build" and "meets the hard constraints" as sufficient justification on its own, without weighing what's being given up. Unlike your triage-email scenario earlier (where a simple, well-performing, already-adequate system was correctly left alone), this scenario explicitly tells you the task has a genuine reasoning-depth requirement that a uniform lighter model won't fully serve — the accuracy trade-off isn't hypothetical here, it's named directly in the setup as the reason Opus-class was being considered in the first place.
- **D)** overcorrects into inaction, treating "we can't afford the theoretically ideal setup for every case" as equivalent to "we can't build this responsibly at all." This ignores that Option 3 exists and genuinely reconciles the constraints — refusing to launch discards a workable, well-reasoned design in favor of an all-or-nothing framing the scenario doesn't actually support.

**Pattern to remember, tying Domain 2 and Domain 3 together**: 
- Model-tier trade-offs aren't only resolved by picking one tier for an entire feature (as in your earlier model-selection scenarios) — when a feature's difficulty genuinely varies case-by-case, the same routing/early-exit discipline from accuracy-latency trade-offs applies directly to *which model tier* handles which case, not just to *which processing steps* run.
- A cheap initial triage step deciding "does this need the expensive model" is a legitimate, exam-relevant design pattern in its own right, distinct from simply picking one model for the whole feature.

Want another model-selection trade-off scenario, or move to prompt caching / context optimization scenarios now?
