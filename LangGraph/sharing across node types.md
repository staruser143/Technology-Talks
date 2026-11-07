Whether the state is shared depends directly on whether you are running a node inside the same graph or delegating to a subgraph.
1. Nodes Within the Same Graph: Shared State
For standard nodes—whether an LLM invocation, a custom function, or a Tool execution (ToolNode)—they all share the exact same state schema:
 * Every node in that graph receives the entire, current state dictionary as its input.
 * Any dictionary returned by a node updates the shared state (either by overwriting fields or by applying reducers like add_messages or operator.add).
 * The next node in the graph reads those updated state keys directly.
2. Subgraph / Separate Agent Execution: Shared vs. Isolated State
When a node is an entire compiled subgraph (a separate agent), you have two distinct architectural choices for how state is handled:
A. Shared Schema (Completely Shared State)
If both the parent graph and the sub-agent graph are defined using the exact same State class/keys:
 * The child agent inherits the full state from the parent when called.
 * The child agent's internal nodes mutate that state.
 * When the child agent finishes, all modified keys seamlessly flow back into the parent graph's state.
B. Isolated / Custom Schema (Private State with Input/Output Translation)
In most real-world multi-agent architectures, you do not want the child agent to share the parent's entire state. For instance, a "Researcher Agent" might generate 20 internal search queries, tool outputs, and scratchpad notes that would clutter the supervisor's main conversation state.
LangGraph allows subgraphs to have their own private SubgraphState schema:
 * Input Translation: When the parent calls the subgraph node, it passes only the keys that match the subgraph's schema (or a custom wrapper node filters/maps the inputs).
 * Private Execution: The subgraph runs its own internal cycles, checkpointers, and state reducers using its private fields without affecting the parent state.
 * Output Mapping: The subgraph only returns its final output keys (e.g., {"final_report": "..."}). When it terminates, only those overlapping or returned keys merge back into the parent graph's state.
Summary
| Execution Type | State Relationship | How It Works |
|---|---|---|
| LLM Node | Shared | Reads parent state keys \rightarrow writes response into parent state. |
| Tool Node (ToolNode) | Shared | Reads messages from parent state \rightarrow writes ToolMessage directly into parent state. |
| Subgraph (Same Schema) | Shared | Inherits full parent state \rightarrow writes changes directly back to parent. |
| Subgraph (Custom Schema) | Isolated / Private | Executes with isolated scratchpad memory; only mapped outputs merge back to the parent. |
