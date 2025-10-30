You're absolutely right — the current `passesFilters` method in `DataMapper` has a **critical flaw with boolean values**.

### 🔍 Problem
When your JSON contains:
```json
{ "active": true }
```
and you use a filter like:
```yaml
filters:
  - field: "$.active"
    operator: EQ
    value: true
```

The comparison **fails** because:
- `JsonPath.read(...)` returns a **`Boolean` object** (`java.lang.Boolean`)
- But your YAML parser (Jackson) may load `value: true` as a **`Boolean`**, **`String` ("true")**, or even **`Integer` (1)** depending on context
- The `Objects.equals(actualStr, expectedStr)` fallback converts both to strings → `"true".equals("true")` ✅  
  **BUT** if types mismatch (e.g., `Boolean` vs `String`), it can fail in edge cases

However, the **real issue** is deeper:  
> **Your current code normalizes everything to `String` for non-numeric comparisons**, which breaks type-safe boolean logic.

---

## ✅ Solution: Preserve and Compare Native Types

We need to:
1. **Avoid string conversion for booleans**
2. **Handle `Boolean` values explicitly**
3. **Ensure YAML `true`/`false` is parsed as `Boolean`**

---

### 🛠 Fixed `passesFilters` Logic for Booleans

Update the **string-based comparison block** in `passesFilters`:

#### ❌ Old (problematic):
```java
String actualStr = normalizedActual.toString();
String expectedStr = normalizedExpected.toString();
return Objects.equals(actualStr, expectedStr); // for EQ
```

#### ✅ New (type-aware):
```java
// After numeric checks, handle booleans
if (normalizedActual instanceof Boolean || normalizedExpected instanceof Boolean) {
    Boolean actualBool = toBoolean(normalizedActual);
    Boolean expectedBool = toBoolean(normalizedExpected);
    if (actualBool == null || expectedBool == null) {
        return false; // can't compare non-boolean as boolean
    }
    switch (opUpper) {
        case "EQ":  return Objects.equals(actualBool, expectedBool);
        case "NE":  return !Objects.equals(actualBool, expectedBool);
        default:
            throw new UnsupportedOperationException("Boolean doesn't support operator: " + operator);
    }
}

// Fallback to string for everything else
String actualStr = normalizedActual.toString();
String expectedStr = normalizedExpected.toString();
// ... string comparisons
```

---

### 🔧 Add Helper: `toBoolean`

Add this method to `DataMapper`:

```java
private Boolean toBoolean(Object value) {
    if (value == null) return null;
    if (value instanceof Boolean) {
        return (Boolean) value;
    }
    if (value instanceof String) {
        String s = ((String) value).trim().toLowerCase();
        if ("true".equals(s) || "1".equals(s)) return true;
        if ("false".equals(s) || "0".equals(s)) return false;
        return null; // not a boolean string
    }
    if (value instanceof Number) {
        return ((Number) value).intValue() != 0;
    }
    return null;
}
```

> This handles:
> - `true` / `false` (Boolean)
> - `"true"`, `"false"`, `"1"`, `"0"` (String)
> - `1`, `0` (Number)

---

### 🧩 Full Updated Comparison Block in `passesFilters`

Replace the **string operator switch** with this:

```java
// After numeric and IN checks...

// Handle Boolean types
if (normalizedActual instanceof Boolean || normalizedExpected instanceof Boolean) {
    Boolean actualBool = toBoolean(normalizedActual);
    Boolean expectedBool = toBoolean(normalizedExpected);
    if (actualBool == null || expectedBool == null) {
        return false;
    }
    switch (opUpper) {
        case "EQ":  return Objects.equals(actualBool, expectedBool);
        case "NE":  return !Objects.equals(actualBool, expectedBool);
        default:
            throw new UnsupportedOperationException("Boolean values do not support operator: " + operator);
    }
}

// Fallback: treat as strings (for CONTAINS, STARTS_WITH, etc.)
String actualStr = normalizedActual.toString();
String expectedStr = normalizedExpected.toString();

switch (opUpper) {
    case "EQ": return Objects.equals(actualStr, expectedStr);
    case "NE": return !Objects.equals(actualStr, expectedStr);
    case "CONTAINS": return actualStr.contains(expectedStr);
    case "STARTS_WITH": return actualStr.startsWith(expectedStr);
    case "ENDS_WITH": return actualStr.endsWith(expectedStr);
    default: throw new UnsupportedOperationException("Unsupported operator: " + operator);
}
```

---

### ✅ Ensure YAML Parses Booleans Correctly

Make sure your **YAML config** uses **unquoted `true`/`false`**:

```yaml
filters:
  - field: "$.active"
    operator: EQ
    value: true    # ✅ Correct — parsed as Boolean

  - field: "$.verified"
    operator: EQ
    value: "true"  # ❌ Avoid — parsed as String
```

> Jackson’s YAML parser will correctly parse unquoted `true` → `Boolean.TRUE`.

---

### 🧪 Test Cases to Verify

Add these unit tests:

```java
@Test
void testBooleanFilter_true() {
    Map<String, Object> data = Map.of("active", true);
    List<FilterCondition> filters = List.of(
        new FilterCondition("$.active", "EQ", true)
    );
    assertTrue(dataMapper.passesFilters(data, filters));
}

@Test
void testBooleanFilter_false() {
    Map<String, Object> data = Map.of("active", false);
    List<FilterCondition> filters = List.of(
        new FilterCondition("$.active", "EQ", false)
    );
    assertTrue(dataMapper.passesFilters(data, filters));
}

@Test
void testBooleanFilter_stringTrue_shouldFail() {
    Map<String, Object> data = Map.of("active", true);
    List<FilterCondition> filters = List.of(
        new FilterCondition("$.active", "EQ", "true") // string
    );
    // Should pass if toBoolean handles "true", or fail if strict
    // With our toBoolean, it WILL pass
    assertTrue(dataMapper.passesFilters(data, filters));
}
```

---

### 🔚 Summary of Fixes

| Issue | Fix |
|------|-----|
| Boolean comparisons fail | Added explicit `Boolean` handling before string fallback |
| YAML `true` parsed as string | Use unquoted `true`/`false` in YAML |
| Type coercion | Added `toBoolean()` helper with flexible parsing |
| Operator safety | Disallow `GT`, `LT`, etc. on booleans |

This ensures your filters work **reliably for booleans**, while remaining backward-compatible with string/numeric comparisons.

Let me know if you'd like the full updated `DataMapper` with this fix integrated!