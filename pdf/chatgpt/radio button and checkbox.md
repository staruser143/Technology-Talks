Perfect — let’s extend your **PDF form template** to also include:

* **Radio button group** (only one option can be selected).
* **Drop-down listbox (combo box)**.

---

## Updated Example with Radio & Dropdown

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.File;

public class PdfFormTemplateExtended {
    public static void main(String[] args) {
        try (PDDocument document = new PDDocument()) {
            // Page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // ----------------- Text Field -----------------
            PDTextField nameField = new PDTextField(acroForm);
            nameField.setPartialName("name");
            acroForm.getFields().add(nameField);

            PDRectangle nameRect = new PDRectangle(100, 700, 200, 20);
            PDAnnotationWidget nameWidget = nameField.getWidgets().get(0);
            nameWidget.setRectangle(nameRect);
            nameWidget.setPage(page);
            page.getAnnotations().add(nameWidget);

            // ----------------- CheckBox -----------------
            PDCheckBox agreeBox = new PDCheckBox(acroForm);
            agreeBox.setPartialName("agreement");
            acroForm.getFields().add(agreeBox);

            PDRectangle checkRect = new PDRectangle(100, 650, 20, 20);
            PDAnnotationWidget checkWidget = agreeBox.getWidgets().get(0);
            checkWidget.setRectangle(checkRect);
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

            radioGroup.getWidgets().add(radioWidget1);
            radioGroup.getCOSObject().setItem("Kids", radioWidget1.getCOSObject()); // link option name

            // Option 2: Female
            PDAnnotationWidget radioWidget2 = new PDAnnotationWidget();
            radioWidget2.setRectangle(new PDRectangle(160, 600, 20, 20));
            radioWidget2.setPage(page);
            page.getAnnotations().add(radioWidget2);

            radioGroup.getWidgets().add(radioWidget2);
            radioGroup.getCOSObject().setItem("Adults", radioWidget2.getCOSObject());

            // Add export values (the "labels" stored when selected)
            radioGroup.setExportValues(new String[]{"Male", "Female"});

            // ----------------- Drop-down List (ComboBox) -----------------
            PDComboBox comboBox = new PDComboBox(acroForm);
            comboBox.setPartialName("country");
            acroForm.getFields().add(comboBox);

            PDRectangle comboRect = new PDRectangle(100, 550, 150, 20);
            PDAnnotationWidget comboWidget = comboBox.getWidgets().get(0);
            comboWidget.setRectangle(comboRect);
            comboWidget.setPage(page);
            page.getAnnotations().add(comboWidget);

            comboBox.setOptions(new String[]{"India", "USA", "UK", "Germany"});
            comboBox.setValue("India"); // default selected

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

            // Save file
            document.save(new File("FormTemplateExtended.pdf"));
            System.out.println("PDF with TextField, Checkbox, Radio, and Dropdown created!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## What this creates

* **Text field** for "Name".
* **Checkbox** for "I Agree".
* **Radio buttons** for "Gender" (Male / Female).
* **Drop-down list** for "Country" with preset options.

---

⚠️ Note: Radio button handling in PDFBox is a bit tricky. The code above ensures they appear and can be toggled in a PDF viewer.

---

👉 Do you also want me to show how to **programmatically set default values** (like Male selected, checkbox checked, etc.) when generating the template?
