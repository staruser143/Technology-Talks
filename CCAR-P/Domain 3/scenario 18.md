**Scenario**

A logistics company's Claude-powered shipping coordinator needs to work with a freight carrier's own separate AI system to arrange complex multi-leg shipments. The carrier's AI system independently decides routing, can propose alternative shipment terms if the original request doesn't fit their available capacity, can counter-propose pricing, and may need to check back-and-forth multiple times as conditions change (a truck breaks down, a route becomes available) — all without either side's system being under the other's control. Neither company controls the other's system, and each side's AI is making genuinely independent decisions that need to be negotiated, not just requested.

Separately, that same shipping coordinator also needs to call a weather-data provider to check forecasts along shipping routes. This weather service is sophisticated — it runs complex internal modeling — but from the coordinator's perspective, it simply takes a location and date range as input and returns a forecast; it doesn't negotiate, propose alternatives, or make independent decisions about anything.

An engineer proposes using an agent-to-agent protocol for **both** integrations, reasoning that "both external systems are AI-powered and sophisticated, so both deserve the same treatment."

**Question**: Is the engineer's reasoning correct? Which integration(s) actually call for an agent-to-agent protocol?

A) Yes — since both the freight carrier's system and the weather service are AI-powered, both should use agent-to-agent protocols to future-proof the integrations.

B) Only the freight carrier integration calls for an agent-to-agent protocol — it involves two independent, autonomous decision-makers (neither controls the other) that need to negotiate, counter-propose, and coordinate over multiple exchanges as conditions change. The weather service, despite being "AI-powered" and sophisticated internally, is functionally a tool from the coordinator's perspective — it's called with input, returns output, and has no autonomy or negotiation involved; it should use a direct API or MCP integration, not agent-to-agent.

C) Neither needs agent-to-agent — since Claude is the one initiating all these requests, everything is fundamentally a tool call regardless of what's on the other end.

D) Only the weather service calls for an agent-to-agent protocol, since accurate weather forecasting requires more sophisticated coordination than logistics negotiation.

Take your best guess and I'll walk through it, or ask me to explain any option first.




Correct — and this scenario isolates the exact distinguishing question named in the material: is the other side a tool or a peer autonomous agent? "Being AI-powered" turns out to be a red herring, and that's deliberate.

**Why B is right**

Walk through what actually distinguishes the two integrations, since the surface similarity ("both are AI-powered and sophisticated") is precisely what the scenario wants you to look past:

- **Freight carrier system**: independently decides routing, can propose alternative terms unprompted, can counter-propose pricing, needs multi-round back-and-forth as conditions change, and — critically — **neither side controls the other**. This is the actual definition of peer autonomy from the material: genuinely autonomous agents that need to negotiate, hand off, or coordinate goals with each other, potentially across organizational boundaries (two different companies here, which is exactly the kind of cross-boundary case the material calls out). The interaction pattern isn't "call this, get a fixed answer back" — it's an evolving negotiation where either side might change its position based on the other's response.
- **Weather service**: "sophisticated internal modeling" describes *how* it produces its output, not *how it behaves as an integration partner*. From the coordinator's side, the interaction is entirely: give location and date range, receive forecast. No negotiation, no counter-proposals, no independent decision-making about anything the coordinator cares about, no back-and-forth that evolves over multiple exchanges. However complex the modeling happening inside that service, functionally it's a tool — indistinguishable in integration terms from a calculator that happens to run a complicated calculation.

The distinguishing question, stated directly in the material, is exactly what separates these two: "does something specific when called, has no autonomy of its own" (weather) versus "has its own reasoning, makes its own decisions, may need to communicate intent/state back and forth" (freight carrier). Sophistication of internal implementation is irrelevant to this test — a tool can be arbitrarily complex internally and still be a tool from the integration's perspective, because what matters is the *shape of the interaction*, not the intelligence behind it.

**Why the others are wrong**

- **A)** falls for exactly the surface-level pattern-match the scenario is built to catch — "AI-powered" is being treated as the deciding factor when it isn't part of the actual test at all. "Future-proofing" also isn't a real justification here; agent-to-agent protocols solve a specific coordination problem (negotiation between autonomous peers) that the weather integration doesn't have and isn't likely to develop, since weather forecasting fundamentally doesn't require back-and-forth negotiation regardless of how the underlying model is built.
- **C)** overcorrects in the opposite direction, collapsing the distinction entirely by arguing "Claude initiates, so everything's a tool call." This ignores the actual behavior described for the freight carrier — multi-round negotiation with counter-proposals from a system neither side controls isn't well-modeled as a simple request-response tool call, regardless of who technically initiates the first message. Who initiates doesn't determine whether the *ongoing interaction* is peer negotiation or single-shot tool use.
- **D)** gets the mapping backwards by conflating "sophisticated" with "needs agent-to-agent," when the actual criterion is autonomy and negotiation, not computational complexity. Weather forecasting, however sophisticated internally, still resolves to fixed, well-defined input/output with no negotiation — reversing which integration needs which protocol based on which one sounds more technically impressive is exactly the wrong axis to sort on.

**Pattern to remember, capping off this whole sub-topic**: the tool-vs-peer-agent test is behavioral, not based on how impressive or AI-driven the other system is. Ask specifically: does the interaction involve one side proposing something and the other side potentially pushing back, countering, or renegotiating over multiple rounds, with neither side fully controlling the outcome? If yes, that's agent-to-agent territory. If the interaction is always "give input, get output," no matter how sophisticated the computation behind that output is, it's a tool — MCP or direct API, decided using the reuse/discoverability framework from your last two scenarios.

That completes solid coverage of protocol selection — direct API, MCP, and the agent-to-agent boundary, each tested from both directions. You've now got strong coverage across all six Integration sub-topics: RAG design, accuracy-latency trade-offs, auth/authz, observability, and protocol selection. Want a mixed review scenario pulling from across Domain 3, or should we move to Domain 4 (Evaluation, Testing & Optimisation, 16%)?