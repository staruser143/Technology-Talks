**Scenario**

A university's RAG-powered academic advising assistant helps students ask questions like "can I take Advanced Algorithms without having taken Data Structures first?" The retrieval step works correctly — it successfully finds and retrieves the exact chunk containing the prerequisite requirement for Advanced Algorithms, along with 14 other chunks that are all genuinely relevant background (course descriptions, department policies, general prerequisite structure). All 15 chunks get passed into Claude's context, ordered roughly by retrieval score, with the single most directly relevant chunk (the actual prerequisite rule) landing in the 8th position out of 15 — right in the middle of the injected context.

Testing shows Claude's answers are inconsistent: sometimes it correctly states the prerequisite requirement, but on repeated identical queries, it sometimes gives a vague or slightly wrong answer that seems to miss or misstate the specific rule — even though the correct chunk was retrieved every single time and is present in the context every single time.

**Question**: What's the most likely explanation for this inconsistency, and what should the team do?

A) The retrieval step is broken and intermittently failing to find the right chunk; the fix is to improve the embedding model so retrieval becomes more reliable.

B) This is a "lost in the middle" problem — the correct chunk is being retrieved successfully every time, but its position (buried in the middle of 15 chunks) makes it easy for the model to under-weight relative to chunks at the start or end of the context. The fix isn't better retrieval, it's reducing how much gets injected (retrieve fewer, more targeted chunks) and/or reordering so the highest-relevance chunk is placed at the start or end of context rather than the middle.

C) The problem is that Claude's context window is too small for this task; switching to a model with a larger context window will resolve the inconsistency.

D) The inconsistency is expected and acceptable random variation; since the correct chunk is present in context every time, no further changes are needed and the team should just re-run queries that produce wrong answers.

Take your best guess and I'll walk through it.




Correct — and this scenario is specifically built so that the obvious diagnosis (retrieval is broken) is wrong, which is the whole point of testing this failure mode.

**Why B is right**

The single most important detail in the scenario is this: **the correct chunk is present in context every single time**, yet the answer is still sometimes wrong. That fact alone should rule out anything upstream of context injection — retrieval is doing its job. The inconsistency has to be happening *after* retrieval, in how the model processes what it was given, which is exactly what "lost in the middle" describes: models tend to attend more reliably to information at the start or end of a long context and less reliably to information buried in the middle, even when that information is fully present and unambiguous. Position 8 of 15 is about as "middle" as it gets.

Two things make this diagnosis solid rather than speculative:
- **The pattern is inconsistency on identical inputs**, not a consistent wrong answer. If the chunk were sometimes missing (a retrieval problem), you'd expect a consistent failure when it's absent and a consistent success when it's present. Instead you get variability on the *same* query with the *same* retrieved context — that's a signature of attention/weighting sensitivity, not a deterministic retrieval failure.
- **This maps directly back to material you've already internalized**: it's the same underlying principle as "competing for attention in one pass" from the decomposition scenarios, just applied to position-within-context instead of task-count-within-a-prompt. More things injected into context doesn't just cost tokens — it dilutes how reliably any single piece gets weighted, and *where* in the sequence something sits affects that further.

The fix follows directly: **reduce what's injected** (do you really need 15 chunks, or would the top 3-5 most relevant ones suffice — tying back to "more retrieved context isn't always better") **and/or reorder** so the highest-relevance chunk is placed at the start or end of context rather than left wherever raw retrieval score happened to rank it. Neither of these requires touching the retrieval mechanism itself, because retrieval isn't what's broken.

**Why the others are wrong**

- **A)** misdiagnoses the layer entirely. The scenario explicitly states the correct chunk is retrieved every time — there's no retrieval failure to fix, and improving the embedding model does nothing to address a problem that happens after retrieval has already succeeded.
- **C)** also misreads the evidence. All 15 chunks fit in context already (they're described as being passed in successfully) — the model isn't running out of room, it's under-weighting something that's fully present. A bigger context window would let you fit *more* chunks, which, if anything, risks making a lost-in-the-middle problem worse by giving the model even more content to bury the important chunk within.
- **D)** gives up on a diagnosable, fixable problem by mislabeling it as unavoidable randomness. This isn't random noise — it's a known, well-documented behavior with identifiable causes (chunk count, chunk position) and identifiable fixes (reduce, reorder). Treating it as "just re-run failed queries" leaves a systematic, addressable weakness in production indefinitely, which is a worse outcome than the modest engineering effort of fixing injection design.

**Pattern to remember**: when the evidence explicitly confirms the right information *is* present in context, but the output is still inconsistent or wrong, don't reach for retrieval-layer fixes (better embeddings, more candidates, bigger context) — the problem lives in *how much* is injected and *where* the important piece sits within that injection, not in whether it was found. This is a distinct failure mode from your telecom (chunking/matching) and policy (metadata filtering) scenarios — all three sit under "RAG can go wrong," but they're diagnosed and fixed completely differently, and the exam is testing whether you can tell which failure mode a given symptom actually points to.

Want one more RAG scenario on silent retrieval failure (no good match found), or should we move to a different Integration sub-topic — protocol selection (MCP vs. API/CLI) or observability at scale?

