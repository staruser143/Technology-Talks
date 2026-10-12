To pass dynamic parameters into your graph at runtime without hardcoding them, LangGraph provides a built-in feature called RunnableConfig.
By adding a special section called configurable to your configuration dictionary, you can pass parameters like max_retries into the invoke() step. Any node or conditional edge can then read these values on the fly.
Here is the complete example showing how to configure and read runtime arguments:
## 1. Update the Routing Edge to Read Configuration
Any node or conditional edge in LangGraph can optionally accept a second parameter usually named config: RunnableConfig. LangGraph will automatically inject the runtime parameters into it.

from typing import Annotated, TypedDict, Listfrom langchain_core.runnables import RunnableConfig  # <-- Import the configuration typefrom langgraph.graph import StateGraph, START, END
# Define standard state structuredef append_notes(old_notes: List[str], new_notes: List[str]) -> List[str]:
    return old_notes + new_notes
class ReviewState(TypedDict):
    draft_content: str
    quality_score: int
    feedback_notes: Annotated[List[str], append_notes]
    approval_status: str
    loop_count: int
# --- Conditional Routing Edge ---def route_after_supervisor(state: ReviewState, config: RunnableConfig):
    """Inspects the state and extracts dynamic runtime configuration details."""
    # Extract 'configurable' arguments. Provide a safe fallback value if omitted.
    configurable = config.get("configurable", {})
    max_retries = configurable.get("max_retries", 3) # Defaults to 3 if not provided
    
    current_loop = state.get("loop_count", 0)
    
    # Dynamic safety barrier check
    if current_loop >= max_retries:
        print(f"⚠️ Safety Guard: Dynamic max retries limit ({max_retries}) hit! Forcing exit.")
        return "force_exit"
        
    if state["approval_status"] == "approved":
        return "end_workflow"
    else:
        return "rewrite"

## 2. Define standard Nodes
We will simulate the same loop structure where the writer fails to change the draft content, causing consecutive rejections.

def writer_node(state: ReviewState):
    current_loop = state.get("loop_count", 0) + 1
    print(f"✍️ [Loop {current_loop}] Writer: Generating draft...")
    return {
        "draft_content": "Short text.",
        "approval_status": "pending",
        "loop_count": current_loop
    }
def reviewer_node(state: ReviewState):
    return {"quality_score": 50, "feedback_notes": ["Too short."]}
def supervisor_node(state: ReviewState):
    return {"approval_status": "rejected"}

## 3. Build the Graph

workflow = StateGraph(ReviewState)

workflow.add_node("writer", writer_node)
workflow.add_node("reviewer", reviewer_node)
workflow.add_node("supervisor", supervisor_node)

workflow.add_edge(START, "writer")
workflow.add_edge("writer", "reviewer")
workflow.add_edge("reviewer", "supervisor")

workflow.add_conditional_edges(
    "supervisor",
    route_after_supervisor,
    {
        "end_workflow": END,
        "rewrite": "writer",
        "force_exit": END
    }
)
app = workflow.compile()

## 4. Execute with Runtime Arguments
When executing your application, you pass a dictionary containing your state inputs as the first argument, and the RunnableConfig dictionary as the second argument.

# Initial State Inputinitial_state = {
    "draft_content": "", 
    "quality_score": 0, 
    "feedback_notes": [], 
    "approval_status": "",
    "loop_count": 0
}

print("--- RUN 1: Restricting to MAX 1 Retry ---")# Run with max_retries set to 1config_run_1 = {"configurable": {"max_retries": 1}}
app.invoke(initial_state, config_run_1)


print("\n--- RUN 2: Restricting to MAX 4 Retries ---")# Run the exact same compiled app again, but allow up to 4 retries dynamicallyconfig_run_2 = {"configurable": {"max_retries": 4}}
app.invoke(initial_state, config_run_2)

## Output Simulation

--- RUN 1: Restricting to MAX 1 Retry ---
✍️ [Loop 1] Writer: Generating draft...
⚠️ Safety Guard: Dynamic max retries limit (1) hit! Forcing exit.

--- RUN 2: Restricting to MAX 4 Retries ---
✍️ [Loop 1] Writer: Generating draft...
✍️ [Loop 2] Writer: Generating draft...
✍️ [Loop 3] Writer: Generating draft...
✍️ [Loop 4] Writer: Generating draft...
⚠️ Safety Guard: Dynamic max retries limit (4) hit! Forcing exit.

## Key Takeaways

* No Re-compilation Needed: You compile the workflow graph (workflow.compile()) just once. You alter its behavior purely by shifting inputs inside invoke().
* Deep Propagation: LangGraph manages passing this config dictionary down automatically. If your node calls another LLM or chain inside it, the configuration passes through to those sub-steps as well.

Would you like to see how to use config parameters to connect a persistent database checkpointer, allowing you to save and resume individual user state histories using unique thread_id keys?

