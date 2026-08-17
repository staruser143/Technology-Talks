We've now covered the four core failure modes pretty thoroughly — let me do the same honest audit I did for metric selection, rather than just saying "we're done."

**What's been covered, mapped against the deep-dive's seven points:**
- **#2 (sample size/significance)**: scenario 1 — solid.
- **#4 (right metrics, full set)**: scenario 2 — solid.
- **#3 (confound control — bundled changes)**: scenario 3 — solid.
- **#3 (confound control — non-random assignment)**: scenario 4 — solid, and this and scenario 3 together give you both major ways "fair comparison" breaks.
- **#6 (iterative, not a single gate)**: touched conceptually but not scenario-tested directly — e.g., a case where a variant that won six months ago is still running unchallenged while user behavior has shifted underneath it (the same staleness principle, applied to "is this still the best version" rather than "is this eval set still accurate").
- **#7 (when A/B testing isn't the right tool at all)**: not scenario-tested — e.g., a low-traffic feature where a full live split would take months to reach significance, or a safety-critical guardrail change where testing it live on real users carries real downside risk even during the test itself, calling for a staged rollout or offline eval instead.

Those last two are genuinely distinct enough from what we've covered that they're worth one more round if you want full coverage — particularly #7, since "should we even A/B test this, or is there a better-suited method" is a real judgment call the exam could test, distinct from "is this A/B test designed well" (which is what all four scenarios so far have tested).

Want me to run one scenario on each of those two remaining gaps, or are you comfortable treating them as understood from the concepts and ready to move to the diagnostic process (prompt failure vs. hallucination vs. model mismatch)?