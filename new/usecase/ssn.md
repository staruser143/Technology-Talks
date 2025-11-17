Excellent question! This is a **very common requirement** in PDF forms where a single SSN field in your source data needs to be **split into three separate fields** (typically for masking and security purposes).

Let me show you several approaches to handle this efficiently.

---

## 🎯 Your Requirements

**Source JSON:**
```json
{
  "applicants": [{
    "demographics": {
      "ssn": "123-45-6789"  // or "123456789"
    }
  }]
}
```

**PDF Template Fields:**
- `ssn_part1` → should get `"123"`
- `ssn_part2` → should get `"45"`
- `ssn_part3` → should get `"6789"`

---

## ✅ Solution 1: SSN Part Transforms (Recommended)

### Step 1: Create SSN Part Transforms

```java
package engine.transform;

import java.util.regex.Pattern;

public class SsnPartTransform extends BaseTransformer {
    private final int part; // 1 = first 3 digits, 2 = middle 2 digits, 3 = last 4 digits
    
    public SsnPartTransform(int part) {
        this.part = part;
    }
    
    @Override
    public Object transform(Object value, Object config) {
        if (value == null) return "";
        
        String ssn = toString(value).replaceAll("[^\\d]", ""); // Remove non-digits
        
        // Validate SSN length
        if (ssn.length() != 9) {
            return handleInvalidSsn(ssn, config);
        }
        
        switch (part) {
            case 1:
                return ssn.substring(0, 3);
            case 2:
                return ssn.substring(3, 5);
            case 3:
                return ssn.substring(5, 9);
            default:
                return "";
        }
    }
    
    private String handleInvalidSsn(String ssn, Object config) {
        // Check for custom error handling
        if (config instanceof java.util.Map) {
            java.util.Map<String, Object> args = (java.util.Map<String, Object>) config;
            String invalidHandling = (String) args.get("invalidHandling");
            
            if ("mask".equals(invalidHandling)) {
                switch (part) {
                    case 1: return "***";
                    case 2: return "**";
                    case 3: return "****";
                }
            } else if ("error".equals(invalidHandling)) {
                return "[INVALID SSN]";
            }
        }
        
        // Default: return empty or masked based on part
        switch (part) {
            case 1: return "***";
            case 2: return "**";
            case 3: return "****";
        }
    }
}
```

### Step 2: Register SSN Part Transforms

```java
// In TransformRegistry constructor
registerBuiltIn("extractSsnPart1", new SsnPartTransform(1));
registerBuiltIn("extractSsnPart2", new SsnPartTransform(2));
registerBuiltIn("extractSsnPart3", new SsnPartTransform(3));
```

### Step 3: YAML Configuration

```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

mappings:
  - source: primary.demographics.ssn
    target: "ssn_part1"
    transform: "extractSsnPart1"

  - source: primary.demographics.ssn
    target: "ssn_part2" 
    transform: "extractSsnPart2"

  - source: primary.demographics.ssn
    target: "ssn_part3"
    transform: "extractSsnPart3"
```

> ✅ **Pros**: Simple, reusable, handles invalid SSNs gracefully  
> ✅ **Cons**: Reads the same source field 3 times (negligible performance impact)

---

## ✅ Solution 2: Single SSN Decompose Transform (Advanced)

### Step 1: Create SSN Decompose Transform

```java
package engine.transform;

import java.util.Map;

public class SsnDecomposeTransform extends BaseTransformer {
    @Override
    public Object transform(Object value, Object config) {
        if (value == null) {
            return Map.of("part1", "***", "part2", "**", "part3", "****");
        }
        
        String ssn = toString(value).replaceAll("[^\\d]", "");
        
        if (ssn.length() != 9) {
            return handleInvalidSsn(config);
        }
        
        return Map.of(
            "part1", ssn.substring(0, 3),
            "part2", ssn.substring(3, 5), 
            "part3", ssn.substring(5, 9)
        );
    }
    
    private Map<String, String> handleInvalidSsn(Object config) {
        String invalidHandling = "mask";
        if (config instanceof Map) {
            Map<String, Object> args = (Map<String, Object>) config;
            invalidHandling = (String) args.getOrDefault("invalidHandling", "mask");
        }
        
        if ("error".equals(invalidHandling)) {
            return Map.of("part1", "[INVALID]", "part2", "[INVALID]", "part3", "[INVALID]");
        } else {
            return Map.of("part1", "***", "part2", "**", "part3", "****");
        }
    }
}
```

### Step 2: Enhanced Multi-Target Processing

```java
// Add to PdfFieldMapper.java
private void processSsnDecomposition(FieldMapping mapping, DocumentContext rootCtx,
                                   Map<String, Object> contextCache, MappingResult result) {
    Object rawValue = resolveValue(mapping.getSource(), rootCtx, contextCache);
    
    Object transformed = TransformRegistry.getInstance()
        .applyTransform(rawValue, mapping.getTransform());
    
    if (transformed instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> ssnParts = (Map<String, Object>) transformed;
        
        // Get field mappings from config
        Map<String, String> fieldMappings = extractSsnFieldMappings(mapping);
        
        for (Map.Entry<String, Object> entry : ssnParts.entrySet()) {
            String partName = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            
            if (fieldMappings.containsKey(partName)) {
                result.setFieldValue(fieldMappings.get(partName), value);
            }
        }
    }
}

private Map<String, String> extractSsnFieldMappings(FieldMapping mapping) {
    if (mapping.getTransform() instanceof Map) {
        Map<String, Object> transformMap = (Map<String, Object>) mapping.getTransform();
        if (transformMap.containsKey("args")) {
            Map<String, Object> args = (Map<String, Object>) transformMap.get("args");
            if (args.containsKey("fieldMappings")) {
                @SuppressWarnings("unchecked")
                Map<String, String> fieldMappings = (Map<String, String>) args.get("fieldMappings");
                return fieldMappings;
            }
        }
    }
    
    // Default field mappings
    return Map.of(
        "part1", "ssn_part1",
        "part2", "ssn_part2", 
        "part3", "ssn_part3"
    );
}
```

### Step 3: YAML Configuration

```yaml
mappings:
  - source: primary.demographics.ssn
    transform:
      name: "ssnDecompose"
      args:
        fieldMappings:
          part1: "ssn_part1"
          part2: "ssn_part2"
          part3: "ssn_part3"
        invalidHandling: "mask"
```

> ✅ **Pros**: Single source read, atomic operation  
> ❌ **Cons**: More complex, less flexible for individual field conditions

---

## ✅ Solution 3: Enhanced Field Mapping with Part Specification

### Step 1: Update FieldMapping Model

```java
// Add to FieldMapping.java
private Integer ssnPart; // 1, 2, or 3

public boolean isSsnPartMapping() {
    return ssnPart != null && ssnPart >= 1 && ssnPart <= 3;
}
```

### Step 2: Enhanced Processing Logic

```java
// In main mapping loop
for (FieldMapping mapping : config.getMappings()) {
    if (mapping.isSsnPartMapping()) {
        processSsnPartMapping(mapping, rootJson, contextCache, result);
    }
    // ... other conditions
}

private void processSsnPartMapping(FieldMapping mapping, DocumentContext rootCtx,
                                 Map<String, Object> contextCache, MappingResult result) {
    Object rawValue = resolveValue(mapping.getSource(), rootCtx, contextCache);
    String ssn = rawValue != null ? rawValue.toString() : "";
    
    String partValue = extractSsnPart(ssn, mapping.getSsnPart());
    
    boolean conditionPassed = ConditionEvaluator.evaluate(
        mapping.getCondition(), rootCtx, partValue
    );
    
    if (!conditionPassed) {
        if (mapping.getDefaultValue() != null) {
            result.setFieldValue(mapping.getTarget(), mapping.getDefaultValue());
        }
        return;
    }
    
    result.setFieldValue(mapping.getTarget(), partValue);
}

private String extractSsnPart(String ssn, int part) {
    if (ssn == null || ssn.isEmpty()) {
        return getMaskedPart(part);
    }
    
    // Remove non-digits
    String digitsOnly = ssn.replaceAll("[^\\d]", "");
    
    if (digitsOnly.length() != 9) {
        return getMaskedPart(part);
    }
    
    switch (part) {
        case 1: return digitsOnly.substring(0, 3);
        case 2: return digitsOnly.substring(3, 5);
        case 3: return digitsOnly.substring(5, 9);
        default: return getMaskedPart(part);
    }
}

private String getMaskedPart(int part) {
    switch (part) {
        case 1: return "***";
        case 2: return "**";
        case 3: return "****";
        default: return "";
    }
}
```

### Step 3: YAML Configuration

```yaml
mappings:
  - source: primary.demographics.ssn
    target: "ssn_part1"
    ssnPart: 1

  - source: primary.demographics.ssn
    target: "ssn_part2"
    ssnPart: 2

  - source: primary.demographics.ssn
    target: "ssn_part3"
    ssnPart: 3
```

> ✅ **Pros**: Clean YAML, efficient processing  
> ✅ **Cons**: Requires model changes

---

## 🧪 Handling Different SSN Formats

### Input Formats Supported:
- `"123-45-6789"` → `"123"`, `"45"`, `"6789"`
- `"123 45 6789"` → `"123"`, `"45"`, `"6789"`
- `"123456789"` → `"123"`, `"45"`, `"6789"`
- `"123-456-789"` → Invalid (masked as `***`, `**`, `****`)

### Enhanced SSN Validation (Optional)

```java
// Add to SsnPartTransform
private boolean isValidSsn(String digits) {
    if (digits.length() != 9) return false;
    
    // Check for all zeros (invalid SSN)
    if ("000000000".equals(digits)) return false;
    
    // Check for area number 666 (invalid)
    if (digits.startsWith("666")) return false;
    
    // Check for area number 900-999 (invalid)
    int area = Integer.parseInt(digits.substring(0, 3));
    if (area >= 900) return false;
    
    return true;
}
```

---

## 📄 Complete Working Example (Solution 1 - Recommended)

### JSON Input
```json
{
  "applicants": [
    {
      "type": "PRIMARY",
      "demographics": {
        "ssn": "123-45-6789"
      }
    },
    {
      "type": "DEPENDENT",
      "demographics": {
        "ssn": "987654321"  // No dashes
      }
    }
  ]
}
```

### YAML Configuration
```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true
  dependents:
    from: applicants
    filter: { type: DEPENDENT }

mappings:
  # Primary SSN
  - source: primary.demographics.ssn
    target: "primary_ssn_part1"
    transform: "extractSsnPart1"
  - source: primary.demographics.ssn
    target: "primary_ssn_part2"
    transform: "extractSsnPart2"
  - source: primary.demographics.ssn
    target: "primary_ssn_part3"
    transform: "extractSsnPart3"

  # Dependent SSN (using collection)
  - collection:
      source: dependents
      maxItems: 3
      targetPrefix: "dependent_"
      itemMappings:
        - source: demographics.ssn
          targetSuffix: "_ssn_part1"
          transform: "extractSsnPart1"
        - source: demographics.ssn
          targetSuffix: "_ssn_part2"
          transform: "extractSsnPart2"
        - source: demographics.ssn
          targetSuffix: "_ssn_part3"
          transform: "extractSsnPart3"
```

### Expected Output
```java
{
  "primary_ssn_part1": "123",
  "primary_ssn_part2": "45", 
  "primary_ssn_part3": "6789",
  "dependent_1_ssn_part1": "987",
  "dependent_1_ssn_part2": "65",
  "dependent_1_ssn_part3": "4321"
}
```

---

## ✅ Recommendation

**Use Solution 1 (SSN Part Transforms)** because it:

- ✅ **Requires minimal changes** to your existing architecture
- ✅ **Is highly reusable** (same transforms work for any SSN field)
- ✅ **Supports individual field conditions** (e.g., only mask part 3 for dependents)
- ✅ **Handles invalid SSNs gracefully** with configurable behavior
- ✅ **Is easy to understand and maintain**
- ✅ **Has negligible performance impact**

The slight redundancy of reading the same source field 3 times is **not a performance concern** compared to the benefits of simplicity and flexibility.

Would you like:
- **Support for different ID types** (EIN, ITIN, etc.)?
- **SSN validation transforms** (check for valid SSN patterns)?
- **Dynamic masking** (different masks for different user roles)?

I'm happy to extend this solution! 🛡️✨