Below is a **services × scenarios matrix** for **Scenarios 1–7**.  
Legend:

*   ✅ **Correct** = one of the *exactly two* best answers for that scenario
*   🟡 **Works** = could work, but suboptimal / violates intent / adds unnecessary complexity
*   ❌ **Distractor** = wrong layer / wrong assumptions / doesn’t meet requirements

***

## ✅ Matrix: Services vs Scenarios (1–7)

| Service / Option                       | S1 NFS→EFS (incremental, retire apps) | S2 SMB→S3 (incremental, no scripts) | S3 400TB seed + weekly updates | S4 Partner uses NFS workflow | S5 SMB→Glacier (one-time, min ops) | S6 On‑prem→2 regions (continuous) | S7 SMB→S3 (avoid hybrid infra) |
| -------------------------------------- | ------------------------------------: | ----------------------------------: | -----------------------------: | ---------------------------: | ---------------------------------: | --------------------------------: | -----------------------------: |
| **AWS DataSync**                       |                                     ✅ |                                   ✅ |                              ✅ |                            ✅ |                                  ✅ |                                 ✅ |                              ✅ |
| **AWS Storage Gateway (File Gateway)** |                                     ❌ |                                   ✅ |                             🟡 |                            ✅ |                                  ❌ |                                🟡 |                              ❌ |
| **AWS Snowball**                       |                                    🟡 |                                   ❌ |                              ✅ |                           🟡 |                                 🟡 |                                🟡 |                             🟡 |
| **Amazon EFS**                         |                                     ✅ |                                   — |                              — |                            — |                                  — |                                 — |                              — |
| **Amazon S3**                          |                                     — |                                   — |                              — |                            — |                                  — |                                 — |                              ✅ |
| **Amazon S3 Replication**              |                                     ❌ |                                   ❌ |                              ❌ |                            ❌ |                                  — |                                 ✅ |                              — |
| **AWS Transfer Family**                |                                     — |                                  🟡 |                              ❌ |                            ❌ |                                  ❌ |                                 — |                             🟡 |
| **S3 Lifecycle policies**              |                                     — |                                   — |                              — |                            — |                                  ✅ |                                 — |                              — |
| **EventBridge**                        |                                     — |                                   ❌ |                              — |                            — |                                  — |                                 — |                              — |
| **AWS DMS**                            |                                     — |                                   — |                              — |                            — |                                  — |                                 ❌ |                              — |

**Notes**

*   “—” = not present as an answer option in that scenario (or not relevant to grade for that scenario).
*   In this matrix, I’ve kept the grading consistent with the earlier answer key:
    *   **S1** “exactly two” was treated as **DataSync + EFS**, with **Snowball as Works** (optional seed).
    *   **S2** “exactly two” was **DataSync + File Gateway** (hybrid SMB endpoint without scripts).
    *   **S6** “exactly two” was **DataSync + S3 Replication** (on‑prem → S3 → cross‑region).

***

## 🔍 Quick pattern read (what the matrix reveals)

### 1) DataSync is the “spine”

You’ll notice **DataSync is ✅ in all 7** scenarios because every scenario involves **file migration/sync** from on‑prem → AWS storage.

### 2) File Gateway flips based on **hybrid access**

*   ✅ when you must preserve **SMB/NFS access semantics** for active workflows (S2, S4)
*   ❌ when **apps are retired** or you must **avoid hybrid infrastructure** (S5, S7)

### 3) S3 Replication only appears when **source is already S3**

That’s why it’s ✅ only in **S6**, where the implied pattern is:

> On‑prem → S3 (DataSync) → cross‑region (S3 Replication)

### 4) Glacier = Lifecycle policy

S5 is the only one that explicitly mentions Glacier, so **Lifecycle** becomes one of the two best answers.

***

