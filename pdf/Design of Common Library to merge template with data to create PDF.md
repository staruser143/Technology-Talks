# Query

I need a common library which can be used to merge data from the database with a template,  which can be either html or pdf, and generate the output in a specific format either html or pdf. it should use a json specification to map fields from database to placeholder fields in template . how do I design one using java.

# Response 
Designing a common Java library to merge database data with templates (HTML/PDF) using a JSON mapping specification is a powerful and reusable approach. Below is a **complete design and implementation guide** using modern Java practices, modularity, and popular libraries.

---

## ✅ GOALS

1. Accept **database data** (as `Map<String, Object>` or `List<Map<String, Object>>`).
2. Accept a **template** (HTML or PDF).
3. Accept a **JSON mapping spec** that maps DB fields → template placeholders.
4. Output merged result as **HTML or PDF**.
5. Be **extensible**, **configurable**, and **testable**.

---

## 🧱 ARCHITECTURE OVERVIEW

```
+---------------------+
|   TemplateMerger    | ← Main API
+----------+----------+
           |
           v
+---------------------+
|  MappingResolver    | ← Parses JSON spec → Field mappings
+---------------------+
           |
           v
+---------------------+     +----------------------+
| TemplateEngine (IF) |<----| HtmlTemplateEngine   |
+---------------------+     +----------------------+
           |                | PdfTemplateEngine    |
           |                +----------------------+
           v
     Output (HTML/PDF)
```

---

## 📦 DEPENDENCIES (Maven)

```xml
<dependencies>
    <!-- JSON Parsing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>

    <!-- HTML Templating -->
    <dependency>
        <groupId>org.freemarker</groupId>
        <artifactId>freemarker</artifactId>
        <version>2.3.32</version>
    </dependency>

    <!-- PDF Generation from HTML -->
    <dependency>
        <groupId>com.openhtmltopdf</groupId>
        <artifactId>openhtmltopdf-core</artifactId>
        <version>1.0.10</version>
    </dependency>
    <dependency>
        <groupId>com.openhtmltopdf</groupId>
        <artifactId>openhtmltopdf-pdfbox</artifactId>
        <version>1.0.10</version>
    </dependency>

    <!-- Logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.7</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.8</version>
    </dependency>
</dependencies>
```

---

## 📄 JSON MAPPING SPEC FORMAT

Example `mapping.json`:

```json
{
  "mappings": [
    {
      "sourceField": "user_name",
      "targetPlaceholder": "{{name}}"
    },
    {
      "sourceField": "user_email",
      "targetPlaceholder": "{{email}}"
    },
    {
      "sourceField": "order_date",
      "targetPlaceholder": "{{date}}",
      "formatter": "yyyy-MM-dd"
    }
  ]
}
```

> You can extend this later with conditions, loops, default values, etc.

---

## 🧩 CORE CLASSES

### 1. `FieldMapping.java` (POJO for JSON mapping)

```java
public class FieldMapping {
    private String sourceField;
    private String targetPlaceholder;
    private String formatter; // optional

    // Getters & Setters
    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }

    public String getTargetPlaceholder() { return targetPlaceholder; }
    public void setTargetPlaceholder(String targetPlaceholder) { this.targetPlaceholder = targetPlaceholder; }

    public String getFormatter() { return formatter; }
    public void setFormatter(String formatter) { this.formatter = formatter; }
}
```

### 2. `MappingSpec.java`

```java
import java.util.List;

public class MappingSpec {
    private List<FieldMapping> mappings;

    public List<FieldMapping> getMappings() { return mappings; }
    public void setMappings(List<FieldMapping> mappings) { this.mappings = mappings; }
}
```

### 3. `MappingResolver.java`

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MappingResolver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> resolve(Map<String, Object> sourceData, String mappingSpecJson) throws IOException {
        MappingSpec spec = objectMapper.readValue(mappingSpecJson, MappingSpec.class);
        return applyMappings(sourceData, spec.getMappings());
    }

    private Map<String, Object> applyMappings(Map<String, Object> sourceData, List<FieldMapping> mappings) {
        Map<String, Object> resolved = new HashMap<>();

        for (FieldMapping mapping : mappings) {
            Object value = sourceData.get(mapping.getSourceField());
            if (value != null && mapping.getFormatter() != null) {
                if (value instanceof java.util.Date) {
                    value = new SimpleDateFormat(mapping.getFormatter()).format(value);
                }
            }
            resolved.put(mapping.getTargetPlaceholder(), value);
        }

        return resolved;
    }
}
```

---

## 🔧 TEMPLATE ENGINE INTERFACE

```java
public interface TemplateEngine {
    byte[] merge(String templateContent, Map<String, Object> data) throws Exception;
    String getOutputFormat(); // "HTML" or "PDF"
}
```

---

## 🌐 HTML TEMPLATE ENGINE (FreeMarker)

```java
import freemarker.template.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HtmlTemplateEngine implements TemplateEngine {

    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);

    public HtmlTemplateEngine() {
        cfg.setTemplateLoader(new StringTemplateLoader());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

    @Override
    public byte[] merge(String templateContent, Map<String, Object> data) throws Exception {
        StringTemplateLoader loader = new StringTemplateLoader();
        String name = "template";
        loader.putTemplate(name, templateContent);
        cfg.setTemplateLoader(loader);

        Template template = cfg.getTemplate(name);
        StringWriter writer = new StringWriter();
        template.process(data, writer);
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getOutputFormat() {
        return "HTML";
    }
}
```

---

## 📄 PDF TEMPLATE ENGINE (Converts HTML → PDF)

```java
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PdfTemplateEngine implements TemplateEngine {

    private final HtmlTemplateEngine htmlEngine = new HtmlTemplateEngine();

    @Override
    public byte[] merge(String templateContent, Map<String, Object> data) throws Exception {
        // First generate HTML
        byte[] htmlBytes = htmlEngine.merge(templateContent, data);
        String html = new String(htmlBytes, StandardCharsets.UTF_8);

        // Convert to PDF
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();

        return os.toByteArray();
    }

    @Override
    public String getOutputFormat() {
        return "PDF";
    }
}
```

> ✅ This approach reuses HTML templates for PDF — very maintainable!

---

## 🚀 MAIN MERGER CLASS (Facade)

```java
import java.io.IOException;

public class TemplateMerger {

    private final MappingResolver mappingResolver = new MappingResolver();

    public byte[] merge(
            String templateContent,
            Map<String, Object> sourceData,
            String mappingSpecJson,
            OutputFormat outputFormat) throws Exception {

        Map<String, Object> resolvedData = mappingResolver.resolve(sourceData, mappingSpecJson);

        TemplateEngine engine = switch (outputFormat) {
            case HTML -> new HtmlTemplateEngine();
            case PDF -> new PdfTemplateEngine();
            default -> throw new IllegalArgumentException("Unsupported format: " + outputFormat);
        };

        return engine.merge(templateContent, resolvedData);
    }

    public enum OutputFormat {
        HTML, PDF
    }
}
```

---

## 🧪 EXAMPLE USAGE

```java
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Example {
    public static void main(String[] args) throws Exception {
        // Sample DB row
        Map<String, Object> dbData = new HashMap<>();
        dbData.put("user_name", "Alice Cooper");
        dbData.put("user_email", "alice@example.com");
        dbData.put("order_date", new java.util.Date());

        // Load mapping spec (from file or string)
        String mappingSpec = Files.readString(Paths.get("mapping.json"));

        // Load template
        String template = """
            <html>
            <body>
                <h1>Hello {{name}}!</h1>
                <p>Email: {{email}}</p>
                <p>Order Date: {{date}}</p>
            </body>
            </html>
            """;

        TemplateMerger merger = new TemplateMerger();

        // Generate HTML
        byte[] htmlOutput = merger.merge(template, dbData, mappingSpec, TemplateMerger.OutputFormat.HTML);
        System.out.println(new String(htmlOutput));

        // Generate PDF
        byte[] pdfOutput = merger.merge(template, dbData, mappingSpec, TemplateMerger.OutputFormat.PDF);
        Files.write(Paths.get("output.pdf"), pdfOutput);
        System.out.println("PDF saved to output.pdf");
    }
}
```

---

## 🧭 EXTENSIONS & ENHANCEMENTS

1. **Support for Lists / Loops**: Extend mapping spec to handle `{{#each}}` or FreeMarker `<#list>`.
2. **Conditional Rendering**: Add `if`/`else` logic in mapping spec.
3. **Multiple Templates**: Merge multiple templates into one output.
4. **Caching**: Cache parsed templates or mapping specs for performance.
5. **Validation**: Validate mapping spec and data before merging.
6. **Custom Formatters / Functions**: Allow registering custom formatters in `MappingResolver`.

---

## ✅ ADVANTAGES

- **Separation of Concerns**: Mapping, templating, and rendering are decoupled.
- **Reusable**: Same template can output HTML or PDF.
- **JSON Configurable**: Non-developers can manage mappings.
- **Extensible**: Easy to add new engines (e.g., Word, Excel).

---

## 📁 PROJECT STRUCTURE SUGGESTION

```
src/
├── main/
│   ├── java/
│   │   └── com/yourcompany/templatemerger/
│   │       ├── TemplateMerger.java
│   │       ├── MappingResolver.java
│   │       ├── TemplateEngine.java
│   │       ├── HtmlTemplateEngine.java
│   │       ├── PdfTemplateEngine.java
│   │       └── model/
│   │           ├── FieldMapping.java
│   │           └── MappingSpec.java
│   └── resources/
│       └── mapping.json
└── test/
    └── java/... (unit tests)
```

---

## 🧾 CONCLUSION

You now have a **flexible, configurable, Java-based template merging library** that:

- Uses **JSON to map DB fields → placeholders**.
- Supports **HTML and PDF output**.
- Is **easy to extend and maintain**.

This design is production-ready and can be packaged as a JAR for reuse across microservices or applications.

