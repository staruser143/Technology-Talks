# Amazon SageMaker Multi-Container Endpoints (MCE) 
- Allow us to deploy up to 15 different serving containers—each potentially running a completely different machine learning framework, runtime environment, or custom image—on a single, shared endpoint behind common compute instances.


## How Multi-Container Endpoints Work
- When you create an MCE, you define multiple container specifications inside a single SageMaker Model. 
- Each container can run its own independent image (e.g., PyTorch, TensorFlow, Scikit-learn, Triton) and point to distinct model artifacts.

MCE operates in two distinct execution modes configured via InferenceExecutionConfig.Mode:
 * **Direct Mode (Direct)**:
   * The caller invokes a specific container on the shared endpoint using the TargetContainerHostname request parameter or header.
   * Enables consolidating multiple unrelated models with low or varying traffic onto a single set of instances without tying them together into a pipeline.
 * **Serial Mode (Serial / Inference Pipelines)**:
   * Requests flow sequentially through the containers in a chain (e.g., Container 1: Scikit-learn data pre-processing -> Container 2: PyTorch/XGBoost inference -> Container 3: Custom post-processing).
   * SageMaker manages passing intermediate outputs between containers with low-latency inter-process communication on the host.

## Key Scenarios: When to Use Multi-Container Endpoints
| Scenario | Why MCE Fits |
|---|---|
| Heterogeneous Frameworks / Environments | We have models that cannot share a single container runtime because they depend on different frameworks (e.g., PyTorch vs. TensorFlow) or conflicting Python/CUDA dependencies. |
| End-to-End Inference Pipelines (Preprocessing & Postprocessing) | We need a sequential pipeline where feature extraction/normalization (e.g., Scikit-learn) feeds directly into an ML model, followed by scoring/formatting logic, without adding external network hops. |
| Cost Consolidation for Low-Traffic Heterogeneous Models | We have several independent models with different tech stacks, none of which receive enough traffic to justify paying for separate dedicated EC2 instances 24/7. |
| Per-Container Security and Access Controls | In direct mode, IAM policies can restrict which callers or IAM roles can invoke specific containers on the endpoint via the sagemaker:TargetContainerHostname condition key. |
Multi-Container Endpoints (MCE) vs. Multi-Model Endpoints (MME)
| Feature | Multi-Container Endpoints (MCE) | Multi-Model Endpoints (MME) |
|---|---|---|
| Serving Container | Up to 15 different container images / runtime stacks. | Single shared container image (e.g., PyTorch, Triton). |
| Model Count | Up to 15 containers/models. | Thousands of models dynamically loaded from Amazon S3. |
| Memory Strategy | All containers run concurrently in memory on the host. | Dynamic Least Recently Used (LRU) memory cache and eviction. |
| Execution Flow | Can be Serial (pipeline chain) or Direct (targeted invocation). | Targeted invocation only (using TargetModel). |
| Primary Use Case | Chained inference pipelines or hosting a few heterogeneous models together. | Large-scale multi-tenant models or adapter/LoRA hot-swapping using the same framework. |
