**Scenario**

A mid-sized company's IT helpdesk Claude agent has five tools: reset password, check ticket status, look up software license availability, restart a user's VPN connection, and check system status/outage information. A newly hired engineer, reviewing the configuration, immediately flags this as "capability bloat" and proposes splitting it into five separate single-tool agents, arguing that "any agent with more than 2-3 tools is bloated by definition and should be split for cleanliness."

A quarterly usage review shows: all five tools are used regularly (ranging from 12% to 28% of conversations each, no orphaned or zero-usage tools), all five tools serve the same coherent purpose (resolving common IT helpdesk requests), and support staff report that most real helpdesk conversations naturally involve 2-3 of these tools together (e.g., a user reports being unable to connect, which often involves checking system status, restarting the VPN connection, and sometimes a password reset, all in one conversation).

**Question**: Is the engineer's "split into five single-tool agents" proposal correct? What's the actual assessment here?

A) Yes — any agent with more than 2-3 tools should be split by default, regardless of usage patterns, since fewer tools per agent is always safer and cleaner.

B) No — this is not capability bloat. All five tools are actively and regularly used, serve one coherent purpose (IT helpdesk support), and real conversations naturally span multiple tools together — splitting them apart would force a single coherent user need (e.g., "I can't connect") to be handled across multiple disconnected agents, adding friction without reducing any actual unjustified capability. Bloat is diagnosed by evidence of unused, unjustified, or unrelated-purpose tools — not by tool count alone.

C) No — the tools should not be split, but only because five is a "safe" number; if the agent had eight or more tools, splitting would automatically be correct regardless of usage data.

D) Yes — but only two of the five tools should be split off (whichever two are used least), while keeping the other three together, since some splitting is always better than none.

Take your best guess and I'll walk through it





Correct — and this scenario is the necessary counterpart to the last one, because it tests whether you actually understood the *diagnostic method*, or just absorbed "fewer tools = better" as a rule to apply reflexively.

**Why B is right**

Run the same audit method from your last scenario, and this time it clears the agent instead of flagging it:

- **No unused/orphaned tools** — every tool is actively invoked, at meaningful and fairly balanced rates (12-28% each). Contrast this directly with the retail scenario's two tools sitting at zero calls for two months tied to a dead initiative — that specific signal, the thing that made bloat diagnosable there, is simply absent here.
- **One coherent purpose, not several unrelated ones** — all five tools serve "resolving common IT helpdesk requests." Compare to the retail agent, whose tools spanned order support, promotions, loyalty, subscriptions, and merchandising — five genuinely different business functions. Here it's one function with five facets of it, which is a meaningfully different shape.
- **Real usage naturally spans multiple tools per conversation** — this is the detail that makes splitting actively counterproductive rather than merely unnecessary. If "user can't connect" routinely needs system-status-check + VPN-restart + sometimes password-reset *in the same conversation*, splitting these into five separate single-tool agents doesn't remove unjustified capability — it forces one coherent user problem to be handled by stitching together multiple disconnected agents, adding real friction (handoffs, lost context, more orchestration overhead) for zero bloat-reduction benefit, since there was no bloat to reduce.

The core distinction, stated plainly by B: **bloat is diagnosed by evidence — unused tools, unjustified scope, unrelated purposes — not by tool count in isolation.** Five actively-used, thematically-coherent, jointly-invoked tools is a well-scoped agent that happens to have five tools. Nine tools with two orphaned and four spanning unrelated business functions is a bloated agent. The number alone tells you nothing; the usage pattern and purpose-coherence are what actually answer the question.

**Why the others are wrong**

- **A)** is the engineer's flawed reasoning restated as the answer, and it's the exact trap this scenario exists to catch: a fixed numeric threshold ("more than 2-3 tools = bloated") applied without checking usage or purpose-coherence. This is the same category of mistake as "decomposition is always a best practice, so split everything" from your very first decomposition-boundary scenario — a generally-reasonable-sounding heuristic turned into a rule applied without evidence.
- **C)** accepts the right conclusion (don't split) but for the wrong, equally arbitrary reason — swapping "more than 2-3 is bloated" for "five is safe, eight isn't" is still a count-based threshold rather than a usage/purpose-based diagnosis. This matters for the exam the same way it mattered in your MCP-vs-direct-API scenario with option C: landing on a correct-sounding answer via the wrong reasoning process doesn't generalize, and the exam is testing the reasoning, not just the conclusion.
- **D)** invents a problem to solve ("some splitting is always better than none") without any evidence pointing at particular tools — nothing in the usage data (12-28%, all actively used, no orphans) singles out any two tools as bloat candidates. Splitting off the "least used" two when the least-used one is still at 12% and still gets combined with others in real conversations doesn't reduce unjustified capability, it just fragments a working, coherent tool set based on relative ranking rather than actual evidence of a problem.

**Pattern to remember, completing the capability bloat picture**: this scenario and the retail one are a matched pair testing the same skill from both directions, exactly like your MCP/direct-API pair and your CRM/hospital auth pair. The diagnostic is never tool count — it's usage evidence (is anything actually unused or orphaned?) and purpose-coherence (do these tools serve one job, or several unrelated ones?). A large, cohesive, actively-used tool set isn't bloat. A small tool set can still be bloated if even one or two of its tools are unjustified or orphaned. The exam wants you to apply the *method* consistently, not memorize a number.

That's thorough coverage of capability bloat from both directions. Ready for a mixed review across all seven Integration sub-topics now, or should we move on to Domain 4 (Evaluation, Testing & Optimisation, 16%)?