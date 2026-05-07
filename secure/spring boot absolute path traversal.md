## Absolute Path Traversal Remediation in Spring Boot

### 1. Validate & Reject Absolute Paths (Basic Check)

```java
import java.nio.file.Path;
import java.nio.file.Paths;

public void validateFilename(String userInput) {
    Path inputPath = Paths.get(userInput);
    
    // Reject absolute paths
    if (inputPath.isAbsolute()) {
        throw new SecurityException("Absolute paths are not allowed");
    }
}
```

---

### 2. Canonicalize + Enforce Base Directory (Core Defense)

This is the most important pattern — always resolve the real path and verify it stays within your base directory.

```java
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileAccessService {

    private static final String BASE_DIR = "/var/www/uploads";

    public File getSafeFile(String userInput) throws IOException {
        // Resolve the canonical (real) path — resolves symlinks, normalizes ../
        Path basePath   = Paths.get(BASE_DIR).toRealPath();
        Path targetPath = basePath.resolve(userInput).normalize().toRealPath();

        // Enforce: resolved path must be inside the base directory
        if (!targetPath.startsWith(basePath)) {
            throw new SecurityException("Access denied: path is outside the allowed directory");
        }

        return targetPath.toFile();
    }
}
```

> `toRealPath()` resolves symlinks. `normalize()` collapses `..` and `.` segments. Together they eliminate both relative and absolute traversal tricks.

---

### 3. Allowlist Approach (Safest)

Never use raw user input as a filename. Map user-controlled keys to server-controlled filenames.

```java
@Service
public class FileAccessService {

    private static final Map<String, String> ALLOWED_FILES = Map.of(
        "invoice",  "invoice_2024.pdf",
        "report",   "annual_report.pdf",
        "manual",   "user_manual.pdf"
    );

    private static final String BASE_DIR = "/var/www/uploads";

    public File getAllowedFile(String key) {
        String filename = ALLOWED_FILES.get(key);
        if (filename == null) {
            throw new IllegalArgumentException("File not found: " + key);
        }
        return new File(BASE_DIR, filename);
    }
}
```

---

### 4. Spring MVC Controller — Putting It Together

```java
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileAccessService fileAccessService;

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String filename) throws IOException {

        // Will throw SecurityException if path is unsafe
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
}
```

---

### 5. Global Exception Handling

Catch security violations centrally so they never leak stack traces to the client.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex) {
        // Log internally, never expose the real reason to the client
        log.warn("Path traversal attempt blocked: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body("Access denied");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body("Invalid file request");
    }
}
```

---

### 6. Spring Security — Restrict File Endpoints

Add an extra layer by locking down file download endpoints via Spring Security config.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/files/**").authenticated()  // require login
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());  // use CSRF tokens in production

        return http.build();
    }
}
```

---

### 7. application.properties — Externalize the Base Directory

```properties
# Never hardcode paths — configure them externally
file.upload.base-dir=/var/www/uploads
file.upload.max-size=10MB
```

```java
@Value("${file.upload.base-dir}")
private String baseDir;
```

---

## Defense-in-Depth Summary

| Layer | Technique |
|---|---|
| **Input validation** | Reject absolute paths, reject null bytes (`%00`) |
| **Path canonicalization** | `normalize()` + `toRealPath()` + `startsWith()` check |
| **Allowlisting** | Map user keys → server-controlled filenames |
| **Access control** | Spring Security authentication on file endpoints |
| **Error handling** | `@RestControllerAdvice` — never leak internal paths |
| **Least privilege** | Run Spring Boot process as a non-root OS user |
| **Config** | Externalize base directory via `application.properties` |

The **canonicalize + base directory check** (layer 2) is your most critical line of defense — always include it even when using allowlists.