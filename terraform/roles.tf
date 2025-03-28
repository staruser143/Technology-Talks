resource "azurerm_user_assigned_identity" "aks_identity" {
  name                = "aks-managed-identity"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
}

# Assign "Azure Service Bus Data Sender" Role
resource "azurerm_role_assignment" "send_role" {
  scope                = azurerm_servicebus_namespace.sb.id
  role_definition_name = "Azure Service Bus Data Sender"
  principal_id         = azurerm_user_assigned_identity.aks_identity.principal_id
}

# Assign "Azure Service Bus Data Receiver" Role
resource "azurerm_role_assignment" "receive_role" {
  scope                = azurerm_servicebus_namespace.sb.id
  role_definition_name = "Azure Service Bus Data Receiver"
  principal_id         = azurerm_user_assigned_identity.aks_identity.principal_id
}

# Assign "Azure Service Bus Owner" Role (if needed for management tasks)
resource "azurerm_role_assignment" "owner_role" {
  scope                = azurerm_servicebus_namespace.sb.id
  role_definition_name = "Azure Service Bus Owner"
  principal_id         = azurerm_user_assigned_identity.aks_identity.principal_id
}