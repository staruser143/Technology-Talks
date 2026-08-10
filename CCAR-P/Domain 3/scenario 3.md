**Scenario**

A social media company runs a content moderation system that reviews user-submitted captions for policy violations (harassment, misinformation, banned content) before they publish. The pipeline: retrieve relevant community guideline sections, run a reasoning pass with Claude's most capable model to judge whether the caption violates policy, then approve or flag. This must complete before the post goes live, with a hard 1-second SLA for the real-time publish flow. Current average latency is 1.8 seconds. Unlike prior systems the company has reviewed, an analysis of caption review logs shows submissions are **fairly uniformly distributed in difficulty** — there's no large "obviously safe" majority the way there was low-risk transactions or repeat HR questions. Trust & safety moderators report that a large share of captions involve sarcasm, cultural context, or borderline phrasing that requires real judgment, and this holds true across most submissions, not just a small minority.

A junior engineer proposes applying the same fix that worked in the last two scenarios: add a cheap early-exit check that fast-approves "obviously safe" captions and only routes the harder cases into the full pipeline.

**Question**: Is the early-exit approach the right fix here, and if not, what should the team do instead?

A) Yes — early-exit worked for the payments and HR scenarios, so the same fast-path pattern should be applied here too, fast-approving captions that clear a lightweight initial check.

B) No — without evidence of a large, cheaply-identifiable low-complexity majority, an early-exit fast-path is unreliable here (it would either rarely trigger, providing little latency benefit, or trigger inappropriately and let nuanced violations slip through). Instead, pursue levers that don't depend on traffic skew: parallelize independent retrieval steps, tighten the guideline retrieval to fewer, more relevant candidates, and use a smaller/faster model only for the retrieval-adjacent sub-steps that don't require deep judgment — while keeping the capable model's full reasoning pass for every caption, since nuanced judgment is needed broadly, not just for a minority.

C) Yes — but implement it as a cache of all previously reviewed captions, since caption text will naturally repeat often enough to matter.

D) No — the SLA should simply be dropped, since content moderation is too high-stakes for speed to be a valid design constraint at all.

Take your best guess and I'll walk through it.





Correct — and this is the most important scenario in the set, because it tests whether you understand *why* the early-exit pattern worked before, not just *that* it worked.

**Why B is right**

The early-exit and caching levers from the last two scenarios weren't magic — they worked specifically *because* the data showed a large, cheaply-identifiable, low-complexity majority (85% low-risk transactions, 70% repeat HR questions). That skew is the load-bearing assumption underneath both fixes. This scenario explicitly removes that assumption: difficulty is "fairly uniformly distributed," and moderators confirm real judgment is needed broadly, not just for a minority carved out at the edges. Applying the same lever here without that precondition is exactly the trap this domain keeps testing — a fix that worked for one traffic pattern doesn't generalize to a different traffic pattern just because it worked twice before.

Worse, forcing an early-exit here has a specific failure mode worth naming: a cheap check trying to fast-approve "obviously safe" captions in a domain full of sarcasm and cultural-context edge cases will either (a) rarely fire with confidence, since few captions are unambiguously safe, giving you little latency benefit, or (b) fire too liberally and let genuinely violating content slip through the cheap path precisely where subtlety matters most. Neither outcome is acceptable for a moderation system.

So the right move is to reach for the levers that **don't depend on traffic being skewed**:
- **Parallelize independent retrieval** (lever #1) — guideline retrieval and any other independent lookups can run concurrently rather than sequentially, a latency win with zero accuracy cost regardless of traffic distribution.
- **Tighten retrieval to fewer, more relevant candidates** — narrower, better-targeted guideline retrieval reduces both latency and the noise the reasoning model has to sort through, without touching the reasoning step itself.
- **Right-size the model per step** (lever #5) — reserve the capable model specifically for the judgment call (where nuance genuinely matters), while using a smaller/faster model only for sub-steps that don't require deep reasoning (e.g., initial guideline retrieval query formulation). This is different from A's mistake in the fraud scenario: there, downgrading the model *for the judgment step itself* was wrong; here, downgrading a model for a *retrieval-adjacent* step while keeping full reasoning intact for every caption is the correct, narrower application of the same lever.

**Why the others are wrong**

- **A)** blindly reapplies a pattern-matched fix without checking whether its precondition (traffic skew) holds — the exact mistake the scenario is built to catch. Two prior wins don't make a lever universally correct; each lever has a condition under which it works, and part of exam-level competence is checking that condition before reaching for it.
- **C)** misdiagnoses the caching opportunity. Caching worked for HR questions because a small set of *questions* repeats verbatim in meaning even when phrased differently. User-submitted captions are far more varied and unlikely to repeat at meaningful volume — betting on caption-text repetition as your primary latency fix is building on an assumption the scenario gives you no evidence for.
- **D)** repeats the "the SLA shouldn't exist" trap, just inverted from the fraud scenario's "raise the SLA" version. A 1-second SLA for real-time publish is a reasonable, stated product constraint (posts need to feel like they publish instantly) — the answer isn't to abandon the constraint, it's to find levers that respect it without depending on an assumption (traffic skew) that doesn't hold here.

**The complete principle across all three scenarios**: early-exit and caching are *conditional* levers — they only work when you have evidence of exploitable skew or repetition in your traffic. When that evidence is present, use it (HR bot, fraud review). When it's explicitly absent or contradicted (content moderation), don't force the pattern — fall back to levers that improve latency unconditionally (parallelization, tighter retrieval, right-sizing models per sub-step) while keeping full reasoning capacity wherever judgment is genuinely and broadly needed. The exam is testing whether you can tell these two situations apart, not whether you've memorized "add an early-exit check" as a universal answer.

Want to keep drilling accuracy-latency scenarios, or move on to auth/authz gap analysis next?
