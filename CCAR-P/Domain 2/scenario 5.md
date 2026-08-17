**Scenario**

A financial services company uses a Sonnet-class model to review loan applications and calculate whether an applicant's debt-to-income ratio and other factors meet approval thresholds — a task involving several sequential numeric calculations and rule comparisons. The team notices the model sometimes arrives at the wrong final approve/deny decision, and on inspection, the errors trace back to arithmetic mistakes partway through the multi-step calculation, or applying the wrong threshold rule at one step because it lost track of an intermediate result. The current prompt simply says: "Given this applicant's financial data, determine if they meet our approval criteria and respond with approve or deny." The team is debating whether to upgrade to an Opus-class model to fix the accuracy problem.

Before upgrading, another engineer suggests first testing whether the same Sonnet-class model performs better if the prompt is changed to explicitly instruct: "Work through this step by step: first calculate the debt-to-income ratio, then compare it to the threshold, then check each remaining criterion in order, showing your work at each step, before stating your final approve/deny decision." Testing shows this alone resolves the vast majority of the errors, with accuracy now comparable to what the Opus upgrade would have achieved.

**Question**: What does this outcome demonstrate about diagnosing this kind of failure, and what should the team conclude?

A) The team should still upgrade to Opus-class regardless of the test results, since a more capable model is always a safer long-term choice even if a cheaper fix currently works.

B) This demonstrates that some accuracy failures on multi-step reasoning tasks stem from missing prompting structure (no chain-of-thought guidance), not insufficient model capability — a zero-shot prompt asking directly for a final answer gives even a capable model no structured way to track intermediate results, causing errors that look like a capability problem but resolve with better prompting technique on the same model tier. The team should adopt the improved prompt and avoid the unnecessary cost of upgrading, since testing showed the cheaper model with better prompting matches the more expensive model's performance.

C) This demonstrates that Sonnet-class models are fundamentally unsuitable for any task involving numeric calculation, and the team should switch to a specialized non-Claude calculation tool instead of using Claude for any part of this task.

D) This demonstrates that chain-of-thought prompting is unnecessary complexity that should be avoided; the team got lucky this time, but should upgrade to Opus-class anyway to avoid relying on prompting technique.

Take your best guess and I'll walk through it.


Correct — and this scenario extends the same "diagnose before upgrading" discipline from the pricing scenario into a different failure category: missing *prompting structure* rather than missing *grounding*, both masquerading as "the model isn't capable enough."

**Why B is right**

The original prompt — "determine if they meet our approval criteria and respond with approve or deny" — asks for a final answer directly, with no instruction to work through the intermediate steps explicitly. This matters because a multi-step numeric/rule-based task (calculate ratio → compare to threshold → check next criterion → next → final decision) requires the model to track several intermediate results across steps. Without being prompted to lay that reasoning out, a model is more likely to compress the reasoning internally and drop or garble an intermediate value along the way — which produces exactly the symptom described: arithmetic mistakes and threshold mix-ups partway through, not a failure of understanding the task itself.

This is precisely what chain-of-thought prompting (from the techniques you covered in the domain overview) is *for* — explicitly instructing the model to work step by step and show intermediate results gives it a structured way to track state across a multi-step calculation, rather than trying to hold it all together implicitly in one pass. The test result confirms this diagnosis directly: **the same model tier, with better prompting technique, matches what the more expensive model would have achieved.** That's strong evidence the original problem was never about raw capability — it was about the prompt not giving the model the scaffolding a multi-step task like this needs, regardless of which tier was running it.

This closes out the full "before you upgrade the model, check what else might be missing" pattern across your last two scenarios: the pricing scenario was about missing *grounding* (no current data supplied), this one is about missing *reasoning structure* (no step-by-step scaffolding requested). Both look identical from the outside — "the model gave wrong answers" — and both get misdiagnosed as capability problems if you don't check what the prompt or context actually gave the model to work with.

**Why the others are wrong**

- **A)** ignores the team's own test evidence in favor of a vague "safer long-term" instinct — but the whole point of testing before upgrading is to let evidence, not general caution, drive the decision. If the cheaper model with better prompting genuinely matches the expensive model's accuracy, paying the ongoing cost premium for Opus buys nothing measurable in return; "always upgrade just in case" is the same reflexive-default trap flagged in your MCP scenario, applied to model tier instead of protocol.
- **C)** overgeneralizes a single instance of a *prompting* fix into a categorical claim about model *capability*, and reaches for the most extreme remedy (abandon Claude entirely for this task) when the evidence points the opposite direction — the same model, prompted correctly, already performs well. Nothing in the scenario suggests Claude models are structurally incapable of arithmetic/rule-following; it suggests the *original prompt* didn't give the model what it needed to do that reliably.
- **D)** draws the exact opposite conclusion from what the evidence shows. The test result is a clean demonstration that chain-of-thought prompting was the fix — calling it "unnecessary complexity" while recommending an upgrade instead means paying more money for the same outcome achievable at lower cost with a different (and successfully tested) prompting approach. This also contradicts the domain material's own framing: chain-of-thought exists specifically for tasks requiring multi-step logic, which is exactly what this task is.

**Pattern to remember, completing this diagnostic thread**: 
- The model is giving wrong answers" is not itself evidence that the model needs to be more capable.
- Before upgrading, check two things first: does the model have the *information* it needs (grounding — your pricing scenario), and does the model have the *structure* it needs to reason through the task reliably (prompting technique — this scenario, chain-of-thought specifically for multi-step logic).
-  A capability upgrade is the right fix only once grounding and prompting have been ruled out as the actual cause — and testing cheaper fixes first, as this engineer did, is exactly how you tell the difference instead of guessing.

That's a thorough, well-tested pass through model selection. Want to move to prompt caching / context optimization scenarios now?
