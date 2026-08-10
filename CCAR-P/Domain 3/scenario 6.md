**Scenario**

A project management SaaS company adds a Claude-powered assistant that lets users ask questions like "what tasks are overdue on my team's projects?" and get answers pulled live from the company's internal project database. The engineering team's implementation: the Claude integration authenticates to the project database using a single powerful service account (originally created for internal data migrations) that has read/write access to every project, task, and comment across *all* customer organizations on the platform. When a user asks a question, their query is passed to Claude along with this service account's credentials, and Claude constructs and runs the database query needed to answer it.

The engineering team's reasoning: "It's simpler to have one credential for the integration, and Claude is smart enough to only query for the data relevant to the user's question, so in practice it'll only ever pull what that user asked about."

**Question**: What is the core problem with this design, and what should the team do instead?

A) There's no real problem — since Claude is instructed to only query relevant data, and the service account being powerful is just a backend implementation detail invisible to users, this is a reasonable trade-off for simplicity.

B) The core problem is a confused-deputy / over-privileged service account: the integration's actual technical access (read/write, all customers, all data) is far broader than what any single request should ever need (read-only, one customer's data). Relying on Claude's good behavior to self-limit queries is not an access control — a misinterpreted question, a prompt injection in task/comment content, or simply an error could result in cross-tenant data exposure or unintended writes. The team should switch to per-user delegated credentials (or at minimum a service account scoped read-only to the requesting user's own organization), enforced at the database/query layer, not just relied upon via instructions to Claude.

C) The core problem is that Claude shouldn't be allowed to construct database queries at all; the team should hardcode a fixed set of pre-approved queries instead.

D) The core problem is latency — broad database access makes queries slower, and the team should optimize the service account's query performance.

Take your best guess and I'll walk through it.



Correct — and this is close to the textbook case of everything this sub-topic warns about, stacked together on purpose.

**Why B is right**

Three distinct problems from the material above are all present in this one design, and B is the only answer that names the actual root cause instead of a symptom:

- **Confused deputy, explicitly.** The system authenticates once with a powerful identity (originally built for *internal migrations*, not user-facing queries) and executes on behalf of whoever happens to be asking. The credential that actually runs is far more powerful than any individual user's own permissions — a classic confused-deputy setup, and the exact pattern flagged in the material as "almost always a designed-in gap unless it's explicitly checking per-user permissions."
- **Massive scope creep.** Read/write, across *all* customer organizations, for a feature that only ever needs read access to one organization's data (the asking user's own team). This is the least-privilege violation named directly — broader access than the task needs, "in case it's useful," except here it's not even a maybe-useful justification, it's leftover access from an unrelated tool.
- **"Claude will behave correctly" is not an access control.** This is the most important part of the wrong reasoning to spot: the team is treating good instruction-following as a *security boundary*. It isn't one. The material calls this out specifically — combine a broad credential with something that constructs and executes queries based on interpreting user input, and you get real exposure the moment interpretation goes wrong: a misread question, an ambiguous request, or (the sharper risk) a prompt injection sitting inside task descriptions or comments that Claude reads as part of answering the query, which could manipulate what gets queried or written. The permission boundary has to be enforced at the system level (the database/query layer respecting per-user or per-tenant scope), not hoped for at the model-behavior level.

The fix — per-user delegated credentials, or at minimum a read-only service account scoped to the requesting user's own organization, enforced where the query actually executes — directly closes all three gaps at once: it removes the confused-deputy pattern (the credential now can't exceed what the user themselves could do), it enforces least privilege (read-only, single-tenant), and it moves the safety guarantee from "we told Claude to behave" to "the system physically cannot return or write data outside scope, regardless of what Claude constructs."

**Why the others are wrong**

- **A)** is the exact mistake B calls out — treating instruction-following as sufficient access control, and dismissing the credential's actual power as "just an implementation detail" when it's precisely the thing that determines the real blast radius if anything goes wrong (misinterpretation, injection, bug).
- **C)** overcorrects into removing Claude's flexibility entirely rather than fixing the actual access boundary. A fixed set of pre-approved queries might incidentally reduce risk, but it doesn't address the underlying problem (the credential itself is over-scoped) and unnecessarily sacrifices the assistant's usefulness for a wide range of legitimate questions it could otherwise answer well within a properly scoped credential.
- **D)** completely misidentifies the category of problem — this is an auth/authz gap, not a performance issue. Optimizing query speed for an overprivileged credential doesn't reduce what that credential is capable of exposing; it just makes the exposure faster.

**Pattern to remember**: whenever a scenario describes a single, powerful, shared credential handling requests on behalf of many different users or tenants, and the safety argument offered is some version of "the AI will only use it appropriately" — that's the signal. Good model behavior is not a substitute for an enforced access boundary; the two are entirely different layers, and only one of them holds up against a bug, a misunderstanding, or malicious input.

Want another auth/authz scenario — maybe one testing scope minimization or the audit-logging/attribution angle — or should we move to observability at scale next?