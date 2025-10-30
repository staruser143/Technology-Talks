Yes — **you can absolutely define multiple conditions based on one or more fields in the source data** to control whether a field (or a repeating item) should be extracted and mapped. This is essential for real-world scenarios like:

- Only map a dependent if they are **under 26 AND a student**
- Only include an invoice line item if **quantity > 0 AND product.active == true**
- Skip an applicant field if **country != "US"**

---

## ✅ Solution: Support **Composite Filters** in Your YAML

You can enhance your `FilterCondition` model to support **logical operators** (`AND`, `OR`) and **nested conditions**, or keep it simple with **implicit AND** across a list of conditions (which covers 90% of use cases).

We’ll go with the **pragmatic approach**:  
> **A list of `FilterCondition` implies `AND` logic** — all must pass.

This is clean, readable, and sufficient for most business rules.

---

### 🔧 1. Keep Your Current `FilterCondition` (Implicit AND)

```java
public class FilterCondition {
    private String field;      // JsonPath: "$.age", "$.role", "$.address.country"
    private String operator;   // EQ, NE, GT, LT, GTE, LTE, CONTAINS, IN, etc.
    private Object value;
}
```

> ✅ **Multiple conditions = AND**:  
> ```yaml
> filters:
>   - field: "$.age"
>     operator: LT
>     value: 26
>   - field: "$.student"
>     operator: EQ
>     value: true
> ```
> → Only matches if **both** are true.

This works **today** with your existing `passesGlobalFilters()` logic (which uses `.allMatch(...)`).

---

### 🧩 2. (Optional) Support Explicit `OR` / Nested Logic

If you need advanced logic like `(age < 26 AND student) OR (disabled == true)`, extend the model:

#### Option A: Group Conditions with `logic: AND|OR`

```yaml
filters:
  logic: OR
  conditions:
    - logic: AND
      conditions:
        - field: "$.age"
          operator: LT
          value: 26
        - field: "$.student"
          operator: EQ
          value: true
    - field: "$.disabled"
      operator: EQ
      value: true
```

But this adds complexity.

#### ✅ Recommendation: **Stick with implicit AND** for simplicity.  
For `OR` cases, **duplicate the mapping**:

```yaml
# Map if (age < 26 AND student)
- sourceField: "$.name"
  targetField: "young_student"
  filters:
    - field: "$.age"
      operator: LT
      value: 26
    - field: "$.student"
      operator: EQ
      value: true

# Map if (disabled == true)
- sourceField: "$.name"
  targetField: "young_student"  # same target
  filters:
    - field: "$.disabled"
      operator: EQ
      value: true
```

> PDF fields are **overwritten** — last write wins. So order matters if values differ.  
> But if both produce same value (e.g., name), it’s safe.

---

### 🧠 3. Apply Filters at Two Levels

Your system already supports filters at the **mapping level** (global). Now extend to **item level** for repeating sections.

#### A. Global Filters (on `FieldMapping`)
→ Skip entire mapping (e.g., don’t fill any dependent fields if applicant is ineligible).

#### B. Per-Item Filters (on `ItemMapping`)
→ Skip individual array items (e.g., skip inactive dependents).

---

### 🔁 Updated `DataMapper` Logic

#### For Single-Value Mappings:
Already supported via `passesGlobalFilters(jsonData, mapping.getFilters())`.

#### For Repeating Mappings:
Add per-item filtering:

```java
private void handleRepeatingMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    Object arrayObj = readJsonPathSafe(jsonData, mapping.getSourceArray());
    if (!(arrayObj instanceof List)) return;

    List<?> items = (List<?>) arrayObj;
    int maxIndex = mapping.getMaxRepetitions() != null 
                   ? Math.min(items.size(), mapping.getMaxRepetitions())
                   : items.size();

    int outputIndex = 1; // PDF is 1-based; only increment when item is included

    for (int i = 0; i < items.size() && outputIndex <= maxIndex; i++) {
        Object item = items.get(i);

        // ✅ Apply per-item filters (if any ItemMapping has filters)
        boolean shouldInclude = mapping.getItemMappings().stream()
            .allMatch(im -> passesFilters(item, im.getFilters())); // or use ANY? Design choice.

        // OR: Apply once per item using a shared filter (better):
        // if (!passesFilters(item, mapping.getItemFilters())) continue;

        if (!shouldInclude) {
            continue;
        }

        for (ItemMapping itemMapping : mapping.getItemMappings()) {
            // Optional: per-field filters
            if (!passesFilters(item, itemMapping.getFilters())) {
                continue;
            }

            Object rawValue = readJsonPathSafe(item, itemMapping.getSourceField());
            if (rawValue == null) continue;

            Object transformed = applyTransformations(rawValue, itemMapping.getTransforms());
            String targetField = itemMapping.getTargetFieldTemplate()
                                             .replace("{index}", String.valueOf(outputIndex));
            result.put(targetField, safeToString(transformed));
        }

        outputIndex++; // only increment when item is used
    }
}
```

> 💡 **Design Note**:  
> Should all `ItemMapping` filters pass, or just the ones relevant to that field?  
> **Recommendation**: Add a top-level `itemFilters` to `FieldMapping` for clarity:

```java
public class FieldMapping {
    // ...
    private List<FilterCondition> itemFilters; // applied once per array item
}
```

Then in YAML:

```yaml
- sourceArray: "$.dependents"
  itemFilters:                     # ← applied to each dependent
    - field: "$.age"
      operator: LT
      value: 26
    - field: "$.active"
      operator: EQ
      value: true
  itemMappings:
    - sourceField: "$.firstName"
      targetFieldTemplate: "dependent.fname.{index}"
    # no per-field filters needed
```

This is cleaner and more efficient.

---

### 📊 Supported Operators (Extend as Needed)

| Operator   | Example                          | Meaning                     |
|-----------|----------------------------------|-----------------------------|
| `EQ`      | `value: "US"`                    | ==                          |
| `NE`      | `value: "CA"`                    | !=                          |
| `GT`      | `value: 18`                      | >                           |
| `GTE`     | `value: 18`                      | >=                          |
| `LT`      | `value: 65`                      | <                           |
| `LTE`     | `value: 65`                      | <=                          |
| `CONTAINS`| `value: "admin"`                 | string contains             |
| `IN`      | `value: ["A", "B", "C"]`         | value in list               |
| `NOT_NULL`| (no value)                       | field exists and not null   |

Implement in `passesFilters`:

```java
private boolean passesFilters(Object context, List<FilterCondition> filters) {
    if (filters == null || filters.isEmpty()) return true;

    return filters.stream().allMatch(f -> {
        Object actual = readJsonPathSafe(context, f.getField());
        Object expected = f.getValue();
        String op = f.getOperator().toUpperCase();

        if ("NOT_NULL".equals(op)) {
            return actual != null && !((actual instanceof String) && ((String) actual).isEmpty());
        }

        if (actual == null) return false;

        if ("IN".equals(op)) {
            if (expected instanceof List) {
                return ((List<?>) expected).contains(actual);
            } else if (expected instanceof String) {
                List<String> vals = Arrays.stream(((String) expected).split(","))
                                          .map(String::trim).collect(Collectors.toList());
                return vals.contains(actual.toString());
            }
        }

        // Numeric comparisons
        if (isNumericOperator(op)) {
            Double a = toDouble(actual);
            Double e = toDouble(expected);
            if (a == null || e == null) return false;
            switch (op) {
                case "GT": return a > e;
                case "GTE": return a >= e;
                case "LT": return a < e;
                case "LTE": return a <= e;
                default: return a.equals(e);
            }
        }

        // Default: string equality
        return Objects.equals(actual.toString(), expected.toString());
    });
}
```

---

### ✅ Real-World YAML Example

```yaml
mappings:
  # Only map if applicant is from US and over 18
  - sourceField: "$.applicant.ssn"
    targetField: "ssn.1"
    filters:
      - field: "$.applicant.country"
        operator: EQ
        value: "US"
      - field: "$.applicant.age"
        operator: GTE
        value: 18

  # Dependents: only include active minors
  - sourceArray: "$.dependents"
    itemFilters:
      - field: "$.active"
        operator: EQ
        value: true
      - field: "$.age"
        operator: LT
        value: 26
    itemMappings:
      - sourceField: "$.firstName"
        targetFieldTemplate: "dependent.fname.{index}"
      - sourceField: "$.relationship"
        targetFieldTemplate: "dependent.rel.{index}"
```

---

### ✅ Summary

| Requirement                             | Supported? | How |
|----------------------------------------|-----------|-----|
| Multiple conditions on source data     | ✅        | List of `FilterCondition` (implicit AND) |
| Conditions across multiple fields      | ✅        | Each condition uses its own `field` |
| Filter repeating items                 | ✅        | Use `itemFilters` in `FieldMapping` |
| Numeric, string, list comparisons      | ✅        | Extend `passesFilters` with operators |
| Complex logic (OR, nested)             | ⚠️        | Use duplicate mappings or extend model |

This gives you **powerful, declarative control** over what data gets merged — all driven by YAML, no code changes needed per form.

Let me know if you'd like the full `passesFilters` utility method or unit tests!