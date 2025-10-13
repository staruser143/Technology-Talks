package com.example.pdfinspect;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController
public class InspectController {

    @PostMapping(
        path = "/inspect",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> inspect(@RequestPart("file") MultipartFile file) throws Exception {
        try (InputStream in = file.getInputStream(); PDDocument doc = Loader.loadPDF(in)) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("file", file.getOriginalFilename());

            Map<String, Object> formMeta = new LinkedHashMap<>();
            if (acroForm != null) {
                formMeta.put("needAppearances", Boolean.TRUE.equals(acroForm.getNeedAppearances()));
                formMeta.put("defaultAppearance", acroForm.getDefaultAppearance());
            } else {
                formMeta.put("needAppearances", null);
                formMeta.put("defaultAppearance", null);
            }
            result.put("acroForm", formMeta);

            if (acroForm == null) {
                result.put("fields", List.of());
                return result;
            }

            // Build page index map for widget page numbers
            Map<PDPage, Integer> pageIndex = new IdentityHashMap<>();
            int idx = 0;
            for (PDPage p : doc.getPages()) pageIndex.put(p, idx++);

            List<Map<String, Object>> fieldList = new ArrayList<>();
            for (PDField f : acroForm.getFieldTree()) {
                fieldList.add(toJson(f, pageIndex));
            }
            result.put("fields", fieldList);

            return result;
        }
    }

    private Map<String, Object> toJson(PDField f, Map<PDPage, Integer> pageIndex) throws Exception {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", f.getFullyQualifiedName());
        m.put("pdfType", f.getFieldType());   // Tx, Btn, Ch, Sig
        m.put("type", readableType(f));       // Text, Checkbox, Radio, ComboBox, ListBox, Signature
        m.put("readOnly", f.isReadOnly());
        m.put("required", f.isRequired());
        m.put("noExport", f.isNoExport());
        m.put("value", f.getValueAsString());

        if (f instanceof PDTextField t) {
            m.put("default", t.getDefaultValue());
            m.put("multiline", t.isMultiline());
            m.put("password", t.isPassword());
            if (f instanceof PDVariableText vt) {
                m.put("defaultAppearance", vt.getDefaultAppearance());
            }
        } else if (f instanceof PDChoice ch) {
            m.put("default", ch.getDefaultValue());

            List<String> display = orEmpty(ch.getOptionsDisplayValues());
            List<String> export  = ch.getOptionsExportValues(); // may be null
            List<Map<String, String>> options = new ArrayList<>();
            for (int i = 0; i < display.size(); i++) {
                String disp = display.get(i);
                String exp  = (export != null && i < export.size()) ? export.get(i) : disp;
                options.add(Map.of("display", disp, "export", exp));
            }
            m.put("options", options);

            if (f instanceof PDComboBox cb) {
                m.put("editable", cb.isEdit());
                m.put("multiSelect", cb.isMultiSelect());
            } else if (f instanceof PDListBox lb) {
                m.put("multiSelect", lb.isMultiSelect());
            }
        } else if (f instanceof PDCheckBox cb) {
            String onVal = cb.getOnValue();
            String cur   = cb.getValue();
            m.put("onValue", onVal);
            m.put("checked", onVal != null && onVal.equals(cur));
        } else if (f instanceof PDRadioButton rb) {
            m.put("choices", orEmpty(rb.getExportValues()));
            m.put("selected", rb.getValue());
        } else if (f instanceof PDSignatureField sig) {
            m.put("signed", sig.getSignature() != null);
        }

        // Widgets / locations
        List<PDAnnotationWidget> widgets = f.getWidgets();
        List<Map<String, Object>> widgetList = new ArrayList<>();
        if (widgets != null) {
            for (PDAnnotationWidget w : widgets) {
                Map<String, Object> wi = new LinkedHashMap<>();
                wi.put("page", w.getPage() != null ? pageIndex.get(w.getPage()) : null);
                var r = w.getRectangle();
                if (r != null) {
                    wi.put("rect", Map.of(
                        "llx", r.getLowerLeftX(),
                        "lly", r.getLowerLeftY(),
                        "urx", r.getUpperRightX(),
                        "ury", r.getUpperRightY()
                    ));
                } else {
                    wi.put("rect", null);
                }
                widgetList.add(wi);
            }
        }
        m.put("widgets", widgetList);

        return m;
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

    private static <T> List<T> orEmpty(List<T> l) { return l == null ? Collections.emptyList() : l; }
}
