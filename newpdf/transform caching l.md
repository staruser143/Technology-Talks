**Transform caching** is an optimization technique where the **results of expensive transformations** are stored (cached) so they don’t need to be recomputed if the **same input and transform spec** are used again.

This is especially useful in PDF form filling when:

- You have **many fields** using the **same transform** (e.g., 10 date fields all using `date:MM/dd/yyyy`)
- You have **complex transforms** (e.g., cryptographic hashing, API lookups, heavy calculations)
- You process **large datasets** (e.g., 1000+ records)

---

## 🚀 Why Cache Transforms?

| Scenario | Without Caching | With Caching |
|--------|------------------|--------------|
| 10 fields with `date:MM/dd/yyyy` on same date `"2025-10-25"` | Parse date **10 times** | Parse date **once**, reuse result |
| Complex `ssn_validate_and_mask` transform | Run validation logic **N times** | Run once per unique SSN |
| Large repeat blocks (e.g., 50 dependents) | Redundant work | Significant speedup |

---

## 🧠 How It Works

### Cache Key = `(input_value, transform_spec)`
- If same input + same transform → return cached result
- If either changes → compute and cache new result

### Example:
```java
// First call
transform("2025-10-25", "date:MM/dd/yyyy") → computes → caches → returns "10/25/2025"

// Later call (same input + transform)
transform("2025-10-25", "date:MM/dd/yyyy") → returns cached "10/25/2025"
```

---

## 🔧 Implementation in `TransformEngine`

### Step 1: Add Cache Storage

```java
import java.util.concurrent.ConcurrentHashMap;

public class TransformEngine {
    // Add cache (thread-safe)
    private final Map<String, String> transformCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 10_000; // prevent memory leak

    // Helper to create cache key
    private String makeCacheKey(String value, String transformSpec) {
        return value + "|" + transformSpec;
    }
}
```

> 💡 Use `ConcurrentHashMap` for thread safety (if used in multi-threaded env).

---

### Step 2: Update `apply()` to Use Cache

```java
public String apply(String value, String transformSpec) {
    if (transformSpec == null || transformSpec.isEmpty()) return value;

    // Create cache key
    String cacheKey = makeCacheKey(value, transformSpec);

    // Try cache first
    String cached = transformCache.get(cacheKey);
    if (cached != null) {
        return cached;
    }

    // Compute result
    String result;
    if (transformSpec.contains("|")) {
        result = applyChain(value, transformSpec);
    } else {
        result = applySingle(value, transformSpec);
    }

    // Cache result (with size limit)
    if (transformCache.size() < MAX_CACHE_SIZE) {
        transformCache.put(cacheKey, result);
    }

    return result;
}
```

> ⚠️ **Note**: Don’t cache **context-aware transforms** (they depend on full JSON, not just field value).

---

### Step 3: (Optional) Add Cache Stats for Debugging

```java
private final AtomicInteger cacheHits = new AtomicInteger();
private final AtomicInteger cacheMisses = new AtomicInteger();

public String apply(String value, String transformSpec) {
    // ... cache check ...
    if (cached != null) {
        cacheHits.incrementAndGet();
        return cached;
    }
    cacheMisses.incrementAndGet();
    // ... compute ...
}

// Add getter for monitoring
public int getCacheHitCount() { return cacheHits.get(); }
public int getCacheMissCount() { return cacheMisses.get(); }
```

---

## 📊 When to Use Caching

| Transform Type | Cache? | Reason |
|---------------|--------|--------|
| `uppercase`, `trim` | ❌ No | Too cheap to benefit |
| `date:pattern` | ✅ Yes | Date parsing is expensive |
| `currency:locale` | ✅ Yes | Number formatting has overhead |
| `ssn_mask` | ⚠️ Maybe | Only if same SSN reused often |
| **Context-aware** | ❌ No | Depends on full JSON context |
| **Random/Time-based** | ❌ Never | Non-deterministic |

---

## 🧪 Benchmark Example

### Without Caching
```java
// 1000 fields with same date
for (int i = 0; i < 1000; i++) {
    apply("2025-10-25", "date:MM/dd/yyyy"); // parses date 1000x
}
// Time: ~50ms
```

### With Caching
```java
// 1000 fields with same date
for (int i = 0; i < 1000; i++) {
    apply("2025-10-25", "date:MM/dd/yyyy"); // parses once, 999 cache hits
}
// Time: ~1ms
```

> ✅ **50x speedup** for repeated transforms!

---

## 🔒 Cache Safety Tips

1. **Limit cache size** → prevent `OutOfMemoryError`
   ```java
   if (transformCache.size() > MAX_CACHE_SIZE) {
       transformCache.clear(); // or use LRU cache
   }
   ```
2. **Don’t cache large inputs** → e.g., skip if `value.length() > 1000`
3. **Avoid caching non-deterministic transforms** → e.g., `current_time`
4. **Clear cache between documents** → if processing multiple PDFs:
   ```java
   public void clearCache() {
       transformCache.clear();
       cacheHits.set(0);
       cacheMisses.set(0);
   }
   ```

---

## 🧩 Advanced: LRU Cache (Optional)

For production systems, use an **LRU (Least Recently Used) cache**:

```java
// Add dependency
<dependency>
  <groupId>com.github.ben-manes.caffeine</groupId>
  <artifactId>caffeine</artifactId>
  <version>3.1.8</version>
</dependency>

// In TransformEngine
private final Cache<String, String> transformCache = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(10))
    .build();
```

---

## ✅ Summary

| Aspect | Recommendation |
|-------|----------------|
| **What to cache** | Expensive, deterministic, repeated transforms |
| **Cache key** | `(input_value, transform_spec)` |
| **Cache size** | Limit to 10K–100K entries |
| **Context-aware** | Never cache |
| **Clearing** | Per-document or time-based expiry |

Transform caching is a **simple change** with **massive payoff** for performance-heavy workloads — especially in batch PDF generation.

Let me know if you'd like the **full updated `TransformEngine` with caching** or help with **LRU implementation**!