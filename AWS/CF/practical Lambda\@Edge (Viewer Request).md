Sure — here’s a **practical Lambda\@Edge (Viewer Request)** sample in **Node.js** that does **host-based redirects**, optionally **preserves path + query**, and returns a **301/302** without ever hitting an origin.

> ✅ Use this by attaching it to your CloudFront distribution’s **Viewer Request** event.

***

## ✅ Sample Lambda\@Edge (Node.js) — Domain → URL Redirect (JSON map)

```js
'use strict';

/**
 * Lambda@Edge — Viewer Request
 * Redirect based on Host header using a JSON mapping.
 *
 * Notes:
 * - Attach to CloudFront "Viewer Request" event.
 * - Return a response directly (no origin call).
 * - Works for both HTTP/HTTPS (CloudFront handles TLS).
 */

// Domain → target base URL mapping (from your JSON document)
const REDIRECT_MAP = {
  "promo1.com": "https://www.company.com/offers/summer",
  "promo2.com": "https://campaign.company.com/landingA",
  "www.promo1.com": "https://www.company.com/offers/summer"
};

// Behavior toggles
const DEFAULT_STATUS_CODE = 302;          // 301 for permanent, 302 for temporary
const PRESERVE_PATH_AND_QUERY = true;     // set false to redirect only to base URL
const CACHE_CONTROL_SECONDS = 300;        // reduce if marketing changes frequently

exports.handler = async (event) => {
  const request = event.Records[0].cf.request;

  // Host header is lowercase in CloudFront request headers object
  const hostHeader = request.headers.host && request.headers.host[0] && request.headers.host[0].value;
  const host = (hostHeader || "").toLowerCase();

  // Lookup target base URL for this domain
  const targetBase = REDIRECT_MAP[host];

  // If no mapping found, you can:
  // 1) pass through to origin, OR
  // 2) return 404, OR
  // 3) redirect to a default page
  if (!targetBase) {
    // Option A: pass-through (if you have a default origin)
    return request;

    // Option B: return 404
    // return {
    //   status: '404',
    //   statusDescription: 'Not Found',
    //   headers: {
    //     'content-type': [{ key: 'Content-Type', value: 'text/plain; charset=utf-8' }]
    //   },
    //   body: 'Unknown domain'
    // };
  }

  // Build redirect URL
  // request.uri starts with '/', and querystring does NOT include '?'
  const uri = request.uri || "/";
  const qs = request.querystring ? `?${request.querystring}` : "";

  let location = targetBase;

  if (PRESERVE_PATH_AND_QUERY) {
    // Avoid double slashes when concatenating
    const base = targetBase.endsWith("/") ? targetBase.slice(0, -1) : targetBase;
    const path = uri.startsWith("/") ? uri : `/${uri}`;
    location = `${base}${path}${qs}`;
  }

  const status = String(DEFAULT_STATUS_CODE);
  const statusDescription = (DEFAULT_STATUS_CODE === 301) ? "Moved Permanently" : "Found";

  // Return redirect response (no origin hit)
  return {
    status,
    statusDescription,
    headers: {
      location: [{ key: "Location", value: location }],
      "cache-control": [{ key: "Cache-Control", value: `max-age=${CACHE_CONTROL_SECONDS}` }],
      // Optional security headers
      "strict-transport-security": [{ key: "Strict-Transport-Security", value: "max-age=31536000; includeSubDomains; preload" }]
    }
  };
};
```

***

# How to Use This in CloudFront (Quick Steps)

1.  **Create a Lambda function** in **us-east-1 (N. Virginia)** (Lambda\@Edge requirement).
2.  Runtime: **Node.js 18/20** (whatever your org allows).
3.  Deploy/publish a **version** (Lambda\@Edge requires a *version*, not `$LATEST`).
4.  In **CloudFront distribution → Behaviors → Lambda function associations**:
    *   Event type: ✅ **Viewer Request**
    *   Attach that **published version**
5.  In CloudFront:
    *   **Alternate domain names (CNAMEs)**: add all 10 domains
    *   Attach **ACM certificate** (in us-east-1) for those domains
    *   Viewer protocol policy:
        *   Either **Redirect HTTP to HTTPS** (recommended), or allow both

***

# Common Enhancements (If You Want It More “Production”)

### 1) Normalize `www.`

If marketing may register both `domain.com` and `www.domain.com`, you can normalize:

```js
const normalizedHost = host.startsWith("www.") ? host.slice(4) : host;
```

Then store only non-www keys.

### 2) Per-domain status code / rules

Extend JSON map:

```json
{
  "promo1.com": { "url": "https://a.com", "code": 301, "preservePath": true },
  "promo2.com": { "url": "https://b.com", "code": 302, "preservePath": false }
}
```

### 3) Default catch-all redirect

If no mapping found, redirect to home page instead of pass-through.

***
