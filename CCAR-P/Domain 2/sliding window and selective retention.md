Whwn to use sliding window and selective retention


Good distinction to nail down precisely, since the last scenario used summarization, and it's worth being clear about when the *other* two techniques from the overview are the better fit instead.

**Sliding window**: keep only the most recent N turns in full; drop (not summarize) anything older than that.

- **When it fits**: tasks where old turns genuinely stop being relevant once enough time/turns have passed — the current turn only ever needs recent context, not the full history. Think of a live customer support conversation about *today's* issue, where turn 3's small talk or an earlier, now-irrelevant tangent has no bearing on turn 20. Also fits when you need a hard, predictable token ceiling (you always know your history costs at most N turns' worth, useful for tight latency/cost budgets) and where losing older detail entirely is an acceptable trade-off.
- **Why it's not the right fit for your IT support scenario**: the guest-network detail from turn 2 was still genuinely load-bearing at turn 15 — a sliding window that dropped turns older than, say, the last 8 would have discarded that detail entirely rather than diluting it, which is a worse outcome than the dilution problem you started with. Sliding window trades "some information gets diluted" for "old information is gone completely" — only acceptable when you're confident nothing that far back still matters.

**Selective retention**: actively decide, based on content/relevance rather than pure recency, what to keep and what to drop — independent of how many turns ago something happened.

- **When it fits**: conversations where relevance doesn't correlate cleanly with recency — a stated constraint from turn 2 might still matter at turn 30, while an entire exchange from turn 25 (a dead-end fix attempt that got fully resolved and closed out) might be safely discardable immediately. This requires actual judgment about *what* matters, not just *how recent* it is — e.g., explicitly tagging and retaining "user's stated constraints/environment facts" as a standing set, while letting resolved sub-threads drop off regardless of how recent they were.
- **This is actually the better-fit technique for your IT support scenario**, arguably more precise than summarization alone: rather than periodically summarizing everything indiscriminately, you'd specifically retain "guest network, not office connection" as a standing fact for the whole conversation (it's a constraint, not a transient step), while letting resolved fix-attempt exchanges (turns 7-17, once each attempt is confirmed failed and abandoned) drop out or compress much more aggressively, since they're no longer actionable once superseded.

**How the three techniques relate, and how the exam likely frames choosing between them**:
- **Summarization/compaction**: preserves *everything*, just compressed — safest for not losing detail, costs some fidelity, still requires processing (and re-processing on update) the compacted version.
- **Sliding window**: cheapest and simplest, but risks total loss of anything outside the window — appropriate only when you're confident nothing older still matters.
- **Selective retention**: most precise (keeps exactly what's relevant regardless of age, drops exactly what isn't regardless of recency), but requires the most design effort — you need a mechanism for judging relevance, not just applying a mechanical rule based on turn count.

The exam-relevant judgment, mirroring the "diagnose from evidence, don't apply a rule reflexively" thread running through this whole domain: pick the technique based on **whether relevance in this specific task correlates with recency**. If old turns reliably stop mattering after enough time (support chat about today's issue), sliding window is simplest and sufficient. If some old content remains critical regardless of age while other content becomes irrelevant quickly (stated constraints vs. resolved sub-threads, as in your IT scenario), selective retention or targeted summarization that explicitly preserves standing facts is worth the extra design effort — a blind sliding window would silently drop exactly the kind of detail that caused your scenario's failure.

Want to try a scenario that specifically tests choosing between sliding window and selective retention?