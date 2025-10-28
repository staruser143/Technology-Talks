Excellent! Let’s extend your transformation engine to support:

✅ **Chained transforms** (e.g., `trim|uppercase|ssn_mask`)  
✅ **Context-aware transforms** (e.g., access other JSON fields like `firstName` + `lastName` → `fullName`)

This brings your PDF filler to **enterprise-grade flexibility**.

---

## 🧩 Part 1: Chained Transforms

### 🔧 Design
- Split transform spec on `|` (pipe)
- Apply transforms **left to right**
- Each transform receives output of previous

### ✅ Update `TransformEngine.java`

```java
// Add to TransformEngine.apply()
public String apply(String value, String transformSpec) {
    if (transformSpec == null || transformSpec.isEmpty()) return value;

    // Handle chaining: "trim|uppercase|ssn_mask"
    if (transformSpec.contains("|")) {
        String result = value;
        for (String step : transformSpec.split("\\|")) {
            result = applySingle(result, step.trim());
        }
        return result;
    }

    return applySingle(value, transformSpec);
}

private String applySingle(String value, String step) {
    if (step.isEmpty()) return value;

    var matcher = PARAM_PATTERN.matcher(step);
    if (matcher.matches()) {
        String name = matcher.group(1);
        String arg = matcher.group(2);
        var transform = paramTransforms.get(name);
        if (transform != null) {
            return transform.apply(value, arg);
        }
    }

    var transform = simpleTransforms.get(step);
    if (transform != null) {
        return transform.apply(value);
    }

    System.err.println("Unknown transform: " + step);
    return value;
}
```

> ✅ Now `transform: "trim|uppercase"` works!

---

## 🧩 Part 2: Context-Aware Transforms

### 🔍 What is "Context"?
The **full JSON data** (or current object) so transforms can access **other fields**.

Example:
```yaml
- pdf_field: "FullName"
  transform: "context:fullName"   # combines $.firstName + $.lastName
```

### 🛠️ Step 1: Update Field Mapping to Carry Context

#### Update `SingleField` record:
```java
public record SingleField(
    String pdf_field,
    String json_path,
    String transform,
    Boolean multi,
    String delimiter,
    // New: whether transform needs full context
    Boolean needs_context  // optional, default false
) implements FieldOrBlock {
    public SingleField {
        if (multi == null) multi = false;
        if (delimiter == null || delimiter.isEmpty()) delimiter = ", ";
        if (needs_context == null) needs_context = false;
    }
}
```

> 💡 In YAML, use `needs_context: true` to enable.

---

### 🛠️ Step 2: Pass Context to Transform Engine

#### Update `fillSingleField` in `PdfFormFiller.java`:
```java
private static void fillSingleField(PDAcroForm form, String jsonText, SingleField field) {
    String value;
    if (field.needs_context()) {
        // Pass full JSON context
        value = TRANSFORM_ENGINE.applyWithContext(jsonText, field.json_path(), field.transform());
    } else {
        // Old behavior
        String rawValue = evaluateJsonPath(jsonText, field.json_path(), field.multi());
        value = applyTransform(rawValue, field.transform());
    }
    setPdfField(form, field.pdf_field(), value);
}
```

---

### 🛠️ Step 3: Add Contextual Transform Support

#### Update `TransformEngine.java`

```java
// Add contextual transforms registry
private final Map<String, ContextualTransform> contextualTransforms = new HashMap<>();

// Register in constructor
public TransformEngine() {
    // ... existing registrations ...

    // Contextual examples
    registerContextual("fullName", (currentValue, jsonContext, jsonPath) -> {
        try {
            String first = JsonPath.read(jsonContext, "$.firstName");
            String last = JsonPath.read(jsonContext, "$.lastName");
            return (first + " " + last).trim();
        } catch (Exception e) {
            return currentValue; // fallback
        }
    });

    registerContextual("ageFromDob", (currentValue, jsonContext, jsonPath) -> {
        try {
            String dob = JsonPath.read(jsonContext, "$.dateOfBirth");
            java.time.LocalDate birth = java.time.LocalDate.parse(dob);
            int age = java.time.Period.between(birth, java.time.LocalDate.now()).getYears();
            return String.valueOf(age);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    });
}

// Registration method
public void registerContextual(String name, ContextualTransform transform) {
    contextualTransforms.put(name, transform);
}

// New apply method
public String applyWithContext(String jsonContext, String jsonPath, String transformSpec) {
    if (transformSpec == null || transformSpec.isEmpty()) {
        // Fallback to normal path evaluation
        return evaluateJsonPathInContext(jsonContext, jsonPath);
    }

    // Check for contextual transform: "context:fullName"
    if (transformSpec.startsWith("context:")) {
        String transformName = transformSpec.substring(8);
        var transform = contextualTransforms.get(transformName);
        if (transform != null) {
            String currentValue = evaluateJsonPathInContext(jsonContext, jsonPath);
            return transform.apply(currentValue, jsonContext, jsonPath);
        }
    }

    // Fallback: treat as regular transform on the field's value
    String value = evaluateJsonPathInContext(jsonContext, jsonPath);
    return apply(value, transformSpec);
}

// Helper: evaluate JsonPath in full context
private String evaluateJsonPathInContext(String jsonContext, String jsonPath) {
    try {
        Object result = JsonPath.read(jsonContext, jsonPath);
        return toStringSafe(result);
    } catch (Exception e) {
        return "";
    }
}

// Helper
private static String toStringSafe(Object obj) {
    return obj == null ? "" : obj.toString();
}

// Functional interface
@FunctionalInterface
public interface ContextualTransform {
    String apply(String currentValue, String fullJsonContext, String originalJsonPath);
}
```

> ⚠️ **Note**: You’ll need to add `com.jayway.jsonpath` import to `TransformEngine`.

---

## 📄 Part 3: YAML Examples

### `mapping.yaml`
```yaml
fields:
  # Chained transforms
  - pdf_field: "CleanName"
    json_path: "$.name"
    transform: "trim|lowercase|capitalize"   # you'd implement capitalize

  # Context-aware: combine fields
  - pdf_field: "FullName"
    json_path: "$.dummy"  # not used, but required
    transform: "context:fullName"
    needs_context: true

  # Context-aware: calculate age
  - pdf_field: "Age"
    json_path: "$.dateOfBirth"
    transform: "context:ageFromDob"
    needs_context: true

  # Mixed: chain + context (advanced)
  - pdf_field: "FormattedFullName"
    json_path: "$.dummy"
    transform: "context:fullName|uppercase"
    needs_context: true
```

> 💡 For `capitalize`, add to `TransformEngine`:
> ```java
> registerSimple("capitalize", s -> {
>     if (s == null || s.isEmpty()) return s;
>     return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
> });
> ```

---

## 🧪 Part 4: Update Integration Test

### `data.json`
```json
{
  "firstName": "john",
  "lastName": "doe",
  "dateOfBirth": "1990-05-15",
  "name": "  ALICE  "
}
```

### Test Case
```java
@Test
void supportsChainedAndContextualTransforms() throws Exception {
    Path data = writeTempFile("""
        {
          "firstName": "john",
          "lastName": "doe",
          "dateOfBirth": "1990-05-15",
          "name": "  ALICE  "
        }
        """);

    Path mapping = writeTempFile("""
        fields:
          - pdf_field: "CleanName"
            json_path: "$.name"
            transform: "trim|lowercase|capitalize"
          - pdf_field: "FullName"
            json_path: "$.dummy"
            transform: "context:fullName"
            needs_context: true
          - pdf_field: "Age"
            json_path: "$.dateOfBirth"
            transform: "context:ageFromDob"
            needs_context: true
        """);

    // Generate simple PDF with these fields
    Path pdf = generateTestPdfWithFields("CleanName", "FullName", "Age");
    Path output = tempDir.resolve("output.pdf");

    PdfFormFiller.fillForm(pdf.toString(), data.toString(), mapping.toString(), output.toString());

    try (var doc = PDDocument.load(output.toFile())) {
        var form = doc.getDocumentCatalog().getAcroForm();
        assertThat(form.getField("CleanName").getValueAsString()).isEqualTo("Alice");
        assertThat(form.getField("FullName").getValueAsString()).isEqualTo("john doe");
        // Age depends on current year — adjust as needed
        assertThat(form.getField("Age").getValueAsString()).matches("\\d+");
    }
}
```

---

## 🔒 Safety & Best Practices

| Concern | Solution |
|--------|----------|
| **Performance** | Cache compiled JsonPath expressions (optional) |
| **Error handling** | Wrap contextual transforms in try-catch |
| **Security** | Don’t allow arbitrary code execution (e.g., no Groovy eval) |
| **Testing** | Unit-test each contextual transform with sample JSON |

---

## 🚀 Advanced: Nested Context for Repeats

For **repeat blocks**, pass the **current item JSON** as context:

#### Update `fillBlockItems`:
```java
if (item instanceof SingleField field) {
    String value;
    if (field.needs_context()) {
        // Use ctx.itemJson as context (current array item)
        value = TRANSFORM_ENGINE.applyWithContext(ctx.itemJson, field.json_path(), field.transform());
    } else {
        String rawValue = evaluateRelativeJsonPath(ctx.itemJson, field.json_path());
        value = applyTransform(rawValue, field.transform());
    }
    // ... rest same
}
```

Now in a dependent repeat:
```yaml
- pdf_field: "Dependent.FullName.{index}"
  json_path: "$.dummy"
  transform: "context:fullName"
  needs_context: true
```
→ Combines `$.firstName` + `$.lastName` **within the current dependent object**.

---

## ✅ Summary

You now support:

| Feature | Syntax | Use Case |
|--------|--------|---------|
| **Chained transforms** | `trim|uppercase|ssn_mask` | Clean + format data |
| **Context-aware transforms** | `context:fullName` + `needs_context: true` | Combine/calculate from multiple fields |
| **Nested context** | Works in repeat blocks | Per-item calculations |

This gives you **unlimited flexibility** while keeping configuration **declarative and testable**.

Let me know if you'd like to add:
- **Custom transform plugins** (loaded from JARs)
- **Transform caching** for performance
- **Validation transforms** (e.g., fail if SSN invalid)