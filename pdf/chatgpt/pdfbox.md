Got it 👍 You want to create a **PDF form template** (with interactive form fields like text boxes, checkboxes, etc.) using **Apache PDFBox**.

PDFBox supports **AcroForms**, which let you define fields in a PDF that can be filled programmatically later.

Here’s a step-by-step guide with Java code:

---

## 1. Add PDFBox Dependency

If you’re using Maven, include:

```xml
<dependency>
  <groupId>org.apache.pdfbox</groupId>
  <artifactId>pdfbox</artifactId>
  <version>2.0.30</version>
</dependency>
```

---

## 2. Create a PDF with a Form Template

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;

public class PdfFormTemplate {
    public static void main(String[] args) {
        try (PDDocument document = new PDDocument()) {
            // Create a page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Create AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // Create a Text Field
            PDTextField nameField = new PDTextField(acroForm);
            nameField.setPartialName("name");
            acroForm.getFields().add(nameField);

            // Add widget annotation for positioning the field
            PDRectangle rect = new PDRectangle(100, 700, 200, 20); // x, y, width, height
            nameField.getWidgets().get(0).setRectangle(rect);
            nameField.getWidgets().get(0).setPage(page);
            page.getAnnotations().add(nameField.getWidgets().get(0));

            // Add a CheckBox Field
            PDCheckBox agreeBox = new PDCheckBox(acroForm);
            agreeBox.setPartialName("agreement");
            acroForm.getFields().add(agreeBox);

            PDRectangle checkRect = new PDRectangle(100, 650, 20, 20);
            agreeBox.getWidgets().get(0).setRectangle(checkRect);
            agreeBox.getWidgets().get(0).setPage(page);
            page.getAnnotations().add(agreeBox.getWidgets().get(0));

            // Draw labels
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(100, 725);
            contentStream.showText("Name:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(130, 655);
            contentStream.showText("I Agree");
            contentStream.endText();

            contentStream.close();

            // Save to file
            document.save(new File("FormTemplate.pdf"));
            System.out.println("PDF Form Template created!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## 3. Output

The generated **FormTemplate.pdf** will have:

* A **text box** at the top for "Name".
* A **checkbox** with label "I Agree".
* Both fields are fillable using Acrobat Reader or any PDF form editor.

---

## 4. Notes

* Use `PDTextField`, `PDCheckBox`, `PDRadioButton`, `PDComboBox`, `PDListBox`, etc. depending on your needs.
* You must add both the **field** (to `acroForm`) and the **widget annotation** (to the page).
* The `PDRectangle` specifies where the form element appears.

---

👉 Do you want me to also show how to **pre-fill data into this template later** (like filling the name automatically and checking the box), or just stick to template creation?
