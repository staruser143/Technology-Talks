An audit trail is essentially the "chain of custody" for a digital document. To satisfy **ESIGN**, **UETA**, and **eIDAS**, your system shouldn't just store the signature; it must store the *context* of how that signature happened.

Below is a standard JSON schema you can use as a blueprint for your database or a "Certificate of Completion" PDF.

### Sample Audit Trail Schema (JSON)

```json
{
  "document_id": "doc_88234af21",
  "document_hash": "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "status": "completed",
  "audit_events": [
    {
      "timestamp": "2026-01-23T09:15:00Z",
      "action": "DOCUMENT_CREATED",
      "actor": {
        "name": "Admin User",
        "email": "admin@company.com",
        "ip_address": "192.168.1.1"
      }
    },
    {
      "timestamp": "2026-01-23T09:16:10Z",
      "action": "EMAIL_SENT",
      "recipient": "signer@client.com",
      "metadata": { "delivery_id": "msg_5521" }
    },
    {
      "timestamp": "2026-01-23T09:17:45Z",
      "action": "CONSENT_GIVEN",
      "actor": {
        "email": "signer@client.com",
        "ip_address": "203.0.113.42",
        "user_agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0...)"
      },
      "details": "User checked 'I agree to do business electronically'"
    },
    {
      "timestamp": "2026-01-23T09:18:30Z",
      "action": "SIGNATURE_APPLIED",
      "actor": {
        "email": "signer@client.com",
        "ip_address": "203.0.113.42"
      },
      "signature_metadata": {
        "method": "drawn",
        "mfa_verified": true,
        "mfa_type": "SMS_OTP"
      }
    }
  ]
}

```

### Why these specific fields matter:

* **`document_hash`**: This is your proof of **Integrity**. If someone changes the price from $100 to $1,000 later, the hash will change, and this audit trail will prove the original version.
* **`CONSENT_GIVEN`**: This satisfies the specific "Consumer Disclosure" requirement in **ESIGN/UETA**. Without a record of this, the signature can be argued as invalid in many US jurisdictions.
* **`user_agent` & `ip_address**`: This provides "Attribution." It helps prove that it was actually the signer's device, not yours, that performed the action.
* **`mfa_verified`**: Essential for **eIDAS Advanced (AdES)** signatures. It proves a "unique link" between the signer and the signature.

---

### Comparison of Requirements

| Feature | ESIGN / UETA (US) | eIDAS (EU) |
| --- | --- | --- |
| **Intent to Sign** | Required (button click/drawing) | Required |
| **Tamper Evidence** | Expected (Audit Log) | **Mandatory** (Digital Seals/Hash) |
| **ID Verification** | Low (Email sufficient) | High (Often requires MFA/ID) |
| **Storage** | 7+ Years recommended | Governed by GDPR + local laws |

### Implementation Tip: The "Envelope" Concept

Don't just store the signed PDF. Store it as an **"Envelope"** (a folder or database record) that contains:

1. The Original PDF (Before signing).
2. The Signed PDF (With the digital signature block).
3. The Audit Trail (The JSON/PDF log above).

**Would you like me to provide a Python or Node.js snippet to generate a SHA-256 hash for document integrity?**
