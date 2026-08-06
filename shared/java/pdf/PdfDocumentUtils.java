package shared.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Shared utilities for loading PDF documents and accessing AcroForm data.
 *
 * Consolidates the repeated pattern of:
 *   Loader.loadPDF(...) -> getDocumentCatalog() -> getAcroForm() -> null check
 *
 * Previously duplicated across:
 *   pdf/FillPdfForm.java, pdf/FormInspector.java, pdf/InspectController.java,
 *   pdf/FormToJson.java, pdf/MapHeBoAlias.java, pdf/RepairDA.java,
 *   pdf/code/FillAndFlattenPdfBox3.java, try/HybridPdfFormFiller.java,
 *   try/AdvancedConfigDrivenPdfMapper.java, engine/AcroformEngine.java
 */
public final class PdfDocumentUtils {

    private PdfDocumentUtils() {}

    public static PDDocument loadPdf(File file) throws IOException {
        return Loader.loadPDF(file);
    }

    public static PDDocument loadPdf(InputStream inputStream) throws IOException {
        return Loader.loadPDF(inputStream);
    }

    public static PDAcroForm getAcroForm(PDDocument doc) {
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        return catalog.getAcroForm();
    }

    public static PDAcroForm requireAcroForm(PDDocument doc) {
        PDAcroForm acroForm = getAcroForm(doc);
        if (acroForm == null) {
            throw new IllegalStateException("No AcroForm in this PDF (it may be non-form or XFA).");
        }
        return acroForm;
    }

    /**
     * Builds a page-to-index map for stable widget page number references.
     *
     * Previously duplicated in:
     *   pdf/InspectController.java (lines 48-51)
     *   pdf/FormToJson.java (lines 48-52)
     */
    public static Map<PDPage, Integer> buildPageIndex(PDDocument doc) {
        Map<PDPage, Integer> pageIndex = new IdentityHashMap<>();
        int idx = 0;
        for (PDPage p : doc.getPages()) {
            pageIndex.put(p, idx++);
        }
        return pageIndex;
    }

    public static byte[] saveToBytes(PDDocument doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        return out.toByteArray();
    }
}
