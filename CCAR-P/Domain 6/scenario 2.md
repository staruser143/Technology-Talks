**Scenario**

An architect is engaged to build a Claude-powered internal tool for a mid-sized manufacturing company's quality-inspection process. During discovery, she interviews three stakeholders separately:

- The **VP of Operations** wants the tool to move fast — she describes a vision where the system autonomously flags and routes defective units without waiting for human sign-off, since inspection delays are costing the company money on every shift.
- The **Head of Compliance** describes strict regulatory requirements (the company supplies parts to the automotive industry, subject to specific traceability and human-sign-off standards) — every defect classification must have a named human reviewer of record before a unit can be scrapped or reworked, no exceptions.
- The **floor supervisor** who'll actually use the tool day-to-day says her biggest pain point isn't speed at all — it's that current defect-classification categories are inconsistent between shifts, causing rework disputes, and she'd value consistency far more than raw speed.

The architect notices these three views aren't fully compatible — full autonomy (VP's vision) directly conflicts with mandatory human sign-off (Compliance's requirement), and neither addresses the floor supervisor's actual top concern (classification consistency).

**Question**: What should the architect do with this information before proceeding to design?


**Question**: What should the architect do with this information before proceeding to design?

A) Proceed with the VP's vision, since she has the most senior title and holds budget authority — her request should take precedence over the other two stakeholders' input.

B) Surface the conflict explicitly to the relevant stakeholders (particularly the VP and Compliance, whose visions are directly incompatible) before design begins, rather than silently picking one interpretation or attempting to design around the tension unstated. This should include naming the specific trade-off (full autonomy vs. mandatory human sign-off cannot both be true simultaneously given the compliance requirement) and incorporating the floor supervisor's classification-consistency need, which none of the higher-level stakeholders had surfaced but which materially affects whether the tool will actually be adopted and trusted by its daily users. Resolving stakeholder conflicts and gaps is part of discovery itself, not something to be quietly worked around during design.

C) Design a system that technically satisfies all three requirements simultaneously by building full autonomy but labeling it with a human's name after the fact for compliance purposes, since this technically produces "a named human reviewer of record" without slowing down the process.

D) Ignore the floor supervisor's input entirely, since she's not a decision-making stakeholder and her operational concerns are outside the scope of architectural discovery, which should focus only on requirements from stakeholders with budget or regulatory authority.


Correct — and this scenario extends the discovery lesson from "surface the real problem behind a stated solution" to a second, equally important discovery skill: **surfacing and reconciling conflicts across multiple stakeholders**, rather than letting a hidden tension get carried silently into design, where it's far more expensive to untangle.

**Why B is right**

The architect's discovery process did its job correctly up to this point — she talked to three separate stakeholders rather than just the most senior one, and as a result she uncovered something none of them individually stated: their visions are **directly, structurally incompatible**. The VP wants full autonomy; Compliance requires mandatory human sign-off with no exceptions given the automotive-industry regulatory context. These aren't two preferences that can be blended or split the difference on — "full autonomy" and "mandatory human sign-off, no exceptions" are logically exclusive as stated. If the architect proceeds to design without resolving this, she's not deferring a decision, she's guessing which stakeholder's vision to secretly prioritize — and whichever way she guesses, the other stakeholder will discover the conflict later, likely after real design or implementation work has already been invested in the wrong direction.

The correct move, exactly as B states, is making the conflict **explicit and visible to the people who can actually resolve it** — this isn't the architect's call to make unilaterally (she doesn't have the authority to decide the company's regulatory posture, and picking wrong could mean building something the company legally can't deploy), but it *is* her responsibility to surface it clearly rather than let it stay hidden. This is the direct analog to your Domain 4 A/B testing lesson about not bundling incompatible things together and hoping it works out — here, the "incompatible things" are stakeholder requirements rather than test variants, but the discipline is the same: name the tension explicitly, don't paper over it.

B also correctly folds in the floor supervisor's input — not because her title carries formal authority, but because her stated pain point (classification inconsistency, not speed) reveals a real requirement neither the VP nor Compliance mentioned, and it's directly relevant to whether the eventual tool will actually work well in practice. A system built purely to satisfy the VP's speed goal and Compliance's sign-off requirement, while ignoring the floor supervisor's consistency problem, might launch technically compliant and still fail in practice if the people using it daily don't trust its categorizations. Good discovery surfaces requirements from the people who'll actually live with the system day to day, not just from those with the most seniority.

**Why the others are wrong**

- **A)** resolves the conflict by seniority alone, without regard to the fact that Compliance's requirement is described as a strict regulatory constraint ("no exceptions"), not merely a competing preference. Ranking stakeholder input purely by title ignores that some requirements — particularly regulatory ones — aren't just another voice in the room to be weighed against a VP's operational preference; they may be a hard constraint the system legally cannot violate, regardless of who outranks whom internally.
- **C)** proposes a fix that satisfies the *letter* of Compliance's stated requirement while completely defeating its actual purpose — attaching a human's name to a decision the human never actually reviewed isn't meaningful human sign-off, it's a fabricated compliance record. This is arguably worse than either honestly picking one stakeholder's vision or surfacing the conflict, since it creates the appearance of regulatory compliance without the substance, which is a serious governance and legal risk in its own right (a falsified record of human review, in a regulated automotive-supply context, is a significant problem beyond just "not really solving the conflict").
- **D)** dismisses a stakeholder's input based purely on formal authority, missing the actual point of talking to the floor supervisor in the first place — discovery that only consults people with budget or regulatory authority systematically misses operational reality, which is exactly the kind of gap that leads to a technically-compliant, formally-approved system nobody on the floor actually trusts or uses well.

**Pattern to remember, extending the discovery arc**: good discovery doesn't stop at understanding one stakeholder's need in isolation — when multiple stakeholders are involved, discovery includes actively checking whether their stated requirements are actually *compatible* with each other, and surfacing any real conflicts explicitly to the people positioned to resolve them, rather than silently picking a side or designing something that only appears to satisfy everyone. This is especially critical when one of the conflicting requirements is a hard regulatory constraint rather than a negotiable preference — those need to be identified and respected as boundaries on the solution space, not treated as just one more input to balance against the rest.

Want another discovery scenario, or move to sub-topic 2 (communicating architectural decisions and trade-offs)?