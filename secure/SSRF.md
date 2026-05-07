## Server-Side Request Forgery (SSRF)

SSRF is a web security vulnerability where an attacker tricks a server into making HTTP requests to unintended destinations — including internal services, cloud metadata endpoints, or other external systems — on the attacker's behalf.

### How It Works

The server acts as a proxy. If an application fetches a URL supplied by the user without validation, an attacker can supply a malicious URL pointing to internal infrastructure the attacker couldn't reach directly.

**Example vulnerable code (Node.js):**
```javascript
// Attacker supplies: url=http://169.254.169.254/latest/meta-data/
app.get('/fetch', async (req, res) => {
  const response = await axios.get(req.query.url); // ❌ No validation
  res.send(response.data);
});
```

### Common Attack Targets

- **Cloud metadata APIs** — `http://169.254.169.254/` (AWS, GCP, Azure) to steal IAM credentials
- **Internal services** — databases, admin panels, Kubernetes APIs not exposed publicly
- **localhost** — `http://127.0.0.1:8080/admin` to hit local-only endpoints
- **Internal port scanning** — enumerate services behind a firewall

---

## Remediation

**1. Validate and allowlist URLs**
Only permit specific domains/IPs your app legitimately needs to contact. Reject everything else.
```python
ALLOWED_HOSTS = {"api.example.com", "cdn.example.com"}
parsed = urlparse(user_url)
if parsed.hostname not in ALLOWED_HOSTS:
    raise ValueError("Host not allowed")
```

**2. Block private/reserved IP ranges**
Before making any outbound request, resolve the hostname to an IP and reject RFC 1918 ranges and loopback addresses.
```
Block: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, 127.0.0.0/8, 169.254.0.0/16
```
Watch out for **DNS rebinding** — resolve once and reuse that IP for the actual request.

**3. Use a dedicated egress proxy**
Route all server-initiated outbound HTTP through a proxy (e.g., Squid, AWS VPC egress) that enforces its own allowlist. This separates policy from application code.

**4. Disable unnecessary URL schemes**
If you only need HTTPS, block `file://`, `gopher://`, `ftp://`, `dict://`, etc., which can be abused to read local files or interact with other protocols.

**5. Don't return raw responses to users**
Avoid reflecting the full response body back to the user — this turns a blind SSRF into a fully readable one. Return only what your application logic needs.

**6. Network-level controls**
Even with code-level fixes, apply defense in depth: use cloud security groups / firewall rules to prevent your app servers from reaching the metadata endpoint or internal admin services directly.

**7. Use SSRF-aware HTTP libraries**
Some libraries (e.g., `ssrf-req-filter` for Node, `urllib3` with custom resolvers) have built-in hooks to intercept and validate requests before they're sent.

---

## Quick Reference

| Layer | Control |
|---|---|
| Input | Allowlist domains, validate URL scheme |
| DNS | Resolve & check IP before requesting |
| Network | Block metadata IPs, segment internal services |
| Proxy | Centralized egress with its own allowlist |
| Response | Don't echo raw server responses to users |

SSRF is listed in the **OWASP Top 10 (A10:2021)**, reflecting how critical it's become with cloud-hosted infrastructure.