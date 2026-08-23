A hospital group asks for Claude to draft discharge summaries. Clinicians already write them in about six minutes; the real bottleneck is the three-day wait for coding review afterwards. What should the architect recommend?

A
Redirect the build to the coding-review queue, where the three days go
B
Deliver the discharge drafter as asked, then look at coding review later
C
Build drafting and coding review together so the whole pathway improves
D
Keep the drafting scope but add a clinician-facing coding suggestion panel

Explanation · correct: A
Value comes from the constraint, not the most visible step. Drafting saves minutes that are not the problem; the review queue is where days are lost, so that is where the architecture should be pointed.

Why not the others: Delivering the drafter first optimises a step that already takes six minutes and defers the only change that moves the outcome. Doing both at once splits a first release across one high-value and one low-value target. A coding suggestion panel improves what enters the queue without changing the queue itself, so the three days remain.

I'm studying for the Anthropic Claude Certified Architect – Professional (CCAR-P) certification. I got the following question on a mock exam.

<question>
 A production assistant's quality has degraded intermittently across three weeks. Prompt, model version, and application code are unchanged. Which TWO data sources are most useful for diagnosis?
</question>


Possible Answers:
A.The billing dashboard broken down by model and by workload

B.End-to-end traces of affected sessions, with context and tool calls
Total requests served per day across the same three-week window

C. Uptime figures for the model endpoint across the affected period

D.Input analysis for new query types, formats, or languages arriving

Explain:
1. Why the correct answer is correct
2. Why the other answers are incorrect
3. Which Anthropic concept or service I misunderstand
4. A simple mental model to remember the difference
5. A similar example question. Do not give the answer yet.

Search online for official Anthropic documentation and list the relevant sources you used. Be concise in your response.




2

A bank's trade-surveillance assistant runs a fixed pipeline: ingest alert, enrich with counterparty data, classify, route. Compliance now needs to reconstruct, months later, exactly which data drove each classification. What should the design add?


A
Store the full model response text alongside the routing decision taken
B
Raise retention on the existing application log from 90 days to 7 years
C
Log the model version and prompt hash for every classification produced
D
Persist the enrichment payload and its version alongside each classification


Explanation · correct: D
The reconstruction requirement is about the inputs the model actually saw. Persisting the enrichment payload with its version pins the evidence to the decision, which is what an audit months later has to reproduce.

Why not the others: Model version and prompt hash pin the configuration but not the counterparty data that varied per alert. The response text records the conclusion without the evidence behind it. Extending log retention keeps whatever was already captured for longer, and the enrichment state was never in it.

3
One prompt asks Claude to read a 90-page tender, test it against 55 procurement standards, score the fit, and draft a bid recommendation. Standards get skipped and the scoring reasoning is thin. The wording has been revised three times. What next?

A
Move to the most capable model available and raise the output token cap
B
Split it into sequenced subtasks passing structured output along
C
Group the 55 standards into one table the model fills in a single pass
D
Add worked examples of strong bid recommendations to the existing prompt


Explanation · correct: B
Skipped items and shallow reasoning across a long multi-part task is a decomposition signature. Sequenced subtasks give each step focused context and produce a verifiable intermediate artefact you can check before the next step consumes it.

Why not the others: More examples improve the shape of the output, not the coverage of a task too large for one pass. A single fill-in table is still one pass over 90 pages, so the same items go missing — with the omissions now harder to spot. A more capable model and a higher token cap treat capacity as the constraint when structure is.

4
A legal team's precedent-analysis feature runs the most capable model at the highest reasoning effort. Latency has reached 40 seconds and users abandon. Accuracy on the eval suite falls two points at medium effort. What should the architect do?
A
Keep the highest effort and add a progress indicator to the interface
B
Cap output tokens so responses complete inside the latency budget
C
Move to a smaller model at the highest effort to recover the latency
D
Sweep effort levels and take the lowest that clears the quality bar


Explanation · correct: D
Effort is the dial that trades reasoning depth against latency and spend, and the right setting is found by sweeping it against the eval suite rather than assumed. A two-point drop may sit well inside the acceptable band once abandonment is priced in.

Why not the others: A progress indicator changes how the wait feels without shortening it, and these users are already leaving. Swapping to a smaller model at maximum effort moves two variables at once and gives up more capability than the effort dial would. Capping output tokens truncates answers rather than shortening the work that precedes them.

5
Objective · Context Window & Token Optimization
A support agent's sessions run 40 to 60 turns. By turn 30 the transcript is dominated by tool results from steps already resolved, and answer quality degrades. The team wants sessions to continue without losing the thread. What fits best?
A
Clear the superseded tool results, keeping the conversation structure
B
Move to a model whose context window comfortably fits the transcript
C
Start a fresh session every 25 turns and restate the case from scratch
D
Summarise each turn into one line and discard the original exchanges
Explanation · correct: A
The bloat is specifically superseded tool output, so removing exactly that reclaims most of the space while leaving the live conversation intact. Pruning what is stale is cheaper and less lossy than rewriting what is still in use.

Why not the others: Summarising every turn compresses the live thread along with the dead weight, losing detail later turns still rely on. A fresh session every 25 turns discards the case history and pushes restatement cost onto the customer. A larger window postpones the same degradation to a higher turn count rather than removing its cause.

6
Objective · Accuracy–Latency Trade-offs
Adding a re-ranking stage to a field-service assistant lifts answer accuracy from 84% to 92% and adds 600 ms. The contractual SLA is a 4-second response; current p95 latency is 1.9 seconds. How should the architect reason about this?
A
Reject it: added latency is never acceptable in a customer-facing path
B
Defer until the SLA is renegotiated to allow slower responses
C
Adopt it only for the queries the SLA does not formally cover
D
Adopt it: the gain is material and p95 stays inside the SLA


Explanation · correct: D
The framework is to quantify both sides against the committed budget. Eight points of accuracy for 600 ms, landing near 2.5 seconds against a 4-second SLA, leaves clear headroom — adopt it and keep watching p95.

Why not the others: Refusing any latency increase is a rule rather than an analysis, and it forfeits a large accuracy gain the budget can absorb. Applying it selectively fragments behaviour on an arbitrary boundary. Deferring for renegotiation seeks permission the existing numbers already grant.

7
Objective · System Issue Diagnosis
A production assistant's quality has degraded intermittently across three weeks. Prompt, model version, and application code are unchanged. Which TWO data sources are most useful for diagnosis?
The billing dashboard broken down by model and by workload
✓
End-to-end traces of affected sessions, with context and tool calls
Total requests served per day across the same three-week window
✗
Uptime figures for the model endpoint across the affected period
✓
Input analysis for new query types, formats, or languages arriving


Explanation · correct: B, E
With code, model, and prompt unchanged, the cause is almost certainly in what is flowing through. Traces show what the system actually did in the failing sessions, and drift analysis shows whether the inputs changed underneath it.

Why not the others: Spend by model explains cost, not quality. Daily request volume describes load without explaining a quality change. Endpoint uptime describes availability, and the system was available while producing poor answers.





