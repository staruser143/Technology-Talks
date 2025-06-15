To provision a **sample Azure Logic App (Consumption)** workflow using **Terraform**, you can follow this basic example. This workflow will trigger when an HTTP request is received and respond with a simple message.

---

### 📁 **Terraform Sample Structure**

#### **1. `main.tf`**
```hcl
provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "example" {
  name     = "logicapp-rg"
  location = "East US"
}

resource "azurerm_logic_app_workflow" "example" {
  name                = "sample-logicapp"
  location            = azurerm_resource_group.example.location
  resource_group_name = azurerm_resource_group.example.name

  definition = jsonencode({
    "$schema" = "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#"
    "contentVersion" = "1.0.0.0"
    "parameters" = {}
    "triggers" = {
      "manual" = {
        "type" = "Request"
        "kind" = "Http"
        "inputs" = {
          "schema" = {}
        }
      }
    }
    "actions" = {
      "response" = {
        "type" = "Response"
        "inputs" = {
          "statusCode" = 200
          "body" = {
            "message" = "Hello from Logic App!"
          }
        }
      }
    }
    "outputs" = {}
  })

  tags = {
    environment = "demo"
  }
}
```

#### **2. Initialize and Deploy**
```bash
terraform init
terraform apply
```

---

### 🧪 What This Does:
- Creates a **resource group**.
- Deploys a **Logic App (Consumption)** with:
  - An **HTTP trigger**.
  - A **response action** that returns a JSON message.

---
