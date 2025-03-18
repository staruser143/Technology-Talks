Yes, the whenMatched and whenNotMatched options in the $merge stage of MongoDB's aggregation framework work similarly to an upsert operation, but they provide more granular control over how existing and new documents are handled when updating a materialized view.


---

Understanding whenMatched and whenNotMatched

When using $merge, MongoDB determines whether a document in the aggregation result matches an existing document in the target collection based on the _id field (or a custom on field if specified).

🔹 whenMatched (If a matching document exists)

Defines what to do when a document from the aggregation matches an existing document in the target collection.

Options:

"merge" → Updates the existing document with new fields (default behavior, like an update).

"replace" → Replaces the entire document with the new one.

"fail" → Throws an error if a match is found.

"keepExisting" → Keeps the existing document as is (ignores updates).

"pipeline" → Allows applying a transformation pipeline to modify the existing document before merging.



🔹 whenNotMatched (If no match is found)

Defines what to do when a document does not exist in the target collection.

Options:

"insert" → Inserts the new document (default behavior, like an insert).

"discard" → Ignores the new document and does not insert it.

"fail" → Throws an error if no match is found.




---

Example: Using $merge for a Materialized View

Let's say we have an orders collection:

{ "_id": 1, "customerId": 100, "amount": 50 }
{ "_id": 2, "customerId": 100, "amount": 70 }
{ "_id": 3, "customerId": 101, "amount": 30 }

We want to create a materialized view that stores total spending per customer:

db.orders.aggregate([
  { $group: { _id: "$customerId", totalSpent: { $sum: "$amount" } } },
  { 
    $merge: { 
      into: "customer_spending", 
      on: "_id", 
      whenMatched: "merge", 
      whenNotMatched: "insert"
    } 
  }
]);

How It Works:

If a customer already exists in customer_spending, their totalSpent is updated (merged).

If a customer does not exist, a new record is inserted.



---

Comparison to Upsert

Key Differences:

$merge provides more control than upsert, allowing replacements, ignoring updates, or applying transformations.

$merge operates at the aggregation level, while upsert is used with individual updateOne or updateMany operations.



---

Best Practices

Use whenMatched: "merge" for incremental updates in a materialized view.

Use whenMatched: "replace" if you need complete data replacement.

Use whenNotMatched: "discard" to prevent inserting new documents.

Schedule re-execution of the aggregation to refresh the materialized view periodically.


Would you like guidance on scheduling automatic refreshes in MongoDB?

