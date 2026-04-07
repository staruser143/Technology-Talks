AWS provides a **full, native CI/CD toolchain** that can be used end‑to‑end or mixed with third‑party tools (GitHub, GitLab, Jenkins). Below is a **clean, architect‑level breakdown**, mapped to the CI/CD lifecycle and **exam‑relevant**.

***

## 🔁 AWS CI/CD Services — By Pipeline Stage

### 1️⃣ Source Control (Code)

Used to store and version application code.

| Service                        | Purpose                                        |
| ------------------------------ | ---------------------------------------------- |
| **AWS CodeCommit**             | Fully managed Git-based source control         |
| **Amazon S3**                  | Artifact source (zip, static assets)           |
| **GitHub / GitHub Enterprise** | External source (via CodePipeline integration) |
| **Bitbucket / GitLab**         | External integrations                          |

✅ **Exam tip:** AWS accepts **external sources** as first‑class citizens.

***

### 2️⃣ Build & Test (CI)

Compiles code, runs tests, creates artifacts.

| Service           | Purpose                                  |
| ----------------- | ---------------------------------------- |
| **AWS CodeBuild** | Fully managed build service (no servers) |
| **Buildspec.yml** | Declarative build/test definition        |

Key features:

*   Supports **Docker builds**
*   Runs **unit, integration tests**
*   Outputs **build artifacts**
*   Pay-per-minute

✅ **Exam traps:**

*   No persistent state between builds
*   Builds run in isolated containers

***

### 3️⃣ Artifact Storage

Where build outputs are stored.

| Service              | Purpose                                  |
| -------------------- | ---------------------------------------- |
| **Amazon S3**        | Default artifact store                   |
| **Amazon ECR**       | Container image registry                 |
| **AWS CodeArtifact** | Dependency repository (npm, Maven, PyPI) |

✅ **Exam tip:**  
ECR ≠ CodeArtifact (containers vs libraries).

***

### 4️⃣ Pipeline Orchestration (CD Control Plane)

Defines stages and transitions.

| Service              | Purpose                   |
| -------------------- | ------------------------- |
| **AWS CodePipeline** | Orchestrates CI/CD stages |

Capabilities:

*   Source → Build → Test → Deploy
*   Manual approval gates
*   Event-based triggers
*   Supports **cross-account deployments**

✅ **Exam tip:**  
CodePipeline **does NOT do builds or deployments itself**.

***

### 5️⃣ Deployment (CD Execution)

Delivers code into environments.

| Service                   | Used For                                  |
| ------------------------- | ----------------------------------------- |
| **AWS CodeDeploy**        | EC2, Auto Scaling, Lambda, ECS            |
| **AWS Elastic Beanstalk** | Platform-managed app deployments          |
| **AWS CloudFormation**    | Infrastructure as Code deployments        |
| **AWS CDK**               | Higher-level IaC (synth → CloudFormation) |

#### CodeDeploy deployment types:

*   🔵 **In‑place** (EC2)
*   🟢 **Blue/Green** (EC2, ECS, Lambda)

✅ **Exam trap:**  
Blue/Green for EC2 **requires load balancer**.

***

### 6️⃣ Testing & Quality Gates (Optional)

| Service             | Purpose                        |
| ------------------- | ------------------------------ |
| **AWS Device Farm** | Mobile/web app testing         |
| **Manual Approval** | Human gate in CodePipeline     |
| **Lambda Hooks**    | Pre/post deployment validation |

***

### 7️⃣ Observability & Security

| Service               | Purpose                        |
| --------------------- | ------------------------------ |
| **Amazon CloudWatch** | Logs, metrics, alarms          |
| **AWS CloudTrail**    | Audit trail                    |
| **AWS IAM**           | Least‑privilege pipeline roles |
| **Amazon Inspector**  | Vulnerability scanning         |
| **AWS Config**        | Compliance tracking            |

✅ **Exam tip:**  
Each CI/CD service uses **its own service role**.

***

## 🔧 Typical AWS Native CI/CD Pipeline

    GitHub / CodeCommit
            ↓
       CodePipeline
            ↓
        CodeBuild
            ↓
       (S3 / ECR)
            ↓
        CodeDeploy
            ↓
     EC2 / ECS / Lambda

***

## 🧠 When to Use What (Quick Decision Guide)

| Need                   | Service              |
| ---------------------- | -------------------- |
| Git hosting            | CodeCommit           |
| Build/test             | CodeBuild            |
| Pipeline orchestration | CodePipeline         |
| EC2/Lambda deployment  | CodeDeploy           |
| Infra deployment       | CloudFormation / CDK |
| Containers             | ECR + ECS/EKS        |
| Dependencies           | CodeArtifact         |

***

## 🎯 Exam‑Focused One‑Liners

*   **CodePipeline** = orchestration only
*   **CodeBuild** = stateless CI engine
*   **CodeDeploy** = deployment strategies
*   **CloudFormation/CDK** = infra as code, not CI
*   **ECR != CodeArtifact**
*   Native CI/CD is **IAM-heavy**

***

