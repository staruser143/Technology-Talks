| Field | Description |
|----------|----------|
| **responseId** | A unique identifier for the response |
| **correlationId** | Matches the correlation Id from the request to tie the response back to the original request.. |
| **commandId** | The identifier of the command that this response is for |
| **batchCommandId** | The identifier of the batch command that this response is for. |
| **commandResponses** | An array of responses for each individual command within the Batch command. |
| **commandName** | An array of responses for each individual command within the Batch command. |
| **timestamp** | When the response was generated. |
| **contentType** | Specifies the format of the response data (e.g., application/json ).. |
| **overallStatus** |The overall status of the batch command execution (e.g., success, partial success, failure).. |
| **status** | The status of the command execution (e.g., success, failure, pending). |
| **payload** |  The result or any data returned by the command execution.|
| **errors** |Any errors encountered during the command execution. |
| **metadata** | Additional information about the response.|
