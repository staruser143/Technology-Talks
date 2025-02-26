| **Field** | **Description** |
|----------|----------|
| **eventName** | A descriptive name for the event, indicating the occurrence. |
| **eventId** |A unique identifier (UUID) for the event. |
| **eventType** |  Specifies the type of event. E.g, Notification, Domain, Integration |
| **eventSource** |  Indicates the specific component, service, or module that directly generated the event (Used to identify the origin of the event within the system) |
| **eventStatus** |  The status of the event |
| **eventVersion** |  A version identifier for the event itself  |
| **correlationId** |Matches the correlation Id from the command to tie the event back to the original request. |
| **commandId** | The identifier of the command that this event is related to. |
| **tags** | Categorizes the event (["order", "creation,"ecommerice"]), Enables filtering and searching of events in logs, databases, and monitoring systems. |
| **retryCount** | Tracks the number of times the event has been retried  |
| **timestamp** | When the event was generated |
| **payload** |Contains the data related to the event, such as order details, customer information, items, and total amount. |
| **metadata** |  Additional information about the event, such as the service that initiated it |
| **metadata.initiatedBy** |   Indicates the user, system, or entity that initiated the command leading to the event. E.g. user123, admin, batchProcessor. |
| **metadata.sourceSystem** |   Indicates the broader system or application context from which the event originated. E.g, OrderManagementSystem, InventoryManagementSystem. |
| **versionId** |Specifies the version of the event structure (1.0). |
| **schemaRef** |Contains the reference to the schema in the schema registry |
| **schemaRef.id** | The identifier for the schema. |
| **schemaRef.version** | The version of the schema |
| **schemaRef.uri** | The URI where the schema can be accessed (For e.g, https://schema-registry.example.com/schemas/orderCreatedSchema/1.0). |
