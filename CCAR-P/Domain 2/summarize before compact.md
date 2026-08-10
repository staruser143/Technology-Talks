Does that mean before invoking /compact i need to extract critical facts to a block in system prompt, then run /compact. Is my understanding correct?

Your understanding is correct in spirit, and worth refining slightly on timing, since the "when" matters as much as the "what."

**The core sequence is right**: yes — critical facts should be pulled out into a protected block (system prompt / CLAUDE.md-equivalent) that sits *outside* what compaction touches, and only then is it safe to compact the rest of the conversation freely, because the things that must never be lost are no longer *at risk* from that compaction pass at all.

**One refinement on timing — don't wait until right before compacting to extract**: the safer practice is to extract a critical fact into the protected block **the moment it's stated**, not as a last-minute step you remember to do right before running `/compact`. Two reasons this matters:

1. **You might forget, or compact automatically fires first.** If you're relying on "I'll extract critical facts right before I manually compact," you're vulnerable to exactly the failure mode from your mental-health-intake scenario — auto-compaction could trigger before you get around to it, especially in a long session where you're not actively monitoring the threshold. Extracting *as soon as the fact appears* removes that timing risk entirely — by the time any compaction happens (automatic or manual), the fact is already safe, regardless of who or what triggers it.
2. **It mirrors the actual mechanism correctly.** The protected block isn't a "pre-compaction checklist step" — it's a parallel, ongoing practice that runs alongside the conversation the whole time, not a one-time action you perform at the moment of compacting. Think of it less as "prepare, then compact" and more as "maintain the protected block continuously; compact whenever needed, safely, because the block is already current."

**So the corrected sequence, precisely**:
1. As the conversation unfolds, any time a critical, standing fact is stated (a constraint, a safety-relevant detail, a hard requirement) — immediately write it into the protected block, not the flowing conversation.
2. Compaction — whether it fires automatically or you invoke `/compact` manually — can then run at any point without risk to those facts, because they were never part of what's being compacted in the first place.
3. If you're choosing manual compaction specifically (the better option when you have the choice), you'd still want to time it at a natural breakpoint and optionally use `focus` — but that's about optimizing the *quality* of what happens to the rest of the conversation, not a precondition for the critical facts' safety, since those are already protected independent of when compaction runs.

So: yes to the overall idea, with the adjustment that the extraction should be a continuous habit tied to *when facts are stated*, not a step you insert immediately before compacting — otherwise you're still exposed to exactly the risk (auto-compact firing first, or simply forgetting) that made relying on compaction alone unsafe in the first place.