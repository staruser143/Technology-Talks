import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

import java.io.File;
import java.util.List;

public class FormInspector {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java FormInspector <pdf-path>");
            return;
        }
        File pdf = new File(args[0]);

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            if (acroForm == null) {
                System.out.println("No AcroForm in this PDF (it may be non-form or XFA).");
                return;
            }

            System.out.println("=== Fields (" + acroForm.getFields().size() + " top-level) ===");
            for (PDField field : acroForm.getFieldTree()) {
                dumpField(field);
                System.out.println();
            }
        }
    }

    private static void dumpField(PDField f) throws Exception {
        String name = f.getFullyQualifiedName();
        String fieldType = f.getFieldType(); // "Tx", "Btn", "Ch", "Sig" etc.

        System.out.println("Name      : " + name);
        System.out.println("Type      : " + readableType(f) + " (" + fieldType + ")");
        System.out.println("ReadOnly  : " + f.isReadOnly() + "   Required: " + f.isRequired() + "   NoExport: " + f.isNoExport());

        // Current / default values (where applicable)
        System.out.println("Value     : " + safe(f.getValueAsString()));
        if (f instanceof PDTextField) {
            PDTextField t = (PDTextField) f;
            System.out.println("Default   : " + safe(t.getDefaultValue()));
            System.out.println("Multiline : " + t.isMultiline() + "   Password: " + t.isPassword());
        } else if (f instanceof PDChoice) {
            PDChoice ch = (PDChoice) f;
            System.out.println("Default   : " + safe(ch.getDefaultValue()));
            // Allowed values (display vs export)
            List<String> display = ch.getOptionsDisplayValues();
            List<String> export  = ch.getOptionsExportValues(); // may be same size as display; may be null if not set
            System.out.println("Options   :");
            if (export != null && !export.isEmpty()) {
                for (int i = 0; i < display.size(); i++) {
                    String disp = display.get(i);
                    String exp  = (i < export.size() ? export.get(i) : disp);
                    System.out.println("  - display=\"" + disp + "\", export=\"" + exp + "\"");
                }
            } else {
                for (String disp : display) {
                    System.out.println("  - \"" + disp + "\"");
                }
            }
            if (f instanceof PDComboBox) {
                PDComboBox cb = (PDComboBox) f;
                System.out.println("Editable  : " + cb.isEdit());
                System.out.println("MultiSel  : " + cb.isMultiSelect());
            } else if (f instanceof PDListBox) {
                PDListBox lb = (PDListBox) f;
                System.out.println("MultiSel  : " + lb.isMultiSelect());
            }
        } else if (f instanceof PDCheckBox) {
            PDCheckBox cb = (PDCheckBox) f;
            String onVal = cb.getOnValue(); // the "export" value when checked
            String cur   = cb.getValue();
            System.out.println("OnValue   : " + onVal);
            System.out.println("Checked   : " + (onVal != null && onVal.equals(cur)));
        } else if (f instanceof PDRadioButton) {
            PDRadioButton rb = (PDRadioButton) f;
            List<String> exportValues = rb.getExportValues(); // allowed selections in the group
            System.out.println("Choices   : " + exportValues);
            System.out.println("Selected  : " + rb.getValue());
        } else if (f instanceof PDSignatureField) {
            PDSignatureField sig = (PDSignatureField) f;
            System.out.println("Signed?   : " + (sig.getSignature() != null));
        }

        // Widget/location info (page and rect), useful to visually identify the field
        List<PDAnnotationWidget> widgets = f.getWidgets();
        if (widgets != null && !widgets.isEmpty()) {
            for (int i = 0; i < widgets.size(); i++) {
                PDAnnotationWidget w = widgets.get(i);
                String pageIndex = (w.getPage() != null && w.getPage().getCOSObject() != null)
                        ? Integer.toString(w.getPage().getCOSObject().getInt("StructParents", -1)) // fallback if index unknown
                        : "?";
                var r = w.getRectangle();
                System.out.printf("Widget[%d]  : page=%s rect=[%.2f, %.2f, %.2f, %.2f]%n",
                        i, pageIndex, r.getLowerLeftX(), r.getLowerLeftY(), r.getUpperRightX(), r.getUpperRightY());
            }
        }
    }

    private static String readableType(PDField f) {
        if (f instanceof PDTextField) return "Text";
        if (f instanceof PDCheckBox) return "Checkbox";
        if (f instanceof PDRadioButton) return "Radio";
        if (f instanceof PDComboBox) return "ComboBox";
        if (f instanceof PDListBox) return "ListBox";
        if (f instanceof PDSignatureField) return "Signature";
        return f.getClass().getSimpleName();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
