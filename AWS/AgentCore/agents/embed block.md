By embedding a structured YAML metadata block at the absolute top of your docs/specs.md file, your subagents can mechanically inspect the file's state before running. This serves as a fail-safe programmatic lock.
Here is how to set up the metadata system, along with the updated agent definitions to read and write it.

Step 1: The docs/specs.md Template Structure

When the planner subagent creates or modifies the specification file, it must always prepend this frontmatter block to the top of the file:
markdown
---
status: DRAFT # Options: DRAFT, APPROVED
version: 1.0.0
last_modified: 2026-10-19
reviewed_by: USER
---

# Technical Specification: [Feature Name]
... rest of the document ...
Use code with caution.

Step 2: Update the Subagent Configurations

Update your agent definitions inside .claude/agents/ to strictly enforce and check this state block.

1. Update architect-planner.md

Force this agent to lock the file into DRAFT status whenever it runs or edits the plan.
markdown
---
name: architect-planner
description: Call this to create or modify technical specifications. Runs when creating a plan or processing user feedback.
tools: [Read, Write, Edit]
model: sonnet
---
You are an expert Software Architect. 

CRITICAL PROTOCOL:
1. When creating or editing `docs/specs.md`, you MUST start the file with the following exact YAML metadata block:
   ---
   status: DRAFT
   version: [Increment version if updating]
   ---
2. Incorporate the user's latest requirements or feedback into the markdown file.
3. Once the file is written with `status: DRAFT`, explicitly halt execution and say: 
   "⚠️ Specifications updated in `docs/specs.md` with status: DRAFT. Please review the file. Reply with 'Approved' to authorize code generation, or provide feedback comments to refine."
Use code with caution.

2. Update code-generator.md

Turn this agent into a strict gatekeeper that throws an error if it reads status: DRAFT.
markdown
---
name: code-generator
description: Call this to implement application code based on docs/specs.md.
tools: [Read, Write, Edit, Glob]
model: sonnet
---
You are an Expert Software Engineer.

CRITICAL PROTOCOL:
1. **FIRST STEP**: You MUST read `docs/specs.md` and inspect the YAML frontmatter block at the top.
2. **GATEKEEPER CHECK**: 
   - If `status: DRAFT`, STOP IMMEDIATELY. Output this error message to the parent session: 
     "❌ Execution Halted: `docs/specs.md` is still in DRAFT status. Human approval is required."
   - If `status: APPROVED`, proceed to step 3.
3. Read the rest of the specifications and project standards to generate the codebase files cleanly.
Use code with caution.

Step 3: How the Parent Agent (Orchestrator) Bridging Works

Since Claude Code runs the main terminal session, it acts as the bridge that flips the flag from DRAFT to APPROVED when you say the word.
When you type "Approved" or "Looks good, proceed" in the chat, Claude Code's parent layer handles the metadata updates seamlessly using the following routine:
[ You say: "Approved!" ]
        │
        ├──► 1. Parent Agent calls Edit tool on "docs/specs.md"
        │       └───► Changes `status: DRAFT` ──► `status: APPROVED`
        │
        └──► 2. Parent Agent invokes "code-generator"
                └───► Generator reads file ──► Sees APPROVED ──► Writes Code 🎉

💡 Pro-Tip for Complete Automation

To make sure you never have to manually touch the markdown file yourself, you can append a quick baseline instruction to your global CLAUDE.md file:
"When the user explicitly types 'Approved', the primary agent must immediately update the frontmatter metadata in docs/specs.md to change status: DRAFT to status: APPROVED before passing execution to the code-generator subagent."
This creates a bulletproof, machine-readable lock state that keeps your subagents perfectly aligned without executing external script tools.
Would you like me to write the final code-evaluator.md prompt to also inspect this metadata flag, or would you like to see a complete template of a finalized specs.md file?
