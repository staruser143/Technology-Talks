## Domain 6: Stakeholder Communication & Lifecycle Management (14%)

This domain shifts register from the technical mechanics of Domains 1-5 to the *process and communication* skills around delivering a Claude-based system — arguably where "architect" as a role (not just a technical builder) gets tested most directly. Five objectives:

### 1. Conduct structured discovery and requirement gathering

Before any architecture decision, the exam wants you to recognize good discovery practice: extracting the *actual* business problem and constraints from stakeholders, not just their first proposed solution. A classic trap: a stakeholder says "we need a chatbot," but the actual underlying need might be better served by a workflow, an internal tool, or isn't an AI problem at all — discovery is about surfacing the real requirement (business goal, constraints, success criteria, stakes) before jumping to Domain 1's pattern-selection work. This connects directly to your very first Domain 1 material: "translate business problems into Claude-based AI solutions" starts with actually understanding the problem, not the requested implementation.

### 2. Communicate architectural decisions and trade-offs

This is about *explaining* the reasoning you've built all session — why workflow vs. agentic, why this model tier, why this level of human oversight — to stakeholders who may not have the technical background to evaluate the decision on its technical merits alone. The exam-relevant skill: translating a technical trade-off (cost vs. latency vs. accuracy, autonomy vs. control) into terms a business stakeholder can actually weigh in on, and being transparent about trade-offs rather than presenting a decision as having no downsides.

### 3. Manage stakeholder feedback loops and expectation alignment (including SLAs)

Distinct from the *technical* feedback loops in Domain 1 — this is about managing what stakeholders *expect* the system to do, setting realistic SLAs collaboratively (not just being handed one and forced to hit it, or overpromising to win buy-in), and creating a real channel for stakeholder feedback to actually inform iteration, not just a one-way status update.

### 4. Document architectures and provide implementation guidance

Producing documentation that lets other people actually understand, maintain, and extend a system you've designed — architecture diagrams, decision rationale (why this choice, not just what the choice is), and guidance sufficient for a team to implement or hand off the work.

### 5. Support lifecycle phases (discovery, design, handoff, monitoring, iteration)

The architect's role doesn't end at launch — this ties directly back to Domain 4's "evaluation doesn't stop at launch" lesson and Domain 5's ongoing governance material, framed here as the *lifecycle* an architect is responsible for shepherding: discovery → design → handoff (to an implementation/ops team, if different from the design team) → monitoring → iteration, and back around as the system evolves.

---

This domain likely tests judgment about *communication quality and process*, more than technical mechanism — so scenarios will probably look different from what we've done: less "diagnose the bug," more "was this the right way to handle a stakeholder interaction, and why." Want to move into scenario practice now?


