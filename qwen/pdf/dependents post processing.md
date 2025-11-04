This is a **classic real-world scenario** that combines **AcroForm limitations** with **dynamic content generation**. Here's a complete, production-ready solution that handles both the **static PDF fields** and **dynamic overflow pages**.

---

## 🎯 Solution Architecture

```
1. Fill first 3 dependents in AcroForm fields
2. Identify overflow dependents (4th, 5th, etc.)
3. Generate overflow table as HTML → PDF
4. Append overflow PDF pages to main document
```

---

## 📄 Step 1: YAML Configuration

```yaml
templates:
  - name: "application-pdf-with-overflow"
    type: PDF
    templatePath: "classpath:templates/app_form.pdf"  # Has 3 dependent slots
    outputPath: "/tmp/filled_app.pdf"
    mappings:
      # Primary applicant
      - sourceObject: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
        fieldMappings:
          - sourceField: "firstName"
            targetField: "primary.fname.1"
          # ... other fields

      # First 3 dependents (for AcroForm)
      - sourceArray: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "dependent"
        maxRepetitions: 3  # ← Only fill first 3
        itemMappings:
          - sourceField: "firstName"
            targetFieldTemplate: "dependent.fname.{index}"
          - sourceField: "age"
            targetFieldTemplate: "dependent.age.{index}"

      # ALL dependents (for overflow processing)
      - sourceField: "applicants[relationship=dependent]"
        targetField: "__all_dependents"  # ← Hidden field for post-processing

    postProcessors:
      - type: "custom"
        name: "dependentOverflowHandler"
        config:
          acroFormLimit: 3
          overflowTemplate: "classpath:templates/dependent_overflow.ftl"
```

> 🔑 **Key Insight**:  
> - Use `maxRepetitions: 3` for AcroForm fields  
> - Extract **all dependents** as hidden field `__all_dependents` for overflow logic

---

## 🧩 Step 2: Custom Post-Processor

### `DependentOverflowHandler.java`
```java
@Component("dependentOverflowHandler")
public class DependentOverflowHandler implements PdfPostProcessor {
    
    private final HtmlToPdfService htmlToPdfService;
    
    @Override
    public void process(PDDocument mainDoc, PdfProcessingContext context) throws IOException {
        Integer acroFormLimit = ((Number) context.getConfig().get("acroFormLimit")).intValue();
        List<?> allDependents = (List<?>) context.getMappedData().get("__all_dependents");
        
        if (allDependents == null || allDependents.size() <= acroFormLimit) {
            return; // No overflow
        }
        
        // Extract overflow dependents (skip first N)
        List<?> overflowDependents = allDependents.subList(acroFormLimit, allDependents.size());
        
        // Generate overflow PDF from HTML template
        Map<String, Object> overflowModel = Map.of("dependents", overflowDependents);
        byte[] overflowPdfBytes = htmlToPdfService.generateFromTemplate(
            (String) context.getConfig().get("overflowTemplate"),
            overflowModel
        );
        
        // Append overflow pages to main document
        appendOverflowPages(mainDoc, overflowPdfBytes);
    }
    
    private void appendOverflowPages(PDDocument mainDoc, byte[] overflowPdfBytes) throws IOException {
        try (PDDocument overflowDoc = PDDocument.load(new ByteArrayInputStream(overflowPdfBytes))) {
            for (PDPage page : overflowDoc.getPages()) {
                mainDoc.addPage(page);
            }
        }
    }
    
    @Override
    public String getName() {
        return "dependentOverflowHandler";
    }
}
```

---

## 📄 Step 3: Overflow HTML Template

### `dependent_overflow.ftl`
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #333; }
        table { 
            width: 100%; 
            border-collapse: collapse; 
            margin-top: 20px; 
        }
        th, td { 
            border: 1px solid #ddd; 
            padding: 12px; 
            text-align: left; 
        }
        th { 
            background-color: #f2f2f2; 
            font-weight: bold; 
        }
        .page-header {
            text-align: center;
            margin-bottom: 30px;
            font-size: 18px;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="page-header">ADDITIONAL DEPENDENTS (Continued)</div>
    
    <table>
        <thead>
            <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Age</th>
                <th>Student</th>
                <th>Active</th>
            </tr>
        </thead>
        <tbody>
            <#list dependents as dep>
                <tr>
                    <td>${dep.firstName!""}</td>
                    <td>${dep.lastName!""}</td>
                    <td>${dep.age!""}</td>
                    <td>${dep.student?string("Yes", "No")}</td>
                    <td>${dep.active?string("Yes", "No")}</td>
                </tr>
            </#list>
        </tbody>
    </table>
</body>
</html>
```

---

## 🧠 Step 4: Enhanced DataMapper (Optional)

If you want to avoid the hidden `__all_dependents` field, you can enhance the post-processor to **re-extract data from source**:

### Alternative Approach in Post-Processor
```java
@Override
public void process(PDDocument mainDoc, PdfProcessingContext context) throws IOException {
    Integer acroFormLimit = ((Number) context.getConfig().get("acroFormLimit")).intValue();
    
    // Re-extract all dependents from original source data
    Object sourceData = context.getSourceData();
    List<?> allDependents = (List<?>) SimplePathResolver.read(sourceData, "applicants[relationship=dependent]");
    
    if (allDependents == null || allDependents.size() <= acroFormLimit) {
        return;
    }
    
    // Rest of logic same as above...
}
```

This eliminates the need for the hidden mapping field.

---

## 🔧 Step 5: HTML-to-PDF Service

### `HtmlToPdfService.java`
```java
@Service
public class HtmlToPdfService {
    
    public byte[] generateFromTemplate(String templatePath, Map<String, Object> model) throws Exception {
        // Load and process FreeMarker template
        Configuration fmConfig = new Configuration(Configuration.VERSION_2_3_32);
        fmConfig.setClassForTemplateLoading(getClass(), "/templates");
        
        Template template = fmConfig.getTemplate(getTemplateName(templatePath));
        StringWriter htmlWriter = new StringWriter();
        template.process(model, htmlWriter);
        String htmlContent = htmlWriter.toString();
        
        // Convert HTML to PDF using OpenHTML + PDFBox
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(out);
        builder.run();
        
        return out.toByteArray();
    }
    
    private String getTemplateName(String templatePath) {
        return templatePath.startsWith("classpath:") 
            ? templatePath.substring("classpath:".length()) 
            : new File(templatePath).getName();
    }
}
```

---

## 📊 How It Works - Step by Step

### Input Data (5 dependents)
```json
{
  "applicants": [
    { "relationship": "primary", "firstName": "John" },
    { "relationship": "dependent", "firstName": "Dep1" },
    { "relationship": "dependent", "firstName": "Dep2" },
    { "relationship": "dependent", "firstName": "Dep3" },
    { "relationship": "dependent", "firstName": "Dep4" },
    { "relationship": "dependent", "firstName": "Dep5" }
  ]
}
```

### Processing Flow
1. **AcroForm Mapping**:  
   - `dependent.fname.1` = "Dep1"  
   - `dependent.fname.2` = "Dep2"  
   - `dependent.fname.3` = "Dep3"  
   - (Dep4, Dep5 ignored by `maxRepetitions: 3`)

2. **Overflow Detection**:  
   - All 5 dependents extracted as `__all_dependents`
   - Overflow = Dep4, Dep5 (indexes 3, 4)

3. **HTML Template Generation**:  
   - Creates HTML table with Dep4, Dep5
   - Converts to PDF

4. **PDF Merging**:  
   - Main PDF (with 3 dependents) + Overflow PDF (with table)
   - Final output: 2+ pages

---

## ⚠️ Important Considerations

### 1. **Page Breaks in Overflow**
- If overflow has 50+ dependents, HTML→PDF will automatically create multiple pages
- No additional logic needed

### 2. **Styling Consistency**
- Match fonts, colors, and spacing to original PDF
- Use same branding in overflow template

### 3. **Error Handling**
```java
// In post-processor
try {
    // Generate overflow PDF
} catch (Exception e) {
    log.error("Failed to generate overflow pages, continuing with main document", e);
    // Don't fail the entire process
}
```

### 4. **Performance**
- Cache HTML templates (FreeMarker is already cached)
- Consider streaming for very large overflow data

---

## 🎯 Benefits of This Approach

| Benefit | Explanation |
|--------|------------|
| **✅ Maintains AcroForm compatibility** | First 3 dependents are editable |
| **✅ Handles unlimited overflow** | HTML→PDF scales automatically |
| **✅ Professional appearance** | Consistent styling across pages |
| **✅ Configurable** | Change limit via YAML |
| **✅ Extensible** | Add more overflow types easily |

---

## 🚀 Final YAML Configuration (Clean Version)

```yaml
templates:
  - name: "family-application"
    type: PDF
    templatePath: "classpath:templates/family_form.pdf"
    outputPath: "${output.dir}/application_${metadata.submissionId}.pdf"
    mappings:
      # Primary applicant
      - sourceObject: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
        fieldMappings:
          - sourceField: "firstName"
            targetField: "primary.fname.1"

      # First 3 dependents (AcroForm)
      - sourceArray: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "dependent"
        maxRepetitions: 3
        itemMappings:
          - sourceField: "firstName"
            targetFieldTemplate: "dependent.fname.{index}"
          - sourceField: "age"
            targetFieldTemplate: "dependent.age.{index}"

    postProcessors:
      - type: "custom"
        name: "dependentOverflowHandler"
        config:
          acroFormLimit: 3
          overflowTemplate: "classpath:templates/dependent_overflow.ftl"
```

This solution gives you the **best of both worlds**:  
- ✅ **Editable AcroForm fields** for the first few dependents  
- ✅ **Unlimited, professional overflow pages** for additional data  

Perfect for real-world government, insurance, and financial forms! 🚀