import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import shared.pdf.PdfDocumentUtils;
import shared.pdf.PdfFieldUtils;

import java.io.File;
import java.io.IOException;

public class FillAndFlattenPdfBox3 {
    public static void main(String[] args) throws IOException {
        File in = new File("FormTemplate_A4_pdfbox3.0.5.pdf");
        try (PDDocument doc = PDDocument.load(in)) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            PdfFieldUtils.setIfPresent(acroForm, "full_name", "Alice Example");
            PdfFieldUtils.setIfPresent(acroForm, "address", "221B Baker Street\nLondon");
            PdfFieldUtils.setIfPresent(acroForm, "accept_terms", "Yes");
            PdfFieldUtils.setIfPresent(acroForm, "gender", "Other");
            PdfFieldUtils.setIfPresent(acroForm, "country", "United Kingdom");

            acroForm.refreshAppearances();
            acroForm.flatten();

            doc.save("FilledAndFlattened_pdfbox3.0.5.pdf");
        }
    }
}
