Moving to sub-topic 3: managing stakeholder feedback loops and expectation alignment, including SLAs.

**Scenario**

During project scoping, a stakeholder insists on a specific SLA: "99.9% uptime and under 1-second response time, guaranteed, from day one of launch." The architect knows from technical assessment that 1-second response time is achievable for simple queries but not for the more complex analytical requests this system will also handle (which realistically need 3-4 seconds given the reasoning depth required), and that 99.9% uptime from day one — before any real production experience with this specific system — is an aggressive target most new systems don't hit until after some operational maturity.

Three approaches:

**Option A**: Agree to the requested SLA as stated, since pushing back risks appearing unable to deliver what the stakeholder wants, and the team can "figure out how to hit it" after the contract is signed.

**Option B**: Propose a differentiated, realistic SLA collaboratively — e.g., a tiered response-time commitment (sub-1-second for simple queries, a clearly different target for complex analytical ones) and a phased uptime target (a slightly more conservative commitment for the first few months, stepping up to 99.9% as operational experience is gained) — explaining the reasoning behind each number rather than just presenting a lower counter-offer.

**Option C**: Tell the stakeholder that SLAs can't reasonably be discussed until after launch, since actual performance can only be known once the system is running in production.

**Question**: Which approach reflects sound SLA-negotiation practice, and why?


**Question**: Which approach reflects sound SLA-negotiation practice, and why?

A) Option A — agreeing to the requested SLA demonstrates responsiveness to the client's needs, and if the targets prove genuinely unachievable, they can be renegotiated later once the shortfall becomes apparent.

B) Option B — collaboratively proposing a differentiated, technically-grounded alternative (tiered response times matched to actual query complexity, a phased uptime ramp reflecting realistic operational maturity) with clear reasoning gives the stakeholder something concrete to evaluate and negotiate from, rather than either an unrealistic promise or no commitment at all — setting expectations that the system can actually be held to, rather than setting up an inevitable, foreseeable SLA breach.

C) Option C — since true performance numbers can only be confirmed after launch, refusing to discuss SLA commitments until then is the only honest approach, even though it leaves the stakeholder without any planning basis in the meantime.

D) All three are reasonable depending on how much negotiating leverage the architect's team has in the deal, since SLA-setting is fundamentally a negotiation dynamic rather than a technical one.


Correct — and this scenario is a direct application of the exact discipline from your last two sub-topics, now specifically framed as SLA-setting: don't manufacture false confidence to please a stakeholder (Option A), don't refuse to engage with legitimate planning needs (Option C), but translate the real technical picture into something concrete and negotiable (Option B).

**Why B is right**

The requested SLA — 99.9% uptime and sub-1-second response, guaranteed, from day one — isn't just aggressive, it's built on a flattened assumption: that all requests are equivalent in complexity, and that day-one performance should match mature-system performance. The architect's technical assessment already shows this doesn't hold: complex analytical queries genuinely need more time given the reasoning involved, and uptime reliability typically improves with real operational experience, not from a standing start. Option B doesn't just reject the stakeholder's number — it replaces the flattened assumption with a more accurate model of how the system actually behaves, translated into a structure the stakeholder can actually negotiate: **tiered response times matched to real query complexity** (the same "different tasks have different needs" logic from your model-selection and latency-optimization material, now expressed as a customer-facing commitment rather than an internal design choice), and a **phased uptime target** that's honest about the gap between day-one and mature-system reliability, with a clear path to the eventually-desired number.

This matters because an SLA isn't just a target to hit — it's a promise the business will be held to, often with real financial or contractual consequences for missing it. Setting an SLA the team already knows, at signing time, is unlikely to be met isn't "being responsive to the client" — it's setting up a foreseeable breach, which damages trust and the relationship far more than a well-reasoned, achievable counter-proposal would. This is precisely the same principle as your "false precision" scenario from the last sub-topic, just applied to a formal contractual commitment rather than an informal accuracy estimate — the stakes of getting this wrong are, if anything, higher here.

**Why the others are wrong**

- **A)** repeats the exact mistake your false-confidence scenario was built to reject, now with a formal contractual commitment instead of an informal number — agreeing to a target the team already has technical reason to doubt, with a vague plan to "figure it out later," sets up a foreseeable, predictable failure to meet a commitment the client is likely building real business plans around. "Renegotiate later once the shortfall becomes apparent" means the client experiences a broken promise first, then has to have the harder conversation the architect could have had honestly at the outset.
- **C)** makes the same overcorrection as your "refuse to discuss anything" wrong answer from the last scenario — SLAs are a normal, expected part of any commercial system engagement, and the client has a legitimate need to plan around some commitment before launch. Refusing to engage at all isn't honesty, it's unhelpfulness dressed up as caution, and it leaves the client with nothing to build their operational and business plans around.
- **D)** treats SLA-setting as purely a negotiating-leverage exercise disentangled from the technical reality — but the whole point of this sub-topic is that a *good* SLA negotiation is grounded in what the system can actually be expected to do, communicated honestly; treating it as "whichever option leverage allows" ignores that committing to numbers the system genuinely can't meet has real consequences regardless of how much negotiating power secured that commitment in the first place.

**Pattern to remember, opening this feedback-loop/expectation-alignment arc**: SLA-setting isn't something to be agreed to under pressure or refused outright — it's a collaborative translation exercise, the same skill from your trade-off-communication scenarios, applied specifically to formal commitments: take the real technical picture (which parts of the system are fast, which are inherently slower and why; what reliability is realistic now vs. later) and turn it into a structured, negotiable proposal the stakeholder can actually plan around, rather than either an unrealistic promise that sets up a breach or a non-answer that leaves them with nothing.

Want another scenario on this sub-topic — maybe one testing what a *genuine* feedback loop looks like versus one that exists only in name — or move to sub-topic 4 (documentation and implementation guidance)?