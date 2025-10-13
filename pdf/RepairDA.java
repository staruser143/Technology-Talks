import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.cos.COSName;

import java.io.File;

public class RepairDA {
    public static void main(String[] args) throws Exception {
        try (PDDocument doc = Loader.loadPDF(new File("template.pdf"))) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            if (acroForm == null) throw new IllegalStateException("No AcroForm in template");

            PDResources dr = acroForm.getDefaultResources();
            if (dr == null) { dr = new PDResources(); acroForm.setDefaultResources(dr); }

            // Register Helvetica Bold as /HeBo
            dr.put(COSName.getPDFName("HeBo"), PDType1Font.HELVETICA_BOLD);

            // If the form-wide DA is empty or broken, set one explicitly (font + size + black color)
            if (acroForm.getDefaultAppearance() == null || acroForm.getDefaultAppearance().isBlank()) {
                acroForm.setDefaultAppearance("/HeBo 8 Tf 0 g");
            }

            // Let PDFBox generate appearances (preferred in 3.x)
            acroForm.setNeedAppearances(false);

            doc.save("template-fixed.pdf");
        }
    }
}
