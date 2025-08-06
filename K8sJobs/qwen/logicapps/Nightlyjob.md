**Yes, absolutely!** You can trigger an Azure Logic App **once per day (as a nightly job)** and still **process multiple Service Bus queue messages in batch** during that single run.

This is actually a **common and effective pattern** for:
- Nightly data aggregation
- Batch reporting
- End-of-day processing
- Bulk data exports or cleanups

---

### ✅ Goal
Run a Logic App **once per day** (e.g., at 2:00 AM), and during that run:
- Read **all available messages** (or up to a limit) from a Service Bus queue
- Process them in **batch** (e.g., transform, send to DB, API, or storage)
- Complete each message so it’s removed from the queue

---

### ✅ How to Do It

Use the **same batch-processing pattern** as before, but with a **daily Recurrence trigger**.

---

### 🛠️ Step-by-Step: Nightly Batch Processing Workflow

#### 1. **Trigger: Run Once Per Day**
```json
"triggers": {
  "Recurrence": {
    "type": "Recurrence",
    "recurrence": {
      "frequency": "Day",
      "interval": 1,
      "timeZone": "UTC",
      "startTime": "2024-04-05T02:00:00Z"
    }
  }
}
```

> ✅ This runs **every day at 2:00 AM UTC**. Adjust `startTime` and `timeZone` (e.g., `"timeZone": "Pacific Standard Time"`) as needed.

---

#### 2. **Action: Peek-Lock Multiple Messages**
Use the **Service Bus "Peek-Lock Message" action** to retrieve up to 10 messages at a time.

> ⚠️ The REST API (used by Logic Apps) limits `maxMessages` to **10 per call**.

```json
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
  }
}
```

---

#### 3. **Loop: Process All Available Messages**

Since you may have **more than 10 messages**, use a **`Do Until` loop** to keep reading until no more messages are available.

#### ✅ Enhanced Workflow with Looping

```json
"actions": {
  "Initialize_MessageBatch": {
    "type": "InitializeVariable",
    "inputs": {
      "variables": [
        {
          "name": "HasMoreMessages",
          "type": "boolean",
          "value": true
        },
        {
          "name": "ProcessedMessageCount",
          "type": "integer",
          "value": 0
        }
      ]
    }
  },
  "Do_Until_No_More_Messages": {
    "type": "Until",
    "expression": "@equals(variables('HasMoreMessages'), false)",
    "limit": {
      "count": 100,
      "timeout": "PT15M"
    },
    "actions": {
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
            "timeout": 30,
            "maxMessages": 10
          }
        }
      },
      "Condition_Check_Messages": {
        "type": "If",
        "expression": "@greater(length(body('Peek-Lock_Message')?['value']), 0)",
        "actions": {
          "For_each_Message": {
            "type": "Foreach",
            "foreach": "@body('Peek-Lock_Message')?['value']",
            "actions": {
              "Process_Message": {
                "type": "Compose",
                "inputs": "Processing message: @{item()?['MessageId']}"
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
                }
              }
            }
          },
          "Increment_Counter": {
            "type": "IncrementVariable",
            "inputs": {
              "name": "ProcessedMessageCount",
              "value": "@length(body('Peek-Lock_Message')?['value'])"
            }
          }
        },
        "else": {
          "actions": {
            "Set_HasMoreMessages_To_False": {
              "type": "SetVariable",
              "inputs": {
                "name": "HasMoreMessages",
                "value": false
              }
            }
          }
        }
      }
    }
  },
  "Log_Total_Processed": {
    "type": "Compose",
    "inputs": "Total messages processed: @{variables('ProcessedMessageCount')}",
    "runAfter": {
      "Do_Until_No_More_Messages": ["Succeeded"]
    }
  }
}
```

---

### ✅ What This Does

| Step | Description |
|------|-------------|
| 🔁 **Recurrence Trigger** | Runs once per day at 2:00 AM |
| 🔁 **Do Until Loop** | Keeps fetching up to 10 messages until queue is empty |
| 📥 **Peek-Lock** | Retrieves batch of messages (up to 10) |
| 🔄 **For Each** | Processes and completes each message |
| ✅ **Complete Message** | Removes message from queue |
| 📊 **Counter** | Tracks total processed messages |

---

### ✅ Benefits of Nightly Batch Processing

- ✅ **Cost-effective**: One execution per day, even if processing 1000+ messages
- ✅ **Predictable load**: Runs during off-peak hours
- ✅ **Bulk operations**: Ideal for ETL, reporting, archiving
- ✅ **Reduced API calls**: Process many messages in one context

---

### ⚠️ Important Considerations

| Consideration | Recommendation |
|--------------|----------------|
| **Lock Duration** | Ensure processing completes within lock time (default 30 sec). Extend it in Service Bus settings if needed (up to 5 min). |
| **Workflow Timeout** | Consumption plan: max 90 days, but best to keep under 10–15 mins for nightly jobs. Standard has more flexibility. |
| **Large Queues** | If you have **thousands of messages**, consider **Azure Function + Durable Task** for better performance. |
| **Time Zone** | Set `timeZone` in recurrence trigger to match your business day. |
| **Monitoring** | Use **Log Analytics** or **Application Insights** to track daily run success/failure. |

---

### ✅ Alternative: Use Azure Function (Recommended for Large Batches)

If you expect **hundreds or thousands of messages**, a **nightly Azure Function** with Service Bus trigger (batched) is more efficient:

```csharp
[FunctionName("NightlyBatchProcessor")]
public async Task Run(
    [TimerTrigger("0 0 2 * * *")] TimerInfo myTimer,
    [ServiceBus("myqueue", Connection = "SB_CONN")] ServiceBusReceiver receiver)
{
    var messages = await receiver.ReceiveMessagesAsync(maxMessages: 1000, maxWaitTime: TimeSpan.FromSeconds(30));
    foreach (var msg in messages)
    {
        // Process
        await ProcessMessageAsync(msg);
        await receiver.CompleteMessageAsync(msg);
    }
}
```

Then call a Logic App if you need orchestration.

---

### ✅ Summary

| Requirement | Supported? | How |
|----------|------------|-----|
| Run once per day | ✅ Yes | Use `Recurrence` with `frequency: Day` |
| Process multiple messages | ✅ Yes | Use `Peek-Lock` + `Do Until` loop |
| Handle large queues | ✅ Yes | Loop until empty (max 10 per call) |
| Remove messages from queue | ✅ Yes | Manually `Complete Message` |
| Best for nightly ETL/reporting | ✅ Yes | Ideal use case |

---

Would you like:
- A **full downloadable JSON workflow**?
- A **PowerShell script** to deploy this?
- A **version using Azure Functions + Logic App**?

Let me know — I can provide it! 🚀