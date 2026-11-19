# Keeping the OpenSearch Index synced with Source Data
- To keep an AWS OpenSearch index in sync with the source data, we need a Change Data Capture (CDC) strategy.
- Since we are using MongoDB as the source, the most reliable approach is to leverage MongoDB's native Change Streams to capture real-time inserts, updates, and deletes, and stream them into OpenSearch.


# Primary architectural options to achieve this sync, ranging from managed AWS services to open-source tools.

## 🔄 Option 1: AWS OpenSearch Ingestion (Managed)

- If the source is Amazon DocumentDB (AWS's MongoDB-compatible database), we can use OpenSearch Ingestion (based on Data Prepper).
- This is a fully managed AWS pipeline that can perform an initial full snapshot of the data and then continuously stream changes (CDC) to the OpenSearch domain .

· **How it works**: 
- We configure a pipeline with the documentdb source plugin and the opensearch sink.
- It handles the CDC infrastructure, providing a low-latency way to keep the index synchronized .
· **Best for**: Teams fully invested in the AWS ecosystem using DocumentDB who want a managed, serverless ingestion pipeline without managing EC2 instances or Lambda functions.

## 🛠️ Option 2: Custom Lambda + Change Streams (Flexible)

- If we are using MongoDB Atlas or a self-managed MongoDB, a common serverless pattern is to use a Lambda function to read from the Change Stream and write to OpenSearch.

· **How it works**:
-  A Lambda function polls the MongoDB change stream (using an event source mapping or a manual loop). 
- It processes each event and makes a signed request to the OpenSearch bulk API to index the document .
**Critical Pitfall**:
-  We must ensure ther Lambda processes the stream in batches and checkpoints the resume token only after OpenSearch acknowledges the write.
**Best for**:
- Custom setups where we need granular control over the transformation logic before indexing.

## 📦 Option 3: Open-Source/Third-Party CDC Tools

- For a more robust, out-of-the-box solution that handles the hard parts of CDC (like snapshot handoff, delete propagation, and exactly-once semantics), we can use specialized tools.

· **VentStream**: 
- An open-source CDC engine that reads MongoDB change streams and syncs to OpenSearch.
- It explicitly handles delete propagation and joined documents, which are often tricky to build manually .
· **mongo-search**:
-  A Java-based library that uses MongoDB Change Streams to sync data to OpenSearch adapters.
-  It supports full re-indexing if the oplog rolls over .
· **Best for**: Teams that want to avoid building custom sync logic but prefer not to use a fully managed SaaS vendor.

## ⚠️ Critical Considerations for the Identity Matching Use Case

Given ther requirement for AOR-scoped identity matching on millions of records, we need to ensure  sync strategy addresses these points:

### 1. Delete Propagation:
- When an enrollment record is deleted in MongoDB, the corresponding document must be deleted from OpenSearch.
- Otherwise, your fuzzy search will return "ghost" matches.
- Ensure the chosen tool supports delete events (Change Streams support this, but we must implement the delete logic).
### 2. Initial Snapshot:
- Before we start streaming changes, we need to backfill the entire existing dataset (millions of documents) into OpenSearch. Most tools (like OpenSearch Ingestion or VentStream) support a "snapshot then stream" mode.

### 3. Idempotency:
- Use the MongoDB _id as the OpenSearch document _id. This ensures that if a change event is processed twice (e.g., after a Lambda retry), it simply overwrites the same document rather than creating a duplicate.

## Recommendation: 
- If we are on DocumentDB, we should consider  OpenSearch Ingestion first for the managed experience.
-  If we are on MongoDB Atlas or self-managed, we should evaluate VentStream or a well-tested Lambda-based pipeline that strictly adheres to checkpointing best practices.