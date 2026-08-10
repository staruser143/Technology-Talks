## Protocol Selection: MCP vs. API/CLI vs. Agent-to-Agent

This sub-topic is about picking the right integration *mechanism*, not just deciding *whether* to integrate with something. Let's go deeper than the initial overview.

### 1. Direct API/CLI integration

This is the simplest option: Claude (or your application code) calls a specific system's API or CLI directly, with a bespoke integration built for that one connection. Characteristics:
- **Tightest coupling, least overhead.** No intermediary standard to learn or maintain — you write exactly the calls you need.
- **Best when**: the integration is single-purpose, used by one application, unlikely to be reused elsewhere, and you fully control both ends (or at least the calling side).
- **Downside**: doesn't scale well if you need the *same* underlying capability available to multiple different agents, apps, or teams — each new consumer means rebuilding the integration from scratch, since there's no shared discovery/interface layer.

### 2. MCP (Model Context Protocol)

MCP standardizes *how* a model discovers and calls external tools and data sources — think of it as a common interface contract, so a tool built once (an MCP server) can be connected to by any MCP-compatible client without custom integration work each time.
- **Best when**: you want a capability to be **discoverable and reusable** across multiple contexts — different agents, different applications, different teams, possibly different organizations — rather than wired into one specific application.
- **Key nuance from the observability/context material you already covered**: MCP servers can expose many tools, and naively loading *all* their schemas into every context is a real, measured failure mode (recall: lazy/progressive tool loading improved Opus 4's tool-selection accuracy from 49% to 74%). So "we should use MCP because it's the standard" is not automatically the complete answer — a good MCP-based design also needs to account for how tool discovery scales as the number of connected servers grows.
- **Downside**: more setup and infrastructure than a direct API call for a single, one-off integration — standardization has a cost when there's nothing to standardize *for* (i.e., only one consumer, ever).

### 3. Agent-to-agent protocols

This is a different category from the first two — it's not about Claude calling a tool, it's about **multiple autonomous agents coordinating with each other**, potentially across organizational or vendor boundaries.
- **Best when**: you have genuinely autonomous agents (not just tools) that need to negotiate, hand off tasks, share state, or coordinate goals with each other — e.g., a travel-booking agent from one company needing to coordinate with a payments agent from another company, each making independent decisions.
- **Distinguishing question**: is the other side of this integration a *tool* (does something specific when called, has no autonomy of its own) or another *agent* (has its own reasoning, makes its own decisions, may need to communicate intent/state back and forth)? If it's a tool, you don't need agent-to-agent protocols even if the tool is sophisticated — MCP or direct API still applies. Agent-to-agent protocols are specifically for peer autonomous systems.

### 4. The exam-relevant decision framework

Ask, roughly in this order:
1. **Is the other side a tool or a peer autonomous agent?** → routes you toward MCP/API vs. agent-to-agent.
2. **If it's a tool: will this connection be reused across multiple consumers/contexts, or is it single-purpose?** → reusable/discoverable need points to MCP; single, tightly-scoped, one-off need points to direct API/CLI.
3. **If MCP: have you accounted for tool-discovery scale?** (progressive disclosure, not dumping every schema into every context) — this is where the Integration domain's sub-topics start reinforcing each other; picking MCP correctly still requires applying the context-management lesson from earlier.

A common exam trap: treating MCP as *always* the "modern, correct" choice regardless of context, when a simple, single-purpose, non-reusable integration is sometimes genuinely better served by a direct API call — added standardization has a real cost, and the exam wants you to justify the choice based on the actual reuse/discovery need, not default to "use the newest-sounding option."

---

Want to jump into scenario practice for this sub-topic now?