Excellent question! 🚀

You want to **externalize templates** — store HTML and PDF form templates **outside your Java project**, possibly in:

- A remote HTTP server (e.g., `https://templates.yourcdn.com/invoice.html`)
- A cloud storage bucket (S3, GCS, Azure Blob)
- A shared network file system (NFS, SMB)
- A database (as BLOBs)
- Git repository (via raw URLs)

This is **essential for production systems** — it allows:
- Non-developers (designers, legal, ops) to update templates without code deploys
- A/B testing of templates
- Multi-tenant template isolation
- Centralized template management

---

## ✅ GOAL

Enhance `TemplateMerger` to support **template sources from remote locations**, while keeping the API clean and secure.

---

# 🧩 DESIGN PRINCIPLE: **Template Source Abstraction**

Instead of hardcoding `String` (HTML) or `String path` (PDF), we’ll introduce a **`TemplateSource` interface** that can fetch content from **any location**.

---

## STEP 1: Define `TemplateSource` Interface

```java
// src/main/java/com/yourcompany/TemplateSource.java
package com.yourcompany;

import java.io.IOException;
import java.io.InputStream;

public interface TemplateSource {
    /**
     * Returns an InputStream of the template content.
     * Caller is responsible for closing the stream.
     */
    InputStream openStream() throws IOException;

    /**
     * Optional: Returns a human-readable identifier (for logging/caching)
     */
    String getId();
}
```

---

## STEP 2: Implement Common Template Sources

### A. 📁 Local File System

```java
// LocalFileTemplateSource.java
package com.yourcompany;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class LocalFileTemplateSource implements TemplateSource {
    private final Path filePath;

    public LocalFileTemplateSource(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(filePath.toFile());
    }

    @Override
    public String getId() {
        return "file://" + filePath;
    }
}
```

---

### B. 🌐 Remote HTTP(S) URL

```java
// HttpTemplateSource.java
package com.yourcompany;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpTemplateSource implements TemplateSource {
    private final String url;
    private final int timeoutMs;

    public HttpTemplateSource(String url) {
        this(url, 10_000);
    }

    public HttpTemplateSource(String url, int timeoutMs) {
        this.url = url;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public InputStream openStream() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "TemplateMerger/1.0");

        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP " + conn.getResponseCode() + " for " + url);
        }

        return conn.getInputStream();
    }

    @Override
    public String getId() {
        return url;
    }
}
```

> ✅ Supports CDN, GitHub raw URLs, S3 pre-signed URLs, etc.

---

### C. ☁️ AWS S3 (Optional)

```java
// S3TemplateSource.java
package com.yourcompany;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStream;

public class S3TemplateSource implements TemplateSource {
    private final S3Client s3;
    private final String bucket;
    private final String key;

    public S3TemplateSource(S3Client s3, String bucket, String key) {
        this.s3 = s3;
        this.bucket = bucket;
        this.key = key;
    }

    @Override
    public InputStream openStream() throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> response = s3.getObject(request);
        return response;
    }

    @Override
    public String getId() {
        return "s3://" + bucket + "/" + key;
    }
}
```

> Add AWS SDK dependency if needed.

---

## STEP 3: Enhance `TemplateEngine` to Use `TemplateSource`

Update the interface:

```java
// TemplateEngine.java
public interface TemplateEngine {
    byte[] merge(TemplateSource templateSource, Map<String, Object> data) throws Exception;
    String getOutputFormat();
}
```

### Update `HtmlTemplateEngine`

```java
// HtmlTemplateEngine.java (updated)
@Override
public byte[] merge(TemplateSource templateSource, Map<String, Object> data) throws Exception {
    String templateContent;
    try (InputStream is = templateSource.openStream()) {
        templateContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // Rest remains same...
    StringTemplateLoader loader = new StringTemplateLoader();
    String name = "template";
    loader.putTemplate(name, templateContent);
    cfg.setTemplateLoader(loader);

    Template template = cfg.getTemplate(name);
    StringWriter writer = new StringWriter();
    template.process(data, writer);
    return writer.toString().getBytes(StandardCharsets.UTF_8);
}
```

### Update `PdfFormTemplateEngine`

> ⚠️ **Important**: PDFBox requires **random access** to PDF files (it seeks), so `InputStream` is **not sufficient** for PDF forms.

✅ **Solution**: For PDF forms, **download to temp file** if source is remote.

```java
// PdfFormTemplateEngine.java (updated)
@Override
public byte[] merge(TemplateSource templateSource, Map<String, Object> data) throws Exception {
    File pdfFile;
    boolean deleteOnExit = false;

    // If it's a local file, use directly
    if (templateSource instanceof LocalFileTemplateSource local) {
        pdfFile = local.getFilePath().toFile();
    } else {
        // Download remote template to temp file
        try (InputStream is = templateSource.openStream()) {
            pdfFile = File.createTempFile("template_", ".pdf");
            deleteOnExit = true;
            Files.copy(is, pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    try (PDDocument document = PDDocument.load(pdfFile)) {
        // ... rest of filling logic ...

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    } finally {
        if (deleteOnExit) {
            pdfFile.deleteOnExit();
        }
    }
}
```

> 💡 This ensures PDFBox works correctly with remote PDFs.

---

## STEP 4: Enhance `TemplateMerger` API

```java
// TemplateMerger.java (updated)
public class TemplateMerger {
    private final MappingResolver mappingResolver = new MappingResolver();

    public byte[] merge(
            TemplateSource templateSource,
            Map<String, Object> sourceData,
            String mappingSpecJson,
            OutputFormat outputFormat,
            TemplateType templateType) throws Exception {

        Map<String, Object> resolvedData = mappingResolver.resolve(sourceData, mappingSpecJson);

        TemplateEngine engine = getEngine(templateType);
        return engine.merge(templateSource, resolvedData);
    }

    // Convenience methods for common sources
    public byte[] mergeFromUrl(
            String templateUrl,
            Map<String, Object> sourceData,
            String mappingSpecJson,
            OutputFormat outputFormat,
            TemplateType templateType) throws Exception {
        return merge(new HttpTemplateSource(templateUrl), sourceData, mappingSpecJson, outputFormat, templateType);
    }

    public byte[] mergeFromFile(
            Path templatePath,
            Map<String, Object> sourceData,
            String mappingSpecJson,
            OutputFormat outputFormat,
            TemplateType templateType) throws Exception {
        return merge(new LocalFileTemplateSource(templatePath), sourceData, mappingSpecJson, outputFormat, templateType);
    }
}
```

---

## STEP 5: Update REST API Controller (for Microservice)

```java
// TemplateMergeController.java (updated)
@PostMapping("/merge")
public ResponseEntity<byte[]> mergeTemplate(@RequestBody TemplateMergeRequest request) {
    try {
        TemplateSource templateSource;
        if (request.getTemplateUrl() != null) {
            templateSource = new HttpTemplateSource(request.getTemplateUrl());
        } else if (request.getTemplateFilePath() != null) {
            templateSource = new LocalFileTemplateSource(Paths.get(request.getTemplateFilePath()));
        } else if (request.getTemplateContent() != null) {
            // For inline HTML (backward compatibility)
            templateSource = new InlineStringTemplateSource(request.getTemplateContent());
        } else {
            throw new IllegalArgumentException("No template source provided");
        }

        // ... rest same ...
        byte[] result = templateMerger.merge(
            templateSource,
            dataMap,
            request.getMappingSpecJson(),
            outputFormat,
            templateType
        );

        // ... return response ...
    }
}
```

Add `InlineStringTemplateSource` for backward compatibility:

```java
public class InlineStringTemplateSource implements TemplateSource {
    private final String content;
    public InlineStringTemplateSource(String content) { this.content = content; }
    @Override public InputStream openStream() { return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)); }
    @Override public String getId() { return "inline"; }
}
```

---

## STEP 6: Update Node.js SDK (Optional)

In your SDK, allow `templateUrl`:

```ts
// In TemplateMergerClient.ts
async mergePdfForm(options: {
  templateUrl: string;  // ← NEW!
   Record<string, any>;
  mapping: FieldMapping[];
}): Promise<Buffer> {
  const request = {
    templateType: 'PDF_FORM',
    templateUrl: options.templateUrl, // ← send URL to Java service
    mappingSpecJson: JSON.stringify({ mappings: options.mapping }),
     options.data
  };
  // ... POST to /api/template/merge
}
```

> ✅ Now Node.js can pass a URL like `"https://cdn.example.com/templates/order-form.pdf"`

---

## 🔐 SECURITY CONSIDERATIONS

When loading remote templates, **always validate URLs** to prevent SSRF:

```java
// In HttpTemplateSource or controller
private static final Set<String> ALLOWED_HOSTS = Set.of(
    "templates.yourcompany.com",
    "your-bucket.s3.amazonaws.com"
);

public HttpTemplateSource(String url) {
    try {
        String host = new URL(url).getHost();
        if (!ALLOWED_HOSTS.contains(host)) {
            throw new SecurityException("Template URL not allowed: " + host);
        }
        this.url = url;
    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid URL", e);
    }
}
```

Or use a **template registry**:

```json
// Instead of raw URL, use template ID
{
  "templateId": "invoice-v3",
   { ... },
  "mappingSpecJson": "..."
}
```

Then resolve `invoice-v3` → `https://cdn/.../invoice-v3.html` via a secure lookup table.

---

## 📁 EXAMPLE TEMPLATE LOCATIONS

| Type | Example URL |
|------|-------------|
| GitHub | `https://raw.githubusercontent.com/yourorg/templates/main/invoice.html` |
| S3 | `https://your-bucket.s3.amazonaws.com/templates/order-form.pdf` |
| CDN | `https://templates.yourcdn.com/v2/certificate.pdf` |
| Internal HTTP | `http://template-service/templates/contract.html` |

---

## ✅ FINAL ARCHITECTURE

```
TemplateMerger
│
├── Accepts TemplateSource (abstraction)
│
├── TemplateSource implementations:
│   ├── LocalFileTemplateSource
│   ├── HttpTemplateSource          ← remote HTML/PDF
│   ├── S3TemplateSource            ← cloud storage
│   └── InlineStringTemplateSource  ← backward compat
│
├── TemplateEngine uses TemplateSource.openStream()
│   ├── HtmlTemplateEngine → reads stream → FreeMarker
│   └── PdfFormTemplateEngine → downloads to temp file → PDFBox
│
└── REST API / SDK passes URL or path → resolved to TemplateSource
```

---

## 🚀 BENEFITS

- ✅ **Decouple templates from code** → no redeploy needed to update design
- ✅ **Support any storage backend** (HTTP, S3, DB, etc.)
- ✅ **Secure** with host allow-listing
- ✅ **Backward compatible** with inline templates
- ✅ **Works for both HTML and PDF forms**

---

## 🧪 EXAMPLE USAGE

```java
// Load HTML template from GitHub
TemplateSource htmlSource = new HttpTemplateSource(
    "https://raw.githubusercontent.com/acme/templates/main/invoice.html"
);

byte[] pdf = merger.merge(
    htmlSource,
    dbData,
    mappingSpec,
    OutputFormat.PDF,
    TemplateType.HTML_TO_PDF
);

// Load PDF form from S3
TemplateSource pdfSource = new S3TemplateSource(s3Client, "my-templates", "order-form.pdf");
byte[] filledPdf = merger.merge(pdfSource, dbData, mappingSpec, OutputFormat.PDF, TemplateType.PDF_FORM);
```

---

## ✅ CONCLUSION

> ✅ **Yes — you can fully externalize templates to remote locations** by:
>
> 1. Introducing a `TemplateSource` abstraction
> 2. Implementing sources for HTTP, S3, local files, etc.
> 3. Updating engines to use streams (with temp file for PDF forms)
> 4. Adding security (host allow-listing)
> 5. Keeping backward compatibility
>
> This makes your system **production-ready, flexible, and maintainable**.

---

## 🛠️ Want Me to Generate the Full Project?

I can provide:

- ✅ Complete Maven project with all sources
- ✅ Security-hardened URL validator
- ✅ Template registry pattern
- ✅ Caching layer (to avoid re-downloading)
- ✅ Unit tests

Just say: **“Generate the full externalized template project”** — and I’ll package it for you! 📦✨