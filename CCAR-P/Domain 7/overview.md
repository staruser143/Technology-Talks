## Domain 7: Developer Productivity & Operational Enablement (7%)

The smallest domain, and it's genuinely where the earlier questions about `settings.json`, `allowedTools`/`disallowedTools`, `permissionMode`, and the workspace trust dialog actually belong — worth circling back to those now that we're in the right domain for them. Three objectives:

### 1. Configure Claude tools and environments for teams (e.g., Claude Code)

This is about setting up Claude Code (or the Agent SDK) for a team, not just an individual — meaning configuration needs to work across multiple developers with different needs, consistently and safely:

- **`settings.json` hierarchy**: settings can live at multiple levels — user-level (personal preferences, applies across all your projects), project-level (checked into the repo, shared with the whole team, enforces team standards), and enterprise/managed level (organization-wide policy, typically taking precedence over individual or even project settings for things like security policy). Knowing this hierarchy matters for team configuration: something you want *every* team member to have (a required guardrail, a standard tool restriction) belongs at project or enterprise level, not left to individual preference.
- **`allowedTools` / `disallowedTools`**: this is the practical, everyday implementation of the least-privilege principle from your Domain 3 auth/authz material — instead of an agent having access to every possible tool, a team can explicitly scope which tools are permitted (or explicitly forbidden) for a given project or role. This is the direct, concrete mechanism behind "remove capabilities the role doesn't need" (recall Sample Question 1 from the official guide).
- **`permissionMode`**: controls how much a session can do without asking — ranging from requiring explicit confirmation for consequential actions (file writes, command execution) to more autonomous modes. This is the practical implementation of your Domain 5 human-in-the-loop material: matching permission mode to the actual stakes of what a given project/session does, the same way you matched HITL mode to a workflow's volume/stakes profile.
- **Workspace trust dialog**: a safety gate that requires explicit user confirmation before a new/untrusted directory's settings and configuration are allowed to take effect — directly relevant if you recall the prompt-injection material from Domain 5: a malicious or compromised repository could otherwise smuggle in configuration that changes agent behavior without the user's awareness. Trusting a workspace is itself a consequential-action decision, deserving the same scrutiny as other guardrail gates.
- **Additional directories**: controlling what filesystem scope a session can access beyond the immediate project — another direct instance of scope minimization, this time applied to file-system access rather than tool access or data access.

### 2. Improve developer workflows using AI-assisted tooling

This covers team-level productivity patterns: standardizing team knowledge/conventions via project-level `CLAUDE.md` files (persistent instructions every team member's session picks up automatically — tying back to your context-management material on where durable, must-not-be-lost information belongs), custom slash commands for repeated team workflows, subagents scoped to specific team tasks (code review, testing conventions), and Skills shared across a team's projects (your Domain 2 prompt-reuse material, now applied at the team-tooling level).

### 3. Support debugging and operational issue resolution

Using Claude Code/Agent SDK itself as a tool for diagnosing and resolving operational issues — this connects directly to your Domain 4 diagnostic material (stop_reason triage, log analysis) and Domain 3 observability material, now framed as "how does a team actually use Claude-powered tooling to debug problems in their own systems," including using `/context` to diagnose session-level issues (your earlier scenario), and structured incident-response workflows where Claude assists with log analysis, root-causing, or reproducing a reported bug.

---

Given this domain is smaller (7%) and pulls together mechanics you've largely already mastered conceptually (least privilege, HITL, context management, prompt reuse) just applied to team tooling specifically, want to move straight into scenario practice?


