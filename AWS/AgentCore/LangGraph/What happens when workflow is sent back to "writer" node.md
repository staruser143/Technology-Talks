When the workflow is routed back to the "writer" node, the loop begins again, but with one critical difference: the "writer" node now receives the updated state containing the feedback and scores from the previous run.
However, looking closely at how the writer_node was written in our basic example, a common LangGraph bug would actually happen. Here is what happens under the hood and how to fix it:
## 1. The Bug: Overwriting the State
In the simple code snippet from the previous answer, the writer_node was hardcoded like this:

def writer_node(state: ReviewState):
    return {
        "draft_content": "Artificial intelligence is changing software development rapidly.",
        "approval_status": "pending",
        "feedback_notes": [] # <-- BUG: This clears out the reviewer's notes!
    }

If the workflow loops back here, the writer completely wipes out the feedback_notes array by returning an empty list []. The writer wouldn't actually "see" or adapt to the reviewer's comments.
## 2. The Fix: Reading State to Make Changes
To make a loop meaningful, the node must read the current state data before returning updates. By default, LangGraph overwrites any keys you return.
Here is how you update the writer_node so it dynamically acts on the feedback when sent back:

def writer_node(state: ReviewState):
    # Check if this is a rewrite loop by looking for feedback
    if state.get("feedback_notes"):
        print(f"✍️ Writer: Fixing draft based on feedback: {state['feedback_notes'][-1]}")
        
        # Simulate fixing the draft based on the reviewer's notes
        return {
            "draft_content": "Artificial intelligence is changing software development rapidly by automating repetitive coding tasks, debugging errors in real-time, and generating boilerplate code instantly.",
            "approval_status": "pending"
            # Note: We omit "feedback_notes" so we don't overwrite or erase the history!
        }
    
    # First-time run logic
    print("✍️ Writer: Creating initial draft.")
    return {
        "draft_content": "Artificial intelligence is changing software development rapidly.",
        "approval_status": "pending"
    }

## 3. Progressive Accumulation (The Reducer Alternative)
If you want to keep a running history of all feedback notes across multiple loops without accidentally overwriting them, you can change your state definition using an Annotated type with an accumulation function (a reducer):

from typing import Annotated, TypedDict, List
def append_notes(old_notes: List[str], new_notes: List[str]) -> List[str]:
    """Tells LangGraph to combine lists instead of replacing them."""
    return old_notes + new_notes
class ReviewState(TypedDict):
    draft_content: str
    quality_score: int
    # This annotation guarantees notes are appended, never overwritten
    feedback_notes: Annotated[List[str], append_notes] 
    approval_status: str

## Summary of the Lifecycle
When sent back to "writer":

   1. The state retains quality_score: 65 and the feedback_notes.
   2. The "writer" node runs, reads the rejection feedback, generates an expanded draft, and returns the new "draft_content".
   3. LangGraph merges this update. The graph moves forward to the "reviewer" node for a second inspection.

Would you like to see the complete, updated code execution to see how the graph breaks out of the loop once the writer expands the text, or do you want to explore how to limit the maximum number of loops so it doesn't get stuck in an infinite cycle?

