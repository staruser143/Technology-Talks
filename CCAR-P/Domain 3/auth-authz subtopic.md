## Domain 3, sub-topic: Auth/authz gap analysis

This sub-topic is really about spotting where an integration's access model is *broader* than the task actually requires — the exam wants you to find the gap between "what this system needs to do its job" and "what it's actually able to do."

### 1. Authentication vs. authorization — keep these distinct

Authentication answers "who/what is this?" Authorization answers "what is this identity allowed to do?" Exam scenarios often blur these on purpose — a system that authenticates properly (verified API key, valid session) can still have a broken *authorization* model if it doesn't then constrain what that authenticated identity can actually touch.

### 2. Service accounts vs. per-user (delegated) credentials

This is the highest-yield distinction in this sub-topic:
- **Service account / single shared credential**: Claude (or the integration layer) authenticates once with one powerful identity and uses it for every user's requests. Simple to build, but it means the system's technical access ceiling is "whatever that service account can do" — not "whatever this specific user is allowed to do."
- **Per-user (delegated) credentials**: the integration acts *as* the requesting user, respecting whatever that user's own permissions already are (OAuth on-behalf-of flows are the common mechanism). This means Claude can never access more than the human it's acting for could access directly.

**Watch for the "confused deputy" problem**: a system with a powerful service account, asked to act on behalf of a low-privilege user, can end up letting that user access data or trigger actions they shouldn't be able to — because the *system's* credential is what actually executes the action, not the user's. If a scenario describes an integration authenticating once and serving requests for many different users with different permission levels, that's almost always a designed-in gap unless it's explicitly checking per-user permissions before acting.

### 3. Scope minimization / least privilege

Even with the right *kind* of credential, the specific scope granted matters: read-only vs. read-write, single-resource vs. whole-system access, time-limited vs. indefinite tokens. The recurring exam trap is an integration requesting broader scope than the task needs "in case it's useful later" — that's exactly the kind of unjustified scope creep the exam wants you to flag and narrow.

### 4. Why this matters more for agentic systems specifically

This connects directly back to Domain 1: an agent that autonomously decides its own next actions is also autonomously deciding *which permissions to exercise*, in a loop, without a human reviewing each step. Combine an over-privileged credential with an agentic loop and a source of untrusted input (a webpage, an email, a document the agent reads as part of its task) and you get a classic prompt-injection-to-action risk: instructions hidden in that untrusted content could manipulate the agent into using its available permissions in ways nobody intended. The tighter the scope, the smaller that blast radius — this is a big reason "narrow the permissions" is so often the correct exam answer for agentic designs specifically.

### 5. Irreversible/high-stakes actions need a human gate, tied to authz

This is where auth/authz overlaps with the human-in-the-loop material from Domain 1: read-only or easily-reversible actions (looking something up, drafting a message) are lower-risk to leave fully automated. Write actions with real consequences (sending money, deleting records, publishing content, modifying account settings) are where the exam expects you to recognize that scope should be tightly bounded and often gated behind explicit human confirmation — not just "technically possible for the agent to do it if it decides to."

### 6. Attribution and audit logging

When an agent acts using a service account, logs may only show "the service account did X" — not which end user's request actually triggered it. For compliance and incident-response purposes, integrations need to preserve **who actually initiated the action**, not just which credential executed it. A scenario describing "we can see the system did X, but not why or for whom" is pointing at this gap.

### 7. Multi-tenant data isolation

If Claude's retrieval or tool access spans multiple customers/tenants (e.g., a shared vector database), a common gap is missing metadata filtering that would prevent one tenant's query from ever surfacing another tenant's data — this is a specific, common flavor of the "over-broad scope" problem from #3, worth recognizing on sight.

---

