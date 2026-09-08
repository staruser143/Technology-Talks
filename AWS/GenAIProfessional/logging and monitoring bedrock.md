To log and monitor Amazon Bedrock inference requests, you can use native AWS services across three primary pillars: Model Invocation Logging (for payloads), CloudWatch Metrics (for performance and token tracking), and AWS CloudTrail (for API auditing). [1] 
------------------------------
## 1. Payload Logging: Model Invocation Logging
This feature allows you to capture full request/response text, images, embeddings, and token counts. By default, it is disabled. [2] 
## How to Enable:

   1. Open the Amazon Bedrock Console.
   2. From the left navigation pane, select Settings.
   3. Navigate to Model invocation logging and toggle it on.
   4. Select the modalities you want to log (Text, Image, Embedding, Video).
   5. Choose your destination target(s): [2, 3, 4] 

| Destination Target | Best Used For | Analysis Tool |
|---|---|---|
| Amazon CloudWatch Logs | Real-time analysis, alerting, and developer debugging. | CloudWatch Logs Insights[](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/AnalyzingLogData.html) |
| Amazon S3 | Long-term compliance storage and large-scale analytical reporting. | Amazon Athena & Amazon QuickSight |

Note: For CloudWatch Logs, you must configure an IAM Role allowing Bedrock to write to your log group. [2] 
------------------------------
## 2. Performance Monitoring: Amazon CloudWatch Metrics
Amazon Bedrock automatically streams operational metrics to CloudWatch under the AWS/Bedrock namespace. You do not need to configure anything to see these. [1] 
Key metrics to monitor via [CloudWatch dashboards](https://docs.aws.amazon.com/prescriptive-guidance/latest/implementing-logging-monitoring-cloudwatch/cloudwatch-dashboards-visualizations.html) include:

* 
* ModelInvocations: The total number of requests sent to a model.
* InvocationLatency: The time taken for the model to return a response. Use this with Output Tokens Per Second (OTPS) to diagnose latency anomalies.
* InvocationErrors: Tracks 4xx and 5xx errors to help catch throttling or model downtime.
* InputTokenCount & OutputTokenCount: Crucial for monitoring system consumption and real-time cost management. [1, 2, 5] 
* 

------------------------------
## 3. Governance and Auditing: AWS CloudTrail
To track who made the request, AWS CloudTrail automatically records administrative and runtime data plane API operations (like InvokeModel, InvokeModelWithResponseStream, and Converse). [1] 

* 
* What it logs: The IAM identity of the caller, time of the event, IP address, and target model ID.
* What it lacks: CloudTrail does not record payload data (prompts/responses) or token counts. [2, 6] 
* 

------------------------------
## 4. Advanced Cost Tracking: Application Inference Profiles
If your main goal is tracking costs across different teams, applications, or environments:

   1. Create an [Application Inference Profile](https://docs.aws.amazon.com/bedrock/latest/userguide/inference-profiles.html).
   2. Assign Cost Allocation Tags to that profile.
   3. Route your inference requests through the profile's ARN instead of the base model ID. This maps your consumption metadata directly to the [AWS Cost Explorer](https://aws.amazon.com/aws-cost-management/aws-cost-explorer/). [5, 7] 

Would you like a sample CloudWatch Logs Insights query to extract token usage from your logs, or an Athena table schema to start analyzing data dropped into S3? [2, 5, 8] 

[1] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock/latest/userguide/monitoring.html)
[2] [https://www.youtube.com](https://www.youtube.com/watch?v=6HclwW4CDRY&t=889)
[3] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock/latest/userguide/model-invocation-logging.html)
[4] [https://www.youtube.com](https://www.youtube.com/watch?v=nsSVB1yB94I)
[5] [https://medium.com](https://medium.com/@aadhith/tracking-costs-for-aws-bedrock-models-using-application-inference-profiles-and-cloudwatch-75e195a6bfed)
[6] [https://repost.aws](https://repost.aws/questions/QU1UoUsxauRpK8NYa78iJi_A/gpt-5-4-via-bedrock-mantle-endpoint-invocation-logging-metrics-and-billing)
[7] [https://dev.to](https://dev.to/aws-builders/bedrock-inference-profiles-from-flying-blind-to-understanding-your-aws-bedrock-usage-in-detail-l6l)
[8] [https://aws.amazon.com](https://aws.amazon.com/blogs/mt/monitoring-generative-ai-applications-using-amazon-bedrock-and-amazon-cloudwatch-integration/)
