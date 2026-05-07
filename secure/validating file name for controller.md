Great catch! The controller directly passes `filename` from the request parameter straight to `fileAccessService.getSafeFile()` without any upfront sanitization. While `getSafeFile()` does protect at the service layer, **defense-in-depth** means you should validate as early as possible. Here's the corrected controller:

```java
@RestController
@RequestMapping("/files")
public class FileController {

    private static final String FILENAME_PATTERN = "^[a-zA-Z0-9_\\-\\.]+$"; // allowlist chars

    @Autowired
    private FileAccessService fileAccessService;

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String filename) throws IOException {

        // 1. Null / empty check
        if (filename == null || filename.isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }

        // 2. Reject absolute paths immediately
        if (Paths.get(filename).isAbsolute()) {
            throw new SecurityException("Absolute paths are not allowed");
        }

        // 3. Allowlist character check — only permit safe characters
        if (!filename.matches(FILENAME_PATTERN)) {
            throw new SecurityException("Invalid characters in filename");
        }

        // 4. Reject null byte injection (%00)
        if (filename.contains("\0")) {
            throw new SecurityException("Null byte detected in filename");
        }

        // 5. Reject obvious traversal patterns as an extra early signal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Path traversal attempt detected");
        }

        // 6. Only now pass to the service — which still does its own canonical check
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

### What Each Check Defends Against

| Check | Attack it blocks |
|---|---|
| Null / blank check | Empty input causing unexpected behaviour |
| `isAbsolute()` | `/etc/passwd`, `C:\Windows\...` |
| Regex allowlist | Any character not in `[a-zA-Z0-9_.-]` |
| Null byte (`\0`) | `file.txt%00.jpg` — tricks extension checks |
| `..` / `/` / `\` | Relative traversal like `../../etc/shadow` |

---

### Key Principle

The service layer's canonical path check is your **safety net**, but the controller should be your **first gate**. Think of it as:

```
Controller  →  reject clearly bad input early (fail fast)
Service     →  enforce base directory as the final guarantee
```

Both layers should always be present — if someone calls `fileAccessService` from another place in the codebase (a scheduler, another controller, etc.), the service-level check still protects it independently.