Yes, Azure Event Grid routes the complete payload that is published to its topic to the event grid subscribers. When you publish an event to an Event Grid topic, the entire event payload, including all its properties and data, is delivered to the subscribed endpoints.

The event payload generally consists of the following components:

1. **Event Metadata**:
   - **Id**: A unique identifier for the event.
   - **Topic**: The resource path of the event source.
   - **Subject**: A path that provides context about the event source.
   - **EventType**: The type of event that occurred (e.g., "Microsoft.Storage.BlobCreated").
   - **EventTime**: The time at which the event was generated.

2. **Event Data**:
   - This section contains the actual data related to the event, which can vary based on the event type. For example, in the case of a "Blob Created" event from Azure Storage, the event data might include the URL of the blob, its size, content type, and other properties.

Here's an example of an event payload for a "Blob Created" event:

```json
[
  {
    "id": "1234-5678-9012-3456",
    "topic": "/subscriptions/{subscription-id}/resourceGroups/{resource-group}/providers/Microsoft.Storage/storageAccounts/{storage-account}",
    "subject": "/blobServices/default/containers/{container}/blobs/{blob-name}",
    "eventType": "Microsoft.Storage.BlobCreated",
    "eventTime": "2023-02-25T10:00:00.1234567Z",
    "data": {
      "api": "PutBlob",
      "clientRequestId": "abcd-1234-efgh-5678",
      "requestId": "1234abcd-5678-efgh-9101-ijkl1234mnop",
      "etag": "0x1234567890abcdef",
      "contentType": "application/octet-stream",
      "contentLength": 1024,
      "blobType": "BlockBlob",
      "url": "https://{storage-account}.blob.core.windows.net/{container}/{blob-name}"
    },
    "dataVersion": "1.0",
    "metadataVersion": "1"
  }
]
```

In this example, the entire payload, including metadata and data, is delivered to the subscribers of the Event Grid topic.

If you have any further questions or need additional details, feel free to ask!