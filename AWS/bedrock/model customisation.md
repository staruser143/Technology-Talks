Amazon Bedrock model customization allows you to adapt base foundation models to specialized domain terminology, proprietary styles, and specific business tasks. Rather than relying solely on large prompt contexts, customization updates the model weights directly.
Customization Techniques
Bedrock supports distinct customization methods depending on your data and target objective:
| Customization Type | Primary Purpose | Required Training Data Format | Typical Use Cases |
|---|---|---|---|
| Fine-Tuning (SFT) | Teaches specific output formats, stylistic tone, or task behaviors. | Supervised prompt-completion pairs in .jsonl format. | Specialized classification, strict schema adherence, corporate voice alignment. |
| Continued Pre-Training (CPT) | Imparts deep, raw domain knowledge and technical jargon into base weights without task labels. | Unlabeled raw text in .jsonl files ({"input": "..."}). | Niche industries (legal statutes, medical journals, proprietary technical docs). |
| Custom Model Import (CMI) | Imports models trained or fine-tuned externally (e.g., Hugging Face or SageMaker) into Bedrock. | Model weights (e.g., Safetensors / PyTorch checkpoints) stored in S3. | Running custom architectures (like fine-tuned Llama or Mistral) through Bedrock APIs. |
End-to-End Workflow
1. Data Preparation (.jsonl)
Training data must reside in an Amazon S3 bucket.
 * Fine-Tuning Example (train.jsonl):
{"prompt": "Summarize policy notice: Employee must give two weeks notice before resignation.", "completion": "Notice period: 2 weeks mandatory prior to departure."}
{"prompt": "Summarize policy notice: Remote work requests require manager approval.", "completion": "Remote work: Requires explicit managerial sign-off."}

 * Continued Pre-Training Example (pretrain.jsonl):
{"input": "In accordance with Subsection 4.2 of internal corporate compliance guidelines, all data lakes must enforce AWS KMS CMK encryption at rest..."}

2. Launching a Customization Job (Boto3)
Use the Bedrock control-plane client (bedrock) to trigger the job:
import boto3

bedrock = boto3.client("bedrock", region_name="us-east-1")

response = bedrock.create_model_customization_job(
    jobName="fintech-custom-titan-ft-01",
    customModelName="fintech-titan-express-v1",
    roleArn="arn:aws:iam::123456789012:role/BedrockCustomizationExecutionRole",
    baseModelIdentifier="arn:aws:bedrock:us-east-1::foundation-model/amazon.titan-text-express-v1",
    customizationType="FINE_TUNING",
    hyperParameters={
        "epochCount": "3",
        "batchSize": "8",
        "learningRate": "0.0001"
    },
    trainingDataConfig={
        "s3Uri": "s3://my-bedrock-datasets/custom-training/train.jsonl"
    },
    validationDataConfig={
        "validators": [
            {"s3Uri": "s3://my-bedrock-datasets/custom-training/validation.jsonl"}
        ]
    },
    outputDataConfig={
        "s3Uri": "s3://my-bedrock-datasets/custom-output/"
    }
)

job_arn = response["jobArn"]
print(f"Customization Job Submitted: {job_arn}")

3. Deployment and Serving (Provisioned Throughput)
 * Custom models cannot be invoked via pay-per-token on-demand pricing; they require Provisioned Throughput.
 * You purchase one or more Model Units (MU) either with a commitment (e.g., 1-month or 6-month) or with no commitment (hourly billing for evaluation).
 * Once the Provisioned Throughput is active, invoke the custom model via the Bedrock Runtime (bedrock-runtime) using either InvokeModel or the Converse API by referencing the provisioned model ARN.
When to Customize vs. RAG vs. Prompting
                             [Decision Framework]
                                      │
           Is dynamic, frequently changing data access required?
                               ┌──────┴──────┐
                             Yes             No
                              │              │
                    [Bedrock Knowledge Bases]  │
                             (RAG)           Does prompt engineering provide
                                             acceptable format & consistency?
                                               ┌──────┴──────┐
                                             Yes             No
                                              │              │
                                    [Prompt Management]   [Bedrock Model]
                                       (Few-Shot)         [Customization]

 * Use Prompt Engineering: Quick iteration, low volume, or when task descriptions fit comfortably within standard token limits.
 * Use RAG (Knowledge Bases): Up-to-date facts, internal document search, strict source attribution, and changing documentation.
 * Use Bedrock Customization: Enforcing non-standard formatting across thousands of calls, eliminating lengthy repetitive system instructions, or aligning closely with corporate linguistic style.
Would you like to explore configuring VPC security and IAM policies for customization jobs, or how to set up Provisioned Throughput to serve a custom model?
