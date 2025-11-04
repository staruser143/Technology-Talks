**Yes — absolutely!** Your post-processing architecture should provide **full access to all relevant data** to enable powerful custom operations.

Let's examine what data you should expose and how to structure it properly.

---

## 📊 Data Available to Post-Processors

Your `PdfProcessingContext` should include:

| Data Type | Description | Use Cases |
|----------|-------------|-----------|
| **`sourceData`** | Original JSON data (before mapping) | Re-extract data with different filters |
| **`mappedData`** | Final mapped data (what was used for AcroForm) | Access already-transformed values |
| **`templateDef`** | Template configuration | Access output path, template name, post-processor config |
| **`processingMetadata`** | Runtime metadata | Timestamps, user info, request ID |

---

## 🧩 Enhanced Context Class

### `PdfProcessingContext.java` (Complete)
```java
package com.example.templatemerge.postprocess;

import com.example.templatemerge.model.TemplateDefinition;
import java.util.Map;

public class PdfProcessingContext {
    
    // Original source data (JSON tree)
    private final Object sourceData;
    
    // Mapped data used for AcroForm filling
    private final Map<String, Object> mappedData;
    
    // Template configuration
    private final TemplateDefinition templateDef;
    
    // Post-processor specific configuration
    private Map<String, Object> processorConfig;
    
    // Additional metadata (optional)
    private final Map<String, Object> metadata;
    
    public PdfProcessingContext(
        Object sourceData,
        Map<String, Object> mappedData,
        TemplateDefinition templateDef,
        Map<String, Object> metadata
    ) {
        this.sourceData = sourceData;
        this.mappedData = mappedData;
        this.templateDef = templateDef;
        this.metadata = metadata != null ? metadata : Map.of();
    }
    
    // Getters
    public Object getSourceData() { return sourceData; }
    public Map<String, Object> getMappedData() { return mappedData; }
    public TemplateDefinition getTemplateDef() { return templateDef; }
    public Map<String, Object> getProcessorConfig() { return processorConfig; }
    public void setProcessorConfig(Map<String, Object> config) { this.processorConfig = config; }
    public Map<String, Object> getMetadata() { return metadata; }
}
```

---

## 🛠 How to Pass Data in TemplateMergeService

### Updated `PdfTemplateMerger.java`
```java
@Override
public void merge(Map<String, Object> mappedData, TemplateDefinition def, Object sourceData) throws IOException {
    // Store sourceData as instance variable or pass through
    this.sourceData = sourceData;
    
    try (PDDocument doc = PDDocument.load(new File(def.getTemplatePath()))) {
        // Fill AcroForm fields
        fillAcroFormFields(doc, mappedData);
        
        // Apply post-processors with full context
        if (!def.getPostProcessors().isEmpty()) {
            Map<String, Object> metadata = Map.of(
                "timestamp", System.currentTimeMillis(),
                "processor", "PdfTemplateMerger"
            );
            
            PdfProcessingContext context = new PdfProcessingContext(
                sourceData,           // Original data
                mappedData,           // Mapped data  
                def,                  // Template config
                metadata              // Runtime metadata
            );
            
            applyPostProcessors(doc, def.getPostProcessors(), context);
        }
        
        doc.save(def.getOutputPath());
    }
}
```

> 🔑 **Note**: You'll need to modify your `TemplateMerger` interface to accept `sourceData`:
> ```java
> void merge(Map<String, Object> mappedData, TemplateDefinition def, Object sourceData) throws IOException;
> ```

---

## 🧪 Real-World Post-Processor Examples

### Example 1: Generate Summary from Mapped Data
```java
@Component("summaryPageAdder")
public class SummaryPageAdder implements PdfPostProcessor {
    
    @Override
    public void process(PDDocument document, PdfProcessingContext context) throws IOException {
        // Use already-mapped data (transformed, filtered)
        String primaryName = (String) context.getMappedData().get("primary.fname.1");
        List<?> dependents = (List<?>) context.getMappedData().get("__all_dependents");
        
        // Generate summary using processed data
        Map<String, Object> summaryModel = Map.of(
            "primaryName", primaryName,
            "dependentCount", dependents != null ? dependents.size() : 0
        );
        
        // Create and append summary page
        appendSummaryPage(document, summaryModel);
    }
}
```

### Example 2: Re-extract Data with Different Logic
```java
@Component("auditTrailAdder")
public class AuditTrailAdder implements PdfPostProcessor {
    
    @Override
    public void process(PDDocument document, PdfProcessingContext context) throws IOException {
        // Re-extract data from original source with different filters
        Object sourceData = context.getSourceData();
        
        // Get ALL applicants (including inactive ones for audit)
        List<?> allApplicants = (List<?>) SimplePathResolver.read(sourceData, "applicants");
        
        // Get only active applicants (what was actually mapped)
        List<?> activeApplicants = (List<?>) context.getMappedData().get("__active_applicants");
        
        // Create audit trail showing differences
        Map<String, Object> auditModel = Map.of(
            "allApplicants", allApplicants,
            "activeApplicants", activeApplicants,
            "excludedCount", allApplicants.size() - activeApplicants.size()
        );
        
        appendAuditPage(document, auditModel);
    }
}
```

### Example 3: Use Template Configuration
```java
@Component("conditionalPageAdder")
public class ConditionalPageAdder implements PdfPostProcessor {
    
    @Override
    public void process(PDDocument document, PdfProcessingContext context) throws IOException {
        // Check template-specific configuration
        Boolean addCoverPage = (Boolean) context.getProcessorConfig().get("addCoverPage");
        String formType = (String) context.getProcessorConfig().get("formType");
        
        // Use original data to make decisions
        Object sourceData = context.getSourceData();
        String actualFormType = (String) SimplePathResolver.read(sourceData, "metadata.formType");
        
        if (Boolean.TRUE.equals(addCoverPage) && "premium".equals(actualFormType)) {
            appendPremiumCoverPage(document);
        }
    }
}
```

---

## 🔧 Advanced: Custom Data Enrichment

You can even **enrich the context** with additional 

```java
// In PdfTemplateMerger, before applying post-processors
PdfProcessingContext context = new PdfProcessingContext(...);

// Add custom enriched data
Map<String, Object> enrichedData = dataEnrichmentService.enrich(sourceData);
context.getMetadata().put("enrichedData", enrichedData);

applyPostProcessors(doc, def.getPostProcessors(), context);
```

Then in post-processor:
```java
Map<String, Object> enriched = (Map<String, Object>) context.getMetadata().get("enrichedData");
String riskScore = (String) enriched.get("riskScore");
```

---

## 📄 YAML Configuration with Context Awareness

```yaml
postProcessors:
  - type: "custom"
    name: "dynamicSummaryAdder"
    config:
      # These are available in processorConfig
      includeFinancialSummary: true
      summaryTemplate: "premium_summary.ftl"
      # Can reference mapped data fields
      primaryField: "primary.fname.1"
```

---

## ⚠️ Important Best Practices

### 1. **Immutable Data**
- Don't modify `sourceData` or `mappedData` in post-processors
- Create new objects for any transformations

### 2. **Type Safety**
- Always check types before casting
- Handle null values gracefully

```java
Object dependentsObj = context.getMappedData().get("dependents");
if (dependentsObj instanceof List) {
    List<?> dependents = (List<?>) dependentsObj;
    // process...
}
```

### 3. **Performance Considerations**
- Cache expensive operations
- Avoid re-parsing JSON in post-processors

### 4. **Error Handling**
- Wrap post-processor logic in try-catch
- Don't let post-processor failures break the main document

```java
try {
    processor.process(document, context);
} catch (Exception e) {
    log.error("Post-processor {} failed", processor.getName(), e);
    // Continue with main document
}
```

---

## ✅ Summary

Your post-processors have access to:

- ✅ **Original source data** → for re-extraction with different logic
- ✅ **Mapped data** → for using already-transformed values  
- ✅ **Template configuration** → for conditional logic
- ✅ **Runtime metadata** → for audit trails, timestamps, etc.

This gives you **maximum flexibility** to implement complex post-processing operations like:
- Dynamic overflow pages
- Audit trails
- Conditional cover pages
- Data validation reports
- Digital signature preparation
- Compliance certificates

The key is structuring your `PdfProcessingContext` to provide all necessary data while maintaining clean separation of concerns. 🚀