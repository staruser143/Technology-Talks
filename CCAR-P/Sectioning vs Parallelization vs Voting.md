This is one of the most important workflow distinctions for CCAR-P. Many candidates memorize definitions but struggle to recognize them in scenarios.

A simple way to think about it:

Sectioning = We know how to split the work before we start
Orchestrator-Workers = We don't know how to split the work until we see the request
Voting = We ask multiple workers the same question and compare answers
Scenario 1: Analyzing a 500-page Annual Report
Sectioning ✅
Workflow
500-page report

Page 1-125   -> Worker 1
Page 126-250 -> Worker 2
Page 251-375 -> Worker 3
Page 376-500 -> Worker 4

Combine summaries

Why?

Before execution, you already know how to split the work.

The decomposition is fixed and predictable.

Mental Model

"Divide and conquer."

Orchestrator-Workers ❌

Usually overkill.

You don't need an AI to decide:

"Should I analyze page 1-125?"

You already know the subtasks.

Voting ❌

Not the goal.

You're splitting the document, not asking multiple opinions on the same content.

Scenario 2: Complex Customer Complaint Investigation

Complaint:

"Your company overcharged me, exposed my data, and cancelled my account."

Orchestrator-Workers ✅
Workflow
Complaint
    ↓
Orchestrator
    ↓
Determines needed experts

Billing Worker
Security Worker
Compliance Worker
Customer Service Worker

Combine findings

Why?

The required investigations depend on the complaint.

Some complaints need only billing.

Others need:

Security
Legal
Fraud
Compliance

You don't know upfront.

This is exactly the type of use case Anthropic recommends for orchestrator-workers.

Sectioning ❌

There may not be natural sections.

The problem isn't:

Part 1
Part 2
Part 3


The problem is discovering what analyses are needed.

Voting ❌

You aren't looking for three opinions on one answer.

You're looking for specialized investigations.

Scenario 3: Legal Contract Review

Contract contains:

Payment Terms
Liability
Termination
Data Privacy
IP Rights

Sectioning ✅
Workflow
Payment Terms -> Worker 1
Liability -> Worker 2
Privacy -> Worker 3
IP Rights -> Worker 4

Why?

The sections already exist.

You know them before execution.

Orchestrator-Workers 🤔

Possible, but unnecessary.

Remember Anthropic's principle:

Use the simplest workflow that works.

Since the decomposition is obvious, sectioning is simpler.

Scenario 4: Root Cause Analysis of a Production Outage

Incident:

Application down
Revenue impacted
Cause unknown

Orchestrator-Workers ✅
Workflow
Incident
   ↓
Orchestrator

Creates:
- Infrastructure analysis
- Application analysis
- Network analysis
- Database analysis

Workers execute

Why?

Every outage is different.

Possible causes:

DNS
Kafka
Database
IAM
Network
Code deployment

You don't know beforehand.

Perfect orchestrator-workers scenario.

Sectioning ❌

No predefined work breakdown exists.

Scenario 5: Evaluating a Difficult Math Problem

Question:

Solve a difficult optimization problem.

Voting ✅
Workflow
Problem
   ↓

Worker A solves
Worker B solves
Worker C solves

Compare answers
Choose consensus

Why?

The objective is to reduce reasoning errors.

Multiple independent attempts often outperform a single answer.

Anthropic refers to this as parallelization through voting.

Sectioning ❌

There aren't natural sections.

Orchestrator-Workers ❌

You don't need task planning.

You need multiple attempts at the same task.

Scenario 6: Resume Screening

100 resumes.

Sectioning ✅
Resume 1-25  -> Worker 1
Resume 26-50 -> Worker 2
Resume 51-75 -> Worker 3
Resume 76-100-> Worker 4


Known workload division.

Scenario 7: Merger & Acquisition Due Diligence

Company data contains:

Financial statements
Cybersecurity reports
Regulatory risks
HR risks
Pending lawsuits
Orchestrator-Workers ✅
Workflow
Target company
      ↓
Orchestrator

Decides:
- Finance analysis
- Security review
- Legal review
- HR review

Workers execute


Some acquisitions may need:

Only Finance + Legal


Others may need:

Finance + Security + Legal + HR + Tax


Subtasks emerge from the input.

Scenario 8: Factual Question Answering for High Accuracy

Question:

"What are the risks of adopting technology X?"

Voting ✅
Question
   ↓

Analyst A answer
Analyst B answer
Analyst C answer

Consensus synthesis


Goal:

Reduce hallucinations
Improve confidence
Get multiple perspectives
Quick Recognition Cheat Sheet
Sectioning

Ask:

"Could I draw the task breakdown before seeing the input?"

Examples:

✅ Chapters
 ✅ Pages
 ✅ Contract sections
 ✅ Resume batches
 ✅ Log partitions

Orchestrator-Workers

Ask:

"Do I need an LLM to figure out what analyses should exist?"

Examples:

✅ Incident investigation
 ✅ Due diligence
 ✅ Research projects
 ✅ Customer complaints
 ✅ Business strategy reviews

Voting

Ask:

"Do multiple workers work on the SAME task?"

Examples:

✅ Mathematical reasoning
 ✅ Fact checking
 ✅ Risk assessment
 ✅ Judge/evaluator patterns
 ✅ Confidence improvement

Ultimate CCAR-P Memory Trick

Imagine a CEO.

Sectioning

CEO says:

You do Finance.
You do Marketing.
You do HR.


Work is predefined.

Orchestrator-Workers

CEO says:

Let me first understand the problem.

Then I'll decide which teams should be involved.


Dynamic decomposition.

Voting

CEO says:

Three consultants,
give me your independent recommendation.

Then I'll choose the best one.


Multiple answers to the same question.

If you can answer these two questions, you'll identify the pattern correctly almost every time:

Are the subtasks known before execution?

Yes → Sectioning

Are multiple workers solving the same problem?

Yes → Voting

Otherwise, is an LLM deciding which subtasks should exist?

Yes → Orchestrator-Workers.
