# Query
I have a Azure Service bus Queue which has events filtered from a Kafka topic which match a specific condition. I want to have a Azure Logic App Workflow triggered once per day which would read the items in the queue and process them accordingly. If processed successfully based on business criteria , it has to remove the item from the queue else leave it back in the queue for later processing.  How do I design the logic app workflow accordingly.

To design an **Azure Logic App Workflow** that processes messages from an **Azure Service Bus Queue** once per day, with conditional message completion or deferral, you can follow this architecture:

---

### ✅ **High-Level Workflow Design**

1. **Trigger**: Recurrence trigger (once per day).
2. **Action**: Connect to Azure Service Bus Queue and **peek-lock** messages.
3. **Loop**: For each message:
   - Evaluate business logic.
   - If successful:
     - **Complete** the message (removes it from the queue).
   - If not:
     - **Abandon** the message (returns it to the queue for later processing).

---

### 🛠️ **Step-by-Step Implementation in Logic Apps (Standard)**

#### 1. **Trigger**
- Use the **Recurrence** trigger.
  - Frequency: `Day`
  - Interval: `1`

#### 2. **Get Messages from Queue**
- Use the **"Service Bus - Peek-lock message"** action.
  - Choose your **Service Bus namespace**, **queue name**, and **connection**.
  - This locks the message for processing without removing it.

#### 3. **Loop Through Messages**
- Use a **"Until"** loop or **"For each"** loop to iterate over messages.
  - Inside the loop:
    - Parse the message body.
    - Apply your **business logic** using **Condition** control.

#### 4. **Conditional Processing**
- If business logic passes:
  - Use **"Complete the message"** action (removes it from the queue).
- If business logic fails:
  - Use **"Abandon the message"** action (returns it to the queue).

> ⚠️ Note: Logic Apps Standard supports **peek-lock** and **manual message settlement**, which is essential for this pattern. Logic Apps Consumption does **not** support this level of control.

---

### 🧠 Additional Tips

- **Batching**: You can use the **"Get messages in batch"** action if you expect multiple messages.
- **Dead-lettering**: Consider adding logic to **dead-letter** messages after a certain number of retries.
- **Monitoring**: Enable diagnostics and alerts for failures or message processing issues.

---
# Query
Yes, please provide a sample JSON.

Here’s a **sample Logic App Standard workflow definition in JSON** that runs **once per day**, reads messages from an **Azure Service Bus Queue**, evaluates a condition, and either completes or abandons the message based on business logic.

This sample assumes:
- You have a Service Bus queue named `my-queue`.
- You have a connection named `servicebusConnection`.
- The business condition checks if the message body contains `"status": "valid"`.

---

### 🧾 Sample Logic App Workflow (Standard) – JSON

```json
{
  "definition": {
    "$schema": "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2019-05-01/workflowDefinition.json",
    "actions": {
      "PeekLockMessage": {
        "type": "ServiceBusPeekLockMessage",
        "inputs": {
          "queueName": "my-queue",
          "connection": {
            "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
          }
        },
        "runAfter": {},
        "metadata": {
          "operationMetadataId": "peekLock"
        }
      },
      "ForEachMessage": {
        "type": "Foreach",
        "foreach": "@body('PeekLockMessage')",
        "actions": {
          "CheckStatus": {
            "type": "If",
            "expression": {
              "equals": [
                "@items('ForEachMessage')?['status']",
                "valid"
              ]
            },
            "actions": {
              "CompleteMessage": {
                "type": "ServiceBusCompleteMessage",
                "inputs": {
                  "queueName": "my-queue",
                  "lockToken": "@items('ForEachMessage')?['lockToken']",
                  "connection": {
                    "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
                  }
                }
              }
            },
            "else": {
              "actions": {
                "AbandonMessage": {
                  "type": "ServiceBusAbandonMessage",
                  "inputs": {
                    "queueName": "my-queue",
                    "lockToken": "@items('ForEachMessage')?['lockToken']",
                    "connection": {
                      "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
                    }
                  }
                }
              }
            }
          }
        },
        "runAfter": {
          "PeekLockMessage": ["Succeeded"]
        }
      }
    },
    "triggers": {
      "Recurrence": {
        "type": "Recurrence",
        "recurrence": {
          "frequency": "Day",
          "interval": 1
        }
      }
    },
    "parameters": {
      "$connections": {
        "defaultValue": {},
        "type": "Object"
      }
    }
  }
}
```

---

### 🧩 Notes

- You’ll need to **replace** `servicebusConnection` with your actual connection name.
- This uses **peek-lock** to ensure messages are not removed until explicitly completed.
- You can enhance the business logic with more complex conditions or external API calls.

# Query
Can you explain the JSON structure further?

Absolutely! Let’s break down the **Logic App Standard JSON structure** into its key components so you can understand how each part contributes to the workflow.

---

## 🧩 JSON Structure Breakdown

### 1. **`definition`**
This is the main body of the Logic App workflow. It contains:
- **`triggers`**: Defines how the workflow starts.
- **`actions`**: Defines what the workflow does after it starts.
- **`parameters`**: Used for external connections or configuration.

---

### 2. **`triggers`**
```json
"triggers": {
  "Recurrence": {
    "type": "Recurrence",
    "recurrence": {
      "frequency": "Day",
      "interval": 1
    }
  }
}
```
- **Purpose**: Starts the workflow once per day.
- **Type**: `Recurrence` is a built-in trigger for scheduled workflows.
- **Frequency**: Daily, with an interval of 1 day.

---

### 3. **`actions`**
This section contains all the steps that happen after the trigger.

#### a. **PeekLockMessage**
```json
"PeekLockMessage": {
  "type": "ServiceBusPeekLockMessage",
  "inputs": {
    "queueName": "my-queue",
    "connection": {
      "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
    }
  }
}
```
- **Purpose**: Reads messages from the Service Bus queue using **peek-lock**.
- **Peek-lock**: Locks the message for processing without removing it.

#### b. **ForEachMessage**
```json
"ForEachMessage": {
  "type": "Foreach",
  "foreach": "@body('PeekLockMessage')",
  "actions": { ... }
}
```
- **Purpose**: Loops through each message returned by `PeekLockMessage`.
- **`@body('PeekLockMessage')`**: Refers to the output of the previous action.

#### c. **CheckStatus (Condition)**
```json
"CheckStatus": {
  "type": "If",
  "expression": {
    "equals": [
      "@items('ForEachMessage')?['status']",
      "valid"
    ]
  },
  "actions": { ... },
  "else": { ... }
}
```
- **Purpose**: Evaluates business logic.
- **Condition**: Checks if the message has `"status": "valid"`.

#### d. **CompleteMessage**
```json
"CompleteMessage": {
  "type": "ServiceBusCompleteMessage",
  "inputs": {
    "queueName": "my-queue",
    "lockToken": "@items('ForEachMessage')?['lockToken']",
    "connection": { ... }
  }
}
```
- **Purpose**: Removes the message from the queue if processed successfully.

#### e. **AbandonMessage**
```json
"AbandonMessage": {
  "type": "ServiceBusAbandonMessage",
  "inputs": {
    "queueName": "my-queue",
    "lockToken": "@items('ForEachMessage')?['lockToken']",
    "connection": { ... }
  }
}
```
- **Purpose**: Returns the message to the queue for retry later.

---

### 4. **`parameters`**
```json
"parameters": {
  "$connections": {
    "defaultValue": {},
    "type": "Object"
  }
}
```
- **Purpose**: Holds connection references (like Service Bus).
- **`$connections`**: Used to securely reference external services.

---

## 🧠 Summary

| Component         | Purpose                                      |
|------------------|----------------------------------------------|
| `Recurrence`      | Triggers the workflow daily                 |
| `PeekLockMessage` | Reads and locks messages from the queue     |
| `ForEachMessage`  | Loops through each message                  |
| `CheckStatus`     | Applies business logic                      |
| `CompleteMessage` | Removes message if processed successfully   |
| `AbandonMessage`  | Returns message if processing fails         |

---

# Query
Can you elaborate on the Service Bus peek-lock message action and how it is used in this scenario.

Great question! The **Service Bus Peek-Lock Message** action is a key part of building reliable and controlled workflows in Azure Logic Apps, especially when you want to **process messages conditionally** and **avoid losing data** due to transient failures.

---

## 🔍 What is Peek-Lock in Azure Service Bus?

The **peek-lock** mechanism is a two-step message retrieval process:

1. **Peek**: Reads the message **without removing** it from the queue.
2. **Lock**: Temporarily locks the message so **other consumers can't process it**.

This allows your Logic App to:
- Safely inspect and process the message.
- Decide whether to **complete**, **abandon**, **defer**, or **dead-letter** the message.

---

## 🧭 How It's Used in Your Scenario

### 🎯 Goal:
You want to process messages **once per day**, and:
- **Remove** them if processed successfully.
- **Leave them** in the queue if not.

### ✅ Peek-Lock Action Behavior:
- **Action Name**: `ServiceBusPeekLockMessage`
- **Returns**: A list of locked messages with metadata like:
  - `body`: The actual message content.
  - `lockToken`: A unique token used to complete or abandon the message.
  - `messageId`, `deliveryCount`, etc.

### 🔄 Follow-up Actions:
After using `PeekLockMessage`, you use the `lockToken` to:
- **Complete the message**: Removes it from the queue.
- **Abandon the message**: Returns it to the queue for retry.
- Optionally: **Defer** or **Dead-letter** based on retry count or error type.

---

## 🧱 Example Flow

1. **Trigger**: Recurrence (daily).
2. **Action**: `PeekLockMessage` from `my-queue`.
3. **Loop**: For each message:
   - Check if `body.status == "valid"`.
   - If yes → `CompleteMessage` using `lockToken`.
   - If no → `AbandonMessage` using `lockToken`.

---

## 🛡️ Why Use Peek-Lock?

| Feature | Benefit |
|--------|---------|
| **Safe Processing** | Prevents message loss during failures. |
| **Controlled Completion** | You decide when to remove the message. |
| **Retry Support** | Messages can be retried if abandoned. |
| **Dead-lettering** | Can route problematic messages for later inspection. |

---

# Query
How can batching be used in this scenario and does it improve performance?

Using **batching** in your Azure Logic App workflow can significantly improve performance and efficiency when processing messages from a Service Bus queue. Here's how it works and why it's beneficial:

---

## 🚀 What is Batching in Service Bus?

Batching allows you to **retrieve multiple messages at once** instead of one-by-one. This reduces:
- The number of Service Bus calls.
- The overhead of looping and locking each message individually.
- Latency and cost for high-volume scenarios.

---

## 🧭 How to Use Batching in Logic Apps Standard

### ✅ Use the Action:
**`ServiceBusReceiveMessages`** (or similar, depending on connector version)

### Key Parameters:
- **`maxMessageCount`**: Number of messages to retrieve in one batch.
- **`queueName`**: Your target queue.
- **`receiveMode`**: Set to `PeekLock` for controlled processing.
- **`connection`**: Your Service Bus connection reference.

### Example:
```json
{
  "type": "ServiceBusReceiveMessages",
  "inputs": {
    "queueName": "my-queue",
    "maxMessageCount": 50,
    "receiveMode": "PeekLock",
    "connection": {
      "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
    }
  }
}
```

---

## 🔄 Workflow with Batching

1. **Trigger**: Recurrence (daily).
2. **Action**: Receive messages in batch (e.g., 50 at a time).
3. **Loop**: Iterate over the batch.
4. **Condition**: Apply business logic.
5. **Complete or Abandon** each message using its `lockToken`.

---

## 📈 Performance Benefits

| Benefit | Description |
|--------|-------------|
| **Reduced API Calls** | Fewer calls to Service Bus = lower latency and cost. |
| **Faster Processing** | Messages are processed in bulk, reducing loop overhead. |
| **Scalability** | Better suited for queues with high message volume. |
| **Improved Throughput** | More messages handled per Logic App run. |

---

## ⚠️ Considerations

- **Lock Duration**: Ensure your Logic App can process all messages within the lock timeout (default is 30 seconds).
- **Error Handling**: Add retry policies or dead-letter logic for failed batches.
- **Throttling**: Be mindful of Service Bus limits and Logic App concurrency settings.

---

# Query
Show me a JSON sample with batching.

Here’s a **sample Logic App Standard workflow JSON** that uses **batching** to receive multiple messages from an Azure Service Bus queue in one go, processes each message, and either completes or abandons it based on a business condition.

---

### 🧾 Sample JSON with Batching

```json
{
  "definition": {
    "$schema": "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2019-05-01/workflowDefinition.json",
    "actions": {
      "ReceiveBatchMessages": {
        "type": "ServiceBusReceiveMessages",
        "inputs": {
          "queueName": "my-queue",
          "maxMessageCount": 50,
          "receiveMode": "PeekLock",
          "connection": {
            "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
          }
        },
        "runAfter": {}
      },
      "ForEachMessage": {
        "type": "Foreach",
        "foreach": "@body('ReceiveBatchMessages')",
        "actions": {
          "CheckStatus": {
            "type": "If",
            "expression": {
              "equals": [
                "@items('ForEachMessage')?['body']?['status']",
                "valid"
              ]
            },
            "actions": {
              "CompleteMessage": {
                "type": "ServiceBusCompleteMessage",
                "inputs": {
                  "queueName": "my-queue",
                  "lockToken": "@items('ForEachMessage')?['lockToken']",
                  "connection": {
                    "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
                  }
                }
              }
            },
            "else": {
              "actions": {
                "AbandonMessage": {
                  "type": "ServiceBusAbandonMessage",
                  "inputs": {
                    "queueName": "my-queue",
                    "lockToken": "@items('ForEachMessage')?['lockToken']",
                    "connection": {
                      "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
                    }
                  }
                }
              }
            }
          }
        },
        "runAfter": {
          "ReceiveBatchMessages": ["Succeeded"]
        }
      }
    },
    "triggers": {
      "Recurrence": {
        "type": "Recurrence",
        "recurrence": {
          "frequency": "Day",
          "interval": 1
        }
      }
    },
    "parameters": {
      "$connections": {
        "defaultValue": {},
        "type": "Object"
      }
    }
  }
}
```

---

### 🔍 Key Differences from Non-Batched Version

| Feature | Description |
|--------|-------------|
| `ServiceBusReceiveMessages` | Retrieves up to 50 messages in one call. |
| `maxMessageCount` | Controls batch size (adjustable). |
| `receiveMode: PeekLock` | Ensures messages are locked for safe processing. |
| Loop over batch | Processes each message individually within the batch. |

---

# Query

How can dead-lettering support be added to this

To add **dead-lettering support** to your Logic App workflow, you can extend the message processing logic to detect messages that repeatedly fail or meet certain error conditions, and then explicitly **move them to the dead-letter queue**.

---

## 🧭 What is Dead-Lettering?

Dead-lettering is used to isolate messages that:
- Cannot be processed due to errors.
- Exceed the maximum delivery count.
- Fail validation or business rules.

These messages are moved to a **dead-letter subqueue** (`<queue-name>/$DeadLetterQueue`) for later inspection or manual intervention.

---

## 🛠️ How to Add Dead-Lettering in Logic Apps

### ✅ Use the Action:
**`ServiceBusDeadLetterMessage`**

### 🔄 Updated Workflow Logic:
1. **Trigger**: Recurrence (daily).
2. **Receive messages in batch** using `PeekLock`.
3. **Loop** through each message.
4. **Apply business logic**:
   - If valid → `CompleteMessage`
   - If retryable → `AbandonMessage`
   - If non-retryable or exceeds retry count → `DeadLetterMessage`

---

## 🧾 Sample JSON Snippet with Dead-Lettering

Add this inside the `else` block of your condition:

```json
"DeadLetterMessage": {
  "type": "ServiceBusDeadLetterMessage",
  "inputs": {
    "queueName": "my-queue",
    "lockToken": "@items('ForEachMessage')?['lockToken']",
    "deadLetterReason": "BusinessValidationFailed",
    "deadLetterErrorDescription": "Message failed business rule validation",
    "connection": {
      "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
    }
  }
}
```

You can also add logic to check `deliveryCount` or a custom retry counter to decide when to dead-letter.

---

## 🧠 Best Practices

| Practice | Benefit |
|---------|---------|
| **Track delivery count** | Avoid infinite retries. |
| **Log dead-lettered messages** | Helps with diagnostics. |
| **Use meaningful reasons** | Easier to triage later. |
| **Monitor dead-letter queue** | Set up alerts or dashboards. |

---

# Query

Show me the full updated JSON sample.

Here’s the **full updated Logic App Standard workflow JSON** that includes:

- **Batching**: Retrieves up to 50 messages at once.
- **Business Logic**: Checks if `status == "valid"`.
- **Message Handling**:
  - `CompleteMessage` if valid.
  - `AbandonMessage` if retryable.
  - `DeadLetterMessage` if non-retryable (e.g., status is `"invalid"`).

---

### 🧾 Full JSON with Batching + Dead-Lettering

```json
{
  "definition": {
    "$schema": "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2019-05-01/workflowDefinition.json",
    "actions": {
      "ReceiveBatchMessages": {
        "type": "ServiceBusReceiveMessages",
        "inputs": {
          "queueName": "my-queue",
          "maxMessageCount": 50,
          "receiveMode": "PeekLock",
          "connection": {
            "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
          }
        },
        "runAfter": {}
      },
      "ForEachMessage": {
        "type": "Foreach",
        "foreach": "@body('ReceiveBatchMessages')",
        "actions": {
          "CheckStatus": {
            "type": "If",
            "expression": {
              "equals": [
                "@items('ForEachMessage')?['body']?['status']",
                "valid"
              ]
            },
            "actions": {
              "CompleteMessage": {
                "type": "ServiceBusCompleteMessage",
                "inputs": {
                  "queueName": "my-queue",
                  "lockToken": "@items('ForEachMessage')?['lockToken']",
                  "connection": {
                    "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
                  }
                }
              }
            },
            "else": {
              "type": "If",
              "expression": {
                "equals": [
                  "@items('ForEachMessage')?['body']?['status']",
                  "invalid"
                ]
              },
              "actions": {
                "DeadLetterMessage": {
                  "type": "ServiceBusDeadLetterMessage",
                  "inputs": {
                    "queueName": "my-queue",
                    "lockToken": "@items('ForEachMessage')?['lockToken']",
                    "deadLetterReason": "BusinessValidationFailed",
                    "deadLetterErrorDescription": "Message failed business rule validation",
                    "connection": {
                      "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
                    }
                  }
                }
              },
              "else": {
                "actions": {
                  "AbandonMessage": {
                    "type": "ServiceBusAbandonMessage",
                    "inputs": {
                      "queueName": "my-queue",
                      "lockToken": "@items('ForEachMessage')?['lockToken']",
                      "connection": {
                        "name": "@parameters('$connections')['servicebusConnection']['connectionId']"
                      }
                    }
                  }
                }
              }
            }
          }
        },
        "runAfter": {
          "ReceiveBatchMessages": ["Succeeded"]
        }
      }
    },
    "triggers": {
      "Recurrence": {
        "type": "Recurrence",
        "recurrence": {
          "frequency": "Day",
          "interval": 1
        }
      }
    },
    "parameters": {
      "$connections": {
        "defaultValue": {},
        "type": "Object"
      }
    }
  }
}
```

---

### 🧠 Logic Summary

| Condition | Action |
|----------|--------|
| `status == "valid"` | ✅ Complete the message |
| `status == "invalid"` | ❌ Dead-letter the message |
| Anything else | 🔁 Abandon the message for retry |

---









