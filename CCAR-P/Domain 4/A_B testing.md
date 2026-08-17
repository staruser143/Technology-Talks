## A/B Testing Methodology — deeper dive

We touched this briefly in the Domain 4 overview; let's go deeper on the mechanics, since this is where a lot of exam traps live.

### 1. The basic setup

Split real traffic (or a representative sample of it) between a **control** (current production version) and a **variant** (the proposed change), running both simultaneously on comparable populations, then compare outcomes on your predefined metrics. The "simultaneously" part matters — comparing this week's variant performance against last month's control performance introduces confounds (traffic patterns, external conditions, seasonality) that a true concurrent split avoids.

### 2. Statistical significance and sample size

The core trap the exam likes: concluding a change is better from a small number of observations that could easily be noise. If your control converts at 70% and your variant converts at 74% on 50 requests each, that's a difference of maybe 2 actual outcomes — nowhere near enough to distinguish a real effect from random variation. The exam-relevant judgment: know that a result needs enough volume (and ideally a formal significance check, not just eyeballing whether the variant number looks bigger) before you treat it as a real finding, not just a lucky sample.

### 3. What counts as a fair comparison — controlling confounds

Beyond simultaneous testing, a fair A/B test needs the two arms to be genuinely comparable otherwise — random assignment (not letting easier/harder cases systematically end up in one arm), and isolating the *one* change being tested (bundling multiple changes into one variant means you can't attribute a result to any single change, which directly echoes your PII-redaction scenario, where two simultaneous changes made it impossible to know which one caused the regression).

### 4. Choosing the right metrics to compare (ties directly to your metric-selection work)

This is where your entire metric-selection arc plugs in directly: an A/B test comparing only the metric you're hoping to improve (e.g., satisfaction score) without also tracking the metrics that might reveal a hidden cost (e.g., reopen rate) is exactly how your proxy-metric-divergence scenario happened — except formalized as a testing methodology failure rather than a slow, unmeasured drift. A well-designed A/B test tracks the *full* metric set (accuracy, latency, cost, safety) for both arms, not just the one metric the change was intended to move.

### 5. Test duration and stopping rules

Related to sample size: stopping a test as soon as the variant *looks* ahead (a common, tempting mistake — "peeking" and stopping early once you like what you see) inflates the risk of concluding a difference exists when it's actually just noise that happened to look favorable at that particular moment. The exam-relevant discipline: decide your sample size / duration threshold in advance, and let the test run to that point before drawing a conclusion, rather than stopping the moment the numbers look good.

### 6. A/B testing is iterative, not a single gate

Directly from the overview: ship, measure, learn, adjust, repeat — not a single pass/fail checkpoint you clear once and never revisit. A change that wins an A/B test today isn't guaranteed to still be the best choice as usage patterns, underlying data, or user behavior shift over time (the same staleness principle from your ground-truth material, now applied to "is this still the best version" rather than "is this eval set still accurate").

### 7. When A/B testing isn't the right tool

Worth knowing the boundary: for low-traffic features, or changes with severe downside risk if wrong (e.g., a safety-critical guardrail change), a full live-traffic A/B test may not be appropriate — you might need a smaller controlled pilot, offline evaluation against a held-out set first, or a staged rollout (small percentage of traffic, gradually increased) rather than a full 50/50 split from day one.

---

Want to move into scenario practice for A/B testing now?