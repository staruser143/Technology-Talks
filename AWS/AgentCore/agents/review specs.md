To implement a human-in-the-loop approval gate that prevents Claude Code from jumping straight from planning to coding, you do not need code. You just need to implement a structural constraint inside the subagent system prompts and establish a tracking mechanism via a status indicator or user-facing explicit stops. [1] (https://github.com/FareedKhan-dev/claude-code-staff-engineer)
By leveraging explicit loop guardrails in your agent files and setting up your global instructions file (CLAUDE.md), you can control this lifecycle perfectly. Here is exactly how to configure it: [1] (https://www.linkedin.com/pulse/claudemd-explained-missing-layer-agent-design-sudeep-devkota-rcfue)

Step 1: Enforce the "Approval Gate" via CLAUDE.md

Your root-level CLAUDE.md rules over all subagent behaviors. Use it to explicitly forbid autonomous multi-agent handoffs without your text verification. [1] (https://github.com/FareedKhan-dev/claude-code-staff-engineer), [2] (https://www.linkedin.com/pulse/claudemd-explained-missing-layer-agent-design-sudeep-devkota-rcfue)
Add this to your CLAUDE.md:
markdown
## Subagent & Workflow Protocols
- Always execute tasks in a sequential loop: Plan ➔ User Review ➔ Generate ➔ Evaluate.
- **CRITICAL**: The `architect-planner` agent must never trigger code generation. It must pause, present the path to `docs/specs.md`, and await human approval in the terminal.
- Do not proceed to `code-generator` unless the user explicitly inputs "Approved" or "Proceed".
- If the user provides feedback/rejections, you must execute `architect-planner` again with the feedback comments until an approval is received.
Use code with caution.

Step 2: Refine the Subagent Instructions

Update your Markdown subagents in .claude/agents/ to actively listen for approval states. [1] (https://code.claude.com/docs/en/sub-agents)

1. Update architect-planner.md

Add a distinct Feedback Loop Protocol to its system instructions:
markdown
---
name: architect-planner
description: Call this first to generate technical specifications. Also call this whenever the user rejects specs or provides feedback comments.
tools: [Read, Write, Edit]
model: sonnet
---
You are an expert Software Architect. 

CRITICAL PROTOCOL:
1. Read the user's initial requirements OR read the existing `docs/specs.md` along with the user's latest feedback comments.
2. Update or rewrite `docs/specs.md` incorporating the feedback.
3. Once written, output this exact message: "⚠️ Spec updated. Please review `docs/specs.md`. Reply with 'Approved' to generate code, or provide feedback to refine the plan."
4. Stop executing immediately. Do not attempt to invoke any other agent.
Use code with caution.

2. Update code-generator.md

Block this agent from starting if it detects an unapproved state. [1] (https://github.com/FareedKhan-dev/claude-code-staff-engineer)
markdown
---
name: code-generator
description: Call this to write source code ONLY after the user has explicitly approved the specs.md file.
tools: [Read, Write, Edit, Glob]
model: sonnet
---
You are an Expert Software Engineer. 

CRITICAL PROTOCOL:
1. Verify that the user has explicitly given approval to proceed in the conversation history. 
2. If the user's last message was feedback/rejection, DO NOT write code. Tell the main agent: "Cannot proceed. Specs require refinement. Please route back to the architect-planner."
3. If approved, read `docs/specs.md` and implement the source files perfectly.
Use code with caution.

Step 3: How You Run the Loop Natively in the Terminal

When interacting with Claude Code in your command line, the interactive loop will natively progress like this based on those prompt guards:

Scenario A: You Reject and Loop Back

1. You: "Create a payment gateway endpoint using the planner agent."
2. Claude Code: (Spawns architect-planner, writes docs/specs.md) ➔ "⚠️ Spec updated. Please review docs/specs.md. Reply with 'Approved' to generate code, or provide feedback to refine the plan." [1] (https://github.com/FareedKhan-dev/claude-code-staff-engineer)
3. You: (You open docs/specs.md, read it, and don't like a detail) ➔ "The database schema is missing a retry_count field. Fix the specs."
4. Claude Code: (Sees feedback, matches description, spawns architect-planner again to rewrite the file) ➔ "⚠️ Spec updated with the retry_count field. Please review docs/specs.md..."

Scenario B: You Approve and Move Forward

5. You: "Approved. Proceed to code generation."
6. Claude Code: (Sees "Approved", delegates to code-generator because the guardrail condition is met) ➔ Generates your clean codebase matching your exact specifications.
This prompt-based routing leverages Claude Code's native task-matching engine to handle conditional branching and loop-backs flawlessly without a drop of execution code. [1] (https://medium.com/@abhinav.dobhal/claude-codes-feature-dev-is-256-lines-of-markdown-not-an-agent-swarm-f9f073856348), [2] (https://code.claude.com/docs/en/sub-agents)
Would you like me to show you how to embed a Metadata Block / Status Flag directly inside the top of the specs.md file (e.g., Status: DRAFT vs Status: APPROVED) so the subagents can read the file state mechanically?
