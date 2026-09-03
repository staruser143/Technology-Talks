# GenAI Concepts

Instead of memorizing AWS services, let's master the underlying GenAI architecture concepts first, then map them to AWS services. This will help both in the exam and in real-world architecture decisions.

---

## Domain 1 — Foundation Model Integration, Data Management, and Compliance (31%)

This is the largest domain in the exam (31%) and arguably the most important. AWS defines the following tasks in Domain 1:

- Analyze requirements and design GenAI solutions
- Select and configure Foundation Models (FMs)
- Implement data validation and processing pipelines
- Design and implement vector store solutions
- Design retrieval mechanisms for FM augmentation (RAG)

### How I suggest we learn it

Don't study Domain 1 as AWS services. Study it as a single flow that maps a business problem to a production GenAI solution:
```
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
```
This flow covers about 70–80% of Domain 1 exam patterns.

---

## Module 1 — Analyze Requirements and Design GenAI Solutions

This is where many candidates lose marks.

The exam often gives:
```
  A company wants to do X.

  Which architecture should be chosen?

```



The first question is NEVER:

```
Which AWS service?
```

The first question is:
```
What type of AI problem is this?
```

# Problem Type Classification:

- ## Type 1 — Content Generation
  - Examples:
  -  marketing content,
  -  email creation,
  -  product descriptions,
  -  summaries

Architecture:
    ```
     Prompt
       ↓
    FM
       ↓
    Generated content
    ```
  - NO RAG not required

- ## Type 2 — Enterprise Knowledge Search
  Examples:
  - HR chatbot,
  - policy assistant,
  - compliance assistant,
  - research assistant

Architecture:
    ```
    User query
       ↓
    Embedding
       ↓
    Vector search
       ↓
    Relevant documents
      ↓
    Prompt
      ↓
    LLM
    ```
RAG required

- ## Type 3 — Structured Decision Support
Examples:
  -  insurance underwriting,
  -  loan processing,
  -  claims processing

Architecture:
  ```
    LLM
     +
    Business rules
     +
    Databases
  ```
  Note: Pure LLM is insufficient

- ## Type 4 — Agentic Systems
Examples:
- travel booking,
-  IT support automation,
-  multi-step workflows
   Architecture:
  ```
    Reasoning
        ↓
    Tool selection
        ↓
    Execute tool
        ↓
    Observe
        ↓
    Plan next action
  
```
This is where agents are useful

## **Exam Trap #1**

Question:
```
Company wants answers grounded in internal documents.
```

Options:
- Prompt engineering
- larger model
- fine-tuning
- RAG
```
Correct answer:
 ✅ RAG

Reason:
 - The issue is knowledge access, not model intelligence.
 - This is one of the most common exam patterns

---

## Module 2 — Foundation Model Selection

### When to use larger models:
- Complex reasoning
- Planning
- Agent workflows
- Long context understanding

Examples:
- Claude Sonnet
- Claude Opus

### When to use smaller models:
- Lower cost
- Lower latency
- High request volume

Examples:
- Simple Classification
- Extraction
- Routing


### *Architect Rule*

Always ask tradeoff questions:

| Requirement | Priority |
| ----------- | -------- |
| Accuracy    | ?        |
| Cost        | ?        |
| Latency     | ?        |
| Throughput  | ?        |

Exam questions usually force a tradeoff.

## *Typical exam scenario*:
 "A chatbot serves 5 million requests/day."

Wrong instinct:
```
Use biggest model.
```
Correct instinct is:
✅ use the smallest model that meets requirements (cost optimization matters).

AWS consistently emphasizes cost optimization.

---

## Module 3
###  Embeddings Mastery

### What is an embedding?
- An embedding converts content into numeric vectors

Example
```
 "Car Insurance"
      ↓
 [0.12, 0.91, 0.77, ...])
```

The vector captures semantic meaning.

## Why Do We Need Embeddings?
Without Embeddings:
```
Keyword Search
```
With Embeddings:
```
Semantic Searc
```
Example:

Query:
```
Vehicle Coverage
```
Can Still Retrieve:
```
Automobile Insurance
```
because meanings are similar.

# Embedding exam rule
Embeddings are used for:
- ✅ Retrieval
- ✅ Similarity search
- ✅ Recommendation

Not used for:
- ❌ Text generation
- ❌ Reasoning
- ❌ Planning

---

## Module 4
###  Vector Stores

### Why not use a relational DB for semantic search?
Suppose:
```
Query:
"Coverage for electric vehicles"
```
Need:
```
Meaning-based search
```

SQL databases are not optimized for nearest-neighbor/vector searches. Use vector databases instead.

# Core concepts:
## Chunk:
 Split documents into sections (e.g., 100-page policy → 500 chunks)
```
100-page policy ↓ 500 chunks
```
## Embedding:
Convert each chunk to a vector


## Index
Used to quickly find nearest vectors.

Examples:

```
HNSW
IVF
```

Learn conceptually.

No need to learn algorithm internals.

## **Exam Trap #2**
Question:
```
Retrieval quality is poor.
```
Possible causes:
- ✅ Chunking issue
- ✅ Embedding issue
- ✅ Retrieval configuration

Not necessarily:
 ❌ Need a larger LLM

---

## Module 5
###  Retrieval-Augmented Generation (RAG)

### Why RAG exists:
Without RAG:
```
LLM Knowledge = Training Cutoff
```
Problems:
- Hallucinations
- Stale information
- Enterprise data unavailable

With RAG:
```
User Query
  ↓ 
Retrieve Documents
  ↓ 
Inject Context
  ↓
Generate Answer
```

# When to use RAG:
| Situation                | RAG |
| ------------------------ | --- |
| Internal documents       | ✅   |
| Frequently changing data | ✅   |
| Regulatory documents     | ✅   |
| Company policies         | ✅   |
| General knowledge only   | ❌   |


## RAG vs Fine-tuning:
| RAG               | Fine-Tuning       |
| ----------------- | ----------------- |
| Adds knowledge    | Changes behavior  |
| Dynamic           | Static            |
| Cheap             | Expensive         |
| Real-time updates | Retraining needed |
| Documents         | Patterns          |


## Easy exam rule:
Need a model to know new facts?
 ✅ RAG

Need a model to respond differently?
 ✅ Fine-tuning

---

## Module 6
###  Data Management
Domain 1 also focuses on data pipelines.

Understand:
### Data Ingestion
```
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
```


Data quality aspects often tested:
- Deduplication
- Data validation
- Metadata enrichment
- PII detection
- Content filtering

Poor quality data → poor retrieval results.

---

## Module 7
###  Compliance and Governance
Architects tend to perform well here.

Think:
### Questions AWS Wants You To Ask
- Is there PII?
- Is there PHI?
- Is data encrypted?
- Is access controlled?
- Is retrieval scoped?
- Is data residency respected?

## Typical exam pattern:
Question:
```
Organization wants to build RAG on sensitive HR records.
```
Best answer likely includes:

✅ Access control
✅ Encryption
✅ Data classification
✅ Auditability

Not simply:
✅ Stronger model
---

## Domain 1 Mental Model (Summary)
Memorize this:
```
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
```


1. Understand business problem
2. Choose GenAI pattern (Prompting, RAG, Agent, Fine-tuning)
3. Select model (cost, quality, latency)
4. Prepare data (validate, clean, chunk)
5. Generate embeddings
6. Store in vector DB
7. Retrieve context
8. Build prompt
9. Generate response
10. Ensure compliance

---

## Architect-Level Exam Insight
If I had to rank Domain 1 topics by exam importance:


| Topic              | Importance |
| ------------------ | ---------- |
| RAG vs Fine-Tuning | ⭐⭐⭐⭐⭐      |
| FM Selection       | ⭐⭐⭐⭐⭐      |
| Embeddings         | ⭐⭐⭐⭐⭐      |
| Vector Stores      | ⭐⭐⭐⭐⭐      |
| Chunking Strategy  | ⭐⭐⭐⭐       |
| Data Pipelines     | ⭐⭐⭐⭐       |
| Compliance         | ⭐⭐⭐⭐       |
| Agentic Patterns   | ⭐⭐⭐        |


---
