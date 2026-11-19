# MongoDB vs OpenSearch for AOR-Scoped Identity Matching

## Recommendation
Since we are on Atlas, we can use Atlas Search for the fuzzy tier and keep MongoDB as the system of record. This avoids a second datastore and a sync pipeline. Add OpenSearch only if Atlas Search falls short on phonetic matching or scoring control. If matching is mostly exact, MongoDB indexes alone are enough.

The use case: when a lead or quote is captured by a broker or agent, check whether the person is already an enrolled member, searching only within that agent's AOR scope.

## Why data volume is not the deciding factor
With AOR scoping, each query only considers the members tied to one agent or broker, and a compound index on the AOR identifier plus the match attributes keeps that fast in MongoDB.
What decides the choice is how we match, not how much data we hold.

## When MongoDB alone is enough
MongoDB alone works when prospect data is clean enough to match deterministically and misses can go to manual review.
- Prospect data carries strong identifiers: SSN or last 4 plus DOB, email, or phone.
- Rules are tiered and exact, for example same DOB, last name and ZIP.
- We can tolerate misses on typos or variants.

To do this well, build compound indexes starting with the AOR identifier, normalize data at write time (lowercased names, standardized phone and address), and store hashed or normalized match keys. This avoids a second datastore and any sync lag.

## Why Atlas Search is the first choice for fuzzy matching

Prospect data is messy: "Jon" vs "Jonathan", "Smyth" vs "Smith", transposed DOB digits, address variants, shared household phone numbers. MongoDB's regex and plain text search handle this poorly, but Atlas Search is Lucene-based and covers most of it inside the same database.
- Fuzzy matching with edit distance, plus n-gram and edge-n-gram analyzers.
- Synonym lists for nicknames.
- Weighted multi-field scoring that returns ranked candidates with a score, not just yes or no.
- A `compound` query where the AOR scope is a hard `filter` and the identity attributes are scored `should` clauses.
- No sync pipeline: Atlas maintains the index from the same collection, so there is no change-data-capture lag, no drift, and no extra step when an AOR is reassigned.
- One system to secure: PHI stays in one place under one access model and one BAA.

## When OpenSearch would still win
Check these before committing to Atlas Search. Any one that fails is a reason to add OpenSearch.
- **Phonetic matching:** confirm the analyzers we need, for example for "Smyth" vs "Smith", are available in Atlas Search. This is the likeliest gap.
- **Scoring control:** confirm Atlas Search's scoring options can reliably separate your high, middle and low score bands.
- **Other use cases:** we want one search platform beyond identity matching, such as logs or observability.
- **Skills and preference:** The team already runs OpenSearch.

## Options compared
The hybrid option fits most real payer workloads: exact rules first, fuzzy scoring only when needed.
| Option | Fits when | Trade-off |
| --- | --- | --- |
| MongoDB indexes only | Matching is deterministic | Poor on typos and variants |
| Atlas Search (if on Atlas) | We need fuzzy matching and want to avoid a sync pipeline | Tied to Atlas; fewer tuning options than OpenSearch |
| Amazon OpenSearch | We need fuzzy, scored matching, and may reuse it for other search needs | Extra system, sync pipeline, eventual consistency |
| Hybrid (MongoDB then OpenSearch) | Most real payer workloads | Slightly more logic; best accuracy and cost |
## Suggested approach: tiered matching
Exact rules run first in MongoDB; OpenSearch scores only what they miss.
&#91;embedded content: tiered matching flow · 2 tiers, 3 score bands\]
Auto-link only above a high score, send the middle band to human review, and treat low scores as a new prospect; calibrate thresholds on labeled sample data.
## Design considerations if we use OpenSearch
Index only what matching needs, and keep MongoDB authoritative so the index can always be rebuilt.
- **Index only match attributes:** name parts, DOB, address, phone, email, AOR ID and a member ID reference, not the full enrollment record. This limits PHI exposure and keeps the index small. Fetch full details from MongoDB by member ID after a match.
- **Sync with change data capture:** MongoDB change streams into Kafka or OpenSearch Ingestion. Expect seconds of lag, which is usually fine for quoting and lead capture.
- **Handle AOR changes:** book-of-business transfers and AOR reassignments must update the index, or scoping will return stale or wrong results.
- **Enforce AOR scoping in the query, not just in the app:** use a mandatory filter, and consider document-level security or routing by AOR ID if tenancy matters.
- **Compliance:** OpenSearch Service is HIPAA-eligible under a BAA. Use VPC-only access, KMS encryption at rest, node-to-node encryption, fine-grained access control and audit logging. Consider hashing SSNs and keeping them out of the searchable index where possible.
- **Sizing:** a small multi-AZ domain (3 data nodes) is plenty at this scale. Serverless is an option, but managed domains are usually cheaper for a steady workload.
- **No auto-merge on score alone:** a false positive links a prospect to the wrong person's record, which is a privacy problem. Calibrate thresholds on labeled sample data.
## Next steps and open questions
Run a bake-off before adding a system: take a sample of real prospect records with known matches, run MongoDB deterministic rules against a scored OpenSearch query, and compare precision and recall. If MongoDB alone catches nearly all true matches, skip OpenSearch. If fuzzy matching finds a meaningful number of true matches the exact rules miss, the extra system is justified.
Open questions that would sharpen the recommendation:
- Which identifiers do we reliably get at lead or quote time?
- Are we on Atlas or self-managed MongoDB?
- How many AORs, and how many members per AOR?