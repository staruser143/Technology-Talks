**Scenario**

A health insurance company operating in both the US and EU deploys a Claude-powered "prior authorization" assistant that reviews requests from doctors to approve or deny insurance coverage for proposed treatments. The system has been in production for a year. A comprehensive external review surfaces the following:

- **Automation scope**: For treatment requests below a certain complexity threshold, the system fully approves or denies coverage automatically with no human involved and no path for a denied patient to request human reconsideration of their specific case.

- **Data access**: To make its determination, the assistant is given access to a patient's entire lifetime medical record across all conditions and providers, even though the specific prior-authorization decision at hand only concerns one narrow, current treatment request.

- **Guardrail behavior**: The system calls an external clinical-criteria verification service to confirm a requested treatment meets medical necessity guidelines before approving. When that service is unreachable (which happened during a recent multi-hour outage), the assistant's fallback logic defaults to auto-approving the request rather than holding it for review.

- **Outcome auditing**: The only metric tracked and reported is overall decision accuracy against historical claims-review outcomes (94%). No one has ever broken this down by patient demographic group, and a fact-finding request from a patient advocacy group results in the company discovering, for the first time, that denial rates are noticeably higher for patients from certain lower-income zip codes, driven by care-facility and provider-network patterns correlated with income and, indirectly, race — even though neither income nor race is an explicit input to the system.

- **Disclosure**: Nothing in the patient-facing coverage decision letter or the doctor-facing portal discloses that the determination was made by an AI system rather than a human claims reviewer; both are styled and worded to appear as standard insurer correspondence.

**Question**: Which of the following best captures the full set of distinct governance/safety issues present, correctly mapped to their sub-topics?

A) There is one root problem — the system should never have been deployed without a human reviewing every single case — and requiring full pre-action human approval for all requests would resolve every issue described.

B) Five distinct, independently-diagnosable issues: (1) HITL/compliance — fully automated coverage decisions with significant effects on patients, with no path to request human review, likely runs afoul of GDPR's restrictions on automated decision-making (for EU patients) and is poor HITL design regardless of jurisdiction, given the stakes involved; (2) HIPAA minimum necessary — granting access to a patient's entire lifetime medical record for a decision concerning one narrow current request violates minimum-necessary principles, the same scope-creep pattern seen in the discharge-summary scenario; (3) fail-open guardrail design — defaulting to auto-approval when the clinical-verification service is unreachable defeats the guardrail's purpose exactly during the window it's most needed, the same pattern as the refund-verification scenario; (4) bias/fairness — overall accuracy was the only tracked metric, masking a real disparity correlated with income and indirectly race, driven by proxy variables (facility/provider-network patterns) rather than explicit demographic inputs, the same proxy-discrimination pattern as the lending scenario; (5) transparency — neither patients nor doctors are told a decision was AI-made, removing their ability to apply appropriately calibrated scrutiny, the same issue as the telehealth chat scenario. Each requires its own targeted fix rather than one blanket structural change.

C) The only serious issue is the fail-open guardrail behavior, since it's a concrete technical bug with a clear root cause; the other four are softer, harder-to-quantify concerns that can reasonably be deprioritized.

D) All five issues stem from using Claude for this task at all; the company should abandon AI-assisted prior authorization entirely and revert to a fully manual process, since no combination of fixes could adequately address a system with this many simultaneous problems.



Correct — and this is a fitting capstone for Domain 5, pulling together every sub-topic you've built across guardrails, risk identification, HITL, compliance, and ethical AI into one compound scenario, exactly mirroring the mixed-review treatment you gave Domains 3 and 4.

**Why B is right — confirming each mapping**

1. **HITL/compliance (automated decisions, no recourse)**: this is your GDPR automated-decision-making scenario, transplanted directly — a fully automated, no-appeal coverage decision is exactly the kind of significant-effect decision that requires a path to human intervention, both as a matter of sound HITL design (the stakes here — access to medical treatment — are at least as high as the loan-denial scenario that established this principle) and as a specific EU regulatory requirement for the company's EU patients. High aggregate accuracy (94%) doesn't satisfy this any more than it did in the credit-scoring scenario — the requirement protects the specific denied patient's right to contest, not the system's average performance.

2. **HIPAA minimum necessary**: this is your discharge-summary scenario's exact mechanism — a narrow task (evaluate one specific treatment request) paired with grossly over-broad data access (the patient's entire lifetime medical history across all conditions and providers). The same "broader access now, justified by hypothetical future convenience" reasoning pattern applies, and the same fix applies: scope access to what the specific determination actually requires.

3. **Fail-open guardrail**: this is your refund-verification scenario precisely — a safety check (clinical-criteria verification) that, when unreachable, defaults to granting the exact outcome it exists to gate (approval) rather than holding for review. During the outage, the system functioned as if the guardrail didn't exist at all, for the entire window it was most needed. The fix is identical: fail closed for consequential actions — hold or escalate when verification can't complete, never auto-approve by default.

4. **Bias/fairness via proxy variables**: this is your lending scenario's proxy-discrimination mechanism, applied to healthcare — no explicit demographic input, but facility/provider-network patterns correlated with income (and indirectly race) drive a real disparity that only surfaced because a patient advocacy group forced the question; the company's own internal metrics (overall accuracy only) never would have caught it, exactly the aggregate-metric blind spot from your hiring and fraud-detection scenarios.

5. **Transparency**: this is your telehealth scenario's exact issue — neither patients nor doctors are told an AI made the determination, styled instead as standard human correspondence, removing anyone's ability to apply appropriately calibrated scrutiny to a decision that materially affects access to care.

**Why the others are wrong**

- **A)** reaches for the single blanket fix you've correctly rejected in every mixed-review scenario across this entire session — full pre-action human approval for everything doesn't address the HIPAA over-access problem, doesn't fix the fail-open guardrail's behavior *during* an outage (a human reviewer still needs the verification service or an alternative to make an informed decision), doesn't add demographic-outcome monitoring, and doesn't add AI disclosure. It also reintroduces exactly the volume-mismatch problem from your approval-fatigue scenario if applied uniformly regardless of case complexity.
- **C)** arbitrarily elevates the most "technical-sounding" issue while dismissing four problems that are, if anything, more consequential — a documented racial/income disparity in healthcare coverage decisions and a complete absence of patient recourse for automated denials are not "softer" concerns just because they're harder to reduce to a single bug ticket; this repeats the same unjustified-prioritization mistake rejected in every prior compound scenario this session.
- **D)** overcorrects into full abandonment without evidence that the five identified problems are unfixable — every one of them has a specific, targeted, previously-validated fix (scope data access, fail closed, add subgroup monitoring, add an appeal path, add disclosure); none of them are inherent, irreducible properties of using AI for this task, they're specific, addressable design choices this particular implementation got wrong.

**The meta-lesson closing Domain 5**: exactly as in your Domain 3 and Domain 4 mixed reviews, a real production system can accumulate multiple, genuinely independent governance failures simultaneously — a compliance gap, a data-scope violation, a guardrail design flaw, a fairness blind spot, and a transparency failure can all coexist in one system without any of them causing the others. Professional-level governance review means enumerating and separately diagnosing all of them, each against its own sub-topic and its own targeted fix, rather than collapsing everything into one dominant narrative or one sweeping structural response.

That's a comprehensive, well-integrated close to Domain 5. Want to move to Domain 6 (Stakeholder Communication & Lifecycle Management, 14%) now?

