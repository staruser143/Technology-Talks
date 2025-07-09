resource "azurerm_servicebus_topic" "example" {
  name                                 = "my-topic"
  namespace_id                         = azurerm_servicebus_namespace.example.id
  enable_partitioning                  = false
  requires_duplicate_detection         = true
  duplicate_detection_history_time_window = "PT10M"
}

resource "azurerm_servicebus_subscription" "example" {
  name           = "my-subscription"
  topic_id       = azurerm_servicebus_topic.example.id
  requires_session = true
}
