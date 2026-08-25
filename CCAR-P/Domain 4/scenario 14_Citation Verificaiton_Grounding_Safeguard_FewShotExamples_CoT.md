**Scenario**

A legal research company's Claude-powered assistant answers questions about case law by citing relevant precedents. Two different quality issues are reported by users in the same week:

**Issue A**: A user asks about a specific contract dispute doctrine. The response correctly identifies the doctrine and explains it accurately, but cites "Henderson v. Blackwood Industries, 2019" as a supporting case — a case that, upon checking, does not exist anywhere in any legal database. The explanation of the legal principle itself is completely correct; the specific case citation is simply fabricated.

**Issue B**: A different user asks the assistant to compare three specific statutes side by side. The response correctly retrieves and understands all three statutes (verified — the underlying facts about each statute are accurate) but presents them in three inconsistent formats — one as a bulleted list, one as a paragraph, one as a table — making the comparison hard to actually read, and the response skips explicitly stating which statute is most favorable for the user's stated situation, something a well-constructed answer should conclude with.

**Question**: How should these two issues be diagnosed, and are they the same category of problem?

A) Both issues are the same category — model capability limitations — and both require upgrading to a more capable model to resolve.

B) They're two distinct categories. Issue A is a hallucination: the legal principle (content grounded in what the model knows) is accurate, but a specific supporting detail (the case citation) is fabricated and untraceable to any real source — confident, plausible-sounding content not actually supported by reality. Issue B is a prompt failure: the underlying content (the statute facts) is accurate and correctly understood, but the output lacks structural consistency (mismatched formats) and skips a step the task requires (explicit comparative conclusion) — a specification/structure gap, not a content-fabrication problem. Different root causes need different fixes: Issue A needs a citation-verification/grounding safeguard (e.g., require citations to be checked against a real case database, or instruct the model to flag uncertainty rather than invent a citation); Issue B needs few-shot examples demonstrating consistent format and explicit chain-of-thought-style instruction to always state a conclusion.

C) Both issues are prompt failures and should be fixed with the same solution: adding more detailed formatting instructions to the system prompt.

D) Both issues are hallucinations, since both involve the model producing output that doesn't fully match what a correct response should contain.

Take your best guess and I'll walk through it.


Correct — and this scenario is built to test the exact distinction from the deep dive: prompt failure and hallucination can produce superficially similar complaints ("the response wasn't right"), but they have genuinely different signatures once you look at *where* the inaccuracy actually lives.

**Why B is right**

Apply the diagnostic test from the material to each issue separately, since that's the actual skill being assessed:

- **Issue A**: the legal *principle* — the actual substantive content the user needed — is correct. What's wrong is one specific, discrete claim (a case citation) that, when checked, traces to nothing real. This is precisely the hallucination signature: "the content itself, where present, tends to be accurate [elsewhere in the response]... but specific claims inside it are fabricated." The model wasn't confused about structure, format, or what was being asked — it confidently generated a citation-shaped piece of content to support an otherwise-accurate explanation, without that citation being grounded in anything real. This is architecturally the same failure as your telecom pricing scenario: a model filling a gap (here, "cite a supporting case" where perhaps no perfectly on-point cached case came to mind) with something plausible-sounding rather than flagging uncertainty or omitting the unsupported detail.

- **Issue B**: the underlying facts about all three statutes are verified accurate — there's no fabrication anywhere in this response. What's wrong is entirely structural: inconsistent formatting across the three statutes, and a missing step (explicit comparative conclusion) that a well-constructed answer to this specific kind of question should include. This is precisely the prompt-failure signature: content correct, structure/completeness of the *response format* wrong. It matches your product-description scenario (format inconsistency) and your insurance-memo scenario (skipped required step) simultaneously — a combined signature you've already learned points to few-shot examples (demonstrating consistent format) plus explicit instruction to always include the comparative conclusion (the same "show the reasoning/complete the required step" logic as chain-of-thought, just applied to "always draw an explicit conclusion" rather than "show step-by-step arithmetic").

Because the two issues have genuinely different root mechanisms, they need genuinely different fixes — exactly as B states: Issue A needs a grounding/verification safeguard (checking citations against a real database, or instructing the model to omit or flag uncertain citations rather than inventing plausible-sounding ones), while Issue B needs structural prompting fixes (few-shot format examples, an explicit "always conclude with X" instruction). Treating them as the same problem would mean applying the wrong fix to at least one of them.

**Why the others are wrong**

- **A)** repeats the now-thoroughly-rejected pattern of reaching for a model upgrade as the default fix, and does so for *both* issues simultaneously without first checking either against the cheaper, more likely explanations — exactly what the deep dive names as "the most repeated mistake pattern in this entire session." Neither issue shows the model-mismatch signature (persistent failure despite proper grounding and structure, tied to genuine reasoning difficulty); both have clear, specific, targeted alternative explanations.
- **C)** correctly identifies Issue B's category but misapplies the same label to Issue A, and proposes a fix (formatting instructions) that does nothing for a fabricated citation — no amount of formatting guidance would prevent the model from inventing a case name that doesn't exist, since the problem was never about how the citation was presented, it was about whether it was real.
- **D)** makes the opposite category error — labeling Issue B a hallucination when nothing in it is fabricated; every fact presented is verified accurate. Calling a formatting/completeness problem a "hallucination" would lead you toward a citation-verification-style fix that's irrelevant here, when what's actually needed is structural prompting guidance.

**Pattern to remember, opening this diagnostic scenario arc**: the fastest way to tell hallucination from prompt failure is to ask, separately, "is the *content* accurate where present?" and "is the *structure/completeness* correct?" A response can fail on one axis while succeeding on the other, and — as this scenario shows directly — two issues that both sound like "the response wasn't quite right" can require completely opposite diagnostic conclusions and completely different fixes once you actually separate content accuracy from structural correctness.

Want another scenario distinguishing these categories, or one specifically testing the model-mismatch boundary (when upgrading actually is the right call)?
