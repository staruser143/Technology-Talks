**Scenario**

A legal-tech startup built a contract review pipeline for NDAs. It's decomposed into six sequential LLM calls: (1) extract all defined terms, (2) extract all obligations, (3) extract all dates/deadlines, (4) check for missing standard clauses, (5) check for unusual/risky language, (6) assemble everything into a final report. Each call was added one at a time over several months, each time because a single combined step was "getting one thing wrong." The pipeline now takes 45 seconds per contract and costs noticeably more per review than it used to. A recent audit found that steps 1 and 2 (defined terms and obligations) almost always produce near-identical extracted spans — obligations are consistently phrased using the defined terms right next to them in the same sentences — and combining them back into one call, when tested, showed no drop in accuracy for either output.

**Question**: What does this scenario indicate about decomposition, and what should the team do?

A) Nothing should change — six focused steps will always outperform five, since each step was added to fix a real problem at the time, so the team should keep all six.

B) Decomposition itself was the wrong approach from the start; the team should collapse the entire pipeline back into a single call now that they have more experience.

C) Decomposition has a point of diminishing returns, and steps can be over-split — the audit found evidence (no accuracy drop when merged) that steps 1 and 2 aren't actually competing for attention, so they should be recombined; the other steps should stay separate unless similar evidence emerges for them.

D) The pipeline should be redesigned as a single agentic loop instead, since six fixed steps clearly indicates the task is too complex for a workflow.

Take your best guess and I'll walk through it.



Correct — and this is the closing piece of the decomposition picture: it cuts both ways, and the exam wants you to treat it as an empirical, ongoing question rather than a one-time decision.

**Why C is right**

The audit result is the key piece of evidence, and it mirrors the same standard from your triage scenario: **decompose or recombine based on measured evidence, not on history or intuition.**

- The fact that steps 1 and 2 "almost always produce near-identical extracted spans" — obligations phrased right next to their defined terms in the same sentences — tells you these two tasks aren't actually competing for attention the way the transcript's quote-extraction and header-writing were. They're extracting from the same local context, not fighting over a long input or juggling incompatible objectives.
- The controlled test (merge them, measure accuracy, see no drop) is exactly the kind of evidence that justified decomposing in the transcript scenario, now pointing the other way. Just as "measured accuracy is already high" was evidence *against* decomposing your triage task, "measured accuracy holds steady after merging" is evidence *for* recombining here.
- Crucially, the answer doesn't say "collapse everything" — it says recombine *only* the two steps with actual evidence behind them, and leave the rest (dates, missing clauses, risky language, assembly) split, unless similar evidence turns up for those too. That's the discipline: each step's justification is independent and needs to be periodically re-examined, not grandfathered in forever just because it solved a real problem once.

This closes the loop on the principle from your last two scenarios: decomposition isn't a one-way ratchet you apply and never revisit. A pipeline built incrementally, where each step was individually justified at the time it was added, can still end up over-split in aggregate — 45 seconds and rising cost per contract, with two of six steps doing redundant work. The fix is the same tool in reverse: measure, then recombine where the evidence supports it.

**Why the others are wrong**

- **A)** ignores the audit entirely. "Each step solved a real problem when added" doesn't mean the *current* six-step configuration is still optimal — needs can overlap or become redundant as a system evolves, and refusing to revisit past decisions in light of new evidence is the same mistake as the "decomposition is always best practice" trap, just inverted into "past decomposition decisions are always still correct."
- **B)** overcorrects — it treats one piece of evidence (steps 1+2 can merge) as proof the entire approach was wrong, when the audit only speaks to those two specific steps. Steps 4, 5, and 6 haven't been shown to have the same redundancy; collapsing everything without evidence for the other four repeats the same "reflexive rule instead of measured decision" error from the opposite direction.
- **D)** misreads the signal completely. Six fixed, sequential steps that always run in the same order for every contract is a workflow — nothing here suggests the *path* is unpredictable (the earlier "does the path vary, or just the content" test). Contract review here always goes terms → obligations → dates → clauses → risks → report; that's not evidence for agentic, it's just a workflow that grew an extra, unneeded joint.

**The full decomposition principle, now complete across all four scenarios you've worked through:**
1. Decompose when a single pass shows real symptoms — dropped details, structural mismatch, output contradictions (transcript scenario).
2. Don't decompose reflexively just because something has multiple outputs, if it's already narrow and measurably working (triage scenario).
3. Even correctly-justified past decomposition can become over-split as a system evolves — periodically re-test with evidence, and recombine where warranted (this scenario).
4. Every step in the decision — split or merge — is made on **measured evidence**, never on a general rule applied without checking the specific case.
