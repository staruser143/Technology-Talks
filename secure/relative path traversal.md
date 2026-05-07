Yes! Relative path traversal is actually the **more classic and common** variant. Instead of supplying an absolute path, the attacker uses `../` sequences to "climb up" out of the intended directory.

### How It Works

```
Base directory:  /var/www/uploads/
Attacker input:  ../../../../etc/passwd
Resolved path:   /etc/passwd   ← escapes the base dir entirely
```

---

### Attack Variations to Know

| Input | Technique |
|---|---|
| `../../etc/passwd` | Basic traversal |
| `..%2F..%2Fetc%2Fpasswd` | URL-encoded `/` |
| `..%252F..%252Fetc%252Fpasswd` | Double URL-encoded |
| `....//....//etc/passwd` | Stripped traversal bypass |
| `..\/..\/etc/passwd` | Mixed separators |
| `file.txt%00.jpg` | Null byte + traversal |

---

## Remediation in Spring Boot

### 1. Controller — Validate Input Early

```java
@RestController
@RequestMapping("/files")
public class FileController {

    private static final String FILENAME_PATTERN = "^[a-zA-Z0-9_\\-\\.]+$";

    @Autowired
    private FileAccessService fileAccessService;

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String filename) throws IOException {

        // 1. Null / blank check
        if (filename == null || filename.isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }

        // 2. Allowlist character check — blocks most traversal chars upfront
        if (!filename.matches(FILENAME_PATTERN)) {
            throw new SecurityException("Invalid characters in filename");
        }

        // 3. Explicitly reject traversal sequences (even encoded forms)
        if (containsTraversalSequences(filename)) {
            throw new SecurityException("Path traversal attempt detected");
        }

        // 4. Null byte injection
        if (filename.contains("\0")) {
            throw new SecurityException("Null byte detected in filename");
        }

        File safeFile = fileAccessService.getSafeFile(filename);

        if (!safeFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(safeFile);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + safeFile.getName() + "\"")
            .body(resource);
    }

    private boolean containsTraversalSequences(String input) {
        // Decode URL encoding before checking — catches %2F, %252F etc.
        String decoded;
        try {
            decoded = java.net.URLDecoder.decode(input, "UTF-8");
        } catch (Exception e) {
            return true; // treat malformed encoding as suspicious
        }

        return decoded.contains("..")
            || decoded.contains("/")
            || decoded.contains("\\")
            || decoded.contains("%2f")   // encoded /
            || decoded.contains("%5c");  // encoded \
    }
}
```

---

### 2. Service — Canonicalize + Base Directory Check (Critical Safety Net)

This is your **ultimate defense** — it catches traversal attempts that slip past the controller, regardless of encoding tricks.

```java
@Service
public class FileAccessService {

    @Value("${file.upload.base-dir}")
    private String baseDir;

    public File getSafeFile(String userInput) throws IOException {

        Path basePath = Paths.get(baseDir).toRealPath();

        // normalize() collapses ../ sequences
        // toRealPath() resolves symlinks — prevents symlink-based traversal
        Path targetPath = basePath
                            .resolve(userInput)
                            .normalize()
                            .toRealPath();

        // Final gate: resolved path must still be inside base directory
        if (!targetPath.startsWith(basePath)) {
            throw new SecurityException(
                "Access denied: path resolves outside the allowed directory");
        }

        return targetPath.toFile();
    }
}
```

> `normalize()` turns `uploads/../../etc/passwd` into `/etc/passwd` and then `startsWith(basePath)` rejects it cleanly.

---

### 3. Bean Validation — Declarative Approach

You can enforce filename rules declaratively using `@Pattern` on the request param, keeping the controller clean.

```java
// Custom annotation
@Constraint(validatedBy = {})
@Pattern(regexp = "^[a-zA-Z0-9_\\-\\.]{1,255}$",
         message = "Filename contains invalid characters")
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeFilename {
    String message() default "Invalid filename";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Use in controller
@GetMapping("/download")
public ResponseEntity<Resource> downloadFile(
        @RequestParam @SafeFilename String filename) throws IOException {
    // Input is already validated by the annotation
    File safeFile = fileAccessService.getSafeFile(filename);
    ...
}
```

Enable validation in your config:
```java
@Configuration
public class ValidationConfig {
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
```

---

### 4. Unit Tests — Verify Your Defenses Hold

Always test traversal attempts explicitly.

```java
@SpringBootTest
class FileAccessServiceTest {

    @Autowired
    private FileAccessService fileAccessService;

    @ParameterizedTest
    @ValueSource(strings = {
        "../../etc/passwd",               // basic relative traversal
        "..%2F..%2Fetc%2Fpasswd",         // URL encoded
        "..%252F..%252Fetc%252Fpasswd",   // double encoded
        "....//....//etc/passwd",         // stripped traversal
        "/etc/passwd",                    // absolute path
        "valid/../../../etc/passwd",      // traversal after valid segment
        "file.txt\0.jpg"                  // null byte injection
    })
    void shouldRejectTraversalAttempts(String maliciousInput) {
        assertThrows(SecurityException.class,
            () -> fileAccessService.getSafeFile(maliciousInput));
    }

    @Test
    void shouldAllowLegitimateFile() throws IOException {
        assertDoesNotThrow(() -> fileAccessService.getSafeFile("report.pdf"));
    }
}
```

---

## Complete Defense-in-Depth Summary

```
HTTP Request
     │
     ▼
┌─────────────────────────────────┐
│         CONTROLLER              │
│  • Null / blank check           │
│  • Allowlist regex              │  ← Fail fast, reject obvious attacks
│  • Traversal sequence check     │
│  • Null byte check              │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│      BEAN VALIDATION            │
│  • @SafeFilename annotation     │  ← Declarative, reusable
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│           SERVICE               │
│  • resolve() + normalize()      │  ← Canonical path resolution
│  • toRealPath()                 │  ← Resolves symlinks
│  • startsWith(basePath) check   │  ← Final, non-bypassable gate
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│      GLOBAL EXCEPTION HANDLER   │
│  • Never leak real paths        │  ← Return 403, log internally
│  • Log all blocked attempts     │
└─────────────────────────────────┘
```

The **canonical path + `startsWith()` check in the service** is the one defense you can never skip — everything else is layered on top to fail fast and reduce attack surface early.