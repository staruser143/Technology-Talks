# Amazon S3 Object Lock

Amazon S3 Object Lock helps protect individual object versions from permanent deletion for a defined period, or indefinitely while a legal hold is active. It uses a **Write Once, Read Many (WORM)** model and is commonly used for regulated records, audit evidence, backup protection, and ransomware resilience.

In practical terms:

> Once a record version is written, it cannot be permanently deleted during its protection period.

## How Object Lock works

Object Lock works with **S3 Versioning**. Protection applies to an individual object version, not just to the object key. If an application uploads another object with the same key, S3 can create a new version; the existing locked version remains protected.

Object Lock has two retention modes and a separate legal-hold mechanism:

1. **Governance mode** — authorized principals can bypass retention when they have the required permission and explicitly request the bypass.
2. **Compliance mode** — a protected version cannot be deleted or have its retention period shortened before the retention date, including by the AWS account root user.
3. **Legal hold** — protects a version until an authorized principal removes the hold. It has no expiration date and can be used with or without a retention period.

### Governance mode

Governance mode protects an object version from deletion by default. A principal with the required permissions can bypass that protection, making this mode useful when strong safeguards are needed alongside a controlled administrative exception.

For example:

| Example version | Retention | Mode |
|---|---:|---|
| `audit-record.json` | 7 years | Governance |

An ordinary application or operations identity cannot delete the protected version. An authorized compliance administrator can bypass retention if granted the required permission and makes the explicit bypass request.

### Compliance mode

Compliance mode provides stronger protection. During the retention period, no identity—including the AWS account root user—can delete the protected version or shorten its retention period.

| Example version | Retain until | Mode |
|---|---|---|
| `audit-record.json` | October 20, 2033 | Compliance |

After the retention date, deletion may be possible, provided no legal hold or other restriction still applies. Compliance mode is appropriate when immutability is a regulatory or contractual requirement.

### Legal hold

A retention period protects a version until a specific date. A legal hold protects it until an authorized principal explicitly removes the hold.

Legal holds do not require a predefined expiration date and can be applied independently of a retention period. For example, a broker’s records may have a seven-year compliance retention period, while records relevant to an active investigation also have a legal hold:

```text
Broker-123 records
├── Retention: 7 years (Compliance mode)
└── Legal hold: On
```

When the retention period expires, the legal hold continues to protect the version until the hold is removed.

## At a glance

| Feature | Governance mode | Compliance mode | Legal hold |
|---|---|---|---|
| Prevents deletion of a protected version by default | Yes | Yes | Yes |
| Has a fixed retention period | Yes | Yes | No |
| Can be bypassed or removed early | Yes, with the required permission and an explicit bypass request | No, during the retention period | Yes, by an authorized principal |
| Protection ends | At the retention date, unless extended | At the retention date, unless extended | When the hold is removed |
| Typical use | Internal governance with a controlled exception | Regulatory or contractual immutability | Investigation or litigation |

## Use in an audit and compliance architecture

For a broker or agent auditing system, Object Lock can provide an immutability control at the storage layer:

```text
Broker / agent interaction
          │
          ▼
Application / agent
  ├── Broker ID
  ├── Session and conversation IDs
  ├── Timestamp and intent
  ├── Search ID
  ├── Agent actions
  └── Tool and API calls
          │
          ▼
     Audit pipeline
          │
          ▼
Amazon S3 (Versioning + Object Lock)
  ├── Governance mode
  └── or Compliance mode
          │
          ▼
 Immutable audit archive
```

The controls serve different purposes:

- **Encryption** protects confidentiality.
- **IAM and bucket policies** control access.
- **Object Lock** protects retained object versions from deletion or retention changes, according to the selected mode.

Object Lock is an additional safeguard against accidental or malicious deletion of protected versions. It does not replace access controls, encryption, or a complete backup and recovery strategy.

For an auditing and compliance architecture decision record, Object Lock is especially relevant when the requirement is not merely to retain audit logs, but to make audit evidence tamper-resistant for a mandated period.
