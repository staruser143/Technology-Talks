version: "1.1"

# Define node types: custom python callables or built-in tool executors
nodes:
  - name: "agent"
    type: "function"
    handler: "call_model"
  - name: "tools"
    type: "tool_node"
    tools: ["get_weather"]

# Fixed linear edges
edges:
  - from: "__start__"
    to: "agent"
  - from: "tools"
    to: "agent"

# Conditional routing edges
conditional_edges:
  - source: "agent"
    router: "tools_condition"
    # Optional explicit path map; if omitted, router returns node names directly
    path_map:
      tools: "tools"
      __end__: "__end__"

Python Implementation of the Dynamic Engine
This parser resolves both custom node functions and prebuilt ToolNode instances, maps conditional routers, and handles sentinel nodes (START and END).
import yaml
from typing import Annotated, Any, Callable
from typing_extensions import TypedDict

from langchain_core.messages import BaseMessage, HumanMessage
from langchain_openai import ChatOpenAI
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode, tools_condition

# --- 1. Define Application State ---
class AgentState(TypedDict):
    messages: Annotated[list[BaseMessage], add_messages]

# --- 2. Define Custom Tools & Models ---
def get_weather(city: str) -> str:
    """Get the current weather for a given city."""
    return f"The weather in {city} is 72°F and sunny."

TOOL_REGISTRY: dict[str, Callable] = {
    "get_weather": get_weather,
}

model = ChatOpenAI(model="gpt-4o-mini").bind_tools(list(TOOL_REGISTRY.values()))

# --- 3. Define Node Handlers & Condition Routers ---
def call_model(state: AgentState) -> dict:
    response = model.invoke(state["messages"])
    return {"messages": [response]}

FUNCTION_REGISTRY: dict[str, Callable] = {
    "call_model": call_model,
}

ROUTER_REGISTRY: dict[str, Callable] = {
    "tools_condition": tools_condition,
}

# --- 4. Extensible YAML Graph Builder ---
def resolve_special_node(name: str):
    if name == "__start__":
        return START
    if name == "__end__":
        return END
    return name

def build_graph_from_yaml(yaml_path: str, state_schema):
    with open(yaml_path, "r") as f:
        config = yaml.safe_load(f)

    builder = StateGraph(state_schema)

    # A. Register Nodes by Type
    for node in config.get("nodes", []):
        node_name = node["name"]
        node_type = node.get("type", "function")

        if node_type == "tool_node":
            # Instantiate prebuilt ToolNode with resolved tool references
            selected_tools = [TOOL_REGISTRY[t] for t in node.get("tools", [])]
            builder.add_node(node_name, ToolNode(selected_tools))

        elif node_type == "function":
            handler_func = FUNCTION_REGISTRY[node["handler"]]
            builder.add_node(node_name, handler_func)

        else:
            raise ValueError(f"Unsupported node type: {node_type}")

    # B. Register Direct Edges
    for edge in config.get("edges", []):
        src = resolve_special_node(edge["from"])
        dst = resolve_special_node(edge["to"])
        builder.add_edge(src, dst)

    # C. Register Conditional Edges
    for c_edge in config.get("conditional_edges", []):
        src = resolve_special_node(c_edge["source"])
        router_fn = ROUTER_REGISTRY[c_edge["router"]]
        
        path_map = c_edge.get("path_map")
        if path_map:
            # Map string targets to special graph tokens if present
            resolved_path_map = {
                key: resolve_special_node(val) for key, val in path_map.items()
            }
            builder.add_conditional_edges(src, router_fn, resolved_path_map)
        else:
            # Uses default routing behavior (return value matches node name directly)
            builder.add_conditional_edges(src, router_fn)

    return builder.compile()

# --- 5. Execution ---
app = build_graph_from_yaml("workflow.yaml", AgentState)

inputs = {"messages": [HumanMessage(content="What's the weather in Seattle?")]}
output = app.invoke(inputs)

for msg in output["messages"]:
    print(f"{msg.type.upper()}: {msg.content}")

How It Works
 * Node Factory Abstraction: The parser checks node.type. If it detects "tool_node", it queries TOOL_REGISTRY and configures ToolNode(tools). If it detects "function", it injects standard Python callables from FUNCTION_REGISTRY.
 * Path Resolution: The helper resolve_special_node() converts YAML tokens like "__start__" and "__end__" into LangGraph's internal START and END sentinel objects.
 * Routing Path Maps: When path_map is specified under conditional_edges, LangGraph maps the string returned by the router function directly to target destination nodes. If path_map is omitted, LangGraph executes the node matching the router's return value.
