Excellent use case! You have a **flat list of applicants** in JSON, where each has a `relationship` field (`"primary"`, `"spouse"`, `"dependent"`), and you need to:

- Map **exactly one `primary`** → **single-value PDF fields** (e.g., `primary.fname.1`)
- Map **optional `spouse`** → **single-value PDF fields** (e.g., `spouse.fname.1`)
- Map **zero-or-more `dependents`** → **repeating PDF fields** (e.g., `dependent.fname.1`, `dependent.fname.2`, ...)

This requires **conditional extraction based on `relationship`**, and **different mapping strategies per type**.

---

## ✅ Solution: Use **JsonPath filters** + **dedicated mappings per role**

You’ll define **three separate mappings** in YAML:
1. One for **primary applicant** (with filter `relationship == "primary"`)
2. One for **spouse** (with filter `relationship == "spouse"`)
3. One for **dependents** (with filter `relationship == "dependent"`)

> 💡 **Key Insight**:  
> Even though all applicants are in one list (`$.applicants`), you can **filter and extract each role separately** using JsonPath.

---

### 📄 Example JSON Input

```json
{
  "applicants": [
    {
      "relationship": "primary",
      "firstName": "John",
      "lastName": "Doe",
      "ssn": "123-45-6789"
    },
    {
      "relationship": "spouse",
      "firstName": "Jane",
      "lastName": "Doe",
      "ssn": "987-65-4321"
    },
    {
      "relationship": "dependent",
      "firstName": "Alice",
      "lastName": "Doe",
      "age": 10
    },
    {
      "relationship": "dependent",
      "firstName": "Bob",
      "lastName": "Doe",
      "age": 8
    }
  ]
}
```

---

### 🧩 YAML Configuration

```yaml
templates:
  - type: PDF
    templatePath: classpath:forms/application.pdf
    outputPath: /tmp/filled.pdf
    mappings:
      # === PRIMARY APPLICANT (single-value) ===
      - sourceField: "$.applicants[?(@.relationship == 'primary')].firstName"
        targetField: "primary.fname.1"
      - sourceField: "$.applicants[?(@.relationship == 'primary')].lastName"
        targetField: "primary.lname.1"
      - sourceField: "$.applicants[?(@.relationship == 'primary')].ssn"
        targetField: "primary.ssn.1"

      # === SPOUSE (single-value, optional) ===
      - sourceField: "$.applicants[?(@.relationship == 'spouse')].firstName"
        targetField: "spouse.fname.1"
      - sourceField: "$.applicants[?(@.relationship == 'spouse')].lastName"
        targetField: "spouse.lname.1"

      # === DEPENDENTS (repeating) ===
      - sourceArray: "$.applicants[?(@.relationship == 'dependent')]"
        maxRepetitions: 5
        itemMappings:
          - sourceField: "$.firstName"
            targetFieldTemplate: "dependent.fname.{index}"
          - sourceField: "$.lastName"
            targetFieldTemplate: "dependent.lname.{index}"
          - sourceField: "$.age"
            targetFieldTemplate: "dependent.age.{index}"
```

---

### 🔍 How It Works

| Mapping Type | JsonPath | Result |
|-------------|--------|--------|
| **Primary** | `$.applicants[?(@.relationship == 'primary')].firstName` | Returns a **list with 1 item** → JsonPath auto-extracts the **first (and only)** value for scalar fields |
| **Spouse** | `$.applicants[?(@.relationship == 'spouse')].firstName` | Returns a **list with 0 or 1 item** → If empty, field is skipped |
| **Dependents** | `$.applicants[?(@.relationship == 'dependent')]` | Returns a **list of dependent objects** → processed as repeating section |

> ✅ **JsonPath Behavior**:  
> When a path returns a **single-element list**, `JsonPath.read(...)` **automatically unwraps it** to the scalar value.  
> When it returns an **empty list**, it returns `null` → your `DataMapper` skips the field.

---

### ⚠️ Important Notes

#### 1. **Primary/Spouse: No `maxRepetitions` needed**
These are **single-value mappings** — JsonPath ensures only one match.

#### 2. **Dependents: Use `sourceArray` + `itemMappings`**
This is a **repeating section**, so use the repeating mapping syntax.

#### 3. **Empty spouse? Fields left blank**
If no spouse exists:
- `$.applicants[?(@.relationship == 'spouse')].firstName` → returns `null`
- Your `DataMapper` skips the field → PDF field remains **unchanged** (typically blank)

> 💡 **Tip**: If you want to **explicitly clear** unmapped fields, extend `PdfTemplateMerger` to reset all fields first.

#### 4. **Validation (Optional)**
Add a **global filter** to ensure exactly one primary applicant:

```yaml
mappings:
  - sourceField: "$.applicants[?(@.relationship == 'primary')].length()"
    targetField: "__dummy__"  # dummy field
    filters:
      - field: "$.applicants[?(@.relationship == 'primary')].length()"
        operator: EQ
        value: 1
```

But this is advanced — usually handled at data validation layer.

---

### 🧪 Edge Cases Handled

| Scenario | Behavior |
|--------|---------|
| No spouse in JSON | Spouse fields skipped (PDF shows blank) |
| Two primaries (invalid data) | JsonPath returns list → may cause error or take first — validate upstream |
| No dependents | Repeating section produces zero fields → PDF dependent slots remain blank |
| Mixed case in `relationship` | Use `== 'Primary'` or normalize with transform |

---

### 🔁 Alternative: Pre-Process with Custom Transformer (Advanced)

If your logic gets more complex (e.g., normalize relationship values), use a **custom transformer**:

```yaml
- sourceField: "$.applicants"
  targetField: "__normalized_applicants"
  transforms:
    - type: custom
      name: normalizeApplicants
```

But for your case, **JsonPath filtering is simpler and sufficient**.

---

### ✅ Summary

Use **three independent mappings** with **JsonPath filters**:
- **Primary**: `$.applicants[?(@.relationship == 'primary')].field`
- **Spouse**: `$.applicants[?(@.relationship == 'spouse')].field`
- **Dependents**: `sourceArray: $.applicants[?(@.relationship == 'dependent')]`

This is **clean, declarative, and fully supported** by your existing `DataMapper` and JsonPath engine.

No code changes needed — just configure the YAML as shown! 🚀