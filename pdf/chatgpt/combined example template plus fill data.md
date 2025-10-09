Perfect 👍 — let’s combine both steps:

1. **Create the PDF form template** (with text field, checkbox, radio buttons, dropdown).
2. **Immediately fill it with sample data** (like name, agreement checked, gender selected, country selected).

---

## Full Combined Example

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.File;

public class PdfFormCreateAndFill {
    public static void main(String[] args) {
        try (PDDocument document = new PDDocument()) {
            // Create a page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Create AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // ----------------- Text Field -----------------
            PDTextField nameField = new PDTextField(acroForm);
            nameField.setPartialName("name");
            acroForm.getFields().add(nameField);

            PDAnnotationWidget nameWidget = nameField.getWidgets().get(0);
            nameWidget.setRectangle(new PDRectangle(100, 700, 200, 20));
            nameWidget.setPage(page);
            page.getAnnotations().add(nameWidget);

            // ----------------- CheckBox -----------------
            PDCheckBox agreeBox = new PDCheckBox(acroForm);
            agreeBox.setPartialName("agreement");
            acroForm.getFields().add(agreeBox);

            PDAnnotationWidget checkWidget = agreeBox.getWidgets().get(0);
            checkWidget.setRectangle(new PDRectangle(100, 650, 20, 20));
            checkWidget.setPage(page);
            page.getAnnotations().add(checkWidget);

            // ----------------- Radio Button Group -----------------
            PDRadioButton radioGroup = new PDRadioButton(acroForm);
            radioGroup.setPartialName("gender");
            acroForm.getFields().add(radioGroup);

            // Option 1: Male
            PDAnnotationWidget radioWidget1 = new PDAnnotationWidget();
            radioWidget1.setRectangle(new PDRectangle(100, 600, 20, 20));
            radioWidget1.setPage(page);
            page.getAnnotations().add(radioWidget1);

            // Option 2: Female
            PDAnnotationWidget radioWidget2 = new PDAnnotationWidget();
            radioWidget2.setRectangle(new PDRectangle(160, 600, 20, 20));
            radioWidget2.setPage(page);
            page.getAnnotations().add(radioWidget2);

            radioGroup.getWidgets().add(radioWidget1);
            radioGroup.getWidgets().add(radioWidget2);
            radioGroup.setExportValues(new String[]{"Male", "Female"});

            // ----------------- Drop-down List -----------------
            PDComboBox comboBox = new PDComboBox(acroForm);
            comboBox.setPartialName("country");
            acroForm.getFields().add(comboBox);

            PDAnnotationWidget comboWidget = comboBox.getWidgets().get(0);
            comboWidget.setRectangle(new PDRectangle(100, 550, 150, 20));
            comboWidget.setPage(page);
            page.getAnnotations().add(comboWidget);

            comboBox.setOptions(new String[]{"India", "USA", "UK", "Germany"});

            // ----------------- Labels -----------------
            PDPageContentStream cs = new PDPageContentStream(document, page);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(100, 725);
            cs.showText("Name:");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(130, 655);
            cs.showText("I Agree");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(100, 620);
            cs.showText("Gender: Male   Female");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(100, 570);
            cs.showText("Country:");
            cs.endText();

            cs.close();

            // ----------------- Fill Data -----------------
            nameField.setValue("Sridhar Balasubramanian");
            agreeBox.check();  // check the box
            radioGroup.setValue("Male"); // must match export value
            comboBox.setValue("Germany");

            // ----------------- Save -----------------
            document.save(new File("FormCreatedAndFilled.pdf"));
            System.out.println("Form template created and filled!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## What this does

* Creates a form with:

  * Text field (`name`)
  * Checkbox (`agreement`)
  * Radio buttons (`gender`)
  * Dropdown (`country`)
* Immediately fills it with:

  * Name = `"Sridhar Balasubramanian"`
  * Agreement = checked
  * Gender = Male
  * Country = Germany

Final output file: **FormCreatedAndFilled.pdf**

---

⚡ Tip: If you want to make sure the user **cannot edit the form after filling**, just call:

```java
acroForm.flatten();
```

before saving.

---

👉 Do you also want me to show how to **fill multiple rows of data (batch filling)** into separate PDFs (like generating 100 personalized filled forms)?
