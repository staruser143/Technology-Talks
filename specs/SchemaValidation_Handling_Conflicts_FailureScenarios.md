
# Why use MongoDB's Schema Validation?

* One of the key advantages of NoSQL databases, including MongoDB, is their flexibility and ability to handle schema evolution more gracefully than * traditional relational databases.
* While MongoDB's schema validation does introduce some constraints, it doesn't necessarily negate the benefits of NoSQL.
* Instead, it provides a way to strike a balance between flexibility and data integrity.

### Balancing Flexibility and Integrity

1. **Optional Validation**:
   - We have the option to apply validation rules selectively.

     For example, we can enforce validation only on specific fields or documents that require strict integrity, while leaving other parts of the schema more flexible.
   - We can also set the validation action to `warn` instead of `error`, allowing documents that don't fully comply with the schema to be inserted while still generating a warning.

2. **Partial Schemas**:
   - We can define partial schemas, where only critical fields are validated, and other fields remain flexible.
   - This allows us to ensure the integrity of key data points without imposing rigid constraints on the entire document.

**Example**:
```json
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": ["quoteId", "status", "version"],
    "properties": {
      "quoteId": {
        "bsonType": "string",
        "description": "must be a string and is required"
      },
      "status": {
        "bsonType": "string",
        "enum": ["Saved", "Updated", "Approved", "Declined"],
        "description": "must be one of the predefined statuses and is required"
      },
      "version": {
        "bsonType": "int",
        "minimum": 1,
        "description": "must be an integer and is required"
      }
      // Other fields can remain flexible
    }
  }
}
```

3. **Dynamic Schemas**:
   - MongoDB allows for dynamic schemas, meaning we can add new fields to documents without requiring a predefined schema.
   - This flexibility is retained even with schema validation, as we can specify which fields are validated and leave others open.

4. **Granular Control**:
   - We can apply schema validation at the collection level, allowing different collections within the same database to have varying levels of strictness.
   - This enables us to tailor validation rules based on the specific needs of each collection.

5. **Evolving Validation Rules**:
   - We can evolve the validation rules over time. As the application evolves, we can update the schema validation rules to accommodate new requirements.
   - MongoDB's schema validation is designed to be flexible and adaptable.

### Practical Considerations

- **Use Cases**: Consider the specific use cases and requirements of the application. For critical data where integrity is paramount, stricter validation may be justified. For other areas where flexibility is more important, we can relax the validation rules.
- **Iterative Approach**: Start with more flexible validation and gradually introduce stricter rules as needed. This iterative approach allows us to balance flexibility and integrity effectively.

By leveraging MongoDB's schema validation in a thoughtful and strategic manner, we can maintain the flexibility of a NoSQL database while ensuring the integrity of the critical data. 
This balance helps us harness the strengths of both worlds without being overly restrictive.


# Handling Conflicts

The decision to increment the version field every time a document is updated, rather than only when new fields are added or existing fields are removed, is rooted in the principles of data consistency and conflict resolution, especially in distributed systems.

### Reasons for Incrementing the Version Field on Every Update

1. **Optimistic Concurrency Control**: 
   - In systems where multiple processes or users might update the same document concurrently, versioning helps prevent conflicts. By incrementing the version with every update, the system ensures that changes are based on the most recent version of the document.
   - If two processes try to update the same document simultaneously, they will both check the version number. If the version number has changed since the process read the document, it will know its update is outdated and can handle the conflict appropriately.

2. **Auditing and Tracking Changes**:
   - Incrementing the version field with each update provides a straightforward way to track changes to the document over time. This is useful for auditing purposes, allowing us to see how the document has evolved with each change.
   - It provides a mechanism to rollback or replay changes if needed, which is crucial in systems where data accuracy and history are important.

3. **Event Sourcing Compatibility**:
   - In event-sourced systems, every change to the state is recorded as an event. Incrementing the version number aligns with the principles of event sourcing, ensuring that each state change is captured and can be replayed to reconstruct the state.

4. **Schema Evolution**:
   - While the primary purpose of versioning might be to handle schema changes, incrementing the version number on every update provides flexibility. It allows the system to manage incremental changes, even if they are minor updates to existing fields.
   - This approach ensures that the system remains adaptable to both small updates and significant schema changes.

5. **Consistency and Integrity**:
   - By consistently incrementing the version field, the system enforces a rigorous method of ensuring data consistency and integrity. It minimizes the risk of stale writes or data corruption due to concurrent updates.

### Example Update Logic

Here’s an example of how we might handle an update with versioning in a MongoDB schema:

```javascript
const updateQuote = async (quoteId, updateData, currentVersion) => {
  const result = await db.collection('quotes').updateOne(
    { quoteId: quoteId, version: currentVersion },
    { $set: updateData, $inc: { version: 1 }, $currentDate: { updatedAt: true } }
  );

  if (result.matchedCount === 0) {
    throw new Error("Conflict detected: Quote has been updated by another process.");
  }
};
```

In this example:
- The update operation includes a condition to check the current version.
- If the version has changed since the document was read, the update will fail, and a conflict can be handled.
- The `version` field is incremented with each update to maintain consistency.

By following this approach, we can ensure robust handling of updates and maintain the integrity of the data in a CQRS-based system using MongoDB. 

# Know the Current Version of the Document

Knowing the current version when updating a document is essential to maintain data integrity and avoid conflicts.
Here's how we can handle this in a typical workflow:

### Steps to Manage and Retrieve the Current Version

1. **Read the Document**: When we retrieve the document for update, include the current version field in the query result.

2. **Client-Side Storage**: Store the retrieved version number on the client side or within the session handling the update. 

3. **Conditional Update**: When performing the update, include a condition in the query that matches the current version.

### Example Workflow

1. **Retrieve Document**:

```javascript
const quoteId = "12345";
const quote = await db.collection('quotes').findOne({ quoteId: quoteId });

if (!quote) {
  throw new Error("Quote not found.");
}

const currentVersion = quote.version;
```

2. **Store Current Version**: Store the `currentVersion` locally or within the session that's handling the update.

3. **Perform Conditional Update**:

```javascript
const updateQuote = async (quoteId, updateData, currentVersion) => {
  const result = await db.collection('quotes').updateOne(
    { quoteId: quoteId, version: currentVersion },
    { $set: updateData, $inc: { version: 1 }, $currentDate: { updatedAt: true } }
  );

  if (result.matchedCount === 0) {
    throw new Error("Conflict detected: Quote has been updated by another process.");
  }
};
```

In this example:
- **Retrieve the document**: When we read the document, we include the version field.
- **Store the version**: We keep the current version locally.
- **Conditional update**: When updating, we ensure that the document's version matches the version you read. If it doesn't, it means the document was updated by another process, and we handle the conflict appropriately.

### Practical Considerations

- **Conflict Handling**: When a conflict is detected (i.e., the version in the database doesn't match the version you have), we can decide how to handle it. Options include retrying the operation, informing the user, or merging changes.
- **Optimistic Concurrency Control**: This method is a form of optimistic concurrency control, which assumes multiple processes can often complete without affecting each other and only checks for conflicts when updating the document.

By following these steps, we can ensure that the system handles updates efficiently and maintains data consistency, preventing stale writes or data corruption. 

# Are we maintaining multiple versions fo the Document?
When we talk about versioning in this context, we are not maintaining multiple versions of a document in the collection.
Instead, we are updating the single document with an incremented version number to track changes and maintain data consistency.

### Key Points

- **Single Document Update**: Each time a document is updated, we increment the version field and apply the changes. We are not creating new documents for each version; we are updating the existing document.

- **Version Field**: The version field acts as a marker for the current state of the document. By checking and incrementing this field with each update, we can detect and handle concurrent modifications.

- **Conflict Detection**: This approach helps in detecting conflicts in a distributed system. If another process tries to update the document with an outdated version, it will fail, prompting appropriate conflict resolution.

- **Audit Log**: If we need to maintain a history of changes, you can implement an audit log or event sourcing separately. This would involve storing each change as an event in an event store, rather than maintaining multiple versions of the same document.

### Example

Let's clarify with a simple example:

1. **Initial Document**:
   ```json
   {
     "quoteId": "12345",
     "version": 1,
     "personalInfo": {
       "firstName": "John",
       "lastName": "Doe"
     },
     "status": "Saved"
   }
   ```

2. **First Update** (Change status):
   - Read the document, get `version` (which is 1).
   - Update the document and increment `version` to 2.
   ```json
   {
     "quoteId": "12345",
     "version": 2,
     "personalInfo": {
       "firstName": "John",
       "lastName": "Doe"
     },
     "status": "Updated"
   }
   ```

3. **Second Update** (Change last name):
   - Read the document, get `version` (which is 2).
   - Update the document and increment `version` to 3.
   ```json
   {
     "quoteId": "12345",
     "version": 3,
     "personalInfo": {
       "firstName": "John",
       "lastName": "Smith"
     },
     "status": "Updated"
   }
   ```

In this example, we only have one document in the collection. We increment the version field with each update to ensure that updates are based on the most current state of the document.

# Handle Failure Scenarios during Version Conflicts

When a conditional update using versioning fails, it usually indicates that another process has modified the document since we last read it.
Here are several strategies to handle this scenario:

### 1. **Retry Mechanism**
Implement a retry mechanism to attempt the update again. This involves reading the document afresh, incorporating the latest changes, and attempting the update again. Be cautious about the number of retries to avoid potential infinite loops.

**Example**:
```javascript
const updateWithRetry = async (quoteId, updateData, maxRetries = 3) => {
  let retries = 0;
  while (retries < maxRetries) {
    const quote = await db.collection('quotes').findOne({ quoteId: quoteId });
    const currentVersion = quote.version;

    const result = await db.collection('quotes').updateOne(
      { quoteId: quoteId, version: currentVersion },
      { $set: updateData, $inc: { version: 1 }, $currentDate: { updatedAt: true } }
    );

    if (result.matchedCount > 0) {
      return; // Update successful
    }
    retries++;
  }
  throw new Error("Conflict could not be resolved after multiple attempts.");
};
```

### 2. **Merge Changes**
Merge changes from the conflicting updates manually or automatically.
This can involve combining fields from the current document with the updates we intended to make.

**Example**:
- Read the document again.
- Merge your changes with the latest version.
- Attempt the update again with the new merged data.

### 3. **User Intervention**
Notify the user about the conflict and ask them to manually resolve it. 
This approach is suitable for systems where manual intervention is acceptable and user confirmation is required.

**Example**:
- Inform the user about the conflict.
- Present them with the current version and the intended updates.
- Allow the user to manually resolve the differences and confirm the update.

### 4. **Event Sourcing**
Use event sourcing to maintain a history of changes. Instead of updating the document directly, append events that represent the changes.
Rebuild the current state by replaying events. This ensures that all changes are captured, and conflicts can be resolved by examining the event history.

**Example**:
- Append events like `PersonalInfoUpdated`, `ContactDetailsUpdated`, etc., to an event store.
- Replay events to rebuild the state of the document.

### 5. **Optimistic Locking with Backoff**
Combine optimistic locking with an exponential backoff strategy.
If a conflict is detected, wait for a random time interval before retrying. This reduces the likelihood of immediate retry conflicts.

**Example**:
```javascript
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const updateWithBackoff = async (quoteId, updateData, maxRetries = 3) => {
  let retries = 0;
  while (retries < maxRetries) {
    const quote = await db.collection('quotes').findOne({ quoteId: quoteId });
    const currentVersion = quote.version;

    const result = await db.collection('quotes').updateOne(
      { quoteId: quoteId, version: currentVersion },
      { $set: updateData, $inc: { version: 1 }, $currentDate: { updatedAt: true } }
    );

    if (result.matchedCount > 0) {
      return; // Update successful
    }
    retries++;
    const backoffTime = Math.pow(2, retries) * 100; // Exponential backoff
    await sleep(backoffTime);
  }
  throw new Error("Conflict could not be resolved after multiple attempts.");
};
```

By implementing one or more of these strategies, we can handle version conflicts effectively and maintain data consistency in your CQRS-based system. 
