I have some more on this domain. What is the difference between automatic summarisation which happens and /compact which is invoked explicitly


Fair to want this laid out cleanly on its own — here's the direct comparison.

| | **Automatic (auto-compact)** | **Manual (`/compact`)** |
|---|---|---|
| **Trigger** | Fires on its own when usage crosses a threshold (roughly the mid-80s to mid-90s percent range of the context window, depending on version/source) | You invoke it yourself, at any point you choose |
| **Timing control** | None — happens under pressure, close to the limit, often after quality may have already started degrading from a crowded context | Full control — best practice is to run it *earlier* (some guidance suggests around 60% usage), before things get crowded, not waiting for a warning |
| **What gets prioritized** | Claude's own generic judgment about what's salient, with no input from you | You can pass a **focus**, e.g., `/compact focus on the API changes` — steering what the summary should prioritize preserving |
| **Quality of the resulting summary** | Generally lower — compressing a very full, already-crowded context tends to produce a less precise summary, since there's more to compress and less room to be selective | Generally higher — less has accumulated, so there's a cleaner, more deliberate compression, and your focus hint sharpens it further |
| **What it disrupts** | Can interrupt your workflow unexpectedly, mid-task, at a moment you didn't choose | You choose the moment — ideally a natural breakpoint (e.g., finishing one phase of work before starting the next) |
| **Underlying mechanism** | Same mechanism — the conversation is sent to a summarization pass, and the result replaces the older messages | Identical mechanism, just deliberately invoked |

**The core distinction, stated simply**:
- They're the *same underlying compaction mechanism* — a summarization pass that replaces older conversation with a condensed version — but automatic compaction is a **reactive safety net** (fires late, no input, lower-quality result), while manual `/compact` is a **deliberate, controlled intervention** (fires when you choose, with an optional focus, generally better preserved).

**Why this distinction actually matters, tying back to what we established**:
- Since neither version — automatic or manual — is a *guarantee* that any given detail survives (both are still judgment-based summarization), the practical takeaway from this whole thread stays the same regardless of which triggers it:
- Genuinely critical, Safety-relevant, or must-not-be-lost facts shouldn't rely on *either* form of compaction correctly preserving them — they belong in a structurally protected location (a dedicated system-prompt/CLAUDE.md-style block) outside what gets compacted at all.
- Manual `/compact` is simply the better of the two *when* compaction does happen, because you're steering a process you don't otherwise control the outcome of.
