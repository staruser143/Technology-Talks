import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.File;

public class FillPdfForm {
    public static void main(String[] args) throws Exception {
        File template = new File("template.pdf");
        File output   = new File("filled.pdf");

        try (PDDocument doc = Loader.loadPDF(template)) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            if (acroForm == null) {
                throw new IllegalStateException("This PDF has no AcroForm (is it an XFA or non-form PDF?).");
            }

            // Prefer letting PDFBox generate appearances for you (default behavior in 3.x)
            // Avoid relying on NeedAppearances; readers differ in support.
            acroForm.setNeedAppearances(false);

            // ---- Fill typical fields ----
            // Text
            PDTextField name = (PDTextField) acroForm.getField("full_name");
            if (name != null) name.setValue("Sridhar Balasubramanian");

            // Multiline text
            PDTextField addr = (PDTextField) acroForm.getField("address");
            if (addr != null) addr.setValue("123 Mount Road\nChennai, TN 600002");

            // Checkbox (either check()/unCheck() or setValue(exportValue))
            PDCheckBox agree = (PDCheckBox) acroForm.getField("agree_terms");
            if (agree != null) agree.check();

            // Radio (use the *export value* configured in the form, not the caption)
            PDRadioButton gender = (PDRadioButton) acroForm.getField("gender");
            if (gender != null) gender.setValue("Male"); // ensure "Male" is a valid export value

            // Combo / Drop‑down (use one of the allowed values)
            PDComboBox country = (PDComboBox) acroForm.getField("country");
            if (country != null) country.setValue("IN"); // or "India" depending on values

            // Date or numeric fields are still text under the hood unless additional JS/formatting is present.
            PDTextField date = (PDTextField) acroForm.getField("dob");
            if (date != null) date.setValue("1979-01-31"); // ISO‑like text works reliably

            // Optional: Flatten to convert fields into static page content (non-editable)
            // Comment out if you want to keep the form fillable.
            // acroForm.flatten();

            doc.save(output);
        }
    }
