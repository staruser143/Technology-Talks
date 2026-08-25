
Scenario

A marketing content company runs two separate Claude-powered features:

Feature 1: A "campaign tagline generator" — given a product description, it produces five different creative tagline options for the marketing team to choose from. The team specifically wants meaningful variety across the five options each time it's run, even for the same product description — they don't want five near-identical taglines.

Feature 2: A multi-step budget-allocation tool — given a marketing budget and a set of channel performance metrics, it needs to work through several dependent calculations (cost-per-acquisition by channel, projected ROI, optimal allocation split) to arrive at a final recommended budget split. The team has noticed occasional arithmetic errors when the calculation involves many channels.

An engineer proposes: for Feature 1, increase the temperature to encourage variety; for Feature 2, also increase the temperature, reasoning that "more randomness might help the model explore different calculation paths and catch errors it would otherwise lock into."


**Question**: Is this engineer's reasoning correct for both features?

A) Yes — temperature is a general-purpose quality lever, and increasing it should improve outcomes for both variety-seeking and accuracy-seeking tasks, since more randomness generally gives the model more opportunities to find a good answer.

B) No — the reasoning is correct for Feature 1 but wrong for Feature 2. Higher temperature is the appropriate lever for Feature 1, since generating varied, distinct tagline options is exactly a wording-variety task. But Feature 2's arithmetic errors are a reasoning-depth problem (the same signature as the loan-approval multi-step calculation scenario), not a wording-variety problem — temperature affects how tokens are sampled in the output, not how carefully the model works through dependent calculations. Increasing temperature for Feature 2 wouldn't help it "explore different calculation paths" in any meaningful sense; if anything, it risks introducing more inconsistency into a task that needs more consistency. The actual fix for Feature 2 is effort/extended thinking (or explicit chain-of-thought prompting), which is largely incompatible with non-default temperature in the first place.

C) No — the reasoning is wrong for both features; temperature should be set to 0 for both regardless of task type, since determinism is always preferable in a production system.

D) Yes, but only for Feature 2 — arithmetic tasks specifically benefit from higher temperature because it allows the model to consider a wider range of possible numerical outputs before converging on an answer.


Correct — and this scenario directly tests whether you internalized the temperature-vs-reasoning-depth correction from the last message rather than defaulting to "temperature is a general quality knob."

**Why B is right**

Feature 1 and Feature 2 have genuinely different failure modes, and matching the right lever to each is the actual skill being tested:

- **Feature 1** is a pure wording-variety task — the team explicitly wants meaningfully different phrasings across five outputs for the same input. This is exactly what temperature is designed for: a higher temperature flattens the token-selection distribution, making the model more likely to produce genuinely different word choices and creative directions across generations, rather than five near-identical variations of the same most-probable phrasing. This is the correct, textbook use case.

- **Feature 2** shows the exact signature from your loan-approval scenario: arithmetic errors on a multi-step, dependent calculation (CPA by channel → ROI → allocation split), which is a reasoning-structure problem, not a wording problem. The engineer's proposed mechanism — "randomness might help the model explore different calculation paths and catch errors" — misunderstands what temperature actually does. Temperature doesn't give the model multiple internal attempts at a calculation to choose the best from; it changes how tokens are sampled in the *single* generation being produced. Injecting more randomness into how a number gets written out doesn't make the underlying arithmetic more careful — if anything, it adds noise to a process that needs more consistency and structure, not less. The actual fix, as established, is effort/extended thinking or explicit step-by-step prompting — giving the model room to work through and track intermediate values, which is a fundamentally different lever from temperature and is largely incompatible with it being set away from default in the first place.

**Why the others are wrong**

- **A)** treats temperature as an all-purpose "make it better" dial, which is precisely the misconception this whole thread has been correcting — temperature's actual effect (output token sampling variety) has no direct bearing on reasoning quality or arithmetic accuracy, so applying it to a reasoning problem doesn't engage the actual mechanism causing the errors.
- **C)** overcorrects by prescribing determinism universally regardless of task — but Feature 1 has a stated, legitimate need for variety (five genuinely different tagline options), and forcing temperature to 0 there would work directly against the feature's actual purpose, likely producing five very similar taglines the marketing team explicitly said they don't want.
- **D)** repeats the engineer's exact misunderstanding and extends it further, inventing a mechanism ("considering a wider range of possible numerical outputs before converging") that isn't how temperature or model generation actually works — there's no internal "consideration of a range before converging" process that temperature taps into; temperature affects the sampling of the single sequence being generated, token by token, not a search over alternative reasoning paths.

**Pattern to remember, closing this topic**: the diagnostic question to ask before touching temperature is the same kind of question you've applied throughout this whole session — what is the actual symptom, and does this lever address that specific mechanism? "I want varied outputs" → temperature is the right lever. "I want more careful, accurate reasoning" → temperature is the wrong lever entirely; that's effort/extended thinking's job, and the two are largely mutually exclusive by design, which itself is a strong signal that they're meant to solve different problems, not overlapping versions of the same one.

Want another scenario on this, or a different topic you wanted to firm up?