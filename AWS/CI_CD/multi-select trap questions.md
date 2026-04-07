Absolutely 😈 — here are **brutal multi‑select trap questions** (SAP‑C02 / DevOps Pro style) focused on **AWS Native CI/CD vs Jenkins vs GitHub Actions**, with **“select ALL that apply”** and **explanations + distractor traps**.

> **Format:** Each question is multi‑select. I’ll give **Answer key + elimination logic** right after each.

***

# 🔥 Brutal Multi‑Select Trap Set (CI/CD Tooling)

## Q1) Regulated org + AWS Organizations guardrails

A financial services company uses **AWS Organizations with SCPs**, requires **CloudTrail traceability** for pipeline actions, and wants the least operational overhead. Which choices best satisfy the requirement? *(Select ALL that apply)*

A. AWS CodePipeline + CodeBuild + CodeDeploy with service roles  
B. Jenkins on EC2 with IAM instance profile and CloudTrail  
C. GitHub Actions with OIDC federation to assume AWS roles  
D. AWS CodeCommit + CodePipeline with cross‑account actions  
E. Self‑managed GitHub runners in private subnets for compliance

✅ **Correct:** **A, C, D**  
**Why:**

*   **A** is the AWS‑native managed chain with IAM service roles and auditability.
*   **C** is valid: GitHub Actions can assume AWS roles using OIDC (common enterprise pattern), and CloudTrail records the AWS API calls made with the assumed role.
*   **D** supports AWS‑centric source + orchestration + cross‑account deployments.

❌ **Traps:**

*   **B** *can* be audited (CloudTrail logs AWS API calls), but violates “least operational overhead” (you manage Jenkins).
*   **E** improves compliance posture but increases ops and doesn’t inherently satisfy the “least overhead” requirement.

***

## Q2) “Pipeline must not have long‑lived credentials anywhere”

Your security team prohibits storing long‑lived cloud keys in CI/CD platforms. You must deploy to AWS across accounts. Which options satisfy this? *(Select ALL that apply)*

A. GitHub Actions using OIDC to assume an IAM role  
B. CodePipeline using a cross‑account role with external ID  
C. Jenkins using static access keys stored in Secrets Manager  
D. Jenkins running on EC2 using instance profile credentials  
E. GitHub Actions using encrypted secrets for AWS access keys

✅ **Correct:** **A, B, D**  
**Why:**

*   **A** uses short‑lived tokens via federation — no long‑lived keys.
*   **B** uses IAM role assumption (short‑lived STS creds), typically no static keys in the pipeline.
*   **D** uses instance profile credentials (temporary creds), avoiding stored keys.

❌ **Traps:**

*   **C** still uses **long‑lived keys**, just stored “securely.” Requirement says none anywhere.
*   **E** is explicitly long‑lived keys (even if encrypted).

***

## Q3) “Minimize blast radius” in multi‑account pipeline

A platform team needs CI/CD to deploy to **30 AWS accounts** while ensuring each target account can’t be modified outside approved actions. Which are best? *(Select ALL that apply)*

A. CodePipeline in a tooling account + cross‑account roles per target account  
B. One Jenkins server with AdministratorAccess to all 30 accounts  
C. GitHub Actions with one role that has access to all 30 accounts  
D. Per‑account IAM roles with least privilege, assumed by pipeline principal  
E. Use SCPs to block mutation outside CloudFormation in target accounts

✅ **Correct:** **A, D, E**  
**Why:**

*   **A** is the standard “tooling account” pattern.
*   **D** is least‑privilege per account (reduces blast radius).
*   **E** enforces guardrails at org level.

❌ **Traps:**

*   **B** and **C** create “one credential to rule them all” blast radius.

***

## Q4) Build workloads: large builds + caching + custom toolchain

You build a monorepo with **heavy compilation**, need **custom build tooling**, want **build caching**, and occasionally run **6‑hour integration tests**. Which options are most appropriate? *(Select ALL that apply)*

A. Jenkins with dedicated workers and persistent cache volumes  
B. GitHub Actions self‑hosted runners with persistent caches  
C. CodeBuild with default settings and no special tuning  
D. CodeBuild with compute type scaling + local caching enabled  
E. CodePipeline only (no build stage)

✅ **Correct:** **A, B, D**  
**Why:**

*   **A** best for long‑running jobs + custom infra + persistent caching.
*   **B** also fits: self‑hosted runners can be tuned and persistent.
*   **D** can fit if you tune CodeBuild (compute, caching modes), but long‑running tests can be pricey/less ergonomic.

❌ **Traps:**

*   **C** “default settings” is the trap — will be slow/costly without caching/tuning.
*   **E** CodePipeline doesn’t build; it orchestrates.

***

## Q5) “We already use GitHub; deploy to AWS; want minimal ops”

A product team uses GitHub and wants the simplest CI/CD with low ops. They deploy to ECS and Lambda. Which are good choices? *(Select ALL that apply)*

A. GitHub Actions + OIDC + deploy using AWS CLI/SDK  
B. CodePipeline triggered by GitHub source + CodeBuild + CodeDeploy  
C. Jenkins self-managed because it’s the most flexible  
D. GitHub Actions + store AWS static keys as repo secrets  
E. CodePipeline + CodeCommit only (force repo migration)

✅ **Correct:** **A, B**  
**Why:**

*   **A** is minimal ops and GitHub‑native.
*   **B** is also minimal ops and integrates well with AWS.

❌ **Traps:**

*   **C** increases ops burden.
*   **D** violates modern security preference; OIDC is better.
*   **E** adds friction (repo migration not required).

***

## Q6) “Need manual approval gates and separation of duties”

A release must pause for human approval before production. Auditors require a record of who approved. Which options support this? *(Select ALL that apply)*

A. CodePipeline manual approval action  
B. GitHub Actions environment protection rules + required reviewers  
C. Jenkins pipeline with input step + RBAC  
D. CodeBuild requires approval before build starts  
E. S3 versioning on artifacts is enough

✅ **Correct:** **A, B, C**  
**Why:**

*   **A** has a native approval action.
*   **B** supports approvals with protected environments.
*   **C** supports approvals using pipeline input + RBAC integration.

❌ **Traps:**

*   **D** CodeBuild is a build service, not a governance/approval gate.
*   **E** versioning is not approval workflow.

***

## Q7) “We need on‑prem connectivity and can’t expose build to internet”

Build jobs must run in a VPC with access to on‑prem systems (private network). Which options fit? *(Select ALL that apply)*

A. CodeBuild running inside a VPC (VPC configuration)  
B. GitHub Actions hosted runners (SaaS)  
C. GitHub Actions self-hosted runners inside VPC  
D. Jenkins agents inside VPC/on-prem  
E. CodePipeline alone inside VPC executes builds

✅ **Correct:** **A, C, D**  
**Why:**

*   **A** supports VPC builds.
*   **C** and **D** place runners/agents inside private networks.

❌ **Traps:**

*   **B** hosted runners won’t access private on‑prem unless you open connectivity outward (often disallowed).
*   **E** CodePipeline orchestrates; it doesn’t “run builds.”

***

## Q8) “We want plug‑ins and non‑standard integrations”

A team needs deep integration with proprietary tools and wants a huge ecosystem of plugins. Which is best? *(Select ALL that apply)*

A. Jenkins  
B. CodePipeline  
C. CodeBuild  
D. GitHub Actions marketplace actions  
E. CodeDeploy only

✅ **Correct:** **A, D**  
**Why:**

*   **A** is plugin king.
*   **D** has a large marketplace of reusable actions.

❌ **Traps:**

*   **B/C** are extensible but not plugin‑centric like Jenkins.
*   **E** is just deployment.

***

## Q9) “Cross-cloud: deploy to AWS + Azure + on‑prem Kubernetes”

A company must use one CI/CD tool to deploy to AWS, Azure, and on‑prem K8s consistently. Which are strong fits? *(Select ALL that apply)*

A. Jenkins  
B. GitHub Actions  
C. AWS CodePipeline as the only orchestrator  
D. CodeDeploy  
E. CodeBuild only

✅ **Correct:** **A, B**  
**Why:**

*   Jenkins and GitHub Actions are commonly used cross‑cloud orchestrators.

❌ **Traps:**

*   **C** can integrate externally but is AWS‑centric; in “one tool for all” exam language, it’s usually not the best fit.
*   **D** is AWS deployments only.
*   **E** is build only.

***

## Q10) “Minimize ops + want event-driven triggers from AWS services”

You want pipelines triggered by changes like **ECR push**, **S3 object upload**, **EventBridge**, and prefer managed services. Which options match? *(Select ALL that apply)*

A. CodePipeline with EventBridge triggers  
B. GitHub Actions triggered by CloudWatch Events directly  
C. Jenkins polling repos on a schedule  
D. CodePipeline source from S3/ECR events  
E. GitHub Actions with a webhook + AWS event bridge integration (custom)

✅ **Correct:** **A, D, E**  
**Why:**

*   **A** is common AWS-native event orchestration.
*   **D** supports AWS event sources patterns.
*   **E** is possible via integration, but not as “native” as CodePipeline.

❌ **Traps:**

*   **B** is not a direct native trigger from AWS events (typically you bridge with webhooks/custom).
*   **C** polling increases ops + cost and is not event-driven.

***

## Q11) “We need predictable cost + constant pipeline usage”

A team runs thousands of builds/day. They prefer predictable cost and can manage infra. Which options can satisfy? *(Select ALL that apply)*

A. Jenkins with reserved capacity (EC2/ASG)  
B. GitHub Actions self-hosted runners on fixed capacity  
C. CodeBuild (pay per minute) always cheapest  
D. GitHub Actions hosted runners only  
E. CodePipeline replaces build compute costs

✅ **Correct:** **A, B, D** *(depending on pricing model and concurrency)*  
**Why:**

*   Fixed capacity infra can make costs predictable.
*   Hosted runners can be predictable if your plan and usage are stable.

❌ **Traps:**

*   **C** pay‑per‑use isn’t always cheapest at high volume.
*   **E** CodePipeline is orchestration, not build compute.

***

## Q12) “Disaster recovery + high availability of the CI/CD system”

Which statements are true? *(Select ALL that apply)*

A. With Jenkins, you must design HA/DR for controllers, agents, storage, and secrets  
B. CodePipeline is managed; AWS handles service availability  
C. GitHub Actions hosted runners require you to manage runner availability  
D. Self-hosted GitHub runners require HA planning by you  
E. CodeBuild requires you to manage patching and AMI updates

✅ **Correct:** **A, B, D**  
**Why:**

*   **A** Jenkins is self-managed.
*   **B** Managed service.
*   **D** Self-hosted runner fleet is yours.

❌ **Traps:**

*   **C** hosted runners are GitHub-managed.
*   **E** CodeBuild is managed; you don’t patch instances.

***

# 🧾 Rapid Scorecard (How “Brutal” Traps Work)

Most traps hit one of these patterns:

1.  **CodePipeline ≠ build/deploy** (it orchestrates)
2.  **“No long-lived credentials” → OIDC / roles / instance profiles**
3.  **Blast radius** → tooling account + per-target role + SCP
4.  **Low ops** → managed (CodePipeline/CodeBuild/CodeDeploy) or SaaS (GHA)
5.  **Flexibility** → Jenkins (but ops heavy)

***

