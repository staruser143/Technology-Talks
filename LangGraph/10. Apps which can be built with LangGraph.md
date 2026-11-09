# LangGraph App Types
- LangGraph is designed to build both generative AI applications and agentic AI systems.
- While pure generative AI focuses on generating text, code, or media (often in linear chains or single-turn prompts), agentic AI introduces autonomous reasoning, tool execution, decision-making, and self-correction loops. 

LangGraph’s cyclic graph and state-machine architecture make it suitable for both paradigms.

1. **Generative AI Applications (Linear & Semi-Linear Pipelines)**
We can use LangGraph for structured, multi-step generative workflows that need deterministic control, schemas, and state passing:
 * Document Processing & Summarization: Multi-stage extraction (chunk \rightarrow summarize \rightarrow synthesize \rightarrow format) where state is passed and combined across steps.
 * Content Generation Pipelines: Drafting blog posts, marketing copy, or reports with distinct phases (outline \rightarrow draft \rightarrow tone review \rightarrow final polish).
 * Deterministic Transformation Pipelines: Cleaning input, formatting data, or categorizing sentiment alongside LLM generations without autonomous loops.

2. **Agentic AI Applications (Autonomous & Cyclical Workflows)**
LangGraph's core strength lies in agentic systems, where the application must decide its own path, use external tools, inspect results, and loop until goals are met:
 * **ReAct Tool-Calling Agents**: Agents that reason, call external APIs or databases, observe the outputs, and iterate until the query is resolved.
 * **Self-Reflective / Corrective Agents**: Graphs that draft code or answers, test/evaluate them via conditional edges, and loop back to fix errors automatically.
 * **Human-in-the-Loop (HITL) Operations**: Autonomous agents that pause at sensitive steps (e.g., executing a database write or sending an email) to await human review, approval, or state overrides.
 * **Multi-Agent Architectures**: Systems where a supervisor or router delegates specialized subtasks to independent sub-agents (e.g., a research agent, a coding agent, and a reviewer agent).

## Comparison
| Feature | Generative AI Flows | Agentic AI Systems |
|---|---|---|
| Execution Flow | Typically Directed Acyclic Graphs (linear or branching). | Cyclical graphs with feedback and retry loops. |
| Control | Program-directed / deterministic transitions. | Model-directed routing via conditional edges. |
| Tool Usage | Fixed prompts or static model calls. | Dynamic tool calling determined at runtime. |
| State Role | Storing inputs, intermediate text, and final deliverables. | Tracking tool outputs, message history, memory checkpoints, and task flags. |
