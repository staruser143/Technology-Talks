**Scenario**

A company runs a Claude-powered research assistant with a large, static toolset — 40 internal tools, all loaded upfront in every request (no Tool Search/deferred loading configured), with a `cache_control` marker placed after the full tool list, before the system prompt content. The team notices that whenever they add or modify even one tool definition in this static list (say, updating a parameter description on one tool), cache hit rates drop to near-zero for a while afterward, and costs spike, even though 39 of the 40 tools didn't change at all.

Separately, a different team at the same company runs a similar research assistant, but built with Tool Search enabled and most tools marked `defer_loading: true`. When their agent discovers and loads a new tool mid-conversation (a tool it hadn't used yet in that session), they notice no corresponding cache disruption — their cached system prompt and core tools keep hitting normally, turn after turn, regardless of which additional tools get discovered along the way.

**Question**: Why do these two teams see such different caching behavior when tools change or get added, and what does this imply about designing for cache stability with large or evolving toolsets?

A) Both teams are seeing the same underlying behavior; the second team's report must be inaccurate, since any change to available tools always invalidates the cache regardless of how the tools are loaded.

B) The two teams differ because of *where* the tools live relative to the cached prefix. Team 1's static tool list is part of the prefix itself (before the `cache_control` marker) — per the documented cache hierarchy (tools → system → messages), any change at the tools level invalidates that level and everything after it, so modifying even one of 40 tools invalidates the whole cached prefix. Team 2's deferred-loading tools are deliberately excluded from the prefix — discovered tools get appended inline in the conversation as a `tool_reference`, expanded outside the cached region — so the prefix (their stable system prompt and core tools) is untouched regardless of what gets discovered. The implication: for large or frequently-evolving toolsets, using Tool Search with deferred loading avoids the cache-fragility that a static, fully-upfront tool list has as an inherent structural property.

C) Team 1's problem is unrelated to tool changes; their cache issue must be a timestamp-style bug like the one discussed earlier, and Team 2 simply hasn't noticed their own cache is also breaking.

D) Team 2's approach is strictly worse despite appearing not to break the cache, since deferred-loading tools always cost more overall than a static list, regardless of how often the static list changes.

Take your best guess and I'll walk through it.



Correct — and this scenario ties together nearly everything from this whole caching/tool-loading thread into one clean structural comparison.

**Why B is right**

The two teams' outcomes aren't a mystery or an inconsistency — they're the direct, predictable consequence of *where each tool physically sits relative to the cache boundary*, exactly the "position relative to the marker, not category or role" principle you locked in several scenarios ago with the timestamp and knowledge-base examples.

- **Team 1**: their 40 tools are part of the static prefix, sitting *before* the `cache_control` marker. The documented cache hierarchy is explicit: `tools → system → messages`, and a change at any level invalidates that level *and everything after it*. So modifying one parameter description on one tool doesn't just cost that one tool's re-processing — it invalidates the *entire* tools-level cache, and since system and messages sit downstream of tools in that hierarchy, the whole prefix effectively has to be rewritten. This is a structural fragility baked into loading everything upfront: a 40-tool static list is only as cache-stable as its least-frequently-changing member, because one change anywhere in that block poisons the whole thing.

- **Team 2**: deferred tools were never part of the prefix to begin with — they live outside it by design, appended inline as a `tool_reference` and expanded at discovery time. Since the prefix (their stable system prompt and core, non-deferred tools) never contains the deferred tools, discovering a new one mid-conversation has nothing to invalidate — there's no shared region between "what changed" and "what's cached." This is the exact mechanism from the last message, now shown producing a real, measurable operational difference between two otherwise-similar systems.

**The broader implication, which is the real point of the scenario**: for a toolset that's large *and* evolving (new tools added, existing ones tweaked over time — which is realistic for any actively-maintained internal tool library), a fully static, all-upfront design has an inherent structural cache fragility that scales badly — the more tools you have and the more often any one of them changes, the more often your entire cache gets invalidated, even though most of the toolset was untouched. Tool Search with deferred loading isn't just a context-size optimization (which is how it was introduced) — it also happens to solve this cache-stability problem as a side benefit, because keeping tools out of the prefix until discovered means routine tool additions/updates stop being cache-invalidating events for anything actually in active use.

**Why the others are wrong**

- **A)** asserts both teams must be experiencing the same thing without engaging with the actual mechanism — but the documentation is explicit that deferred tools are excluded from the prefix specifically to prevent this outcome; dismissing Team 2's report as inaccurate ignores a deliberately engineered behavior rather than a coincidence or measurement error.
- **C)** reaches for an unrelated diagnosis (the timestamp bug pattern) that doesn't fit the evidence at all — Team 1's problem is explicitly tied to tool list modifications, not a stray variable value inside the prefix, and there's no basis given for assuming Team 2's cache is secretly broken when the mechanism described (tools excluded from the prefix) directly explains why it wouldn't be.
- **D)** introduces an unsupported cost claim with nothing in the scenario or the material backing it — deferred loading's trade-off (from your earlier deep dive) is a modest one-time discovery round-trip on first use, not some blanket "always costs more" penalty; and the scenario is specifically about cache *stability* under change, where Team 2's approach is demonstrably more robust, not "worse despite appearing better."

**Pattern to remember, closing out this deep technical thread**: cache fragility under change is fundamentally about *what's inside the prefix*, not about tool count or feature sophistication in the abstract. A large, static, all-upfront tool list is cache-fragile specifically because every tool in it is inside the invalidation blast radius of any single change to any one of them. Deferring tools out of the prefix — originally motivated by context-size and selection-accuracy concerns — has the additional structural benefit of insulating your cache from routine toolset evolution, which matters a great deal for any system where tools get added or updated over time rather than staying permanently fixed.

That's a genuinely deep, well-integrated pass through caching, context management, and tool-loading mechanics — well beyond the original blueprint's surface description, but all of it legitimately load-bearing for a working architect. Want to keep going on this thread, or move to Domain 4 (Evaluation, Testing & Optimization, 16%) now?

