Absolutely! Below is a **complete, self-contained working example** in Java that:

1. Parses an **XFDL file** (XML) to extract field data  
2. Maps XFDL field IDs (`sid`) to **PDF AcroForm field names**  
3. Uses **Apache PDFBox** to fill a PDF form template  
4. Saves the filled PDF

---

### 📁 Project Structure
```
xfdl-to-pdf/
├── pom.xml
├── src/main/java/
│   └── XfdlToPdfFiller.java
├── forms/
│   ├── sample.xfdl
│   └── form_template.pdf
└── output/
    └── filled_form.pdf
```

---

### 1. `pom.xml` (Maven Dependencies)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>xfdl-to-pdf</artifactId>
    <version>1.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- PDFBox for PDF form filling -->
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>3.0.2</version>
        </dependency>
    </dependencies>
</project>
```

---

### 2. `XfdlToPdfFiller.java`

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XfdlToPdfFiller {

    // 🔁 Field name mapping: XFDL 'sid' → PDF field name
    private static final Map<String, String> FIELD_MAPPING = Map.of(
        "full_name", "fullName",
        "email", "emailAddress",
        "is_subscribed", "subscribeCheckbox",
        "country", "countryList"
    );

    public static void main(String[] args) {
        try {
            File xfdlFile = new File("forms/sample.xfdl");
            File pdfTemplate = new File("forms/form_template.pdf");
            File outputFile = new File("output/filled_form.pdf");

            // Ensure output dir exists
            outputFile.getParentFile().mkdirs();

            // Step 1: Parse XFDL
            Map<String, String> xfdlData = parseXfdl(xfdlFile);
            System.out.println("Parsed XFDL data: " + xfdlData);

            // Step 2: Fill PDF
            fillPdfForm(pdfTemplate, xfdlData, outputFile);
            System.out.println("✅ Filled PDF saved to: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 📄 Parse XFDL (simple version – assumes no namespaces)
    public static Map<String, String> parseXfdl(File xfdlFile)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Disable external entities for security
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xfdlFile);
        doc.getDocumentElement().normalize();

        Map<String, String> data = new HashMap<>();
        NodeList fields = doc.getElementsByTagName("field");

        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) fields.item(i);
            String sid = field.getAttribute("sid");
            NodeList values = field.getElementsByTagName("value");
            if (values.getLength() > 0) {
                String value = values.item(0).getTextContent().trim();
                data.put(sid, value);
            }
        }
        return data;
    }

    // 🖨️ Fill PDF AcroForm using PDFBox
    public static void fillPdfForm(File pdfTemplate, Map<String, String> xfdlData, File outputFile)
            throws IOException {
        try (PDDocument document = PDDocument.load(pdfTemplate)) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                throw new IllegalArgumentException("PDF does not contain an AcroForm!");
            }

            // 🔧 Critical: Ensures filled fields render correctly in all viewers
            acroForm.setNeedAppearances(true);

            // Fill each field
            for (Map.Entry<String, String> entry : xfdlData.entrySet()) {
                String xfdlSid = entry.getKey();
                String rawValue = entry.getValue();

                // Map XFDL sid to PDF field name
                String pdfFieldName = FIELD_MAPPING.getOrDefault(xfdlSid, xfdlSid);
                PDField field = acroForm.getField(pdfFieldName);

                if (field == null) {
                    System.out.println("⚠️ Warning: PDF field not found: " + pdfFieldName);
                    continue;
                }

                // Handle checkboxes (XFDL often uses "on"/"off")
                if (field instanceof org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox) {
                    boolean checked = "on".equalsIgnoreCase(rawValue) ||
                                     "true".equalsIgnoreCase(rawValue) ||
                                     "1".equals(rawValue);
                    // PDF checkboxes typically expect "Yes"/"Off" or custom export values
                    field.setValue(checked ? "Yes" : "Off");
                } else {
                    // Regular text, combo, etc.
                    field.setValue(rawValue);
                }
            }

            // Optional: flatten form (makes fields non-editable)
            // acroForm.flatten();

            document.save(outputFile);
        }
    }
}
```

---

### 3. Sample `sample.xfdl`

Save this as `forms/sample.xfdl`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xfdl>
  <global>
    <title>Sample Registration Form</title>
  </global>
  <page sid="page1">
    <field sid="full_name">
      <value>Alice Johnson</value>
    </field>
    <field sid="email">
      <value>alice@example.com</value>
    </field>
    <field sid="is_subscribed">
      <value>on</value>
    </field>
    <field sid="country">
      <value>Canada</value>
    </field>
  </page>
</xfdl>
```

---

### 4. Create `form_template.pdf`

You need a **fillable PDF** with the following **AcroForm field names**:
- `fullName` (text field)
- `emailAddress` (text field)
- `subscribeCheckbox` (checkbox with export value = `Yes`)
- `countryList` (text or combo box)

#### 🔧 How to create it:
1. Open **Adobe Acrobat Pro** (or free alternative like **PDFescape** or **LibreOffice Draw**)
2. Create a simple form with labeled fields
3. **Name the fields exactly** as in `FIELD_MAPPING`:
   - Text field → Properties → Name = `fullName`
   - Checkbox → Properties → Options → Export Value = `Yes`
4. Save as `forms/form_template.pdf`

> 💡 Don’t have Acrobat?  
> Download a [sample fillable PDF](https://www.pdfscripting.com/public/FreeStuff/PDF_Form_Examples.zip) or use this minimal one:  
> [https://www.uscis.gov/sites/default/files/document/forms/g-325a.pdf](https://www.uscis.gov/sites/default/files/document/forms/g-325a.pdf) (inspect field names with PDFBox or `pdftk`)

---

### 5. Run the Application

```bash
# Build
mvn compile

# Run
mvn exec:java -Dexec.mainClass="XfdlToPdfFiller"
```

✅ Output:
```
Parsed XFDL data: {full_name=Alice Johnson, email=alice@example.com, is_subscribed=on, country=Canada}
✅ Filled PDF saved to: /path/to/xfdl-to-pdf/output/filled_form.pdf
```

Open `filled_form.pdf` — your data should appear in the correct fields!

---

### 🔍 Troubleshooting Tips

| Issue | Fix |
|------|-----|
| Fields appear empty in browser | Call `acroForm.setNeedAppearances(true)` (already in code) |
| Checkbox not checked | Verify the PDF’s **export value** (usually `"Yes"` or `"On"`) |
| `NullPointerException` on field | Field name mismatch — print all PDF field names using:  
`acroForm.getFieldMap().keySet().forEach(System.out::println);` |
| XFDL uses namespaces | Use `getElementsByTagNameNS()` and enable namespace awareness |

---

### ✅ What This Example Does **NOT** Handle (But Can Be Extended)
- XFDL with XML namespaces (e.g., `xmlns="http://www.ibm.com/..."`)
- Encrypted XFDL fields
- Repeating tables or dynamic sections
- Digital signatures

But for **basic data transfer from XFDL to PDF**, this is production-ready.

---

Let me know if you'd like:
- A version that **lists all PDF field names automatically**
- Support for **XFDL namespaces**
- A **Python equivalent** using `pypdf`
- Instructions to **create the PDF template programmatically**

Happy coding! 🚀