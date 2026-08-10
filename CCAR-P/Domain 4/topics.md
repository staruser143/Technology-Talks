## Domain 4: Evaluation, Testing & Optimization (16%)

This domain has real overlap with material you've already built — observability (Domain 3), feedback loops (Domain 1), diagnosing model-vs-prompt-vs-context issues (Domain 2) — but it's specifically about the *discipline* of measuring and improving system quality systematically, rather than architecture or prompting technique in isolation. Six objectives:

### 1. Define evaluation metrics (accuracy, latency, cost, safety, security)

The foundational skill: before you can improve anything, you need metrics that actually capture what "good" means for a given system — and different systems need different metric mixes. A classification task has a clean accuracy metric (correct/incorrect); a generative task (summarization, drafting) needs something more nuanced (does it capture the key points? is it faithful to the source?). The exam-relevant judgment: **metrics should match what actually matters for the task**, not default to whatever's easiest to measure — this echoes the "measure both sides of a trade-off" discipline from your PII-redaction scenario, now generalized: pick metrics deliberately, across all five categories (accuracy, latency, cost, safety, security), not just the one that's convenient to track.

### 2. Design evaluation datasets and test frameworks using mixed methodologies

You need a representative set of test cases to actually evaluate against — not just a handful of easy examples that happen to work. "Mixed methodologies" means combining approaches: automated metrics (exact match, similarity scores) for what can be checked mechanically, human evaluation for nuanced quality judgments, and model-based evaluation (using an LLM to judge another LLM's output) for scale where human review doesn't feasibly cover enough volume. A good eval dataset also deliberately includes edge cases and known-hard examples, not just the easy majority — directly echoing the "traffic isn't uniform" lesson from your accuracy-latency scenarios, applied to test-set construction instead of production routing.

### 3. Conduct A/B testing and iterative improvements

Testing a change against a real baseline, with actual traffic split, rather than assuming a change is an improvement because it seems reasonable — this is the systematic version of the "measure before you conclude a fix worked" discipline from your PII-redaction and caching-claim scenarios, formalized as an actual methodology with real statistical considerations (sample size, avoiding premature conclusions from noisy early results).

### 4. Diagnose system issues (prompt failure, hallucinations, model mismatch)

This is largely the formalization of skills you've already built across this whole session — distinguishing whether a quality problem stems from missing grounding (your pricing scenario), missing prompting structure (your loan-arithmetic scenario), an actual model-capability mismatch (rare, but real), or a genuine hallucination (the model confidently generating unsupported content). The exam wants a systematic diagnostic *process* here, not just intuition.

### 5. Optimize token usage, latency, and cost-performance trade-offs

Directly builds on your accuracy-latency-cost material from Domain 3 and token-management material from Domain 2 — now framed as an ongoing *evaluation and optimization* discipline rather than a one-time design decision: continuously measuring where cost/latency is actually going and iterating.

### 6. Monitor system performance using logging and observability tools

Overlaps substantially with Domain 3's observability sub-topic (silent failures, dashboards vs. alerting, distributed tracing) — here it's framed as part of the ongoing evaluation loop: production monitoring isn't separate from evaluation, it's evaluation's continuous, real-world extension after a system ships.

---

Given the real overlap with material you've already deeply covered, I'd suggest we focus scenario practice on what's genuinely new: **defining the right metrics for a given task, designing evaluation datasets well, A/B testing methodology, and the formal diagnostic process for prompt failure vs. hallucination vs. model mismatch.** Want to start with metric definition, or dive straight into scenario practice mixing across the domain?