To check the current state of a specific workflow run, use graph.get_state(config) by passing the thread_id associated with that execution.
This returns a StateSnapshot object containing the current state values, the next scheduled node, and execution metadata.
Step-by-Step Code Example
# Pass the thread_id of the workflow run you want to inspect
config = {"configurable": {"thread_id": "session-101"}}

# Fetch the current state snapshot
state_snapshot = graph.get_state(config)

Key Attributes of the Snapshot
| Attribute | What It Tells You | Example Value |
|---|---|---|
| state_snapshot.values | The current values stored across all state keys. | {'messages': [...], 'retry_count': 1} |
| state_snapshot.next | Which node is scheduled to run next. If empty (), the workflow has finished (END). | ('tools',) or () |
| state_snapshot.created_at | Timestamp when this state was committed. | '2026-11-07T15:30:00.000Z' |
| state_snapshot.metadata | Metadata about how this state was produced, including the step index and writes from the previous node. | {'source': 'loop', 'step': 2, 'writes': {...}} |
| state_snapshot.tasks | Any active, pending, or interrupted tasks currently waiting to execute. | Useful for Human-in-the-Loop workflows. |
Practical Example: Inspecting Workflow Status
snapshot = graph.get_state(config)

if not snapshot.next:
    print("Workflow Status: COMPLETED")
    print("Final State Values:", snapshot.values)
else:
    print(f"Workflow Status: IN PROGRESS / PAUSED")
    print(f"Current State Values : {snapshot.values}")
    print(f"Next Node to Execute: {snapshot.next}")

Note: For graph.get_state() to work, the graph must be compiled with a checkpointer (such as MemorySaver, SqliteSaver, or PostgresSaver).
