**Scenario**

A logistics company wants Claude to handle "shipment exception" resolution: when a package is delayed, damaged, or lost, Claude should figure out the appropriate resolution (reship, refund, partial credit, escalate to human) and draft a customer message. Engineering's first version was one big prompt handling everything in a single pass: read the exception details, decide resolution type, calculate any refund amount, and write the customer message — all at once. Two months in, they've noticed refund calculations are sometimes inconsistent with what the customer message claims ("we're crediting you $12" in the message, but the actual refund processed is $18), and unusual exception types (multi-item shipments with partial damage) get resolutions that don't match the company's documented policy tree, while more common single-item cases work fine.

Separately, the team has a support-ticket escalation queue where a human reviews every case Claude marks "escalate to human," but nobody reviews the cases Claude resolves on its own — leadership has said they're "confident in the escalation logic" because escalation volume has stayed low. The team is now debating two changes: (1) whether to decompose the pipeline, and (2) whether to add sampling review of auto-resolved cases. There's also a new proposal to make the whole system agentic, arguing that "shipment issues are unpredictable, so an agent should decide everything dynamically" — but the business has a stated requirement that customers get a response within 10 seconds of submitting an exception report.

**Question**: Which combination of changes best addresses what's actually going wrong, while respecting the stated constraint?

A) Make the entire system agentic to handle the unpredictability of shipment types; this alone will also fix the refund/message inconsistency and the missing review of auto-resolved cases.

B) Decompose the pipeline (e.g., separate resolution-type decision, refund calculation, and message drafting into sequential steps so the message is generated from the calculated refund rather than in parallel with it) to fix the consistency and policy-mismatch issues; separately, add random sampling review of auto-resolved (non-escalated) cases, since low escalation volume alone doesn't prove auto-resolved cases are correct — it only proves few cases *hit the escalation trigger*. Keep the overall system as a workflow to preserve the 10-second SLA, reserving any dynamic decision-making for a narrowly scoped step if needed.

C) Add sampling review of auto-resolved cases, but leave the single-pass prompt as-is, since the inconsistency and policy-mismatch issues are minor prompting problems that better instructions will resolve without restructuring anything.

D) Decompose the pipeline and make it fully agentic to handle the unpredictable exception types, accepting that response time may exceed 10 seconds since accuracy matters more for customer-facing financial decisions.

Take your best guess and I'll walk through it, or ask me to explain any option first if you'd like.




Correct — and this scenario deliberately stacks four sub-topics together (decomposition, feedback-loop bias, workflow-vs-agentic, and SLA constraints) because the real exam's harder questions do exactly this. Let's map every symptom in the scenario to the concept it's testing, since that's the actual skill here — not memorizing four answers, but diagnosing which principle applies to which piece of evidence.

**Mapping the symptoms to the fixes**

- **"$12 in the message but $18 actually processed"** → this is the *exact* signature from your podcast-transcript scenario: an output (customer message) generated in the same pass as another output (refund amount) it should actually depend on, with no guarantee of consistency between them. Fix: decompose so the message is drafted *from* the calculated refund, not alongside it — same principle, different domain.

- **"Unusual exception types don't match the policy tree, but common cases work fine"** → this is the attention-dilution signature: one pass juggling resolution-type decision + refund math + message writing does fine when the case is simple, but degrades on edge cases where more competing reasoning is needed simultaneously. Same diagnosis as the transcript's "quotes buried in the middle" — decompose so resolution-type decision gets its own focused step, checked against policy before anything downstream happens.

- **"Escalation volume has stayed low, so leadership is confident"** → this is precisely the flawed feedback-loop trap from your support-agent thumbs-down scenario. Low escalation volume only tells you how many cases *tripped the escalation trigger* — it says nothing about whether the trigger itself is well-calibrated, or whether auto-resolved cases are actually correct. A case could be wrongly resolved without ever coming close to the escalation threshold. This needs the same fix as before: random sampling of the *unflagged* (here, auto-resolved) population to get an actual quality baseline, independent of a mechanism that only reacts to cases crossing a specific line.

- **"Make it fully agentic because shipment issues are unpredictable" + 10-second SLA** → this is your loan-scheduling scenario again: unpredictability alone doesn't justify full agentic reach, and a stated hard latency constraint overrides the "most flexible" design. The correct move is the nested pattern from your combined-pattern scenarios: keep the reliable, fast, testable parts (refund calculation, message drafting) as workflow steps, and — only if truly needed — scope any dynamic decision-making narrowly (e.g., just the initial resolution-type classification for genuinely ambiguous multi-item cases), rather than handing the entire pipeline over to open-ended looping that could blow the SLA the same way the multi-agent scheduling design did.

**Why the others fail, briefly, since you've already reasoned through each piece individually:**
- **A)** repeats the "agentic magically fixes everything" trap — going agentic doesn't create consistency between message and refund, and doesn't add review coverage to auto-resolved cases; it's solving the wrong layer of the problem entirely, and ignores the SLA.
- **C)** repeats the "better phrasing fixes a structural problem" trap from your first decomposition scenario — the inconsistency described isn't a wording issue, it's a sequencing issue that persists regardless of how the single prompt is worded.
- **D)** correctly identifies decomposition as needed but pairs it with full agentic scope and then explicitly discards the stated SLA — that's the loan-scheduling mistake again, treating a stated business constraint as negotiable rather than a hard boundary on the design.

**What this mixed scenario demonstrates about how the real exam likely tests Domain 1**:
- individual concepts (decomposition, feedback loops, pattern selection, business constraints) rarely show up in isolation in a well-designed scenario question. 
- They compound, and the correct answer usually involves diagnosing *multiple independent problems* and applying the *right, separate* fix to each — not one silver-bullet architectural change that's assumed to fix everything at once. 
- Be suspicious of any answer option that claims one change (like "go agentic" or "just reword the prompt") solves three unrelated symptoms simultaneously.

