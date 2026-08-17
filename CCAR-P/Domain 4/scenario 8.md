**Scenario**

A cybersecurity company built a Claude-powered system 18 months ago that classifies incoming network traffic descriptions as "known malicious pattern," "known benign pattern," or "requires human review." At launch, they built a 2,000-example eval set with ground-truth labels based on the threat-pattern database as it existed at that time, and the system scored 91% accuracy against it — strong, and it's remained the team's standard eval set ever since, re-run after every model or prompt update to confirm nothing regressed.

Eighteen months later, the system still scores 90-91% on this same eval set — consistently strong, no red flags from the team's regular testing. However, a recent external security audit found the system has been misclassifying several *newer* attack patterns that emerged in the last year as "requires human review" when they should be confidently flagged as "known malicious" — patterns that didn't exist when the eval set was built, and which the eval set's now-18-month-old ground truth labels have no representation of at all, since every example in the set reflects the threat landscape as it stood at launch.

**Question**: What's the core issue with how this eval set has been maintained, and what should the team do?

A) There's no issue — the consistent 90-91% score across 18 months of updates is strong evidence of stable, reliable performance, and the external audit's findings are likely unrelated to the eval set itself.

B) The core issue is eval-set staleness: the ground truth was accurate and representative when built, but the underlying reality it represents (the threat landscape) has continued evolving while the eval set stayed frozen at its original snapshot — meaning the eval set has been silently and increasingly failing to test the system against current, relevant cases, even though it still runs and reports a score every time. A consistently strong score on a stale eval set doesn't indicate current-world performance; it only indicates consistent performance against an increasingly outdated slice of reality. The team should periodically refresh the eval set with current examples (including recently emerged attack patterns), not just re-run the original fixed set indefinitely.

C) The issue is that the eval set should have been built without any temporal component at all, since any point-in-time snapshot will eventually become inaccurate; the team should have used a fundamentally different evaluation approach from the start.

D) The issue is unrelated to the eval set; this is purely a model capability gap, and the team should upgrade to a more capable model to handle novel attack patterns better.

Take your best guess and I'll walk through it.