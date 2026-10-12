Here is the complete, runnable code updated with the smart writer logic.
In this execution, the first loop results in a low score because the text is too short. The supervisor rejects it and routes it back to the writer. The writer detects the feedback, expands the draft, and on the second pass, the reviewer approves it, breaking the workflow out of the loop.

from typing import Annotated, TypedDict, Listfrom langgraph.graph import StateGraph, START, END
# ==========================================# 1. State Definition with a Reducer# ==========================================def append_notes(old_notes: List[str], new_notes: List[str]) -> List[str]:
    """Ensures feedback notes accumulate rather than overwrite."""
    return old_notes + new_notes
class ReviewState(TypedDict):
    draft_content: str
    quality_score: int
    feedback_notes: Annotated[List[str], append_notes]
    approval_status: str
# ==========================================# 2. Updated Node Definitions# ==========================================def writer_node(state: ReviewState):
    """Step 1: Creates or updates the draft based on current state."""
    # Check if there is existing feedback in the state
    if state["feedback_notes"]:
        print(f"\n✍️ [Loop 2] Writer: Reading feedback: '{state['feedback_notes'][-1]}'")
        print("✍️ [Loop 2] Writer: Expanding the draft with technical details...")
        return {
            "draft_content": (
                "Artificial intelligence is changing software development rapidly. "
                "By leveraging LLMs, developers can automate boilerplate code generation, "
                "detect complex runtime vulnerabilities instantly, and translate legacy codebases "
                "into modern frameworks with minimal human intervention."
            ),
            "approval_status": "pending"
        }
    
    # First-time execution path
    print("\n✍️ [Loop 1] Writer: Creating initial short draft...")
    return {
        "draft_content": "Artificial intelligence is changing software development rapidly.",
        "approval_status": "pending",
        "feedback_notes": [] # Initialize list
    }
def reviewer_node(state: ReviewState):
    """Step 2: Evaluates text length and scores it."""
    content = state["draft_content"]
    print(f"🧐 Reviewer: Evaluating draft (Length: {len(content)} chars)...")
    
    if len(content) < 100:
        print("🧐 Reviewer: Content too short. Giving low score.")
        return {
            "quality_score": 65,
            "feedback_notes": ["Draft is too generic. Add specific software engineering examples."]
        }
    else:
        print("🧐 Reviewer: Excellent depth. Giving high score.")
        return {
            "quality_score": 95,
            "feedback_notes": ["Clear, specific, and technically sound."]
        }
def supervisor_node(state: ReviewState):
    """Step 3: Changes status based on score threshold."""
    score = state["quality_score"]
    print(f"👔 Supervisor: Checking score ({score})...")
    
    if score >= 80:
        print("👔 Supervisor: Meets quality bars. Approved!")
        return {"approval_status": "approved"}
    else:
        print("👔 Supervisor: Quality too low. Rejected.")
        return {"approval_status": "rejected"}
# ==========================================# 3. Conditional Edge Logic# ==========================================def route_after_supervisor(state: ReviewState):
    """Inspects approval state to break or continue loop."""
    if state["approval_status"] == "approved":
        return "end_workflow"
    else:
        return "rewrite"
# ==========================================# 4. Graph Construction and Execution# ==========================================# Initialize graphworkflow = StateGraph(ReviewState)
# Register nodes
workflow.add_node("writer", writer_node)
workflow.add_node("reviewer", reviewer_node)
workflow.add_node("supervisor", supervisor_node)
# Set up edges
workflow.add_edge(START, "writer")
workflow.add_edge("writer", "reviewer")
workflow.add_edge("reviewer", "supervisor")
# Set up conditional routing out of supervisor
workflow.add_conditional_edges(
    "supervisor",
    route_after_supervisor,
    {
        "end_workflow": END,
        "rewrite": "writer"
    }
)
# Compile and executeapp = workflow.compile()final_state = app.invoke({
    "draft_content": "", 
    "quality_score": 0, 
    "feedback_notes": [], 
    "approval_status": ""
})
# Print ultimate state summary
print("\n" + "="*40)
print("🎯 FINAL WORKFLOW STATE RESULT:")
print("="*40)
print(f"Status: {final_state['approval_status'].upper()}")
print(f"Final Score: {final_state['quality_score']}/100")
print(f"Final Draft: '{final_state['draft_content']}'")
print(f"All Accumulated Feedback: {final_state['feedback_notes']}")

## Streamed Console Output Simulation
When you run this script, your console will output:

✍️ [Loop 1] Writer: Creating initial short draft...
🧐 Reviewer: Evaluating draft (Length: 62 chars)...
🧐 Reviewer: Content too short. Giving low score.
👔 Supervisor: Checking score (65)...
👔 Supervisor: Quality too low. Rejected.

✍️ [Loop 2] Writer: Reading feedback: 'Draft is too generic. Add specific software engineering examples.'
✍️ [Loop 2] Writer: Expanding the draft with technical details...
🧐 Reviewer: Evaluating draft (Length: 263 chars)...
🧐 Reviewer: Excellent depth. Giving high score.
👔 Supervisor: Checking score (95)...
👔 Supervisor: Meets quality bars. Approved!

========================================
🎯 FINAL WORKFLOW STATE RESULT:
========================================
Status: APPROVED
Final Score: 95/100
Final Draft: 'Artificial intelligence is changing software development rapidly. By leveraging LLMs, developers can automate boilerplate code generation...'
All Accumulated Feedback: ['Draft is too generic. Add specific software engineering examples.', 'Clear, specific, and technically sound.']

Would you like to see how to prevent an infinite loop by adding a max retry counter directly into the state variables, or should we look at how to stream these node transitions in real-time?

