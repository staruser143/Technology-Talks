This is a good RAG design scenario because the corpus is not homogeneous. You have at least **three different document behaviors** and **three different query behaviors**, so a single “split everything into 500-token chunks and vector search” approach will be weak.

The right design is a **multi-index, query-routed RAG pipeline** where contracts, methodology content, and project write-ups are chunked and indexed differently.

***

# 1. Start with the Query Types

Your users ask three different kinds of questions:

| Query Type          | Example                                                  | Retrieval Need                                               |
| ------------------- | -------------------------------------------------------- | ------------------------------------------------------------ |
| Concept lookup      | “What does our methodology say about X?”                 | Find relevant methodology concepts and procedures            |
| Exact-target lookup | “Find the clause about termination in the Acme contract” | Locate a specific clause in a specific contract              |
| Broad synthesis     | “Summarize how we’ve handled engagements like this one”  | Retrieve many related past write-ups and synthesize patterns |

These are different enough that I would not use one retrieval strategy for all.

***

# 2. Recommended High-Level RAG Pipeline

Use a pipeline like this:

```text
User Query
   ↓
Query Classifier / Router
   ↓
Select Retrieval Strategy
   ↓
Retrieve from Relevant Indexes
   ↓
Optional Reranking
   ↓
Context Assembly
   ↓
LLM Generation
   ↓
Answer with Citations / Sources
```

The most important part is the **query router**.

***

## Query Router

The router decides what kind of retrieval to use.

Example routing logic:

```text
If query mentions:
  - "contract"
  - "clause"
  - "section"
  - "termination"
  - named client
  - effective date
Then route to:
  Contract clause retrieval

If query asks:
  - "methodology says"
  - "procedure"
  - "standard approach"
  - "how should we"
Then route to:
  Methodology retrieval

If query asks:
  - "similar engagements"
  - "past projects"
  - "how have we handled"
  - "summarize examples"
Then route to:
  Project write-up synthesis
```

In production, I would use a combination of:

1. Rule-based routing for obvious cases
2. LLM-based query classification for ambiguous cases
3. Metadata-aware filtering once intent is detected

***

# 3. Corpus-Specific Strategy

You have three document types:

1. **Client engagement contracts**
2. **Past project write-ups**
3. **Methodology handbook**

Each needs a different chunking and indexing approach.

***

# 4. Client Engagement Contracts

## Nature of Data

Contracts are:

* Highly structured
* Section-numbered
* Clause-based
* Legal/commercial in tone
* Often require exact retrieval
* Sensitive to missing words or wrong references

Example structure:

```text
1. Definitions
2. Scope of Services
3. Fees and Payment
4. Confidentiality
5. Termination
5.1 Termination for Convenience
5.2 Termination for Cause
5.3 Effects of Termination
6. Limitation of Liability
```

For contracts, the goal is usually not broad semantic discovery. The goal is **precision**.

***

## Recommended Chunking Strategy for Contracts

Use **clause-aware structural chunking**.

Chunk at:

```text
Document → Article/Section → Clause/Subclause
```

Example chunks:

```json
{
  "client": "Acme",
  "document_type": "contract",
  "section_number": "5.1",
  "section_title": "Termination for Convenience",
  "text": "Either party may terminate this Agreement for convenience by providing thirty days written notice..."
}
```

Do not blindly split by token size first.

Instead:

1. Detect section headers and clause numbers.
2. Preserve each clause as a semantic unit.
3. If a clause is too long, split recursively inside the clause.
4. Always attach the parent section heading.

***

## Contract Chunking Pattern

```text
Parent:
5. Termination

Child:
5.1 Termination for Convenience

Child:
5.2 Termination for Cause

Child:
5.3 Effects of Termination
```

For very long clauses:

```text
5.2 Termination for Cause
   ↓
5.2-a Breach conditions
5.2-b Cure period
5.2-c Notice requirements
```

But every child chunk should carry:

```text
Contract name
Client name
Section number
Section title
Parent section
Effective date
Jurisdiction
Version
```

***

## Recommended Indexing Strategy for Contracts

Use **hybrid indexing with strong metadata filters**.

You need:

### 1. Sparse index / BM25

Useful for exact terms like:

```text
termination
indemnity
force majeure
limitation of liability
governing law
```

### 2. Dense vector index

Useful when users use different wording:

```text
Can the client exit the agreement early?
```

This should still match:

```text
Termination for Convenience
```

### 3. Metadata index

Critical for narrowing retrieval.

Example metadata:

```json
{
  "document_type": "contract",
  "client": "Acme",
  "contract_id": "ACME-2025-MSA",
  "section_number": "5.1",
  "section_title": "Termination for Convenience",
  "effective_date": "2025-04-01",
  "status": "active",
  "jurisdiction": "New York"
}
```

***

## Contract Retrieval Flow

For:

```text
Find the clause about termination in the Acme contract.
```

Use:

```text
Filter:
  document_type = contract
  client = Acme

Retrieve:
  BM25 search for "termination"
  + vector search for semantic variants

Rerank:
  Prefer exact section titles and clause headings

Return:
  Exact clause text with citation
```

Expected result:

```text
The relevant clause is Section 5.1, "Termination for Convenience", in the Acme Master Services Agreement.
```

***

## Best Contract Strategy Summary

```text
Chunking:
Clause-aware structural chunking

Indexing:
Hybrid search + metadata filters + exact field search

Retrieval:
High precision, low top-k, strong citation requirements
```

Use small top-k values initially, for example:

```text
top_k = 5 to 10
```

Because contract queries usually expect exact answers, not broad context.

***

# 5. Methodology Handbook

## Nature of Data

The methodology handbook is:

* Structured
* Procedure-oriented
* Concept-heavy
* Likely organized by phases, activities, templates, checkpoints, roles, and deliverables

Example:

```text
1. Discovery Phase
2. Assessment Phase
3. Solution Design
4. Delivery Governance
5. Risk Management
6. Quality Review Procedure
```

User question:

```text
What does our methodology say about stakeholder alignment?
```

This is a **concept lookup** question.

The system needs to find the applicable methodology section, not necessarily an exact phrase.

***

## Recommended Chunking Strategy for Methodology Handbook

Use **structure-aware recursive chunking**.

Chunk at logical procedural boundaries:

```text
Chapter → Section → Procedure → Step
```

Example:

```json
{
  "document_type": "methodology",
  "chapter": "Discovery Phase",
  "section": "Stakeholder Alignment",
  "procedure": "Initial Stakeholder Mapping",
  "text": "The engagement team should identify executive sponsors, operational owners, technical stakeholders..."
}
```

The chunk should usually include:

```text
Section title
Procedure name
Step name
Relevant body text
```

***

## Chunk Size Recommendation

For methodology content, use moderate chunks:

```text
Chunk size: 700 to 1,200 tokens
Overlap: 100 to 200 tokens
```

Why?

Because methodology answers often require enough surrounding context to explain:

* Why the procedure exists
* When to use it
* Steps involved
* Roles and responsibilities
* Outputs or deliverables

A 200-token chunk may be too small.

***

## Recommended Indexing Strategy for Methodology

Use:

```text
Dense vector index
+
BM25 sparse index
+
metadata filters
```

This is ideal because users may ask conceptually.

Example:

```text
How should we validate solution assumptions?
```

The handbook may use terms like:

```text
hypothesis validation
design assurance
checkpoint review
```

Dense vectors help bridge the vocabulary gap.

BM25 still helps when exact methodology terms are used.

***

## Metadata for Methodology

Use metadata such as:

```json
{
  "document_type": "methodology",
  "methodology_version": "v3.2",
  "phase": "Discovery",
  "activity": "Stakeholder Alignment",
  "role": "Engagement Manager",
  "deliverable": "Stakeholder Map",
  "procedure_id": "DISC-STAKE-001"
}
```

***

## Methodology Retrieval Flow

For:

```text
What does our methodology say about stakeholder alignment?
```

Use:

```text
Filter:
  document_type = methodology

Retrieve:
  dense vector search for concept match
  + BM25 for exact methodology terms

Rerank:
  Prefer current methodology version
  Prefer procedure-level chunks
  Prefer chunks with phase/activity metadata

Return:
  Answer with procedure references and citations
```

***

## Best Methodology Strategy Summary

```text
Chunking:
Structure-aware recursive chunking

Indexing:
Hybrid dense + sparse search with version and phase metadata

Retrieval:
Concept-oriented retrieval with reranking
```

Recommended top-k:

```text
top_k = 8 to 15
```

This gives enough material for a reasoned methodology answer without overloading the LLM.

***

# 6. Past Project Write-Ups

## Nature of Data

Past project write-ups are:

* Long-form prose
* Less structured
* Narrative
* Case-study-like
* Useful for broad synthesis
* Likely to contain lessons learned, approach, outcomes, challenges, and client context

Example user question:

```text
Summarize how we've handled engagements like this one.
```

This is not an exact lookup. It requires:

1. Finding similar engagements
2. Retrieving multiple relevant passages
3. Synthesizing common patterns
4. Possibly comparing approaches and outcomes

***

## Recommended Chunking Strategy for Project Write-Ups

Use **semantic chunking plus document-level summaries**.

For long-form prose, fixed-size chunking can split the narrative poorly. Better options:

```text
Semantic chunking
+
section-aware chunking if headings exist
+
summary indexing
```

If write-ups have headings:

```text
Client Context
Challenge
Approach
Solution
Technology Stack
Outcomes
Lessons Learned
```

Then chunk by those headings.

If they are pure prose, use semantic shifts.

***

## Project Write-Up Chunking Pattern

Use multiple levels:

```text
Document summary
   ↓
Section summary
      ↓
Semantic chunks
```

Example:

```json
{
  "document_type": "project_writeup",
  "client_industry": "Insurance",
  "engagement_type": "Cloud Migration",
  "section": "Approach",
  "text": "The team began with application portfolio assessment, dependency mapping, and phased migration planning..."
}
```

Also create a compact project-level summary:

```json
{
  "document_type": "project_summary",
  "project_name": "Insurance Cloud Migration",
  "industry": "Insurance",
  "problem": "Legacy application modernization",
  "approach": "Portfolio assessment, migration factory, governance model",
  "outcome": "Reduced infrastructure cost and improved release agility"
}
```

This summary index is extremely useful for broad synthesis.

***

## Recommended Indexing Strategy for Project Write-Ups

Use a **multi-level index**.

### Index 1: Project Summary Index

Used to find similar engagements first.

Stores one compact representation per project.

Good for:

```text
Find projects similar to this engagement.
```

### Index 2: Project Detail Chunk Index

Used after similar projects are identified.

Stores section-level or semantic chunks.

Good for:

```text
What approach did we use?
What risks came up?
What lessons were learned?
```

### Index 3: Metadata Index

Important for filtering.

Metadata examples:

```json
{
  "document_type": "project_writeup",
  "industry": "banking",
  "region": "APAC",
  "engagement_type": "Data Platform Modernization",
  "technology": ["AWS", "Snowflake", "Kafka"],
  "delivery_model": "Agile",
  "year": 2024
}
```

***

## Project Write-Up Retrieval Flow

For:

```text
Summarize how we've handled engagements like this one.
```

Use a two-stage retrieval pattern:

```text
Stage 1:
Retrieve similar projects from project summary index

Stage 2:
For top N projects, retrieve relevant detailed chunks

Stage 3:
Rerank by relevance, recency, industry, engagement type

Stage 4:
Generate synthesis across projects
```

Example:

```text
User query:
"How have we handled Salesforce data migration engagements for insurance clients?"

Stage 1:
Find top 10 similar project summaries.

Stage 2:
Retrieve approach, risks, lessons learned, and outcomes sections from those 10 projects.

Stage 3:
Group evidence by theme.

Stage 4:
Generate answer:
- Common approach
- Typical challenges
- Reusable accelerators
- Delivery risks
- Lessons learned
- Example engagements
```

***

## Best Project Write-Up Strategy Summary

```text
Chunking:
Semantic chunking + section-aware chunking + project-level summaries

Indexing:
Multi-level vector index + metadata filters + optional hybrid search

Retrieval:
Broad recall first, then evidence-focused reranking
```

Recommended top-k:

```text
Stage 1 project summary retrieval:
top_k = 10 to 20 projects

Stage 2 detailed chunk retrieval:
top_k = 3 to 5 chunks per selected project
```

***

# 7. Recommended Design by Data Type

| Data Type            | Chunking Strategy                           | Indexing Strategy                                     | Retrieval Style  |
| -------------------- | ------------------------------------------- | ----------------------------------------------------- | ---------------- |
| Client contracts     | Clause-aware structural chunking            | Hybrid search + metadata + exact field filters        | Precision lookup |
| Methodology handbook | Structure-aware recursive chunking          | Hybrid dense + sparse + metadata                      | Concept lookup   |
| Project write-ups    | Semantic/section-aware chunking + summaries | Multi-level vector index + metadata + hybrid optional | Broad synthesis  |

***

# 8. Recommended Design by Query Type

## Query Type 1: “What does our methodology say about X?”

Use:

```text
Index:
Methodology index

Chunking:
Structure-aware recursive chunks

Search:
Hybrid search

Filters:
document_type = methodology
current_version = true

Reranking:
Prefer procedure-level and official handbook chunks

Answer style:
Explain the methodology guidance and cite section/procedure references
```

***

## Query Type 2: “Find the clause about termination in the Acme contract”

Use:

```text
Index:
Contract clause index

Chunking:
Clause-aware structural chunks

Search:
Metadata filter + BM25 + vector search

Filters:
client = Acme
document_type = contract

Reranking:
Exact title/heading match gets high priority

Answer style:
Return the exact clause, section number, contract name, and citation
```

***

## Query Type 3: “Summarize how we’ve handled engagements like this one”

Use:

```text
Index:
Project summary index
+
Project detailed chunk index

Chunking:
Project-level summary + semantic chunks

Search:
Two-stage retrieval

Filters:
industry, engagement_type, technology, year, region if available

Reranking:
Prefer similar industry, recent projects, similar scope

Answer style:
Synthesize across multiple engagements with examples and caveats
```

***

# 9. Proposed Physical Index Design

I would create separate logical indexes.

```text
contracts_clause_index
methodology_index
project_summary_index
project_detail_index
```

***

## contracts\_clause\_index

Stores one record per clause or subclause.

```json
{
  "id": "acme-msa-2025-section-5-1",
  "text": "Either party may terminate this Agreement for convenience...",
  "embedding": [0.12, -0.33, 0.87],
  "metadata": {
    "document_type": "contract",
    "client": "Acme",
    "contract_name": "Acme Master Services Agreement",
    "section_number": "5.1",
    "section_title": "Termination for Convenience",
    "effective_date": "2025-04-01",
    "status": "active"
  }
}
```

***

## methodology\_index

Stores structured methodology chunks.

```json
{
  "id": "methodology-v3-discovery-stakeholder-alignment",
  "text": "Stakeholder alignment should begin during the discovery phase...",
  "embedding": [0.44, 0.11, -0.29],
  "metadata": {
    "document_type": "methodology",
    "version": "3.2",
    "phase": "Discovery",
    "activity": "Stakeholder Alignment",
    "procedure_id": "DISC-STAKE-001",
    "is_current": true
  }
}
```

***

## project\_summary\_index

Stores one summary per project.

```json
{
  "id": "project-2024-insurance-cloud-migration-summary",
  "summary": "This engagement involved modernizing legacy insurance applications through cloud migration...",
  "embedding": [0.18, -0.27, 0.65],
  "metadata": {
    "document_type": "project_summary",
    "industry": "Insurance",
    "engagement_type": "Cloud Migration",
    "region": "APAC",
    "year": 2024,
    "technologies": ["AWS", "Kubernetes", "Kafka"]
  }
}
```

***

## project\_detail\_index

Stores detailed chunks from write-ups.

```json
{
  "id": "project-2024-insurance-cloud-migration-approach-01",
  "text": "The team used a phased migration approach beginning with application portfolio assessment...",
  "embedding": [0.22, 0.19, -0.72],
  "metadata": {
    "document_type": "project_writeup",
    "project_id": "project-2024-insurance-cloud-migration",
    "section": "Approach",
    "industry": "Insurance",
    "engagement_type": "Cloud Migration",
    "year": 2024
  }
}
```

***

# 10. Recommended Retrieval Architecture

```text
                          ┌─────────────────────┐
                          │      User Query      │
                          └──────────┬──────────┘
                                     │
                                     ▼
                          ┌─────────────────────┐
                          │ Query Intent Router  │
                          └──────────┬──────────┘
                                     │
        ┌────────────────────────────┼────────────────────────────┐
        │                            │                            │
        ▼                            ▼                            ▼
┌──────────────────┐       ┌──────────────────┐        ┌────────────────────┐
│ Contract Lookup  │       │ Methodology RAG  │        │ Project Synthesis  │
└────────┬─────────┘       └────────┬─────────┘        └─────────┬──────────┘
         │                          │                            │
         ▼                          ▼                            ▼
┌──────────────────┐       ┌──────────────────┐        ┌────────────────────┐
│ Clause Index     │       │ Methodology Index│        │ Project Summary Idx│
│ Hybrid + Filter  │       │ Hybrid + Filter  │        └─────────┬──────────┘
└────────┬─────────┘       └────────┬─────────┘                  │
         │                          │                            ▼
         ▼                          ▼                  ┌────────────────────┐
┌──────────────────┐       ┌──────────────────┐        │ Project Detail Idx │
│ Rerank Clauses   │       │ Rerank Procedure │        │ Per-project chunks │
└────────┬─────────┘       └────────┬─────────┘        └─────────┬──────────┘
         │                          │                            │
         └──────────────┬───────────┴──────────────┬─────────────┘
                        ▼                          ▼
              ┌────────────────────────────────────────┐
              │ Context Assembly + Citation Formatting │
              └───────────────────┬────────────────────┘
                                  ▼
                        ┌──────────────────┐
                        │       LLM        │
                        └──────────────────┘
```

***

# 11. Context Assembly Strategy

The way you assemble context should differ per query type.

## For Contract Lookup

Use small, precise context:

```text
- Matching clause
- Parent section title
- Neighboring clause if needed
- Contract metadata
```

Avoid giving too much unrelated contract text.

***

## For Methodology Lookup

Use procedure-level context:

```text
- Relevant methodology chunk
- Parent phase
- Procedure steps
- Related deliverables
- Current version indicator
```

This helps the LLM answer in a structured way.

***

## For Project Synthesis

Use grouped evidence:

```text
Project 1:
  - Context
  - Approach
  - Outcome
  - Lessons learned

Project 2:
  - Context
  - Approach
  - Outcome
  - Lessons learned

Project 3:
  - Context
  - Approach
  - Outcome
  - Lessons learned
```

Then ask the LLM to synthesize:

```text
Identify common patterns, differences, risks, and reusable recommendations.
```

***

# 12. Important Design Choice: Separate Indexes vs Single Index

You could store everything in one vector database with document\_type metadata.

But for this scenario, I recommend **separate logical indexes**.

## Why?

Because contracts, methodology, and project write-ups have different retrieval behavior.

| Concern              | Single Index        | Separate Indexes      |
| -------------------- | ------------------- | --------------------- |
| Simplicity           | Easier initially    | Slightly more complex |
| Query precision      | Lower               | Higher                |
| Tuning per data type | Harder              | Easier                |
| Chunking strategy    | Usually compromised | Optimized per corpus  |
| Access control       | Harder              | Cleaner               |
| Evaluation           | Harder              | Easier                |

Recommended:

```text
Use one physical vector platform, but separate logical indexes or collections.
```

For example:

```text
rag_contracts
rag_methodology
rag_project_summaries
rag_project_details
```

***

# 13. Access Control and Security Considerations

This scenario has contracts, so access control is important.

At retrieval time, always apply security filters before semantic search or during filtered retrieval.

Example:

```json
{
  "allowed_clients": ["Acme", "Contoso"],
  "allowed_document_types": ["contract", "methodology"],
  "user_region": "APAC"
}
```

The retrieval layer should never retrieve chunks the user is not authorized to see.

For contract data especially, avoid relying only on the LLM to ignore unauthorized context. The context should not be retrieved in the first place.

***

# 14. Evaluation Strategy

You should evaluate each query type separately.

## Contract Evaluation

Measure:

```text
Exact clause hit rate
Correct section number
Correct client contract
No wrong contract leakage
Citation accuracy
```

Example test:

```text
Question:
Find termination clause in Acme contract.

Expected:
Acme MSA, Section 5.1 or 5.2 depending wording.
```

***

## Methodology Evaluation

Measure:

```text
Concept recall
Procedure accuracy
Version correctness
Citation to correct methodology section
Answer completeness
```

***

## Project Synthesis Evaluation

Measure:

```text
Similarity of retrieved projects
Coverage of relevant examples
Quality of synthesis
Whether answer distinguishes evidence from inference
Diversity of examples
```

For broad synthesis, do not only evaluate whether one chunk was retrieved. Evaluate whether the answer uses the right set of engagements.

***

# 15. Recommended Initial Configuration

A practical starting point:

## Contracts

```text
Chunking:
Clause-level, section-aware

Overlap:
Usually no overlap at clause level
Small overlap only when splitting long clauses

Index:
Hybrid, BM25 + vector

Metadata:
client, contract_id, section_number, section_title, status, effective_date

top_k:
5 to 10
```

## Methodology Handbook

```text
Chunking:
Structure-aware recursive

Chunk size:
700 to 1,200 tokens

Overlap:
100 to 200 tokens

Index:
Hybrid

Metadata:
version, phase, activity, procedure_id, role, deliverable

top_k:
8 to 15
```

## Project Write-Ups

```text
Chunking:
Semantic or section-aware

Chunk size:
800 to 1,500 tokens

Overlap:
100 to 200 tokens

Additional:
Generate project-level summaries

Index:
Project summary index + project detail index

Stage 1 top_k:
10 to 20 projects

Stage 2 top_k:
3 to 5 detailed chunks per project
```

***

# 16. My Recommended Final Design

For this professional-services partner, I would design the RAG system as follows:

```text
1. Ingest all documents with document-type classification.

2. For contracts:
   - Parse section numbers and headings.
   - Chunk by clause/subclause.
   - Store in a contract clause index.
   - Use hybrid search with client/contract metadata filters.

3. For methodology handbook:
   - Parse table of contents, chapters, procedures, steps.
   - Chunk by procedure or logical section.
   - Use recursive fallback for long sections.
   - Store in methodology index.
   - Use hybrid search and version filtering.

4. For project write-ups:
   - Extract project metadata.
   - Generate project-level summaries.
   - Chunk detailed write-ups semantically or by section.
   - Store summaries and detailed chunks in separate indexes.
   - Use two-stage retrieval for synthesis.

5. Add a query router:
   - Contract clause lookup
   - Methodology concept lookup
   - Project synthesis

6. Add reranking:
   - Exact section/title boost for contracts.
   - Procedure/current-version boost for methodology.
   - Similarity, recency, industry, and engagement-type boost for projects.

7. Generate answers with citations:
   - Clause reference for contracts.
   - Section/procedure reference for methodology.
   - Project examples and evidence grouping for synthesis.
```

***

# Short Answer

Use a **multi-index RAG architecture**:

```text
Contracts:
Clause-aware chunking + hybrid search + metadata filters

Methodology:
Structure-aware recursive chunking + hybrid search + versioned metadata

Project write-ups:
Semantic/section-aware chunking + project summaries + two-stage retrieval
```

This design gives you **precision for contracts**, **conceptual retrieval for methodology**, and **broad synthesis for past project experience**, instead of forcing all document types into one generic retrieval pattern.
