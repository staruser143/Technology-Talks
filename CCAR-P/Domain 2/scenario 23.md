**Scenario**

A developer is using Claude Code on a long-running debugging session. After about 90 minutes of work, they notice the session feels "sluggish" and Claude seems to be losing track of earlier decisions made in the conversation. Their immediate instinct is to run `/compact` to free up space and hopefully restore performance.

Before doing that, a colleague suggests running `/context` first. The output shows:

```
Context Usage: 142k/200k tokens (71%)
System prompt: 2.8k tokens (1.4%)
System tools: 17k tokens (8.5%)
MCP tools: 68k tokens (34%)
  - jira-server: 31k tokens
  - slack-server: 22k tokens
  - confluence-server: 15k tokens
Custom agents: 1.2k tokens (0.6%)
Memory files: 3k tokens (1.5%)
Messages (actual conversation): 50k tokens (25%)
Free space: 58k tokens (29%)
```

The developer realizes they connected Jira, Slack, and Confluence MCP servers at the start of this session out of habit, but this particular debugging task hasn't used any of those three tools even once — it's been entirely file reads, bash commands, and conversation about the code itself.

**Question**: Given this breakdown, what's the most appropriate next step, and why?

A) Run `/compact` immediately as originally planned — the context is at 71%, which is high enough to warrant summarizing the conversation regardless of what the breakdown shows.

B) Disable the three unused MCP servers (jira-server, slack-server, confluence-server) first, since they account for 68k tokens (34% of the entire window) and none have been used in this task — this directly recovers more space than compaction would, without touching the actual conversation content (only 50k tokens, 25%) that reflects real decisions and context from the debugging session. Only compact afterward if the conversation itself still needs trimming once the unused tool overhead is removed.
 
C) The breakdown is irrelevant; sluggishness in a long session is always caused by conversation length, so compaction is the correct fix regardless of what /context shows.

D) Disconnect all MCP servers permanently across every future session, since any MCP server is a context liability and none should ever be connected by default.

Take your best guess and I'll walk through it.



Correct — and this scenario is a clean, direct application of the "measure before you fix" sequence we just laid out, with the `/context` breakdown doing exactly the diagnostic work it's designed for.

**Why B is right**

Look at what the breakdown actually reveals: of the 142k tokens consumed, **68k (34%, more than a third of the entire window) is MCP tool definitions for three servers that have contributed zero actual value to this task** — the debugging session has been entirely file reads, bash commands, and conversation about the code, never touching Jira, Slack, or Confluence. Meanwhile, the **actual conversation content — the real decisions, the real debugging history the developer is worried about losing — is only 50k tokens (25%)**, less than the wasted MCP overhead alone.

This is precisely the diagnostic distinction the material draws: the developer's instinct ("feels sluggish → compact the conversation") assumed the bloat was in the conversation, when the breakdown shows the *majority* of consumed space is dead weight that has nothing to do with the conversation at all. Disabling the three unused servers recovers 68k tokens immediately — more than compaction would likely free by summarizing a 50k-token conversation — and it does so **without touching the actual debugging history**, meaning zero risk of losing a decision made earlier in the session, exactly the risk you already identified with compaction (judgment-based summarization can lose things that still matter). This is the "targeted fix over broad fix" discipline running through this entire session: recovering space from something genuinely unused is strictly better than compressing something that's actually still needed, when both options are on the table and the data tells you which one is which.

Only after that — if the conversation itself is *still* large enough to warrant it once the unused overhead is gone — would compaction be the next, more targeted step, and at that point it'd be compacting a genuinely smaller, more relevant pool of content.

**Why the others are wrong**

- **A)** ignores the diagnostic evidence entirely and reaches for the action that was already decided *before* checking `/context` — the whole point of running `/context` first was to inform the decision, and jumping straight to compaction regardless of what it shows discards exactly the information that reveals a better, less risky fix was available. This is the same "diagnose from evidence, not from habit" lesson from nearly every misdiagnosis scenario in this domain — reaching for the familiar fix without checking whether it's actually the right one for what's causing the symptom.
- **C)** asserts a blanket rule ("sluggishness is always conversation length") that the scenario's own evidence directly contradicts — a full third of the context window here has nothing to do with conversation length at all. Treating this as a fixed rule rather than something to verify per-session is the same mistake as every reflexively-applied heuristic you've correctly rejected throughout this whole set (the model-selection scenarios, the caching scenarios, the capability-bloat scenarios).
- **D)** overcorrects into a permanent, blanket policy based on one session's evidence — the fact that Jira/Slack/Confluence were unused *in this specific debugging task* doesn't mean they're never useful; a task that genuinely involves checking a Jira ticket or posting a Slack update would need them connected. The right response to this scenario's evidence is session-specific (disable what's unused *right now*), not a permanent rule discarding tools that are legitimately valuable in other contexts — the same "don't overgeneralize one instance into a universal rule" mistake as several of your earlier wrong-answer options.

**Pattern to remember, closing out this context-management thread properly**: `/context` exists specifically to prevent exactly the mistake the developer was about to make — assuming the bloat is in the conversation and reaching for compaction, when the real bloat might be sitting in unused tool registrations, oversized memory files, or other overhead that has nothing to do with the conversation at all. Checking first turns a guess into a measured decision, and very often reveals that the actual fix is cheaper, safer, and more targeted than the one you'd have reached for by instinct.

That's a genuinely thorough, well-integrated close to Domain 2 and its context-management threads. Want to move to Domain 4 (Evaluation, Testing & Optimization, 16%) now?