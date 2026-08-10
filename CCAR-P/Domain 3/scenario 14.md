**Scenario**

A travel booking company's Claude-powered trip-planning assistant orchestrates several integrated calls per user request: a flight-search API, a hotel-availability API, a currency-conversion service, and a subagent that checks visa requirements. Each of these components logs its own activity independently — the flight-search API logs to its own service's logging system, the hotel API to another, currency conversion to a third, and the visa subagent's reasoning trace goes to yet another location. None of these logs share a common identifier tying them back to the specific user request that triggered them.

A customer complains that they were quoted a hotel price in the wrong currency, resulting in a booking confirmation showing an amount roughly 10x too high. Support needs to determine: did the currency-conversion service return a wrong rate, did the hotel API return a price already in a different currency than expected and Claude mislabeled it, or did Claude apply a conversion twice by mistake? To investigate, an engineer has to manually go into four separate logging systems, estimate a rough time window based on the customer's complaint timestamp, and try to eyeball which entries in each system might correspond to this one request — a process that takes most of a day and still ends with some uncertainty about which component actually caused the error.

**Question**: What's the core observability gap here, and what should the team do?

A) The gap is that each individual service isn't logging enough detail; the fix is to increase the verbosity of each service's independent logs so there's more information to manually search through.

B) The core gap is missing distributed tracing across integration boundaries — each component logs in isolation with no shared identifier linking them to a single logical user request, making root-cause analysis for any single request require manual, time-consuming, and uncertain correlation across disconnected systems. The fix is to implement a trace ID (or correlation ID) generated at the start of each user request and propagated through every downstream call — flight search, hotel lookup, currency conversion, visa subagent — so that all logs from a single request can be pulled together instantly, in sequence, regardless of which service produced them.

C) The gap is that there are too many integrated services; the fix is to reduce the number of external integrations so there's less to log and correlate.

D) The gap is irrelevant to this specific bug — since the root cause is clearly a currency conversion error, the team should just directly audit the currency-conversion service's code, and better logging infrastructure wouldn't have changed the outcome.

Take your best guess, and I'll walk through the reasoning.




Correct — and this scenario demonstrates something the earlier silent-failure scenario didn't fully cover: even when you *do* have a detectable, customer-reported problem (unlike the invisible shipping bug), you can still be badly hampered by not being able to reconstruct *what actually happened* across a multi-component request.

**Why B is right**

Notice what kind of problem this is, distinct from your last scenario: the shipping-tracker bug was about *detecting* that something was wrong when nothing was flagging it. This scenario assumes detection already happened (the customer complained) — the gap here is entirely about **diagnosis speed and certainty once you know something's wrong.** Four independent logging systems, no shared identifier, means the engineer's investigation is reduced to timestamp-guessing and manual correlation — "a process that takes most of a day and still ends with some uncertainty" is the tell. That's not a logging *content* problem, it's a logging *structure* problem: the information needed to answer "which of these four components caused this" might well exist somewhere across those four systems, but there's no way to reliably assemble it into one coherent picture of this specific request.

The fix — a trace/correlation ID generated once at request start and propagated through every downstream call — directly solves this by construction: pull one ID, and every log entry from every component that touched this specific request comes back together, in order, regardless of which service originally wrote it. This turns "a day of uncertain manual correlation" into "look up one ID." This is the concrete mechanism behind the material's point that distributed tracing is what lets you actually trace one logical request across multiple system hops, rather than debugging by manually correlating disconnected logs — which "doesn't scale and often fails to actually find the root cause," exactly as demonstrated here.

**Why the others are wrong**

- **A)** misdiagnoses the axis of the problem. More verbose logs *within* each isolated system doesn't fix the correlation gap *between* systems — you'd just have more text to manually search through four still-disconnected places, with the same fundamental problem (no shared identifier) making it just as hard, or harder, to know which entries in each system actually belong to this one request.
- **C)** treats integration complexity itself as the enemy, but reducing the number of integrations isn't a real fix for an observability gap — flight search, hotel lookup, currency conversion, and visa checking are all genuinely necessary capabilities for this product; removing them to make logging easier would mean removing the product's core value to avoid solving a solvable infrastructure problem. This is the same overcorrection pattern flagged in earlier scenarios — discard real capability instead of fixing the actual gap.
- **D)** assumes the root cause before investigating, which is precisely what the scenario shows is *not* actually knowable yet — the complaint is consistent with at least three distinct possible causes (wrong rate returned, hotel API pricing mislabeled, double conversion applied), and the whole reason the investigation is hard is that nothing currently distinguishes between them. Jumping straight to auditing the currency service's code might turn out to be right, or might waste a day ruling out an innocent component while the real bug — say, Claude applying conversion twice — sits untouched. Good tracing is precisely what would have told the team where to look *first*, instead of guessing based on which component sounds most likely.

**Pattern to remember**: distributed tracing and the "instrument for semantic success, not just technical success" lesson from your last scenario are solving two different problems that both fall under observability. Semantic-success monitoring is about *detecting* that something's wrong when standard health metrics wouldn't show it. Distributed tracing is about *diagnosing* which component is actually responsible once you know something's wrong, when the request spans multiple independently-logging systems. A mature integrated system generally needs both — one to notice failures, the other to root-cause them efficiently once noticed.

Want another scenario — maybe one specifically on alerting vs. dashboards (the last piece of this sub-topic), or should we move to protocol selection (MCP vs. API/CLI vs. agent-to-agent) next?