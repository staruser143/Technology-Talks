You're absolutely right to question this — **repeating the same JsonPath filter for every field (firstName, lastName, ssn, etc.) is redundant, error-prone, and hard to maintain**.

✅ **Yes — you can and should avoid repetition** by using a **top-level filter** that isolates the correct applicant **once**, then maps multiple fields from that filtered object.

However, **your current `FieldMapping` model doesn’t support this directly** for **single-value mappings**.

---

## 🔍 The Problem with Current Approach

```yaml
# ❌ Repetitive and fragile
- sourceField: "$.applicants[?(@.relationship == 'primary')].firstName"
  targetField: "primary.fname.1"
- sourceField: "$.applicants[?(@.relationship == 'primary')].lastName"
  targetField: "primary.lname.1"
- sourceField: "$.applicants[?(@.relationship == 'primary')].ssn"
  targetField: "primary.ssn.1"
```

- ❌ Same filter repeated 3+ times
- ❌ If you change the condition, you must update every field
- ❌ Inefficient: JsonPath evaluates the filter **once per field**

---

## ✅ Better Approach: **"Scoped Mapping" for Single Objects**

We need a way to say:
> “**Find the primary applicant once**, then map its `firstName`, `lastName`, etc.”

This is **exactly what your `repeating` mapping does for arrays** — but we need it for **single objects** too.

---

## 🛠 Solution: Extend `FieldMapping` to Support **Object Extraction + Nested Mappings**

Add a new mode: **`sourceObject` + `fieldMappings`** (analogous to `sourceArray` + `itemMappings`).

### 🔧 Updated `FieldMapping.java` (Add These Fields)

```java
public class FieldMapping {

    // ... existing fields ...

    /**
     * JsonPath to a SINGLE object (not array).
     * Example: "$.applicants[?(@.relationship == 'primary')][0]"
     */
    private String sourceObject;

    /**
     * Mappings relative to the extracted object.
     */
    private List<ObjectFieldMapping> fieldMappings = new ArrayList<>();

    // New helper
    public boolean isObjectMapping() {
        return sourceObject != null && !sourceObject.trim().isEmpty();
    }

    // Update validate()
    public void validate() {
        long modes = Stream.of(
                (sourceField != null),
                (sourceArray != null),
                (sourceObject != null)
            ).filter(b -> b).count();

        if (modes != 1) {
            throw new IllegalStateException("Exactly one of sourceField, sourceArray, or sourceObject must be set");
        }

        if (isObjectMapping()) {
            if (fieldMappings == null || fieldMappings.isEmpty()) {
                throw new IllegalStateException("'fieldMappings' required for object mappings");
            }
        }
        // ... rest unchanged
    }
}
```

### 🆕 New: `ObjectFieldMapping.java`

```java
public class ObjectFieldMapping {
    private String sourceField;        // relative path, e.g., "$.firstName"
    private String targetField;        // e.g., "primary.fname.1"
    private List<TransformSpec> transforms = new ArrayList<>();
    // optional: filters (rare for single object)
}
```

---

### 📄 Updated YAML (Clean & DRY)

```yaml
mappings:
  # ✅ PRIMARY APPLICANT — single object mapping
  - sourceObject: "$.applicants[?(@.relationship == 'primary')][0]"
    fieldMappings:
      - sourceField: "$.firstName"
        targetField: "primary.fname.1"
      - sourceField: "$.lastName"
        targetField: "primary.lname.1"
      - sourceField: "$.ssn"
        targetField: "primary.ssn.1"

  # ✅ SPOUSE — single object mapping (optional)
  - sourceObject: "$.applicants[?(@.relationship == 'spouse')][0]"
    fieldMappings:
      - sourceField: "$.firstName"
        targetField: "spouse.fname.1"
      - sourceField: "$.lastName"
        targetField: "spouse.lname.1"

  # ✅ DEPENDENTS — repeating (unchanged)
  - sourceArray: "$.applicants[?(@.relationship == 'dependent')]"
    maxRepetitions: 5
    itemMappings:
      - sourceField: "$.firstName"
        targetFieldTemplate: "dependent.fname.{index}"
      - sourceField: "$.lastName"
        targetFieldTemplate: "dependent.lname.{index}"
```

> ✅ **Key Improvement**:  
> The **filter is written once** in `sourceObject`.  
> All fields are mapped **relative to the extracted object**.

---

### 🧠 Update `DataMapper` to Handle `sourceObject`

Add this to `mapData`:

```java
if (mapping.isObjectMapping()) {
    handleObjectMapping(jsonData, mapping, result);
} else if (mapping.isRepeating()) {
    handleRepeatingMapping(jsonData, mapping, result);
} else {
    handleSingleValueMapping(jsonData, mapping, result);
}
```

#### New Method: `handleObjectMapping`

```java
private void handleObjectMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    // Extract the single object (could be null if not found)
    Object obj = readJsonPathSafe(jsonData, mapping.getSourceObject());
    if (obj == null) {
        return; // skip all fields
    }

    // Apply global filters (optional, on root JSON)
    if (!passesFilters(jsonData, mapping.getFilters())) {
        return;
    }

    // Map each field relative to the extracted object
    for (ObjectFieldMapping fieldMap : mapping.getFieldMappings()) {
        Object rawValue = readJsonPathSafe(obj, fieldMap.getSourceField());
        if (rawValue == null) continue;

        Object transformed = applyTransformations(rawValue, fieldMap.getTransforms());
        result.put(fieldMap.getTargetField(), safeToString(transformed));
    }
}
```

> 💡 **Note**:  
> - `sourceObject` uses `[0]` to **unwrap the first match** from JsonPath filter (which returns a list).  
> - If no match, `readJsonPathSafe` returns `null` → entire block skipped.

---

### 🔍 Why `[0]` in `sourceObject`?

JsonPath filter `$.applicants[?(@.relationship == 'primary')]` returns a **list**.  
To get the **first (and only) object**, we add `[0]`:
```jsonpath
$.applicants[?(@.relationship == 'primary')][0]
```

This ensures `obj` is a **single object** (not a list), so `$.firstName` works in `ObjectFieldMapping`.

---

### ✅ Benefits

| Benefit | Explanation |
|-------|------------|
| **DRY** | Filter written once |
| **Efficient** | JsonPath evaluated once per role |
| **Maintainable** | Change condition in one place |
| **Safe** | If applicant not found, all fields skipped |
| **Consistent** | Same pattern as repeating sections |

---

### 🧪 Edge Case: No Primary Applicant?

- `$.applicants[?(@.relationship == 'primary')][0]` → returns `null`
- `handleObjectMapping` exits early → no fields mapped
- PDF shows blank for primary fields (as expected)

---

### 🚫 Alternative (Without Code Change): Use a Dummy Field

If you **can’t modify the library**, you could use a **single dummy mapping** to extract the object, then reference it in transforms — but this is hacky.

✅ **Best Practice**: Extend your model with `sourceObject` + `fieldMappings`.

---

### ✅ Summary

| Approach | Repetition? | Efficient? | Clean? |
|--------|-----------|----------|-------|
| Per-field JsonPath | ❌ Yes | ❌ N× filters | ❌ |
| **`sourceObject` + `fieldMappings`** | ✅ No | ✅ 1 filter | ✅ |

**Do this** — it’s the right abstraction for your use case, and aligns with your existing repeating-section design.

Let me know if you'd like the full updated `FieldMapping` and `DataMapper` with this enhancement!