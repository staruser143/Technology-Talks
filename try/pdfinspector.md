Here’s a simple Java utility that uses **Apache PDFBox** to extract and list all AcroForm field names from a PDF:

---

### 🧑‍💻 Java Class: `PdfFieldInspector.java`

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PdfFieldInspector {

    public static void main(String[] args) {
        try {
            PDDocument document = PDDocument.load(new File("template.pdf"));
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                System.out.println("No AcroForm found in the PDF.");
                return;
            }

            FileWriter writer = new FileWriter("extracted_fields.txt");

            for (PDField field : acroForm.getFields()) {
                String fieldName = field.getFullyQualifiedName();
                System.out.println("Field: " + fieldName);
                writer.write(fieldName + "\n");
            }

            writer.close();
            document.close();
            System.out.println("Field names saved to extracted_fields.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

---

### ✅ What It Does
- Loads `template.pdf`
- Extracts all AcroForm field names
- Prints them to console
- Saves them to `extracted_fields.txt`

---

### 📦 Requirements
Make sure you have PDFBox in your `pom.xml`:
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.29</version>
</dependency>
```

---

Would you like help compiling and running this, or packaging it into a Maven project with a sample PDF? You can also upload your PDF and I’ll inspect the fields for you.
