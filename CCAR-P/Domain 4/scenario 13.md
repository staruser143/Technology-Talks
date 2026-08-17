**Scenario 1 — Iterative testing, not a single gate**

A travel booking company ran an A/B test 8 months ago comparing two versions of their Claude-powered itinerary-suggestion prompt. Version B won clearly — higher click-through to booking, statistically significant, properly randomized, well-powered. It's been running as the sole production version ever since, with the team treating that 8-month-old result as settled and moving on to other priorities.

In the time since, the company's user base has shifted meaningfully: international bookings now make up 40% of traffic (up from under 10% at the time of the original test), and a new loyalty-tier feature launched three months ago changes what kind of itinerary suggestions loyalty members actually want to see. Nobody has re-tested Version B against any alternative since the original test, and no one has checked whether Version B is still the best-performing option given how different the traffic and user base now look.

**Question**: What's the core issue here, and what should the team do?

A) There's no issue — Version B won a properly-run, statistically significant test, and a valid experimental result doesn't need to be revisited once established; the original test remains the authoritative answer indefinitely.

B) The core issue is treating a single A/B test result as a permanent, one-time gate rather than as a conclusion valid for the conditions under which it was tested — the user base and product context have changed substantially since the original test (international traffic share, a new loyalty feature), and Version B's advantage was never re-validated under these new conditions. The team should periodically re-test (or at minimum re-evaluate) current production performance against alternatives, especially after significant shifts in traffic composition or product features, rather than treating an 8-month-old win as permanently settled.

C) The issue is that the original test wasn't run long enough; an 8-month test duration would have been required to capture all future traffic shifts within the test itself.

D) The issue is irrelevant to A/B testing methodology; changes in traffic composition are a marketing concern that has nothing to do with whether Version B remains the right prompt.

Take your best guess on Scenario 1, and then I'll give you Scenario 2 on when A/B testing isn't the right tool at all.



Correct — and this scenario is the A/B testing version of the exact same staleness principle you already nailed with the eval-set and ground-truth scenarios, just applied to "is this still the right production choice" instead of "is this eval set still measuring the right thing."

**Why B is right**

A/B test results are conclusions about performance *under the conditions the test was run in* — the traffic mix, the product features, the user base at that specific point in time. Version B's win 8 months ago was a real, valid finding, but it was a finding about a world where international bookings were under 10% of traffic and no loyalty-tier feature existed. That world no longer exists — international traffic has quadrupled its share, and an entirely new feature now shapes what a meaningful chunk of users actually want from itinerary suggestions. Nothing about the *original* test was flawed; the issue is that its conclusion has an implicit "as of these conditions" attached to it that the team stopped tracking the moment they treated it as permanently settled.

This directly mirrors your threat-landscape eval-staleness scenario: there, ground truth was accurate when built but silently stopped representing current reality; here, a production decision was correct when validated but the team never checked whether the conditions that made it correct still hold. The fix is the same instinct in both cases — periodic re-evaluation, especially triggered by known, significant shifts (a new feature launch, a major traffic-composition change) rather than an indefinite, unexamined "it won once, so it's settled" assumption.

**Why the others are wrong**

- **A)** treats statistical validity as if it implies permanence — but a well-run test proves the result was real *for that test*, not that it remains optimal forever regardless of how the underlying population and product change. This is the same complacency as trusting an old eval score without questioning whether the world it was measured against still exists.
- **C)** misunderstands what duration fixes — a test needs to run long enough to reach statistical significance for the conditions *during* the test; it can't be run "long enough" to pre-emptively capture changes that hadn't happened yet at the time. No test duration, however extended, protects against drift that occurs *after* the test concludes — that requires re-testing, not a longer original test.
- **D)** draws an artificial boundary that doesn't hold — traffic composition and feature changes are exactly the kind of shift that determines whether a prompt optimized for one user population still performs best for a different one; dismissing this as "just marketing" ignores that the whole reason A/B testing exists is to validate performance for your *actual current* users, which is a moving target, not a fixed one.

Now, Scenario 2 — testing when a full live A/B test isn't the appropriate tool at all:

---

**Scenario 2 — When A/B testing isn't the right tool**

A hospital network wants to test a proposed change to their Claude-powered clinical documentation assistant: a new guardrail configuration that changes how the system handles ambiguous medication-dosage language in doctors' dictated notes. An engineer proposes running a standard 50/50 live A/B test across all real patient documentation traffic for several weeks to gather enough data for statistical significance, the same way they'd test any other prompt change.

A clinical safety officer objects, pointing out two concerns: (1) if the new guardrail configuration has an undetected flaw, running it on 50% of live patient documentation for weeks means a flaw could affect a large volume of real medical records before the test concludes and anyone notices a problem; and (2) this system also has relatively low daily volume compared to the company's consumer-facing products, meaning reaching statistical significance the normal way could take an impractically long time regardless.

**Question**: Is a standard 50/50 live A/B test the right methodology here? What should the team do instead?