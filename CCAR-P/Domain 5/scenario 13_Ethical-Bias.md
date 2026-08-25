Moving to sub-topic 5: ethical AI considerations — bias, fairness, and transparency. As flagged in the concepts overview, bias/fairness connects directly back to your Domain 4 metric-selection work (aggregate metrics can hide demographic-level disparities the same way they hid minority-class or tail-latency failures), and transparency is its own distinct concern about whether users understand what they're interacting with and how much to trust it.

**Scenario**

A company's Claude-powered resume-screening assistant scores job applicants and recommends which candidates advance to interviews. The team evaluates the system using overall accuracy against a set of past hiring decisions made by human recruiters, and it scores 91% — strong agreement with historical outcomes. The system launches company-wide.

Six months later, an internal equity review breaks down the system's recommendations by demographic group (something never checked before, since the original evaluation only looked at overall accuracy) and finds: candidates from certain university backgrounds and certain gaps-in-employment patterns (which correlate, though imperfectly, with candidates who took parental leave or had caregiving responsibilities) are recommended to advance at a meaningfully lower rate than their qualifications, evaluated independently, would suggest — a pattern that traces back to the historical hiring decisions the system was trained/evaluated against, which themselves reflected the same human biases the company is now trying to move past.

**Question**: What's the core issue here, and how does it connect to material you've already covered in Domain 4?



No problem — let's work through it together rather than me quizzing you cold.

**Start with what actually happened, step by step:**

1. The system was evaluated using only **overall accuracy against historical human hiring decisions** — meaning "correct" was defined as "matches what human recruiters decided in the past."
2. It scored 91% — that sounds like a strong, trustworthy result.
3. Nobody checked *how* that accuracy was distributed across different demographic groups — only the aggregate number was tracked.
4. Six months later, breaking it down by group reveals the system systematically disadvantages certain candidates — and specifically, the pattern traces back to the *human* hiring decisions it was trained/evaluated against, which already contained the same bias.

**Now connect this to what you already know from Domain 4** — this is really the exact same shape as two scenarios you've already solved correctly:

- **Your fraud-detection scenario**: overall accuracy (97.5%) looked great, but broke down badly on a *specific subgroup* (actual fraud cases) once you looked past the aggregate. Here, overall accuracy (91%) looks great, but breaks down badly on a *specific subgroup* (certain demographic/background patterns) once someone finally checked. Same mechanism: an aggregate metric mathematically dominated by the majority can completely hide serious minority-group failure.

- **Your ground-truth scenarios (Domain 4)**: you learned to ask "is the ground truth itself trustworthy, or could it be biased/flawed?" — for stale data, for low-agreement ambiguous labels. Here it's a third flavor of that same question: the ground truth (historical human hiring decisions) wasn't neutral or objectively "correct" — it was itself the product of human bias. Training/evaluating a system to match biased ground truth means the system learns to *reproduce* that bias faithfully, and a high accuracy score against that ground truth actually means "the system successfully learned to replicate the bias," not "the system is fair."

**So the core issue has two layers, both worth naming**:
1. **A metric-selection gap** (same as fraud detection) — no demographic breakdown was ever checked, only the aggregate, so the bias was invisible until someone specifically looked for it.
2. **A ground-truth/bias problem specific to this domain** — even if you *did* check subgroup accuracy against this same ground truth, "matching historical human decisions" was never actually the right target to optimize toward, because those decisions were biased to begin with. This is a new wrinkle beyond ground-truth staleness or ambiguity — it's ground truth that's *systematically skewed* in a specific, harmful direction.

**What the fix should include, given both layers**:
- Add subgroup-level fairness metrics (not just aggregate accuracy) — measuring whether qualified candidates from different backgrounds are recommended at comparable rates, independent of matching historical outcomes.
- Recognize that "matches what humans did before" is not automatically "correct" or "fair" as a target — the evaluation needs an independent standard for what a fair outcome looks like, not just fidelity to a potentially-biased historical baseline.
- This likely also has real-world legal/compliance implications (many jurisdictions restrict discriminatory hiring practices, including those mediated through automated tools) — tying back to your compliance sub-topic, since bias in hiring isn't just an ethics concern in the abstract, it can be a regulatory one too.

Does that walk-through make sense as the reasoning? Want to try the next one yourself with multiple-choice options, now that this pattern's been laid out?
