## Optimizing Context Windows and Managing Token Usage

This is the sub-topic that ties together threads you've already built in Domain 3 (lost-in-the-middle, progressive discovery) and applies them more broadly to overall context management — not just retrieved documents or tools, but everything competing for space in a request: instructions, conversation history, examples, and intermediate outputs.

### 1. Context window budget is a shared, finite resource

Every category of content in a prompt draws from the same pool: system instructions, few-shot examples, retrieved documents, tool definitions, conversation history, and the current turn's content. The exam wants you to think of context management as a *budgeting* problem — not "does this fit" (a large context window makes almost anything technically fit) but "does including this actively help, or is it diluting attention and adding cost for marginal or no benefit." This is the direct generalization of "lost in the middle" (Domain 3) beyond just RAG chunks — it applies to conversation history, examples, anything.

### 2. Conversation history management

In a multi-turn conversation, naively carrying the full transcript forward on every turn means token cost (and dilution risk) grows unboundedly as the conversation lengthens. Key techniques:
- **Summarization/compaction**: periodically condense older turns into a shorter summary that preserves what's still relevant, rather than carrying verbatim history forever.
- **Sliding window**: keep only the most recent N turns in full, dropping or summarizing older ones.
- **Selective retention**: keep what's still relevant to the current task, discard what's now irrelevant (e.g., a resolved sub-question from earlier in a long support conversation).

The exam-relevant judgment: over-aggressive trimming loses genuinely needed context (a user's stated constraint from 10 turns ago that's still relevant); under-aggressive trimming re-introduces the cost and dilution problems of monolithic context. This is the same trade-off shape as decomposition and progressive discovery — evidence-based tuning, not a fixed rule.

### 3. Token counting as a design input, not an afterthought

Treating token budget as something you discover only when a request fails or costs more than expected is a reactive anti-pattern. The material's framing: token efficiency is "an engineering discipline... measured at build-time," not a post-hoc surprise. This means actually counting tokens for your specific prompts/model (not estimating with a generic rule of thumb) as part of designing a system, especially one with cost or latency constraints.

### 4. Trimming what doesn't need to be there

- **Tool output truncation**: a tool call that returns a large payload (e.g., a full API response with dozens of fields) often only needs a handful of those fields for the task — passing the full raw payload into context wastes tokens on data the model will never use.
- **Redundant context**: the same information supplied twice (e.g., in both a system prompt and repeated in a retrieved document) — trim to one authoritative copy.
- **Over-verbose instructions**: guardrail/instruction bloat (which you already covered) is itself a context-budget problem as much as a "competing instructions" problem — every unnecessary instruction token is also just unnecessary token cost.

### 5. How this sub-topic connects to everything else in Domain 2

- This is genuinely the capstone of the domain: model selection interacts with context budget (a model's usable context varies, and processing cost scales with tokens regardless of tier);
- caching only helps the *stable* portion of context, so managing what's dynamic vs. reusable is itself a context-optimization decision;
- few-shot examples and chain-of-thought both directly cost context budget, so technique selection and context optimization are the same lever viewed from different angles — every added example or reasoning instruction is tokens spent, and worth it only if it earns its cost.

---

Want to move into scenario practice for this sub-topic now?
