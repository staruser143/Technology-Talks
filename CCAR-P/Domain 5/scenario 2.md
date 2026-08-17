**Scenario**

A company's Claude-powered agent can process refund requests, calling a "verify-purchase" service before issuing any refund — the guardrail logic is: call verify-purchase, and only proceed with the refund if the service confirms the purchase is valid and eligible. The engineering team implemented this as: `try { result = call_verify_purchase(); if (result.valid) issue_refund(); } catch (error) { issue_refund(); }` — reasoning that "if the verification service is down, we don't want to block legitimate customers from getting refunds, so we default to approving in that case."

During a partial outage of the verify-purchase service last month, this fallback behavior meant that for several hours, every refund request was automatically approved regardless of purchase validity — including a number of fraudulent requests for purchases that didn't exist, which would have been caught and blocked had the verification service been reachable.

**Question**: What's the core safety-control design flaw here, and what should the team do?



**Question**: What's the core safety-control design flaw here, and what should the team do?

A) There's no flaw — prioritizing customer experience during an outage by defaulting to approval is a reasonable business trade-off, and the fraud that occurred during the outage window is an acceptable cost of maintaining service availability.

B) The core flaw is a **fail-open** design on a consequential, hard-to-reverse action (money leaving the company) — when the verification check itself fails or is unreachable, the system defaults to *allowing* the exact action the check exists to gate, meaning the safety control's own failure mode defeats its purpose entirely. For actions with real financial/security consequences, the system should **fail closed**: if verification can't be completed, the refund should be held for human review or queued/retried, not automatically approved. Fail-open is only appropriate for low-stakes or easily-reversible actions where blocking legitimate users is a worse outcome than occasionally allowing an unverified one.

C) The issue is that the verify-purchase service itself is unreliable and needs better uptime; once that service achieves 100% uptime, the current fallback logic is fine as-is.

D) The issue is unrelated to the fallback logic; the team should add a guardrail instruction to the system prompt telling the model not to issue refunds without verification, which would prevent this from happening regardless of the service's availability.


Correct — and this scenario names a distinct, important safety-engineering concept that's worth having precise vocabulary for: **fail-open vs. fail-closed**, and specifically why the choice between them should depend on the stakes of the action being gated, not on what's most convenient during an outage.

**Why B is right**

Look at what the `catch` block actually does: when the verification check errors out for *any* reason — a timeout, a service crash, a network blip — the code doesn't pause, doesn't queue, doesn't escalate. It calls `issue_refund()` directly, the exact action the check exists to gate. This means the safety control's failure mode is *functionally identical to it approving every request* — the guardrail doesn't degrade gracefully, it inverts entirely the moment it can't run. During the outage, this wasn't a guardrail with reduced effectiveness; it was, for practical purposes, no guardrail at all, and every fraudulent request that arrived during that window sailed through as if verification had actively confirmed it.

This is a sharper, more mechanical version of the "instructions aren't enforcement" lesson from your salary-change scenario — except here the enforcement mechanism *existed* and was structurally real, but its own error-handling path silently discarded it under exactly the condition (the check being unavailable) where you'd most want it to hold. The fix, correctly stated in B, is to **fail closed** for this class of action: if verification can't be completed, the refund shouldn't auto-issue *or* auto-deny — it should be held (queued for retry once the service recovers, or routed to human review) so that the absence of a "yes" is treated as "not yet approved," not as "approved by default." This directly mirrors the human-in-the-loop principle from your Domain 1 material: consequential, hard-to-reverse actions (money leaving the company) warrant a checkpoint that holds even when the automated path breaks down, rather than silently defaulting to the riskier outcome.

The scenario also implicitly teaches the boundary condition worth remembering: fail-open isn't always wrong. If this were, say, a low-stakes internal tool checking whether to show a "recommended" badge next to a product, defaulting to "show it anyway" during a service outage would be a perfectly reasonable trade-off — the cost of being wrong is trivial. The team's reasoning ("don't block legitimate customers") is a real and valid *type* of concern; it's just been applied to an action where the cost of being wrong is asymmetric and serious, rather than to one where it's genuinely low-stakes.

**Why the others are wrong**

- **A)** accepts the business trade-off framing at face value without weighing what it's actually trading — some unmeasured amount of fraud, at scale, for the entire duration of any future outage, is a very different cost than "occasionally a legitimate customer waits a bit longer for their refund," which is what fail-closed would have actually produced instead. This isn't a reasonable trade-off calibrated to the real stakes; it's an accidental full bypass of the safety control disguised as one.
- **C)** treats the fix as "make the dependency more reliable" rather than "make the failure mode of the safety check itself safe" — but no service achieves 100% uptime, and designing a safety control that becomes a full bypass the moment its dependency has *any* downtime, however rare, is a fragile design regardless of how reliable that dependency generally is. The fix belongs in the fallback logic, not in chasing an unreachable uptime guarantee.
- **D)** proposes an instructional guardrail as a fix for what is fundamentally a code-level, structural failure — the model was never making the decision to bypass verification here; the `catch` block did, in application logic the model never sees or influences. A system-prompt instruction has zero bearing on what a `try/catch` block does when an API call throws an error — this confuses a prompting-layer fix with an application-architecture problem, the same category mismatch as trying to fix a caching bug with a bigger model.

**Pattern to remember, extending the guardrails arc**: a safety control isn't fully specified until you've also specified what happens when the control *itself* fails or is unreachable — and for consequential, hard-to-reverse actions, that fallback should default to blocking/holding (fail closed), never to silently granting the exact permission the control exists to gate (fail open). The stakes of the action being gated, not the convenience of the fallback path, should determine which default is correct.

Want another scenario here, or move to sub-topic 2 (identifying risks, limitations, and failure modes of LLM systems)?