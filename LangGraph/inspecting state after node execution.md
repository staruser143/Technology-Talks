# Inspecting State after Node Execution
- To inspect which node updated a state value after the graph runs, use a **checkpointer (such as MemorySaver)** along with the .**get_state_history()** method.
- Every time a node executes, the checkpointer captures a snapshot called a **StateSnapshot**.
- Each snapshot contains a metadata dictionary with a **"source"** key that records the node responsible for that specific transition.

## Step-by-Step Code Example
```python
import operator
from typing import Annotated, TypedDict
from langgraph.checkpoint.memory import MemorySaver
from langgraph.graph import StateGraph, START, END

# 1. State schema using an append reducer
class PipelineState(TypedDict):
    data: Annotated[list[str], operator.add]

# 2. Define nodes returning clean data updates (no manual tagging needed)
def step_one(state: PipelineState) -> dict:
    return {"data": ["Output from Step 1"]}

def step_two(state: PipelineState) -> dict:
    return {"data": ["Output from Step 2"]}

# 3. Assemble graph
builder = StateGraph(PipelineState)
builder.add_node("node_a", step_one)
builder.add_node("node_b", step_two)

builder.add_edge(START, "node_a")
builder.add_edge("node_a", "node_b")
builder.add_edge("node_b", END)

# 4. Compile with a Checkpointer (MemorySaver)
checkpointer = MemorySaver()
graph = builder.compile(checkpointer=checkpointer)

# 5. Execute with a required thread_id configuration
config = {"configurable": {"thread_id": "session-101"}}
graph.invoke({"data": []}, config)

Inspecting the Execution History
.get_state_history(config) yields snapshots in reverse chronological order (from most recent back to initialization):
# Retrieve all recorded checkpoints for this thread
history = list(graph.get_state_history(config))

for snapshot in history:
    # 'source' can be 'loop' (internal transition), 'input', or custom node updates
    source_type = snapshot.metadata.get("source")
    
    # LangGraph tags specific node transitions under 'step' and 'writes'
    step = snapshot.metadata.get("step")
    writes = snapshot.metadata.get("writes")
    
    print("--- Checkpoint ---")
    print(f"Step Index : {step}")
    print(f"Node Executed (Writes): {writes}")
    print(f"Current State Values  : {snapshot.values}")
    print(f"Next Node to Run      : {snapshot.next}")
```
### Output Representation
```
--- Checkpoint ---
Step Index : 2
Node Executed (Writes): {'node_b': {'data': ['Output from Step 2']}}
Current State Values  : {'data': ['Output from Step 1', 'Output from Step 2']}
Next Node to Run      : ()

--- Checkpoint ---
Step Index : 1
Node Executed (Writes): {'node_a': {'data': ['Output from Step 1']}}
Current State Values  : {'data': ['Output from Step 1']}
Next Node to Run      : ('node_b',)

--- Checkpoint ---
Step Index : 0
Node Executed (Writes): {'__start__': {'data': []}}
Current State Values  : {'data': []}
Next Node to Run      : ('node_a',)
```

## Key Properties in StateSnapshot
 * **snapshot.metadata["writes"]**: A dictionary where keys are the specific node names (e.g., 'node_a', 'node_b') and the values are the exact state diff returned by that node before the reducer merged it.
 * **snapshot.values**: The cumulative state up to that point.
 * **snapshot.next**: A tuple containing the node scheduled to run next. An empty tuple () indicates the graph terminated (END).
 * **snapshot.config**: Contains the checkpoint ID (checkpoint_id), which allows you to fork the conversation or replay execution from that exact historical step.
