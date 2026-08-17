**Scenario**

An architect has finished designing a Claude-powered contract-analysis tool for a client's legal team, meeting every requirement discussed during discovery. While finalizing the design, she discovers something the client never asked about: the system performs excellently on contracts written in standard English, but its accuracy drops meaningfully on contracts containing extensive Latin legal terminology and archaic drafting conventions common in certain older or more traditional deal types — a real limitation, though not something explicitly covered in the original requirements conversation, and not something the client has raised as a concern.

Three options for how to proceed at the final review meeting:

**Option A**: Present the design as fully meeting all requirements, without raising the Latin-terminology limitation, since the client never asked about this specific case and raising unprompted concerns risks introducing doubt about a design that otherwise satisfies everything discussed.

**Option B**: Wait to see if the client mentions anything related to older or Latin-heavy contracts; if they do, disclose it then; if not, it likely isn't relevant to their actual use case and doesn't need to come up.

**Option C**: Proactively disclose the limitation at the review meeting, even though it wasn't asked about — explaining what was found, how significant the accuracy drop is, whether it's likely to affect their actual contract volume, and what mitigation options exist (e.g., flagging low-confidence cases from this category for manual review) — so the client can decide with full information whether this matters for their use case.

**Question**: Which option reflects sound practice for communicating trade-offs, and why?


**Question**: Which option reflects sound practice for communicating trade-offs, and why?

A) Option A — since the client never asked about this specific scenario, it's outside the agreed scope of the engagement, and volunteering unprompted concerns could unnecessarily undermine confidence in an otherwise successful design.

B) Option B — waiting for the client to raise it themselves respects their ability to identify what matters to their own use case, without the architect presuming to guess what's relevant to them.

C) Option C — the architect discovered a real, material limitation during the course of the work; proactively surfacing it (with context on severity, actual relevance to their volume, and mitigation options) gives the client the information needed to make an informed decision about whether it matters for their specific situation, rather than leaving a known gap for them to potentially discover later, in production, without warning. This is consistent with the transparency principle from being upfront about trade-offs rather than only disclosing what's directly asked about — the client can't ask about a limitation they don't know exists, so waiting to be asked effectively means the information may never surface until it causes a real problem.

D) The architect should not mention it during the review meeting at all, but should instead quietly build in the mitigation (flagging low-confidence cases for review) without explaining why, since implementing a technical fix is sufficient and doesn't require a conversation.


Correct — and this scenario extends the "make the trade-off legible" lesson from your last scenario into a sharper, more consequential form: this time, the trade-off isn't something the stakeholder *asked* about and needs translated — it's something they don't even know to ask about, which makes proactive disclosure the only way the information reaches them at all.

**Why C is right**

The core insight, stated directly in C's reasoning, is the one worth internalizing: **a client can't ask about a limitation they don't know exists.** Option B's framing — "wait and see if they raise it" — sounds respectful of the client's judgment, but it quietly assumes the client already has enough information to know this is a relevant thing to ask about, which the scenario explicitly establishes they don't ("not something the client has raised as a concern," "not something explicitly covered in the original requirements conversation"). Passive disclosure only works when the other party already knows enough to prompt for it — here, that condition doesn't hold, so waiting effectively means the information stays hidden indefinitely, until the client happens to encounter a Latin-heavy contract in production and the system underperforms with no warning that this was a known, foreseeable gap.

This connects directly to the transparency principle from your Domain 5 telehealth scenario, applied at the architect-client relationship level instead of the AI-system-to-end-user level: withholding known, material information because it wasn't explicitly requested denies the other party the ability to make a genuinely informed decision. The client might reasonably decide this limitation doesn't matter much for their actual deal volume (if they rarely handle older or Latin-heavy contract types) — but that's *their* call to make, and they can only make it if they know the limitation exists in the first place. C preserves that decision-making authority by giving them the full picture: what was found, how severe it is, whether it's likely to actually affect their volume, and what mitigation exists — exactly the kind of complete, actionable framing that made Response C the right answer in your previous scenario too.

**Why the others are wrong**

- **A)** treats "outside the originally discussed scope" as equivalent to "not worth mentioning," but a limitation discovered during the actual work — even one nobody thought to ask about upfront — is directly relevant information about the deliverable the client is about to receive and rely on. The stated worry ("raising it might undermine confidence") prioritizes the architect's presentation over the client's ability to make an informed decision, which is precisely the trade-off transparency is meant to prevent — the momentary discomfort of disclosing a real limitation is a much smaller cost than the client discovering it unexpectedly in production, potentially on a client's own consequential contract.
- **B)** sounds more measured than A, but functionally produces the same outcome for exactly the reason C's own reasoning identifies — the client has no way to prompt for something they're unaware of, so "waiting for them to raise it" isn't really respecting their judgment, it's placing the entire burden of surfacing a known limitation on someone who doesn't have the information needed to know they should ask.
- **D)** attempts to solve the problem purely at the technical level (quietly adding a mitigation) while avoiding the conversation entirely — but this still denies the client the actual decision-making information: they'd never know a limitation existed, why the mitigation was added, or whether the mitigation fully addresses their specific risk profile. A silently-added technical fix without explanation also means the client can't evaluate whether the mitigation is sufficient for their actual use case — that judgment call is being made unilaterally and invisibly by the architect, rather than transparently, with the client's input.

**Pattern to remember, closing this trade-off communication arc**: proactive disclosure of known, material limitations — even ones outside the originally discussed scope, even ones nobody asked about — is a core part of communicating trade-offs honestly. The test isn't "did the stakeholder ask" but "does the stakeholder need this information to make a genuinely informed decision about the system they're relying on" — and if the answer is yes, waiting to be asked isn't neutral, it's a choice to withhold information the other party doesn't know they're missing.

Want another scenario here, or move to sub-topic 3 (managing stakeholder feedback loops and expectation alignment, including SLAs)?