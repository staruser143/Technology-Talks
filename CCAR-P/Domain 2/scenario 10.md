**Scenario**

A company's Claude-powered internal writing assistant helps employees draft casual Slack messages and internal memos. The system prompt includes a single guardrail instruction: "Maintain a professional but friendly tone; avoid profanity and avoid making commitments on behalf of the company (e.g., promising deadlines, discounts, or contractual terms) — if asked to draft something that would require such a commitment, note that a human should confirm the specific details before sending."

A new compliance-minded employee reviews this and argues: "This is just an instruction — the model could still ignore it and draft something with profanity or an unauthorized commitment. Per the salary-change incident precedent, this needs to be backed by structural or system-level enforcement, like a mandatory keyword filter blocking certain words and a hard block preventing the assistant from ever mentioning deadlines, discounts, or terms in any draft."

**Question**: Is the compliance employee's recommendation the right call here? Should this guardrail also be upgraded to structural/system-level enforcement?

A) Yes — per the earlier principle that instructions alone aren't reliable enforcement, every guardrail, regardless of the task's stakes, should be backed by structural or system-level enforcement to be considered a real safeguard.

B) No — this guardrail protects a low-stakes, easily-reversible output (a draft Slack message or memo that a human will read and send themselves, not an action that executes automatically). An occasional tone slip or an unconfirmed mention of a deadline in a *draft* has minimal real consequence, since a human is already in the loop by the nature of the task (drafting content someone else will review and send). Adding hard keyword blocks here risks the over-restriction problem from your last scenario (blocking legitimate uses of words like "deadline" or "discount" in perfectly appropriate contexts) for a risk level that doesn't warrant it. Instructional guardrails are proportionate here; the salary-change case involved an action that executed automatically without further human review, which is what actually justified structural enforcement there.

C) No — guardrails are never necessary for internal-facing tools, only customer-facing ones, so this instruction should be removed entirely.

D) Yes, but only the profanity filter needs structural enforcement; the commitment-related guardrail should remain instructional, since profanity is more embarrassing than an unauthorized commitment.

Take your best guess and I'll walk through it.


Correct — and this scenario is the necessary counterweight to the salary-change scenario, testing whether you actually absorbed *why* structural enforcement was warranted there, or just concluded "structural enforcement is always better than instructional."

**Why B is right**

The compliance employee's argument sounds principled — "instructions alone aren't reliable, so back everything with hard enforcement" — but it over-generalizes the salary-change lesson by dropping the variable that actually mattered there: **what happens automatically if the guardrail fails.** In the salary-change scenario, a guardrail failure meant a real payroll change could reach the payroll system with no further human checkpoint — the model's own judgment was the *only* thing standing between "ambiguous request" and "money moves." That's what made instructional-only enforcement insufficient: the action executed on the model's say-so alone.

This scenario is structurally different in exactly that respect: the output is a *draft* — a Slack message or memo that a human employee reads, edits if needed, and chooses to send themselves. The human-in-the-loop already exists here, built into the nature of the task, not bolted on as an extra safeguard. If the guardrail fails and the model drafts something with a stray "I'll guarantee this ships by Friday" or an off-tone word, the consequence is bounded: a human sees it before it goes anywhere, and can catch or fix it before any real commitment is made. That's a fundamentally lower-stakes failure mode than an unconfirmed salary change silently reaching payroll.

B also correctly flags the cost side of over-applying structural enforcement here: a hard keyword block on "deadline," "discount," or "terms" would trigger on completely legitimate, harmless uses of those words in ordinary business writing ("let's discuss the project deadline," "I saw a great discount at the conference booth") — this is exactly the over-restriction failure mode from your last scenario, just introduced *proactively* this time instead of diagnosed after the fact. Matching enforcement level to actual stakes isn't just about not under-protecting high-risk actions — it's equally about not over-constraining low-risk ones.

**Why the others are wrong**

- **A)** takes the salary-change lesson and turns it into a blanket rule stripped of the reasoning that made it correct there — "instructions aren't reliable, so structural enforcement everywhere" ignores that structural enforcement has real costs (rigidity, over-blocking legitimate content, engineering overhead) that only make sense to pay when the actual risk justifies it. This is the same "one lever always applies regardless of context" trap you already correctly rejected in your content-moderation early-exit scenario back in Domain 3 — a fix that was right in one situation doesn't generalize to every situation that superficially resembles it.
- **C)** invents an unrelated distinction (internal vs. customer-facing) that isn't what actually determines guardrail necessity — plenty of internal tools handle consequential, high-stakes actions (the salary-change assistant itself was internal-facing), and plenty of customer-facing tools are low-stakes. The real variable is action consequence and reversibility, not audience.
- **D)** picks an arbitrary distinction between the two constraints within the same guardrail (profanity vs. commitments) based on which one "feels more embarrassing," rather than evaluating actual consequence — neither profanity nor an unconfirmed commitment in a *draft* reaches anyone before a human reviews it, so there's no principled reason to structurally enforce one and not the other; both are equally covered by the same "human reviews the draft before it goes anywhere" safety net.

**Pattern to remember, completing the guardrails set**: the salary-change and writing-assistant scenarios are a matched pair testing the same skill from both directions, just like your MCP/direct-API pair and your bloat/no-bloat pair. The deciding question for whether a guardrail needs structural enforcement isn't "is this important" or "did a bad precedent exist somewhere else" — it's specifically **does a human check this before real-world consequence occurs, or does the model's compliance with the instruction stand alone as the only safeguard before something consequential happens automatically.** Draft content reviewed by a human before sending needs proportionate, usually instructional guardrails. Actions that execute directly need enforced ones.

That's thorough, matched-pair coverage of guardrails. Ready to move into templates/caching scenario practice now?