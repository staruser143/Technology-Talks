**Scenario**

A financial services company wants Claude to answer employee questions about internal HR policies (benefits, leave, expense rules) via a Slack bot. The current design: every question triggers a fresh retrieval of the top 30 candidate chunks from a vector database covering all HR documents, followed by a re-ranking pass on all 30 candidates down to the best 5, which are then sent to Claude along with the question. Measured performance: average response time is 6.5 seconds, but the company's SLA for the Slack bot is 3 seconds. An analysis of six months of query logs shows that roughly 70% of questions are near-duplicates of a small set of extremely common questions ("how many PTO days do I get," "what's the parental leave policy," "how do I submit an expense report"), while the remaining 30% are genuinely varied, one-off questions.

**Question**: Given the latency constraint and this usage pattern, which combination of changes would most effectively bring the system within the 3-second SLA while preserving accuracy?

A) Reduce the number of retrieved candidates from 30 to 10 for every query, and skip re-ranking entirely for every query — a uniform, simpler pipeline for all requests.

B) Add a cache for the common/near-duplicate questions (serving cached answers near-instantly for that ~70%), and keep the full retrieve-30-then-rerank-to-5 pipeline only for the remaining ~30% of genuinely novel questions.

C) Switch entirely to a larger context window model and pass all HR documents directly in every prompt, eliminating retrieval altogether.

D) Keep the current pipeline unchanged for all queries, but tell users to expect responses within 3 seconds anyway, since most queries will feel fast once cached by the underlying infrastructure automatically.

Take your best guess and I'll walk through it.




Correct — and this scenario is built directly around lever #3 (caching) plus recognizing that a uniform pipeline is the wrong response when your query distribution isn't uniform.

**Why B is right**

The query log analysis is the whole scenario, and the exam wants you to actually use it: 70% of traffic isn't just *similar*, it's near-duplicate of a small, stable set of common questions. That's a textbook caching opportunity — you don't need to re-run retrieval and re-ranking for "how many PTO days do I get" every single time it's asked, because the answer to that question doesn't change between requests. Serving cached answers for that majority slice gets you near-instant responses for most traffic with zero accuracy cost (the answer is the same because the underlying policy is the same).

Critically, B **doesn't touch the pipeline for the remaining 30%** — the genuinely novel, one-off questions still get the full retrieve-30-then-rerank-to-5 treatment, because those are exactly the cases where thoroughness actually matters and where you can't shortcut without risking a wrong or incomplete answer on an HR policy question. This is the tiered/staged pattern (lever #4) combined with caching (lever #3) — using two levers together, applied selectively based on evidence about which requests need which treatment. That's the same "compound problem, compound fix, applied selectively" discipline from your logistics mixed-review scenario.

**Why the others are wrong**

- **A)** applies a uniform downgrade to every query regardless of type — this is the mistake of treating "reduce accuracy budget" as a blanket policy instead of a targeted one. It might get you under 3 seconds, but it pays that latency win by cutting corners on the 30% of genuinely hard questions too, where reduced retrieval depth is most likely to cause a real accuracy problem — exactly where you can least afford it.
- **C)** ignores the actual bottleneck. Passing all HR documents into every prompt trades a retrieval-latency problem for a "processing a huge prompt every time" problem, which doesn't obviously get you under 3 seconds and actually makes *every* query pay the cost of the full document set, including the 70% that could've been served near-instantly from cache. It also throws away the log evidence entirely — the data is telling you most queries are repetitive, and this answer ignores that entirely in favor of brute force.
- **D)** doesn't change anything about the system and just hopes an unspecified "underlying infrastructure" caching layer materializes on its own — there's no evidence in the scenario that such caching exists, and telling users to expect 3 seconds doesn't make a 6.5-second pipeline actually respond in 3 seconds. This is the "argue with the constraint instead of solving it" trap from your loan-scheduling scenario, just phrased as denial instead of renegotiation.

**Pattern to remember**: when a scenario hands you usage-pattern data (query logs, traffic distribution, repeat-rate stats) alongside a latency constraint, that data is almost always the key to the correct answer — it's telling you *where* you can safely cut cost (the repetitive, low-variance slice) versus where you can't (the genuinely novel slice). A uniform fix applied to all traffic, in either direction, tends to be the wrong answer when the underlying traffic isn't uniform.

