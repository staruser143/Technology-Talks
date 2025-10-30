Yes — **absolutely!** Your library can be extended to support **multiple template files** (both **FreeMarker HTML** and **AcroForm PDF**) in the **same YAML configuration file**.

This is a powerful enhancement that enables:
- Generating **both a PDF form and an HTML summary** from the same source data.
- Creating **multiple output documents** (e.g., `application.pdf`, `cover_letter.html`, `checklist.pdf`) in one merge operation.
- Supporting **multi-document workflows** (e.g., for government forms, insurance packets, etc.).

---

## ✅ Design Strategy

Instead of having **one** `templateType`, `templatePath`, and `outputPath` at the root level, promote them to a **per-template** structure.

### 🔁 Before (Single Template)
```yaml
templateType: PDF
templatePath: ...
outputPath: ...
mappings: [...]
```

### ✅ After (Multiple Templates)
```yaml
templates:
  - type: PDF
    templatePath: classpath:forms/applicant.pdf
    outputPath: /out/applicant.pdf
    mappings: [...]   # specific to this template

  - type: HTML
    templatePath: classpath:templates/summary.ftl
    outputPath: /out/summary.html
    mappings: [...]   # can reuse or customize
```

> ✅ **Each template has its own mapping rules**, allowing:
> - Different field names (PDF: `applicant.fname.1` vs HTML: `applicantName`)
> - Different filters/transforms per output
> - Shared or independent data extraction

---

## 🧩 Updated `MergeConfig.java`

```java
package com.example.templatemerge.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MergeConfig {

    /**
     * List of template definitions (PDF, HTML, etc.)
     */
    private List<TemplateDefinition> templates = new ArrayList<>();

    public void validate() {
        if (templates == null || templates.isEmpty()) {
            throw new IllegalStateException("At least one template is required");
        }
        for (int i = 0; i < templates.size(); i++) {
            TemplateDefinition def = templates.get(i);
            if (def.getType() == null) {
                throw new IllegalStateException("Template " + i + ": 'type' is required");
            }
            if (def.getTemplatePath() == null || def.getTemplatePath().trim().isEmpty()) {
                throw new IllegalStateException("Template " + i + ": 'templatePath' is required");
            }
            if (def.getOutputPath() == null || def.getOutputPath().trim().isEmpty()) {
                throw new IllegalStateException("Template " + i + ": 'outputPath' is required");
            }
            for (int j = 0; j < def.getMappings().size(); j++) {
                try {
                    def.getMappings().get(j).validate();
                } catch (IllegalStateException e) {
                    throw new IllegalStateException(
                        "Template " + i + ", mapping " + j + ": " + e.getMessage(), e);
                }
            }
        }
    }
}
```

---

## 🧩 New: `TemplateDefinition.java`

```java
package com.example.templatemerge.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TemplateDefinition {

    /**
     * Template type: PDF or HTML
     */
    private TemplateType type;

    /**
     * Path to source template (PDF or FTL)
     */
    private String templatePath;

    /**
     * Output file path
     */
    private String outputPath;

    /**
     * Mappings specific to this template
     */
    private List<FieldMapping> mappings = new ArrayList<>();
}
```

> 🔁 **Note**: `FieldMapping`, `ItemMapping`, etc., remain **unchanged** — they are reused per template.

---

## 📄 Example YAML: Multi-Template Config

```yaml
templates:
  # AcroForm PDF: official form
  - type: PDF
    templatePath: classpath:templates/applicant_form.pdf
    outputPath: /tmp/output/applicant.pdf
    mappings:
      - sourceField: "$.applicant.firstName"
        targetField: "applicant.fname.1"
      - sourceArray: "$.dependents"
        itemFilters:
          - field: "$.age"
            operator: LT
            value: 26
        itemMappings:
          - sourceField: "$.firstName"
            targetFieldTemplate: "dependent.fname.{index}"

  # FreeMarker HTML: printable summary
  - type: HTML
    templatePath: classpath:templates/summary.ftl
    outputPath: /tmp/output/summary.html
    mappings:
      - sourceField: "$.applicant.fullName"
        targetField: "fullName"   # matches FreeMarker ${fullName}
      - sourceField: "$.dependents"
        targetField: "dependents" # pass entire list to template
        transforms:
          - type: custom
            name: filterActiveDependents

  # Another PDF: checklist
  - type: PDF
    templatePath: classpath:templates/checklist.pdf
    outputPath: /tmp/output/checklist.pdf
    mappings:
      - sourceField: "$.metadata.submissionId"
        targetField: "submission_id.1"
```

> 💡 **Key Insight**:  
> - The **same source JSON** is used for all templates.  
> - Each template defines **how it wants the data shaped** (via its own `mappings`).  
> - HTML templates can receive **rich objects** (e.g., full `dependents` list), while PDFs need **flattened, indexed fields**.

---

## 🧠 Updated `TemplateMergeService`

Your service now processes **all templates** in the config:

```java
@Service
public class TemplateMergeService {

    private final Map<TemplateType, TemplateMerger> mergers;
    private final ObjectMapper objectMapper;

    public void mergeFromJson(String json, MergeConfig config) throws IOException {
        Object jsonData = objectMapper.readValue(json, Object.class); // or JsonNode
        config.validate();

        for (TemplateDefinition templateDef : config.getTemplates()) {
            TemplateMerger merger = mergers.get(templateDef.getType());
            if (merger == null) {
                throw new IllegalArgumentException("Unsupported template type: " + templateDef.getType());
            }

            // Create a temporary MergeConfig-like object for this template
            // Or refactor DataMapper to accept mappings directly
            Map<String, Object> mappedData = dataMapper.mapData(jsonData, templateDef.getMappings());

            // Pass mappedData + templateDef to merger
            merger.merge(mappedData, templateDef);
        }
    }
}
```

> 🔧 **Refactor Tip**: Update `DataMapper.mapData(...)` to accept `List<FieldMapping>` instead of `MergeConfig`:
> ```java
> public Map<String, Object> mapData(Object jsonData, List<FieldMapping> mappings) { ... }
> ```

---

## 🖨️ Merger Interface Update

Update `TemplateMerger` to accept `TemplateDefinition`:

```java
public interface TemplateMerger {
    void merge(Map<String, Object> mappedData, TemplateDefinition templateDef) throws IOException;
}
```

### Example: `PdfTemplateMerger`

```java
@Override
public void merge(Map<String, Object> mappedData, TemplateDefinition def) throws IOException {
    try (PDDocument doc = PDDocument.load(ResourceUtils.getFile(def.getTemplatePath()))) {
        PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
        for (Map.Entry<String, Object> entry : mappedData.entrySet()) {
            PDField field = form.getField(entry.getKey());
            if (field != null) {
                field.setValue(entry.getValue().toString());
            }
        }
        doc.save(def.getOutputPath());
    }
}
```

### Example: `HtmlTemplateMerger`

```java
@Override
public void merge(Map<String, Object> model, TemplateDefinition def) throws IOException {
    Template template = fmConfig.getTemplate(new File(def.getTemplatePath()).getName());
    try (Writer out = new FileWriter(def.getOutputPath())) {
        template.process(model, out);
    }
}
```

> ✅ **HTML Advantage**: You can pass **lists, nested objects** directly to FreeMarker — no need to flatten!

---

## ✅ Benefits of This Design

| Feature | Supported? |
|-------|-----------|
| Multiple PDFs | ✅ |
| Multiple HTMLs | ✅ |
| Mix of PDF + HTML | ✅ |
| Template-specific mappings | ✅ |
| Shared source data | ✅ |
| Independent filters/transforms per template | ✅ |
| Backward compatible (wrap single template in list) | ✅ |

---

## 🔄 Backward Compatibility (Optional)

If you want to support **legacy single-template YAML**, add a converter:

```java
// In MergeConfig deserialization
if (templates.isEmpty() && (templateType != null || templatePath != null)) {
    // auto-wrap into templates list
    TemplateDefinition def = new TemplateDefinition();
    def.setType(templateType);
    def.setTemplatePath(templatePath);
    def.setOutputPath(outputPath);
    def.setMappings(mappings);
    this.templates = Arrays.asList(def);
}
```

But for a **new library**, it’s cleaner to **require the `templates[]` structure** from day one.

---

## ✅ Summary

You now have a **fully extensible, multi-template merging engine** that supports:

- 📄 **AcroForm PDFs** with index-based fields
- 🌐 **FreeMarker HTML** with rich data models
- 🔍 **JsonPath-powered data extraction**
- 🧩 **Per-template mapping rules**
- 🚦 **Conditional filtering & transformations**

All driven by a **single, clean YAML config**.

Let me know if you'd like the full updated `TemplateMergeService` or a sample multi-template Spring Boot app!