Great choice! **MongoDB Atlas** is a powerful, flexible, and scalable database that integrates well with **Azure Logic Apps** via HTTP or the **MongoDB Atlas Connector for Logic Apps (preview)** or even through **custom APIs**.

Below is a **complete template** for your **agent signature workflow**, using:

- ✅ **Azure Logic App (Standard)**
- ✅ **MongoDB Atlas** (to store business state: reminders sent, signature status)
- ✅ **Azure Service Bus** (to trigger the workflow)
- ✅ **Email reminders** with tracking
- ✅ **Callback endpoint** to resume on signature

---

## 🎯 Use Case Recap
- Agent submits enrollment → message sent to Service Bus
- Logic App starts, sends signature request
- Sends reminders on days 3, 5, 7, 9, 11 (tracked in MongoDB)
- Timeout after 12 days → mark as `PENDING_APPROVAL`
- All state (reminders, status) stored in **MongoDB Atlas**

---

## 🛠️ Step 1: MongoDB Atlas – Data Model

### Collection: `signature_tracking`

```json
{
  "applicationId": "APP-123",
  "agentEmail": "agent@broker.com",
  "createdAt": "2025-04-01T08:00:00Z",
  "signatureReceived": false,
  "signatureReceivedAt": null,
  "status": "WAITING_FOR_SIGNATURE",
  "remindersSent": [
    {
      "day": 3,
      "sentAt": "2025-04-04T09:00:00Z",
      "subject": "Reminder: Please sign application APP-123"
    },
    {
      "day": 5,
      "sentAt": "2025-04-06T09:00:00Z",
      "subject": "Urgent: Signature Required"
    }
  ]
}
```

> 🔐 Use `applicationId` as a unique index to prevent duplicates.

---

## 🌐 Step 2: Enable MongoDB Atlas API Access

MongoDB Atlas provides a **Data API** (REST) that allows external apps (like Logic Apps) to query/update documents.

### Enable Data API:
1. Go to [MongoDB Atlas](https://cloud.mongodb.com)
2. Navigate: **Atlas UI → Data API**
3. Enable Data API for your cluster
4. Create an **API Key** (with read/write access)
5. Note:
   - **Data API Endpoint**: `https://data.mongodb-api.com/app/data-xxxxx/endpoint/data/v1`
   - **App ID**: `your-app-id`
   - **API Key**: `your-api-key`

---

## 🧩 Step 3: Logic App Workflow (Standard) – Template

> Use **Visual Studio Code + Azure Logic Apps extension** to build this.

### Trigger: Service Bus Queue

```json
{
  "type": "Trigger",
  "inputs": {
    "host": {
      "connection": {
        "name": "@parameters('$connections')['servicebus']['connectionId']"
      }
    },
    "method": "get",
    "path": "/onMessage/dequeuenext"
  },
  "operation": "OnMessage",
  "runtimeConfiguration": {
    "concurrency": {
      "requests": 1
    }
  }
}
```

> Message body:
```json
{
  "applicationId": "APP-123",
  "agentEmail": "agent@broker.com",
  "applicantName": "John Doe"
}
```

---

### Action 1: Insert Tracking Record in MongoDB (If Not Exists)

Use **HTTP + Data API** to upsert the initial record.

```json
"Insert_Tracking_Record": {
  "type": "Http",
  "inputs": {
    "method": "POST",
    "uri": "https://data.mongodb-api.com/app/data-xxxxx/endpoint/data/v1/action/findOneAndUpdate",
    "headers": {
      "Content-Type": "application/json",
      "Access-Control-Request-Headers": "*",
      "api-key": "YOUR_ATLAS_API_KEY"
    },
    "body": {
      "collection": "signature_tracking",
      "database": "onboarding_db",
      "dataSource": "Cluster0",
      "filter": { "applicationId": "@{triggerBody()?['applicationId']}" },
      "update": {
        "$setOnInsert": {
          "applicationId": "@{triggerBody()?['applicationId']}",
          "agentEmail": "@{triggerBody()?['agentEmail']}",
          "createdAt": "@utcNow()",
          "signatureReceived": false,
          "status": "WAITING_FOR_SIGNATURE",
          "remindersSent": []
        }
      },
      "upsert": true
    }
  }
}
```

> This ensures only one record exists per application.

---

### Action 2: Send Signature Request Email

Use **Office 365 Outlook** or **SendGrid**.

Include a callback link:
```
https://prod-xx.westus.logic.azure.com/workflows/.../triggers/manual/paths/invoke/callbacks/sign?applicationId=APP-123&api-version=...
```

---

### Action 3: Wait for Agent Signature (Callback)

Use **"Wait for HTTP request"** action.

```json
"Wait_for_Signature": {
  "type": "Request",
  "kind": "Callback",
  "inputs": {
    "method": "POST",
    "relativePath": "/callbacks/sign"
  }
}
```

When agent clicks the link, this resumes the workflow.

---

### Action 4: Send Reminder Emails (With MongoDB Tracking)

Use a **foreach** loop over `[3,5,7,9,11]` or individual steps.

#### Example: Day 3 Reminder

```json
"Check_and_Send_Day_3_Reminder": {
  "type": "If",
  "expression": "not(contains(body('Get_Tracking_Record')?['document']?['remindersSent'], { 'day': 3 }))",
  "actions": {
    "Delay_Until_Day_3": {
      "type": "DelayUntil",
      "inputs": "@addDays(body('Insert_Tracking_Record')?['createdAt'], 3)"
    },
    "Send_Day_3_Email": {
      "type": "Http",
      "inputs": {
        "uri": "https://logicapp-send-email.azurewebsites.net/api/send",
        "method": "POST",
        "body": {
          "to": "@{triggerBody()?['agentEmail']}",
          "subject": "Reminder: Sign Application @{triggerBody()?['applicationId']}",
          "body": "Please sign by day 5..."
        }
      }
    },
    "Log_Day_3_Sent": {
      "type": "Http",
      "inputs": {
        "method": "PATCH",
        "uri": "https://data.mongodb-api.com/app/data-xxxxx/endpoint/data/v1/action/updateOne",
        "headers": {
          "Content-Type": "application/json",
          "api-key": "YOUR_ATLAS_API_KEY"
        },
        "body": {
          "collection": "signature_tracking",
          "database": "onboarding_db",
          "dataSource": "Cluster0",
          "filter": { "applicationId": "@{triggerBody()?['applicationId']}" },
          "update": {
            "$push": {
              "remindersSent": {
                "day": 3,
                "sentAt": "@utcNow()",
                "subject": "Reminder: Please sign application @{triggerBody()?['applicationId']}"
              }
            }
          }
        }
      }
    }
  }
}
```

> Repeat for days 5, 7, 9, 11 — or use a loop (more advanced).

---

### Action 5: Timeout After 12 Days

Wrap the `Wait_for_Signature` in a **Timeout Scope**:

```json
"Wait_with_Timeout": {
  "type": "Scope",
  "timeout": "P12D",
  "actions": {
    "Wait_for_Signature": { ... }
  },
  "onTimeout": {
    "Update_Status_to_Pending": {
      "type": "Http",
      "inputs": {
        "method": "PATCH",
        "uri": "https://data.mongodb-api.com/app/data-xxxxx/endpoint/data/v1/action/updateOne",
        "headers": {
          "Content-Type": "application/json",
          "api-key": "YOUR_ATLAS_API_KEY"
        },
        "body": {
          "collection": "signature_tracking",
          "database": "onboarding_db",
          "dataSource": "Cluster0",
          "filter": { "applicationId": "@{triggerBody()?['applicationId']}" },
          "update": {
            "$set": {
              "status": "PENDING_APPROVAL",
              "timeoutOccurred": true,
              "updatedAt": "@utcNow()"
            }
          }
        }
      }
    },
    "Notify_Admin": {
      "type": "Http",
      "inputs": {
        "uri": "https://webhook.office.com/...",
        "method": "POST",
        "body": "Application @{triggerBody()?['applicationId']} timed out waiting for agent signature."
      }
    }
  }
}
```

---

### Action 6: On Signature Received – Update MongoDB

After `Wait_for_Signature` resumes:

```json
"Update_On_Signature": {
  "type": "Http",
  "inputs": {
    "method": "PATCH",
    "uri": "https://data.mongodb-api.com/app/data-xxxxx/endpoint/data/v1/action/updateOne",
    "headers": {
      "Content-Type": "application/json",
      "api-key": "YOUR_ATLAS_API_KEY"
    },
    "body": {
      "collection": "signature_tracking",
      "database": "onboarding_db",
      "dataSource": "Cluster0",
      "filter": { "applicationId": "@{triggerBody()?['applicationId']}" },
      "update": {
        "$set": {
          "signatureReceived": true,
          "signatureReceivedAt": "@utcNow()",
          "status": "SIGNED",
          "updatedAt": "@utcNow()"
        }
      }
    }
  }
}
```

---

## 🔐 Security Tips

- Store **MongoDB API Key** in **Azure Key Vault**
- Reference it in Logic App as: `@{listSecrets('your-key-vault', 'mongodb-api-key')}`
- Use **VNet injection** or **IP whitelisting** in MongoDB Atlas
- Validate `applicationId` and signature token in callback

---

## ✅ Benefits of This Design

| Feature | How It’s Achieved |
|-------|-------------------|
| State persistence | MongoDB Atlas (external, auditable) |
| Long-running workflow | Logic Apps Standard (12-day wait) |
| Idempotent reminders | Check MongoDB before sending |
| Audit trail | Full history in MongoDB |
| Scalable | Both Logic Apps and Atlas scale independently |

---

## 📥 Want This as a Downloadable Project?

I can provide:
- Full **Logic App Bicep/ARM template**
- **VS Code project** with workflow.json
- **MongoDB Atlas Terraform module**
- **Postman collection** to test callbacks

Just let me know!

---

✅ **Final Summary**:  
Yes, **Logic Apps manages execution state**, but for **business-critical tracking (reminders, status)**, **externalize to MongoDB Atlas** — it gives you control, auditability, and integration power.

Let me know if you'd like help connecting this to your Kafka → Service Bus pipeline!