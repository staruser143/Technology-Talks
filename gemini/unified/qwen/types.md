You're absolutely right to question this — if you don't have `targetVariable` in your current implementation, you don't need to add it! Let me clarify the **practical, incremental approach** to support HTML templates with your existing codebase.

---

## 🔍 First: Your Current Implementation Likely *Already Works* for HTML

### ✅ Check Your `FieldMapping` Class
If it has this structure:
```java
public class FieldMapping {
    private String sourceField;
    private String targetField;  // ← This works for BOTH PDF and HTML
    // ... other fields
}
```

Then you can use `targetField` for HTML too — **no new fields needed**.

The difference is in **how you use the value**, not the field name.

---

## 📄 Simple YAML for HTML (Using Existing `targetField`)

```yaml
templates:
  - name: "summary-html"
    type: HTML
    mappings:
      # Use targetField for HTML variables too!
      - sourceField: "applicants"
        targetField: "applicants"  # ← Becomes ${applicants} in FreeMarker
      
      - sourceField: "applicants[relationship=dependent]"
        targetField: "dependents"  # ← Becomes ${dependents}
      
      - sourceField: "metadata"
        targetField: "metadata"    # ← Becomes ${metadata}
```

### How It Works:
- `targetField: "applicants"` becomes a key in the `Map<String, Object>`
- FreeMarker uses the **key name** as the variable name: `${applicants}`
- The **value** remains as the original Java object (List, Map, etc.)

No code changes needed — your current `DataMapper` already supports this!

---

## 🚀 Enhanced: Template-Specific Mapping Engines (Practical Implementation)

Let me show you how to implement separate mapping engines **without breaking your existing code**.

### Phase 1: Minimal Changes (Works Today)

#### **Step 1: Detect Template Type in DataMapper**
```java
// In your existing DataMapper.mapData()
public Map<String, Object> mapData(Object jsonData, MergeConfig config, TemplateType templateType) {
    Map<String, Object> result = new HashMap<>();
    
    for (FieldMapping mapping : config.getMappings()) {
        if (templateType == TemplateType.HTML) {
            handleHtmlMapping(jsonData, mapping, result);
        } else {
            handlePdfMapping(jsonData, mapping, result);
        }
    }
    return result;
}

private void handleHtmlMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    Object value = SimplePathResolver.read(jsonData, mapping.getSourceField());
    if (value == null) return;
    
    // ✅ Preserve object structure for HTML
    Object transformed = applyTransformations(value, mapping.getTransforms());
    result.put(mapping.getTargetField(), transformed); // ← Don't convert to String!
}

private void handlePdfMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    Object value = SimplePathResolver.read(jsonData, mapping.getSourceField());
    if (value == null) return;
    
    // ✅ Convert to String for PDF
    Object transformed = applyTransformations(value, mapping.getTransforms());
    result.put(mapping.getTargetField(), safeToString(transformed));
}
```

#### **Step 2: Update TemplateMergeService**
```java
// In TemplateMergeService
public void mergeTemplate(String templateName, Object sourceData, Path outputPath) {
    TemplateDefinition def = mergeConfig.getTemplateByName(templateName);
    
    // ✅ Pass template type to DataMapper
    Map<String, Object> mappedData = dataMapper.mapData(sourceData, def, def.getType());
    
    TemplateMerger merger = mergers.get(def.getType());
    merger.merge(mappedData, def);
}
```

**That's it!** Your existing YAML works for both:
- PDF: `targetField: "primary.fname.1"` → becomes string
- HTML: `targetField: "applicants"` → becomes List object

---

## 🧩 Phase 2: Enhanced Mapping Engines (Optional)

If you want cleaner separation, here's how to implement it incrementally:

### Step 1: Create Interface
```java
public interface MappingEngine {
    Map<String, Object> mapData(Object jsonData, List<FieldMapping> mappings);
}
```

### Step 2: Implement PDF Engine
```java
@Component
public class PdfMappingEngine implements MappingEngine {
    
    private final TransformerRegistry transformerRegistry;
    
    @Override
    public Map<String, Object> mapData(Object jsonData, List<FieldMapping> mappings) {
        Map<String, String> result = new HashMap<>(); // String values only
        
        for (FieldMapping mapping : mappings) {
            if (mapping.isSingleField()) {
                Object value = SimplePathResolver.read(jsonData, mapping.getSourceField());
                if (value == null) continue;
                
                Object transformed = transformerRegistry.applyAll(mapping.getTransforms(), value);
                result.put(mapping.getTargetField(), safeToString(transformed));
            }
            // ... handle other mapping types
        }
        
        // Convert to Map<String, Object> for compatibility
        return new HashMap<>(result);
    }
    
    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
```

### Step 3: Implement HTML Engine
```java
@Component
public class HtmlMappingEngine implements MappingEngine {
    
    private final TransformerRegistry transformerRegistry;
    
    @Override
    public Map<String, Object> mapData(Object jsonData, List<FieldMapping> mappings) {
        Map<String, Object> model = new HashMap<>(); // Rich objects allowed
        
        for (FieldMapping mapping : mappings) {
            if (mapping.isSingleField()) {
                Object value = SimplePathResolver.read(jsonData, mapping.getSourceField());
                if (value == null) continue;
                
                // ✅ Allow complex objects to be returned from transformers
                Object transformed = transformerRegistry.applyAllForHtml(mapping.getTransforms(), value, jsonData);
                model.put(mapping.getTargetField(), transformed);
            }
        }
        return model;
    }
}
```

### Step 4: Enhanced TransformerRegistry
```java
@Component
public class TransformerRegistry {
    
    // Existing method (for PDF)
    public Object applyAll(List<TransformSpec> transforms, Object input) {
        Object current = input;
        for (TransformSpec spec : transforms) {
            current = apply(spec, current); // returns strings for PDF
        }
        return current;
    }
    
    // New method (for HTML)
    public Object applyAllForHtml(List<TransformSpec> transforms, Object input, Object sourceData) {
        Object current = input;
        for (TransformSpec spec : transforms) {
            // Pass sourceData for context-aware transforms
            current = applyForHtml(spec, current, sourceData);
        }
        return current;
    }
    
    private Object applyForHtml(TransformSpec spec, Object input, Object sourceData) {
        if ("custom".equals(spec.getType())) {
            CustomTransformer transformer = applicationContext.getBean(spec.getName(), CustomTransformer.class);
            // Use the enhanced method that accepts sourceData
            return transformer.transform(input, spec.getParams(), sourceData);
        }
        return apply(spec, input); // fall back to existing logic
    }
}
```

---

## 📄 Updated YAML Examples (Using Existing `targetField`)

### PDF Template (Flattened Fields)
```yaml
templates:
  - name: "application-pdf"
    type: PDF
    mappings:
      - sourceField: "applicants[relationship=primary].firstName"
        targetField: "primary.fname.1"  # ← PDF field name
      - sourceField: "metadata.submissionId"
        targetField: "submission_id.1"   # ← PDF field name
```

### HTML Template (Rich Objects)
```yaml
templates:
  - name: "summary-html"  
    type: HTML
    mappings:
      - sourceField: "applicants"
        targetField: "applicants"  # ← HTML variable name (same field!)
      - sourceField: "applicants[relationship=dependent]"
        targetField: "dependents"  # ← HTML variable name
      - sourceField: "metadata"
        targetField: "metadata"    # ← HTML variable name
```

### FreeMarker Template Using These Variables
```html
<!DOCTYPE html>
<html>
<body>
    <!-- Uses targetField names directly -->
    <h1>Applicants (${applicants?size})</h1>
    
    <#list applicants as app>
        <div class="applicant">
            <h3>${app.firstName} ${app.lastName}</h3>
            <p>Relationship: ${app.relationship}</p>
            
            <!-- Nested data access -->
            <#if app.addresses??>
                <h4>Addresses</h4>
                <#list app.addresses as addr>
                    <p>${addr.type}: ${addr.street}</p>
                </#list>
            </#if>
        </div>
    </#list>
    
    <h2>Dependents (${dependents?size})</h2>
    <#list dependents as dep>
        <p>${dep.firstName} (${dep.age})</p>
    </#list>
    
    <p>Submission ID: ${metadata.submissionId}</p>
</body>
</html>
```

---

## 🔧 Key Implementation Points

### 1. **No YAML Schema Changes Needed**
- Keep using `targetField` for both PDF and HTML
- The **meaning** differs by template type, but the **field name** stays the same

### 2. **DataMapper Decides Based on Template Type**
```java
// This is the only change you need
if (templateType == TemplateType.HTML) {
    // Preserve object structure
    result.put(mapping.getTargetField(), transformed);
} else {
    // Flatten to string
    result.put(mapping.getTargetField(), transformed.toString());
}
```

### 3. **Your Existing YAML Works Immediately**
```yaml
# This works for HTML right now!
- sourceField: "applicants"
  targetField: "applicants"
```

### 4. **FreeMarker Gets the Full Object Graph**
The `Map<String, Object>` passed to FreeMarker contains:
- `applicants` → List of Maps (each applicant as Map)
- `dependents` → Filtered List of Maps
- `metadata` → Map of metadata fields

FreeMarker can navigate this naturally: `${applicants[0].addresses[0].street}`

---

## 🚀 Migration Strategy

### Week 1: **Minimal Change**
1. Add template type parameter to `DataMapper.mapData()`
2. Implement the if/else logic for HTML vs PDF
3. Test with simple HTML template

### Week 2: **Enhance Transformers**
1. Update `CustomTransformer` interface to accept `sourceData`
2. Modify `TransformerRegistry` to pass context to HTML transforms

### Week 3: **Optional Cleanup**
1. Extract separate mapping engines if code becomes too complex
2. Add validation to ensure HTML mappings don't use PDF-style field names

---

## ✅ Summary

**You don't need `targetVariable`** — your existing `targetField` works perfectly for HTML templates.

**The key insight is**:  
- **PDF**: `targetField` = PDF field name → value must be string  
- **HTML**: `targetField` = FreeMarker variable name → value can be any object

**Implementation**: Add one parameter (`TemplateType`) to your `DataMapper.mapData()` method and use it to decide whether to preserve object structure (HTML) or flatten to strings (PDF).

This gives you **immediate HTML support** with minimal changes to your existing, working codebase! 🚀