Here’s a concise summary of **when to add Messaging Units (MUs) in Azure Service Bus Premium**:

---

### ✅ **What is an MU?**
- A **Messaging Unit** is a dedicated slice of compute and memory for your Premium namespace.
- Each MU provides **predictable throughput and latency**.
- You can scale from **1 to 4 MUs** (or more in some regions).

---

### **When to Add More MUs**
1. **Throughput Saturation**
   - If your namespace is hitting **throttling limits** or **latency increases** under load.
   - Each MU adds more **concurrent connections**, **message processing capacity**, and **brokered operations per second**.

2. **High Concurrency**
   - Large number of **simultaneous senders/receivers** or **sessions**.
   - Example: Thousands of clients maintaining active connections.

3. **Large Message Size or Sessions**
   - Premium supports **up to 100 MB per message**, but large payloads consume more resources.
   - Session-enabled queues/topics also require more memory.

4. **Isolation for Multiple Workloads**
   - If you run **multi-tenant apps** or **critical workloads** in the same namespace, adding MUs can isolate performance.

---

### **Cost Impact**
- **Each MU adds a fixed hourly cost** (~\$0.928/hr → \$678/month per MU).
- **Geo-DR and other features remain the same**, but cost doubles with 2 MUs.
- **Break-even analysis**:
  - 1 MU becomes cost-effective vs Standard at **~1.29B ops/month**.
  - 2 MUs **do not break even** on cost alone (even at 2B ops/month), so you add them **only for performance, SLA, or compliance reasons**.

---
