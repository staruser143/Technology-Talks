**Scenario**

A retail company's VP of Customer Experience approaches an architect with a specific request: "We need you to build us an AI chatbot for our website — customers keep complaining they can't find answers, and I want something like ChatGPT that can talk to them." She's already secured budget and wants to move straight to implementation planning.

Two possible ways the architect could proceed:

**Approach A**: Accept the request as scoped — begin technical planning for a customer-facing chatbot immediately, since the stakeholder has clearly articulated what she wants and has budget approval, and further questioning might seem like second-guessing a decision-maker who's already done her homework.

**Approach B**: Before any technical planning, ask structured discovery questions — what specifically are customers unable to find, what's the current process (do they email support, use search, contact a hotline), what would "success" look like beyond "having a chatbot," what's the actual failure being experienced (missing information, hard-to-navigate site, slow support response), and only then determine what solution actually fits.

**Question**: Which approach reflects sound discovery practice, and why?



**Question**: Which approach reflects sound discovery practice, and why?

A) Approach A — since the stakeholder has budget authority and has clearly articulated a specific request, proceeding directly to implementation planning respects her decision-making authority and avoids unnecessary delay; questioning a well-defined request risks appearing obstructive.

B) Approach B — a stated solution ("build us a chatbot") is not the same as a validated problem statement, and good discovery practice means surfacing the actual underlying need before committing to an implementation. "Customers can't find answers" could stem from several different root causes (poor site search, missing content, an overwhelmed support team, a confusing navigation structure) that call for different solutions — a chatbot might be the right answer, or might be solving the wrong layer of the problem entirely, or might need to be paired with fixing an underlying content/search gap to actually work well. Asking these questions isn't second-guessing the stakeholder's authority to decide — it's ensuring the eventual decision, whoever makes it, is grounded in the actual problem rather than the first proposed solution.

C) Both approaches are equally valid, since the stakeholder's explicit request should always take precedence over discovery process regardless of context, as she is the one accountable for the outcome.

D) Neither approach is appropriate; the architect should independently decide what to build without consulting the stakeholder further at any point in the process, since technical architecture decisions should be made by technical experts alone.




Correct — and this scenario opens the domain with the foundational discovery instinct: a stakeholder's stated solution is data about what they think they need, not a validated specification of the actual problem, and conflating the two is the most common way discovery goes wrong before any technical work even begins.

**Why B is right**

Notice exactly what the VP provided: a proposed *solution* ("an AI chatbot... like ChatGPT") and a *symptom* ("customers complain they can't find answers"), but no actual diagnosis of *why* customers can't find answers. That gap matters enormously, because several genuinely different underlying problems could produce the identical symptom:

- If the real issue is **poor site search or missing content**, a chatbot layered on top of the same underlying content gaps might just become a more expensive, conversational way to fail to find the same missing answers — the chatbot would need to be paired with (or might be entirely secondary to) fixing the content/search problem itself.
- If the real issue is **an overwhelmed support team** unable to keep up with volume, a chatbot handling common questions might be exactly right — but the success metric would be about deflection/resolution rate, not just "having a chatbot."
- If the real issue is **confusing navigation**, the actual fix might be information architecture, with a chatbot serving as, at best, a partial workaround rather than addressing the root cause.

Each of these has a different correct architecture, a different success metric, and potentially a different (or additional) non-AI fix entirely. Approach B's questions — what specifically customers can't find, what the current process looks like, what success means beyond "having a chatbot" — are exactly what's needed to distinguish between these possibilities *before* committing engineering effort to build the wrong thing well. This directly echoes the very first lesson of Domain 1: "translate business problems into Claude-based AI solutions" presumes you've actually identified the business problem, not just inherited the first solution someone proposed for it.

Crucially, B's framing gets the *tone* of this right too: asking these questions isn't overriding the stakeholder's authority or implying she's wrong — it's professional diligence that serves her own stated goal (actually solving the customer-experience problem) better than immediately building what she happened to name first. A good discovery conversation would likely still end with her decision-making authority fully intact; it just ensures that decision is made with a clearer picture of the actual problem.

**Why the others are wrong**

- **A)** conflates "the stakeholder has authority to decide" with "the stakeholder's first-stated solution should be accepted without examination" — these are different things. Budget approval and clear articulation of a *desired solution* say nothing about whether that solution is actually the right fit for the underlying problem; a stakeholder can be fully empowered to make the final call while still benefiting from a more complete picture before making it. Treating discovery questions as inherently obstructive misunderstands their purpose — they're not a challenge to her authority, they're the information needed to use that authority well.
- **C)** avoids making an actual judgment by declaring both approaches equally valid regardless of context — but the material and the scenario are specifically testing whether you recognize that skipping discovery risks building a technically well-executed solution to the wrong problem, a real and costly failure mode distinct from technical execution quality.
- **D)** overcorrects in the opposite direction, excluding the stakeholder from the process entirely — but discovery is inherently collaborative; the whole point is asking the stakeholder better questions, not sidelining her and deciding unilaterally. This misunderstands discovery as "the architect knows best independently" rather than "the architect helps surface information the stakeholder herself may not have thought to volunteer."

**Pattern to remember, opening the discovery arc**: a stakeholder's request almost always arrives as a proposed solution bundled with an underlying problem — good discovery practice means separating the two, understanding the actual problem and its root cause before evaluating whether the proposed solution (or some other approach) is genuinely the right fit, rather than treating the first-named solution as the requirement itself.

Want another discovery scenario, or move to sub-topic 2 (communicating architectural decisions and trade-offs)?