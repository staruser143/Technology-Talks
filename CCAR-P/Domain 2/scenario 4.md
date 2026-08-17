**Scenario**

A company built a customer-facing assistant on a Sonnet-class model to answer questions about their software product's features and pricing tiers. After launch, they notice the assistant frequently gives inconsistent answers about pricing — sometimes citing the current pricing tiers correctly, sometimes citing outdated pricing from an old promotional period, and occasionally blending details from two different tiers into one incorrect answer. The engineering lead's proposed fix: "the model clearly isn't capable enough to handle this reliably — let's upgrade to the most capable Opus-class model, since a smarter model should stop making these mistakes."

Before implementing the upgrade, another engineer checks the system prompt and finds: the assistant has no retrieval or reference document for current pricing at all — it's relying entirely on whatever pricing information happened to be in its training data, which includes outdated information from before the product's last two pricing changes, mixed with general knowledge about typical SaaS pricing structures.

**Question**: Is upgrading to a more capable model the right fix here? What should the team actually do?

A) Yes — upgrading to Opus-class is the correct fix; more capable models have more accurate and current training data, so the pricing inconsistencies will resolve once the model itself is smarter.

B) No — this isn't a model-capability problem at all; it's a missing-grounding problem. No model, regardless of capability tier, can reliably know a company's current, specific pricing without being given that information directly (e.g., via RAG retrieval of the current pricing page, or the pricing table included directly in the system prompt) — all models rely on training data that goes stale and was never guaranteed to have current pricing to begin with. The fix is to supply current pricing as grounded context, not to upgrade model tier.

C) Yes, but only partially — the team should upgrade to Opus-class AND also add pricing documentation, since both changes together are always better than either alone.

D) No — the team should downgrade to the smallest available model, since smaller models are less likely to blend information from multiple sources incorrectly.

Take your best guess and I'll walk through it.



Correct — and this scenario is arguably the most important one in this set, because it tests whether you can tell a **model-selection problem** apart from a **context/grounding problem** that just looks like one on the surface. That distinction shows up constantly in real production debugging, and the exam wants you to catch it before reaching for the expensive fix.

**Why B is right**

The engineering lead's diagnosis — "the model isn't capable enough" — makes an assumption that the *second* engineer's investigation directly disproves: there's no pricing information being supplied to the model at all. Without a system prompt, retrieval mechanism, or reference document containing current pricing, the model has exactly one source to draw from — whatever pricing-adjacent information exists somewhere in its training data, which is inherently a mix of outdated information (pre-existing the last two pricing changes), and general SaaS pricing patterns that happen to sound plausible but aren't specific to this company. That's not a reasoning failure — it's the model doing exactly what you'd expect an ungrounded model to do: producing plausible-sounding but unreliable answers about something it was never given accurate information about.

This is exactly the diagnostic instinct from Sample Question 3 in the official guide (confident-but-wrong answers pointing to a retrieval/grounding issue, not a model issue) — just relocated from a RAG-specific scenario into general model-selection reasoning, which is precisely why it's worth testing in Domain 2 as well as Domain 3. **No amount of model capability compensates for missing information.** A more capable model would still have no current pricing data to draw from — it might produce more *fluent* wrong answers, or blend sources more *convincingly*, but the fundamental problem (no grounded source of truth) remains completely untouched.

The actual fix follows directly: supply current pricing as explicit context — either via RAG retrieval from the live pricing page/documentation, or directly embedded in the system prompt if the pricing table is small and stable enough to include outright (tying back to the caching material — stable reference content like this is exactly what belongs in the cacheable prefix). Once the model has an authoritative, current source to ground its answer in, the inconsistency has a real chance of resolving — something a model upgrade alone cannot guarantee.

**Why the others are wrong**

- **A)** repeats the engineering lead's flawed diagnosis as if it were the answer, and the assumption underneath it — "more capable models have more accurate and current training data" — doesn't hold up: model capability and training data recency are different properties, and even a highly capable model's training data has a cutoff and was never guaranteed to include this specific company's exact current pricing tiers to begin with. A smarter model reasoning more fluently from the same absent/stale information isn't a fix, it's the same failure wearing a more convincing voice.
- **C)** hedges by doing both, but the "add pricing documentation" half already fully solves the problem, making the Opus upgrade an unnecessary added cost bundled in for no diagnosed reason — this is the same mistake as your Domain 3 fraud-scenario wrong-answer pattern, layering an expensive fix onto a targeted one instead of first checking whether the cheap, correctly-targeted fix alone resolves the actual root cause. "Both together are always better" isn't a real justification — it's avoiding the harder diagnostic work of confirming what the fix actually needs to be.
- **D)** proposes an unrelated, unmotivated change with no logical connection to the diagnosed problem — model size doesn't determine how much a model "blends" ungrounded information, and downgrading does nothing to address the actual gap (missing current pricing data) while likely making other aspects of the assistant's performance worse for no offsetting benefit.

**Pattern to remember, closing out model selection**: 
- Before concluding a quality problem is a model-capability issue, check whether the model was actually given what it needs to succeed.
-  Symptoms that look like "the model isn't smart enough" — confident wrong answers, inconsistency, blending of stale and current information — are very often actually **missing or stale grounding**, and no model tier upgrade fixes a problem that's really about what's (or isn't) in the context.
- This is the single most common trap in real-world model-selection debugging, and it's why the exam pairs it with the RAG diagnostic instinct from Domain 3 — the two domains share this exact failure signature.

That's solid, well-rounded coverage of model selection. Want to move to prompt caching / context optimization scenarios now, or keep going with model selection?
