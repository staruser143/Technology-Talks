To **generate a PDF from a FreeMarker-based HTML template after merging it with data**, you need a **library that converts HTML to PDF**. Apache PDFBox (which you're using for AcroForm) **cannot render HTML** — it only handles PDF forms and content streams.

---

## ✅ Recommended Solution: **OpenHTML to PDF (Flying Saucer)**

This is the **most popular, mature, and reliable** library for HTML → PDF conversion in Java.

### 📦 Maven Dependencies

```xml
<!-- FreeMarker (for HTML template merging) -->
<dependency>
    <groupId>org.freemarker</groupId>
    <artifactId>freemarker</artifactId>
    <version>2.3.32</version>
</dependency>

<!-- Flying Saucer: HTML to PDF -->
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-pdf</artifactId>
    <version>9.1.22</version>
</dependency>

<!-- Optional: For better font handling -->
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-core</artifactId>
    <version>9.1.22</version>
</dependency>
```

> 💡 **Note**: Flying Saucer uses **iText 5** under the hood (included transitively).

---

## 🧠 How It Works

1. **Merge data with FreeMarker** → get HTML string
2. **Convert HTML string to PDF** using Flying Saucer

---

## 🛠 Implementation Example

### 1. **HTML Template (`summary.ftl`)**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; }
        .applicant { margin-bottom: 20px; }
    </style>
</head>
<body>
    <h1>Application Summary</h1>
    <div class="applicant">
        <h2>Primary Applicant</h2>
        <p>Name: ${primary.firstName} ${primary.lastName}</p>
        <p>SSN: ${primary.ssn}</p>
    </div>
    
    <h2>Dependents</h2>
    <#list dependents as dep>
        <p>${dep.firstName} (Age: ${dep.age})</p>
    </#list>
</body>
</html>
```

### 2. **PDF Generation Service**
```java
import org.xhtmlrenderer.pdf.ITextRenderer;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.nio.file.Path;

@Service
public class HtmlToPdfService {

    private final Configuration fmConfig;

    public HtmlToPdfService() {
        fmConfig = new Configuration(Configuration.VERSION_2_3_32);
        fmConfig.setClassForTemplateLoading(HtmlToPdfService.class, "/templates");
        fmConfig.setDefaultEncoding("UTF-8");
    }

    public void generatePdfFromHtmlTemplate(
        String templateName, 
        Map<String, Object> dataModel, 
        Path outputPdfPath
    ) throws Exception {
        
        // 1. Merge data with FreeMarker → HTML string
        Template template = fmConfig.getTemplate(templateName);
        StringWriter htmlWriter = new StringWriter();
        template.process(dataModel, htmlWriter);
        String htmlContent = htmlWriter.toString();

        // 2. Convert HTML to PDF
        try (OutputStream out = new FileOutputStream(outputPdfPath.toFile())) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(out);
        }
    }
}
```

### 3. **Usage in Your TemplateMergeService**
```java
// In your existing TemplateMergeService
public void mergeHtmlToPdfTemplate(String templateName, Object sourceData, Path outputPath) throws Exception {
    // 1. Map data using your existing DataMapper
    TemplateDefinition def = mergeConfig.getTemplateByName(templateName);
    Map<String, Object> mappedData = dataMapper.mapData(sourceData, def.getMappings());
    
    // 2. Generate PDF from HTML template
    htmlToPdfService.generatePdfFromHtmlTemplate(
        getTemplateNameWithoutExtension(def.getTemplatePath()),
        mappedData,
        outputPath
    );
}
```

---

## 🔧 Important Considerations

### 1. **HTML Must Be XHTML-Compliant**
Flying Saucer requires **well-formed XHTML**:
- Use `<br />` not `<br>`
- Close all tags (`<p>...</p>`)
- Include `<!DOCTYPE html>`

### 2. **CSS Support is Limited**
- ✅ Basic CSS (fonts, colors, margins, padding)
- ❌ Flexbox, Grid, advanced CSS3
- ✅ Tables, lists, basic positioning

### 3. **Font Handling**
For custom fonts or Unicode (e.g., Chinese, Arabic):
```java
ITextRenderer renderer = new ITextRenderer();
renderer.getFontResolver().addFont("path/to/font.ttf", true);
```

### 4. **Page Size & Margins**
Control via CSS:
```html
<style>
@page {
    size: letter;
    margin: 1in;
}
</style>
```

---

## 🆚 Alternative Libraries

| Library | Pros | Cons |
|--------|------|------|
| **Flying Saucer** | ✅ Mature, reliable, good CSS support | ❌ Uses older iText 5 (AGPL license) |
| **OpenPDF + Flying Saucer** | ✅ Apache 2.0 license | ❌ Slightly less maintained |
| **Puppeteer (via Java)** | ✅ Chrome-quality rendering | ❌ Requires Node.js + Chrome |
| **Thymeleaf + OpenPDF** | ✅ Modern alternative to FreeMarker | ❌ Still needs HTML→PDF engine |

> 💡 **For most enterprise apps, Flying Saucer is the best choice**.

---

## 📦 Updated Dependencies (Apache 2.0 License)

If you're concerned about iText 5's AGPL license, use **OpenPDF**:

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.30</version>
</dependency>
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-pdf-itext5</artifactId>
    <version>9.1.22</version>
    <exclusions>
        <exclusion>
            <groupId>com.lowagie</groupId>
            <artifactId>itext</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

Then configure Flying Saucer to use OpenPDF:
```java
ITextRenderer renderer = new ITextRenderer(new ITextOutputDevice(), new ITextFontResolver(), new OpenPDFPDFCreationRenderer());
```

---

## ✅ Summary

To generate PDF from FreeMarker HTML templates, you need:

1. **FreeMarker** → merge data with HTML template
2. **Flying Saucer** → convert HTML to PDF
3. **Proper XHTML/CSS** → ensure rendering quality

Your final workflow:
```
JSON Data 
  → DataMapper 
  → Map<String, Object> 
  → FreeMarker 
  → HTML String 
  → Flying Saucer 
  → PDF File
```

This gives you **dynamic, data-driven PDFs** with full layout control — perfect for summaries, reports, and cover letters! 🚀