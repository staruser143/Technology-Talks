Yes, you can absolutely automate the entire lifecycle by creating a Master Orchestrator Subagent. [1] (https://medium.com/@urvvil08/built-an-orchestrator-ai-agent-that-takes-my-issue-to-pull-requests-ab1912c93b52), [2] (https://www.reddit.com/r/ClaudeAI/comments/1mdtguv/can_subagents_run_other_subagents/)
Because Claude Code's runtime supports a Task tool—which allows one agent to programmatically spawn and delegate to other agents—you can build a supervisor agent that monitors your manual approvals and loops back automatically if a stage fails. [1] (https://www.channel.tel/blog/claude-code-subagents-orchestrator-pattern), [2] (https://www.langchain.com/blog/introducing-dynamic-subagents-in-deep-agents)
Here is how to map a zero-code @workflow-master orchestrator to handle the loop completely hands-off.

Step 1: Create the Master Orchestrator Agent

Save this file as .claude/agents/workflow-master.md. It acts as the manager overseeing the other three subagents. [1] (https://www.reddit.com/r/ClaudeAI/comments/1mdtguv/can_subagents_run_other_subagents/)
markdown
---
name: workflow-master
description: Core manager to orchestrate the software development lifecycle across planning, generation, and evaluation agents.
tools: [Read, Write, Edit, Task]
model: sonnet
---
You are the Master Workflow Orchestrator. Your sole job is to guide the user's feature request through our multi-agent pipeline using the `Task` tool to spawn specialized subagents.

CRITICAL PROCESS LIFECYCLE:

1. **PHASE 1: PLAN**
   - Delegate the initial requirements to the planner subagent using the Task tool: `Invoke @architect-planner to generate specs for: [User Request]`.
   - Wait for it to complete and output `docs/specs.md` with status `DRAFT`.

2. **PHASE 2: HUMAN-IN-THE-LOOP GATE**
   - **PAUSE AND PROMPT**. Output this text verbatim to the terminal:
     "⏸️ **Orchestrator Notice**: Planning phase complete. Please review `docs/specs.md`. 
     Type **'Approved'** to proceed, or provide feedback comments to refine the specifications."
   - Halt execution completely. Wait for the user's next message.

3. **PHASE 3: BRANCHING LOGIC ON USER INPUT**
   - **If the user provides feedback comments instead of approval:** 
     Immediately spawn the planner agent again: `Invoke @architect-planner to update specs using these comments: [User Feedback]`. Then return to Step 2.
   - **If the user inputs 'Approved':**
     First, use your Edit tool to modify `docs/specs.md` changing `status: DRAFT` to `status: APPROVED`. Proceed to Step 4.

4. **PHASE 4: CODE GENERATION**
   - Spawn the generator subagent: `Invoke @code-generator to implement the approved specs in docs/specs.md`.
   - Wait for the code files to be generated.

5. **PHASE 5: QA EVALUATION & SELF-CORRECTION LOOP**
   - Spawn the evaluator subagent: `Invoke @code-evaluator to perform a strict review of the new code against docs/specs.md`.
   - Inspect the evaluator's markdown report:
     - **If PASS:** Report success to the user and mark the feature complete.
     - **If FAIL:** Programmatically route the failures straight back to the developer: `Invoke @code-generator to fix the code to resolve these evaluator failures: [Insert Failure Report]`. Repeat the evaluation step until it passes.
Use code with caution.

Step 2: The New Automated Workflow Experience

Now, your interaction with the terminal becomes vastly simpler. You only talk to one agent, and it pulls the levers behind the scenes:
1. Kick off the automated run:You: @workflow-master build a secure payment retry endpoint for our checkout service.
2. The Automation takes over:
The Master Orchestrator automatically calls @architect-planner via the Task tool. The planner outputs the blueprint and hands control back to the Master. [1] (https://www.mindstudio.ai/blog/build-custom-sub-agents-claude-code-yaml), [2] (https://www.langchain.com/blog/introducing-dynamic-subagents-in-deep-agents)
3. The Gate hits:
The Master pauses and outputs:⏸️ Orchestrator Notice: Planning phase complete. Please review docs/specs.md. Type 'Approved' to proceed, or provide feedback comments to refine the specifications.
4. You provide input:
	• Scenario A (Feedback): You type "The timeout should be 5 seconds instead of 10." The Master automatically catches this, wakes back up, forces the planner to rewrite the file, and reprompts you for approval.
	• Scenario B (Approval): You type "Approved". The Master immediately updates the markdown frontmatter metadata flag to status: APPROVED, invokes @code-generator to build the app, and routes the code directly into @code-evaluator. If the evaluator complains about a bug, the master loops the code generation step internally without bothering you until it achieves a green PASS check. [1] (https://code.claude.com/docs/en/workflows), [2] (https://www.youtube.com/watch?v=vDVSGVpB2vc&vl=en&t=83)
Would you like to explore enabling Claude Code's Agent Teams mode (CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1) so your subagents can actively chat in parallel split-panes, or does this hierarchical Task-based loop fit your deployment goals?
