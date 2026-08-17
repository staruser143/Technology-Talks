When using manual approach should we add the critical facts in a separate  block somewhere like system prompt


Yes — and this is exactly the right instinct, because it converts a *hope* ("compaction will probably keep this") into a *guarantee* ("this is structurally protected from being lost").

**Why this works, tying directly to material you already have**

Recall the system-prompt characteristic you established early in Domain 2: content belongs in the system prompt when it's "true for every request in this deployment," not something that should ever be treated as optional or subject to summarization judgment calls. A critical fact like a stated allergy or a hard budget cap is exactly that kind of content — it's not really *part of the flowing conversation* that compaction is designed to compress, it's a standing constraint that should be true and visible for the entire interaction, structurally separate from the turn-by-turn back-and-forth.

**Two ways this actually gets implemented, both real:**

1. **A dedicated system-prompt block for extracted standing facts**: as the conversation progresses, the application layer (not the model's own judgment during compaction) explicitly extracts and writes critical facts into a persistent section of the system prompt — "Customer constraints: severe shellfish allergy; budget cap $4,000; must return by the 15th." This section is never subject to compaction because it isn't part of the conversation history being summarized at all — it lives outside that mechanism entirely, updated deliberately by your application logic whenever a new standing fact is stated.

2. **The equivalent pattern from the Claude Code material you just read**: persistent rules going in `CLAUDE.md` rather than relying on conversation history — the docs are explicit that "instructions from early in the conversation may be lost" during compaction, and the fix given is precisely "put persistent rules in CLAUDE.md rather than relying on conversation history." That's the same principle, just implemented as a project-level file instead of a system-prompt block — the mechanism differs by product surface, but the underlying architecture is identical: **critical, durable facts belong in a structurally protected location, separate from and immune to whatever happens to the flowing conversational history.**

**Why this is the more reliable design than trusting compaction, even manual compaction**: even a well-timed, manually-triggered `/compact` is still a summarization pass making its own judgment about what's important — it's better than the automatic version because you control *timing* and can add a *focus* hint, but it's not a hard guarantee that every critical fact survives every single compaction event, especially across a very long session with multiple compaction cycles. A separate, protected block sidesteps that uncertainty entirely: the fact isn't being *judged* as worth keeping each time — it's simply never part of what gets compressed.

**The practical design pattern this suggests, tying back to your trip-planning scenario**: 
- Rather than relying purely on selective retention *within* the conversation flow, you'd combine it with this structural approach — extract standing constraints out to a protected system-level block the moment they're stated, and let compaction/selective retention handle everything else (the resolved sub-decisions, the discarded hotel comparisons) that's genuinely fine to compress or drop. The protected block handles "must never be lost";
- Conversation-history management handles "everything else, judged by actual ongoing relevance."

Want a scenario that tests choosing between relying on compaction alone versus extracting critical facts to a protected block?
