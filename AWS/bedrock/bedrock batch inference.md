# Amazon Bedrock Batch Inference
- Asynchronous processing capability designed for high-volume, non-real-time workloads.
- Instead of sending live request-response queries over HTTP, multiple invocation prompts are bundled into newline-delimited JSON (.jsonl) files in Amazon S3, processed by Bedrock in the background, and returned as output files in S3.
- Workloads running on batch inference typically receive an approximate 50% discount compared to standard on-demand token rates.

## 1. Architectural Workflow
The batch inference lifecycle operates across five core steps:
```
┌─────────────────────────────────┐
│     Client / Ingestion Layer    │
│  (Data Wrangler / Lambda / ETL) │
└────────────────┬────────────────┘
                 │ 1. Upload input.jsonl
                 ▼
┌─────────────────────────────────┐
│       Amazon S3 Input Bucket    │
└────────────────┬────────────────┘
                 │ 2. create_model_invocation_job()
                 ▼
┌─────────────────────────────────┐
│   Amazon Bedrock Batch Service  │ ──► Reads S3 via IAM Role[span_7](start_span)[span_7](end_span)
│   (Asynchronous Compute Engine) │ ──► Runs inference on target model[span_8](start_span)[span_8](end_span)
└────────────────┬────────────────┘
                 │ 3. Writes results & error manifests
                 ▼
┌─────────────────────────────────┐
│      Amazon S3 Output Bucket    │
│   (output.jsonl & errors.jsonl) │
└────────────────┬────────────────┘
                 │ 4. EventBridge status change notification
                 ▼
┌─────────────────────────────────┐
│     Downstream Processing       │
│  (Step Functions / Lambda / DB) │
└─────────────────────────────────┘
```

 * **Input Preparation & S3 Ingestion**: Records are prepared and uploaded as a .jsonl manifest to an Amazon S3 URI.
 * **Job Submission**: A call to bedrock.create_model_invocation_job(...) initiates the task by specifying the target modelId, input S3 prefix, output S3 destination, and execution roleArn.
 * **Execution & Scaling**: Bedrock retrieves the file and queues the tasks across model instances.
 * **Output Generation**: Bedrock deposits generated completions and error files directly into the destination S3 prefix.
 * **Completion Notification**: Bedrock updates the job status (Completed, Failed, or Stopped) and emits status change events to Amazon EventBridge, which triggers downstream notification or post-processing pipelines.

## 2. Data Formats
### Input Schema (.jsonl)
Each line of the input file must be a self-contained JSON object containing a unique identifier and a model input payload matching the target foundation model’s schema (e.g., Anthropic Claude Messages API or Titan payload):
```json
{"recordId": "rec-001", "modelInput": {"anthropic_version": "bedrock-2023-05-31", "max_tokens": 1000, "messages": [{"role": "user", "content": [{"type": "text", "text": "Summarize this document: ..."}]}]}}
{"recordId": "rec-002", "modelInput": {"anthropic_version": "bedrock-2023-05-31", "max_tokens": 1000, "messages": [{"role": "user", "content": [{"type": "text", "text": "Translate this statement into Spanish: ..."}]}]}}
```


 * **recordId**: A distinct identifier used to map output lines back to original inputs.
 * **modelInput**: Matches the parameters expected by InvokeModel or the Converse API format for that specific model family.
Output Schema (.jsonl)
Bedrock outputs a corresponding .jsonl file where each line links back to the request:

```json
{
  "recordId": "rec-001",
  "modelInput": { ... },
  "modelOutput": {
    "id": "msg_01...",
    "type": "message",
    "role": "assistant",
    "content": [{"type": "text", "text": "This document covers..."}],
    "usage": {"input_tokens": 420, "output_tokens": 85}
  }
}
```
 * If an individual record fails (e.g., token limit exceeded or invalid JSON syntax), Bedrock routes the record with error metadata to a separate error file rather than dropping the entire run.


## 3. Service IAM Role Configuration
Bedrock assumes a dedicated IAM service role to interact with S3 on your behalf:
 * Trust Relationship: Allows bedrock.amazonaws.com to perform sts:AssumeRole.
 * Execution Permissions:
   * s3:GetObject and s3:ListBucket on the input S3 bucket/prefix.
   * s3:PutObject on the output S3 bucket/prefix.
   * bedrock:InvokeModel on the target foundation model ARN.
## 4. Code Implementation Example (Boto3)
### Submitting a Batch Inference Job
```python
import boto3

bedrock = boto3.client("bedrock", region_name="us-east-1")

response = bedrock.create_model_invocation_job(
    jobName="nightly-document-summary-job-001",
    roleArn="arn:aws:iam::123456789012:role/BedrockBatchExecutionRole",
    modelId="anthropic.claude-3-5-sonnet-20240620-v1:0",
    inputDataConfig={
        "s3InputDataConfig": {
            "s3Uri": "s3://enterprise-nlp-bucket/batch-input/prompts.jsonl"
        }
    },
    outputDataConfig={
        "s3OutputDataConfig": {
            "s3Uri": "s3://enterprise-nlp-bucket/batch-output/"
        }
    }
)

job_arn = response["jobArn"]
print(f"Batch job started with ARN: {job_arn}")
```

## Monitoring Job State
- Because batch inference does not provide a synchronous request token, pipelines query status via bedrock:getModelInvocationJob or track completion via EventBridge:
```python
status_response = bedrock.get_model_invocation_job(jobIdentifier=job_arn)
print("Current Status:", status_response["status"])  # Submitted | InProgress | Completed | Failed | Stopped
```
## 5. When to Use Batch vs. Other Inference Modes
| Deployment Pattern | Latency Profile | Billing Structure | Best Suited For |
|---|---|---|---|
| Batch Inference | Minutes to hours (asynchronous) | Pay-per-token with ~50% discount | Offline bulk document processing, dataset re-labeling, periodic batch ETL. |
| On-Demand | Real-time (sub-second to seconds) | Standard pay-per-token rate | Conversational chat, interactive web apps, sporadic live traffic. |
| Cross-Region Inference | Real-time (dynamic routing) | Standard on-demand token rate | Mitigating regional ThrottlingException (429) spikes and quota bottlenecks. |
| Provisioned Throughput | Guaranteed low latency | Hourly commit per Model Unit (MU) | Predictable high-traffic 24/7 SLAs and serving custom fine-tuned models. |
