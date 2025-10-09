import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateFormTemplatePdfBox3 {
    public static void main(String[] args) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            // A4 page
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // AcroForm
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = new PDAcroForm(doc);
            catalog.setAcroForm(acroForm);

            // Default font/resources + DA (avoid blank text in viewers)
            PDFont helv = PDType1Font.HELVETICA;
            PDResources dr = new PDResources();
            dr.put(COSName.getPDFName("Helv"), helv);
            acroForm.setDefaultResources(dr);
            acroForm.setDefaultAppearance("/Helv 10 Tf 0 g");
            acroForm.setNeedAppearances(false);

            // Static labels (simple, non-overwriting constructor is fine here)
            drawLabels(doc, page);

            // Fields
            PDTextField name = addTextField("full_name", page, acroForm, 140, 770, 300, 20, false);
            PDTextField address = addTextField("address", page, acroForm, 140, 730, 300, 48, true);

            PDCheckBox terms = addCheckBox("accept_terms", page, acroForm, 140, 690, 14, "Yes");

            PDRadioButton gender = addRadioGroup(
                    "gender", page, acroForm,
                    new String[]{"Male", "Female", "Other"},
                    new float[][]{{140, 650}, {210, 650}, {285, 650}},
                    12
            );

            PDComboBox country = addComboBox(
                    "country", page, acroForm,
                    140, 610, 200, 20,
                    Arrays.asList("India", "United States", "United Kingdom", "Singapore", "Australia")
            );

            // (Optional) prefill to verify appearances
            name.setValue("John Doe");
            address.setValue("1 Infinite Loop\nCupertino, CA");
            terms.setValue("Yes");            // must match export value
            gender.setValue("Female");        // must match one export value
            country.setValue("India");

            // refresh appearances (3.x)
            acroForm.refreshAppearances();

            doc.save("FormTemplate_A4_pdfbox3.0.5.pdf");
        }
    }

    private static void drawLabels(PDDocument doc, PDPage page) throws IOException {
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);

            cs.newLineAtOffset(40, 772);
            cs.showText("Full Name:");

            cs.newLineAtOffset(0, -40);
            cs.showText("Address:");

            cs.newLineAtOffset(0, -48);
            cs.showText("Accept Terms:");

            cs.newLineAtOffset(0, -40);
            cs.showText("Gender:");

            cs.newLineAtOffset(0, -40);
            cs.showText("Country:");

            cs.endText();
        }
    }

    private static PDTextField addTextField(
            String name, PDPage page, PDAcroForm acroForm,
            float x, float y, float w, float h, boolean multiline
    ) throws IOException {
        PDTextField field = new PDTextField(acroForm);
        field.setPartialName(name);
        field.setMultiline(multiline);

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y, w, h));
        widget.setPage(page);
        styleWidget(widget, new float[]{1f, 1f, 0.88f}); // light yellow
        page.getAnnotations().add(widget);

        field.setWidgets(Arrays.asList(widget));
        acroForm.getFields().add(field);
        return field;
    }

    private static PDCheckBox addCheckBox(
            String name, PDPage page, PDAcroForm acroForm,
            float x, float y, float size, String exportValue
    ) throws IOException {
        PDCheckBox cb = new PDCheckBox(acroForm);
        cb.setPartialName(name);
        cb.setExportValues(Arrays.asList(exportValue));

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y, size, size));
        widget.setPage(page);
        styleWidget(widget, new float[]{0.95f, 0.95f, 0.95f});
        page.getAnnotations().add(widget);

        cb.setWidgets(Arrays.asList(widget));
        acroForm.getFields().add(cb);
        return cb;
    }

    private static PDRadioButton addRadioGroup(
            String name, PDPage page, PDAcroForm acroForm,
            String[] options, float[][] positions, float size
    ) throws IOException {
        PDRadioButton rb = new PDRadioButton(acroForm);
        rb.setPartialName(name);
        rb.setExportValues(Arrays.asList(options));

        List<PDAnnotationWidget> widgets = new ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            PDAnnotationWidget w = new PDAnnotationWidget();
            w.setRectangle(new PDRectangle(positions[i][0], positions[i][1], size, size));
            w.setPage(page);
            styleWidget(w, new float[]{0.97f, 0.97f, 0.97f});
            page.getAnnotations().add(w);
            widgets.add(w);
        }
        rb.setWidgets(widgets);
        acroForm.getFields().add(rb);
        return rb;
    }

    private static PDComboBox addComboBox(
            String name, PDPage page, PDAcroForm acroForm,
            float x, float y, float w, float h,
            List<String> options
    ) throws IOException {
        PDComboBox combo = new PDComboBox(acroForm);
        combo.setPartialName(name);
        combo.setOptions(options);      // same list used for display/export
        combo.setEditable(false);

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(x, y, w, h));
        widget.setPage(page);
        styleWidget(widget, new float[]{1f, 1f, 1f});
        page.getAnnotations().add(widget);

        combo.setWidgets(Arrays.asList(widget));
        acroForm.getFields().add(combo);
        return combo;
    }

    private static void styleWidget(PDAnnotationWidget widget, float[] bgRGB) {
        PDAppearanceCharacteristicsDictionary acd =
                new PDAppearanceCharacteristicsDictionary(new COSDictionary());
        acd.setBorderColour(new PDColor(new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE));
        acd.setBackground(new PDColor(bgRGB, PDDeviceRGB.INSTANCE));
        widget.setAppearanceCharacteristics(acd);
    }
