# Automating AppConfig with Bedrock Model Evaluation Job
- Automating AWS AppConfig routing updates after an Amazon Bedrock Model Evaluation job passes its quality gate creates a closed-loop CI/CD deployment track.
- This replaces manual reviews with an event-driven or Step Functions-orchestrated gate that dynamically promotes candidate models (or begins canary traffic shifting).

## Architecture Overview
```
 ┌──────────────────────────────────────────────┐
 │ Bedrock Automated Model Evaluation Job       │
 │ - Evaluates candidate model on golden dataset│
 └──────────────────────┬───────────────────────┘
                        │
                        ▼ (Output stored in S3)
 ┌──────────────────────────────────────────────┐
 │ Quality Gate Assessment (Lambda / Step Fn)   │
 │ 1. Reads evaluation metrics from S3          │
 │ 2. Validates thresholds (e.g. Accuracy ≥0.85)│
 └──────────────────────┬───────────────────────┘
                        │ Gate Passed
                        ▼
 ┌──────────────────────────────────────────────┐
 │ AWS AppConfig Deployment Promotion           │
 │ 1. Create Hosted Config / Profile Version    │
 │ 2. Start Deployment (e.g. Canary 10% 20 Mins)│
 └──────────────────────┬───────────────────────┘
                        │
                        ▼ (Dynamic Shift)
 ┌──────────────────────────────────────────────┐
 │ Bedrock Converse Client / Lambda Router      │
 │ - Ingests new model/weights at runtime       │
 └──────────────────────────────────────────────┘
```

## Step-by-Step Implementation
### 1. Define the Quality Gate Evaluation Logic (S3 Metrics Parser)
When an automated Bedrock Model Evaluation job finishes, its scoring matrices (Factual Accuracy/Groundedness, Relevance, Fluency) are output as JSON lines or summary files to Amazon S3.
The Lambda function below downloads the evaluation output from S3, verifies whether the candidate foundation model meets the minimum threshold (e.g., Accuracy \ge 0.85), and proceeds to publish a new configuration profile to AppConfig.
```python
import json
import boto3
from urllib.parse import urlparse

s3 = boto3.client("s3")
bedrock = boto3.client("bedrock", region_name="us-east-1")
appconfig = boto3.client("appconfig", region_name="us-east-1")

APP_ID = "GenAIPlatform"
ENV_ID = "Production"
CONFIG_PROFILE_ID = "BedrockModelRouting"
DEPLOYMENT_STRATEGY = "AppConfig.Canary10Percent20Minutes"  # Gradual canary shift

ACCURACY_GATE_THRESHOLD = 0.85

def evaluate_quality_metrics(evaluation_job_name: str) -> tuple[bool, float]:
    """Retrieves Bedrock Evaluation job results from S3 and checks quality gate."""
    job_details = bedrock.get_evaluation_job(jobIdentifier=evaluation_job_name)
    
    if job_details["status"] != "COMPLETED":
        return False, 0.0

    output_s3_uri = job_details["outputDataConfig"]["s3Uri"]
    parsed_s3 = urlparse(output_s3_uri)
    bucket = parsed_s3.netloc
    prefix = parsed_s3.path.lstrip("/")

    # Bedrock evaluation exports metric summaries into the output directory
    metrics_key = f"{prefix}output.jsonl"
    obj = s3.get_object(Bucket=bucket, Key=metrics_key)
    
    # Example metric extraction (assuming mean accuracy across dataset items)
    scores = []
    for line in obj["Body"].read().decode("utf-8").splitlines():
        if line.strip():
            record = json.loads(line)
            # Locate target metric score (e.g., Accuracy / Groundedness)
            metric_val = record.get("metrics", {}).get("Accuracy", 0.0)
            scores.append(metric_val)
            
    mean_accuracy = sum(scores) / len(scores) if scores else 0.0
    passed = mean_accuracy >= ACCURACY_GATE_THRESHOLD
    return passed, mean_accuracy

2. Update Configuration and Trigger AppConfig Deployment
When the candidate passes the quality gate, create a new configuration version pointing traffic to the candidate model, then invoke start_deployment:
def promote_candidate_model(candidate_model_id: str, baseline_model_id: str):
    """Creates a new AppConfig hosted configuration version and starts canary deployment."""
    # 1. Prepare updated routing profile (e.g., 20% candidate canary or 100% promotion)
    updated_config_payload = {
        "routing_strategy": "CANARY",
        "baseline_model": {
            "model_id": baseline_model_id,
            "temperature": 0.2,
            "max_tokens": 1000
        },
        "candidate_model": {
            "model_id": candidate_model_id,
            "temperature": 0.2,
            "max_tokens": 1000
        },
        "traffic_split_percent": {
            "candidate": 20,
            "baseline": 80
        },
        "circuit_breaker": {
            "force_baseline_only": False
        }
    }
    
    # 2. Create Hosted Configuration Version in AppConfig
    version_response = appconfig.create_hosted_configuration_version(
        ApplicationIdentifier=APP_ID,
        ConfigurationProfileIdentifier=CONFIG_PROFILE_ID,
        Content=json.dumps(updated_config_payload).encode("utf-8"),
        ContentType="application/json",
        Description="Automated promotion following successful Bedrock Evaluation gate."
    )
    new_version_number = version_response["VersionNumber"]

    # 3. Start progressive canary rollout
    deployment_response = appconfig.start_deployment(
        ApplicationIdentifier=APP_ID,
        EnvironmentIdentifier=ENV_ID,
        DeploymentStrategyIdentifier=DEPLOYMENT_STRATEGY,
        ConfigurationProfileIdentifier=CONFIG_PROFILE_ID,
        ConfigurationVersion=str(new_version_number),
        Description=f"Promoted {candidate_model_id} via canary deployment."
    )
    
    return deployment_response["DeploymentNumber"]

def lambda_handler(event, context):
    eval_job_name = event["evaluation_job_name"]
    candidate_model = event["candidate_model_id"]
    baseline_model = event.get("baseline_model_id", "us.anthropic.claude-3-5-sonnet-20240620-v1:0")

    passed, score = evaluate_quality_metrics(eval_job_name)
    
    if not passed:
        return {
            "status": "GATE_FAILED",
            "reason": f"Mean accuracy {score:.2f} did not meet SLA {ACCURACY_GATE_THRESHOLD}"
        }
        
    dep_number = promote_candidate_model(candidate_model, baseline_model)
    return {
        "status": "PROMOTION_STARTED",
        "score": score,
        "deployment_number": dep_number
    }
```

### 3. Orchestrating with AWS Step Functions
Instead of running long-lived polling in a single Lambda, use an AWS Step Functions state machine with native retry/catch policies to orchestrate the pipeline:
```json
{
  "Comment": "Bedrock Evaluation to AppConfig Automated Gate",
  "StartAt": "WaitForEvaluationCompletion",
  "States": {
    "WaitForEvaluationCompletion": {
      "Type": "Wait",
      "Seconds": 120,
      "Next": "CheckEvaluationJobStatus"
    },
    "CheckEvaluationJobStatus": {
      "Type": "Task",
      "Resource": "arn:aws:states:::aws-sdk:bedrock:getEvaluationJob",
      "Parameters": {
        "JobIdentifier.$": "$.evaluation_job_name"
      },
      "ResultPath": "$.eval_status",
      "Next": "IsEvaluationComplete"
    },
    "IsEvaluationComplete": {
      "Type": "Choice",
      "Choices": [
        {
          "Variable": "$.eval_status.Status",
          "StringEquals": "COMPLETED",
          "Next": "RunQualityGateCheck"
        },
        {
          "Variable": "$.eval_status.Status",
          "StringEquals": "FAILED",
          "Next": "EvaluationJobFailed"
        }
      ],
      "Default": "WaitForEvaluationCompletion"
    },
    "RunQualityGateCheck": {
      "Type": "Task",
      "Resource": "arn:aws:states:::lambda:invoke",
      "Parameters": {
        "FunctionName": "arn:aws:lambda:us-east-1:123456789012:function:evaluate-and-promote-appconfig",
        "Payload": {
          "evaluation_job_name.$": "$.evaluation_job_name",
          "candidate_model_id.$": "$.candidate_model_id",
          "baseline_model_id.$": "$.baseline_model_id"
        }
      },
      "ResultSelector": {
        "result.$": "$.Payload"
      },
      "Next": "EvaluatePromotionResult"
    },
    "EvaluatePromotionResult": {
      "Type": "Choice",
      "Choices": [
        {
          "Variable": "$.result.status",
          "StringEquals": "PROMOTION_STARTED",
          "Next": "PromotionSucceeded"
        }
      ],
      "Default": "QualityGateRejected"
    },
    "PromotionSucceeded": {
      "Type": "Succeed"
    },
    "QualityGateRejected": {
      "Type": "Fail",
      "Error": "QualityGateFailure",
      "Cause": "Model evaluation score fell below accuracy boundaries."
    },
    "EvaluationJobFailed": {
      "Type": "Fail",
      "Error": "BedrockEvaluationFailed",
      "Cause": "Bedrock evaluation job halted unexpectedly or failed."
    }
  }
}
```

### Automated Rollback Strategy
Attach an Amazon CloudWatch Alarm to the AppConfig Environment to protect against regressions once the canary traffic shift begins:
 * **Alarm Trigger**: Monitor Bedrock.ServerErrors or custom CloudWatch metrics tracking runtime hallucination/accuracy regression alerts during the deployment window.
 * **Automated Action**: If the alarm transitions to ALARM state, AppConfig immediately halts deployment progression and reverts runtime configuration to the previous version.
 * **Programmatic Circuit Breaker**: If custom post-inference monitoring detects latency spikes, invoke appconfig.stop_deployment(...) to immediately reset the traffic split to 100% on the baseline model.
