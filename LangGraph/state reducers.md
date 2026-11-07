A state reducer is a function that defines how a node's return value merges with the existing state instead of replacing it.
In LangGraph, the default behavior for any key in your state schema is an overwrite: whenever a node returns {"my_key": value}, LangGraph overwrites state["my_key"] with the new value. Reducers let you override this default so keys can append, accumulate, or merge custom data.
Why Reducers Are Needed
Without a reducer, any list or collection resets every time a node outputs an update:
# Without a reducer:
state = {"messages": [HumanMessage(content="Hello")]}

# Node returns:
{"messages": [AIMessage(content="Hi there!")]}

# Default LangGraph behavior (OVERWRITE):
# state["messages"] becomes [AIMessage(content="Hi there!")], losing the HumanMessage!

By attaching a reducer function via Python's Annotated type hint, LangGraph passes both the current value and the incoming update to the reducer function, storing the returned merged value in state:
When to Use State Reducers
| Scenario | What Happens Without a Reducer | Reducer Solution |
|---|---|---|
| Chat History / Messages | Every turn erases prior turns, breaking context. | Annotated[list, add_messages] appends new messages while automatically handling ID deduplication and message replacement. |
| Parallel Node Execution (Fan-Out / Fan-In) | If two nodes run at the same time and update the same key, one will clobber the other. | An append reducer (e.g., operator.add) safely collects outputs from all parallel branches into a list. |
| Audit Logs & Execution Traces | Only the most recent step's log survives. | A list-concat reducer accumulates the full trace step-by-step. |
| Running Counters / Aggregations | Nodes would need to fetch the count, increment it, and return the new total manually. | An arithmetic reducer can add the update directly to the running total. |
How to Implement Reducers
1. The Prebuilt Message Reducer (add_messages)
This is the standard reducer used for conversational agents. It appends new messages to the list and replaces existing messages if they share the same id (useful for streaming or tool message reconciliation).
from typing import Annotated
from typing_extensions import TypedDict
from langchain_core.messages import BaseMessage
from langgraph.graph.message import add_messages

class AgentState(TypedDict):
    # Overwrite behavior disabled; new messages append or merge by ID
    messages: Annotated[list[BaseMessage], add_messages]

2. Built-in Python Operator Reducer (operator.add)
Used for generic lists where you simply want to append items without message-specific deduplication:
import operator
from typing import Annotated
from typing_extensions import TypedDict

class ParallelPipeline(TypedDict):
    # Appends new items to the list: current_list + update_list
    collected_results: Annotated[list[str], operator.add]
    step_count: Annotated[int, operator.add]  # Increments count: current + update

3. Custom Logic Reducers
You can supply any standard Python function taking (current_value, update_value):
from typing import Annotated
from typing_extensions import TypedDict

def merge_unique_tags(current: list[str], update: list[str]) -> list[str]:
    """Appends only tags that are not already in the state."""
    current = current or []
    return list(dict.fromkeys(current + update))

class DocumentTaggerState(TypedDict):
    tags: Annotated[list[str], merge_unique_tags]

Rule of Thumb
 * Use default keys (no reducer) when the state variable represents a snapshot of the current moment (e.g., current_user_id, status = "in_progress", retry_count, active_task).
 * Use reducers when the variable represents an ongoing timeline, log, conversation history, or parallel aggregation (e.g., messages, errors, findings, history).
