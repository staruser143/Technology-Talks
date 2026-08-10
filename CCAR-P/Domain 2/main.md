## Domain 2: Claude Models, Prompting & Context Engineering (13%)

Five objectives here, and they cluster into two groups: **choosing the right model/prompt setup** (objectives 1-3) and **managing what's actually in the context window efficiently** (objectives 4-5, where Sample Question 2 lives).

### 1. Selecting appropriate Claude models based on trade-offs

The current lineup spans a capability/cost/speed spectrum — Haiku (fastest, cheapest, best for high-volume simple tasks), Sonnet (balanced, the default workhorse for most production use cases), and Opus (most capable, for tasks needing the deepest reasoning), plus the newer Mythos-tier models for specialized advanced use. The exam-relevant skill isn't memorizing which model is "best" — it's matching model choice to task complexity and cost/latency constraints, the same discipline as your Domain 3 "right-size the model per step" lever. A classification task doesn't need your most expensive model; a genuinely hard multi-step reasoning task might suffer noticeably on a lighter model. Expect scenarios where a design uses an oversized model for a simple task (wasteful) or an undersized model for a task needing real reasoning (quality risk) — you have to spot the mismatch.

### 2. Designing system prompts, templates, and guardrails

System prompts set persistent behavior, role, and constraints for every request. Templates make prompts reusable and consistent across many requests rather than hand-written each time. Guardrails are explicit instructions/constraints meant to keep outputs within acceptable bounds (tone, scope, refusal behavior, format compliance). The exam angle: a well-designed system prompt separates *stable* instructions (role, constraints, formatting rules) from *dynamic* content (the actual user request) — which, notably, sets up directly for the caching material below.

### 3. Prompt engineering techniques

- **Zero-shot**: just ask, no examples — fine for simple, well-understood tasks.
- **Few-shot**: provide examples of desired input/output pairs — helps when format or style needs to be precise and isn't obvious from instructions alone.
- **Chain-of-thought**: prompting the model to reason step-by-step before answering — improves performance on tasks requiring multi-step logic, at the cost of more output tokens (and therefore latency/cost — tying back to Domain 3's trade-off material).
The exam tests matching technique to task, not "more technique is always better" — few-shot examples add tokens and cost; use them when format precision genuinely needs it, not by default.

### 4. Optimizing context windows and managing token usage

This is where "lost in the middle" (which you already know from RAG) and general context bloat matter for prompting specifically — not just retrieved documents, but everything in the prompt: instructions, examples, conversation history. Managing this well means trimming what doesn't need to be there, summarizing/compacting long conversation history rather than carrying it verbatim forever, and being deliberate about what earns a place in context.

### 5. Prompt reuse strategies: caching, modular prompts, Skills

This is the exact mechanism behind **Sample Question 2** in the official guide, so it's worth being precise about how it actually works:

- **Prompt caching**: Anthropic's API can cache a stable prefix of your prompt (system instructions, tool definitions, reference documents) so repeated requests don't reprocess it from scratch — cached reads cost a fraction of normal input tokens (roughly 10%) and are meaningfully faster than full reprocessing, while cache writes cost a bit more than normal (roughly 1.25x) the first time. The critical design requirement: **stable content must come first, dynamic content last** — because caching works on a prefix match, anything that changes between requests (like a user's varying message) has to be positioned *after* everything reusable, or you break the match and lose the benefit entirely. This is precisely why Sample Question 2's correct answer is "place the static system prompt and policy before the dynamic content and enable caching" — reordering isn't cosmetic, it's what makes caching structurally possible.
- **Modular prompts**: breaking a large prompt into reusable, composable pieces (a base instruction block, a task-specific block, a formatting block) rather than one monolithic hand-written prompt per use case — easier to maintain and update consistently across many applications.
- **Skills**: reusable, on-demand instruction sets/procedures Claude can load only when relevant, rather than being permanently baked into every system prompt — this connects directly back to progressive discovery from Domain 3: don't front-load every possible instruction set into context, let Claude pull in what's relevant to the task at hand.

---

Want to jump into scenario practice for Domain 2 now?