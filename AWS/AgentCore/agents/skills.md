Yes, combining Claude Skills and Subagents is the ultimate architecture pattern for advanced workflows. [1] (https://medium.com/@SandeepTnvs/claude-skills-vs-sub-agents-architecture-use-cases-and-effective-patterns-3e535c9e0122)
While Subagents act as your isolated "workers" who handle specific runtime contexts, Skills act as "expert playbooks" (SKILL.md) that provide domain-specific knowledge and repeatable execution workflows on demand. [1] (https://medium.com/@unicodeveloper/10-must-have-skills-for-claude-and-any-coding-agent-in-2026-b5451b013051), [2] (https://www.reddit.com/r/ClaudeAI/comments/1obq6wq/understanding_claude_skills_vs_subagents_its_not/)
By decoupling your process instructions into Skills, you can inject them dynamically into your automated subagents. This drastically slims down your subagent files, prevents token bloat, and makes your architecture modular. [1] (https://medium.com/@nir.shilon/claude-code-skills-a-practical-guide-19122b268385), [2] (https://claude.com/resources/articles/skills-explained), [3] (https://x.com/dani_avila7/article/2041185537172607014)

The Architecture: Skill-Guided Subagents

Instead of writing massive prompt guidelines inside each subagent's markdown configuration, you define small, highly specific playbooks in your .claude/skills/ directory. Your agents then load these playbooks only when needed. [1] (https://academy.claude.com/courses/introduction-to-agent-skills/what-are-skills), [2] (https://medium.com/@SandeepTnvs/claude-skills-vs-sub-agents-architecture-use-cases-and-effective-patterns-3e535c9e0122), [3] (https://claude.com/resources/articles/skills-explained), [4] (https://medium.com/@nir.shilon/claude-code-skills-a-practical-guide-19122b268385)
                [ @workflow-master ]
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
 [@architect-planner]  [@code-generator]  [@code-evaluator]
        │                │                │
        ▼                ▼                ▼
   Loads Skill:     Loads Skill:     Loads Skill:
 🛠️ spec-authoring 💻 clean-coder   🔍 qa-audit

Step 1: Create the Playbook Skills

Save these standard SKILL.md bundles inside your project folder at .claude/skills/. [1] (https://academy.claude.com/courses/introduction-to-agent-skills/what-are-skills)

1. .claude/skills/spec-authoring/SKILL.md

markdown
---
name: spec-authoring
description: Playbook for generating technical specs with proper architectural constraints.
---
When authoring architectural specifications:
1. Always establish clear API endpoint contracts (Paths, Methods, Payloads, Responses).
2. Document explicit data validation requirements and structural boundaries.
3. Factor in transient failures by forcing a retry mechanism layout (exponential backoff).
4. Outline precise error-handling mapping (e.g., HTTP statuses 400, 401, 422, 500).
Use code with caution.

2. .claude/skills/clean-coder/SKILL.md

markdown
---
name: clean-coder
description: Playbook for engineering safe, production-grade applications.
---
When generating source code implementation:
1. Wrap async/await boundaries with descriptive try/catch blocks.
2. Isolate internal business logic from router layers (Separation of Concerns).
3. Ensure every functional logic path has inline, descriptive documentation.
4. Adhere strictly to the project's static naming schemes (e.g., camelCase for TypeScript, snake_case for Python).
Use code with caution.

3. .claude/skills/qa-audit/SKILL.md

markdown
---
name: qa-audit
description: Playbook for conducting strict security and code health compliance audits.
---
When evaluating source code repositories:
1. Check for hardcoded credentials, secret keys, or exposed debug variables.
2. Confirm that all incoming parameters undergo structural input sanitization.
3. Ensure mock test configurations cover minimum test thresholds (aim for 80%+ coverage).
4. Verify that the implemented file matches the designated spec contracts 1:1.
Use code with caution.

Step 2: Bind the Skills to Your Subagents

Now that your portable playbooks exist, you can simplify your .claude/agents/ definitions. You tell the agents to automatically invoke or reference the matching skill. [1] (https://www.truefoundry.com/blog/claude-skills), [2] (https://medium.com/@unicodeveloper/10-must-have-skills-for-claude-and-any-coding-agent-in-2026-b5451b013051)

Updated .claude/agents/architect-planner.md

markdown
---
name: architect-planner
description: Call this to create or modify technical specifications.
tools: [Read, Write, Edit]
model: sonnet
---
You are an expert Software Architect. 

CRITICAL PROTOCOL:
1. Execute the `/spec-authoring` skill playbook to analyze requirements.
2. Ensure you prepend the YAML block (`status: DRAFT`) to `docs/specs.md`.
Use code with caution.

Updated .claude/agents/code-generator.md

markdown
---
name: code-generator
description: Call this to implement application code.
tools: [Read, Write, Edit, Glob]
model: sonnet
---
You are an Expert Software Engineer.

CRITICAL PROTOCOL:
1. Verify `docs/specs.md` shows `status: APPROVED`.
2. Load and follow the `/clean-coder` skill playbook to implement the required source code files cleanly.
Use code with caution.

Updated .claude/agents/code-evaluator.md

markdown
---
name: code-evaluator
description: Performs strict QA verification.
tools: [Read, Glob, Grep]
model: sonnet
---
You are a Strict Senior QA Engineer.

CRITICAL PROTOCOL:
1. Verify `docs/specs.md` shows `status: APPROVED`.
2. Execute the `/qa-audit` skill playbook against the generated files and output your PASS/FAIL breakdown.
Use code with caution.

Why This Simplifies and Enhances the Workflow

1. Portable Expertise: You can now invoke /qa-audit or /clean-coder directly as slash commands in your main terminal chat at any time (/qa-audit), without needing to run the full master automation loop. [1] (https://code.claude.com/docs/en/skills), [2] (https://medium.com/@unicodeveloper/10-must-have-skills-for-claude-and-any-coding-agent-in-2026-b5451b013051)
2. Lean Subagents: The subagents can focus entirely on state control (checking file statuses, loop triggers, and writing data) rather than getting bogged down by walls of engineering guidelines inside their core prompts.
3. Cross-Surface Consistency: Because Agent Skills follow an open standard, the exact same .claude/skills/ folders will work seamlessly across Claude Code, the Claude Desktop App, and the Claude API Platform without modification. [1] (https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview), [2] (https://code.claude.com/docs/en/skills)
Would you like me to map out a mock directory tree showing exactly how your .claude/agents/ and .claude/skills/ folders should align within your codebase to ensure Claude Code picks them up cleanly? [1] (https://code.claude.com/docs/en/skills)
