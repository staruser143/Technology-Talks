**Scenario**

A company runs three different Claude-powered features:

1. **A high-volume email subject-line classifier** (spam / not-spam), processing 2 million emails per day, where the task is a simple binary judgment based on short text.
2. **A customer-facing coding assistant** that helps developers debug complex, multi-file codebases, often requiring the model to trace logic across several interacting components and reason about subtle edge cases.
3. **A general-purpose internal chatbot** for answering employee questions about expense policies, meeting scheduling norms, and other everyday business questions — moderate complexity, high daily volume, no deep multi-step reasoning required.

The engineering team, under pressure to simplify their infrastructure, proposes using the **same mid-tier model (Sonnet-class) for all three features**, reasoning that "one model for everything is simpler to maintain and Sonnet is a safe, capable default."

**Question**: Is a single mid-tier model for all three the right call? What would a more optimized approach look like, and why?

A) Yes — using one model everywhere is always the right choice for maintainability; model selection should never vary by task within the same company.

B) No — the spam classifier (#1) is high-volume and low-complexity, making it a strong fit for a smaller/faster/cheaper model (like Haiku-class) to minimize cost and latency at scale; the coding assistant (#2) involves genuinely complex multi-step reasoning across interacting components, making it a strong fit for the most capable model (Opus-class) despite higher cost, since reasoning quality matters most there; the internal chatbot (#3) is a reasonable fit for the mid-tier model as originally proposed, since it needs more capability than simple classification but doesn't require the deepest reasoning. Model selection should match task complexity and volume, not default to one size for everything.

C) No — all three should use the most capable (Opus-class) model available, since accuracy should always be maximized regardless of cost or latency considerations.

D) No — all three should use the smallest/cheapest (Haiku-class) model available, since cost efficiency should always be prioritized over capability.

Take your best guess and I'll walk through it.



Correct — and this scenario is built to test the core discipline of model selection: matching model tier to the *combination* of task complexity and volume/cost sensitivity, not defaulting to a single choice for organizational convenience.

**Why B is right**

Walk through what actually distinguishes these three features, since the exam wants you to reason from task characteristics, not company preference:

- **Spam classifier (#1)**: binary judgment, short text, 2 million requests/day. This is the textbook case for a smaller, faster model — the task genuinely doesn't require deep reasoning (it's a straightforward classification), and at that volume, cost and latency compound massively. Running this on a heavier model wouldn't meaningfully improve accuracy on a task this simple, but it would multiply cost by a large factor across millions of daily calls — the same "right-size the model per step" lever from your Domain 3 accuracy-latency material, just applied at the level of choosing a model for an entire feature rather than one step in a pipeline.
- **Coding assistant (#2)**: explicitly described as requiring tracing logic across multiple interacting components and reasoning about subtle edge cases — this is precisely where a more capable model's deeper reasoning earns its higher cost. Debugging complex, multi-file logic is exactly the kind of task where a lighter model is more likely to miss subtle interactions or propose a fix that breaks something elsewhere. Here, accuracy genuinely trades off against cost in the *other* direction — worth paying for the most capable model because task difficulty, not volume, is the dominant constraint.
- **Internal chatbot (#3)**: moderate complexity, no deep multi-step reasoning, high volume but not extreme. This is the case where the "safe mid-tier default" the team proposed is actually the right fit — not because it's convenient, but because it happens to match this specific task's actual requirements: more capability than pure classification needs, but nowhere near the reasoning depth the coding assistant needs.

The point of the scenario: **the team's proposed one-size-fits-all reasoning happens to be correct for exactly one of the three features (#3), by coincidence, and clearly wrong for the other two.** A uniform choice applied for maintenance convenience, without evaluating each task's actual profile, will systematically overspend on the simple task and underserve the hard one.

**Why the others are wrong**

- **A)** states the team's flawed reasoning as if it were a rule — "one model for everything is always right" ignores that these three tasks have genuinely different complexity and volume profiles, and treats maintainability as automatically outweighing the real cost and quality consequences of a mismatched model. Simpler infrastructure is a real, valid consideration, but it's a trade-off to weigh against task fit — not something that overrides task fit categorically.
- **C)** repeats a mistake you've now rejected multiple times across domains (your loan-scheduling and fraud-detection scenarios both featured a version of "the most thorough/accurate option always wins," and it was wrong both times). Running 2 million daily spam-classification calls on the most capable model would be a massive, unjustified cost increase for a task that doesn't need that reasoning depth — "maximize accuracy regardless of cost" ignores that the accuracy gain on a simple binary task would likely be negligible anyway, while the cost increase would be very real.
- **D)** makes the mirror-image mistake — optimizing purely for cost regardless of task difficulty would degrade the coding assistant's actual usefulness on genuinely hard debugging tasks, where a lighter model is more likely to miss the kind of subtle, multi-component reasoning the task explicitly requires. Cost efficiency matters, but not at the expense of the one feature in this set that specifically needs deeper reasoning capability to do its job.

**Pattern to remember**: 
- Model selection is a per-feature (or even per-step, as you saw in Domain 3) decision driven by two independent variables — task complexity (how much reasoning depth is actually needed) and volume/cost sensitivity (how much does getting this wrong on model choice compound at scale) — not a single company-wide default chosen for convenience.
-  The exam will often give you multiple features or steps with different profiles in one scenario specifically to test whether you evaluate each independently rather than reaching for one answer that "sounds safe" across the board.

Want another model-selection scenario, or move to a scenario on prompt caching/context optimization next?
