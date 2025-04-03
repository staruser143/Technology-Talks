Absolutely! Below is a **complete Terraform setup** that includes:  
✅ A **managed identity module**  
✅ A **Service Bus module**  
✅ A **root module** that ties everything together  

---

### **📌 Step 1: Managed Identity Module (`modules/managed_identity`)**
This module creates a **user-assigned managed identity** and assigns **RBAC roles** for Service Bus.

📌 **`modules/managed_identity/main.tf`**  
```hcl
variable "identity_name" {}
variable "resource_group_name" {}
variable "location" {}
variable "servicebus_namespace_id" {}

resource "azurerm_user_assigned_identity" "managed_identity" {
  name                = var.identity_name
  resource_group_name = var.resource_group_name
  location            = var.location
}

resource "azurerm_role_assignment" "servicebus_send" {
  scope                = var.servicebus_namespace_id
  role_definition_name = "Azure Service Bus Data Sender"
  principal_id         = azurerm_user_assigned_identity.managed_identity.principal_id
}

resource "azurerm_role_assignment" "servicebus_receive" {
  scope                = var.servicebus_namespace_id
  role_definition_name = "Azure Service Bus Data Receiver"
  principal_id         = azurerm_user_assigned_identity.managed_identity.principal_id
}
```

📌 **`modules/managed_identity/outputs.tf`**  
```hcl
output "identity_id" {
  value = azurerm_user_assigned_identity.managed_identity.id
}

output "principal_id" {
  value = azurerm_user_assigned_identity.managed_identity.principal_id
}
```

📌 **`modules/managed_identity/variables.tf`**  
```hcl
variable "identity_name" { type = string }
variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "servicebus_namespace_id" { type = string }
```

---

### **📌 Step 2: Service Bus Module (`modules/servicebus`)**
This module creates the **Azure Service Bus namespace and queue**.

📌 **`modules/servicebus/main.tf`**  
```hcl
variable "resource_group_name" {}
variable "location" {}
variable "namespace_name" {}
variable "sku" { default = "Standard" }
variable "queue_name" {}

resource "azurerm_servicebus_namespace" "sb_namespace" {
  name                = var.namespace_name
  resource_group_name = var.resource_group_name
  location            = var.location
  sku                 = var.sku
}

resource "azurerm_servicebus_queue" "sb_queue" {
  name         = var.queue_name
  namespace_id = azurerm_servicebus_namespace.sb_namespace.id
}
```

📌 **`modules/servicebus/outputs.tf`**  
```hcl
output "namespace_id" {
  value = azurerm_servicebus_namespace.sb_namespace.id
}

output "queue_id" {
  value = azurerm_servicebus_queue.sb_queue.id
}
```

📌 **`modules/servicebus/variables.tf`**  
```hcl
variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "namespace_name" { type = string }
variable "sku" { type = string }
variable "queue_name" { type = string }
```

---

### **📌 Step 3: Root Terraform Configuration (`main.tf`)**
This configuration **calls both modules** and connects them.

📌 **`main.tf`**
```hcl
module "servicebus" {
  source              = "./modules/servicebus"
  resource_group_name = "RG-Messaging"
  location            = "eastus"
  namespace_name      = "my-servicebus-namespace"
  queue_name          = "my-queue"
}

module "managed_identity" {
  source                = "./modules/managed_identity"
  identity_name         = "aks-user-assigned-mi"
  resource_group_name   = "RG-Messaging"
  location              = "eastus"
  servicebus_namespace_id = module.servicebus.namespace_id
}
```

---

### **📌 Step 4: Terraform Execution**
Run Terraform commands to deploy this setup:

```sh
terraform init
terraform plan
terraform apply
```

---

### **✅ Why This Works?**
✔ **Modules encapsulate functionality** for managed identity and Service Bus  
✔ **RBAC roles are assigned automatically** when the identity is created  
✔ **Service Bus doesn’t need separate RBAC logic**—it remains clean  
✔ **Future Services (e.g., Event Grid, Storage) can use the same identity without modifying roles separately**  

This will create a **user-assigned managed identity**, **Azure Service Bus namespace**, and **queue**, while ensuring the **identity has the correct permissions**.

Would you like any additional refinements based on your setup? 🚀