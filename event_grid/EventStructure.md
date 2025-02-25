Yes, when you publish a custom event like a "Quote Created" event to an Event Grid topic, it's important to follow the required structure to ensure that Event Grid can properly process and route the event. Azure Event Grid expects events to be in a specific schema, which includes both mandatory metadata and event data. 

Here's the structure you should follow:

1. **Event Metadata**: This includes fields such as `id`, `eventType`, `subject`, `eventTime`, and `dataVersion`.

2. **Event Data**: This section contains the actual payload specific to the event you're publishing.

Here’s an example of how a custom "Quote Created" event might look:

```json
[
  {
    "id": "abcd-1234-efgh-5678",
    "eventType": "MyApp.QuoteCreated",
    "subject": "Quotes/1234",
    "eventTime": "2025-02-25T10:00:00.1234567Z",
    "data": {
      "quoteId": "1234",
      "customerId": "5678",
      "amount": 1000.00,
      "currency": "USD",
      "createdDate": "2025-02-25T10:00:00Z"
    },
    "dataVersion": "1.0",
    "metadataVersion": "1"
  }
]
```

### Key Components:

- **id**: A unique identifier for the event.
- **eventType**: A string that describes the event type, e.g., "MyApp.QuoteCreated".
- **subject**: A string that provides context about the event, e.g., "Quotes/1234".
- **eventTime**: The time at which the event occurred.
- **data**: An object containing the event-specific data.
- **dataVersion**: Version of the event data schema.
- **metadataVersion**: Version of the metadata schema.

### Publishing the Event:
To publish this event, you can use the Azure SDK or the REST API. Here's an example using the Azure CLI:

```bash
az eventgrid event publish \
  --topic-endpoint "https://<your-topic-name>.<region>-1.eventgrid.azure.net/api/events" \
  --subject "Quotes/1234" \
  --event-type "MyApp.QuoteCreated" \
  --data '{
    "quoteId": "1234",
    "customerId": "5678",
    "amount": 1000.00,
    "currency": "USD",
    "createdDate": "2025-02-25T10:00:00Z"
  }' \
  --event-time "2025-02-25T10:00:00.1234567Z" \
  --data-version "1.0"
```

By ensuring your custom events follow this structure, you enable Azure Event Grid to effectively process and route the events to the subscribers.

Let me know if you have any further questions or need additional assistance!