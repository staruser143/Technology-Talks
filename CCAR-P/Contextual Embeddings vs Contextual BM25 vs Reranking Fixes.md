# Contextual Embeddings vs Contextual BM25 vs Reranking Fixes

This is a very important CCAR-P topic because many candidates memorize the numbers (35% → 49% → 67%) but don't actually understand what each layer is fixing.

Think of these as three different opportunities to improve retrieval:

- Contextual Embeddings fix the vector representation.
- Contextual BM25 fix the keyword/lexical representation.
- Reranking fixes mistakes after retrieval by reordering results.

Anthropic describes Contextual Retrieval as combining Contextual Embeddings and Contextual BM25, with additional gains available from reranking. Anthropic reports roughly 35% retrieval-failure redu[...] 

## The problem they're solving

Imagine you have an HR handbook.

Original document:

> Employees may take up to 12 weeks of parental leave following the birth or adoption of a child.

After chunking:

**Chunk 847:**

> "...up to 12 weeks of parental leave following the birth or adoption..."

Notice what's missing?

The chunk no longer says:

- This is an HR policy
- It belongs to employee benefits
- It is a parental leave section

The chunk has lost context.

Anthropic's insight is that many retrieval failures occur because chunking removes important context.

---

## 1. Contextual Embeddings

### What it does

Before generating the embedding, Claude creates additional context:

**Document:** Employee Handbook

**Chunk:**

> "...up to 12 weeks of parental leave following birth or adoption..."

**Generated context:**

> "This section describes employee parental leave benefits and eligibility."

The embedding is created from the combination of:

- Context: This section describes employee parental leave benefits and eligibility.
- Chunk: ...up to 12 weeks of parental leave...

not from the chunk alone.

### Why it helps

Suppose a user asks:

> What maternity benefits do employees receive?

Without contextual embeddings, the chunk "12 weeks of parental leave" may not semantically match "maternity benefits" well enough.

With contextual embeddings, the phrase "employee parental leave benefits" is embedded into the vector representation and the semantic similarity becomes much stronger.

### Mental model

Think: Contextual Embeddings improve what the vector "means." It helps the embedding model understand: What is this chunk about?

---

## 2. Contextual BM25

### What it does

BM25 is keyword search.

Traditional BM25 indexes the literal tokens found in the chunk, for example:

- 12
- weeks
- parental
- leave
- birth
- adoption

But it does not contain terms that don't exist in the chunk, such as:

- benefits
- HR
- maternity
- employee policy

Anthropic adds the generated context to the lexical index as well. So BM25 indexes additional terms such as:

- employee
- benefits
- parental leave
- HR policy
- eligibility

alongside the original chunk text.

### Example

**Search query:**

> employee benefits after having a child

- Traditional BM25: ❌ May miss the chunk.
- Contextual BM25: ✅ Finds it because the added context contains matching keywords.

### Mental model

Think: Contextual BM25 improves keyword matching. Embeddings help meanings. BM25 helps words.

---

## Why Anthropic combines both

Vector retrieval and keyword retrieval fail differently.

**Vector search strength**: good at semantic similarity (e.g., car = automobile).

**Vector search weakness**: can miss exact identifiers (e.g., API-4627, Policy-9.4.2, Product SKU ABC123) because exact identifiers matter.

**BM25 strength**: excellent at matching exact words (e.g., SKU-12345, ErrorCode-567, Invoice-2025-04).

**BM25 weakness**: poor at semantic matching (e.g., automobile vs car may not match well).

Therefore:

Vector Search + BM25 = Hybrid Retrieval

Anthropic's Contextual Retrieval enhances both sides.

---

## 3. Reranking

This happens AFTER retrieval.

Assume retrieval returns:

1. Travel Expense Policy
2. Parental Leave Benefits
3. Work From Home Policy
4. Medical Insurance
5. Child Care Benefits

**User asked:** How much parental leave is available?

The correct document exists but it is ranked #2.

### What a reranker does

A stronger model evaluates the query + each candidate chunk and determines which chunk best answers the query, then reorders the results, for example:

1. Parental Leave Benefits
2. Child Care Benefits
3. Medical Insurance
4. Travel Expense Policy
5. Work From Home Policy

Anthropic found that adding reranking on top of Contextual Retrieval produced the largest improvement, reducing retrieval failures by about 67%.

---

## Simple analogy

Imagine finding a book in a library.

- Contextual Embeddings: Improves the meaning of the catalog entry.
  - Book: Chapter 8
  - Becomes: Cooking Book · Chapter 8 · Desserts · Chocolate Cake Recipe

- Contextual BM25: Adds extra keywords to the catalog (desserts, cake, chocolate, baking, recipe).

- Reranking: A librarian reviews search results and moves the best match to the top.

---

## CCAR-P exam shortcut

When you see:

- "Add context before embedding" → ✅ Contextual Embeddings (fixes semantic retrieval)
- "Update lexical index / BM25 with context" → ✅ Contextual BM25 (fixes keyword retrieval)
- "Reorder retrieved candidates" → ✅ Reranking (fixes ranking quality)

A useful exam heuristic is:

Embeddings improve representation → BM25 improves retrieval coverage → Reranking improves ordering.

That's often enough to identify the correct answer even when the numeric percentages are not mentioned.
