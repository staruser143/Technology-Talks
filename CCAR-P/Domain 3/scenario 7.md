**Scenario**

A company builds a Claude-powered assistant for its sales team that helps draft follow-up emails after client calls. To do this, it needs access to: the client's contact info, recent deal notes, and past email thread history for that specific client — all pulled from the company's CRM. The engineering team sets up an OAuth integration with the CRM and requests the following scopes for the integration: `contacts:read`, `deals:read`, `emails:read`, `emails:send`, `deals:write`, `contacts:write`, and `admin:users` — reasoning that "we might add features later that need to update deal stages or contact info, and requesting broader scopes now means we won't have to go through OAuth re-approval again when we do."

**Question**: What's the problem with this scope request, and what should the team do?

A) No problem — requesting broader scopes upfront is efficient engineering practice, since it avoids the friction of re-requesting OAuth approval every time a new feature is added.

B) This is scope creep: the assistant's actual current task (drafting follow-up emails using contact info, deal notes, and email history) only requires read access to contacts, deals, and emails, plus the ability to send emails. `deals:write`, `contacts:write`, and `admin:users` are unjustified by the stated task and should not be requested — scopes should be requested to match what the current feature actually needs, with additional scopes requested (and re-approved) if and when a future feature genuinely requires them.

C) The problem is `emails:send` specifically — since sending emails is a write action, the assistant should only ever be given read access, and a human should manually copy and send every drafted email.

D) There's no real problem with the scopes themselves, but the team should add a confirmation dialog before every action, which makes the broad scopes safe regardless of what's granted.

Take your best guess and I'll walk through it.



Correct — and this scenario isolates scope minimization cleanly, without the confused-deputy/service-account complexity from the last one, so the reasoning should feel sharper.

**Why B is right**

Walk through what the stated task actually requires versus what was requested:

- **Needed**: `contacts:read` (client info), `deals:read` (deal notes), `emails:read` (thread history for context), `emails:send` (the actual output — sending the drafted follow-up).
- **Requested but unjustified**: `deals:write`, `contacts:write`, `admin:users` — none of these are used by anything the task description mentions. They're being requested purely speculatively, for hypothetical future features.

The team's stated reasoning — "we might need it later, and pre-requesting avoids OAuth re-approval friction" — is precisely the scope-creep justification the material calls out: broader access than the task needs, "in case it's useful later." Notice what's being traded away for that convenience: `admin:users` in particular is wildly disproportionate to "draft follow-up emails" — if this integration is ever compromised, misconfigured, or manipulated (say, via a prompt injection sitting in a deal note or email thread the assistant reads as context), the actual blast radius is bounded by what the credential *can* do, not by what the feature was *designed* to do. An assistant that only ever needed to draft and send emails now has a technical path to modify user accounts platform-wide. That gap between designed-for and technically-capable-of is exactly what scope minimization exists to close.

The right process, as B states: request what the current feature needs, and treat future features as a reason to request *additional* scopes *when they're actually built* — re-approval friction is a legitimate cost, but it's the cost of keeping the access boundary honest, not a cost worth avoiding by pre-granting access nothing currently uses.

**Why the others are wrong**

- **A)** treats avoiding re-approval friction as worth more than keeping the credential's actual power matched to actual need — this is the scope-creep justification stated as if it were a virtue instead of the problem itself.
- **C)** misidentifies which scope is the issue. `emails:send` is directly justified by the stated task — the assistant's whole purpose is drafting *and sending* follow-ups, so removing send access doesn't fix scope creep, it just breaks the feature and pushes the actual work onto a human anyway, while leaving the real overreach (`deals:write`, `contacts:write`, `admin:users`) completely untouched.
- **D)** makes the same mistake as the previous scenario's wrong answer A: treating a UI/process safeguard (a confirmation dialog) as a substitute for actually bounding what the credential can do. A confirmation dialog might catch some misuse if a human is paying close attention every time, but it doesn't reduce the credential's technical capability at all — and it does nothing against failure modes that don't route through a visible action a human would think to scrutinize (e.g., a compromised or manipulated call using `admin:users` in a way that isn't obviously flagged as suspicious in whatever the confirmation UI shows).

**Pattern to remember, refined from the last scenario**: the previous scenario was about *who* the credential acts as (confused deputy — one identity for many users). This one is about *what* the credential can do regardless of who it's acting for (scope minimization — request only current-need permissions). Both are least-privilege violations, but they're different axes: identity/delegation vs. permission breadth. A scenario can have either problem alone, or — as your first auth scenario showed — both stacked together. Learn to check both axes independently: "is this the right *identity* boundary?" and separately, "is this the right *permission* boundary?"

Want another scope minimization scenario, one on the audit-logging/attribution angle, or move to observability at scale?