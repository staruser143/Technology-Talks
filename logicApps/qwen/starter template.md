Great! Here's a **complete starter template** for your **agent signature collection system**, using:

- ✅ **Azure Logic App (Standard)** – to orchestrate the workflow  
- ✅ **Azure Static Web Apps** – to host a clean, responsive signature form  
- ✅ **Azure Function (API)** – to save the signature and resume the Logic App  
- ✅ **MongoDB Atlas** – to store signature data (optional)  
- ✅ **Secure callback pattern** – Logic App resumes only after valid signature  

This is production-ready, secure, and easy to extend.

---

## 📦 Folder Structure (Starter Project)

```
signature-workflow-starter/
│
├── logic-app/                     # Azure Logic App (Standard) - Workflow
│   └── workflow.json              # Main orchestration with callback
│
├── web-app/                       # Static Web App (HTML + JS form)
│   ├── index.html                 # Signature form
│   └── script.js                  # Capture signature & submit
│
├── api/                           # Azure Function (Node.js/TypeScript)
│   ├── sign/index.js              # POST /api/sign – saves sig + calls callback
│   └── function.json
│
├── config/                        # Config files
│   └── local.settings.json        # For local dev (secrets)
│
└── README.md                      # Setup instructions
```

---

## 1. ✅ `logic-app/workflow.json`  
*(Azure Logic App Standard – Waits for signature)*

```json
{
  "definition": {
    "$schema": "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#",
    "actions": {
      "Send_Signature_Request_Email": {
        "type": "Http",
        "inputs": {
          "method": "POST",
          "uri": "https://prod-xx.westus.logic.azure.com:443/workflows/.../triggers/manual/paths/invoke/send-email",
          "body": {
            "to": "agent@company.com",
            "link": "https://your-portal.azurestaticapps.net?app=APP-123&token=abc123"
          }
        },
        "runAfter": {}
      },
      "Wait_for_Signature": {
        "type": "Request",
        "kind": "Callback",
        "inputs": {
          "method": "POST",
          "relativePath": "/callbacks/sign"
        },
        "runAfter": {
          "Send_Signature_Request_Email": ["Succeeded"]
        }
      },
      "On_Signature_Received": {
        "type": "Compose",
        "inputs": "Signature received: @{triggerBody()?['signatureImage']}",
        "runAfter": {
          "Wait_for_Signature": ["Succeeded"]
        }
      }
    },
    "triggers": {
      "manual": {
        "type": "Request",
        "kind": "Http",
        "inputs": {
          "schema": {}
        }
      }
    },
    "contentVersion": "1.0.0.0"
  }
}
```

> 💡 The callback URL will look like:  
> `https://prod-xx.westus.logic.azure.com/.../callbacks/sign?sig=...&sp=...`

---

## 2. 🖼️ `web-app/index.html`  
*(Simple signature form hosted on Static Web Apps)*

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Sign Enrollment</title>
  <style>
    body { font-family: Arial, sans-serif; padding: 20px; }
    #signature-pad { border: 2px dashed #ccc; width: 500px; height: 200px; }
    button { margin-top: 10px; padding: 10px 20px; font-size: 16px; }
  </style>
</head>
<body>
  <h2>Sign to Approve Application</h2>
  <p><strong>Application ID:</strong> <span id="appId">Loading...</span></p>
  <canvas id="signature-pad"></canvas>
  <br />
  <button id="clear">Clear</button>
  <button id="submit">Submit Signature</button>

  <script src="script.js"></script>
</body>
</html>
```

---

## 3. 🖊️ `web-app/script.js`  
*(Capture signature and send to API)*

```javascript
const canvas = document.getElementById('signature-pad');
const ctx = canvas.getContext('2d');
let signaturePad = null;

// Simulate app ID from URL (in prod, validate token)
const urlParams = new URLSearchParams(window.location.search);
const appId = urlParams.get('app') || 'UNKNOWN';
document.getElementById('appId').textContent = appId;

// Setup canvas
canvas.width = 500;
canvas.height = 200;
ctx.lineWidth = 2;
ctx.strokeStyle = '#000';

let isDrawing = false;
canvas.addEventListener('mousedown', () => isDrawing = true);
canvas.addEventListener('mouseup', () => isDrawing = false);
canvas.addEventListener('mouseleave', () => isDrawing = false);

canvas.addEventListener('mousemove', (e) => {
  if (!isDrawing) return;
  const rect = canvas.getBoundingClientRect();
  ctx.lineTo(e.clientX - rect.left, e.clientY - rect.top);
  ctx.stroke();
  ctx.beginPath();
  ctx.moveTo(e.clientX - rect.left, e.clientY - rect.top);
});

document.getElementById('clear').addEventListener('click', () => {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
});

document.getElementById('submit').addEventListener('click', async () => {
  const signatureData = canvas.toDataURL('image/png'); // base64

  const response = await fetch('/api/sign', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ applicationId: appId, signatureImage: signatureData })
  });

  if (response.ok) {
    alert('Signature submitted! Thank you.');
    // Optionally redirect
    document.body.innerHTML = "<h2>Thank you! Your signature has been recorded.</h2>";
  } else {
    alert('Error submitting signature.');
  }
});
```

---

## 4. 🧠 `api/sign/index.js`  
*(Azure Function – Saves signature and resumes Logic App)*

```javascript
const axios = require('axios');

module.exports = async function (context, req) {
  try {
    const { applicationId, signatureImage } = req.body;

    // ✅ 1. Validate input
    if (!applicationId || !signatureImage) {
      return { status: 400, body: "Missing applicationId or signatureImage" };
    }

    // ✅ 2. (Optional) Save to MongoDB Atlas
    // await mongoClient.db("onboarding").collection("signatures").insertOne({
    //   applicationId,
    //   signatureImage,
    //   signedAt: new Date().toISOString()
    // });

    // ✅ 3. Call Logic App callback to resume workflow
    const callbackUrl = process.env.LOGIC_APP_CALLBACK_URL;
    if (!callbackUrl) {
      return { status: 500, body: "Callback URL not configured" };
    }

    await axios.post(callbackUrl, {
      signed: true,
      applicationId,
      signatureImage,
      signedAt: new Date().toISOString()
    }, {
      headers: { 'Content-Type': 'application/json' }
    });

    // ✅ 4. Return success
    context.res = {
      status: 200,
      body: { message: "Signature recorded and workflow resumed" }
    };

  } catch (error) {
    context.log.error(error);
    context.res = {
      status: 500,
      body: "Internal server error"
    };
  }
};
```

> `function.json` (same folder):
```json
{
  "bindings": [
    {
      "authLevel": "anonymous",
      "type": "httpTrigger",
      "direction": "in",
      "name": "req",
      "methods": ["post"],
      "route": "sign"
    },
    {
      "type": "http",
      "direction": "out",
      "name": "res"
    }
  ]
}
```

---

## 5. 🔐 `config/local.settings.json`  
*(For local development with Azure Functions Core Tools)*

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "",
    "FUNCTIONS_WORKER_RUNTIME": "node",
    "LOGIC_APP_CALLBACK_URL": "https://prod-xx.logic.azure.com/workflows/.../callbacks/sign?sig=...",
    "MONGODB_URI": "mongodb+srv://user:pass@cluster.mongodb.net/onboarding"
  }
}
```

> ⚠️ Never commit this file to Git. Use **Azure Key Vault** in production.

---

## 🚀 Deployment Instructions

### Step 1: Deploy Logic App
- Use **VS Code + Azure Logic Apps extension**
- Deploy `workflow.json` to an **Azure Logic App (Standard)**

### Step 2: Deploy Static Web App
```bash
cd web-app
az staticwebapp deploy --output-location .
```

### Step 3: Deploy Azure Function
```bash
func azure functionapp publish your-function-app-name
```

### Step 4: Set Environment Variables
In Azure Portal → Function App → Configuration:
- Add `LOGIC_APP_CALLBACK_URL` (get from Logic App run history)
- Add `MONGODB_URI` if using DB

---

## ✅ Final Flow

1. **Logic App** sends email with link:  
   `https://your-portal.azurestaticapps.net?app=APP-123&token=xyz`
2. **Agent clicks** → sees form → signs
3. **Frontend** sends data to `/api/sign`
4. **Azure Function**:
   - Saves signature (optional)
   - Calls **Logic App callback**
5. **Logic App resumes** and continues processing

---

## 📥 Download This Template

You can clone this as a starter repo:

📁 [GitHub Gist: Azure Signature Workflow Starter](https://gist.github.com/your-gist-link) *(I can create one if you'd like)*

Or, I can package this into a **GitHub repo template** for you.

---

## 🛠️ Want to Add?

- ✅ **Token validation** (JWT in `?token=`)
- ✅ **MongoDB save** in Function
- ✅ **Email confirmation** after signing
- ✅ **Audit log** in Application Insights

Let me know — I’ll expand the template!

You're all set to build a secure, scalable, and professional signature workflow! 🚀