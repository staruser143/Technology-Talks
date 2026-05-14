Great questions. Let me break down each source precisely:

---

## Wiz Fields That Indicate Internet Exposure

When you export findings from Wiz (via UI, API, or SIEM integration), these are the key fields to look for:

### Primary Exposure Fields

| Wiz Field | Description |
|---|---|
| `networkExposure` | Key field — values: **`Public`**, `Internal`, `Restricted` — filter on **`Public`** |
| `isInternetFacing` | Boolean (`true`/`false`) — directly flags internet-exposed resources |
| `exposedPorts` | Lists open ports visible from the internet |
| `ingressRoute` | Shows the network path — e.g., Load Balancer → EC2 → App. Confirms actual reachability |
| `attackSurface` | Wiz's own attack surface score, factoring in exposure + asset criticality |

### Supporting Context Fields

| Wiz Field | Description |
|---|---|
| `subscriptionName` / `projectName` | Identifies which cloud account/project the asset lives in |
| `resourceType` | e.g., `VM`, `Container`, `ServerlessFunction` — helps identify app type |
| `relatedFindings` | Links cloud misconfig findings (e.g., overly permissive Security Group) that caused exposure |
| `externalIpAddress` | Confirms a public IP is attached |
| `cloudProviderURL` | Direct link to the AWS/Azure/GCP resource |

### How to Filter in Wiz
When querying via **Wiz API (GraphQL)** or exporting CSVs, use:
```graphql
vulnerabilities(filter: {
  networkExposure: {equals: "Public"},
  severity: {in: ["CRITICAL", "HIGH"]}
})
```
Or in the Wiz UI:
> **Vulnerabilities → Filters → Network Exposure = Public + Severity = Critical/High**

---

## How to Get EPSS Scores

**EPSS = Exploit Prediction Scoring System** — probability (0 to 1) that a CVE will be exploited in the wild within 30 days.

### Source
- **Official API**: `https://api.first.org/data/v1/epss`
- Maintained by **FIRST.org** — free, no auth required

### Query by CVE
```bash
# Single CVE
curl "https://api.first.org/data/v1/epss?cve=CVE-2021-44228"

# Multiple CVEs
curl "https://api.first.org/data/v1/epss?cve=CVE-2021-44228,CVE-2022-22965"
```

### Response
```json
{
  "data": [{
    "cve": "CVE-2021-44228",
    "epss": "0.97565",      ← 97.5% probability of exploitation
    "percentile": "0.99977",
    "date": "2024-01-15"
  }]
}
```
### Thresholds to Use
| EPSS Score | Action |
|---|---|
| > 0.6 (60%) | Treat as actively exploitable — patch immediately |
| 0.3 – 0.6 | High risk — patch within SLA |
| < 0.3 | Standard SLA applies |

---

## How to Get CVSS Scores

**CVSS = Common Vulnerability Scoring System** — base severity score (0–10).

### Source — NVD (National Vulnerability Database)
- **API**: `https://services.nvd.nist.gov/rest/json/cves/2.0`
- Maintained by **NIST** — free, no auth required (API key optional for higher rate limits)

```bash
curl "https://services.nvd.nist.gov/rest/json/cves/2.0?cveId=CVE-2021-44228"
```

### Response (key fields)
```json
{
  "vulnerabilities": [{
    "cve": {
      "id": "CVE-2021-44228",
      "metrics": {
        "cvssMetricV31": [{
          "cvssData": {
            "baseScore": 10.0,        ← CVSS score
            "baseSeverity": "CRITICAL",
            "vectorString": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H"
          }
        }]
      }
    }
  }]
}
```
> **Note**: Both Wiz and Checkmarx already embed CVSS scores in their findings — you may not need to call NVD separately unless enriching a merged dataset.

---

## How to Get the CISA KEV List

**KEV = Known Exploited Vulnerabilities** — CVEs that CISA has confirmed are actively being exploited in the wild. This is your **most actionable** list.

### Source
- **Direct JSON feed**: `https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json`
- Maintained by **CISA** — free, no auth, updated frequently

```bash
curl "https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json"
```

### Key Fields in the Response
```json
{
  "vulnerabilities": [{
    "cveID": "CVE-2021-44228",
    "vendorProject": "Apache",
    "product": "Log4j2",
    "vulnerabilityName": "Apache Log4j2 Remote Code Execution",
    "dateAdded": "2021-12-10",
    "dueDate": "2021-12-24",       ← Federal agencies must fix by this date
    "requiredAction": "Apply updates per vendor instructions",
    "knownRansomwareCampaignUse": "Known"   ← 🚨 Ransomware flag
  }]
}
```

### Use `knownRansomwareCampaignUse` as an Escalation Flag
| Value | Meaning |
|---|---|
| `Known` | Actively used in ransomware — **immediate action** |
| `Unknown` | Exploited but ransomware link unconfirmed |

---

## Putting It All Together — Enrichment Pipeline

```
Wiz Export (networkExposure=Public)
+
Checkmarx Export (Severity=Critical/High)
        ↓
Deduplicate on CVE ID
        ↓
Enrich each CVE:
  ├── EPSS score  → api.first.org
  ├── CVSS score  → NVD API (or use embedded score)
  └── KEV match   → CISA JSON feed
        ↓
Priority Score = Internet Exposed (Wiz)
               + KEV match
               + EPSS > 0.6
               + CVSS ≥ 9.0
        ↓
Auto-create Jira tickets ranked by Priority Score
```

This enrichment can be automated with a simple Python script that pulls from all three APIs nightly and updates your vulnerability backlog automatically.