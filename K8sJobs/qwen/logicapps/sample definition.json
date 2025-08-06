Absolutely! Below is a **sample Azure Logic App (Consumption) workflow** that **batches read messages from an Azure Service Bus queue** using a **Recurrence trigger**, retrieves multiple messages via **Peek-Lock**, processes them in a loop, and then **completes each message** manually.

This pattern enables **batch processing** — multiple messages handled in a single workflow run.

---

### ✅ Scenario
- Every 2 minutes, the Logic App checks the Service Bus queue.
- It retrieves up to **10 messages** in a batch.
- Each message is processed (e.g., logged, transformed, sent to another system).
- After processing, each message is marked as **completed** to remove it from the queue.

> ⚠️ You must **manually complete** each message when using Peek-Lock.

---

### 🧩 Sample Workflow Definition (JSON - Logic Apps ARM Template / Code View)

```json
{
  "definition": {
    "$schema": "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#",
    "contentVersion": "1.0.0.0",
    "parameters": {},
    "triggers": {
      "Recurrence": {
        "type": "Recurrence",
        "recurrence": {
          "frequency": "Minute",
          "interval": 2
        }
      }
    },
    "actions": {
      "Initialize_MessageBatch": {
        "type": "InitializeVariable",
        "inputs": {
          "variables": [
            {
              "name": "MessageBatch",
              "type": "array",
              "value": []
            }
          ]
        },
        "runAfter": {}
      },
      "Peek-Lock_Message": {
        "type": "ServiceBus",
        "inputs": {
          "host": {
            "connection": {
              "name": "@parameters('$connections')['servicebus']['connectionId']"
            }
          },
          "method": "get",
          "path": "/@{encodeURIComponent('your-queue-name')}/messages",
          "queries": {
            "api-version": "2021-04-01",
            "timeout": 60,
            "maxMessages": 10
          }
        },
        "runAfter": {
          "Initialize_MessageBatch": ["Succeeded"]
        }
      },
      "Check_If_Messages_Received": {
        "type": "Condition",
        "expression": {
          "not": {
            "equals": [
              "@length(body('Peek-Lock_Message')?['value'])",
              0
            ]
          }
        },
        "actions": {
          "For_each_Message": {
            "type": "Foreach",
            "foreach": "@body('Peek-Lock_Message')?['value']",
            "actions": {
              "Log_or_Process_Message_Content": {
                "type": "Compose",
                "inputs": {
                  "MessageId": "@item()?['MessageId']",
                  "Body": "@{coalesce(body('Peek-Lock_Message')?['ContentData'], 'No content')}",
                  "Label": "@item()?['Label']",
                  "ProcessedAt": "@utcNow()"
                },
                "runAfter": {}
              },
              "YOUR_PROCESSING_LOGIC_HERE": {
                "type": "Http",
                "inputs": {
                  "method": "POST",
                  "uri": "https://your-api.example/process",
                  "body": "@body('Log_or_Process_Message_Content')",
                  "headers": {
                    "Content-Type": "application/json"
                  }
                },
                "runAfter": {
                  "Log_or_Process_Message_Content": ["Succeeded"]
                }
              },
              "Complete_Message": {
                "type": "ServiceBus",
                "inputs": {
                  "host": {
                    "connection": {
                      "name": "@parameters('$connections')['servicebus']['connectionId']"
                    }
                  },
                  "method": "post",
                  "path": "/@{encodeURIComponent('your-queue-name')}/messages/@{item()?['LockToken']}/complete"
                },
                "runAfter": {
                  "YOUR_PROCESSING_LOGIC_HERE": ["Succeeded"]
                }
              }
            },
            "runAfter": {
              "Log_or_Process_Message_Content": ["Succeeded"]
            }
          }
        },
        "runAfter": {
          "Peek-Lock_Message": ["Succeeded"]
        }
      }
    },
    "outputs": {},
    "parameters": {
      "$connections": {
        "defaultValue": {},
        "type": "Object"
      }
    }
  },
  "parameters": {
    "$connections": {
      "value": {
        "servicebus": {
          "connectionId": "/subscriptions/your-sub-id/resourceGroups/your-rg/providers/Microsoft.Web/connections/servicebus",
          "connectionName": "servicebus",
          "id": "/subscriptions/your-sub-id/providers/Microsoft.Web/locations/westus/managedApiConnections/servicebus"
        }
      }
    }
  }
}
```

---

### 🔧 Key Configuration Notes

1. **Replace placeholders:**
   - `'your-queue-name'` → your actual Service Bus queue name
   - `'https://your-api.example/process'` → your actual processing endpoint (or replace with another action like Azure Function, Storage, etc.)
   - Update the subscription, resource group, and connection details.

2. **Service Bus Connection:**
   - Ensure you have a valid **Service Bus connection** set up in the Logic App.
   - The connection must have **Listen and Manage** permissions (needed for Peek-Lock and Complete).

3. **Error Handling (Optional but Recommended):**
   Add a `Scope` or `Terminate` action inside the loop if processing fails, and consider:
   - **Abandon Message** (to requeue)
   - **Dead-letter Message** (if invalid)

   Example: Add on failure:
   ```json
   "Abandon_Message": {
     "type": "ServiceBus",
     "inputs": {
       "host": {
         "connection": {
           "name": "@parameters('$connections')['servicebus']['connectionId']"
         }
       },
       "method": "post",
       "path": "/@{encodeURIComponent('your-queue-name')}/messages/@{item()?['LockToken']}/abandon"
     },
     "runAfter": {
       "YOUR_PROCESSING_LOGIC_HERE": ["Failed"]
     }
   }
   ```

---

### ✅ Benefits of This Approach

- **Batch processing**: Up to 10 (or more) messages per run.
- **Cost efficient**: Fewer workflow executions.
- **Controlled processing**: Handle messages in bulk (e.g., batch insert into DB, aggregate alerts).
- **Reliable**: Manual lock and complete ensures no message loss.

---

### 📝 Limitations & Best Practices

- **Lock Duration**: Messages are locked for a default time (usually 30 seconds). Ensure your workflow completes within the **lock timeout**.
  - You can renew the lock if needed (advanced).
- **Max Messages**: `maxMessages` can be up to 10 in Logic Apps (Service Bus limitation for REST API).
- **Throughput**: For high-volume queues, consider **Azure Functions** with native batch support (`Message[]`).

---

### ✅ Want to Scale Further?

For **high-throughput scenarios**, consider:
- **Azure Function** with Service Bus trigger (natively supports batch of `Message[]`)
- Then call Logic App for orchestration if needed.

Example Function signature:
```csharp
public static void Run([ServiceBusTrigger("myqueue", Connection = "SB_CONN")] Message[] messages, ILogger log)
```

---

Let me know if you'd like:
- A **PowerShell/ARM template** to deploy this
- **Error handling** enhancements
- Integration with **Azure Blob Storage**, **Event Grid**, or **SQL DB**

Happy automating! 🚀