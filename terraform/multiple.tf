variable "servicebus_roles" {
  default = [
    "Azure Service Bus Data Sender",
    "Azure Service Bus Data Receiver",
    "Azure Service Bus Owner"
  ]
}

resource "azurerm_role_assignment" "servicebus_roles" {
  for_each            = toset(var.servicebus_roles)
  scope               = azurerm_servicebus_namespace.sb.id
  role_definition_name = each.value
  principal_id        = azurerm_user_assigned_identity.aks_identity.principal_id
}