
# Design of Command Specification

Designing an ideal command specification for sending requests in a CQRS pattern is crucial for ensuring clarity, consistency, and maintainability. Here’s a suggested structure for the  command specification:

### Command Specification Structure

1. **Command Name:** A descriptive name for the command, indicating the action to be performed.
2. **Unique Identifier:** A unique identifier (UUID) for each command to ensure idempotency and traceability.
3. **Timestamp:** The timestamp when the command was created or sent.
4. **Payload:** The data required to execute the command, typically represented as an object.
5. **Metadata:** Additional information about the command, such as the user who initiated it, the source system, etc.
6. **Validation Rules:** Define validation rules to ensure the command's payload meets the required criteria.

### Example Command Specification

```json
{
  "commandName": "CreateOrderCommand",
  "commandId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-02-25T18:30:00Z",
  "payload": {
    "orderId": "ORD12345",
    "customerId": "CUST67890",
    "items": [
      {
        "itemId": "ITEM001",
        "quantity": 2,
        "price": 100
      },
      {
        "itemId": "ITEM002",
        "quantity": 1,
        "price": 50
      }
    ],
    "totalAmount": 250
  },
  "metadata": {
    "initiatedBy": "user123",
    "sourceSystem": "OrderService"
  },
  "validationRules": {
    "orderId": "required|string",
    "customerId": "required|string",
    "items": "required|array",
    "totalAmount": "required|number"
  }
}
```

### Explanation of the Structure

1. **Command Name:** Clearly states the purpose of the command (`CreateOrderCommand`).
2. **Command Id:** A unique UUID (`123e4567-e89b-12d3-a456-426614174000`) ensures the command can be uniquely identified.
3. **Timestamp:** Records when the command was issued (`2025-02-25T18:30:00Z`), which helps in auditing and debugging.
4. **Payload:** Contains the essential data needed to execute the command, such as order details, customer information, items, and total amount.
5. **Metadata:** Provides additional context about the command, including who initiated it and the source system.
6. **Validation Rules:** Specifies the rules to validate the command's payload, ensuring all required fields are present and correctly formatted.


# CorrelationId

Including a `correlationId` in the command specification can be highly beneficial.
* The `correlationId` helps to track and correlate multiple related requests and responses across different systems, services, or components. 
* This is especially useful in distributed systems where a single business process might span multiple microservices or components.

### Benefits of Including `correlationId`

1. **Traceability:** It allows us to trace the flow of a request through various components and services, making debugging and monitoring easier.
2. **Consistency:** Ensures consistency by correlating commands, events, and queries related to the same business process or transaction.
3. **Logging:** Improves logging by providing a unique identifier that can be used to group and analyze logs related to a specific request.
4. **Error Handling:** Facilitates better error handling by tracking which parts of the process have succeeded or failed.

### Updated Command Specification Example

Here’s an example of a command specification with the inclusion of a `correlationId`:

```json
{
  "commandName": "CreateOrderCommand",
  "commandId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-02-25T18:30:00Z",
  "correlationId": "789e4567-e89b-12d3-a456-426614174123",
  "payload": {
    "orderId": "ORD12345",
    "customerId": "CUST67890",
    "items": [
      {
        "itemId": "ITEM001",
        "quantity": 2,
        "price": 100
      },
      {
        "itemId": "ITEM002",
        "quantity": 1,
        "price": 50
      }
    ],
    "totalAmount": 250
  },
  "metadata": {
    "initiatedBy": "user123",
    "sourceSystem": "OrderService"
  },
  "validationRules": {
    "orderId": "required|string",
    "customerId": "required|string",
    "items": "required|array",
    "totalAmount": "required|number"
  }
}
```

### Key Elements

1. **Command Name:** Indicates the purpose of the command (`CreateOrderCommand`).
2. **Command Id:** Unique identifier for the command (`123e4567-e89b-12d3-a456-426614174000`).
3. **Timestamp:** When the command was issued (`2025-02-25T18:30:00Z`).
4. **Correlation Id:** Unique identifier for correlating related requests (`789e4567-e89b-12d3-a456-426614174123`).
5. **Payload:** Contains the data needed to execute the command.
6. **Metadata:** Additional context about the command.
7. **Validation Rules:** Rules to validate the command's payload.

Including a `correlationId` can greatly enhance the observability and manageability of the  system. It’s a best practice in distributed system design.



# Multiple Commands 


To send multiple commands as part of a single request, we can design a **Batch Command specification** that encapsulates multiple individual commands. This approach allows us to batch multiple commands together and send them in a single payload. Here's how we can structure it:

### Partner Command Specification Structure

1. **Batch Command Name:** A descriptive name for the Batch command, indicating the batch of commands to be executed.
2. **Unique Identifier:** A unique identifier (UUID) for the partner command.
3. **Timestamp:** The timestamp when the partner command was created or sent.
4. **Commands:** An array of individual command specifications, each containing its own unique structure.
5. **Correlation Id:** A unique identifier to correlate related requests.
6. **Metadata:** Additional information about the partner command, such as the user who initiated it, the source system, etc.

### Example Batch Command Specification

```json
{
  "batchCommandName": "OrderProcessingCommands",
  "batchCommandId": "789e4567-e89b-12d3-a456-426614174789",
  "timestamp": "2025-02-25T19:00:00Z",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "commands": [
    {
      "commandName": "CreateOrderCommand",
      "commandId": "123e4567-e89b-12d3-a456-426614174000",
      "timestamp": "2025-02-25T18:30:00Z",
      "payload": {
        "orderId": "ORD12345",
        "customerId": "CUST67890",
        "items": [
          {
            "itemId": "ITEM001",
            "quantity": 2,
            "price": 100
          },
          {
            "itemId": "ITEM002",
            "quantity": 1,
            "price": 50
          }
        ],
        "totalAmount": 250
      },
      "metadata": {
        "initiatedBy": "user123",
        "sourceSystem": "OrderService"
      },
      "validationRules": {
        "orderId": "required|string",
        "customerId": "required|string",
        "items": "required|array",
        "totalAmount": "required|number"
      }
    },
    {
      "commandName": "UpdateInventoryCommand",
      "commandId": "e7d4a5b3-4f9d-4879-a978-3c6d0b7b16d3",
      "timestamp": "2025-02-25T18:35:00Z",
      "payload": {
        "itemId": "ITEM001",
        "quantity": 10
      },
      "metadata": {
        "initiatedBy": "inventoryManager",
        "sourceSystem": "InventoryService"
      },
      "validationRules": {
        "itemId": "required|string",
        "quantity": "required|number|min:0"
      }
    }
  ],
  "metadata": {
    "initiatedBy": "batchProcessor",
    "sourceSystem": "OrderProcessingService"
  }
}
```

# Domain or Namespace

Including the domain or namespace in the  command specification can be very useful. 
It helps to organize and categorize commands, especially in large, complex systems where commands may span multiple domains or bounded contexts.
This approach enhances clarity, maintainability, and traceability.

### Updated Command Specification Example

Here’s how we can include the domain or namespace in the command specification:

```json
{
  "batchCommandName": "OrderProcessingCommands",
  "batchCommandId": "789e4567-e89b-12d3-a456-426614174789",
  "timestamp": "2025-02-25T19:00:00Z",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "domain": "OrderManagement",
  "commands": [
    {
      "commandName": "CreateOrderCommand",
      "commandId": "123e4567-e89b-12d3-a456-426614174000",
      "timestamp": "2025-02-25T18:30:00Z",
      "payload": {
        "orderId": "ORD12345",
        "customerId": "CUST67890",
        "items": [
          {
            "itemId": "ITEM001",
            "quantity": 2,
            "price": 100
          },
          {
            "itemId": "ITEM002",
            "quantity": 1,
            "price": 50
          }
        ],
        "totalAmount": 250
      },
      "metadata": {
        "initiatedBy": "user123",
        "sourceSystem": "OrderService"
      },
      "validationRules": {
        "orderId": "required|string",
        "customerId": "required|string",
        "items": "required|array",
        "totalAmount": "required|number"
      }
    },
    {
      "commandName": "UpdateInventoryCommand",
      "commandId": "e7d4a5b3-4f9d-4879-a978-3c6d0b7b16d3",
      "timestamp": "2025-02-25T18:35:00Z",
      "payload": {
        "itemId": "ITEM001",
        "quantity": 10
      },
      "metadata": {
        "initiatedBy": "inventoryManager",
        "sourceSystem": "InventoryService"
      },
      "validationRules": {
        "itemId": "required|string",
        "quantity": "required|number|min:0"
      }
    }
  ],
  "metadata": {
    "initiatedBy": "batchProcessor",
    "sourceSystem": "OrderProcessingService"
  }
}
```

### Explanation

1. **Domain:** The `domain` field specifies the domain or bounded context to which the commands belong (`OrderManagement` in this case).

By including the domain or namespace in the command specification, we can improve the overall structure and manageability of the system. 

# Command Response Specification

Designing a command response specification is equally important to ensure clarity, consistency, and meaningful feedback.

Here’s an example structure for a command response specification:

### Command Response Specification Structure

1. **Response Id:** A unique identifier for the response.
2. **Correlation Id:** Matches the correlation Id from the request to tie the response back to the original request.
3. **Command Id:** The identifier of the command that this response is for.
4. **Timestamp:** When the response was generated.
5. **Status:** The status of the command execution (e.g., success, failure, pending).
6. **Payload:** The result or any data returned by the command execution.
7. **Errors:** Any errors encountered during the command execution.
8. **Metadata:** Additional information about the response.

### Example Command Response Specification

```json
{
  "responseId": "b3d9f4c5-1a2e-4b8d-8e9b-3e6d4f7b5d8a",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "commandId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-02-25T19:05:00Z",
  "status": "success",
  "payload": {
    "orderId": "ORD12345",
    "message": "Order created successfully"
  },
  "errors": [],
  "metadata": {
    "processedBy": "OrderService",
    "processingTime": "100ms"
  }
}
```

### Explanation of the Structure

1. **Response Id:** Unique identifier for the response (`b3d9f4c5-1a2e-4b8d-8e9b-3e6d4f7b5d8a`).
2. **Correlation Id:** Matches the correlation Id from the request to tie the response back to the original request (`123e4567-e89b-12d3-a456-426614174123`).
3. **Command Id:** The identifier of the command that this response is for (`123e4567-e89b-12d3-a456-426614174000`).
4. **Timestamp:** When the response was generated (`2025-02-25T19:05:00Z`).
5. **Status:** The status of the command execution (`success`).
6. **Payload:** The result or any data returned by the command execution. In this case, it includes `orderId` and a success message.
7. **Errors:** Any errors encountered during the command execution. Here, it's an empty array, indicating no errors.
8. **Metadata:** Additional information about the response, such as `processedBy` indicating the service that processed the command, and `processingTime` showing how long it took to process.

### Key Considerations

- **Correlation Id:** Ensures that responses can be traced back to their corresponding requests.
- **Status:** Clearly indicates the outcome of the command execution.
- **Errors:** Provides detailed error information for debugging and handling failures.
- **Metadata:** Adds context to the response, such as processing time and the service that handled the command.

Including these elements in the command response specification will help ensure that responses are informative, traceable, and useful for both clients and developers.

# Multiple Command Responses

When dealing with multiple commands sent within a single request, it's important to provide a response that clearly indicates the status and results of each individual command. Here’s a suggested structure for a partner command response specification:

### Batch Command Response Specification Structure

1. **Response Id:** A unique identifier for the overall partner command response.
2. **Correlation Id:** Matches the correlation Id from the request to tie the response back to the original request.
3. **Batch Command Id:** The identifier of the partner command that this response is for.
4. **Timestamp:** When the response was generated.
5. **Overall Status:** The overall status of the partner command execution (e.g., success, partial success, failure).
6. **Individual Command Responses:** An array of responses for each individual command within the partner command.
7. **Metadata:** Additional information about the overall response.

### Example Partner Command Response Specification

```json
{
  "responseId": "f3d9f4c5-1a2e-4b8d-8e9b-3e6d4f7b5d8a",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "BatchCommandId": "789e4567-e89b-12d3-a456-426614174789",
  "timestamp": "2025-02-25T19:10:00Z",
  "overallStatus": "partial_success",
  "individualCommandResponses": [
    {
      "commandName": "CreateOrderCommand",
      "commandId": "123e4567-e89b-12d3-a456-426614174000",
      "status": "success",
      "payload": {
        "orderId": "ORD12345",
        "message": "Order created successfully"
      },
      "errors": [],
      "metadata": {
        "processedBy": "OrderService",
        "processingTime": "100ms"
      }
    },
    {
      "commandName": "UpdateInventoryCommand",
      "commandId": "e7d4a5b3-4f9d-4879-a978-3c6d0b7b16d3",
      "status": "failure",
      "payload": {},
      "errors": [
        {
          "errorCode": "OUT_OF_STOCK",
          "errorMessage": "Item ITEM001 is out of stock"
        }
      ],
      "metadata": {
        "processedBy": "InventoryService",
        "processingTime": "120ms"
      }
    }
  ],
  "metadata": {
    "processedBy": "OrderProcessingService",
    "totalProcessingTime": "220ms"
  }
}
```

### Explanation of the Structure

1. **Response Id:** Unique identifier for the overall partner command response (`f3d9f4c5-1a2e-4b8d-8e9b-3e6d4f7b5d8a`).
2. **Correlation Id:** Matches the correlation Id from the request to tie the response back to the original request (`123e4567-e89b-12d3-a456-426614174123`).
3. **Batch Command Id:** The identifier of the partner command that this response is for (`789e4567-e89b-12d3-a456-426614174789`).
4. **Timestamp:** When the response was generated (`2025-02-25T19:10:00Z`).
5. **Overall Status:** Indicates the overall status of the partner command execution (`partial_success` in this case).
6. **Individual Command Responses:** An array containing the response for each individual command:
   - **CreateOrderCommand:** Status is `success`, with a payload containing `orderId` and a success message.
   - **UpdateInventoryCommand:** Status is `failure`, with an error message indicating that the item is out of stock.
7. **Metadata:** Additional information about the overall response, such as the service that processed the partner command and the total processing time.


By following this structure, we can ensure that each command's response is clearly documented and easy to trace, while also providing an overall summary of the partner command execution.


# Content-Type in Response

including the `contentType` in the command response specification can be very useful. The `contentType` indicates the format or type of data contained in the response, which helps in correctly interpreting and processing the response. Here's why it makes sense to include it:

### Benefits of Including contentType

1. **Data Interpretation:** Clearly indicates the format of the response data (e.g., JSON, XML, plain text), ensuring that the consuming service or application can parse and handle the data correctly.
2. **Interoperability:** Enhances interoperability between different systems and services by explicitly stating the content type.
3. **Validation:** Assists in validating the response data by ensuring it matches the expected content type.
4. **Security:** Helps in preventing issues related to data injection attacks by explicitly defining the type of content expected.
5. **Flexibility:** Allows for different types of responses based on the content type, providing flexibility in how data is communicated.

### Updated Command Response Specification Example

Here’s how we can include the `contentType` in the command response specification:

```json
{
  "responseId": "b3d9f4c5-1a2e-4b8d-8e9b-3e6d4f7b5d8a",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "commandId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-02-25T19:05:00Z",
  "status": "success",
  "contentType": "application/json",
  "payload": {
    "orderId": "ORD12345",
    "message": "Order created successfully"
  },
  "errors": [],
  "metadata": {
    "processedBy": "OrderService",
    "processingTime": "100ms"
  }
}
```

### Key Elements

1. **responseId:** Unique identifier for the response.
2. **correlationId:** Matches the correlation Id from the request to tie the response back to the original request.
3. **commandId:** The identifier of the command that this response is for.
4. **timestamp:** When the response was generated.
5. **status:** The status of the command execution (e.g., success, failure, pending).
6. **contentType:** Specifies the format of the response data (`application/json` in this case).
7. **payload:** The result or any data returned by the command execution.
8. **errors:** Any errors encountered during the command execution.
9. **metadata:** Additional information about the response.

### Summary

Including the `contentType` in the command response specification provides clear information about the format of the response data, enhancing data interpretation, interoperability, validation. 

In a typical HTTP response, the `Content-Type` is indeed included as a header field. Including it within the body of a command response specification can seem redundant. However, in certain scenarios, it can still be beneficial to have `contentType` explicitly mentioned in the response body:

### When It's Useful to Include contentType in the Response Body

1. **Non-HTTP Protocols:**
   - When using protocols other than HTTP (e.g., message brokers like Kafka, Azure Event Hubs), where headers are not standard or uniformly handled, having `contentType` in the response body ensures that the data format is explicitly stated.

2. **Logging and Auditing:**
   - Including `contentType` within the response body provides a self-contained log record, which can be useful for auditing and debugging purposes, especially when reviewing stored responses.

3. **Consistency Across Transport Layers:**
   - By including `contentType` in the response body, we ensure that the data format information is consistently available regardless of the transport layer. This can be particularly helpful in multi-protocol architectures.

### Example Usage in HTTP

When dealing with HTTP responses, the `Content-Type` header is typically sufficient:

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "responseId": "b3d9f4c5-1a2e-4b8d-8e9b-3e6d4f7b5d8a",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "commandId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-02-25T19:05:00Z",
  "status": "success",
  "payload": {
    "orderId": "ORD12345",
    "message": "Order created successfully"
  },
  "errors": [],
  "metadata": {
    "processedBy": "OrderService",
    "processingTime": "100ms"
  }
}
```

In this example, the `Content-Type` header specifies the format of the response as `application/json`.

### Conclusion

While the `Content-Type` header in HTTP responses is typically sufficient to indicate the data format, including `contentType` within the response body can still be useful in certain scenarios, such as non-HTTP protocols, logging, auditing, and ensuring consistency across different transport layers.


# Async Command Request
Commands can be asynchronous. When dealing with asynchronous commands, it’s important to ensure that both the request and response specifications clearly indicate the asynchronous nature of the command and provide the necessary details for handling the asynchronous processing.

### Identifying Asynchronous Commands in the Request and Response Specifications

#### Request Specification

1. **Command Type:** Indicate that the command is asynchronous.
2. **Callback URL:** Provide a URL where the response should be sent once the command is processed.
3. **Command Status:** Optional field to initially indicate that the command has been received and is being processed.

#### Response Specification

1. **Command Status:** Indicate the status of the command (e.g., pending, completed, failed).
2. **Callback Response:** A separate response sent to the callback URL with the final result once the command is processed.

### Example Asynchronous Command Request Specification

Here's an example of an asynchronous command request:

```json
{
  "commandName": "ProcessOrderCommand",
  "commandId": "789e4567-e89b-12d3-a456-426614174789",
  "timestamp": "2025-02-25T19:00:00Z",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "commandType": "asynchronous",
  "callbackUrl": "https://example.com/callback",
  "payload": {
    "orderId": "ORD12345",
    "customerId": "CUST67890",
    "items": [
      {
        "itemId": "ITEM001",
        "quantity": 2,
        "price": 100
      },
      {
        "itemId": "ITEM002",
        "quantity": 1,
        "price": 50
      }
    ],
    "totalAmount": 250
  },
  "metadata": {
    "initiatedBy": "user123",
    "sourceSystem": "OrderService"
  }
}
```

### Example Asynchronous Command Initial Response Specification

Here's an example of the initial response sent immediately after receiving the command:

```json
{
  "responseId": "b3d9f4c5-1a2e-4b8d-8e9b-3e6d4f7b5d8a",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "commandId": "789e4567-e89b-12d3-a456-426614174789",
  "timestamp": "2025-02-25T19:00:01Z",
  "status": "pending",
  "message": "Command received and processing started.",
  "metadata": {
    "processedBy": "OrderService"
  }
}
```

### Example Asynchronous Command Callback Response Specification

Here's an example of the final response sent to the callback URL once the command is processed:

```json
{
  "responseId": "d3e9f6c8-4a2f-5b6d-9e8b-4f3d7c5f6d8b",
  "correlationId": "123e4567-e89b-12d3-a456-426614174123",
  "commandId": "789e4567-e89b-12d3-a456-426614174789",
  "timestamp": "2025-02-25T19:05:00Z",
  "status": "success",
  "payload": {
    "orderId": "ORD12345",
    "message": "Order processed successfully"
  },
  "errors": [],
  "metadata": {
    "processedBy": "OrderService",
    "processingTime": "5 minutes"
  }
}
```

### Explanation

- **Command Type:** The `commandType` field in the request indicates that the command is asynchronous.
- **Callback URL:** The `callbackUrl` field in the request specifies where the final response should be sent once the command is processed.
- **Initial Response:** The initial response is sent immediately to confirm that the command has been received and is being processed, with a status of "pending."
- **Callback Response:** The final response is sent to the callback URL once the command is fully processed, with the final status and results.

By including these fields, we can clearly indicate the asynchronous nature of the command and ensure that the request and response specifications provide all the necessary details for handling asynchronous processing. 
This approach enhances the reliability and clarity of the system's communication.


# Combine Sync and Async Commands

 Combining multiple command responses into a single response object can provide a more organized and concise way to handle the results of multiple commands. 
This way, we can have a single response object that encapsulates the responses of all commands, making it easier to track and manage the overall process.

### Example Combined Response Specification

Here's how you can structure a single response object that includes multiple command responses:

```json
{
  "responseId": "combinedResponse123",
  "correlationId": "xyz456",
  "timestamp": "2025-02-25T19:05:00Z",
  "status": "partial_success",
  "commandResponses": [
    {
      "commandName": "CheckInventoryCommand",
      "commandId": "abc123",
      "status": "success",
      "timestamp": "2025-02-25T19:00:01Z",
      "payload": {
        "itemId": "ITEM001",
        "availableQuantity": 10,
        "message": "Item is available"
      },
      "errors": [],
      "metadata": {
        "processedBy": "InventoryService",
        "processingTime": "1s"
      }
    },
    {
      "commandName": "ProcessPaymentCommand",
      "commandId": "def456",
      "status": "pending",
      "timestamp": "2025-02-25T19:00:02Z",
      "message": "Payment processing started",
      "metadata": {
        "processedBy": "PaymentService"
      }
    }
  ],
  "metadata": {
    "overallProcessingTime": "5 minutes",
    "initiatedBy": "user123"
  }
}
```

### Explanation

1. **responseId:** Unique identifier for the combined response.
2. **correlationId:** Matches the correlation Id from the request to tie the combined response back to the original request.
3. **timestamp:** When the combined response was generated.
4. **status:** Overall status of the command execution (e.g., partial_success if not all commands succeeded).
5. **commandResponses:** An array containing individual responses for each command.
   - **commandName:** The name of the command.
   - **commandId:** Unique identifier for the command.
   - **status:** The status of the command execution (e.g., success, pending).
   - **timestamp:** When the individual command response was generated.
   - **payload:** The result or data returned by the command execution.
   - **errors:** Any errors encountered during the command execution.
   - **metadata:** Additional information about the command execution.
6. **metadata:** Additional information about the combined response, such as overall processing time and the entity that initiated the request.

### Handling Asynchronous Callbacks

For asynchronous commands, you can update the `commandResponses` array with the final results once the processing is complete:

```json
{
  "responseId": "combinedResponse123",
  "correlationId": "xyz456",
  "timestamp": "2025-02-25T19:05:00Z",
  "status": "complete_success",
  "commandResponses": [
    {
      "commandName": "CheckInventoryCommand",
      "commandId": "abc123",
      "status": "success",
      "timestamp": "2025-02-25T19:00:01Z",
      "payload": {
        "itemId": "ITEM001",
        "availableQuantity": 10,
        "message": "Item is available"
      },
      "errors": [],
      "metadata": {
        "processedBy": "InventoryService",
        "processingTime": "1s"
      }
    },
    {
      "commandName": "ProcessPaymentCommand",
      "commandId": "def456",
      "status": "success",
      "timestamp": "2025-02-25T19:05:00Z",
      "payload": {
        "orderId": "ORD12345",
        "transactionId": "TRANS67890",
        "message": "Payment processed successfully"
      },
      "errors": [],
      "metadata": {
        "processedBy": "PaymentService",
        "processingTime": "5 minutes"
      }
    }
  ],
  "metadata": {
    "overallProcessingTime": "5 minutes",
    "initiatedBy": "user123"
  }
}
```

### Summary

By using a single response object that includes multiple command responses, you can provide a clear and organized view of the overall process. This approach makes it easier to track and manage the results of multiple commands, whether they are synchronous or asynchronous.









