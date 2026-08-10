When using tool search to load a tool does caching happen at that Time


Genuinely good question, and the answer is more elegant than you might expect — this is a real, deliberately-designed interaction, not an afterthought.

**The short answer: no, discovering a tool via Tool Search does *not* invalidate your existing cache.** This was specifically engineered to avoid the exact problem you might reasonably expect — that dynamically loading a new tool schema mid-conversation would break your cached prefix, forcing an expensive cache-write every time a new tool gets discovered.

**How it actually works, mechanically:**
- Tools marked for deferred loading (`defer_loading: true`) are **excluded from the system-prompt prefix entirely** — they're not sitting in the cached region waiting to be "filled in" later.
- When Claude discovers one of these tools via a search, the API appends a lightweight `tool_reference` block **inline in the conversation** (not back in the system prompt/tools prefix), and expands it into the full definition only at that point.
- Because the discovered tool lands in the *conversation*, not the *cached prefix*, **the prefix itself is untouched** — your existing cached tools/system-prompt block stays exactly as it was, still valid, still hitting.

So the sequencing you'd actually see: your stable, cached tools + system prompt keep hitting cache normally on every turn; the newly-discovered tool's definition just gets added as fresh, uncached content in that turn's conversation — the same way any new user message or tool result would be uncached. It's not that the new tool gets "cached at the time it's loaded" — it's that it deliberately sits *outside* the cached prefix so it can't disturb what's already cached.

**One related mechanical rule worth flagging, since it's the inverse case**: a tool marked `defer_loading: true` **cannot** also carry its own `cache_control` marker — the API returns a 400 if you try. That makes sense given the mechanism: a deferred tool is, by design, meant to be discovered and expanded inline rather than sitting in the stable cached prefix, so giving it its own cache marker would be contradictory to how it's meant to behave.

**Why this matters for the exam-relevant judgment you've been building all session**: this is a case where Anthropic's own engineering already solved the "won't dynamic tool loading break caching" tension you'd otherwise expect to encounter — a designed interaction between two features that could easily have conflicted, rather than a trade-off you have to manage yourself. Worth contrasting with the general rule from earlier in the caching docs: *"switching models or mutating tools mid-session... invalidates the cache from its tier downward"* — that's true for tools that are part of your static, upfront tool list changing. Tool Search's deferred-loading tools are specifically exempt from that because they were never part of the cached prefix to begin with.

Want a scenario testing this — maybe one where a design change to a static (non-deferred) tool list breaks caching, contrasted with one using Tool Search that doesn't?