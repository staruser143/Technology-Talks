package shared.pdf;

import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Shared utilities for PDF form field inspection and manipulation.
 *
 * Consolidates:
 *   - readableType() duplicated identically in FormInspector, InspectController, FormToJson
 *   - setIfPresent() pattern from FillAndFlattenPdfBox3, FillPdfForm, HybridPdfFormFiller
 *   - nullSafeList/orEmpty helpers from FormToJson and InspectController
 */
public final class PdfFieldUtils {

    private PdfFieldUtils() {}

    /**
     * Maps a PDField subclass to a human-readable type name.
     *
     * Previously duplicated identically in:
     *   pdf/FormInspector.java (lines 107-115)
     *   pdf/InspectController.java (lines 137-145)
     *   pdf/FormToJson.java (lines 155-163)
     */
    public static String readableType(PDField f) {
        if (f instanceof PDTextField)      return "Text";
        if (f instanceof PDCheckBox)       return "Checkbox";
        if (f instanceof PDRadioButton)    return "Radio";
        if (f instanceof PDComboBox)       return "ComboBox";
        if (f instanceof PDListBox)        return "ListBox";
        if (f instanceof PDSignatureField) return "Signature";
        return f.getClass().getSimpleName();
    }

    /**
     * Sets a field value if the field exists in the form.
     *
     * Previously duplicated in:
     *   pdf/code/FillAndFlattenPdfBox3.java (lines 30-35)
     *   pdf/FillPdfForm.java (multiple null-check-then-setValue blocks)
     */
    public static void setIfPresent(PDAcroForm acroForm, String fieldName, String value) throws IOException {
        PDField field = acroForm.getField(fieldName);
        if (field != null) {
            field.setValue(value);
        }
    }

    /**
     * Returns the list or an empty list if null.
     *
     * Previously duplicated as:
     *   nullSafeList() in pdf/FormToJson.java (line 205-207)
     *   orEmpty() in pdf/InspectController.java (line 147)
     */
    public static <T> List<T> nullSafeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public static String safeString(String s) {
        return s == null ? "" : s;
    }
}
