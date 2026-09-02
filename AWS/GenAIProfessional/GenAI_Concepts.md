Domain 1
Foundation Model Integration, Data Management, and Compliance (31%)

This is the largest domain in the exam (31%) and arguably the most important. AWS defines the following tasks in Domain 1:

Analyze requirements and design GenAI solutions
Select and configure Foundation Models (FMs)
Implement data validation and processing pipelines
Design and implement vector store solutions
Design retrieval mechanisms for FM augmentation (RAG)
How I Suggest We Learn It

Don't study Domain 1 as AWS services.

Study it as:

Business Problem
      ↓
Model Selection
      ↓
Data Preparation
      ↓
Embedding
      ↓
Vector Store
      ↓
Retrieval
      ↓
Prompt Construction
      ↓
Model Invocation
      ↓
Governance & Compliance


This single flow covers about 70-80% of Domain 1.

Module 1
Analyze Requirements and Design GenAI Solutions

This is where many candidates lose marks.

The exam often gives:

A company wants to do X.

Which architecture should be chosen?

The first question is NEVER:

Which AWS service?

The first question is:

What type of AI problem is this?

Problem Type Classification
Type 1: Content Generation

Examples:

Marketing content
Email creation
Product descriptions
Summaries

Architecture:

Prompt
   ↓
FM
   ↓
Generated Content


No RAG needed.

Type 2: Enterprise Knowledge Search

Examples:

HR chatbot
Policy assistant
Compliance assistant
Research assistant

Architecture:

User Query
      ↓
Embedding
      ↓
Vector Search
      ↓
Relevant Documents
      ↓
Prompt
      ↓
LLM


RAG required.

Type 3: Structured Decision Support

Examples:

Insurance underwriting
Loan processing
Claims processing

Architecture:

LLM
+
Business Rules
+
Databases


Pure LLM is insufficient.

Type 4: Agentic Systems

Examples:

Travel booking
IT support automation
Multi-step workflows

Architecture:

Reasoning
    ↓
Tool Selection
    ↓
Execute Tool
    ↓
Observe
    ↓
Plan Next Action


This is where Agents come in.

Exam Trap #1

Question:

Company wants answers grounded in internal documents.

Options:

Prompt Engineering
Larger Model
Fine-Tuning
RAG

Correct Answer:

✅ RAG

Reason:

The issue is knowledge access, not model intelligence.

This is one of the most common exam patterns.

Module 2
Foundation Model Selection

AWS loves testing this.

When To Use Larger Models

Use when you need:

Complex reasoning
Planning
Agent workflows
Long context understanding

Examples:

Claude Sonnet
Claude Opus
When To Use Smaller Models

Use when you need:

Lower cost
Lower latency
High request volume

Examples:

Simple classification
Extraction
Routing
Architect Rule

Always ask:

Requirement	PriorityAccuracy	?
Cost	?
Latency	?
Throughput	?

Exam questions usually force a tradeoff.

Typical Scenario

"A chatbot serves 5 million requests/day."

Wrong instinct:

Use biggest model.

Correct instinct:

✅ Smallest model meeting requirements.

AWS consistently emphasizes cost optimization.

Module 3
Embeddings Mastery

This topic appears everywhere.

What Is An Embedding?

An embedding converts content into vectors.

Example:

"Car Insurance"

↓

[0.12, 0.91, 0.77, ...]


The vector captures semantic meaning.

Why Do We Need Embeddings?

Without embeddings:

Keyword Search


With embeddings:

Semantic Search


Example:

Query:

Vehicle Coverage

Can still retrieve:

Automobile Insurance

because meanings are similar.

Embedding Exam Rule

Embeddings are used for:

✅ Retrieval

✅ Similarity Search

✅ Recommendation

Not used for:

❌ Text Generation

❌ Reasoning

❌ Planning

Module 4
Vector Stores

Another very important area.

Why Not Use Relational DB?

Suppose:

Query:
"Coverage for electric vehicles"


Need:

Meaning-based search


SQL databases are not optimized for nearest-neighbor vector searches.

Vector databases are.

Core Concepts
Chunk

Document split into sections.

Example:

100-page policy
↓
500 chunks

Embedding

Each chunk converted to vector.

Index

Used to quickly find nearest vectors.

Examples:

HNSW
IVF

Learn conceptually.

No need to learn algorithm internals.

Exam Trap #2

Question:

Retrieval quality is poor.

Possible causes:

✅ Chunking issue

✅ Embedding issue

✅ Retrieval configuration

Not necessarily:

❌ Need larger LLM

Module 5
Retrieval Augmented Generation (RAG)

This is the heart of Domain 1.

AWS explicitly lists retrieval mechanisms and RAG-related architectures as key exam content.

Why RAG Exists

Without RAG:

LLM Knowledge
= Training Cutoff


Problems:

Hallucinations
Stale information
Enterprise data unavailable

With RAG:

User Query
      ↓
Retrieve Documents
      ↓
Inject Context
      ↓
Generate Answer

When To Use RAG
Situation	RAGInternal documents	✅
Frequently changing data	✅
Regulatory documents	✅
Company policies	✅
General knowledge only	❌
RAG vs Fine-Tuning

One of the most tested concepts.

RAG	Fine-TuningAdds knowledge	Changes behavior
Dynamic	Static
Cheap	Expensive
Real-time updates	Retraining needed
Documents	Patterns
Easy Exam Rule

Need model to know new facts?

✅ RAG

Need model to respond differently?

✅ Fine-Tuning

Module 6
Data Management

Domain 1 also focuses on data pipelines.

Understand:

Data Ingestion
Files
Databases
APIs
Streams


↓

Processing

↓

Vectorization

↓

Storage

Data Quality

Common exam themes:

Deduplication
Data validation
Metadata enrichment
PII detection
Content filtering

Poor quality data produces poor retrieval.

Module 7
Compliance and Governance

Architects tend to perform well here.

Think:

Questions AWS Wants You To Ask
Is there PII?
Is there PHI?
Is data encrypted?
Is access controlled?
Is retrieval scoped?
Is data residency respected?
Typical Exam Pattern

Question:

Organization wants to build RAG on sensitive HR records.

Best answer likely includes:

✅ Access control

✅ Encryption

✅ Data classification

✅ Auditability

Not simply:

✅ Stronger model

Domain 1 Mental Model

Memorize this:

1. Understand Business Problem

2. Choose GenAI Pattern
   - Prompting
   - RAG
   - Agent
   - Fine-Tuning

3. Select Model
   - Cost
   - Quality
   - Latency

4. Prepare Data
   - Validate
   - Clean
   - Chunk

5. Generate Embeddings

6. Store in Vector DB

7. Retrieve Context

8. Build Prompt

9. Generate Response

10. Ensure Compliance

Architect-Level Exam Insight

If I had to rank Domain 1 topics by exam importance:

Topic	ImportanceRAG vs Fine-Tuning	⭐⭐⭐⭐⭐
FM Selection	⭐⭐⭐⭐⭐
Embeddings	⭐⭐⭐⭐⭐
Vector Stores	⭐⭐⭐⭐⭐
Chunking Strategy	⭐⭐⭐⭐
Data Pipelines	⭐⭐⭐⭐
Compliance	⭐⭐⭐⭐
Agentic Patterns	⭐⭐⭐

