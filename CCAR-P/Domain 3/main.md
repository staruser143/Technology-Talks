## Domain 3: Integration (19% — the largest domain)

This is the biggest chunk of the exam, and it's where "does this person actually know how to wire Claude into a real system" gets tested most heavily. It splits into a few core areas:

### 1. RAG pipeline design
Retrieval-Augmented Generation means giving Claude access to external knowledge it wasn't trained on, by retrieving relevant chunks and injecting them into context. Key decisions the exam probes:
- **Chunking strategy** — how you split documents (by fixed size, by semantic boundary, by document structure like headers) directly affects retrieval quality. Too small and you lose context; too large and you dilute relevance and waste tokens.
- **Indexing/embedding choice** — how chunks get turned into searchable vectors, and whether metadata filtering (date, source, category) is layered on top of pure semantic search.
- **Retrieval strategy** — pure vector similarity vs. hybrid search (vector + keyword/BM25) vs. re-ranking a larger candidate set down to the best few. Hybrid and re-ranking tend to win in real systems because pure vector search misses exact-match cases (product codes, names) that keyword search catches easily.

### 2. Protocol selection: MCP vs. API/CLI vs. agent-to-agent
Expect scenario questions asking you to pick the right integration mechanism:
- **MCP (Model Context Protocol)** — standardizes how Claude discovers and calls external tools/data sources, especially useful when you want reusable, discoverable connections across multiple contexts (not just one bespoke integration).
- **Direct API/CLI calls** — simpler, tighter, appropriate when you're integrating with one specific system and don't need the discovery/standardization benefits MCP provides.
- **Agent-to-agent protocols** — relevant when multiple autonomous agents (possibly from different systems/vendors) need to communicate and coordinate, not just when Claude calls a single tool.

### 3. Auth/authz gap analysis
Integration questions test whether you can spot missing pieces in an access model: does the integration respect per-user permissions, or does it use a single over-privileged service account for everything? Scope creep (an integration technically *could* access more than the task needs) is a common wrong-answer bait in these scenarios.

### 4. Accuracy-latency trade-offs
This is the Integration-domain cousin of the business-value-pillars material from Domain 1: more retrieval sources, more re-ranking, more candidate chunks generally improve accuracy but cost latency. Expect scenarios where you have to pick a design that fits a stated latency budget, same discipline as your loan-scheduling and logistics scenarios.

### 5. Observability at scale
Once integrations are in production, you need visibility into retrieval quality, tool call success/failure rates, and where in a pipeline things break — not just whether the system is "up."

### 6. Progressive discovery vs. monolithic context
This is a nuanced one: instead of stuffing everything an agent *might* need into context up front (monolithic), progressive discovery means giving Claude lightweight ways to discover and pull in more context/tools only as needed. This keeps context lean and reduces the "too much competing for attention" problem you already know well from decomposition — same underlying principle, applied to how much you front-load into a single context window versus letting the model pull in what it needs on demand.

---

