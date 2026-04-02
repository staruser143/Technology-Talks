Correct vs Distractor Exam TableBelow is a **Pro‑level “Correct vs Distractor” exam table**, framed exactly how AWS Solutions Architect **Professional (SAP‑C02)** questions test this scenario.

***

## ✅ Correct vs Distractor Exam Table

**Scenario: Regional failure of third‑party service accessed via AWS PrivateLink**

| Option                                                                                                                              | Correct / Distractor | Why (Exam Reasoning)                                                                                                                                                                                                                |
| ----------------------------------------------------------------------------------------------------------------------------------- | -------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Amazon Route 53 private hosted zone with health checks and a failover routing policy pointing to Regional PrivateLink endpoints** | ✅ **CORRECT**        | Route 53 health checks can validate **application‑level failures** (HTTP 500s). Failover routing provides **automatic Regional redirection** at DNS level. Fully compatible with **PrivateLink** and preserves private connectivity |
| Amazon Route 53 latency‑based routing between PrivateLink endpoints                                                                 | ❌ Distractor         | Latency routing does **not fail over on errors**; it may continue sending traffic to a failing Region if latency is still low                                                                                                       |
| Network Load Balancer health checks on the PrivateLink endpoint service                                                             | ❌ Distractor         | NLB health checks validate **target health only**, not third‑party **application response quality** (e.g., internal server errors)                                                                                                  |
| AWS Global Accelerator in front of the trading application                                                                          | ❌ Distractor         | Global Accelerator **does not integrate with PrivateLink** endpoints and cannot route traffic to Interface VPC Endpoints                                                                                                            |
| Implement retries and circuit breakers in the application code                                                                      | ❌ Distractor         | Improves resilience but **does not provide Regional failover** away from the failing third‑party service                                                                                                                            |
| VPC peering or Transit Gateway between Regions                                                                                      | ❌ Distractor         | Network connectivity does not detect or bypass **application‑level failures**                                                                                                                                                       |
| Multi‑Region ALB with health checks routing to PrivateLink services                                                                 | ❌ Distractor         | PrivateLink endpoints are **Regional and not directly fronted by ALB** for cross‑Region routing                                                                                                                                     |
| AWS App Mesh with Envoy retries                                                                                                     | ❌ Distractor         | Improves resiliency locally but still depends on the **same Regional backend** unless combined with Route 53 failover                                                                                                               |

***

## 🔑 Exam Takeaways (What AWS Wants You to Recognize)

*   **PrivateLink = Regional only**
*   **Application‑level failure (HTTP 500)** ⇒ Route 53 health check
*   **Regional failover requirement** ⇒ Route 53 failover routing
*   **Security constraint** ⇒ Private hosted zone + Interface VPC endpoints
*   **Global Accelerator is a trap** when PrivateLink is involved

***

## 🧠 One‑Line Exam Rule (Memorize This)

> **If a PrivateLink‑based service fails at the application level in one Region, the only AWS‑native way to fail over Regions is Route 53 health‑check‑based DNS failover.**
***
**Control‑Tower‑style multi‑Region PrivateLink DNS diagram**
