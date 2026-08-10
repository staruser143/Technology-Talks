## RAG Pipeline Design — deeper dive

Let's go a level deeper than the initial overview, since this is one of the most concept-dense parts of Integration.

### 1. Chunking strategies, in more detail

- **Fixed-size chunking** (e.g., 300-500 tokens with 10-20% overlap): simplest to implement, predictable cost, but blind to document structure — can split a table mid-row or cut a definition off from the term it defines.
- **Structural/semantic chunking**: splits on natural boundaries — headers, paragraphs, list items, sentence groups with topic coherence. Produces variable-size chunks but each one is more likely to be a complete, coherent unit of meaning.
- **Recursive chunking**: tries larger structural units first (sections), and only falls back to smaller splits (paragraphs, sentences) if a unit is still too large — a hybrid that respects structure where possible while still bounding chunk size.
- **Document-aware chunking**: uses format-specific logic — keep tables intact as a single chunk, keep code blocks intact, treat each Q&A pair in an FAQ as one chunk. This matters a lot for structured documents (contracts, policies, API docs) where an arbitrary split destroys meaning.

**Exam-relevant judgment**: chunk size trades off two failure modes. Too small → a chunk lacks enough context to be useful on its own, or the answer is split across two chunks and neither one alone is retrieved with high enough relevance. Too large → retrieval becomes less precise (a big chunk might be "somewhat relevant" to many queries rather than "highly relevant" to one), and you waste context budget on irrelevant surrounding text.

### 2. Embeddings and indexing choices

- The embedding model determines what "similar" means — general-purpose embedding models work fine for broad domains, but specialized/fine-tuned embeddings can matter a lot for jargon-heavy domains (legal, medical, code) where generic embeddings may not capture domain-specific meaning well.
- **Metadata is not optional in production RAG.** Tagging chunks with source, date, document type, access permissions, etc. lets you filter *before* or *alongside* semantic search — this is often what separates a demo RAG system from a production one. A common exam trap: a design that does semantic search across an entire corpus with no metadata filtering, when the query clearly implies a filterable scope (e.g., "what's the current PTO policy" should filter to the *current* version of the policy doc, not retrieve semantically-similar-but-outdated versions too).

### 3. Retrieval strategy, in more detail

- **Pure vector (dense) search**: strong at conceptual/paraphrase matching ("time off" matching "PTO"), weak at exact-token matching (product SKUs, error codes, proper nouns it wasn't trained to associate).
- **Keyword/BM25 (sparse) search**: strong at exact matches, weak at conceptual/paraphrase matching.
- **Hybrid search**: runs both and combines/merges results (often via a fusion method like reciprocal rank fusion) — generally the safer default because it covers both failure modes above.
- **Re-ranking**: a second-stage model scores a larger candidate set for actual relevance to the query, more expensive per-item than initial retrieval but far more accurate — the "retrieve wide, re-rank narrow" pattern from the accuracy-latency material lives here specifically.
- **Multi-hop / iterative retrieval**: for queries that need information from multiple, non-adjacent parts of a corpus (e.g., "compare the refund policy in 2023 vs. 2025"), a single retrieval pass may not surface both needed documents — this is where an agentic retrieval step (retrieve, assess if enough info was found, retrieve again if not) becomes relevant, tying back to Domain 1's pattern-selection material.

### 4. Failure modes the exam likes to test

- **Retrieval returning technically-similar-but-wrong-context chunks** (e.g., pulling last year's expired policy because it's semantically close to this year's, with no recency filtering).
- **The "lost in the middle" problem**: even with good retrieval, if you stuff too many retrieved chunks into context, the model can under-weight information buried in the middle of a long context — meaning *more* retrieved context isn't always better, tying back to the "competing for attention" principle from decomposition.
- **Retrieval failing silently**: if nothing relevant was actually found, and the system doesn't detect that, Claude may answer from general knowledge or hallucinate rather than surfacing "I don't have information on this" — a good RAG design needs a way to detect and handle the no-good-match case explicitly rather than always injecting whatever came back.

---

Want to try scenario practice on RAG pipeline design now?