## Capability Bloat — deeper dive

### What it actually is

An agent accumulates tools, integrations, or capabilities over time — often incrementally, each addition individually reasonable — until its configuration includes far more than any single task actually exercises. Unlike auth/authz scope creep (which is specifically about *permission breadth* — read vs. write, single-resource vs. system-wide), capability bloat is about the *number and variety of distinct tools/capabilities* an agent carries, regardless of whether any individual one is dangerous on its own. You can have a bloated agent made entirely of read-only, low-risk tools — the problem isn't that any one tool is risky, it's that the aggregate footprint has grown past what the agent's actual job requires.

### How bloat typically accumulates

This almost never happens in one bad decision — it creeps in the same way the six-step legal-contract pipeline in your earlier decomposition scenario grew an extra joint: each tool was added to solve a real, specific problem at the time. A support agent gets a "look up order status" tool, then later a "check inventory" tool for a different feature, then a "process returns" tool for yet another feature, then a "send promotional email" tool because someone thought it might be handy — and eighteen months later, one agent is carrying a dozen tools spanning several different job functions, most of which any given conversation never touches.

### Why it's a problem even beyond the progressive-discovery/context-cost angle

You already know the context-cost consequence (more tools competing for attention degrades tool-selection accuracy, per the Opus 4/4.5 evidence). Capability bloat has additional costs on top of that:

- **Larger attack/error surface.** Every additional tool is another way the agent's behavior could go wrong — a misinterpreted instruction, a prompt injection in content the agent reads, or a bug could trigger a tool that has nothing to do with the task at hand, simply because it's available. This is the same "blast radius" logic from auth/authz, now applied to capability count rather than permission breadth.
- **Harder to test and evaluate.** More tools means more possible interaction paths and edge cases to cover in eval suites — bloat makes the Evaluation domain's job harder too.
- **Harder to reason about and maintain.** An agent whose configuration mixes several unrelated job functions is harder for a human to audit, review, or predict — "what could this agent possibly do" becomes a much longer, murkier answer than it needs to be for the agent's actual purpose.
- **Diffuses ownership and accountability.** A single "do everything" agent with tools spanning several teams' domains makes it unclear who's responsible for reviewing or updating any particular capability.

### How to evaluate/audit for it (the exam-relevant skill)

The core method mirrors what you already applied in the six-step legal-contract-review decomposition scenario, just aimed at tools instead of pipeline steps: **measure actual usage against granted capability.**
- What fraction of an agent's tools are actually invoked across real traffic? A tool that's never or rarely called is a bloat candidate.
- Do the agent's tools span multiple distinct job functions/domains, or one coherent purpose? Multiple unrelated functions on one agent is often a sign it should be decomposed into narrower agents (tying back to Domain 1's decomposition and multi-agent material) rather than kept as one broad one.
- Was each tool added because of a *current, active* need, or a speculative "might be useful later" one? This is the same unjustified-scope-creep smell from your CRM auth scenario, just applied to tool inventory instead of OAuth scopes.

### The fix

Not necessarily "remove all unused tools reflexively" (same caution as the decomposition-boundary lesson — verify with evidence, don't apply a blanket rule) — but audit periodically, remove or split off genuinely unused/unjustified capabilities, and consider whether a bloated single agent should be decomposed into narrower, purpose-scoped agents (or tools gated behind progressive discovery, or both — the two sub-topics are complementary fixes, not substitutes for each other).

---

Want to try scenario practice on capability bloat now?




