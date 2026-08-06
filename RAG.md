# RAG (Retrieval-Augmented Generation) patterns

RAG (Retrieval-Augmented Generation) patterns define how retrieval from external knowledge sources is combined with LLM generation to improve accuracy, grounding, and relevance. This document is an architect-level reference that groups key RAG patterns from basic → advanced → production-grade, with recommended use-cases and characteristics.

---

## 1. Basic RAG Patterns

### 1.1 Simple (Naïve) RAG

Flow:

```
User Query → Embed → Vector Search → Top-K Docs → LLM → Response
```

Characteristics:

- Single-shot retrieval
- No iteration
- Stateless

Use cases:

- FAQ bots
- Internal document search

Pros / Cons:

- ✅ Simple, fast
- ❌ Prone to hallucination if retrieval is weak

---

### 1.2 Query-Augmented RAG (Prompt Injection of Context)

Description: retrieved chunks are injected directly into the prompt.

Pattern:

```
Prompt = { Query + Retrieved Context }
```

Enhancements:

- Add a guard instruction like: "Answer ONLY from the provided context."

Pros / Cons:

- ✅ Improves grounding
- ❌ Limited reasoning

---

## 2. Retrieval Optimization Patterns

### 2.1 Multi-Query RAG

Idea: generate multiple reformulated queries to improve recall.

Flow:

```
User Query → LLM → {Q1, Q2, Q3} → Retrieve per query → Merge results → LLM
```

- ✅ Better recall
- ✅ Handles ambiguous queries


### 2.2 Hybrid Search RAG

Combine semantic and lexical search:

- Vector search (semantic)
- BM25 / keyword search (lexical)

When to use:

- Exact matches (IDs, codes)
- Semantic meaning

### 2.3 Reranking Pattern

Flow:

```
Retrieve Top-N (e.g., 50) → Reranker model → Select Top-K (e.g., 5) → LLM
```

- ✅ Improves precision
- ✅ Critical for production

### 2.4 Context Compression RAG

Reduce irrelevant text before sending to the LLM using:

- Extractive summarization
- Relevance filtering

- ✅ Reduces token cost
- ✅ Improves signal quality

---

## 3. Iterative & Reasoning RAG Patterns

### 3.1 Iterative (Self-Refinement) RAG

Flow:

```
Query → Retrieve → Answer → Evaluate → Re-retrieve → Refine
```

- ✅ Improves answer quality
- ❌ Higher latency

### 3.2 Self-Ask / Decomposition RAG

Break complex queries into sub-questions and answer/compose them.

Example:

- "What is X and how does it compare to Y?"
  - Q1: What is X?
  - Q2: What is Y?
  - Q3: Compare

- ✅ Handles complex queries

### 3.3 Chain-of-Thought + Retrieval

Retrieve → reason step-by-step (use when you need better logical accuracy).

- ✅ Better logical accuracy

---

## 4. Agentic RAG Patterns (relevant for LangGraph & Agentic AI)

### 4.1 Tool-Calling / Agent RAG

Description: the LLM (or an agent) decides when and what to retrieve. If knowledge is insufficient, the agent calls the retriever tool.

- ✅ Dynamic
- ✅ Reduces unnecessary retrieval

### 4.2 Multi-Agent RAG

Specialized agents with responsibilities (example mapping):

- Retrieval Agent — fetch context
- Reasoning Agent — generate the answer
- Critic / Reviewer — validate
- Planner — orchestrate

- ✅ Production-grade pattern
- ✅ Aligns well with LangGraph

### 4.3 Supervisor Pattern (LangGraph-style)

A central controller manages the workflow:

```
User → Supervisor
        ├── Retrieval Node
        ├── Reasoning Node
        └── Validation Node
```

- ✅ Deterministic control
- ✅ Enterprise-friendly

---

## 5. Knowledge Structuring Patterns

### 5.1 Graph RAG

Use knowledge graphs instead of flat text chunks when relationships matter (e.g., healthcare, fraud detection).

- ✅ Handles relationships

### 5.2 Structured RAG (SQL / APIs)

Retrieve directly from structured sources like SQL, APIs, data lakes for high-accuracy answers.

- ✅ Accurate for structured data

### 5.3 Metadata Filtering RAG

Filter before retrieval, e.g.:

```sql
WHERE department = 'claims'
  AND year = 2025
```

- ✅ Improves relevance
- ✅ Critical in enterprise

---

## 6. Advanced Production Patterns

### 6.1 Streaming RAG

Retrieve while generating to reduce latency and improve UX.

- ✅ Lower latency
- ✅ Better UX

### 6.2 Cached RAG

Cache embeddings, query results, and responses to optimize cost.

- ✅ Cost optimization

### 6.3 Feedback / Learning RAG

Use user feedback to:

- Retrain the retriever
- Improve ranking

- ✅ Continuous improvement

### 6.4 Guardrailed RAG

Add security and quality gates:

- Input filters (prompt injection mitigation)
- Output validation (sanity checks, compliance)

- ✅ Secure RAG systems

---

## 7. Enterprise RAG Composite Pattern

A real-world production system typically combines many patterns:

- Hybrid Search + Reranking + Metadata Filtering + Multi-Query + Agent Orchestration + Guardrails


## Architect Cheat Sheet (Decision Mapping)

| Problem / Signal       | Recommended Pattern      |
|------------------------|--------------------------|
| Low recall             | Multi-query              |
| Low precision          | Reranking                |
| High cost              | Compression              |
| Complex reasoning      | Decomposition            |
| Dynamic workflows      | Agentic RAG              |
| Structured data needs  | SQL / API RAG            |
| Compliance / security  | Guardrailed RAG         |

---


### Recommended mental model

Think of RAG patterns across three axes:

1) Retrieval Quality
- Multi-query, Hybrid search, Reranking

2) Reasoning Depth
- Decomposition, Iteration, Chain-of-thought

3) Orchestration
- Agent-based, Graph-based, Multi-agent

---
