# Thread Id
- A thread_id is a unique identifier used by LangGraph's checkpointer to track, store, and isolate state across multiple execution runs or conversation turns.

## What Does thread_id Do?
 * **Session Isolation**: Each unique thread_id represents an independent workflow instance or conversational thread. Two different users interacting with the same graph can each have their own thread_id without their states mixing.
 * **State Persistence**: When a checkpointer (MemorySaver, SqliteSaver, PostgresSaver) is attached, LangGraph saves snapshots under that specific thread_id at every step.
 * **Resuming and Context Continuation**: Passing an existing thread_id in subsequent invocations allows the graph to pick up where it left off (such as appending to chat history or resuming after an interruption/crash).

### How to Set thread_id for a Workflow Run
The thread_id is passed as part of the execution config dictionary under the key "configurable":
```python
config = {
    "configurable": {
        "thread_id": "unique-session-id-123"
    }
}
```

This configuration dictionary is passed as the second argument to graph execution methods:
## 1. Running the Workflow (invoke)
# Initial invocation: State is created under thread_id 'user-42'
```python
result1 = app.invoke(
    {"messages": [HumanMessage(content="Hello, my name is Alice")]},
    config={"configurable": {"thread_id": "user-42"}}
)

# Second invocation: Uses the exact same thread_id to continue the conversation
result2 = app.invoke(
    {"messages": [HumanMessage(content="What is my name?")]},
    config={"configurable": {"thread_id": "user-42"}}
)
```
# Output will remember "Alice" because state was loaded from thread 'user-42'

## 2. Streaming Output (stream)
```python
config = {"configurable": {"thread_id": "user-42"}}

for chunk in app.stream({"messages": [HumanMessage(content="Tell me a joke")]}, config):
    print(chunk)
```
## 3. Inspecting or Patching State (get_state & update_state)
```python
config = {"configurable": {"thread_id": "user-42"}}

# Read the snapshot for this thread
snapshot = app.get_state(config)
print("Current State:", snapshot.values)

# Update state for this thread
app.update_state(config, {"step_count": 5})
```
## Common Strategies for Choosing a thread_id
 * **Chat Applications**: Use the user ID or a combined f"user_{user_id}_chat_{session_uuid}" so users can have multiple separate chat threads.
 * **Batch or Background Jobs**: Use an order ID, task UUID, or transaction number (e.g., f"order_{order_id}").
 * **Ad-hoc Runs**: Generate a random UUID with import uuid; str(uuid.uuid4()).
