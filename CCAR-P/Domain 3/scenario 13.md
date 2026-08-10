**Scenario**

A retail company runs a Claude-powered order support agent that uses several integrated tools: an order-lookup API, a shipping-tracker API, and a refund-processing API. The engineering team's monitoring dashboard shows: 99.8% uptime, average response time of 1.2 seconds, and zero unhandled exceptions in the logs over the past month. Leadership considers the system fully healthy based on this dashboard.

A support manager, reviewing a sample of actual conversations manually (not because of any alert — she was just curious), discovers a recurring pattern: when the shipping-tracker API is queried for an order number in a specific legacy format (orders placed before a 2023 system migration), it returns a technically valid, well-formed response — HTTP 200, valid JSON — but with an empty tracking-events array, because the legacy order numbers aren't recognized by the newer tracking system. Claude, receiving this empty-but-valid response, tells customers "there's no tracking information available yet for this order," even for orders that shipped and were delivered weeks ago. This has been happening for an estimated 8% of all shipping-status queries since before the monitoring dashboard was even set up, and nothing in the current monitoring would ever flag it.

**Question**: What's the core gap in this observability setup, and what should be done?

A) The dashboard is sufficient — uptime, latency, and exception logs are the standard metrics for system health, and this issue is really a data problem (legacy order number incompatibility) that's outside the scope of what observability should catch.

B) This is a classic silent-failure pattern: the tool call succeeds at the HTTP/technical level (200, valid JSON) while failing semantically (wrong/empty result due to an upstream data compatibility issue), and nothing in uptime/latency/exception monitoring can distinguish this from a legitimate "no tracking info yet" response. The fix is to instrument for semantic tool success, not just technical success — e.g., track and alert on unusually high rates of empty results from the shipping-tracker tool, break down tool performance by order-number format/age to catch format-specific failures, and periodically sample real conversations for quality rather than relying solely on automated infrastructure metrics.

C) The core gap is that the support manager should not have needed to manually review conversations; the fix is to eliminate all legacy order numbers from the system so this category of query can never occur again.

D) The core gap is that Claude should have caught the empty response and refused to answer, since a well-designed system should never let a technically valid but semantically empty response reach the customer.

Take your best guess and I'll walk through it.




Correct — and this scenario is the cleanest possible illustration of the exact distinction Observability at Scale is built around: technical success versus semantic success.

**Why B is right**

Every number on that dashboard is real and genuinely good — 99.8% uptime, 1.2-second latency, zero unhandled exceptions. None of it is lying. And none of it is capable of catching this bug, because the failure doesn't produce an error anywhere in the stack: the API call succeeds (200), the JSON is well-formed, Claude receives a valid response and behaves exactly as instructed (report what the tracking data says). Every individual component did its job correctly by the standards the dashboard measures. The failure only exists at the *semantic* layer — an empty tracking-events array *means* "no data available" to the code, but for these legacy orders it actually means "the lookup itself is broken," and nothing in the current monitoring can tell those two situations apart.

This is precisely the "tool 'succeeding' at the HTTP level is different from succeeding semantically" distinction from the material, and the scenario sharpens it further with a detail worth noticing: the manager found this **by accident**, through manual review she wasn't prompted to do by any alert — meaning this bug had been running at an estimated 8% failure rate for an unknown period before anyone noticed. That's the real cost of monitoring only technical-success signals: a meaningful, systematic failure can run in production indefinitely, invisible to every metric leadership was actually watching.

The fix in B addresses this at the right layer, with three concrete pieces:
- **Track semantic outcomes, not just technical ones** — an unusually high rate of empty results from one specific tool is a real, alertable signal, distinct from "the tool responded" or "the tool errored."
- **Break down performance by relevant dimensions** (here, order-number format/age) — this is exactly the "per-stage/per-component attribution" principle from the material; an aggregate "shipping tool works fine" metric hides a format-specific failure that only shows up when you segment the data the right way.
- **Periodic sampling of real conversations** — this is the same "random sampling of unflagged/auto-resolved output" discipline you've already applied twice (the support-agent thumbs-down scenario and the loan-recommendation scenario), now generalized to any integrated system: automated metrics alone are never sufficient, some form of direct quality sampling has to run continuously, not just when someone happens to get curious.

**Why the others are wrong**

- **A)** repeats the exact mistake the scenario is built to expose — treating uptime/latency/exceptions as the *complete* definition of system health, when the whole point is that those metrics are structurally blind to this category of failure. Calling it "a data problem outside the scope of observability" is backwards: legacy data incompatibility causing systematic wrong answers to customers is exactly the kind of thing observability should exist to catch — the data problem is real, but failing to detect it for an unknown period is the observability failure.
- **C)** proposes eliminating the legacy order numbers as if that's a real, immediate fix — but this ignores that those orders and their customers already exist and need support now; you can't retroactively migrate historical orders out of existence to avoid building proper monitoring. It also sidesteps the actual lesson (build the observability that would catch this class of bug) in favor of a narrow, brittle workaround that wouldn't generalize to the next tool/data-format mismatch that comes along.
- **D)** places the burden on Claude to independently determine that a technically valid response is "suspicious" — but Claude has no way to know that an empty array from this specific tool, for this specific order-number format, is anomalous rather than a legitimate "no data yet" case; that distinction lives in *system-level pattern knowledge* (this tool empty-responds unusually often for legacy formats), which is exactly what observability instrumentation is for, not something to expect the model to infer unprompted from a single response in isolation.

**Pattern to remember, tying this back to the broader theme**: the two most dangerous properties of a silent-failure bug are that it (1) produces no error anywhere standard monitoring would show, and (2) is often systematic (tied to a real underlying cause — here, order-number format) rather than random, meaning it affects a predictable, ongoing slice of traffic rather than being a one-off fluke. Good observability design specifically hunts for the gap between "technically completed" and "actually correct," using segmented metrics and periodic direct sampling — not because standard infrastructure metrics are wrong, but because they were never designed to answer that question in the first place.

Want another observability scenario — maybe one on distributed tracing across integration boundaries, or alerting vs. dashboards specifically?