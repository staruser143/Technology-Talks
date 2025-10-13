import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FormToJson {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java FormToJson <pdf-path> [out.json]");
            return;
        }
        File pdf = new File(args[0]);
        String out = (args.length >= 2 ? args[1] : null);

        String json = inspectToJson(pdf);
        if (out == null) {
            System.out.println(json);
        } else {
            Files.writeString(Path.of(out), json, StandardCharsets.UTF_8);
            System.out.println("Wrote " + out);
        }
    }

    public static String inspectToJson(File pdf) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"file\": ").append(jsonString(pdf.getName())).append(",\n");
            if (acroForm == null) {
                sb.append("  \"acroForm\": null,\n");
                sb.append("  \"fields\": []\n");
                sb.append("}\n");
                return sb.toString();
            }

            // Build page->index map for stable widget page numbers
            Map<PDPage, Integer> pageIndex = new IdentityHashMap<>();
            int idx = 0;
            for (PDPage p : doc.getPages()) {
                pageIndex.put(p, idx++);
            }

            sb.append("  \"acroForm\": {\n");
            sb.append("    \"needAppearances\": ").append(acroForm.getNeedAppearances() != null && acroForm.getNeedAppearances()).append(",\n");
            sb.append("    \"defaultAppearance\": ").append(jsonString(acroForm.getDefaultAppearance())).append("\n");
            sb.append("  },\n");

            // Fields
            List<PDField> fields = new ArrayList<>();
            for (PDField f : acroForm.getFieldTree()) fields.add(f);

            sb.append("  \"fields\": [\n");
            for (int i = 0; i < fields.size(); i++) {
                PDField f = fields.get(i);
                appendFieldJson(sb, f, pageIndex);
                if (i < fields.size() - 1) sb.append(",\n"); else sb.append("\n");
            }
            sb.append("  ]\n");
            sb.append("}\n");
            return sb.toString();
        }
    }

    private static void appendFieldJson(StringBuilder sb, PDField f, Map<PDPage, Integer> pageIndex) throws Exception {
        sb.append("    {\n");
        put(sb, "name", f.getFullyQualifiedName(), true);
        put(sb, "pdfType", f.getFieldType(), true);               // Tx, Btn, Ch, Sig
        put(sb, "type", readableType(f), true);                   // Text, Checkbox, Radio, ComboBox, ListBox, Signature
        put(sb, "readOnly", f.isReadOnly(), true);
        put(sb, "required", f.isRequired(), true);
        put(sb, "noExport", f.isNoExport(), true);
        put(sb, "value", f.getValueAsString(), true);

        // Defaults & type-specific properties
        if (f instanceof PDTextField t) {
            put(sb, "default", t.getDefaultValue(), true);
            put(sb, "multiline", t.isMultiline(), true);
            put(sb, "password", t.isPassword(), true);
            if (f instanceof PDVariableText vt) {
                put(sb, "defaultAppearance", vt.getDefaultAppearance(), true);
            }
        } else if (f instanceof PDChoice ch) {
            put(sb, "default", ch.getDefaultValue(), true);

            // Options (display vs export)
            List<String> display = nullSafeList(ch.getOptionsDisplayValues());
            List<String> export  = ch.getOptionsExportValues(); // may be null
            if (export == null || export.isEmpty()) {
                // Only display values present
                putOptionsArray(sb, display, null, true);
            } else {
                putOptionsArray(sb, display, export, true);
            }

            if (f instanceof PDComboBox cb) {
                put(sb, "editable", cb.isEdit(), true);
                put(sb, "multiSelect", cb.isMultiSelect(), true);
            } else if (f instanceof PDListBox lb) {
                put(sb, "multiSelect", lb.isMultiSelect(), true);
            }
        } else if (f instanceof PDCheckBox cb) {
            String onVal = cb.getOnValue();
            String cur = cb.getValue();
            put(sb, "onValue", onVal, true);
            put(sb, "checked", onVal != null && onVal.equals(cur), true);
        } else if (f instanceof PDRadioButton rb) {
            List<String> exportValues = nullSafeList(rb.getExportValues());
            putArray(sb, "choices", exportValues, true);
            put(sb, "selected", rb.getValue(), true);
        } else if (f instanceof PDSignatureField sig) {
            put(sb, "signed", sig.getSignature() != null, true);
        }

        // Widgets (page + rect)
        List<PDAnnotationWidget> widgets = f.getWidgets();
        if (widgets == null) widgets = Collections.emptyList();
        sb.append("      \"widgets\": [\n");
        for (int i = 0; i < widgets.size(); i++) {
            PDAnnotationWidget w = widgets.get(i);
            var r = w.getRectangle();
            Integer pageIdx = (w.getPage() != null ? pageIndex.get(w.getPage()) : null);

            sb.append("        {");
            sb.append("\"page\": ").append(pageIdx == null ? "null" : pageIdx.toString()).append(", ");
            if (r != null) {
                sb.append("\"rect\": {");
                sb.append("\"llx\": ").append(fmt(r.getLowerLeftX())).append(", ");
                sb.append("\"lly\": ").append(fmt(r.getLowerLeftY())).append(", ");
                sb.append("\"urx\": ").append(fmt(r.getUpperRightX())).append(", ");
                sb.append("\"ury\": ").append(fmt(r.getUpperRightY())).append("}");
            } else {
                sb.append("\"rect\": null");
            }
            sb.append("}");
            if (i < widgets.size() - 1) sb.append(",\n"); else sb.append("\n");
        }
        sb.append("      ]\n");

        sb.append("    }");
    }

    // ---------- Helpers ----------

    private static String readableType(PDField f) {
        if (f instanceof PDTextField)       return "Text";
        if (f instanceof PDCheckBox)        return "Checkbox";
        if (f instanceof PDRadioButton)     return "Radio";
        if (f instanceof PDComboBox)        return "ComboBox";
        if (f instanceof PDListBox)         return "ListBox";
        if (f instanceof PDSignatureField)  return "Signature";
        return f.getClass().getSimpleName();
    }

    private static void put(StringBuilder sb, String key, String val, boolean comma) {
        sb.append("      ").append(jsonString(key)).append(": ").append(jsonString(val));
        if (comma) sb.append(","); sb.append("\n");
    }

    private static void put(StringBuilder sb, String key, boolean val, boolean comma) {
        sb.append("      ").append(jsonString(key)).append(": ").append(val);
        if (comma) sb.append(","); sb.append("\n");
    }

    private static void putArray(StringBuilder sb, String key, List<String> arr, boolean comma) {
        sb.append("      ").append(jsonString(key)).append(": ");
        if (arr == null) {
            sb.append("null");
        } else {
            sb.append("[");
            for (int i = 0; i < arr.size(); i++) {
                sb.append(jsonString(arr.get(i)));
                if (i < arr.size() - 1) sb.append(", ");
            }
            sb.append("]");
        }
        if (comma) sb.append(","); sb.append("\n");
    }

    private static void putOptionsArray(StringBuilder sb, List<String> display, List<String> export, boolean comma) {
        sb.append("      \"options\": [");
        if (display != null) {
            for (int i = 0; i < display.size(); i++) {
                String disp = display.get(i);
                String exp = (export != null && i < export.size()) ? export.get(i) : null;
                sb.append("{\"display\": ").append(jsonString(disp))
                  .append(", \"export\": ").append(jsonString(exp == null ? disp : exp)).append("}");
                if (i < display.size() - 1) sb.append(", ");
            }
        }
        sb.append("]");
        if (comma) sb.append(","); sb.append("\n");
    }

    private static List<String> nullSafeList(List<String> list) {
        return (list == null) ? Collections.emptyList() : list;
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        StringBuilder out = new StringBuilder();
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b");  break;
                case '\f': out.append("\\f");  break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
        return out.toString();
    }

    private static String fmt(float v) {
        // Compact float for JSON
        if (Math.abs(v - Math.rint(v)) < 1e-6) {
            return Integer.toString((int) Math.rint(v));
        }
        return Float.toString(v);
    }
}
