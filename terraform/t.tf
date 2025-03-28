provider "azurerm" {
  features {}
}

# Resource Group
resource "azurerm_resource_group" "rg" {
  name     = "rg-aks-servicebus"
  location = "East US"
}

# Fetch Existing AKS Cluster
data "azurerm_kubernetes_cluster" "existing_aks" {
  name                = "your-existing-aks-cluster"
  resource_group_name = "your-aks-resource-group"
}

# Create a Managed Identity for AKS
resource "azurerm_user_assigned_identity" "aks_identity" {
  name                = "aks-managed-identity"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
}

# Azure Service Bus Namespace
resource "azurerm_servicebus_namespace" "sb" {
  name                = "sb-namespace"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  sku                 = "Standard"
}

# Service Bus Topic
resource "azurerm_servicebus_topic" "topic" {
  name                = "order-events"
  namespace_id        = azurerm_servicebus_namespace.sb.id
  enable_partitioning = true
}

# Service Bus Subscription
resource "azurerm_servicebus_subscription" "sub" {
  name            = "order-subscription"
  topic_id        = azurerm_servicebus_topic.topic.id
  max_delivery_count = 10
}

# Assign "Azure Service Bus Data Sender" Role to AKS Managed Identity
resource "azurerm_role_assignment" "role_send" {
  scope                = azurerm_servicebus_namespace.sb.id
  role_definition_name = "Azure Service Bus Data Sender"
  principal_id         = azurerm_user_assigned_identity.aks_identity.principal_id
}

# Assign Managed Identity to Existing AKS
resource "azurerm_kubernetes_cluster_identity" "aks_identity_binding" {
  kubernetes_cluster_id = data.azurerm_kubernetes_cluster.existing_aks.id
  identity_ids          = [azurerm_user_assigned_identity.aks_identity.id]
}