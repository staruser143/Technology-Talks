If you're preparing for CCAR-P, it is important to understand that Claude's permission model is layered. Many exam questions test evaluation order rather than individual settings.

1. High-Level Components

Claude Agent SDK / Claude Code permission system consists of:

Hooks
Deny Rules (disallowed_tools)
Ask Rules
Permission Mode
Allow Rules (allowed_tools)
Runtime approval callback (canUseTool)

Anthropic documents a specific evaluation order when a tool is requested.

Permission Evaluation Flow

When Claude wants to execute a tool:

Tool Request
     |
     v
1. Hooks
     |
     v
2. Deny Rules (disallowed_tools)
     |
     v
3. Ask Rules
     |
     v
4. Permission Mode
     |
     v
5. Allow Rules (allowed_tools)
     |
     v
6. canUseTool callback


This order is explicitly documented by Anthropic.

2. Hooks

Hooks are custom interception points.

Common hook:

PreToolUse


Runs before a tool executes.

Example:

def pre_tool_use(tool_name, input):
    if tool_name == "Bash" and "rm -rf" in input:
        return deny


Hooks can:

Allow
Deny
Modify behavior
Log activity

Important CCAR-P nuance:

A hook "allow" does NOT bypass deny rules, ask rules, or permission checks later in the pipeline.

So:

Hook = Allow
Deny Rule = Match

Result = DENIED


Deny still wins.

3. Deny Rules (disallowed_tools)

These are hard blocks.

Example:

{
  "disallowed_tools": [
      "Bash",
      "Write"
  ]
}


Meaning:

Claude can never use:
- Bash
- Write


Key behavior:

DENY > EVERYTHING


Anthropic specifically states that deny rules are enforced even when using bypassPermissions.

Example:

{
  "disallowed_tools": ["Bash"]
}


Even:

--permission-mode bypassPermissions


cannot execute Bash.

4. Ask Rules

Ask rules force human approval.

Example:

{
  "ask": [
      "Bash(git push *)",
      "Bash(rm *)"
  ]
}


Meaning:

Always ask before:
- git push
- rm commands


Important:

Ask rules override allow rules.

Allow Rule = Match
Ask Rule = Match

Result = Ask User


Anthropic states ask rules still require approval even in bypassPermissions.

5. Permission Modes

Permission mode decides the default behavior when the tool reaches this stage.

Mode 1: Default / Manual
Ask before risky operations


Common behavior:

Operation	BehaviorRead	Auto
Write	Ask
Bash	Ask

Used for normal development.

Mode 2: acceptEdits
Auto-approve file edits
Ask for commands


Example:

Action	ResultEdit file	Allowed
Create file	Allowed
Bash command	Ask

Anthropic describes acceptEdits as automatically approving file operations while retaining approval requirements for other risky actions.

Good for:

Trusted coding sessions

Mode 3: dontAsk
Never prompt


Behavior:

If approval needed -> deny


Example:

Tool requires approval
Mode = dontAsk

Result = DENIED


Anthropic notes that ask-rule matches and user-interaction tools are denied because this mode never prompts.

Good for:

Batch jobs
CI checks
Non-interactive workflows

Mode 4: plan

Planning-only mode.

Purpose:

Claude can analyze
Claude can plan

Cannot automatically make changes


Anthropic states file-edit and shell-write operations are routed for approval regardless of allow rules in this mode.

Good for:

Architecture reviews
Migration planning
Code investigation

Mode 5: bypassPermissions

Most dangerous mode.

Approve nearly everything automatically


Example:

claude --permission-mode bypassPermissions


Claude can:

Edit files
Execute commands
Use tools

without asking.

However there are exceptions:

Deny Rules still apply
{
  "disallowed_tools": ["Bash"]
}


Bash remains blocked.

Ask Rules still apply

Anthropic states ask rules continue to force approval even under bypass mode.

Critical Deletes

Dangerous removals can still be protected.

6. allowed_tools

Defines what tools may be used.

Example:

{
  "allowed_tools": [
      "Read",
      "Write",
      "Grep"
  ]
}


Think of it as a whitelist.

Allowed:

Read
Write
Grep


Not allowed:

Bash
WebFetch
MCP tools


unless separately approved.

7. disallowed_tools

Blacklist.

Example:

{
  "disallowed_tools": [
      "Bash",
      "WebFetch"
  ]
}


Everything else is allowed to continue through the pipeline.

Exam tip:

Deny rules always win.
```【1-9500e8】【3-2fdfd0】

---

# 8. Allow vs Deny

Suppose:

```json
{
  "allowed_tools": [
      "Bash"
  ],

  "disallowed_tools": [
      "Bash"
  ]
}


Result:

DENIED


Because:

Deny > Allow
```【1-9500e8】

---

# 9. `canUseTool` Callback

Final runtime approval point.

Example:

```python
def can_use_tool(tool_name, tool_input):
    if tool_name == "Bash":
        return False
    return True


Used for:

Custom approvals
Business logic
Dynamic policy checks

Anthropic indicates ask-rule matches and certain tool invocations fall through to this callback for approval decisions.

CCAR-P Mental Model

Memorize this:

Hook
  ↓
Deny
  ↓
Ask
  ↓
Permission Mode
  ↓
Allow
  ↓
canUseTool


And these three rules:

Rule #1
Deny beats everything.

Rule #2
Ask can still force approval
even when bypassPermissions is enabled.

Rule #3
bypassPermissions ≠ ignore security.

Deny rules and ask rules still work.


These are the nuances that frequently appear in advanced Claude Agent SDK and CCAR-P scenario questions.
