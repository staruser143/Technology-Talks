**Scenario**

An enterprise research assistant sends this payload structure on every request:

```
system: [
  { type: "text", text: "<tool definitions, 2,000 tokens, never change>", cache_control: {"type": "ephemeral"} },
  { type: "text", text: "<internal knowledge base reference doc, 8,000 tokens, never changes across any request>" }
]
messages: [
  { role: "user", content: "<user's varying question>" }
]
```

The tool definitions and the 8,000-token knowledge base document are both completely static — neither has changed in months. The team enabled caching, and testing shows: the tool definitions are consistently served from cache (fast, cheap), but the 8,000-token knowledge base document is being processed at full price on every single request, contributing no cache savings at all, even though it never changes.

**Question**: Why isn't the knowledge base document benefiting from caching, given that it's just as stable as the tool definitions? What should the team do?

A) The knowledge base document is too large to be cached, regardless of marker placement; the team should split it into smaller sub-documents under 1,000 tokens each.

B) The `cache_control` marker is placed after the tool definitions but before the knowledge base document — meaning only the tool definitions fall within the cached prefix. The knowledge base document, despite being just as stable, sits after the marker and is therefore excluded from the cached region and reprocessed at full cost every time. The fix is to move the marker to after the knowledge base document instead (or add it there as a second breakpoint), so both stable blocks fall within the cached prefix.

C) This is expected and cannot be improved — only one block per system role can ever be cached, so some stable content will always be excluded regardless of marker placement.

D) The issue is that the knowledge base document should be moved to the user message instead of the system block, which would make it eligible for caching.

Take your best guess and I'll walk through it.




Correct — and this is the mirror image of the last two scenarios: instead of a *variable* value accidentally ending up inside the cached region, here a perfectly *stable* value has been accidentally left outside it. Same mechanism, opposite mistake.

**Why B is right**

Apply the same rule you've now used three times: caching only covers what's before or at the marker, regardless of how stable the content after it happens to be. In this payload:

```
system: [
  { tool definitions, cache_control: marker here },   ← cached prefix ends here
  { 8,000-token knowledge base doc }                   ← after the marker, NOT cached
]
```

The marker sits right after the tool definitions. That closes the cached region at that point — it doesn't matter that the knowledge base document is equally unchanging; **the mechanism doesn't know or care how stable something is, it only knows what's inside the marked boundary versus outside it.** Since the knowledge base document comes after the marker, it's treated exactly like genuinely variable content (the user's question) — reprocessed fresh, at full price, every single call — even though nothing about it actually requires that.

This is a subtly different mistake from the timestamp bug: there, a *variable* value was placed *before* the marker, breaking the match entirely. Here, a *stable* value was placed *after* the marker, so the cache still hits reliably (that's why the tool definitions are working fine) — it's just hitting on a smaller region than it could be, leaving real savings on the table. The fix follows directly: extend the cached prefix to include the knowledge base document too, either by moving the single marker to after both stable blocks, or by adding a second breakpoint after the knowledge base document specifically (recall from the material: up to 4 breakpoints are allowed, useful exactly for this — caching multiple stable sections, especially if they have different reasons to eventually change independently of each other).

**Why the others are wrong**

- **A)** reaches for a size-based explanation that doesn't fit the evidence. 8,000 tokens is well within normal cacheable range (comfortably above minimum thresholds across every model tier), and the tool definitions block right next to it — smaller, but still working — proves size isn't the obstacle. Splitting into sub-1,000-token pieces wouldn't fix anything and would just add unnecessary complexity, since the actual issue is marker position, not document size.
- **C)** invents a limitation ("only one block per system role can ever be cached") that directly contradicts the material — up to 4 explicit breakpoints are supported per request specifically so multiple sections can be cached, whether independently or as one extended contiguous prefix. This scenario's fix doesn't even need a second breakpoint necessarily; simply moving the existing single marker later would cover both blocks in one cached prefix, since the system already forms prefixes hierarchically and finds the longest matching sequence up to wherever the marker sits.
- **D)** proposes moving stable content into the *user message* — the wrong direction entirely. The user message is exactly where genuinely variable, per-request content belongs; moving a permanently static reference document there doesn't make it more cacheable, it actually makes it harder to reason about as stable content, and does nothing to address the actual fix, which is simply repositioning the marker within the system block where the document already correctly lives.

**Pattern to remember, completing this pair of caching mistakes**: caching bugs run in two directions — pulling variable content *before* the marker (breaks the cache entirely, as in the timestamp case) and leaving stable content *after* the marker (cache still works, but on a smaller region than it should, quietly leaving savings on the table, as in this case). Both are marker-placement errors, not caching-eligibility problems with the content itself — the fix in both cases is the same diagnostic move: check exactly what sits before versus after the breakpoint, not what the content technically "is" or where it's nested.

Want to try one more caching scenario, or move to a different Domain 2 sub-topic now?