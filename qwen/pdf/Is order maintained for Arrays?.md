Excellent and very important question!

You're asking:  
> **When multiple mappings use the same `sourceArray` (e.g., `applicants`) with different `itemFilters` (e.g., `relationship=primary`, `relationship=dependent`), how do we ensure that:**
> 1. **The same source data is used consistently?**
> 2. **Order is preserved across mappings?**
> 3. **No element is "consumed" or skipped globally?**

Let’s break this down.

---

## ✅ Short Answer

> **Yes — your current design already ensures consistency, order, and isolation.**  
> Each mapping **independently filters the same original array**, so:
> - ✅ **Order is preserved** (uses original list order)
> - ✅ **No element is "used up"** (filters are read-only)
> - ✅ **Same source data is used** for all mappings

There is **no shared state or mutation** — each mapping gets a **fresh, independent view** of the data.

---

## 🔍 How It Works (Step by Step)

Assume this JSON:
```json
{
  "applicants": [
    { "id": 1, "relationship": "primary" },
    { "id": 2, "relationship": "spouse" },
    { "id": 3, "relationship": "dependent" },
    { "id": 4, "relationship": "dependent" }
  ]
}
```

And this YAML:
```yaml
mappings:
  - sourceObject: "applicants"
    itemFilters: [{ field: "relationship", operator: EQ, value: "primary" }]
    fieldMappings: [ ... ]

  - sourceArray: "applicants"
    itemFilters: [{ field: "relationship", operator: EQ, value: "dependent" }]
    itemMappings: [ ... ]
```

### 🔄 Execution Flow

1. **`DataMapper.mapData()`** receives the **same `jsonData` object** for all mappings.
2. For **each mapping**:
   - `SimplePathResolver.read(jsonData, "applicants")` → returns the **same `List`** (by reference or value)
   - The list is **never modified**
   - `itemFilters` are applied **independently**:
     - Mapping 1: scans list → finds `id=1`
     - Mapping 2: scans **same original list** → finds `id=3`, `id=4`
3. **No state is shared** between mappings.

> ✅ **Result**:  
> - Primary gets `id=1`  
> - Dependents get `id=3`, `id=4` (in original order)  
> - Spouse is ignored (not mapped)  
> - **All mappings see the full, unmodified source array**

---

## 🧠 Key Design Guarantees

| Guarantee | How It’s Achieved |
|---------|------------------|
| **Consistent source data** | Same `jsonData` object passed to all mappings |
| **Order preservation** | Uses `List` iteration order (JSON array order) |
| **No element consumption** | Filters are **read-only predicates** — no mutation |
| **Isolation between mappings** | Each mapping has its own filter context |

---

## ⚠️ Potential Pitfall: **Non-Deterministic Order**

If your **source JSON array order is not guaranteed**, then output order may vary.

### Example Risk:
- API returns dependents in random order
- PDF expects `dependent.fname.1` = oldest child

### ✅ Solution: **Add Sorting (Optional)**

Extend your model to support **sorting**:

```yaml
- sourceArray: "applicants"
  itemFilters:
    - field: "relationship"
      operator: EQ
      value: "dependent"
  sortBy: "age"        # ← new field
  sortOrder: ASC
```

Then in `handleRepeatingMapping`:
```java
List<?> filteredItems = items.stream()
    .filter(item -> passesFilters(item, mapping.getItemFilters()))
    .sorted(comparator) // based on sortBy
    .collect(Collectors.toList());
```

> 🔸 **But by default, your design preserves original JSON order — which is correct.**

---

## 🔒 What About Thread Safety?

- **`jsonData` is read-only** → safe to share across mappings
- **No shared mutable state** in `DataMapper`
- **Each merge operation is stateless**

✅ Safe for concurrent use (e.g., multiple PDFs generated in parallel).

---

## 📊 Real-World Behavior Summary

| Scenario | Behavior |
|--------|--------|
| Same `sourceArray` used in 3 mappings | ✅ All see full original array |
| Filters overlap (e.g., `age > 18` and `student=true`) | ✅ Each mapping applies its own filter |
| Source array order: `[A, B, C]` | ✅ Output order: `A.1, B.2, C.3` |
| One mapping modifies data | ❌ Not possible — data is read-only |

---

## ✅ Best Practices for Clients

1. **Ensure source JSON array order is meaningful** (e.g., sort dependents by age upstream)
2. **Don’t rely on side effects** — each mapping is independent
3. **Use `maxRepetitions`** to cap output if PDF has limited slots

---

## 🔚 Final Answer

> **Your design already correctly handles this**:  
> Every mapping **independently and read-only** filters the **same original source array**, preserving order and ensuring no element is "lost" or double-used.

This is **exactly the right behavior** for a declarative, functional-style data mapper.

No changes needed — you’re safe! ✅