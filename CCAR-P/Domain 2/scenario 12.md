**Scenario**

A company builds a customer support assistant. Every request sends: a system block containing (1) a 5,000-token support-policy document, followed immediately by (2) a `cache_control` marker placed right after that policy document, followed by (3) a separate block containing the specific customer's name and account tier (e.g., "Customer: Jane Doe, Tier: Gold") with no `cache_control` marker on it, and finally the user's message content with the customer's actual question, also with no marker.

Payload structure, explicitly:

```
system: [
  { type: "text", text: "<5,000-token support policy>", cache_control: {"type": "ephemeral"} },
  { type: "text", text: "Customer: Jane Doe, Tier: Gold" }
]
messages: [
  { role: "user", content: "<customer's question>" }
]
```

The customer's name/tier block changes on every request (different customer each time); the question also changes every request; the policy document never changes.

**Question**: Given this exact payload structure, will this design achieve a good cache hit rate on the policy document across different customers' requests? Why or why not?

A) No — since the customer name/tier block is a different value on every request, and it appears in the same "system" role as the cached policy document, it will still be included in the cached region and break the match every time, identical to the timestamp bug.

B) Yes — the `cache_control` marker is placed immediately after the policy document and before the customer name/tier block, so only the policy document (up to and including the marker) is the cached prefix; the customer name/tier block and the user's question both fall after the marker, so they're processed as fresh content on every call without invalidating the cache on the policy document.

C) No — prompt caching only works if the entire system role, including all blocks within it, contains zero variation; since the system role here has any variable content at all, regardless of marker placement, caching cannot occur.

D) Yes, but only because the customer name/tier block is short; if it were longer than the policy document, it would break the cache regardless of marker placement.

Take your best guess and I'll walk through it.


Not quite — let's walk through why B is actually correct and where C's reasoning breaks down, since this is the exact distinction we just spent time sharpening.

**Why B is right**

The whole point of a `cache_control` marker is that it defines the boundary precisely — it doesn't matter what role something is nested under ("system" here); what matters is whether it's **before or after the marker itself**. In this payload:

- The 5,000-token policy document is followed *immediately* by the `cache_control` marker. That marker closes off the cached region right there — the cached prefix is exactly "the policy document, and nothing else."
- The customer name/tier block comes *after* that marker, still technically inside the same "system" array, but that doesn't matter — it's outside the cached region because it's positioned after the breakpoint.
- The user's question, also after the marker (in the separate `messages` array), is likewise outside the cached region — same as always.

So on every request: the policy document (before/at the marker) matches what was cached previously → cache hit, cheap read. The customer name/tier and the question (after the marker) → processed fresh every time, at normal cost — exactly as intended, since those genuinely do change per-request. This is structurally identical to "Version B" from a few messages back, just with the customer info playing the role the timestamp played there. The fix we discussed for the timestamp bug — move the variable content to *after* the marker — is exactly what's already done correctly in this scenario.

**Why C is wrong, and why it's an understandable mistake to make right after the timestamp discussion**

C generalizes the *lesson* ("a variable value breaks caching") into the wrong *scope* ("any variation anywhere in the system role breaks caching, regardless of marker position"). But that's precisely the distinction the whole timestamp discussion was building toward: it's never about which *role* (system vs. user) something sits in, or whether the system role as a whole is "pure" — it's specifically about position **relative to the marker**. A system role can absolutely contain both cached and non-cached content side by side, as long as the marker is placed correctly between them, which is exactly what this payload does. If C's logic were true, `cache_control` markers within the system array would be pointless — you'd need the entire system role to be static, which isn't how the mechanism works, and isn't consistent with the material's own point that you can define up to 4 breakpoints specifically to cache different sections independently within one request.

**Why A and D would also be wrong here, briefly:**
- **A)** repeats C's core error, just framed as "same role = same cached region" — but role isn't the boundary, the marker is. This design deliberately avoids the timestamp bug precisely by placing the marker before the variable block, not after it.
- **D)** invents a length-based exception with no basis in the mechanism — cache validity is about byte-identical matching of the marked region, not about the relative size of what comes after it. A long or short block after the marker has zero effect on whether the marked prefix hits, since it's not part of that prefix either way.

**The corrected mental model, stated as cleanly as possible**: `cache_control` doesn't care about roles, blocks, or "system vs. user" as categories — it only cares about **sequence position relative to the marker**. Everything up to and including the marker must be byte-identical across calls to hit. Everything after it can vary freely without affecting that hit, no matter where it technically lives in the payload structure.

Want to try another one to confirm this is locked in, or move to a different Domain 2 sub-topic?