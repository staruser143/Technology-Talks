Yes. Let us go deeper on modular prompts and skills first


## Modular Prompts — deeper dive

### What it actually is

Instead of writing one long, hand-crafted prompt per use case, you break prompt content into separate, independently-maintained blocks that get composed together at request time: a base role/persona block, a domain-knowledge block, a task-instruction block, a formatting-rules block, an examples block. Different features or requests assemble different combinations of the same underlying blocks, rather than each feature owning its own fully-duplicated prompt text.

### Why this matters, distinct from caching

Caching is about *runtime efficiency* — reusing already-processed tokens across calls. Modular prompts are about *maintainability and consistency* at the authoring level — a completely different problem that happens to often show up alongside caching, but isn't the same thing. You could have a perfectly modular prompt system with terrible caching (blocks reassembled in a different order every time, breaking prefix matching), or a monolithic hand-written prompt that caches beautifully but is a nightmare to update consistently across 15 near-duplicate copies.

### The concrete problem modular prompts solve

Recall the exam trap named in the system-prompts material: "a system with many near-duplicate hand-written prompts for what's structurally the same task... the fix is usually consolidating into one parameterized template." Modular prompts are the generalized version of that fix — instead of one template per task, you maintain a *library* of reusable blocks, and different tasks compose different combinations. If your company's tone/brand-voice guidelines change, you edit the one shared "brand voice" block once, and every feature that includes that block picks up the change automatically — instead of hunting down and manually editing 12 separate hand-written prompts that each happened to restate the same guidelines slightly differently.

### The exam-relevant trade-off

Modularity adds a layer of composition complexity (you need some system for assembling the right blocks for the right context) in exchange for consistency and maintainability at scale. The judgment the exam wants: this is worth it when the same instructional content is genuinely reused across multiple features/prompts (the same "does this get reused across multiple consumers" question from your MCP-vs-direct-API framework in Domain 3, just applied to prompt content instead of tool integrations). For a single, one-off prompt used in exactly one place, full modularity is unnecessary overhead — the same "don't over-engineer for reuse that doesn't exist" principle.

---

## Skills — deeper dive, as a prompt reuse strategy specifically

### What it is, in this context

A Skill packages a reusable procedure, instruction set, or specialized knowledge once — as a discoverable unit Claude can load on demand — rather than that same content being duplicated into every system prompt that might need it, or (the opposite failure) permanently loaded into every context whether relevant or not.

### How Skills relate to modular prompts, and how they're different

Skills are a specific, more powerful *implementation* of the modular-prompt idea, with one key addition: **discoverability and on-demand loading**, not just composability. A modular prompt block still has to be manually selected and assembled by whoever's building the prompt/feature. A Skill can be *discovered by Claude itself* at runtime, based on relevance to the current task — this is exactly the progressive-discovery mechanism you already covered in Domain 3, applied specifically to reusable instructions rather than tools. Recall the concrete mechanism: only lightweight metadata (name + description) is loaded for all available Skills up front; Claude reads the full instructions only when a Skill becomes relevant to the task at hand.

### Why this matters as a *reuse* strategy specifically

The reuse benefit of Skills is twofold:
1. **Write once, use anywhere a relevant task shows up** — a "how to write a compliant financial disclosure" Skill, once built, doesn't need to be copy-pasted into every system prompt for every feature that might occasionally need it — any Claude instance with access to that Skill can discover and apply it when relevant.
2. **Doesn't cost context on tasks that don't need it** — unlike baking the same instructions permanently into a system prompt (which would cost tokens on every single request, including the ones that never touch that specific procedure), a Skill's full content is only loaded when actually relevant — directly avoiding the "too much competing for attention" and token-cost problems from monolithic context loading.

### The exam-relevant distinction to hold onto

- **Caching**: same content, reused across *time* (repeated calls), saving reprocessing cost.
- **Modular prompts**: same content, reused across *different features/prompts*, saving authoring/maintenance effort, assembled manually/deliberately per use case.
- **Skills**: same content, reused across *different tasks/contexts*, discovered and loaded *automatically* only when relevant, saving both authoring effort and context cost simultaneously.

These three solve genuinely different problems and are complementary, not competing — a well-designed system often uses all three together: a Skill's content, once loaded into a given conversation, still benefits from caching on subsequent turns in that same conversation, and a Skill itself might be internally composed of modular sub-blocks.

---

Want to try scenario practice now — including cases where the exam might test which reuse strategy actually fits a described situation?




