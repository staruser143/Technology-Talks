public class AcroformEngine implements DocumentEngine {
    @Override public boolean supports(String t) { return "acroform".equalsIgnoreCase(t); }

    @Override
    public RenderResult render(TemplateMetadata meta, RenderContext ctx) {
        PDDocument doc = PDDocument.load(ctx.resolver.open(meta.resources.formUri));
        PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

        Map<String, Object> data = ctx.data;
        meta.acroform.fieldMappings.forEach((path, fieldName) -> {
            Object value = JsonPath.read(data, "$." + path);  // or your resolver
            PDField field = form.getField(fieldName);
            if (field != null && value != null) {
                applyValue(field, value);
            }
        });

        if (meta.acroform.flatten) form.flatten();

        byte[] pdf = saveToBytes(doc);
        return new RenderResult(pdf, "application/pdf");
    }
}
