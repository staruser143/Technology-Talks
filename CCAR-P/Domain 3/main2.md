Good — this domain rewards concrete detail, so let's go deeper on each piece.

## 1. RAG pipeline design

**Chunking**: The core tension is granularity. Fixed-size chunking (e.g., 500 tokens with overlap) is simple and predictable but can split a sentence or table mid-thought. Semantic/structural chunking (splitting on headers, paragraphs, or logical sections) preserves meaning better but produces uneven chunk sizes. Overlap between chunks (e.g., 10-20%) helps prevent losing context at chunk boundaries. Exam framing: pick chunking strategy based on document structure — a legal contract with numbered clauses wants structural chunking; a chat log or unstructured notes may be fine with fixed-size.

**Indexing**: Pure embedding-based (dense vector) indexing captures semantic similarity but can miss exact matches — a part number, an error code, a person's name — because embeddings are tuned for meaning, not literal string matches. This is why most production RAG systems layer **metadata filtering** on top (filter by date range, document type, source, permissions) before or alongside semantic search.

**Retrieval strategy** — this is the highest-yield sub-topic for exam questions:
- **Pure vector search**: fast, good at conceptual/semantic queries, weak on exact-match terms.
- **Hybrid (vector + keyword/BM25)**: combines semantic recall with exact-match precision — generally the safer default in production.
- **Re-ranking**: retrieve a larger candidate set cheaply (e.g., top 50 via vector search), then use a more expensive but more accurate re-ranker to narrow to the best 5-10 before sending to Claude. This trades a bit of latency for meaningfully better relevance — same accuracy-latency trade-off logic from Domain 1's business-value material.

## 2. Protocol selection: MCP vs. API/CLI vs. agent-to-agent

The decision test the exam wants: **how many contexts will this connection be reused across, and does discoverability matter?**
- **MCP** shines when you want a tool/data source to be discoverable and reusable across multiple different agents, apps, or teams — it standardizes "what can I call and how" so you're not hand-rolling a new integration contract every time. Worth knowing: MCP servers can expose many tools, and loading *all* their schemas into context up front is a real, documented failure mode (see progressive discovery below) — this is a common wrong-answer trap where "just add another MCP tool" is treated as free.
- **Direct API/CLI** is the right call for a single, tightly-scoped, one-off integration where you control both ends and don't need cross-context reusability — it's simpler and has less overhead than standing up an MCP server for something that will only ever be called from one place.
- **Agent-to-agent protocols** apply specifically when you have multiple *autonomous* agents (potentially from different vendors/systems) that need to negotiate, hand off tasks, or coordinate state with each other — not just "Claude calling a tool," but agent-to-agent communication as a first-class concern.

## 3. Auth/authz gap analysis

The exam pattern here is almost always: a scenario describes an integration, and you have to spot that it's using **broader access than the task needs**. Two recurring failure modes to watch for:
- **Over-privileged service accounts** — an integration authenticates once with a single powerful credential instead of respecting per-user permissions, meaning Claude (or a compromised prompt) could technically access data the requesting user shouldn't see.
- **Scope creep** — an integration requests read/write access when read-only would do, or access to an entire system when only one resource type is needed.
The right-answer pattern is almost always "narrow the scope to exactly what the task requires" — least privilege, applied to tool/data access design.

## 4. Accuracy-latency trade-offs

Same discipline as Domain 1's SLA scenarios, just localized to retrieval/integration choices: more retrieval sources, larger candidate sets, and re-ranking steps all improve accuracy but add latency and cost. The exam will give you a stated latency budget and ask you to pick the retrieval design that fits it — sometimes that means fewer sources, a lighter re-ranker, or caching frequent queries, rather than the "most thorough" pipeline.

## 5. Observability at scale

Uptime and latency dashboards (infrastructure health) are necessary but not sufficient — this is the same distinction from your loan-recommendation feedback-loop scenario, just applied to integrations specifically. Real observability for integrated systems means tracking: retrieval relevance (are the right chunks actually being returned?), tool call success/failure rates (is a specific tool silently failing or timing out?), and pinpointing *where* in a multi-step pipeline failures cluster — not just whether the system responded.

## 6. Progressive discovery vs. monolithic context

This is worth understanding concretely, because it's a real, measured phenomenon, not just theory. When you load *all* available tool definitions into context up front (monolithic), two things degrade as the tool count grows: token cost balloons, and — more importantly — **accuracy on tool selection drops**, because the model has to search through more irrelevant options to find the right one. Anthropic's own internal testing found that giving models fewer, more relevant tools (rather than more capable models) was what improved tool-selection accuracy — Opus 4 improved from 49% to 74% accuracy on tool selection benchmarks when lazy tool loading was implemented, and Opus 4.5 jumped from 79.5% to 88.1%.

Progressive discovery fixes this by giving Claude a lightweight way to *find* relevant tools/context on demand (e.g., a search-style lookup) instead of front-loading everything, loading full details only for what's actually relevant to the current task. Anthropic has documented token reductions in the tens-of-thousands-of-tokens range using this approach in production tool libraries. The exam-relevant takeaway: this is the Integration domain's version of the "too much competing for attention in one pass" principle you already know from Domain 1's decomposition material — just applied to *how much gets front-loaded into context* rather than how many LLM calls a task is split into.

---

