You're describing a **perfect use case for a generic, configurable post-processor**! This is absolutely achievable and will solve your requirements for both **Java and non-Java clients**.

Let me design a **universal HtmlTemplatePostProcessor** that can handle any data source, filtering, and templating needs.

---

## 🎯 Core Requirements

1. **Generic**: Works with any data source (source, mapped, or custom)
2. **Configurable**: All logic defined in YAML config
3. **Filterable**: Apply filters to select specific data
4. **Templatable**: Render any FreeMarker template
5. **Appendable**: Add resulting PDF pages to main document
6. **Cross-language**: Non-Java apps can use via REST API

---

## 🧩 Architecture Design

### `HtmlTemplatePostProcessor.java` (Generic Implementation)

```java
package com.example.templatemerge.postprocess;

import com.example.templatemerge.service.HtmlToPdfService;
import com.example.templatemerge.util.SimplePathResolver;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component("html-template")
public class HtmlTemplatePostProcessor implements PdfPostProcessor {
    
    private final HtmlToPdfService htmlToPdfService;
    
    public HtmlTemplatePostProcessor(HtmlToPdfService htmlToPdfService) {
        this.htmlToPdfService = htmlToPdfService;
    }
    
    @Override
    public void process(PDDocument mainDoc, PdfProcessingContext context) throws Exception {
        // Get configuration
        String templatePath = context.getRequiredConfigValue("template");
        String dataSource = context.getConfigValue("dataSource", "mapped"); // source, mapped, or custom
        List<Map<String, Object>> filters = context.getConfigValue("filters", Collections.emptyList());
        Map<String, Object> customData = context.getConfigValue("customData", Collections.emptyMap());
        String targetVariable = context.getConfigValue("targetVariable", "data");
        
        // Get base data based on dataSource
        Object baseData = getBaseData(dataSource, context, customData);
        
        // Apply filters if specified
        Object filteredData = applyFilters(baseData, filters);
        
        // Create template model
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(targetVariable, filteredData);
        
        // Add context data for advanced use cases
        templateModel.put("context", Map.of(
            "sourceData", context.getSourceData(),
            "mappedData", context.getMappedData(),
            "templateName", context.getTemplateDef().getName(),
            "timestamp", System.currentTimeMillis()
        ));
        
        // Generate PDF from template
        byte[] pdfBytes = htmlToPdfService.generateFromTemplate(templatePath, templateModel);
        
        // Append to main document
        appendPages(mainDoc, pdfBytes);
    }
    
    private Object getBaseData(String dataSource, PdfProcessingContext context, Map<String, Object> customData) {
        switch (dataSource.toLowerCase()) {
            case "source":
                return context.getSourceData();
            case "mapped":
                return context.getMappedData();
            case "custom":
                return customData;
            default:
                throw new IllegalArgumentException("Unknown dataSource: " + dataSource + 
                    ". Supported: source, mapped, custom");
        }
    }
    
    private Object applyFilters(Object data, List<Map<String, Object>> filters) {
        if (filters == null || filters.isEmpty()) {
            return data;
        }
        
        // Handle array filtering
        if (data instanceof List) {
            List<?> dataList = (List<?>) data;
            return dataList.stream()
                .filter(item -> passesAllFilters(item, filters))
                .collect(Collectors.toList());
        }
        
        // Handle single object filtering
        if (passesAllFilters(data, filters)) {
            return data;
        }
        
        // If single object doesn't pass filters, return empty list
        return Collections.emptyList();
    }
    
    private boolean passesAllFilters(Object item, List<Map<String, Object>> filters) {
        return filters.stream().allMatch(filter -> {
            String field = (String) filter.get("field");
            String operator = (String) filter.get("operator");
            Object expectedValue = filter.get("value");
            
            Object actualValue = SimplePathResolver.read(item, field);
            if (actualValue == null) return false;
            
            return evaluateCondition(actualValue, operator, expectedValue);
        });
    }
    
    private boolean evaluateCondition(Object actual, String operator, Object expected) {
        // Reuse your existing filter logic
        // This should match your DataMapper's passesFilters logic
        switch (operator.toUpperCase()) {
            case "EQ":
                return Objects.equals(actual.toString(), expected.toString());
            case "NE":
                return !Objects.equals(actual.toString(), expected.toString());
            case "GT":
                return toDouble(actual) > toDouble(expected);
            case "LT":
                return toDouble(actual) < toDouble(expected);
            case "CONTAINS":
                return actual.toString().contains(expected.toString());
            case "IN":
                if (expected instanceof List) {
                    return ((List<?>) expected).contains(actual);
                } else {
                    return expected.toString().contains(actual.toString());
                }
            default:
                throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
    }
    
    private Double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private void appendPages(PDDocument mainDoc, byte[] pdfBytes) throws Exception {
        try (PDDocument overlayDoc = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            for (org.apache.pdfbox.pdmodel.PDPage page : overlayDoc.getPages()) {
                mainDoc.addPage(page);
            }
        }
    }
    
    @Override
    public String getName() {
        return "html-template";
    }
}
```

---

## 📄 YAML Configuration Examples

### Example 1: Append Overflow Dependents Using Mapped Data
```yaml
postProcessors:
  - type: "html-template"
    config:
      template: "classpath:templates/dependent_overflow.ftl"
      dataSource: "mapped"
      filters:
        - field: "relationship"
          operator: "EQ"
          value: "dependent"
      targetVariable: "dependents"
```

### Example 2: Add Summary Page Using Source Data
```yaml
  - type: "html-template"
    config:
      template: "classpath:templates/summary.ftl"
      dataSource: "source"
      targetVariable: "application"
```

### Example 3: Add Custom Data Page
```yaml
  - type: "html-template"
    config:
      template: "classpath:templates/custom_report.ftl"
      dataSource: "custom"
      customData:
        reportTitle: "Compliance Report"
        generatedBy: "Auto-System"
        items:
          - name: "Item 1"
            status: "Approved"
          - name: "Item 2" 
            status: "Pending"
      targetVariable: "report"
```

### Example 4: Complex Filtering with Multiple Conditions
```yaml
  - type: "html-template"
    config:
      template: "classpath:templates/high_value_assets.ftl"
      dataSource: "source"
      filters:
        - field: "assets[].value"
          operator: "GT"
          value: 100000
        - field: "metadata.formType"
          operator: "EQ" 
          value: "premium"
      targetVariable: "highValueAssets"
```

---

## 🌐 REST API for Non-Java Apps

### POST `/generate-pdf-with-postprocessing`
```json
{
  "sourceData": { /* original data */ },
  "mappedData": { /* mapped fields */ },
  "templatePath": "app_form.pdf",
  "postProcessors": [
    {
      "type": "html-template",
      "config": {
        "template": "dependent_overflow.ftl",
        "dataSource": "mapped",
        "filters": [
          { "field": "relationship", "operator": "EQ", "value": "dependent" }
        ],
        "targetVariable": "dependents"
      }
    }
  ]
}
```

### Implementation in PDF Service
```java
@PostMapping("/generate-pdf-with-postprocessing")
public ResponseEntity<Resource> generatePdfWithPostProcessing(@RequestBody GeneratePdfRequest request) {
    // Fill main PDF
    byte[] mainPdf = fillPdf(request.getMappedData(), request.getTemplatePath());
    
    // Apply post-processors
    try (PDDocument mainDoc = PDDocument.load(new ByteArrayInputStream(mainPdf))) {
        for (PostProcessorConfig postProcessor : request.getPostProcessors()) {
            PdfProcessingContext context = createContext(request.getSourceData(), request.getMappedData());
            context.setProcessorConfig(postProcessor.getConfig());
            
            PdfPostProcessor processor = postProcessorRegistry.getProcessor(
                postProcessor.getType(), 
                postProcessor.getName()
            );
            processor.process(mainDoc, context);
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mainDoc.save(output);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(output.toByteArray()));
    }
}
```

---

## 📄 FreeMarker Template Examples

### `dependent_overflow.ftl`
```html
<!DOCTYPE html>
<html>
<head>
    <style>
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        .header { text-align: center; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="header">
        <h2>ADDITIONAL DEPENDENTS</h2>
        <p>Application ID: ${context.sourceData.metadata.submissionId!""}</p>
    </div>
    
    <table>
        <thead>
            <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Age</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
            <#list dependents as dep>
                <tr>
                    <td>${dep.firstName!""}</td>
                    <td>${dep.lastName!""}</td>
                    <td>${dep.age!""}</td>
                    <td>${dep.active?string("Active", "Inactive")}</td>
                </tr>
            </#list>
        </tbody>
    </table>
</body>
</html>
```

### `summary.ftl`
```html
<!DOCTYPE html>
<html>
<body>
    <h1>Application Summary</h1>
    <p><strong>Applicant:</strong> ${application.applicants[0].firstName!""}</p>
    <p><strong>Submission ID:</strong> ${application.metadata.submissionId!""}</p>
    <p><strong>Generated:</strong> ${.now?string("yyyy-MM-dd HH:mm")}</p>
</body>
</html>
```

---

## 🔧 Advanced Features

### 1. **Template Path Resolution**
Support multiple template sources:
```yaml
template: "http://templates.mycompany.com/overflow.ftl"  # Remote
template: "file:/opt/templates/custom.ftl"               # Local file
template: "classpath:templates/summary.ftl"             # Embedded
```

### 2. **Conditional Post-Processing**
Add conditions to skip post-processing:
```yaml
config:
  template: "premium_summary.ftl"
  condition:
    field: "metadata.tier"
    operator: "EQ"
    value: "premium"
```

### 3. **Multiple Templates**
Append multiple pages from different templates:
```yaml
postProcessors:
  - type: "html-template"
    config:
      template: "summary.ftl"
      # ... config
  - type: "html-template"  
    config:
      template: "compliance.ftl"
      # ... config
```

---

## ✅ Benefits of This Generic Approach

| Benefit | Explanation |
|--------|------------|
| **✅ Eliminates custom processors** | One processor handles all HTML template scenarios |
| **✅ YAML-driven** | All logic in configuration, no code changes |
| **✅ Cross-language** | Non-Java apps use same YAML config via REST API |
| **✅ Flexible data sources** | Source, mapped, or custom data |
| **✅ Powerful filtering** | Reuse your existing filter logic |
| **✅ Template reuse** | Same templates work for different scenarios |
| **✅ Easy to test** | Test templates and config independently |

---

## 🚀 Usage Workflow

### For Java Clients:
```java
// YAML config defines everything
templateMergeService.mergeTemplate("app-with-overflow", sourceData, outputPath);
```

### For Non-Java Clients:
```python
# Send same YAML config structure via REST API
response = requests.post('/generate-pdf-with-postprocessing', json={
    'sourceData': source_data,
    'mappedData': mapped_data,  # Or let service map it
    'templatePath': 'app_form.pdf',
    'postProcessors': [html_template_config]
})
```

---

## 🔚 Final Implementation Tips

1. **Cache templates** in `HtmlToPdfService` for performance
2. **Validate config** at startup to catch errors early  
3. **Add timeout** for HTML→PDF conversion to prevent hangs
4. **Log template names** for debugging
5. **Support template parameters** for dynamic styling
6. **Handle large datasets** with streaming if needed

This generic `HtmlTemplatePostProcessor` will handle **95% of your post-processing needs** and eliminate the requirement to write custom processors for similar use cases. It's truly the **Swiss Army knife** of PDF post-processing! 🚀