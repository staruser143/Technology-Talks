**AWS RAM** stands for **AWS Resource Access Manager**.

It is a service that lets you **securely share AWS resources across AWS accounts**, **within an AWS Organization**, or **with external AWS accounts**, **without copying or recreating those resources**.

***

## 1. What problem AWS RAM solves

In large enterprises, you often have:

*   Multiple AWS accounts (prod, non-prod, shared services, teams)
*   Common or centrally managed resources

Without RAM, you would have to:

*   Duplicate resources
*   Manually coordinate permissions
*   Manage complex cross-account IAM policies

👉 **AWS RAM enables centralized ownership with distributed usage.**

***

## 2. How AWS RAM works (high level)

1.  **Resource owner account**
    *   Owns the resource (e.g., subnet, Transit Gateway, license)
2.  **Create a RAM resource share**
    *   Select resources to share
    *   Select principals (AWS accounts, OU, or Organization)
3.  **Recipient account**
    *   Accepts the share (auto-accepted for AWS Organizations)
    *   Uses the resource **as if it existed locally**, but cannot modify ownership

✅ **Ownership stays with the source account**  
✅ **Usage happens in the recipient account**

***

## 3. Common AWS resources that can be shared via RAM

### Networking (most common in SAP‑C02)

*   VPC subnets
*   AWS Transit Gateway
*   Transit Gateway route tables
*   AWS Network Firewall policies
*   IPAM pools

### Compute & services

*   EC2 Capacity Reservations
*   Outposts resources
*   App Mesh meshes

### Data & analytics

*   Amazon Aurora clusters
*   Glue Data Catalog resources
*   Lake Formation databases and tables

### Other

*   AWS License Manager licenses

> 💡 **You cannot share IAM roles, S3 buckets, or Lambda functions via RAM.**

***

## 4. Typical enterprise patterns using AWS RAM

### ✅ Shared Services VPC pattern

*   Central networking account owns:
    *   VPC
    *   Subnets
    *   Transit Gateway
*   Application accounts:
    *   Launch EC2/EKS/RDS into **shared subnets**

### ✅ Central Transit Gateway pattern

*   One TGW shared to:
    *   Many application accounts
    *   Many VPCs
*   Simplifies hub‑and‑spoke networking

### ✅ Data governance pattern

*   Central data account shares:
    *   Glue / Lake Formation catalogs
*   Consumer teams query shared data without duplication

***

## 5. AWS RAM vs similar mechanisms (exam-safe)

| Need                              | Service        |
| --------------------------------- | -------------- |
| Share physical AWS resources      | **AWS RAM** ✅  |
| Cross-account API access          | IAM AssumeRole |
| Organization-wide guardrails      | SCP            |
| Fine-grained data access          | Lake Formation |
| Network private access to service | PrivateLink    |

👉 **AWS RAM ≠ IAM permissions**

RAM shares *resources*, not *permissions*.

***

## 6. Security & governance notes (important for exams)

*   RAM works best with **AWS Organizations**
    *   Auto-accept sharing
    *   OU-level targeting
*   Recipient accounts:
    *   **Can use**
    *   **Cannot delete or modify ownership**
*   IAM permissions are still required separately
*   Supports **resource-level isolation and blast‑radius control**

***

## 7. One‑line exam definition

> **AWS RAM allows an AWS account to share supported AWS resources with other AWS accounts or OUs so they can be used without duplication, while ownership remains centralized.**

***

