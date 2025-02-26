| **Field** | **Description** |
|----------|----------|
| **commandName** | A descriptive name for the command, indicating the action to be performed |
| **commandId** | A unique identifier (UUID) for each command to ensure idempotency and traceability. |
| **commandType** |Indicate that the command is asynchronous. |
| **callbackUrl** | Provide a URL where the response should be sent once the async command is processed. |
| **timestamp** | The timestamp when the command was created or sent. |
| **correlationId** | Unique identifier for correlating related requests. |
| **Payload** | The data required to execute the command, typically represented as an object |
| **Metadata** | Additional information about the command, such as the user who initiated it, the source system, etc |
| **validationRules** | Define validation rules to ensure the command's payload meets the required criteria |
| **batchCommandName** | A descriptive name for the Batch command, indicating the batch of commands to be executed |
| **batchCommandId** | A unique identifier (UUID) for the Batch command.. |
| **Domain** | The domain field specifies the domain or bounded context to which the commands belong |
