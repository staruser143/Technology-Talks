Good one to consolidate — we touched this across a few messages earlier (defer_loading, caching interaction), but let's pull it together into one clear picture of what it actually is and when to use it, the same way we just did for temperature.

**What it is**

The tool search tool is itself a tool — a special one you add to your `tools` array — that lets Claude search for and discover other tools **on demand** instead of having every tool's full schema loaded into context from the start. Two variants: `tool_search_tool_regex_20251119` (pattern-based matching) and a BM25 variant (keyword-relevance based).

**How it's used, mechanically**

- You mark the tools you *don't* want loaded upfront with `defer_loading: true` in their definitions.
- The tool search tool itself must **never** be deferred — it needs to be available immediately so Claude has something to search with in the first place.
- Keep your **3-5 most frequently-used tools non-deferred** — pay the small context cost of always having them loaded, since deferring them would mean paying a search round-trip on nearly every request.
- When Claude determines a deferred tool is relevant to the current task, it searches, gets a lightweight reference back, and the full tool definition expands inline in the conversation at that point — not back in the system prompt/tools prefix.

**Why you'd use it — the two problems it solves**

1. **Context/token cost**: loading dozens of tool schemas upfront costs real tokens on every single request, even for tasks that only ever touch one or two of them. Deferred loading means you only pay for what's actually used.
2. **Tool-selection accuracy**: this is the concrete, measured reason it exists — Anthropic's own testing found tool-selection accuracy dropped meaningfully once a model had to choose among too many available tools at once (recall the specific figures: Opus 4 went from 49% to 74%, Opus 4.5 from 79.5% to 88.1%, purely from showing fewer, more relevant tools). Fewer options in context at decision time means more reliable tool choice.

**When it's actually worth using — the threshold**

Not universal — with **fewer than roughly 10 tools total**, loading everything upfront is typically *faster* than paying the first-time discovery round-trip Tool Search introduces. It earns its cost once you're dealing with enough tools (dozens, especially across multiple connected MCP servers) that the context savings and selection-accuracy improvement outweigh that overhead.

**The caching interaction worth remembering**: deferred tools are excluded from the cacheable prefix entirely — they're stripped out before the cache key is even computed, and discovery happens inline in the conversation instead. This means using Tool Search doesn't break your existing prompt caching; your stable, non-deferred tools and system prompt keep hitting cache normally regardless of what gets discovered along the way.

**Where this connects across the domains you've covered**: this is Domain 3's progressive-discovery-vs-monolithic-context principle, Domain 2's context/token optimization discipline, and Domain 3's capability-bloat sub-topic (an agent carrying more tools than any given task needs) — Tool Search is the concrete mechanism that lets you have a large, capable tool library available to an agent without paying its full context cost on every single request.

Want a scenario testing when Tool Search is worth enabling (per that ~10-tool threshold) versus when a simpler, fully-loaded approach is actually the better call?