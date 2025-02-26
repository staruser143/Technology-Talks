# Schema Design for WRITE Model

Designing a schema for the WRITE model in a CQRS-based system with MongoDB involves several best practices to ensure efficiency and scalability. Here are some key considerations:

1. **Understand the Data Access Patterns**: Design the schema based on how the application will access the data. This includes understanding the read and write patterns, as well as the relationships between different data entities.

2. **Use Embedded Documents**: For related data that is frequently accessed together, consider using embedded documents. This can reduce the need for joins and improve performance.

3. **Denormalize Data**: Unlike relational databases, MongoDB benefits from denormalization. This means duplicating data where necessary to optimize read performance.

4. **Schema Versioning**: Implement schema versioning to handle changes over time. This allows us to evolve the schema without disrupting existing data.

5. **Indexing**: Use indexes to improve query performance. Be mindful of the types of indexes we create, as they can impact write performance.

6. **Partitioning**: For large datasets, consider partitioning the data to distribute the load and improve performance.

7. **Avoid Over-Normalization**: While normalization is a common practice in relational databases, over-normalizing in MongoDB can lead to performance issues.
8.  Aim for a balance that suits our application's needs.

9. **Use Schema Validation**: Leverage MongoDB's schema validation to enforce data integrity and ensure that the documents adhere to the defined schema.

10. **Optimize for Write Operations**: Since this is the WRITE model, prioritize schema designs that optimize write operations. This might involve trade-offs with read performance, but it's essential for maintaining high write throughput.

11. **Monitor and Adjust**: Continuously monitor the database performance and be prepared to adjust the schema as the application evolves.

By following these best practices, we can design a robust and efficient schema for the WRITE model in the CQRS-based system using MongoDB.

: [MongoDB Schema Design Best Practices](https://www.mongodb.com/developer/products/mongodb/mongodb-schema-design-best-practices/)


Designing the WRITE model for a Quoting domain in a CQRS-based system requires careful consideration to ensure it handles multiple commands efficiently. Here’s a potential structure for the schema in MongoDB, which can accommodate various commands like `savePersonalInfo`, `saveContactDetails`, `saveDependentInfo`, `saveEmploymentInfo`, `saveQuote`, `updateQuote`, `addDependentToQuote`, and `addProductToQuote`.

### MongoDB Schema Design for Quote 

**Quote Document Structure:**

```json
{
  "quoteId": "string",
  "personalInfo": {
    "firstName": "string",
    "lastName": "string",
    "dob": "string",
    "gender": "string"
  },
  "contactDetails": {
    "email": "string",
    "phone": "string",
    "address": {
      "street": "string",
      "city": "string",
      "state": "string",
      "postalCode": "string",
      "country": "string"
    }
  },
  "employmentInfo": {
    "employer": "string",
    "position": "string",
    "salary": "number",
    "employmentStatus": "string"
  },
  "dependents": [
    {
      "dependentId": "string",
      "firstName": "string",
      "lastName": "string",
      "dob": "string",
      "relationship": "string"
    }
  ],
  "products": [
    {
      "productId": "string",
      "productName": "string",
      "productType": "string",
      "premium": "number",
      "coverageAmount": "number"
    }
  ],
  "status": "string",
  "createdAt": "date",
  "updatedAt": "date"
}
```

### Best Practices

- **Validation**: Use schema validation to ensure data integrity. Validate each command before processing.
- **Indexing**: Index fields that are frequently queried, such as `quoteId`, to improve query performance.
- **Partitioning**: For large datasets, consider partitioning the Quotes collection by a logical shard key, such as `quoteId`.

By following these guidelines, we can create a well-structured WRITE model that efficiently handles multiple commands in the Quoting domain.


# Including Version and Status Field
Incorporating both a version field and a status field in the Quote schema enables accommodating schema evolution and track the status of each quote. 


### Enhanced MongoDB Schema Design

**Quote Document Structure:**

```json
{
  "quoteId": "string",
  "personalInfo": {
    "firstName": "string",
    "lastName": "string",
    "dob": "string",
    "gender": "string"
  },
  "contactDetails": {
    "email": "string",
    "phone": "string",
    "address": {
      "street": "string",
      "city": "string",
      "state": "string",
      "postalCode": "string",
      "country": "string"
    }
  },
  "employmentInfo": {
    "employer": "string",
    "position": "string",
    "salary": "number",
    "employmentStatus": "string"
  },
  "dependents": [
    {
      "dependentId": "string",
      "firstName": "string",
      "lastName": "string",
      "dob": "string",
      "relationship": "string"
    }
  ],
  "products": [
    {
      "productId": "string",
      "productName": "string",
      "productType": "string",
      "premium": "number",
      "coverageAmount": "number"
    }
  ],
  "marketCategory": "string", // e.g., Individual or Medicare
  "location": {
    "state": "string",
    "zipCode": "string"
  },
  "status": "string", // e.g., Saved, Updated, Approved, Declined
  "version": "number",
  "createdAt": "date",
  "updatedAt": "date"
}
```

### Best Practices for Versioning and Status

1. **Version Field**: 
   - Include a `version` field to manage schema evolution. Each time a document is updated, increment the version number. This helps in tracking changes and handling concurrency.
   - Use optimistic concurrency control to handle updates, ensuring that changes are based on the correct document version.

2. **Status Field**:
   - Include a `status` field to track the lifecycle of the quote. Define a set of statuses such as `Saved`, `Updated`, `Approved`, `Declined`, etc.
   - Implement state transitions to enforce valid status changes, ensuring the quote follows the correct workflow.

3. **Handling Market Category and Location**:
   - Include fields for `marketCategory`, `state`, and `zipCode` to capture dependencies on market category and location.
   - Use these fields to apply business rules and validation specific to each category or location.

This enhanced schema design ensures that the system can manage schema evolution, and track the status of each quote. 

# Including _Id and Domain Specific ID field

In MongoDB, each document automatically has a unique `_id` field, which serves as the primary key.
We have a couple of options when it comes to storing the domain/aggregate ID:

### Option 1: Use `_id` for Domain/Aggregate ID
We can use the default `_id` field to store the domain-specific or aggregate ID. 
This approach simplifies the schema by leveraging the existing unique identifier provided by MongoDB.

**Pros**:
- No need for an additional field.
- Ensures uniqueness by default.
- Simplifies indexing and querying.

**Example**:
```json
{
  "_id": "domainSpecificId",
  "personalInfo": {
    "firstName": "John",
    "lastName": "Doe"
  },
  // Other fields...
}
```

### Option 2: Use a Separate Field for Domain/Aggregate ID
We can add a separate field for the domain-specific or aggregate ID while keeping the `_id` field. 
This approach provides more flexibility, especially if we need to distinguish between internal MongoDB IDs and domain-specific IDs.

**Pros**:
- Clear separation between MongoDB's internal ID and the domain ID.
- Flexibility in managing different types of IDs.

**Example**:
```json
{
  "_id": "mongodbGeneratedId",
  "quoteId": "domainSpecificId",
  "personalInfo": {
    "firstName": "John",
    "lastName": "Doe"
  },
  // Other fields...
}
```

### Considerations
- **Uniqueness**: Ensure that the domain-specific ID field (`quoteId` in the example) is unique. We can create a unique index on this field to enforce uniqueness.
- **Query Performance**: If we frequently query by the domain-specific ID, consider creating an index on that field to improve query performance.
- **Clarity**: Using a separate field can make the schema more explicit and clear, especially for developers who need to understand the distinction between internal and domain-specific IDs.

### Example Index Creation
If we choose to use a separate field, we can create a unique index on the domain-specific ID field:

```javascript
db.collection('quotes').createIndex({ quoteId: 1 }, { unique: true });
```

### Recommendation
If the primary concern is simplicity and we don't have a strong need to differentiate between MongoDB's internal ID and the domain ID, using `_id` for the domain-specific ID is a straightforward approach. However, if we anticipate the need for clear separation or additional flexibility, using a separate field is a viable option.


# Schema Evolution

Evolving a schema while maintaining compatibility for both old and new clients can be a challenge. 
Here are some strategies to handle schema evolution and ensure smooth transitions:

### Schema Evolution Strategies

1. **Backward Compatibility**:
   - Design the new schema changes to be backward compatible. This means that existing clients can continue to operate with the old schema while new clients use the updated schema.
   - Use default values for new fields so that old clients can ignore them.

**Example**:
```json
{
  "quoteId": "domainSpecificId",
  "personalInfo": {
    "firstName": "John",
    "lastName": "Doe"
  },
  "newField": "defaultValue", // New field with a default value
  "version": 1
}
```

2. **Versioning in Data**:
   - Include a `schemaVersion` field in the documents. This helps in identifying which version of the schema the document adheres to.
   - Write code that handles different schema versions appropriately.

**Example**:
```json
{
  "quoteId": "domainSpecificId",
  "personalInfo": {
    "firstName": "John",
    "lastName": "Doe"
  },
  "newField": "defaultValue",
  "version": 1,
  "schemaVersion": 2 // Indicates the schema version
}
```

3. **Transformations and Migrations**:
   - Implement transformations or migrations to convert data from the old schema to the new schema. This can be done in real-time as data is accessed or through a batch process.
   - Store data in a format that can be easily transformed or migrated to the new schema.

**Example**:
- **Real-time Transformation**: When an old client accesses the data, transform it to the old schema format on-the-fly.
- **Batch Migration**: Run a background process to migrate all existing documents to the new schema.

4. **Feature Flags**:
   - Use feature flags to control the rollout of new schema features. This allows us to gradually introduce new features to a subset of clients before enabling them for everyone.
   - Monitor the usage and performance of the new schema under controlled conditions.

5. **API Versioning**:
   - Version the APIs to handle different schema versions. Old clients can continue to use the old API version, while new clients can use the updated API version.
   - This allows us to maintain separate code paths for different schema versions.

**Example**:
- `/api/v1/quotes` for the old schema.
- `/api/v2/quotes` for the new schema.

### Handling Schema Evolution in Practice

1. **Read and Write Handlers**:
   - Implement separate read and write handlers for different schema versions. These handlers can convert data to the appropriate schema format based on the `schemaVersion` field.

2. **Schema Validation**:
   - Use MongoDB's schema validation to enforce data integrity for different schema versions. This ensures that documents adhere to the correct schema based on their version.

3. **Documentation**:
   - Keep detailed documentation of schema changes and versioning strategies. This helps developers understand how to handle different schema versions and ensures consistency across the system.

By adopting these strategies,we can evolve the schema smoothly, accommodate both old and new clients, and maintain data integrity. 

# MongoDB Schema Validation

MongoDB's schema validation is a powerful feature that allows us to enforce rules on the structure and content of documents within a collection. This helps ensure data integrity by ensuring that documents adhere to a predefined schema.

### Key Features of MongoDB Schema Validation

1. **JSON Schema**:
   - MongoDB uses JSON Schema syntax to define validation rules. These rules can specify the required fields, field types, allowed values, and more.
   - We can apply validation rules to individual fields or to entire documents.

2. **Validation Actions**:
   - We can configure the validation action to either `error` or `warn`. When set to `error`, documents that do not meet the validation criteria are rejected. When set to `warn`, invalid documents generate a warning but are still allowed.

3. **Validation Levels**:
   - The validation level can be set to `strict` or `moderate`. In `strict` mode, all validation rules are enforced for all documents. In `moderate` mode, validation rules are only enforced for documents that are being inserted or updated.

### Example: Enforcing a Schema

Let's say we have a `quotes` collection, and we want to enforce a schema with specific validation rules.

**Schema Definition**:

```json
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": ["quoteId", "personalInfo", "status", "version"],
    "properties": {
      "quoteId": {
        "bsonType": "string",
        "description": "must be a string and is required"
      },
      "personalInfo": {
        "bsonType": "object",
        "required": ["firstName", "lastName"],
        "properties": {
          "firstName": {
            "bsonType": "string",
            "description": "must be a string and is required"
          },
          "lastName": {
            "bsonType": "string",
            "description": "must be a string and is required"
          }
        }
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
      },
      "marketCategory": {
        "bsonType": "string",
        "enum": ["Individual", "Medicare"],
        "description": "must be 'Individual' or 'Medicare'"
      },
      "location": {
        "bsonType": "object",
        "required": ["state", "zipCode"],
        "properties": {
          "state": {
            "bsonType": "string",
            "description": "must be a string and is required"
          },
          "zipCode": {
            "bsonType": "string",
            "description": "must be a string and is required"
          }
        }
      }
    }
  }
}
```

**Applying the Schema Validation**:

We can apply this schema validation when creating the collection or by modifying an existing collection.

**Create Collection with Validation**:
```javascript
db.createCollection("quotes", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["quoteId", "personalInfo", "status", "version"],
      properties: {
        quoteId: { bsonType: "string", description: "must be a string and is required" },
        personalInfo: {
          bsonType: "object",
          required: ["firstName", "lastName"],
          properties: {
            firstName: { bsonType: "string", description: "must be a string and is required" },
            lastName: { bsonType: "string", description: "must be a string and is required" }
          }
        },
        status: { bsonType: "string", enum: ["Saved", "Updated", "Approved", "Declined"], description: "must be one of the predefined statuses and is required" },
        version: { bsonType: "int", minimum: 1, description: "must be an integer and is required" },
        marketCategory: { bsonType: "string", enum: ["Individual", "Medicare"], description: "must be 'Individual' or 'Medicare'" },
        location: {
          bsonType: "object",
          required: ["state", "zipCode"],
          properties: {
            state: { bsonType: "string", description: "must be a string and is required" },
            zipCode: { bsonType: "string", description: "must be a string and is required" }
          }
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
```

**Modify Existing Collection**:
```javascript
db.runCommand({
  collMod: "quotes",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["quoteId", "personalInfo", "status", "version"],
      properties: {
        quoteId: { bsonType: "string", description: "must be a string and is required" },
        personalInfo: {
          bsonType: "object",
          required: ["firstName", "lastName"],
          properties: {
            firstName: { bsonType: "string", description: "must be a string and is required" },
            lastName: { bsonType: "string", description: "must be a string and is required" }
          }
        },
        status: { bsonType: "string", enum: ["Saved", "Updated", "Approved", "Declined"], description: "must be one of the predefined statuses and is required" },
        version: { bsonType: "int", minimum: 1, description: "must be an integer and is required" },
        marketCategory: { bsonType: "string", enum: ["Individual", "Medicare"], description: "must be 'Individual' or 'Medicare'" },
        location: {
          bsonType: "object",
          required: ["state", "zipCode"],
          properties: {
            state: { bsonType: "string", description: "must be a string and is required" },
            zipCode: { bsonType: "string", description: "must be a string and is required" }
          }
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
```

### Benefits of Schema Validation

- **Data Integrity**: Ensures that documents conform to the defined structure and constraints, preventing invalid data from being stored.
- **Consistency**: Helps maintain a consistent data model, making it easier to manage and query data.
- **Error Prevention**: Catches and prevents errors at the database level, reducing the risk of data corruption and inconsistencies.

By using MongoDB's schema validation, we can enforce rules on the data, ensuring that it adheres to the expected structure and constraints, thus maintaining data integrity and consistency.


# Using Indexes on other fields

There are several other fields in the MongoDB schema that we might want to consider indexing to improve query performance and efficiency. 
Here are some recommendations:

### Recommended Indexes

1. **Status Field**:
   - Index the `status` field to efficiently query documents based on their status (e.g., `Saved`, `Updated`, `Approved`, `Declined`).
   ```javascript
   db.collection('quotes').createIndex({ status: 1 });
   ```

2. **Version Field**:
   - Index the `version` field to efficiently handle updates and conflict resolution based on the document version.
   ```javascript
   db.collection('quotes').createIndex({ version: 1 });
   ```

3. **Market Category**:
   - Index the `marketCategory` field if we frequently query or filter documents based on the market category (e.g., `Individual`, `Medicare`).
   ```javascript
   db.collection('quotes').createIndex({ marketCategory: 1 });
   ```

4. **Location Fields**:
   - Index the `state` and `zipCode` fields to optimize queries that filter documents by location.
   ```javascript
   db.collection('quotes').createIndex({ "location.state": 1 });
   db.collection('quotes').createIndex({ "location.zipCode": 1 });
   ```

5. **Created At and Updated At Fields**:
   - Index the `createdAt` and `updatedAt` fields to efficiently query documents based on their creation or modification timestamps.
   ```javascript
   db.collection('quotes').createIndex({ createdAt: 1 });
   db.collection('quotes').createIndex({ updatedAt: 1 });
   ```

### Compound Indexes

Depending on the query patterns, we might also consider creating compound indexes.
Compound indexes allow us to index multiple fields together and can significantly improve the performance of certain queries.

**Example Compound Index**:
If we frequently query documents based on `marketCategory` and `state` together, we could create a compound index:
```javascript
db.collection('quotes').createIndex({ marketCategory: 1, "location.state": 1 });
```

### Monitoring and Adjusting Indexes

It's essential to monitor the performance of the indexes and adjust them as needed.
Use MongoDB's tools like `explain()` and the built-in performance monitoring features to analyze query performance and optimize the indexes accordingly.

### Summary

By carefully selecting and creating indexes based on the query patterns, we can significantly improve the performance and efficiency of the MongoDB-based system.
Indexing fields like `status`, `version`, `marketCategory`, `location.state`, `location.zipCode`, `createdAt`, and `updatedAt` can help optimize the queries and ensure a smooth operation.

