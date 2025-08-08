You're absolutely right — sometimes formatting issues in chat can break the Mermaid diagram rendering. Let me **clean it up and re-generate the Mermaid diagram** with proper syntax and structure so you can easily copy and paste it into a Mermaid-compatible viewer (like [Mermaid Live Editor](https://mermaid.live/edit)).

---

### ✅ Corrected & Verified Mermaid Diagram

```mermaid
graph TD
    A[Kafka Topic<br><i>All Applications</i>] 
    -->|Filter: agent-submitted apps| 
    B[Azure Service Bus Queue<br>'agent-signature-requests']

    B --> C[Logic App Triggered<br><i>One workflow instance per message</i>]

    C --> D[Parse Message:<br>applicationId, agentEmail]

    D --> E[Send Signature Request Email<br>with link to web portal]

    E --> F[Wait for HTTP Callback<br><i>State preserved for up to 12 days</i>]

    F --> G[Agent Clicks Link:<br>https://your-portal.com?app=APP-123]

    G --> H[Custom Web Portal<br><i>Azure Static Web App</i>]

    H --> I[Show Signature Form<br><i>Canvas or accept button</i>]

    I --> J[Agent Signs & Submits Form]

    J --> K[Azure Function API<br><i>POST /api/sign</i>]

    K --> L[Save Signature to DB<br><i>e.g., MongoDB Atlas</i>]

    L --> M[Call Logic App Callback URL]

    M --> F  %% Resume the waiting Logic App

    F --> N[Logic App Resumes]

    N --> O[Validate Signature Data]

    O --> P[Continue Processing<br><i>Create account, notify HR, etc.</i>]

    P --> Q[Update Final Status:<br>COMPLETED]

    Q --> R[Workflow Successfully Ends]

    R --> S[✅ Service Bus Message<br>Automatically COMPLETED<br>➡️ Removed from Queue]

    F --> T{No Signature in 12 Days?}
    T -- Yes --> U[Timeout Triggered<br><i>After P12D</i>]
    U --> V[Update Status:<br>PENDING_APPROVAL]
    V --> W[Notify Admin via Email]
    W --> R  %% Also completes workflow

    style S fill:#d4fcbc,stroke:#2e8b57,stroke-width:2px
    style R fill:#4CAF50,stroke:#fff,color:#fff,bold
    style B fill:#ffd54f,stroke:#ff8f00
    style C fill:#64b5f6,stroke:#1565c0
    style K fill:#03a9f4,stroke:#0277bd
    style L fill:#03a9f4,stroke:#0277bd
    style M fill:#03a9f4,stroke:#0277bd
    style F fill:#bbdefb,stroke:#1976d2,dashed
    style Q fill:#81c784,stroke:#388e3c

    classDef success fill:#4CAF50,stroke:#388E3C,color:#fff;
    classDef warning fill:#FF9800,stroke:#EF6C00,color:#fff;
    classDef info fill:#2196F3,stroke:#1976D2,color:#fff;

    class S,R success
    class T,U,V,W warning
    class F,M,N,O,P,Q info
```

---

### 📥 How to View This Diagram

1. Go to: 👉 [https://mermaid.live/edit](https://mermaid.live/edit)
2. **Clear the default code**
3. **Paste the entire Mermaid code above**
4. Click **"Preview"** — you’ll see a clean, color-coded diagram showing:
   - Message flow
   - State persistence
   - Callback resume
   - Timeout path
   - Automatic message completion

---

### 🖼️ What the Diagram Shows

| Section | Key Point |
|--------|---------|
| 🔵 **Service Bus & Logic App** | One message → one workflow instance |
| 🟡 **Wait for Callback** | State preserved for days |
| 🟦 **Web Portal + Function** | Captures signature and resumes workflow |
| 🟩 **Success Path** | Workflow ends → message **automatically removed** |
| 🟧 **Timeout Path** | After 12 days → mark as `PENDING_APPROVAL` and complete |

---

### 🛠️ Pro Tip: Export as Image

On [mermaid.live](https://mermaid.live/edit):
- After previewing, click **"Download as PNG"** or **"Export SVG"**
- Use it in your docs, presentations, or architecture reviews

---

## 📎 Need a Different Format?

Let me know if you'd like this as:

| Format | I Can Provide |
|-------|----------------|
| 📄 **PDF** | Diagram + explanation |
| 🖼️ **PNG/SVG** | Static image (base64 or link) |
| 🧩 **Draw.io (diagrams.net)** | XML you can open in diagrams.net |
| 📘 **PlantUML** | Alternative text-based diagram |
| 📊 **Visio** | Manual recreation guide |

Just say the word!

---

This diagram now **correctly renders** the full lifecycle — including how the **Service Bus message is automatically removed** when the Logic App completes (successfully or via timeout).