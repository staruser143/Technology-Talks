TemplateMetadata meta = templateRegistry.get(templateKey, version);
validateAgainstSchema(meta.inputSchemaRef, job.data);

DocumentEngine engine = engineRegistry.get(meta.engine)
    .orElseThrow(() -> new IllegalStateException("Engine not registered: " + meta.engine));

RenderResult base = engine.render(meta, ctx);

byte[] processed = applyProcessors(meta.processors.post, base.bytes, meta, ctx);

for (OutputRequest o : job.outputs) {
    outputRegistry.get(o.adapter)
        .orElseThrow(() -> new IllegalArgumentException("Unknown output adapter: " + o.adapter))
        .deliver(processed, o, meta);
}
