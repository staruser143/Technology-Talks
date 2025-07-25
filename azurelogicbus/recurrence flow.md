# Query
For recurrence daily workflow, my business condition is as below. The ASB queue would have filtered events from Kafka Topic. The events are only for those applications which are submitted by a broker/agent for a healthcare domain and in that scenario we have to get the brokers esignature. We have a max of 12 days , and reminder emails have to be sent to the agent after  3,7,10 and 12 days, if he/she has not provided the esignature, yet. After 12 days, the application status would be considered as "PENDING". How would I keep track of this in this recurrence workflow and send emails accordingly.


To implement your **daily recurrence workflow** with **reminder logic and status tracking** for broker e-signatures, we need to design a system that:

1. **Tracks the age of each application**.
2. **Sends reminder emails** at specific intervals (3, 7, 10, 12 days).
3. **Updates the application status to "PENDING"** after 12 days if no e-signature is received.

---

## 🧭 Key Design Considerations

### 1. **Message Structure in Service Bus**
Each message should contain:
- `applicationId`
- `brokerId`
- `submissionDate` (timestamp)
- `esignatureReceived` (boolean or timestamp)
- `status` (e.g., `SUBMITTED`, `SIGNED`, `PENDING`)

### 2. **Workflow Logic**
On each daily run:
- Read messages from the queue (batch).
- For each message:
  - Calculate **days since submission**.
  - If `esignatureReceived == false`:
    - Send reminder email at 3, 7, 10, or 12 days.
    - If `days >= 12`, update status to `"PENDING"` and complete the message.
  - If `esignatureReceived == true`, complete the message.

---

## 🧾 JSON Workflow Outline (Logic App Standard)

Here’s a simplified structure of how the workflow would look:

```json
{
  "actions": {
    "ReceiveBatchMessages": { ... },
    "ForEachMessage": {
      "type": "Foreach",
      "foreach": "@body('ReceiveBatchMessages')",
      "actions": {
        "CalculateDaysSinceSubmission": {
          "type": "InlineCode",
          "inputs": {
            "language": "JavaScript",
            "code": "return Math.floor((Date.now() - new Date(item.body.submissionDate)) / (1000 * 60 * 60 * 24));"
          }
        },
        "CheckEsignature": {
          "type": "If",
          "expression": {
            "equals": ["@items('ForEachMessage')?['body']?['esignatureReceived']", false]
          },
          "actions": {
            "ReminderLogic": {
              "type": "Switch",
              "expression": "@outputs('CalculateDaysSinceSubmission')",
              "cases": {
                "3": { "actions": { "SendReminderEmail": { ... } } },
                "7": { "actions": { "SendReminderEmail": { ... } } },
                "10": { "actions": { "SendReminderEmail": { ... } } },
                "12": {
                  "actions": {
                    "SendReminderEmail": { ... },
                    "UpdateStatusToPending": { ... },
                    "CompleteMessage": { ... }
                  }
                }
              },
              "default": {
                "actions": {
                  "AbandonMessage": { ... }
                }
              }
            }
          },
          "else": {
            "actions": {
              "CompleteMessage": { ... }
            }
          }
        }
      }
    }
  }
}
```

---

## 📧 Email Action

Use **Send Email (Outlook or SMTP)** action:
- To: `brokerEmail`
- Subject: `"Reminder: Please e-sign application #{{applicationId}}"`
- Body: Include days since submission and a link to sign.

---

## 🧠 Additional Enhancements

- **Store state externally** (e.g., Azure Table Storage or Cosmos DB) if messages don’t persist long enough in the queue.
- **Use Logic App variables** to track `daysSinceSubmission`.
- **Add logging** for audit and diagnostics.

---

# Query
To implement your **daily recurrence workflow** with **reminder logic and status tracking** for broker e-signatures, we need to design a system that:

1. **Tracks the age of each application**.
2. **Sends reminder emails** at specific intervals (3, 7, 10, 12 days).
3. **Updates the application status to "PENDING"** after 12 days if no e-signature is received.

---

## 🧭 Key Design Considerations

### 1. **Message Structure in Service Bus**
Each message should contain:
- `applicationId`
- `brokerId`
- `submissionDate` (timestamp)
- `esignatureReceived` (boolean or timestamp)
- `status` (e.g., `SUBMITTED`, `SIGNED`, `PENDING`)

### 2. **Workflow Logic**
On each daily run:
- Read messages from the queue (batch).
- For each message:
  - Calculate **days since submission**.
  - If `esignatureReceived == false`:
    - Send reminder email at 3, 7, 10, or 12 days.
    - If `days >= 12`, update status to `"PENDING"` and complete the message.
  - If `esignatureReceived == true`, complete the message.

---

## 🧾 JSON Workflow Outline (Logic App Standard)

Here’s a simplified structure of how the workflow would look:

```json
{
  "actions": {
    "ReceiveBatchMessages": { ... },
    "ForEachMessage": {
      "type": "Foreach",
      "foreach": "@body('ReceiveBatchMessages')",
      "actions": {
        "CalculateDaysSinceSubmission": {
          "type": "InlineCode",
          "inputs": {
            "language": "JavaScript",
            "code": "return Math.floor((Date.now() - new Date(item.body.submissionDate)) / (1000 * 60 * 60 * 24));"
          }
        },
        "CheckEsignature": {
          "type": "If",
          "expression": {
            "equals": ["@items('ForEachMessage')?['body']?['esignatureReceived']", false]
          },
          "actions": {
            "ReminderLogic": {
              "type": "Switch",
              "expression": "@outputs('CalculateDaysSinceSubmission')",
              "cases": {
                "3": { "actions": { "SendReminderEmail": { ... } } },
                "7": { "actions": { "SendReminderEmail": { ... } } },
                "10": { "actions": { "SendReminderEmail": { ... } } },
                "12": {
                  "actions": {
                    "SendReminderEmail": { ... },
                    "UpdateStatusToPending": { ... },
                    "CompleteMessage": { ... }
                  }
                }
              },
              "default": {
                "actions": {
                  "AbandonMessage": { ... }
                }
              }
            }
          },
          "else": {
            "actions": {
              "CompleteMessage": { ... }
            }
          }
        }
      }
    }
  }
}
```

---

## 📧 Email Action

Use **Send Email (Outlook or SMTP)** action:
- To: `brokerEmail`
- Subject: `"Reminder: Please e-sign application #{{applicationId}}"`
- Body: Include days since submission and a link to sign.

---

## 🧠 Additional Enhancements

- **Store state externally** (e.g., Azure Table Storage or Cosmos DB) if messages don’t persist long enough in the queue.
- **Use Logic App variables** to track `daysSinceSubmission`.
- **Add logging** for audit and diagnostics.

---

# Query

Generate complete JSON with email actions.

Here’s a **complete Logic App Standard workflow JSON** that:

- Runs **daily**.
- Reads **batched messages** from an Azure Service Bus queue.
- Checks if the **broker has submitted an e-signature**.
- Sends **reminder emails** on days 3, 7, 10, and 12.
- Updates the application status to **"PENDING"** after 12 days.
- Completes or abandons the message accordingly.

---

### 🧾 Full JSON Workflow with Email and Status Logic

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
          "CalculateDaysSinceSubmission": {
            "type": "InlineCode",
            "inputs": {
              "language": "JavaScript",
              "code": "return Math.floor((Date.now() - new Date(item.body.submissionDate)) / (1000 * 60 * 60 * 24));"
            }
          },
          "CheckEsignature": {
            "type": "If",
            "expression": {
              "equals": ["@items('ForEachMessage')?['body']?['esignatureReceived']", false]
            },
            "actions": {
              "ReminderSwitch": {
                "type": "Switch",
                "expression": "@outputs('CalculateDaysSinceSubmission')",
                "cases": {
                  "3": {
                    "actions": {
                      "SendReminderEmail3": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Reminder: E-signature required for application",
                          "body": "Please e-sign application @items('ForEachMessage')?['body']?['applicationId'] submitted 3 days ago."
                        }
                      }
                    }
                  },
                  "7": {
                    "actions": {
                      "SendReminderEmail7": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Reminder: E-signature required for application",
                          "body": "Please e-sign application @items('ForEachMessage')?['body']?['applicationId'] submitted 7 days ago."
                        }
                      }
                    }
                  },
                  "10": {
                    "actions": {
                      "SendReminderEmail10": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Reminder: E-signature required for application",
                          "body": "Please e-sign application @items('ForEachMessage')?['body']?['applicationId'] submitted 10 days ago."
                        }
                      }
                    }
                  },
                  "12": {
                    "actions": {
                      "SendReminderEmail12": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Final Reminder: E-signature required",
                          "body": "This is your final reminder to e-sign application @items('ForEachMessage')?['body']?['applicationId']."
                        }
                      },
                      "UpdateStatusToPending": {
                        "type": "InlineCode",
                        "inputs": {
                          "language": "JavaScript",
                          "code": "item.body.status = 'PENDING'; return item.body;"
                        }
                      },
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
                    }
                  }
                },
                "default": {
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
            "else": {
              "actions": {
                "CompleteMessageSigned": {
                  "type": "ServiceBusCompleteMessage",
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

### 🧠 Notes

- You can replace `SendEmail` with your actual email connector (e.g., Outlook, SendGrid).
- `InlineCode` is used for calculating days and updating status. You can replace this with Azure Functions or built-in expressions if preferred.
- Ensure your Service Bus messages include `submissionDate`, `brokerEmail`, `applicationId`, and `esignatureReceived`.

---

# Query
Does the logic app workflow store any state for this scenario

Great question! By default, **Azure Logic Apps do not store state across runs** unless you explicitly design them to do so. In your scenario—where you need to track the age of each application and send reminders at specific intervals—**state management is essential**.

---

## 🧠 Why State Is Needed

You need to remember:
- When each application was submitted.
- Whether reminders have already been sent.
- Whether the e-signature has been received.
- Whether the application has been marked as `"PENDING"`.

---

## 🛠️ Options for State Management

### 1. **Use Azure Storage (Recommended)**
Store application metadata in:
- **Azure Table Storage** or
- **Azure Cosmos DB**

Each record would include:
```json
{
  "applicationId": "12345",
  "brokerId": "B001",
  "submissionDate": "2025-07-13T10:00:00Z",
  "esignatureReceived": false,
  "remindersSent": [3, 7],
  "status": "SUBMITTED"
}
```

Your Logic App would:
- Query this store daily.
- Update `remindersSent` and `status` fields.
- Send reminders only if the day is in `[3, 7, 10, 12]` and not already sent.

---

### 2. **Use Durable Functions (Advanced)**
If you need **workflow orchestration with built-in state**, consider using **Azure Durable Functions**. They can:
- Maintain state across invocations.
- Handle timers and reminders natively.
- Scale efficiently.

---

### 3. **Use SQL Database**
If you already have a relational database, you can store application tracking data there and query/update it from Logic Apps using SQL connectors.

---

## 🔄 Workflow Update Needed

To support state:
- Add actions to **read from** and **write to** your chosen data store.
- Use conditions to check if a reminder has already been sent.
- Update the record after sending a reminder or changing status.

---

# Query
Since we are compute the days since submission inline and then based on that doing email action, why would we need to track them separately in remainderSent fields

You're absolutely right to question the need for tracking `remindersSent` separately when you're already computing `daysSinceSubmission` inline. Let’s clarify when and why you **might** want to track reminders explicitly—and when you **don’t need to**.

---

## ✅ Why You *Don’t Need* `remindersSent` in Your Current Design

In your current Logic App workflow:
- You **calculate the age of the application** each day.
- You **send reminders only on specific days** (3, 7, 10, 12).
- The workflow runs **once per day**, so it naturally aligns with the reminder schedule.

This means:
- You **don’t need to remember** which reminders were sent.
- The logic is **stateless**, relying only on `submissionDate` and `esignatureReceived`.

This is **simple and efficient**, especially if:
- Messages stay in the queue until completed.
- You don’t need to audit or report on which reminders were sent.

---

## 🧠 When You *Would Need* `remindersSent`

You’d want to track reminders explicitly if:
1. **You need auditability**: e.g., to report which reminders were sent and when.
2. **You want to avoid duplicate reminders** in case the workflow runs more than once per day or is retried.
3. **You store messages externally** (e.g., in a database or storage) and revisit them across multiple workflows.
4. **You want to support custom reminder logic**, like skipping reminders based on user actions.

In those cases, storing a `remindersSent` array or flags would help you:
- Avoid sending the same reminder twice.
- Maintain a history of communication.
- Enable richer business logic.

---

## 🧩 Summary

| Scenario | Need `remindersSent`? |
|----------|------------------------|
| Stateless daily workflow | ❌ No |
| Audit/logging required | ✅ Yes |
| Multiple workflows or retries | ✅ Yes |
| External state management | ✅ Yes |

---

# Query

Great! Since you want to track **reminders and application status** in a **MongoDB Atlas collection**, your Logic App workflow will need to:

---

## 🧱 Updated Architecture Overview

1. **Trigger**: Daily recurrence.
2. **Read messages** from Azure Service Bus (batch).
3. **For each message**:
   - Query MongoDB Atlas to get the application record.
   - Calculate `daysSinceSubmission`.
   - If `esignatureReceived == false`:
     - Check if a reminder for that day has already been sent.
     - If not, send email and update MongoDB with the new reminder day.
     - If `days >= 12`, update status to `"PENDING"`.
   - If `esignatureReceived == true`, mark message as complete.

---

## 🧩 MongoDB Document Schema (Example)

```json
{
  "applicationId": "APP12345",
  "brokerId": "BRK001",
  "submissionDate": "2025-07-13T10:00:00Z",
  "esignatureReceived": false,
  "remindersSent": [3, 7],
  "status": "SUBMITTED",
  "brokerEmail": "agent@example.com"
}
```

---

## 🔌 Integration with MongoDB Atlas

To connect Logic Apps to MongoDB Atlas:
- Use the **HTTP action** to call a **MongoDB Data API** endpoint.
- Or use **Azure Functions** as a proxy to interact with MongoDB securely.

---

## ✅ Next Steps

Would you like me to:
1. Generate a **Logic App JSON** that includes MongoDB Data API calls?
2. Help you set up the **MongoDB Data API** for secure access?
3. Provide a **sample Azure Function** that handles MongoDB updates?


# Query

Here’s a **Logic App Standard workflow JSON** that integrates with **MongoDB Atlas** using the **MongoDB Data API** to:

- Read messages from Azure Service Bus.
- Calculate days since submission.
- Query MongoDB for the application record.
- Send reminders if needed.
- Update the MongoDB document with reminder history and status.

---

### 🧾 Assumptions

- You have a **MongoDB Data API endpoint**.
- You have a **MongoDB Atlas collection** named `applications`.
- You have an **API key** and **Data Source name**.
- You use **HTTP actions** to interact with MongoDB.

---

### 🧾 Sample JSON Workflow (Simplified)

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
        }
      },
      "ForEachMessage": {
        "type": "Foreach",
        "foreach": "@body('ReceiveBatchMessages')",
        "actions": {
          "CalculateDaysSinceSubmission": {
            "type": "InlineCode",
            "inputs": {
              "language": "JavaScript",
              "code": "return Math.floor((Date.now() - new Date(item.body.submissionDate)) / (1000 * 60 * 60 * 24));"
            }
          },
          "QueryMongoDB": {
            "type": "Http",
            "inputs": {
              "method": "POST",
              "uri": "https://data.mongodb-api.com/app/<app-id>/endpoint/data/v1/action/findOne",
              "headers": {
                "Content-Type": "application/json",
                "api-key": "<your-api-key>"
              },
              "body": {
                "dataSource": "<your-data-source>",
                "database": "<your-database>",
                "collection": "applications",
                "filter": {
                  "applicationId": "@items('ForEachMessage')?['body']?['applicationId']"
                }
              }
            }
          },
          "ReminderLogic": {
            "type": "If",
            "expression": {
              "and": [
                {
                  "equals": ["@body('QueryMongoDB')?['document']?['esignatureReceived']", false]
                },
                {
                  "not": {
                    "contains": [
                      "@body('QueryMongoDB')?['document']?['remindersSent']",
                      "@outputs('CalculateDaysSinceSubmission')"
                    ]
                  }
                }
              ]
            },
            "actions": {
              "SendReminderEmail": {
                "type": "SendEmail",
                "inputs": {
                  "to": "@body('QueryMongoDB')?['document']?['brokerEmail']",
                  "subject": "Reminder: E-signature required",
                  "body": "Please e-sign application @body('QueryMongoDB')?['document']?['applicationId'] submitted @outputs('CalculateDaysSinceSubmission') days ago."
                }
              },
              "UpdateMongoDB": {
                "type": "Http",
                "inputs": {
                  "method": "POST",
                  "uri": "https://data.mongodb-api.com/app/<app-id>/endpoint/data/v1/action/updateOne",
                  "headers": {
                    "Content-Type": "application/json",
                    "api-key": "<your-api-key>"
                  },
                  "body": {
                    "dataSource": "<your-data-source>",
                    "database": "<your-database>",
                    "collection": "applications",
                    "filter": {
                      "applicationId": "@items('ForEachMessage')?['body']?['applicationId']"
                    },
                    "update": {
                      "$push": {
                        "remindersSent": "@outputs('CalculateDaysSinceSubmission')"
                      },
                      "$set": {
                        "status": {
                          "$cond": {
                            "if": {
                              "$gte": ["@outputs('CalculateDaysSinceSubmission')", 12]
                            },
                            "then": "PENDING",
                            "else": "@body('QueryMongoDB')?['document']?['status']"
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          },
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

### 🔐 Replace Placeholders

- `<app-id>`: Your MongoDB Data API App ID.
- `<your-api-key>`: Your MongoDB Data API key.
- `<your-data-source>`: Typically `"Cluster0"` or your cluster name.
- `<your-database>`: Your MongoDB database name.

---

# Query

Yes, you can absolutely use a **custom service** to query and update your MongoDB Atlas collection instead of using the MongoDB Data API directly. This approach offers several advantages:

---

## ✅ Benefits of Using a Custom Service

1. **Security**: You can encapsulate your MongoDB credentials and logic, avoiding direct exposure in Logic Apps.
2. **Flexibility**: You can implement complex business logic, validation, and error handling.
3. **Reusability**: The service can be reused across multiple workflows or applications.
4. **Audit & Logging**: You can log all interactions for compliance and debugging.

---

## 🧱 Architecture Overview

### Logic App Workflow:
- Trigger: Daily recurrence.
- Read messages from Azure Service Bus.
- For each message:
  - Call your custom service (e.g., REST API hosted in Azure App Service or Azure Function).
  - Service handles:
    - Querying MongoDB.
    - Calculating days since submission.
    - Sending reminders.
    - Updating status and reminder history.
  - Logic App completes or abandons the message based on the service response.

---

## 🔌 Integration in Logic App (HTTP Action)

Here’s a sample HTTP action to call your custom service:

```json
{
  "type": "Http",
  "inputs": {
    "method": "POST",
    "uri": "https://your-custom-service/api/processApplication",
    "headers": {
      "Content-Type": "application/json",
      "Authorization": "Bearer <your-token>"
    },
    "body": {
      "applicationId": "@items('ForEachMessage')?['body']?['applicationId']",
      "submissionDate": "@items('ForEachMessage')?['body']?['submissionDate']",
      "esignatureReceived": "@items('ForEachMessage')?['body']?['esignatureReceived']",
      "brokerEmail": "@items('ForEachMessage')?['body']?['brokerEmail']"
    }
  }
}
```

---

## 🧠 What Your Custom Service Should Do

- Connect to MongoDB Atlas.
- Retrieve the application document.
- Calculate `daysSinceSubmission`.
- Check if a reminder for that day has already been sent.
- Send email if needed.
- Update `remindersSent` and `status` fields.
- Return a response indicating whether the Logic App should complete or abandon the message.

---

# Query

Here’s a **sample Node.js implementation** of a custom service that:

- Connects to **MongoDB Atlas**.
- Retrieves an application document.
- Calculates **days since submission**.
- Sends a **reminder email** if needed.
- Updates the document with `remindersSent` and `status`.

---

## 🧱 Prerequisites

- MongoDB Atlas connection string.
- Email service (e.g., SendGrid, Nodemailer).
- Express.js for API routing.

---

## 📦 Dependencies

Install these via npm:

```bash
npm install express mongodb nodemailer dayjs
```

---

## 🧾 Sample Code (`server.js`)

```javascript
const express = require('express');
const { MongoClient } = require('mongodb');
const nodemailer = require('nodemailer');
const dayjs = require('dayjs');

const app = express();
app.use(express.json());

const mongoUri = 'your-mongodb-connection-string';
const client = new MongoClient(mongoUri);
const dbName = 'your-db-name';
const collectionName = 'applications';

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: 'your-email@gmail.com',
    pass: 'your-app-password'
  }
});

app.post('/api/processApplication', async (req, res) => {
  const { applicationId, submissionDate, esignatureReceived, brokerEmail } = req.body;

  try {
    await client.connect();
    const db = client.db(dbName);
    const collection = db.collection(collectionName);

    const appDoc = await collection.findOne({ applicationId });
    if (!appDoc) return res.status(404).send('Application not found');

    const daysSinceSubmission = dayjs().diff(dayjs(appDoc.submissionDate), 'day');
    const remindersSent = appDoc.remindersSent || [];

    const reminderDays = [3, 7, 10, 12];
    const shouldSendReminder = reminderDays.includes(daysSinceSubmission) && !remindersSent.includes(daysSinceSubmission);

    if (esignatureReceived) {
      return res.send({ action: 'complete' });
    }

    if (shouldSendReminder) {
      await transporter.sendMail({
        from: 'your-email@gmail.com',
        to: brokerEmail,
        subject: `Reminder: E-signature required for application ${applicationId}`,
        text: `Please e-sign the application submitted ${daysSinceSubmission} days ago.`
      });

      const update = {
        $push: { remindersSent: daysSinceSubmission }
      };

      if (daysSinceSubmission >= 12) {
        update.$set = { status: 'PENDING' };
      }

      await collection.updateOne({ applicationId }, update);
      return res.send({ action: 'reminderSent' });
    }

    if (daysSinceSubmission >= 12 && appDoc.status !== 'PENDING') {
      await collection.updateOne({ applicationId }, { $set: { status: 'PENDING' } });
      return res.send({ action: 'statusUpdated' });
    }

    res.send({ action: 'noAction' });
  } catch (error) {
    console.error(error);
    res.status(500).send('Internal Server Error');
  } finally {
    await client.close();
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
```

---

## 🧠 What This Service Does

- Accepts application data from Logic App.
- Checks MongoDB for the application.
- Calculates how many days have passed.
- Sends reminder if needed.
- Updates MongoDB with reminder history and status.
- Returns a response to guide Logic App (e.g., complete or abandon message).

---

# Query

Here’s a **sample Node.js implementation** of a custom service that:

- Connects to **MongoDB Atlas**.
- Retrieves an application document.
- Calculates **days since submission**.
- Sends a **reminder email** if needed.
- Updates the document with `remindersSent` and `status`.

---

## 🧱 Prerequisites

- MongoDB Atlas connection string.
- Email service (e.g., SendGrid, Nodemailer).
- Express.js for API routing.

---

## 📦 Dependencies

Install these via npm:

```bash
npm install express mongodb nodemailer dayjs
```

---

## 🧾 Sample Code (`server.js`)

```javascript
const express = require('express');
const { MongoClient } = require('mongodb');
const nodemailer = require('nodemailer');
const dayjs = require('dayjs');

const app = express();
app.use(express.json());

const mongoUri = 'your-mongodb-connection-string';
const client = new MongoClient(mongoUri);
const dbName = 'your-db-name';
const collectionName = 'applications';

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: 'your-email@gmail.com',
    pass: 'your-app-password'
  }
});

app.post('/api/processApplication', async (req, res) => {
  const { applicationId, submissionDate, esignatureReceived, brokerEmail } = req.body;

  try {
    await client.connect();
    const db = client.db(dbName);
    const collection = db.collection(collectionName);

    const appDoc = await collection.findOne({ applicationId });
    if (!appDoc) return res.status(404).send('Application not found');

    const daysSinceSubmission = dayjs().diff(dayjs(appDoc.submissionDate), 'day');
    const remindersSent = appDoc.remindersSent || [];

    const reminderDays = [3, 7, 10, 12];
    const shouldSendReminder = reminderDays.includes(daysSinceSubmission) && !remindersSent.includes(daysSinceSubmission);

    if (esignatureReceived) {
      return res.send({ action: 'complete' });
    }

    if (shouldSendReminder) {
      await transporter.sendMail({
        from: 'your-email@gmail.com',
        to: brokerEmail,
        subject: `Reminder: E-signature required for application ${applicationId}`,
        text: `Please e-sign the application submitted ${daysSinceSubmission} days ago.`
      });

      const update = {
        $push: { remindersSent: daysSinceSubmission }
      };

      if (daysSinceSubmission >= 12) {
        update.$set = { status: 'PENDING' };
      }

      await collection.updateOne({ applicationId }, update);
      return res.send({ action: 'reminderSent' });
    }

    if (daysSinceSubmission >= 12 && appDoc.status !== 'PENDING') {
      await collection.updateOne({ applicationId }, { $set: { status: 'PENDING' } });
      return res.send({ action: 'statusUpdated' });
    }

    res.send({ action: 'noAction' });
  } catch (error) {
    console.error(error);
    res.status(500).send('Internal Server Error');
  } finally {
    await client.close();
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
```

---

## 🧠 What This Service Does

- Accepts application data from Logic App.
- Checks MongoDB for the application.
- Calculates how many days have passed.
- Sends reminder if needed.
- Updates MongoDB with reminder history and status.
- Returns a response to guide Logic App (e.g., complete or abandon message).

---


# Query

Here’s the **updated Logic App Standard JSON** that:

- Runs daily.
- Reads messages from Azure Service Bus in batch.
- Calculates `daysSinceSubmission` inline.
- Sends reminder emails on days 3, 7, 10, and 12.
- Calls a **custom service** to update MongoDB Atlas with:
  - `reminderDay`
  - `status = PENDING` if `days >= 12`
- Completes the message if processed.

---

### 🧾 Updated Logic App JSON (Simplified for Clarity)

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
        }
      },
      "ForEachMessage": {
        "type": "Foreach",
        "foreach": "@body('ReceiveBatchMessages')",
        "actions": {
          "CalculateDaysSinceSubmission": {
            "type": "InlineCode",
            "inputs": {
              "language": "JavaScript",
              "code": "return Math.floor((Date.now() - new Date(item.body.submissionDate)) / (1000 * 60 * 60 * 24));"
            }
          },
          "CheckEsignature": {
            "type": "If",
            "expression": {
              "equals": ["@items('ForEachMessage')?['body']?['esignatureReceived']", false]
            },
            "actions": {
              "ReminderSwitch": {
                "type": "Switch",
                "expression": "@outputs('CalculateDaysSinceSubmission')",
                "cases": {
                  "3": {
                    "actions": {
                      "SendReminderEmail3": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Reminder: E-signature required",
                          "body": "Please e-sign application @items('ForEachMessage')?['body']?['applicationId'] submitted 3 days ago."
                        }
                      },
                      "UpdateMongoDB3": {
                        "type": "Http",
                        "inputs": {
                          "method": "POST",
                          "uri": "https://your-service/api/updateApplication",
                          "headers": {
                            "Content-Type": "application/json"
                          },
                          "body": {
                            "applicationId": "@items('ForEachMessage')?['body']?['applicationId']",
                            "reminderDay": 3,
                            "setStatusToPending": false
                          }
                        }
                      }
                    }
                  },
                  "7": {
                    "actions": {
                      "SendReminderEmail7": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Reminder: E-signature required",
                          "body": "Please e-sign application @items('ForEachMessage')?['body']?['applicationId'] submitted 7 days ago."
                        }
                      },
                      "UpdateMongoDB7": {
                        "type": "Http",
                        "inputs": {
                          "method": "POST",
                          "uri": "https://your-service/api/updateApplication",
                          "headers": {
                            "Content-Type": "application/json"
                          },
                          "body": {
                            "applicationId": "@items('ForEachMessage')?['body']?['applicationId']",
                            "reminderDay": 7,
                            "setStatusToPending": false
                          }
                        }
                      }
                    }
                  },
                  "10": {
                    "actions": {
                      "SendReminderEmail10": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Reminder: E-signature required",
                          "body": "Please e-sign application @items('ForEachMessage')?['body']?['applicationId'] submitted 10 days ago."
                        }
                      },
                      "UpdateMongoDB10": {
                        "type": "Http",
                        "inputs": {
                          "method": "POST",
                          "uri": "https://your-service/api/updateApplication",
                          "headers": {
                            "Content-Type": "application/json"
                          },
                          "body": {
                            "applicationId": "@items('ForEachMessage')?['body']?['applicationId']",
                            "reminderDay": 10,
                            "setStatusToPending": false
                          }
                        }
                      }
                    }
                  },
                  "12": {
                    "actions": {
                      "SendReminderEmail12": {
                        "type": "SendEmail",
                        "inputs": {
                          "to": "@items('ForEachMessage')?['body']?['brokerEmail']",
                          "subject": "Final Reminder: E-signature required",
                          "body": "This is your final reminder to e-sign application @items('ForEachMessage')?['body']?['applicationId']."
                        }
                      },
                      "UpdateMongoDB12": {
                        "type": "Http",
                        "inputs": {
                          "method": "POST",
                          "uri": "https://your-service/api/updateApplication",
                          "headers": {
                            "Content-Type": "application/json"
                          },
                          "body": {
                            "applicationId": "@items('ForEachMessage')?['body']?['applicationId']",
                            "reminderDay": 12,
                            "setStatusToPending": true
                          }
                        }
                      }
                    }
                  }
                },
                "default": {
                  "actions": {}
                }
              }
            },
            "else": {
              "actions": {}
            }
          },
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

### 🔧 What You Need to Customize

- Replace `https://your-service/api/updateApplication` with your actual service endpoint.
- Replace `SendEmail` with your actual email connector (e.g., Outlook, SendGrid).
- Ensure your service handles the `reminderDay` and `setStatusToPending` flags correctly.

---




