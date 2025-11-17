Great question! You're absolutely right to ask about the integration point. The `processMultiSourceMapping` method needs to be **called from your main mapping loop**, but you'll also need to **enhance your `FieldMapping` model** to detect when a mapping is multi-source.

Let me show you the complete integration:

---

## 🔧 Step 1: Enhanced FieldMapping Model

First, update your `FieldMapping.java` to support the `sources` field and detection methods:

```java
package model;

import java.util.List;

public class FieldMapping {
    // Existing fields
    private String source;           // For single source
    private List<String> sources;    // For multiple sources (NEW)
    private String target;
    private Object transform;
    private Condition condition;
    private String defaultValue;
    private CollectionMapping collection;

    // Detection methods
    public boolean isScalar() {
        return source != null && target != null;
    }

    public boolean isMultiSource() {
        return sources != null && !sources.isEmpty() && target != null;
    }

    public boolean isCollection() {
        return collection != null;
    }

    // Getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }
    
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    
    // ... other getters/setters
}
```

---

## 🔧 Step 2: Update the Main Mapping Loop

In your `PdfFieldMapper.mapJsonToFields()` method, update the loop that processes mappings:

```java
// In PdfFieldMapper.java - inside mapJsonToFields() method
for (FieldMapping mapping : config.getMappings()) {
    if (mapping.isMultiSource()) {
        // Handle multi-source mappings
        processMultiSourceMapping(mapping, rootJson, contextCache, result);
    }
    else if (mapping.isScalar()) {
        // Handle single-source mappings  
        processScalarToResult(mapping, rootJson, contextCache, result);
    }
    else if (mapping.isCollection()) {
        // Handle collection mappings
        processCollectionToResult(
            mapping.getCollection(),
            rootJson,
            contextCache,
            result,
            "", null, 0
        );
    }
}
```

This is where `processMultiSourceMapping` gets **invoked** — it's called when the mapping configuration has a `sources` list instead of a single `source`.

---

## 📄 Step 3: YAML Configuration Structure

Your YAML now supports two syntaxes:

### Single Source (existing):
```yaml
mappings:
  - source: primary.demographics.firstName
    target: primary_firstName
    transform: "toUpperCase"
```

### Multi-Source (new):
```yaml
mappings:
  - sources:
      - primary.demographics.firstName
      - primary.demographics.lastName
    target: primary_fullName
    transform: "concat"
```

SnakeYAML will automatically bind:
- `source: "value"` → `fieldMapping.setSource("value")`
- `sources: [val1, val2]` → `fieldMapping.setSources(Arrays.asList("val1", "val2"))`

---

## 🔧 Step 4: Complete `processMultiSourceMapping` Method

Here's the complete implementation with proper error handling:

```java
private void processMultiSourceMapping(FieldMapping mapping, DocumentContext rootCtx,
                                     Map<String, Object> contextCache, MappingResult result) {
    // Collect all source values into a Map
    Map<String, Object> sourceValues = new HashMap<>();
    
    for (String sourcePath : mapping.getSources()) {
        // Extract a meaningful key name from the source path
        String keyName = extractKeyFromSourcePath(sourcePath);
        Object value = resolveValue(sourcePath, rootCtx, contextCache);
        sourceValues.put(keyName, value);
    }
    
    // Evaluate condition (if any) against the combined source values
    boolean conditionPassed = ConditionEvaluator.evaluate(
        mapping.getCondition(), 
        rootCtx, 
        sourceValues  // Pass the entire map as the "value" for condition evaluation
    );
    
    if (!conditionPassed) {
        if (dryRun) {
            System.out.println("⏭️  Skipped multi-source mapping (condition failed): " + mapping.getTarget());
        }
        return;
    }
    
    // Apply transform to the combined source values
    Object transformed = TransformRegistry.getInstance()
        .applyTransform(sourceValues, mapping.getTransform());
    
    // Handle final value with defaults
    String finalValue = (transformed != null) ? transformed.toString() : "";
    if (finalValue.trim().isEmpty() && mapping.getDefaultValue() != null) {
        finalValue = mapping.getDefaultValue();
    }
    
    // Set the result
    result.setFieldValue(mapping.getTarget(), finalValue);
    
    if (dryRun) {
        String safeVal = SensitiveFieldDetector.isSensitive(mapping.getTarget()) ?
            SensitiveFieldDetector.maskValue(finalValue, mapping.getTarget().contains("email")) :
            finalValue;
        System.out.println("✅ " + mapping.getTarget() + " = '" + safeVal + "'");
    }
}

private String extractKeyFromSourcePath(String sourcePath) {
    // Extract the last part after the final dot
    // e.g., "primary.demographics.firstName" → "firstName"
    if (sourcePath == null || sourcePath.isEmpty()) {
        return "value";
    }
    
    int lastDot = sourcePath.lastIndexOf('.');
    if (lastDot != -1 && lastDot < sourcePath.length() - 1) {
        return sourcePath.substring(lastDot + 1);
    }
    
    return sourcePath;
}
```

---

## 🧪 Step 5: Example with Condition Evaluation

If you want to use conditions with multi-source mappings, your condition can evaluate against the combined data:

```yaml
- sources:
    - primary.demographics.firstName
    - primary.demographics.lastName
  target: primary_fullName
  condition:
    type: "notNull"
    field: "firstName"  # This refers to the extracted key name
  transform: "concat"
```

The condition evaluator will receive a `Map` like:
```java
{
  "firstName": "John",
  "lastName": "Doe"
}
```

And your existing `ConditionEvaluator` should be able to handle this (you may need to enhance it to work with Map contexts).

---

## 🔍 How SnakeYAML Binds the YAML

Given this YAML:
```yaml
mappings:
  - sources:
      - primary.demographics.firstName
      - primary.demographics.lastName
    target: primary_fullName
    transform: "concat"
```

SnakeYAML will:
1. Create a new `FieldMapping` object
2. Call `setSources(Arrays.asList("primary.demographics.firstName", "primary.demographics.lastName"))`
3. Call `setTarget("primary_fullName")`
4. Call `setTransform("concat")`

This makes `isMultiSource()` return `true`, so your main loop calls `processMultiSourceMapping`.

---

## ✅ Summary

**Integration Flow:**
1. **YAML config** defines `sources` (plural) instead of `source`
2. **SnakeYAML** binds this to `FieldMapping.sources` list
3. **Main mapping loop** detects `isMultiSource()` and calls `processMultiSourceMapping`
4. **Method collects all source values**, applies transforms, and sets the target field

This approach maintains **backward compatibility** (existing single-source mappings still work) while adding powerful multi-source capabilities.

Would you like me to show how to enhance the `ConditionEvaluator` to work with multi-source Map contexts, or provide unit tests for this integration?