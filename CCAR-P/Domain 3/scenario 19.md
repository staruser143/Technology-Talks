**Scenario**

An enterprise software company builds a company-wide Claude-powered assistant that connects to 40 different internal MCP servers — one each for HR systems, finance tools, project management, code repositories, customer databases, and dozens of smaller internal tools, each server exposing anywhere from 3 to 15 tools. The engineering team's initial implementation loads every tool definition from every connected server into context at the start of every single conversation, reasoning that "Claude should always know everything it has access to, so it can decide what's relevant." After launch, users report that the assistant frequently calls the wrong tool for simple requests (e.g., using a finance-reporting tool when asked a basic HR question), response latency is noticeably slow even for simple queries, and token costs are far higher than projected — even for conversations that only ever end up using one or two tools.

**Question**: What's the core issue, and what should the team do?

A) The issue is that 40 MCP servers is simply too many to integrate with; the team should cut the number of connected servers down to fewer than 10, sacrificing overall functionality to fix the performance problem.

B) The core issue is monolithic context loading — every tool definition from all 40 servers is loaded into every conversation regardless of relevance, which directly causes the observed symptoms: more tools competing for attention degrades tool-selection accuracy (explaining the wrong-tool-called errors), and loading hundreds of tool schemas upfront adds unnecessary latency and token cost even when only one or two tools end up being used. The fix is progressive discovery — expose a lightweight way for Claude to discover relevant tools/servers based on the current task (e.g., a search-style lookup over tool names/descriptions) and only load full tool schemas for what's actually relevant, rather than front-loading everything for every conversation.

C) The issue is that Claude's model isn't capable enough to handle 40 servers' worth of tools; upgrading to a more capable model would resolve the wrong-tool-selection problem without needing to change the context-loading approach.

D) There's no real issue — some tool-selection errors and added latency are an expected and acceptable cost of giving Claude broad access to enterprise systems, and users should just be more specific in their requests to help Claude pick the right tool.

Take your best guess and I'll walk through it.




Correct — and this scenario is essentially a direct dramatization of the measured evidence from the material, just spelled out as symptoms instead of benchmark numbers.

**Why B is right**

Match each symptom to its cause, since that's the actual skill being tested:

- **"Frequently calls the wrong tool for simple requests"** — this is the tool-selection accuracy degradation named directly in the material: the model isn't getting *dumber*, it's being asked to search through a much larger, mostly-irrelevant set of options for every single request. Recall the concrete evidence: Opus 4 improved from 49% to 74% accuracy on tool selection benchmarks, and Opus 4.5 jumped from 79.5% to 88.1%, purely from showing the model fewer, more relevant tools rather than a better model. This scenario is the inverse of that finding — a company running the *unfixed* version of exactly what that testing was measuring.
- **"Response latency is noticeably slow even for simple queries"** — processing hundreds of tool schemas costs real generation time on every single request, including the trivial ones. A simple HR question shouldn't require reasoning over finance, code-repo, and customer-database tool definitions it will never touch — but under monolithic loading, it has to anyway.
- **"Token costs far higher than projected, even for conversations using one or two tools"** — this is the token-cost side of the same root cause. This maps directly to the concrete figure from the material: full MCP tool library loading consumed 77,000 tokens versus 8,700 tokens with on-demand tool discovery — an 85% reduction. The company here is paying the 77,000-token version of that trade for every conversation, regardless of how few tools actually get used.

The fix — progressive discovery, giving Claude a lightweight way to find relevant tools/servers per task rather than loading all 40 servers' worth of schemas every time — directly targets the actual cause (context overload), not any of the symptoms individually. This is the same "fewer, more relevant tools" principle in action: narrowing what's in context *before* reasoning begins is what actually restores tool-selection accuracy, rather than trying to compensate for an overloaded context after the fact.

**Why the others are wrong**

- **A)** treats server count as the problem rather than *how* those servers' tools get loaded into context. Cutting genuinely useful integrations to fix a context-management problem sacrifices real capability (HR, finance, code, customer data — all presumably valuable to different users at different times) to work around a fixable architectural issue, the same overcorrection pattern flagged repeatedly across this domain — discard functionality instead of fixing the actual mechanism.
- **C)** misattributes the cause to model capability rather than context design. The measured evidence directly contradicts this: the improvement in tool-selection accuracy came from *reducing what the model has to search through*, not from using a more capable model — the material is explicit that this was true even holding model capability fixed (both Opus 4 and Opus 4.5 improved from the same intervention). A more capable model reasoning over the same bloated, mostly-irrelevant 40-server context would likely still show degraded selection accuracy relative to a well-scoped context, just perhaps less severely.
- **D)** normalizes a fixable, well-documented failure mode as an acceptable cost, and shifts the burden onto users ("be more specific") rather than fixing the system. This is the same "argue with a fixable constraint instead of solving it" pattern from earlier domains — asking users to compensate for a context-management design flaw isn't a real fix, especially when the actual fix (progressive discovery) is a known, effective, well-evidenced pattern rather than a hard trade-off with no good answer.

**Pattern to remember, closing out the Integration domain's context-management thread**: when a system connects to many tools/servers/capabilities and shows the *combination* of wrong-tool-selection errors, unexplained latency on simple requests, and token costs disproportionate to actual usage, that specific symptom cluster points to monolithic context loading — not model capability, not "too many integrations to be useful," and not user behavior. The fix is architectural: discover, then load — never load everything, always.

That completes solid coverage of all six Integration sub-topics — RAG design, accuracy-latency, auth/authz, observability, protocol selection, and progressive discovery. Want a mixed review scenario pulling from across all of Domain 3, or should we move to Domain 4 (Evaluation, Testing & Optimisation, 16%)?