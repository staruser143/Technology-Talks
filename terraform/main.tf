# Fetch Existing AKS Cluster
data "azurerm_kubernetes_cluster" "existing_aks" {
  name                = "your-existing-aks-cluster"  # Update with your AKS cluster name
  resource_group_name = "your-aks-resource-group"    # Update with the AKS resource group
}

# Create a Managed Identity
resource "azurerm_user_assigned_identity" "aks_identity" {
  name                = "aks-managed-identity"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
}

# Assign Managed Identity to Existing AKS
resource "azurerm_role_assignment" "aks_identity_role" {
  scope                = data.azurerm_kubernetes_cluster.existing_aks.id
  role_definition_name = "Managed Identity Operator"
  principal_id         = azurerm_user_assigned_identity.aks_identity.principal_id
}