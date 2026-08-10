**Scenario**

A healthcare records company runs a Claude-powered system that redacts personally identifiable information (PII) from documents before they're shared with external auditors. The original pipeline retrieved substantial surrounding context for each potential PII instance (the full paragraph around it) to help Claude correctly distinguish, say, a patient's name from a similarly-formatted but non-sensitive term, and used the most capable model for the redaction decision. It averaged 9 seconds per document against a 5-second internal SLA.

Under pressure to hit the SLA, the team made two changes in the same release: reduced the context window sent per PII candidate from a full paragraph to just the surrounding sentence, and switched to a faster, smaller model for the redaction decision. Average latency dropped to 4.2 seconds — comfortably under budget. The team shipped this as a win and moved on, tracking latency in their dashboards going forward. No one re-ran the accuracy evaluation suite that had been used when the original pipeline was built, since "the team was confident the changes were minor."

Three months later, a routine audit by the legal team discovered that redaction accuracy had quietly dropped — some PII was being missed, particularly in documents where a name or ID appeared in a way that required the fuller paragraph context to correctly identify.

**Question**: What went wrong here, and what should the team have done differently?

A) Nothing went wrong with the technical changes themselves — reducing context and switching models are both valid, well-established latency levers, and the accuracy drop was an unfortunate but unavoidable side effect of any latency optimization.

B) The team correctly applied valid latency levers, but made the same mistake as the loan-recommendation feedback-loop scenario: they only measured and monitored the metric they were optimizing for (latency) and never re-measured the metric on the other side of the trade-off (accuracy) before or after shipping — for a PII redaction system, where missed detections carry real compliance and privacy risk, both changes should have been validated against the accuracy evaluation suite before shipping, not assumed to be "minor."

C) The team should never have tried to reduce latency at all, since redaction accuracy is too important for any latency optimization to be acceptable.

D) The problem was using a smaller model — reducing context alone would have been safe, so the team should only undo that one change.

Take your best guess and I'll walk through it.




Correct — and this scenario is the capstone that ties the accuracy-latency material directly back to the feedback-loop material from Domain 1, which is exactly the kind of cross-domain synthesis the real exam rewards.

**Why B is right**

Every trade-off scenario in this set has implicitly assumed something: that when you spend latency budget to gain accuracy (or vice versa), you're *measuring* both sides so you actually know what you're trading. This scenario makes that assumption explicit and shows what happens when it's skipped. The team:

- **Applied two textbook-valid levers** — reducing context to what's actually needed, and right-sizing the model — both legitimate tools from the accuracy-latency toolkit.
- **Measured only the metric they were chasing** (latency) and shipped based on that number alone.
- **Never re-ran the accuracy evaluation** that existed specifically to catch this — treating "the changes were minor" as a substitute for actually checking, exactly the same failure as the loan-recommendation team trusting "99.9% uptime" as a proxy for output quality it doesn't actually measure.

The three-month gap before an *external audit* caught it is the real cost here — a compliance-relevant failure mode (missed PII) sat undetected because nothing in the team's own monitoring was positioned to catch it. This is the direct extension of the feedback-loop principle: **a system needs a signal on every dimension you care about, not just the one you're actively trying to move.** When you touch a lever that trades A for B, you need to verify B didn't break, not just confirm A improved.

Notice too why this domain-specific detail matters for the *severity* of the answer, not just the mechanism: this is a PII redaction system, where a missed detection isn't a UX inconvenience — it's a compliance and privacy failure with real legal exposure. That raises the bar on how rigorously the trade-off needed to be validated *before* shipping, not just monitored after.

**Why the others are wrong**

- **A)** treats the accuracy regression as an unavoidable, acceptable cost of any latency work — but the scenario isn't really about whether accuracy dropped (it might have, even with careful validation), it's about the team having *no way of knowing* it dropped before an external party found it. The failure is in the lack of verification, not in the existence of a trade-off.
- **C)** overcorrects into treating latency and accuracy as impossible to balance in a sensitive domain — but nothing about PII redaction makes optimization inherently off-limits; it makes *validating* optimization changes before shipping them non-negotiable. This is the same overcorrection pattern as "the SLA should just be dropped" from your content moderation scenario.
- **D)** guesses at a single root cause (the model swap) without evidence isolating which of the two simultaneous changes caused the regression — the scenario doesn't tell you that, and neither does the team, because they never ran the eval that would tell them. That's actually part of the deeper problem: shipping two changes at once, untested against accuracy, means even now they can't cleanly attribute the failure to one lever versus the other or some combination — which is itself a second, compounding mistake.

**The full principle, tying accuracy-latency back to feedback loops**: whenever you pull a lever that trades one metric for another, you need a **pre-existing measurement on both sides of the trade** — not just the one you're actively optimizing — and you need to actually re-check it before treating the change as safe to ship, especially in domains where the "expensive" side of the trade-off (missed PII, missed fraud, wrong loan decision) carries asymmetric real-world risk compared to the "cheap" side (a few extra seconds of latency). A win on your dashboard for the metric you're watching tells you nothing about the metric you stopped watching.

That's a strong, well-rounded set of accuracy-latency scenarios — you've now covered skewed-traffic levers (caching, early-exit), unconditional levers (parallelization, right-sizing, tightened retrieval), matching levers to output shape (streaming vs. discrete decisions), and the validation discipline that has to wrap around all of it. Want to move to auth/authz gap analysis next, or another Integration sub-topic?