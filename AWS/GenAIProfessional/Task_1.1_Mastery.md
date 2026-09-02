The actual exam often presents:

Business Problem → Constraints → Desired Outcome → Pick best GenAI architecture

The candidates who struggle usually jump immediately to:

Bedrock Knowledge Bases, OpenSearch, Titan Embeddings...

The candidates who score well ask:

What is the actual GenAI problem being solved?

Task 1.1 Mastery Framework

For every scenario, force yourself through this sequence:

STEP 1
What business problem exists?

STEP 2
What GenAI pattern fits?

- Prompting
- RAG
- Fine-tuning
- Agent
- Traditional ML
- Hybrid

STEP 3
What are the constraints?

- Accuracy
- Cost
- Latency
- Security
- Explainability
- Freshness

STEP 4
What architecture best satisfies constraints?

Scenario 1
Internal Policy Assistant

A company wants employees to ask questions about:

HR policies
Travel policies
Compliance procedures

Policies change every week.

What should the architect recommend?

Option A

Fine-tune a model every week

Option B

Use a larger model

Option C

RAG using enterprise documents

Option D

Prompt engineering only

Correct Answer

✅ C

Architect Reasoning

Ask:

Is the issue reasoning?
No

Is the issue model capability?
No

Is the issue access to changing information?
Yes


Therefore:

Need current documents


RAG is designed precisely for that.

Exam Mindset

Whenever you see:

Internal documents
Frequently changing information
Policies
Knowledge base
Documentation


Your brain should immediately think:

✅ RAG

Scenario 2
Insurance Claims Classification

An insurer processes millions of claims.

Need:

classify claim type
low latency
low cost

No content generation required.

What is the best solution?

Option A

Largest LLM available

Option B

Small classification model

Option C

RAG

Option D

Agent workflow

Correct Answer

✅ B

Architect Reasoning

Question trap:

Many candidates see "AI" and choose LLM.

But:

Problem = Classification


Not generation.

Traditional classification is faster and cheaper.

Important Principle

Never use a generative architecture for a predictive problem.

Scenario 3
Legal Document Assistant

Firm has:

900,000 contracts
contracts updated daily

Users ask:

What indemnification clauses exist with vendor X?

Best architecture?

Option A

Prompt Only

Option B

Fine-tune on contracts

Option C

RAG

Option D

Agent

Correct Answer

✅ C

Why?

Requirement:

Need specific information
from a dynamic document corpus


That's classic RAG.

Exam Trap

People see:

900,000 contracts


They choose fine-tuning.

Wrong.

Fine-tuning doesn't continuously inject changing knowledge.

Scenario 4
Brand Voice Consistency

Marketing team wants all generated content to:

use corporate tone
use company style guide
follow approved wording

Knowledge freshness is not important.

Option A

RAG

Option B

Fine-Tuning

Option C

Vector Search

Option D

Larger Context Window

Correct Answer

✅ B

Architect Reasoning

Question:

Need new knowledge?


No.

Question:

Need different behavior?


Yes.

Fine-tuning changes behavior.

Golden Rule
RAG -> changes knowledge

Fine-tuning -> changes behavior


If you master just this one concept, you will answer many questions correctly.

Scenario 5
Executive Research Assistant

Requirements:

gather market reports
analyze trends
visit multiple sources
compare findings
create summary
Option A

Single Prompt

Option B

RAG Only

Option C

Agent Architecture

Option D

Fine-Tuning

Correct Answer

✅ C

Why?

Need multi-step reasoning.

Find
Analyze
Compare
Summarize


Single retrieval is insufficient.

An agent can:

Reason
Retrieve
Invoke Tools
Iterate
Generate Result

Architect Pattern

When you see:

Find
Research
Compare
Plan
Decide
Investigate


Think Agents.

Scenario 6
Contact Center Assistant

Requirements:

answer customer questions
use product manuals
manuals updated weekly
response in under 2 seconds

Most important architecture?

Correct Answer

✅ RAG

The latency requirement may influence model selection.

It does NOT change the knowledge architecture.

Architect Lesson

Separate:

Knowledge Architecture


from

Model Selection


The exam loves testing this distinction.

Scenario 7
Medical Knowledge Discovery

Hospital wants doctors to retrieve latest treatment procedures.

Procedures change frequently.

High accuracy required.

Candidate Thought Process
High accuracy
→ Bigger Model

Wrong


Ask:

Where does accuracy come from?


Current knowledge.

Therefore:

✅ Retrieval quality

✅ Data quality

✅ Grounding

✅ RAG

not necessarily bigger models.

Scenario 8
Code Generation Platform

Need:

generate code
consistent coding standards
company libraries usage

Knowledge changes monthly.

What would you recommend?

Correct Answer

Usually:

✅ RAG + Fine-Tuning

Reason:

Need:

Current internal libraries


→ RAG

Need:

Consistent coding style


→ Fine-Tuning

Architect Insight

Real-world systems often use:

Fine-Tuning
+
RAG


not one or the other.

Scenario 9
Fraud Investigation Assistant

Investigators ask:

Show similar fraud cases.

Need semantic similarity.

Best architecture component?

Option A

Embedding Search

Option B

Fine-Tuning

Option C

Prompt Template

Option D

Agent

Correct Answer

✅ A

Reason:

Requirement:

Find similar items


Embeddings solve semantic similarity.

Scenario 10
CEO Wants "ChatGPT For Everything"

A company wants one solution that:

answers policy questions
books meetings
creates reports
accesses ERP
accesses CRM
performs actions

Best architecture?

Correct Answer

✅ Agentic System

Why?

This is not a knowledge problem.

This is:

Reasoning
+
Tool Usage
+
Action Execution


Agent territory.

Architect-Level Exam Trick

Many questions can be solved by identifying the missing capability.

Missing Capability	SolutionNew Knowledge	RAG
Better Behaviour	Fine-Tuning
Similarity Search	Embeddings
Multi-Step Actions	Agents
Classification	ML Model
Content Creation	LLM

Memorize this table.

Hard Scenario (Very Similar to Professional-Level Questions)

A pharmaceutical company wants:

Scientists ask questions about research papers.
Papers are updated daily.
Responses must cite sources.
Hallucinations must be minimized.
Cost should remain reasonable.

What would you choose?

A

Largest foundation model

B

Fine-tune quarterly

C

RAG with citations

D

Agent architecture

Correct Answer

✅ C

Why Architects Get This Right

Break it down:

Requirement	MeaningResearch papers	External knowledge
Updated daily	Dynamic content
Source citation	Grounding
Low hallucination	Retrieval
Cost sensitive	Avoid unnecessary retraining
Every clue points to RAG.


Homework

For each scenario in the exam, train yourself to answer these four questions first:

1. Is this a knowledge problem?

2. Is this a behavior problem?

3. Is this a retrieval problem?

4. Is this an action-execution problem?


If you can classify the problem correctly, nearly every Task 1.1 question becomes much easier.

