Great question! **Signature fields** and **date pickers** are common in professional PDF forms (e.g., contracts, applications), but they work differently than regular fields:

---

## 🖊️ 1. **Signature Fields**

### How They Work
- A **signature field** in PDF is a special annotation that can hold:
  - A **digital signature** (cryptographic, legally binding)
  - Or a **graphical signature** (image/drawing)
- In **fillable forms**, it’s usually a **placeholder** for the user to sign later (via Adobe Acrobat, DocuSign, etc.)
- **Programmatically**, you **cannot** add a *valid digital signature* without a certificate — but you **can**:
  - Add a **signature field placeholder**
  - Or **stamp a static image/text** (not a true signature)

### ✅ What You Can Do in Tests
For **testing purposes**, we’ll add a **signature field placeholder** — it appears as a clickable box in PDF viewers.

#### 🔧 Add to `TestPdfGenerator.java`

```java
// Inside generateTestPdf()
addLabel(page, xLabel, y, "Signature:");
y -= lineHeight;
addSignatureField(acroForm, page, xField, y - 20, 200, 40, "Applicant.Signature");
y -= 2 * lineHeight;
```

#### Helper Method

```java
private static void addSignatureField(
    PDAcroForm form, PDPage page,
    float x, float y, float width, float height, String fieldName
) throws IOException {
    PDSignatureField sigField = new PDSignatureField(form);
    sigField.setPartialName(fieldName);
    
    PDAnnotationWidget widget = sigField.getWidgets().get(0);
    widget.setRectangle(new PDRectangle(x, y, width, height));
    widget.setPage(page);
    widget.setPrinted(true);
    
    // Optional: Set appearance stream (shows "Sign Here" text)
    try (var content = new PDPageContentStream(page.getDocument(), page, true, true)) {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
        content.newLineAtOffset(x + 5, y + 15);
        content.showText("Sign Here");
        content.endText();
    }
    
    form.getFields().add(sigField);
    page.getAnnotations().add(widget);
}
```

> ⚠️ **Important**:  
> - This creates a **signature field placeholder** — not a signed document.  
> - To **actually sign**, you’d need a `.pfx` certificate and use `PDSignature` with `ExternalSigningSupport` (complex, not for testing).

---

## 📅 2. **Date Pickers**

### How They Work
- PDF **does not have a native "date picker" widget** like HTML.
- Instead, forms use:
  - A **text field** formatted to accept dates (e.g., `MM/dd/yyyy`)
  - **JavaScript** (in Adobe Acrobat) to attach a calendar popup
- **Programmatically**, you just fill the **text field** with a formatted date string.

### ✅ What You Can Do
- Create a **text field** with a descriptive name (e.g., `Application.Date`)
- Use your existing **`date` transform** to format the value

#### 🔧 Add to `TestPdfGenerator.java`

```java
// Inside generateTestPdf()
addLabelAndTextField(document, acroForm, page, xLabel, y, "Application Date:", "Application.Date", xField);
y -= lineHeight;
```

> 💡 No special field type needed — it’s just a **text field** that *conventionally* holds a date.

---

## 🧪 3. Update Test Data & Mapping

### `test-data.json`
```json
{
  "primaryApplicant": {
    "firstName": "john",
    "applicationDate": "2025-10-25"
  }
}
```

### `test-mapping.yaml`
```yaml
fields:
  - pdf_field: "Application.Date"
    json_path: "$.primaryApplicant.applicationDate"
    transform: "date:MM/dd/yyyy"

  - pdf_field: "Applicant.Signature"
    json_path: "$.primaryApplicant.name"
    transform: "uppercase"
    # In real use, you wouldn't fill signature from JSON — but for test, we stamp text
```

> 🔒 **Security Note**:  
> In production, **never auto-fill signature fields from data** — signatures should be provided by the user at time of signing.

---

## 🧪 4. Update Integration Test

```java
@Test
void fillsDateAndSignatureFields() throws Exception {
    PdfFormFiller.fillForm(
        testPdf.toString(),
        testData.toString(),
        testMapping.toString(),
        outputPdf.toString()
    );

    try (var doc = PDDocument.load(outputPdf.toFile())) {
        var form = doc.getDocumentCatalog().getAcroForm();

        // Date field (text field with formatted date)
        assertThat(form.getField("Application.Date").getValueAsString())
            .isEqualTo("10/25/2025");

        // Signature field: PDFBox returns empty string for signature fields!
        // But we can check that the field exists and has our stamped text
        PDField sigField = form.getField("Applicant.Signature");
        assertThat(sigField).isNotNull();

        // Note: getValueAsString() on PDSignatureField returns ""
        // To verify appearance, you'd need to inspect the appearance stream (advanced)
    }
}
```

> ⚠️ **Signature Field Quirk**:  
> `PDSignatureField.getValueAsString()` **always returns empty string** — even if you set a value.  
> The visual appearance is stored in the **widget annotation’s appearance stream**, not the field value.

---

## 🛠️ Optional: Stamp Text on Signature Field (for Testing)

If you want to **visually see** a "signature" in tests (e.g., `"JOHN DOE"`), you can **override the appearance**:

```java
private static void stampSignatureText(PDDocument doc, PDPage page, 
        float x, float y, float width, float height, String text) throws IOException {
    // Create appearance stream
    PDAnnotationWidget widget = new PDAnnotationWidget();
    PDAppearanceDictionary appearance = new PDAppearanceDictionary();
    PDAppearanceStream normalStream = new PDAppearanceStream(doc);
    normalStream.setResources(new PDResources());
    normalStream.setBBox(new PDRectangle(width, height));

    try (var content = new PDPageContentStream(doc, normalStream)) {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(5, height - 15);
        content.showText(text);
        content.endText();
    }

    appearance.setNormalAppearance(normalStream);
    widget.setAppearance(appearance);
    widget.setRectangle(new PDRectangle(x, y, width, height));
    widget.setPage(page);
    page.getAnnotations().add(widget);
}
```

But note: this **replaces** the signature field with a **static annotation** — it’s no longer a fillable signature field.

---

## ✅ Summary

| Feature | How It Works | How to Handle in Code |
|--------|--------------|------------------------|
| **Signature Field** | Placeholder for digital/graphical signature | Use `PDSignatureField`; don’t expect `getValueAsString()` to work |
| **Date "Picker"** | Just a text field + JavaScript (in Acrobat) | Treat as regular text field; use `date` transform |
| **Testing** | Verify field exists; stamp text if needed for visual checks | Use appearance streams for visual tests (optional) |

---

## 📌 Best Practices

1. **Signature Fields**:
   - Only create the **placeholder** in your template
   - **Do not auto-fill** from JSON in production
   - For testing, verify the field **exists**, not its value

2. **Date Fields**:
   - Use **text fields** with clear labels (`Date of Birth`)
   - Apply your `date:MM/dd/yyyy` transform
   - Validate input date format in your app

3. **PDF Generation**:
   - Keep test PDFs simple
   - Use real PDF editors (Adobe, LibreOffice) for complex forms

---

You now understand how to handle **signature fields** and **date pickers** in PDF forms — both in **templates** and **automated tests**!

Let me know if you'd like to:
- Add **JavaScript actions** to date fields (advanced)
- Implement **true digital signing** (with certificates)
- Generate **multi-page forms** with headers/footers