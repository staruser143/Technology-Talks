**Scenario**

A company's Claude-powered internal IT support chatbot handles multi-turn troubleshooting conversations with employees. A typical conversation might run 15-20 turns as the assistant works through diagnosing a connectivity issue. Currently, the system sends the *entire* conversation transcript, verbatim, on every single turn — by turn 18, the transcript itself has grown to roughly 12,000 tokens, even though the actual issue (a VPN configuration problem) was identified and partially addressed by turn 6, and turns 7-17 consist mostly of the assistant trying different fix suggestions and the employee reporting whether each one worked.

The team notices two things: (1) response latency has been creeping up noticeably as conversations get longer, and (2) in a few long conversations, the assistant has "forgotten" a detail the employee mentioned early on (e.g., "I'm on the guest network, not my usual office connection" stated in turn 2) and given advice inconsistent with that detail by turn 15, even though it's technically still present somewhere in the transcript.

**Question**: What's the most likely explanation for both symptoms, and what should the team do?

A) The context window is too small for this use case; the team should switch to a model with a larger context window, which would resolve both the latency growth and the forgotten-detail problem without any other changes.

B) Both symptoms point to unmanaged conversation history growth: rising latency is a direct, expected consequence of the transcript growing on every turn with no trimming, and the "forgotten" early detail is a lost-in-the-middle-style dilution problem — the detail is technically present but increasingly buried as more turns accumulate around it. The fix is active conversation history management: periodically summarize/compact older turns (e.g., condense turns 1-10 into a short summary preserving key facts like "user is on guest network" once the conversation moves well past them) rather than carrying the full verbatim transcript forever.

C) Both symptoms are unrelated to context management; the forgotten detail indicates a model capability issue requiring an upgrade, and the latency issue is a separate infrastructure/network problem unrelated to prompt size.

D) The team should cap conversations at exactly 5 turns and force employees to start a new conversation after that, since shorter conversations are always better regardless of the task.

Take your best guess and I'll walk through it.



Correct — and this scenario is essentially "lost in the middle" transplanted from Domain 3's RAG context into conversation history specifically, which is exactly the generalization the domain overview flagged as the key connective thread for this sub-topic.

**Why B is right**

Both symptoms trace back to the same root cause, and it's worth confirming why they're not actually two separate problems:

- **Rising latency** is a direct, mechanical consequence of sending an ever-growing verbatim transcript on every single turn — by turn 18, the model is processing roughly 12,000 tokens of history before it even gets to the current turn's actual question. This isn't a mysterious performance regression; it's the predictable cost of unmanaged context growth, the same "more retrieved context isn't always better" cost problem from Domain 3, just triggered by conversation length instead of retrieval breadth.
- **The forgotten guest-network detail** is the accuracy-side twin of that same growth: the detail is technically present in the transcript (nothing was deleted), but by turn 15 it's buried under 13 turns of intervening troubleshooting exchanges — structurally identical to your university academic-advising scenario, where the correct chunk was retrieved every time but under-weighted by its position deep in a crowded context. Here, the "crowded context" is accumulated conversation history rather than retrieved RAG chunks, but the mechanism (something true and present becoming less reliably attended to as more content surrounds it) is the same.

The fix follows the same logic as your RAG dilution fix, adapted to conversation management specifically: **summarize/compact older turns** rather than carrying them forever verbatim. Condensing turns 1-10 into a short summary that explicitly preserves the facts still relevant going forward ("user is on guest network, not office connection; VPN config issue identified in turn 6; fixes attempted: X, Y, Z, none resolved yet") does two things at once — it shrinks the token footprint (addressing latency) and it *promotes* the still-relevant early detail out of a "buried in turn 2 of 18" position into a compact, recently-restated summary that's much harder for the model to lose track of (addressing the dilution problem). This is a more targeted, evidence-matched fix than either symptom would suggest in isolation.

**Why the others are wrong**

- **A)** repeats the exact misdiagnosis pattern you've now correctly rejected multiple times (your university scenario's wrong answer C, your pricing-grounding scenario) — treating a context-management/structural problem as a capacity problem. A larger context window would let the *same* growing, unmanaged transcript fit for longer before hitting a hard limit, but it does nothing to address *why* an early detail becomes less reliably weighted as more content accumulates around it — that's an attention/positioning issue, not a capacity issue, and a bigger window arguably makes it easier to accumulate even more unmanaged history before anyone notices the dilution problem.
- **C)** splits one root cause into two unrelated, wrongly-diagnosed problems — attributing the forgotten detail to "model capability" repeats the now-familiar mistake of reaching for a capability explanation without first checking whether the model was given well-structured context to work with (it wasn't — it was given an ever-growing, unmanaged transcript). And attributing rising latency to "infrastructure/network" ignores the much more direct, evidenced explanation sitting right in the scenario: transcript size is growing linearly with turn count, and processing cost scales with token count — no exotic infrastructure explanation is needed when a simpler, directly-supported one already fits.
- **D)** overcorrects with an arbitrary, rigid cap that ignores task reality — some troubleshooting conversations genuinely need more than 5 turns to resolve, and forcing a hard restart doesn't actually solve the underlying problem (it just resets it prematurely, likely losing legitimately-needed context and frustrating users who now have to re-explain their issue from scratch). This is the same "blanket rule applied without regard to actual need" mistake as "always split agents past 2-3 tools" from your IT helpdesk capability-bloat scenario — the fix isn't an arbitrary limit, it's active, evidence-based management of what's carried forward.

**Pattern to remember, closing out Domain 2**: unmanaged conversation history is subject to the exact same dilution and cost dynamics as unmanaged retrieval or unmanaged tool-loading — "lost in the middle" isn't a RAG-specific phenomenon, it's a general property of long, undifferentiated context, and it shows up anywhere content accumulates without active curation: retrieved chunks, loaded tools, or turn-by-turn conversation history alike. The fix pattern is also consistent across all three: don't carry everything forward indefinitely — actively curate (summarize, filter, discover-on-demand) based on what's still relevant to the current task.

That completes solid coverage of all five Domain 2 sub-topics — model selection, guardrails, prompt engineering technique, prompt reuse, and context/token optimization. Want a mixed review across all of Domain 2 now, or move to Domain 4 (Evaluation, Testing & Optimization, 16%)?

