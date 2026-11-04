Foundation model (FM) deployment and lifecycle management in Amazon Bedrock encompasses two dimensions: how foundation models are hosted and scaled (Deployment) and how model versions are tracked, monitored, and retired (Lifecycle Management).
1. Foundation Model Deployment Options
Amazon Bedrock provides four primary deployment patterns to match latency, capacity, and cost profiles:
| Deployment Option | Pricing / Billing Model | Capacity & Predictability | Primary Use Case |
|---|---|---|---|
| On-Demand Inference | Pay-as-you-go per token (separate input and output token rates) | Shared multi-tenant pool; subject to account service quotas (RPM/TPM) | Rapid prototyping, ad-hoc evaluations, variable or bursty application traffic. |
| Cross-Region Inference | Standard On-Demand token pricing (routed across regional pools) | High availability; automatically shifts traffic across geo-defined AWS Regions | Mitigating regional quotas, avoiding 429 ThrottlingException during spikes. |
| Provisioned Throughput | Billed per Model Unit (MU) per hour (hourly no-commit, or 1/6-month commitments) | Dedicated compute capacity with guaranteed throughput and fixed low latency | Sustained 24/7 production workloads, strict SLAs, custom fine-tuned models. |
| Batch Inference | Pay-as-you-go per token at an approximate 50% discount | Asynchronous, queued offline processing via S3 .jsonl files | Bulk document summarization, offline dataset scoring, large-scale ETL. |
Custom Models vs. SageMaker Endpoints
 * Custom Models in Bedrock: Models customized via Supervised Fine-Tuning (SFT), Continued Pre-training (CPT), or Model Distillation can be deployed via Provisioned Throughput (or On-Demand deployment targets for supported families like Amazon Nova and Titan).
 * Hybrid / SageMaker Endpoints: If using complex parameter-efficient fine-tuning (PEFT/LoRA) with custom runtime scripts or multi-adapter hot-swapping, models can be hosted on Amazon SageMaker AI Multi-Model Endpoints (MME) with LRU memory caching or imported into Bedrock via Custom Model Import (CMI).
2. Bedrock Model Lifecycle States
Every foundation model on Amazon Bedrock transitions through three distinct operational lifecycle stages:
┌─────────────────────────────────┐
│             ACTIVE              │  ◄── In production; accepts requests, supports
└────────────────┬────────────────┘      new customizations & Provisioned Throughput
                 │
                 │ AWS sets deprecation schedule (typically ≥ 6 months notice)
                 ▼
┌─────────────────────────────────┐
│             LEGACY              │  ◄── Borrowed time; existing users retain access,
└────────────────┬────────────────┘      no new customizations or Provisioned Throughput
                 │
                 │ Published EOL Date reached
                 ▼
┌─────────────────────────────────┐
│          END-OF-LIFE            │  ◄── Deprecated; API calls fail (ValidationException).
└─────────────────────────────────┘      Requests are NEVER silently redirected.

 * Active:
   * Fully supported and maintained by the model provider.
   * Open for new on-demand requests, Provisioned Throughput purchases, model customization jobs, and quota increases.
 * Legacy:
   * Signals that deprecation is approaching (models generally stay in Legacy for at least 6 months before EOL).
   * Existing callers can continue invocations, but new customers cannot enable the model, and existing accounts may lose access after extended periods of inactivity.
   * Restrictions: You cannot create new customization jobs (fine-tuning) or purchase new Provisioned Throughput on a legacy model.
   * Public Extended Access: For select models, extended legacy windows may be offered at premium token pricing while organizations finalize migration.
 * End-of-Life (EOL):
   * The model is retired. Invocations directed at its model identifier will return a ValidationException.
   * Bedrock does not auto-redirect or silently rewrite model targets. Application pipelines will break unless actively migrated prior to the EOL cutoff.
3. Programmatic Lifecycle Tracking & Governance
To maintain continuous operations without hardcoded model references, implement these architectural patterns:
A. Decouple Model IDs via AWS AppConfig or Parameter Store
Avoid hardcoding strings like anthropic.claude-3-5-sonnet-20240620-v1:0 in application code. Store model identifiers and fallback rules inside AWS AppConfig or Amazon API Gateway mapping stages:
[Client Request] ──► [API Gateway] ──► [Lambda Router] ──► [Amazon Bedrock API]
                                              ▲
                                              │ (Pulls active model ID)
                                      [AWS AppConfig]

B. Automated Model Status Auditing
Query Bedrock control-plane APIs (bedrock:ListFoundationModels or bedrock:GetFoundationModel) via a scheduled Lambda function or EventBridge rule to inspect the modelLifecycle status:
import boto3

bedrock = boto3.client("bedrock", region_name="us-east-1")

def audit_model_lifecycles(model_ids: list[str]):
    for model_id in model_ids:
        details = bedrock.get_foundation_model(modelIdentifier=model_id)
        lifecycle_status = details["modelDetails"].get("modelLifecycle", {}).get("status")
        
        if lifecycle_status == "LEGACY":
            print(f"[ALERT] Model {model_id} is in LEGACY state! Plan migration.")
        elif lifecycle_status == "ACTIVE":
            print(f"[OK] Model {model_id} is ACTIVE.")

C. Notification Integration
Subscribe to AWS Health (Personal Health Dashboard) events through Amazon EventBridge. When AWS schedules a transition to Legacy or sets an EOL date for a model enabled in your account, EventBridge can automatically trigger a Jira ticket, SNS topic, or Slack notification for the engineering team.
4. Migration & Deprecation Workflow
When an FM enters the Legacy phase, execute a structured multi-phase transition:
 * Model Evaluation & Regression Testing: Run target replacement models through Amazon Bedrock Model Evaluation jobs using golden datasets to verify groundedness, relevance, and formatting.
 * Converse API Standardization: Ensure client code uses Bedrock's unified Converse API. Standardizing on bedrock_runtime.converse() allows switching model families (e.g., Anthropic Claude, Meta Llama, Amazon Nova) with zero changes to request/response schema parsing.
 * Prompt & Guardrail Re-tuning: Update parameterized prompt templates via Bedrock Prompt Management (publishing an immutable prompt version) and re-test Bedrock Guardrails to prevent prompt drift or output changes.
 * Phased Rollout: Employ canary or linear traffic shifting using AWS AppConfig or API Gateway stage weightings to monitor CloudWatch invocation logs, latency, and token consumption before fully cutting over.
Would you like to see an AWS CloudFormation/CDK example for automating health alerts when models enter the Legacy state, or explore managing Provisioned Throughput scaling policies?
