# LangGraph App Architecture Gen AI vs Agentic AI

- Under the hood, the foundational primitives are identical, but **how the graph is connected and who controls the execution path are fundamentally different**.
- In both cases, LangGraph uses the exact same four building blocks: 
  - a central State schema (TypedDict or Pydantic),
  - Nodes (Python functions),
  - Edges (direct or conditional), and 
  - a Checkpointer/Engine (.compile()).
- The structural architecture diverges based on flow topology, edge control, and state mutability:


## Architectural Differences
| Architectural Dimension | Generative AI Applications | Agentic AI Applications |
|---|---|---|
| Graph Topology | **DAG (Directed Acyclic Graph)**: Linear sequences, fan-outs, or fixed branch-and-merge pipelines. | **Cyclical Graph**: Contains feedback loops, self-correction iterations, and retry edges. |
| Control of Routing | **Programmer-directed**: Hardcoded direct edges (add_edge) or deterministic rule-based checks. | **Model-directed**: Dynamic conditional edges (add_conditional_edges) driven by LLM decisions (e.g., tools_condition). |
| Termination (END) | **Predetermined**: The workflow reaches END after executing a fixed number of predetermined nodes. | **Dynamic / Goal-driven**: The graph loops continuously until the LLM decides no further tools or retries are needed. |
| State Structure | **Data-transformation payloads**: Schema stores inputs, structured intermediate text, and final formatted outputs. | **Execution & context timeline**: Schema heavily uses reducers (Annotated[list, add_messages]) to track conversational history, tool outputs, and action traces. |
| Interaction with External World | **Read-heavy / Static**: Prompts generate text or extract data against fixed inputs. | **Read-Write / Action-oriented**: Nodes invoke external APIs, execute code, run database operations, and observe results. |
| Checkpointer & Interruption Role | **Audit & Caching**: Checkpoints preserve intermediate outputs for long-running batch jobs. | **Session memory & Human-in-the-Loop**: Checkpoints handle multi-turn dialogue, resume after tool crashes, and pause at approval gates (interrupt_before). |

## Code Comparison
1. **Generative AI Architecture (Deterministic / Sequential)**

A document processing pipeline where the developer dictates every step from START to END without cyclical feedback:

```python
builder = StateGraph(DocumentState)
builder.add_node("extract_text", extract_text)
builder.add_node("summarize", summarize_with_llm)
builder.add_node("format_markdown", format_markdown)

# Strict linear flow (No cycles, developer-controlled)
builder.add_edge(START, "extract_text")
builder.add_edge("extract_text", "summarize")
builder.add_edge("summarize", "format_markdown")
builder.add_edge("format_markdown", END)
```

2. **Agentic AI Architecture (Autonomous & Cyclical)**

A ReAct agent where the LLM determines at runtime whether to call tools, repeat the loop, or stop:
```python
builder = StateGraph(AgentState)
builder.add_node("model", call_llm)
builder.add_node("tools", ToolNode(tools))

builder.add_edge(START, "model")

# Cyclical dynamic routing (Model-controlled)
builder.add_conditional_edges("model", tools_condition)  # LLM chooses 'tools' or 'END'
builder.add_edge("tools", "model")                      # Loop back to model
```

The architecture changes from a deterministic pipeline (Generative AI) where you direct the transitions, to a state machine loop (Agentic AI) where conditional edges delegate routing authority to the LLM.
