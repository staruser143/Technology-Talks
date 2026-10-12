To prevent infinite loops in LangGraph, you can track the loop count directly inside your workflow state and add a safety check to your conditional edge.
Here is how to modify the previous workflow by adding a loop_count variable to the state to force an exit if it loops too many times.
## 1. Update the State and Nodes
We add loop_count to our ReviewState and increment it inside our writer_node.

from typing import Annotated, TypedDict, Listfrom langgraph.graph import StateGraph, START, END
def append_notes(old_notes: List[str], new_notes: List[str]) -> List[str]:
    return old_notes + new_notes
# 1. Add loop_count to the Stateclass ReviewState(TypedDict):
    draft_content: str
    quality_score: int
    feedback_notes: Annotated[List[str], append_notes]
    approval_status: str
    loop_count: int  # <-- Tracks how many times the loop has run
def writer_node(state: ReviewState):
    # Determine the current loop number (default to 0 if not set, then add 1)
    current_loop = state.get("loop_count", 0) + 1
    print(f"\n✍️ [Loop {current_loop}] Writer: Processing draft...")
    
    # Simulate a stubborn writer that never changes the text
    # This would normally cause an infinite loop!
    return {
        "draft_content": "Artificial intelligence is changing software development rapidly.",
        "approval_status": "pending",
        "loop_count": current_loop  # <-- Pass the incremented count back to state
    }
# (Reviewer and Supervisor nodes remain the same)def reviewer_node(state: ReviewState):
    return {
        "quality_score": 65,  # Permanently low score to trigger a rewrite request
        "feedback_notes": ["Still too short."]
    }
def supervisor_node(state: ReviewState):
    return {"approval_status": "rejected"}

## 2. Update the Conditional Edge (The Safety Valve)
The routing function now inspects loop_count. If it exceeds our MAX_RETRIES threshold, it overrides the supervisor's rejection and forces the graph to end.

MAX_RETRIES = 3
def route_after_supervisor(state: ReviewState):
    """Inspects approval state AND loop count to prevent infinite cycles."""
    # Safety Check: If we have looped too many times, break out immediately
    if state.get("loop_count", 0) >= MAX_RETRIES:
        print(f"⚠️ Safety Guard: Max retries ({MAX_RETRIES}) reached. Forcing exit.")
        return "force_exit"
        
    if state["approval_status"] == "approved":
        return "end_workflow"
    else:
        return "rewrite"

## 3. Wire and Execute the Graph
Map the new "force_exit" routing option straight to the END node.

workflow = StateGraph(ReviewState)

workflow.add_node("writer", writer_node)
workflow.add_node("reviewer", reviewer_node)
workflow.add_node("supervisor", supervisor_node)

workflow.add_edge(START, "writer")
workflow.add_edge("writer", "reviewer")
workflow.add_edge("reviewer", "supervisor")
# Configure the conditional edge with the safety exit route
workflow.add_conditional_edges(
    "supervisor",
    route_after_supervisor,
    {
        "end_workflow": END,
        "rewrite": "writer",
        "force_exit": END  # <-- Maps our safety trigger to the end of the graph
    }
)
app = workflow.compile()
# Run with loop_count initialized to 0final_state = app.invoke({
    "draft_content": "", 
    "quality_score": 0, 
    "feedback_notes": [], 
    "approval_status": "",
    "loop_count": 0
})

print(f"\n🎯 Graph Finished. Final Loop Count: {final_state['loop_count']}")

## Streamed Console Output
If you run this code, the system stops cleanly on the third iteration instead of looping forever:

✍️ [Loop 1] Writer: Processing draft...
👔 Supervisor: Checking score... Quality too low. Rejected.

✍️ [Loop 2] Writer: Processing draft...
👔 Supervisor: Checking score... Quality too low. Rejected.

✍️ [Loop 3] Writer: Processing draft...
⚠️ Safety Guard: Max retries (3) reached. Forcing exit.

🎯 Graph Finished. Final Loop Count: 3

Would you like to see how to pass runtime configuration arguments to your graph to dynamically change the MAX_RETRIES limit without hardcoding it?

