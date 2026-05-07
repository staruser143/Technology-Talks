## Incorrect Permission Assignment for File System Resources (CWE-732)

This vulnerability occurs when an application assigns overly permissive access rights to files or directories, allowing unauthorized users or processes to read, modify, or execute sensitive resources.

### How It Manifests in Spring Boot

**Common scenarios:**

- Writing files/directories with world-readable or world-writable permissions (e.g., `chmod 777`)
- Temporary files created with insecure default permissions
- Log files containing sensitive data accessible to all users
- Configuration files (with passwords/secrets) readable by non-privileged users
- Uploaded files stored with incorrect permissions

---

### Remediation Strategies

**1. Set Explicit File Permissions Using `PosixFilePermission`**

```java
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

// BAD: No permission control
Files.createFile(Path.of("/app/data/sensitive.txt"));

// GOOD: Explicit restrictive permissions
Path filePath = Path.of("/app/data/sensitive.txt");
Set<PosixFilePermission> permissions = EnumSet.of(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE
    // No GROUP or OTHERS permissions
);
Files.createFile(filePath, PosixFilePermissions.asFileAttribute(permissions));
```

**2. Secure Temporary File Creation**

```java
// BAD: Uses system default permissions (often world-readable)
File temp = File.createTempFile("upload", ".tmp");

// GOOD: Explicitly restricted temp file
Path secureTmp = Files.createTempFile(
    "upload", ".tmp",
    PosixFilePermissions.asFileAttribute(
        EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
    )
);
```

**3. Secure File Upload Handling in Spring Boot**

```java
@Service
public class FileUploadService {

    private static final Set<PosixFilePermission> FILE_PERMS = EnumSet.of(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE
    );

    private static final Set<PosixFilePermission> DIR_PERMS = EnumSet.of(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.OWNER_EXECUTE
    );

    public Path storeFile(MultipartFile file, String uploadDir) throws IOException {
        Path directory = Path.of(uploadDir);

        // Secure directory creation
        if (!Files.exists(directory)) {
            Files.createDirectories(directory,
                PosixFilePermissions.asFileAttribute(DIR_PERMS));
        }

        Path destination = directory.resolve(
            StringUtils.cleanPath(file.getOriginalFilename())
        );

        // Write file then immediately restrict permissions
        Files.copy(file.getInputStream(), destination,
            StandardCopyOption.REPLACE_EXISTING);
        Files.setPosixFilePermissions(destination, FILE_PERMS);

        return destination;
    }
}
```

**4. Secure Logging Configuration (`logback-spring.xml`)**

```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/myapp/app.log</file>

        <!-- Restrict log file permissions -->
        <prudent>true</prudent>

        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/myapp/app.%d{yyyy-MM-dd}.log</fileNamePattern>
        </rollingPolicy>

        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

Then set permissions at the OS/startup level:
```bash
install -m 640 -o appuser -g appgroup /dev/null /var/log/myapp/app.log
```

**5. Use a `umask` at Application Startup**

Set a restrictive umask before your Spring Boot app starts to ensure all created files inherit safe defaults:

```bash
# In your startup script
umask 027   # Files: 640, Dirs: 750
java -jar myapp.jar
```

**6. Application Properties / Config File Protection**

```bash
# application.properties or application.yml should NOT be world-readable
chmod 600 src/main/resources/application.properties

# For deployed configs
chmod 640 /etc/myapp/application.properties
chown appuser:appgroup /etc/myapp/application.properties
```

---

### Quick Reference: Permission Values

| Permission | Octal | Meaning |
|---|---|---|
| `600` | rw------- | Owner read/write only ✅ |
| `640` | rw-r----- | Owner rw, group read ✅ |
| `644` | rw-r--r-- | World-readable ⚠️ |
| `777` | rwxrwxrwx | Full access for everyone ❌ |

---

### Key Takeaways

- Always use **`PosixFilePermissions`** when creating files programmatically — never rely on OS defaults.
- Apply the **principle of least privilege**: grant only what the application actually needs.
- Protect **config files, logs, and temp files** — these are the most commonly misconfigured.
- Run your Spring Boot app as a **dedicated non-root user** with minimal OS-level permissions.
- On Windows, use **ACLs via `AclFileAttributeView`** instead of POSIX permissions.