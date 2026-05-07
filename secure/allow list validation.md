Yes! The allowlist is actually the **strongest and most important** check. Here's why each layer alone is insufficient, and why all three work best together.

---

### Why Each Layer Alone is Insufficient

#### Without Allowlist — Blocklist Can Be Bypassed
Blocklists try to anticipate every bad input. Attackers only need to find **one encoding or variant you missed**:

```
You block:   ../
Attacker tries: ..%2F          ← URL encoded
                ..%252F        ← double encoded
                ..%c0%af       ← overlong UTF-8 encoding
                ....//         ← stripped traversal
                ..\            ← Windows separator
                %2e%2e%2f      ← fully encoded
```
No matter how many sequences you block, a new bypass might exist.

#### Without Canonical Check — Allowlist Can Be Bypassed
Even with an allowlist, if you don't canonicalize:

```java
// Allowlist passes "report.pdf" ✓
// But attacker sends: "report.pdf/../../etc/passwd"
// Naive join:  /var/www/uploads/report.pdf/../../etc/passwd
//              → resolves to /etc/passwd  ← BYPASSED
```

---

### All Three Layers Together

```java
// ─── Controller ────────────────────────────────────────────
private static final Set<String> ALLOWED_FILES = Set.of(
    "report.pdf",
    "invoice.pdf",
    "manual.pdf"
);

private boolean isInvalidFilename(String input) {
    String decoded;
    try {
        decoded = URLDecoder.decode(input, StandardCharsets.UTF_8);
    } catch (Exception e) {
        return true;
    }

    return input.isBlank()
        || Paths.get(decoded).isAbsolute()           // absolute path check
        || decoded.contains("..")                    // relative traversal check
        || decoded.contains("/")
        || decoded.contains("\\")
        || decoded.contains("\0")
        || !input.matches("^[a-zA-Z0-9_\\-.]+$");   // character allowlist
}

@GetMapping("/download")
public ResponseEntity<Resource> downloadFile(
        @RequestParam String filename) throws IOException {

    // Layer 1 — Character & pattern validation
    if (isInvalidFilename(filename)) {
        throw new SecurityException("Invalid filename");
    }

    // Layer 2 — Allowlist: only explicitly permitted files allowed
    if (!ALLOWED_FILES.contains(filename)) {
        throw new SecurityException("File not permitted");
    }

    // Layer 3 — Canonical path check in service (final gate)
    File safeFile = fileAccessService.getSafeFile(filename);
    ...
}
```

---

### What Each Layer Contributes

```
Input: "report.pdf/../../etc/passwd"

Layer 1 — isInvalidFilename()
  • contains "/"  → BLOCKED ✗
  • (stops here — never reaches layer 2)

Input: "evil.sh"

Layer 1 — isInvalidFilename()
  • passes regex  → OK
Layer 2 — Allowlist
  • not in ALLOWED_FILES → BLOCKED ✗

Input: "report.pdf"  (legitimate)

Layer 1 — passes ✓
Layer 2 — in ALLOWED_FILES ✓
Layer 3 — resolves inside basePath ✓ → FILE SERVED
```

---

### The Right Mental Model

Think of the three layers as having distinct jobs — not overlapping redundancy:

| Layer | Type | Job | What it stops |
|---|---|---|---|
| **Character validation** | Blocklist | Reject clearly malicious patterns early | Encoded traversal, null bytes, separators |
| **Allowlist** | Allowlist | Only permit known-good filenames | Unknown files, zero-days, logic flaws |
| **Canonical path check** | Invariant | Enforce base directory as mathematical guarantee | Everything that slips past the above two |

---

### Key Takeaway

```
Blocklist alone   →  can be bypassed with new encodings
Allowlist alone   →  can be bypassed if canonicalization is missing
Canonical alone   →  safe but no early rejection, harder to audit

All three together →  no known bypass exists
        ↑
   This is what you want
```

The allowlist is the most **semantically correct** check — it says *"I know exactly what files should be served"* rather than trying to guess what's dangerous. Combined with canonical path resolution as the final mathematical guarantee, you get a defense that is both strict and bypass-proof.