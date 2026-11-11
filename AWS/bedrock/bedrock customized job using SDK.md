# Bedrock Model Customization Job Using SDK
- An Amazon Bedrock model customization job configures serverless compute to update foundation model weights directly using your training corpus in Amazon S3.
- The primary SDK entry point is the control-plane client (bedrock), using the create_model_customization_job API.

## 1. Prerequisites & Preparation
### A. IAM Execution Role Policy
Bedrock requires an execution role granting access to read training datasets and write output artifacts to your S3 bucket.
 * **Trust Policy**: Principal bedrock.amazonaws.com with sts:AssumeRole.
 * Permissions Policy:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::my-bedrock-datasets",
        "arn:aws:s3:::my-bedrock-datasets/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "s3:PutObject",
      "Resource": "arn:aws:s3:::my-bedrock-datasets/custom-output/*"
    }
  ]
}
```
### B. Dataset Formatting
Datasets must reside in S3 as newline-delimited JSON (.jsonl):
 * **Fine-Tuning (FINE_TUNING)**: Supervised prompt-completion pairs.
   ```json
   {"prompt": "Summarize policy notice: Employees must give two weeks notice.", "completion": "Notice period: 2 weeks mandatory prior to departure."}
   ```
 * **Continued Pre-training (CONTINUED_PRE_TRAINING)**: Unlabeled raw domain text.
    ```json
      {"input": "According to Section 4.2 of internal corporate security policies, all databases must use AWS KMS encryption..."}
    ```

## 2. Submitting the Customization Job (Python SDK / Boto3)
Initialize the bedrock control-plane client and configure training hyperparameters, dataset paths, and target model identifiers:
```python
import boto3

bedrock = boto3.client("bedrock", region_name="us-east-1")

response = bedrock.create_model_customization_job(
    jobName="titan-support-classifier-v1",
    customModelName="titan-support-classifier",
    roleArn="arn:aws:iam::123456789012:role/BedrockCustomizationExecutionRole",
    baseModelIdentifier="arn:aws:bedrock:us-east-1::foundation-model/amazon.titan-text-express-v1",
    customizationType="FINE_TUNING",  # or "CONTINUED_PRE_TRAINING"
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
```

## 3. Monitoring Job Status & Metrics
Because customization is an asynchronous batch process, use get_model_customization_job to track execution and retrieve loss metrics:
```python
import time

def wait_for_customization(job_arn: str, poll_interval: int = 60):
    while True:
        status_res = bedrock.get_model_customization_job(jobIdentifier=job_arn)
        status = status_res["status"]
        print(f"Job Status: {status}")

        if status == "Completed":
            output_model_arn = status_res["outputModelArn"]
            metrics = status_res.get("trainingMetrics", {})
            validation_metrics = status_res.get("validationMetrics", [])
            print(f"Custom Model Created: {output_model_arn}")
            print(f"Final Training Loss: {metrics.get('trainingLoss')}")
            return output_model_arn

        elif status in ["Failed", "Stopped"]:
            failure_reason = status_res.get("failureMessage", "No message provided.")
            raise RuntimeError(f"Job halted with status {status}: {failure_reason}")

        time.sleep(poll_interval)

model_arn = wait_for_customization(job_arn)
```

## 4. Serving the Customized Model
Once the model reaches the Completed state, it is ready for deployment:
 * **Deployment Options**:
   * **Provisioned Throughput**: Custom models generally require dedicated capacity. Purchase one or more Model Units (MUs) with no commitment (hourly billing) or a 1/6-month term using create_provisioned_model_throughput.
   * **On-Demand Deployment**: Supported base models (such as Amazon Nova or select Titan variants) can also be deployed to on-demand serverless targets without dedicated capacity reservation.
 * **Inference Execution**:
   Call the provisioned model ARN using the runtime client (bedrock-runtime):
```python
bedrock_runtime = boto3.client("bedrock-runtime", region_name="us-east-1")

provisioned_model_arn = "arn:aws:bedrock:us-east-1:123456789012:provisioned-model/my-custom-endpoint"

response = bedrock_runtime.converse(
    modelId=provisioned_model_arn,
    messages=[
        {"role": "user", "content": [{"text": "Summarize: All remote work requests require manager approval."}]}
    ],
    inferenceConfig={"maxTokens": 200, "temperature": 0.1}
)

print(response["output"]["message"]["content"][0]["text"])
```
