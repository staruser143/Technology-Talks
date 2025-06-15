Here’s how you can build the **email-to-OneDrive Logic App workflow** in the **Azure Portal**:

---

### 🛠️ Step-by-Step Guide

#### **1. Sign in to Azure Portal**
- Go to https://portal.azure.com
- Use your Microsoft account to log in.

#### **2. Create a Logic App**
- In the search bar, type **“Logic Apps”** and select it.
- Click **“+ Create”**.
- Fill in the required details:
  - **Subscription** and **Resource Group**
  - **Logic App name**
  - **Region**
  - **Type**: Choose **Consumption** (for pay-per-use) or **Standard** (for more control)
- Click **“Review + Create”**, then **“Create”**.

#### **3. Open the Logic App Designer**
- Once deployed, go to your Logic App resource.
- Click **“Logic App Designer”** from the left menu.
- Choose **“Blank Logic App”** to start from scratch.

#### **4. Add a Trigger**
- Search for **“Outlook 365”** and select:
  - **“When a new email arrives (V3)”**
- Sign in to your Outlook account and grant permissions.
- Configure:
  - **Folder**: Inbox
  - **Include Attachments**: Yes
  - Add filters if needed (e.g., From, Subject)

#### **5. Add a Condition**
- Click **“+ New step”** → **“Control”** → **“Condition”**
- Set:
  - **Subject** → **contains** → `"Invoice"`

#### **6. Inside the “If yes” branch**
- Click **“Add an action”**
- Search for **“Get attachments”** (Outlook 365)
- Then add another action: **“Create file”** (OneDrive)
  - Choose the folder path
  - Use dynamic content to name the file and insert the attachment content

#### **7. Save and Test**
- Click **“Save”** in the top menu.
- Send a test email with an attachment and subject containing “Invoice” to your Outlook inbox.
- Monitor the run history in the Logic App overview.

---
