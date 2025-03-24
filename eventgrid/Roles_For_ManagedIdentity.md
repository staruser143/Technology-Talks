To use a managed identity for publishing and subscribing to events in Azure Event Grid, you need to assign specific roles to the managed identity. Here are the required roles and permissions:

### Publishing Events

1. **Event Grid Data Sender Role**:
   - **Purpose**: Allows the managed identity to send events to the Event Grid topic.
   - **Permission**: `Microsoft.EventGrid/events/send/action`
   - **Assignment**: Assign this role to the managed identity on the Event Grid topic[1](https://learn.microsoft.com/en-us/azure/event-grid/authenticate-with-microsoft-entra-id).

### Subscribing to Events

1. **Event Grid Data Receiver Role** (if applicable):
   - **Purpose**: Allows the managed identity to receive events from the Event Grid topic.
   - **Permission**: `Microsoft.EventGrid/eventSubscriptions/read/action`
   - **Assignment**: Assign this role to the managed identity on the Event Grid topic or subscription[2](https://learn.microsoft.com/en-us/azure/event-grid/managed-service-identity).

2. **Additional Roles for Specific Destinations**:
   - **Service Bus**: If the destination is an Azure Service Bus, assign the `Azure Service Bus Data Sender` role to the managed identity on the Service Bus queue or topic[2](https://learn.microsoft.com/en-us/azure/event-grid/managed-service-identity).
   - **Storage Account**: If the destination is an Azure Storage account, assign the `Storage Blob Data Contributor` role to the managed identity on the storage account[2](https://learn.microsoft.com/en-us/azure/event-grid/managed-service-identity).
   - **Event Hubs**: If the destination is an Azure Event Hub, assign the `Azure Event Hubs Data Sender` role to the managed identity on the Event Hub[2](https://learn.microsoft.com/en-us/azure/event-grid/managed-service-identity).

### Example: Assigning Roles Using Azure CLI

1. **Assign Event Grid Data Sender Role**:
   ```bash
   az role assignment create \
     --assignee <managed_identity_client_id> \
     --role "Event Grid Data Sender" \
     --scope <event_grid_topic_resource_id>
   ```

2. **Assign Service Bus Data Sender Role** (if using Service Bus as a destination):
   ```bash
   az role assignment create \
     --assignee <managed_identity_client_id> \
     --role "Azure Service Bus Data Sender" \
     --scope <service_bus_queue_resource_id>
   ```

3. **Assign Storage Blob Data Contributor Role** (if using Storage Account for dead-lettering):
   ```bash
   az role assignment create \
     --assignee <managed_identity_client_id> \
     --role "Storage Blob Data Contributor" \
     --scope <storage_account_resource_id>
   ```

By assigning these roles, you ensure that the managed identity has the necessary permissions to publish events to the Event Grid topic and receive events from it.


[2](https://learn.microsoft.com/en-us/azure/event-grid/managed-service-identity): [Event delivery with managed identity](https://learn.microsoft.com/en-us/azure/event-grid/managed-service-identity)
[3](https://learn.microsoft.com/en-us/azure/event-grid/enable-identity-system-topics): [Enable managed identity on Azure Event Grid system topic](https://learn.microsoft.com/en-us/azure/event-grid/enable-identity-system-topics)
[1](https://learn.microsoft.com/en-us/azure/event-grid/authenticate-with-microsoft-entra-id): [Authenticate Event Grid publishing clients using Microsoft Entra ID](https://learn.microsoft.com/en-us/azure/event-grid/authenticate-with-microsoft-entra-id)
