# OpenSearch Neural Search Plugin
- The Neural Search plugin in Amazon OpenSearch Service simplifies semantic retrieval by integrating directly with foundation models hosted on Amazon Bedrock (e.g., Amazon Titan Text Embeddings V2 or Cohere Embed). 
- Instead of manually generating embeddings via an external Lambda function or custom ETL pipeline prior to ingestion and querying, OpenSearch calls Amazon Bedrock internally via an ML Commons Connector.
- The end-to-end implementation workflow requires four sequential phases: 
  - IAM setup,
  - ML connector configuration, 
  - pipeline/index definition, and 
  - search execution.

## Phase 1: IAM Role & Security Configuration
- OpenSearch Service requires an IAM role with permission to invoke models in Amazon Bedrock.
### 1. Create an IAM Role for OpenSearch (OpenSearchBedrockAccessRole)
Attach the following trust and permissions policies:

 * **Trust Relationship**:
 ```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "es.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```
 * ***Permissions Policy***:
 ```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "bedrock:InvokeModel",
      "Resource": "arn:aws:bedrock:*::foundation-model/amazon.titan-embed-text-v2:0"
    }
  ]
}
```
### 2. Map IAM Role in OpenSearch Dashboards
 * Log in to OpenSearch Dashboards and go to Security \rightarrow Roles.
 * Select the ml_full_access role.
 * Choose Mapped users \rightarrow Manage mapping, and add the ARN of your signing IAM role or user under Backend roles.

## Phase 2: Create the Bedrock ML Connector & Register the Model
Use the OpenSearch ML Commons API to define a connector pointing to Bedrock.
### 1. Create the ML Connector
```
POST /_plugins/_ml/connectors/_create
```
```json
{
  "name": "Amazon Bedrock Titan Embeddings V2 Connector",
  "description": "Connector to invoke Titan Text Embeddings V2 on Amazon Bedrock",
  "version": "1",
  "protocol": "aws_sigv4",
  "parameters": {
    "region": "us-east-1",
    "service_name": "bedrock"
  },
  "credential": {
    "roleArn": "arn:aws:iam::123456789012:role/OpenSearchBedrockAccessRole"
  },
  "actions": [
    {
      "action_type": "predict",
      "method": "POST",
      "url": "https://bedrock-runtime.us-east-1.amazonaws.com/model/amazon.titan-embed-text-v2:0/invoke",
      "headers": {
        "content-type": "application/json",
        "x-amz-content-sha256": "required"
      },
      "request_body": "{\"inputText\": \"${parameters.inputText}\", \"dimensions\": 512, \"normalize\": true}",
      "pre_process_function": "connector.pre_process.bedrock.embedding",
      "post_process_function": "connector.post_process.bedrock.embedding"
    }
  ]
}
```
Take note of the returned connector_id.

### 2. Register and Deploy the Model
Register the connector as an active model within OpenSearch:
```
POST /_plugins/_ml/models/_register
```
```json
{
  "name": "Titan-Text-Embed-V2",
  "function_name": "remote",
  "description": "Amazon Bedrock Titan Embedding model",
  "connector_id": "<your-connector-id>"
}
```

Take note of the returned task_id and poll GET /_plugins/_ml/tasks/<task_id> to retrieve the model_id.
Deploy the registered model:
POST /_plugins/_ml/models/<your-model-id>/_deploy

## Phase 3: Ingestion Pipeline & k-NN Index Setup
Configure OpenSearch to convert raw text documents into vector embeddings automatically upon indexing.
### 1. Define an Ingest Pipeline with text_embedding
PUT /_ingest/pipeline/bedrock-text-embedding-pipeline
```json
{
  "description": "Generates embeddings using Bedrock on document ingestion",
  "processors": [
    {
      "text_embedding": {
        "model_id": "<your-model-id>",
        "field_map": {
          "text_content": "embedding_vector"
        }
      }
    }
  ]
}
```
### 2. Create the k-NN Index
Configure the index with the hnsw method and match the dimensionality set in the connector (e.g., 512 dimensions for Titan V2):
PUT /enterprise-knowledge-base
```json
{
  "settings": {
    "index.knn": true,
    "default_pipeline": "bedrock-text-embedding-pipeline"
  },
  "mappings": {
    "properties": {
      "text_content": { "type": "text" },
      "embedding_vector": {
        "type": "knn_vector",
        "dimension": 512,
        "method": {
          "name": "hnsw",
          "space_type": "innerproduct",
          "engine": "faiss",
          "parameters": {
            "ef_construction": 256,
            "m": 16
          }
        }
      },
      "metadata": {
        "properties": {
          "department": { "type": "keyword" }
        }
      }
    }
  }
}
```
### 3. Ingest Raw Documents
Send raw text directly—the ingest pipeline invokes Bedrock and populates embedding_vector automatically:
POST /enterprise-knowledge-base/_doc/1
```json
{
  "text_content": "Amazon S3 server-side encryption with AWS KMS keys (SSE-KMS) enforces audit trails in AWS CloudTrail.",
  "metadata": {
    "department": "compliance"
  }
}
```

## Phase 4: Querying with Neural Search
When executing queries, pass plain text using the neural query clause. The plugin calls Bedrock behind the scenes to convert query_text into a vector before traversing the HNSW graph:
### 1. Standard Neural Search
GET /enterprise-knowledge-base/_search
```json
{
  "_source": ["text_content", "metadata"],
  "query": {
    "neural": {
      "embedding_vector": {
        "query_text": "How do I enforce KMS encryption in S3?",
        "model_id": "<your-model-id>",
        "k": 5
      }
    }
  }
}
```

### 2. Advanced Hybrid Search (BM25 + Neural Vector + RRF)
For production RAG pipelines, combine lexical search and semantic neural search using a search pipeline configured with Reciprocal Rank Fusion (RRF) to optimize relevance:
 * Search Pipeline Definition:
PUT /_search/pipeline/rrf-hybrid-pipeline
```json
{
  "phase_results_processors": [
    {
      "score-ranker-processor": {
        "combination": {
          "technique": "rrf",
          "parameters": {
            "rank_constant": 60
          }
        }
      }
    }
  ]
}
```
 * Hybrid Search Query Execution:
GET /enterprise-knowledge-base/_search?search_pipeline=rrf-hybrid-pipeline
```json
{
  "_source": ["text_content", "metadata"],
  "query": {
    "hybrid": {
      "queries": [
        {
          "match": {
            "text_content": "SSE-KMS S3 encryption audit trail"
          }
        },
        {
          "neural": {
            "embedding_vector": {
              "query_text": "SSE-KMS S3 encryption audit trail",
              "model_id": "<your-model-id>",
              "k": 10,
              "filter": {
                "term": { "metadata.department": "compliance" }
              }
            }
          }
        }
      ]
    }
  }
}
```

## Key Operational Considerations
 * **Dimensionality Alignment**: When using Matryoshka-capable models like Amazon Titan Text Embeddings V2, verify that the dimensions specified in your connector payload (e.g., 256, 512, or 1024) match the dimension mapped on the knn_vector field exactly.
 * **Distance Metric Selection**: Because Titan V2 vectors are pre-normalized ("normalize": true), use space_type: "innerproduct" rather than cosinesimil. Dot product produces identical rankings while skipping square root calculations, saving CPU during HNSW graph hops.
 * **Shard Size Management**: Keep vector shards between 10 GB and 30 GB to prevent memory circuit breaker errors (knn.memory.circuit_breaker.limit), as HNSW graphs must reside entirely in RAM.
