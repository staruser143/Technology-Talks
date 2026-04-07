Below is an **exam‑oriented and architecture‑friendly decision tree** to help you choose the **right AWS Transfer Family protocol** (SFTP vs FTPS vs FTP vs AS2).

This is tailored for **SAP‑C02 / enterprise design scenarios**, with clear elimination logic.

***

# AWS Transfer Family – Protocol Selection Decision Tree

    Start
     │
     │── Do you need B2B / EDI-style guaranteed delivery,
     │   message signing, non‑repudiation, or MDNs?
     │        │
     │        ├── YES → ✅ AS2
     │        │        (Retail, supply-chain, healthcare, EDI, compliance-heavy)
     │        │
     │        └── NO
     │
     │── Is strong encryption + modern security mandatory?
     │   (No plaintext credentials or data)
     │        │
     │        ├── NO → ⚠️ FTP (generally discouraged)
     │        │        (Only for legacy, controlled networks)
     │        │
     │        └── YES
     │
     │── Do clients support SSH-based authentication
     │   (keys, IAM, automation-friendly)?
     │        │
     │        ├── YES → ✅ SFTP
     │        │        (Most common & default choice)
     │        │
     │        └── NO
     │
     │── Do clients explicitly require TLS certificates
     │   and FTPS (explicit or implicit)?
     │        │
     │        ├── YES → ✅ FTPS
     │        │        (Legacy enterprise tools, Windows-only stacks)
     │        │
     │        └── NO → ✅ SFTP (fallback / recommend modernization)

***

## Protocol-by-Protocol Decision Rationale

### ✅ **SFTP (Default / Most Recommended)**

**Choose SFTP if:**

*   You want the **simplest, most secure, most common option**
*   Clients support SSH keys
*   Automation and CI/CD pipelines are involved
*   You want **least firewall complexity**

**Why AWS prefers it**

*   Single TCP port (22)
*   Strong encryption
*   Easy IAM + SSH key integration
*   Most common **SAP‑C02 answer**

**Typical scenarios**

*   Partner uploads to S3
*   Secure data ingestion
*   Replacing EC2-hosted SFTP servers

✅ **Exam hint:**

> “Secure file transfer with minimal operational overhead” → **SFTP**

***

### ✅ **FTPS (TLS-based legacy security)**

**Choose FTPS if:**

*   Client mandates **TLS certificates**
*   Windows or legacy enterprise tools only support FTPS
*   Compliance policies explicitly disallow SSH

**Trade-offs**

*   Harder firewall rules (control + data channels)
*   More client-side complexity
*   Still secure, but operationally heavier than SFTP

✅ **Exam hint:**

> “Client explicitly requires FTPS using SSL certificates” → **FTPS**

***

### ⚠️ **FTP (Unencrypted)**

**Choose FTP only if:**

*   Legacy systems **cannot be changed**
*   Operates in a **trusted, isolated network**
*   Security requirements are explicitly relaxed

**Why it’s almost never correct**

*   No encryption
*   Credentials in plaintext
*   Fails most compliance reviews

❌ **Exam elimination rule:**

> If security is mentioned → **FTP is wrong**

***

### ✅ **AS2 (B2B / EDI specialist protocol)**

**Choose AS2 if:**

*   EDI document exchange is required
*   Trading partners demand:
    *   **Non‑repudiation**
    *   **Digital signatures**
    *   **Message Disposition Notifications (MDNs)**

**Common industries**

*   Retail supply chains
*   Healthcare
*   Manufacturing
*   Large-scale B2B integrations

✅ **Exam power phrase:**

> “EDI with guaranteed delivery and compliance” → **AS2**

***

## Fast Elimination Matrix (Exam Speed)

| Requirement Mentioned         | Correct Protocol |
| ----------------------------- | ---------------- |
| EDI / MDN / Non‑repudiation   | ✅ AS2            |
| Secure, standard, minimal ops | ✅ SFTP           |
| TLS certificates required     | ✅ FTPS           |
| Legacy + insecure             | ⚠️ FTP           |
| Single port simplicity        | ✅ SFTP           |
| Compliance-heavy B2B          | ✅ AS2            |

***

## One-Glance SAP‑C02 Reflex Rule

> **If the question does NOT explicitly say AS2 or FTPS → choose SFTP**

This rule holds true in **80%+** of Transfer Family exam questions.

***

