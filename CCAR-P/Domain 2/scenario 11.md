**Scenario**

A legal-tech company's contract-review assistant sends the following to Claude on every request: a system prompt containing role instructions and a 6,000-token internal legal-review playbook (identical on every call), followed by the current date and time stamped at the top of the prompt ("Current review timestamp: 2026-08-07 14:32:03"), followed by the specific contract clause being reviewed (varies every request), followed by the same 6,000-token playbook content is also referenced again at the bottom as a "reminder" before the final instruction. The team enabled prompt caching but is confused why their cache hit rate is extremely low and costs haven't dropped as expected, even though the playbook content itself never changes.

**Question**: What's most likely causing the low cache hit rate, and what should the team fix?

A) The playbook is simply too large to be cached effectively; the team should shorten it below 1,000 tokens regardless of what content that removes.

B) Two issues: (1) the timestamp is placed before the varying clause content but within what should be the stable prefix, and since caching requires the cached portion to be byte-identical across calls, a per-request timestamp anywhere in that prefix breaks the cache match every single time; and (2) repeating the playbook a second time at the bottom, after the varying clause, adds cost and doesn't benefit from caching anyway, since content after the varying section can never be part of a stable, reusable prefix. Fix: remove the timestamp from the cached region entirely (move it after the variable content, or drop it if unnecessary), and remove the duplicated playbook reference at the bottom, relying on the model to still have the playbook from earlier in context rather than re-stating it.

C) Prompt caching doesn't work for legal/contract content at all due to the sensitivity of the data; the team should disable caching and accept the higher cost as a compliance requirement.

D) The issue is that the model tier is wrong; switching to a different Claude model will resolve the caching problem regardless of prompt structure.

Take your best guess and I'll walk through it, or ask me to explain any option first.




Correct — and this scenario packages two distinct, common caching mistakes into one design, both testing the same underlying rule from Sample Question 2: caching requires an identical, stable prefix, and *anything* that breaks byte-for-byte identity in that prefix — even something as small as a timestamp — defeats it entirely.

**Why B is right**

Walk through the prompt structure as laid out, in order:

1. **System prompt + 6,000-token playbook** (stable — good candidate for the cached prefix)
2. **Timestamp** (`"Current review timestamp: 2026-08-07 14:32:03"`) — changes on every single call, down to the second
3. **The varying clause** (correctly dynamic, correctly placed after the stable content)
4. **The playbook repeated again** at the bottom, after the varying content

The timestamp is the more damaging error. Recall from the material: "exact matching: cache hits require 100% identical prompt segments... up to and including the block marked with cache control." A timestamp that changes every request, sitting *before* the varying clause but still within what the team intended as the stable region, means the "stable" prefix is never actually identical twice — it changes on literally every call, down to the second. This silently defeats the entire cache, even though the 6,000-token playbook itself never changes: caching doesn't check "is most of this the same," it requires exact match up to the breakpoint. One small varying element inside an otherwise-huge stable block is enough to invalidate the whole prefix, every time.

The duplicated playbook at the bottom is a separate, distinct problem: content placed *after* the varying clause can never benefit from caching in the first place, regardless of how stable its own content is, because everything before it in the sequence (including the varying clause) has already broken prefix continuity by that point. So this second copy is paying full, non-cached input-token cost on every single call, on top of contributing nothing to context that the model doesn't already have from the first copy earlier in the prompt.

The fix directly targets both: move or remove the timestamp so it doesn't sit inside the stable prefix (if a timestamp is genuinely needed, it belongs after the variable content, never before or within the reusable block), and drop the redundant second copy of the playbook, trusting the model to retain and apply instructions it already received earlier in the same context — removing both the wasted tokens and the caching-defeating variability in one pass.

**Why the others are wrong**

- **A)** misdiagnoses size as the problem when the evidence points to *structure*. A 6,000-token playbook is well above the minimum cacheable threshold (comfortably above even the highest tier's minimum) — size was never the obstacle here; a non-identical prefix was. Arbitrarily shrinking the playbook below 1,000 tokens would sacrifice real content (a legal review playbook is presumably substantive for good reason) without addressing the actual cause, and the cache would likely still fail to hit if the timestamp problem isn't fixed.
- **C)** invents a compliance objection that isn't supported by anything in the scenario or the material — prompt caching is a mechanism for reusing prompt *content* across requests; it doesn't inherently create some special sensitivity risk for legal data that would justify disabling it outright, and the material doesn't describe any such restriction. This also throws away a real, fixable cost optimization to avoid a structural problem that has a much cheaper, targeted fix.
- **D)** repeats the now-familiar "misdiagnose a structural/prompting problem as something else" trap from your model-selection scenarios (pricing grounding, arithmetic chain-of-thought) — model tier has no bearing on cache-hit mechanics, which are governed entirely by prompt structure and byte-identical prefix matching, not by which Claude model is receiving the request.

**Pattern to remember**: caching failures are almost always structural, not capacity-related — the two most common real-world causes are (1) something small and request-specific (a timestamp, a request ID, a "current user" field) accidentally sitting inside what's meant to be the stable prefix, silently breaking exact-match on every call, and (2) stable content duplicated or referenced again *after* the variable section, which can never be cached regardless of its own stability, since caching only reuses a contiguous prefix up to the first point of variation. Both waste money in different ways — one destroys the cache entirely, the other just never benefits from it in the first place — and the exam likes testing whether you can spot each independently within one design.

Want another caching/template scenario, or should we move to a different Domain 2 sub-topic?
