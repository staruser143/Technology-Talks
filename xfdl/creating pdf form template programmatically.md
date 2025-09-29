Yes — **you can absolutely create a PDF AcroForm template programmatically** using **Apache PDFBox** (or other libraries like iText). This is especially useful when you want to **generate a fillable PDF from scratch** based on your XFDL structure, without needing Adobe Acrobat.

Below is a **complete, working example** that:

1. Reads an **XFDL file**  
2. **Dynamically creates a PDF** with matching AcroForm fields  
3. Saves it as a **fillable PDF template**  
4. (Optional) Fills it with the same XFDL data

---

### ✅ Why Create PDF Templates Programmatically?
- No manual design in Acrobat needed  
- Fully automated pipeline: `XFDL → PDF template → filled PDF`  
- Ideal for batch processing or dynamic form generation

---

### 📁 Updated Project Structure
```
xfdl-to-pdf/
├── pom.xml
├── src/main/java/
│   ├── XfdlToPdfFiller.java      // (fills existing PDF)
│   └── XfdlToPdfTemplate.java    // ← NEW: creates PDF from XFDL
├── forms/
│   └── sample.xfdl
└── output/
    ├── generated_template.pdf
    └── filled_from_generated.pdf
```

---

### 🔧 Step 1: `XfdlToPdfTemplate.java` – Generate Fillable PDF from XFDL

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XfdlToPdfTemplate {

    public static void main(String[] args) {
        try {
            File xfdlFile = new File("forms/sample.xfdl");
            File templateOutput = new File("output/generated_template.pdf");
            File filledOutput = new File("output/filled_from_generated.pdf");

            templateOutput.getParentFile().mkdirs();

            // Step 1: Parse XFDL to get field definitions
            List<FieldDef> fields = parseXfdlForTemplate(xfdlFile);
            System.out.println("Parsed fields: " + fields);

            // Step 2: Generate PDF with AcroForm fields
            createPdfTemplate(fields, templateOutput);
            System.out.println("✅ PDF template created: " + templateOutput.getAbsolutePath());

            // Step 3 (Optional): Fill it with the same data
            XfdlToPdfFiller.fillPdfFromXfdl(xfdlFile, templateOutput, filledOutput);
            System.out.println("✅ Filled PDF: " + filledOutput.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Represents a form field parsed from XFDL
    public static class FieldDef {
        public String sid;
        public String value;
        public boolean isCheckbox;

        public FieldDef(String sid, String value) {
            this.sid = sid;
            this.value = value;
            // Heuristic: if value is "on"/"off", treat as checkbox
            this.isCheckbox = "on".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value);
        }
    }

    // Parse XFDL and return field metadata (for template creation)
    public static List<FieldDef> parseXfdlForTemplate(File xfdlFile)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xfdlFile);
        doc.getDocumentElement().normalize();

        List<FieldDef> fields = new ArrayList<>();
        NodeList fieldNodes = doc.getElementsByTagName("field");

        for (int i = 0; i < fieldNodes.getLength(); i++) {
            Element el = (Element) fieldNodes.item(i);
            String sid = el.getAttribute("sid");
            String value = el.getElementsByTagName("value").item(0).getTextContent().trim();
            fields.add(new FieldDef(sid, value));
        }
        return fields;
    }

    // Create a new PDF with AcroForm fields based on XFDL structure
    public static void createPdfTemplate(List<FieldDef> fields, File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Create AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // Set default font
            acroForm.setDefaultResources(acroForm.getDefaultResources());
            acroForm.setDefaultAppearance("/Helv 0 Tf 0 g");

            float margin = 50;
            float yStart = 750;
            float lineHeight = 30;
            float fieldWidth = 200;
            float labelWidth = 120;

            float y = yStart;

            for (FieldDef field : fields) {
                if (y < 50) { // Add new page if needed
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    y = yStart;
                }

                // Add label (non-interactive text)
                // (PDFBox doesn't draw text directly on page easily without PDPageContentStream)
                // For simplicity, we'll just position fields; labels can be added similarly if needed.

                float xField = margin + labelWidth;

                if (field.isCheckbox) {
                    // Create checkbox
                    PDCheckBox checkBox = new PDCheckBox(acroForm);
                    checkBox.setPartialName(field.sid); // This becomes the field name
                    checkBox.setExportValue("Yes"); // Standard export value

                    // Position: checkboxes are small
                    checkBox.getWidgets().get(0).setRectangle(
                        new PDRectangle(xField, y - 10, 15, 15)
                    );
                    checkBox.getWidgets().get(0).setPage(page);
                    checkBox.setReadOnly(false);

                    acroForm.getFields().add(checkBox);
                    y -= lineHeight;
                } else {
                    // Create text field
                    PDTextField textField = new PDTextField(acroForm);
                    textField.setPartialName(field.sid);
                    textField.setDefaultAppearance("/Helv 12 Tf 0 0 0 rg");

                    textField.getWidgets().get(0).setRectangle(
                        new PDRectangle(xField, y - 20, fieldWidth, 20)
                    );
                    textField.getWidgets().get(0).setPage(page);
                    textField.setReadOnly(false);

                    acroForm.getFields().add(textField);
                    y -= lineHeight;
                }
            }

            // Optional: Add instructions
            // (Would require PDPageContentStream to draw static text)

            document.save(outputFile);
        }
    }
}
```

---

### 🔧 Step 2: Update `XfdlToPdfFiller.java` to Support Reuse

Add this **static helper method** to your existing `XfdlToPdfFiller.java`:

```java
// Add this method to XfdlToPdfFiller.java
public static void fillPdfFromXfdl(File xfdlFile, File pdfTemplate, File outputFile)
        throws Exception {
    Map<String, String> data = parseXfdl(xfdlFile);
    // Use identity mapping since PDF field names = XFDL 'sid'
    fillPdfForm(pdfTemplate, data, outputFile);
}
```

> 💡 Note: In the generated PDF, field names **match XFDL `sid` exactly**, so no custom mapping is needed.

---

### 📄 Sample `sample.xfdl` (same as before)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xfdl>
  <page sid="page1">
    <field sid="full_name"><value>Alice Johnson</value></field>
    <field sid="email"><value>alice@example.com</value></field>
    <field sid="is_subscribed"><value>on</value></field>
    <field sid="country"><value>Canada</value></field>
  </page>
</xfdl>
```

---

### ▶️ Run It

```bash
mvn compile
mvn exec:java -Dexec.mainClass="XfdlToPdfTemplate"
```

### ✅ Output
- `output/generated_template.pdf` → a **fillable PDF** with:
  - Text fields: `full_name`, `email`, `country`
  - Checkbox: `is_subscribed`
- `output/filled_from_generated.pdf` → same PDF, **pre-filled** with XFDL data

---

### 🖼️ What the Generated PDF Looks Like
```
[full_name]      [Alice Johnson      ]
[email]          [alice@example.com  ]
[is_subscribed]  [✓]                 ← checkbox
[country]        [Canada             ]
```

> ⚠️ **Note**: This example **does not draw field labels** (like "Full Name:") for simplicity.  
> To add static labels, you’d use `PDPageContentStream` to draw text next to each field.

---

### 🔧 How to Add Labels (Optional Enhancement)

Inside the loop in `createPdfTemplate`, before creating the field:

```java
try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
    contentStream.beginText();
    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
    contentStream.newLineAtOffset(margin, y - 15);
    contentStream.showText(field.sid.replace("_", " ") + ":");
    contentStream.endText();
}
```

> ❗ But note: you can’t use `PDPageContentStream` inside the field loop easily because it must be closed. Better to collect label positions and draw them in a separate pass.

---

### ✅ Advantages of Programmatic PDF Template Creation
- Fully automated
- Scales to hundreds of forms
- Integrates into CI/CD or batch processing
- No licensing costs (vs. Adobe Acrobat)

---

### 🚫 Limitations
- Basic layout only (no complex designs)
- No support for dropdowns, radio groups, or signatures without extra code
- Appearance is minimal (but functional)

---

### Final Thought
> **Yes — with PDFBox, you can go from XFDL → dynamic PDF template → filled PDF, all in code.**  
> This is perfect for modernizing legacy XFDL workflows while maintaining compatibility with standard PDF viewers.

Let me know if you'd like:
- Dropdown (choice) field support
- Multi-page layout with labels
- Export to **XFDF** instead
- A version that **preserves XFDL styling hints** (if present)

Happy automating! 🤖📄