# Is there a need to use /context at a any time


Good question — worth being precise about what `/context` actually gives you and when it's genuinely useful, versus when you're better served by the tools we've already covered.

**What `/context` actually is**: 
- A live diagnostic command in Claude Code that shows a full breakdown of exactly what's consuming your context window right now — system prompt, system tools, MCP tools (broken down per-server), custom agents, memory files, skills, conversation/messages, and remaining free space — along with actionable suggestions (e.g., "MCP server X uses 12% of context — consider disabling if not needed," or "memory file exceeds 50KB — review for outdated entries").

**When it's genuinely worth using — three real, distinct cases:**

1. **Before reaching for compaction at all.** This is a direct, concrete instance of "diagnose before applying a fix" — the same discipline you've applied throughout this whole session. If a session is running low on context, the first move isn't necessarily to compact or summarize the conversation; it's to check *what's actually consuming the space*. If `/context` shows that a chunky, mostly-unused MCP server is eating 15-20% of the window before you've even typed a message, disabling it recovers real space without touching the conversation at all — a cheaper, more targeted fix than compacting content that was never the problem. This is exactly the "monolithic tool loading" failure mode from your Domain 3 progressive-discovery scenarios, made visible and actionable.

2. **Catching bloated persistent files before they tax every single turn.** Recall that CLAUDE.md/memory files get prepended to every turn — a bloated one isn't a one-time cost, it's a recurring tax on every message for the rest of the session. `/context` surfaces this directly ("memory file exceeds 50KB") so you can trim it once, rather than silently paying that cost turn after turn.

3. **Deciding whether compaction is actually warranted yet, and how urgently.** Rather than guessing or waiting for the automatic threshold to fire (which, as you now know, tends to compact later and less deliberately than a manual, well-timed pass), `/context` gives you the actual percentage — letting you make an informed choice about *when* to run `/compact` rather than reacting to it.

**How this fits into the overall diagnostic sequence for context problems, tying together everything from this thread**: 
- `/context` is the *measurement* step — it's the token-counting/observability instinct applied specifically to a live session, the same "measure before you fix" discipline as the token-counting API we discussed earlier, just scoped to what's actually in the window right now rather than a hypothetical prompt design.
- The sensible order is: **check `/context` first** (what's actually consuming space, and is it something disposable like an idle MCP server, or genuinely necessary conversation content) → **only then decide** whether the fix is disabling something unused, trimming a bloated persistent file, or actually compacting/summarizing conversation history.
- Jumping straight to compaction without checking `/context` first risks compressing conversation content you didn't need to touch, when the actual bloat was sitting in an unused tool registration the whole time.

