**Scenario**

A software company wants Claude to help their DevOps team investigate production incidents. When an alert fires, Claude should look at the error logs, form a hypothesis about the cause, and then decide what to check next — maybe query a metrics dashboard, maybe check recent deploys, maybe search past incident reports for similar patterns — repeating this investigation until it either identifies a likely root cause or determines it needs human escalation. The number and type of checks needed varies wildly: a database timeout might take two lookups, a cascading service failure might take ten. The team's stated priority is accuracy of the root-cause diagnosis; they've explicitly said they're willing to accept variable response times for this.

**Question**: Which architectural pattern best fits this system, and why?

A) Workflow — because there's a clear repeatable process (check logs, check metrics, check deploys, check history) that can be defined as a fixed sequence of steps.

B) Agentic — because the next action depends on what's discovered at each step, and the number of steps isn't known in advance.

C) Augmented LLM — because giving Claude access to logs, metrics, and deploy history as tools in a single call lets it investigate everything at once.

D) Workflow with a human-in-the-loop gate after each step, since incident diagnosis is high-stakes and needs approval before Claude proceeds further.

Take your best guess, and I'll walk through the reasoning.




Correct again. Let's lock in why, since this scenario was designed to test the exact boundary you nailed last time from the other direction.

**Why B is right**

Apply the same test: does the *path* vary, or just the *content*? Here, the path itself is unknown in advance — "maybe query metrics, maybe check deploys, maybe search history," repeating an unknown number of times depending on what's found. That's the defining trait of agentic: the LLM observes the result of one action and uses it to decide the next action, in a loop, until it judges the investigation complete. The team's own stated priority — accuracy over predictable latency — is also a direct signal. Variable, open-ended latency is the cost you pay for letting the model reason its way through an unpredictable problem space, and this team explicitly said they're willing to pay it.

**Why the others are wrong**

- **A) Workflow** is the trap mirroring last time's trap, just flipped. It's tempting because there IS a repeatable *menu* of possible checks (logs, metrics, deploys, history). But a menu of available actions isn't the same as a fixed *sequence*. A workflow would force Claude to check metrics before deploys every single time, even when the log output already screams "recent deploy broke this" and checking metrics first would be a waste. That rigidity is exactly wrong for a domain where a two-lookup case and a ten-lookup case both need to be handled well.

- **C) Augmented LLM** fails because it implies one call with all the tools available. In practice, a single LLM turn can only reason over what it's already gathered — it can't observe the result of one tool call and use that to decide the next one within the same turn. Investigation-by-observation across multiple rounds *is* the loop; you can't compress it into a single-shot call without losing the "observe, then decide" behavior that root-cause diagnosis needs.

- **D) Human-in-the-loop after every step** sounds responsible but wasn't asked for, and it defeats the point. The scenario doesn't say this is an action-taking system (nothing here changes production state, it's investigation and diagnosis) — so gating every read-only lookup behind human approval would slow it to a crawl for no safety benefit. Human-in-the-loop belongs at points of *consequence* — e.g., before Claude restarts a service or pages someone — which this scenario doesn't include. (This is worth flagging because Domain 5, Governance, will test exactly when human-in-the-loop gates are warranted — the answer is "at points of irreversible or high-stakes action," not "everywhere.")

**Pattern refinement**:
- A fixed *menu* of available actions is not the same as a fixed *sequence*.
- If the model must choose which action, in what order, based on intermediate results — that's agentic, even if every individual action it might take is itself well-defined.
