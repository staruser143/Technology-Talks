Perfect — this is exactly the **SAP‑C02 “nuance separation” table** AWS expects you to internalize.

Below is the **Works vs Correct vs Distractor super‑table**, tuned for **PrivateLink + Regional failure + application‑level errors**.

***

## ✅ Works vs ✅✅ Correct vs ❌ Distractor — Super‑Table

**Scenario: Third‑party service via PrivateLink returns internal errors in one Region; must fail over to another Region**

| Option                                                                                                | Classification   | Why (Exam‑Level Reasoning)                                                                                                                                              |
| ----------------------------------------------------------------------------------------------------- | ---------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Route 53 private hosted zone + health checks + failover routing to Regional PrivateLink endpoints** | ✅✅ **CORRECT**   | Only option that detects **application‑level failures (HTTP 500s)** and performs **automatic Regional failover** while fully supporting **PrivateLink** and private DNS |
| Route 53 latency‑based routing to multiple PrivateLink endpoints                                      | ✅ *WORKS*        | Can distribute traffic, but **does not trigger failover on errors**—latency may remain low even when the service is unhealthy                                           |
| Application‑side retries + circuit breaker pattern                                                    | ✅ *WORKS*        | Improves stability but **does not shift traffic to another Region** once the local third‑party service is unhealthy                                                     |
| Multi‑Region deployment of the trading application with manual DNS change                             | ✅ *WORKS*        | Achieves failover eventually, but **not automated** and violates RTO expectations implied by financial loss                                                             |
| NLB health checks on the PrivateLink endpoint service                                                 | ✅ *WORKS*        | Detects **target liveness**, not **response correctness**. Service may respond with 500s and still pass health checks                                                   |
| AWS App Mesh / Envoy retries across Regions                                                           | ✅ *WORKS*        | Enhances resilience but still depends on **routing logic outside of DNS** and is not PrivateLink‑aware by default                                                       |
| AWS Global Accelerator in front of the application                                                    | ❌ **DISTRACTOR** | **Does not support PrivateLink endpoints**; commonly tempting but invalid                                                                                               |
| VPC peering or Transit Gateway to another Region                                                      | ❌ **DISTRACTOR** | Solves connectivity, **not failure detection or traffic steering**                                                                                                      |
| Multi‑Region ALB fronting PrivateLink services                                                        | ❌ **DISTRACTOR** | PrivateLink services are **Regional and cannot be fronted cross‑Region by ALB**                                                                                         |
| Rely on timeout‑based failover in SDKs                                                                | ❌ **DISTRACTOR** | No deterministic Regional traffic control; unpredictable under high‑loss financial workloads                                                                            |

***

## 🔎 How the Exam Separates *Correct* vs *Works*

### ✅ *WORKS*

*   Improves resiliency **within a Region**
*   Requires **manual intervention**
*   Detects **infrastructure failures only**
*   Lacks **DNS‑level Regional control**

### ✅✅ *CORRECT*

*   Detects **application‑level errors**
*   Performs **automatic Regional failover**
*   Works **natively with PrivateLink**
*   Requires **no public exposure**

### ❌ *DISTRACTOR*

*   Breaks PrivateLink assumptions
*   Solves the wrong layer (network vs application)
*   Sounds “enterprise‑grade” but **fails one key constraint**

***

## 🧠 Exam Memory Hook

> **PrivateLink + application errors + Regional failover ⇒ Route 53 health‑check‑based DNS failover**  
> Anything else either *works locally* or *sounds impressive but violates PrivateLink rules*.

***
