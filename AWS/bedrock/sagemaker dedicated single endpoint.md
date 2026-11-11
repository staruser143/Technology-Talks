# Amazon SageMaker Dedicated Single-Model Endpoint
- Hosts a single, specific machine learning model on one or more dedicated compute instances (such as EC2 ml.m5, ml.c5, or GPU-backed ml.g5/ml.p4d instances) provisioned exclusively for that workload.
- Unlike Multi-Model Endpoints (MME) — which share instances and dynamically load models from S3—or Multi-Container Endpoints (MCE)—which pack multiple containers onto shared hardware—a dedicated single-model endpoint assigns 100% of the underlying compute, RAM/VRAM, and network bandwidth to a single model artifact and its serving container.

## Key Architectural Characteristics
 * **1:1 Resource Allocation**: The model stays continuously loaded in memory (RAM or GPU VRAM), ensuring predictable performance without container contention.
 * **Zero Cold-Start / Dynamic Caching Latency**: Eliminates the latency spikes caused by Least Recently Used (LRU) memory eviction or downloading weights from Amazon S3 at invocation time.
 * Dedicated Auto Scaling: Scales horizontally (adding/removing EC2 instances behind the endpoint load balancer) strictly based on that single model’s real-time metrics, such as InvocationsPerInstance or target GPU/CPU utilization.
 * **Independent Lifecycle & Canary Deployments**: Enables isolated zero-downtime Blue/Green updates, linear or canary traffic shifting, and automatic rollbacks via SageMaker Deployment Guardrails and Model Registry integration without risking other models.

## When to Consider Using Dedicated Single-Model Endpoints

| Scenario | Why Dedicated Endpoints Fit |
|---|---|
| Strict, Ultra-Low Latency SLAs | Applications (such as algorithmic trading, real-time fraud detection, or interactive search ranking) where P99 latency must stay in single-digit milliseconds and cannot tolerate cold-start downloads. |
| High, Sustained 24/7 Traffic | High-volume production models where instances maintain steady utilization; sharing resources via MME provides little cost advantage when the hardware is already fully utilized. |
| Massive Deep Learning & Large Foundation Models | Models that consume the entirety of an instance's memory or require multi-GPU tensor parallelism (e.g., Llama 70B, Falcon, Stable Diffusion XL) and cannot fit alongside other models. |
| Isolated Fault Domains & Tier-1 Workloads | Mission-critical models requiring strict operational and blast-radius isolation, preventing the noisy-neighbor effect where heavy traffic to one model starves memory or compute from another. |
| Independent Auto-Scaling Patterns | Models whose traffic patterns fluctuate rapidly (e.g., flash sales or peak-hour spikes) and need dedicated auto-scaling policies tuned specifically to their inference duration. |
| Strict Compliance & Hardware Isolation | Regulated workloads (such as HIPAA, PCI-DSS, or government contracts) that mandate dedicated, non-shared compute environments for sensitive inference pipelines. |

## Comparison: Dedicated vs. MME vs. MCE
| Dimension | Dedicated Single-Model | Multi-Model Endpoint (MME) | Multi-Container Endpoint (MCE) |
|---|---|---|---|
| Model Count | 1 model per endpoint | Thousands per endpoint | Up to 15 containers/models |
| Memory State | Permanently resident in RAM/VRAM | Loaded dynamically via LRU cache | All containers reside concurrently in RAM |
| Resource Isolation | Complete (no noisy neighbor) | Shared across models | Shared across containers |
| Cold Starts | None (once initialized) | Yes (on S3 cache misses) | None (once initialized) |
| Cost Profile | Highest per model (dedicated instances) | Lowest for long-tail/bursty models | Low for small sets of heterogeneous models |
