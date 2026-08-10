How do I configure deferred loading of tools and for tools which get loaded on demand, is there an option to add them to cache


Good — I now have both parts of your question confirmed directly from the docs.

**How to configure deferred loading**

You include every tool in the same `tools` array as normal, and add `defer_loading: true` on the ones you don't want loaded upfront:

```python
tools=[
  {"name": "tool_search_tool_regex_20251119"},  # or bm25 variant — must NOT be deferred
  {"name": "core_lookup_tool", "input_schema": {...}, "cache_control": {"type": "ephemeral"}},  # always-loaded, cached
  {"name": "rarely_used_tool_A", "input_schema": {...}, "defer_loading": True},
  {"name": "rarely_used_tool_B", "input_schema": {...}, "defer_loading": True},
  ...
]
```

A few real constraints worth knowing:
- **The tool search tool itself must never be deferred** — it has to be available upfront so Claude has something to search *with*.
- **Keep your 3-5 most frequently-used tools non-deferred** — this avoids paying the search round-trip for tools you know you'll need almost every time; deferral is for the long tail.
- **`defer_loading` and `cache_control` are mutually exclusive on the same tool** — this confirms and explains the 400 error we discussed earlier: a deferred tool is by definition excluded from the prefix, so marking it for caching would be contradictory.
- Worth flagging honestly since I found it directly contradicted elsewhere: one GitHub issue thread notes that in Claude Code/Agent SDK specifically, deferred loading currently only applies to **MCP-sourced tools**, not built-in tools — so if you're configuring this through the Agent SDK rather than the raw Messages API, that's a real practical constraint to check against, separate from the raw API's more general behavior.

**Your second question — can a dynamically-discovered tool subsequently be added to cache?**

The direct answer is: **not really, and it isn't meant to be, because that would defeat the purpose.** The documentation is explicit that deferred tools are "stripped from the rendered tools section *before the cache key is computed*" — they structurally can't participate in the prefix, discovered or not. When Claude discovers one, it gets expanded inline as a `tool_reference` block in the *conversation body*, and — this is the key detail — "the cache remains valid across the turn where the tool is discovered **and the turn where it's called**." So the tool doesn't get promoted into the cached prefix after discovery; instead, the discovery itself doesn't disturb the cache in the first place, and the tool's presence in the conversation from that point forward just rides along as part of the normal (uncached) conversation content, the same as any other message.

**Why this design makes sense rather than being a limitation**: if a discovered tool *could* later get folded into the cached prefix, you'd be right back to the fragility problem from your last scenario — every discovery event would eventually mutate the "stable" region, and you'd be one tool-discovery away from invalidating your carefully-built cache. Keeping discovered tools permanently in the conversation-body category (never promoted to the prefix) is precisely what makes the cache-preservation guarantee durable across an entire long conversation, not just the first turn.

**The recommended overall pattern, stated directly in the material**: start with a small, always-loaded, cached core toolset (your 3-5 most-used tools), let Tool Search discover and pull in anything additional as genuinely needed, and expect to keep getting cache hits on your core turn after turn — even across a long conversation where the model ends up discovering many additional tools along the way, since none of that discovery activity ever touches the cached region.