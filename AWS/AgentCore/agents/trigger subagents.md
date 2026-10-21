Yes, absolutely. In Claude Code, you can explicitly trigger any custom subagent by typing its name with the @ prefix directly in your terminal prompt.
When you configure a markdown file in .claude/agents/architect-planner.md, it registers @architect-planner as a global command in your interactive workspace environment.

How Your Approval Loop Looks Using @ Commands

Instead of relying on Claude Code's automated intent routing, using @ commands gives you complete manual control over the pipeline execution steps.

1. Initialize the Spec Setup

You: @architect-planner create a specification for an email notification service.
Claude Code: (Spawns planner agent, writes docs/specs.md with status: DRAFT) ➔ "⚠️ Specifications updated with status: DRAFT. Please review the file..."

2. Loop Back with Feedback (If needed)

If you read the file and want modifications, invoke the planner directly again with your feedback notes:
You: @architect-planner add a retry logic of 3 attempts to the email spec.
Claude Code: (Re-spawns planner agent, modifies the file, leaves it as status: DRAFT)

3. Update Status and Generate Code

Once the spec looks perfect, you flip the flag and explicitly invoke your code generator:
You: Update status to APPROVED in specs.md and run @code-generator to build it.
Claude Code: (Main session edits the YAML block to status: APPROVED, spawns the generator, the generator verifies the flag, and outputs the codebase)

⚡ Troubleshooting Agent Invocation Names

• File-Based Naming: The name you use after the @ symbol corresponds exactly to the name: field defined in the agent's YAML frontmatter (or the filename if the name field is omitted).
• Autocomplete: When inside a Claude Code terminal session, typing @ will display a dropdown menu allowing you to autocomplete your custom registered agents (@architect-planner, @code-generator, @code-evaluator).
Would you like me to map out how the final @code-evaluator should be structured to verify both the APPROVED spec file and the final codebase health?
