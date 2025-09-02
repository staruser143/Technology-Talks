# query

When deploying workflow definition separately should we refer already provisioned resources like resource group, storage account, app service plan, logic app standard resource in terraform script

# response 
Yes — **when deploying only the workflow definition separately**, you **do not need to re-declare or re-create** the infrastructure resources (like Resource Group, Storage Account, App Service Plan, or Logic App), **but you *do* need to reference them** in your Terraform configuration **if you're using Terraform to deploy the workflow**.

Let’s clarify this important distinction:

---

### ✅ Key Principle:
> You **don’t recreate** the infrastructure, but you **must reference the existing Logic App resource** to update its `definition` (i.e., the workflow JSON).

So:  
🔹 **No** — you don’t re-provision storage, app plan, etc.  
🔹 **Yes** — you **must refer to the existing Logic App resource** in Terraform to update its workflow definition.

---

## ✅ Scenario: Deploy Workflow Definition Only (Using Terraform)

You’ve already created:
- Resource Group
- Storage Account
- App Service Plan
- Logic App (Standard) — the "host"

Now you want to **update just the workflow logic** using Terraform.

### ✅ Correct Approach: Use `data` blocks or known values

You can use **`data` sources** to **reference existing resources**, and then update only the `azurerm_logic_app_workflow`'s `definition`.

### Example: `workflow-only.tf`

```hcl
# Provider
provider "azurerm" {
  features {}
}

# Reference existing resources (already provisioned)
data "azurerm_resource_group" "existing" {
  name = "rg-logicapp-prod"
}

data "azurerm_storage_account" "existing" {
  name                = "mystorageaccount123"
  resource_group_name = data.azurerm_resource_group.existing.name
}

data "azurerm_service_plan" "existing" {
  name                = "sp-logicapp-prod"
  resource_group_name = data.azurerm_resource_group.existing.name
}

# Update ONLY the workflow definition
resource "azurerm_logic_app_workflow" "main" {
  name                = "my-logic-app-prod"  # Must match existing Logic App name
  location            = data.azurerm_resource_group.existing.location
  resource_group_name = data.azurerm_resource_group.existing.name

  # Reference existing plan and storage
  app_service_plan_id = data.azurerm_service_plan.existing.id
  storage_account_id  = data.azurerm_storage_account.existing.id
  storage_account_name = data.azurerm_storage_account.existing.name
  storage_account_access_key = data.azurerm_storage_account.existing.primary_access_key

  # 👇 This is the only thing that changes!
  definition = file("${path.module}/workflows/MyUpdatedWorkflow.json")

  # Optional: parameters (e.g., connections)
  parameters = {}
}
```

---

### 🔍 What Happens on `terraform apply`?

- Terraform reads the **current state** of the Logic App
- Compares the current `definition` with your new JSON
- **Updates only the workflow definition**
- Leaves infrastructure (storage, plan, etc.) **untouched**

✅ No recreation  
✅ No downtime  
✅ Only the logic changes

---

## ✅ Alternative: Use Hardcoded Values (Simpler, Less Dynamic)

If you know the names and IDs, you can skip `data` blocks:

```hcl
resource "azurerm_logic_app_workflow" "main" {
  name                = "my-logic-app-prod"
  resource_group_name = "rg-logicapp-prod"
  location            = "East US"

  app_service_plan_id = "/subscriptions/xxx/resourceGroups/rg-logicapp-prod/providers/Microsoft.Web/serverfarms/sp-logicapp-prod"
  storage_account_id  = "/subscriptions/xxx/resourceGroups/rg-logicapp-prod/providers/Microsoft.Storage/storageAccounts/mystorageaccount123"
  storage_account_name = "mystorageaccount123"
  storage_account_access_key = "xxx" # Use Terraform variables or backend

  definition = file("workflows/MyWorkflow.json")
}
```

> ⚠️ Less flexible, but works for simple cases.

---

## ✅ Best Practices

| Practice | Why |
|--------|-----|
| ✅ Use `data` blocks | Keeps config dynamic and avoids hardcoded IDs |
| ✅ Store secrets in TF backend or Azure Key Vault | Never hardcode keys in `.tf` files |
| ✅ Use `terraform workspace` or `count` for multi-env | e.g., dev/stage/prod |
| ✅ Separate `infrastructure.tf` and `workflow.tf` | Clear separation of concerns |

---

## ❌ What NOT to Do

```hcl
# DON'T recreate infrastructure!
resource "azurerm_storage_account" "new" {
  name = "mystorageaccount123" # Already exists!
  # ...
}
```

This causes:
- Conflicts
- Drift
- Possible recreation (if Terraform doesn’t know it already exists)

---

## ✅ Alternative: Non-Terraform Workflow Deployment

If you **don’t want to use Terraform at all** for workflow updates, consider:

| Method | Use Case |
|-------|--------|
| **VS Code Deploy** | Local dev, quick updates |
| **Azure CLI ZIP Deploy** | CI/CD pipelines |
| **GitHub Actions** | Automated builds |
| **Azure DevOps Release Pipeline** | Enterprise workflows |

These **bypass Terraform entirely** for code deployment, which is often preferred:
- Infra = Terraform (managed by platform team)
- Code = ZIP deploy (managed by app team)

---

## ✅ Summary

| Question | Answer |
|--------|--------|
| Do I need to refer to existing resources in Terraform when deploying only the workflow? | ✅ **Yes — at least the Logic App, storage, and plan must be referenced** |
| Do I need to re-declare storage or app plan as `resource` blocks? | ❌ **No — use `data` blocks or known values instead** |
| Can I update just the workflow without touching infra? | ✅ **Yes — as long as you reference existing resources correctly** |
| Should I use Terraform for workflow-only updates? | ✅ Yes, **if you want full IaC control**<br>❌ Or use CLI/VS Code for faster dev loops |


