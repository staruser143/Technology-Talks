# Using MongoDB as primary store

If we are already on MongoDB Atlas: 
-   **Use MongoDB Atlas Search**:  It eliminates the complexity of syncing data to a separate system and is sufficient for the scale we need provided we architect the queries correctly.

If need maximum search flexibility and have dedicated ops capacity:
**Use OpenSearch**:
- It offers the most powerful fuzzy matching and relevance tuning, but comes with the overhead of managing a separate cluster and data synchronization pipeline.

- Avoid Redis as the primary identity search engine. It is excellent for caching and real-time lookups, but its search capabilities (while improving) are less suited for complex fuzzy matching and large-scale document analysis compared to Atlas Search or OpenSearch.


## Detailed analysis for each option based on our requirements:

### 1. MongoDB Atlas Search (Best for Architectural Simplicity)

If the enrollment data and prospect data are (or will be) stored in MongoDB, Atlas Search is the most pragmatic choice.

**Performance at Scale**: 
 - A well-designed Atlas Search index using the lucene.keyword analyzer for exact matching on critical fields (like SSN or Member ID) and standard text analyzers for names can perform well.
-  For identity matching, we should first attempt deterministic matching (exact match on ID/SSN) and only use fuzzy logic as a fallback.

**Operational Efficiency**:
- Atlas Search embeds a Lucene index directly alongside the database, meaning there is no separate sync pipeline to build or maintain.
- The prospect data and enrollment data can be queried in one place, reducing the risk of data inconsistency.

**Cost**:
- We pay for the Atlas cluster resources, avoiding the infrastructure and engineering costs of a separate search cluster .

### 2. Amazon OpenSearch (Best for Advanced Search Control)

If we require the absolute best **fuzzy matching algorithm**, **phonetic matching (e.g., Soundex, Metaphone for names)**, and **complex relevance scoring**, OpenSearch is the industry standard.

**Search Capabilities**: 
- It is built from the ground up for search, using inverted indexes. 
- It excels at handling typos and variations in names or addresses, which is common in prospect data .

**Architectural Cost**
- We must build and maintain a data pipeline (using Change Streams or a connector) to sync the enrollment data from MongoDB (or the source database) into OpenSearch . 
- This introduces eventual consistency and operational overhead.
- We also need expertise to manage shards, JVM tuning, and cluster scaling .

**Use Case Fit**:
-  If the primary database is not MongoDB (e.g., it is a relational database), then OpenSearch is a very strong candidate because we are already accustomed to managing data movement between systems.

### 3. Redis (Not Recommended for Primary Search)

While Redis is excellent for ultra-low-latency lookups (e.g., checking a cache before hitting the database), it is not the right tool for the core identity matching requirement at the million-document scale.

**Limitations**:
 - Redis is primarily an in-memory data structure store.
 - While it has search capabilities (RediSearch), indexing a million documents with complex fuzzy matching fields may lead to high memory costs or performance degradation compared to Lucene-based systems.

**Use Case**:
-  It could be used as a caching layer to store the results of frequent identity checks or to store a mapping of AOR (Agent of Record) to their assigned prospects, reducing load on the primary search engine.

## Critical Architectural Considerations for Healthcare Identity Matching

### 1. AOR Scoping: 
- Matching only against the scope of AOR, is a crucial filter.
- In both Atlas Search and OpenSearch, we should apply the AOR filter first (using a standard B-Tree or keyword index) before running the expensive fuzzy matching on names or demographics. This dramatically reduces the search space.

### 2. Deterministic vs. Probabilistic: 
- For healthcare payers, we should prioritize deterministic matching (exact match on Member ID, SSN, or Date of Birth + Last Name) to avoid false positives . 
- Use probabilistic/fuzzy matching (which both Atlas Search and OpenSearch support) only when deterministic keys are missing or have minor data entry errors.

### 3. Hybrid Approach: 
- The most performant architecture often involves a hybrid approach.
- Use MongoDB as your source of truth and for exact/structured queries (filtering by AOR, exact ID match). 
- If the exact match fails and we need deep fuzzy search across millions of records, offload that specific query to an OpenSearch index that is optimized for text analysis.

## Final Verdict:
-  If we are on MongoDB Atlas, start with Atlas Search. It is likely sufficient for the scale and will save us significant DevOps time.
-  If we find that fuzzy matching on names is not accurate enough (e.g., struggling with common name variations), then we can consider adding OpenSearch as a secondary, specialized search index.

