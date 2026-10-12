Yes, absolutely. You can use standard Amazon Bedrock Knowledge Bases to fetch data from both static document stores and live enterprise internal systems without writing complex code or utilizing AgentCore. [1] 
The platform splits these capabilities based on the structure of the underlying data: Unstructured Data Sources (for static docs) and Structured Data Sources (for live enterprise databases). [2] 
------------------------------
## 1. Fetching from Static Knowledge Bases (Unstructured Data)
For static or slowly changing enterprise documentation, Bedrock uses standard Vector-based RAG. It natively syncs, chunks, embeds, and indexes your files. [1, 3, 4] 

* 
* How it connects: You use fully managed data connectors.
* Supported Repositories: Amazon S3, Microsoft SharePoint, Microsoft OneDrive, Google Drive, Confluence, and a built-in Web Crawler.
* Supported Formats: PDFs, Markdown, TXT, CSV, DOCX, and audio/video files.
* The API Call: Your router invokes RetrieveAndGenerate. Bedrock finds the right document snippet via semantic search, passes it to the LLM, and returns the answer alongside strict source citations. [1, 2, 5, 6, 7, 8] 
* 

## 2. Fetching from Internal/Enterprise Systems (Structured Data)
For live business data residing inside transactional databases or data warehouses, Bedrock uses Text-to-SQL / Text-to-GQL RAG. It avoids vector matching entirely, instead querying data dynamically. [2, 9] 

* 
* How it connects: It integrates natively with enterprise data lakes and analytics engines. [9] 
* Supported Systems: Amazon Redshift, AWS Glue Data Catalog (which bridges to transactional systems), and enterprise platforms like SAP S/4HANA (via AWS Glue OData extractors). [2, 10] 
* The API Call: When a user asks an analytical question ("Show me total sales for Q3 in the Northeast region"), the router sends it to Bedrock. The GenerateQuery API automatically translates the natural language prompt into a precise SQL query, runs it securely against the live database, and interprets the rows of data back into human text. [2, 7] 
* 

------------------------------
## How to Structuring This in Your Router
Because standard Bedrock handles both, you can map multiple specialized knowledge base IDs right into your front-end intent classifier. Your single chat bar routing logic expands gracefully:

                        ┌──────────────────────────────┐
                        │   Intent Classifier Lambda   │
                        └──────────────┬───────────────┘
                                       │
                ┌──────────────────────┼──────────────────────┐
                ▼                      ▼                      ▼
      [Informational Query]   [Analytical Query]       [Multi-Step Action]
       "Read the policy"       "Query live sales"     "Update Salesforce"
                │                      │                      │
                ▼                      ▼                      ▼
     ┌────────────────────┐ ┌────────────────────┐ ┌────────────────────┐
     │  Standard Bedrock  │ │  Standard Bedrock  │ │  Amazon Bedrock  │
     │   Knowledge Base   │ │   Knowledge Base   │ │    AgentCore       │
     │ (Unstructured/S3)  │ │ (Structured/Redshift)│ │  (MicroVM Loop)    │
     └────────────────────┘ └────────────────────┘ └────────────────────┘

## When to Shift a System Fetch to AgentCore Instead
While standard Bedrock is great at reading from enterprise systems, you must pass the request to Bedrock AgentCore if the interaction requires:

   1. Writing/Modifying Data: Doing a POST/PUT request (e.g., updating a customer's record, modifying an invoice status).
   2. REST API-Only Systems: Interfacing with a third-party app that doesn't expose its database layer but requires navigating a secured REST API endpoint using the Model Context Protocol (MCP). [11, 12] 

Are the enterprise systems you need to fetch data from primarily relational databases / data warehouses (like SQL, Redshift, SAP), or are they REST API-driven platforms (like Salesforce, Jira, or ServiceNow)?

[1] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock/latest/userguide/knowledge-base.html)
[2] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock/latest/userguide/kb-how-data.html)
[3] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/amazon-bedrock-knowledge-bases-now-supports-amazon-opensearch-service-managed-cluster-as-vector-store/)
[4] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock/latest/userguide/kb-how-it-works.html)
[5] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/implement-web-crawling-in-knowledge-bases-for-amazon-bedrock/)
[6] [https://aws.amazon.com](https://aws.amazon.com/about-aws/whats-new/2026/06/amazon-bedrock-managed-knowledge-base/)
[7] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock/latest/userguide/kb-how-retrieval.html)
[8] [https://dev.to](https://dev.to/aws-builders/amazon-bedrock-managed-knowledge-base-what-developers-actually-need-to-know-3lnb)
[9] [https://aws.amazon.com](https://aws.amazon.com/blogs/aws/new-amazon-bedrock-capabilities-enhance-data-processing-and-retrieval/)
[10] [https://aws.amazon.com](https://aws.amazon.com/blogs/awsforsap/uncover-new-possibilities-from-your-sap-and-enterprise-data-with-amazon-bedrock-knowledge-bases/)
[11] [https://aws.amazon.com](https://aws.amazon.com/blogs/aws/introducing-amazon-bedrock-managed-knowledge-base-for-faster-more-accurate-enterprise-ai-applications/)
[12] [https://repost.aws](https://repost.aws/questions/QUgBSR-m5LQwy6dB912xYVeg/bedrock-agent-with-real-time-data)
