You're right — I folded it into the MCP discussion and observability notes but never gave it the standalone treatment the way I did for the other five. Let's fix that.

## Progressive Discovery vs. Monolithic Context

### The core problem this addresses

Every token of context Claude has to hold — tool definitions, retrieved documents, prior conversation, instructions — is competing for the same finite attention. You've already seen two flavors of this principle: decomposition (Domain 1, too many *tasks* in one pass) and lost-in-the-middle (RAG, too much *retrieved content* in one pass). Progressive discovery is the same underlying issue applied to a third case: **too many available *tools/capabilities* loaded into context at once**, whether or not they're ever used.

### Monolithic context, defined

The naive approach: load every tool definition, every skill, every piece of potentially-relevant context up front, for every request, regardless of whether that specific task needs it. This is simple to build and reason about — nothing has to be discovered dynamically, it's all just... there.

The problem, backed by real measured evidence you already saw: Anthropic's internal testing found Opus 4 improved from 49% to 74% accuracy on tool selection benchmarks, and Opus 4.5 jumped from 79.5% to 88.1%, when lazy tool loading was implemented instead of loading everything upfront — the improvement came specifically from showing the model fewer, more relevant tools, not from a better model. And this compounds badly at enterprise scale: connecting to a GitHub server, a Jira server, and other services each contributing their own tool definitions means the context window fills, accuracy drops, latency increases, and costs climb.

### Progressive discovery, defined

Instead of front-loading everything, Claude gets a lightweight way to *discover* what's available — often just names/short descriptions at first — and only loads full detail (a tool's full schema, a document's full content, a skill's full instructions) for the specific things relevant to the current task. The filesystem-based Skills model is a concrete real example of this: at startup, only the metadata (name and description) from all Skills is pre-loaded, and Claude reads the full SKILL.md only when the Skill becomes relevant, reading additional files only as needed.

There's an even more aggressive version of this worth knowing for the exam: instead of the model directly invoking dozens of individual tool calls, it can write code that discovers and composes the needed capabilities programmatically, with only the final result returned to the conversation. Anthropic's tool search feature shows this concretely — using a full MCP tool library with traditional context loading consumed 77,000 tokens, while discovering tools on demand dropped that to 8,700 tokens, an 85% reduction, with accuracy improving as well.

### The trade-off (yes, there is one)

Progressive discovery isn't free — it adds a discovery/lookup step (latency, and a small risk the model doesn't find the right tool if descriptions are poorly written) in exchange for a smaller, more relevant context. The exam-relevant judgment: this trade almost always favors progressive discovery once the number of available tools/capabilities grows past a small handful, because the accuracy cost of an overloaded context tends to outweigh the modest latency cost of a discovery step. For a system with only 2-3 tools total, monolithic loading is fine — there's nothing meaningful to discover.

### How this connects to the rest of Domain 3

This sub-topic is really the context-management lens applied across the other five: it's why "just use MCP because it's the standard" isn't a complete answer (an MCP server exposing many tools needs progressive discovery to scale), it's a specific instance of the observability concern (context bloat degrading accuracy is a silent-failure-adjacent problem — nothing errors, it just gets worse), and it echoes the same instinct as RAG's "more retrieved context isn't always better."

---

Want to try scenario practice on this now?