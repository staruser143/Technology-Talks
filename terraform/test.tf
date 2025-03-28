output "managed_identity_client_id" {
  description = "Client ID of the Managed Identity for AKS"
  value       = azurerm_user_assigned_identity.aks_identity.client_id
}

output "managed_identity_principal_id" {
  description = "Principal ID of the Managed Identity for AKS (used for role assignments)"
  value       = azurerm_user_assigned_identity.aks_identity.principal_id
}

output "servicebus_namespace" {
  description = "Azure Service Bus Namespace"
  value       = azurerm_servicebus_namespace.sb.name
}

output "servicebus_topic" {
  description = "Azure Service Bus Topic"
  value       = azurerm_servicebus_topic.topic.name
}

output "servicebus_namespace_fqdn" {
  description = "Fully Qualified Domain Name (FQDN) of the Service Bus Namespace"
  value       = azurerm_servicebus_namespace.sb.endpoint
}