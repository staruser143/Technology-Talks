Excellent idea! Supporting **multiple PDF templates** in a single YAML configuration makes your system **much more flexible and maintainable**. Let me show you several approaches to achieve this.

---

## 🎯 Your Requirements

You want to:
- **Support multiple PDF templates** (e.g., enrollment form, coverage summary, dependent addendum)
- **Reuse contexts** across templates
- **Have template-specific mappings**
- **Generate multiple PDFs from single JSON input**

---

## ✅ Solution 1: Template-Based Configuration Structure

### Enhanced YAML Schema

```yaml
# Global contexts (shared across all templates)
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true
  spouse:
    from: applicants
    filter: { type: SPOUSE }
    first: true
  dependents:
    from: applicants
    filter: { type: DEPENDENT }

# Multiple templates with their own mappings
templates:
  enrollmentForm:
    mappings:
      - source: primary.demographics.firstName
        target: "primary_firstName"
      - source: primary.demographics.ssn
        target: "ssn_part1"
        transform: "extractSsnPart1"
      # ... other enrollment form mappings

  coverageSummary:
    mappings:
      - source: primary.currentCoverages[?(@.isActive == true)][0].insuranceName
        target: "primary_insurance_name"
      - collection:
          source: dependents
          maxItems: 3
          slotOffset: 1
          itemMappings:
            - source: currentCoverages[?(@.isActive == true)][0].insuranceName
              target: "dependent_coverage_{slot}_name"
      # ... other coverage summary mappings

  dependentAddendum:
    mappings:
      - collection:
          source: dependents
          maxItems: 3
          itemMappings:
            - source: demographics.firstName
              target: "dep_{index}_firstName"
            - source: demographics.ssn
              target: "dep_{index}_ssn_part1"
              transform: "extractSsnPart1"
      # ... dependent addendum mappings
```

---

## 🔧 Step 1: Enhanced Model Classes

### `TemplateConfig.java`
```java
package model;

import java.util.List;

public class TemplateConfig {
    private String templateName;
    private String pdfTemplatePath;
    private String outputPath;
    private List<FieldMapping> mappings;

    // Getters and setters
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getPdfTemplatePath() { return pdfTemplatePath; }
    public void setPdfTemplatePath(String pdfTemplatePath) { this.pdfTemplatePath = pdfTemplatePath; }

    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }

    public List<FieldMapping> getMappings() { return mappings; }
    public void setMappings(List<FieldMapping> mappings) { this.mappings = mappings; }
}
```

### `MappingConfig.java` (Enhanced)
```java
package model;

import java.util.List;
import java.util.Map;

public class MappingConfig {
    private Map<String, ContextDef> contexts;
    private List<FieldMapping> mappings; // Global mappings (optional)
    private Map<String, TemplateConfig> templates; // Multiple templates

    // Getters and setters
    public Map<String, ContextDef> getContexts() { return contexts; }
    public void setContexts(Map<String, ContextDef> contexts) { this.contexts = contexts; }

    public List<FieldMapping> getMappings() { return mappings; }
    public void setMappings(List<FieldMapping> mappings) { this.mappings = mappings; }

    public Map<String, TemplateConfig> getTemplates() { return templates; }
    public void setTemplates(Map<String, TemplateConfig> templates) { this.templates = templates; }

    public boolean hasTemplates() {
        return templates != null && !templates.isEmpty();
    }
}
```

---

## ⚙️ Step 2: Enhanced PdfFieldMapper

### Updated Processing Method
```java
package engine;

import model.*;

public class PdfFieldMapper {
    private boolean dryRun = false;

    public void processAllTemplates(String yamlPath, String jsonInput) throws Exception {
        MappingConfig config = loadConfig(yamlPath);
        DocumentContext rootJson = JsonPath.parse(jsonInput);

        // Build contexts (shared across all templates)
        Map<String, String> contextJsonPaths = buildContextJsonPaths(config.getContexts());
        Map<String, Object> contextCache = evaluateContexts(contextJsonPaths, rootJson);

        if (config.hasTemplates()) {
            // Process each template
            for (Map.Entry<String, TemplateConfig> entry : config.getTemplates().entrySet()) {
                String templateName = entry.getKey();
                TemplateConfig templateConfig = entry.getValue();
                
                System.out.println("🔄 Processing template: " + templateName);
                
                // Map fields for this template
                MappingResult result = mapFieldsForTemplate(
                    templateConfig.getMappings(), 
                    rootJson, 
                    contextCache
                );
                
                if (dryRun) {
                    System.out.println("📊 " + templateName + " MAPPED FIELDS:");
                    result.getFieldValues().forEach((field, value) -> 
                        System.out.println("  " + field + " = '" + value + "'")
                    );
                } else {
                    // Merge with PDF
                    mergeFieldsIntoPdf(
                        result, 
                        templateConfig.getPdfTemplatePath(), 
                        templateConfig.getOutputPath()
                    );
                }
            }
        } else {
            // Fallback to single template mode (backward compatibility)
            MappingResult result = mapFieldsForTemplate(
                config.getMappings(), 
                rootJson, 
                contextCache
            );
            
            if (!dryRun) {
                // You'll need to specify PDF paths somehow for backward compatibility
                throw new IllegalStateException("Single template mode requires PDF paths");
            }
        }
    }

    private MappingResult mapFieldsForTemplate(
            List<FieldMapping> mappings, 
            DocumentContext rootJson, 
            Map<String, Object> contextCache) {
        
        MappingResult result = new MappingResult();
        
        for (FieldMapping mapping : mappings) {
            if (mapping.isMultiSource()) {
                processMultiSourceMapping(mapping, rootJson, contextCache, result);
            } else if (mapping.isScalar()) {
                processScalarToResult(mapping, rootJson, contextCache, result);
            } else if (mapping.isCollection()) {
                processCollectionToResult(
                    mapping.getCollection(),
                    rootJson,
                    contextCache,
                    result,
                    "", null, 0
                );
            } else if (mapping.isSsnPartMapping()) {
                processSsnPartMapping(mapping, rootJson, contextCache, result);
            }
        }
        
        return result;
    }

    // ... rest of your existing methods (buildContextJsonPaths, evaluateContexts, etc.)
}
```

---

## 📄 Step 3: Complete YAML Configuration Example

```yaml
# Shared contexts across all templates
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true
  spouse:
    from: applicants
    filter: { type: SPOUSE }
    first: true
  dependents:
    from: applicants
    filter: { type: DEPENDENT }

# Multiple templates
templates:
  # Main enrollment form
  enrollmentForm:
    pdfTemplatePath: "templates/enrollment_form.pdf"
    outputPath: "output/enrollment_{applicationId}.pdf"
    mappings:
      - source: primary.demographics.firstName
        target: "primary_firstName"
      - source: primary.demographics.lastName
        target: "primary_lastName"
      - source: primary.demographics.dob
        target: "primary_dob"
        transform: "extractYear"
      - source: primary.demographics.ssn
        target: "ssn_part1"
        transform: "extractSsnPart1"
      - source: primary.demographics.ssn
        target: "ssn_part2"
        transform: "extractSsnPart2"
      - source: primary.demographics.ssn
        target: "ssn_part3"
        transform: "extractSsnPart3"
      
      # Coverage mappings for primary
      - source: primary.currentCoverages[?(@.isActive == true)][0].insuranceName
        target: "primary_insurance_name"

  # Coverage summary document
  coverageSummary:
    pdfTemplatePath: "templates/coverage_summary.pdf"
    outputPath: "output/coverage_summary_{applicationId}.pdf"
    mappings:
      - source: primary.currentCoverages[?(@.isActive == true)][0].insuranceName
        target: "primary_coverage_name"
      - source: primary.currentCoverages[?(@.isActive == true)][0].coverageDateStart
        target: "primary_coverage_start"
      
      # Dependent coverage mappings
      - collection:
          source: dependents
          maxItems: 3
          slotOffset: 1
          itemMappings:
            - source: currentCoverages[?(@.isActive == true)][0].insuranceName
              target: "dependent_{slot}_coverage_name"
            - source: currentCoverages[?(@.isActive == true)][0].coverageDateStart
              target: "dependent_{slot}_coverage_start"

  # Dependent addendum (for applications with dependents)
  dependentAddendum:
    pdfTemplatePath: "templates/dependent_addendum.pdf"
    outputPath: "output/dependent_addendum_{applicationId}.pdf"
    mappings:
      - collection:
          source: dependents
          maxItems: 3
          itemMappings:
            - source: demographics.firstName
              target: "dep_{index}_firstName"
            - source: demographics.lastName
              target: "dep_{index}_lastName"
            - source: demographics.dob
              target: "dep_{index}_dob"
            - source: demographics.ssn
              target: "dep_{index}_ssn_part1"
              transform: "extractSsnPart1"
            - source: demographics.ssn
              target: "dep_{index}_ssn_part2"
              transform: "extractSsnPart2"
            - source: demographics.ssn
              target: "dep_{index}_ssn_part3"
              transform: "extractSsnPart3"
```

---

## 🔧 Step 4: Enhanced Output Path with Variables

You can enhance the output path to support **dynamic variables** like `{applicationId}`:

```java
// In PdfFieldMapper.java
private String resolveOutputPath(String outputPathTemplate, DocumentContext rootJson) {
    if (outputPathTemplate.contains("{applicationId}")) {
        try {
            String appId = rootJson.read("$.applicationId").toString();
            return outputPathTemplate.replace("{applicationId}", appId);
        } catch (Exception e) {
            // Fallback to timestamp if applicationId not found
            return outputPathTemplate.replace("{applicationId}", 
                String.valueOf(System.currentTimeMillis()));
        }
    }
    return outputPathTemplate;
}
```

Then update your merge method:
```java
private void mergeFieldsIntoPdf(MappingResult result, String pdfTemplatePath, String outputPathTemplate) 
        throws Exception {
    String outputPath = resolveOutputPath(outputPathTemplate, rootJson); // You'll need to store rootJson
    // ... rest of method
}
```

---

## 🚀 Step 5: Usage Example

### `Main.java`
```java
import engine.PdfFieldMapper;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("input.json")));
        
        PdfFieldMapper mapper = new PdfFieldMapper()
            .dryRun(args.length > 0 && "--dry-run".equals(args[0]));
        
        // Process all templates defined in YAML
        mapper.processAllTemplates("multi_template_config.yaml", json);
        
        if (!mapper.isDryRun()) {
            System.out.println("✅ All templates processed successfully!");
        }
    }
}
```

---

## ✅ Benefits of This Approach

| Feature | Benefit |
|--------|---------|
| **Shared contexts** | Define once, use across all templates |
| **Template isolation** | Each template has its own mappings |
| **Backward compatible** | Single template mode still works |
| **Flexible output** | Different output paths for each template |
| **Maintainable** | All configuration in one YAML file |
| **Extensible** | Easy to add new templates |

---

## 🔧 Alternative: Separate YAML Files Approach

If you prefer **separation of concerns**, you can also support **multiple YAML files**:

```java
public void processMultipleConfigs(List<String> yamlPaths, String jsonInput) throws Exception {
    for (String yamlPath : yamlPaths) {
        MappingConfig config = loadConfig(yamlPath);
        // Process single template
        processSingleTemplate(config, jsonInput);
    }
}
```

But the **single YAML with templates** approach is generally **more maintainable** since contexts are shared.

---

## 🧪 Advanced: Conditional Template Processing

You can even add **conditions** to templates:

```yaml
templates:
  dependentAddendum:
    condition:
      type: "greaterThan"
      field: "dependents.length"
      value: 0
    pdfTemplatePath: "templates/dependent_addendum.pdf"
    # ... mappings
```

Then in your processing logic:
```java
if (templateConfig.getCondition() != null) {
    boolean shouldProcess = ConditionEvaluator.evaluate(
        templateConfig.getCondition(), 
        rootJson, 
        rootJson
    );
    if (!shouldProcess) {
        System.out.println("⏭️  Skipping template: " + templateName + " (condition not met)");
        continue;
    }
}
```

This allows you to **dynamically decide** which templates to generate based on the data!

---

## ✅ Summary

**Use the template-based YAML structure** because it:

- ✅ **Supports multiple PDF templates** in a single configuration
- ✅ **Shares contexts** across templates to avoid duplication  
- ✅ **Maintains backward compatibility** with existing single-template configs
- ✅ **Provides flexible output paths** with dynamic variables
- ✅ **Is easy to extend** with conditions and advanced features

This approach gives you **enterprise-grade flexibility** while keeping your configuration **clean and maintainable**!

Would you like me to show how to implement **conditional template processing** or **dynamic output path variables** in more detail?