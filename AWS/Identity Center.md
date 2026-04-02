**AWS IAM Identity Center** (formerly **AWS Single Sign-On**) is used to **centrally manage workforce identities and provide single sign‑on (SSO) access** to **multiple AWS accounts and business applications**.

***

## What IAM Identity Center is used for

### 1. **Centralized workforce identity management**

IAM Identity Center allows you to:

*   Create and manage **users and groups** in one place **or**
*   Connect to an **external identity provider (IdP)** such as:
    *   Microsoft Entra ID (Azure AD)
    *   Okta
    *   Ping Identity
    *   On‑premises Active Directory

👉 This avoids managing IAM users separately in each AWS account.

***

### 2. **Single Sign‑On (SSO) to AWS accounts**

Users can:

*   Sign in **once**
*   Access **multiple AWS accounts** and **roles** without re‑authenticating

✅ Especially useful in **AWS Organizations (multi‑account setups)**.

Example:

*   A developer logs in once and accesses:
    *   Dev account → DeveloperRole
    *   Test account → PowerUserRole
    *   Prod account → ReadOnlyRole

***

### 3. **Permission management using permission sets**

Instead of managing IAM roles manually in each account:

*   You define **permission sets** (collections of IAM policies)
*   Assign them to **users/groups**
*   Apply them across **multiple AWS accounts**

This enables:

*   Consistent permissions
*   Easier audits
*   Least‑privilege access at scale

***

### 4. **SSO to business applications**

IAM Identity Center also supports SSO to **non‑AWS applications**, such as:

*   Salesforce
*   Jira
*   ServiceNow
*   Custom SAML 2.0 applications

So users can use **one corporate identity** to access:

*   AWS console
*   AWS CLI
*   SaaS apps

***

### 5. **Short‑lived credentials (no long‑term IAM users)**

When users access AWS via Identity Center:

*   They receive **temporary credentials**
*   No long‑term access keys are stored

✅ Improves security  
✅ Reduces credential leakage risk

***

## What IAM Identity Center is **NOT** used for (exam trap 🚨)

| Not used for                          | Why                                   |
| ------------------------------------- | ------------------------------------- |
| Application / service‑to‑service auth | Use **IAM roles**                     |
| Public user authentication            | Use **Amazon Cognito**                |
| Fine‑grained resource policies        | Use **IAM policies**                  |
| API authorization for apps            | Use **Cognito / API Gateway / OAuth** |

***

## Typical use cases

*   ✅ Enterprise **multi‑account AWS environments**
*   ✅ Centralized access control for **large teams**
*   ✅ Replacing IAM users with **federated identities**
*   ✅ Enforcing **corporate IdP + MFA**
*   ✅ Auditable, scalable access management

***

