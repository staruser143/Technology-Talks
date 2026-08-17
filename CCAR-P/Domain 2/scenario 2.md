**Scenario**

A hospital network is building two Claude-powered features, both processing short text at high volume:

1. **Patient satisfaction survey tagging**: classifies free-text survey comments ("food was cold," "nurse was wonderful") into predefined categories (food, staff, cleanliness, wait time, other) for a quarterly report. ~50,000 comments processed per week.
2. **Nurse intake note triage**: reads short, free-text nursing notes written during patient intake ("pt reports chest tightness, mild SOB, denies radiation") and assigns an urgency level (routine / urgent / emergent) that determines how quickly a physician reviews the patient. ~50,000 notes processed per week.

An engineer, having just internalized "high volume + short text = use the smallest/cheapest model," proposes using a Haiku-class model for both features, since they look structurally identical: short input, high volume, single-label classification output.

**Question**: Is the engineer's reasoning correct for both features? What's the right model choice for each, and why?

A) Yes — both tasks are short-text, high-volume classification, so both should use the smallest/cheapest model; the surface structure of the task (short input, single-label output) is what determines the right model tier.

B) No — while survey tagging (#1) is a good fit for a smaller/faster model (low stakes, straightforward category matching, an occasional misclassification has minimal real-world consequence), intake triage (#2) looks structurally similar (short text, single-label output) but is NOT a good fit for the same tier: it requires picking up on subtle clinical language and has severe real-world consequences if urgency is misjudged (a missed "emergent" case could delay critical care). Task stakes and the subtlety of judgment required — not just input length and output format — should drive model selection; #2 warrants a more capable model despite its surface resemblance to #1.

C) No — both should use the most capable model available, since this is a healthcare application and healthcare applications should always use the highest-tier model regardless of task.

D) No — the intake triage task should use a cheaper model than the survey tagging task, since triage is more time-sensitive and cheaper models respond faster.

Take your best guess and I'll walk through it.


Correct — and this scenario is designed to catch the exact trap the previous scenario's lesson could create if over-applied: pattern-matching on *surface* task shape ("short text, high volume, classification") instead of what actually determines model fit.

**Why B is right**

The engineer's proposed rule — "high volume + short text = smallest model" — is a reasonable-*sounding* heuristic extracted from the last scenario, but it's the wrong variable to generalize on. Volume and input length were correlated with the right answer in the spam-classifier case, but they weren't the *reason* Haiku was right there — the reason was that spam/not-spam is a low-stakes, low-ambiguity judgment where an occasional error has minimal consequence. This scenario deliberately holds volume and input length constant across both features specifically to isolate the variable that actually matters: **stakes and judgment subtlety**.

- **Survey tagging (#1)**: "food was cold" → category "food" is close to unambiguous pattern-matching. Misclassifying a handful of comments in a quarterly report has essentially no real-world consequence — nobody's care changes based on this. Small, fast, cheap model is genuinely appropriate here, same reasoning as the spam classifier.
- **Intake triage (#2)**: "pt reports chest tightness, mild SOB, denies radiation" requires picking up on subtle clinical signal — the difference between "routine" and "emergent" can hinge on exactly the kind of nuanced language interpretation that's much easier for a model to get wrong under compressed reasoning capacity. And critically, the consequence of getting it wrong is severe and asymmetric: misjudging an emergent case as routine could genuinely delay care for a patient in danger. This is a case where the "surface shape" of the task (short text in, single label out) is almost irrelevant to the model-selection decision — what matters is that a wrong answer here is categorically more costly than a wrong answer on survey tagging, even though both tasks look identical from a token-count and output-format perspective.

**Why the others are wrong**

- **A)** takes the exact bait the scenario is designed to set — generalizing from the previous scenario's *correlated* signals (volume, input length) rather than the *actual* driver (stakes/subtlety), and ends up recommending the smallest model for a task where a misjudgment could delay urgent care. This is a good example of why the exam tests boundary cases immediately after establishing a pattern — to see whether you learned the underlying principle or just the surface pattern.
- **C)** overcorrects in the other direction by treating "healthcare" as a blanket trigger for "always use the most capable model," which ignores that survey tagging is genuinely low-stakes *even within* a healthcare context — the domain a task sits in doesn't automatically dictate its stakes; the specific task does. This wastes cost on a task that doesn't need it, the same category of mistake as your Domain 3 "add human review to every read-only lookup" trap — applying a blanket safety posture without matching it to actual risk.
- **D)** confuses two unrelated properties — inferring that "time-sensitive" implies "should use a cheaper/faster model" gets the trade-off backwards. Smaller models are typically faster per-token, but that speed advantage doesn't matter if the model is more likely to be *wrong* on a task requiring subtle judgment — for triage specifically, being fast but occasionally missing an emergent case is far worse than being a bit slower but reliably accurate. Time-sensitivity argues for a model that gets it right the first time, not necessarily the fastest model available.

**Pattern to remember, extending your model-selection framework**: 
- The deciding factors are task **complexity/subtlety of judgment required** and **stakes/consequence of an error** — not input length, output format, or volume, which can look identical across tasks with very different real risk profiles.
- Two tasks that are structurally twins on the surface (short input, single-label classification, similar volume) can call for completely different model tiers once you look at what the judgment actually requires and what happens if it's wrong.
- The exam will use this exact "twin-looking tasks, different correct answers" structure to check whether you're reasoning from the right variable.

Want another model-selection scenario, or move to prompt caching / context optimization scenarios next?
