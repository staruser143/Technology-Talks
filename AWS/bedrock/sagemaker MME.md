Amazon SageMaker Multi-Model Endpoints (MME) allow you to host multiple machine learning models on a shared compute infrastructure behind a single endpoint.
Instead of provisioning dedicated EC2 instances for every single model, SageMaker dynamically loads models into instance memory (RAM or GPU VRAM) from Amazon S3 upon receiving an invocation request, managing memory and cache evictions automatically using a Least Recently Used (LRU) policy.
How Multi-Model Endpoints Work
 * Storage in Amazon S3: You store all your packaged model artifacts (e.g., model1.tar.gz, model2.tar.gz) in a shared Amazon S3 bucket prefix.
 * Targeted Invocations: When invoking the endpoint, client requests include the TargetModel parameter (or header) to specify which model should execute the prediction.
 * Dynamic Memory & LRU Caching:
   * First Request (Cold Start): SageMaker downloads the model from S3 into the host container's memory and serves the request.
   * Subsequent Requests (Warm Cache): Predictions execute directly from memory with low latency.
   * Memory Management: When memory reaches capacity, SageMaker automatically evicts the least recently used (LRU) model from RAM/VRAM to make room for incoming models.
 * Shared Serving Container: All models behind the MME generally run on the same framework container (e.g., PyTorch, XGBoost, Scikit-learn, or Triton Inference Server).
Key Scenarios: When to Use Multi-Model Endpoints
| Scenario | Why MME Fits |
|---|---|
| Multi-Tenant / Per-Customer Models | If you maintain hundreds or thousands of personalized models trained for individual clients, stores, or organizations that experience sporadic, non-concurrent usage. |
| Multi-Adapter / LoRA Deployments | In Generative AI workloads, hosting a single base foundation model on a GPU instance while dynamically hot-swapping lightweight Low-Rank Adaptation (LoRA) adapters from S3 based on incoming request metadata. |
| Geographic / Category Segmentation | Workloads with distinct models partitioned by geography or product category (e.g., city-specific real estate price models, localized demand forecasting) where traffic shifts throughout the day. |
| A/B Testing & Shadow Deployments | Comparing dozens of candidate model variations against a production baseline without provisioning duplicate compute clusters for each experimental variant. |
| Cost Optimization for Infrequently Invoked Models | Consolidating low-traffic or bursty models onto a shared pool of instances instead of paying 24/7 for underutilized dedicated real-time endpoints. |
When NOT to Use Multi-Model Endpoints
 * Heterogeneous Frameworks: If your models require completely different runtime environments and dependencies (e.g., one runs PyTorch with CUDA 12 and another runs TensorFlow or Scikit-learn), use Multi-Container Endpoints (MCE) or separate dedicated endpoints.
 * Consistently High, Non-Tolerant Latency SLAs: If every model receives massive 24/7 traffic and your application cannot tolerate the initial cold-start delay (loading weights from S3 to memory), Dedicated Single-Model Endpoints with Provisioned Concurrency are preferred.
 * Models That Exceed Total Instance Memory: If an individual model's weights alone exceed the RAM/VRAM capacity of the largest supported instance type (e.g., massive 70B+ LLMs spanning multi-node tensor parallelism), MME dynamic caching is unsuitable.
