Below is a **ready‑to‑drop, production‑grade**  
✅ **`.github/copilot-instructions.md` for Enterprise Java**  
designed for **Spring Boot, clean architecture, security, and regulated environments**.

You can copy‑paste this as‑is.

***

# GitHub Copilot Instructions — Enterprise Java

## 1. General Coding Standards

*   Target **Java 17 or later**
*   Follow **clean, readable, self‑documenting code**
*   Prefer **composition over inheritance**
*   Avoid premature optimization
*   Favor **explicitness over cleverness**

***

## 2. Architecture & Design Principles

*   Follow **Clean Architecture / Hexagonal Architecture**
    *   Controllers → Services → Domain → Infrastructure
*   Keep **frameworks at the edges**
*   Domain logic must be **framework‑agnostic**
*   Avoid anemic domain models where business logic belongs in services or domain entities

**Strictly avoid:**

*   God services
*   Static state
*   Tight coupling between layers
*   Business logic in controllers

***

## 3. Spring Boot & Dependency Injection

*   Use **constructor injection only**
*   Do **not** use field injection
*   Prefer **explicit beans** over component scanning when clarity matters
*   Use Spring stereotypes appropriately:
    *   `@RestController`
    *   `@Service`
    *   `@Component`
    *   `@Repository`

***

## 4. API Design (REST)

*   Follow RESTful design conventions
*   Use **DTOs** for request/response models
*   Never expose entities directly through APIs
*   Validate inputs using **Bean Validation (Jakarta Validation)**
*   Use **OpenAPI / Swagger annotations** for public APIs

```java
@Validated
public record CreateOrderRequest(
    @NotNull UUID customerId,
    @Positive BigDecimal amount
) {}
```

***

## 5. Exception Handling & Errors

*   Use **centralized exception handling** (`@ControllerAdvice`)
*   Do not expose internal exception messages directly
*   Return **consistent error response structures**
*   Map domain exceptions to appropriate HTTP status codes

***

## 6. Security Requirements (Mandatory)

*   Assume **zero‑trust** networking
*   Never log:
    *   PII
    *   PHI
    *   Secrets
*   Do not hardcode:
    *   Credentials
    *   Tokens
    *   Keys
*   Use structured logging (e.g., SLF4J + Logback or JSON logging)
*   Include **correlation IDs / trace IDs** in logs
*   Prefer **OAuth2 / JWT‑based authentication**
*   Validate authorization explicitly at service boundaries

***

## 7. Logging & Observability

*   Use **structured logs**
*   Avoid `System.out.println`
*   Log:
    *   Entry/exit at service boundaries (sparingly)
    *   Errors with context
*   Do not log entire request/response payloads by default
*   Make logs **production‑safe by default**

***

## 8. Testing Standards

*   Always include tests for:
    *   Business logic
    *   Edge cases
*   Use:
    *   **JUnit 5**
    *   **Mockito** (or equivalent)
*   Favor **unit tests over integration tests**
*   Mock infrastructure dependencies
*   Name tests descriptively

```java
shouldThrowExceptionWhenCustomerIsInactive()
```

***

## 9. Persistence & Data Access

*   Prefer **Spring Data JPA** with clear repository boundaries
*   Avoid complex queries inside services
*   Keep SQL/JPA queries readable and maintainable
*   Handle transactions explicitly where needed
*   Do not leak persistence models outside data access layer

***

## 10. Performance & Scalability

*   Avoid blocking calls in reactive or async flows
*   Be mindful of N+1 queries
*   Prefer pagination and batching for large datasets
*   Do not introduce unnecessary caching without justification

***

## 11. Configuration & Secrets

*   Externalize all configuration
*   Use environment‑specific configuration files
*   Never assume local defaults apply in production
*   Follow **12‑Factor App principles**

***

## 12. Documentation Expectations

*   Add Javadoc for:
    *   Public APIs
    *   Non‑trivial business logic
*   Comments should explain *why*, not *what*
*   Keep README and API docs up to date

***

## 13. Code Generation Guidelines for Copilot

When generating code, you must:

✅ Include error handling  
✅ Follow the architecture rules above  
✅ Use enterprise‑safe defaults  
✅ Prefer clarity over brevity  
❌ Do not introduce experimental frameworks  
❌ Do not generate placeholder logic without comments

***

## 14. Libraries & Vendor Lock‑In

*   Favor **open‑source, widely adopted libraries**
*   Avoid proprietary vendor APIs unless explicitly instructed
*   Design abstractions that allow swapping implementations

***

## 15. Compliance & Audit Readiness

*   Assume code may be reviewed for:
    *   Security audits
    *   Regulatory compliance
    *   Production incidents
*   Design defensively and explicitly

***

### ✅ Overall Intent

> Generate **production‑grade, secure, maintainable, enterprise‑ready Java code** suitable for long‑lived systems.

***


