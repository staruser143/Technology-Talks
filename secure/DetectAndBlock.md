## Detecting and Blocking Private/Loopback Addresses in SSRF Prevention

The core challenge is that **you can't just check the URL string** — you must resolve the hostname to an IP and validate that IP. Attackers can bypass string checks using creative encodings and DNS tricks.

---

### Why String Matching Alone Fails

Attackers can encode `127.0.0.1` in many ways that all resolve to the same address:
```
http://127.0.0.1          # obvious
http://2130706433         # decimal encoding
http://0x7f000001         # hex encoding
http://0177.0.0.1         # octal encoding
http://127.1              # shorthand
http://[::1]              # IPv6 loopback
http://localtest.me       # public DNS that resolves to 127.0.0.1
```
All of these bypass naive blocklists. You must **resolve first, then check the IP**.

---

### The Correct Detection Flow

```
User-supplied URL
       │
       ▼
1. Parse the URL → extract hostname
       │
       ▼
2. Resolve hostname → IP address (DNS lookup)
       │
       ▼
3. Check IP against blocked ranges
       │
      / \
   BLOCK  ALLOW
           │
           ▼
4. Make HTTP request using the RESOLVED IP
   (not the hostname — prevents DNS rebinding)
```

> **Step 4 is critical.** If you resolve, validate, then re-resolve during the actual request, an attacker can use **DNS rebinding** to swap the IP between your check and your request.

---

### Implementation Examples

**Python**
```python
import socket
import ipaddress
import urllib.parse
import requests

BLOCKED_NETWORKS = [
    ipaddress.ip_network("10.0.0.0/8"),
    ipaddress.ip_network("172.16.0.0/12"),
    ipaddress.ip_network("192.168.0.0/16"),
    ipaddress.ip_network("127.0.0.0/8"),
    ipaddress.ip_network("169.254.0.0/16"),  # link-local / metadata
    ipaddress.ip_network("::1/128"),          # IPv6 loopback
    ipaddress.ip_network("fc00::/7"),         # IPv6 private
]

def is_safe_url(url: str) -> tuple[bool, str]:
    parsed = urllib.parse.urlparse(url)

    # Only allow http/https
    if parsed.scheme not in ("http", "https"):
        return False, f"Scheme '{parsed.scheme}' not allowed"

    hostname = parsed.hostname
    if not hostname:
        return False, "No hostname found"

    try:
        # Resolve to IP
        ip_str = socket.gethostbyname(hostname)
        ip = ipaddress.ip_address(ip_str)
    except socket.gaierror:
        return False, "DNS resolution failed"

    # Check against blocked ranges
    for network in BLOCKED_NETWORKS:
        if ip in network:
            return False, f"IP {ip} is in blocked range {network}"

    return True, ip_str  # Return resolved IP for use in request

def safe_fetch(url: str):
    safe, result = is_safe_url(url)
    if not safe:
        raise ValueError(f"Blocked: {result}")

    # Use the resolved IP directly to prevent DNS rebinding
    parsed = urllib.parse.urlparse(url)
    safe_url = url.replace(parsed.hostname, result)
    return requests.get(safe_url, headers={"Host": parsed.hostname}, timeout=5)
```

**Node.js**
```javascript
const dns = require("dns").promises;
const ipaddr = require("ipaddr.js"); // npm install ipaddr.js

const BLOCKED_CIDRS = [
  "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16",
  "127.0.0.0/8", "169.254.0.0/16", "::1/128", "fc00::/7"
].map(cidr => ipaddr.parseCIDR(cidr));

async function isSafeUrl(inputUrl) {
  const parsed = new URL(inputUrl);

  if (!["http:", "https:"].includes(parsed.protocol)) {
    throw new Error(`Scheme not allowed: ${parsed.protocol}`);
  }

  const { address } = await dns.lookup(parsed.hostname);
  const ip = ipaddr.parse(address);

  for (const cidr of BLOCKED_CIDRS) {
    if (ip.match(cidr)) {
      throw new Error(`Blocked IP range: ${address}`);
    }
  }

  return address; // Use this resolved IP for the actual request
}
```

---

### Edge Cases You Must Handle

| Edge Case | Risk | Fix |
|---|---|---|
| **DNS rebinding** | IP changes between check and request | Use resolved IP directly in the request |
| **IPv6** | `::1`, `fc00::/7` bypass IPv4 checks | Validate both IPv4 and IPv6 |
| **Decimal/hex/octal IPs** | Bypass string blocklists | Always resolve via DNS/socket, never parse manually |
| **Redirects** | Server redirects to internal IP | Disable redirects or re-validate each redirect destination |
| **0.0.0.0** | Resolves to localhost on some systems | Add `0.0.0.0/8` to blocklist |
| **Multiple DNS answers** | One safe IP returned, another used | Check *all* resolved IPs, block if any is private |

---

### Handling Redirects Safely

```python
def safe_fetch_no_redirects(url):
    # Fetch without following redirects
    response = requests.get(url, allow_redirects=False, timeout=5)

    # If redirected, validate the new location before following
    if response.is_redirect:
        new_url = response.headers.get("Location")
        safe, _ = is_safe_url(new_url)
        if not safe:
            raise ValueError(f"Redirect to unsafe URL: {new_url}")
        return safe_fetch_no_redirects(new_url)

    return response
```

---

### Use a Battle-Tested Library

Don't roll your own in production — use libraries specifically built for this:

| Language | Library |
|---|---|
| Python | `ssrfcheck`, `urllib3` with custom resolvers |
| Node.js | `ssrf-req-filter`, `axios` + custom adapter |
| Go | `safecurl` |
| Java | Custom `HttpClient` with IP validation interceptor |

These handle encoding tricks, IPv6, redirect chains, and DNS rebinding out of the box.