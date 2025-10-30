Absolutely! You can **avoid requiring users to write array suffixes like `[]` or `[0]` in YAML**, and instead **infer array access automatically in your code** based on context.

This makes your YAML even cleaner and more intuitive:

### ❌ Current (user must know array syntax):
```yaml
sourceArray: "applicants[relationship=dependent]"   # ← needs filter
sourceObject: "applicants[relationship=primary][0]" # ← needs [0]
```

### ✅ Desired (user writes natural paths):
```yaml
sourceArray: "applicants"        # ← just the array name
itemFilters:
  - field: "relationship"
    operator: EQ
    value: "dependent"

sourceObject: "applicants"       # ← same array
itemFilters:                     # ← but with different filter
  - field: "relationship"
    operator: EQ
    value: "primary"
```

> ✅ **No `[0]`, no `[]`, no JsonPath** — just **field names** and **filters**.

---

## ✅ How It Works

Your code will:
1. Treat `sourceArray` and `sourceObject` as **pointers to a JSON array**
2. Apply `itemFilters` to **select items** from that array
3. For `sourceObject`: take the **first matching item**
4. For `sourceArray`: take **all matching items**

This is **more declarative** and **hides array mechanics** from the user.

---

## 🛠 Implementation Plan

### 1. **Keep YAML Simple**
```yaml
mappings:
  # Primary applicant (single object from array)
  - sourceObject: "applicants"
    itemFilters:
      - field: "relationship"
        operator: EQ
        value: "primary"
    fieldMappings:
      - sourceField: "firstName"
        targetField: "primary.fname.1"

  # Dependents (all matching from same array)
  - sourceArray: "applicants"
    itemFilters:
      - field: "relationship"
        operator: EQ
        value: "dependent"
    itemMappings:
      - sourceField: "firstName"
        targetFieldTemplate: "dependent.fname.{index}"
```

> 🔑 **Key**: Both use `"applicants"` — the **filter determines the role**.

---

### 2. **Update `DataMapper` Logic**

#### For `sourceObject`:
- Read the **full array**: `readSimplePath(jsonData, mapping.getSourceObject())`
- Filter items using `itemFilters`
- Take **first match**

#### For `sourceArray`:
- Read the **full array**
- Filter items using `itemFilters`
- Use **all matches**

> 💡 **No need for `[0]` or `[*]` in paths!**

---

### 3. **Enhanced `handleObjectMapping`**

```java
private void handleObjectMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    // 1. Read the full array (e.g., "applicants" → List)
    Object rawArray = readSimplePath(jsonData, mapping.getSourceObject());
    if (!(rawArray instanceof List)) return;

    List<?> items = (List<?>) rawArray;

    // 2. Find FIRST item that passes itemFilters
    Object selectedItem = null;
    for (Object item : items) {
        if (passesFilters(item, mapping.getItemFilters())) {
            selectedItem = item;
            break;
        }
    }

    if (selectedItem == null) return;

    // 3. Map fields from selectedItem
    for (ObjectFieldMapping fieldMap : mapping.getFieldMappings()) {
        Object rawValue = readSimplePath(selectedItem, fieldMap.getSourceField());
        if (rawValue == null) continue;
        Object transformed = applyTransformations(rawValue, fieldMap.getTransforms());
        result.put(fieldMap.getTargetField(), safeToString(transformed));
    }
}
```

---

### 4. **Enhanced `handleRepeatingMapping`**

```java
private void handleRepeatingMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    // 1. Read full array
    Object rawArray = readSimplePath(jsonData, mapping.getSourceArray());
    if (!(rawArray instanceof List)) return;

    List<?> items = (List<?>) rawArray;
    int maxAllowed = mapping.getMaxRepetitions() != null 
                     ? Math.min(items.size(), mapping.getMaxRepetitions()) 
                     : items.size();

    int outputIndex = 1;
    for (Object item : items) {
        if (outputIndex > maxAllowed) break;
        if (!passesFilters(item, mapping.getItemFilters())) continue;

        for (ItemMapping itemMapping : mapping.getItemMappings()) {
            Object rawValue = readSimplePath(item, itemMapping.getSourceField());
            if (rawValue == null) continue;
            Object transformed = applyTransformations(rawValue, itemMapping.getTransforms());
            String targetField = itemMapping.getTargetFieldTemplate()
                    .replace("{index}", String.valueOf(outputIndex));
            result.put(targetField, safeToString(transformed));
        }
        outputIndex++;
    }
}
```

> ✅ **Both methods use the same `itemFilters` logic** — consistent and DRY.

---

### 5. **Update `FieldMapping` Validation**

Allow `itemFilters` for **both** `sourceObject` and `sourceArray`:

```java
public void validate() {
    // ... mode validation ...

    if (isObjectMapping() || isRepeating()) {
        // itemFilters are allowed (and encouraged)
        if (isObjectMapping() && (fieldMappings == null || fieldMappings.isEmpty())) {
            throw new IllegalStateException("'fieldMappings' required for object mappings");
        }
        if (isRepeating() && (itemMappings == null || itemMappings.isEmpty())) {
            throw new IllegalStateException("'itemMappings' required for repeating mappings");
        }
    }
}
```

---

## 📄 Final YAML Example (Super Clean!)

```yaml
templates:
  - type: PDF
    templatePath: classpath:form.pdf
    outputPath: /tmp/out.pdf
    mappings:
      # Primary applicant
      - sourceObject: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
        fieldMappings:
          - sourceField: "firstName"
            targetField: "primary.fname.1"
          - sourceField: "lastName"
            targetField: "primary.lname.1"

      # Spouse
      - sourceObject: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "spouse"
        fieldMappings:
          - sourceField: "firstName"
            targetField: "spouse.fname.1"

      # Dependents
      - sourceArray: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "dependent"
        maxRepetitions: 5
        itemMappings:
          - sourceField: "firstName"
            targetFieldTemplate: "dependent.fname.{index}"
          - sourceField: "age"
            targetFieldTemplate: "dependent.age.{index}"
```

> ✅ **No array syntax in paths**  
> ✅ **Filters are explicit and reusable**  
> ✅ **Same source array used for all roles**

---

## ✅ Benefits

| Benefit | Explanation |
|--------|------------|
| **Simpler YAML** | Users don’t need to know array indexing |
| **Consistent filtering** | Same `itemFilters` for single and repeating |
| **Less error-prone** | No risk of wrong index (`[0]` vs `[1]`) |
| **More semantic** | Focus on **what** (role) not **how** (index) |

---

## 🔁 Backward Compatibility

If you still want to support raw paths (e.g., for advanced users), keep `PathTranslator` and allow both:

- `"applicants"` → auto-resolve as array
- `"applicants[0]"` → pass through to JsonPath

But for most use cases, **the filter-based approach is superior**.

---

## ✅ Summary

> **Yes — eliminate array suffixes (`[0]`, `[]`) from YAML entirely.**  
> Use **`sourceObject` / `sourceArray` + `itemFilters`** to declaratively select data from arrays.

This gives you:
- ✅ Cleanest possible YAML
- ✅ Full flexibility
- ✅ No JsonPath exposure
- ✅ Reusable filter logic

Your users write **what they want**, your code handles **how to get it**.

Let me know if you'd like the full updated `DataMapper` with this simplified path handling! 🚀