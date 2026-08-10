**Scenario**

A legal-tech company needs Claude to process incoming contracts: extract key dates and parties (straightforward extraction), summarize the contract's main obligations (moderate synthesis), and flag any clauses that deviate from standard risk tolerance (requires nuanced legal judgment comparing clause language against known risk patterns). Two engineers propose different approaches:

**Engineer A's proposal**: Use the most capable (Opus-class) model for one single call that does all three tasks — extraction, summarization, and risk-flagging — together, reasoning that "the risk-flagging part needs strong reasoning, so just use the best model for everything and keep it simple."

**Engineer B's proposal**: Decompose into three separate calls — use a smaller/faster (Haiku-class) model for the straightforward extraction step, a mid-tier (Sonnet-class) model for the summarization step, and reserve the most capable (Opus-class) model specifically for the risk-flagging step, since that's the only part that genuinely needs deep reasoning.

**Question**: Which approach better reflects sound model-selection practice, and why?

A) Engineer A's approach — using one capable model for the entire task is simpler to build and guarantees the highest quality across all three subtasks, since a more capable model is never worse than a less capable one at any task.

B) Engineer B's approach — decomposing the task lets model selection be matched independently to each subtask's actual reasoning requirements: cheap/fast for simple extraction, moderate for synthesis, and reserving the most expensive model specifically for the one subtask (risk-flagging) that actually needs its reasoning depth — avoiding paying premium-model cost on the two subtasks that don't need it.

C) Neither — a single prompt on the cheapest available model is sufficient for all three subtasks, since Claude models are capable enough that model tier rarely matters in practice.

D) Engineer A's approach — because decomposing into three separate calls always costs more in aggregate than one call to a single capable model, regardless of which model is used for each step.

Take your best guess and I'll walk through it.


Correct — and this scenario deliberately combines two things you've now drilled separately (decomposition from Domain 1, model right-sizing from this domain) to test whether you can apply them together, which is exactly the kind of synthesis the real exam rewards on its harder items.

**Why B is right**

Engineer A's instinct — "the hard part needs a strong model, so use the strong model for everything" — makes a subtle but important error: it treats model capability as an *all-or-nothing* choice for the whole task, when the task itself isn't uniform. Extraction, summarization, and risk-flagging require genuinely different reasoning depths:

- **Extraction** (dates, parties) is close to pattern-matching against structured text — the same profile as your survey-tagging scenario. A smaller model handles this reliably and cheaply.
- **Summarization** (main obligations) needs real synthesis but not deep judgment — a solid fit for a mid-tier model, similar to your internal-chatbot case from the first model-selection scenario.
- **Risk-flagging** (comparing clause language against risk patterns) is the one subtask that actually resembles your intake-triage case — subtle judgment, real consequences if wrong (a missed risky clause could mean a bad contract gets signed). This is where paying for the most capable model's reasoning depth is genuinely justified.

Engineer B's approach recognizes that **decomposition and model selection are complementary tools, not separate decisions**: once you split a task into its component steps (Domain 1's decomposition principle), each step becomes its own independent model-selection decision (this domain's principle). Running all three steps through Opus, as Engineer A proposes, means paying premium-model cost on the two steps (extraction, summarization) that don't need it — the same wasted cost pattern from your spam-classifier scenario, just now showing up as one wasteful sub-step inside an otherwise-necessary pipeline rather than as an entire oversized feature.

**Why the others are wrong**

- **A)** rests on a false premise worth naming directly: "a more capable model is never worse than a less capable one at any task" isn't the actual justification for using it everywhere — even where it's technically true on quality alone, it ignores that quality is only one axis. The real question was never "would Opus do fine at extraction" (probably yes) but "does extraction need Opus's reasoning depth to do fine" (no) — paying for capability the task doesn't require is waste, not safety margin, the exact same logic that made "use the most capable model for all three features" wrong in your hospital scenario's tempting wrong answer C.
- **C)** overcorrects in the opposite direction, and the scenario gives you a specific, concrete reason to reject it: risk-flagging is explicitly described as requiring "nuanced legal judgment comparing clause language against known risk patterns" — this is precisely the kind of subtle, high-stakes judgment your intake-triage scenario established as the marker for needing a more capable model. Treating "model tier rarely matters" as a general rule ignores that you've now seen multiple scenarios (coding assistant, intake triage) where it mattered a great deal.
- **D)** asserts a blanket cost claim without basis — three calls using right-sized models per step (cheap, moderate, expensive only where needed) is very plausibly *cheaper in aggregate* than one call to the most expensive model for the entire task, especially if extraction and summarization make up the bulk of the token volume. The real comparison isn't "one call vs. three calls" in the abstract, it's "cost of matched-tier models across three steps" vs. "cost of one premium-tier model across the same total work" — and premium-tier pricing applied to steps that don't need it is the more expensive path, not the cheaper one.

**Pattern to remember, tying Domain 1 and Domain 2 together**: when a task has genuinely separable subtasks with different reasoning requirements, the model-selection decision shouldn't be made once for the whole task — it should be made per-subtask, after decomposition. A single "hardest part" requirement doesn't justify applying the most expensive model uniformly across parts of the task that don't share that requirement. This is a common shape for the exam's harder Domain 2 items specifically because it forces you to recognize that decomposition (which subtask needs what) has to happen *before* model selection can be done well (which model fits each subtask) — treating them as one combined judgment gets you Engineer B's answer.

Want another model-selection scenario, or move to prompt caching / context optimization scenarios next?