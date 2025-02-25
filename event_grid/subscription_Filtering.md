You don't need to create a separate subscription for each event type. Azure Event Grid allows you to filter events within a single subscription based on specific criteria, such as event type.

Here's how you can configure event filters within a subscription:

1. **Event Type Filtering**: You can specify one or more event types to filter. This ensures that only events matching the specified types are sent to your endpoint.
   - Example: If you're interested only in "Blob Created" and "Blob Deleted" events from an Azure Storage account, you can set up filters to receive just these events.

2. **Subject Filtering**: You can filter events based on the subject field. This field often contains the resource path of the event source.
   - Example: If you want to receive events only for blobs in a specific container within a storage account, you can filter based on the subject.

3. **Advanced Filtering**: Event Grid also supports advanced filtering based on event data, such as exact matches, prefix matches, suffix matches, and more.
   - Example: You can filter events based on specific properties in the event data payload.

Here's a brief example of how you might set up filtering in an Azure CLI command:

```bash
az eventgrid event-subscription create \
  --name mySubscription \
  --source-resource-id /subscriptions/{subscription-id}/resourceGroups/{resource-group}/providers/Microsoft.Storage/storageAccounts/{storage-account} \
  --endpoint https://example.com/webhook \
  --included-event-types Microsoft.Storage.BlobCreated Microsoft.Storage.BlobDeleted \
  --subject-begins-with /blobServices/default/containers/my-container/
```

With these filtering options, you can tailor the events you receive to match your specific requirements, all within a single subscription.

Let me know if you need any further details or assistance!