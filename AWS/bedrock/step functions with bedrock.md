Using AWS Step Functions with Amazon Bedrock allows you to orchestrate generative AI workloads natively without managing servers or custom polling loops. Step Functions provides direct optimized service integrations, SDK task actions, built-in retry mechanisms, and flow control.
Core Integration Patterns
| Pattern | AWS States Language (ASL) Resource | Primary Use Case |
|---|---|---|
| Real-Time Model Invocation | arn:aws:states:::bedrock:invokeModel | Single-turn generation, summarization, or agent decision steps. |
| Batch Inference Submission | arn:aws:states:::bedrock:createModelInvocationJob | Offline, high-volume processing at ~50% discount. |
| Asynchronous Polling | arn:aws:states:::bedrock:getModelInvocationJob | Querying batch or long-running job status inside a Wait/Choice loop. |
| Automated Evaluation Gates | arn:aws:states:::aws-sdk:bedrock:getEvaluationJob | Enforcing automated quality gates before production deployments. |
Pattern 1: Direct Synchronous Foundation Model Invocation
Step Functions can invoke models (such as Anthropic Claude or Amazon Titan) directly via bedrock:invokeModel. Use Retry blocks with exponential backoff on Bedrock.ThrottlingException to handle rate limits gracefully.
{
  "Comment": "Direct Amazon Bedrock Model Invocation",
  "StartAt": "InvokeBedrockModel",
  "States": {
    "InvokeBedrockModel": {
      "Type": "Task",
      "Resource": "arn:aws:states:::bedrock:invokeModel",
      "Parameters": {
        "ModelId": "anthropic.claude-3-5-sonnet-20240620-v1:0",
        "Body": {
          "anthropic_version": "bedrock-2023-05-31",
          "max_tokens": 1000,
          "messages": [
            {
              "role": "user",
              "content": [
                {
                  "type": "text",
                  "text.$": "States.Format('Summarize the following text: {}', $.inputText)"
                }
              ]
            }
          ]
        },
        "ContentType": "application/json",
        "Accept": "application/json"
      },
      "ResultSelector": {
        "completion.$": "States.StringToJson($.Body.content[0].text)"
      },
      "ResultPath": "$.modelOutput",
      "Retry": [
        {
          "ErrorEquals": [
            "Bedrock.ThrottlingException",
            "Bedrock.ModelTimeoutException"
          ],
          "IntervalSeconds": 5,
          "MaxAttempts": 3,
          "BackoffRate": 2.0
        }
      ],
      "Catch": [
        {
          "ErrorEquals": ["States.ALL"],
          "ResultPath": "$.error",
          "Next": "HandleModelFailure"
        }
      ],
      "End": true
    },
    "HandleModelFailure": {
      "Type": "Fail",
      "Error": "BedrockInvocationFailed",
      "Cause": "The foundation model call failed or timed out."
    }
  }
}

Pattern 2: Asynchronous Batch Inference with a Polling Loop
Amazon Bedrock Batch Inference processes newline-delimited JSON (.jsonl) files from Amazon S3 asynchronously. Because it does not feature a .sync token integration, use a standard Wait \rightarrow Task \rightarrow Choice polling loop:
[Submit Bedrock Batch Job] 
         │
         ▼
    [Wait 60s] ◄────────────────┐
         │                      │
         ▼                      │ (Status == InProgress / Submitted)
[Get Batch Job Status]          │
         │                      │
         ▼                      │
   [Choice State] ──────────────┘
         ├── Status == "Completed" ──► [Process Output S3 Path]
         └── Status == "Failed"    ──► [Fail State]

ASL Definition:
{
  "Comment": "Bedrock Batch Inference Polling Pipeline",
  "StartAt": "SubmitBatchInferenceJob",
  "States": {
    "SubmitBatchInferenceJob": {
      "Type": "Task",
      "Resource": "arn:aws:states:::bedrock:createModelInvocationJob",
      "Parameters": {
        "JobName.$": "States.Format('batch-job-{}', States.UUID())",
        "RoleArn": "arn:aws:iam::123456789012:role/BedrockBatchExecutionRole",
        "ModelId": "anthropic.claude-3-5-sonnet-20240620-v1:0",
        "InputDataConfig": {
          "S3InputDataConfig": {
            "S3Uri.$": "$.input_s3_uri"
          }
        },
        "OutputDataConfig": {
          "S3OutputDataConfig": {
            "S3Uri.$": "$.output_s3_uri"
          }
        }
      },
      "ResultSelector": {
        "job_arn.$": "$.JobArn"
      },
      "ResultPath": "$.batch_job",
      "Next": "WaitForBatchJob"
    },
    "WaitForBatchJob": {
      "Type": "Wait",
      "Seconds": 60,
      "Next": "GetBatchJobStatus"
    },
    "GetBatchJobStatus": {
      "Type": "Task",
      "Resource": "arn:aws:states:::bedrock:getModelInvocationJob",
      "Parameters": {
        "JobIdentifier.$": "$.batch_job.job_arn"
      },
      "ResultSelector": {
        "status.$": "$.Status",
        "output_s3_uri.$": "$.OutputDataConfig.S3OutputDataConfig.S3Uri"
      },
      "ResultPath": "$.job_status",
      "Retry": [
        {
          "ErrorEquals": ["Bedrock.ThrottlingException"],
          "IntervalSeconds": 5,
          "MaxAttempts": 3,
          "BackoffRate": 1.5
        }
      ],
      "Next": "EvaluateJobStatus"
    },
    "EvaluateJobStatus": {
      "Type": "Choice",
      "Choices": [
        {
          "Variable": "$.job_status.status",
          "StringEquals": "Completed",
          "Next": "BatchSuccess"
        },
        {
          "Variable": "$.job_status.status",
          "StringEquals": "Failed",
          "Next": "BatchFailed"
        }
      ],
      "Default": "WaitForBatchJob"
    },
    "BatchSuccess": {
      "Type": "Pass",
      "Parameters": {
        "status": "COMPLETED",
        "results_s3_uri.$": "$.job_status.output_s3_uri"
      },
      "End": true
    },
    "BatchFailed": {
      "Type": "Fail",
      "Error": "BatchJobExecutionFailed",
      "Cause": "Bedrock Batch Job completed with a Failed status."
    }
  }
}

Pattern 3: Multi-Agent Loops and Guarded ReAct Chains
Step Functions can coordinate Supervisor-Worker architectures, dynamically dispatch tasks, evaluate model reasoning with a Verifier agent, and enforce stopping conditions:
 * Iteration Boundaries: Foundation models should not decide autonomously when to exit loops. Initialize an iteration counter (e.g., remaining_iterations = 3) and decrement it with States.MathAdd($.remaining_iterations, -1). If it reaches 0, route to a Fail state circuit breaker.
 * Orchestrator Saga & Rollbacks: If downstream tool invocations (or semantic Verifier checks) fail, use Catch blocks to execute compensating actions (e.g., refunding charges, releasing holds) in reverse sequence.
Critical Production Best Practices
 * Managing the 256 KB Payload Limit: AWS Step Functions has an execution payload limit of 256 KB. Avoid passing raw base64 images or massive multi-turn conversation arrays directly across states. Pass S3 URIs or session keys, using ResultSelector to keep only relevant fields in the state payload.
 * Resilience & Retries: Always include Retry blocks for transient errors like Bedrock.ThrottlingException and Bedrock.ModelTimeoutException.
 * IAM Role Permissions: The Step Functions execution role must have bedrock:InvokeModel (or bedrock:CreateModelInvocationJob and bedrock:GetModelInvocationJob) on the respective Bedrock ARNs. For batch inference, ensure the Bedrock execution role also has s3:GetObject on the input bucket and s3:PutObject on the output bucket.
Would you like to explore setting up an automated quality evaluation gate with Bedrock Model Evaluation, or integrating dynamic model routing via AWS AppConfig?
