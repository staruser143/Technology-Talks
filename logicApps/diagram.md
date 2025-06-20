Here is the **architecture diagram** illustrating the migration of the post-enrollment workflow to **Azure Logic Apps**:

![Post-Enrollment Workflow Migration to Azure Logic Apps](blob:https://m365.cloud.microsoft/6ef0f56f-d1e1-4c71-aece-d71d19ac8989)

---

### 🔍 Diagram Highlights

- **Triggers**:
  - **HTTP Trigger**: For real-time API calls.
  - **Timer Trigger**: For scheduled batch jobs.

- **Logic Apps Workflow**:
  - Central orchestration using Azure Logic Apps.

- **Connectors**:
  - **SQL Database**: For data storage and retrieval.
  - **Blob Storage**: For file handling.
  - **Email Connector**: For notifications.

- **Custom Logic**:
  - **Azure Functions**: For executing custom business logic.

- **External Systems**:
  - **CRM System** and **External APIs** integrated via Logic Apps.

---

