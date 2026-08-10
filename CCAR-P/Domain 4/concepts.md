Good instinct — let's build this out properly before testing it, the same way we did for Domains 2 and 3.

## 1. Defining evaluation metrics

**The core skill is mapping task type to metric type**, since a mismatched metric gives you false confidence:

- **Classification/extraction tasks** (spam detection, entity extraction, routing): precision, recall, F1 — and critically, know when to weight these differently. A medical triage system (your intake-triage scenario) cares far more about recall on "emergent" cases (missing one is catastrophic) than precision (a false alarm costs a doctor's time, not a life) — so accuracy alone as a single number can hide a dangerous imbalance.
- **Generative tasks** (summarization, drafting, explanation): harder to measure mechanically. Faithfulness/groundedness (does the output only claim what the source supports — directly relevant to hallucination), completeness (are key points covered), and adherence to format/style (tying back to your few-shot material) are the real dimensions, often requiring human or model-based judgment rather than a clean automated score.
- **Agentic/multi-step tasks**: task completion rate (did it actually accomplish the goal, not just produce plausible-looking output), efficiency (how many steps/tool calls did it take), and error recovery (did it correctly handle a failed tool call or dead end).

**Cost, latency, safety, security as first-class metrics, not afterthoughts**: the exam wants you to treat these as equally deliberate design choices, not things you only notice when they become a problem — directly the "measure both sides of a trade-off" discipline from your PII-redaction and caching scenarios. A system evaluated only on accuracy, with cost and latency never formally tracked, is exactly the setup that let your fraud-detection and content-moderation SLA problems go undetected until they became urgent.

**The common exam trap**: a single, easy-to-compute metric (like exact-match accuracy) standing in for a genuinely more nuanced quality question it doesn't actually capture — e.g., using exact-match accuracy for a task where multiple correct phrasings exist, which would penalize genuinely good outputs that don't match a reference string verbatim.

## 2. Designing evaluation datasets and test frameworks

**Representativeness matters more than size.** A large eval set full of easy, similar examples tells you less than a smaller set that deliberately spans the real distribution of difficulty — directly the "traffic isn't uniform" lesson from your accuracy-latency scenarios, now applied to what you test against rather than what you route in production. If your production traffic is 85% easy cases and 15% hard ones (your fraud-detection scenario), an eval set that's 95% easy cases will report misleadingly high accuracy while systematically under-testing the cases that actually matter most.

**Mixed methodologies, concretely:**
- **Automated/exact-match**: cheap, scalable, works when there's a clear right answer (classification labels, structured extraction fields).
- **Human evaluation**: necessary for nuanced quality judgments (tone, helpfulness, faithfulness) that don't reduce to a mechanical check — but expensive and slow, so it doesn't scale to every request.
- **Model-based evaluation (LLM-as-judge)**: a middle ground — using a capable model to score outputs against a rubric, scaling further than human review while capturing more nuance than pure automated matching. Worth knowing the exam-relevant caveat: an LLM judge has its own failure modes and biases (it can be fooled by confident-sounding but wrong output, similar to how a human skimming might be), so it's a scaling tool, not a total replacement for periodic human calibration checks.

**Deliberately including edge cases and known-hard examples**: an eval set should specifically include the borderline, ambiguous, and historically-problematic cases — not just a random sample of "normal" traffic — the same instinct as your content-moderation scenario, where the whole traffic distribution needed real judgment, not just the easy majority.

## 3. A/B testing and iterative improvement

**The core discipline**: never conclude a change is an improvement just because it seems reasonable or a small manual check looked fine — this is the systematic, statistically-grounded version of the "measure before concluding" lesson from your PII-redaction and cache-hit-rate-claim scenarios. Split real traffic between the current version (control) and the proposed change (variant), and compare actual outcomes.

Exam-relevant nuances:
- **Sample size and statistical significance**: a small number of requests can show noisy, misleading differences — concluding "the new prompt is better" from 20 examples where 12 looked good is not a valid test. The exam wants you to recognize when a result is too small/early to trust.
- **What to measure in the test**: the metrics defined in objective 1 — not just "does this look better," but the actual accuracy/cost/latency/safety metrics that matter for this system.
- **Iterative, not one-shot**: A/B testing is a repeated discipline — ship a change, measure, learn, adjust — not a single gate you pass once and never revisit (tying back to the "periodically re-verify, don't assume a past-good state holds forever" lesson from your decomposition-boundary and drift-detection material).

## 4. Diagnosing system issues (prompt failure, hallucinations, model mismatch)

This objective formalizes a *process* you've already practiced repeatedly without it being named:

- **Prompt failure**: the prompt doesn't give the model what it needs — missing grounding (your pricing scenario), missing reasoning structure (your loan-arithmetic scenario), or an underspecified target (your product-description scenario). Diagnostic signature: a *different, better-constructed prompt* on the *same model* fixes it.
- **Hallucination**: the model generates confident, plausible-sounding content that isn't actually supported by anything real — often when it lacks grounding and fills the gap with something plausible rather than admitting uncertainty (your silent-retrieval-failure RAG scenario is a hallucination case specifically). Diagnostic signature: check whether the claim is actually traceable to real, provided context — if not, and the model didn't flag uncertainty, that's a hallucination, not a reasoning error.
- **Model mismatch**: the task genuinely exceeds what this model tier can reliably do, even with a well-constructed prompt and proper grounding — the rare case where an upgrade is actually the right fix, as validated by your Domain 2 model-selection trade-off scenarios (the coding-assistant, intake-triage cases). Diagnostic signature: a well-constructed prompt with proper grounding *still* fails reliably, specifically on the hardest subset of cases, in a way that scales with task difficulty.

**The exam-relevant discipline**: check prompt/grounding causes *before* concluding model mismatch, since jumping to "the model isn't good enough" without ruling out the cheaper, more common causes is the single most repeated mistake pattern across this entire session (pricing, loan-arithmetic, product-description, orchestrator-synthesis scenarios all rejected exactly this jump).

## 5. Optimize token usage, latency, and cost-performance trade-offs

This is your Domain 2/3 accuracy-latency-cost material, reframed as an *ongoing evaluation loop* rather than a one-time design choice: continuously measuring where cost and latency actually go (token counting, `/context`-style breakdowns, per-step attribution) and iterating — not a decision you make once at launch and never revisit.

## 6. Monitor system performance using logging and observability tools

This is Domain 3's observability material (silent failures, dashboards vs. alerting, distributed tracing, semantic vs. technical success), reframed here as the continuous, production-side half of the evaluation loop — evaluation doesn't stop at launch; production monitoring is evaluation running indefinitely, in the real world, on real traffic.

---

Given how much of this maps onto scenarios you've already worked through, want to move into scenario practice now — focused especially on the genuinely new ground (metric selection nuances, eval dataset design, A/B testing methodology, and the formal prompt-vs-hallucination-vs-model-mismatch diagnostic)?