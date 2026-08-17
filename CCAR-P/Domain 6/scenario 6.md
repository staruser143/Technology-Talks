**Scenario**

Early in a project, before any real evaluation has been conducted, a prospective client's procurement team pushes the architect for a firm commitment: "We need you to guarantee 98% accuracy in the contract before we sign, so we can build our internal business case around a specific number." The architect has strong general experience with similar systems, but no actual evaluation data yet for *this* specific task, dataset, or edge-case profile — the honest answer is that the real number won't be known until a proper eval is built and run against representative data.

Three ways to respond:

**Option A**: Commit to the requested 98% figure in the contract, reasoning that her general experience suggests something in that range is plausible, and refusing to provide a specific number risks losing the deal to a competitor willing to commit to one.

**Option B**: Refuse to provide any numbers or commitments at all until a full evaluation is complete, telling the client "we can't discuss performance expectations until after the system is built," even though this leaves their business-case planning with nothing to work from in the meantime.

**Option C**: Be honest that a specific, reliable number isn't yet knowable, explain why (no evaluation has been run against representative data for this specific task yet), and propose a concrete path to get there — e.g., a scoped pilot phase with a properly-designed eval set (drawing on everything from Domain 4) that will produce a real, trustworthy number within a defined timeframe, with an interim range based on comparable past work clearly labeled as a preliminary estimate, not a guarantee.

**Question**: Which option reflects sound practice, and why?


**Question**: Which option reflects sound practice, and why?

A) Option A — committing to a specific figure demonstrates confidence and closes the deal, and since her general experience makes the number plausible, some imprecision is an acceptable cost of winning the business.

B) Option B — refusing to provide any information until after full evaluation protects the architect from being held to an inaccurate early estimate, which is the more professionally cautious approach.

C) Option C — honestly distinguishing between what's genuinely known (comparable past work suggesting a rough range) and what isn't yet known (this specific task's real performance, which requires an actual eval), while proposing a concrete path to get a trustworthy number, gives the client something usable now (a labeled preliminary estimate) without pretending to a precision that doesn't exist yet. This mirrors the discipline from Domain 4's evaluation material directly — a number isn't trustworthy just because it's confidently stated, and presenting an unvalidated guess as a firm guarantee sets up a commitment that may not hold once real evaluation data exists, which is worse for the client's actual business-case planning than an honest, labeled estimate with a clear path to certainty.

D) All three options are equally defensible responses to a difficult client pressure situation, and the choice mostly comes down to the architect's personal risk tolerance and negotiating style rather than any substantive best practice.


Correct — and this scenario names a distinct discipline from your last two: not disclosing a *known* trade-off (your Latin-terminology scenario) or translating a *known* trade-off into business terms (your autonomy-vs-workflow scenario), but honestly communicating the *boundary of what's actually known at all* — resisting the pressure to manufacture false precision because a stakeholder wants a firm number.

**Why C is right**

The procurement team's request is understandable — they want a number to build a business case around — but the honest situation is that no real evaluation has been run yet, which means any specific figure offered right now would be a guess dressed up as a commitment. C's approach does the actual hard thing: it separates what's genuinely known (general experience with comparable systems suggesting a rough range) from what isn't (this specific task's real accuracy, which requires an actual eval built and run, exactly per your Domain 4 material — a representative dataset, appropriate metrics, not just intuition). This directly mirrors the discipline you built throughout the entire evaluation domain: a number's trustworthiness comes from how it was actually measured, not from how confidently it's stated. Committing to "98%" without having run anything is precisely the kind of unvalidated number your eval-staleness and ground-truth scenarios taught you to distrust — except here the architect would be the one manufacturing it, under pressure, rather than discovering someone else already had.

Critically, C doesn't just refuse to help — it gives the client something genuinely useful *now* (a labeled preliminary range, clearly marked as an estimate rather than a guarantee) *and* a concrete path to real certainty (a scoped pilot with a proper eval, on a defined timeline). This serves the client's actual underlying need — planning their business case — better than either extreme, because a number they can trust the *nature* of (this is a rough estimate, here's when we'll have something firmer) is more useful for real planning than a falsely precise commitment that might not hold once real data exists.

**Why the others are wrong**

- **A)** manufactures false confidence to win the deal, exactly the failure mode this scenario is built to test against. If the real evaluated number later comes in at, say, 89% instead of 98%, the architect hasn't just delivered a disappointing result — she's delivered a broken commitment the client built real business decisions around, which is a far worse outcome than having been honest about uncertainty upfront. This is the stakeholder-communication version of presenting an untrustworthy metric as if it were solid — the same failure as your proxy-metric-divergence scenario, just happening at the contract-negotiation stage instead of after months of production drift.
- **B)** overcorrects into unhelpful rigidity — refusing to offer *any* information, including a clearly-labeled rough estimate based on real comparable experience, leaves the client with nothing to plan around at all, which isn't actually more honest, it's just less useful. There's a real difference between "I won't guarantee a number I haven't measured" (appropriately cautious) and "I won't discuss performance at all until everything is finished" (unnecessarily obstructive) — C finds the useful middle ground B refuses to offer.
- **D)** treats this as a matter of personal style with no substantive right answer, but the scenario is specifically testing whether you recognize that fabricating precision you don't have (A) has real, foreseeable downstream costs (a broken commitment), while C's approach — honest about uncertainty, useful in the interim, with a concrete path to real certainty — serves both the immediate relationship and the eventual outcome better. This isn't a stylistic preference; it's the same "measure before you claim to know" discipline that ran through the entirety of Domain 4, now tested under real negotiating pressure.

**Pattern to remember, closing this trade-off communication arc**: the honest handling of genuine uncertainty is a distinct skill from disclosing a known limitation or translating a known trade-off — it requires resisting real pressure (a client wanting a firm number, a deal potentially riding on it) to avoid manufacturing false precision, while still providing something useful (a labeled estimate, a concrete path to a real answer) rather than either fabricating confidence or refusing to engage. The through-line connecting all three of your trade-off scenarios: good communication means the stakeholder ends up with an *accurate* picture of the system — its real capabilities, its real limitations, and the real boundary between what's known and what isn't — never a falsely comfortable one.

That's a solid, well-rounded set covering communicating trade-offs. Want to move to sub-topic 3 (managing stakeholder feedback loops and expectation alignment, including SLAs) now?