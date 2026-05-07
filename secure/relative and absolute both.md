Yes, absolutely! You should **always validate both simultaneously** because:

1. An attacker won't announce which technique they're using
2. Some payloads **combine both** (e.g., `/var/www/../../etc/passwd`)
3. Validating only one leaves a clear bypass route

The good news is — your defenses **naturally cover both at the same time** without needing separate code paths.

---

### One Payload Can Be Both

```
/var/www/uploads/../../../../etc/passwd
     ↑                    ↑
  absolute start     relative traversal
```

---

### How Each Layer Covers Both Simultaneously

#### Controller — Single Check Catches Both

```java
private boolean isInvalidFilename(String input) {
    String decoded;
    try {
        // Decode first to catch %2F, %252F etc.
        decoded = URLDecoder.decode(input, StandardCharsets.UTF_8);
    } catch (Exception e) {
        return true; // malformed encoding = suspicious
    }

    return input.isBlank()
        || Paths.get(decoded).isAbsolute()          // catches absolute paths
        || decoded.contains("..")                   // catches relative traversal
        || decoded.contains("/")                    // catches path separators
        || decoded.contains("\\")                   // catches Windows separators
        || decoded.contains("\0")                   // catches null byte injection
        || !input.matches("^[a-zA-Z0-9_\\-.]+$");  // allowlist — rejects everything else
}
```

```java
@GetMapping("/download")
public ResponseEntity<Resource> downloadFile(
        @RequestParam String filename) throws IOException {

    // One call — blocks both absolute AND relative traversal
    if (isInvalidFilename(filename)) {
        throw new SecurityException("Invalid filename");
    }

    File safeFile = fileAccessService.getSafeFile(filename);
    ...
}
```

---

#### Service — `normalize()` + `startsWith()` is the Universal Final Gate

This single check is what makes both attacks impossible regardless of input:

```java
public File getSafeFile(String userInput) throws IOException {
    Path basePath   = Paths.get(baseDir).toRealPath();
    Path targetPath = basePath.resolve(userInput).normalize().toRealPath();

    // This ONE check defeats both attack types:
    //   absolute: /etc/passwd         → doesn't start with basePath → rejected
    //   relative: ../../etc/passwd    → normalizes to /etc/passwd   → rejected
    //   combined: /base/../../etc/..  → normalizes + resolves       → rejected
    if (!targetPath.startsWith(basePath)) {
        throw new SecurityException("Access denied");
    }

    return targetPath.toFile();
}
```

---

### Why `startsWith(basePath)` Defeats Everything

```
Base dir:  /var/www/uploads

Attack 1 — Absolute:
  resolve("/etc/passwd")       → /etc/passwd
  startsWith("/var/www/uploads") → false ✗ BLOCKED

Attack 2 — Relative:
  resolve("../../etc/passwd")  → /var/www/uploads/../../etc/passwd
  normalize()                  → /etc/passwd
  startsWith("/var/www/uploads") → false ✗ BLOCKED

Attack 3 — Combined:
  resolve("/var/www/../../etc/passwd")
  normalize()                  → /etc/passwd
  startsWith("/var/www/uploads") → false ✗ BLOCKED

Legitimate file:
  resolve("report.pdf")        → /var/www/uploads/report.pdf
  startsWith("/var/www/uploads") → true ✓ ALLOWED
```

---

### Think of It as Two Walls, Not Two Separate Problems

```
┌──────────────────────────────────────────────────────┐
│              CONTROLLER (First Wall)                 │
│                                                      │
│   isInvalidFilename() blocks:                        │
│   • Absolute paths  (/etc/passwd)                    │
│   • Relative seqs   (../../)                         │
│   • Encoded variants (%2F, %252F)                    │
│   • Null bytes      (\0)                             │
│   • Non-allowlisted chars                            │
└───────────────────────┬──────────────────────────────┘
                        │ Only clean filenames pass
                        ▼
┌──────────────────────────────────────────────────────┐
│               SERVICE (Second Wall)                  │
│                                                      │
│   normalize() + toRealPath() + startsWith()          │
│   • Catches anything that slipped through            │
│   • Works against ALL path types unconditionally     │
│   • Cannot be bypassed regardless of encoding        │
└──────────────────────────────────────────────────────┘
```

The **service layer's canonical check is the single source of truth** — it doesn't need to know whether an attack was absolute or relative. If the resolved path isn't inside the base directory, it's rejected. Period. The controller just helps you fail fast and reduce noise.