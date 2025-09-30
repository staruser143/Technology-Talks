public class FreemarkerEngine implements DocumentEngine {
    private final Configuration cfg;              // FreeMarker Configuration
    private final HtmlToPdfRenderer renderer;     // Strategy interface

    public FreemarkerEngine(Configuration cfg, HtmlToPdfRenderer renderer) {
        this.cfg = cfg;
        this.renderer = renderer;
    }

    @Override public boolean supports(String t) { return "html-freemarker".equalsIgnoreCase(t); }

    @Override
    public RenderResult render(TemplateMetadata meta, RenderContext ctx) {
        Template tmpl = cfg.getTemplate(ctx.resolver.resolveToReader(meta.resources.templateUri));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(tmpl, ctx.data); // pseudo

        byte[] output;
        String mime = meta.output.mimeType;
        if ("application/pdf".equalsIgnoreCase(mime)) {
            output = renderer.toPdf(html, ctx.resolver, meta.resources.assets);
        } else {
            output = html.getBytes(StandardCharsets.UTF_8);
        }
        return new RenderResult(output, mime);
    }
