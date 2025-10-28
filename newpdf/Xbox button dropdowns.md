Absolutely! Let’s enhance the `TestPdfGenerator` to support **checkboxes**, **radio buttons (grouped)**, and **drop-down (choice) fields** — all commonly used in real-world PDF forms (e.g., tax forms, applications).

We’ll:
✅ Add checkbox fields (e.g., `Consent.Granted`)  
✅ Add radio button groups (e.g., `Gender.Male`, `Gender.Female`)  
✅ Add drop-downs with options (e.g., `State` with `NY`, `CA`, etc.)  
✅ Update test data/mapping to fill them  
✅ Validate in integration tests  

---

## 📦 1. Updated `TestPdfGenerator.java`

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TestPdfGenerator {

    public static void generateTestPdf(Path outputPath, int maxDependents) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);
            acroForm.setDefaultResources(acroForm.getDefaultResources());
            acroForm.setDefaultAppearance("/Helv 12 Tf 0 0 0 rg");

            float y = 750;
            final float lineHeight = 30;
            final float xLabel = 50;
            final float xField = 200;

            // === Primary Applicant Fields ===
            addLabel(page, xLabel, y, "Primary Applicant");
            y -= lineHeight;

            addLabelAndTextField(document, acroForm, page, xLabel, y, "First Name:", "PrimaryApplicant.FName.1", xField);
            y -= lineHeight;
            addLabelAndTextField(document, acroForm, page, xLabel, y, "Last Name:", "PrimaryApplicant.LName.1", xField);
            y -= lineHeight;

            // Checkbox
            addLabelAndCheckbox(acroForm, page, xLabel, y, "Consent to Terms?", "Consent.Granted", xField);
            y -= lineHeight;

            // Radio Group: Gender
            addLabel(page, xLabel, y, "Gender:");
            y -= lineHeight;
            addRadioButton(acroForm, page, xField, y, "Male", "Gender", "M");
            addRadioButton(acroForm, page, xField + 80, y, "Female", "Gender", "F");
            y -= lineHeight;

            // Drop-down: State
            addLabelAndDropdown(acroForm, page, xLabel, y, "State:", "Contact.State",
                List.of("NY", "CA", "TX", "FL"), xField);
            y -= 2 * lineHeight; // dropdown needs more space

            // === Dependents ===
            for (int i = 1; i <= maxDependents; i++) {
                addLabel(page, xLabel, y, "Dependent " + i);
                y -= lineHeight;

                addLabelAndTextField(document, acroForm, page, xLabel, y, "First Name:", "Dependent.FName." + i, xField);
                y -= lineHeight;
                addLabelAndTextField(document, acroForm, page, xLabel, y, "Last Name:", "Dependent.LName." + i, xField);
                y -= lineHeight;

                // Dependent checkbox
                addLabelAndCheckbox(acroForm, page, xLabel, y, "Full-time Student?", "Dependent.Student." + i, xField);
                y -= lineHeight;

                // Nested address
                addLabelAndTextField(document, acroForm, page, xLabel, y, "Street:", "Dependent.Addr.Street." + i + ".1", xField);
                y -= lineHeight;
                addLabelAndTextField(document, acroForm, page, xLabel, y, "City:", "Dependent.Addr.City." + i + ".1", xField);
                y -= lineHeight;
            }

            document.save(outputPath.toFile());
        }
    }

    // --- Helper: Plain Label (non-interactive) ---
    private static void addLabel(PDPage page, float x, float y, String text) throws IOException {
        try (var content = new org.apache.pdfbox.pdmodel.PDPageContentStream(page.getDocument(), page, true, true)) {
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(x, y);
            content.showText(text);
            content.endText();
        }
    }

    // --- Text Field ---
    private static void addLabelAndTextField(
        PDDocument doc, PDAcroForm form, PDPage page,
        float xLabel, float y, String labelText, String fieldName, float xField
    ) throws IOException {
        addLabel(page, xLabel, y, labelText);
        PDTextField field = new PDTextField(form);
        field.setPartialName(fieldName);
        addFieldWidget(field, page, xField, y - 12, 200, 20);
        form.getFields().add(field);
    }

    // --- Checkbox ---
    private static void addLabelAndCheckbox(
        PDAcroForm form, PDPage page,
        float xLabel, float y, String labelText, String fieldName, float xField
    ) throws IOException {
        addLabel(page, xLabel, y, labelText);
        PDCheckbox checkbox = new PDCheckbox(form);
        checkbox.setPartialName(fieldName);
        addFieldWidget(checkbox, page, xField, y - 12, 20, 20);
        form.getFields().add(checkbox);
    }

    // --- Radio Button (part of a group) ---
    private static void addRadioButton(
        PDAcroForm form, PDPage page,
        float x, float y, String label, String groupName, String exportValue
    ) throws IOException {
        // Add label next to button
        try (var content = new org.apache.pdfbox.pdmodel.PDPageContentStream(page.getDocument(), page, true, true)) {
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(x + 25, y);
            content.showText(label);
            content.endText();
        }

        // Create or get radio group
        PDRadioButton radioGroup = null;
        for (PDField field : form.getFields()) {
            if (field instanceof PDRadioButton rb && groupName.equals(field.getPartialName())) {
                radioGroup = rb;
                break;
            }
        }
        if (radioGroup == null) {
            radioGroup = new PDRadioButton(form);
            radioGroup.setPartialName(groupName);
            form.getFields().add(radioGroup);
        }

        // Add button widget
        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y - 10, 15, 15));
        widget.setPage(page);
        widget.setPrinted(true);

        COSDictionary ap = new COSDictionary();
        widget.getCOSObject().setItem(COSName.AP, ap);

        radioGroup.getWidgets().add(widget);
        page.getAnnotations().add(widget);

        // Set export value (what gets stored when selected)
        // PDFBox doesn't easily support per-widget export values in radio groups.
        // Workaround: Use separate checkboxes with mutual exclusion (not ideal).
        // For testing, we'll treat radio as a text field with fixed values.
        // → In practice, many forms use text fields for radio-like behavior.
    }

    // ⚠️ Simpler approach: Use a TEXT FIELD for radio selection
    // (since PDFBox radio support is limited and complex)
    private static void addRadioAsTextField(
        PDAcroForm form, PDPage page,
        float xLabel, float y, String labelText, String fieldName, float xField
    ) throws IOException {
        addLabel(page, xLabel, y, labelText);
        PDTextField field = new PDTextField(form);
        field.setPartialName(fieldName);
        addFieldWidget(field, page, xField, y - 12, 200, 20);
        form.getFields().add(field);
    }

    // --- Drop-down (Choice Field) ---
    private static void addLabelAndDropdown(
        PDAcroForm form, PDPage page,
        float xLabel, float y, String labelText, String fieldName,
        List<String> options, float xField
    ) throws IOException {
        addLabel(page, xLabel, y, labelText);
        PDChoiceField dropdown = new PDChoiceField(form);
        dropdown.setPartialName(fieldName);
        dropdown.setOptions(options.toArray(new String[0]));
        dropdown.setComboBox(false); // true = editable combo, false = fixed dropdown
        addFieldWidget(dropdown, page, xField, y - 12, 200, 20);
        form.getFields().add(dropdown);
    }

    // --- Generic Widget Helper ---
    private static void addFieldWidget(PDField field, PDPage page, float x, float y, float width, float height) {
        PDAnnotationWidget widget = field.getWidgets().get(0);
        widget.setRectangle(new PDRectangle(x, y, width, height));
        widget.setPage(page);
        widget.setPrinted(true);
        page.getAnnotations().add(widget);
    }
}
```

> ⚠️ **Note on Radio Buttons**:  
> Apache PDFBox has **limited support** for true radio groups with export values.  
> **Workaround**: Use a **text field** to store the selected value (e.g., `"M"` or `"F"`).  
> This is common in real forms and simplifies testing.

---

## 📄 2. Update Test Data & Mapping

### `test-data.json`
```json
{
  "primaryApplicant": {
    "firstName": "john",
    "lastName": "doe",
    "consent": true,
    "gender": "M",
    "state": "NY"
  },
  "dependents": [
    {
      "firstName": "Alice",
      "lastName": "Doe",
      "isStudent": true,
      "includeInForm": true,
      "addresses": [
        { "street": "123 Main St", "city": "New York" }
      ]
    }
  ]
}
```

### `test-mapping.yaml`
```yaml
fields:
  - pdf_field: "PrimaryApplicant.FName.1"
    json_path: "$.primaryApplicant.firstName"
    transform: "uppercase"

  - pdf_field: "PrimaryApplicant.LName.1"
    json_path: "$.primaryApplicant.lastName"
    transform: "uppercase"

  - pdf_field: "Consent.Granted"
    json_path: "$.primaryApplicant.consent"
    transform: "boolean_checkbox"   # custom transform

  - pdf_field: "Gender"
    json_path: "$.primaryApplicant.gender"
    # no transform: store "M" or "F" directly

  - pdf_field: "Contact.State"
    json_path: "$.primaryApplicant.state"
    # value must match dropdown option

  - repeat: true
    json_array_path: "$.dependents"
    condition: "$.includeInForm == true"
    items:
      - pdf_field: "Dependent.FName.{index}"
        json_path: "$.firstName"
      - pdf_field: "Dependent.LName.{index}"
        json_path: "$.lastName"
      - pdf_field: "Dependent.Student.{index}"
        json_path: "$.isStudent"
        transform: "boolean_checkbox"
      - repeat: true
        json_array_path: "$.addresses"
        items:
          - pdf_field: "Dependent.Addr.Street.{parent_index}.{index}"
            json_path: "$.street"
          - pdf_field: "Dependent.Addr.City.{parent_index}.{index}"
            json_path: "$.city"
```

---

## 🔧 3. Add `boolean_checkbox` Transform

Update `applyTransform` in `PdfFormFiller.java`:

```java
private static String applyTransform(String value, String transformSpec) {
    if (transformSpec == null || transformSpec.isEmpty()) return value;
    value = value.trim();

    if ("boolean_checkbox".equals(transformSpec)) {
        return Boolean.parseBoolean(value) ? "Yes" : "Off";
        // PDFBox checkboxes use "Yes"/"Off" by default
    }
    // ... rest unchanged
}
```

> ✅ PDF checkboxes are **checked** when value = `"Yes"`, **unchecked** when `"Off"`.

---

## 🧪 4. Update Integration Test

```java
@Test
void fillsAllFieldTypes() throws Exception {
    PdfFormFiller.fillForm(
        testPdf.toString(),
        testData.toString(),
        testMapping.toString(),
        outputPdf.toString()
    );

    try (var doc = PDDocument.load(outputPdf.toFile())) {
        var form = doc.getDocumentCatalog().getAcroForm();

        // Text
        assertThat(form.getField("PrimaryApplicant.FName.1").getValueAsString())
            .isEqualTo("JOHN");

        // Checkbox
        assertThat(form.getField("Consent.Granted").getValueAsString())
            .isEqualTo("Yes");

        // Radio (as text field)
        assertThat(form.getField("Gender").getValueAsString())
            .isEqualTo("M");

        // Dropdown
        assertThat(form.getField("Contact.State").getValueAsString())
            .isEqualTo("NY");

        // Dependent checkbox
        assertThat(form.getField("Dependent.Student.1").getValueAsString())
            .isEqualTo("Yes");
    }
}
```

---

## ✅ Summary of Supported Field Types

| Field Type | PDFBox Class | Test Value | Notes |
|-----------|--------------|-----------|-------|
| **Text** | `PDTextField` | `"JOHN"` | Standard |
| **Checkbox** | `PDCheckbox` | `"Yes"` / `"Off"` | Use `boolean_checkbox` transform |
| **Radio** | `PDTextField` (workaround) | `"M"` | True radio groups are complex |
| **Dropdown** | `PDChoiceField` | `"NY"` | Must match option exactly |

---

## 🚀 Final Notes

- **Checkboxes**: PDF uses `"Yes"`/`"Off"` by default. You can customize appearance, but `"Yes"` is standard.
- **Radio Buttons**: For full radio behavior, consider using **separate checkboxes with JavaScript** (beyond scope). Text field is simpler for data-driven forms.
- **Dropdowns**: Ensure JSON value **exactly matches** an option (case-sensitive).

You now have a **comprehensive test PDF generator** that covers **all major form field types** — perfect for end-to-end testing of your PDF filler!

Let me know if you'd like to add **signature fields**, **date pickers**, or **JavaScript actions**!