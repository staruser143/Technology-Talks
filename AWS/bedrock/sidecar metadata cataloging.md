Sidecar metadata cataloging is an architectural pattern used in Retrieval-Augmented Generation (RAG) pipelines—most notably with Amazon Bedrock Knowledge Bases—to attach structured business and technical attributes to unstructured source files stored in object storage (Amazon S3) without modifying the original document content.
It pairs a primary content file (like a PDF, text, or markdown document) with a companion .metadata.json "sidecar" file residing in the exact same storage prefix.
How It Works
 * Sidecar Placement & Naming Convention:
   For every source document in Amazon S3, a dedicated JSON sidecar file is placed at the identical path by appending .metadata.json to the full filename:
   * Source Document: s3://my-bedrock-kb-bucket/policies/travel-policy.pdf
   * Metadata Sidecar File: s3://my-bedrock-kb-bucket/policies/travel-policy.pdf.metadata.json
 * Schema and Data Types:
   The sidecar file defines a top-level metadataAttributes object containing custom key-value pairs. Supported data types include STRING, NUMBER, BOOLEAN, and STRING_LIST:
   {
  "metadataAttributes": {
    "department": "human_resources",
    "document_type": "policy",
    "effective_year": 2024,
    "is_confidential": false,
    "target_roles": ["manager", "executive"]
  }
}

 * Ingestion & Catalog Hydration:
   When Bedrock syncs the S3 data source, it extracts and chunks the primary document, reads the adjacent .metadata.json file, and binds those structured attributes directly to each generated chunk vector. These metadata fields are then indexed alongside the embeddings in the target vector store (such as Amazon OpenSearch Service/Serverless or Aurora PostgreSQL).
Core Use Cases and Benefits
 * Runtime Pre-Filtering & Scoped Retrieval:
   Allows retrieval APIs (Retrieve or RetrieveAndGenerate) to execute deterministic boolean filters (e.g., department == 'human_resources' AND effective_year >= 2024) concurrently with vector similarity searches. This narrows the search space, cuts retrieval latency, and prevents irrelevant cross-domain documents from entering the prompt context window.
 * Multi-Tenancy & Access Control Isolation:
   Documents can be tagged with attributes like tenant_id, department, or clearance_level. Queries can dynamically inject tenant and role filters, ensuring users only retrieve information they have authorization to access.
 * Lineage Tracking and Citation Attribution:
   Retaining properties like version numbers, author names, canonical URLs, and publication dates allows downstream applications to present structured citations and trace retrieved text chunks back to their exact source origin.
