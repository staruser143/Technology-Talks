**Yes — absolutely!** You can (and often should) give transformers access to the **full source data context**, not just the current field value. This is essential for complex transformations that depend on multiple fields.

Let me show you how to design this properly.

---

## 🔍 Why You Need Full Source Data Access

Common scenarios requiring full context:
- **Conditional masking**: Only mask SSN if `country != "US"`
- **Calculated fields**: `fullName = firstName + " " + lastName`  
- **Validation-based transforms**: Format phone number differently based on country
- **Cross-field references**: Use `relationship` to determine how to format `name`

---

## 🧩 Enhanced Transformer Interface

### Updated `CustomTransformer.java`
```java
package com.example.templatemerge.transformer;

import java.util.Map;

/**
 * Enhanced transformer interface with full context access
 */
public interface CustomTransformer {
    
    /**
     * Transform with only current field value (backward compatible)
     */
    default Object transform(Object input, Map<String, Object> params) {
        return transform(input, params, null);
    }
    
    /**
     * Transform with full source data context
     * @param input Current field value
     * @param params Transformer parameters from YAML
     * @param sourceData Full source JSON data (original context)
     * @return Transformed value
     */
    Object transform(Object input, Map<String, Object> params, Object sourceData);
}
```

> 🔑 **Backward compatible**: Existing transformers still work via default method

---

## 🔄 Updated TransformerRegistry

### `TransformerRegistry.java` (Enhanced)
```java
@Component
public class TransformerRegistry {
    
    private final ApplicationContext applicationContext;
    private final Object currentSourceData; // Thread-local or passed context
    
    // Store source data in thread-local for access during transformations
    private static final ThreadLocal<Object> sourceDataContext = new ThreadLocal<>();
    
    public static void setSourceDataContext(Object sourceData) {
        sourceDataContext.set(sourceData);
    }
    
    public static void clearSourceDataContext() {
        sourceDataContext.remove();
    }
    
    public Object apply(TransformSpec spec, Object input) {
        Object sourceData = sourceDataContext.get();
        
        if ("custom".equals(spec.getType())) {
            CustomTransformer transformer = applicationContext.getBean(spec.getName(), CustomTransformer.class);
            return transformer.transform(input, spec.getParams(), sourceData);
        } else {
            // Built-in transformers don't need context (or add context support if needed)
            return applyBuiltIn(spec.getType(), input, spec.getParams(), sourceData);
        }
    }
    
    private Object applyBuiltIn(String type, Object input, Map<String, Object> params, Object sourceData) {
        // Most built-ins don't need context, but you could extend them
        switch (type) {
            case "uppercase": return input.toString().toUpperCase();
            case "lowercase": return input.toString().toLowerCase();
            // Add context-aware built-ins if needed
            default:
                throw new IllegalArgumentException("Unknown transformer: " + type);
        }
    }
}
```

---

## 📄 YAML Configuration Examples

### Example 1: Conditional SSN Masking
```yaml
mappings:
  - sourceObject: "applicants"
    itemFilters:
      - field: "relationship"
        operator: EQ
        value: "primary"
    fieldMappings:
      - sourceField: "ssn"
        targetField: "primary.ssn.1"
        transforms:
          - type: custom
            name: conditionalSsnMask
            params:
              maskWhen: "country != US"  # Optional condition
```

### Example 2: Full Name Construction
```yaml
      - sourceField: "firstName"
        targetField: "primary.fullname.1"
        transforms:
          - type: custom
            name: fullNameBuilder
            params:
              lastNameField: "lastName"
              middleNameField: "middleName"
```

---

## 🧪 Custom Transformer Implementations

### Conditional SSN Masking Transformer
```java
@Component("conditionalSsnMask")
public class ConditionalSsnMaskTransformer implements CustomTransformer {
    
    @Override
    public Object transform(Object input, Map<String, Object> params, Object sourceData) {
        if (input == null) return "";
        
        String ssn = input.toString();
        
        // Extract country from full source data
        String country = extractCountry(sourceData);
        
        // Only mask if not US
        if (!"US".equals(country)) {
            return maskSsn(ssn);
        }
        
        return ssn; // Return as-is for US
    }
    
    private String extractCountry(Object sourceData) {
        // Use your existing path resolver
        Object countryObj = SimplePathResolver.read(sourceData, "country");
        return countryObj != null ? countryObj.toString() : "";
    }
    
    private String maskSsn(String ssn) {
        if (ssn.length() >= 4) {
            return "***-**-" + ssn.substring(ssn.length() - 4);
        }
        return ssn;
    }
}
```

### Full Name Builder Transformer
```java
@Component("fullNameBuilder")
public class FullNameBuilderTransformer implements CustomTransformer {
    
    @Override
    public Object transform(Object input, Map<String, Object> params, Object sourceData) {
        String firstName = input != null ? input.toString() : "";
        String lastNameField = (String) params.get("lastNameField");
        String middleNameField = (String) params.get("middleNameField");
        
        // Extract other fields from source data
        String lastName = extractField(sourceData, lastNameField);
        String middleName = extractField(sourceData, middleNameField);
        
        StringBuilder fullName = new StringBuilder();
        fullName.append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            fullName.append(" ").append(lastName);
        }
        
        return fullName.toString().trim();
    }
    
    private String extractField(Object sourceData, String fieldName) {
        if (fieldName == null) return "";
        Object value = SimplePathResolver.read(sourceData, fieldName);
        return value != null ? value.toString() : "";
    }
}
```

---

## 🧠 Integration with DataMapper

### Updated `DataMapper.java` (Critical Part)
```java
// In your mapData method, set the source data context before processing
public Map<String, Object> mapData(Object jsonData, List<FieldMapping> mappings) {
    Map<String, Object> result = new HashMap<>();
    
    try {
        // Set source data context for transformers
        TransformerRegistry.setSourceDataContext(jsonData);
        
        for (FieldMapping mapping : mappings) {
            if (!passesFilters(jsonData, mapping.getFilters())) {
                continue;
            }
            
            if (mapping.isObjectMapping()) {
                handleObjectMapping(jsonData, mapping, result);
            } else if (mapping.isRepeating()) {
                handleRepeatingMapping(jsonData, mapping, result);
            } else if (mapping.isSingleField()) {
                handleSingleValueMapping(jsonData, mapping, result);
            }
        }
    } finally {
        // Always clear context to prevent memory leaks
        TransformerRegistry.clearSourceDataContext();
    }
    
    return result;
}
```

### Handle Object and Repeating Mappings
For `sourceObject` and `sourceArray` mappings, you need to set the **item-level context**:

```java
private void handleObjectMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    Object extractedObject = readSimplePath(jsonData, mapping.getSourceObject());
    if (extractedObject == null) return;
    
    // Set item-level context for transformers
    TransformerRegistry.setSourceDataContext(extractedObject);
    
    try {
        for (ObjectFieldMapping fieldMap : mapping.getFieldMappings()) {
            Object rawValue = readSimplePath(extractedObject, fieldMap.getSourceField());
            if (rawValue == null) continue;
            Object transformed = applyTransformations(rawValue, fieldMap.getTransforms());
            result.put(fieldMap.getTargetField(), safeToString(transformed));
        }
    } finally {
        TransformerRegistry.setSourceDataContext(jsonData); // Restore parent context
    }
}
```

Similarly for repeating mappings:

```java
private void handleRepeatingMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
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
        
        // Set item-level context
        TransformerRegistry.setSourceDataContext(item);
        
        try {
            for (ItemMapping itemMapping : mapping.getItemMappings()) {
                Object rawValue = readSimplePath(item, itemMapping.getSourceField());
                if (rawValue == null) continue;
                Object transformed = applyTransformations(rawValue, itemMapping.getTransforms());
                String targetField = itemMapping.getTargetFieldTemplate()
                        .replace("{index}", String.valueOf(outputIndex));
                result.put(targetField, safeToString(transformed));
            }
        } finally {
            TransformerRegistry.setSourceDataContext(jsonData); // Restore
        }
        
        outputIndex++;
    }
}
```

---

## 🧪 Usage in Complex Scenarios

### Example: Age-Based Formatting
```java
@Component("ageFormatter")
public class AgeFormatterTransformer implements CustomTransformer {
    
    @Override
    public Object transform(Object input, Map<String, Object> params, Object sourceData) {
        if (input == null) return "";
        
        Integer age = Integer.parseInt(input.toString());
        String relationship = extractField(sourceData, "relationship");
        
        if ("dependent".equals(relationship)) {
            if (age < 13) {
                return age + " (Child)";
            } else if (age < 18) {
                return age + " (Teen)";
            }
        }
        
        return age.toString();
    }
    
    private String extractField(Object sourceData, String fieldName) {
        Object value = SimplePathResolver.read(sourceData, fieldName);
        return value != null ? value.toString() : "";
    }
}
```

### YAML Usage:
```yaml
itemMappings:
  - sourceField: "age"
    targetFieldTemplate: "dependent.age.{index}"
    transforms:
      - type: custom
        name: ageFormatter
```

---

## ⚠️ Important Considerations

### 1. **Thread Safety**
- `ThreadLocal` ensures each thread has its own context
- Always use try-finally to clear context

### 2. **Performance**
- Avoid expensive operations in transformers
- Cache path resolution if needed

### 3. **Error Handling**
- Wrap transformer logic in try-catch
- Provide meaningful error messages

### 4. **Testing**
- Easy to unit test with mock source 
```java
@Test
void testConditionalSsnMask() {
    Map<String, Object> sourceData = Map.of("country", "CA", "ssn", "123-45-6789");
    Object result = transformer.transform("123-45-6789", Map.of(), sourceData);
    assertEquals("***-**-6789", result);
}
```

---

## ✅ Benefits of This Approach

| Benefit | Explanation |
|--------|------------|
| **✅ Full context access** | Transformers can use any field from source data |
| **✅ Backward compatible** | Existing transformers continue to work |
| **✅ Flexible** | Supports complex business logic in transformations |
| **✅ Testable** | Easy to unit test with mock data |
| **✅ Performant** | Thread-local context avoids parameter passing overhead |

---

## 🔚 Final Recommendation

**Always provide full source data context to transformers** — it's essential for real-world business logic. The `ThreadLocal` approach is clean, performant, and maintains backward compatibility.

Your transformers can now implement sophisticated logic like:
- Conditional formatting
- Cross-field calculations  
- Validation-based transformations
- Country/region-specific formatting
- Role-based data masking

This makes your transformation system truly powerful and enterprise-ready! 🚀