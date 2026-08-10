**Scenario**

A SaaS company's Claude-powered API responds to customer integration requests, with a documented SLA promising "fast, reliable responses." The team tracks and reports one primary latency metric: **average response time**, which sits at a healthy 1.2 seconds — comfortably fast, and the number they include in their public status page and quarterly reliability reports.

A enterprise customer escalates a complaint: their automated integration is timing out and failing roughly 3-4% of the time, well above what a "1.2 second average" would suggest should happen. Investigating further, the team pulls a full latency distribution instead of just the average and finds: the vast majority of requests (roughly 95%) complete in under 800ms, genuinely fast — but a small, consistent tail of requests (around 3-5%) take 8-12 seconds, occasionally longer, often enough to trip the customer's own integration timeout. Because this slow tail is a small fraction of total volume, it barely moves the average at all — the fast majority dominates the mean, making the aggregate number look excellent while a real, customer-impacting problem sits hidden in the tail.

**Question**: What's the core issue with using average response time as the primary latency metric here, and what should the team do?

A) There's no issue — a 1.2 second average is a strong result, and the enterprise customer's timeout problem is likely a configuration issue on their end unrelated to the company's actual latency performance.

B) The core issue is the same pattern as an aggregate accuracy metric hiding minority-class failure, applied to latency: an average is dominated by the large fast majority and can completely mask a real, consistent tail of slow requests that matters a great deal to the users who experience it. The team should track and report percentile-based metrics (p95, p99) alongside the average, since these specifically capture tail behavior that an average structurally cannot, and should treat SLA commitments and reliability reporting based on tail percentiles rather than (or in addition to) the mean.

C) The issue is that averages should never be used for any metric under any circumstances; the team should exclusively report percentiles for every metric they track, including accuracy and cost.

D) The issue is unrelated to metric choice; the enterprise customer's integration should simply be reconfigured with a longer timeout to accommodate whatever latency the system produces.

Take your best guess and I'll walk through it.




Correct — and this closes the loop precisely: the mechanism is structurally identical to your fraud-detection scenario, just with "average" playing the role "overall accuracy" played there, and "the slow tail" playing the role "the fraud minority class" played there.

**Why B is right**

Run the same diagnostic the skeptical engineer ran in your fraud scenario, just for latency: an average is a single number computed across the *entire* distribution, and when that distribution is dominated by a large fast majority (95% under 800ms), the average is going to reflect that majority almost regardless of what the remaining tail looks like — a 3-5% slice of requests running 8-12 seconds barely moves a mean that's otherwise anchored by thousands of sub-second requests. That's exactly what happened: 1.2 seconds looks like a strong, uniform result, but it's actually an average of two very different populations blended into one number that represents neither well.

The consequence is identical in shape to the fraud scenario too: the metric being tracked and reported (average) is structurally insensitive to the exact failure mode that matters most to the people it affects — just as overall accuracy was insensitive to missed fraud, average latency is insensitive to the slow tail, and in both cases the aggregate looked *good* precisely because the majority-case performance was masking a real, consequential minority-case failure. The enterprise customer experiencing 8-12 second responses and timing out isn't an edge case the company can dismiss — they're a real instance of the exact population the average was hiding.

The fix is the direct percentile equivalent of "measure recall specifically on the fraud class": **p95 and p99 metrics** specifically capture what's happening at the tail — "95% of requests complete in under X" and "99% of requests complete in under Y" — numbers that can't be dominated by the fast majority the way a mean can, because they're specifically reporting on where the *slow* end of the distribution sits, not blending it away into an average. SLA commitments and reliability reporting anchored to tail percentiles (rather than, or in addition to, the mean) would have surfaced this problem long before an enterprise customer had to escalate it — the same "if you'd tracked the right metric, you'd have caught this before it became a customer-facing crisis" lesson from nearly every observability scenario in Domain 3.

**Why the others are wrong**

- **A)** dismisses a specific, measured, customer-reported problem by assuming the cause lies elsewhere, without any evidence supporting that assumption — and the team's own follow-up investigation directly contradicts it: the distribution data confirms the slow tail is real, internal, and consistent (3-5% of requests, not a one-off). This is the same "the aggregate score is good, so any contradicting evidence must be wrong or irrelevant" complacency rejected in your fraud-detection and proxy-metric scenarios.
- **C)** overcorrects into a blanket rule ("never use averages for anything") that isn't supported by the lesson here — averages are perfectly fine, even preferable, for metrics where the distribution is reasonably uniform and tail behavior isn't the primary concern; the issue isn't "averages are always wrong," it's "averages are the wrong choice specifically when tail behavior matters and the distribution has a meaningful skew," which is a targeted diagnosis, not a universal indictment of a whole class of statistic.
- **D)** treats the symptom (the customer's timeout tripping) as the thing to fix, rather than the cause (the system genuinely produces 8-12 second responses for a real slice of requests) — telling the customer to raise their timeout doesn't make the underlying slow tail disappear, it just makes it less visible to that one customer while leaving the actual performance problem, and its cost to every other affected request, completely unaddressed.

**Pattern to remember, closing the loop on this entire metric-selection thread**: the "aggregate hides tail/minority failure" lesson isn't specific to accuracy — it's a general property of any single summary statistic computed across a non-uniform population, whether that population is split by class (fraud vs. legitimate) or by performance distribution (fast majority vs. slow tail). Any time a metric reduces a diverse population to one number, ask what that number could be hiding, and specifically whether the population it's summarizing has meaningful internal variation that the aggregate is structurally built to smooth over.

That's genuinely complete coverage of metric selection now — five scenarios spanning accuracy-dimension mismatches, minority-class masking, missing-dimension gaps, proxy-metric divergence, and the aggregate-vs-tail latency version of the same core lesson. Ready to move into eval dataset design.