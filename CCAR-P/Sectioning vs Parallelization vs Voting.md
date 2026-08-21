# Sectioning vs Parallelization (Voting) vs Orchestrator-Workers

This is one of the most important workflow distinctions for CCAR-P. Many candidates memorize definitions but struggle to recognize them in scenarios. This document gives a quick mental model, examples, and a cheat-sheet to identify the right pattern.

## Core definitions

- **Sectioning** — We know how to split the work before we start.
- **Orchestrator-Workers** — We don't know how to split the work until we see the request (an LLM/orchestrator decides what analyses are needed).
- **Voting (Parallelization through voting)** — We ask multiple workers the same question and compare answers to reduce reasoning errors.

---

## Scenarios

### Scenario 1 — Analyzing a 500-page Annual Report
- Pattern: Sectioning ✅

Workflow:
- 500-page report
- Page 1–125   → Worker 1
- Page 126–250 → Worker 2
- Page 251–375 → Worker 3
- Page 376–500 → Worker 4
- Combine summaries

Why?
- You already know how to split the work before execution.
- The decomposition is fixed and predictable.

Mental model: "Divide and conquer."

Orchestrator-Workers: ❌ Usually overkill — you don’t need an AI to decide predefined subtasks.

Voting: ❌ Not appropriate — you’re not seeking multiple independent opinions on the same content; you’re partitioning it.

---

### Scenario 2 — Complex Customer Complaint Investigation
Complaint example: “Your company overcharged me, exposed my data, and cancelled my account.”

- Pattern: Orchestrator-Workers ✅

Workflow:
- Complaint → Orchestrator → determines needed experts
  - Billing Worker
  - Security Worker
  - Compliance Worker
  - Customer Service Worker
- Combine findings

Why?
- The investigations required depend on the complaint; some complaints need only billing, others need security, legal, fraud, compliance, etc.
- You don't know the necessary subtasks upfront — an orchestrator decides them.

Sectioning: ❌ There may not be natural sections.

Voting: ❌ You want specialized analysis, not multiple opinions on the same question.

---

### Scenario 3 — Legal Contract Review
Contract contains: Payment Terms, Liability, Termination, Data Privacy, IP Rights

- Pattern: Sectioning ✅

Workflow:
- Payment Terms → Worker 1
- Liability → Worker 2
- Privacy → Worker 3
- IP Rights → Worker 4

Why?
- Sections already exist and are known before execution, so sectioning is simplest.

Orchestrator-Workers: 🤔 Possible but unnecessary — prefer the simplest workflow that works.

---

### Scenario 4 — Root Cause Analysis of a Production Outage
Incident: Application down, revenue impacted, cause unknown

- Pattern: Orchestrator-Workers ✅

Workflow:
- Incident → Orchestrator → creates tasks such as:
  - Infrastructure analysis
  - Application analysis
  - Network analysis
  - Database analysis
- Workers execute and report

Why?
- Every outage is different; causes (DNS, Kafka, DB, IAM, network, deployment) vary and aren’t known beforehand.

Sectioning: ❌ No predefined breakdown exists.

---

### Scenario 5 — Evaluating a Difficult Math Problem
Question: Solve a difficult optimization problem

- Pattern: Voting ✅

Workflow:
- Problem → multiple independent workers (A, B, C) solve it → compare answers → choose consensus

Why?
- Goal is to reduce reasoning errors; multiple independent attempts often outperform a single answer.

Sectioning: ❌ No natural sections.
Orchestrator-Workers: ❌ Task planning isn’t needed — you need multiple independent attempts.

---

### Scenario 6 — Resume Screening
100 resumes

- Pattern: Sectioning ✅

Workflow:
- Resume 1–25  → Worker 1
- Resume 26–50 → Worker 2
- Resume 51–75 → Worker 3
- Resume 76–100 → Worker 4

Why?
- Known workload division and predictable decomposition.

---

### Scenario 7 — Mergers & Acquisitions Due Diligence
Company data contains: financial statements, cybersecurity reports, regulatory risks, HR risks, pending lawsuits

- Pattern: Orchestrator-Workers ✅

Workflow:
- Target company → Orchestrator decides required analyses:
  - Finance analysis
  - Security review
  - Legal review
  - HR review
  - Tax review (if needed)

Why?
- Subtasks emerge from the input; some acquisitions need only Finance + Legal, others need Finance + Security + Legal + HR + Tax.

---

### Scenario 8 — Factual Question Answering for High Accuracy
Question: “What are the risks of adopting technology X?”

- Pattern: Voting ✅

Workflow:
- Question → Analyst A, B, C produce independent answers → consensus synthesis

Why?
- Reduce hallucinations, improve confidence, get multiple perspectives.

---

## Quick Recognition Cheat Sheet

- Sectioning: Ask — “Could I draw the task breakdown before seeing the input?”
  - Indicators: chapters, pages, contract sections, resume batches, log partitions

- Orchestrator-Workers: Ask — “Do I need an LLM to figure out what analyses should exist?”
  - Indicators: incident investigation, due diligence, research projects, customer complaints, business strategy reviews

- Voting: Ask — “Do multiple workers work on the SAME task?”
  - Indicators: mathematical reasoning, fact checking, risk assessment, judge/evaluator patterns, confidence improvement

---

## Ultimate CCAR-P Memory Trick (CEO analogy)
- Sectioning: CEO says: “You do Finance. You do Marketing. You do HR.” — work is predefined.
- Orchestrator-Workers: CEO says: “Let me first understand the problem, then I’ll decide which teams should be involved.” — dynamic decomposition.
- Voting: CEO says: “Three consultants, give me your independent recommendation. Then I’ll choose the best one.” — multiple answers to the same question.

If you can answer these two questions, you'll identify the pattern correctly almost every time:
1. Are the subtasks known before execution?
   - Yes → Sectioning
2. Are multiple workers solving the same problem?
   - Yes → Voting

Otherwise, ask: Is an LLM deciding which subtasks should exist?
- Yes → Orchestrator-Workers

---

*Formatted for clarity and quick scanning.*
