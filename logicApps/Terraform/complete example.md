can you provide full complete example

# response 
Absolutely! Here's a **complete, end-to-end example** of how to:

1. ✅ **Provision infrastructure** (Resource Group, Storage, App Service Plan, Logic App)  
2. ✅ **Design a simple workflow** locally using VS Code  
3. ✅ **Deploy only the workflow definition later** using **Terraform**, referencing existing resources  
4. ✅ Use best practices: separation of concerns, `data` blocks, secure access

---

## 🧩 Full Example: Deploy Logic App Standard with Terraform (Two-Phase)

### 📁 Project Structure
```
logicapp-terraform-example/
│
├── phase1-infrastructure/
│   ├── main.tf
│   ├── variables.tf
│   └── terraform.tfstate (generated)
│
├── phase2-workflow/
│   ├── main.tf
│   ├── workflows/
│   │   └── HelloWorkflow.json
│   └── terraform.tfstate (generated)
│
└── README.md
```

---

## 🧱 PHASE 1: Provision Infrastructure

> Run this **once** to set up the environment.

📁 `phase1-infrastructure/main.tf`

```hcl
provider "azurerm" {
  features {}
}

locals {
  env        = "dev"
  name_base  = "logicappdemo"
}

resource "azurerm_resource_group" "main" {
  name     = "${local.name_base}-rg-${local.env}"
  location = "East US"
}

resource "azurerm_storage_account" "main" {
  name                     = "${local.name_base}storage${local.env}"
  resource_group_name      = azurerm_resource_group.main.name
  location                 = azurerm_resource_group.main.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  allow_blob_public_access = false
}

resource "azurerm_service_plan" "main" {
  name                = "${local.name_base}-sp-${local.env}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  os_type             = "Windows"
  sku_name            = "Y1"  # Consumption plan for Logic Apps Standard
}

resource "azurerm_logic_app_workflow" "main" {
  name                = "${local.name_base}-app-${local.env}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  app_service_plan_id = azurerm_service_plan.main.id
  storage_account_id  = azurerm_storage_account.main.id

  # Empty definition (will be updated later)
  definition = jsonencode({
    definition = {
      "$schema" = "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2019-05-01/workflowdefinition.json#"
      contentVersion = "1.0.0.0"
      parameters = {}
      triggers = {}
      actions = {}
      outputs = {}
    }
  })
}

# Output values for reference
output "logic_app_name" {
  value = azurerm_logic_app_workflow.main.name
}

output "resource_group_name" {
  value = azurerm_resource_group.main.name
}
```

📁 `phase1-infrastructure/variables.tf` (optional)

```hcl
variable "location" {
  default = "East US"
}
```

### 🔧 Deploy Phase 1

```bash
cd phase1-infrastructure
terraform init
terraform apply -auto-approve
```

✅ This creates:
- Resource Group: `logicappdemo-rg-dev`
- Storage: `logicappdemostoragedev`
- App Service Plan: `logicappdemo-sp-dev`
- Logic App: `logicappdemo-app-dev` (empty)

---

## 🧪 PHASE 2: Design & Deploy Workflow

### Step 2.1: Design Workflow Locally (VS Code)

Install:
- [Azure Logic Apps (Standard) Extension](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurelogicapps)

Create a new project:
- Name: `MyLogicAppLocal`
- Trigger: **When a HTTP request is received**

Add an action:
- **Compose** → Output: `"Hello from Terraform-deployed workflow!"`

📁 `phase2-workflow/workflows/HelloWorkflow.json`

```json
{
  "definition": {
    "$schema": "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2019-05-01/workflowdefinition.json#",
    "contentVersion": "1.0.0.0",
    "parameters": {},
    "triggers": {
      "manual": {
        "type": "Request",
        "kind": "Http",
        "inputs": {
          "schema": {}
        }
      }
    },
    "actions": {
      "Compose": {
        "type": "Compose",
        "inputs": "Hello from Terraform-deployed workflow!"
      }
    },
    "outputs": {}
  },
  "parameters": {}
}
```

---

### Step 2.2: Deploy Workflow Using Terraform (Update Only)

📁 `phase2-workflow/main.tf`

```hcl
provider "azurerm" {
  features {}
}

# Reference existing resources
data "azurerm_resource_group" "existing" {
  name = "logicappdemo-rg-dev"
}

data "azurerm_storage_account" "existing" {
  name                = "logicappdemostoragedev"
  resource_group_name = data.azurerm_resource_group.existing.name
}

data "azurerm_service_plan" "existing" {
  name                = "logicappdemo-sp-dev"
  resource_group_name = data.azurerm_resource_group.existing.name
}

data "azurerm_logic_app_workflow" "existing" {
  name                = "logicappdemo-app-dev"
  resource_group_name = data.azurerm_resource_group.existing.name
}

# Update ONLY the workflow definition
resource "azurerm_logic_app_workflow" "workflow" {
  # Must match existing Logic App name
  name                = data.azurerm_logic_app_workflow.existing.name
  location            = data.azurerm_logic_app_workflow.existing.location
  resource_group_name = data.azurerm_resource_group.existing.name

  # Reuse existing infrastructure
  app_service_plan_id = data.azurerm_service_plan.existing.id
  storage_account_id  = data.azurerm_storage_account.existing.id
  storage_account_name = data.azurerm_storage_account.existing.name
  storage_account_access_key = data.azurerm_storage_account.existing.primary_access_key

  # 👇 Deploy real workflow
  definition = file("${path.module}/workflows/HelloWorkflow.json")

  # Optional: parameters (e.g., connections)
  parameters = {}
}
```

> 🔐 **Security Tip**: Store `storage_account_access_key` in a secure backend (e.g., Azure Key Vault + Terraform Remote State), or use managed identity.

---

### 🔧 Deploy Phase 2

```bash
cd phase2-workflow
terraform init
terraform apply -auto-approve
```

✅ Result:
- The **Logic App** now has your workflow
- Go to Azure Portal → Logic App → **Designer** → You’ll see your "Compose" action
- Test it by triggering via HTTP

---

## 📡 Test the Workflow

1. Go to **Azure Portal**
2. Open your Logic App: `logicappdemo-app-dev`
3. Click **Logic App Designer**
4. Find the **HTTP trigger URL** (click "When a HTTP request is received")
5. Copy and call it:

```bash
curl -X POST https://prod-xx.westus.logic.azure.com:443/...
```

6. Check **Run history** → You should see success!

---

## 🔄 Update Workflow Later?

Just:
1. Modify `HelloWorkflow.json`
2. Run `terraform apply` in `phase2-workflow/`

No infrastructure change — only the logic updates.

---

## ✅ Best Practices Used

| Feature | Applied? |
|-------|--------|
| Separation of infra and code | ✅ |
| Reuse existing resources via `data` | ✅ |
| No hardcoded IDs (except names) | ✅ |
| Idempotent, repeatable deployments | ✅ |
| Supports CI/CD (GitHub Actions, etc.) | ✅ |

---

## 🚀 Optional: CI/CD Pipeline (GitHub Actions)

```yaml
name: Deploy Logic App Workflow

on:
  push:
    branches: [ main ]

env:
  LOGIC_APP_NAME: logicappdemo-app-dev
  RESOURCE_GROUP: logicappdemo-rg-dev

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Azure Login
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v2
        with:
          terraform_version: 1.5.7

      - name: Terraform Init
        run: terraform init
        working-directory: ./phase2-workflow

      - name: Terraform Apply
        run: terraform apply -auto-approve
        working-directory: ./phase2-workflow
```

> Store `AZURE_CREDENTIALS` as a secret (from a Service Principal with contributor access).

---

## 📦 Download This Example

You can clone this structure into your repo:

```bash
git clone https://github.com/yourname/logicapp-terraform-example.git
```

Or create it manually using the files above.

---

## ✅ Summary

| Goal | Achieved? |
|------|-----------|
| Provision infra once | ✅ Phase 1 |
| Deploy workflow later | ✅ Phase 2 |
| Use Terraform for both | ✅ Yes |
| Avoid recreating resources | ✅ Used `data` blocks |
| Support DevOps workflows | ✅ CI/CD ready 