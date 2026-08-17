**Scenario**

A subscription streaming service wants to test whether a new Claude-powered content-recommendation prompt (variant) improves user engagement compared to their current one (control). They run the test for a properly predetermined duration with adequate sample size — 15,000 users per arm — and this time the results are statistically significant: the variant shows a genuinely real, reliable 8% increase in "average watch time per session," the single metric they set out to test.

Excited by this clean, statistically solid result, the team ships the variant to 100% of users immediately. Two weeks after full rollout, a separate customer-retention analysis (not something tracked as part of the original A/B test) shows unsubscribe rate has ticked up noticeably compared to the same period the previous month. Investigating further, product analysts find that the new recommendation prompt achieves its watch-time increase largely by recommending longer-runtime content (multi-hour documentaries, extended cuts) even when a user's viewing history suggests they generally prefer shorter content — technically increasing time-per-session, but leaving a meaningful share of users feeling like the recommendations no longer fit their actual taste.

**Question**: Given that the original A/B test was statistically sound and correctly run, what went wrong, and what should the team have done differently?

A) Nothing went wrong with the testing methodology; a properly powered, statistically significant result is sufficient grounds for a full rollout decision on its own, and the unsubscribe increase is likely an unrelated coincidence.

B) The core issue is that the test measured only the one metric it was designed to improve (watch time per session) without also tracking other metrics that could reveal a hidden cost of achieving that improvement — the same proxy-metric-divergence pattern from earlier, now showing up as an A/B testing design gap rather than a slow drift: optimizing for and validating against a single metric can produce a real, statistically genuine win on that metric while masking damage on an unmeasured one (here, recommendation fit and retention). The team should have tracked a fuller metric set in the original test itself — including downstream signals like subscription retention or recommendation satisfaction, not just the primary target metric — rather than only discovering the trade-off two weeks after a full rollout via an unrelated analysis.

C) The issue is that the test needed an even larger sample size than 15,000 per arm; the retention problem would have been caught with more users in the original test.

D) The issue is that A/B testing is fundamentally the wrong methodology for recommendation systems, and the team should switch to exclusively using human review panels for any future prompt changes.

Take your best guess and I'll walk through it.



Correct — and this scenario is deliberately built to show that a *methodologically flawless* A/B test can still lead to a bad rollout decision, because the flaw here isn't in *how* the test was run (sample size, duration, statistical rigor were all done correctly) — it's in *what* was measured.

**Why B is right**

This is your satisfaction-vs-reopen-rate scenario, replayed at a different point in the lifecycle: there, the divergence emerged slowly over months of unmonitored iteration against a single metric; here, it's baked into a single, well-executed A/B test that only ever looked at one metric by design. The mechanism is identical — watch-time-per-session is a genuine, real, statistically valid signal, but it's a **proxy** for the actual goal (a recommendation system that serves users well and keeps them subscribed), not the goal itself. The variant found a real way to move that specific proxy: recommend longer content, and average watch time per session goes up almost mechanically, regardless of whether the recommendations actually fit what the user wanted. The 8% lift is completely real and statistically sound — the test wasn't broken, it just answered a narrower question than the team thought it was asking.

This is exactly the "choosing the right metrics to compare" principle from the deep dive: "a well-designed A/B test tracks the *full* metric set... not just the one metric the change was intended to move." Had the original test also tracked something like recommendation-fit satisfaction, or even a short-term proxy for retention risk, the trade-off might have been visible *inside* the test itself — before a full rollout, while it was still contained to 15,000 users rather than the entire subscriber base — instead of surfacing two weeks later via an unrelated analysis, after the damage was already live for everyone.

**Why the others are wrong**

- **A)** repeats the exact "statistically significant on my one tracked metric = safe to ship" complacency the scenario is built to dismantle — statistical soundness only tells you the *measured* effect is real, it says nothing about effects you didn't measure. Dismissing the unsubscribe increase as coincidental, immediately after the investigation directly traced it to the variant's behavior (recommending longer content misaligned with viewing history), ignores a specific, evidenced causal explanation in favor of an unsupported one.
- **C)** misdiagnoses the category of problem — this was never a sample-size or power issue; 15,000 users per arm was a properly powered test for the metric it tracked, and it detected that metric's effect correctly and reliably. No amount of additional users in the same single-metric test design would have caught a trade-off on a *different* metric that was never being tracked in the first place — more volume doesn't create visibility into a dimension you never measured.
- **D)** overcorrects into abandoning A/B testing entirely for an entire category of system, when the actual, narrower lesson is "track more than one metric within the test" — the same "discard the whole tool because one dimension of it was incomplete" overcorrection you've now rejected in nearly every domain (guardrails, MCP, caching, automated eval metrics). A/B testing worked exactly as designed here; it just wasn't asked the full question.

**Pattern to remember, connecting this directly to your earlier proxy-metric scenario**: single-metric optimization is dangerous whether it happens through slow, unmonitored iteration (your satisfaction-score scenario) or through a single, well-run, statistically sound test (this scenario) — the failure mode is the same underlying risk wearing two different timelines. The defense is also the same in both cases: never let one metric, however real and statistically solid its result, stand alone as the sole basis for a rollout decision — pair it with the metrics that could reveal what achieving that win might be quietly costing elsewhere.

Want another A/B testing scenario, or move to the formal diagnostic process for prompt failure vs. hallucination vs. model mismatch?