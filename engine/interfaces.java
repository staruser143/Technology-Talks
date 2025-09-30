public interface DocumentEngine {
    boolean supports(String engineType);
    RenderResult render(TemplateMetadata meta, RenderContext ctx) throws RenderException;
}

public interface Processor {
    String type(); // e.g., "pdf.watermark"
    byte[] apply(byte[] content, TemplateMetadata meta, RenderContext ctx) throws ProcessingException;
}

public interface OutputAdapter {
    String key();  // e.g., "email.acs" or "storage.azureblob"
    void deliver(byte[] content, OutputRequest req, TemplateMetadata meta) throws DeliveryException;
}

public interface Registry<T> {
    Optional<T> get(String keyOrType);
    void register(String keyOrType, T impl);
}

public final class RenderContext {
    public final Map<String,Object> data;
    public final Map<String,Object> runtime;   // correlationId, idempotencyKey, tenantId, etc.
    public final ResourceResolver resolver;    // loads templates/assets/fonts from Git/S3/Blob
    // ...
}
