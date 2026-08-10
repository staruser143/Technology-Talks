Let's go deeper on the actual levers you can pull, since the exam tends to test whether you know *which knob to turn*, not just that a trade-off exists.

## The core trade-off, restated precisely

Every step you add to a retrieval or verification pipeline that improves accuracy — more sources, larger candidate sets, re-ranking, multi-hop retrieval, verification passes — costs time. The exam's version of this question is rarely "should you care about latency" (obviously yes) — it's "given this specific latency budget, which combination of techniques gets you the most accuracy without blowing it."

## The concrete levers

**1. Parallelize independent retrieval calls.** If you're pulling from three separate sources (a vector database, a SQL lookup, a document store), and none depends on the others' results, run them concurrently instead of sequentially. This is the same "independent subtasks" recognition from your legal-playbook multi-agent scenario, just applied to retrieval calls instead of agents — sequential unnecessarily stacks latency; parallel doesn't.

**2. Retrieve wide, re-rank narrow, cheaply.** Cast a wide net with a fast, cheap method (vector search over thousands of candidates is fast), then apply a more expensive re-ranking step only to a small shortlist (e.g., top 50 → re-rank → top 5). You get most of the accuracy benefit of thorough re-ranking while only paying its cost on a small set, not the whole corpus.

**3. Cache aggressively for repeated queries.** If a meaningful fraction of requests are similar or identical (common support questions, standard lookups), caching retrieval results or even full responses avoids paying the accuracy-latency cost repeatedly for the same answer. This is a "free" latency win that doesn't sacrifice accuracy at all, so the exam tends to treat "have you considered caching" as a first move before trading anything away.

**4. Tiered/staged responses (fast path + async follow-up).** This is the pattern from your logistics mixed-review scenario: give an immediate, lighter-weight response within the SLA, then run the more thorough, slower verification in the background and follow up or flag exceptions if something doesn't check out. You satisfy the latency constraint on the critical path and still get thoroughness where it matters, just not synchronously.

**5. Right-size the model per step.** Not every step in a pipeline needs your most capable (and slowest) model. A cheap classification or extraction step can often run on a smaller, faster model, reserving the more expensive model for the step where reasoning quality actually matters. This trades a small amount of per-step accuracy for meaningful latency and cost savings — usually a good trade when the cheap step is genuinely simple.

**6. Reduce candidate/context size, not just downstream steps.** Progressive discovery (from the last message) is itself a latency lever — smaller context to process means faster generation, on top of the accuracy benefit from less irrelevant noise.

**7. Set an early-exit/confidence threshold.** If an initial fast pass is confident enough (e.g., a high-similarity exact match), skip the expensive verification/re-ranking steps entirely for that request. Reserve the costly path for genuinely ambiguous cases. This means your *average* latency stays low even though your worst-case pipeline is thorough — the SLA is about typical experience, and this lets the system spend its "latency budget" selectively rather than uniformly.

## How the exam tends to frame this

Expect a scenario with a stated numeric latency budget (like your loan-scheduling and logistics scenarios) and a design that currently exceeds it. The trap answers are usually: "just accept the higher latency because accuracy matters more" (ignores a stated hard constraint), or "abandon the accurate approach entirely" (overcorrects). The credited answer usually **combines two or three of the levers above** — e.g., parallelize the independent lookups *and* move re-ranking to only the ambiguous cases *and* cache common queries — rather than picking one silver-bullet fix. This mirrors the "compound problems need compound, specific fixes" lesson from your logistics mixed-review scenario in Domain 1 — the same discipline shows up here, just with latency-focused levers instead of decomposition/feedback-loop fixes.

