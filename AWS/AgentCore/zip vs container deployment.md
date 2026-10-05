Amazon Bedrock AgentCore Runtime provides two distinct deployment methods to balance developer velocity with enterprise flexibility: **Direct Code Deployment (ZIP)** and **Container-Based Deployment**. 

While both methods execute your agent inside the same secure, serverless microVM environment and have access to all AgentCore primitives (Memory, Identity, Gateway, etc.), they differ fundamentally in **who manages the build environment and the underlying OS dependencies**.

Here is a detailed breakdown of the differences and how to choose the right approach for your healthcare and insurance use cases.

---

### 1. Direct Code Deployment (ZIP File)
In this model, you package your agent’s source code and a dependency manifest (e.g., Python’s `requirements.txt` or Node.js’s `package.json`) into a `.zip` archive. You upload this directly to AgentCore via the AWS CLI, Console, or the AgentCore CLI (`agentcore deploy`). 

* **How it works:** AgentCore’s managed build service intercepts your ZIP file, provisions a temporary build environment, installs your dependencies based on your manifest, and packages the final runtime artifact into the microVM.
* **The Developer Experience:** It feels like traditional serverless (e.g., AWS Lambda). You focus entirely on your agent logic and prompts. There are no Dockerfiles to write, no container registries to manage, and no local Docker daemon required.

**Pros:**
* **Rapid Iteration:** You can deploy a code change in seconds. Ideal for tweaking prompts, adjusting tool schemas, or testing new agent frameworks.
* **Zero Infrastructure Management:** No need to maintain container images, ECR repositories, or CI/CD pipelines for image building.
* **Fast Prototyping:** Gets a Proof of Concept (PoC) into a production-like environment almost instantly.

**Cons:**
* **Limited OS-Level Control:** You cannot install custom system-level packages (e.g., via `apt-get` or `yum`). You are restricted to the libraries available in the standard managed runtime.
* **Dependency Resolution Limits:** If your agent requires highly specific, non-standard binary dependencies or proprietary C++ libraries, the managed build environment may not support them.
* **Build Times:** For massive dependency trees, the managed build step can add a few minutes to the deployment process.

---

### 2. Container-Based Deployment
In this model, you write a `Dockerfile`, build a container image, push it to a registry like Amazon Elastic Container Registry (ECR), and provide the image URI to AgentCore Runtime.

* **How it works:** AgentCore pulls your pre-built image and executes it directly inside the microVM. You have complete control over the base OS, the file system, and the environment variables.
* **The Developer Experience:** It feels like traditional container orchestration. You are responsible for the image lifecycle, security scanning, and registry management.

**Pros:**
* **Complete Environment Control:** You can install *any* OS-level dependency, custom binary, or proprietary system library your agent requires.
* **Portability & Consistency:** "Works on my machine" translates directly to production. The exact same image can be tested locally or deployed to Amazon EKS if you adopt a hybrid architecture.
* **Enterprise DevSecOps Integration:** Fits seamlessly into existing enterprise CI/CD pipelines that mandate immutable, pre-scanned artifacts.

**Cons:**
* **Higher Operational Overhead:** Requires expertise in Docker, container security scanning, and managing ECR repositories.
* **Slower Iteration Loop:** Changing a single line of code requires rebuilding the image, pushing it to ECR, and updating the AgentCore deployment configuration.

---

### How to Choose: Scenarios for Healthcare & Insurance

When deciding between the two, the choice usually comes down to **dependency complexity**, **security/compliance mandates**, and **development lifecycle maturity**.

#### Scenario A: Choose ZIP (Direct Code) When...
1. **Building Customer-Facing Service Bots:** You are building a standard policy FAQ bot or a claims status tracker using popular Python frameworks (like Strands or LangChain) that only rely on standard `pip` packages and REST API calls.
2. **Rapid Prototyping & PoCs:** Your team is exploring a new use case (e.g., an AI agent to summarize medical records) and needs to iterate on prompts and tool selections daily without waiting for container builds.
3. **Standardized Tech Stacks:** Your enterprise has standardized on a specific Python or Node.js version, and all required libraries are readily available in public package managers.

#### Scenario B: Choose Container When...
1. **Complex Document Processing & Proprietary Tools:** Your insurance agent needs to parse complex, legacy medical documents using a proprietary, C++-based OCR engine or a specialized HL7/FHIR binary wrapper that requires specific OS-level shared libraries (`.so` files) not available via `pip`.
2. **Strict Enterprise SecOps Mandates:** Your security and compliance team requires that all production code is packaged into immutable, pre-scanned container images (using tools like Trivy or Clair) and stored in a centrally governed ECR repository before deployment. They do not allow code to be built "on the fly" by a managed service.
3. **Hybrid / Multi-Compute Portability:** You want the flexibility to run the exact same agent codebase on **AgentCore Runtime** for bursty, serverless scaling, but also deploy it to your internal **Amazon EKS** cluster for high-throughput, always-on workloads or GPU-accelerated local inference.
4. **Custom Data Science Environments:** Your agent relies on heavy, custom-compiled machine learning models or specific data science libraries that require a highly customized Linux environment to function correctly.

### Summary Recommendation
For most healthcare and insurance enterprises, a **phased approach** is highly effective:
* **Start with ZIP** during the design, development, and PoC phases to maximize developer velocity and rapidly validate the agent's reasoning and tool-calling capabilities.
* **Transition to Containers** when the agent is ready for production, especially if it requires proprietary healthcare data parsers, must pass strict enterprise security audits, or needs to be portable across your broader AWS compute landscape (EKS/ECS).