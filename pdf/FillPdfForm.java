import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import shared.pdf.PdfDocumentUtils;
import shared.pdf.PdfFieldUtils;

import java.io.File;

public class FillPdfForm {
    public static void main(String[] args) throws Exception {
        File template = new File("template.pdf");
        File output   = new File("filled.pdf");

        try (PDDocument doc = PdfDocumentUtils.loadPdf(template)) {
            PDAcroForm acroForm = PdfDocumentUtils.requireAcroForm(doc);

            acroForm.setNeedAppearances(false);

            PdfFieldUtils.setIfPresent(acroForm, "full_name", "Sridhar Balasubramanian");
            PdfFieldUtils.setIfPresent(acroForm, "address", "123 Mount Road\nChennai, TN 600002");

            PDCheckBox agree = (PDCheckBox) acroForm.getField("agree_terms");
            if (agree != null) agree.check();

            PDRadioButton gender = (PDRadioButton) acroForm.getField("gender");
            if (gender != null) gender.setValue("Male");

            PDComboBox country = (PDComboBox) acroForm.getField("country");
            if (country != null) country.setValue("IN");

            PdfFieldUtils.setIfPresent(acroForm, "dob", "1979-01-31");

            doc.save(output);
        }
    }
}
