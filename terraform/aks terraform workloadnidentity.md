Yes! You can automate the same setup for an existing AKS cluster (with or without OIDC issuer enabled) using Terraform. Here's how to do it:

---

### **1. Terraform Providers Configuration**
```hcl
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.0.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.0.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.0.0"
    }
  }
}

provider "azurerm" {
  features {}
}

# Authenticate to AKS (replace with your cluster's credentials)
provider "kubernetes" {
  host                   = azurerm_kubernetes_cluster.aks.kube_config.0.host
  client_certificate     = base64decode(azurerm_kubernetes_cluster.aks.kube_config.0.client_certificate)
  client_key             = base64decode(azurerm_kubernetes_cluster.aks.kube_config.0.client_key)
  cluster_ca_certificate = base64decode(azurerm_kubernetes_cluster.aks.kube_config.0.cluster_ca_certificate)
}

provider "helm" {
  kubernetes {
    host                   = azurerm_kubernetes_cluster.aks.kube_config.0.host
    client_certificate     = base64decode(azurerm_kubernetes_cluster.aks.kube_config.0.client_certificate)
    client_key             = base64decode(azurerm_kubernetes_cluster.aks.kube_config.0.client_key)
    cluster_ca_certificate = base64decode(azurerm_kubernetes_cluster.aks.kube_config.0.cluster_ca_certificate)
  }
}
```

---

### **2. Enable OIDC Issuer (if not already enabled)**
If your existing AKS cluster doesn't have OIDC enabled, update it with Terraform:
```hcl
resource "azurerm_kubernetes_cluster" "aks" {
  name                = "my-existing-aks-cluster"
  resource_group_name = "my-resource-group"
  location            = "eastus"
  # ... (existing configuration)

  oidc_issuer_enabled = true  # Force-enable OIDC issuer
}
```

---

### **3. Install Workload Identity Webhook via Helm**
```hcl
resource "helm_release" "workload_identity" {
  name       = "workload-identity-webhook"
  repository = "https://azure.github.io/azure-workload-identity/charts"
  chart      = "workload-identity-webhook"
  namespace  = "azure-workload-identity-system"
  create_namespace = true
}
```

---

### **4. Create Managed Identity and Role Assignment**
```hcl
resource "azurerm_user_assigned_identity" "pod_identity" {
  name                = "aks-pod-identity"
  resource_group_name = azurerm_kubernetes_cluster.aks.resource_group_name
  location            = azurerm_kubernetes_cluster.aks.location
}

# Example: Grant access to a Storage Account
resource "azurerm_role_assignment" "storage_contributor" {
  scope                = azurerm_storage_account.example.id
  role_definition_name = "Storage Blob Data Contributor"
  principal_id         = azurerm_user_assigned_identity.pod_identity.principal_id
}
```

---

### **5. Create Kubernetes Service Account**
```hcl
resource "kubernetes_service_account" "workload" {
  metadata {
    name      = "workload-sa"
    namespace = "my-namespace"
    annotations = {
      "azure.workload.identity/client-id" = azurerm_user_assigned_identity.pod_identity.client_id
    }
  }
}
```

---

### **6. Federate the Service Account with Azure AD**
```hcl
resource "azurerm_federated_identity_credential" "federated" {
  name                = "aks-federated-identity"
  resource_group_name = azurerm_user_assigned_identity.pod_identity.resource_group_name
  audience            = ["api://AzureADTokenExchange"]
  issuer              = azurerm_kubernetes_cluster.aks.oidc_issuer_url
  parent_id           = azurerm_user_assigned_identity.pod_identity.id
  subject             = "system:serviceaccount:${kubernetes_service_account.workload.metadata[0].namespace}:${kubernetes_service_account.workload.metadata[0].name}"
}
```

---

### **7. Deploy Pod with Service Account**
```hcl
resource "kubernetes_pod" "workload" {
  metadata {
    name      = "workload-pod"
    namespace = kubernetes_service_account.workload.metadata[0].namespace
  }

  spec {
    service_account_name = kubernetes_service_account.workload.metadata[0].name

    container {
      name  = "my-container"
      image = "my-app-image"
      env {
        name  = "AZURE_CLIENT_ID"
        value = azurerm_user_assigned_identity.pod_identity.client_id
      }
      env {
        name  = "AZURE_TENANT_ID"
        value = data.azurerm_client_config.current.tenant_id
      }
      env {
        name  = "AZURE_FEDERATED_TOKEN_FILE"
        value = "/var/run/secrets/azure/tokens/azure-identity-token"
      }
    }
  }
}
```

---

### **Key Notes:**
1. **Existing Cluster Import**: If your AKS cluster isn't managed by Terraform, import it first:
   ```bash
   terraform import azurerm_kubernetes_cluster.aks /subscriptions/<SUBSCRIPTION_ID>/resourceGroups/<RG>/providers/Microsoft.ContainerService/managedClusters/<CLUSTER_NAME>
   ```

2. **OIDC Verification**: After enabling OIDC, verify the issuer URL:
   ```hcl
   output "oidc_issuer_url" {
     value = azurerm_kubernetes_cluster.aks.oidc_issuer_url
   }
   ```

3. **Azure SDKs**: Your application code should still use `DefaultAzureCredential` from Azure SDKs.

4. **Webhook Auto-Injection**: The Helm chart automatically injects token volumes into pods using the annotated service account.

---

### **Final Workflow:**
```bash
terraform init
terraform plan
terraform apply
```

This Terraform code will:
1. Enable OIDC issuer (if not already enabled)
2. Install Workload Identity components
3. Create and configure the managed identity
4. Set up the Kubernetes service account and federated credentials
5. Deploy a pod with the necessary authentication setup

No manual Azure CLI steps needed! 🚀