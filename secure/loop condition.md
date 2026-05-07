## Unchecked Input for Loop Condition (CWE-606)

This vulnerability occurs when **user-controlled input is used directly as a loop condition or iteration count** without validation — allowing attackers to cause excessive iterations, denial of service, infinite loops, or resource exhaustion.

---

### Why It's Dangerous

An attacker can manipulate loop behavior to:
- Cause **Denial of Service (DoS)** via excessive CPU/memory consumption
- Trigger **infinite loops** that hang the application
- Force **out-of-bounds access** in arrays/buffers
- Enable **algorithmic complexity attacks**

---

### How It Happens

The core problem is trusting user input directly in loop control:

```
for (int i = 0; i < userInput; i++)   ← userInput is never validated
while (count < request.getParameter("limit"))  ← raw HTTP param used
```

---

### Vulnerable Code Examples & Fixes

**❌ Vulnerable — Java: Raw HTTP parameter as loop bound**
```java
int count = Integer.parseInt(request.getParameter("count"));
for (int i = 0; i < count; i++) {
    processItem(i); // attacker sends count = 999999999
}
```
**✅ Secure — Validate and cap the input**
```java
int count = Integer.parseInt(request.getParameter("count"));
if (count < 0 || count > 1000) {
    throw new IllegalArgumentException("Count must be between 0 and 1000");
}
for (int i = 0; i < count; i++) {
    processItem(i);
}
```

---

**❌ Vulnerable — Python: User input controls loop**
```python
iterations = int(input("Enter iterations: "))
for i in range(iterations):  # attacker enters 10_000_000_000
    do_heavy_work()
```
**✅ Secure — Enforce bounds**
```python
MAX_ITERATIONS = 500

iterations = int(input("Enter iterations: "))
if not (1 <= iterations <= MAX_ITERATIONS):
    raise ValueError(f"Iterations must be between 1 and {MAX_ITERATIONS}")

for i in range(iterations):
    do_heavy_work()
```

---

**❌ Vulnerable — C: User-controlled array loop (buffer risk)**
```c
int n;
scanf("%d", &n);
for (int i = 0; i < n; i++) {
    process(data[i]);  // n can exceed array size → out-of-bounds
}
```
**✅ Secure — Bound against array size**
```c
int n;
scanf("%d", &n);
if (n <= 0 || n > MAX_SIZE) {
    fprintf(stderr, "Invalid input\n");
    return -1;
}
for (int i = 0; i < n; i++) {
    process(data[i]);
}
```

---

**❌ Vulnerable — JavaScript: API query param as loop count**
```javascript
const limit = req.query.limit;
for (let i = 0; i < limit; i++) {  // No type check or bounds
    results.push(fetchRecord(i));
}
```
**✅ Secure — Parse, validate, and cap**
```javascript
const MAX_LIMIT = 100;
const limit = parseInt(req.query.limit, 10);

if (isNaN(limit) || limit < 1 || limit > MAX_LIMIT) {
    return res.status(400).json({ error: "Limit must be between 1 and 100" });
}
for (let i = 0; i < limit; i++) {
    results.push(fetchRecord(i));
}
```

---

### Remediation Checklist

| Step | Action |
|---|---|
| **1. Identify** | Audit all loops where the condition/bound derives from user input, config files, or external APIs |
| **2. Validate Type** | Ensure input is the expected type (int, not string/float/NaN) before use |
| **3. Enforce Min/Max Bounds** | Define and enforce a safe upper and lower limit for all loop counts |
| **4. Reject Invalid Input** | Fail fast — throw an exception or return an error rather than silently clamping |
| **5. Add Timeouts** | Implement execution time limits for loops that may run long regardless of count |
| **6. Rate Limiting** | Apply API-level rate limiting to prevent repeated large-loop requests |
| **7. Static Analysis** | Use SAST tools (Semgrep, Coverity, Checkmarx) to detect unvalidated loop conditions |

---

### Defense-in-Depth Strategy

```
User Input
    ↓
[Type Check]       → reject non-numeric / malformed
    ↓
[Range Validation] → enforce MIN and MAX bounds
    ↓
[Business Logic]   → does this value make sense contextually?
    ↓
[Loop Execution]   → safe, bounded iteration
    ↓
[Timeout Guard]    → kill if execution exceeds time limit
```

---

### Relevant Standards & References
- **CWE**: CWE-606 – Unchecked Input for Loop Condition
- **CWE**: CWE-400 – Uncontrolled Resource Consumption
- **OWASP**: A03:2021 – Injection / Input Validation
- **OWASP**: Denial of Service Cheat Sheet