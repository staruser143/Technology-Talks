## Why Block RFC 1918 and Loopback Addresses in SSRF

These address ranges represent **private/internal network space** — infrastructure that is intentionally not reachable from the public internet. If your server makes a request to one of these IPs on behalf of an attacker, it acts as a bridge into your private network.

---

### RFC 1918 — Private IP Ranges

| Range | CIDR | Typical Use |
|---|---|---|
| 10.x.x.x | 10.0.0.0/8 | Corporate/cloud internal networks |
| 172.16–31.x.x | 172.16.0.0/12 | Docker networks, VPCs |
| 192.168.x.x | 192.168.0.0/16 | Home/office LANs |

These are **routable only within a private network**. An external attacker cannot directly reach `http://10.0.0.5:8080` from their laptop — but *your server can*, because it lives inside that network.

**What an attacker gains:**
- Access to internal databases (e.g., `http://10.0.1.20:5432`)
- Admin UIs not meant to be public (e.g., `http://192.168.1.1/admin`)
- Microservices with no authentication (they assumed network isolation was enough)
- Internal Kubernetes API server, Redis, Elasticsearch, etc.

---

### Loopback — `127.0.0.0/8`

`127.0.0.1` (localhost) refers to **the server itself**. Services bound to loopback are explicitly designed to be unreachable from any other machine.

**What an attacker gains:**
- Hit `http://127.0.0.1:8080/admin` — a local admin panel with no auth
- Interact with local agent processes (e.g., HashiCorp Vault agent, monitoring daemons)
- Exploit other locally running services that assumed they were safe

---

### Link-Local — `169.254.0.0/16`

This range deserves equal attention. It hosts **cloud instance metadata APIs**:

| Provider | Metadata URL |
|---|---|
| AWS | `http://169.254.169.254/latest/meta-data/` |
| GCP | `http://169.254.169.254/computeMetadata/v1/` |
| Azure | `http://169.254.169.254/metadata/instance` |

These endpoints hand out **IAM credentials, access tokens, and instance configuration** to anyone who can reach them — with no authentication, because they were designed assuming only the legitimate instance would ever call them. An SSRF here can lead to full cloud account compromise.

---

### The Core Principle

> These ranges are dangerous precisely *because* your server has legitimate access to them. The attacker is exploiting trust that was granted to your server, not to them.

Normal internet users can't reach these addresses. Your server can. SSRF turns your server into an **unintentional proxy** into spaces that were protected only by network boundaries — and once those boundaries are bypassed via SSRF, there's often no second layer of defense.