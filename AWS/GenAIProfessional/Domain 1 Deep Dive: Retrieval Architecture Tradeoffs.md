# Retrieval Architecture

Many candidates understand what RAG is, but the Professional exam increasingly tests whether you know why retrieval quality succeeds or fails.

In real-world GenAI systems:

```
User Experience Quality

≈ 20% Model
≈ 80% Retrieval Architecture
```

A great model with poor retrieval performs badly.

A moderate model with excellent retrieval often performs exceptionally well.

# Retrieval Architecture Pipeline

Mentally visualize every RAG system as:
```
Documents
    ↓
Chunking
    ↓
Embedding
    ↓
Vector Store
    ↓
Retrieval
    ↓
Re-ranking
    ↓
Context Construction
    ↓
LLM
```

Most exam questions target one of these layers.

# Layer 1: Chunking Strategy

This is one of the most important topics.

## What is Chunking?

Split large documents into smaller units.

Example:
```
100-page HR Manual

↓

Chunk 1
Chunk 2
Chunk 3
```
...

Why Not Store Whole Documents?

Imagine:
```
500-page policy document
```

User asks:
```
"How many vacation days do contractors receive?"
```

If retrieval returns all 500 pages:
```
Large context
High cost
Low relevance
```

Bad retrieval.

# Chunk Size Tradeoff
## Scenario A: Large Chunks
```
Chunk Size = 5000 tokens
```

Advantages:

✅ More context

✅ Better completeness

Disadvantages:

❌ Lower retrieval precision

❌ Higher token cost

❌ More irrelevant content

## Scenario B: Small Chunks

```
Chunk Size = 200 tokens
```

Advantages:

✅ High precision

✅ Lower cost

Disadvantages:

❌ Context fragmentation

❌ Missing relationships

# AWS Exam Style Question

Users complain:

```
Answers are incomplete.
```

Investigation:
```
Chunks are very small.
```

Most likely issue?

✅ Chunk fragmentation.

# Chunk Overlap

Example:
```
Chunk A
Paragraphs 1-5

Chunk B
Paragraphs 4-8
```

Overlap preserves continuity.

Without overlap:

```
Information gets split
across chunk boundaries.
```

# Professional Insight


Typically:
```
Too Small
  →
Low Recall

Too Large
  →
Low Precision
```


# Scenario

The chatbot misses important information located across adjacent sections of documents.

Best fix?

A. Larger FM

B. Fine-Tuning

C. Increase chunk overlap

D. Agent

✅ Answer: C


# Layer 2: Metadata Filtering

This is heavily used in enterprise systems.

Suppose:
```
1 million HR documents
```

Each document has metadata:
```json
{
  "department": "Finance",
  "country": "India",
  "classification": "Internal"
}
```

Instead of searching all documents:

Retrieve:
```
department = Finance
country = India
```
first.

Then run vector search.

# Why It Matters

Benefits:

✅ Better relevance

✅ Lower latency

✅ Improved security

✅ Reduced hallucinations

# Professional Scenario

HR assistant retrieves salary data from another department.

Most likely missing?

A. Larger embedding model

B. Fine-Tuning

C. Metadata-based authorization

D. Prompt engineering

✅ C

# Layer 3: Semantic Search vs Hybrid Search

This is very important.

## Semantic Search

Uses embeddings.

Query:

```
vehicle insurance
```

Can retrieve:
```
automobile coverage
```

because meanings are similar.

Advantage:

✅ Meaning-aware

Disadvantage:

❌ Misses exact keyword importance

## Keyword Search

Searches exact terms.

Query:
```
Product-XR900
```

Needs exact match.

Advantage:

✅ Precise identifiers

Disadvantage:

❌ Doesn't understand meaning

## Hybrid Search

Combines both.

```
Vector Search
+
Keyword Search
```

## Exam Pattern

Company stores:

```
Product IDs
SKU numbers
Legal codes
```

Best retrieval pattern?

✅ Hybrid Search

## Architect Rule

Use Hybrid Search when data contains:

```
IDs
Product Names
Regulations
Error Codes
Part Numbers
```

# Layer 4: Re-ranking

This is increasingly important.

Imagine retrieval returns:

```
Top 20 chunks
```

Many are only partially relevant.

Re-ranking adds another step:

```
Retrieve Top 20
      ↓
Score Relevance
      ↓
Return Top 5
```



Benefits:

✅ Better precision

✅ Better answer quality

✅ Less context pollution

## Professional Scenario

Current retrieval:

``
Top 20 documents returned
``

Answer quality inconsistent.

Retrieval recall appears acceptable.

Likely improvement?

A. Larger model

B. Re-ranking

C. Fine-Tuning

D. Temperature tuning

✅ B

# The Retrieval Quality Formula

Think:
```
Recall
=
Did I find the right information?

Precision
=
Did I return mostly relevant information?
```

## Exam Trick

Poor retrieval can come from:

```
Low Recall
```

or
```
Low Precision
```

Different problems.

Different fixes.

# Layer 5: Query Transformation

Users ask bad questions.

Example:
```
"What's the leave policy?"
```

Retrieval system rewrites:

```
"Employee annual vacation leave policy"
```

before searching.

Benefits:

✅ Better retrieval quality

✅ Better recall

✅ Better context finding

## Scenario

Users enter vague questions.

Relevant documents exist.

Retrieval still fails.

Best improvement?

✅ Query Rewriting / Query Expansion

Not necessarily a larger model.

# Layer 6: Parent-Child Retrieval

This is frequently used in enterprise RAG.


Problem:
```
Small chunks retrieve well.

Large chunks provide context.
```

Need both.

Solution:

```
Parent Document
       ↓
Small Child Chunks
       ↓
Retrieve Child
       ↓
Return Parent Context
```

Example:
```
300-page manual
```

Retrieve:
```
Relevant paragraph
```

Return:
```
Entire section
```

Benefits:

✅ Precision

✅ Context preservation

# Layer 7: Multi-Hop Retrieval

Professional-level topic.

Question:
```
Which customers bought products
that were recalled due to a defect
reported in Europe?
```

May require:

```
Retrieve Fact A
      ↓
Retrieve Fact B
      ↓
Combine
```

Single retrieval often fails.

Need:
```
Multi-step retrieval
```

Sometimes implemented using agents.

# Scenario

A research assistant must:
```
Search papers
Compare results
Discover relationships
```

Best enhancement?

✅ Multi-hop retrieval

or

✅ Agent-assisted retrieval

# Layer 8: Context Compression

Another emerging pattern.

Retrieved:
```
50 pages
```

Model only needs:
```
6 paragraphs
```

Compress first.

Then send to LLM.

Benefits:

✅ Reduced token cost

✅ Lower latency

✅ Better focus

# The Professional-Level Retrieval Troubleshooting Matrix

This is worth memorizing.

| Symptom                            | Likely Root Cause        |
| ---------------------------------- | ------------------------ |
| Missing facts                      | Low recall               |
| Irrelevant answers                 | Low precision            |
| Answer incomplete                  | Chunk size too small     |
| Context polluted                   | Chunk size too large     |
| Security leakage                   | Missing metadata filters |
| Product IDs not found              | Need hybrid search       |
| Retrieval returns too much content | Need re-ranking          |
| Vague queries fail                 | Query transformation     |
| Facts spread across documents      | Multi-hop retrieval      |
| Good retrieval but expensive       | Context compression      |

## Professional Exam Challenge
Consider this scenario:

A global manufacturer has:

```
20 million documents
```

Users search:
```
Product IDs
Part Numbers
Maintenance Manuals
Technical Procedures
```

Requirements:

- High accuracy
- Exact part number matching
- Semantic understanding
- Source citations
- Fast retrieval


What architecture would I choose?
```
Hybrid Search
      +
Metadata Filters
      +
Re-ranking
      +
RAG
```


Why?

- Product IDs → Keyword Search
- Manuals → Semantic Search
- Accuracy → Re-ranking
- Citations → RAG grounding
- Scale → Strong vector store architecture

This type of multi-layer retrieval design is exactly the level of thinking that starts appearing in tougher AWS Generative AI Professional questions.

