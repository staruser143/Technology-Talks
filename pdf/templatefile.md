Great question!

In the current design I provided, **the template is expected to be HTML — even when generating PDF output**. The `PdfTemplateEngine` internally uses the `HtmlTemplateEngine` to generate HTML first, then converts that HTML to PDF using `openhtmltopdf`.

---

## ❓ So what if your *template is already a PDF file* (not HTML)?

That’s a different — and more complex — use case.

PDF files are **not plain-text templates with placeholders** like HTML or FreeMarker templates. They are **binary, structured documents** (often compressed), and you cannot simply do string replacement like `{{name}} → "Alice"`.

---

## ✅ SOLUTION: Use a PDF Library that Supports “Form Fields” or “Placeholders”

To merge data into an **existing PDF template**, you need to:

1. Design the PDF with **named form fields** (e.g., using Adobe Acrobat, LibreOffice, or PDF editors).
2. Use a Java PDF library to **fill those form fields programmatically**.
3. Optionally **flatten** the form so fields become static text.

---

## 🧰 Recommended Library: **Apache PDFBox**

PDFBox is powerful, open-source, and supports AcroForm (PDF forms) manipulation.

### ➕ Add Dependency

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.30</version>
</dependency>
```

---

## 🔄 MODIFIED DESIGN TO SUPPORT PDF TEMPLATES

We’ll extend the system to support two kinds of PDF handling:

| Type | Description |
|------|-------------|
| **PDF from HTML** | Uses HTML template → rendered to PDF (current approach) |
| **PDF Form Template** | Uses existing PDF with form fields → filled with data |

---

## 🆕 New Engine: `PdfFormTemplateEngine.java`

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class PdfFormTemplateEngine implements TemplateEngine {

    @Override
    public byte[] merge(String templatePdfPath, Map<String, Object> data) throws Exception {
        try (PDDocument document = PDDocument.load(new File(templatePdfPath))) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                throw new IllegalArgumentException("PDF does not contain a form");
            }

            // Fill each field
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                PDField field = acroForm.getField(entry.getKey());
                if (field != null) {
                    field.setValue(entry.getValue() != null ? entry.getValue().toString() : "");
                } else {
                    System.out.println("Warning: No field found with name: " + entry.getKey());
                }
            }

            // Optional: Flatten form (make fields non-editable)
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

> ⚠️ Note: This engine expects the **template identifier to be a file path** (not content string), since PDFs are binary. You may want to adjust the `TemplateEngine` interface to accept `Object templateSource` or create a variant.

---

## 🆕 Enhanced `TemplateEngine` Interface (Optional)

To support both text-based (HTML) and binary-based (PDF form) templates:

```java
public interface TemplateEngine {
    byte[] merge(Object templateSource, Map<String, Object> data) throws Exception;
    String getOutputFormat();
}
```

Then update engines:

- `HtmlTemplateEngine`: expects `String`
- `PdfFormTemplateEngine`: expects `String` (file path) or `File` or `InputStream`

---

## 🆕 Enhanced `TemplateMerger` with Template Type Detection

You can now support 3 template types:

1. HTML → HTML
2. HTML → PDF (via rendering)
3. PDF Form → Filled PDF

```java
public class TemplateMerger {

    private final MappingResolver mappingResolver = new MappingResolver();

    public byte[] merge(
            Object templateSource,  // String for HTML, String path for PDF form
            Map<String, Object> sourceData,
            String mappingSpecJson,
            OutputFormat outputFormat,
            TemplateType templateType) throws Exception {

        Map<String, Object> resolvedData = mappingResolver.resolve(sourceData, mappingSpecJson);

        TemplateEngine engine = switch (templateType) {
            case HTML -> new HtmlTemplateEngine();
            case HTML_TO_PDF -> new PdfTemplateEngine(); // renders HTML to PDF
            case PDF_FORM -> new PdfFormTemplateEngine(); // fills existing PDF form
            default -> throw new IllegalArgumentException("Unsupported template type");
        };

        if (outputFormat == OutputFormat.HTML && templateType != TemplateType.HTML) {
            throw new IllegalArgumentException("Cannot output HTML from PDF template");
        }

        return engine.merge(templateSource, resolvedData);
    }

    public enum OutputFormat {
        HTML, PDF
    }

    public enum TemplateType {
        HTML,          // template is HTML string → output as HTML
        HTML_TO_PDF,   // template is HTML string → render to PDF
        PDF_FORM       // template is PDF form (file path) → fill and output PDF
    }
}
```

---

## 📝 How to Create a PDF Form Template

1. Open **LibreOffice Writer** or **Adobe Acrobat**.
2. Design your document.
3. Insert **Form Fields**:
   - In LibreOffice: `View → Toolbars → Form Controls` → add Text Box → right-click → **Control** → set “Name” (e.g., “name”, “email”).
   - Export as PDF → make sure “Create PDF Form” is enabled.
4. In Adobe Acrobat: Tools → Prepare Form → add text fields → name them.

These field names (e.g., “name”, “email”) must match your **mapping spec’s `targetPlaceholder`** (or you can map them via JSON).

---

## 🔄 Updated Mapping Spec for PDF Forms

Since PDF form fields are identified by **field names** (not `{{placeholders}}`), you can reuse the same mapping spec:

```json
{
  "mappings": [
    {
      "sourceField": "user_name",
      "targetPlaceholder": "name"  ← this is now the PDF field name
    },
    {
      "sourceField": "user_email",
      "targetPlaceholder": "email"
    }
  ]
}
```

> ✅ No change needed! The `MappingResolver` outputs a Map like `{ "name" → "Alice", "email" → "alice@example.com" }`, which `PdfFormTemplateEngine` uses as field names.

---

## 🧪 Example Usage with PDF Form Template

```java
public class PdfFormExample {
    public static void main(String[] args) throws Exception {
        Map<String, Object> dbData = Map.of(
            "user_name", "Alice Cooper",
            "user_email", "alice@example.com"
        );

        String mappingSpec = """
            {
              "mappings": [
                { "sourceField": "user_name", "targetPlaceholder": "name" },
                { "sourceField": "user_email", "targetPlaceholder": "email" }
              ]
            }
            """;

        TemplateMerger merger = new TemplateMerger();

        byte[] filledPdf = merger.merge(
            "./template-form.pdf",  // path to PDF form
            dbData,
            mappingSpec,
            TemplateMerger.OutputFormat.PDF,
            TemplateMerger.TemplateType.PDF_FORM
        );

        Files.write(Paths.get("filled-output.pdf"), filledPdf);
        System.out.println("Filled PDF saved!");
    }
}
```

---

## 🆚 Comparison: HTML Template vs PDF Form Template

| Feature | HTML Template → PDF | PDF Form Template |
|--------|----------------------|-------------------|
| Template Format | HTML (text) | PDF (binary) |
| Placeholders | `{{name}}`, `<h1>${name}</h1>` | Named form fields: `name`, `email` |
| Styling | Full CSS control | Limited to PDF form design |
| Dynamic Layout | Yes (flowing content) | Fixed layout |
| Tools to Create | Any HTML editor | Acrobat, LibreOffice, PDF editors |
| Best For | Reports, invoices, dynamic content | Forms, contracts, certificates |

---

## ✅ RECOMMENDATION

- Use **HTML → PDF** for **dynamic, styled, flowing documents** (invoices, reports).
- Use **PDF Form → Filled PDF** for **fixed-layout forms** (applications, certificates, government forms).

---

## 🧩 Final Architecture

```
TemplateMerger
│
├── MappingResolver → resolves DB fields → target keys (field names or placeholders)
│
├── TemplateEngine
     ├── HtmlTemplateEngine        → for HTML output
     ├── PdfTemplateEngine         → for HTML → PDF rendering
     └── PdfFormTemplateEngine     → for filling existing PDF forms
```

---

## ✅ Summary

> ❓ “How does this work if template is PDF file?”

✅ You need to:
- Use a PDF form with named fields.
- Use Apache PDFBox to fill those fields.
- Extend the `TemplateMerger` to support `TemplateType.PDF_FORM`.
- Reuse the same JSON mapping spec — just ensure `targetPlaceholder` matches PDF field names.

This makes your library **truly universal**: it can handle both **HTML templates** and **PDF form templates**, using the same data and mapping spec.

Let me know if you want to support **image insertion**, **barcode generation**, or **multi-page PDF merging** — I can help you extend it further!