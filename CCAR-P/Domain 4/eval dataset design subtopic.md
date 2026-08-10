## Eval Dataset Design — deeper dive

We touched this in the domain overview, but let's go deeper before testing it, following the same pattern as everything else.

### 1. Representativeness over size

A common instinct is "more test cases = better eval," but a large eval set that's mostly easy, similar cases tells you less than a smaller, deliberately-diverse one. The eval set should mirror the actual shape of production traffic — including its difficulty distribution — not just be a large random sample that happens to be dominated by the easy majority (the same 85/15 skew logic from your fraud and content-moderation scenarios, now applied to *what you test against* rather than *what you route in production*).

### 2. Deliberate edge-case and known-hard-case inclusion

Beyond mirroring the natural distribution, a well-designed eval set *specifically* over-samples or deliberately includes: known historical failure cases (bugs you've already found and fixed — regression protection), boundary/ambiguous cases (the borderline "routine vs. urgent" triage cases from your hospital scenario), and adversarial or unusual inputs (malformed data, unexpected formats, edge-of-scope requests). If you only test against "normal" traffic, you'll never catch problems that only show up in the tail — directly the same principle as your p95/p99 latency lesson, just applied to test coverage instead of production monitoring.

### 3. Ground truth quality

An eval is only as good as its reference answers. Watch for: **inconsistent or subjective ground truth** (different human annotators disagreeing on what "correct" means for ambiguous cases — worth measuring inter-annotator agreement, not just trusting a single annotator's judgment), and **stale ground truth** (references that were correct when written but no longer reflect current reality — directly your "superseded policy document" RAG scenario, now applied to the eval set itself: an eval set testing against outdated correct answers will penalize a system for being *right* about current information).

### 4. Avoiding eval-set overfitting / data leakage

If the same eval set is used repeatedly to guide iteration (tune the prompt, check the eval score, tune again, check again), there's a real risk of implicitly "overfitting" to the specific quirks of that fixed set — the system gets very good at the exact cases in your eval set without genuinely improving on the broader task. The exam-relevant discipline: periodically refresh the eval set with new cases, and treat a strong eval score with appropriate skepticism if the same fixed set has been iterated against many times — this is the same "measured evidence can become stale and needs periodic re-verification" principle from your decomposition-boundary and drift-detection material, now applied to the evaluation harness itself.

### 5. Mixed methodologies — when to use which layer

- **Automated/exact-match**: fast, cheap, consistent — use where there's a genuinely clear right answer (classification labels, values with normalized comparison as you fixed in your legal-extraction scenario).
- **Model-based (LLM-as-judge)**: scales further than human review, useful for nuanced dimensions like faithfulness or tone — but needs periodic calibration against human judgment, since the judge model has its own blind spots and can be fooled the same way a human skimming might be.
- **Human evaluation**: the ground-truth anchor for genuinely subjective or high-stakes quality judgments, but doesn't scale to every request — used on a sample, or specifically on cases automated/model-based methods flag as uncertain or borderline.

A well-designed framework typically layers these: automated checks catch the mechanically-checkable majority cheaply, model-based judging covers nuanced quality at scale, and human review anchors and calibrates both — periodically checking that the automated and model-based layers still agree with human judgment, rather than assuming they always will.

### 6. Dataset size and statistical adequacy

Related directly to A/B testing: an eval set needs enough cases *per relevant segment* (not just overall) to draw a reliable conclusion — 200 total cases sounds like a lot, but if only 5 of them represent a critical edge case, you can't reliably conclude much about that edge case specifically, even though the total-N sounds reasonable.

---

Want to move into scenario practice for this sub-topic now?