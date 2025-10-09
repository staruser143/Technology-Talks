Got it 👍 — so you want to take the **form template PDF** and **fill it with data programmatically** using PDFBox.

This is very common:

* Create the form once (with AcroForm fields).
* Later, open it and fill values like *name, country, gender, agreement*, etc.

---

## Example: Filling Values in a Form PDF

Let’s assume you already have a template `FormTemplateExtended.pdf` (from the last example).
Here’s how to load it and set values:

```java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;

public class FillPdfForm {
    public static void main(String[] args) {
        try (PDDocument document = PDDocument.load(new File("FormTemplateExtended.pdf"))) {

            // Get the form
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            if (acroForm != null) {
                // Text Field
                PDField nameField = acroForm.getField("name");
                if (nameField != null) {
                    nameField.setValue("Sridhar Balasubramanian");
                }

                // Checkbox
                PDField agreeBox = acroForm.getField("agreement");
                if (agreeBox != null) {
                    agreeBox.setValue("Yes"); // "Yes" is usually the ON value
                }

                // Radio Button (gender)
                PDField genderField = acroForm.getField("gender");
                if (genderField != null) {
                    genderField.setValue("Male"); // must match export value
                }

                // Dropdown (country)
                PDField countryField = acroForm.getField("country");
                if (countryField != null) {
                    countryField.setValue("Germany"); // must match one of the options
                }
            }

            // Save a new filled form
            document.save("FilledForm.pdf");
            System.out.println("Form filled successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## Notes

1. **Export Values**:

   * For checkboxes/radio buttons, the `setValue()` must match the **export value** you set when creating the field (`Yes`, `Male`, `Female`, etc.).
   * If you don’t set them explicitly, PDFBox defaults to `"Yes"` for checkboxes.

2. **Text Fields**: any string works.

3. **Dropdowns (ComboBox)**:

   * Must use one of the available options (`India`, `USA`, `UK`, `Germany`).

4. **Locking the Form**:

   * If you want to make the form **non-editable after filling**, you can flatten it:

     ```java
     acroForm.flatten();
     ```

     This will turn fields into plain text in the PDF.

---

👉 Do you want me to show you a **combined example** where we create the template **and** immediately fill it with sample data, so you see both steps together?
