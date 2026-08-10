**Scenario**

A software company's RAG-powered internal documentation assistant helps engineers ask questions about their codebase and internal tools. It retrieves the top 5 chunks by vector similarity score for every query and always passes those 5 chunks into Claude's context, regardless of how relevant they actually are — there's no minimum similarity threshold and no check for whether the retrieved chunks are actually related to the question. An engineer asks about a tool that was decommissioned two years ago and has no documentation left in the knowledge base at all. The system retrieves the 5 chunks that happened to score *highest* among everything available — which, since nothing is actually about the decommissioned tool, are just the 5 most tangentially-related documents (some general architecture docs, an unrelated tool's setup guide). These get passed to Claude, which then generates a plausible-sounding but entirely fabricated explanation of how the decommissioned tool supposedly works, loosely stitched together from the unrelated context it was given.

**Question**: What's the core gap in this system, and how should it be fixed?

A) The core problem is chunk size; the 5 retrieved chunks are probably too small to contain a complete answer, and increasing chunk size would give Claude enough information to answer correctly.

B) The core gap is that retrieval always returns a fixed number of results with no relevance floor and no signal to Claude (or the system) about whether what was found is actually a good match — "top 5" always returns something, even when nothing in the corpus is truly relevant. The fix is to set a minimum similarity/relevance threshold below which no chunks are injected (or a low-confidence flag is passed to Claude), and to explicitly instruct/design the system to state when it doesn't have relevant information rather than always attempting an answer from whatever was retrieved.

C) The problem is that Claude shouldn't be trusted to answer documentation questions at all; the system should be replaced with a simple keyword search that returns raw documents to the engineer without any LLM involvement.

D) The problem is unavoidable — any RAG system will occasionally be asked about something not in its knowledge base, and hallucination in that case is an acceptable and expected trade-off for having a conversational assistant at all.

Take your best guess and I'll walk through it.



Correct — and this closes out the RAG failure-mode set nicely, because it isolates the specific danger of "always returning something" versus the other failure modes you've already diagnosed.

**Why B is right**

The core mechanical flaw is right there in the design: **"top 5" is a fixed-count operation, not a relevance-gated one.** Vector similarity search will always rank *something* highest, even if the highest-ranked item is a poor match in absolute terms — similarity scores are relative to what's in the corpus, not an objective measure of "is this actually relevant to the query." When nothing in the knowledge base is genuinely about the decommissioned tool, "top 5 by score" doesn't return "no good answer exists" — it returns "the 5 least-bad options among everything available," with no signal anywhere in the pipeline that those options are actually weak matches.

This is a different failure mode from everything else in this set, and it's worth being precise about why:
- The telecom scenario was about chunks being *malformed* (structure destroyed).
- The policy scenario was about the *wrong* chunk being retrieved with high confidence (metadata blindness).
- The university scenario was about the *right* chunk being retrieved but under-weighted (positional dilution).
- **This scenario is about *no good chunk existing at all*, and the system having no way to detect or communicate that.** That's a categorically different problem — it's not about retrieval quality, it's about the absence of a relevance floor and the absence of any "I don't know" pathway.

The fix has two parts, matching the two things the material calls out: a **minimum similarity threshold** (so genuinely poor matches don't get injected as if they were useful context — either return nothing, or flag low confidence), and a **design/instruction layer that makes "I don't have information on this" an explicit, expected output** rather than treating every retrieval as sufficient grounds for a confident answer. Both matter — the threshold prevents bad context from reaching the model, and the "allowed to say I don't know" instruction prevents the model from stretching to fabricate an answer even from marginal context it does receive.

**Why the others are wrong**

- **A)** reaches for a chunk-size explanation that doesn't fit the evidence — the problem isn't that the retrieved chunks were too small to contain the answer, it's that they don't contain the answer *at all*, because nothing relevant exists in the corpus. No amount of resizing chunks conjures information that was never indexed in the first place.
- **C)** overcorrects by abandoning the LLM layer entirely rather than fixing the actual gap (missing relevance floor). Removing Claude from the loop also removes the value it adds when retrieval *does* work well — this discards a working system to avoid a specific, fixable failure mode, the same overcorrection pattern you've flagged correctly in earlier scenarios (like "ban all latency optimization" or "delete all historical documents").
- **D)** is the most tempting wrong answer because it's *partially* true — you can't index information that doesn't exist, so some version of "sometimes there's no answer" is genuinely unavoidable. But the scenario isn't testing whether gaps in a knowledge base can exist; it's testing whether the system *handles* that situation well. "Occasionally being asked about something out of scope" is unavoidable — "confidently fabricating an answer when that happens" is not, and is squarely fixable with a relevance threshold and an explicit no-match pathway. Calling the hallucination itself "acceptable" ignores that it's a solvable design gap, not an inherent limitation.

**The complete RAG failure-mode map, now that you've drilled all four:**
1. **Chunking/matching mismatch** (telecom) — structure destroyed or query type (exact vs. semantic) mismatched to retrieval method.
2. **Metadata blindness** (policy) — semantic similarity can't distinguish current/valid from outdated/superseded; needs filtering.
3. **Positional dilution** (university) — correct info retrieved but under-weighted by position in a crowded context.
4. **No relevance floor** (this one) — retrieval always returns *something*, with no mechanism to detect or signal "nothing here is actually relevant."

Each has a distinct signature in the symptoms described, and a distinct fix — the exam's harder RAG questions will likely hinge on telling these four apart from the described symptoms rather than reaching for a generic "improve retrieval" answer.

That's solid, comprehensive coverage of RAG pipeline design. Want to move to another Integration sub-topic now — protocol selection (MCP vs. API/CLI vs. agent-to-agent), or observability at scale?