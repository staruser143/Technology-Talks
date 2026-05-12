## Model Context Protocol (MCP)

**MCP (Model Context Protocol)** is an open standard developed by Anthropic that defines how AI models communicate with external tools, data sources, and services. Think of it as a universal "plug-in standard" for AI — similar to how USB-C standardized device connections.

### The Core Idea

Before MCP, every AI integration was custom-built: if you wanted Claude to read your Google Drive, query a database, or call an API, someone had to write bespoke glue code for each connection. MCP replaces that chaos with a single, consistent protocol.

**Three components make up an MCP connection:**

- **Host** — the AI app (e.g., Claude.ai, Claude Code)
- **Client** — manages the connection on the AI side
- **Server** — a lightweight service that exposes tools, data, or prompts to the AI

### How It Relates to Agentic AI

MCP is essentially the *infrastructure layer* that makes agentic AI practical. Here's why:

**Agents need to act, not just respond.** An agentic AI doesn't just answer questions — it takes multi-step actions: browsing the web, writing files, calling APIs, managing calendars. MCP gives agents a standardized way to do all of this.

| Agentic Capability | How MCP Enables It |
|---|---|
| Using tools | MCP servers expose callable tools the model can invoke |
| Accessing live data | MCP connects to databases, APIs, file systems in real time |
| Multi-agent workflows | Agents can hand off tasks to other agents via MCP |
| Memory & persistence | External memory stores are accessible as MCP resources |
| Taking real-world actions | Any service with an MCP server becomes actionable |

### Why It Matters

1. **Composability** — agents can chain together many MCP servers to accomplish complex goals
2. **Security** — each server defines exactly what it exposes, limiting blast radius
3. **Ecosystem** — thousands of pre-built MCP servers exist (GitHub, Slack, Postgres, Stripe, etc.), so agents gain capabilities instantly
4. **Interoperability** — MCP is model-agnostic; other AI systems are adopting it too

### A Simple Analogy

If an AI agent is a contractor, MCP is the standardized toolkit belt. Without it, the contractor has to custom-forge every tool from scratch. With it, they can pick up any compatible tool and get to work immediately.

MCP is what's quietly making the shift from "AI that talks" to "AI that *does*" possible at scale.