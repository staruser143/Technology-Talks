Here’s a **clean, exam‑grade decision tree** that tells you **when to use AWS Native CI/CD vs Jenkins vs GitHub Actions**, with **architectural intent + exam trap awareness**.

Think of this as a **mental flowchart** you can replay during SAP‑C02 / DevOps Pro / interviews.

***

## 🧠 CI/CD Tooling — High‑Signal Decision Tree

    START
     |
     |-- Is source code primarily hosted on GitHub?
     |        |
     |        |-- YES
     |        |     |
     |        |     |-- Do you want minimal infra, fastest setup, repo-native CI?
     |        |     |        |
     |        |     |        |-- YES --> ✅ GitHub Actions
     |        |     |        |
     |        |     |        |-- NO  --> Continue →
     |        |     |
     |        |     |-- Do you need complex workflows, cross-cloud, custom plugins?
     |        |              |
     |        |              |-- YES --> ✅ Jenkins
     |        |              |-- NO  --> ✅ GitHub Actions
     |
     |-- NO (AWS CodeCommit / Bitbucket / Mixed repos)
     |
     |-- Do you require tight AWS integration with IAM, CloudTrail, SCPs?
     |        |
     |        |-- YES
     |        |     |
     |        |     |-- Strong compliance / audit / least-privilege needs?
     |        |     |        |
     |        |     |        |-- YES --> ✅ AWS Native CI/CD
     |        |     |        |
     |        |     |        |-- NO  --> Continue →
     |        |
     |        |-- NO --> Continue →
     |
     |-- Do you need full control over build agents, plugins, custom runners?
     |        |
     |        |-- YES --> ✅ Jenkins
     |        |
     |        |-- NO
     |
     |-- Do you want serverless, low-ops, pay-for-use CI/CD?
     |        |
     |        |-- YES --> ✅ AWS Native CI/CD OR GitHub Actions
     |        |
     |        |-- NO  --> ✅ Jenkins

***

## ✅ Final Recommendation Outcomes (Why Each Wins)

***

### ✅ **Choose AWS Native CI/CD (CodePipeline + CodeBuild + CodeDeploy)** when:

✔ You are **AWS‑centric**
✔ IAM, SCPs, CloudTrail logging matter  
✔ Regulated environment (healthcare, finance)
✔ Cross‑account deployments required
✔ Want **managed, opinionated CI/CD**

**Typical use cases**

*   SAP‑C02 exam scenarios
*   Enterprise AWS orgs
*   Infrastructure + app pipelines together
*   Blue/Green with ALB, ECS, Lambda

**Hidden exam clue**

> “Must integrate with IAM/SCPs and provide audit logs”

✅ **AWS Native CI/CD**

***

### ✅ **Choose GitHub Actions** when:

✔ Code lives on GitHub  
✔ Want **fastest setup**
✔ Event‑driven workflows (`on: push`, `pull_request`)
✔ Minimal ops
✔ SaaS‑style CI/CD is acceptable

**Typical use cases**

*   Product teams
*   Startup / mid‑size orgs
*   App‑focused CI/CD
*   Open source projects

**Exam trap**

> GitHub Actions is **not AWS‑managed** despite AWS deployments being possible

***

### ✅ **Choose Jenkins** when:

✔ Maximum flexibility needed  
✔ Complex pipelines / custom logic
✔ Custom plugins / legacy builds
✔ Hybrid / multi‑cloud / on‑prem
✔ Long‑running jobs, exotic toolchains

**Typical use cases**

*   Legacy enterprises
*   Mainframe / C++ / heavy builds
*   Hybrid (on‑prem + cloud)

**Architect warning**

*   You own **scaling, patching, backups**
*   High operational overhead

***

## 🧭 One‑Glance Comparison (Exam‑Ready)

| Dimension        | AWS Native | GitHub Actions | Jenkins        |
| ---------------- | ---------- | -------------- | -------------- |
| Managed service  | ✅ Fully    | ✅ SaaS         | ❌ Self‑managed |
| AWS IAM native   | ✅ Yes      | ❌ No           | ❌ No           |
| Infra to manage  | ❌ None     | ❌ None         | ✅ Yes          |
| Plugin ecosystem | ❌ Limited  | ⚠️ Growing     | ✅ Massive      |
| Multi‑cloud      | ⚠️ Limited | ✅ Good         | ✅ Excellent    |
| Exam‑favorite    | ✅✅✅        | ⚠️             | ⚠️             |

***

## 🎯 SAP‑C02 / DevOps Pro Exam Heuristics

**If the question says…**

*   “Comply with least privilege” → ✅ AWS CI/CD
*   “Already using GitHub” → ✅ GitHub Actions
*   “Custom build tooling / hybrid” → ✅ Jenkins
*   “Minimize operational overhead” → ✅ AWS CI/CD / GitHub Actions
*   “Audit & traceability required” → ✅ AWS CI/CD

***

## ⚠️ Common Exam Traps

❌ “CodePipeline builds code” → **Wrong** (CodeBuild does)  
❌ “Jenkins is cheaper” → **Usually false (ops cost)**  
❌ “GitHub Actions replaces CodePipeline” → **Context‑dependent**  
❌ “AWS CI/CD works only with AWS services” → **Can integrate externally**

***

