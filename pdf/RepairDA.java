import shared.pdf.PdfFontUtils;

import java.io.File;

/**
 * Repairs the default appearance (DA) of a PDF form by registering
 * Helvetica Bold under /HeBo.
 *
 * Now delegates to PdfFontUtils.repairFontAndSave() which consolidates
 * the logic previously duplicated between RepairDA.java and MapHeBoAlias.java.
 */
public class RepairDA {
    public static void main(String[] args) throws Exception {
        PdfFontUtils.repairFontAndSave(
            new File("template.pdf"),
            new File("template-fixed.pdf")
        );
    }
}
