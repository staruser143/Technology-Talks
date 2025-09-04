You're right to ask for a structured way to choose.  
**Quick correction first (my earlier message was wrong):** *Logic Apps Consumption is stateful by default and does **not** support stateless workflows; Logic Apps Standard supports **both** stateful and stateless workflows.* [1](https://learn.microsoft.com/en-us/azure/logic-apps/export-from-consumption-to-standard-logic-app)[2](https://blog.sandro-pereira.com/2024/08/30/friday-fact-logic-app-standard-workflows-can-be-configured-to-run-in-both-stateful-and-stateless-modes/)[3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare)

---

## Decision matrix: **Stateful vs. Stateless** (Azure Logic Apps)

Use the matrix below to score your scenario. Higher total wins.

> **How to score:** For each criterion, assign a **weight** (1–5) based on importance to your scenario, then give **Stateful** and **Stateless** a **score** (1–5). Multiply (Weight × Score) per cell and sum the totals.

| Criterion | What to consider | Bias (typical) | Key facts / evidence |
|---|---|---|---|
| **Long-running & orchestration** | Will the workflow span minutes–days with waits/approvals or external dependencies? | **Stateful** | Stateful persists execution state and history; Standard supports both modes, while Consumption is stateful-only. [4](https://learn.microsoft.com/en-us/azure/logic-apps/logic-apps-limits-and-config)[3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare) |
| **Auditability & diagnostics** | Do you need full run history for compliance/forensics? | **Stateful** | Run history is first-class; stateless has minimal/optional history (you can enable limited run history in Standard). [5](https://learn.microsoft.com/en-us/azure/logic-apps/view-workflow-status-run-history)[6](https://learn.microsoft.com/en-us/azure/logic-apps/create-standard-workflows-visual-studio-code) |
| **Throughput & latency** | Do you need ultra-fast responses and very high RPS? | **Stateless** | Stateless avoids persistence overhead and is typically faster for hot-path APIs. [7](https://www.serverlessnotes.com/docs/when-to-use-azure-logic-apps-stateless-execution) |
| **Cost sensitivity** | Is minimizing per-run/storage cost critical at high volume? | **Stateless** | Stateful uses external storage transactions; stateless reduces storage churn. (Standard: stateful incurs Azure Storage transactions.) [3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare) |
| **Failure recovery & durability** | Do you need easy replay/resubmit after faults? | **Stateful** | You get persisted runs and can review/re-run with same inputs. [5](https://learn.microsoft.com/en-us/azure/logic-apps/view-workflow-status-run-history) |
| **Network isolation / private access** | Need VNET integration, private endpoints, or single-tenant isolation? | **Stateless or stateful in Standard** | These capabilities are part of **Logic Apps Standard** (single-tenant), not Consumption. [3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare) |
| **Connector performance** | Heavy use of Service Bus/Event Hubs/SQL with low latency? | **Stateless or stateful in Standard** | Standard adds high-throughput **built-in connectors** on the single-tenant runtime. [3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare) |
| **Hosting model constraints** | Must you use Consumption? | **Stateful** | Consumption runs **stateful** workflows; stateless isn’t available there. [1](https://learn.microsoft.com/en-us/azure/logic-apps/export-from-consumption-to-standard-logic-app)[2](https://blog.sandro-pereira.com/2024/08/30/friday-fact-logic-app-standard-workflows-can-be-configured-to-run-in-both-stateful-and-stateless-modes/) |
| **Developer workflow** | Need local debug/run, containers, or portability? | **Standard (either mode)** | Standard supports local dev in VS Code and containerized/runtime flexibility. [6](https://learn.microsoft.com/en-us/azure/logic-apps/create-standard-workflows-visual-studio-code)[8](https://learn.microsoft.com/en-us/azure/logic-apps/create-single-tenant-workflows-azure-portal) |

---

### Blank scoring template (copy/paste)

```
Criterion                                Weight (1–5)   Stateful score (1–5)   Stateless score (1–5)
Long-running & orchestration
Auditability & diagnostics
Throughput & latency
Cost sensitivity
Failure recovery & durability
Network isolation / private access
Connector performance
Hosting model constraints
Developer workflow
-----------------------------------------------------------------------------------------------
Totals:                                   ____           Stateful total: ____    Stateless total: ____
Decision: (higher total wins)
```

---

## Worked examples

### 1) **Order approval with SLAs & audit**
- Human approvals, vendor callbacks, and compliance audits.
- **Weights & scores (example):**

| Criterion | W | Stateful | Stateless |
|---|---:|---:|---:|
| Long-running & orchestration | 5 | 5 | 2 |
| Auditability & diagnostics | 5 | 5 | 2 |
| Throughput & latency | 2 | 3 | 4 |
| Cost sensitivity | 3 | 3 | 4 |
| Failure recovery & durability | 4 | 5 | 3 |
| Network isolation / private access | 3 | 5 | 5 |
| Connector performance | 3 | 4 | 4 |
| Hosting model constraints | 2 | 5 | 1 |
| Developer workflow | 2 | 5 | 5 |
**Totals** |  | **(5×5 + 5×5 + 2×3 + 3×3 + 4×5 + 3×5 + 3×4 + 2×5 + 2×5) = 104** | **(5×2 + 5×2 + 2×4 + 3×4 + 4×3 + 3×5 + 3×4 + 2×1 + 2×5) = 78** |

**Recommendation:** **Stateful** (Logic Apps **Standard – stateful** if you need single-tenant/VNET; **Consumption** can also work since it’s stateful by default). [3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare)

---

### 2) **API gateway-style orchestration for an e‑commerce checkout**
- Synchronous request/response, sub‑second latency target, very high volume.
- **Weights & scores (example):**

| Criterion | W | Stateful | Stateless |
|---|---:|---:|---:|
| Long-running & orchestration | 3 | 2 | 4 |
| Auditability & diagnostics | 2 | 4 | 3 |
| Throughput & latency | 5 | 3 | 5 |
| Cost sensitivity | 4 | 3 | 5 |
| Failure recovery & durability | 3 | 4 | 3 |
| Network isolation / private access | 3 | 5 | 5 |
| Connector performance | 4 | 4 | 5 |
| Hosting model constraints | 2 | 5 | 1 |
| Developer workflow | 2 | 5 | 5 |
**Totals** |  | **(3×2 + 2×4 + 5×3 + 4×3 + 3×4 + 3×5 + 4×4 + 2×5 + 2×5) = 90** | **(3×4 + 2×3 + 5×5 + 4×5 + 3×3 + 3×5 + 4×5 + 2×1 + 2×5) = 113** |

**Recommendation:** **Stateless** on **Logic Apps Standard** for the hot path; optionally call a **stateful** workflow for background enrichment/compensation. [3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare)[7](https://www.serverlessnotes.com/docs/when-to-use-azure-logic-apps-stateless-execution)

---

## Quick chooser (rules of thumb)

- **Need stateless?** Choose **Logic Apps Standard** (only Standard supports stateless). [3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare)  
- **Must use Consumption?** You’ll be in **stateful** mode. [1](https://learn.microsoft.com/en-us/azure/logic-apps/export-from-consumption-to-standard-logic-app)[2](https://blog.sandro-pereira.com/2024/08/30/friday-fact-logic-app-standard-workflows-can-be-configured-to-run-in-both-stateful-and-stateless-modes/)  
- **Heavy compliance/audit or long waits?** Prefer **stateful**. [5](https://learn.microsoft.com/en-us/azure/logic-apps/view-workflow-status-run-history)  
- **High‑RPS, low‑latency APIs?** Prefer **stateless** (Standard). [7](https://www.serverlessnotes.com/docs/when-to-use-azure-logic-apps-stateless-execution)  
- **Need VNET/private endpoints or single‑tenant isolation?** Use **Standard** (either mode). [3](https://learn.microsoft.com/en-us/azure/logic-apps/single-tenant-overview-compare)

---
