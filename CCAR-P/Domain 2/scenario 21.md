**Scenario**

A legal-tech company builds a multi-agent contract review system — the same underlying task as your earlier legal-playbook scenario (IP terms, liability, termination clauses, data privacy), but now implemented as an actual multi-agent system: one orchestrator delegates each playbook review to its own specialized subagent, each running in its own isolated context window, then the orchestrator synthesizes all four findings into one consolidated risk report.

The current implementation has each subagent return its findings as free-form prose — a full narrative writeup of everything it found while reviewing its assigned playbook against the 200-page contract, often running 1,500-2,000 words per subagent, including its reasoning process, which clauses it considered and ruled out, and its final conclusions all blended together. The orchestrator receives all four of these free-form writeups and has to read through, interpret, and synthesize roughly 7,000 words of varying-structure prose into the final consolidated report.

The team notices two problems: (1) the orchestrator's synthesis step is slow and expensive, since it's processing a large volume of prose on every run, and (2) the final consolidated report is inconsistent in quality — sometimes a subagent's actual key finding gets lost or under-emphasized in the synthesis step because it was buried in the middle of that subagent's long narrative alongside a lot of reasoning detail the orchestrator didn't actually need.

**Question**: What's the most targeted fix here, and why?

A) Switch the orchestrator to a more capable model, since a smarter model should be able to synthesize the four writeups more reliably regardless of their format.

B) Require each subagent to return a structured, schema-constrained result (e.g., `{"category": "IP", "risk_level": "low"|"medium"|"high", "flagged_clauses": [...], "recommendation": "..."}`) instead of free-form narrative prose — this directly reduces the token volume the orchestrator has to process (addressing cost/latency) and eliminates the "buried key finding in a long narrative" problem (addressing the inconsistent-quality symptom), since the orchestrator now receives exactly the fields it needs rather than having to extract them from unconstrained prose. Subagents can still reason internally however they need to reach their conclusions; only the return channel needs to be constrained.

C) Give each subagent access to the full 200-page contract plus all four playbooks simultaneously, so they can cross-reference each other's areas and produce a single unified writeup instead of four separate ones.

D) Reduce the number of subagents from four to two by combining categories, since fewer subagents means less total context for the orchestrator to process.

Take your best guess and I'll walk through it.


Good question — the honest answer is: **both exist, and they're not equally good.**

**Automatic (auto-compact)**: Claude Code monitors token usage and, when the conversation approaches the context limit (roughly the mid-90s percent range), it steps in on its own — it clears older tool outputs first, and if that's not enough, runs a summarization pass over the conversation history, replacing older messages with a condensed summary. This is the safety net that keeps a long session running instead of hard-failing when it hits the limit.

**Manual (`/compact`)**: you trigger the same underlying mechanism deliberately, on your own timing, optionally with a focus (e.g., `/compact focus on the API changes`) to steer what gets prioritized in the summary.

**Which is actually better, and why it matters for the exam's framing**: the sources are consistent that manual, deliberately-timed compaction tends to produce better outcomes than waiting for the automatic trigger, for a reason that maps directly onto something you already reasoned through in your IT-support scenario: **automatic compaction happens under pressure, at a fixed threshold, without input on what's actually still important** — it makes its own judgment call about what to keep and discard, and by the time it fires (often quite late, close to the hard limit), quality may have already started degrading from context crowding before compaction even kicks in. Manual compaction, run earlier and at a natural breakpoint you choose, lets you front-load exactly the kind of selective retention you already identified as the right technique when relevance doesn't correlate cleanly with recency — you can point it at what to focus on, rather than trusting a generic "compress everything older" pass to correctly guess that a stated constraint from early on still matters.

**One important caveat directly relevant to something you flagged earlier in this thread**: compaction is explicitly *not* the same as guaranteed retention. Details from early in the conversation — including instructions — can be lost during compaction, which is exactly why persistent, must-not-be-lost information (rules, standing constraints) is better placed somewhere that survives compaction entirely (e.g., a project-level instructions file) rather than relying on it surviving inside conversation history that might eventually get summarized away. This is a sharper, more concrete version of the same lesson from your trip-planning selective-retention scenario: a shellfish allergy stated in turn 2 is exactly the kind of fact you would *not* want left to a generic auto-summarization pass to correctly judge as worth preserving — anything that critical belongs in a more durable, explicitly-protected form of context, not just trusted to survive ordinary compaction.

So, to directly answer: it's available both ways, but the exam-relevant judgment mirrors everything else in this domain — automatic is a safety net, manual/deliberate is the more reliable choice when you actually know what's still load-bearing and want to control what survives, and truly critical standing facts shouldn't depend on either mechanism getting the call right.

Want a scenario testing this — the difference between relying on auto-compaction versus deliberate context management for something safety-critical?