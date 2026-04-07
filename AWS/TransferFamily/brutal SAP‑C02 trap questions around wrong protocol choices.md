Excellent — here are **brutal SAP‑C02‑style trap questions** specifically designed to catch **wrong AWS Transfer Family protocol choices**.

These mirror the **elimination traps**, wording tricks, and compliance misdirection you’ll see in the real exam.

***

# 🔥 Brutal SAP‑C02 Trap Questions

## AWS Transfer Family – Protocol Selection

***

## Question 1: The “Invisible AS2 Trap”

A retail enterprise exchanges purchase orders with hundreds of suppliers.  
The partners require **non‑repudiation, message signing, encryption, and delivery receipts**.  
The solution must be **fully managed** and integrate with Amazon S3.

**Which solution should the Solutions Architect choose?**

A. AWS Transfer Family using SFTP  
B. AWS Transfer Family using FTPS  
C. AWS Transfer Family using AS2  
D. Amazon S3 with presigned URLs

✅ **Correct answer:** **C**

### Why this is brutal

*   ✅ Keywords: **non‑repudiation**, **receipts**, **EDI semantics**
*   ❌ SFTP and FTPS are secure but **do not support MDNs**
*   ❌ Presigned URLs lack partner‑to‑partner delivery guarantees

**Exam reflex:**

> If you see *non‑repudiation* or *MDN* → ✅ **AS2**, no thinking required.

***

## Question 2: The “Secure ≠ AS2” Trap

A SaaS provider allows customers to upload daily CSV files securely into Amazon S3.  
The provider controls the onboarding process and wants **minimal client configuration**.  
Security and operational simplicity are critical.

**Which protocol should be used?**

A. AS2  
B. FTPS  
C. SFTP  
D. FTP

✅ **Correct answer:** **C**

### Why people fail

*   ❌ AS2 sounds “more secure”, but **adds unnecessary EDI baggage**
*   ❌ FTPS complicates firewall rules
*   ✅ SFTP is secure, simple, and industry default

**Exam rule:**

> “Secure upload + simplicity + no EDI” → **SFTP**

***

## Question 3: The “Certificate Word Trap”

A financial institution requires file transfers encrypted using **TLS certificates**, as mandated by internal compliance.  
Clients run legacy Windows software that **does not support SSH key authentication**.

**Which Transfer Family protocol meets these requirements?**

A. SFTP  
B. FTPS  
C. AS2  
D. FTP

✅ **Correct answer:** **B**

### Why this traps people

*   ❌ SFTP is secure but **does not use TLS certificates**
*   ✅ FTPS explicitly uses TLS
*   ❌ AS2 not required unless EDI semantics exist

**Trigger phrase:**

> “TLS certificates required” → ✅ **FTPS**

***

## Question 4: The “FTP Isn’t Dead?” Trap

An internal batch system uploads files nightly from a trusted on‑prem network to Amazon S3.  
The data is non‑sensitive.  
The company wants the **simplest protocol with minimal overhead**.

**What should the architect recommend?**

A. FTP  
B. SFTP  
C. FTPS  
D. AS2

✅ **Correct answer (SAP‑C02 lens):** **B**

### Why this is savage

*   Even though FTP *might* technically work…
*   ✅ **SAP‑C02 will never reward unencrypted protocols**
*   ❌ “Non‑sensitive” is a red herring

**Exam bias rule:**

> If security is not explicitly relaxed → **FTP is wrong**

***

## Question 5: The “Too Much Security” Trap

A logistics company exchanges shipping manifests with partners.  
The partners only require **encrypted transport** and have no audit or compliance demands beyond confidentiality.

**Which option is MOST appropriate?**

A. AS2 with MDNs  
B. FTPS  
C. SFTP  
D. SFTP over a VPN

✅ **Correct answer:** **C**

### Elimination logic

*   ❌ AS2 adds cost and operational complexity without benefit
*   ❌ FTPS not required unless TLS is mandated
*   ✅ SFTP provides strong encryption with least friction

**Exam wording cue:**

> “MOST appropriate” = avoid over‑engineering

***

## Question 6: The “Single Port Firewall Trap”

A partner requires file uploads through a firewall that only allows **one inbound TCP port**.  
The solution must be secure and managed.

**Which protocol should be selected?**

A. FTPS  
B. FTP  
C. SFTP  
D. AS2

✅ **Correct answer:** **C**

### Why this works

*   ✅ SFTP uses **one port (22)**
*   ❌ FTPS requires multiple data ports
*   ❌ FTP is insecure
*   ❌ AS2 is irrelevant unless EDI is required

**Exam shortcut:**

> “Firewall complexity” → ✅ **SFTP**

***

## Question 7: The “AS2 by Industry Name” Trap

A healthcare provider exchanges claim data with clearinghouses.  
The exchange must support **encryption, digital signatures, and delivery acknowledgements**.

**Which protocol BEST fits this requirement?**

A. SFTP  
B. FTPS  
C. AS2  
D. HTTPS uploads to Amazon S3

✅ **Correct answer:** **C**

### Hidden clue

*   Healthcare + acknowledgements + signing = **EDI**
*   Even if EDI is not spelled out, AS2 is implied

***

## Question 8: The “False Familiarity” Trap

A developer proposes SFTP for an EDI workload because it is secure and widely supported.

**What is the BEST reason this is incorrect?**

A. SFTP does not support encryption  
B. SFTP does not provide message-level acknowledgements  
C. SFTP cannot integrate with S3  
D. SFTP is not supported by AWS

✅ **Correct answer:** **B**

### Why SAP‑C02 loves this

*   Forces protocol‑level understanding
*   Security ≠ EDI guarantees

***

## Ultra‑Fast Elimination Cheat

| Statement in Question          | Kill Which Answer |
| ------------------------------ | ----------------- |
| “Non‑repudiation / MDN”        | ❌ SFTP / FTPS     |
| “TLS certificates mandated”    | ❌ SFTP            |
| “Minimal ops / default secure” | ❌ AS2             |
| “Firewall complexity”          | ❌ FTPS            |
| “Secure” mentioned anywhere    | ❌ FTP             |

***

### Want next?

