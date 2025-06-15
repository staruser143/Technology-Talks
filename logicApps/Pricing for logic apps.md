Here’s a breakdown of the **Azure Logic Apps pricing** as of 2025, based on the official [Azure pricing page](https://azure.microsoft.com/en-us/pricing/details/logic-apps/) [1](https://azure.microsoft.com/en-us/pricing/details/logic-apps/):

---

### ⚡ **1. Consumption Plan (Multi-Tenant)**
**Best for:** Lightweight, event-driven workflows.

#### 💰 Pricing Model:
- **Pay-per-use**: You are charged for each **trigger**, **action**, and **connector call**.
- **Standard Connector**: \$0.000125 per call
- **Enterprise Connector**: \$0.001 per call
- **Data Retention**: \$0.12 per GB/month (for run history)

#### 🧾 Example:
If your workflow runs 1,000 times a day with 5 actions each:
- 1,000 × 5 = 5,000 actions/day
- Monthly cost ≈ 5,000 × 30 × \$0.000125 = **\$18.75/month**

---

### 🧱 **2. Standard Plan (Single-Tenant)**
**Best for:** Complex, stateful, or enterprise-grade workflows.

#### 💰 Pricing Model:
- Based on **App Service Plan** (compute + memory)
- Example pricing (Premium v3 plans):
  - **P1 v3 (2 cores, 8 GB RAM)**: \$229.95/month
  - **P2 v3 (4 cores, 16 GB RAM)**: \$459.90/month
- **Workflow Service Plan**: \$140.16/month (vCPU) + \$10.001/month (Memory)

#### 🧾 Additional Costs:
- **Connectors**: Same per-call pricing as Consumption
- **Integration Account** (for B2B/EDI):
  - Basic: \$0.42/hour
  - Standard: \$1.37/hour

---

### 🧠 Summary Comparison

| Feature | Consumption | Standard |
|--------|-------------|----------|
| Billing | Per execution | Per compute (App Service Plan) |
| Scaling | Auto | Manual or Elastic |
| Local Dev | ❌ | ✅ |
| Stateful | ❌ | ✅ |
| VNET Support | ❌ | ✅ |

---

