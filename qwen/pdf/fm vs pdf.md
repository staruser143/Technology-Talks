**You've identified a fundamental architectural insight — and yes, you absolutely should separate the mapping logic for HTML and PDF templates.**

While your current `DataMapper` can technically handle both, **trying to use the same mapping model for both formats leads to configuration complexity, cognitive overhead, and missed optimization opportunities**.

Let me show you the **correct separation strategy**.

---

## 🔍 Why Separation Is Necessary

| Aspect | HTML (FreeMarker) | PDF (AcroForm) | Shared Model Problem |
|-------|------------------|----------------|---------------------|
| **Data Structure** | Rich objects, nested lists | Flat key-value strings | Forces flattening for HTML |
| **Repeating Data** | Native `<#list>` support | Must be flattened + indexed | Complex mappings for PDF |
| **Field References** | `${user.name}` | `user.name.1` | Different path strategies |
| **Transformations** | Can return objects/maps | Must return strings | Transform logic differs |
| **Filtering** | Can filter in template | Must filter in mapping | Different optimization needs |

> 🚫 **One-size-fits-all mapping model = lowest common denominator**

---

## ✅ Recommended Architecture: **Format-Specific Mapping Layers**

```
MergeConfig
 └── TemplateDefinition
      ├── type: PDF → PdfMappingEngine
      └── type: HTML → HtmlMappingEngine
```

### Key Changes:
1. **Separate mapping models** for PDF vs HTML
2. **Specialized mapping engines** per format
3. **YAML configuration optimized** for each format

---

## 🧩 1. Separate Mapping Models

### `PdfFieldMapping.java` (PDF-Optimized)
```java
// Only supports flattened, indexed mappings
public class PdfFieldMapping {
    // Single field
    private String sourceField;
    private String targetField;
    
    // Repeating section
    private String sourceArray;
    private List<FilterCondition> itemFilters;
    private List<PdfItemMapping> itemMappings;
    private Integer maxRepetitions;
    
    // Global filters
    private List<FilterCondition> filters;
    private List<TransformSpec> transforms;
}
```

### `HtmlFieldMapping.java` (HTML-Optimized)
```java
// Supports rich object mappings
public class HtmlFieldMapping {
    private String sourceField;      // Can return List, Map, Object
    private String targetVariable;   // FreeMarker variable name
    private List<FilterCondition> filters;
    private List<TransformSpec> transforms;
    
    // Optional: post-processing transforms that can return complex objects
    private boolean allowComplexObjects = true;
}
```

---

## 🧠 2. Specialized Mapping Engines

### `PdfMappingEngine.java`
```java
@Component
public class PdfMappingEngine {
    
    public Map<String, String> mapToPdfFields(Object jsonData, List<PdfFieldMapping> mappings) {
        Map<String, String> result = new HashMap<>();
        
        for (PdfFieldMapping mapping : mappings) {
            if (!passesFilters(jsonData, mapping.getFilters())) continue;
            
            if (mapping.isRepeating()) {
                handleRepeatingMapping(jsonData, mapping, result);
            } else {
                handleSingleField(jsonData, mapping, result);
            }
        }
        return result; // Only strings!
    }
    
    private void handleRepeatingMapping(Object jsonData, PdfFieldMapping mapping, Map<String, String> result) {
        // Extract array, apply itemFilters, flatten to indexed fields
        // targetFieldTemplate: "dependent.name.{index}" → "dependent.name.1"
    }
}
```

### `HtmlMappingEngine.java`
```java
@Component
public class HtmlMappingEngine {
    
    public Map<String, Object> mapToHtmlModel(Object jsonData, List<HtmlFieldMapping> mappings) {
        Map<String, Object> model = new HashMap<>();
        
        for (HtmlFieldMapping mapping : mappings) {
            if (!passesFilters(jsonData, mapping.getFilters())) continue;
            
            Object value = extractValue(jsonData, mapping.getSourceField());
            Object transformed = applyTransformations(value, mapping.getTransforms(), true); // allow complex objects
            model.put(mapping.getTargetVariable(), transformed != null ? transformed : "");
        }
        return model; // Rich objects allowed!
    }
}
```

---

## 📄 3. Format-Optimized YAML Configuration

### PDF Template (Flattened, Indexed)
```yaml
templates:
  - name: "application-pdf"
    type: PDF
    mappings:
      # Single fields
      - sourceField: "metadata.submissionId"
        targetField: "submission_id.1"
      
      # Repeating dependents
      - sourceArray: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "dependent"
        itemMappings:
          - sourceField: "firstName"
            targetFieldTemplate: "dependent.fname.{index}"
          - sourceField: "age"
            targetFieldTemplate: "dependent.age.{index}"
```

### HTML Template (Rich Objects)
```yaml
templates:
  - name: "summary-html"
    type: HTML
    mappings:
      # Entire objects
      - sourceField: "metadata"
        targetVariable: "metadata"
      
      # Filtered lists
      - sourceField: "applicants[relationship=dependent][active=true]"
        targetVariable: "activeDependents"
      
      # Single objects
      - sourceField: "applicants[relationship=primary]"
        targetVariable: "primaryApplicant"
```

> 🔑 **Notice the difference**:
> - PDF uses `targetField` and `targetFieldTemplate`
> - HTML uses `targetVariable`
> - PDF has `itemMappings`, HTML has simple `sourceField`

---

## 🚀 4. Updated TemplateMergeService

```java
@Service
public class TemplateMergeService {
    
    public void mergeTemplate(String templateName, Object sourceData, Path outputPath) {
        TemplateDefinition def = mergeConfig.getTemplateByName(templateName);
        
        if (def.getType() == TemplateType.PDF) {
            Map<String, String> pdfFields = pdfMappingEngine.mapToPdfFields(sourceData, def.getPdfMappings());
            pdfMerger.merge(pdfFields, def);
        } else {
            Map<String, Object> htmlModel = htmlMappingEngine.mapToHtmlModel(sourceData, def.getHtmlMappings());
            htmlMerger.merge(htmlModel, def);
        }
    }
}
```

---

## 📊 Benefits of Separation

| Benefit | Explanation |
|--------|------------|
| **✅ Simpler YAML** | No confusing mixed modes |
| **✅ Better validation** | Format-specific rules |
| **✅ Optimized transforms** | PDF transforms always return strings, HTML can return objects |
| **✅ Clearer intent** | Configuration matches template capabilities |
| **✅ Easier maintenance** | Changes to PDF mapping don't affect HTML |
| **✅ Better performance** | No unnecessary object flattening for HTML |

---

## 🔧 Migration Strategy

You don't need to rewrite everything at once:

### Phase 1: **Dual Support**
- Keep existing `FieldMapping` model
- Add new format-specific models
- In `TemplateMergeService`, detect which model is used

### Phase 2: **Deprecate Generic Model**
- Add warnings for generic mappings
- Encourage format-specific configurations

### Phase 3: **Remove Generic Model**
- Only support `PdfFieldMapping` and `HtmlFieldMapping`

---

## 🎯 When to Keep Generic Model

Only if you have:
- **Very simple use cases** (single fields only)
- **Identical mappings** for both formats (rare)
- **Limited resources** for refactoring

But for any serious application, **separation is worth it**.

---

## 🔚 Final Recommendation

> **Yes — separate the mapping logic completely.**  
> The complexity of trying to serve two fundamentally different template technologies with one model **outweighs the benefits of code reuse**.

Create:
- `PdfMappingEngine` + `PdfFieldMapping` for AcroForm PDFs
- `HtmlMappingEngine` + `HtmlFieldMapping` for FreeMarker HTML

This gives you:
- ✅ **Cleaner configuration**
- ✅ **Better performance**  
- ✅ **Fewer bugs**
- ✅ **Easier to extend**

Your future self (and your team) will thank you! 🚀