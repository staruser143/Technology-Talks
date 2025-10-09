import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;
import java.io.IOException;

public class FillAndFlattenPdfBox3 {
    public static void main(String[] args) throws IOException {
        File in = new File("FormTemplate_A4_pdfbox3.0.5.pdf");
        try (PDDocument doc = PDDocument.load(in)) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            setIfPresent(acroForm, "full_name", "Alice Example");
            setIfPresent(acroForm, "address", "221B Baker Street\nLondon");
            setIfPresent(acroForm, "accept_terms", "Yes");   // must match checkbox export value
            setIfPresent(acroForm, "gender", "Other");       // must match one radio export value
            setIfPresent(acroForm, "country", "United Kingdom");

            // Ensure appearances are generated with defaults
            acroForm.refreshAppearances();

            // Optional: flatten to make non-editable
            acroForm.flatten();

            doc.save("FilledAndFlattened_pdfbox3.0.5.pdf");
        }
    }

    private static void setIfPresent(PDAcroForm acroForm, String name, String value) throws IOException {
        PDField f = acroForm.getField(name);
        if (f != null) {
            f.setValue(value);
        }
    }
}
