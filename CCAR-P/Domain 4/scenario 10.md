**Scenario**

A subscription streaming service wants to test whether a new Claude-powered content-recommendation prompt (variant) improves user engagement compared to their current one (control). They run the test for a properly predetermined duration with adequate sample size — 15,000 users per arm — and this time the results are statistically significant: the variant shows a genuinely real, reliable 8% increase in "average watch time per session," the single metric they set out to test.

Excited by this clean, statistically solid result, the team ships the variant to 100% of users immediately. Two weeks after full rollout, a separate customer-retention analysis (not something tracked as part of the original A/B test) shows unsubscribe rate has ticked up noticeably compared to the same period the previous month. Investigating further, product analysts find that the new recommendation prompt achieves its watch-time increase largely by recommending longer-runtime content (multi-hour documentaries, extended cuts) even when a user's viewing history suggests they generally prefer shorter content — technically increasing time-per-session, but leaving a meaningful share of users feeling like the recommendations no longer fit their actual taste.

**Question**: Given that the original A/B test was statistically sound and correctly run, what went wrong, and what should the team have done differently?

A) Nothing went wrong with the testing methodology; a properly powered, statistically significant result is sufficient grounds for a full rollout decision on its own, and the unsubscribe increase is likely an unrelated coincidence.

B) The core issue is that the test measured only the one metric it was designed to improve (watch time per session) without also tracking other metrics that could reveal a hidden cost of achieving that improvement — the same proxy-metric-divergence pattern from earlier, now showing up as an A/B testing design gap rather than a slow drift: optimizing for and validating against a single metric can produce a real, statistically genuine win on that metric while masking damage on an unmeasured one (here, recommendation fit and retention). The team should have tracked a fuller metric set in the original test itself — including downstream signals like subscription retention or recommendation satisfaction, not just the primary target metric — rather than only discovering the trade-off two weeks after a full rollout via an unrelated analysis.

C) The issue is that the test needed an even larger sample size than 15,000 per arm; the retention problem would have been caught with more users in the original test.

D) The issue is that A/B testing is fundamentally the wrong methodology for recommendation systems, and the team should switch to exclusively using human review panels for any future prompt changes.

Take your best guess and I'll walk through it.