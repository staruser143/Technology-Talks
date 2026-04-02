Absolutely — CloudFront + Lambda\@Edge is a really elegant pattern for **domain-based redirects** with **near-zero ops**. Below is a deep, practical explanation of *how* it works, what you configure, and what to watch out for.

***

# 1) What CloudFront + Lambda\@Edge is doing (in plain terms)

### **CloudFront**

*   Acts as the **global front door** for all 10 domains.
*   Terminates **HTTPS** using an **ACM certificate**.
*   Accepts **HTTP** and can automatically **redirect HTTP → HTTPS**.
*   Routes every request through an edge location close to the visitor.

### **Lambda\@Edge**

*   Runs **your redirect logic at the CloudFront edge**, *before* the request goes to any backend.
*   It can:
    *   Inspect the incoming request (especially the **Host header**)
    *   Decide the target URL
    *   Immediately return a **301/302 response** with a `Location` header

✅ Result: **No servers, no ALB, no ECS**, and usually **no origin calls** at all.

***

# 2) The request flow end-to-end

Let’s say you own these domains:

*   `promo1.com` → redirect to `https://www.company.com/offers/summer`
*   `promo2.com` → redirect to `https://campaign.company.com/landingA`

### Visitor hits:

`https://promo1.com`

### The flow looks like this:

1.  **Route 53** resolves `promo1.com` → **CloudFront distribution** (Alias A/AAAA record).
2.  Browser connects over **HTTPS** to CloudFront edge.
3.  CloudFront invokes **Lambda\@Edge** (Viewer Request trigger).
4.  Lambda reads `Host: promo1.com`.
5.  Lambda looks up redirect destination from the mapping (your JSON).
6.  Lambda responds:

```http
HTTP/1.1 301 Moved Permanently
Location: https://www.company.com/offers/summer
Cache-Control: max-age=300
```

7.  Browser follows the redirect.

**Important:** Because Lambda returns a response directly, **CloudFront never needs to contact an origin**.

***

# 3) Why “Viewer Request” trigger is the best choice

Lambda\@Edge can run at different points in the CloudFront lifecycle.

For redirects, you typically choose:

✅ **Viewer Request**  
Because it runs **as soon as CloudFront receives the request**, *before caching logic* and *before origin*.

This gives you:

*   Fastest response time
*   No origin needed
*   Full control over redirect behavior (301 vs 302, preserve path/query, etc.)

***

# 4) How the JSON mapping is used

You said:

> “All domains and target URLs are defined in a JSON document.”

You have two common ways to use that JSON with least ops:

***

## Option A (simplest): Embed JSON map in Lambda code

Good when:

*   Only \~10 domains
*   Redirects don’t change frequently

Pros:

*   No S3/SSM dependencies
*   Fast, simple, robust

Cons:

*   Updating mapping requires redeploying Lambda\@Edge (and replication to edge takes time)

***

## Option B (more flexible): Store JSON in S3 and cache it

Good when:

*   Marketing changes redirects often
*   Want to update without redeploying function

Typical pattern:

*   JSON file in **S3**
*   Lambda\@Edge loads it **occasionally**, stores in memory (cache), re-checks periodically

⚠️ Reality check: Lambda\@Edge has limitations vs normal Lambda (especially around AWS SDK calls and permissions), and it runs in CloudFront edge context. You can still do this, but it can be trickier than embedding config.

**Best low-ops compromise:**  
If changes are infrequent (common for 10 domains), embed mapping in code.

***

# 5) HTTP + HTTPS handling (important exam nuance)

### Requirement: “Redirect service must accept HTTP and HTTPS”

With CloudFront:

*   You can configure CloudFront viewer protocol policy to:
    *   **Redirect HTTP → HTTPS**, or
    *   Allow both HTTP and HTTPS

**Typical best practice:**

1.  CloudFront config: **Redirect HTTP to HTTPS**
2.  Lambda\@Edge handles redirect destination

This results in:

*   Visitors who type `http://promo1.com` get upgraded to HTTPS
*   Then they get redirected to the target URL

***

# 6) Multi-domain HTTPS: Certificate and CloudFront aliases

To serve HTTPS for each domain, CloudFront needs:

### ✅ Alternate domain names (CNAMEs)

Add all 10 domains (and optionally `www.` versions) into CloudFront as “Alternate Domain Names”.

### ✅ ACM certificate (must be in us-east-1)

CloudFront requires the ACM certificate to be issued in **N. Virginia (us-east-1)**.

You can use:

*   A wildcard cert (e.g., `*.promo-brand.com`) if domains are subdomains
*   Or a SAN cert containing all 10 domains (if they’re unrelated)

***

# 7) Route 53 setup

For each domain hosted zone in Route 53:

*   Create an **A record (Alias)**
*   Target = CloudFront distribution

This makes DNS routing fully managed and eliminates IP-based records.

***

# 8) Redirect behavior details (what your function should decide)

A good redirect function usually handles:

### a) Preserve path/query

Example:

*   Request: `https://promo1.com/deals?src=google`
*   Redirect to: `https://www.company.com/offers/summer/deals?src=google`

You decide whether to:

*   keep path + query
*   drop them
*   map only host-level redirects

### b) Choose 301 vs 302

*   **301**: permanent (better for SEO, cached by browsers more aggressively)
*   **302**: temporary (good for short campaigns)

Marketing often wants **302** for campaigns; architects often prefer **301** for stable brand redirects.

### c) Add cache headers

You can include:

*   `Cache-Control: max-age=300`  
    so CloudFront and browsers cache redirect responses for a short period.

***

# 9) Operational characteristics (why this is “least effort”)

✅ **No infrastructure to patch**
✅ **No scaling**
✅ **High availability built-in**
✅ **Global performance**
✅ **CloudFront logs/metrics available**
✅ **Works cleanly with Route 53 DNS**

The only “ops” tasks are:

*   Updating redirect mappings (code or JSON file)
*   Certificate renewal (ACM mostly automates this)
*   Monitoring logs if needed

***

# 10) Gotchas / limitations to be aware of

Here are common real-world and exam gotchas:

1.  **Lambda\@Edge deployment time**
    *   Updates can take minutes to replicate globally.

2.  **Certificate region**
    *   ACM cert must be in `us-east-1` for CloudFront.

3.  **Multiple environments**
    *   If you have dev/test/prod, you may want separate CloudFront distributions.

4.  **Redirect loops**
    *   Ensure your target URL does not point back to the same domain.

5.  **Caching**
    *   If you cache redirects too aggressively, changes won’t propagate quickly. Keep TTL modest.

***

# 11) Minimal reference implementation (conceptual)

### Redirect logic (pseudo / conceptual)

*   Determine the incoming domain from `Host`
*   Lookup target URL in map
*   Construct redirect URL
*   Return HTTP response with `Location`

