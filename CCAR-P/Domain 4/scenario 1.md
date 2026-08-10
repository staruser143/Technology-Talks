**Scenario**

A legal document review startup builds a Claude-powered system that extracts key clauses from contracts (parties, effective dates, termination conditions, indemnification terms) into a structured JSON output. To evaluate the system before launch, the team builds an eval set of 200 contracts with human-annotated "correct" JSON outputs, and measures performance using **exact string match** — the extracted value for each field must match the annotated reference value character-for-character to count as correct.

Running this eval, the system scores only 61% accuracy. But when the team's lead reviewer manually spot-checks 30 of the "incorrect" cases, she finds that in the large majority of them, the extracted value is actually *correct in substance* — e.g., the reference says "January 1, 2025" and the model extracted "1/1/2025," or the reference says "Acme Corporation" and the model extracted "Acme Corp." The dates and party names are right; they're just formatted differently than the human annotator happened to write them.

**Question**: What's the core issue with this evaluation setup, and what should the team do?

A) The 61% score is accurate and concerning; the model's extraction capability is genuinely poor and the team should upgrade to a more capable model before launch.

B) The core issue is a metric-task mismatch: exact string match is too strict for a task where multiple correct representations of the same underlying value exist (date formats, name abbreviations) — the metric is measuring formatting consistency with one specific annotation style, not extraction correctness. The team should use a metric that captures semantic/substantive correctness (e.g., normalized comparison for dates, fuzzy/semantic matching for names, or a rubric-based human/model-judged review for whether the extracted value is substantively correct) rather than exact string match.

C) The eval set itself is flawed and should be discarded; the team should launch without further evaluation, relying on production monitoring instead.

D) The issue is irrelevant — a 61% score is still useful directional information regardless of what's causing it, so the team should proceed with whatever decision they would have made based on the raw number.

Take your best guess and I'll walk through it.


Correct — and this is a direct, concrete instance of the exam trap named explicitly in the concepts overview: "a single, easy-to-compute metric standing in for a genuinely more nuanced quality question it doesn't actually capture."

**Why B is right**

The diagnostic move here mirrors exactly what you've done throughout this whole session: don't trust the aggregate number without checking what's actually driving it. The lead reviewer's spot-check is the critical step — manually inspecting a sample of "failures" and finding that most of them aren't failures at all, just different valid representations of the same correct value. "1/1/2025" and "January 1, 2025" refer to the identical date; "Acme Corp" and "Acme Corporation" refer to the identical entity. The model isn't making extraction errors — it's making *formatting choices* that happen to differ from whatever convention the human annotator used when writing the reference set, and exact string match has no way to distinguish "wrong" from "differently formatted but right."

This is precisely the metric-task mismatch warned about in the concepts overview: exact-match accuracy is the right tool when there's genuinely one correct string (a classification label, a fixed-vocabulary field), but wrong for a task where the underlying *value* matters and multiple valid *representations* of that value exist. The fix follows directly — normalize before comparing (parse both the extracted and reference dates into a common format, then compare), use fuzzy/semantic matching for names (or a canonicalization step, e.g., matching against a known entity list), or fall back to rubric-based human/model judgment specifically asking "is this substantively correct," not "is this byte-identical to the reference." Any of these would likely reveal the system's *true* accuracy is meaningfully higher than 61% — possibly high enough to actually be launch-ready, which the current metric is actively obscuring.

**Why the others are wrong**

- **A)** takes the flawed metric's output at face value and reaches for the "upgrade the model" fix without first checking whether the measurement itself is trustworthy — this is the exact same misdiagnosis pattern as your pricing-grounding and loan-arithmetic scenarios, just relocated to the *evaluation* layer instead of the *production* layer. Upgrading the model wouldn't fix anything here: a more capable model would still get dinged by exact-match for writing "1/1/2025" instead of "January 1, 2025," because the problem was never in the model's extraction capability.
- **C)** overcorrects by discarding evaluation entirely rather than fixing the specific, diagnosable flaw in how it's measured — this is the same "abandon a generally-necessary practice because one implementation of it is broken" overcorrection you've rejected repeatedly (abandoning guardrails, abandoning MCP, abandoning caching). The eval *set* (200 real contracts with human annotations) is likely fine; it's the *metric* applied to it that's flawed — a much narrower, more targeted problem.
- **D)** ignores that the number is actively misleading, not just imprecise — treating a metric known to be measuring the wrong thing as still "useful directional information" is worse than useless if it leads the team to delay launch (or make an expensive model-upgrade decision) based on a score that dramatically understates real performance. A wrong number confidently acted upon is more dangerous than no number at all, precisely because it creates false certainty.

**Pattern to remember, opening Domain 4's diagnostic thread**: an evaluation score is only as trustworthy as the metric producing it — before concluding a system underperforms (or over-performs) based on a number, check whether that metric is actually measuring the dimension you care about, the same way you'd check whether a production symptom traces back to a real cause before reaching for a fix. A low score can mean "the system is bad" or it can mean "the ruler is measuring the wrong thing" — and confusing the two leads to solving a problem that doesn't exist while leaving the real one (if any) undiagnosed.

Want another scenario on metric selection, or move to eval dataset design specifically?