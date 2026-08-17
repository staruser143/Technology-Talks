**Scenario**

A company builds a Claude-powered internal assistant for HR staff that can look up employee records and, when instructed, initiate a salary change request that goes into the payroll system. The system prompt includes this instruction: "Only initiate a salary change if the requesting HR staff member has explicitly confirmed the change amount and the employee's ID. Never initiate a change based on an ambiguous or unconfirmed request." The engineering team considers this sufficient, reasoning that "we've clearly instructed the model on exactly when it's allowed to act, so the guardrail is in place."

Three months in, an incident occurs: an HR staffer's message was ambiguously worded ("bump Sarah's salary like we discussed, same as last time"), and the model — trying to be helpful — initiated a salary change based on its best guess of what was meant, without the amount or employee ID actually being explicitly confirmed in that message. The change was caught before payroll processed it, but only because a separate manager happened to notice the request log by chance.

**Question**: What's the core gap here, and what should the team do differently?

A) The guardrail was fine as designed; the model simply made a mistake in this one instance, and the fix is to add a more detailed instruction to the system prompt explaining, with more examples, exactly what counts as an "ambiguous" request.

B) The core gap is that this consequential action (initiating a real payroll change) was protected only by an instructional guardrail — a rule the model was told to follow, not one the system enforced. Since a misinterpretation or edge case can cause the model to act outside the intended boundary despite the instruction, the fix is a system-level guardrail: require the salary-change action itself to be gated behind a structural check (e.g., explicit required parameters that must be present and unambiguous before the action tool can even be called, and/or a mandatory human confirmation step before the change is submitted to payroll) — not just an instruction asking the model to self-police ambiguity.

C) The core gap is that the model shouldn't have access to HR data at all; the fix is to remove the assistant's access to employee records entirely.

D) The core gap is that the incident was caught, which proves the current safeguards are working as intended; no changes are needed.

Take your best guess and I'll walk through it.


Correct — and this scenario is close to a direct extension of the guide's own Sample Question 1 (the refund/delete tool least-privilege example), just testing the *guardrail* angle of the same underlying principle rather than the *permission-scope* angle.

**Why B is right**

The system prompt's instruction was well-written — clear, specific, even correctly anticipating the failure mode ("never initiate a change based on an ambiguous or unconfirmed request"). That's exactly what makes this scenario useful: it shows that **a well-worded instructional guardrail still isn't a guarantee**, because it's fundamentally a request for the model to correctly judge, on its own, whether a given message clears an ambiguity threshold — and "bump Sarah's salary like we discussed, same as last time" is precisely the kind of borderline phrasing where reasonable interpretation can go either way. The model wasn't ignoring its instructions; it was trying to be helpful and made a judgment call that turned out wrong. That's the nature of instructional guardrails: they shape behavior probabilistically, they don't enforce a hard boundary.

This is the same "instructions are not a substitute for an enforced access control" lesson from your CRM auth scenario, relocated from *who can access what* to *what conditions must hold before a consequential action fires*. The fix follows the same logic: for a genuinely high-stakes action — a real payroll change, not a read-only lookup — the boundary needs to be enforced by the system, not requested of the model. Concretely, that means:
- **Structural gating**: the salary-change tool itself should require specific, explicit parameters (exact amount, exact employee ID) to even be callable — if the model can't extract those unambiguously from the conversation, the tool call simply can't be constructed, rather than the model having to self-assess "is this ambiguous enough to refuse."
- **Human confirmation before submission**: given this is exactly the kind of consequential, hard-to-reverse action the material flags as needing a human gate, a mandatory confirmation step before the change reaches payroll adds a system-level checkpoint that doesn't depend on the model getting its judgment right on the first try.

Notice also the detail that the incident "was caught before payroll processed it, but only because a manager happened to notice the request log by chance" — that's the exact same "safety net that isn't actually a safety net" pattern from your fintech feedback-loop scenario. Discovery by luck isn't a functioning safeguard; it's the absence of one that just happened not to cause harm this time.

**Why the others are wrong**

- **A)** tries to patch the problem by making the *instruction* more detailed — more examples of what counts as "ambiguous." This might modestly reduce the failure rate, but it doesn't change the fundamental structure of the guardrail: it's still asking the model to correctly self-judge ambiguity in real time, for every future edge case, including ones no example anticipated. This is the same "better phrasing fixes a structural problem" trap from your very first decomposition scenario — no amount of prompt refinement converts a probabilistic instruction into a hard enforcement boundary.
- **C)** overcorrects by removing a capability (HR data access) that isn't actually the source of the risk — read access to employee records for lookups is a reasonable, useful capability; the actual danger is specifically the *unconfirmed action* (initiating a real payroll change) being insufficiently gated. This conflates "the assistant has access to sensitive data" with "the assistant can take a consequential action without adequate confirmation" — two different concerns, and only the second one caused this incident.
- **D)** draws the opposite conclusion from what the evidence supports. The incident being caught by chance is not evidence the safeguards worked — it's evidence there was no reliable safeguard at all, and the company got lucky this time. Treating a near-miss as proof of a working system is the same complacency trap as trusting flat thumbs-down volume as proof of stable quality in your support-agent feedback-loop scenario — an absence of visible failure isn't the same as a functioning safety mechanism.

**Pattern to remember**: 
- Whenever a scenario describes a consequential, real-world action (money movement, account changes, irreversible operations) protected only by a system-prompt instruction telling the model when it's allowed to act, that's the signal — instructional guardrails are appropriate for shaping tone, scope, and low-stakes behavior, but consequential actions need structural or system-level enforcement (required-parameter gating, human confirmation, action-level permission checks) that doesn't depend on the model correctly judging every ambiguous edge case in the moment.

Want another guardrails scenario, or move to scenario practice on templates/caching next?
