# Resume Workflow Execution
- As long as you use a persistent checkpointer (like SqliteSaver or PostgresSaver rather than in-memory MemorySaver), you can resume execution simply by calling graph.invoke(None, config) using the same thread_id.
- Passing None as the input tells LangGraph to look up the latest checkpoint saved for that thread_id, load its state, and resume execution from whichever node was scheduled to run next (snapshot.next).

## 1. How Resuming from a Crash Works
When an unhandled exception or crash happens (e.g., API outage, network error, container restart):
 * Any node that completed prior to the crash has already saved its state to the checkpointer.
 * The node that crashed fails to write its state update.
 * When you reconnect and invoke with None, LangGraph inspects the last successfully saved checkpoint for that thread_id and re-runs the failed step.
```python
import sqlite3
from typing import TypedDict
from langgraph.checkpoint.sqlite import SqliteSaver
from langgraph.graph import StateGraph, START, END

class JobState(TypedDict):
    step_num: int
    data: str

def safe_step(state: JobState) -> dict:
    print("Executing safe step...")
    return {"step_num": 1, "data": "Step 1 finished"}

def fragile_step(state: JobState) -> dict:
    print("Executing fragile step...")
    # Simulate a crash if it hasn't succeeded yet
    if state["step_num"] < 2:
        raise ConnectionError("Network dropped during execution!")
    return {"step_num": 2, "data": state["data"] + " -> Step 2 finished"}

builder = StateGraph(JobState)
builder.add_node("safe_node", safe_step)
builder.add_node("fragile_node", fragile_step)

builder.add_edge(START, "safe_node")
builder.add_edge("safe_node", "fragile_node")
builder.add_edge("fragile_node", END)

# Use persistent storage (disk or database, not in-memory RAM)
conn = sqlite3.connect("workflow_state.db", check_same_thread=False)
checkpointer = SqliteSaver(conn)
graph = builder.compile(checkpointer=checkpointer)

config = {"configurable": {"thread_id": "job-order-42"}}
```
## 2. Simulating the Crash and Recovery

### Run 1: Execution Crashes
```python
try:
    graph.invoke({"step_num": 0, "data": ""}, config)
except Exception as e:
    print(f"Workflow halted with error: {e}")
```
# Output:
# Executing safe step...
# Executing fragile step...
# Workflow halted with error: Network dropped during execution!

At this point:
 * "safe_node" has completed and committed to workflow_state.db.
 * The latest checkpoint has snapshot.values = {'step_num': 1, 'data': 'Step 1 finished'} and snapshot.next = ('fragile_node',).

### Run 2: Resume Seamlessly (Without Re-running Previous Nodes)

Later—after resolving the network issue, rebooting, or retrying—call the graph with None:

# Pass `None` as the input payload:
```python
result = graph.invoke(None, config)
print("Resumed result:", result)
```

# Output:
# Executing fragile step...
# Resumed result: {'step_num': 2, 'data': 'Step 1 finished -> Step 2 finished'}

LangGraph skips "safe_node" entirely and picks up directly at "fragile_node".
## 3. Alternative: Manually Patch State Before Resuming
If a crash was caused by corrupted data or invalid arguments in state, you can update the state manually before restarting:

# 1. Inspect the last good state
```python
current_state = graph.get_state(config)
print("Next scheduled node:", current_state.next)
```
# 2. Patch the state to fix or bypass the cause of the failure
```python
graph.update_state(config, {"data": "Manually patched data", "step_num": 2})
```
# 3. Resume the workflow from the patched checkpoint
```python
graph.invoke(None, config)
```
### Important Considerations for Crash Recovery
 * **Database Persistence**: MemorySaver keeps checkpoints in RAM; if your Python process terminates or crashes, that memory is wiped. Use SqliteSaver (local dev) or PostgresSaver (production) to persist state across process restarts.
 * **Node Idempotency**: The node that crashed will be re-executed from scratch upon resume. Ensure side-effects in critical nodes (like charging a credit card or sending an email) are idempotent or placed behind Human-in-the-Loop review gates.
