Here is an example of a multi-step Content Review and Approval Workflow.
In this scenario, a writer creates content, an AI reviewer checks it for quality, and a supervisor gives final approval. The workflow state acts as the central notebook, passing the draft, quality scores, and feedback from step to step.
## 1. Define the Workflow State
We use a standard Python TypedDict. Each node will receive this state, read from it, and return a dictionary that updates it.

from typing import TypedDict, List
# Define what our shared memory looks likeclass ReviewState(TypedDict):
    draft_content: str       # The core text being worked on
    quality_score: int       # Out of 100, set by the reviewer
    feedback_notes: List[str] # List of revision suggestions
    approval_status: str     # "pending", "approved", or "rejected"

## 2. Define the Nodes (The Steps)
Each function represents a "node" or a step in the process. They automatically receive the current ReviewState.

def writer_node(state: ReviewState):
    """Step 1: Write the initial draft."""
    print("✍️ Writer: Draft created.")
    return {
        "draft_content": "Artificial intelligence is changing software development rapidly.",
        "approval_status": "pending",
        "feedback_notes": []
    }
def reviewer_node(state: ReviewState):
    """Step 2: Evaluate the draft and give feedback."""
    print("🧐 Reviewer: Evaluating draft content...")
    content = state["draft_content"]
    
    # Simple logic simulating an AI evaluation
    if len(content) < 100:
        score = 65
        notes = state["feedback_notes"] + ["Content is too short. Expand on technical examples."]
    else:
        score = 90
        notes = state["feedback_notes"] + ["Great length and depth."]
        
    return {
        "quality_score": score,
        "feedback_notes": notes
    }
def supervisor_node(state: ReviewState):
    """Step 3: Make a final approval decision based on the score."""
    print("👔 Supervisor: Making final decision...")
    
    if state["quality_score"] >= 80:
        status = "approved"
    else:
        status = "rejected"
        
    return {
        "approval_status": status
    }

## 3. Route the Flow (Conditional State Check)
We need a routing function (a "conditional edge") that inspects the state after the supervisor node to decide whether the workflow ends or goes back to the writer for fixes.

def route_after_supervisor(state: ReviewState):
    """Inspects the state to determine the next path."""
    if state["approval_status"] == "approved":
        return "end_workflow"
    else:
        return "rewrite"

## 4. Build and Run the Graph
Now, we stitch the steps together using LangGraph's StateGraph.

from langgraph.graph import StateGraph, START, END
# Initialize the graph with our state structureworkflow = StateGraph(ReviewState)
# Add our steps
workflow.add_node("writer", writer_node)
workflow.add_node("reviewer", reviewer_node)
workflow.add_node("supervisor", supervisor_node)
# Set up the linear flow
workflow.add_edge(START, "writer")      # Start -> Writer
workflow.add_edge("writer", "reviewer")  # Writer -> Reviewer
workflow.add_edge("reviewer", "supervisor") # Reviewer -> Supervisor
# Set up the conditional flow based on the State
workflow.add_conditional_edges(
    "supervisor",
    route_after_supervisor,
    {
        "end_workflow": END,      # If approved, stop
        "rewrite": "writer"       # If rejected, loop back to the writer
    }
)
# Compile into an executable appapp = workflow.compile()
# Run the workflow with an empty starting statefinal_state = app.invoke({"draft_content": "", "quality_score": 0, "feedback_notes": [], "approval_status": ""})

print("\n🎯 Final Workflow State:")
print(final_state)

## How the State Mutated During This Run:

   1. START initializes the state as empty.
   2. writer_node runs, populating draft_content with a short sentence.
   3. reviewer_node reads the short text, flags it as too brief, sets quality_score to 65, and appends a tip to feedback_notes.
   4. supervisor_node reads the 65 score and updates approval_status to "rejected".
   5. route_after_supervisor reads the "rejected" status and sends the workflow back to the "writer" node to fix it using the accumulated feedback.

Would you like to see how to use LangGraph's prebuilt Message state to handle a multi-turn chat history instead of custom text fields, or see how to inject human feedback during the supervisor step?

