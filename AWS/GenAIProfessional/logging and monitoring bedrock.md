# Logging And Monitoring Amazon Bedrock Inference Requests
- To log and monitor Amazon Bedrock inference requests, we can use native AWS services across three primary pillars:
- **Model Invocation Logging (for payloads), CloudWatch Metrics (for performance and token tracking), and AWS CloudTrail (for API auditing)**.
------------------------------
## 1. Payload Logging: Model Invocation Logging
This feature allows us to capture full request/response text, images, embeddings, and token counts. By default, it is disabled.
## How to Enable:

   1. Open the Amazon Bedrock Console.
   2. From the left navigation pane, select Settings.
   3. Navigate to Model invocation logging and toggle it on.
   4. Select the modalities we want to log (Text, Image, Embedding, Video).
   5. Choose the destination target(s):

| Destination Target | Best Used For | Analysis Tool |
|---|---|---|
| Amazon CloudWatch Logs | Real-time analysis, alerting, and developer debugging. | CloudWatch Logs Insights[](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/AnalyzingLogData.html) |
| Amazon S3 | Long-term compliance storage and large-scale analytical reporting. | Amazon Athena & Amazon QuickSight |

Note: For CloudWatch Logs, we must configure an IAM Role allowing Bedrock to write to the log group.
------------------------------
## 2. Performance Monitoring: Amazon CloudWatch Metrics
Amazon Bedrock automatically streams operational metrics to CloudWatch under the AWS/Bedrock namespace. We do not need to configure anything to see these.
Key metrics to monitor via [CloudWatch dashboards](https://docs.aws.amazon.com/prescriptive-guidance/latest/implementing-logging-monitoring-cloudwatch/cloudwatch-dashboards-visualizations.html) include:

* **ModelInvocations**: The total number of requests sent to a model.
* **InvocationLatency**: The time taken for the model to return a response. Use this with Output Tokens Per Second (OTPS) to diagnose latency anomalies.
* **InvocationErrors**: Tracks 4xx and 5xx errors to help catch throttling or model downtime.
* **InputTokenCount** & **OutputTokenCount**: Crucial for monitoring system consumption and real-time cost management.

------------------------------
## 3. Governance and Auditing: AWS CloudTrail
To track who made the request, AWS CloudTrail automatically records administrative and runtime data plane API operations (like InvokeModel, InvokeModelWithResponseStream, and Converse).

* **What it logs**: The IAM identity of the caller, time of the event, IP address, and target model ID.
* **What it lacks**: CloudTrail does not record payload data (prompts/responses) or token counts.

------------------------------
## 4. Advanced Cost Tracking: Application Inference Profiles
If the main goal is tracking costs across different teams, applications, or environments:

   1. Create an [Application Inference Profile](https://docs.aws.amazon.com/bedrock/latest/userguide/inference-profiles.html).
   2. Assign Cost Allocation Tags to that profile.
   3. Route the inference requests through the profile's ARN instead of the base model ID. This maps the consumption metadata directly to the [AWS Cost Explorer](https://aws.amazon.com/aws-cost-management/aws-cost-explorer/).
[7] [https://dev.to](https://dev.to/aws-builders/bedrock-inference-profiles-from-flying-blind-to-understanding-your-aws-bedrock-usage-in-detail-l6l)
[8] [https://aws.amazon.com](https://aws.amazon.com/blogs/mt/monitoring-generative-ai-applications-using-amazon-bedrock-and-amazon-cloudwatch-integration/)
