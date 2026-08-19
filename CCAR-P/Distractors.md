I have reviewed the **Sample Questions and Rationales (Section 8)** and the **Detailed Objectives by Domain (Section 6)** from the Exam Guide PDF to ground these classes in the official exam logic. 

Here is your `$learn` breakdown for the **8 Distractor Classes**. Recognizing these is your secret weapon for eliminating wrong answers and securing that 900+/1000 score.

---

### **The 8 Distractor Classes**

**1. Guard-Instead-of-Remove**
*   **What it is:** Proposing a compensating control (like a prompt warning, confirmation step, or audit log) instead of eliminating an unnecessary capability entirely.
*   **Violates:** P2 (Least privilege: remove, don't just guard)
*   **Concrete Example:** A support agent only needs to read tickets. The distractor suggests "adding a confirmation prompt before the agent can delete accounts" instead of removing the `delete_account` tool from the configuration entirely (as seen in Sample Q1).
*   **Impact:** Destroys **safety** and **compliance**, as probabilistic models can be jailbroken or hallucinate past prompt-based guards. [Domain 3] & [Domain 5]

**2. Blunt-Instrument Optimization**
*   **What it is:** Solving a cost, latency, or performance problem by crudely chopping off capabilities or context, rather than intelligently restructuring the architecture.
*   **Violates:** P3 (Structural optimization beats blunt instruments)
*   **Concrete Example:** To fix high latency, the distractor suggests "truncating the 8,000-token policy document to the first 1,000 tokens" instead of using prompt caching (as seen in Sample Q2). 
*   **Impact:** Drastically reduces **accuracy** and **safety** by stripping the model of necessary context, creating a false sense of **cost/latency** savings. [Domain 2]

**3. Wrong-Layer Diagnosis**
*   **What it is:** Blaming the wrong component of the AI stack for a failure, leading to ineffective "fixes."
*   **Violates:** P1 (Fix the failing component, not a proxy)
*   **Concrete Example:** A RAG system starts hallucinating after a document refresh. The distractor suggests "lowering the model's temperature" or "upgrading to a larger model," when the actual root cause is the retrieval layer feeding stale or irrelevant chunks (as seen in Sample Q3).
*   **Impact:** Wastes **cost** and engineering time while the actual **accuracy** and **SLA** issues remain completely unresolved. [Domain 4]

**4. Detective-for-Preventive**
*   **What it is:** Relying on post-hoc monitoring or logging to catch a failure after it has already occurred, rather than architecting the system to prevent the failure in the first place.
*   **Violates:** P5 (Governance & evaluation by design)
*   **Concrete Example:** For a high-risk financial approval system, the distractor suggests "implementing comprehensive logging of all agent decisions for monthly compliance audits" instead of hardcoding a Human-in-the-Loop (HITL) gate for adverse decisions.
*   **Impact:** Catastrophic **safety** and regulatory **SLA** failures, as the damage is already done before the "detective" control catches it. [Domain 5]

**5. Over-Engineering**
*   **What it is:** Proposing a highly complex, heavy architectural pattern for a simple, well-defined problem that could be solved with a lightweight, deterministic approach.
*   **Violates:** P4 (Proportionate & business-value-aligned)
*   **Concrete Example:** Using a complex, multi-agent swarm with web-search and self-reflection loops to answer a simple, static FAQ question that could be handled by a single, cached RAG lookup.
*   **Impact:** Unnecessarily inflates **cost** and **latency**, while introducing new points of failure that degrade overall system **reliability**. [Domain 1] & [Domain 3]

**6. Capability Bloat**
*   **What it is:** Granting an agent or system access to tools, data, or scopes that it does not strictly need for its defined task, "just in case."
*   **Violates:** P2 (Least privilege) & P4 (Proportionate)
*   **Concrete Example:** Giving a read-only customer service agent `write` access to the user database so it "has the flexibility to update addresses if the user asks."
*   **Impact:** Expands the attack surface, directly threatening **safety**, **security**, and **compliance** SLAs. [Domain 3]

**7. Vibes-Based Evaluation**
*   **What it is:** Relying on ad-hoc, manual, or intuitive checks ("it feels better in the playground") rather than structured, repeatable, metric-driven testing.
*   **Violates:** P6 (Evidence over intuition)
*   **Concrete Example:** Deploying a new prompt template because the developer manually tested three queries and liked the outputs, without running it against a curated "Golden Dataset" of edge cases and adversarial inputs.
*   **Impact:** Leads to silent **accuracy** and **safety** regressions in production that are only discovered after real users are impacted. [Domain 4]

**8. Compliance-as-Afterthought**
*   **What it is:** Designing the entire technical architecture first and only engaging security, legal, or compliance stakeholders at the very end of the lifecycle.
*   **Violates:** P5 (Governance by design)
*   **Concrete Example:** Building a full RAG pipeline ingesting PII, and only asking the security team for a GDPR review the week before the planned production launch.
*   **Impact:** Forces massive, expensive architectural rework late in the lifecycle, blowing **cost** and **SLA** timelines, or worse, shipping a non-compliant system. [Domain 5] & [Domain 6]

---

### **Why This Matters for the Exam**
The CCAR-P exam does not just test if you know *what* a technology is; it tests if you can spot *flawed architectural reasoning*. Often, the correct answer is the one that requires slightly more upfront engineering effort (e.g., building a HITL gate, structuring a cacheable prompt) to protect long-term **cost, latency, accuracy, safety, and SLA** metrics. The distractors will always offer the "easy, lazy, or reactive" way out.

[All Domains]

Does this make sense before we look at trade-offs?