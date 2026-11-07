An executable foundation to build your Mastering LangGraph notebook, organized progressively from minimal state mechanics to tool-calling workflows.
Phase 1: The Core Architecture & Minimal Graph
LangGraph models applications as cyclical directed graphs where nodes pass and mutate a central shared data structure called State.
Every LangGraph program consists of 4 parts:
 * State Definition: A Python TypedDict or Pydantic model defining the schema.
 * Nodes: Regular Python functions taking (state) and returning a dictionary with state updates.
 * Edges: Connections defining transitions (START, standard edges, conditional edges, END).
 * Compilation: Compiling the definition into an executable runnable via .compile().
Starter Template: Deterministic State Machine (No LLM Required)
from typing import TypedDict
from langgraph.graph import StateGraph, START, END

# 1. Define the State Schema
class WorkflowState(TypedDict):
    input_text: str
    processed_text: str
    step_count: int

# 2. Define the Node Functions
def clean_input(state: WorkflowState) -> dict:
    cleaned = state["input_text"].strip().lower()
    return {"processed_text": cleaned, "step_count": state.get("step_count", 0) + 1}

def add_greeting(state: WorkflowState) -> dict:
    greeting = f"Hello, {state['processed_text'].capitalize()}!"
    return {"processed_text": greeting, "step_count": state["step_count"] + 1}

# 3. Construct the Graph
builder = StateGraph(WorkflowState)

builder.add_node("cleaner", clean_input)
builder.add_node("greeter", add_greeting)

builder.add_edge(START, "cleaner")
builder.add_edge("cleaner", "greeter")
builder.add_edge("greeter", END)

# 4. Compile and Run
graph = builder.compile()

initial_state = {"input_text": "   ALICE   ", "step_count": 0}
result = graph.invoke(initial_state)
print(result)
# Output: {'input_text': '   ALICE   ', 'processed_text': 'Hello, Alice!', 'step_count': 2}

Phase 2: Core Features to Master
| Feature | What It Solves | Key Syntax / Primitives |
|---|---|---|
| State Reducers | Overwriting vs. appending data (e.g., chat history) | Annotated[list, add_messages] |
| Conditional Edges | Dynamic branching based on output, checks, or tool calls | builder.add_conditional_edges("node", routing_fn) |
| Persistence (Checkpointers) | Multi-turn memory, state recovery, and session threads | MemorySaver(), thread_id config |
| Human-in-the-Loop | Approval gates, inspection, and manual state overrides | interrupt_before, interrupt_after |
| Streaming | Real-time output feedback and state inspection | graph.stream(), astream_events() |
Understanding Reducers: Appending vs. Overwriting
By default, returning a key in a node dictionary overwrites that field in the state. To accumulate conversational context or logs, use a reducer:
from typing import Annotated
from typing_extensions import TypedDict
from langgraph.graph.message import add_messages
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage

class ChatState(TypedDict):
    # 'add_messages' ensures new messages append to the list rather than replacing it
    messages: Annotated[list[BaseMessage], add_messages]

Phase 3: Building a Practical Tool-Calling Agent
The ReAct pattern (Reason + Act) is the standard pattern for building agents in LangGraph. The agent inspects the conversation, decides whether to invoke an external tool or finish, loops back on tool execution, and terminates when a final answer is produced.
from typing import Annotated
from typing_extensions import TypedDict
from langchain_openai import ChatOpenAI
from langchain_core.messages import BaseMessage, HumanMessage
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode, tools_condition

# 1. Define custom tools
def get_weather(city: str) -> str:
    """Get the current weather for a given city."""
    return f"The weather in {city} is 72°F and sunny."

tools = [get_weather]

# 2. Bind tools to the model
model = ChatOpenAI(model="gpt-4o-mini").bind_tools(tools)

# 3. State Schema
class AgentState(TypedDict):
    messages: Annotated[list[BaseMessage], add_messages]

# 4. Agent Decision Node
def call_model(state: AgentState) -> dict:
    response = model.invoke(state["messages"])
    return {"messages": [response]}

# 5. Build Graph with Conditional Loop
workflow = StateGraph(AgentState)

workflow.add_node("agent", call_model)
workflow.add_node("tools", ToolNode(tools))

workflow.add_edge(START, "agent")

# tools_condition checks if the LLM output has tool_calls:
# -> If yes, routes to 'tools'
# -> If no, routes to END
workflow.add_conditional_edges("agent", tools_condition)
workflow.add_edge("tools", "agent")

app = workflow.compile()

# 6. Execute
response = app.invoke({"messages": [HumanMessage(content="What's the weather in Seattle?")]})
for msg in response["messages"]:
    print(f"{msg.type.upper()}: {msg.content}")

Recommended Next Step
Would you like to build out Phase 2.1 (Adding Persistent Memory & Checkpointers) to manage multi-turn chat sessions with thread_id, or implement a Human-in-the-Loop approval gate for tool execution?
