Great question — and **yes, you absolutely *can* use `sourceArray` for the entire `applicants` list and apply **item-level filters** to selectively map only `primary`, `spouse`, or `dependents`!**

In fact, this is a **valid and simpler approach** that **doesn’t require adding the new `sourceObject` mode** — especially if you're trying to avoid code changes.

Let’s compare both strategies:

---

## ✅ Option 1: Use `sourceArray` + `itemFilters` (Your Suggestion)

### 📄 YAML Example

```yaml
mappings:
  # Primary applicant as "repeating" with maxRepetitions: 1
  - sourceArray: "$.applicants"
    itemFilters:
      - field: "$.relationship"
        operator: EQ
        value: "primary"
    maxRepetitions: 1
    itemMappings:
      - sourceField: "$.firstName"
        targetFieldTemplate: "primary.fname.{index}"   # → .1
      - sourceField: "$.lastName"
        targetFieldTemplate: "primary.lname.{index}"

  # Spouse
  - sourceArray: "$.applicants"
    itemFilters:
      - field: "$.relationship"
        operator: EQ
        value: "spouse"
    maxRepetitions: 1
    itemMappings:
      - sourceField: "$.firstName"
        targetFieldTemplate: "spouse.fname.{index}"

  # Dependents
  - sourceArray: "$.applicants"
    itemFilters:
      - field: "$.relationship"
        operator: EQ
        value: "dependent"
    maxRepetitions: 5
    itemMappings:
      - sourceField: "$.firstName"
        targetFieldTemplate: "dependent.fname.{index}"
```

### ✅ Why This Works

- `sourceArray: "$.applicants"` → gets the full list
- `itemFilters` → only processes items matching the role
- `maxRepetitions: 1` → ensures only **one** primary/spouse is mapped (even if data is invalid)
- `{index}` → becomes `1` for primary/spouse (since only one item passes)

> 🎯 **Result**:  
> - `primary.fname.1` = John  
> - `spouse.fname.1` = Jane  
> - `dependent.fname.1` = Alice, `dependent.fname.2` = Bob

### ✅ Pros
- **No code changes needed** — uses your existing `FieldMapping` model
- **Consistent pattern** — same structure for all roles
- **Leverages existing filtering logic**

### ⚠️ Minor Quirks
- You’re **abusing "repeating" for single values** (semantically odd, but functionally correct)
- Target field uses `{index}` even though you know it’ll always be `1`
- Slightly less intuitive than a dedicated "single object" mode

---

## 🆚 Option 2: New `sourceObject` Mode (DRY, Explicit)

As proposed earlier, this is **cleaner conceptually**:
- Separates **single-object** vs **array** concerns
- No `{index}` in single-field names
- More readable YAML

But requires **code changes**.

---

## 🔍 So Which Should You Choose?

| Criteria | Use `sourceArray` + `itemFilters` | Use `sourceObject` |
|--------|-------------------------------|------------------|
| **Avoid code changes** | ✅ Yes | ❌ No |
| **Already have working repeating logic** | ✅ Yes | — |
| **Want minimal YAML changes** | ✅ Yes | ❌ Need new syntax |
| **Prefer semantic clarity** | ❌ (abusing repeating) | ✅ Yes |
| **Future-proofing** | ⚠️ Okay | ✅ Better |

---

## ✅ Recommendation

> **Start with Option 1 (`sourceArray` + `itemFilters`)** — it works **today** with your current code.

It’s **simple, effective, and requires zero changes** to your library. The `{index}` quirk is harmless since you control the PDF field names.

You can always **add `sourceObject` later** if you need more complex single-object scenarios.

---

### 🧪 Real-World Validation

Given this JSON:
```json
{
  "applicants": [
    { "relationship": "primary", "firstName": "John" },
    { "relationship": "spouse", "firstName": "Jane" },
    { "relationship": "dependent", "firstName": "Alice" }
  ]
}
```

Your `sourceArray` approach produces:
- `primary.fname.1` ← John ✅
- `spouse.fname.1` ← Jane ✅
- `dependent.fname.1` ← Alice ✅

**Exactly what you need.**

---

### 💡 Pro Tip: Hide the Index in PDF Field Names

If you don’t like `.1` for single values, **just name your PDF fields without index**:
- Use `primary_fname` instead of `primary.fname.1`
- Then set `targetFieldTemplate: "primary_fname"` (no `{index}`)

But since your PDF **already uses `.1`**, stick with it — it’s standard in AcroForm templates.

---

## ✅ Final Answer

> **Yes, you can (and should!) use `sourceArray: "$.applicants"` with `itemFilters` for primary, spouse, and dependents.**  
> It’s **simpler, requires no code changes**, and works perfectly with your existing `DataMapper`.

Only consider `sourceObject` if you later need to support **non-indexed single fields** or want **semantic purity**.

Go with what works — and your current design already supports it! 🚀