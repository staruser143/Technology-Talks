# AWS CloudWatch metrics for Amazon Bedrock
- Bedrock publishes operational telemetry to Amazon CloudWatch under the namespace AWS/Bedrock/Guardrails.
- The metric InvocationsIntervened tracks the number of times guardrail policies (such as sensitive information filters, word filters, or denied topics) intervene on user prompts or model responses.
- When updating guardrail versions, an unintended surge in InvocationsIntervened typically indicates a high false-positive rate (over-blocking benign user prompts). 
- By configuring a CloudWatch metric alarm on this metric and attaching it to the monitors property of an AWS AppConfig Environment, AppConfig can automatically halt canary deployment progression and roll back the configuration pointer to the last known good guardrail version.

## Step-by-Step Configuration
### 1. Configure the CloudWatch Metric Alarm
- We can create an alarm based either on the raw intervention count or on an intervention rate calculated using CloudWatch Metric Math ((InvocationsIntervened / Invocations) * 100).
```python
import boto3

cloudwatch = boto3.client("cloudwatch", region_name="us-east-1")

GUARDRAIL_ID = "gr-1234567890ab"
ALARM_NAME = "BedrockGuardrailHighInterventionAlarm"

# Alarm trips if more than 5 interventions occur across a 1-minute evaluation period
cloudwatch.put_metric_alarm(
    AlarmName=ALARM_NAME,
    AlarmDescription="Triggers automated rollback if guardrail intervention rate surges during canary deployment.",
    ActionsEnabled=True,
    MetricName="InvocationsIntervened",
    Namespace="AWS/Bedrock/Guardrails",
    Statistic="Sum",
    Dimensions=[
        {"Name": "GuardrailId", "Value": GUARDRAIL_ID}
    ],
    Period=60,                  # 1-minute window
    EvaluationPeriods=1,        # Trip rapidly to protect user experience
    Threshold=5.0,              # Threshold for interventions in the canary window
    ComparisonOperator="GreaterThanThreshold",
    TreatMissingData="notBreaching"
)

print(f"Created CloudWatch Alarm: {ALARM_NAME}")
```

### 2. Create the AppConfig IAM Service Role for Alarms
AppConfig requires permissions to poll the alarm state via cloudwatch:DescribeAlarms.
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "cloudwatch:DescribeAlarms"
      ],
      "Resource": "*"
    }
  ]
}
```
The trust policy must permit AppConfig to assume the role:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "appconfig.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```
### 3. Link the CloudWatch Alarm to the AppConfig Environment Monitor
- In AWS AppConfig, automated rollbacks are tied to the Environment resource (not the configuration profile or deployment strategy). Add the alarm ARN and execution role ARN to the environment's monitors list:
```python
import boto3

appconfig = boto3.client("appconfig", region_name="us-east-1")

APPLICATION_ID = "GenAIPlatform"
ENVIRONMENT_ID = "Production"
ACCOUNT_ID = "123456789012"
ALARM_ARN = f"arn:aws:cloudwatch:us-east-1:{ACCOUNT_ID}:alarm:BedrockGuardrailHighInterventionAlarm"
ROLE_ARN = f"arn:aws:iam::{ACCOUNT_ID}:role/AppConfigCloudWatchMonitorRole"

# Update AppConfig Environment to attach the alarm monitor
response = appconfig.update_environment(
    ApplicationId=APPLICATION_ID,
    EnvironmentId=ENVIRONMENT_ID,
    Monitors=[
        {
            "AlarmArn": ALARM_ARN,
            "AlarmRoleArn": ROLE_ARN
        }
    ]
)

print(f"Updated Environment {ENVIRONMENT_ID} with CloudWatch Rollback Monitor.")
```

### 4. Execute a Deployment with a Canary Strategy and Bake Time
- To give the CloudWatch alarm time to detect regressions, use a deployment strategy with a non-zero Bake Time (e.g., AppConfig.Canary10Percent20Minutes).
```python
import json
import boto3

appconfig = boto3.client("appconfig", region_name="us-east-1")

CONFIG_PROFILE_ID = "GuardrailRouting"
NEW_GUARDRAIL_VERSION = "3"

# 1. Update the configuration payload to test the candidate guardrail version
routing_payload = {
    "guardrail_id": "gr-1234567890ab",
    "baseline_version": "2",
    "canary_version": NEW_GUARDRAIL_VERSION,
    "canary_traffic_percent": 10
}

# 2. Publish a new hosted configuration version
version_resp = appconfig.create_hosted_configuration_version(
    ApplicationId=APPLICATION_ID,
    ConfigurationProfileId=CONFIG_PROFILE_ID,
    Content=json.dumps(routing_payload).encode("utf-8"),
    ContentType="application/json",
    Description=f"Canary test of Guardrail version {NEW_GUARDRAIL_VERSION}"
)
version_number = str(version_resp["VersionNumber"])

# 3. Start deployment with automated rollback monitoring
deployment = appconfig.start_deployment(
    ApplicationId=APPLICATION_ID,
    EnvironmentId=ENVIRONMENT_ID,
    DeploymentStrategyId="AppConfig.Canary10Percent20Minutes",
    ConfigurationProfileId=CONFIG_PROFILE_ID,
    ConfigurationVersion=version_number,
    Description="Gradual canary shift with InvocationsIntervened automated rollback."
)

print(f"Deployment started: Deployment #{deployment['DeploymentNumber']}")
```

## How the Rollback Lifecycle Works at Runtime
 * **Traffic Shifting**: 10% of traffic begins routing to Guardrail version 3, while 90% stays on version 2.
 * **Telemetry Check**: If the new guardrail version incorrectly blocks benign queries, Bedrock increments AWS/Bedrock/Guardrails -> InvocationsIntervened.
 * **Threshold Breach**: When the metric crosses 5 interventions in 1 minute, the CloudWatch alarm transitions to ALARM state.
 * **Automated Reversion**: AppConfig detects the alarm change during the deployment or bake-time window. It immediately halts traffic progression, reverts the deployed configuration version back to the previous stable state, and updates the deployment status to ROLLED_BACK.
