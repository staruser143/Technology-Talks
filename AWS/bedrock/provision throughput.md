# Provisioned Throughput vs On-Demand Inference

For running customized models in Amazon Bedrock—including models created via Supervised Fine-Tuning (SFT), Continued Pre-training (CPT), Model Distillation, or Reinforcement Fine-Tuning (RFT)- we can use either Provisioned Throughput or On-Demand Inference, depending on the base model family and workload profile.


## 1. Provisioned Throughput (Dedicated Capacity)
Historically, all custom models in Amazon Bedrock required Provisioned Throughput. It remains the standard deployment model for high-volume, latency-sensitive production workloads.

 * **How It Works**: We purchase dedicated compute units called Model Units (MUs) to host our custom model weights.
 * **Pricing Options**:
   * **No commitment (Hourly)**: Billed per hour per Model Unit. We can create the provisioned throughput, run evaluations or load tests, and delete it immediately when done to avoid continuous charges.
   * **Committed (1-month or 6-month)**: Provides discounted pricing for steady-state production workloads.
 * **Best Used When**:
   * We require guaranteed throughput and consistent, low latency without shared-pool queuing.
   * We have continuous, high request volume where dedicated capacity is more cost-effective per token than paying per request.
## 2. Custom Model On-Demand Deployment (Pay-as-you-go)

Amazon Bedrock also supports On-Demand Deployment for Custom Models (available for select model families like Amazon Nova and Titan).

 * **How It Works**: Rather than allocating fixed Model Units, we deploy the custom model to an on-demand deployment target. The model spins up dynamically and scales serverlessly.
 * **Pricing**: We pay only for the input and output tokens processed during model invocations, with no idle capacity charges.
 * **Best Used When**:
   * **Development and Testing**: Running ad-hoc testing, quality evaluation, or sporadic test suites where reserving dedicated Model Units would be cost-prohibitive.
   * **Low-Volume / Bursty Workloads**: Applications with intermittent user traffic, batch testing, or low daily request volumes.

### Summary Comparison
| Deployment Mode | Billing Structure | Capacity & Throughput | Primary Fit |
|---|---|---|---|
| On-Demand | Pay-per-token (input + output) | Shared, dynamically allocated capacity | Low traffic, prototyping, CI/CD validation, intermittent jobs. |
| Provisioned Throughput (Hourly / No Commit) | Hourly rate per Model Unit (MU) | Dedicated, guaranteed capacity | Load testing, short-term high-throughput benchmarks. |
| Provisioned Throughput (1/6 mo Commit) | Discounted monthly rate per MU | Dedicated, guaranteed capacity | Steady-state 24/7 production traffic with strict latency SLAs. |
