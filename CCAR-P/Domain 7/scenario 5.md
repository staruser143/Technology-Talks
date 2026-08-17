**Question**: For which agent(s) is implementing Tool Search with deferred loading actually the right call?

A) Both agents should implement Tool Search, since it's a best practice that should be applied universally regardless of tool count, per the engineer's reasoning for Agent A.

B) Only Agent B is a good fit for Tool Search. Agent A has just 6 tools, well under the roughly-10-tool threshold where deferred loading's discovery round-trip overhead outweighs its context-savings benefit — all 6 tools should simply stay loaded upfront, since they're used regularly and the schema cost is small to begin with. Agent B, with 90+ tools across 12 servers where any given question only needs 2-3, is exactly the profile Tool Search is designed for — both the context-cost problem (paying for 90+ schemas on every request) and the tool-selection-accuracy problem (choosing correctly among 90+ options) are real and measurable here, and deferred loading directly addresses both.

C) Neither agent should use Tool Search; the mechanism only provides benefit above 100+ tools, and Agent B's 90+ tools still falls short of that threshold.

D) Only Agent A should use Tool Search, since smaller tool sets benefit more from search-based discovery, while Agent B's large tool count means the search mechanism itself would become a bottleneck.


Correct — and this scenario directly applies the ~10-tool threshold from the deep dive, testing whether "best practice" gets applied reflexively or matched to the actual situation, the same discipline that's run through nearly every technique-selection scenario this session.

**Why B is right**

Run the actual cost-benefit for each agent against the threshold:

- **Agent A (6 tools)**: this sits comfortably under the ~10-tool point where the deep dive established deferred loading's discovery round-trip overhead outweighs any context savings. All 6 tools are also described as "used fairly regularly" — meaning there's no meaningful subset that sits unused most of the time to defer in the first place. Implementing Tool Search here would add a search step's latency and complexity to nearly every request, for schemas small enough that loading them upfront was never actually a meaningful cost. This is a direct instance of the "reflexively apply a generally-good technique without checking whether its precondition holds" trap you've correctly rejected repeatedly this session — the engineer's reasoning ("it's best practice, apply it everywhere") is exactly the kind of context-free justification that's been wrong every time it's appeared in this format across this entire conversation.

- **Agent B (90+ tools across 12 servers, 2-3 needed per question)**: this is squarely the profile Tool Search exists for. Both problems named in the deep dive are concretely present: the **context-cost problem** (paying for 90+ tool schemas' worth of tokens on every single request, when the overwhelming majority go unused for any given question) and the **tool-selection-accuracy problem** (the exact scale — dozens of tools — where Anthropic's own measured data showed accuracy dropping without deferred loading). Deferring the long tail while keeping the handful of genuinely most-common tools (if any exist across such a broad, varied assistant) non-deferred is exactly the recommended pattern.

**Why the others are wrong**

- **A)** repeats the engineer's flawed universal-application reasoning as the correct conclusion — applying Tool Search to Agent A's 6 regularly-used tools adds real overhead (a search round-trip) without a corresponding benefit, since there was no meaningful context bloat or selection-accuracy problem to solve there in the first place.
- **C)** invents a much higher threshold (100+) than what's actually established, and even by its own logic misapplies it — the material's threshold is around 10, not 100, and Agent B's 90+ tools clears that established threshold by a wide margin regardless of where exactly the line sits between 10 and 100.
- **D)** inverts the actual relationship entirely — the threshold logic runs the opposite direction from what D claims. Smaller tool sets don't benefit *more* from search-based discovery; they benefit *less*, because there's less to save by deferring a handful of tools that were cheap to load in the first place. And nothing in the material suggests search becomes a "bottleneck" at higher tool counts — if anything, a larger tool set is precisely where search's relevance-filtering value (and the measured accuracy improvement) becomes more pronounced, not less.

**Pattern to remember, closing this topic**: Tool Search's value is threshold-dependent, not universal — it earns its overhead specifically once a tool set is large and/or sparse enough that context savings and selection-accuracy gains outweigh the added discovery round-trip cost. A small, frequently-used tool set (Agent A) gets no benefit and only pays the cost; a large, sparse tool set (Agent B) is exactly where the mechanism's actual value shows up. The exam-relevant discipline, as in every other technique-selection scenario this session: check whether the tool count and usage pattern actually justify the technique, don't apply it because it's generally recommended.

Want another scenario, or a different topic to work through?