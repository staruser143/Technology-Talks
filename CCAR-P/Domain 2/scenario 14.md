**Scenario**

A company's overnight batch job processes 500 support tickets sequentially, one Claude API call per ticket, all sharing the identical 6,000-token system prompt (support policy, tone guidelines, output format) with a `cache_control` marker correctly placed right after that static content, before each ticket's varying content. The job is well-optimized in terms of marker placement. However, due to how the batch job is architected, there's a rate-limiting delay built in: each ticket takes about 8 minutes to fully process (including a slow downstream logging step) before the next API call for the next ticket fires. The team is confused why, despite correct marker placement, they're seeing full-price processing on nearly every single call instead of the expected cache savings after the first request.

**Question**: Given correct marker placement, why is this batch job still not benefiting from caching on most calls, and what should the team do?

A) Marker placement must actually be wrong somewhere; the team should re-examine the payload structure rather than looking at timing, since timing has no bearing on cache behavior.

B) The default cache TTL (time-to-live) is 5 minutes, refreshed on each cache hit — but this batch job's calls are spaced roughly 8 minutes apart due to the processing delay between tickets, which exceeds the default TTL, so the cache expires between calls before the next request arrives. The fix is to either restructure the job to fire requests closer together (under 5 minutes apart), or use the extended 1-hour TTL option (at a higher cache-write cost) to accommodate the longer gap between calls.

C) Batch jobs are fundamentally incompatible with prompt caching; the team should abandon caching for this use case entirely.

D) The issue is unrelated to caching mechanics; the team should reduce the system prompt to under 6,000 tokens to force faster cache writes that persist longer regardless of the gap between calls.

Take your best guess and I'll walk through it.



Correct — and this scenario introduces a variable that hasn't come up yet in this set: **time**, not just structure. Marker placement was a red herring here on purpose — it was correct from the start, testing whether you'd default to "must be a structural problem" out of habit rather than checking the actual cause.

**Why B is right**

Recall the specific detail from the material: the cache's default minimum lifetime (TTL) is 5 minutes, and — critically — this lifetime **refreshes each time the cached content is used**, but it still expires if nothing hits it in time. That's exactly what's happening here: each ticket takes ~8 minutes end-to-end before the next call fires. Since 8 minutes exceeds the 5-minute default TTL, the cache from ticket N has already expired by the time ticket N+1's request goes out — so ticket N+1 pays a full cache-write cost again (processing the whole 6,000-token prefix fresh), instead of getting a cheap cache-read hit. This repeats on essentially every call, which is exactly the "full-price processing on nearly every single call" symptom described — the marker is doing its job correctly; there's just never a live cache left to hit by the time the next request needs it.

The fix follows directly from the mechanism, and B correctly offers both real options: either **close the timing gap** (restructure the batch job so consecutive calls fire closer together than 5 minutes apart — e.g., decoupling the slow logging step from the next ticket's dispatch, so it doesn't block the next request), or **extend the TTL** to the 1-hour option, which comfortably covers an 8-minute gap between calls, at the cost of a higher cache-write price. Either approach directly targets the actual cause (elapsed time between calls exceeding the cache's lifetime), rather than touching anything about prompt structure, which was never the problem here.

**Why the others are wrong**

- **A)** assumes the problem must be structural because that's the pattern from your last three scenarios — but the scenario explicitly states marker placement is correct, and the actual detail worth noticing (8 minutes between calls, well past a 5-minute default TTL) is a timing fact, not a structural one. This is a useful trap precisely because it tests whether you're diagnosing from evidence or pattern-matching to the most recent lesson category.
- **C)** overcorrects into abandoning caching for an entire class of workload, when the real issue is a specific, fixable configuration mismatch (default TTL too short for this job's actual cadence) — not an inherent incompatibility between batch processing and caching. Plenty of batch jobs run consecutive calls fast enough to benefit from the default TTL; this one specifically has an unusually long per-ticket delay baked into its architecture.
- **D)** reaches for a token-count explanation with no basis in the mechanism — TTL duration isn't a function of prefix size, and "faster cache writes" isn't how TTL works at all; a shorter prompt would still expire after the same 5-minute window regardless of how quickly it was originally written to cache. This repeats the same "guess at prompt size as the culprit" mistake from your knowledge-base scenario's wrong answer A, just relocated to a timing problem where size is even less relevant.

**Pattern to remember, adding the third dimension to caching diagnostics**: you've now covered all three ways a cache can fail to deliver expected savings — (1) a variable value sitting *before* the marker, breaking the match entirely (the timestamp bug), (2) stable content sitting *after* the marker, leaving it uncached even though it could be included (the knowledge-base gap), and (3) correct structure and marker placement, but **too much elapsed time between calls relative to the TTL**, letting the cache expire before the next request arrives (this scenario). All three produce the same surface symptom — "we expected caching to help, but costs aren't dropping" — but the fix is completely different depending on which of the three is actually the cause, which is exactly why checking marker placement *and* request timing *and* content stability, in that order, is the right diagnostic sequence rather than assuming it's always a structural issue.

That's thorough, well-tested coverage of prompt caching mechanics. Want to try more caching scenarios, or move to a different Domain 2 sub-topic — model selection you've covered well, guardrails covered well; that leaves prompt engineering techniques (zero-shot/few-shot/chain-of-thought) or context window optimization as the remaining fresh ground?

