Perfect — given your stack and standards, **GitHub Copilot can be configured very effectively** so you **do NOT have to repeat coding standards or architecture rules in every prompt**.

Below is a **ready‑to‑commit, enterprise‑grade Copilot setup** tailored for:

*   ✅ **Java 21 + Spring Boot**
*   ✅ **Hexagonal / Clean Architecture**
*   ✅ **Sonar rules**
*   ✅ **OWASP security**
*   ✅ **Internal structured logging format**

This is exactly how mature teams are standardizing Copilot usage.

***

# ✅ What this setup gives you

*   Copilot **automatically knows** your:
    *   Architecture constraints
    *   Security and compliance expectations
    *   Logging and error‑handling rules
    *   Code quality expectations (Sonar)
*   Works **across the whole team**
*   No need to repeat standards in every Copilot Chat prompt
*   Versioned, auditable, and enforceable via repo

***

# 📁 Recommended Repository Structure

```text
.github/
├── copilot-instructions.md          # Global, always-on rules
├── instructions/
│   ├── java-backend.instructions.md # Java + Spring specific
│   ├── architecture.instructions.md
│   ├── security.instructions.md
│   └── logging.instructions.md
```

This keeps things **modular, readable, and evolvable**.

***

# 1️⃣ `.github/copilot-instructions.md`

## (Global, Always-On Instructions)

Apply to **all Copilot Chat interactions**.

```md
# GitHub Copilot – Project Instructions

This repository implements a backend service using Java and Spring Boot.
All AI-generated suggestions must strictly follow the standards below.

## Core principles
- Follow Hexagonal / Clean Architecture.
- Prioritize readability, testability, and maintainability.
- Generate production-ready code; avoid examples or pseudo-code.
- Prefer clarity over brevity.

## Language & Platform
- Java 21
- Spring Boot 3.x
- Maven build system

## Quality gates (non-negotiable)
- Code must comply with SonarQube rules (clean code, no code smells).
- No duplicated logic.
- No commented-out code.
- Explicit error handling is required.

## Security baseline
- Follow OWASP Top 10 guidelines.
- Never log secrets, tokens, credentials, or PII.
- Input validation is mandatory at all boundaries.

## Validation before final output
- Ensure code compiles.
- Ensure imports are correct.
- Highlight any architectural or security trade-offs explicitly.
```

***

# 2️⃣ `java-backend.instructions.md`

## (Java + Spring Boot conventions)

```md
---
applyTo: "**/*.java"
description: "Java & Spring Boot backend standards"
---
## Spring Boot standards
- Use constructor injection only.
- Avoid field injection and static state.
- Use @Validated for request validation.
- Use records for immutable DTOs where appropriate.

## API design
- RESTful principles.
- Use OpenAPI annotations.
- Consistent HTTP status codes.
- Controllers must be thin (no business logic).

## Error handling
- Centralized exception handling via @ControllerAdvice.
- Map domain errors → application errors → HTTP responses.
- No generic RuntimeException.

## Testing
- JUnit 5 for unit tests.
- Mockito for mocking.
- Integration tests for persistence and messaging when applicable.
```

***

# 3️⃣ `architecture.instructions.md`

## (Hexagonal / Clean Architecture enforcement)

```md
---
applyTo: "**/*.java"
description: "Hexagonal / Clean Architecture rules"
---
## Architecture rules
- Domain layer must be framework-agnostic.
- No Spring, JPA, or infrastructure dependencies in domain.
- Application layer orchestrates use cases.
- Infrastructure layer adapts external systems.

## Dependency direction
- Infrastructure → Application → Domain
- Domain must not depend on any outer layer.

## Packaging
- Package by feature, not by technical layers.
- Ports are interfaces owned by the domain or application layer.
- Adapters implement ports.

## Forbidden anti-patterns
- Repositories used directly in controllers
- Business logic in controllers
- Entity exposure through APIs
```

***

# 4️⃣ `security.instructions.md`

## (OWASP & secure coding rules)

```md
---
applyTo: "**/*.java"
description: "Security & OWASP guidelines"
---
## Input & output security
- Validate all external inputs.
- Use allowlists instead of denylists.
- Encode output where applicable.

## Authentication & Authorization
- Enforce authorization checks per use case.
- Avoid security logic in controllers.
- Clearly separate authn and authz responsibilities.

## Data protection
- Never log sensitive data.
- Do not expose stack traces in API responses.
- Use secure defaults for configuration.

## OWASP alignment
- Prevent SQL injection (no string-concatenated queries).
- Prevent mass assignment.
- Prevent broken object-level authorization.
```

***

# 5️⃣ `logging.instructions.md`

## (Internal structured logging format)

```md
---
applyTo: "**/*.java"
description: "Logging standards"
---
## Logging framework
- Use SLF4J only.
- No System.out or System.err.

## Format
- Structured JSON logs.
- Include:
  - correlationId / traceId
  - service name
  - operation or use case
  - error code (if applicable)

## Logging rules
- Log business events at INFO.
- Log validation issues at WARN.
- Log technical failures at ERROR.
- Never log sensitive or personal data.

## Error logging
- Errors must include context but not stack trace by default.
- Stack traces only at DEBUG or when explicitly required.
```

***

# 🚨 Important Limitation (Be aware)

✅ These instructions **DO apply to**:

*   Copilot Chat
*   Copilot Agent workflows

❌ These instructions **do NOT apply to**:

*   Inline autocomplete (“ghost text” suggestions)

👉 Inline suggestions rely heavily on **existing code, structure, and examples** — which your clean architecture will naturally reinforce.

***

# ✅ Best Practices (from experience)

✔ Keep instruction files **short and directive**  
✔ Avoid prose — use bullet‑style rules  
✔ Treat instruction files like **coding standards as code**  
✔ Review them quarterly with your architecture board

***
