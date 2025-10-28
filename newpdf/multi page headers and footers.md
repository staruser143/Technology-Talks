Excellent! Supporting **multi-page PDF forms with headers/footers** is essential for professional documents (e.g., tax forms, applications, contracts). Below is a complete solution using **Apache PDFBox** that:

✅ Creates **multi-page fillable PDFs**  
✅ Adds **consistent headers/footers** on every page  
✅ Preserves **form field interactivity** across pages  
✅ Works seamlessly with your existing **YAML mapping + JSON data** pipeline  

---

## 🧩 Key Concepts

| Concept | How PDFBox Handles It |
|--------|------------------------|
| **Multi-page form** | Add multiple `PDPage` objects to `PDDocument` |
| **Headers/Footers** | Draw static text/graphics on each page’s content stream |
| **Form fields** | Fields can be placed on any page; `PDAcroForm` is document-wide |
| **Page numbers** | Insert dynamically (e.g., "Page 1 of 3") |

---

## 📦 1. Enhanced `TestPdfGenerator` for Multi-Page

### ✅ Features:
- Generates **N pages** of form content
- Adds **header** (e.g., form title) and **footer** (e.g., page numbers)
- Places form fields on correct pages
- Keeps all fields part of the **same AcroForm**

### `TestPdfGenerator.java` (Updated)

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class TestPdfGenerator {

    private static final float MARGIN = 50;
    private static final float HEADER_HEIGHT = 40;
    private static final float FOOTER_HEIGHT = 30;
    private static final float LINE_HEIGHT = 25;

    /**
     * Generates a multi-page test PDF with headers, footers, and form fields.
     *
     * @param outputPath     Output PDF path
     * @param totalPages     Total number of pages
     * @param maxDependents  Number of dependent slots (distributed across pages)
     */
    public static void generateMultiPageTestPdf(Path outputPath, int totalPages, int maxDependents) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);
            acroForm.setDefaultResources(acroForm.getDefaultResources());
            acroForm.setDefaultAppearance("/Helv 12 Tf 0 0 0 rg");

            // Create pages
            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                // Add header and footer
                addHeader(document, page, "Application Form", pageNum, totalPages);
                addFooter(document, page, "Confidential", pageNum, totalPages);

                // Add form content (fields)
                float startY = PDRectangle.A4.getHeight() - HEADER_HEIGHT - MARGIN;
                float endY = FOOTER_HEIGHT + MARGIN;
                addPageContent(document, acroForm, page, startY, endY, pageNum, totalPages, maxDependents);
            }

            document.save(outputPath.toFile());
        }
    }

    // --- Header ---
    private static void addHeader(PDDocument doc, PDPage page, String title, int pageNum, int totalPages) throws IOException {
        float y = PDRectangle.A4.getHeight() - 30;
        try (var content = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.newLineAtOffset(MARGIN, y);
            content.showText(title);
            content.setFont(PDType1Font.HELVETICA, 10);
            content.newLineAtOffset(PDRectangle.A4.getWidth() - 2 * MARGIN - 100, 0);
            content.showText("Page " + pageNum + " of " + totalPages);
            content.endText();
        }
    }

    // --- Footer ---
    private static void addFooter(PDDocument doc, PDPage page, String footerText, int pageNum, int totalPages) throws IOException {
        float y = 20;
        try (var content = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);
            content.newLineAtOffset(MARGIN, y);
            content.showText(footerText);
            content.newLineAtOffset(PDRectangle.A4.getWidth() - 2 * MARGIN - 150, 0);
            content.showText("Form ID: APP-2025");
            content.endText();
        }
    }

    // --- Page Content (Form Fields) ---
    private static void addPageContent(
        PDDocument doc, PDAcroForm form, PDPage page,
        float startY, float endY, int pageNum, int totalPages, int maxDependents
    ) throws IOException {
        float y = startY;
        float xLabel = MARGIN;
        float xField = MARGIN + 150;

        if (pageNum == 1) {
            // Page 1: Primary applicant + first few dependents
            addSectionLabel(page, xLabel, y, "Primary Applicant");
            y -= LINE_HEIGHT;
            addTextField(doc, form, page, xLabel, y, "First Name:", "PrimaryApplicant.FName.1", xField);
            y -= LINE_HEIGHT;
            addTextField(doc, form, page, xLabel, y, "Last Name:", "PrimaryApplicant.LName.1", xField);
            y -= 2 * LINE_HEIGHT;

            int dependentsPerPage = Math.max(1, maxDependents / totalPages);
            int startIdx = 0;
            int endIdx = Math.min(dependentsPerPage, maxDependents);

            addDependents(doc, form, page, xLabel, xField, y, startIdx, endIdx);
        } else if (pageNum <= totalPages) {
            // Subsequent pages: remaining dependents
            int dependentsPerPage = Math.max(1, maxDependents / totalPages);
            int startIdx = (pageNum - 1) * dependentsPerPage;
            int endIdx = Math.min(startIdx + dependentsPerPage, maxDependents);

            if (startIdx < maxDependents) {
                addSectionLabel(page, xLabel, y, "Dependents (Continued)");
                y -= LINE_HEIGHT;
                addDependents(doc, form, page, xLabel, xField, y, startIdx, endIdx);
            }
        }
    }

    private static void addDependents(
        PDDocument doc, PDAcroForm form, PDPage page,
        float xLabel, float xField, float yStart,
        int startIndex, int endIndex
    ) throws IOException {
        float y = yStart;
        for (int i = startIndex; i < endIndex; i++) {
            int depNum = i + 1;
            addSectionLabel(page, xLabel, y, "Dependent " + depNum);
            y -= LINE_HEIGHT;
            addTextField(doc, form, page, xLabel, y, "First Name:", "Dependent.FName." + depNum, xField);
            y -= LINE_HEIGHT;
            addTextField(doc, form, page, xLabel, y, "Last Name:", "Dependent.LName." + depNum, xField);
            y -= 2 * LINE_HEIGHT;
        }
    }

    // --- Reusable Field Helpers ---
    private static void addSectionLabel(PDPage page, float x, float y, String text) throws IOException {
        try (var content = new org.apache.pdfbox.pdmodel.PDPageContentStream(page.getDocument(), page, true, true)) {
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(x, y);
            content.showText(text);
            content.endText();
        }
    }

    private static void addTextField(
        PDDocument doc, PDAcroForm form, PDPage page,
        float xLabel, float y, String labelText, String fieldName, float xField
    ) throws IOException {
        addSectionLabel(page, xLabel, y, labelText);
        PDTextField field = new PDTextField(form);
        field.setPartialName(fieldName);
        addFieldWidget(field, page, xField, y - 12, 200, 20);
        form.getFields().add(field);
    }

    private static void addFieldWidget(PDField field, PDPage page, float x, float y, float width, float height) {
        PDAnnotationWidget widget = field.getWidgets().get(0);
        widget.setRectangle(new PDRectangle(x, y, width, height));
        widget.setPage(page);
        widget.setPrinted(true);
        page.getAnnotations().add(widget);
    }

    // For testing
    public static void main(String[] args) throws IOException {
        generateMultiPageTestPdf(java.nio.file.Paths.get("multi-page-form.pdf"), 3, 8);
        System.out.println("Multi-page test PDF generated: multi-page-form.pdf");
    }
}
```

> ✅ This creates a **3-page form** with:
> - Page 1: Primary applicant + first 2–3 dependents
> - Pages 2–3: Remaining dependents
> - Header: `"Application Form • Page X of Y"`
> - Footer: `"Confidential • Form ID: APP-2025"`

---

## 🧪 2. Update Integration Test

Your existing `PdfFormFiller.fillForm()` works **unchanged** — because:

- `PDAcroForm` is **document-scoped**, not page-scoped
- Field names like `Dependent.FName.5` are filled regardless of which page they’re on

### Test Case

```java
@Test
void fillsMultiPageForm() throws Exception {
    Path multiPagePdf = tempDir.resolve("multi-page.pdf");
    TestPdfGenerator.generateMultiPageTestPdf(multiPagePdf, 3, 8);

    // Use data with 5 dependents
    Path data = tempDir.resolve("data.json");
    Files.writeString(data, """
        {
          "primaryApplicant": { "firstName": "john" },
          "dependents": [
            {"firstName": "Alice", "includeInForm": true},
            {"firstName": "Bob", "includeInForm": true},
            {"firstName": "Carol", "includeInForm": true},
            {"firstName": "David", "includeInForm": true},
            {"firstName": "Eve", "includeInForm": true}
          ]
        }
        """);

    Path mapping = tempDir.resolve("mapping.yaml");
    Files.writeString(mapping, """
        fields:
          - pdf_field: "PrimaryApplicant.FName.1"
            json_path: "$.primaryApplicant.firstName"
            transform: "uppercase"
          - repeat: true
            json_array_path: "$.dependents"
            condition: "$.includeInForm == true"
            items:
              - pdf_field: "Dependent.FName.{index}"
                json_path: "$.firstName"
        """);

    Path output = tempDir.resolve("filled-multi.pdf");
    PdfFormFiller.fillForm(multiPagePdf.toString(), data.toString(), mapping.toString(), output.toString());

    // Validate fields across pages
    try (var doc = PDDocument.load(output.toFile())) {
        var form = doc.getDocumentCatalog().getAcroForm();
        assertThat(form.getField("PrimaryApplicant.FName.1").getValueAsString()).isEqualTo("JOHN");
        assertThat(form.getField("Dependent.FName.1").getValueAsString()).isEqualTo("Alice");
        assertThat(form.getField("Dependent.FName.5").getValueAsString()).isEqualTo("Eve");
        // Fields 6-8 remain empty (not in JSON)
    }
}
```

---

## 🔍 How Headers/Footers Work

| Aspect | Implementation |
|-------|----------------|
| **Static content** | Drawn directly on each page’s content stream |
| **Dynamic page numbers** | Inserted at generation time (`Page 1 of 3`) |
| **Non-interactive** | Headers/footers are **not form fields** — they’re part of the page background |
| **Consistency** | Same code runs for every page |

> 💡 **Important**: Headers/footers are **not editable** — they’re baked into the page content. Only the **form fields** are interactive.

---

## 🛠️ Advanced: Dynamic Headers (Per Page)

If you need **different headers per page** (e.g., "Section 1: Personal Info"), pass a `Map<Integer, String>` to `generateMultiPageTestPdf()` and use it in `addHeader()`.

---

## ✅ Best Practices

1. **Field Naming**: Keep your naming convention (`Entity.Field.Index`) — it works across pages.
2. **Page Layout**: Calculate `y` positions carefully to avoid overlap with header/footer.
3. **Testing**: Verify fields on **all pages** (e.g., `Dependent.FName.5` might be on page 2).
4. **Performance**: For large forms (>10 pages), consider streaming generation.

---

## 📌 Summary

You now have a **complete multi-page PDF form generator** that:
- ✅ Creates professional multi-page forms
- ✅ Adds consistent headers/footers
- ✅ Distributes form fields across pages
- ✅ Integrates seamlessly with your existing YAML/JSON/PDFBox pipeline

This approach is used in real-world systems like **tax preparation software**, **insurance applications**, and **government forms**.

Let me know if you'd like to add:
- **Page-specific watermarks**
- **Conditional page generation** (e.g., only add page 2 if dependents exist)
- **Bookmarks/outlines** for navigation