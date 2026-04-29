Below is a **clean, practical developer‑ready security checklist** you can use **during design, coding, code review, and before release**.  
This is written so teams can literally check items off.

***

# ✅ Secure Coding Developer Checklist

Use this checklist for **every feature, service, or change**.

***

## 1️⃣ Design & Architecture (Before Coding)

*   ☐ Threat model completed (entry points, trust boundaries, assets)
*   ☐ OWASP Top 10 risks considered for this feature
*   ☐ Authentication and authorization approach clearly defined
*   ☐ Least‑privilege principle applied to users, services, and data access
*   ☐ Secure defaults chosen (deny by default, explicit allow)
*   ☐ Data classification identified (PII / PHI / Confidential / Public)

***

## 2️⃣ Input Validation & Data Handling

*   ☐ All external inputs treated as **untrusted**
*   ☐ Server‑side validation implemented (not only client‑side)
*   ☐ Allow‑lists used instead of block‑lists
*   ☐ Input length, type, format, and range validated
*   ☐ File uploads validated (type, size, content)
*   ☐ Path traversal protections applied for file access

***

## 3️⃣ Output Encoding & Injection Prevention

*   ☐ Parameterized queries used for all database access
*   ☐ ORM used safely (no dynamic query building)
*   ☐ Output encoded based on context (HTML, JS, URL, SQL)
*   ☐ No direct user input passed to shell / OS / eval
*   ☐ Templates escape output by default

***

## 4️⃣ Authentication

*   ☐ Standard auth framework/library used (OAuth2, OIDC, SAML)
*   ☐ Passwords hashed using modern algorithms (bcrypt / Argon2)
*   ☐ No custom crypto or auth logic written
*   ☐ MFA enforced where required
*   ☐ Account lockout / throttling implemented
*   ☐ Session tokens are random, short‑lived, and rotated

***

## 5️⃣ Authorization & Access Control

*   ☐ Authorization enforced **server‑side**
*   ☐ Role‑based or attribute‑based access clearly defined
*   ☐ Authorization checked on **every sensitive action**
*   ☐ No reliance on UI‑only restrictions
*   ☐ Cross‑tenant / cross‑user isolation verified

***

## 6️⃣ API & Microservices Security

*   ☐ Every API endpoint authenticated
*   ☐ JWTs validated (issuer, audience, signature, expiry)
*   ☐ Rate limiting applied
*   ☐ Sensitive APIs protected from direct internet exposure
*   ☐ Service‑to‑service authentication enforced (mTLS / tokens)
*   ☐ Request/response payloads validated

***

## 7️⃣ Secrets & Configuration

*   ☐ No secrets committed to source control
*   ☐ Secrets stored in a secrets manager
*   ☐ Environment‑specific configs separated
*   ☐ Secrets rotated periodically
*   ☐ Application runs with minimal permissions
*   ☐ Debug mode disabled in production

***

## 8️⃣ Error Handling & Logging

*   ☐ Generic error messages returned to users
*   ☐ Stack traces not exposed externally
*   ☐ No secrets, tokens, or PII logged
*   ☐ Logs sanitize user inputs (prevent log forging)
*   ☐ Sufficient audit logs for sensitive actions
*   ☐ Logging centralized and monitored

***

## 9️⃣ Dependency & Supply Chain Security

*   ☐ Dependencies sourced from trusted registries
*   ☐ Dependency versions pinned
*   ☐ Unused dependencies removed
*   ☐ SCA scan run (Dependabot / Snyk / OWASP Dependency‑Check)
*   ☐ Known CVEs evaluated and mitigated

***

## 🔟 Secure Defaults & Platform Hardening

*   ☐ HTTPS enforced everywhere
*   ☐ Secure HTTP headers enabled (CSP, HSTS, X‑Frame‑Options)
*   ☐ CSRF protection enabled (where applicable)
*   ☐ CORS rules restrictive and explicit
*   ☐ Deserialization secured / disabled if unnecessary

***

## 1️⃣1️⃣ Automated Security Testing

*   ☐ SAST run in CI pipeline
*   ☐ DAST or API security tests executed
*   ☐ Dependency scanning enabled in CI
*   ☐ Container/image scanning performed
*   ☐ Security failures break the build (gated CI)

***

## 1️⃣2️⃣ Code Review (Security Focus)

*   ☐ Input validation reviewed
*   ☐ Authorization logic reviewed carefully
*   ☐ No hard‑coded credentials found
*   ☐ Logging reviewed for sensitive data
*   ☐ Exception handling reviewed
*   ☐ Security checklist explicitly validated in PR

***

## 1️⃣3️⃣ Pre‑Release & Production Readiness

*   ☐ Pen test or security review completed (for critical systems)
*   ☐ Monitoring & alerting configured
*   ☐ Incident response steps documented
*   ☐ Audit trail verified
*   ☐ Compliance requirements met (HIPAA / PCI / GDPR if applicable)

***

## 🧠 Golden Rule (For Developers)

> **If attacker‑controlled data enters your code, it must be validated, constrained, and neutralized before use.**

***


