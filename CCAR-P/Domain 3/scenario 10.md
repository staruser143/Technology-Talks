**Scenario**

A company's internal RAG system helps employees ask questions about company policy ("what's our current remote work policy," "how much is the annual bonus this year"). The knowledge base contains policy documents going back 6 years — every time a policy changes, a new version of the document is added, but old versions are never deleted, since the legal team wants to preserve historical records for compliance purposes. Each document does have a `published_date` and `status` (`current` or `superseded`) field in its metadata, but the RAG pipeline's retrieval step only performs semantic vector search over document content — it doesn't reference the metadata fields at all.

An employee asks "what's the current remote work policy," and the system confidently returns an answer based on a policy document from 3 years ago that was semantically very close to the query (it uses very similar wording, "remote work policy," in its title and content) — but that document's status is `superseded`, and a newer, currently-active version exists with slightly different wording that scored lower in pure semantic similarity.

**Question**: What's the core gap here, and what should the team do?

A) The core problem is chunk size — the old and new policy documents are probably being split into chunks that are too large, causing the mismatch; reducing chunk size will fix the retrieval issue.

B) The core gap is that retrieval relies purely on semantic similarity and ignores available metadata (status: current/superseded, published_date) that directly disambiguates which document should be authoritative for this kind of query. The fix is to use metadata filtering — e.g., filter to `status: current` before or alongside semantic search for policy questions — rather than relying on semantic similarity alone to surface the right document among near-duplicate versions.

C) The core problem is that old policy versions shouldn't exist in the knowledge base at all; the legal team's requirement to retain them for compliance is incompatible with a good RAG system, so old versions should be removed from retrieval entirely, compliance concerns aside.

D) The core problem is the embedding model; a better embedding model would have correctly scored the current version higher than the superseded one based on subtle wording differences.

Take your best guess and I'll walk through it.



Correct — and this is close to the cleanest possible illustration of why metadata filtering isn't optional in production RAG, because the failure mode here is genuinely dangerous: a confident, wrong answer that looks completely legitimate.

**Why B is right**

The scenario is engineered so that semantic similarity alone actively works *against* correctness: the superseded document scores higher precisely because it uses similar wording to the query, not because it's the right answer. This is exactly the "technically-similar-but-wrong-context" failure mode named in the material — recency and validity are dimensions that pure semantic similarity has no way to represent, because "how semantically close is this text to the query" and "is this document currently authoritative" are simply different questions. A 3-year-old superseded policy and a brand-new current policy on the same topic will often be *more* semantically similar to each other than either is to a slightly-differently-worded query — similarity says nothing about which one you should trust.

The critical detail the scenario hands you: **the metadata needed to solve this already exists** (`status: current/superseded`, `published_date`) — it's just not being used anywhere in retrieval. That's the exact gap the material calls out: "a common exam trap: a design that does semantic search across an entire corpus with no metadata filtering, when the query clearly implies a filterable scope." "What's the *current* remote work policy" is about as direct a signal as a query can give that recency/validity filtering should apply — the word "current" is functionally a metadata filter request embedded in natural language.

The fix is precise and doesn't require touching anything else in the pipeline: filter to `status: current` before or alongside the semantic search, so superseded documents are excluded from the candidate pool for this kind of query entirely, rather than trusting semantic ranking to sort them correctly (which the scenario shows it doesn't).

**Why the others are wrong**

- **A)** reaches for a chunking explanation that doesn't fit the evidence at all — the scenario's failure isn't about a chunk being incomplete or malformed, it's about the *entire correct document* being outranked by the *entire wrong document*. Changing chunk size doesn't touch the actual problem: two whole documents, both fully intact, being compared on semantic similarity alone with no recency signal.
- **C)** overcorrects by treating a legitimate business requirement (legal retention of historical policy versions for compliance) as something that should be sacrificed for RAG convenience — that's backwards. The right move is never "delete data the business needs to keep because it makes retrieval simpler"; it's "use the metadata that already distinguishes valid-for-retrieval-as-current from retain-for-compliance-only." This is the same discipline as respecting a stated business constraint from Domain 1's SLA scenarios — the compliance requirement is a real constraint the design has to work within, not around.
- **D)** guesses at a model-quality explanation without evidence supporting it, and misses that even a *perfect* embedding model comparing two genuinely similar policy documents on wording alone has no basis for knowing one is stale — "current vs. superseded" isn't a semantic property of the text, it's an operational fact stored in metadata that the text itself may not clearly signal (older documents don't typically say "hey, I'm outdated now" in their content).

**Pattern to remember, tying this back to the RAG failure-modes list**: whenever a corpus contains multiple versions, drafts, or variants of similar content (policy revisions, contract templates across time, superseded product specs), semantic similarity alone is structurally blind to which one is authoritative — recency, status, and validity are metadata questions, not content-similarity questions, and need to be enforced as filters, not left to ranking. If a scenario gives you metadata fields sitting unused in a design, that's almost always the answer.

Want another metadata filtering scenario, one on the "lost in the middle" failure mode, or one on retrieval failing silently (no good match found)?