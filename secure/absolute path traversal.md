## Absolute Path Traversal Vulnerability

**Absolute path traversal** is a security vulnerability where an attacker can supply an absolute file path (e.g., `/etc/passwd` or `C:\Windows\system32\config\SAM`) as input, causing the application to access files outside the intended directory — anywhere on the filesystem the process has permission to read.

### How It Works

Normal behavior: your app serves files from `/var/www/uploads/`. A user requests `report.pdf` → app reads `/var/www/uploads/report.pdf`.

With absolute path traversal: the attacker supplies `/etc/passwd` directly. If the app naively passes user input to file APIs, it reads that absolute path instead, bypassing the intended directory entirely.

```python
# Vulnerable code
filename = request.args.get("file")   # attacker sends "/etc/passwd"
open(filename, "r")                   # reads /etc/passwd directly
```

### Difference from Relative Path Traversal

| | Relative (`../`) | Absolute |
|---|---|---|
| Input | `../../etc/passwd` | `/etc/passwd` |
| Technique | Escapes base dir by climbing up | Ignores base dir entirely |
| Root cause | No `../` stripping | No absolute path rejection |

---

## Remediation Strategies

### 1. Validate and Reject Absolute Paths
Reject any input that starts with `/`, `\`, or a drive letter like `C:`.

```python
import os

def is_safe_filename(filename):
    if os.path.isabs(filename):          # rejects /etc/passwd, C:\...
        raise ValueError("Absolute paths not allowed")
    return filename
```

### 2. Canonicalize and Enforce a Base Directory
Always resolve the final path and confirm it starts with your allowed base directory.

```python
BASE_DIR = "/var/www/uploads"

def safe_open(user_input):
    # Resolve symlinks and normalize the path
    full_path = os.path.realpath(os.path.join(BASE_DIR, user_input))
    
    # Ensure the resolved path is still inside BASE_DIR
    if not full_path.startswith(BASE_DIR + os.sep):
        raise PermissionError("Access denied: path outside base directory")
    
    return open(full_path, "r")
```

### 3. Use an Allowlist (Safest Approach)
Only permit pre-approved filenames or identifiers — never use raw user input as a path.

```python
ALLOWED_FILES = {"report": "report.pdf", "invoice": "invoice.pdf"}

def get_file(key):
    if key not in ALLOWED_FILES:
        raise ValueError("File not found")
    return open(f"/var/www/uploads/{ALLOWED_FILES[key]}", "r")
```

### 4. Use `basename()` to Strip Directory Components

```python
import os

safe_name = os.path.basename(user_input)  # "/etc/passwd" → "passwd"
# But still combine with canonicalization — basename alone isn't sufficient
```

> ⚠️ `basename()` alone is not enough — always combine it with base directory enforcement.

### 5. Run with Least Privilege
Even if traversal occurs, limit the damage by running your application process with minimal OS permissions — it shouldn't be able to read `/etc/shadow` or sensitive system files in the first place.

---

## Summary Checklist

- Reject absolute paths in user input
- Canonicalize paths with `realpath()` before file access
- Confirm the resolved path is inside your allowed base directory
- Prefer allowlists over freeform filenames
- Apply the principle of least privilege to your process