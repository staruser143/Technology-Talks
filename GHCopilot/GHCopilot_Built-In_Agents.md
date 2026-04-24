**GitHub Copilot in Visual Studio Code does provide built‑in agents**, and as of **VS Code 1.116 (April 2026)** these are **first‑class, native capabilities**, not just extensions.

Below is a **concise, architect‑level breakdown** of what is *actually built in* versus what is optional or custom.

***

## ✅ Built‑in Copilot agents in VS Code

### 1. **Plan Agent** (built‑in, first step)

**Purpose:** Planning before coding

*   Analyzes your **entire workspace**
*   Asks clarifying questions
*   Produces a **step‑by‑step implementation plan**
*   Designed to be handed off to an implementation agent

Typical use:

```text
Create a plan to add OAuth2 login to this Spring Boot app
```

This agent exists specifically to **separate planning from execution**, which is critical for large refactors or feature work.    [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/overview)

***

### 2. **Local Agent** (interactive)

**Purpose:** Hands‑on coding inside VS Code

*   Runs **locally inside the editor**
*   Can:
    *   Edit multiple files
    *   Use terminal
    *   Run tests
    *   Iterate on failures
*   Best for **interactive, supervised work**

This is the default agent most developers interact with first.    [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/overview), [\[clouddev.blog\]](https://clouddev.blog/GitHub/Copilot/the-four-types-of-github-copilot-agents-local-background-cloud-and-sub-agents-explained/)

***

### 3. **Background Agent** (Copilot CLI)

**Purpose:** Autonomous work without blocking you

*   Runs **locally but outside the VS Code UI**
*   Survives VS Code restarts
*   Can execute long‑running tasks
*   Can delegate work to cloud agents

Example use cases:

*   Large refactors
*   Code cleanup
*   Test generation

 [\[clouddev.blog\]](https://clouddev.blog/GitHub/Copilot/the-four-types-of-github-copilot-agents-local-background-cloud-and-sub-agents-explained/), [\[visualstud...gazine.com\]](https://visualstudiomagazine.com/articles/2026/04/16/vs-code-updates-boost-ai-agents-terminal-control-and-copilot-workflow.aspx)

***

### 4. **Cloud (Coding) Agent**

**Purpose:** Fully autonomous development in the cloud

*   Executes on **GitHub‑hosted environments**
*   Triggered by:
    *   Assigning a GitHub Issue to Copilot
    *   Delegation from VS Code chat
*   Produces **pull requests automatically**
*   Ideal for:
    *   Background feature work
    *   Fixes while you’re offline
    *   Team‑shared tasks

This agent is distinct from local VS Code agents and runs on GitHub Actions.    [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/copilot-cloud-agent), [\[github.blog\]](https://github.blog/changelog/2025-07-14-start-and-track-github-copilot-coding-agent-sessions-from-visual-studio-code/)

***

## 🧠 Built‑in “chat participants” (often mistaken for agents)

These are **not autonomous agents**, but **domain‑aware assistants** that ship by default:

| Participant  | What it does                                      |
| ------------ | ------------------------------------------------- |
| `@workspace` | Understands **entire codebase structure**         |
| `@terminal`  | Translates natural language → terminal actions    |
| `@vscode`    | Knows VS Code settings, commands, editor behavior |

Example:

```text
@workspace explain how authentication is implemented
```

They provide **contextual intelligence**, not autonomous execution.    [\[bing.com\]](https://bing.com/search?q=GitHub+Copilot+Chat+agents+VS+Code+%40workspace), [\[4sysops.com\]](https://4sysops.com/archives/what-are-vs-code-chat-participants-chat-variables-and-slash-commands-for-github-copilot/)

***

## ❌ What is **not** built‑in by default

*   ❌ Custom domain agents (you define these)
*   ❌ Third‑party agents (Anthropic / OpenAI personas)
*   ❌ Skills, MCP servers, hooks (opt‑in configuration)

These require `.agent.md`, `.prompt.md`, MCP configuration, or org enablement.    [\[learn.microsoft.com\]](https://learn.microsoft.com/en-us/training/modules/configure-customize-github-copilot-visual-studio-code/)

***

## ✅ Summary (architect‑friendly)

**Out of the box, VS Code + GitHub Copilot includes:**

✅ Plan Agent  
✅ Local Agent  
✅ Background Agent  
✅ Cloud Coding Agent  
✅ Workspace / Terminal / VS Code chat participants  
✅ Session management, delegation, debug logs

In short:

> ** GitHub Copilot ships with real, built‑in agents in VS Code, capable of planning, coding, debugging, and producing PRs autonomously.**

***


