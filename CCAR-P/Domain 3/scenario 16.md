**Scenario**

A startup builds a Claude-powered assistant that helps their internal sales team by pulling data from their company's CRM. This is the only place the CRM is ever accessed by an AI system — no other team, application, or agent at the company needs this connection, and there are no plans to expose it more broadly. The engineering team is debating between building a direct API integration (writing specific calls to the CRM's REST API for the handful of operations the assistant needs — read contacts, read deals, read email history) versus standing up a full MCP server that wraps the CRM's entire API surface, reasoning that "MCP is the modern standard, so we should build it that way even for this internal tool."

**Question**: Which approach is more appropriate here, and why?

A) MCP — because MCP is the current best-practice standard for any AI-to-system integration, so it should be used by default regardless of the specific use case.

B) Direct API/CLI integration — because this is a single-purpose, single-consumer integration (one internal assistant, one CRM, no reuse or discovery need across other teams/agents), so the standardization and infrastructure overhead of building and maintaining a full MCP server isn't justified by any actual reuse benefit; a direct integration covering just the handful of operations needed is simpler to build and maintain.

C) Agent-to-agent protocol — because the sales assistant and the CRM are both complex systems, so they should communicate as peer agents.

D) Neither — the team should avoid integrating Claude with the CRM at all, and instead have salespeople manually copy data into the chat.

Take your best guess and I'll walk through it.






Correct — and this is the scenario built specifically to catch the "always pick the newest-sounding standard" trap named directly in the material.

**Why B is right**

Run through the decision framework in order:

1. **Is the other side a tool or a peer agent?** The CRM is a tool — it has an API, it does what it's told when called, it has no autonomy or independent decision-making. That already rules out agent-to-agent protocols.
2. **Will this connection be reused across multiple consumers/contexts, or is it single-purpose?** The scenario is explicit: "this is the only place the CRM is ever accessed by an AI system," "no other team, application, or agent," "no plans to expose it more broadly." That's about as clean a single-consumer, non-reusable case as you'll get. There is no discoverability benefit to capture here, because there's nothing else to discover it.

Given that, MCP's core value proposition — a standard interface so a capability built once can be picked up by many different consumers without rebuilding the integration each time — has no one to deliver that value *to*. You'd be paying the cost of standardization (building and maintaining an MCP server, wrapping the CRM's full API surface even though only three operations are actually used) for a benefit that structurally can't materialize, since reuse is exactly what's ruled out by the scenario. A direct API integration covering just read contacts, read deals, read email history is simpler to build, simpler to maintain, and matches the actual scope of what's needed — nothing more.

**Why the others are wrong**

- **A)** is the exact trap the material warns about by name: treating MCP as the default "correct" choice because it's the modern standard, independent of whether the actual reuse/discovery need that justifies it is present. This is the same pattern as reflexively decomposing a task that's already working well (your triage-email scenario) — applying a generally-good practice without checking whether its precondition actually holds in this specific case.
- **C)** misreads the CRM as something it isn't. A REST API that responds to CRUD-style calls has no autonomy, no independent reasoning, nothing to "negotiate" — agent-to-agent protocols solve a coordination problem between peers that simply doesn't exist here. This confuses "the CRM is a complex system" with "the CRM is an autonomous agent," which are unrelated properties — plenty of complex systems are still just tools from the perspective of this decision.
- **D)** avoids the actual question by removing Claude from the workflow entirely rather than choosing between the two legitimate integration options — this doesn't answer "which protocol," it sidesteps the need for one, and does so at real cost to the sales team's efficiency without any stated reason to believe the integration itself is unsafe or unwise (contrast this with the earlier CRM auth/authz scenario, where the actual problem was over-scoped credentials, not the existence of the integration).

**Pattern to remember**: MCP earns its overhead specifically when there's a real discoverability/reuse need across multiple consumers — multiple agents, multiple teams, or an intent to expose the capability more broadly later. When a scenario explicitly rules that out (single consumer, no reuse plans, narrow fixed scope), that's a direct signal toward the simpler direct-integration option, and "it's the modern standard" isn't sufficient justification on its own — the exam wants the choice tied to the actual reuse need stated in the scenario, not to which option sounds more sophisticated.

Want another protocol-selection scenario — maybe one where the correct answer flips toward MCP, or one testing the agent-to-agent boundary specifically?