Amazon Bedrock Cross-Region Inference is a managed capability that automatically and dynamically routes foundation model (FM) invocation requests across multiple AWS Regions. Instead of hardcoding a specific regional model endpoint, callers invoke a pre-configured inference profile (e.g., us.anthropic.claude-3-5-sonnet-... or eu.anthropic.claude-3-5-sonnet-...).
Bedrock monitors regional traffic conditions and transparently shifts requests to regions with available capacity without requiring developers to build custom cross-region load balancing, retry, or failover logic.
How It Works
 * Unified Inference Profiles: Applications reference an inference profile ID or ARN rather than a single regional model ID.
 * No Added Routing Cost: Invocations are billed at standard On-Demand token pricing for the model in the source region.
 * Compliance Boundaries (Geographic Routing): Routing stays within defined geographic boundaries (e.g., US regions, EU regions) to satisfy local data handling requirements.
 * High Throughput Limits: Quotas (Tokens Per Minute / Requests Per Minute) are aggregated across the participating regions, providing significantly higher throughput than single-region allocations.
Key Scenarios for Cross-Region Inference
| Scenario | Why Cross-Region Inference Fits |
|---|---|
| Mitigating Bursty Traffic & Throttling (429 Too Many Requests) | Prevents ThrottlingException errors during unpredictable spikes by shifting excess requests to adjacent regions with idle capacity. |
| High Availability & Region Resilience | Automatically bypasses localized service degradation or transient regional capacity bottlenecks without maintaining manual failover scripts. |
| Geographic Data Residency & Jurisdictional Compliance | For workloads that must comply with regional boundaries (such as GDPR in the EU or US federal constraints), a geographic profile ensures requests never leave that geopolitical boundary. |
| Bypassing Single-Region Quota Limits | When an application scales beyond a single AWS region's on-demand account limits (RPM/TPM), cross-region profiles deliver near double or more the aggregate throughput. |
| Multi-Region Application Routing Without Custom Middleware | Replaces brittle client-side fallback architectures (e.g., Step Functions circuit breakers or Lambda try-catch cross-region retries) with native AWS managed routing. |
When Not to Use It
 * Strict Single-Region Mandates: If compliance or regulatory rules require data to be processed within one specific data center or AWS region (rather than a geographic region like the EU or US), cross-region profiles cannot be used.
 * Provisioned Capacity Requirements: Cross-Region Inference applies to On-Demand model invocations. For dedicated, SLA-backed capacity or custom fine-tuned weights, Bedrock Provisioned Throughput (Model Units) in a specific target region is required.
