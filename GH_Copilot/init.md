Yes — **for Copilot Chat in VS Code**, you *can* provide **persistent, repository-wide coding standards and tech-stack rules** so you don’t have to repeat them in every prompt. This is done via **custom instruction files** (Markdown) that VS Code automatically injects into Copilot Chat context. [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions), [\[docs.github.com\]](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions)

> ⚠️ Important limitation: **These instruction files affect Copilot *Chat* (and agentic tasks), but not inline “ghost text” autocomplete suggestions as you type.** [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)

Below is the practical, “team-ready” way to set this up.

***

## 1) The main solution: `.github/copilot-instructions.md` (project-wide, always-on)

Create this file in your repo:

    <repo-root>/.github/copilot-instructions.md

VS Code will treat it as **always-on instructions automatically included in every chat request** for that workspace. [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions), [\[docs.github.com\]](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions)

### What to put inside (recommended structure)

Use it like a “Copilot onboarding guide”:

*   **Tech stack** (language versions, frameworks, libraries)
*   **Architecture conventions** (layering, folder structure, boundaries)
*   **Coding standards** (style, naming, error handling, logging)
*   **Security rules** (OWASP, secrets handling, authN/authZ policies)
*   **Testing expectations** (unit tests required, frameworks, coverage goals)
*   **Build/run commands** (how to run locally, how to validate changes)

GitHub Docs also recommends including context like how to **build, test, and validate** changes. [\[docs.github.com\]](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions)

### Example `copilot-instructions.md` (template)

*(You can copy/paste and adapt)*

```md
# Project Copilot Instructions

## Tech stack (must follow)
- Language: Java 21
- Framework: Spring Boot 3.x
- Build: Maven
- API style: REST + OpenAPI
- Persistence: JPA (Hibernate) + PostgreSQL
- Messaging: Kafka
- Observability: OpenTelemetry + Prometheus metrics + structured JSON logs

## Coding standards
- Prefer constructor injection; avoid field injection.
- Use SLF4J; no System.out.println.
- Validate inputs; never trust client data.
- Prefer immutable DTOs/records where possible.
- Handle errors with consistent error responses and error codes.

## Architecture rules
- Maintain clean layering: controller -> service -> repository.
- No direct repository calls from controllers.
- No business logic in controllers.
- Cross-cutting concerns via filters/interceptors/aspects.

## Security & compliance
- Never log secrets, tokens, PII.
- Use parameterized queries; avoid string concatenation in SQL.
- Validate authorization on every protected endpoint.

## Testing requirements
- Add unit tests for new logic (JUnit 5 + Mockito).
- Add integration tests for repositories where needed (Testcontainers).
- Include edge cases and negative tests.

## Validation before final answer
- Ensure code compiles.
- Ensure formatting matches project conventions.
- Include any new files required (tests, configs, docs).
```

This becomes the “default rules” Copilot Chat uses automatically. [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions), [\[docs.github.com\]](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions)

***

## 2) More granular rules: `.github/instructions/*.instructions.md` (path/file specific)

When different parts of the repo have different standards (e.g., frontend vs backend), create one or more files like:

    <repo-root>/.github/instructions/backend.instructions.md
    <repo-root>/.github/instructions/frontend.instructions.md

These support **scoping** using YAML frontmatter (like `applyTo`) so rules apply only to matching files/paths. [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions), [\[docs.github.com\]](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions)

### Example: backend-specific rules

```md
---
applyTo: "**/*.java"
description: "Backend (Spring Boot) rules"
---
- Use Spring @RestController + @Validated for input validation.
- For mapping DTOs use MapStruct (not manual mapping unless trivial).
- Use ResponseEntity only when needed; otherwise return the type directly.
- Prefer package-by-feature structure under /src/main/java/com/acme/<feature>/*
```

### Example: frontend-specific rules

```md
---
applyTo: "**/*.{ts,tsx}"
description: "Frontend (React) rules"
---
- Use React functional components and hooks.
- Prefer TanStack Query for server state.
- Use Tailwind utility-first approach; no custom CSS unless required.
- All components must be accessible (ARIA where appropriate).
```

VS Code supports having **multiple instruction files**, and it combines them into the chat context (order not guaranteed). [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)

***

## 3) Agent-oriented instructions: `AGENTS.md` (optional)

If you use Copilot’s **agentic workflows / coding agents**, VS Code also supports **AGENTS.md** instructions files. [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions), [\[docs.github.com\]](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions)

This is useful when you want “agent behavior” rules, e.g., “always run tests, always update docs, always produce a PR summary”.

***

## 4) How to enable/use it in VS Code (practical steps)

### A) Ensure instruction files are used

VS Code documents that custom instructions can be configured to influence chat automatically using instruction files. [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)

In current VS Code builds, you typically:

*   Add the files in the repo
*   Open the repo workspace in VS Code
*   Use **Copilot Chat**
*   (Optional) Use **Chat Customizations editor** to manage them (“Chat: Open Chat Customizations”). [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)

### B) Bootstrap quickly with `/init` (recommended)

VS Code docs mention you can set up your project for AI with **`/init`** to generate custom instructions tailored to your project. [\[github.com\]](https://github.com/microsoft/vscode-docs/blob/main/docs/copilot/customization/custom-instructions.md), [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)

This is a great way to generate a first draft, then you refine it.

***

## 5) Key limitation (so you don’t get surprised)

### ✅ Works for:

*   Copilot Chat responses
*   Copilot “agent” style tasks

### ❌ Does *not* apply to:

*   **Inline autocomplete suggestions while typing** (ghost text)

VS Code clearly notes: **custom instructions are not taken into account for inline suggestions**. [\[code.visua...studio.com\]](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)

**Workaround for inline suggestions:** keep strong signals in codebase context:

*   clear folder structure
*   consistent patterns
*   lint rules + formatter config
*   example reference implementations  
    (Inline completion is heavily driven by local code context.)

***

## 6) Recommended “enterprise/team” pattern (minimal effort, maximum consistency)

If you want this to scale across teams:

1.  **Repo-wide:** `.github/copilot-instructions.md`
2.  **Per area:** `.github/instructions/*.instructions.md`
3.  **Keep it short:** bullet rules, avoid essays
4.  **Add “non-negotiables”:** security + logging + testing
5.  **Update quarterly:** treat it like “coding standards as code”

GitHub Docs describes combining repository-wide instructions with path-specific instruction files when files match a path pattern. [\[docs.github.com\]](https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions)

***

