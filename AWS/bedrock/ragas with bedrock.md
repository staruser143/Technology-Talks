Yes, Ragas can be used directly with Amazon Bedrock.
You can use Bedrock in two ways with Ragas:
 * As the system under evaluation: Assessing responses, agent traces, and context retrieved from Bedrock Knowledge Bases or Bedrock Converse models.
 * As the evaluation judge and embedding engine: Powering Ragas’s internal scoring calculations using Bedrock-hosted foundation models (e.g., Anthropic Claude, Amazon Nova) and embedding models (e.g., Amazon Titan Embeddings).
Integration Architecture
Ragas relies on LangChain wrappers (langchain-aws) to communicate with Bedrock foundation models:
┌────────────────────────────────────────────────────────┐
│                      Ragas Framework                   │
│   (Faithfulness, Relevancy, Context Precision/Recall)  │
└───────────────────────────┬────────────────────────────┘
                            │
               ┌────────────┴────────────┐
               ▼                         ▼
   ┌───────────────────────┐ ┌───────────────────────┐
   │ LangchainLLMWrapper   │ │ LangchainEmbeddings   │
   │ (ChatBedrock)         │ │ (BedrockEmbeddings)   │
   └───────────┬───────────┘ └───────────┬───────────┘
               │                         │
               ▼                         ▼
   ┌─────────────────────────────────────────────────┐
   │                  Amazon Bedrock                 │
   │   - LLM: Claude 3.5 Sonnet / Amazon Nova Pro    │
   │   - Embeddings: Amazon Titan Embeddings V2      │
   └─────────────────────────────────────────────────┘

Implementation Example
To use Bedrock models as both the Judge LLM and the Embedding Model in Ragas:
import boto3
from datasets import Dataset
from langchain_aws import ChatBedrock, BedrockEmbeddings
from ragas.llms import LangchainLLMWrapper
from ragas.embeddings import LangchainEmbeddingsWrapper
from ragas import evaluate
from ragas.metrics import (
    faithfulness,
    answer_relevancy,
    context_precision,
    context_recall,
)

# 1. Initialize AWS Bedrock clients via langchain-aws
boto_session = boto3.Session(region_name="us-east-1")
bedrock_client = boto_session.client("bedrock-runtime")

# Evaluator LLM (e.g., Claude 3.5 Sonnet or Amazon Nova)
evaluator_llm = LangchainLLMWrapper(
    ChatBedrock(
        model_id="anthropic.claude-3-5-sonnet-20240620-v1:0",
        client=bedrock_client,
        model_kwargs={"temperature": 0.0}
    )
)

# Evaluator Embeddings (e.g., Titan Embeddings V2)
evaluator_embeddings = LangchainEmbeddingsWrapper(
    BedrockEmbeddings(
        model_id="amazon.titan-embed-text-v2:0",
        client=bedrock_client
    )
)

# 2. Assign Bedrock to Ragas metrics
metrics = [faithfulness, answer_relevancy, context_precision, context_recall]
for metric in metrics:
    metric.llm = evaluator_llm
    if hasattr(metric, "embeddings"):
        metric.embeddings = evaluator_embeddings

# 3. Prepare evaluation dataset (retrieved from your Bedrock RAG pipeline)
data_samples = {
    "question": ["How do I configure MTU for VPC peering?"],
    "contexts": [["Amazon VPC peering supports an MTU of 1500 bytes for inter-region and up to 9001 bytes for intra-region connections."]],
    "answer": ["VPC peering supports an MTU of 1500 bytes across regions and up to 9001 bytes within the same region."],
    "ground_truth": ["VPC peering MTU is 1500 for inter-region and 9001 for intra-region."]
}
dataset = Dataset.from_dict(data_samples)

# 4. Run Ragas evaluation
results = evaluate(dataset=dataset, metrics=metrics)
print(results.to_pandas())

Ragas vs. Native Bedrock Model Evaluation
Amazon Bedrock provides built-in model evaluation, but Ragas serves distinct needs:
| Feature | Amazon Bedrock Model Evaluation | Ragas with Bedrock |
|---|---|---|
| Primary Scope | Benchmarking standalone foundation models (FMs) and custom models (SFT/CPT). | Evaluating end-to-end RAG pipelines and multi-step agent traces. |
| Retrieval Diagnostics | Basic accuracy/relevance; does not decouple chunk ranking from generator quality. | Specifically isolates retrieval performance via Context Precision and Context Recall. |
| Deployment Mode | Fully managed AWS batch job depositing JSON metrics into S3. | Client-side Python SDK executed inside local scripts, notebooks, or CI/CD pipelines. |
| Bedrock Agent Tracing | Requires manual parsing of orchestration traces. | Provides built-in utility functions (convert_to_ragas_messages) to parse Bedrock Agent execution traces directly. |
