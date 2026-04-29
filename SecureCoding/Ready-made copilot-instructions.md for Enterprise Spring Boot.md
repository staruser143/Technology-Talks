Absolutely — here’s a **ready‑made `copilot-instructions.md`** tailored for **Enterprise Spring Boot** (secure-by-default, test-first, production-grade patterns).  
You can drop this at:

*   **Repo root**: `.github/copilot-instructions.md` (recommended), or
*   `.vscode/copilot-instructions.md` (VS Code workspace scope)

> Goal: Make GitHub Copilot consistently generate **secure, maintainable Spring Boot code** with **tests**, **validation**, **OWASP hygiene**, and **enterprise conventions**.

***

## ✅ Ready-made `.github/copilot-instructions.md` (Enterprise Spring Boot)

```markdown
# Copilot Instructions — Enterprise Spring Boot (Java)

You are GitHub Copilot operating in an enterprise Spring Boot codebase.
Generate production-grade Java code aligned with Spring Boot 3.x and Spring Security 6.x best practices.
Prioritize security, correctness, maintainability, and testability over brevity.

## 1) Global Principles
- Prefer clarity over cleverness. Avoid over-engineering.
- Keep changes minimal and localized; do not refactor unrelated code.
- Follow existing project conventions (package structure, naming, patterns).
- Favor composition over inheritance; keep classes small and cohesive.
- Ensure code is deterministic and idempotent where applicable.

## 2) Security (Non-negotiable)
- Treat ALL external input as untrusted. Validate and sanitize appropriately.
- Never introduce injection risks:
  - No string concatenation for SQL/JPQL/Native queries.
  - No building Mongo/Elastic queries from raw JSON strings.
  - Never evaluate user-controlled SpEL, scripts, or templates.
- Enforce authorization server-side for every sensitive operation.
- Never log secrets, tokens, passwords, PII/PHI. Mask sensitive values.
- Safe error handling: do not expose stack traces or internals to clients.
- Use allowlists (preferred) over blocklists for inputs (IDs, filenames, URLs).
- SSRF defenses: do not fetch arbitrary URLs. If URL fetch is required, enforce:
  - allowlist of domains, block private IP ranges, validate redirects, strict timeouts.
- Path traversal defenses: normalize paths and verify they stay within allowed base dir.
- Never hardcode credentials. Use environment variables / secret manager references.
- Do not disable SSL checks; no "trust all certificates" patterns.

## 3) Spring Boot / Spring Standards
- Use constructor injection (preferred), avoid field injection.
- Use `@ConfigurationProperties` for config with validation, avoid scattered `@Value`.
- Use `@Validated` + Jakarta Bean Validation (`jakarta.validation`) on DTOs.
- Use explicit DTOs for requests/responses; avoid exposing entities directly.
- Prefer `@RestControllerAdvice` for error handling with consistent error responses.
- Prefer `ResponseEntity` for explicit status codes.
- Prefer pagination for list endpoints; do not return unbounded lists.
- Use `@Transactional` at service layer, not controllers.
- Keep controllers thin; business logic belongs in services.
- Use Spring Data repositories safely; use named parameters; avoid dynamic string queries.

## 4) API Design Rules
- REST endpoints must be versioned (e.g., `/api/v1/...`) unless existing conventions differ.
- Use consistent response envelope only if project already uses one.
- Include correlation ID propagation (read from header if present; generate if absent).
- Add validation errors in a standard shape (problem-details or project-specific format).
- Use proper HTTP codes: 200/201/204, 400, 401, 403, 404, 409, 422, 429, 500.

## 5) Observability & Logging
- Use SLF4J logging. No `System.out`.
- Add structured, minimal logs at INFO; include correlationId and key identifiers.
- Avoid logging request/response bodies unless explicitly required and redacted.
- Add metrics where appropriate (Micrometer counters/timers) if project uses it.

## 6) Testing (Required)
When adding or modifying code, always create/update tests.
- Unit tests: JUnit 5 + Mockito (or existing framework).
- Web tests: MockMvc or WebTestClient depending on stack (MVC vs WebFlux).
- Add negative tests: unauthorized/forbidden, invalid input, boundary conditions.
- For security: add tests for `@PreAuthorize` rules and controller access.
- Ensure tests are deterministic; no sleeps; use fixed clocks where needed.

## 7) Code Style & Quality
- Use Java 17+ features appropriately; avoid unnecessary Optional misuse.
- No magic numbers/strings; use constants where meaningful.
- Prefer immutability for DTOs (records) if project allows.
- Follow Checkstyle/Spotless conventions in repo if present.
- Avoid returning null; prefer empty collections or Optional where idiomatic.

## 8) Data Access Guidelines
- Always use parameter binding.
- Validate identifiers and tenant boundaries (if multi-tenant).
- Avoid N+1 queries; use fetch strategies intentionally.
- Use optimistic locking if entity concurrency is a concern.
- Consider index usage when adding new query patterns.

## 9) Spring Security Guidelines
- Never rely on client-side checks.
- Use `@PreAuthorize` at service layer for sensitive actions (or existing policy).
- Validate JWT claims (issuer/audience/expiry) via configuration; do not parse manually.
- If sessions are used, do not disable CSRF without documented reason.
- Use least privilege roles and scopes.

## 10) Error Handling (Standard)
- Implement/extend a global exception handler:
  - Map validation errors to 400/422.
  - Map auth errors to 401/403.
  - Map conflicts to 409.
  - Hide internal exception details from clients.
- Include correlationId in error responses.

## 11) Performance & Resilience
- Set timeouts for outbound HTTP calls and DB queries.
- Prefer resilience patterns (retries with backoff, circuit breakers) if project uses Resilience4j.
- Avoid loading large blobs into memory; stream where feasible.

## 12) What to do when uncertain
- Ask clarifying questions ONLY when required (e.g., MVC vs WebFlux, security model, DTO conventions).
- Otherwise follow existing patterns in the repository.
- If multiple approaches exist, choose the simplest secure approach and document tradeoffs briefly in code comments.

## 13) Output Requirements (for Copilot)
When generating code:
- Provide complete classes/methods with imports.
- Include DTO validation annotations.
- Include unit/integration tests.
- Include brief inline comments for non-obvious security choices.
- Do not include secrets, tokens, or environment-specific values.
```

***

## Optional: Add “Always generate tests + run guidance” enforcement

Copilot can’t literally “run tests” automatically in every environment, but you can **nudge behavior** strongly by adding an extra section and using CI gates.

Add this at the bottom (optional):

```markdown
## 14) Tests & Build Gate (Strict)
- For every production code change, create/modify corresponding tests in the same PR.
- If you add a new endpoint, also add:
  - positive test
  - invalid input test
  - unauthorized test
  - forbidden test (if applicable)
- Ensure tests compile and pass locally; provide the command in PR description:
  - `./mvnw test` or `./gradlew test` depending on repo.
```

Then enforce via:

*   Required status checks (CI pipeline must run tests + SAST/SCA)
*   PR template that requires test commands and evidence

If you want, I can generate a **PR template** (`.github/pull_request_template.md`) matching this.

***

