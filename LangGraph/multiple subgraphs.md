Yes, you can have multiple subgraphs in a LangGraph application.
In fact, embedding multiple subgraphs into a parent graph is the standard architectural pattern for building hierarchical multi-agent systems (such as supervisor architectures with specialized sub-agents) and complex modular pipelines.
A compiled subgraph (builder.compile()) satisfies the exact interface of a node: it takes state as an input and returns state updates. You can add as many subgraphs as needed as individual nodes inside a parent graph.
Common Multi-Subgraph Patterns
 * Hierarchical / Supervisor Architecture: A top-level supervisor node directs tasks to multiple specialized subgraphs (e.g., a research_agent subgraph, a coding_agent subgraph, and a review_agent subgraph).
 * Sequential Multi-Agent Pipelines: Data passes through specialized subgraphs in sequence (e.g., Data Ingestion Subgraph \rightarrow Transformation Subgraph \rightarrow Reporting Subgraph).
 * Parallel / Fan-Out Subgraphs: The parent graph forks execution into multiple subgraphs concurrently to run tasks in parallel (e.g., searching multiple distinct databases simultaneously).
Minimal Example: Parent Graph with Multiple Subgraphs
Here is how two separate compiled subgraphs are embedded as distinct nodes within a parent orchestrator:
from typing import TypedDict
from langgraph.graph import StateGraph, START, END

# --- Shared State Schema ---
class AppState(TypedDict):
    task: str
    research_notes: str
    code_output: str
    summary: str

# -------------------------------------------------------------------
# Subgraph 1: Research Agent (can have its own internal loops & tools)
# -------------------------------------------------------------------
def search_step(state: AppState) -> dict:
    return {"research_notes": f"Found documentation for: {state['task']}"}

def summarize_step(state: AppState) -> dict:
    return {"research_notes": state["research_notes"] + " | Summarized key points."}

research_builder = StateGraph(AppState)
research_builder.add_node("search", search_step)
research_builder.add_node("summarize", summarize_step)
research_builder.add_edge(START, "search")
research_builder.add_edge("search", "summarize")
research_builder.add_edge("summarize", END)

# Compile Subgraph 1
research_subgraph = research_builder.compile()


# -------------------------------------------------------------------
# Subgraph 2: Coding Agent (its own independent workflow)
# -------------------------------------------------------------------
def draft_code(state: AppState) -> dict:
    return {"code_output": f"def run(): print('{state['task']}')"}

def test_code(state: AppState) -> dict:
    return {"code_output": state["code_output"] + "\n# All tests passed."}

coder_builder = StateGraph(AppState)
coder_builder.add_node("draft", draft_code)
coder_builder.add_node("test", test_code)
coder_builder.add_edge(START, "draft")
coder_builder.add_edge("draft", "test")
coder_builder.add_edge("test", END)

# Compile Subgraph 2
coder_subgraph = coder_builder.compile()


# -------------------------------------------------------------------
# Top-Level Parent Graph (Orchestrates Subgraphs)
# -------------------------------------------------------------------
def finalize(state: AppState) -> dict:
    return {"summary": "Workflow completed successfully."}

parent_builder = StateGraph(AppState)

# Add compiled subgraphs directly as nodes:
parent_builder.add_node("researcher", research_subgraph)
parent_builder.add_node("coder", coder_subgraph)
parent_builder.add_node("finalizer", finalize)

# Wire the parent flow
parent_builder.add_edge(START, "researcher")
parent_builder.add_edge("researcher", "coder")
parent_builder.add_edge("coder", "finalizer")
parent_builder.add_edge("finalizer", END)

# Compile the top-level app
app = parent_builder.compile()

# Execute the full multi-subgraph workflow
result = app.invoke({"task": "Build an ETL script"})
print("Research :", result["research_notes"])
print("Code     :", result["code_output"])
print("Status   :", result["summary"])

Key Subgraph Behaviors
 * State Isolation: Subgraphs can share the parent's state schema, or they can maintain their own private schema (scratchpad memory) so internal intermediate variables do not pollute the parent graph.
 * Independent Checkpointing: Subgraphs run within the parent's checkpointing context, allowing state history, pause/resume, and time-travel inspection across subgraphs.
 * Observability: In tools like LangGraph Studio or when streaming with stream_mode="updates", subgraphs are visually and logically nested, making it straightforward to trace which child graph is active.
