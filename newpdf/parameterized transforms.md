Excellent question! **Parameterized transforms** are transforms that accept **arguments** (e.g., `date:MM/dd/yyyy`, `mask:***-**-****`), and supporting them in your **YAML schema** requires special handling to provide **rich IDE support** for both the **transform name** and its **parameters**.

Let’s break this down:

---

## 🔍 What Are Parameterized Transforms?

### Definition
A **parameterized transform** has the syntax:
```
transform_name:argument
```

### Examples
| Transform Spec | Meaning |
|---------------|---------|
| `date:MM/dd/yyyy` | Format date using pattern `MM/dd/yyyy` |
| `mask:***-**-****` | Mask value using pattern `***-**-****` |
| `currency:en-US` | Format as USD currency |
| `regex_replace:\d+->[NUMBER]` | Replace digits with `[NUMBER]` |

### Why They’re Tricky for Schemas
- JSON Schema doesn’t natively support **"enum with parameters"**
- You need to validate both:
  1. The **transform name** is valid (`date`, `mask`, etc.)
  2. The **argument** is valid for that transform (e.g., `MM/dd/yyyy` is a valid date pattern)

---

## 🧩 Solution Strategy

We’ll use a **hybrid approach**:
1. **Document** all parameterized transforms in `markdownDescription`
2. Use **regex pattern** for basic syntax validation
3. Provide **examples** for common patterns
4. **Runtime validation** handles complex argument validation

---

## 🛠️ Step 1: Update Transform Metadata for Parameters

### Enhanced `TransformMetadata.java`
```java
// src/main/java/com/yourcompany/pdffiller/transform/TransformMetadata.java
package com.yourcompany.pdffiller.transform;

import java.util.List;

public interface TransformMetadata {
    String name();
    String description();
    String category();
    List<String> examples();
    ParameterSpec parameter(); // NEW: parameter specification

    // For simple transforms (no parameters)
    static TransformMetadata simple(String name, String description, String category, List<String> examples) {
        return new SimpleTransformMetadata(name, description, category, examples, null);
    }

    // For parameterized transforms
    static TransformMetadata parameterized(String name, String description, String category, 
                                         ParameterSpec parameter, List<String> examples) {
        return new ParameterizedTransformMetadata(name, description, category, examples, parameter);
    }

    record ParameterSpec(String name, String description, boolean required) {}

    record SimpleTransformMetadata(
        String name, String description, String category, List<String> examples, ParameterSpec parameter
    ) implements TransformMetadata {}

    record ParameterizedTransformMetadata(
        String name, String description, String category, List<String> examples, ParameterSpec parameter
    ) implements TransformMetadata {}
}
```

---

## 🧱 Step 2: Register Parameterized Transforms with Metadata

### Update `TransformEngine.java`
```java
public TransformEngine() {
    // Simple transforms
    registerSimple("uppercase", String::toUpperCase,
        TransformMetadata.simple("uppercase", "Converts to UPPERCASE", "formatting", 
            List.of("john → JOHN")));

    // Parameterized transforms
    registerParameterized("date", (value, pattern) -> {
        try {
            return LocalDate.parse(value.trim()).format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return "INVALID_DATE";
        }
    }, TransformMetadata.parameterized("date", 
        "Formats date using Java DateTimeFormatter pattern", 
        "formatting",
        new ParameterSpec("pattern", "Date pattern (e.g., MM/dd/yyyy, yyyy-MM-dd)", true),
        List.of("2025-10-25 → 10/25/2025 (with pattern MM/dd/yyyy)")));

    registerParameterized("mask", this::applyMaskPattern,
        TransformMetadata.parameterized("mask",
            "Masks value using pattern (X = digit, * = mask)",
            "masking",
            new ParameterSpec("pattern", "Mask pattern (e.g., ***-**-****, (XXX) XXX-XXXX)", true),
            List.of("123456789 → ***-**-789 (with pattern ***-**-****)")));
}
```

---

## 📝 Step 3: Enhanced Schema Generator

### Update `SchemaGenerator.java`
```java
public String generateSchema() {
    // ... existing code ...

    // Build markdown description with ALL transforms (simple + parameterized)
    StringBuilder md = new StringBuilder("Available transforms:\n\n");
    
    for (Map.Entry<String, TransformMetadata> entry : transformEngine.getAllMetadata().entrySet()) {
        TransformMetadata meta = entry.getValue();
        md.append("- **`").append(meta.name());
        
        if (meta.parameter() != null) {
            md.append(":<").append(meta.parameter().name()).append(">");
        }
        md.append("`**\n");
        md.append("  - ").append(meta.description()).append("\n");
        
        if (meta.parameter() != null) {
            md.append("  - Parameter: `").append(meta.parameter().name())
              .append("` - ").append(meta.parameter().description()).append("\n");
        }
        
        if (!meta.examples().isEmpty()) {
            md.append("  - Examples: ");
            md.append(meta.examples().stream()
                .map(ex -> "`" + ex + "`")
                .collect(Collectors.joining(", ")));
            md.append("\n");
        }
        md.append("\n");
    }
    transform.put("markdownDescription", md.toString());

    // Add regex pattern for basic syntax validation
    // Matches: transform_name OR transform_name:argument
    transform.put("pattern", "^[a-zA-Z_][a-zA-Z0-9_]*(\\s*:\\s*.+)?$");

    // ... rest of method ...
}
```

---

## 🎯 Step 4: Generated Schema Example

The generated `pdf-form-filler.schema.json` will now include:

```json
"transform": {
  "type": "string",
  "description": "Transform to apply before setting field value",
  "pattern": "^[a-zA-Z_][a-zA-Z0-9_]*(\\s*:\\s*.+)?$",
  "markdownDescription": "Available transforms:\n\n- **`uppercase`**\n  - Converts to UPPERCASE\n  - Examples: `john → JOHN`\n\n- **`date:<pattern>`**\n  - Formats date using Java DateTimeFormatter pattern\n  - Parameter: `pattern` - Date pattern (e.g., MM/dd/yyyy, yyyy-MM-dd)\n  - Examples: `2025-10-25 → 10/25/2025 (with pattern MM/dd/yyyy)`\n\n- **`mask:<pattern>`**\n  - Masks value using pattern (X = digit, * = mask)\n  - Parameter: `pattern` - Mask pattern (e.g., ***-**-****, (XXX) XXX-XXXX)\n  - Examples: `123456789 → ***-**-789 (with pattern ***-**-****)`\n\n"
}
```

---

## 💻 Step 5: IDE Experience

### In VS Code with YAML extension:

1. **Autocomplete** shows:
   ```
   uppercase
   date:<pattern>
   mask:<pattern>
   ```

2. **Hover tooltip** shows:
   ```
   date:<pattern>
   Formats date using Java DateTimeFormatter pattern
   Parameter: pattern - Date pattern (e.g., MM/dd/yyyy, yyyy-MM-dd)
   Examples: 2025-10-25 → 10/25/2025 (with pattern MM/dd/yyyy)
   ```

3. **Syntax validation**:
   - ✅ `date:MM/dd/yyyy` → valid
   - ✅ `mask:***-**-****` → valid  
   - ❌ `invalid_transform:arg` → flagged (not in metadata)
   - ❌ `date` → valid (but runtime will handle missing parameter)

---

## ⚠️ Limitations & Workarounds

### Limitation 1: Can’t Validate Parameter Values in Schema
- **Problem**: Schema can’t validate that `MM/dd/yyyy` is a valid date pattern
- **Solution**: **Runtime validation** in `TransformEngine`

### Update `TransformEngine.validateTransformSpec()`:
```java
private void validateSingleTransform(String step) {
    var matcher = PARAM_PATTERN.matcher(step);
    if (matcher.matches()) {
        String name = matcher.group(1);
        String arg = matcher.group(2);
        TransformMetadata meta = transformMetadata.get(name);
        
        if (meta == null) {
            throw new IllegalArgumentException("Unknown transform: " + name);
        }
        
        // Validate specific parameters
        if ("date".equals(name)) {
            try {
                DateTimeFormatter.ofPattern(arg);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Invalid date pattern '" + arg + "': " + e.getMessage());
            }
        }
        // Add more parameter validation here
    }
    // ... rest ...
}
```

### Limitation 2: Complex Parameter Syntax
- **Problem**: `regex_replace:\d+->[NUMBER]` has special characters
- **Solution**: Document escaping requirements in metadata examples

---

## 🧪 Step 6: Client App Usage

### `mapping.yaml`
```yaml
# yaml-language-server: $schema=./pdf-form-filler.schema.json
fields:
  - pdf_field: "FormattedDate"
    json_path: "$.birthDate"
    transform: "date:MM/dd/yyyy"  # ← IDE shows tooltip with examples!

  - pdf_field: "MaskedSSN"
    json_path: "$.ssn"
    transform: "mask:***-**-****"
```

---

## 🔒 Advanced: Separate Schema for Each Transform Type

For **maximum validation**, you could generate a schema that uses `oneOf`:

```json
"transform": {
  "oneOf": [
    { "const": "uppercase", "description": "Converts to uppercase" },
    {
      "type": "string",
      "pattern": "^date:\\s*(.+)$",
      "description": "Date formatting"
    },
    {
      "type": "string", 
      "pattern": "^mask:\\s*(.+)$",
      "description": "Masking"
    }
  ]
}
```

But this is **complex to maintain** and **doesn’t scale** well with many transforms. The **markdownDescription + runtime validation** approach is more practical.

---

## ✅ Summary

| Aspect | Implementation |
|-------|----------------|
| **Parameterized transforms** | `transform_name:argument` syntax |
| **Schema documentation** | Include `<parameter>` in markdownDescription |
| **Basic validation** | Regex pattern for `name:arg` syntax |
| **Advanced validation** | Runtime validation of parameter values |
| **IDE experience** | Autocomplete + rich tooltips with examples |

This gives you the **best of both worlds**:
- ✅ **Developer-friendly** IDE experience
- ✅ **Robust runtime validation**
- ✅ **Scalable** as you add more transforms

Your YAML configuration becomes **self-documenting** and **hard to misuse** — perfect for enterprise environments!

Let me know if you'd like to see:
- **Chained parameterized transforms** (e.g., `trim|date:MM/dd/yyyy`)
- **Context-aware parameterized transforms**
- **Automated test generation** from transform metadata