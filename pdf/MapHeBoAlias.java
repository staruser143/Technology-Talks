import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.cos.COSName;

import java.io.File;

public class MapHeBoAlias {
    public static void main(String[] args) throws Exception {
        try (PDDocument doc = Loader.loadPDF(new File("template.pdf"))) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            if (acroForm == null) throw new IllegalStateException("No AcroForm in template");

            PDResources dr = acroForm.getDefaultResources();
            if (dr == null) { dr = new PDResources(); acroForm.setDefaultResources(dr); }

            // Create Helvetica Bold as a Standard14 font (PDFBox 3.x way)
            PDFont helvBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            // Register it under the exact alias used by your DA: /HeBo
            dr.put(COSName.getPDFName("HeBo"), helvBold);

            // Optional: set a default appearance (font + size + color) for the form
            if (acroForm.getDefaultAppearance() == null || acroForm.getDefaultAppearance().isBlank()) {
                acroForm.setDefaultAppearance("/HeBo 8 Tf 0 g"); // 8pt, black
            }

            acroForm.setNeedAppearances(false); // let PDFBox generate appearances
            doc.save("template-fixed.pdf");
        }
    }
}
