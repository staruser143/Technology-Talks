I have reviewed the **"Minimally Qualified Candidate Profile"** and the **"Detailed objectives by domain" (Domains 1–7)** from the Exam Guide PDF, which explicitly and implicitly embed these core architectural principles into every exam objective.

Here is your `$learn` breakdown for the **6 Master Principles**. These are the foundational lenses through which every correct exam answer is filtered.

---

### **The 6 Master Principles**

**P1: Fix the failing component, not a proxy**
*   **What it is:** Diagnosing and resolving the root cause of a system failure at its actual source, rather than applying a workaround to a downstream symptom.
*   **Why it matters:** It prevents wasted engineering effort and ensures sustainable system reliability.
*   **How it works:** If a RAG system is hallucinating answers after a document update, you investigate and fix the retrieval/indexing step (e.g., broken embeddings or stale chunks), rather than blindly tweaking the LLM's temperature or swapping to a larger model. 
*   **What breaks without it:** **Distractor Class 3 (Wrong-Layer Diagnosis)**. You waste time and money "fixing" the model, while the actual broken component (the data pipeline) continues to degrade **accuracy** and **safety**. [Domain 4]

**P2: Least privilege: remove, don't just guard**
*   **What it is:** Eliminating unnecessary capabilities, tools, or data access from an agent’s configuration entirely, rather than relying on prompts or policies to restrict them.
*   **Why it matters:** It is the only true way to eliminate an attack surface and prevent unauthorized actions.
*   **How it works:** If a customer support agent only needs to read tickets and draft replies, you completely remove the `process_refund` and `delete_account` tools from its API configuration. 
*   **What breaks without it:** **Distractor Class 1 (Guard-Instead-of-Remove)**. Adding a confirmation prompt or audit logging leaves the capability intact, risking catastrophic **safety** and **compliance** failures if the model is jailbroken or hallucinates. [Domain 3] & [Domain 5]

**P3: Structural optimization beats blunt instruments**
*   **What it is:** Solving performance, cost, or accuracy problems by intelligently restructuring the architecture, rather than applying brute-force changes.
*   **Why it matters:** It preserves system capabilities while optimizing resource consumption.
*   **How it works:** To reduce the cost and latency of an 8,000-token system prompt, you place the static content at the beginning of the payload and enable **prompt caching**, preserving 100% of the context. 
*   **What breaks without it:** **Distractor Class 2 (Blunt-Instrument Optimization)**. Blindly truncating the prompt to 1,000 tokens (destroying **accuracy** and **safety**) or downgrading to the smallest model (destroying reasoning capability) just to meet a **cost/latency** SLA. [Domain 2]

**P4: Proportionate & business-value-aligned**
*   **What it is:** Selecting models, patterns, and infrastructure that precisely match the risk profile, volume, and value of the specific business use case.
*   **Why it matters:** It prevents overspending on low-value tasks and under-investing in high-risk tasks.
*   **How it works:** Routing high-volume, low-stakes user queries (e.g., password reset guidance) to Claude 3.5 Haiku for optimal **cost/latency**, while reserving Claude 3.5 Sonnet for complex, high-stakes contract analysis where **accuracy** is paramount.
*   **What breaks without it:** **Distractor Class 5 (Over-Engineering)** or **Class 6 (Capability Bloat)**. Using a complex, multi-agent orchestration framework for a simple FAQ bot, blowing up **cost** and **latency** without delivering proportional business value. [Domain 1]

**P5: Governance & evaluation by design**
*   **What it is:** Baking safety, compliance, and human oversight directly into the system's workflow architecture, rather than treating them as post-deployment afterthoughts.
*   **Why it matters:** Regulatory and ethical requirements cannot be reliably enforced by probabilistic models alone.
*   **How it works:** In a financial KYC system, if a deterministic sanctions check returns a partial name match, the architecture hard-routes the workflow to a Human-in-the-Loop (HITL) queue for manual review, preventing the LLM from making the final adverse decision.
*   **What breaks without it:** **Distractor Class 4 (Detective-for-Preventive)**. Relying on post-hoc audit logs to catch compliance violations after they happen, rather than preventing them architecturally, leading to severe **safety** and regulatory **SLA** failures. [Domain 5]

**P6: Evidence over intuition (metrics, eval datasets, logs)**
*   **What it is:** Using structured, automated, and repeatable testing frameworks to measure system performance, rather than relying on manual, ad-hoc checks.
*   **Why it matters:** It provides objective proof of system quality and catches silent regressions before they reach production.
*   **How it works:** Before deploying a new prompt template, you run it against a curated "Golden Dataset" of 100 diverse scenarios (including edge cases and adversarial inputs) using an automated evaluation script to measure exact match, semantic similarity, and tool-call accuracy.
*   **What breaks without it:** **Distractor Class 7 (Vibes-Based Evaluation)**. Deploying a change because it "felt" better during a quick manual playground test, only to have **accuracy** and **safety** plummet in production on untested edge cases. [Domain 4]

---

### **Trade-off Impact Summary**
Applying these principles consistently requires **higher upfront engineering effort** (e.g., building eval datasets, designing HITL gates, structuring cacheable prompts). However, this investment directly protects and optimizes **cost, latency, accuracy, safety, and SLA compliance** at scale, which is the defining trait of the "Minimally Qualified Candidate" (MQC).
