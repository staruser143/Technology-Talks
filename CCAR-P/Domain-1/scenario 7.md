**Scenario**

A startup wants Claude to triage incoming customer support emails into one of five categories (billing, technical issue, feature request, complaint, other) and assign a priority level (low, medium, high). Right now this is a single prompt: "Here's the email, return the category and priority as JSON." It performs well — accuracy checks show it's correct on 96% of a 500-email test set, and it runs in under a second per email. A new team member suggests splitting this into two separate calls — one prompt to determine category, a second prompt to determine priority based on the category — arguing that "decomposition is always a best practice, so this should be split too."

**Question**: Is the team member's suggestion correct?

A) Yes — decomposition is a best practice, so any multi-output task should be split into separate calls for each output, regardless of current performance.

B) No — this task is already a single, well-defined, narrow classification with strong measured accuracy and low latency; splitting it adds a second LLM call (cost, latency) and a dependency (priority now waits on category) without a demonstrated problem to fix.

C) Yes — but only because priority should never be determined in the same call as category, since priority is inherently more subjective.

D) No — because splitting classification tasks is technically impossible in Claude; category and priority must always be returned together.

Take your best guess and I'll walk through it.



Correct — and this scenario exists specifically to catch people who over-apply what they just learned. Good instinct not to.

**Why B is right**

Decomposition is a tool for solving a specific problem: a task where errors trace back to the model doing *too much at once* — competing objectives, dropped details in long inputs, structural inconsistency between outputs. This task doesn't show any of those symptoms. It's:

- **Narrow in scope** — one short email in, one small structured JSON out. There's no long input to lose attention across, unlike the 90-minute transcript.
- **Measurably working** — 96% accuracy on a real test set and sub-second latency isn't a hunch, it's evidence. Domain 4 (Evaluation) reinforces this same instinct: you diagnose based on measured failure modes, not on principle.
- **Not exhibiting any decomposition symptom** — no dropped details, no structural mismatch, no internal contradiction between category and priority in the outputs described.

Splitting it anyway would add a second LLM call (extra cost and latency, potentially doubling response time) and introduce a dependency that didn't exist before (priority now can't be computed until category finishes) — for a problem that isn't there. That's a real regression, not neutral.

**Why the others are wrong**

- **A)** is the trap this scenario is built around. "Decomposition is a best practice" is true as a *conditional* tool — apply it when a task shows the specific failure signatures from the last scenario (attention dilution, sequencing errors, output contradictions). Treating it as a universal rule to apply to *every* multi-output task ignores that decomposition has real costs (latency, cost, complexity) that only pay off when there's a real problem to solve.
- **C)** invents a rule that isn't supported by anything in the scenario — "priority is inherently more subjective" isn't a stated fact here, and even if priority were harder to judge, that alone doesn't mean it needs a separate call if the combined approach is already hitting 96% accuracy.
- **D)** is simply false as a technical claim — nothing prevents splitting classification tasks into separate calls; the question is whether you *should*, not whether you *can*.

**The exam-level takeaway**: this pairs directly with your transcript scenario to test the *boundary* of the same principle from both sides. Decomposition is diagnosed from evidence of a specific problem (dilution, sequencing, contradiction) — not applied reflexively because "it's generally good practice" or because a task happens to produce more than one output. A well-performing, narrow, measured task is itself the evidence that decomposition isn't needed. If the exam gives you accuracy numbers and latency figures that already look good, that's usually a signal the "correct" answer is *leave it alone*, not *add architecture*.

