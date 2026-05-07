## Adding Timeouts to Guard Against Long-Running Loops

Here are practical ways to implement timeouts across different languages and scenarios:

---

### 1. Python

**Using `signal` (Unix/Linux only)**
```python
import signal

def timeout_handler(signum, frame):
    raise TimeoutError("Loop execution exceeded time limit")

signal.signal(signal.SIGALRM, timeout_handler)
signal.alarm(5)  # 5-second timeout

try:
    for i in range(iterations):
        do_heavy_work()
finally:
    signal.alarm(0)  # Cancel alarm after completion
```

**Using `concurrent.futures` (Cross-platform)**
```python
from concurrent.futures import ThreadPoolExecutor, TimeoutError

def heavy_loop():
    for i in range(iterations):
        do_heavy_work()

with ThreadPoolExecutor(max_workers=1) as executor:
    future = executor.submit(heavy_loop)
    try:
        future.result(timeout=5)  # 5-second timeout
    except TimeoutError:
        print("Loop timed out — aborting execution")
```

---

### 2. Java

**Using `ExecutorService`**
```java
ExecutorService executor = Executors.newSingleThreadExecutor();

Future<?> future = executor.submit(() -> {
    for (int i = 0; i < count; i++) {
        processItem(i);
    }
});

try {
    future.get(5, TimeUnit.SECONDS);  // 5-second timeout
} catch (TimeoutException e) {
    future.cancel(true);  // Interrupt the thread
    System.out.println("Loop timed out — cancelled");
} finally {
    executor.shutdown();
}
```

**Using time-check inside the loop (lightweight)**
```java
long startTime = System.currentTimeMillis();
long timeoutMs = 5000; // 5 seconds

for (int i = 0; i < count; i++) {
    if (System.currentTimeMillis() - startTime > timeoutMs) {
        throw new RuntimeException("Loop exceeded time limit");
    }
    processItem(i);
}
```

---

### 3. JavaScript / Node.js

**Using `AbortController` with async loops**
```javascript
const controller = new AbortController();
const timeout = setTimeout(() => controller.abort(), 5000); // 5s timeout

try {
    for (let i = 0; i < limit; i++) {
        if (controller.signal.aborted) {
            throw new Error("Loop timed out");
        }
        await processItem(i);
    }
} finally {
    clearTimeout(timeout);
}
```

**Using `Promise.race` for async operations**
```javascript
const loopTask = async () => {
    for (let i = 0; i < limit; i++) {
        await processItem(i);
    }
};

const timeoutTask = new Promise((_, reject) =>
    setTimeout(() => reject(new Error("Loop timed out")), 5000)
);

await Promise.race([loopTask(), timeoutTask]);
```

---

### 4. C / C++

**Using `alarm()` signal (POSIX)**
```c
#include <signal.h>
#include <unistd.h>

volatile sig_atomic_t timed_out = 0;

void handle_timeout(int sig) {
    timed_out = 1;
}

signal(SIGALRM, handle_timeout);
alarm(5);  // 5-second timeout

for (int i = 0; i < n && !timed_out; i++) {
    process(data[i]);
}

if (timed_out) {
    fprintf(stderr, "Loop timed out\n");
}
alarm(0);  // Cancel alarm
```

**Using clock-based check inside loop**
```c
#include <time.h>

clock_t start = clock();
double timeout_sec = 5.0;

for (int i = 0; i < n; i++) {
    double elapsed = (double)(clock() - start) / CLOCKS_PER_SEC;
    if (elapsed > timeout_sec) {
        fprintf(stderr, "Timeout reached at iteration %d\n", i);
        break;
    }
    process(data[i]);
}
```

---

### 5. In-Loop Time Check Pattern (Universal)

This is the **simplest and most portable** approach — check elapsed time on every N iterations to avoid overhead:

```python
import time

start = time.time()
TIMEOUT = 5        # seconds
CHECK_EVERY = 100  # check every 100 iterations to reduce overhead

for i in range(iterations):
    if i % CHECK_EVERY == 0:
        if time.time() - start > TIMEOUT:
            raise TimeoutError(f"Timed out at iteration {i}")
    do_heavy_work()
```

---

### Choosing the Right Approach

| Approach | Best For | Platform |
|---|---|---|
| `signal.alarm` | Simple scripts, Unix daemons | Linux/macOS only |
| `ThreadPoolExecutor` / `ExecutorService` | Web apps, APIs | Cross-platform |
| `AbortController` | Async JS loops | Node.js / Browser |
| In-loop time check | Lightweight, no threading | Universal |
| `Promise.race` | Async JS tasks | Node.js / Browser |

---

### Key Best Practices

- **Always clean up** — cancel alarms and shut down executors in `finally` blocks
- **Log timeouts** — record which input/iteration count triggered the timeout for monitoring
- **Return partial results** — where appropriate, return what was processed before timeout rather than nothing
- **Combine with input validation** — timeouts are a *last line of defense*, not a substitute for bounding loop input upfront