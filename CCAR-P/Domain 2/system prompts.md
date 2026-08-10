## Design System Prompts, Templates, and Guardrails — deeper dive

### 1. System prompts: what they're actually for

A system prompt sets persistent context that applies across the entire interaction — role, tone, constraints, domain knowledge, output format expectations — separate from the user's specific request. The exam-relevant distinction: **system prompt content should be things that are true for every request in this deployment, not things specific to one conversation.** A common design flaw is cramming request-specific instructions into the system prompt (rebuilding it per-request) when they belong in the user turn instead — this also directly undermines caching, since the system prompt is exactly the kind of content meant to stay stable and reusable.

### 2. Templates: consistency at scale

A template is a reusable structure with variable slots filled in per-request — rather than hand-writing a new prompt for every instance of a repeated task. Templates matter for two reasons the exam cares about:
- **Consistency**: the same task performed 10,000 times should follow the same instructions and format every time, not drift based on who wrote which prompt variant.
- **Maintainability**: fixing a flaw or updating a policy means editing one template, not hunting down every place a similar instruction was hand-written.

A common exam trap: a system with many near-duplicate hand-written prompts for what's structurally the same task (e.g., a slightly different prompt per product category doing the same classification) — the fix is usually consolidating into one parameterized template, not maintaining a growing pile of near-identical variants.

### 3. Guardrails: three flavors worth distinguishing

- **Instructional guardrails**: explicit rules in the prompt about what the model should and shouldn't do (tone, scope, refusal conditions, format compliance). Cheapest to implement, but rely on the model actually following the instruction — not a hard technical boundary. This is the same limitation flagged in your Domain 3 auth scenario: instructions are not the same as an enforced control.
- **Structural/output guardrails**: constraining the *shape* of what comes back — structured output formats (JSON schemas, constrained generation), which make certain classes of malformed or off-policy output mechanically harder to produce, not just discouraged.
- **System-level guardrails**: checks that happen outside the model entirely — input/output filtering, validation layers, human review gates for high-stakes actions. These are the actual enforced boundary when the consequence of a guardrail failure is serious, echoing the "instructions aren't a substitute for enforced access control" lesson directly.

**Exam-relevant judgment**: for anything low-stakes, instructional guardrails alone are often sufficient and appropriately lightweight. For anything high-stakes (financial actions, medical guidance, compliance-sensitive outputs), the exam expects you to recognize that instructional guardrails alone are insufficient — they should be paired with structural or system-level enforcement, the same discipline as knowing when a workflow needs a human-in-the-loop gate versus when full model autonomy is fine.

### 4. Where guardrails should live: prompt vs. system design

A subtle but testable point: not every guardrail belongs in the prompt at all. Rate limits, content filters, permission boundaries, and action confirmation gates are often better enforced in the surrounding system than requested of the model — the same "confused deputy" lesson from auth/authz, generalized: a rule the model is merely *told* to follow is not the same as a rule the system *enforces*.

### 5. Guardrail failure modes to recognize

- **Over-restrictive guardrails**: so many constraints stacked in the system prompt that the model becomes unable to helpfully respond to legitimate edge cases — competing instructions can also dilute attention the same way competing tasks do in decomposition.
- **Guardrails that only exist in instructions for consequential actions**: exactly the gap from your CRM/hospital-style scenarios, just relocated to prompt design — "we told it not to" isn't a guardrail for anything genuinely high-stakes.
- **Stale or conflicting guardrails accumulated over time**: similar to capability bloat — guardrails added incrementally for past incidents that may no longer apply, never revisited, sometimes contradicting newer instructions.

---

Want to move into scenario practice for this sub-topic now?
