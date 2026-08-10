**Scenario**

A payments company builds a Claude-powered fraud detection classifier that labels each transaction as "fraudulent" or "legitimate." Their production traffic is heavily imbalanced: roughly 98% of transactions are legitimate, and only about 2% are actually fraudulent. The team evaluates the system using **overall accuracy** (correct labels / total labels) and reports a strong result: 97.5% accuracy. Leadership is pleased and considers the system ready to scale up.

A skeptical engineer digs deeper and builds a breakdown by class rather than looking at the aggregate number. She finds: of all the transactions that were actually fraudulent, the system correctly caught only about 15% of them — the vast majority of real fraud was labeled "legitimate" and let through. Because fraudulent transactions are such a small share of total volume, even this poor fraud-catching performance still produces a high *overall* accuracy score, since the system is getting the (much larger) legitimate-transaction majority right almost all the time.

**Question**: What's the core issue with using overall accuracy as the primary metric here, and what should the team do?

A) There's no issue — 97.5% accuracy is a strong result by any reasonable standard, and the system should proceed to scale up as planned.

B) The core issue is that overall accuracy, on a heavily imbalanced dataset, is dominated by performance on the majority class (legitimate transactions) and can mask very poor performance on the minority class (fraud) that actually matters most for this task's purpose. The team should evaluate using metrics that specifically capture minority-class performance — recall on fraud specifically (what fraction of actual fraud is caught), and precision on fraud (of what's flagged as fraud, how much really is) — rather than relying on a single aggregate accuracy number that a model could achieve simply by defaulting toward the majority class.

C) The issue is that the eval dataset's class balance (98% legitimate, 2% fraud) doesn't match reality and needs to be artificially rebalanced to 50/50 before any evaluation can be trusted.

D) The issue is unrelated to metric choice; the low fraud-catching rate indicates the model needs more training data specifically on fraudulent transaction examples, which is a model-training concern outside the scope of evaluation design.

Take your best guess and I'll walk through it.




Correct — and this is one of the most important and common evaluation traps in real production systems, precisely because the aggregate number looks so reassuring while hiding a severe failure on exactly the cases the system exists to catch.

**Why B is right**

Do the arithmetic the way the skeptical engineer did: if 98% of traffic is legitimate and the system gets legitimate transactions right almost all the time, it can score extremely high on overall accuracy *purely from the majority class*, almost regardless of how it performs on the 2% that's actually fraudulent. A system that caught essentially none of the fraud but correctly labeled every legitimate transaction would still score around 98% accuracy — the minority class barely moves the aggregate number, even though catching that minority class *is the entire point of the system*. This is exactly what the engineer's breakdown reveals: 15% recall on fraud (85% of real fraud slips through), sitting almost invisibly underneath a 97.5% headline number.

This is a sharper, more dangerous version of the same "aggregate metric hides segment-level failure" pattern from your identity-verification observability scenario (Domain 3) and your drug-interaction-latency healthtech scenario — except here it's not a segment of *traffic* being hidden, it's the entire *purpose* of the system being hidden behind a technically-true but functionally-meaningless top-line number. The fix is exactly what the concepts overview flagged: **recall specifically on the minority/critical class** (of all real fraud, what fraction did we catch — the metric that matters most given what's at stake if fraud goes undetected) and **precision on that class** (of what we flagged as fraud, how much really was — since over-flagging has its own cost, blocking legitimate customers). Neither of these gets diluted by the 98% majority the way overall accuracy does, because they're computed specifically *within* the fraud class rather than across the whole imbalanced population.

**Why the others are wrong**

- **A)** takes the flawed aggregate metric at face value, exactly the mistake the scenario is built to expose — a "strong result by any reasonable standard" isn't actually true once you know the standard being used (overall accuracy) is structurally insensitive to the very failure mode (missed fraud) the system is supposed to prevent. This repeats the misdiagnosis pattern from your last scenario, just with an inflated number instead of a deflated one — either direction, trusting an aggregate metric without checking what's actually driving it is the same underlying mistake.
- **C)** proposes rebalancing the *data* rather than fixing the *metric* — but artificially rebalancing to 50/50 would distort what the eval set represents (real-world traffic genuinely is imbalanced this way) and doesn't actually solve the diagnostic problem, since you can still compute class-specific recall/precision on the real, imbalanced distribution without needing to fabricate a different one. The fix belongs at the metric-selection level, not the dataset-composition level — this scenario's problem is a direct parallel to your exact-match scenario: the data (200 real contracts; real imbalanced transaction volume) was fine both times, the *metric applied to it* was the flaw.
- **D)** jumps to a training/model-capability explanation without first establishing that the current *evaluation* is even correctly measuring the problem it's diagnosing — this is the same premature-model-fix pattern rejected in nearly every scenario across this whole session. It's entirely possible the model already has reasonable fraud-detection capability that a properly-designed metric (and downstream, properly-designed decision threshold) would reveal; jumping straight to "needs more training data" skips the diagnostic step of first measuring correctly, which is exactly what Domain 4 objective 1 is about.

**Pattern to remember, deepening this metric-selection thread**: this scenario and your exact-match scenario are a matched pair, both showing the same underlying lesson from different angles — a metric can be *technically well-defined and honestly computed* while still being the wrong lens for what the task actually needs. There, the metric was too strict (penalizing correct-but-differently-formatted output). Here, the metric is too forgiving in exactly the place it needed to be strict (letting majority-class performance mask minority-class failure). Both times, the fix isn't "the system is bad, change the system" — it's "the ruler is wrong, change the ruler," and only once you have a trustworthy measurement can you correctly judge whether the underlying system actually needs fixing at all.

Want another metric-selection scenario, or move to eval dataset design now?