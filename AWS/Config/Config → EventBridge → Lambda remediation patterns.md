Below are **battle‑tested remediation patterns** for **AWS Config → EventBridge → Lambda**, framed the way they show up in **SAP‑C02 / security architecture** scenarios. I’ll give you: event flow, when to use which pattern, guardrails (idempotency, safety), and concrete JSON examples.

***

# 0) Mental Model (What actually happens)

1.  **AWS Config Rule** evaluates a resource (managed or custom rule).
2.  When evaluation changes to **NON\_COMPLIANT**, Config emits an event.
3.  **Amazon EventBridge** matches the event (rule) and routes it.
4.  **Lambda** performs remediation (directly or via SSM Automation / Step Functions).
5.  (Optional) Post result back to **Security Hub / SNS / Ticketing**.

> **Key exam point:** Config is the *detector for configuration drift/compliance*, EventBridge is the *router*, Lambda (or SSM Automation) is the *actor*.

***

# 1) Pattern A — “Direct Auto‑Remediation” (Fastest path)

### ✅ Use when

*   Remediation is **safe**, **low blast radius**, and **deterministic**
*   Example: enforce encryption, remove public access, attach required tags, restrict SG inbound rules

### Flow

`Config NON_COMPLIANT → EventBridge Rule → Lambda → API call remediation`

### EventBridge rule (match NON\_COMPLIANT evaluations)

```json
{
  "source": ["aws.config"],
  "detail-type": ["Config Rules Compliance Change"],
  "detail": {
    "newEvaluationResult": {
      "complianceType": ["NON_COMPLIANT"]
    }
  }
}
```

### Lambda remediation “shape” (best practices)

*   Parse: `detail.configRuleName`, `detail.resourceType`, `detail.resourceId`
*   Remediate via AWS SDK (boto3)
*   Add **idempotency**: check current state before changing
*   Add **allowlist**: only remediate certain rule names / resource types

**Common direct fixes**

*   S3: block public access / remove public ACLs / restrict policy
*   SG: remove `0.0.0.0/0` from sensitive ports
*   EBS: snapshot + re-create encrypted volume (or enforce at launch via SCP)
*   IAM: disable old access keys (careful: business impact)

***

# 2) Pattern B — “Lambda starts SSM Automation” (Enterprise‑grade, safer)

Direct Lambda is great, but SSM Automation provides:

*   **built‑in approvals**, **rollback steps**, **audit trails**
*   **retries**, **step-level logging**
*   easier reuse across teams

### ✅ Use when

*   Remediation requires **multiple steps**
*   You want **standardized runbooks**
*   You need **approval or change-control integration**

### Flow

`Config → EventBridge → Lambda (dispatcher) → SSM Automation Runbook → Remediate`

### EventBridge target can be Lambda (dispatcher)

Lambda then calls:

*   `ssm:StartAutomationExecution` with parameters (resourceId, region, ruleName)

**Example runbook use cases**

*   SG remediation with approval step
*   Remove public S3 policy + notify owner + create ticket
*   Tag enforcement: add missing tags from CMDB mapping

**Why this is “exam‑clean”**

*   Clear separation: detection vs orchestration vs action
*   Auditable and governed

***

# 3) Pattern C — “Human‑in‑the‑Loop” (Approval Gate)

### ✅ Use when

*   Remediation could cause downtime (closing ports, revoking creds, deleting policies)
*   You need **business owner sign‑off**

### Flow options

**Option 1:** `Config → EventBridge → Step Functions → (SNS/Slack Approval) → Lambda/SSM`

**Option 2:** `Config → EventBridge → Lambda → create ticket (Jira/ServiceNow) → callback → remediation`

Even though you asked Config→EventBridge→Lambda, the “approval gate” is often done with:

*   Step Functions callback patterns, or
*   SSM Automation “Approval” step, or
*   Manual ticket workflow.

**Exam trap:** If they say “must be approved”, pure Lambda auto-fix is usually a distractor.

***

# 4) Pattern D — “Quarantine Instead of Fix” (Containment)

Sometimes you don’t “fix” immediately; you **contain**.

### ✅ Use when

*   You suspect compromise
*   You want to stop exposure first, then investigate

### Examples

*   Put instance in **quarantine security group**
*   Remove IAM policy attachment temporarily
*   Deny actions using **permission boundary** (careful to predesign)
*   Move resource to isolated subnet (harder; usually SG/ACL first)

### Flow

`Config NON_COMPLIANT → EventBridge → Lambda → containment action + notify`

This is extremely common in regulated enterprises.

***

# 5) Pattern E — “Owner‑Aware Remediation” (Tag‑driven routing)

### ✅ Use when

*   You need to notify the right team / apply different remediations by app
*   Enterprises enforce ownership via tags (`Owner`, `AppID`, `CostCenter`)

### Flow

`Config → EventBridge → Lambda → look up tags → route remediation`

*   If `Environment=Prod` → require approval
*   If `Environment=Dev` → auto-remediate
*   If owner missing → tag as `UnknownOwner` and raise ticket

**Best practice:** Use a central mapping store (DynamoDB/CMDB) keyed by `AppID`.

***

# 6) Pattern F — “Bulk/Periodic Remediation” (When events aren’t enough)

Config events are near‑real‑time, but enterprises also run periodic sweeps.

### ✅ Use when

*   You want daily/weekly enforcement
*   You want to backfill missed events
*   You want to remediate at scale with rate limiting

### Flow

`EventBridge Scheduler (cron) → Lambda → query Config Aggregator → remediate in batches`

This pairs nicely with Org‑wide Config aggregators.

***

# 7) Hardening & Safety Guardrails (Don’t skip this)

## A) Idempotency

Your Lambda must behave safely if triggered multiple times:

*   Re-check current state before applying change
*   Use a “remediated marker” tag or DynamoDB idempotency table  
    Example tag: `Remediation=ConfigRuleName:2026-04-03T...`

## B) Blast-radius controls

*   Allowlist rules: only remediate specific `configRuleName`s
*   Allowlist resource types
*   Block prod: require approval if `Environment=Prod`

## C) Least privilege (IAM)

Remediation Lambda roles should be **surgically scoped**:

*   Only `ec2:RevokeSecurityGroupIngress` for SG fixes
*   Only `s3:PutBucketPublicAccessBlock` for S3
*   Only `ssm:StartAutomationExecution` if using runbooks

## D) Failure handling

*   Send failed remediations to an SQS DLQ
*   Notify via SNS/Teams/Slack
*   Optionally push finding to Security Hub

***

# 8) Concrete Example Patterns (Most asked in exams)

## Example 1: “S3 bucket is public” auto-remediate

*   Config managed rule: `s3-bucket-public-read-prohibited` / `s3-bucket-public-write-prohibited`
*   Lambda remediation:
    1.  Enable Block Public Access (account or bucket)
    2.  Remove public ACL (if present)
    3.  Notify owner

## Example 2: “Security Group allows 0.0.0.0/0 on 22”

*   Config custom/managed rule
*   Lambda remediation:
    *   Revoke ingress for port 22 from 0.0.0.0/0
    *   Add exception if tag `AllowSSHFromInternet=true` (rare, but sometimes)

## Example 3: “Required tags missing”

*   Config rule: `required-tags`
*   Lambda remediation:
    *   Apply tags from CMDB (AppID → Owner/CostCenter)
    *   If AppID missing → mark noncompliant and raise ticket (don’t guess)

***

# 9) “Correct vs Works vs Distractor” (Micro cheat-sheet)

**Correct** (most enterprise-ready)

*   Config → EventBridge → Lambda → SSM Automation (runbook) → Remediate (+ approvals if needed)

**Works** (fine for simple dev)

*   Config → EventBridge → Lambda direct API fix

**Distractors**

*   CloudTrail for compliance drift (it’s API history, not compliance evaluation)
*   Security Hub alone to remediate (it aggregates findings; remediation still needs automation)
*   CloudWatch alarms to detect resource misconfiguration (not a config state evaluator)

***
