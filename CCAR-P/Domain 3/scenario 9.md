**Scenario**

A telecom company builds a RAG system to help customer support agents answer questions using the company's product manuals and troubleshooting guides. Initial testing reveals a specific pattern of failures: when agents ask questions like "what's the error code for a modem sync failure," the system sometimes retrieves chunks that mention "sync failure" conceptually but misses the chunk that actually contains the specific error code table (formatted as a structured table in the source PDF, with error codes in one column and descriptions in another). The current pipeline uses fixed-size chunking (400 tokens, no overlap) applied uniformly across all manual content, followed by pure vector similarity search with no keyword component.

**Question**: What's the most likely cause of this specific failure pattern, and what combination of changes would address it?

A) The embedding model is fundamentally inadequate for this domain; the fix is to fine-tune a custom embedding model on telecom terminology before changing anything else.

B) Two compounding issues: (1) fixed-size chunking with no document-aware handling likely splits the error code table awkwardly across chunk boundaries or separates codes from their descriptions, destroying the table's meaning, and (2) pure vector search struggles with exact-match lookups like specific error codes, which are more of a keyword-matching problem than a semantic-similarity one. Fix: use document-aware chunking that keeps tables intact as single chunks, and add a keyword/BM25 component (hybrid search) alongside vector search to catch exact-match queries like error codes.

C) The problem is that 400 tokens is too large a chunk size; simply reducing to 100 tokens per chunk will resolve the issue without any other changes.

D) The system needs a complete architecture change to an agentic multi-hop retrieval loop, since a single retrieval pass is fundamentally insufficient for this kind of technical content.

Take your best guess and I'll walk through it.




Correct — and this scenario is designed to combine two distinct RAG failure modes in one symptom, which is exactly the kind of layered diagnosis this domain rewards.

**Why B is right**

The specific detail "formatted as a structured table... error codes in one column, descriptions in another" is the scenario doing a lot of work — it's telling you this isn't generic prose, it's exactly the kind of content that generic fixed-size chunking destroys. Two separate problems are compounding here, and the correct answer is the one that fixes both, not just one:

- **Chunking problem**: fixed-size, 400-token chunking with no document-awareness has no concept of "this table is one unit." A table splitting mid-row across a chunk boundary means the chunk retrieved might contain error codes with no descriptions, or descriptions with no codes — the meaning that only exists when the row is intact gets destroyed by an arbitrary token-count cutoff. This is the exact document-aware chunking gap named in the material: tables, code blocks, and other structured content need format-specific handling, not uniform splitting.
- **Retrieval-strategy problem**: even if the table chunk were intact, "what's the error code for X" is fundamentally an exact/near-exact matching problem — the user wants a specific alphanumeric code, not a conceptually-similar passage. Pure vector search is tuned for semantic/paraphrase similarity ("sync failure" matching "synchronization error"), which is a strength for prose questions but a known weak spot for precise term/code lookups. This is the textbook case for hybrid search — the keyword/BM25 component catches the exact-match pattern that vector search alone is likely to under-rank.

Fixing only one of these leaves a gap: document-aware chunking alone keeps the table intact but doesn't help if vector search still under-ranks it for a code-specific query; hybrid search alone helps ranking but doesn't matter if the table's meaning was already destroyed at the chunking stage. The correct fix addresses both layers because the scenario's evidence points to both being broken simultaneously.

**Why the others are wrong**

- **A)** jumps to the most expensive, slowest fix (custom embedding fine-tuning) without first ruling out the much cheaper, more targeted explanations that the evidence directly supports (table-splitting, lack of keyword matching). This is the same discipline as the accuracy-latency material: diagnose from the actual evidence before reaching for a heavy structural change, and a fine-tuned embedding model wouldn't even fix the chunking problem — if the table's still split awkwardly, better embeddings are being asked to make sense of already-broken chunks.
- **C)** guesses at a parameter tweak (smaller chunks) without addressing *why* the current chunking is failing. Smaller chunks could make table-splitting *worse*, not better — a 100-token chunk is even more likely to catch a partial table row divorced from its column context than a 400-token one. This treats chunk *size* as the variable to tune when the real issue is chunking *strategy* (uniform token-count vs. document-aware).
- **D)** reaches for the most architecturally complex answer (agentic multi-hop retrieval) for a problem that doesn't show multi-hop symptoms. Multi-hop retrieval solves "the answer requires combining information from multiple, non-adjacent parts of the corpus" (like your 2023-vs-2025 policy comparison example) — this scenario is a single-lookup problem (one error code, one table) that's failing at the chunking and matching level, not because one retrieval pass structurally can't reach across multiple sources.

**Pattern to remember**: when a RAG failure involves structured content (tables, code, key-value pairs, numbered lists) and the query is asking for something exact rather than conceptual, check both chunking (is the structure preserved as a unit?) and retrieval strategy (does the search method handle exact matches, not just semantic similarity?) — these two failure modes commonly co-occur precisely because generic RAG pipelines are usually tuned for prose, and structured/technical content breaks both assumptions at once.

Want another RAG scenario — maybe one testing metadata filtering or the "lost in the middle" failure mode next?