

## 1. Understanding the requirement

At prospect or quote creation we have something like:
```

Prospect/Quote
 ├─ AOR = AOR-12345
 ├─ First Name = Sridhar
 ├─ Last Name = Balasubramanian
 ├─ DOB = 1975-06-10
 ├─ ZIP = 600045
 └─ maybe Email / Phone / Address
```

Backend enrollment might contain:
```

HCID        AOR        FirstName   LastName          DOB
---------   --------   ----------  ----------------  ----------
HC123456    AOR-12345  Sridhar     Balasubramanian  1975-06-10
HC987654    AOR-78901  Sridhar     Balasubramanian  1975-06-10
```

Even if both records otherwise match, only HC123456 should be considered, because matching crosses neither AOR ownership nor AOR data boundaries.

When sufficient confidence exists, create:

PROSPECT_HCID_LINK
```
prospect_id | quote_id | HCID     | match_type | score | matched_at
------------|----------|----------|------------|-------|------------
P10001      | Q50001   | HC123456 | EXACT      | 1.00  | ...

```
That AOR constraint is actually your biggest performance advantage.

## 2. I would not perform matching against your operational enrollment database

At large scale, avoid:
```
Quote API
   |
   +--> SELECT from enrollment
        WHERE AOR = ?
        AND ...
```

especially once the logic evolves into:
```
lower(first_name)
soundex(last_name)
DOB +/- errors
fuzzy name
address normalization
phone comparison

```
It becomes expensive, difficult to index effectively, and couples prospect creation directly to the enrollment system.

Instead introduce a Member/Client Matching Index.

## Recommended architecture

```
                Enrollment / Member System
                         |
                    CDC / Events
                         |
                         v
                Matching Projection
                         |
                         v
              +----------------------+
              | Client Match Index   |
              |                      |
              | AOR                  |
              | HCID                 |
              | normalizedFirstName  |
              | normalizedLastName   |
              | DOB                  |
              | ZIP                  |
              | phone                |
              | email                |
              | matchKeys...         |
              +----------+-----------+
                         |
                         | AOR-scoped search
                         |
Prospect/Quote API ---> Matching Service
                         |
                   Candidate generation
                         |
                   Match + score
                         |
              +----------+-----------+
              |                      |
           MATCH                  NO MATCH
              |
              v
        Prospect-HCID Link

```
The enrollment system remains the system of record. The match index is a purpose-built read projection.

## 3. The most important optimization: AOR as the partition/routing key

Suppose we have:
```
50 million enrollment records
100,000 AORs
```
We should not search 50 million customers for each prospect.

Conceptually:
```
50M customers
     |
     +---- AOR-001
     +---- AOR-002
     +---- AOR-003
     ...
     +---- AOR-12345 ----> only these records
```

If AOR-12345 owns 2,000 customers, the matching problem has effectively changed from:
```
Find the closest person among 50 million records.
```
to:
```
Find the closest person among the subset belonging to AOR-12345.
```
- This is exactly where technologies such as OpenSearch or Elasticsearch custom routing become attractive. 
- Both support routing documents using a custom routing value, and supplying that value during search limits the query to the associated shard rather than unnecessarily fanning the request across the whole index.

For example:
```
routing = hash(AOR_ID)
```

or logically:
```json
{
  "aorId": "AOR-12345",
  "hcid": "HC123456",
  "firstName": "SRIDHAR",
  "lastName": "BALASUBRAMANIAN",
  "dob": "1975-06-10",
  "zip": "600045"
}

```
- Importantly, We should  not create one physical index/table per AOR. 
- With potentially tens or hundreds of thousands of brokers/AORs, that becomes an operational nightmare.

Use:
```
shared physical index + logical AOR partitioning/routing.
```
Also watch for very large AORs. OpenSearch supports routing a custom routing value across a configured subset of shards, which can help avoid concentrating a disproportionately large routing key on one shard.

## 4. Don't start with fuzzy matching


Recommendation is a  multi-stage matcher:

### Stage 1: Normalize

At ingestion time normalize fields once:
```
" Sridhar "             → SRIDHAR
"Balasubramanian"       → BALASUBRAMANIAN
"(312) 555-1234"        → 3125551234
"sridhar@gmail.com "    → sridhar@gmail.com
"600 045"               → 600045
```

We could also maintain phonetic/transliteration keys where justified.

The trick is:

Do normalization and key generation during indexing, not repeatedly for every quote search.

## 5. Generate deterministic match keys

This is probably your highest-performance option.

For every enrolled person generate several lookup keys.

For HC123456:
```
AOR_ID       = AOR123
HCID         = HC123456

K1 = hash(AOR123 | SRIDHAR | BALASUBRAMANIAN | 19750610)

K2 = hash(AOR123 | BALASUBRAMANIAN | 19750610 | 600045)

K3 = hash(AOR123 | SRIDHAR | 19750610 | 600045)

K4 = hash(AOR123 | normalizedPhone)

K5 = hash(AOR123 | normalizedEmail)

```
When a quote arrives, calculate the same keys.

Instead of:

search millions of records


you're effectively doing:
```
K1 → lookup
K2 → lookup
K3 → lookup
````
...


An indexed key lookup can be extremely fast.

## 6. Then introduce candidate generation

The matcher should not fuzzy-compare the incoming prospect against every record belonging to an AOR.

Instead:

Step 1: Exact candidate generation
For example:
```
AOR AND DOB AND lastName
```

might return:
```
HC123  Sridhar Balasubramanian
HC456  Sridar  Balasubramaniam
HC789  Sridhar Balasubramanian
```

We've reduced thousands/millions of possibilities to perhaps a handful of candidates.

### Step 2: detailed scoring

Only now calculate similarity.

Example:
```
Attribute	Match	WeightAOR	Exact/mandatory	filter
DOB	Exact	30
Last name	96%	25
First name	94%	20
ZIP	Exact	10
Email	Exact	10
Phone	Exact	5
```
Result:
```
HC123456 → 98
HC456789 → 84
HC999999 → 52

```
Then configure thresholds, initially based on validated historical data rather than treating these illustrative numbers as universal:
```
>= 95     AUTO_MATCH
80–94     CANDIDATE / review
< 80      NO_MATCH
```

## 7. Matching waterfall

We could actually implement a matching waterfall, rather than one giant query.

### Level 1: Highly reliable deterministic
```
AOR + government/customer identifier if permitted
AOR + normalized email
AOR + normalized phone
```

If unique:

MATCH immediately

### Level 2: Strong demographic
```
AOR
AND firstName
AND lastName
AND DOB
```
### Level 3: Alternative deterministic

```
AOR
AND lastName
AND DOB
AND ZIP
```

### Level 4: fuzzy
```
Only if Levels 1–3 fail:

AOR = exact
DOB = exact/controlled tolerance
firstName ≈ fuzzy
lastName ≈ fuzzy
ZIP ≈ exact
```

This gives us both performance and explainability.

For healthcare, explainability is particularly valuable. Rather than:

"AI says this is probably HC123456."

we can retain:

MATCH_RULE = AOR+DOB+FUZZY_NAME+ZIP
MATCH_SCORE = 96.4

## 8. Technology options

Several approaches are viable.
```
Option	   Performance	  Fuzzy matching	Complexity	     My view
---------  -- ---------   --------------  ----------------  ----------
HC123456        AOR-12345  Sridhar     Balasubramanian  1975-06-10
RDBMS indexes	Excellent exact	Limited	Low	Good if matching is mostly deterministic
Redis	Excellent	Limited	Medium	Great accelerator
Elasticsearch/OpenSearch	Excellent	Excellent	Medium	Strong fit
Dedicated MDM/identity resolution	Good	Excellent	High	Consider if enterprise identity is broader
Vector DB	Good	Excellent semantic	Medium/High	Not my first choice here
Graph DB	Variable	Useful relationships	High	Not needed for basic matching
```


## Recommended approach

Given the requirements, we would lean toward:
```
OpenSearch / Elasticsearch + deterministic match keys + fuzzy fallback.
```
Elasticsearch's documented routing mechanism specifically reduces search impact by directing searches toward shards associated with the supplied routing values.

OpenSearch similarly routes search traffic to primary/replica shards and supports adaptive replica selection based partly on previous execution time, network latency, and search-thread queue size.


Classic entity-resolution techniques are safer and far more explainable:
```
normalization
exact matching
blocking/candidate generation
edit distance
Jaro-Winkler
phonetic keys
rule/weighted scoring
```
Vector/ANN matching could be a later fallback for especially messy names/addresses, but it wouldn't be my foundation.

## 10. Link record is separate from the matching index

The transactional association should live somewhere durable:
```
PROSPECT_CLIENT_LINK

link_id
prospect_id
quote_id
aor_id
hcid
match_status
match_method
match_score
match_version
created_at
```

For example:
```
P100
Q200
AOR123
HC98765
AUTO_MATCHED
DEMOGRAPHIC_EXACT
100
MATCH_RULE_V3
2026-11-17T...
```

The match_version is particularly useful. If we later change matching rules, we can explain exactly why an earlier prospect was linked.

## 11. One tricky AOR problem to solve explicitly

There is an important edge case in what we've described:

What happens when the AOR changes?

Suppose:
```
HC12345

Jan–Jun → AOR-A
Jul–Dec → AOR-B

```
If the incoming quote belongs to AOR-B, should:
```
AOR-B + Name + DOB
```

find the enrollment originally created under AOR-A?

If business rules say no, AOR is a strict security/matching boundary.

If business rules say yes after an AOR transfer, then AOR ownership needs effective dating:
```
HCID_AOR_ASSIGNMENT

HCID
AOR_ID
effective_from
effective_to
status
```

and the matching index must stay synchronized when an AOR assignment changes.

We would treat this as an explicit architecture/business decision rather than simply embedding AOR_ID permanently inside the customer record.

## 12. Recommended target architecture

For the scenario we should implement:
```
                        Enrollment
                            |
                            | CDC / domain events
                            v
                     Kafka / Event Bus
                            |
                    Indexing Consumer
                            |
              normalize + generate match keys
                            |
                            v
                 Client Matching Index
                 OpenSearch/Elasticsearch
                   routing = AOR_ID
                            ^
                            |
                 candidate retrieval
                            |
Quote/Prospect ---> Match API
                      |
                 normalize input
                      |
              deterministic keys
                      |
                  candidates
                      |
                scoring rules
                      |
          +-----------+------------+
          |           |            |
       MATCH       REVIEW       NO MATCH
          |
          v
   Prospect-HCID Link DB
```

The direct path can realistically be designed for sub-second response, but we would set the exact SLO only after benchmarking your actual AOR size distribution, match rules, and concurrency. The architecture keeps the quote flow independent of the enrollment system's query performance.

## Architecture decision I would make

We would choose:

```
AOR-scoped blocking + normalized deterministic match keys + OpenSearch/Elasticsearch candidate search + weighted fuzzy scoring + a durable Prospect/Quote ↔ HCID link table.
```
Not:

```
global fuzzy search across the full customer population.
```

The key design principle is"filter/block first, score second." AOR gives us a very strong first blocking dimension, then DOB/name/ZIP/email/phone keys reduce that even further. That combination is what makes real-time matching viable at high volume.