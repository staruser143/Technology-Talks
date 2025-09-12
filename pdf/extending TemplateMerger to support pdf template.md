# Query 

how would the templatemerger class be extended to support pdf_form.

# Response 
Great! You want to **extend the `TemplateMerger` class to support `PDF_FORM` templates** — meaning: you have an existing PDF file with form fields, and you want to fill it with data mapped from a database using a JSON spec.

Let’s walk through the **minimal, clean, production-ready extension** of `TemplateMerger` to support this.

---

## ✅ GOAL

Support this usage:

```java
byte[] output = templateMerger.merge(
    "./form-template.pdf",      // ← PDF file path
    dbData,                     // ← Map from DB
    mappingSpecJson,            // ← JSON mapping spec
    TemplateMerger.OutputFormat.PDF,
    TemplateMerger.TemplateType.PDF_FORM  // ← NEW!
);
```

---

## 🔄 STEP 1: Update `TemplateEngine` Interface

Since PDF form templates are identified by **file paths** (not strings like HTML), we need to make the `templateSource` parameter flexible.

### ➤ Option A: Use `Object` (Simple & Practical)

```java
public interface TemplateEngine {
    byte[] merge(Object templateSource, Map<String, Object> data) throws Exception;
    String getOutputFormat();
}
```

> ✅ Pros: Simple, backward compatible, handles String (HTML) or String/Path/File (PDF).
>  
> ⚠️ Cons: Loses type safety — but acceptable for a flexible template system.

---

## 🧱 STEP 2: Implement `PdfFormTemplateEngine`

We already wrote this — here’s the **final polished version**:

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class PdfFormTemplateEngine implements TemplateEngine {

    @Override
    public byte[] merge(Object templateSource, Map<String, Object> data) throws Exception {
        if (!(templateSource instanceof String)) {
            throw new IllegalArgumentException("Expected file path as String for PDF form template");
        }

        String pdfPath = (String) templateSource;

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                throw new IllegalArgumentException("No form found in PDF: " + pdfPath);
            }

            // Fill form fields
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String fieldName = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue().toString() : "";

                PDField field = acroForm.getField(fieldName);
                if (field != null) {
                    field.setValue(value);
                } else {
                    System.err.println("Warning: PDF form has no field named '" + fieldName + "'");
                }
            }

            // Optional: Flatten form to make fields non-editable
            acroForm.flatten();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    @Override
    public String getOutputFormat() {
        return "PDF";
    }
}
```

> ✅ Uses Apache PDFBox — make sure you have the dependency!

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.30</version>
</dependency>
```

---

## 🔄 STEP 3: Extend `TemplateMerger` with `TemplateType`

We’ll add an enum `TemplateType` and modify the `merge` method to accept it.

### ➤ Updated `TemplateMerger.java`

```java
import java.util.Map;

public class TemplateMerger {

    private final MappingResolver mappingResolver = new MappingResolver();

    // --- Public API ---
    public byte[] merge(
            Object templateSource,
            Map<String, Object> sourceData,
            String mappingSpecJson,
            OutputFormat outputFormat,
            TemplateType templateType) throws Exception {

        Map<String, Object> resolvedData = mappingResolver.resolve(sourceData, mappingSpecJson);

        TemplateEngine engine = getEngine(templateType);

        // Validate: PDF_FORM can only output PDF
        if (templateType == TemplateType.PDF_FORM && outputFormat != OutputFormat.PDF) {
            throw new IllegalArgumentException("PDF_FORM template can only generate PDF output");
        }

        return engine.merge(templateSource, resolvedData);
    }

    // --- Factory Method ---
    private TemplateEngine getEngine(TemplateType templateType) {
        return switch (templateType) {
            case HTML -> new HtmlTemplateEngine();
            case HTML_TO_PDF -> new PdfTemplateEngine();
            case PDF_FORM -> new PdfFormTemplateEngine();
        };
    }

    // --- Enums ---
    public enum OutputFormat {
        HTML, PDF
    }

    public enum TemplateType {
        HTML,          // Template is HTML string → output HTML
        HTML_TO_PDF,   // Template is HTML string → render to PDF
        PDF_FORM       // Template is PDF file path → fill form → output PDF
    }
}
```

---

## 🔄 STEP 4 (Optional): Keep Backward Compatibility

If you already have code calling `merge(...)` without `TemplateType`, you can overload the method:

```java
// Legacy method — assumes HTML template
public byte[] merge(
        String templateContent,
        Map<String, Object> sourceData,
        String mappingSpecJson,
        OutputFormat outputFormat) throws Exception {
    TemplateType type = (outputFormat == OutputFormat.HTML) ? TemplateType.HTML : TemplateType.HTML_TO_PDF;
    return merge(templateContent, sourceData, mappingSpecJson, outputFormat, type);
}
```

Now both old and new code work:

```java
// ✅ Old usage still works
merger.merge(htmlString, data, mapping, OutputFormat.PDF);

// ✅ New usage for PDF forms
merger.merge("./form.pdf", data, mapping, OutputFormat.PDF, TemplateType.PDF_FORM);
```

---

## 📄 STEP 5: Example JSON Mapping for PDF Form

Your mapping spec remains unchanged — `targetPlaceholder` now maps to **PDF form field names**.

```json
{
  "mappings": [
    { "sourceField": "user_name",   "targetPlaceholder": "full_name" },
    { "sourceField": "user_email",  "targetPlaceholder": "email" },
    { "sourceField": "order_date",  "targetPlaceholder": "date", "formatter": "MM/dd/yyyy" }
  ]
}
```

> 💡 Tip: Name your PDF form fields in Acrobat/LibreOffice to match `targetPlaceholder` values (e.g., “full_name”, “email”).

---

## 🧪 STEP 6: Usage Example

```java
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PdfFormDemo {
    public static void main(String[] args) throws Exception {
        // Simulate DB data
        Map<String, Object> dbData = new HashMap<>();
        dbData.put("user_name", "John Doe");
        dbData.put("user_email", "john@example.com");
        dbData.put("order_date", new java.util.Date());

        // Load mapping spec
        String mappingSpec = """
            {
              "mappings": [
                { "sourceField": "user_name", "targetPlaceholder": "full_name" },
                { "sourceField": "user_email", "targetPlaceholder": "email" },
                { "sourceField": "order_date", "targetPlaceholder": "date", "formatter": "yyyy-MM-dd" }
              ]
            }
            """;

        TemplateMerger merger = new TemplateMerger();

        // Fill PDF form
        byte[] filledPdf = merger.merge(
            "./src/test/resources/form-template.pdf",  // ← your PDF form
            dbData,
            mappingSpec,
            TemplateMerger.OutputFormat.PDF,
            TemplateMerger.TemplateType.PDF_FORM
        );

        Files.write(Paths.get("output-filled-form.pdf"), filledPdf);
        System.out.println("✅ PDF form filled and saved to output-filled-form.pdf");
    }
}
```

---

## ✅ FINAL ARCHITECTURE

```
TemplateMerger
│
├── resolves data using MappingResolver → { "full_name": "John Doe", ... }
│
├── selects engine by TemplateType:
     ├── HTML → HtmlTemplateEngine
     ├── HTML_TO_PDF → PdfTemplateEngine (HTML → PDF)
     └── PDF_FORM → PdfFormTemplateEngine (fills PDF form fields)
│
└── returns byte[] (HTML or PDF)
```

---

## 🎯 ADVANTAGES OF THIS DESIGN

- ✅ **Single interface** for HTML and PDF templates.
- ✅ **Reuse mapping spec** — no change needed.
- ✅ **Type-safe enums** guide correct usage.
- ✅ **Backward compatible** — old code still works.
- ✅ **Clear separation** — each engine does one thing well.

---

## 🚀 NEXT STEPS / ENHANCEMENTS

- Add support for `InputStream` or `byte[]` as PDF template source.
- Add field validation — throw if required field is missing.
- Support dropdowns, checkboxes, radio buttons in PDF forms.
- Add digital signature after filling.
- Cache `PDDocument` if processing many records with same template.

---

## ✅ SUMMARY

You’ve now extended `TemplateMerger` to fully support **PDF form templates**:

1. Added `TemplateType.PDF_FORM`.
2. Implemented `PdfFormTemplateEngine` using Apache PDFBox.
3. Kept mapping spec unchanged — `targetPlaceholder` = PDF field name.
4. Maintained backward compatibility.

This makes your library **universal**: whether you start with HTML or PDF, you can generate HTML or PDF output using the same clean, JSON-driven mapping system.

