**Scenario**

A company builds an internal analytics platform that wraps several capabilities: querying their data warehouse, generating charts from query results, and looking up metric definitions from their internal data dictionary. Initially, this was built as a one-off integration for a single internal Slack bot. Over the past year, though, three other teams have separately asked to connect their own Claude-powered tools to these same capabilities: the customer success team wants their support-ticket assistant to be able to pull usage metrics, the product team wants their roadmap-planning assistant to query feature-adoption data, and a new "ask-anything" company-wide assistant is being built that needs access to all of it plus a dozen other internal systems. Each of these teams would otherwise need to build and maintain their own bespoke integration to the data warehouse's query interface, duplicating the same connection logic three more times, and any future team wanting similar access would face the same duplication.

**Question**: Given this situation, is it time to move from the original single-purpose integration to MCP, and why?

A) No — since the original integration already works fine for the Slack bot, there's no reason to change it; each new team should just build their own separate direct API integration, since that keeps things simple and independent.

B) Yes — with multiple independent consumers (Slack bot, support assistant, roadmap assistant, company-wide assistant) now needing the same underlying capabilities, and more likely to appear in the future, this is exactly the reuse/discoverability scenario MCP is designed for: build the data warehouse query, chart generation, and metric-lookup capabilities once as an MCP server, and any MCP-compatible client (each team's assistant) can connect to it without rebuilding the integration from scratch.

C) Yes — but only because MCP is the newer technology and the team should always migrate to newer standards regardless of the specific reuse situation.

D) No — the right fix is to have all four teams share a single combined assistant instead of building separate tools, which avoids the integration question entirely.

Take your best guess and I'll walk through it.



Correct — and this scenario is the direct mirror of the CRM one, which is exactly the point: same decision framework, opposite conclusion, because the actual facts on the ground are opposite.

**Why B is right**

Run the same framework you just applied, and watch it flip:

1. **Is the other side a tool or a peer agent?** Still a tool — data warehouse queries, chart generation, metric lookups are all things that get called and return results, no autonomy involved. This step doesn't change the outcome here, same as last time.
2. **Will this connection be reused across multiple consumers/contexts, or is it single-purpose?** This is where the scenario is engineered to flip the CRM case entirely. Instead of "the only place this is ever accessed, no other team, no plans to expose it," you now have **four separate, independent consumers**, three of which appeared *after* the original build, plus an explicit signal that more are likely ("any future team wanting similar access would face the same duplication"). That's the precondition for MCP's value proposition actually being present: a capability built once, needed by many.

Given that, standardizing into an MCP server is exactly where the setup cost pays for itself — instead of four teams each writing and maintaining their own bespoke connection to the data warehouse's query interface (duplicated logic, duplicated maintenance burden, four places that need updating if the warehouse's interface changes), you build the capability once and let every MCP-compatible consumer connect to it without rebuilding the integration. This is the reuse/discoverability need the material describes directly — "you want a capability to be discoverable and reusable across multiple contexts... rather than wired into one specific application."

Worth flagging, since it connects back to the earlier context-management material: with potentially a dozen-plus systems eventually behind this MCP server (the "ask-anything" assistant alone mentions needing "a dozen other internal systems"), this is also exactly the situation where progressive tool discovery matters — a well-built MCP integration here shouldn't dump every tool schema from every connected system into every consumer's context by default; it should let each consuming assistant discover and pull in only what it actually needs per task. Choosing MCP correctly here doesn't automatically mean the implementation is complete — the scale consideration from the observability/context material still applies.

**Why the others are wrong**

- **A)** freezes the original decision even though the facts that justified it (single consumer, no reuse need) no longer hold. This is the same mistake as the six-step legal-contract-review scenario from your decomposition set, just inverted: there, evidence showed a merge was warranted and the team should update a past decision; here, evidence shows a *split into shared infrastructure* is warranted, and refusing to revisit the original single-purpose build ignores that the situation has materially changed. It also actively recommends the CRM scenario's correct answer applied to the wrong situation — direct API integration, repeated four separate times, is precisely the duplicated-maintenance-burden outcome the scenario describes as the cost of *not* moving to MCP.
- **C)** reaches the right conclusion for the wrong reason, and that distinction matters on this exam. "MCP is newer, so migrate" is the same reflexive-default trap flagged in the CRM scenario — it just happens to land on the correct answer here by coincidence, because this scenario's actual reuse need genuinely justifies MCP. If you're picking MCP because it's new rather than because multiple real consumers need shared discoverable access, you'll get the CRM scenario wrong for the same reasoning that gets this one "right" — the exam is testing the reasoning process, not just pattern-matching to "MCP good."
- **D)** avoids the actual integration-architecture question by proposing an organizational merger of four different tools with different purposes (a Slack bot, a support assistant, a roadmap assistant, a company-wide assistant) into one — these serve different teams with different workflows for good reasons, and collapsing them isn't a real substitute for solving the underlying "how should multiple consumers share one capability" problem; even a single combined assistant would still need a well-architected way to access the data warehouse, chart generation, and metric lookup capabilities, which is exactly the question MCP answers.

**Pattern to remember, now complete across both scenarios**: the correct protocol choice isn't a property of the technology itself (MCP isn't "better" or "worse" in the abstract) — it's a property of the *situation*: number of independent consumers, present and reasonably anticipated, and whether standardized discoverability actually saves real duplicated effort. Same framework, applied honestly to the facts given, correctly produces "direct API" in one scenario and "MCP" in the near-identical-looking next one. The exam is testing whether you apply the reasoning consistently, not whether you have a favorite answer.

Want another protocol-selection scenario — maybe one testing the agent-to-agent boundary specifically, or should we do a mixed review across all of Domain 3's sub-topics now that you've covered RAG, auth/authz, observability, and protocol selection?