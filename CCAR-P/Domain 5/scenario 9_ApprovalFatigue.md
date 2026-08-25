**Scenario**

A mid-sized company initially deployed a Claude-powered expense-reimbursement assistant using post-action audit (matching the earlier "high volume, low stakes" logic) — but after a minor incident, leadership overcorrected and mandated pre-action approval for *every* expense, regardless of amount, reasoning that "more human oversight is always safer." A single finance manager is now responsible for approving roughly 200 expense requests per day, each requiring her to click "approve."

Three months later, an internal review finds: the manager's average time spent per approval is under 4 seconds, she has approved 100% of requests submitted in the last month with zero rejections, and a sample of her approvals includes several that clearly violate stated policy (personal items submitted as business expenses, amounts significantly exceeding category limits) — errors a genuine review would have caught immediately, but didn't, because she is functionally unable to meaningfully evaluate 200 requests a day and has settled into approving everything by default just to keep up.

**Question**: What's the core issue here, and how does it relate to the original mode-selection decision?


**Question**: What's the core issue here, and how does it relate to the original mode-selection decision?

A) There's no issue — a 100% approval rate simply reflects that all submitted expenses genuinely comply with policy, and the fast approval times reflect the manager's efficiency and expertise at quickly recognizing valid requests.

B) This illustrates **approval fatigue** — a specific failure mode where pre-action approval, when applied at a volume the human reviewer cannot realistically sustain genuine scrutiny for, degrades into a rubber stamp that provides the *appearance* of oversight without its actual substance. This directly validates the original volume-based reasoning from the mode-selection scenario: pre-action approval was correctly identified as fitting *low-volume, high-stakes* workflows specifically because a human can genuinely evaluate each one; forcing it onto a *high-volume* workflow (200/day) doesn't make the workflow safer, it makes the "approval" step functionally meaningless while creating a false sense of security that a safeguard is working. The team should revert to post-action audit (the originally-correct mode for this volume/stakes profile) or, if some pre-action gating is still wanted, restrict it specifically to expenses above a meaningful risk threshold rather than applying it uniformly to all 200 daily requests.

C) The issue is that the finance manager is underperforming and should be replaced with a more diligent reviewer, since the problem is about individual effort rather than workflow design.

D) The issue is that pre-action approval is inherently flawed as a concept and should never be used for any workflow, since this incident demonstrates human reviewers cannot be trusted to perform genuine oversight under any circumstances.

Quick close on that one before we move on — the answer is B, and it's the direct, real-world validation of your Workflow 1 mode-selection reasoning: pre-action approval forced onto a volume it can't sustain doesn't add safety, it creates a rubber-stamp illusion of oversight (4-second reviews, 100% approval rate, policy violations sailing through) while feeling like a stronger safeguard than the post-action audit it replaced. The fix is reverting to the volume-appropriate mode, or restricting pre-action gating to a genuinely high-risk subset rather than applying it uniformly. Worth holding onto: "more human oversight" isn't automatically safer if the volume makes genuine oversight impossible — it can produce a worse safeguard than a lighter-touch one that's actually sustainable
