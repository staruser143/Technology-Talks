**Scenario**

A retail company's customer service Claude agent started 18 months ago with three tools: look up order status, check return eligibility, and check shipping estimates — all built for the "help customers with existing orders" use case. Over time, as different product teams wanted to use the same agent for their own initiatives, more tools were added: apply promotional discount codes (for a marketing campaign), update customer loyalty tier (for a loyalty program revamp), modify subscription billing frequency (for a subscription product launch), and generate personalized product recommendations (for a merchandising experiment). The agent now has 9 tools total.

A quarterly review of tool-call logs shows: the original three order-related tools account for 94% of all tool invocations across all customer conversations. The four newer tools combined account for roughly 4% of invocations, and two of them ("modify subscription billing frequency" and "generate personalized product recommendations") haven't been called even once in the last two months, because the subscription launch and merchandising experiment they were built for have both quietly wound down. Each of the four newer tools also has read/write access scoped appropriately for its own function — none of them are individually over-privileged.

**Question**: What's the core issue here, and what should the team do?

A) There's no real issue — since each individual tool is appropriately scoped and none is over-privileged, the auth/authz concerns are fully addressed, so the current configuration is fine as-is.

B) This is capability bloat: the agent's configuration has accumulated tools spanning multiple unrelated job functions (order support, promotions, loyalty, subscriptions, merchandising) added incrementally for different initiatives, most of which see negligible or zero actual use. Even though no individual tool is over-privileged, the aggregate footprint exceeds what the agent's core, high-usage purpose (order support) actually needs. The team should audit usage data (which they already have), remove or retire genuinely unused/orphaned tools (especially the two with zero recent calls tied to wound-down initiatives), and reconsider whether the remaining low-usage tools belong on this agent at all versus a separate, purpose-scoped agent.

C) The issue is that the agent needs even more tools to fully serve all use cases; the fix is to add dedicated tools for every remaining product team's use case so the agent becomes a true one-stop assistant.

D) The issue is entirely a progressive discovery problem; simply implementing lazy tool loading for the existing 9 tools fully resolves the situation without needing to change which tools are actually configured.

Take your best guess and I'll walk through it.



Correct — and this scenario is a clean, close-to-textbook case, deliberately built to isolate capability bloat from the two things it's easy to confuse it with: auth/authz scope issues and the progressive-discovery mechanism.

**Why B is right**

The usage data is doing the diagnostic work here, the same way it did in your six-step legal-contract decomposition scenario — measured evidence, not intuition, is what tells you where the problem actually is:

- **94% of invocations on 3 of 9 tools** tells you the agent's *actual, exercised* purpose is order support — that's what it's really being used for, regardless of what else got bolted on.
- **Two tools with zero calls in two months, tied to initiatives that have "quietly wound down"** — this is the sharpest signal in the scenario. These aren't underused, they're orphaned: built for a purpose that no longer exists, sitting on the agent's configuration anyway. This is exactly the "each tool was added to solve a real problem at the time" accumulation pattern — every addition was individually reasonable when made, but nobody came back to remove the ones whose reason evaporated.
- **The remaining two low-usage tools (4% combined) spanning promotions and loyalty** — still active, still occasionally used, but represent a different question: do these belong on the *same* agent as order support, or would the agent (and its evals, and its blast radius) be cleaner if these were split into a separate, purpose-scoped agent or gated more deliberately?

The fix follows the same audit discipline from the material: measure usage against granted capability, distinguish "unused because genuinely unneeded" from "occasionally used for a real but different purpose," and act on each differently — retire the orphaned tools outright, and reconsider whether the still-active-but-low-usage ones warrant staying on this agent or moving to a narrower one.

**Why the others are wrong**

- **A)** makes exactly the mistake the scenario is built to catch — conflating "no individual tool is over-privileged" (an auth/authz question, already ruled out here) with "the aggregate tool set is appropriately scoped" (a capability bloat question, which is a completely separate axis). You worked through this same distinction explicitly in your earlier hospital audit-logging scenario: passing on one axis doesn't mean the design passes on every axis. Here, every tool being individually well-scoped tells you nothing about whether nine tools spanning five unrelated business functions belong on one agent.
- **C)** moves in exactly the wrong direction — treating an already-bloated agent's problem as "not enough coverage yet" and proposing to add more unrelated capabilities on top of a configuration that's already carrying orphaned, unused tools. This compounds the actual problem (too much unjustified capability) by adding more of the same thing that caused it.
- **D)** correctly identifies a *related* sub-topic but misapplies it as if it were a substitute fix. Progressive discovery would reduce the context-cost pain of having 9 tools (less schema loaded per conversation), but it doesn't address why two of those tools exist for initiatives that no longer exist, or whether unrelated business functions should be split across separate agents. This is the distinction from the last message made concrete: an agent can have great progressive-loading mechanics and still be bloated underneath — the loading strategy makes the bloat cheaper to carry, it doesn't remove the bloat itself. The orphaned tools still represent unjustified capability and unnecessary blast radius regardless of how efficiently they're loaded into context.

**Pattern to remember**: capability bloat is diagnosed by measuring actual usage against configured capability, and the fix targets the *configuration* (remove, retire, split into purpose-scoped agents) — not the loading mechanism (which is a separate, complementary fix for cost/accuracy, not a substitute for right-sizing what's actually granted). When a scenario gives you usage-frequency data alongside a growing, incrementally-accumulated tool list, especially with a tool tied to a wound-down initiative, that's the signal pointing at this sub-topic specifically.

That completes all seven Integration sub-topics now, correctly aligned with the official blueprint. Want another capability bloat scenario — maybe one testing the "when does bloat justify splitting into multiple agents" boundary — or should we do a mixed review across all of Domain 3, or move to Domain 4 (Evaluation, Testing & Optimisation)?