Yes. In LangGraph, a node is simply a Python callable or runnable that receives the current state and returns state updates. Because of this design, a node can encapsulate an LLM invocation, a tool execution, or even an entire independent agent (compiled sub-graph).
1. LLM Invocation Node
The node directly executes a call to an LLM provider (using LangChain wrappers like ChatOpenAI or raw native SDKs) and appends or writes the response into the state:
def call_model_node(state: MyState) -> dict:
    response = model.invoke(state["messages"])
    return {"messages": [response]}

2. Tool Execution Node
The node executes one or more tools. This can be written as a custom function or using LangGraph's prebuilt ToolNode:
 * Custom function:
   def execute_calculator_node(state: MyState) -> dict:
    result = run_calculator(state["expression"])
    return {"calculation_result": result}

 * Prebuilt ToolNode:
   from langgraph.prebuilt import ToolNode
# ToolNode is itself a valid graph node
tool_node = ToolNode([fetch_weather, query_database])
builder.add_node("tools", tool_node)

3. Agent Execution Node (Sub-Graph / Multi-Agent)
A node can be an entire compiled LangGraph agent. In hierarchical and multi-agent systems, a parent graph can treat an entire compiled sub-graph agent as a single node:
# 1. Build and compile an independent research agent
research_subgraph = create_research_agent().compile()

# 2. Add the entire agent as a single node inside the supervisor graph
supervisor_builder = StateGraph(SupervisorState)
supervisor_builder.add_node("researcher_agent", research_subgraph)

When the execution reaches "researcher_agent", the full internal loop of the research agent runs, finishes its work, and returns its final output back into the parent supervisor state.
