import shared.pdf.PdfFontUtils;

import java.io.File;

/**
 * Maps the /HeBo alias to Helvetica Bold in a PDF form's default resources.
 *
 * Now delegates to PdfFontUtils.repairFontAndSave() which consolidates
 * the logic previously duplicated between MapHeBoAlias.java and RepairDA.java.
 */
public class MapHeBoAlias {
    public static void main(String[] args) throws Exception {
        PdfFontUtils.repairFontAndSave(
            new File("template.pdf"),
            new File("template-fixed.pdf")
        );
    }
}
