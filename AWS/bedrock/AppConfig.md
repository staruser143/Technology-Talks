Using AWS AppConfig with Amazon Bedrock decouples your application code from generative AI runtime parameters. Rather than redeploying microservices or Lambda functions whenever you switch models, update system prompts, tune hyperparameters, or alter routing rules, you manage these settings dynamically at runtime.
Core Use Cases
| Pattern | How AppConfig Integrates with Bedrock |
|---|---|
| Dynamic Model Routing & Fallback | Store the target modelId (or cross-region inference profile ARN) in AppConfig. Switch from Anthropic Claude to Amazon Nova without code deployment if an endpoint throttles or enters legacy deprecation. |
| Canary Traffic Shifting & Rollbacks | Define split percentages (e.g., 90% baseline model, 10% candidate custom model/prompt). Use AppConfig deployment strategies to shift traffic linearly while CloudWatch alarms monitor for latency spikes or error rates to trigger automated rollbacks. |
| Decoupled Prompt & Hyperparameter Tuning | Control inference configurations (such as temperature, topP, maxTokens) or managed Prompt Management alias IDs outside the codebase. |
| Feature Flags for Generative AI Features | Safely gate capabilities (e.g., streaming responses, multi-modal ingestion, or experimental Bedrock Agents) for select user cohorts. |
Architecture Overview
                                      AWS Cloud
                     ┌────────────────────────────────────────┐
                     │              AWS AppConfig             │
                     │  - Model IDs / Routing Percentages     │
                     │  - Hyperparameters (Temp, TopP)        │
                     │  - JSON Schema Validator / Rollbacks   │
                     └───────────────────┬────────────────────┘
                                         │ AppConfig Lambda Extension
                                         │ (Local Cache & Poll)
                                         ▼
[ Client Request ] ──► [ Amazon API Gateway ] ──► [ AWS Lambda Router ]
                                                          │
                                                          ▼
                                              ┌───────────────────────┐
                                              │    Amazon Bedrock     │
                                              │ (Converse API / FMs)  │
                                              └───────────────────────┘

Using the AWS AppConfig Lambda Extension or SDK caching, the compute layer polls AppConfig locally in memory. This prevents per-request API overhead and eliminates extra latency.
Step-by-Step Implementation Guide
Step 1: Define the AppConfig Configuration Profile
Create a JSON schema in AppConfig with your routing and runtime parameters.
{
  "routing_strategy": "CANARY",
  "baseline_model": {
    "model_id": "us.anthropic.claude-3-5-sonnet-20240620-v1:0",
    "temperature": 0.2,
    "max_tokens": 1000
  },
  "candidate_model": {
    "model_id": "us.amazon.nova-pro-v1:0",
    "temperature": 0.1,
    "max_tokens": 1000
  },
  "traffic_split_percent": {
    "candidate": 20,
    "baseline": 80
  },
  "circuit_breaker": {
    "force_baseline_only": false
  }
}

Tip: Attach a JSON Schema validator in AppConfig to ensure required keys (model_id, temperature) are never accidentally deployed with invalid formats or out-of-bound values.
Step 2: Implement Dynamic Model Invocations in Code
The following Lambda handler retrieves the dynamic configuration from the AppConfig local agent extension (running on http://localhost:2772) and invokes the selected model using the unified Amazon Bedrock Converse API.
import json
import random
import urllib.request
import boto3

bedrock_runtime = boto3.client("bedrock-runtime", region_name="us-east-1")

APPCONFIG_PORT = 2772
APPLICATION = "GenAIPlatform"
ENVIRONMENT = "Production"
CONFIGURATION = "BedrockModelRouting"

def get_appconfig_settings() -> dict:
    """Fetch configuration from AppConfig local extension cache."""
    url = f"http://localhost:{APPCONFIG_PORT}/applications/{APPLICATION}/environments/{ENVIRONMENT}/configurations/{CONFIGURATION}"
    try:
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode("utf-8"))
    except Exception:
        # Fallback to local hardcoded defaults if AppConfig agent is unreachable
        return {
            "baseline_model": {
                "model_id": "us.anthropic.claude-3-5-sonnet-20240620-v1:0",
                "temperature": 0.2,
                "max_tokens": 1000
            },
            "circuit_breaker": {"force_baseline_only": True}
        }

def select_model(config: dict) -> dict:
    """Determine model target based on traffic weights and circuit breaker."""
    if config.get("circuit_breaker", {}).get("force_baseline_only", False):
        return config["baseline_model"]
        
    candidate_weight = config.get("traffic_split_percent", {}).get("candidate", 0)
    if candidate_weight > 0 and random.uniform(0, 100) < candidate_weight:
        return config.get("candidate_model", config["baseline_model"])
        
    return config["baseline_model"]

def lambda_handler(event, context):
    user_prompt = event.get("prompt", "Summarize operational status.")
    
    # 1. Fetch dynamic config & determine model
    config = get_appconfig_settings()
    selected_target = select_model(config)
    
    # 2. Invoke Bedrock via standard Converse API
    response = bedrock_runtime.converse(
        modelId=selected_target["model_id"],
        messages=[{"role": "user", "content": [{"text": user_prompt}]}],
        inferenceConfig={
            "maxTokens": selected_target.get("max_tokens", 1000),
            "temperature": selected_target.get("temperature", 0.2)
        }
    )
    
    output_text = response["output"]["message"]["content"][0]["text"]
    
    return {
        "statusCode": 200,
        "selected_model": selected_target["model_id"],
        "response": output_text
    }

Step 3: Configure Deployment Strategies and Automated Rollbacks
 * Deployment Strategy: Use built-in templates such as AppConfig.Linear50PercentEvery30Seconds or AppConfig.Canary10Percent20Minutes to gradually roll out routing updates across your fleet.
 * CloudWatch Alarm Rollback Monitor: Link an Amazon CloudWatch alarm monitoring Bedrock.ServerErrors (5xx errors) or invocation latency to the AppConfig Environment. If an alarm breaches thresholds during the deployment window, AppConfig automatically halts traffic shifting and rolls back the configuration pointer to the last known good state.
Best Practices
 * Standardize on the Converse API: Always use bedrock_runtime.converse() instead of provider-specific APIs (e.g., invoke_model). This guarantees that payload structures remain identical whether AppConfig points to Claude, Titan, Llama, or Nova.
 * Pair with Cross-Region Profiles: Store Cross-Region Inference profile identifiers (e.g., us.anthropic...) in AppConfig rather than single-region IDs to protect against regional quotas and throttling exceptions.
 * Decouple Custom Provisioned Models: Store the Provisioned Throughput ARN for customized fine-tuned models in AppConfig rather than hardcoding endpoint ARNs.
