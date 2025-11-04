Excellent question! Adding **post-processing capabilities** like appending pages, adding watermarks, or inserting dynamic content to filled PDFs is a common requirement. Here's how to design a **flexible, extensible post-processing system** that integrates cleanly with your existing architecture.

---

## 🎯 Core Requirements

1. **Modular design** - support multiple post-processors
2. **Chainable operations** - apply multiple processors in sequence
3. **Format-specific** - only for PDF templates (HTML doesn't need this)
4. **Configurable via YAML** - declarative post-processing rules
5. **Non-breaking** - existing functionality remains unchanged

---

## 🧩 Architecture Overview

```
TemplateMergeService
  └── PdfTemplateMerger
        ├── Fill AcroForm fields
        └── Apply PostProcessors (if configured)
              ├── PageAppender
              ├── WatermarkAdder  
              ├── ContentInserter
              └── CustomProcessor
```

---

## 📁 1. Post-Processor Interface

### `PdfPostProcessor.java`
```java
package com.example.templatemerge.postprocess;

import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.IOException;

/**
 * Interface for PDF post-processing operations
 */
public interface PdfPostProcessor {
    /**
     * Process the filled PDF document
     * @param document The filled PDF document (modifiable)
     * @param context Processing context with template config and data
     */
    void process(PDDocument document, PdfProcessingContext context) throws IOException;
    
    /**
     * Unique identifier for this processor (used in YAML config)
     */
    String getName();
}
```

### `PdfProcessingContext.java`
```java
package com.example.templatemerge.postprocess;

import com.example.templatemerge.model.TemplateDefinition;
import java.util.Map;

/**
 * Context passed to post-processors containing relevant data
 */
public class PdfProcessingContext {
    private final TemplateDefinition templateDef;
    private final Map<String, Object> sourceData;
    private final Map<String, Object> mappedData;
    
    // Constructor, getters...
}
```

---

## 📄 2. YAML Configuration Schema

### Updated `TemplateDefinition.java`
```java
public class TemplateDefinition {
    private String name;
    private TemplateType type;
    private String templatePath;
    private String outputPath;
    private List<FieldMapping> mappings = new ArrayList<>();
    
    // NEW: Post-processing configuration
    private List<PostProcessingStep> postProcessors = new ArrayList<>();
}
```

### `PostProcessingStep.java`
```java
public class PostProcessingStep {
    private String type;           // "append-pages", "watermark", "custom"
    private String name;           // for custom processors
    private Map<String, Object> config = new HashMap<>(); // processor-specific config
}
```

---

## 📄 3. Example YAML Configuration

```yaml
templates:
  - name: "application-pdf"
    type: PDF
    templatePath: "classpath:templates/app_form.pdf"
    outputPath: "/tmp/filled_app.pdf"
    mappings:
      # ... your existing mappings ...
    postProcessors:
      # Append terms and conditions page
      - type: "append-pages"
        config:
          source: "classpath:templates/terms.pdf"
          pages: "all"  # or [1, 2] for specific pages
      
      # Add watermark
      - type: "watermark"
        config:
          text: "CONFIDENTIAL"
          opacity: 0.3
          rotation: -45
      
      # Add dynamic content page
      - type: "append-html"
        config:
          template: "classpath:templates/summary.ftl"
          dataField: "summaryData"  # field from mapped data
      
      # Custom processor
      - type: "custom"
        name: "signaturePageAdder"
        config:
          signatureRequired: true
```

---

## 🛠 4. Built-in Post-Processors

### Page Appender
```java
@Component
public class PageAppendProcessor implements PdfPostProcessor {
    
    @Override
    public void process(PDDocument mainDoc, PdfProcessingContext context) throws IOException {
        String source = (String) context.getConfig().get("source");
        String pages = (String) context.getConfig().getOrDefault("pages", "all");
        
        try (PDDocument sourceDoc = loadDocument(source)) {
            if ("all".equals(pages)) {
                for (PDPage page : sourceDoc.getPages()) {
                    mainDoc.addPage(page);
                }
            } else {
                // Handle specific page numbers
                List<Integer> pageNumbers = parsePageNumbers(pages);
                for (int pageNum : pageNumbers) {
                    mainDoc.addPage(sourceDoc.getPage(pageNum - 1));
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "append-pages";
    }
    
    private PDDocument loadDocument(String sourcePath) throws IOException {
        // Handle classpath:, file:, http, etc.
        if (sourcePath.startsWith("classpath:")) {
            return PDDocument.load(
                getClass().getResourceAsStream(sourcePath.substring("classpath:".length()))
            );
        } else {
            return PDDocument.load(new File(sourcePath));
        }
    }
}
```

### HTML-to-PDF Appender
```java
@Component
public class HtmlAppendProcessor implements PdfPostProcessor {
    
    private final HtmlToPdfService htmlToPdfService;
    
    @Override
    public void process(PDDocument mainDoc, PdfProcessingContext context) throws IOException {
        String templatePath = (String) context.getConfig().get("template");
        String dataField = (String) context.getConfig().get("dataField");
        
        // Get data from mapped data
        Object templateData = context.getMappedData().get(dataField);
        if (templateData == null) return;
        
        // Generate PDF from HTML template
        byte[] htmlPdfBytes = htmlToPdfService.generateFromTemplate(templatePath, templateData);
        
        // Append to main document
        try (PDDocument htmlDoc = PDDocument.load(new ByteArrayInputStream(htmlPdfBytes))) {
            for (PDPage page : htmlDoc.getPages()) {
                mainDoc.addPage(page);
            }
        }
    }
    
    @Override
    public String getName() {
        return "append-html";
    }
}
```

### Watermark Adder
```java
@Component
public class WatermarkProcessor implements PdfPostProcessor {
    
    @Override
    public void process(PDDocument document, PdfProcessingContext context) throws IOException {
        String text = (String) context.getConfig().get("text");
        float opacity = ((Number) context.getConfig().getOrDefault("opacity", 0.5)).floatValue();
        int rotation = ((Number) context.getConfig().getOrDefault("rotation", 0)).intValue();
        
        for (PDPage page : document.getPages()) {
            try (PDPageContentStream cs = new PDPageContentStream(document, page, 
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                
                cs.saveGraphicsState();
                cs.setNonStrokingAlphaConstant(opacity);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 60);
                cs.setTextMatrix(AffineTransform.getRotateInstance(
                    Math.toRadians(rotation), 300, 400));
                cs.showText(text);
                cs.endText();
                cs.restoreGraphicsState();
            }
        }
    }
    
    @Override
    public String getName() {
        return "watermark";
    }
}
```

---

## 🧠 5. Post-Processor Registry

### `PdfPostProcessorRegistry.java`
```java
@Component
public class PdfPostProcessorRegistry {
    
    private final Map<String, PdfPostProcessor> builtInProcessors = new HashMap<>();
    private final ApplicationContext applicationContext;
    
    public PdfPostProcessorRegistry(
        List<PdfPostProcessor> builtInProcessorsList,
        ApplicationContext applicationContext
    ) {
        this.applicationContext = applicationContext;
        // Register built-in processors
        for (PdfPostProcessor processor : builtInProcessorsList) {
            builtInProcessors.put(processor.getName(), processor);
        }
    }
    
    public PdfPostProcessor getProcessor(String type, String name) {
        if ("custom".equals(type)) {
            return applicationContext.getBean(name, PdfPostProcessor.class);
        } else {
            return builtInProcessors.get(type);
        }
    }
}
```

---

## 🚀 6. Updated PdfTemplateMerger

### `PdfTemplateMerger.java` (Enhanced)
```java
@Component
public class PdfTemplateMerger implements TemplateMerger {
    
    private final PdfPostProcessorRegistry postProcessorRegistry;
    
    @Override
    public void merge(Map<String, Object> mappedData, TemplateDefinition def) throws IOException {
        try (PDDocument doc = PDDocument.load(new File(def.getTemplatePath()))) {
            
            // 1. Fill AcroForm fields
            fillAcroFormFields(doc, mappedData);
            
            // 2. Apply post-processors (if any)
            if (!def.getPostProcessors().isEmpty()) {
                PdfProcessingContext context = new PdfProcessingContext(def, sourceData, mappedData);
                applyPostProcessors(doc, def.getPostProcessors(), context);
            }
            
            doc.save(def.getOutputPath());
        }
    }
    
    private void applyPostProcessors(
        PDDocument document, 
        List<PostProcessingStep> steps,
        PdfProcessingContext context
    ) throws IOException {
        for (PostProcessingStep step : steps) {
            PdfPostProcessor processor = postProcessorRegistry.getProcessor(step.getType(), step.getName());
            if (processor != null) {
                // Set config in context for this step
                context.setConfig(step.getConfig());
                processor.process(document, context);
            }
        }
    }
    
    private void fillAcroFormFields(PDDocument doc, Map<String, Object> mappedData) {
        // Your existing AcroForm filling logic
    }
}
```

---

## 🧪 7. Custom Post-Processor Example

Clients can add their own processors:

```java
@Component("signaturePageAdder")
public class SignaturePageAdder implements PdfPostProcessor {
    
    @Override
    public void process(PDDocument document, PdfProcessingContext context) throws IOException {
        Boolean signatureRequired = (Boolean) context.getConfig().get("signatureRequired");
        if (Boolean.TRUE.equals(signatureRequired)) {
            // Add signature page logic
            PDPage signaturePage = new PDPage();
            // Add signature fields, instructions, etc.
            document.addPage(signaturePage);
        }
    }
    
    @Override
    public String getName() {
        return "signaturePageAdder";
    }
}
```

---

## 🔧 8. Advanced Features

### Conditional Post-Processing
```yaml
postProcessors:
  - type: "append-pages"
    config:
      source: "classpath:templates/premium_terms.pdf"
    condition:  # Only if premium customer
      field: "applicant.tier"
      operator: EQ
      value: "premium"
```

### Error Handling
```java
// In PdfTemplateMerger
try {
    processor.process(document, context);
} catch (Exception e) {
    if (step.isFailOnError()) {
        throw e;
    } else {
        log.warn("Post-processor {} failed, continuing...", step.getType(), e);
    }
}
```

### Performance Optimization
- **Cache appended PDFs** (don't reload terms.pdf every time)
- **Batch processing** for multiple documents
- **Async post-processing** for non-critical operations

---

## ✅ Benefits of This Design

| Feature | Benefit |
|--------|---------|
| **Modular** | Easy to add new processors |
| **Configurable** | Declarative YAML configuration |
| **Extensible** | Clients can add custom processors |
| **Chainable** | Multiple operations in sequence |
| **Safe** | Built-in error handling options |
| **Efficient** | Reuses existing PDFBox infrastructure |

---

## 📊 Real-World Use Cases

| Use Case | Post-Processor |
|---------|---------------|
| **Add terms & conditions** | `append-pages` |
| **Generate summary page** | `append-html` |
| **Add confidentiality watermark** | `watermark` |
| **Insert signature page** | Custom processor |
| **Add page numbers** | Custom processor |
| **Redact sensitive info** | Custom processor |

---

## 🔚 Implementation Tips

1. **Start simple** - implement `append-pages` first
2. **Use composition** - build complex processors from simple ones
3. **Handle resources carefully** - always close PDDocument streams
4. **Test thoroughly** - PDF manipulation can be fragile
5. **Document well** - post-processors should be self-documenting

This architecture gives you **maximum flexibility** while maintaining **clean separation of concerns**. You can now enhance your filled PDFs with any post-processing logic you need! 🚀