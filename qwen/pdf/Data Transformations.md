Excellent question! Your current design **already has strong support for data transformations**, and it’s structured in a **modular, extensible way** that allows both **built-in** and **custom transformations** — applied at **multiple levels** (global, per-field, per-item).

Let’s break down how it works, where it’s applied, and how to use it effectively.

---

## ✅ 1. **Transformation Model**

You define transformations in YAML using `TransformSpec`:

```yaml
transforms:
  - type: uppercase          # built-in
  - type: custom
    name: formatDate         # Spring bean name
    params:
      inputFormat: "yyyy-MM-dd"
      outputFormat: "MM/dd/yyyy"
```

### `TransformSpec.java`
```java
public class TransformSpec {
    private String type;       // "uppercase", "custom", etc.
    private String name;       // for custom: bean name
    private Map<String, Object> params = new HashMap<>();
}
```

---

## ✅ 2. **Where Transformations Are Applied**

Your design supports transformations at **three levels**:

| Level | YAML Location | Use Case |
|------|---------------|--------|
| **Global (single-field)** | `FieldMapping.transforms` | Apply to entire single-value field |
| **Per-field (object)** | `ObjectFieldMapping.transforms` | Apply to field in `sourceObject` |
| **Per-field (repeating)** | `ItemMapping.transforms` | Apply to field in `sourceArray` items |

> ✅ **All paths go through the same `TransformerRegistry`** → consistent behavior.

---

## ✅ 3. **Transformer Registry**

The heart of the system:

### `TransformerRegistry.java`
```java
@Component
public class TransformerRegistry {

    private final Map<String, Function<Object, Object>> builtIns = new HashMap<>();
    private final ApplicationContext ctx;

    public TransformerRegistry(ApplicationContext ctx) {
        this.ctx = ctx;
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        builtIns.put("uppercase", input -> toStringOrEmpty(input).toUpperCase());
        builtIns.put("lowercase", input -> toStringOrEmpty(input).toLowerCase());
        builtIns.put("trim", input -> toStringOrEmpty(input).trim());
        builtIns.put("default", input -> input == null || toStringOrEmpty(input).isEmpty() 
            ? "N/A" : input);
    }

    public Object apply(TransformSpec spec, Object input) {
        if ("custom".equals(spec.getType())) {
            CustomTransformer transformer = ctx.getBean(spec.getName(), CustomTransformer.class);
            return transformer.transform(input, spec.getParams());
        } else {
            Function<Object, Object> fn = builtIns.get(spec.getType());
            if (fn == null) throw new IllegalArgumentException("Unknown transformer: " + spec.getType());
            return fn.apply(input);
        }
    }
}
```

---

## ✅ 4. **Built-in Transformations (Examples)**

| Transform | YAML | Effect |
|---------|------|--------|
| Uppercase | `type: uppercase` | `"john" → "JOHN"` |
| Date Format | `type: custom`, `name: formatDate` | `"2025-01-01" → "01/01/2025"` |
| Mask SSN | `type: custom`, `name: maskSsn` | `"123-45-6789" → "***-**-6789"` |
| Default Value | `type: default` | `null → "N/A"` |

---

## ✅ 5. **Custom Transformer Interface**

Clients implement this to add business logic:

### `CustomTransformer.java`
```java
public interface CustomTransformer {
    Object transform(Object input, Map<String, Object> params);
}
```

### Example: Date Formatter
```java
@Component("formatDate")
public class DateFormatTransformer implements CustomTransformer {
    @Override
    public Object transform(Object input, Map<String, Object> params) {
        if (input == null) return "";
        String inputFormat = (String) params.get("inputFormat");
        String outputFormat = (String) params.get("outputFormat");
        
        try {
            LocalDate date = LocalDate.parse(input.toString(), 
                DateTimeFormatter.ofPattern(inputFormat));
            return date.format(DateTimeFormatter.ofPattern(outputFormat));
        } catch (Exception e) {
            return input; // or throw
        }
    }
}
```

---

## ✅ 6. **How It’s Invoked in `DataMapper`**

In all mapping methods, you call:

```java
Object transformed = applyTransformations(rawValue, fieldMapping.getTransforms());
```

Which chains all transforms:

```java
private Object applyTransformations(Object value, List<TransformSpec> transforms) {
    Object current = value;
    for (TransformSpec spec : transforms) {
        current = transformerRegistry.apply(spec, current);
    }
    return current;
}
```

> ✅ **Order matters**: transforms are applied left-to-right.

---

## 📄 7. **Real-World YAML Examples**

### A. Single Field with Transform
```yaml
- sourceField: "applicant.dob"
  targetField: "dob.1"
  transforms:
    - type: custom
      name: formatDate
      params:
        inputFormat: "yyyy-MM-dd"
        outputFormat: "MM/dd/yyyy"
```

### B. Repeating Field with Masking
```yaml
- sourceArray: "applicants"
  itemFilters:
    - field: "relationship"
      operator: EQ
      value: "primary"
  itemMappings:
    - sourceField: "ssn"
      targetFieldTemplate: "ssn.{index}"
      transforms:
        - type: custom
          name: maskSsn
```

### C. Object Field with Default
```yaml
- sourceObject: "applicants"
  itemFilters:
    - field: "relationship"
      operator: EQ
      value: "spouse"
  fieldMappings:
    - sourceField: "employer"
      targetField: "spouse.employer.1"
      transforms:
        - type: default
```

---

## ✅ 8. **Advanced: Collection-Aware Transforms**

For fields that return **lists** (e.g., `sourceField: "tags"`), you can:

- **Join**: `transforms: [{type: join, params: {delimiter: ", "}}]`
- **First**: `transforms: [{type: first}]`

Implement as built-ins or custom:

```java
builtIns.put("join", input -> {
    if (!(input instanceof List)) return input.toString();
    return ((List<?>) input).stream()
        .map(obj -> obj == null ? "" : obj.toString())
        .collect(Collectors.joining(", "));
});
```

---

## ✅ 9. **Error Handling & Safety**

- **Null safety**: `safeToString()` ensures PDF fields get strings
- **Graceful failure**: Custom transformers can return original value on error
- **Type flexibility**: Transforms work on `String`, `Number`, `List`, etc.

---

## 🔒 10. **Security & Validation**

- **Validate transforms at startup**:
  ```java
  public void validate() {
      for (TransformSpec t : transforms) {
          if ("custom".equals(t.getType())) {
              // Ensure bean exists
              if (!ctx.containsBean(t.getName())) {
                  throw new IllegalStateException("Transformer bean not found: " + t.getName());
              }
          }
      }
  }
  ```
- **Avoid code injection**: Never use `eval()` or dynamic class loading

---

## ✅ Summary: Transformation Capabilities

| Feature | Supported? |
|--------|-----------|
| Built-in transforms (uppercase, trim, etc.) | ✅ |
| Custom transforms (Spring beans) | ✅ |
| Per-field transforms | ✅ |
| Parameterized transforms | ✅ |
| Chained transforms | ✅ |
| Collection-aware transforms | ✅ |
| Null-safe | ✅ |
| PDF/HTML output safe | ✅ |

Your design is **already production-ready** for transformations. The only thing missing might be **more built-in transforms** (e.g., `date`, `mask`, `join`), which you can add easily.

---

## 🚀 Recommendation

1. **Add common built-ins**:
   - `date`
   - `mask`
   - `join`
   - `substring`
2. **Document the transform interface** for clients
3. **Add validation** to ensure custom transformer beans exist

You’re all set! 🎯