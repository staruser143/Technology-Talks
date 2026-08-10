**Scenario**

A payments company runs a Claude-powered transaction review system that flags potentially fraudulent transactions before they're approved. Every transaction currently goes through the same full pipeline: retrieve the customer's transaction history (last 90 days), retrieve merchant risk data, run a detailed reasoning pass with Claude's most capable model to assess fraud likelihood, and return a decision. This must complete before the transaction is approved, and the company has a hard 2-second SLA (industry standard for checkout flow). Current average latency is 3.8 seconds. A review of transaction data shows: about 85% of transactions are low-value, from established customers with years of clean history and familiar merchants — these almost always get approved with high confidence very early in the reasoning process. The remaining 15% involve some combination of new customers, unusual amounts, or unfamiliar merchants, and genuinely need the full history lookup, merchant risk data, and careful reasoning to get right.

**Question**: Which combination of changes would most effectively bring this within the 2-second SLA while preserving fraud-catching accuracy where it matters?

A) Switch every transaction to a smaller, faster model to cut latency uniformly, accepting that fraud detection accuracy will drop somewhat across the board as a reasonable trade-off for meeting the SLA.

B) Add an early, cheap first-pass check (e.g., a lightweight risk score based on easily available signals like transaction amount, customer tenure, merchant familiarity) that can approve the ~85% of clearly low-risk transactions almost immediately with high confidence; route only the ~15% of transactions that don't clear this fast check into the full pipeline (history lookup, merchant risk data, detailed reasoning with the capable model).

C) Keep the full pipeline for every transaction, but run the history lookup and merchant risk data retrieval in parallel instead of sequentially, and consider that sufficient.

D) Increase the SLA to 4 seconds instead of changing the pipeline, since fraud review is high-stakes and the current design already produces good results.

Take your best guess and I'll walk through it.




Correct — and this scenario adds a new layer on top of caching: the **early-exit/confidence threshold** lever (#7), applied to a case where caching itself wouldn't even work.

**Why B is right**

Notice this scenario is deliberately *not* solvable by caching the way the HR bot scenario was — every transaction is unique (different amount, different timestamp, different specifics), so there's no "identical repeated question" to cache the answer to. That's exactly why the exam pairs it right after the caching scenario: it wants you to recognize that the *same underlying principle* (don't spend your expensive path on requests that don't need it) has a different implementation depending on the shape of the problem.

Here, the data tells you 85% of transactions share a *pattern* — low value, established customer, familiar merchant — that's identifiable cheaply, before you've paid for the expensive history lookup and reasoning pass. So the fix is a fast, lightweight first-pass check using signals that are already cheap to access, which can confidently clear the easy majority in well under budget. Only the transactions that don't clear that fast check — the genuinely ambiguous 15% — get routed into the full pipeline, where the 3.8-second latency is a reasonable cost because that's precisely where careful reasoning matters most for catching real fraud.

This is early-exit logic: your **worst-case pipeline stays thorough**, but your **average-case latency drops**, because most requests never touch the expensive path at all. The SLA is about typical experience across all traffic, so this lets you meet it without uniformly degrading quality anywhere it counts.

**Why the others are wrong**

- **A)** repeats the mistake from the HR scenario's wrong answer A — a uniform downgrade applied to all traffic, including the 15% that most needs careful reasoning. In a fraud detection context this is a worse mistake than in the HR bot case: degrading accuracy uniformly means you're specifically weakening detection on the harder cases, which is exactly backwards from where you'd want to preserve rigor.
- **C)** is a real, valid latency lever (parallelizing independent retrieval calls, lever #1) — but it's applied uniformly and likely isn't enough on its own. Parallelizing the two retrieval calls might shave off some latency, but it doesn't address that 85% of transactions are paying for a full history lookup, merchant risk retrieval, and a capable-model reasoning pass when they didn't need most of that in the first place. It's a partial fix mistaken for a complete one — treating one lever as sufficient when the traffic-pattern evidence calls for a bigger structural change.
- **D)** is the "argue with the constraint" trap again, and worth noting it appears in a different flavor from the loan-scheduling scenario: there, the SLA was justified by a measured business effect (patient abandonment); here, it's stated as "industry standard for checkout flow" — also not something you get to unilaterally renegotiate away just because the current design happens to produce good results. A good result that violates a hard external constraint (checkout UX standards, likely tied to cart abandonment and conversion) isn't a solved problem, it's a design that needs to change.

**Pattern to remember, now generalized across both scenarios**: when traffic can be cheaply and reliably split into "clearly low-complexity" and "genuinely needs the expensive path" *before* running the expensive path, build a fast triage step that does that splitting and routes accordingly. Whether the "cheap majority" is identified via **exact-repeat caching** (HR bot) or a **cheap risk/complexity signal computed on the fly** (fraud review) depends on whether the requests are literally repeating or just share exploitable structure — but the underlying discipline is the same: don't pay your most expensive step's cost on requests that don't need it.

