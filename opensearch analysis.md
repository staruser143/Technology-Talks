For your use case, I would use Amazon OpenSearch Service</Amazon OpenSearch Service> as a real-time, AOR-scoped client matching index, not as the master system for enrollment or the place where the final Prospect/Quote → HCID association lives. It is well suited to this because it gives you indexed filtering/search plus the ability to combine exact, fuzzy, and scored criteria.

## 1. Where OpenSearch fits

### Solution
```
Backend Enrollment
System / HCID
      |
      | CDC / events
      v
Matching data pipeline
      |
      | normalize + build search representation
      v
Amazon OpenSearch
"Client Matching Index"
      ^
      |
      | AOR + demographic search
      |
Matching Service <----- Prospect / Quote API
      |
      +---- Exact match ---> HCID
      |
      +---- Probable match --> review/secondary rules
      |
      +---- No match
      |
      v
Prospect-HCID Link DB
```

The critical distinction is:
```
Backend enrollment system = system of record
OpenSearch = optimized matching/read model
Match service = matching rules and confidence logic
Link database/table = durable Prospect/Quote → HCID association
```
This means OpenSearch can be rebuilt without losing authoritative client/link data.

## 2. What would actually be stored in OpenSearch?

Imagine your backend enrollment record is:
```json
{
  "hcid": "HC123456",
  "aorId": "AOR-10023",
  "firstName": "Sridhar",
  "lastName": "Balasubramanian",
  "dateOfBirth": "1975-06-10",
  "zip": "600045",
  "email": "sridhar@example.com",
  "phone": "(984) 123-4567"
}
```

We should create a matching representation such as:

{
  "hcid": "HC123456",

  "aorId": "AOR-10023",

  "firstName": "SRIDHAR",
  "lastName": "BALASUBRAMANIAN",
  "dateOfBirth": "1975-06-10",

  "zip": "600045",

  "email": "sridhar@example.com",
  "phone": "9841234567",

  "firstNameSearch": "sridhar",
  "lastNameSearch": "balasubramanian",

  "nameDobKey": "SRIDHAR|BALASUBRAMANIAN|19750610",
  "lastNameDobZipKey": "BALASUBRAMANIAN|19750610|600045"
}


Notice that this isn't necessarily a copy of your source enrollment schema.

It is a purpose-built search projection.

## 3. AOR should be a hard filter

This is probably the most important design decision.

Suppose the customer data contains:
```
AOR-100
   10,000 customers

AOR-200
   70,000 customers

AOR-300
   8,000 customers
```

A prospect arrives:
```
AOR = AOR-100
Name = Sridhar Balasubramanian
DOB = 1975-06-10
ZIP = 600045
```

We should never ask:
```
Who in the entire client population looks like Sridhar?
```
You ask:
```
Within AOR-100, who looks like Sridhar?
```
Conceptually:
```
FILTER
    aorId = AOR-100

THEN MATCH
    firstName
    lastName
    DOB
    ZIP
```

This dramatically reduces your candidate set and, just as importantly, prevents a fuzzy match against another AOR's customer.

## 4. AOR can also become the OpenSearch routing key

We can go one step further.

Instead of merely storing:
```
"aorId": "AOR-100"
``
we can use the AOR as the routing value.

Conceptually when indexing:
```
PUT /client-matching/_doc/HC123456?routing=AOR-100
```

and searching:
```
GET /client-matching/_search?routing=AOR-100
```

That makes a significant architectural difference.

Without routing:
```
                         Query
                           |
                +----------+----------+
                |          |          |
             Shard 1    Shard 2    Shard 3
                |          |          |
                +----------+----------+
                           |
                        Results

``
With AOR routing:
```

                         Query
                     routing=AOR100
                           |
                        Shard N
                           |
                        Results

```
- OpenSearch routing hashes a routing value to determine where documents live. 
- Supplying routing during search allows the request to target the appropriate shard rather than querying the whole index.

### Architectural caveat

We should not use AOR routing until we know the distribution of clients per AOR.

If most AORs have:
```
500 - 10,000 clients
``

but one large AOR has:
```
8,000,000 clients
```

pure routing=AOR_ID could create an undesirable hot/large shard concentration.

So we would first analyze:

Customers per AOR
```
P50
P90
P95
P99
MAX
``

That distribution tells us whether AOR_ID directly is an appropriate routing strategy or whether the biggest AORs require partitioning.

## 5. The matching query is where OpenSearch really helps

Suppose a prospect enters:
```
AOR = AOR-100
FirstName = "Sridhar"
LastName = "Balasubramanian"
DOB = 1975-06-10
ZIP = 600045
```

The Match Service constructs an OpenSearch query.

A simplified version might look conceptually like:
```json
{
  "size": 10,
  "query": {
    "bool": {

      "filter": [
        {
          "term": {
            "aorId": "AOR-100"
          }
        }
      ],

      "should": [

        {
          "term": {
            "dateOfBirth": "1975-06-10"
          }
        },

        {
          "term": {
            "zip": "600045"
          }
        },

        {
          "match": {
            "firstNameSearch": {
              "query": "sridhar",
              "fuzziness": "AUTO"
            }
          }
        },

        {
          "match": {
            "lastNameSearch": {
              "query": "balasubramanian",
              "fuzziness": "AUTO"
            }
          }
        }
      ]
    }
  }
}
```

Conceptually OpenSearch is saying:

Mandatory:

```
    AOR = AOR-100
```
Prefer:
```
    DOB matches
    ZIP matches
    First Name is highly similar
    Last Name is highly similar
```

The query returns candidate HCIDs ranked by relevance.

## 6. But I wouldn't implement one giant fuzzy query

This is important.

For payer/broker system, I'd implement a waterfall.

### Query 1: strongest deterministic match

Try:
```
AOR
+
First Name exact
+
Last Name exact
+
DOB exact
```

## Example:
```
AOR100
SRIDHAR
BALASUBRAMANIAN
1975-06-10
```

If exactly one HCID is found:
```
HC123456
```

We may not need any fuzzy matching at all.

## Query 2: alternate deterministic match

If Query 1 doesn't succeed:
```
AOR
+
LastName
+
DOB
+
ZIP
```

or another approved combination.

## Query 3: fuzzy candidate matching

Only then:
```
AOR = exact
DOB = exact/high weight
Last Name ≈ fuzzy
First Name ≈ fuzzy
ZIP = exact/high weight
```

This preserves performance and reduces false positives.

## 7. Example: spelling variation

Backend:
```
FirstName: Sridhar
LastName : Balasubramanian
DOB      : 1975-06-10

```
Prospect:
```
FirstName: Sridar
LastName : Balasubramaniam
DOB      : 1975-06-10
```

A conventional database lookup like:
```
WHERE
first_name = 'Sridar'
AND last_name = 'Balasubramaniam'
AND dob = '1975-06-10'
```

returns:
```
NO MATCH
```

The OpenSearch candidate search can tolerate the name variations while DOB and AOR provide stronger anchors:
```
AOR       EXACT
DOB       EXACT
```
```
Sridar
  ↓
Sridhar

Balasubramaniam
  ↓
Balasubramanian
```

Result:
```
HC123456
```

That's one of the strongest reasons to introduce OpenSearch here.

## 8. However, we should not use the native _score as your business match score

This distinction matters a lot.

We don't want:
```
OpenSearch _score = 8.72
```
therefore customer matched


That is a search relevance score, not necessarily your business-specific identity confidence.

Instead have OpenSearch answer:

Give me the most plausible 5 or 10 HCID candidates.

Then your Match Service calculates something deterministic:
```
Candidate HC123456

AOR
    exact       mandatory

DOB
    exact       +35

Last name
    similarity  +25

First name
    similarity  +20

ZIP
    exact       +10

Email
    exact       +10
---------------------
Match score      97
```

Then:
```
score >= 95
     AUTO MATCH

80 <= score < 95
     POSSIBLE MATCH

score < 80
     NO MATCH

```
Those thresholds are illustrative. In production I would derive them from labeled historical matches and explicitly optimize the false-positive rate. In this domain, a false match to the wrong HCID can be materially worse than temporarily failing to match.

## 9. Another optimization: deterministic composite keys

This is where I think your design can become extremely fast.

During indexing, generate:
```
Key1 =
AOR + FirstName + LastName + DOB

Key2 =
AOR + LastName + DOB + ZIP

Key3 =
AOR + Email

Key4 =
AOR + Phone
```

For example:
```
AOR100|SRIDHAR|BALASUBRAMANIAN|19750610
```

Store a hash of the key if appropriate:
```
key1Hash = SHA256(...)
```

The first search becomes effectively:
```
key1Hash = X
```

That's extremely cheap compared with fuzzy matching.

So your matching waterfall becomes:
```
                Prospect
                   |
                   v
               Normalize
                   |
                   v
          Generate Match Keys
                   |
          +--------+---------+
          |                  |
     Exact lookup         No result
          |                  |
       Match                v
                     Candidate Search
                            |
                     OpenSearch fuzzy
                            |
                       Top 5 HCIDs
                            |
                        Scoring
```

This is the recommended design.

## 10. What about keeping OpenSearch current?

This is another critical part.

Suppose:
```
HC123456
Name changed
Address changed
AOR changed
Phone changed
```

We don't want your search index several hours behind.

A reasonable architecture is:
```
Enrollment DB
     |
     | CDC
     v
Kafka / Kinesis
     |
     v
Normalization / Mapping
     |
     v
OpenSearch
```

If the platform already uses Kafka, this fits naturally with the CDC/event-driven patterns you've worked with.

Alternatively Amazon OpenSearch Ingestion</Amazon OpenSearch Ingestion> provides managed ingestion pipelines consisting of sources, buffers, processors and sinks, using Data Prepper as its underlying data engine. It supports features including persistent buffering, dead-letter queues, back pressure, and end-to-end acknowledgements.

The desired operational behavior to be:
```
Enrollment update
      |
      +---- few seconds ----> OpenSearch

```
rather than batch syncs every hour.

## 11. AOR changes need special handling

This becomes particularly interesting with routing.

Suppose:
```
HC123456

OLD:
AOR = AOR100

NEW:
AOR = AOR200
```

If AOR is the routing key, the document conceptually has to move from:

Shard determined by
```
hash(AOR100)
```

to:
```
Shard determined by
hash(AOR200)
```

The projection/indexing process therefore needs to understand AOR reassignment explicitly.

We need to model that as an event such as:
```
HCID_AOR_CHANGED

HCID: HC123456
oldAor: AOR100
newAor: AOR200
effectiveDate: ...

```
The indexing pipeline removes/reindexes the searchable representation appropriately.

And the authoritative history still belongs outside OpenSearch:
```
HCID
AOR
effectiveFrom
effectiveTo
```
## 12. The final link shouldn't depend on OpenSearch

After matching:
```
Prospect P123
Quote Q456
        |
      MATCH
        |
        v
HCID HC123456
```

create the durable transactional record:

```
PROSPECT_HCID_LINK
--------------------------------
prospectId       P123
quoteId          Q456
hcid             HC123456
aorId            AOR100
matchType        AUTO
matchRule        NAME_DOB_ZIP_V3
matchScore       98.2
matchedAt        timestamp
```

After that, normal application processing reads the link record.

It shouldn't have to ask OpenSearch every time:
```
Which HCID did P123 match?
```
OpenSearch's responsibility ended once it helped identify the candidate.

## 13. Security deserves special treatment

Because this index may contain healthcare/member demographic information, I would not expose OpenSearch directly to the Prospect/Quote application.

Use:
```
Quote Service
      |
      v
Matching Service
      |
      v
Amazon OpenSearch


rather than:

Browser / Quote UI
      |
      X
Amazon OpenSearch
```

Amazon OpenSearch Service</Amazon OpenSearch Service> supports IAM, encryption mechanisms and fine-grained access controls.

In practice I'd put the domain/collection behind private network controls, enforce encryption, tightly restrict IAM permissions, and avoid returning unnecessary demographics. The Match Service should return something closer to:
```json
{
  "status": "MATCHED",
  "hcid": "HC123456",
  "score": 98.2,
  "matchRule": "DEMOGRAPHIC_V3"
}
```

rather than returning complete member documents.

## 14. What I would prototype first

We should start with perhaps 1–5 million representative enrollment records, while preserving the real AOR-size distribution.

Test these cases:
```
Scenario	ExpectedExact name + DOB	Match
Typo in first name	Match
Typo in last name	Match
Exact name, different DOB	No/low confidence
Same name/DOB under different AOR	Never cross AOR
Two people same name + DOB under same AOR	Ambiguous
Missing DOB	Lower-confidence rules
Changed ZIP	Should still be candidate
Nickname such as Bob/Robert	Candidate if business permits
AOR transfer	Correct effective ownership
Duplicate HCIDs	Ambiguous/manual resolution
```
Then measure:
```
p50 latency
p95 latency
p99 latency

candidate count/query

auto-match %
no-match %
ambiguous-match %

false-positive rate
false-negative rate

index freshness lag
```

For this use case, false-positive matching should be a first-class metric, not merely search latency.

## Recommended design

We should narrow the architecture to:

Exact matching path
```
Prospect
    ↓
Normalize
    ↓
AOR + deterministic match keys
    ↓
OpenSearch exact lookup
    ↓
HCID
```

Fallback path
```
Exact match failed
    ↓
AOR-restricted candidate search
    ↓
OpenSearch fuzzy name search
    ↓
Top ~5 candidates
    ↓
Application/business scoring
    ↓
MATCH / AMBIGUOUS / NO MATCH
``

- The key architectural point is that Amazon OpenSearch Service</Amazon OpenSearch Service> is primarily the high-speed candidate retrieval engine. 
- The Matching Service should own the identity-resolution decision. 
- That separation gives you the performance of a search engine without making business-critical HCID linkage dependent on opaque search relevance scores.