For modular prompts, is there a template mechanism to be used for building it from other reusable parts. Also, from where do store and retrieve the base prompts accordingly


Good question, and the honest answer splits into two very different situations — Skills have a real, defined mechanism; modular prompts mostly don't.

**Skills — yes, there's a defined structure and storage location**

A Skill is a folder, not just a prompt string:

```
my-skill/
├── SKILL.md          ← required: YAML frontmatter (name, description) + markdown instructions
├── references/        ← optional: detailed docs, loaded only when referenced
├── scripts/            ← optional: executable code Claude can run
├── templates/          ← optional: file templates
└── assets/              ← optional: static files
```

The `SKILL.md` frontmatter's `description` field is what makes it discoverable — that's the lightweight metadata loaded for every Skill up front (the progressive-discovery mechanism), and the full body only gets read into context when Claude judges it's actually relevant to the current task.

Storage location depends on scope — this exists at multiple levels with a defined precedence order:
- **Personal/user-level**: `~/.claude/skills/` — available across all your projects
- **Project-level**: `.claude/skills/` inside a specific repo — shared with anyone who clones it, good for standardizing team workflows
- **Plugin-level**: bundled with a plugin, namespaced so it doesn't conflict with other skills

So for Skills specifically, "where do you store and retrieve them" has a concrete, documented answer: a filesystem convention Claude (via Claude Code, and the equivalent mechanism in the Agent SDK) scans at session start.

**Modular prompts — no, there's no equivalent Anthropic-prescribed mechanism**

This is the important distinction to hold onto for the exam: "modular prompts" isn't a specific Anthropic product feature with a defined storage/templating system the way Skills is. It's a general software-engineering *pattern* you implement yourself in your own application code — there's no `MODULAR.md` convention or built-in template engine from Anthropic. In practice, teams build this themselves with ordinary tools: a templating library (Jinja2, string formatting, etc.) to assemble blocks at request time, and storage that's just... wherever your team stores config/content normally — a database, a set of files in your own repo, a prompt-management service, version control alongside your application code. The "mechanism" is whatever your engineering team designs, not something Anthropic ships.

**Why this distinction actually matters for the exam**: if a scenario asks you to choose *between* modular prompts and Skills for a given reuse need, the deciding factor is often exactly this — do you want Claude to *discover and load automatically* based on relevance (Skills, with its defined discovery mechanism), or do you want deliberate, manual composition controlled entirely by your own application logic (modular prompts, built with whatever tooling you choose)? Skills trade some control for automatic relevance-matching; modular prompts give you full control but require you to build and maintain the composition logic yourself.

Want to try scenario practice now that tests choosing between caching, modular prompts, and Skills for different situations?