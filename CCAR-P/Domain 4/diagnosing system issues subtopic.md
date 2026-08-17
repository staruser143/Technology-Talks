## Diagnosing System Issues (prompt failure, hallucinations, model mismatch) — deeper dive

We defined the three categories briefly in the domain overview; let's build out the actual diagnostic *process* — since this objective is really about knowing what order to check things in and what evidence distinguishes each category, not just being able to name them.

### 1. The diagnostic order matters — cheapest, most common causes first

This is the single throughline across nearly every misdiagnosis scenario in this whole session: **check prompt/grounding causes before concluding model mismatch.** The order should be:

1. **Is the model missing information it needs?** (grounding gap — your pricing scenario)
2. **Is the model missing structural guidance it needs?** (prompt failure — your loan-arithmetic, product-description scenarios)
3. **Is the model generating unsupported content confidently?** (hallucination — distinct from prompt failure, covered below)
4. **Does the task genuinely exceed this model tier's capability even with proper grounding and prompting?** (model mismatch — the rare, legitimate case)

Jumping to step 4 without ruling out 1-3 first is the most repeated mistake pattern in this entire session.

### 2. Prompt failure — the diagnostic signature

**What it looks like**: inconsistent formatting, missed instructions, errors on multi-step logic, output that technically answers the question but not in the way needed.
**The test that confirms it**: does a *better-constructed prompt on the same model* fix the problem? If yes, it was never a capability issue — it was a specification issue. This is directly falsifiable and cheap to test (your loan-arithmetic scenario: same Sonnet-class model, chain-of-thought instruction added, errors resolved).
**Sub-categories worth distinguishing** (you've already built the diagnostic muscle for this): missing grounding vs. missing reasoning structure vs. underspecified format target — each needs a different fix (retrieval/context vs. chain-of-thought vs. few-shot), so "it's a prompt problem" isn't a complete diagnosis on its own — which *kind* of prompt problem matters for choosing the right fix.

### 3. Hallucination — the diagnostic signature, and how it differs from prompt failure

**What it looks like**: confident, fluent, plausible-sounding content that isn't actually supported by anything real — a fabricated statistic, a citation that doesn't exist, a claim not traceable to any provided source.
**The test that confirms it**: can the specific claim be traced back to real, provided context (a retrieved document, the conversation, verified facts)? If the claim exists nowhere in what the model was actually given, and the model didn't flag uncertainty, that's hallucination.
**The key distinction from prompt failure**: prompt failure is usually a *structural* problem (wrong format, skipped steps, missing a required instruction) — the content itself, where present, tends to be accurate. Hallucination is a *content* problem — the structure might be perfect, fluent, well-formatted, but specific claims inside it are fabricated. Your telecom pricing scenario and your news-summarization ROUGE scenario are both hallucination cases specifically, not prompt-failure cases — the model wasn't confused about what format to use, it was confidently inventing content it wasn't actually grounded in.
**Common root cause worth knowing**: hallucination often occurs specifically when a model lacks genuine grounding and fills the gap with something plausible rather than expressing uncertainty — which is why the fix usually overlaps with grounding fixes (RAG, explicit source-citation requirements, instructions to say "I don't know" when uncertain) rather than purely structural prompt fixes.

### 4. Model mismatch — the diagnostic signature, and why it's the last resort

**What it looks like**: failures that persist *even with* proper grounding and well-constructed prompting, specifically clustering on the genuinely hardest subset of cases, scaling with task difficulty rather than being fixable by better specification.
**The test that confirms it**: after ruling out grounding gaps and prompt structure issues (steps 1-2 above), does the task still fail reliably, in a way tied to genuine reasoning depth or complexity the model tier can't handle? Your coding-assistant and intake-triage scenarios (Domain 2) are legitimate model-mismatch cases — but notably, in both, the *reasoning depth requirement* was established as real and specific *before* concluding a stronger model was needed, not as a first guess.

### 5. Why this ordering matters for the exam specifically

Every wrong-answer option across this entire session that said "upgrade the model" as a first move — pricing scenario, loan-arithmetic, product-description, orchestrator-synthesis — was wrong for the same reason: it skipped steps 1-3 and jumped straight to step 4. The exam's diagnostic questions are very likely to present a symptom and offer "upgrade the model" as a tempting, plausible-sounding wrong answer specifically to test whether you check the cheaper, more common causes first.

---

Want to move into scenario practice for this sub-topic now?