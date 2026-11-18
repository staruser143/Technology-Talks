- Since your system of record for enrollment/HCID is already on MongoDB Atlas</MongoDB Atlas>, we should evaluate MongoDB Search before introducing Amazon OpenSearch Service</Amazon OpenSearch Service> as another datastore/indexing platform.

- For the particular matching problem, MongoDB Search may actually be the simpler architecture because it supports fuzzy matching, filtering, custom relevance scoring, custom analyzers, and compound search against the data already in MongoDB.

## 1. The architecture becomes considerably simpler

### With OpenSearch, 
```
MongoDB Atlas Enrollment
       |
      CDC
       |
 Kafka / EventBridge
       |
 Indexing Pipeline
       |
       v
Amazon OpenSearch
       ^
       |
 Matching Service

```
### With MongoDB Search:
```
MongoDB Atlas
 Enrollment Collection
       |
       +---- MongoDB Search Index
                    ^
                    |
              Matching Service
                    ^
                    |
             Prospect / Quote

```
We eliminate:
```
CDC/index replication into another platform
MongoDB → OpenSearch synchronization
another persistence/search technology
duplicated enrollment data
AOR-change synchronization between MongoDB and OpenSearch
much of the operational reconciliation logic
```
That is a significant architectural advantage.

## 2. How I'd model it

The enrollment collection may already contain something resembling:
```json
{
  "_id": "xxx",
  "hcid": "HC123456",

  "aorId": "AOR100",

  "firstName": "Sridhar",
  "lastName": "Balasubramanian",
  "dob": "1975-06-10",

  "zip": "600045",

  "email": "sridhar@example.com",
  "phone": "9841234567"
}
```

We should add either materialized normalized fields or derive them during the enrollment processing path:
```json
{
  "hcid": "HC123456",

  "aorId": "AOR100",

  "firstName": "Sridhar",
  "lastName": "Balasubramanian",
  "dob": "1975-06-10",

  "normalizedFirstName": "sridhar",
  "normalizedLastName": "balasubramanian",

  "normalizedEmail": "sridhar@example.com",
  "normalizedPhone": "9841234567",
  "zip": "600045",

  "matchKey1": "...",
  "matchKey2": "..."
}
```

Then build a MongoDB Search index only over relevant matching fields, rather than indexing the entire enrollment document.

MongoDB specifically recommends static mappings when possible because dynamic mappings can unnecessarily increase the size and performance footprint of a Search index.

## 3. The AOR constraint works very nicely

Say the incoming prospect is:
```
AOR       = AOR100
FirstName = Sridar
LastName  = Balasubramaniam
DOB       = 1975-06-10
ZIP       = 600045
```

The search logic is essentially:
```
MUST

AOR = AOR100

AND

one/more of:

DOB       = exact
ZIP       = exact
FirstName ≈ fuzzy
LastName  ≈ fuzzy
```

- MongoDB Search's compound search capability is well suited to combining filtering and matching conditions.
-  MongoDB explicitly documents Search as supporting fuzzy matching, filtering, customizable relevance scoring, and custom analyzers.

Conceptually:
```
db.enrollments.aggregate([
  {
    $search: {
      index: "client_match",

      compound: {

        filter: [
          {
            equals: {
              path: "aorId",
              value: "AOR100"
            }
          }
        ],

        should: [
          {
            text: {
              path: "normalizedFirstName",
              query: "sridar",
              fuzzy: {
                maxEdits: 1
              }
            }
          },
          {
            text: {
              path: "normalizedLastName",
              query: "balasubramaniam",
              fuzzy: {
                maxEdits: 1
              }
            }
          }
        ]
      }
    }
  },

  { $limit: 5 },

  {
    $project: {
      hcid: 1,
      aorId: 1,
      firstName: 1,
      lastName: 1,
      dob: 1,
      score: {
        $meta: "searchScore"
      }
    }
  }
])
```

- MongoDB Search fuzzy text matching uses Damerau-Levenshtein distance and supports maxEdits of 1 or 2, along with controls such as prefixLength and maxExpansions.

- For person-name matching, We should normally start conservatively with:
```
maxEdits = 1
```

rather than immediately accepting two-character differences.

## 4. We should use a two-stage strategy

We should not send every prospect directly into fuzzy MongoDB Search.

We should use:

### Stage 1: Deterministic matching

We can exploit ordinary MongoDB indexes for your strongest rules:
```
AOR + normalizedEmail

AOR + normalizedPhone

AOR + firstName + lastName + DOB

AOR + lastName + DOB + ZIP
```

For example:
```
db.enrollments.find({
    aorId: "AOR100",
    normalizedFirstName: "sridhar",
    normalizedLastName: "balasubramanian",
    dob: ISODate("1975-06-10")
})
.limit(2)
```

With appropriate compound indexes, this should be the cheapest path.

### Stage 2: MongoDB Search

Only if deterministic matching fails:
```
                   Prospect
                       |
                  Normalize
                       |
                       v
              Exact Mongo lookup
                       |
                 +-----+-----+
                 |           |
               Found      Not found
                 |           |
                HCID         v
                       MongoDB Search
                             |
                      fuzzy candidate
                          retrieval
                             |
                       Top 5 candidates
                             |
                      Matching Engine
                             |
                 +-----------+-----------+
                 |           |           |
              MATCH      AMBIGUOUS    NO MATCH

```
That's the architecture I would currently favor.

## 5. Candidate retrieval vs identity decision still applies

Even though we're replacing OpenSearch, I would preserve our earlier architectural boundary.

### MongoDB Search answers:
```
"Which enrollment records look most like this prospect?"
```
The Matching Service answers:
```
"Are these two actually the same person?"
```
Do not automatically treat:
```
searchScore = highest
```

as:
```
identity = same person
```

For example the top candidate might be:
```
HCID: HC10001

AOR                Exact
DOB                Exact
First Name         95%
Last Name          96%
ZIP                Exact
Email              Missing
Phone              Different
```

The business scoring engine can compute:
```
DOB             30
First Name      20
Last Name       25
ZIP             10
Email            0
Phone            0
------------------
Confidence       85
```

The policy might then say:
```
>= 95     Auto-link

85-94     Possible/secondary validation

< 85      Don't link

```
The numeric thresholds need to be calibrated with real historical cases rather than adopted blindly.

## 6. One pattern I'd specifically recommend: blocking

This gets especially important when you have tens of millions of members.
Don't ask MongoDB Search:
```
Search for "John Smith"
```

across everything.

Instead formulate the problem as:
```
AOR100
    |
    +--- DOB
            |
            +--- ZIP / Last Name
                      |
                      +--- fuzzy name


This is known as blocking/candidate generation in entity-resolution systems.
```

We are progressively shrinking:
```
50,000,000 enrollments

        ↓ AOR

50,000

        ↓ DOB / ZIP / other strong field

20-100

        ↓ fuzzy matching

~5 candidates

        ↓ business scoring

1 HCID
```

The actual counts will depend on your production AOR distribution, but this illustrates why AOR should participate as a hard boundary rather than merely contributing to the score.

## 7. One difference from OpenSearch: AOR routing

This is where we should distinguish the products.

With Amazon OpenSearch Service</Amazon OpenSearch Service>, we discussed making:
```
routing = AOR_ID
```

so searches can target only the routed shard.

We should not attempt to reproduce that OpenSearch implementation literally in MongoDB Search.

Instead, for MongoDB, think in terms of:
```
AOR filter/blocking
+
MongoDB collection sharding strategy
+
Search index partition/scaling
```

For very large Search indexes, MongoDB documents index partitioning and specifically notes that a Search index approaching more than 2.1 billion index objects should use numPartitions or a sharded cluster.

So:

AOR remains the logical matching boundary, but it does not have to become a one-to-one physical Search partition.

That's an important distinction.

## 8. Search latency / consistency needs consideration

There is one architectural issue we should explicitly test.

MongoDB documents eventual consistency and indexing latency as considerations when creating/updating Search indexes.

Imagine this sequence:
```
10:00:00
Enrollment HC123 created

10:00:01
Prospect/Quote matching requested

10:00:??
Search index sees HC123
```

If the business requires a newly created enrollment to become matchable immediately, measure this carefully during the proof of concept.

Interestingly, this is another reason the two-stage solution is useful:
```
Exact MongoDB query
       +
MongoDB Search fallback
```

rather than making Search the only matching mechanism.

## 9. MongoDB Search vs OpenSearch for your use case

Here's how we would make the architecture decision.
```
Requirement	MongoDB Search	Amazon OpenSearchEnrollment already in MongoDB	Excellent	Requires replication
Exact matching	Excellent	Excellent
Fuzzy names	Excellent	Excellent
AOR filtering	Excellent	Excellent
Custom relevance	Excellent	Excellent
Dedicated routing by AOR	Less direct	Excellent
CDC pipeline required	No additional copy	Yes
Data duplication	Minimal	Enrollment projection duplicated
Operational complexity	Lower	Higher
Cross-source identity data	Good	Potentially stronger neutral search tier
Independent scaling	Available, architecture-dependent	Strong
Existing MongoDB skill set	Advantage	Additional platform
Future search use cases	Strong	Very strong
10. The decision changes if you later add other enrollment systems
```


If:
```
                 MongoDB Atlas
                     |
             all enrollment records
                     |
                     v
              Matching requirement

``
then we should favor:

## MongoDB Search

because adding OpenSearch creates another derived data platform without a compelling benefit yet.

Tomorrow

Suppose it becomes:
```
MongoDB Atlas ────────┐
                      |
Legacy Oracle ────────+
                      |
Salesforce ───────────+----> Client Identity Index
                      |
Claims Platform ──────+
                      |
Mainframe ────────────┘
```

then we would reconsider a dedicated identity/search projection, potentially backed by Amazon OpenSearch Service</Amazon OpenSearch Service>, because the matching index is no longer naturally owned by MongoDB.

That is an important architecture criterion.

## Recommendation for  current situation


###  Primary path
```
Prospect / Quote
       ↓
Normalization
       ↓
AOR boundary
       ↓
Exact MongoDB compound-index lookup
       ↓
Match?
   YES ───────→ HCID link

    NO
     ↓
MongoDB Search
     ↓
AOR-filtered fuzzy candidate generation
     ↓
Top 3-5 HCIDs
     ↓
Business match scoring
     ↓
MATCH / AMBIGUOUS / NO MATCH
     ↓
Prospect/Quote ↔ HCID Link
```

We  would choose this over adding OpenSearch initially.

The most important POC isn't MongoDB Search vs OpenSearch raw throughput. It's testing 5 things with your actual enrollment distribution:
```
P95/P99 matching latency
Search-index freshness after enrollment changes
False-positive rate
Ambiguous-match rate
Performance for your largest AOR, not your average AOR
```
If MongoDB Search satisfies those, introducing OpenSearch would likely be unnecessary architectural complexity.