A .metadata.json file in Amazon Bedrock is a "sidecar" file used primarily with Amazon Bedrock Knowledge Bases to attach structured metadata attributes to source documents stored in Amazon S3.
When Bedrock syncs and ingests a document, it reads the corresponding .metadata.json file, associates those key-value pairs with the vectorized document chunks, and indexes them into the target vector database (such as Amazon OpenSearch Serverless, Amazon Aurora pgvector, or Pinecone).
Key Usages and Benefits
 * Pre-Filtering and Scoped Retrieval:
   * It allows applications querying the Knowledge Base (via Retrieve or RetrieveAndGenerate APIs) to filter search results by explicit attributes (e.g., year == 2024, category == 'finance', or access_level == 'admin').
   * This prevents irrelevant documents from polluting the context window and eliminates cross-domain semantic confusion.
 * Access Control & Multi-Tenancy Isolation:
   * You can tag documents with identifiers such as tenantId or department. Queries can then enforce dynamic metadata pre-filtering to ensure users only retrieve documents matching their clearance level or organization boundary.
 * Source Citation and Attribution Enrichment:
   * Retaining attributes like author names, document revision versions, publication dates, and canonical URLs makes it straightforward to present structured citations and source lineage back to end-users.
Naming Convention and Placement
To associate metadata with a file, the metadata file must reside in the same Amazon S3 path/prefix as the primary document and append .metadata.json to the full document file name:
 * Source Document: s3://my-bedrock-kb-bucket/policies/travel-policy.pdf
 * Metadata Sidecar File: s3://my-bedrock-kb-bucket/policies/travel-policy.pdf.metadata.json
File Structure and Supported Data Types
The file must contain a top-level metadataAttributes object. Supported types include STRING, NUMBER, BOOLEAN, and STRING_LIST:
{
  "metadataAttributes": {
    "document_type": "policy",
    "department": "human_resources",
    "effective_year": 2024,
    "is_confidential": false,
    "target_roles": ["manager", "executive"]
  }
}

Example: Query-Time Metadata Filtering
Once ingested, the Bedrock Agent Runtime client uses these metadata attributes inside retrievalConfiguration filters:
import boto3

bedrock_agent_runtime = boto3.client("bedrock-agent-runtime", region_name="us-east-1")

response = bedrock_agent_runtime.retrieve(
    knowledgeBaseId="KB12345678",
    retrievalQuery={"text": "What is the per diem meal allowance?"},
    retrievalConfiguration={
        "vectorSearchConfiguration": {
            "numberOfResults": 5,
            "filter": {
                "andAll": [
                    {
                        "equals": {
                            "key": "department",
                            "value": "human_resources"
                        }
                    },
                    {
                        "greaterThanOrEquals": {
                            "key": "effective_year",
                            "value": 2024
                        }
                    }
                ]
            }
        }
    }
)

