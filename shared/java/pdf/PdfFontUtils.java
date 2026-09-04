package shared.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.cos.COSName;

import java.io.File;
import java.io.IOException;

/**
 * Shared utilities for PDF font registration and default appearance repair.
 *
 * Consolidates the nearly-identical logic from:
 *   pdf/RepairDA.java
 *   pdf/MapHeBoAlias.java
 *
 * Both files: load PDF, get AcroForm, ensure default resources exist,
 * register a Helvetica Bold font under /HeBo, set default appearance,
 * set NeedAppearances=false, and save.
 */
public final class PdfFontUtils {

    private PdfFontUtils() {}

    /**
     * Ensures the AcroForm has a default resources dictionary.
     * Creates one if it doesn't exist.
     */
    public static PDResources ensureDefaultResources(PDAcroForm acroForm) {
        PDResources dr = acroForm.getDefaultResources();
        if (dr == null) {
            dr = new PDResources();
            acroForm.setDefaultResources(dr);
        }
        return dr;
    }

    /**
     * Registers a font under a given alias in the AcroForm's default resources.
     */
    public static void registerFont(PDAcroForm acroForm, String alias, PDFont font) {
        PDResources dr = ensureDefaultResources(acroForm);
        dr.put(COSName.getPDFName(alias), font);
    }

    /**
     * Registers Helvetica Bold under /HeBo and sets a default appearance if missing.
     * This is the common pattern extracted from RepairDA.java and MapHeBoAlias.java.
     */
    public static void registerHelveticaBoldAsHeBo(PDAcroForm acroForm) {
        PDFont helvBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        registerFont(acroForm, "HeBo", helvBold);

        if (acroForm.getDefaultAppearance() == null || acroForm.getDefaultAppearance().isBlank()) {
            acroForm.setDefaultAppearance("/HeBo 8 Tf 0 g");
        }

        acroForm.setNeedAppearances(false);
    }

    /**
     * Full repair workflow: load PDF -> fix fonts -> save.
     * Replaces the entire main() in both RepairDA.java and MapHeBoAlias.java.
     */
    public static void repairFontAndSave(File inputPdf, File outputPdf) throws Exception {
        try (PDDocument doc = PdfDocumentUtils.loadPdf(inputPdf)) {
            PDAcroForm acroForm = PdfDocumentUtils.requireAcroForm(doc);
            registerHelveticaBoldAsHeBo(acroForm);
            doc.save(outputPdf);
        }
    }
}
