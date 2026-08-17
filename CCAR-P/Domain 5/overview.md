Let us move into Domain 5

## Domain 5: Governance, Safety & Risk Management (14%)

Five objectives here. As with Domain 4, there's real overlap with material you've already built — guardrails (Domain 2), human-in-the-loop and consequential-action gating (Domain 1's salary-change/loan-scheduling material, Domain 3's auth scenarios) — but two areas are genuinely new ground: regulatory compliance specifics and ethical AI considerations (bias, fairness, transparency).

### 1. Implement guardrails and safety controls

This is largely Domain 2's guardrails sub-topic, viewed through a governance lens rather than a prompt-engineering lens: instructional vs. structural vs. system-level enforcement, matched to actual stakes and reversibility (your salary-change and writing-assistant scenarios). Nothing fundamentally new mechanically — what's new here is framing guardrails as part of a formal *risk management* posture, not just a prompting technique.

### 2. Identify risks, limitations, and failure modes of LLM systems

This asks you to catalog and reason about categories of things that can go wrong with LLM systems specifically — hallucination, prompt injection, jailbreaking/adversarial inputs, data leakage (a system inadvertently revealing information it shouldn't), overreliance (users trusting output without appropriate scrutiny), and capability limitations (tasks the model genuinely can't do reliably). The exam-relevant skill: given a system description, identify which failure modes are actually plausible risks for *that* system, not reciting a generic list — a customer-facing chatbot's risk profile differs meaningfully from an internal code-review agent's.

### 3. Apply human-in-the-loop validation strategies

You've already built strong instincts here (salary-change scenario, loan-scheduling human-gate reasoning) — this objective formalizes *when* human review belongs in a workflow: at points of irreversible or high-stakes action, not uniformly everywhere (the "read-only lookups don't need a human gate" lesson) and not nowhere for consequential actions (the "instructions aren't enforcement" lesson). New territory here: different *modes* of human-in-the-loop — pre-action approval (human confirms before something happens), post-action audit (human reviews after, catching problems but not preventing them), and escalation-based (only routed to a human when the system itself flags uncertainty) — each with different risk/cost trade-offs.

### 4. Ensure compliance with regulations (e.g., GDPR, HIPAA, FedRAMP)

Genuinely new ground. High-level, exam-relevant awareness (not legal expertise):
- **GDPR** (EU data protection): concerns like data minimization, right to deletion/erasure, consent for data processing, and restrictions on automated decision-making with significant effects on individuals without human review.
- **HIPAA** (US healthcare data): protected health information (PHI) handling — who can access it, how it's stored/transmitted, audit logging requirements (directly connecting to your hospital attribution scenario from Domain 3).
- **FedRAMP** (US government cloud security): a security authorization framework for cloud services handling government data — relevant when a system might be deployed for government use, with strict infrastructure and data-handling requirements.
The exam angle is almost certainly scenario-based: given a system handling certain data types or serving certain users, recognize which regulatory considerations apply and what design implications follow (e.g., healthcare data → HIPAA-relevant access controls and audit logging; EU users → GDPR-relevant data handling and deletion capability).

### 5. Address ethical AI considerations (bias, fairness, transparency)

Also genuinely new ground:
- **Bias**: a system performing differently (worse) for different demographic groups — often inherited from training data or introduced through design choices (e.g., an eval set, echoing your eval-design material, that isn't representative across the populations the system actually serves).
- **Fairness**: related but distinct — ensuring the system's decisions or treatment don't systematically disadvantage particular groups, which sometimes requires deliberate measurement (bias can be invisible in an aggregate metric the same way your fraud-detection and underwriting scenarios showed minority-class failures hiding in aggregate accuracy — a direct, genuine connection to Domain 4).
- **Transparency**: whether users understand they're interacting with an AI system, and whether the system's reasoning/limitations are appropriately disclosed rather than presented as more authoritative or certain than warranted.

---

Given the real overlap with Domains 1-4, I'd suggest weighting scenario practice toward the two genuinely new areas — compliance and ethical AI/bias — with a lighter pass on guardrails and human-in-the-loop since you've already built strong instincts there. Want to proceed that way, or cover all five sub-topics evenly?

