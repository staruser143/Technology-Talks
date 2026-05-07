Great question! When a file storage API endpoint constructs its path variables and query strings from user input, there are multiple attack surfaces beyond just path traversal. Here's a comprehensive secure design approach:

---

### Attack Surface Overview

```
GET /api/files/{fileId}/download?version=2&format=pdf
              ↑                    ↑          ↑
         path variable          query param  query param
         (user input)           (user input) (user input)
```

Threats include path traversal, parameter tampering, IDOR (Insecure Direct Object Reference), injection, and unauthorized access.

---

## 1. Never Use Raw User Input in File Paths — Use Indirect References

The safest approach is to **never expose real filenames or paths** in the API. Use opaque identifiers instead.

```java
// ❌ UNSAFE — real filename exposed in URL
GET /api/files/download?name=report_2024.pdf

// ✅ SAFE — opaque UUID, no real path info leaked
GET /api/files/550e8400-e29b-41d4-a716-446655440000/download
```

```java
@Service
public class FileStorageService {

    // Map UUID → real file metadata (stored in DB, not derived from input)
    public FileMetadata resolveFile(UUID fileId, String userId) {
        FileMetadata meta = fileRepository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException("File not found"));

        // Ownership check — covered in section 4
        if (!meta.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        return meta;
    }
}
```

```java
// FileMetadata entity — real path never exposed to client
@Entity
public class FileMetadata {
    private UUID id;             // what the client sees
    private String storagePath;  // real path, server-side only
    private String ownerId;
    private String originalName;
    private String mimeType;
    private long fileSize;
}
```

---

## 2. Validate Every Path Variable and Query Parameter

```java
@RestController
@RequestMapping("/api/files")
@Validated
public class FileStorageController {

    private static final Set<String> ALLOWED_FORMATS  = Set.of("pdf", "csv", "xlsx");
    private static final Set<String> ALLOWED_VERSIONS = Set.of("1", "2", "3");

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable UUID fileId,           // UUID type rejects non-UUID input automatically
            @RequestParam(required = false,
                          defaultValue = "1")
                          String version,
            @RequestParam(required = false,
                          defaultValue = "pdf")
                          String format,
            Authentication auth) {

        // 1. Validate query params against allowlists
        if (!ALLOWED_FORMATS.contains(format)) {
            throw new IllegalArgumentException("Invalid format: " + format);
        }

        if (!ALLOWED_VERSIONS.contains(version)) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }

        // 2. Resolve file via UUID — no path construction from user input
        FileMetadata meta = fileStorageService.resolveFile(fileId, auth.getName());

        // 3. Serve file safely
        return fileStorageService.serveFile(meta);
    }
}
```

> Using `UUID` as the `@PathVariable` type means Spring automatically rejects anything that isn't a valid UUID — no extra validation needed for the ID itself.

---

## 3. Canonicalize When Path Construction is Unavoidable

Sometimes you genuinely need to build a path (e.g., subdirectory per user). Always canonicalize:

```java
@Service
public class FileStorageService {

    @Value("${file.storage.base-dir}")
    private String baseDir;

    public File resolveStoragePath(String userId, String fileId) throws IOException {
        // Build path from server-controlled segments — not raw user input
        Path basePath = Paths.get(baseDir).toRealPath();

        // Sanitize userId and fileId individually before joining
        String safeUserId = sanitizeSegment(userId);
        String safeFileId = sanitizeSegment(fileId);

        Path targetPath = basePath
                            .resolve(safeUserId)
                            .resolve(safeFileId)
                            .normalize()
                            .toRealPath();

        // Final canonical check
        if (!targetPath.startsWith(basePath)) {
            throw new SecurityException("Path traversal detected");
        }

        return targetPath.toFile();
    }

    private String sanitizeSegment(String input) {
        // Only allow alphanumeric, hyphen, underscore
        if (!input.matches("^[a-zA-Z0-9_\\-]{1,64}$")) {
            throw new SecurityException("Invalid path segment: " + input);
        }
        return input;
    }
}
```

---

## 4. Prevent IDOR — Enforce Ownership at Every Request

IDOR (Insecure Direct Object Reference) is when a user accesses another user's file by guessing/changing the ID.

```java
@Service
public class FileStorageService {

    public FileMetadata resolveFile(UUID fileId, String requestingUserId) {
        FileMetadata meta = fileRepository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException("File not found"));

        // Always verify ownership — never trust the ID alone
        if (!meta.getOwnerId().equals(requestingUserId)) {
            // Return 404 instead of 403 — don't reveal the file exists
            throw new FileNotFoundException("File not found");
        }

        return meta;
    }

    // For shared/org files — check role-based access
    public FileMetadata resolveSharedFile(UUID fileId, String userId) {
        FileMetadata meta = fileRepository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException("File not found"));

        boolean hasAccess = filePermissionRepository
            .existsByFileIdAndUserId(fileId, userId);

        if (!hasAccess) {
            throw new FileNotFoundException("File not found");
        }

        return meta;
    }
}
```

---

## 5. Validate MIME Type and File Extension on Serve

Never trust stored metadata blindly — re-validate before serving.

```java
@Service
public class FileStorageService {

    private static final Map<String, String> ALLOWED_MIME_TYPES = Map.of(
        "pdf",  "application/pdf",
        "csv",  "text/csv",
        "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "png",  "image/png",
        "jpg",  "image/jpeg"
    );

    public ResponseEntity<Resource> serveFile(FileMetadata meta) throws IOException {
        File file = new File(meta.getStoragePath());

        if (!file.exists()) {
            throw new FileNotFoundException("File not found on disk");
        }

        // Re-detect MIME type from actual file content — not from filename
        String detectedMime = Files.probeContentType(file.toPath());
        String expectedMime = ALLOWED_MIME_TYPES.get(meta.getFileExtension());

        if (!detectedMime.equals(expectedMime)) {
            throw new SecurityException("MIME type mismatch — possible file tampering");
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(detectedMime))
            // attachment forces download — prevents XSS via inline HTML rendering
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + meta.getOriginalName() + "\"")
            // Prevent browsers from MIME sniffing
            .header("X-Content-Type-Options", "nosniff")
            .body(resource);
    }
}
```

---

## 6. Rate Limiting and Audit Logging

```java
// Rate limiting per user via Bucket4j
@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket getBucket(String userId) {
        return buckets.computeIfAbsent(userId, k ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(
                    20, Refill.greedy(20, Duration.ofMinutes(1)))) // 20 req/min
                .build()
        );
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        String userId = ((HttpServletRequest) req).getRemoteUser();
        if (userId != null && !getBucket(userId).tryConsume(1)) {
            ((HttpServletResponse) res).setStatus(429);
            return;
        }
        chain.doFilter(req, res);
    }
}

// Audit logging on every file access
@Service
public class FileStorageService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public FileMetadata resolveFile(UUID fileId, String userId) {
        FileMetadata meta = fileRepository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException("File not found"));

        if (!meta.getOwnerId().equals(userId)) {
            // Log failed access attempts for security monitoring
            auditLog.warn("UNAUTHORIZED_ACCESS attempt by user={} for fileId={}", 
                          userId, fileId);
            throw new FileNotFoundException("File not found");
        }

        auditLog.info("FILE_ACCESS user={} fileId={} file={}", 
                      userId, fileId, meta.getOriginalName());
        return meta;
    }
}
```

---

## Complete Security Architecture

```
Client Request
GET /api/files/{uuid}/download?version=1&format=pdf
        │
        ▼
┌───────────────────────────────────────┐
│         SPRING SECURITY               │
│  • Authentication required            │
│  • JWT / session validation           │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│         RATE LIMITING FILTER          │
│  • Per-user request throttling        │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│            CONTROLLER                 │
│  • UUID type validation (auto)        │
│  • Query param allowlist checks       │
│  • No raw input passed to filesystem  │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│             SERVICE                   │
│  • UUID → real path via DB lookup     │
│  • Ownership / IDOR check             │
│  • Canonical path verification        │
│  • MIME type re-validation            │
│  • Audit logging                      │
└──────────────────┬────────────────────┘
                   │
                   ▼
┌───────────────────────────────────────┐
│          FILE SYSTEM / STORAGE        │
│  • Process runs as non-root           │
│  • Storage dir outside web root       │
│  • Files stored with UUID names       │
└───────────────────────────────────────┘
```

---

## Summary of Secure Practices

| Concern | Approach |
|---|---|
| Path traversal | Canonical path + `startsWith()` check |
| Filename exposure | Opaque UUIDs — never expose real paths |
| IDOR | Ownership check on every request |
| Query param abuse | Allowlist for every param value |
| MIME spoofing | Re-detect MIME from file content |
| Inline XSS | `Content-Disposition: attachment` + `X-Content-Type-Options: nosniff` |
| Brute force | Rate limiting per user |
| Forensics | Audit log every access and rejection |
| Privilege escalation | Run process as non-root OS user |

The **golden rule** is: user input should only ever be used to **look up server-controlled data** — never to construct filesystem paths directly.