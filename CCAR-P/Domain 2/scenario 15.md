**Scenario**

A company uses Claude to generate short product descriptions from a structured spec sheet (product name, dimensions, materials, key features). The current zero-shot prompt says: "Write a product description based on the following spec sheet: {spec}." Testing across 50 products shows the *content* is consistently accurate — no factual errors, all key features mentioned correctly — but the *format* is inconsistent: sometimes it's a single paragraph, sometimes bullet points, sometimes it opens with a question, sometimes a bold headline, with no discernible pattern, and the marketing team wants every description to follow one specific consistent structure (a one-sentence hook, then three bullet points, then a closing sentence) that's hard to fully describe in words but easy to demonstrate.

**Question**: Given this specific failure pattern, which prompt engineering technique is most directly suited to fix it, and why?

A) Chain-of-thought — instruct the model to "think step by step about the best way to structure this description" before writing, since reasoning through the structure first should produce more consistent output.

B) Few-shot — provide 2-3 example spec-sheet-to-description pairs showing the exact desired structure (hook, three bullets, closing sentence), since the problem is output format/style inconsistency, not factual/reasoning errors, and demonstrating the desired pattern directly addresses that.

C) Switch to a more capable model — the inconsistency suggests the current model isn't capable enough to reliably produce structured output.

D) Neither zero-shot, few-shot, nor chain-of-thought will help; the only fix is to build a custom structured-output parser that reformats whatever Claude produces into the desired structure after the fact.

Take your best guess, and I'll walk through the reasoning.


Correct — and this scenario is built to isolate the signature that specifically points to few-shot, distinct from the chain-of-thought signature you already nailed in the loan-approval arithmetic scenario.

**Why B is right**

The diagnostic detail that matters most: **content is consistently accurate, format is inconsistent, with no discernible pattern to the variation.** That's a completely different failure signature from your loan-approval scenario, where the model was making actual *reasoning* errors (wrong arithmetic, lost track of intermediate results) — a content/logic failure. Here, the model clearly understands the spec sheet and extracts the right information every time; what's missing is a single, specific structural convention that's "hard to fully describe in words but easy to demonstrate" — that phrase is doing a lot of work in the scenario, because it's precisely the condition under which few-shot outperforms trying to write an ever-more-detailed instruction.

Chain-of-thought wouldn't touch this problem at all — asking the model to "think step by step about the best way to structure this" doesn't tell it *which* structure the marketing team actually wants; it would still be guessing at an underspecified target, just with more reasoning tokens spent arriving at an equally arbitrary choice. The task doesn't have a multi-step logic or calculation problem to reason through — there's no intermediate state being lost, no arithmetic being botched. It has an underspecified-target problem, and showing 2-3 examples of spec-sheet-in, hook+three-bullets+closing-sentence-out directly gives the model the exact pattern to match, which is what few-shot is for.

**Why the others are wrong**

- **A)** applies chain-of-thought to a problem it isn't suited for — the scenario's failure signature (consistent content, inconsistent format) doesn't show any evidence of a reasoning breakdown, which is what CoT specifically addresses. This is exactly the trap named in the material: reaching for CoT on a formatting/consistency problem that few-shot would fix more directly.
- **C)** repeats the now-familiar misdiagnosis pattern from your pricing-grounding and loan-arithmetic scenarios — treating a structural/technique problem as a capability problem. A more capable model given the same underspecified zero-shot instruction ("write a product description") would have no more information about the marketing team's specific desired structure than the current model does; capability doesn't substitute for showing the model what "correct" actually looks like.
- **D)** overcorrects into abandoning prompting technique entirely in favor of post-hoc parsing — but there's no evidence in the scenario that a fix at the prompt level is insufficient; few-shot is a well-suited, much simpler fix for exactly this failure mode. Building a custom parser to force arbitrary free-form output into a fixed shape after the fact is a heavier, more brittle solution to a problem that direct demonstration likely solves cleanly at the source.

**Pattern to remember, now with both signatures established**: content/reasoning failures (wrong logic, dropped intermediate results, arithmetic errors on multi-step tasks) point to chain-of-thought. Format/style/consistency failures, where the model clearly understands the content but doesn't know the specific structural convention you want, point to few-shot. These are genuinely different failure signatures, and the exam's job task analysis clearly wants you to diagnose which one you're looking at before reaching for a fix — reaching for the wrong technique wastes tokens without addressing the actual gap, exactly as you saw with model-tier upgrades in the earlier scenarios.

Want another scenario — maybe one where the right answer combines few-shot and chain-of-thought together, since a task can need both — or move to a different sub-topic?