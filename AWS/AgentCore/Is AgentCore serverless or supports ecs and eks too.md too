Amazon Bedrock AgentCore is fundamentally designed as a **serverless, managed platform**, but it offers flexible deployment models and deep integration options that allow you to leverage its capabilities alongside **Amazon ECS** or **Amazon EKS**. 

Here is a breakdown of how AgentCore handles deployment and how it interacts with container orchestration platforms:

### 1. AgentCore Runtime: Managed Serverless Execution
The core hosting environment, **AgentCore Runtime**, is a fully managed, serverless service [[10]]. You do not provision or manage underlying servers, clusters, or nodes. 
* **Deployment Methods**: It supports both **direct code upload** (e.g., packaging your agent code and dependencies in a `.zip` file for rapid iteration) and **container-based deployment** (providing a container image for complex use cases requiring custom native dependencies) [[18]]. 
* **Execution**: Even when you provide a container image, AgentCore Runtime executes it within its own secure, purpose-built **microVM-based serverless environment**, not on a customer-managed ECS or EKS cluster [[11]]. This provides hardware-level session isolation, automatic scaling to zero, and consumption-based pricing (you only pay for active compute time, not during I/O waits) [[11]].

### 2. The Hybrid Approach: Hosting Agents on ECS/EKS + AgentCore Services
If your enterprise requires running the actual agent compute workload on your own Amazon ECS or Amazon EKS clusters (e.g., for strict internal networking policies, existing Kubernetes investments, or specific GPU requirements), you can still use AgentCore’s enterprise-grade services. AgentCore is designed as a set of **composable primitives** that can be consumed via API/SDK from anywhere [[15]].

Common hybrid patterns include:
* **AgentCore Identity on ECS/EKS**: You can run your agent application in an ECS task or EKS pod and use AgentCore Identity to handle complex inbound authentication (OIDC/JWT) and secure outbound OAuth 2.0 token vaulting for third-party tools (e.g., EHR systems or claims databases) [[15]].
* **AgentCore Gateway**: You can use the Gateway to transform your existing APIs or Lambda functions (which may be fronting ECS/EKS services) into secure, agent-ready Model Context Protocol (MCP) tools with built-in throttling and authorization [[11]].
* **AgentCore Observability**: Your ECS/EKS-hosted agent can emit OpenTelemetry traces directly to AgentCore Observability for unified, step-by-step debugging and monitoring alongside other AgentCore services [[11]].

### 3. Trade-offs: AgentCore Runtime vs. Self-Managed ECS/EKS
If you are deciding between using the managed AgentCore Runtime versus building your own agent runtime on EKS/ECS, consider the following operational differences [[11]]:

| Capability | Amazon Bedrock AgentCore Runtime | Self-Managed on Amazon EKS / ECS |
| :--- | :--- | :--- |
| **Deployment** | CLI-driven (`agentcore deploy`), direct code or container image upload. No Dockerfiles or Helm charts required. | Requires Dockerfiles, Helm charts/Kubernetes manifests, CI/CD pipelines, and ingress configuration. |
| **Isolation** | Hardware-level microVM isolation per session (prevents cross-tenant data leakage). | Container-level namespace isolation (requires additional tools like Kata Containers/gVisor for stronger isolation). |
| **Scaling** | Serverless, consumption-based, scales to zero automatically. | Requires configuring Horizontal Pod Autoscalers (HPA), Cluster Autoscaler, or Karpenter. |
| **Agent Memory** | Managed short-term and long-term memory primitives. | You must provision and manage Redis, vector databases, and custom extraction logic. |
| **Security & Policy** | Built-in token vaulting and deterministic Cedar-based policy enforcement at the Gateway layer. | You must build custom OAuth flows, manage secrets, and implement policy decision points (e.g., Open Policy Agent). |

### Recommendation for Healthcare & Insurance
For regulated domains handling PHI or sensitive claims data, **AgentCore Runtime (serverless)** is generally recommended because it eliminates entire categories of infrastructure misconfiguration risks. Its built-in microVM session isolation, VPC/PrivateLink support, and HIPAA-eligible architecture reduce the compliance burden significantly [[11]]. 

However, if your organization mandates that all compute workloads reside in a centrally governed EKS cluster, the **hybrid approach** is fully supported: host the agent logic on EKS, and delegate identity, tool gateway routing, and observability to AgentCore to maintain enterprise-grade security and auditability [[15]].